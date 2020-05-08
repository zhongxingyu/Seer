 package org.droid_in_the_sky.rocks;
 
 import org.libsdl.app.SDLActivity;
 import android.os.*;
 
 import android.view.WindowManager;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 
 public class Rocks extends SDLActivity {
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
 /*
     protected void onDestroy() {
         super.onDestroy();
         //android.os.Process.killProcess(android.os.Process.myPid());
     }
 */
     public void onConfigurationChanged (Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // kill process: avoids having to deal w/ EGL context recreation
         android.os.Process.killProcess(android.os.Process.myPid());
         //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 }
