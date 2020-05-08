 package com.twormobile.mytravelasia.http;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 /**
  * Retrieves feeds from the REST webservice.
  *
  * @author avendael
  */
 public class FeedHttpClient {
     private static final String BASE_URL = "http://www.mytravel-asia.com/";
 
    private static AsyncHttpClient client = new AsyncHttpClient();
 
     public static void getFeeds(AsyncHttpResponseHandler responseHandler) {
         RequestParams params = new RequestParams();
 
         params.put("country_name", "Philippines");
         client.get(BASE_URL + "feed.json", params, responseHandler);
     }
 
     public static Gson getFeedGsonParser() {
         return new GsonBuilder()
                 .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                 .excludeFieldsWithoutExposeAnnotation().create();
     }
 }
