 package com.r0adkll.sparc.pillalarm.server;
 
import android.net.Uri;

 import com.r0adkll.deadskunk.network.AsyncHttpClient;
 import com.r0adkll.deadskunk.network.JsonHttpResponseHandler;
 import com.r0adkll.deadskunk.network.RequestParams;
 import com.r0adkll.deadskunk.utils.Utils;
 
import org.json.JSONObject;

import java.net.URI;

 /**
  * Created by r0adkll on 8/24/13.
  */
 public class APIClient {
     private static final String TAG = "API_CLIENT";
 
     /***********************************
      * Server Addresses
      */
 
 	/*
 	 * This is the BASE url for the API server, the endpoints are all
 	 * concatenated to this url.
 	 */
     public static final String BASE = "http://ec2-50-16-178-223.compute-1.amazonaws.com:7070/search/";
 
 
     /***********************************
      * Variables
      */
 
 	/*
 	 * The Asynchronous Http Request Client for making all
 	 * network calls to the Nimbus Server
 	 */
     public static final AsyncHttpClient _client = new AsyncHttpClient();
 
     /***********************************
      * Helper Methods
      */
 
     /**
      * Combine the specified target script with the API base
      * url to get the fully formed request URL
      * @param target		the target endpoint
      * @return				the full request URL
      */
     public static String getApiUrl(String target){
         return BASE.concat(target);
     }
 
 
     /***********************************
      * Client Methods
      * -GET,POST,DEL, etc...
      */
 
     /**
      * Make a /GET request to the nimbus API server
      * @param target		the target endpoint
      * @param params		the request params
      * @param handler		the async response handler
      */
     public static void getRequest(String target, RequestParams params, JsonHttpResponseHandler handler){
         // Get Fully Formed URL
         String fullURL = getApiUrl(target);
 
         Utils.log(TAG, "Get Request: " + fullURL);
 
        // Pre-emptively cache URI Parsing
        try{
            URI.create(fullURL);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
            handler.onFailure(e, new JSONObject());
            return;
        }

         // Make get request
         if(params != null){
             _client.get(fullURL, params, handler);
         }else
             _client.get(fullURL, handler);
     }
 
     /**
      * Make a /POST request to the nimbus API server with nothing but
      * request parameters
      * @param target		the target endpoint
      * @param params		the request params
      * @param handler		the async response handler
      */
     public static void postRequest(String target, RequestParams params, JsonHttpResponseHandler handler){
         // Get Fully Formed URL
         String fullURL = getApiUrl(target);
 
         // Make Post request
         if(params != null){
             // Post Request
             _client.post(fullURL, params, handler);
         }else
             _client.post(fullURL, handler);
 
         // Log
         Utils.log(TAG, "Posting Server Request!");
     }
 
     /***********************************
      * Inner Classes
      *
      */
 
     /**
      * This is just a static ENUM class of sorts
      * for easy access to the endpoint string constants
      *
      * @author drew.heavner
      */
     public static class Targets{
         /*
          * Server Target endpoints
          */
 
 
     }
 }
