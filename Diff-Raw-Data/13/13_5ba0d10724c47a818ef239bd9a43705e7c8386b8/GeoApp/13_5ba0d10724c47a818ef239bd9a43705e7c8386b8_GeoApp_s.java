 package net.virtalab.android.geolib;
 
 import android.app.Application;
 import android.content.Context;
 
 /**
  * Application object which holder Context with Resources
  * <p/>
  */
class GeoApp extends Application {
     private static Context mContext;
 
     public void onCreate(){
         super.onCreate();
         mContext =this;
     }
     public static Context getContext(){
        return mContext;
    }
 }
