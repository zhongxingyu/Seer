 package com.ytripper.net;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.ytripper.util.StringUtil;
 import com.ytripper.video.YoutubePlaylistObject;
 import com.ytripper.video.YoutubeVideoObject;
 import com.ytripper.video.YoutubeVideoStreamStore;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * This example demonstrates the use of the {@link ResponseHandler} to simplify
  * the process of processing the HTTP response and releasing associated resources.
  */
 public class YoutubeConnection {
     protected static ObjectMapper mapper = new ObjectMapper();
     
     protected static void printHashMap(HashMap<String, Object> map, int level) {
         for (Map.Entry<String, Object> entry : map.entrySet()) {
             if (entry.getValue() instanceof HashMap) {
                 for(int i = 0; i < level; i++) {
                     System.out.print("\t");
                 }
                 System.out.println(entry.getKey() + " => ");
                 printHashMap((HashMap<String, Object>)entry.getValue(), level + 1);
             }
             else if (entry.getValue() instanceof ArrayList) {
                 for(int i = 0; i < level; i++) {
                     System.out.print("\t");
                 }
                 System.out.println(entry.getKey() + " => ");
                 printArrayList((ArrayList<Object>)entry.getValue(), level + 1);
             }
             else {
                 for(int i = 0; i < level; i++) {
                     System.out.print("\t");
                 }
                 System.out.println(entry.getKey() + " => " + entry.getValue());
                 
             }
         }
     }
     
     protected static void printArrayList(ArrayList<Object> array, int level) {
         for (Object element: array) {
             if (element instanceof HashMap) {
                 printHashMap((HashMap<String, Object>)element, level + 1);
             }
             else if (element instanceof ArrayList) {
                 printArrayList((ArrayList<Object>)element, level + 1);
             }
             else {
                 for(int i = 0; i < level; i++) {
                     System.out.print("\t");
                 }
                 System.out.println(element);
                 
             }
         }        
     }
     
     protected static void printHashMapS(HashMap<String, String> map, int level) {
         for (Map.Entry<String, String> entry : map.entrySet()) {
             for(int i = 0; i < level; i++) {
                 System.out.print("\t");
             }
             System.out.println(entry.getKey() + " => " + entry.getValue());
         }
     }
     
     protected static HashMap<String, String> parseFmtString(String fmtString) {
         String[] fmtElements = fmtString.split("&");
         HashMap<String, String> fmtMap = new HashMap<String, String>();
         
         for (String fmtElement : fmtElements) {
             String[] fmtPair = fmtElement.split("=", 2);
             
             if (fmtPair.length == 2) {
                 fmtMap.put(fmtPair[0], fmtPair[1]);
             }
         }
         
         return fmtMap;
     }
     
     public static YoutubePlaylistObject getYoutubePlaylistObject(String playlistId) {
         YoutubePlaylistObject playlist = new YoutubePlaylistObject();
         String playlistUrl = "http://gdata.youtube.com/feeds/api/playlists/" + playlistId + "?v=2&alt=json";
         
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse hr;
         httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
         
         try {
             HttpGet httpget = new HttpGet(playlistUrl);
             System.out.println("Executing request " + httpget.getURI());
             hr = httpclient.execute(httpget);
         } catch (Exception e) {
             System.out.println("EXCEPTION: " + e.getMessage());
             httpclient.getConnectionManager().shutdown();
             return null;
         }
         
         System.out.println("CONTENT-TYPE: " + hr.getFirstHeader("Content-Type").getValue());
         System.out.println("STATUS:       " + hr.getStatusLine());
         
         String jsonData;
         HashMap<String, Object> playlistMap;
         
         //Slurp entire JSON string into memory and make a map out of it.
         try {
             jsonData = StringUtil.slurp(hr.getEntity().getContent());
             playlistMap = mapper.readValue(jsonData, HashMap.class);
         } catch (Exception e) {
             System.out.println("EXCEPTION: " + e.getMessage());
             return null;
         }
         
         //Get a hashmap of the base object
         HashMap<String, Object> feed = (HashMap)playlistMap.get("feed");
 
         //Get title and set playlist title to it.
         HashMap<String, String> titleObject = (HashMap)feed.get("title");
         String title = titleObject.get("$t");
         playlist.setTitle(title);
 
         //Get a list of video entries and add them to the playlist.
         ArrayList<Object> videos = (ArrayList)feed.get("entry");
         for (Object video : videos) {
             HashMap<String, Object> videoMap = (HashMap)video;
             HashMap<String, String> contentMap = (HashMap)videoMap.get("content");
             
            String src = contentMap.get("src");
            src = src.substring(src.lastIndexOf("/") + 1, src.indexOf("?"));
            src = "http://www.youtube.com/watch?v=" + src;
            
            YoutubeVideoObject youtubeVideo = getYoutubeVideoObject(src);
            playlist.addYoutubeVideoObject(youtubeVideo);
         }
         
         httpclient.getConnectionManager().shutdown();
                 
         return playlist;
     }
 
     public static YoutubeVideoObject getYoutubeVideoObject(String videoUrl) {
         YoutubeVideoObject youtubeVideo = null;
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse hr;
         httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
         
         try {
             HttpGet httpget = new HttpGet(videoUrl);
             System.out.println("Executing request " + httpget.getURI());
             hr = httpclient.execute(httpget);
         } catch (Exception e) {
             System.out.println("EXCEPTION: " + e.getMessage());
             httpclient.getConnectionManager().shutdown();
             return null;
         }
         
         System.out.println("CONTENT-TYPE: " + hr.getFirstHeader("Content-Type").getValue());
         System.out.println("STATUS:       " + hr.getStatusLine());
 
         if (!hr.getFirstHeader("Content-Type").getValue().matches("text/html.*")) {
             return null;
         }
         
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(hr.getEntity().getContent()));
             String line;
 
             while ((line = in.readLine()) != null) {
                 if (line.matches("(.*)\"url_encoded_fmt_stream_map\":(.*)")) {
                     line = line.substring(line.indexOf("=") + 1).trim();
 
                     HashMap<String, Object> jsonData = mapper.readValue(line, HashMap.class);
                     printHashMap(jsonData, 0);
                     youtubeVideo = new YoutubeVideoObject(jsonData);
 
                     String fmtString = ((HashMap<String,String>)jsonData.get("args")).get("url_encoded_fmt_stream_map");
                     String[] urlStrings = fmtString.split(",");
 
                     for(String urlString : urlStrings) {
                         System.out.println("URL_STRING: " + urlString);
                         HashMap<String, String> fmtMap = parseFmtString(urlString);
                         printHashMapS(fmtMap, 1);
                         YoutubeVideoStream stream = new YoutubeVideoStream(fmtMap);
                         youtubeVideo.addStream(stream);
                     }
                 }
             }
         } catch (Exception e) {
             System.out.println("EXCEPTION: " + e.getMessage());
             return null;
         }
         
         httpclient.getConnectionManager().shutdown();
         return youtubeVideo;
      }
 }
