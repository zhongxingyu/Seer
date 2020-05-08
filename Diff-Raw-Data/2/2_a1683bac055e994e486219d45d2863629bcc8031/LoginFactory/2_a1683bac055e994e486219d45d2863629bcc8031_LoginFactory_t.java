 
 package net.ikhlasstudio.kmitlwifi.login;
 
 import net.ikhlasstudio.kmitlwifi.util.Util;
 import android.content.Context;
 import android.util.Log;
 
 public class LoginFactory {
     private static String LOG_TAG = "LoginFactory";
     
     public static Loginable getInstance(Context context) {
         Loginable lg = null;
         String ssid = new Util(context).getWifiSSID();
         Log.v(LOG_TAG, "current SSID: "+ssid);
         
        if(ssid.contains("KMITL-WiFi")){
             lg = new KMITLWiFiLogin(context);
         }
         
         return lg;
     }
 }
