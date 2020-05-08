 package com.tryrosberry.ololostfilm.logic.api;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 import com.tryrosberry.ololostfilm.logic.storage.ConstantStorage;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class LostFilmRestClient {
 
     private static final String BASE_URL = ConstantStorage.BASE_URL;
 
     private static AsyncHttpClient client = new AsyncHttpClient();
 
     public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
         client.get(getAbsoluteUrl(url), params, responseHandler);
     }
 
     public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
         client.post(getAbsoluteUrl(url), params, responseHandler);
     }
 
     private static String getAbsoluteUrl(String relativeUrl) {
         return BASE_URL + relativeUrl;
     }
 
     public static String buildRequestGetUrl(String relativeUrl,HashMap<String,String> map){
         StringBuilder url = new StringBuilder();
         url.append(relativeUrl+"?");
         try {
             Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
             while (entries.hasNext()) {
                 Map.Entry<String, String> entry = entries.next();
                 url.append("&"+entry.getKey());
                 url.append("="+URLEncoder.encode(entry.getValue(), "utf-8"));
             }
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
         return url.toString();
     }
 
 }
