 package com.rogoapp;
 
 /* 					**ServerCleint class**
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
 
 import org.json.JSONObject;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class ServerClient{
 	private boolean status;
 	private JSONObject lastResponse;
 	private ServerClientAsync requestAsync;
 	
 	public ServerClient(){
 		status = false;
 		lastResponse = null;
 	}
 	
 	
 	public JSONObject genericPostRequest(String request, List<NameValuePair> nameValuePairs) {
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
 	   
 	    
 	    try {
 	        
 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 	        this.requestAsync = new ServerClientAsync();
 	        this.requestAsync.execute(httppost);
 	        
 	        while(!this.requestAsync.isFinished()){} //This is horrible and will kill the UI thread. DON'T USE THIS
 			
 			JSONObject jObj = this.requestAsync.getResult();
 			
 			return jObj;
 	    }
 	    catch (IOException e) {
 	    	System.err.print(e);
 	    }
 	    return null;
 	}  
 	
 	private boolean checkSuccess(){
 		//DO NOT USE THIS METHOD
 		//This method is a helper method for genericPostRequest(..)
 		String statusStr = null;
 		try{
 			statusStr = lastResponse.getString("status");
 		} catch (JSONException e){
 			
 		} 
 		if(statusStr.equals("success")){
 			status = true;
 			return true;
 		}
 		else{
 			status = false;
 			return false;
 		}
 	}
 	
 	
 	public boolean getStatus(){
 		return status;
 	}
 	
 	public JSONObject getLastResponse(){
 		return lastResponse;
 	}
 	
 	
 	
 	
 	public JSONObject register(String username, String email, String saltedPassword){
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		nameValuePairs.add(new BasicNameValuePair("username", username));
 		nameValuePairs.add(new BasicNameValuePair("email", email));
 		nameValuePairs.add(new BasicNameValuePair("password", saltedPassword));
 		JSONObject jObj = genericPostRequest("register.json", nameValuePairs);
 		
 		return jObj;
 	}
 	
 	//returns true if there is a connected network or if a network is being connected
 		public static boolean isNetworkAvailable() {
 			//gives the application's context
			Context context = RandomFunTime.showContext();
 			
 			//magical connectivity genie
 			ConnectivityManager connectivityManager 
 		          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 		}
 	
 	
 }
