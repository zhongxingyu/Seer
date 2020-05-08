package com.derpicons.gshelf;
 
 import java.util.ArrayList;
 
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.util.Log;
 
 
 public class Network {
 	
 
 	ArrayList<Game> getGames(String name)
 	{
 		//should use string.format
 		String url = "http://thegamesdb.net/api/GetGame.php?name=" + name;
 		
 		//create the http get from the url
 		HttpGet getMethod = new HttpGet(url);
 		
 		
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		
 		try {
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			String responseBody = client.execute(getMethod, responseHandler);
 			Log.i("RESPONSE BODY", responseBody);
 			
 			return parseResponse(responseBody);
 		} 
 		catch (Throwable t){
 			//can use t as a string to pass to logcat to display error
 			Log.i("ERROR", t.toString());
 			
 			return null;
 		}
 		
 		
 		
 	}
 	
 	ArrayList<Game> parseResponse(String response)
 	{
 		return null;
 	}
 
 }
