 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.super2k.openglen.android;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.graphics.PixelFormat;
 import android.os.Build;
 import android.os.Build.VERSION;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.super2k.openglen.RenderSetting;
 import com.super2k.openglen.SurfaceConfiguration;
 import com.super2k.openglen.nibbler.UserEvent;
 import com.super2k.openglen.utils.ConfigurationParameters;
 import com.super2k.openglen.utils.Metrics;
 
 /**
  * Base OpenGLEN activity that can be used on Android to get an activity that uses
  * OpenGLEN for rendering.
  * The activity uses an extension of SurfaceView to render into -
  * this is the OpenGLENThread which also holds the thread driving the rendering.
  *
  * @author Richard Sahlin
  *
  */
 @SuppressLint("NewApi")
 public class OpenGLENActivity extends Activity {
 
     protected final String TAG = getClass().getSimpleName();
 
     //***************************************
     //Android specific configurations:
     //***************************************
 
     /**
      * Default windowformat if no property is set
      */
     public final static int DEFAULT_WINDOWFORMAT = PixelFormat.RGBX_8888;
 
     /**
      * Set a String property in bundle to enable/disabe fullscreen.
      * Possible values are 'true' or 'false'
      */
     public final static String ANDROID_FULLSCREEN_KEY = "fullscreen";
 
     /**
      * Property to control if android navigation bar is hidden, set to true to hide navigation
      * bar. Only has effect when in fullscreen mode.
      * Set the property 'hidenavigation' in the Bundle extras before starting the activity.
      * Can be set either programatically or by sending parameter to the instrumentation.
      */
     public final static String ANDROID_HIDE_NAVIGATIONBAR_PROPERTY = "hidenavigationbar";
 
     /**
      * The android windowformat
      * Valid values are defined in the WINDOWFORMAT_TABLE
      */
     public final static String ANDROID_WINDOWFORMAT_KEY = "windowformat";
 
     public final static String WINDOWFORMAT_RGBX_8888 = "RGBX_8888";
     public final static String WINDOWFORMAT_RGBA_8888 = "RGBA_8888";
     public final static String WINDOWFORMAT_RGB_565 = "RGB_565";
     public final static String WINDOWFORMAT_RGB_888 = "RGB_888";
     public final static String WINDOWFORMAT_RGBA_5551 = "RGBA_5551";
     public final static String WINDOWFORMAT_RGBA_4444 = "RGBA_4444";
 
     public final static String[] WINDOWFORMAT_TABLE = new String[] {
         WINDOWFORMAT_RGBX_8888,
         WINDOWFORMAT_RGBA_8888,
         WINDOWFORMAT_RGB_565,
         WINDOWFORMAT_RGB_888,
         WINDOWFORMAT_RGBA_5551,
         WINDOWFORMAT_RGBA_4444 };
 
     protected final static int[] WINDOWFORMAT_VALUES_TABLE = new int[] {
         PixelFormat.RGBX_8888,
         PixelFormat.RGBA_8888,
         PixelFormat.RGB_565,
         PixelFormat.RGB_888,
         PixelFormat.RGBA_5551,
         PixelFormat.RGBA_4444 };
 
     /**
      * EGL depth buffer depth, ie number of bits required in colorbuffer.
      */
     public final static String EGL_CONFIG_TABLE[] = new String[] {
             "egldepthbits",
             "eglredbits",
             "eglgreenbits",
             "eglalphabits",
             "eglbluebits",
             "eglsamples" };
 
     public final static int EGL_DEPTH_INDEX = 0;
     public final static int EGL_RED_INDEX = 1;
     public final static int EGL_GREEN_INDEX = 2;
     public final static int EGL_BLUE_INDEX = 3;
     public final static int EGL_ALPHA_INDEX = 4;
     public final static int EGL_SAMPLES_INDEX = 5;
 
     /**
      * Parameter for locking screen to landscape or portrait
      * Valid values are portrait or landscape
      */
     public final static String ORIENTATION_KEY = "orientation";
 
     /**
      * Value for ORIENTATION_KEY to set landscape orientation. Set this as a value in the
      * bundle.
      */
     public final static String LANDSCAPE = "landscape";
     /**
      * Value for ORIENTATION_KEY to set portrait orientation. Set this as a value in the
      * bundle.
      */
     public final static String PORTRAIT = "portrait";
 
     /**
      * orientation, use -1 so that we can detect that an orientation is set.
      */
     public int mOrientation = -1;
 
     /**
      * Default value for activity fullscreen.
      * Set to the value of the Bundle key ANDROID_FULLSCREEN_PROPERTY
      */
     protected boolean mFullscreen = true;
 
     /**
      * If true, and fullscreen is true, then the navigation bar is hidden.
      */
     protected boolean mHideNavigationBar = false;
 
     protected OpenGLENThread mOpenGLENThread;
     /**
      * Window surfaceformat.
      */
     protected int mWindowFormat = DEFAULT_WINDOWFORMAT;
 
     //***************************************
     //OpenGLEN Configurations
     //***************************************
 
     protected SurfaceConfiguration mSurfaceConfig = new SurfaceConfiguration();
 
     protected RenderSetting mRenderSetting = new RenderSetting();
 
     /**
      * Called when the activity is first created.
      * Subclasses of OpenGLENActivity shall first call super.onCreate
      * then make a call to setOpenGLENView to start OpenGLEN.
      * This implementation will require a wake lock to wakeup the display and keep display on.
      * It will also flag to windowmanager to show when locked and keep screen on.
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "Software build: " + Build.ID );
         Log.d(TAG, "Device name:" + Build.MANUFACTURER + " " + Build.BRAND + " "
                 + Build.DEVICE + " (" +
                 Build.BOARD + ", " + Build.CPU_ABI + " + " + Build.CPU_ABI2
                 + ", " + Build.DISPLAY + ")");
         Log.d(TAG, "Android version:" + VERSION.RELEASE +
               "(" + VERSION.INCREMENTAL + ")");
 
         //Check system properties for configuration.
         checkConfiguration();
         getWindow().setFormat(mWindowFormat);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         if (mFullscreen) {
             requestWindowFeature(Window.FEATURE_NO_TITLE);
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             if (mHideNavigationBar) {
                 try {
                     getWindow().getDecorView().setSystemUiVisibility(
                             View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                 } catch (Throwable t) {
                     //Ignore, will happen if SDK level too low.
                 }
             }
 
         }
         if (mOrientation != -1) {
             if (getRequestedOrientation() != mOrientation) {
                 setRequestedOrientation(mOrientation);
                 //Will most likely trigger a restart of the app
                 Log.d(TAG, "Setting new orientation: " + mOrientation + ", exit onCreate()");
             }
         }
         Metrics.createMetrics(getWindowManager().getDefaultDisplay());
 
         super.onCreate(savedInstanceState);
         setOpenGLENView();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         if (mOpenGLENThread != null) {
 //            mOpenGLENThread.start();
         }
         Log.d(TAG, "onStart()");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.d(TAG, "onPause()");
         if (mOpenGLENThread != null) {
             mOpenGLENThread.pause();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.d(TAG, "onResume()");
         if (mOpenGLENThread != null) {
             mOpenGLENThread.resume();
         }
 
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         Log.d(TAG, "onStop()");
         if (mOpenGLENThread != null) {
             mOpenGLENThread.stop();
         }
 
 
     }
 
     /**
      * Creates a new OpenGLENThread (View), and sets to the contentview.
      * A new application context is created and used with the OpenGLENThread.
      */
     protected void setOpenGLENView() {
         Log.d(TAG, "setOpenGLENView()");
         String runnerClass = getApplicationContext().getString(R.string.runner);
         String renderer = getApplicationContext().getString(R.string.renderer);
         mOpenGLENThread = new OpenGLENThread(this, mRenderSetting, mSurfaceConfig,
                 mWindowFormat, renderer, runnerClass);
         setContentView(mOpenGLENThread);
         mOpenGLENThread.setKeepScreenOn(true);
 
     }
 
     /**
      * Check config parameters in properties.
      */
     protected void checkConfiguration() {
 
         /**
          * Check Android specific configuration
          */
 
         Bundle extra = getIntent().getExtras();
         if (extra!=null) {
 
             //Check EGL config.
             for (int i = 0; i < EGL_CONFIG_TABLE.length; i++) {
                 String eglconfig = extra.getString(EGL_CONFIG_TABLE[i]);
                 if (eglconfig!=null) {
                     int value = Integer.parseInt(eglconfig);
                     if (value<0) {
                         throw new IllegalArgumentException("Invalid EGL buffer bits:"+value);
                     }
                     switch (i) {
                         case EGL_DEPTH_INDEX: //depth
                             mSurfaceConfig.setDepthBits(value);
                             break;
                         case EGL_RED_INDEX: //red
                             mSurfaceConfig.setRedBits(value);
                             break;
                         case EGL_GREEN_INDEX: //green
                             mSurfaceConfig.setGreenBits(value);
                             break;
                         case EGL_BLUE_INDEX: //blue
                             mSurfaceConfig.setBlueBits(value);
                             break;
                         case EGL_ALPHA_INDEX: //alpha
                             mSurfaceConfig.setAlphaBits(value);
                             break;
                         case EGL_SAMPLES_INDEX: //samples
                             mSurfaceConfig.setSamples(value);
                             mRenderSetting.enableMultisampling(true);
                         default:
                             throw new IllegalArgumentException("Illegal EGL value (" +
                                    i + ") for option "+eglconfig);
                     }
                 }
             }
 
             String str = extra.getString(ANDROID_FULLSCREEN_KEY);
             if (str != null) {
                 if (str.equalsIgnoreCase("true")) {
                     mFullscreen = true;
                 } else if (str.equalsIgnoreCase("false")) {
                     mFullscreen = false;
                 } else {
                     throw new IllegalArgumentException("Invald value for " +
                            ANDROID_FULLSCREEN_KEY + ":" + str);
                 }
 
             }
 
             str = extra.getString(ANDROID_HIDE_NAVIGATIONBAR_PROPERTY);
             if (str != null) {
                 Log.d(TAG, "Got value for navigation:" + str);
                 if (str.equalsIgnoreCase("true")) {
                     mHideNavigationBar = true;
                 } else if (str.equalsIgnoreCase("false")) {
                     mHideNavigationBar = false;
                 } else {
                     throw new IllegalArgumentException("Invald value for " +
                            ANDROID_HIDE_NAVIGATIONBAR_PROPERTY +
                            ":" + str);
                 }
             }
             str = extra.getString(ANDROID_WINDOWFORMAT_KEY);
 
             if (str!=null) {
                 int i;
                 for (i = 0; i < WINDOWFORMAT_TABLE.length; i++) {
                     if (str.equalsIgnoreCase(WINDOWFORMAT_TABLE[i])) {
                         mWindowFormat = WINDOWFORMAT_VALUES_TABLE[i];
                         break;
                     }
                 }
                 if (i == WINDOWFORMAT_TABLE.length) {
                     throw new IllegalArgumentException("Invalid value for property " +
                            ANDROID_WINDOWFORMAT_KEY + ":" + str);
                 }
             }
 
             String orientation = extra.getString(ORIENTATION_KEY);
             if (orientation != null) {
                 if (orientation.equalsIgnoreCase(LANDSCAPE)) {
                     mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                 } else if (orientation.equalsIgnoreCase(PORTRAIT)) {
                     mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                 } else
                     throw new IllegalArgumentException("Invalid screen orientation:" + orientation);
             }
 
         }
 
         int format = ConfigurationParameters.getWindowFormatProperty();
         if (format !=-1) {
             mWindowFormat = format;
         }
 
         //Read samples and depthbits from R.string get an easy way of changing parameters.
         //May be overwritten by property values in setConfiguration.
         mSurfaceConfig.setSamples(Integer.parseInt(getString(R.string.samples)));
         if (getString(R.string.enablemultisampling).equalsIgnoreCase("true")) {
             mRenderSetting.enableMultisampling(true);
         }
 
         /**
          * Fetch the general configuration, EGL and renderer.
          */
         ConfigurationParameters.getSurfaceConfiguration(mSurfaceConfig);
 
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "onDestroy()");
         //Should not call destroy on OpenGLENThread since that listens to the SurfaceHolder
         //callback and should already been notified.
         super.onDestroy();
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
 
         case KeyEvent.KEYCODE_VOLUME_DOWN:
         case KeyEvent.KEYCODE_VOLUME_UP:
             return super.onKeyUp(keyCode, event);
         case KeyEvent.KEYCODE_BACK:
             if (keyCode == KeyEvent.KEYCODE_BACK) {
                 UserEvent e = new UserEvent(UserEvent.TYPE_KEY_UP, UserEvent.BACK_KEY, null);
                 mOpenGLENThread.mRunner.userEvent(e);
                 return true;
             }
 
     }
     return false;
 
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
 
             case KeyEvent.KEYCODE_VOLUME_DOWN:
             case KeyEvent.KEYCODE_VOLUME_UP:
                 return super.onKeyDown(keyCode, event);
             case KeyEvent.KEYCODE_BACK:
                 if (keyCode == KeyEvent.KEYCODE_BACK) {
                     UserEvent e = new UserEvent(UserEvent.TYPE_KEY_DOWN, UserEvent.BACK_KEY, null);
                     mOpenGLENThread.mRunner.userEvent(e);
                     return true;
                 }
 
         }
         return false;
     }
 
     /**
      * Helper method to set the EGL properties to a Bundle before starting the
      * OpenGLENActivity. Note that this method must be called before the Activity is started.
      * @param bundle The Bundle to set the EGL properties to.
      * @param depth Number of depth bits
      * @param red Number of red bits
      * @param green Number of green bits
      * @param blue Number of blue bits
      * @param alpha Number of alpha bits in the EGL window.
      */
     public final static void setEGLProperties(Bundle bundle, int depth,
                                               int red, int green, int blue, int alpha) {
 
         bundle.putString(OpenGLENActivity.EGL_CONFIG_TABLE[OpenGLENActivity.EGL_DEPTH_INDEX],
                 Integer.toString(depth));
         bundle.putString(OpenGLENActivity.EGL_CONFIG_TABLE[OpenGLENActivity.EGL_RED_INDEX],
                 Integer.toString(red));
         bundle.putString(OpenGLENActivity.EGL_CONFIG_TABLE[OpenGLENActivity.EGL_GREEN_INDEX],
                 Integer.toString(green));
         bundle.putString(OpenGLENActivity.EGL_CONFIG_TABLE[OpenGLENActivity.EGL_BLUE_INDEX],
                 Integer.toString(blue));
         bundle.putString(OpenGLENActivity.EGL_CONFIG_TABLE[OpenGLENActivity.EGL_ALPHA_INDEX],
                 Integer.toString(alpha));
 
     }
 
     /**
      * Sets the Bundle properties for the activity, orientation, fullscreen and windowformat.
      * @param bundle The Bundle to set properties in.
      * @param orientation The screen orientation, see ORIENTATION_PARAMETER or null for no
      * required orientation
      * @param windowFormat The activity window format, see WINDOWFORMAT_TABLE or null for
      * no required window format.
      * @param fullscreen True to enable fullscreen, false otherwise.
      */
     public final static void setActivityProperties(Bundle bundle, String orientation,
            String windowFormat, boolean fullscreen) {
         if (orientation != null) {
             bundle.putString(OpenGLENActivity.ORIENTATION_KEY, orientation);
         }
         if (windowFormat != null) {
             bundle.putString(OpenGLENActivity.ANDROID_WINDOWFORMAT_KEY, windowFormat);
         }
         if (fullscreen) {
             bundle.putString(ANDROID_FULLSCREEN_KEY, "true");
             bundle.putString(ANDROID_HIDE_NAVIGATIONBAR_PROPERTY, "true");
 
         } else {
             bundle.putString(ANDROID_FULLSCREEN_KEY, "false");
         }
     }
 
 }
