 package com.r2mobile.mytravelasia;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 
 /**
  * An activity which displays a splash screen.
  */
 public class SplashActivity extends Activity {
     private static final String TAG = SplashActivity.class.getSimpleName();
     private static final int MAX_DURATION = 1000;
 
     private boolean isActive = true;
     private boolean isExit = false;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
         showScreenMetrics();
 
         Thread timerThread = new Thread() {
             @Override
             public void run() {
                 try {
                     int waited = 0;
                     while (isActive && (waited < MAX_DURATION)) {
                         sleep(10);
                         if (isActive) {
                             waited += 10;
                         }
                     }
                 } catch (InterruptedException e) {
                     finish();
                 } finally {
                     if (!isExit) {
                         Intent startUpIntent = new Intent(SplashActivity.this, StartUpActivity.class);
 
                         startActivity(startUpIntent);
                     }
                     finish();
                 }
             }
         };
         timerThread.start();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             isActive = false;
             return true;
         }
 
         return super.onTouchEvent(event);
     }
 
     /**
      * This will simply print out the screen's density, dpHeight, dpWidth, etc. Useful for testing with different
      * devices.
      */
     private void showScreenMetrics() {
         Resources resources = getResources();
         DisplayMetrics metrics = resources.getDisplayMetrics();
         float density = resources.getDisplayMetrics().densityDpi;
         float dpHeight = metrics.heightPixels / (density / 160f);
         float dpWidth = metrics.widthPixels / (density / 160f);
 
         Log.d(TAG, "::showScreenMetrics() -- density " + density);
         Log.d(TAG, "::showScreenMetrics() -- dpHeight " + dpHeight);
         Log.d(TAG, "::showScreenMetrics() -- dpWidth " + dpWidth);
         Log.d(TAG, "::showScreenMetrics() -- xPpi " + metrics.xdpi);
         Log.d(TAG, "::showScreenMetrics() -- yPpi " + metrics.ydpi);
     }
 }
