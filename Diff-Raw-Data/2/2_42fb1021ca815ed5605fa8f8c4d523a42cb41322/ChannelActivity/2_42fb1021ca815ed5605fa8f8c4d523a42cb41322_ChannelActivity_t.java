 package ru.isu.drevin.hw5;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Destaren
  * Date: 03.05.13
  * Time: 19:35
  * To change this template use File | Settings | File Templates.
  */
 public class ChannelActivity extends Activity {
 
     DBHelper dbHelper;
     ListView listView;
     GetPostsTask postsTask;
     String channelID;
 
     ArrayList<Map<String, String>> news;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.channel);
 
         dbHelper = new DBHelper(this);
         listView = (ListView) findViewById(R.id.postList);
 
         String url = getIntent().getStringExtra("url");
         channelID = getIntent().getStringExtra("channelID");
 
         postsTask = (GetPostsTask) getLastNonConfigurationInstance();
         if(postsTask == null) {
             postsTask = new GetPostsTask();
             postsTask.execute(url);
         } else if (postsTask.getStatus() == AsyncTask.Status.FINISHED) {
             setListViewListener2();
         }
         postsTask.link(this);
     }
 
     public Object onRetainNonConfigurationInstance() {
         postsTask.unLink();
         return postsTask;
     }
 
     public void setListViewListener(GetPostsTask task) {
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, task.newsTitles);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Map<String,String> item = news.get(i);
                 String url = item.get("link");
                 Intent intent = new Intent("ru.isu.drevin.hw5.News", Uri.parse(postsTask.newsLinks[i]));
                 startActivity(intent);
                 //Toast.makeText(getApplicationContext(), "_"+l+"_", Toast.LENGTH_SHORT).show();
             }
         });
     }
 
     public void setListViewListener2() {
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         Cursor c = db.query("rssNews", null, "channel_id = ?", new String[]{channelID}, null, null, "pubDate DESC");
         if(c.getCount() > 0 && c.moveToFirst()) {
             news = new ArrayList<Map<String, String>>(c.getCount());
 
             int linkColPos = c.getColumnIndex("key");
             int titleColPos = c.getColumnIndex("title");
 
             do {
                 Map<String, String> map = new HashMap<String, String>();
                 map.put("link",c.getString(linkColPos));
                 map.put("title",c.getString(titleColPos));
                 news.add(map);
             } while(c.moveToNext());
 
             String[] from = {"title"};
             int[] to = {android.R.id.text1};
 
             SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), news, android.R.layout.simple_list_item_1, from, to);
 
             listView.setAdapter(adapter);
             listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     Map<String,String> item = news.get(i);
                     String url = item.get("link");
                    Intent intent = new Intent("ru.isu.drevin.hw5.News", Uri.parse(url));
                     startActivity(intent);
                     //Toast.makeText(getApplicationContext(), "_"+l+"_", Toast.LENGTH_SHORT).show();
                 }
             });
         } else {
             Toast.makeText(getApplicationContext(), "No news available", Toast.LENGTH_SHORT).show();
         }
     }
 
     static class GetPostsTask extends AsyncTask<String, Void, Void> {
 
         ChannelActivity activity = null;
         String[] newsTitles, newsLinks, pubDates;
         boolean error = false;
 
         public void link(ChannelActivity act) {
             activity = act;
         }
 
         public void unLink() {
             activity = null;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             if(activity != null) {
             }
         }
 
         @Override
         protected Void doInBackground(String... urls) {
             String result = "";
             try {
                 if(!downloadRSS(urls[0])) {
                     error = true;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 error = true;
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
 
             if(error) {
                 Toast.makeText(activity.getApplicationContext(), "Connection error", Toast.LENGTH_SHORT).show();
                 activity.setListViewListener2();
                 return;
             }
             if(activity != null) {
                 Date lastNewsDate = activity.getLastNewsDate();
                 for(int i = 0; i < newsTitles.length; i++) {
                     Date date = new Date(pubDates[i]);
                     if(date.after(lastNewsDate)) {
                         ContentValues cv = new ContentValues();
                         cv.put("channel_id", Integer.parseInt(activity.channelID));
                         cv.put("key", newsLinks[i]);
                         cv.put("title", newsTitles[i]);
                         cv.put("pubDate", date.getTime() / 1000);
                         long inserted = activity.dbHelper.getWritableDatabase().insert("rssNews",null,cv);
                     }
                 }
                 activity.setListViewListener2();
             }
         }
 
         private InputStream OpenHttpGETConnection(String urlString) throws IOException {
             /*
             InputStream in;
             try {
                 HttpClient httpClient = new DefaultHttpClient();
                 HttpResponse httpResponse = httpClient.execute(new HttpGet(urlString));
                 in = httpResponse.getEntity().getContent();
             } catch (Exception e) {
                 throw new IOException("Error connecting");
             }
             return in;
             //*/
             ///*
             InputStream in = null;
             int response = -1;
 
             URL url = new URL(urlString);
             URLConnection conn = url.openConnection();
 
             if(!(conn instanceof HttpURLConnection))
                 throw new IOException("Not an HTTP connection");
 
             try {
                 HttpURLConnection httpConn = (HttpURLConnection) conn;
                 httpConn.setAllowUserInteraction(false);
                 httpConn.setInstanceFollowRedirects(true);
                 httpConn.setRequestMethod("GET");
                 httpConn.connect();
 
                 response = httpConn.getResponseCode();
                 if(response == HttpURLConnection.HTTP_OK) {
                     in = httpConn.getInputStream();
                 }
             } catch (Exception ex) {
                 throw new IOException("Error connecting");
             }
 
             return  in;
             //*/
         }
 
         private boolean downloadRSS(String urlString) {
             InputStream in = null;
             try {
                 in = OpenHttpGETConnection(urlString);
                 if(in == null) {
                     if(activity != null)
                         Toast.makeText(activity.getApplicationContext(), "Wrong connection params", Toast.LENGTH_SHORT).show();
                     return false;
                 }
             } catch (Exception e) {
                 if(activity != null)
                     Toast.makeText(activity.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                 return false;
             }
 
             Document document = null;
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder documentBuilder;
 
             try {
                 documentBuilder = documentBuilderFactory.newDocumentBuilder();
                 document = documentBuilder.parse(in);
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             document.getDocumentElement().normalize();
 
             NodeList rssItems = document.getElementsByTagName("item");
             newsLinks = new String[rssItems.getLength()];
             newsTitles = new String[rssItems.getLength()];
             pubDates = new String[rssItems.getLength()];
 
 
             Date lastNewsDate = activity.getLastNewsDate();
             SQLiteDatabase db = activity.dbHelper.getWritableDatabase();
 
             for(int i = 0; i < rssItems.getLength(); i++) {
                 Node rssItem = rssItems.item(i);
                 Element news = (Element) rssItem;
                 String title = news.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
                 String link = news.getElementsByTagName("link").item(0).getFirstChild().getNodeValue();
                 String pubDate = news.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
 
                 newsTitles[i] = title;
                 newsLinks[i] = link;
                 pubDates[i] = pubDate;
                 //String pubDate = news.getElementsByTagName("pubDate").item(0).getNodeValue();
             }
 
             return true;
         }
     }
 
     public Date getLastNewsDate() {
         Date lastDate = new Date(0);
         SQLiteDatabase db = dbHelper.getReadableDatabase();
 
         try {
             Cursor c = db.query("rssNews", new String[]{"pubDate"}, null, null, null, null, "pubDate DESC");
             if(c.getCount() > 0 && c.moveToFirst()) {
                 int dateColPos = c.getColumnIndex("pubDate");
                 long date = c.getInt(dateColPos);
                 lastDate = new Date(date*1000);
             }
         } catch (Exception e) {
             return new Date(0);
         }
 
         return lastDate;
     }
 }
