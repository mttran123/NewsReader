package com.example.gohasu.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int id;
    String url;
    String title;
    static ArrayList<String> urlList = new ArrayList<>();
    static ArrayList<String> titleList = new ArrayList<>();
    ListView listView;

    SQLiteDatabase myDatabase;

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            try {
                JSONObject jsonObject = new JSONObject(s);

                if(!jsonObject.isNull("url") && !jsonObject.isNull("title")) {

                    url = jsonObject.getString("url");
                    title = jsonObject.getString("title");
                    urlList.add(url);
                    titleList.add(title);

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, titleList);

                    listView.setAdapter(arrayAdapter);


                }

            } catch (Exception e) {

                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < 7; i++) {
                        id = (int) array.get(i);

                        DownloadTask newTask = new DownloadTask();
                        newTask.execute("https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }

//            String sql = "INSERT INTO articles(articleId, title) VALUES (?,?)";
//            SQLiteStatement statement = myDatabase.compileStatement(sql);
//            statement.bindString(1, id+"");
//            statement.bindString(2, title);
//            statement.execute();
//
//

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDatabase = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR)");


        listView = findViewById(R.id.listView);

        DownloadTask task = new DownloadTask();
        task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                intent.putExtra("newsId", i);
                startActivity(intent);
            }
        });

    }
}
