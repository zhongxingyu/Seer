 package org.gnuton.newshub.utils;
 
 import android.app.Application;
 import android.content.Context;
 
 import org.gnuton.newshub.MainActivity;
 
 /**
  * Created by gnuton on 6/5/13.
  */
 
 public class MyApp extends Application {
     private static MyApp mInstance;
     private static DiskLruImageCache mCache;
     public static MainActivity mMainActivity;

     public static MyApp getInstance() {
         return mInstance;
     }
 
     public static Context getContext(){
         return mInstance;
     }
 
     public static DiskLruImageCache getImageCache() {
        return mCache;
     }

     @Override
     public void onCreate() {
         mInstance = this;
         mCache = new DiskLruImageCache();
         super.onCreate();
     }
 }
