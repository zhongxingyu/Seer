 package net.virtalab.android.geolib;
 
 import android.app.Application;
 import android.content.Context;
 
 /**
  * Application object which holder Context with Resources
  * <p/>
  */
public class GeoApp extends Application {
     private static Context mContext;
 
    /**
     * Method called at very start of application
     */
     public void onCreate(){
         super.onCreate();
         mContext =this;
     }

    /**
     * Provides App Context
     *
     * @return Context
     */
     public static Context getContext(){
        return mContext;
    }
 }
