 package com.trustripes.Constants;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class ConstantValues {
 	/* Everything here are variables available for every class in the app */
 	
 	/* Root URL for connection */
	public static final String URL = "http://dev.trustripes.com";
 
 	/* Quality Assurance */
 	//public static final String URL = "http://qas.trustripes.com";
 	
 	/* Production */
	//public static final String URL = "http://www.trustripes.com";
 	
 	/* For Session Status */
 	public static final String USER_DATA = "UserDataPreferences";
 	
 	public static final int[] developmentIds = {1,2,28,42};
 	
 	public static boolean isInDevelopmentTeam(int key){
 		for(int i=0;i<4;i++){
 			if(key == developmentIds[i]){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static final String codeName(int i){	
 		int previous;
 		int next;
 		
 		previous = i - 11;
 		next = i + 11;
 		
 		if(i <= 10)
 			previous = previous * -1;
 				
 		return Integer.toHexString(previous) + Integer.toHexString(i)+Integer.toHexString(next);
 	}
 	
 	public static final boolean getConnectionStatus(Context context){
 		 /* Check Connection Status */
         ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
         NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
         if(wifi.isAvailable() || mobile.isAvailable()){
         	/* There is connection to transfer data */
         	return true;
         }
         else{
         	/* There isn't connection */ 
         	return false;
         }	
 	}
 	
 }
