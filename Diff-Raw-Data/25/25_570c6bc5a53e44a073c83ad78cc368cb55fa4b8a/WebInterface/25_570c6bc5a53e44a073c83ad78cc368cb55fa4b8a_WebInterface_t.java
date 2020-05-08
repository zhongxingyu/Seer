 //Ronaldo Barnes
 //Java 1 week 3
 //January 2013
 //Full Sail University
 
 package com.rbarnes.lib;
 
 
 import java.io.BufferedInputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class WebInterface {
 	static Boolean _connected = false;
 	static String _connectionType = "Unavailable";
 	
 	//Get the type of network connection
 	public static String getConnectionType(Context context){
 		netInfo(context);
 		return _connectionType;
 	}
 	//Check to see if there is an Internet connection
 	public static Boolean getConnectionStatus(Context context) {
 		netInfo(context);
 		return _connected;
 	}
 	//Retrieve network info.
 	private static void netInfo(Context context){
 		ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo nInfo = cManager.getActiveNetworkInfo();
 		if(nInfo != null){
 			
 			if(nInfo.isConnected()){
 				_connectionType = nInfo.getTypeName();
 				_connected = true;
 				
 			}
 		}
 	}
 	public static String getUrlStringResponse(URL url){
 		String response = "";
 		
 		try{
 			URLConnection connection = url.openConnection();
 			BufferedInputStream bin = new BufferedInputStream(connection.getInputStream());
 			
 			byte[] contentBytes = new byte[1024];
 			int bytesRead = 0;
 			StringBuffer responseBuffer = new StringBuffer();
 			
			while((bytesRead = bin.read(contentBytes)) != -1){
 				
 				response = new String(contentBytes,0,bytesRead);
				
 				responseBuffer.append(response);
 				
 			}
 			return responseBuffer.toString();
 		}catch (Exception e){
 			Log.e("URL RESPONSE ERROR","getURLStringResponse");
 		}
 		
 		return response;
 	}
 }
 
