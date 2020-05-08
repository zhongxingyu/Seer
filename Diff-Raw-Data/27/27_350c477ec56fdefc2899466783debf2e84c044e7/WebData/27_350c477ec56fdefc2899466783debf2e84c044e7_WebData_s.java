 package com.lpuente.pack_Lib;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class WebData {
 
 
 	//holder values / accessible outside of class
 	
 	static Boolean _connection = false;
 	static String _connType = "Unavailable";
 	
	//function to call connection service and type function and return the connection type
 	public static String getConnectType(Context context)
 	{
 		onlineInfo(context);	//connection service and type function
 		return _connType;		//connection type value
 	}
 
	//Function to return Boolean. Is connected? In other words connection status
 	public static Boolean getConnectStatus(Context context)
 	{
 		onlineInfo(context);	//connection service and type function
 		return _connection;		//boolean value
 	}
 	
 	
	//function to allow if there is connection service and Type.
 	private static void onlineInfo(Context context)
 	{
 		//connectivity manager object
 		ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 		//Network info object / check to see if network is already active
 		NetworkInfo  netInfo = connMan.getActiveNetworkInfo();
 		
 		//check to make sure network activity is not null. 
 		if(netInfo != null)
 		{
 			//make sure if connected
 			if(netInfo.isConnected())
 			{
 			//getting connection type by string "name"
 				_connType = netInfo.getTypeName();
 				_connection = true;
 			}
 		}
 	}
 	
 }
