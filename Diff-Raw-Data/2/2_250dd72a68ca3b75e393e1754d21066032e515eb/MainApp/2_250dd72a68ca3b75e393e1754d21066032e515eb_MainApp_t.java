 package com.patchingzone.energyvampire;
 
 import java.io.IOException;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.codebutler.android_websockets.HybiParser;
 import com.codebutler.android_websockets.SocketIOClient;
 
 import android.os.Bundle;
 import android.app.Application;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.GpsStatus.Listener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.provider.Settings;
 import android.test.suitebuilder.annotation.Suppress;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView.FindListener;
 
 public class MainApp extends Application{
 
 	
 	public static SharedPreferences app_preferences;
 	
 	public static SocketIOClient ioWebSocket;
 	public static String address;
 	public static Boolean canConnect = false;
 	public static Boolean gameActive = false;
 	
 	LocationManager GPS;
 	LocationManager locationManager;
 	LocationListener listener;
 	
 	public static Thread connection;
 	public static int connectStatus = 2;
 	
 	//gps
 	public void createGPS()
 	{
     	GPS = (LocationManager) getSystemService(LOCATION_SERVICE);	        	
     	String provider;
     	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // Define the criteria how to select the locatioin provider -> use
 	    // default
 	    Criteria criteria = new Criteria();
 	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
 	    provider = locationManager.getBestProvider(criteria, false);
 	    //Log.e("asd", ""+locationManager.getAllProviders());
 	    listener = new LocationListener() {
 			
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				
 			}
 			
 			@Override
 			public void onProviderEnabled(String provider) {
 					
 			}
 			
 			@Override
 			public void onProviderDisabled(String provider) {	
 				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 	        	  startActivity(intent);
 			}
 			
 			@Override
 			public void onLocationChanged(Location location) {
 				Date df = new java.util.Date(location.getTime());
 				
 				JSONObject gpsData = new JSONObject();
 				JSONArray arguments = new JSONArray();
 				
 				//arguments.put("gps");
 				String vv = new SimpleDateFormat("dd-MM-yyyy , HH:mm:ss").format(df);
 				
 				try {
 					gpsData.put("lat", location.getLatitude());
 					gpsData.put("Long", location.getLongitude());
 					gpsData.put("Alt", location.getAltitude());
 					gpsData.put("Acc", location.getAccuracy());
 					gpsData.put("Time", vv);
 					gpsData.put("TimeStamp", location.getTime());
 					gpsData.put("Head", location.getBearing());
 					arguments.put(gpsData);
 					ioWebSocket.emit("gpsUpdate", arguments);
 					Log.d("gps", arguments.toString());
				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 	    
 	    locationManager.requestLocationUpdates(provider, 100, 1, listener );
 	}
 	
 	public void stopGps()
 	{
 		locationManager.removeUpdates(listener);
 	}
 
 	//connection
 	public boolean startConnection() {
 		if(app_preferences.getString("IP", "") != "" && app_preferences.getString("Port", "") != "")
 		{
 			connectStatus = 0;
 			address = "http://"+ app_preferences.getString("IP", "") +":"+ app_preferences.getString("Port", "");
 
 			connection =  new Thread() {
 				@Override
 				public void run() {
 					try {
 						//MainActivity.pbConnect.setVisibility(View.VISIBLE);
 						socketCientConnection();
 						ioWebSocket.connect();
 					} catch (Exception e) {
 						Log.e("exeption", "" + e);
 						MainApp.connectStatus = 2;
 					}
 				}
 			};
 			connection.start();
 			//check Connection
 			while(connectStatus == 0){
 				try {
 					Thread.sleep(100);
 					
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				//Log.d("connection", "Connecting");
 			}
 		}
 		return canConnect;
 	}
 	
 	//close connection
 	
 	public boolean closeConnection(){
 		try {
 			connection = null;
 			ioWebSocket.disconnect();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			return true;
 		}
 	}
 	
 	public void socketCientConnection()
 	{
 		ioWebSocket = new SocketIOClient(URI.create(address), new SocketIOClient.Handler() {
 			String tag = "socketCientConnection";
 					
 			@Override
 			public void onConnect() {
 		        Log.d(tag, "Connected!");
 		        MainApp.canConnect = true;  
 		        MainApp.connectStatus = 1; 
 		    }
 
 		    @Override
 		    public void on(String event, JSONArray arguments) {
 		        Log.d(tag, String.format("Got event %s: %s", event, arguments.toString())); 
 		        
 		        if(event.equals("playerJoined")) // player list
 		        	onPlayerList(arguments);
 		        if(event.equals("startGame"))
 		        	startGame();
 		        if(event.equals("stopGame"))
 		        	gameActive = false;
 		    }
 
 		    @Override
 		    public void onDisconnect(int code, String reason) {
 		        Log.d(tag, String.format("Disconnected! Code: %d Reason: %s", code, reason));  
 		    }
 
 		    @Override
 		    public void onError(Exception error) {
 		        //Log.e(tag, "Error!", error);
 		        MainApp.connectStatus = 2;  
 		    }
 		});
 	}
 	
 	public void startGame()
 	{
 		Log.d("game", "0");
 		if(!gameActive){
 			gameActive = true;
 			Intent intent = new Intent(this, Game.class );
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 			Log.d("game", "1");
 		}
 		
 	}
 	
 	public void onPlayerList(JSONArray players)
 	{
 		// code for reaciving players here //
 		Log.d("playerList" , players.toString());
 	}
 	
 	public void testMsg()
 	{
 		JSONArray arguments = new JSONArray();
 		arguments.put("Hello Server");
 		try {
 			ioWebSocket.emit("debug", arguments);
 			//Log.d("message", "sended");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 }
