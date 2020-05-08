 
 
 package com.cocosw.framework.app;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import com.androidquery.util.AQUtility;
import com.cocosw.accessory.connectivity.NetworkConnectivity;
 import com.cocosw.accessory.utils.UIUtils;
 import com.cocosw.framework.BuildConfig;
 import com.cocosw.framework.network.Network;
 import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
 import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
 
 
 /**
  * Android Bootstrap application
  */
 public abstract class CocoApp extends Application {
 
     protected static CocoApp instance;
 
     private static String TAG = "Coco";
 
     /**
      * Create main application
      */
     @TargetApi(Build.VERSION_CODES.GINGERBREAD)
     public CocoApp() {
         instance = this;
         if (BuildConfig.DEBUG) {
             AQUtility.setDebug(true);
             if (UIUtils.hasGingerbread()) {
                 StrictMode
                         .setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                 .detectNetwork().penaltyLog().build());
             }
             if (showActivityLiftcycle())
             ApplicationHelper.registerActivityLifecycleCallbacks(this, mDebugCallbacks);
         }
     }
 
     protected boolean showActivityLiftcycle() {
         return false;
     }
 
 
     /**
      * Create main application
      *
      * @param context
      */
     public CocoApp(final Context context) {
         this();
         attachBaseContext(context);
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
        NetworkConnectivity.getInstance(this);
         Network.init(this);
         TAG = getString(getApplicationInfo().labelRes);
     }
 
     public static Application getApp() {
         if (instance==null) {
             throw new IllegalAccessError("Please create your own Application class inherit from CocoApp, and add it to manifest");
         }
         return instance;
     }
 
     private ActivityLifecycleCallbacksCompat mDebugCallbacks = new ActivityLifecycleCallbacksCompat() {
         @Override
         public void onActivityStopped(Activity activity) {
             Log.d(TAG, "onActivityStopped activity=" + activity);
         }
 
         @Override
         public void onActivityStarted(Activity activity) {
             Log.d(TAG, "onActivityStarted activity=" + activity);
         }
 
         @Override
         public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
             Log.d(TAG, "onActivitySaveInstanceState activity=" + activity);
         }
 
         @Override
         public void onActivityResumed(Activity activity) {
             Log.d(TAG, "onActivityResumed activity=" + activity);
         }
 
         @Override
         public void onActivityPaused(Activity activity) {
             Log.d(TAG, "onActivityPaused activity=" + activity);
         }
 
         @Override
         public void onActivityDestroyed(Activity activity) {
             Log.d(TAG, "onActivityDestroyed activity=" + activity);
         }
 
         @Override
         public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
             Log.d(TAG, "onActivityCreated activity=" + activity);
         }
     };
 }
