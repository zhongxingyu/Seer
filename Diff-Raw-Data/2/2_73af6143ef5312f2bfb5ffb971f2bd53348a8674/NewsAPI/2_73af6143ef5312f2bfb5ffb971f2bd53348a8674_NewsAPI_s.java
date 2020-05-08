 package com.taiwan.news.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.taiwan.news.entity.Category;
 import com.taiwan.news.entity.News;
 import com.taiwan.news.entity.NewsPicture;
 
 public class NewsAPI {
     public final static int     APPLE     = 1;
     public final static int     FREEDOM   = 2;
     public final static int     UNION     = 3;
     public final static int     CHINATIME = 4;
     public final static int     ECONOMY   = 5;
     final static String         HOST      = "http://106.187.102.146:8000";
     public static final String  TAG       = "NEWS_API";
     public static final boolean DEBUG     = true;
     static String[]             source    = { "蘋果日報", "自由時報", "聯合報", "中時電子報", "經濟日報" };
 
     public static ArrayList<Category> getSourceCategory(int source) {
         return Category.getCategory(source);
     }
 
     public static ArrayList<News> getPromotionNews(int source) {
         ArrayList<News> news = new ArrayList();
         String message = getMessageFromServer("GET", "/api/v1/news/promotion.json?source_id=" + source, null);
         if (message == null) {
             return null;
         } else {
             try {
                 JSONArray newsArray;
                 newsArray = new JSONArray(message.toString());
                 for (int i = 0; i < newsArray.length(); i++) {
                     int id = newsArray.getJSONObject(i).getInt("id");
                     String title = newsArray.getJSONObject(i).getString("title");
                     int category_id = newsArray.getJSONObject(i).getInt("category_id");
 
                     ArrayList<NewsPicture> pics = new ArrayList<NewsPicture>();
                     JSONArray picArray = newsArray.getJSONObject(i).getJSONArray("pics");
                     for (int j = 0; j < picArray.length(); j++) {
                         String intro = picArray.getJSONObject(j).getString("description");
                         String link = picArray.getJSONObject(j).getString("link");
                         NewsPicture pic = new NewsPicture(link, intro);
                         pics.add(pic);
                     }
 
                     News n = new News(id, "", pics, "", category_id, new Date(), Category.getCategoryName(category_id), title);
                     news.add(n);
                 }
 
             } catch (JSONException e) {
                 e.printStackTrace();
                 return null;
             }
         }
         return news;
     }
 
     public static ArrayList<News> getCateroyNews(int source, int category, int page) {
         ArrayList<News> news = new ArrayList();
         String message = getMessageFromServer("GET", "/api/v1/news.json?category_id=" + category + "&page=" + page, null);
         if (message == null) {
             return null;
         } else {
             try {
                 JSONArray newsArray;
                 newsArray = new JSONArray(message.toString());
                 for (int i = 0; i < newsArray.length(); i++) {
                     int id = newsArray.getJSONObject(i).getInt("id");
                     String title = newsArray.getJSONObject(i).getString("title");
                     String release = newsArray.getJSONObject(i).getString("release_time");
                     DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                     Date release_time = createFormatter.parse(release);
 
                     ArrayList<NewsPicture> pics = new ArrayList<NewsPicture>();
                     JSONObject node = newsArray.getJSONObject(i);
                     if (!node.isNull("pic_link")) {
                         String link = node.getString("pic_link");
                         NewsPicture pic = new NewsPicture(link, "");
                         pics.add(pic);
                     }
                     News n = new News(id, "", pics, "", 0, release_time, "", title);
                     news.add(n);
                 }
 
             } catch (JSONException e) {
                 e.printStackTrace();
                 return null;
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return news;
     }
 
     public static News getNewsDetail(int newsId) {
         News n = null;
         String message = getMessageFromServer("GET", "/api/v1/news/" + newsId + ".json", null);
         if (message == null) {
             return null;
         } else {
             try {
                 JSONObject newsObject;
                 newsObject = new JSONObject(message.toString());
                 int id = newsObject.getInt("id");
                 String content = newsObject.getString("content");
                 String title = newsObject.getString("title");
                 int source_id = newsObject.getInt("source_id");
                 int category_id = newsObject.getInt("category_id");
                 String release = newsObject.getString("release_time");
                 DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                 Date release_time = createFormatter.parse(release);
 
                 ArrayList<NewsPicture> pics = new ArrayList<NewsPicture>();
                 JSONArray picArray = newsObject.getJSONArray("pics");
                 for (int i = 0; i < picArray.length(); i++) {
                     String intro = picArray.getJSONObject(i).getString("description");
                     String link = picArray.getJSONObject(i).getString("link");
                     NewsPicture pic = new NewsPicture(link, intro);
                     pics.add(pic);
                 }
 
                n = new News(id, source[source_id], pics, content, category_id, release_time, Category.getCategoryName(category_id), title);
 
             } catch (JSONException e) {
 
                 e.printStackTrace();
                 return null;
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 return null;
             }
         }
         return n;
     }
 
     public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
         URL url;
         try {
             url = new URL(HOST + apiPath);
             if (DEBUG)
                 Log.d(TAG, "URL: " + url);
 
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod(requestMethod);
 
             connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
             if (requestMethod.equalsIgnoreCase("POST"))
                 connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.connect();
 
             if (requestMethod.equalsIgnoreCase("POST")) {
                 OutputStream outputStream;
 
                 outputStream = connection.getOutputStream();
                 if (DEBUG)
                     Log.d("post message", json.toString());
 
                 outputStream.write(json.toString().getBytes());
                 outputStream.flush();
                 outputStream.close();
             }
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             StringBuilder lines = new StringBuilder();
             ;
             String tempStr;
 
             while ((tempStr = reader.readLine()) != null) {
                 lines = lines.append(tempStr);
             }
             if (DEBUG)
                 Log.d("MOVIE_API", lines.toString());
 
             reader.close();
             connection.disconnect();
 
             return lines.toString();
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
