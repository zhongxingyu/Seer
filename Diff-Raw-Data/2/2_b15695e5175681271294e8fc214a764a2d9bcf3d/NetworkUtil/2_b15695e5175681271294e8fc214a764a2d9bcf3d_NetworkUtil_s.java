 package com.music.fmv.utils;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.text.TextUtils;
 import com.music.fmv.tasks.threads.IDownloadListener;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 8/19/13
  * Time: 11:20 AM
  */
 public final class NetworkUtil {
     public enum Method {
         POST, GET
     }
 
     public static boolean isNetworkAvailable(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo info = cm.getActiveNetworkInfo();
         return info != null && info.isConnected();
     }
 
     public static String doGet(String urlRequest, Map<String, String> params) throws Exception {
         return doRequest(generateUrl(urlRequest, params), Method.GET, null);
     }
 
     public static String doPost(String urlRequest, String data) throws Exception {
         return doRequest(urlRequest, Method.POST, data);
     }
 
    public static String doRequest(String urlRequest, Method method, Stringz data) throws Exception {
         URL url = new URL(urlRequest);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setConnectTimeout(5000);
         connection.setRequestMethod(method.name());
         connection.setDoInput(true);
         if (method == Method.POST) {
             connection.setDoOutput(true);
         }
         connection.setRequestProperty("Content-Type", "application/json");
         connection.addRequestProperty("Accept", "application/json");
 
         //Send data to service
         if (data != null && method == Method.POST) {
             BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
             outputStream.write(data.getBytes());
             outputStream.flush();
             outputStream.close();
         }
 
         //read response
 
         if (connection.getResponseCode() != 200) return null;
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         StringBuilder responseBuilder = new StringBuilder();
         String s = null;
         while ((s = reader.readLine()) != null) {
             responseBuilder.append(s);
         }
         reader.close();
         return responseBuilder.toString();
     }
 
     //returns a valid url from Map
     public static String generateUrl(String baseUrl, Map<String, String> params) {
         if (params == null || params.size() == 0) return baseUrl;
 
         Iterator<String> keys = params.keySet().iterator();
         StringBuilder urlBuilder = new StringBuilder();
         urlBuilder.append(baseUrl);
         int counter = 0;
 
         while (keys.hasNext()) {
             String key = keys.next();
             urlBuilder.append(counter++ == 0 ? "?" : "&").append(key).append("=").append(encodeString(params.get(key)));
         }
 
         return urlBuilder.toString();
     }
 
     public static String encodeString(String s) {
         if (TextUtils.isEmpty(s)) return "";
         try {
             return URLEncoder.encode(s, "utf-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return s;
     }
 
     public static void downloadFile(File f, String songUrl, IDownloadListener listener) throws Exception {
         URL url = new URL(songUrl);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setConnectTimeout(5000);
         connection.setRequestMethod("GET");
         connection.setDoInput(true);
         InputStream inStream = connection.getInputStream();
         FileOutputStream outStream = new FileOutputStream(f);
 
         Integer fileLength = connection.getContentLength();
         try {
             byte data[] = new byte[16384];
 
             int lastPercentNotify = -1, curPercent;
             int count;
             int total = 0;
 
             while ((count = inStream.read(data, 0, data.length)) != -1) {
                 total += count;
                 outStream.write(data, 0, count);
                 curPercent = (total * 100) / fileLength;
 
                 if (curPercent != lastPercentNotify && curPercent % 10 == 0 && listener != null) {
                     listener.onDownload(f.getName(), curPercent, 100);
                     lastPercentNotify = curPercent;
                 }
             }
         } finally {
             inStream.close();
             outStream.close();
         }
     }
 }
