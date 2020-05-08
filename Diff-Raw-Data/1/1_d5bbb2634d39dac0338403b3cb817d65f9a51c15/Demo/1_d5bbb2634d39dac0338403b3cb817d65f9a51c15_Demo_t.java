 /*
  * Copyright (C) 2011 Ron Huang
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 
 package org.ronhuang.vistroller;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.util.Log;
 import android.graphics.PixelFormat;
 import android.opengl.GLSurfaceView;
 
 
 /** The main activity for the Demo. */
 public class Demo extends Activity implements VistrollerListener
 {
     // Vistroller instance
     private Vistroller mVistroller;
 
     // Our views:
     private GLSurfaceView mBgView;
     private View mFgView;
 
     // Our renderer:
     private FrameMarkersRenderer mRenderer;
 
     // Force orientation.
     private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
 
     // Log tag
     private static final String TAG = "Demo";
 
     // FIXME: remove
     // The minimum time the splash screen should be visible:
     private static final long MIN_SPLASH_SCREEN_TIME = 2000;
     // The time when the splash screen has become visible:
     private long mSplashScreenStartTime = 0;
 
 
     /** Called when the activity first starts or the user navigates back
      * to an activity. */
     protected void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "Demo::onCreate");
         super.onCreate(savedInstanceState);
 
         // Specify layout
         setContentView(R.layout.demo);
 
         // Apply screen orientation
         setRequestedOrientation(mScreenOrientation);
 
         // As long as this window is visible to the user, keep the device's
         // screen turned on and bright.
         getWindow().setFlags(
             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         // Initialize background view.
         mBgView = (GLSurfaceView)findViewById(R.id.camera_background);
         mBgView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
         mBgView.setEGLContextClientVersion(2);
         mBgView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
 
         // Initialize foreground view.
         mFgView = (View)findViewById(R.id.canvas_foreground);
 
         // FIXME: remove
         mSplashScreenStartTime = System.currentTimeMillis();
 
         // Create and initialize Vistroller instance.
         mVistroller = new Vistroller(this);
         mVistroller.addListener(this);
         mVistroller.onCreate();
     }
 
 
    /** Called when the activity will start interacting with the user.*/
     protected void onResume() {
         Log.d(TAG, "Demo::onResume");
         super.onResume();
 
         mVistroller.onResume();
         if (null != mRenderer) {
             mBgView.setVisibility(View.VISIBLE);
             mBgView.onResume();
         }
     }
 
 
     /** Called when the system is about to start resuming a previous activity.*/
     protected void onPause() {
         Log.d(TAG, "Demo::onPause");
         super.onPause();
 
         if (null != mRenderer) {
             mBgView.setVisibility(View.INVISIBLE);
             mBgView.onPause();
         }
         mVistroller.onPause();
     }
 
 
     /** The final call you receive before your activity is destroyed.*/
     protected void onDestroy() {
         Log.d(TAG, "Demo::onDestroy");
         super.onDestroy();
 
         mVistroller.onDestroy();
     }
 
 
     /** Listen to Vistroller events. */
     public void onVistrollerStateChanged(Vistroller.State state) {
         Log.d(TAG, "Demo::onVistrollerStateChanged: " + state);
 
         switch (state) {
         case ENGINE_INITIALIZED:
             // Create renderer.
             mRenderer = new FrameMarkersRenderer();
             mRenderer.setActivityPortraitMode(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
             DisplayMetrics metrics = new DisplayMetrics();
             getWindowManager().getDefaultDisplay().getMetrics(metrics);
             mRenderer.setScreenSize(metrics.widthPixels, metrics.heightPixels);
 
             // Apply renderer to background view.
            mBgView.setVisibility(View.VISIBLE);
             mBgView.setRenderer(mRenderer);
             break;
 
         case TRACKER_INITIALIZED:
             // The elapsed time since the splash screen was visible:
             long splashScreenTime = System.currentTimeMillis() - mSplashScreenStartTime;
             long newSplashScreenTime = 0;
             if (splashScreenTime < MIN_SPLASH_SCREEN_TIME)
                 newSplashScreenTime = MIN_SPLASH_SCREEN_TIME - splashScreenTime;
 
             // Request a callback function after a given timeout to dismiss
             // the splash screen:
             Handler handler = new Handler();
             handler.postDelayed(new Runnable() {
                 public void run() {
                     // Hide the splash screen.
                     mFgView.setVisibility(View.INVISIBLE);
 
                     // Start the camera.
                     mVistroller.requestStartCamera();
 
                     // Activate the renderer.
                     mRenderer.configureProjectMatrix();
                     mRenderer.configureVideoBackground();
                     mRenderer.mIsActive = true;
                 }
             }, newSplashScreenTime);
             break;
 
         case SYSTEM_INITIALIZED:
             // Hint to the virtual machine that it would be a good time to
             // run the garbage collector.
             //
             // NOTE: This is only a hint. There is no guarantee that the
             // garbage collector will actually be run.
             System.gc();
             break;
 
         default:
             break;
         }
     }
 }
