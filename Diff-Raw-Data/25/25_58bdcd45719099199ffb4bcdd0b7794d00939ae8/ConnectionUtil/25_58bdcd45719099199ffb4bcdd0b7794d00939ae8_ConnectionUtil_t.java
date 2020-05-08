 package com.intalker.borrow.util;
 
 import com.intalker.borrow.HomeActivity;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class ConnectionUtil {
 	public static boolean connectedAsGPRS() {
 		ConnectivityManager cm;
 		HomeActivity app = HomeActivity.getApp();
 		if (null == app) {
 			return false;
 		}
 		cm = (ConnectivityManager) app
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
		if(null == activeNetInfo) {
			return false;
		}
 		switch (activeNetInfo.getType()) {
 		case ConnectivityManager.TYPE_MOBILE:
 		case ConnectivityManager.TYPE_MOBILE_DUN:
 		case ConnectivityManager.TYPE_MOBILE_HIPRI:
 		case ConnectivityManager.TYPE_MOBILE_MMS:
 		case ConnectivityManager.TYPE_MOBILE_SUPL:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	public static boolean connectedAsWifi() {
 		ConnectivityManager cm;
 		HomeActivity app = HomeActivity.getApp();
 		if (null == app) {
 			return false;
 		}
 		cm = (ConnectivityManager) app
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
		if(null == activeNetInfo) {
			return false;
		}
 		switch (activeNetInfo.getType()) {
 		case ConnectivityManager.TYPE_WIFI:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
