 package com.android.demo.util;
 
 import com.android.demo.db.DemoDatabase;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class Util
 {
     public static Context ms_AppContext;
     private static DemoDatabase m_db = null;
     private static boolean ms_bNetworkStatus;
 
     public static void setAppContext( Context appContext )
     {
         ms_AppContext = appContext;
         m_db = new DemoDatabase( ms_AppContext );
     }
 
    public static void setNetworkStatus(Context appContext)
     {
        ConnectivityManager connectivityManager = (ConnectivityManager) appContext.getSystemService( Context.CONNECTIVITY_SERVICE );
         NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
         NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
         NetworkInfo wifiInfo = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
         Log.d( "Util-setNetworkStatus()", "activeNetInfo " + activeNetInfo + " mobNetInfo " + mobNetInfo );
 
         if( ( mobNetInfo != null && mobNetInfo.isAvailable() && mobNetInfo.isConnected() )
                 || ( activeNetInfo != null && activeNetInfo.isAvailable() && activeNetInfo.isConnected() )
                 || ( wifiInfo != null && wifiInfo.isAvailable() && wifiInfo.isConnected() ) )
         {
             // NetworkBroadcastReceiver.ms_bIsNetworkAvailable = true;
             ms_bNetworkStatus = true;
             Log.d( "Util-setNetworkStatus()", "NETWORK AVAILABLE" );
         }
         else
         {
             ms_bNetworkStatus = false;
             Log.d( "Util-setNetworkStatus()", "NETWORK NOT AVAILABLE" );
             // NetworkBroadcastReceiver.ms_bIsNetworkAvailable = false;
         }
     }
 
     public static boolean getNetworkStatus()
     {
         return ms_bNetworkStatus;
     }
 
     public static DemoDatabase getDB()
     {
         if( m_db == null )
             m_db = new DemoDatabase( ms_AppContext );
 
         return m_db;
     }
 
 }
