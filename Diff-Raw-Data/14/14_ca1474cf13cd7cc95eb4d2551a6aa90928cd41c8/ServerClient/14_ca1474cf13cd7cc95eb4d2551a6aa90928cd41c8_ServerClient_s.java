 package com.rogoapp;
 
 /* 					**ServerClient class**
  * Class designed to handle all client/server communications
  * Processes are invoked and data requested with HTTP Post Requests
  * To make request, use method genericPostRequest(..)
  * For the first arg, input request type into "String request"
  * For the second arg, input a list of NameValuePairs (more info in method)
  * 
  * To see if the last request was successful, use getStatus()
  * 
  * The JSON Object retrieved from the server is returned with genericPostRequest(..) 
  * It is also saved as lastResponse
  * To use the JSON Object again, use getLastResponse()
  * 
  * 				**Created by Joey Siracusa for Rogo**
  * 
  * THE FOLLOWING LIBRARIES ARE NEEDED TO USE THE SERVERLCIENT CLASS:
 import java.util.List;
 import java.util.ArrayList;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 import org.json.JSONException;
  */
 
 
 import com.rogoapp.auth.*;
 
 
 
 import android.content.Context;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ClientProtocolException;
 
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONObject;
 import org.json.JSONException;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class ServerClient{
 	private static JSONObject lastResponse;
 	private static boolean isFinished;
 	private static String status;
 	
 	
 	public ServerClient(){
 		lastResponse = null;
 		isFinished = false;
 		status = null;
 	}
 	
 	
 	public static JSONObject genericPostRequest(String request, List<NameValuePair> nameValuePairs, Context context) {
 	    // Takes a request and a list of NameValuePairs for an http post request
 		// Other classes must make nameValuePairs list
 		// They will need org.apache.http.NameValuePair
 		// and java.util.ArrayList
 		// and java.util.List
 		// Example of list:
 		// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		// nameValuePairs.add(new BasicNameValuePair("id", "12345"));
 		// Returns a JSON object
 	    HttpClient httpclient = new DefaultHttpClient();
 	    HttpPost httppost = new HttpPost("http://api.rogoapp.com/request/" + request);
 	    
 	    if(!(request.equals("register") || request.equals("login"))){
 	    	AccountAuthenticator aa = new AccountAuthenticator(context);
 	    	String newSession = aa.changeSession();
	    	nameValuePairs.add(new BasicNameValuePair("session", newSession));
 	    }
 	    
 	    
 	    try {
 
 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 	        
 	        // fetch data in background thread
 	        ServerClientThread scThread = new ServerClientThread(httppost);
 	        scThread.start();
 	        try {
 				scThread.join();
 			} catch (InterruptedException e) {
 				
 				System.err.println("IN SERVERCLIENT: "+ e);
 			}
 	        
 	        System.out.println("IN SERVERCLIENT: status = " + status);
 	        
 	        
 			return ServerClient.lastResponse;
 	    }
 	    catch (IOException e) {
 	    	System.err.print(e);
 	    }
 	    
 	    return null;
 	}  
 	
 	public JSONObject getLastResponse(){
 		return lastResponse;
 	}
 	
 	public void reset(){
 		ServerClient.isFinished = false;
 		ServerClient.status = null;
 		ServerClient.lastResponse = null;
 	}
 	
 	public void setLastResponse(JSONObject jObj){
 		ServerClient.lastResponse = jObj;
 		ServerClient.isFinished = true;
 		if(jObj != null){
 			try{
 				status = jObj.getString("status");
 			}catch(JSONException e){
 				System.out.println("IN SERVERCLIENT: in setLastResponse: " + e);
 			}
 		}
 		
 	}
 
 	//returns true if there is a connected network or if a network is being connected
 		public static boolean isNetworkAvailable() {
 			//gives the application's context
 			Context context = MainScreenActivity.showContext();
 			
 			//magical connectivity genie
 			ConnectivityManager connectivityManager 
 		          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 		}
 	
 	
 }
