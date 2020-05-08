 package org.gnuton.newshub.utils;
 
 import android.app.Application;
 import android.content.Context;
 import android.os.Handler;
 import android.util.Log;
 
 import org.gnuton.newshub.MainActivity;
 import org.gnuton.newshub.db.RSSEntryDataSource;
 import org.gnuton.newshub.types.RSSEntry;
 
 /**
  * Created by gnuton on 6/5/13.
  */
 
 public class MyApp extends Application {
     private static final String TAG = MyApp.class.getName();
     private static MyApp mInstance;
     private static DiskLruImageCache mCache;
     public static MainActivity mMainActivity;
     public static RSSEntry mEntry;
 
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
 
         // Delete old articles
         final Runnable r = new Runnable()
         {
             public void run()
             {
                 Log.d(TAG, "Removing old entries");
                 RSSEntryDataSource eds = new RSSEntryDataSource(MyApp.getContext());
                 eds.deleteOld();
                //BUGFIX: This fix DB in devices affected by entries with invalid feedIDs.
                eds.deleteEntriesWithInvalidIDs();
             }
         };
 
         Handler handler = new Handler();
         handler.postDelayed(r, 1000);
     }
 }
