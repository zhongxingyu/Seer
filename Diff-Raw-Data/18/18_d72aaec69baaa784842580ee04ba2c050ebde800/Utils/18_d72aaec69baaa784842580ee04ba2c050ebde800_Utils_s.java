 package org.dianmobile.droplet.utils;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 /**
  * ù
  * 硢ʽת
  * 
  * @author FreeTymeKiyan
  * @version 0.0.1
  */
 public class Utils {
 	
 	/**
      * ӵķ
      * 
      * @return 	false	δ
      * 			true	
      */
     public static boolean checkNetworkState(Context context) {
 		ConnectivityManager cm = (ConnectivityManager) 
 				context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		if(cm == null)
 		    return false; // ޷ȡConnectivityManager
 		NetworkInfo netinfo = cm.getActiveNetworkInfo();
 		if(netinfo == null) {
 		    return false; // ޷ȡNetworkInfo
 		}
 		if(netinfo.isConnected()) {
 		    return true; // ӣͨӵķʽ
 		}
 		return false;
     }
 }
