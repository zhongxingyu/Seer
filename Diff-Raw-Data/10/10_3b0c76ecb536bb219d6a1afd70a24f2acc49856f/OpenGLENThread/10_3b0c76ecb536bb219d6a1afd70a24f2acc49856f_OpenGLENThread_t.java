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
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import javax.microedition.khronos.egl.EGL10;
 import javax.microedition.khronos.egl.EGLContext;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.os.SystemClock;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.ViewConfiguration;
 
 import com.super2k.nibbler.android.AndroidStreamResolver;
 import com.super2k.openglen.ConstantValues;
 import com.super2k.openglen.EGLRenderer;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.RenderSetting;
 import com.super2k.openglen.Renderer;
 import com.super2k.openglen.SurfaceConfiguration;
 import com.super2k.openglen.nibbler.CompatibilityRunner;
 import com.super2k.openglen.utils.Log;
 
 /**
  * Implementation of a custom view, this class extends the SurfaceView.
  * When the surface is created rendering is started using a RenderPipeline that sets up EGL and GLES.
  * This method means that all events needs to be handled
  * (ie it is not done as in the case of Android GLSurfaceView)
  * It can give more controll and flexibility as the EGL/GLES are bound to
  * the thread used in this class.
  * @author Richard Sahlin
  *
  */
 public class OpenGLENThread extends SurfaceView implements SurfaceHolder.Callback, Runnable {
 
     private final String TAG = this.getClass().getSimpleName();
 
     private final static String INVALID_PIXELFORMAT = "Invalid pixelformat:";
 
     protected final static float DEFAULT_ZNEAR = 1f;
 
     protected final static float DEFAULT_ZFAR = 3000;
 
     public final static float ZNEAR = DEFAULT_ZNEAR;
 
     public final static float ZFAR = DEFAULT_ZFAR;
 
     /**
      * Difference between System.currentTimeMillis -SystemClock.uptimeMillis()
      */
     private long mUptimeToMillis = 0;
 
     protected EGLContext mglContext;
 
     protected float[] mTouchDown = new float[2]; //Touch down position x, y.
     protected float mTouchSlop;
     /**
      * Time of first touch, used for tap timeout.
      */
     protected long mTouchDownTime;
     /**
      * Time of event, converted to currentTimeMillis()
      */
     protected long mEventTime;
     /**
      * When a press becomes longpress.
      */
     protected int mLongPressThreshold;
 
     protected boolean mHandledLongPress = false;
     /**
      * Number of millis to wait for a tap.
      */
     protected int mTapTimeout;
 
     EGLRenderer mRenderer;
     volatile SurfaceConfiguration mSurfaceConfig;
     RenderSetting mRenderSetting;
     SurfaceHolder mSurfaceHolder;
 
     protected boolean mRestart = true;     //false will exit main loop and trigger restart of
                                             //the OpenGLEN runner.
     protected boolean mDestroy = false;
     protected boolean mPause = false;       //Set to true to pause but not exit main loop.
     protected boolean mCreatedEGL = false;
 
     protected Activity mActivity;
 
     protected static long mStartFreeMem = 0; //ActivityManager free memory when we start
     protected static long mDestroyFreeMem = 0; //ActivityManager free memory at destroy
     protected MemoryInfo mMemoryInfo = new MemoryInfo();
     protected ActivityManager mAM;
     protected int mTicksCount;
     protected int MEMORY_LOG_INTERVAL = 5000;
 
 
     CompatibilityRunner mRunner;
     Thread mWorkerThread; //This is the working thread.
     /**
      * If not null then this will be set to the thread when created.
      */
     UncaughtExceptionHandler mExceptionHandler;
 
     private String mRenderStr; //Render version, used to toggle GLES1/GLES2
 
     /**
      * Constructor to use an EGL based renderer.
      * @param The Activity
      * @param renderSetting
      * @param surfaceConfig EGL surface configuration
      * @param pixelFormat
      * PIXELFORMAT_RGBA_4444
      * PIXELFORMAT_RGBA_5551
      * PIXELFORMAT_RGBA_8888
      * PIXELFORMAT_RGBX_8888
      * PIXELFORMAT_RGB_565
      * PIXELFORMAT_RGB_888
      * @throws IllegalArgumentException if renderer is not 'GLES2',
      * or pixelFormat not one of the listed values.
      *
      */
     public OpenGLENThread(Activity activity, RenderSetting renderSetting,
             SurfaceConfiguration surfaceConfig, int pixelFormat, String renderer,
             String runnerClass) {
 
         super(activity.getApplicationContext());
         try {
             Class<?> rClass = Class.forName(runnerClass);
             setup(activity, renderSetting, surfaceConfig, pixelFormat, renderer,
                     (CompatibilityRunner)rClass.newInstance());
 
         } catch (ClassNotFoundException cnfe) {
             String errStr = "Could not create class " + runnerClass;
             Log.d(TAG, errStr);
             throw new IllegalArgumentException(errStr);
         } catch (InstantiationException ie) {
             String errStr = "Could not instantiate class " + runnerClass;
             Log.d(TAG, errStr);
             throw new IllegalArgumentException(errStr);
         } catch (IllegalAccessException ia) {
             String errStr = "Could not access class " + runnerClass;
             Log.d(TAG, errStr);
             throw new IllegalArgumentException(errStr);
         }
 
     }
 
     /**
      * Constructor to use an EGL based renderer and an already created
      * runner class.
      * @param activity The activity
      * @param renderSetting
      * @param surfaceConfig EGL surface configuration
      * @param pixelFormat
      * PIXELFORMAT_RGBA_4444
      * PIXELFORMAT_RGBA_5551
      * PIXELFORMAT_RGBA_8888
      * PIXELFORMAT_RGBX_8888
      * PIXELFORMAT_RGB_565
      * PIXELFORMAT_RGB_888
      * @throws IllegalArgumentException if renderer is not 'GLES2',
      * or pixelFormat not one of the listed values.
      *
      */
     public OpenGLENThread(Activity activity, RenderSetting renderSetting,
             SurfaceConfiguration surfaceConfig, int pixelFormat, String renderer,
             CompatibilityRunner runner) {
         super(activity.getApplicationContext());
         setup(activity, renderSetting, surfaceConfig, pixelFormat, renderer, runner);
     }
 
     /**
      * Setup the thread.
      * @param activity The activity
      * @param renderSetting
      * @param surfaceConfig EGL surface configuration
      * @param pixelFormat
      * PIXELFORMAT_RGBA_4444
      * PIXELFORMAT_RGBA_5551
      * PIXELFORMAT_RGBA_8888
      * PIXELFORMAT_RGBX_8888
      * PIXELFORMAT_RGB_565
      * PIXELFORMAT_RGB_888
      * @throws IllegalArgumentException if renderer is not 'GLES2', or pixelFormat not one of the listed values
      * or runner is null.
      */
     private void setup(Activity activity, RenderSetting renderSetting, SurfaceConfiguration surfaceConfig,
             int pixelFormat, String renderer, CompatibilityRunner runner) {
         Log.d(TAG, "Creating new instance of OpenGLENThread with runner: " + runner);
         if (runner == null) {
             throw new IllegalArgumentException("Runner class is null");
         }
         if (renderer == null) {
             throw new IllegalArgumentException("Renderer is null");
         }
         if (!renderer.equalsIgnoreCase("GLES2")) {
             throw new IllegalArgumentException("Invalid renderer:" + renderer);
         }
         if (pixelFormat != ConstantValues.PIXELFORMAT_RGB_565 &&
                 pixelFormat != ConstantValues.PIXELFORMAT_RGB_888 &&
                 pixelFormat != ConstantValues.PIXELFORMAT_RGBA_4444 &&
                 pixelFormat != ConstantValues.PIXELFORMAT_RGBA_5551 &&
                 pixelFormat != ConstantValues.PIXELFORMAT_RGBA_8888 &&
                 pixelFormat != ConstantValues.PIXELFORMAT_RGBX_8888) {
             throw new IllegalArgumentException(INVALID_PIXELFORMAT + pixelFormat);
         }
 
         mTouchSlop = ViewConfiguration.getTouchSlop();
         mLongPressThreshold = ViewConfiguration.getLongPressTimeout();
         mTapTimeout = ViewConfiguration.getTapTimeout();
         mSurfaceConfig = surfaceConfig;
         mRenderSetting = renderSetting;
         mSurfaceHolder = getHolder();
         mSurfaceHolder.addCallback(this);
         mActivity = activity;
         mRenderStr = renderer;
         if (mRunner != null) {
             throw new IllegalArgumentException("Runner is not null, release before setting new.");
         }
         mRunner = runner;
         mAM = (ActivityManager)mActivity.getApplicationContext().
                 getSystemService(Context.ACTIVITY_SERVICE);
 
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         Log.d(TAG, "Surface size: " + width + ", " + height + ". Format: " + format);
         switch (format) {
         case PixelFormat.RGB_565:
             mSurfaceConfig.setRedBits(5);
             mSurfaceConfig.setGreenBits(6);
             mSurfaceConfig.setBlueBits(5);
             mSurfaceConfig.setAlphaBits(0);
             Log.d(Renderer.OPENGLEN_TAG, "Set surfaceconfig to RGB565");
         break;
         }
         //surface changed is called at least once after surface created.
         if (mWorkerThread == null || mDestroy) {
             mDestroy = false;
             mRestart = true;
             mPause = false;
             mWorkerThread = new Thread(this);
             if (mExceptionHandler != null) {
                 mWorkerThread.setUncaughtExceptionHandler(mExceptionHandler);
             }
             Log.d(TAG, "Created Thread, starting.");
             mWorkerThread.setPriority(Thread.MAX_PRIORITY);
             mWorkerThread.start();
         } else {
            if (!mCreatedEGL) {
                //Make thread restart.
                Log.d(TAG, "Surface size changed, restarting thread.");
                mRestart = true;
            } else {
                mRenderer.setViewPort(0, 0, width, height);
            }
         }
 
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         Log.d(TAG, "surfaceCreated()");
         ActivityManager am =
                 (ActivityManager)mActivity.getApplicationContext().getSystemService(
                 Context.ACTIVITY_SERVICE);
         MemoryInfo outInfo = new MemoryInfo();
         am.getMemoryInfo(outInfo);
         Log.d(TAG, "Memory available at surfaceCreated(): " + outInfo.availMem);
         if (mStartFreeMem == 0) {
             mStartFreeMem = outInfo.availMem;
         }
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         Log.d(TAG, "SurfaceDestroyed");
         mCreatedEGL = false;
         mRenderer.releaseEGL();
     }
 
     @Override
     public void run() {
 
         try {
 
             Log.d(TAG, "Entering thread run()");
 
             if (mRenderStr.equalsIgnoreCase("GLES2")) {
                 mRenderer = new GLES20EGLRenderer(mSurfaceConfig,
                         mActivity.getApplicationContext(), mRenderSetting);
 
             } else {
                 throw new IllegalArgumentException("Only GLES2 renderer implemented");
             }
             try {
                 mRenderer.initRenderer();
             }
             catch (OpenGLENException e) {
                 throw new IllegalArgumentException(e);
             }
             mRunner.setup( mRenderer, new AndroidStreamResolver(mActivity.getApplicationContext().
                     getAssets()), getWidth(),getHeight());
             while (mRestart && !mDestroy) {
                 try {
                     mRenderer.createEGL(EGL10.EGL_DEFAULT_DISPLAY, mSurfaceHolder);
                     mCreatedEGL = true;
                     mRunner.start();
                 }
                 catch (OpenGLENException e) {
                     throw new IllegalArgumentException(e);
                 }
 
                 mRestart = false; //
                 while (!mDestroy && !mRestart) {
                     while (!mPause && !mDestroy && !mRestart && mCreatedEGL) {
                         if (mUptimeToMillis == 0) {
                             mUptimeToMillis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                         }
                         if (mRunner.processFrame(null) == CompatibilityRunner.DESTROY) {
                             mActivity.setResult(Activity.RESULT_OK);
                             destroy();
                         }
                         mTicksCount += mRenderer.getProfileInfo().getFrameTicks() / 1000;
                         if (mTicksCount > MEMORY_LOG_INTERVAL) {
                             mAM.getMemoryInfo(mMemoryInfo);
                             Log.d(Renderer.OPENGLEN_TAG, "Available memory: " +
                                     mMemoryInfo.availMem);
                             mTicksCount = 0;
                         }
                         Thread.yield();
                     }
                     Thread.yield();
                 }
                 //restart set to true or destroy set to true..
                 Log.d(TAG, "Exiting main loop. (restart,destroy) " + mRestart + ", " + mDestroy);
                 Thread.yield();
             }
 
         }
 
         finally {
             /**
              * We have finished executing in this thread, either due to exception or that
              * user finished.
              * If surfaceChanged gets called again this thread should loop.
              */
             if (mRunner != null && mDestroy) {
                 mRunner.destroy();
             }
             /**
              * Teardown renderer and EGL
              */
             Log.d(TAG, "Exiting, releasing EGL");
             mRenderer.releaseEGL();
             mRenderer.destroy();
             ActivityManager am = (ActivityManager)mActivity.getApplicationContext().
                                 getSystemService(Context.ACTIVITY_SERVICE);
             MemoryInfo outInfo = new MemoryInfo();
             System.gc();
             am.getMemoryInfo(outInfo);
             int diff = (int) (mStartFreeMem - outInfo.availMem);
             if (diff > 0) {
                 Log.e(TAG, "Memory available at destroy:" + outInfo.availMem +
                         ", have leaked " + diff);
             }
 
         }
         mActivity.finish();
 
     }
 
     /**
      * Stops the main loop but does not exit the thread.
      * Calls pause on the CompatibilityRunner if not null
      */
     public void pause() {
         Log.d(TAG, "pause(), restart=" + mRestart + ", destroy=" + mDestroy);
         mPause = true;
         mUptimeToMillis = 0;
         if (mRunner != null) {
             mRunner.pause();
         }
     }
 
     /**
      *
      */
     public void stop() {
     }
 
     /**
      * Starts the thread, if paused will make thread run again.
      */
     public void start() {
         Log.d(TAG, "start(), restart=" + mRestart + ", destroy=" + mDestroy + ", pause=" + mPause);
         if (mPause == true) {
             mPause = false;
         }
     }
 
     /**
      * Calls resume on the Compatibilityrunner, if not null
      */
     public void resume() {
         if (mRunner != null) {
             mRunner.resume();
             start();
         }
     }
 
     /**
      * Destroys the thread, this will exit the thread and release any resources.
      * Note that this method signals to the thread that it shall exit, it will not
      * release any resources before the thread has exited.
      * If you are relying on resources to be freed you must explicitly wait for thread
      * to exit after calling this method.
      */
     public void destroy() {
         Log.d(TAG, "destroy(), restart=" + mRestart + ", destroy=" + mDestroy + ", pause=" + mPause);
         mDestroy = true;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (mRunner == null || mUptimeToMillis == 0) {
             Log.d(TAG, "Exiting onTouch: " + mUptimeToMillis);
             return false;
         }
         int pid;
         int action = event.getAction();
         float x;
         float y;
 
         if (event.getPointerCount() > 1) {
             Log.d(TAG, "More than one pointer: " + event.getPointerCount());
         }
         for (int i = 0; i < event.getPointerCount(); i++) {
             pid = event.getPointerId(i);
             action = event.getAction();
             x = event.getX(i);
             y = event.getY(i);
             if (pid == 0) {
                 mEventTime = event.getEventTime() + mUptimeToMillis;
                 switch (action) {
                     case MotionEvent.ACTION_DOWN:
                     case MotionEvent.ACTION_POINTER_DOWN:
                         mHandledLongPress = false;
                         mTouchDownTime = mEventTime;
                         mTouchDown[0] = x;
                         mTouchDown[1] = y;
                         mRunner.touchDown(x, y, mTouchDownTime);
                         break;
                     case MotionEvent.ACTION_UP: //Check for tap.
                         mRunner.touchUp(x, y, mEventTime);
                         if (mEventTime - mTouchDownTime < mTapTimeout &&
                                 Math.abs(mTouchDown[0] - x) < mTouchSlop &&
                                 Math.abs(mTouchDown[1] - y) < mTouchSlop) {
                             mRunner.touchTap(x, y, mEventTime);
                         } else {
                             if (!mHandledLongPress &&
                                     mEventTime - mTouchDownTime > mLongPressThreshold) {
                                 mRunner.longPress(x, y, mEventTime);
                             }
                         }
                         break;
                     case MotionEvent.ACTION_MOVE:
                         mRunner.touchMove(x, y, mEventTime);
                         if (Math.abs(mTouchDown[0] - x) > mTouchSlop ||
                             Math.abs(mTouchDown[1] - y) > mTouchSlop) {
                             mHandledLongPress = true;
                         }
                         break;
                 }
             }
         }
         return true;
     }
 
     /**
      * Returns the runner that is driven by this thread.
      * @return The CompatibilityRunner that is driven by this thread.
      */
     public CompatibilityRunner getRunner() {
         return mRunner;
     }
 
     /**
      * Sets the exceptionhandler for the thread. If the thread is already created the exceptionhandler
      * will be set to it. Otherwise the handler will be stored and set to the thread when created.
      * @param handler
      */
     public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
         mExceptionHandler = handler;
         if (mWorkerThread != null) {
             mWorkerThread.setUncaughtExceptionHandler(handler);
         }
     }
 
 }
