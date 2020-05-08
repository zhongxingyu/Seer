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
 
 import javax.microedition.khronos.egl.EGL10;
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.egl.EGLContext;
 import javax.microedition.khronos.egl.EGLDisplay;
 import javax.microedition.khronos.egl.EGLSurface;
 
 import android.content.Context;
 
 import com.super2k.openglen.EGLRenderer;
 import com.super2k.openglen.RenderSetting;
import com.super2k.openglen.Renderer;
 import com.super2k.openglen.SurfaceConfiguration;
 import com.super2k.openglen.android.utils.EGLUtils;
 import com.super2k.openglen.utils.JavaUtils;
 import com.super2k.openglen.utils.Log;
 import com.super2k.openglen.utils.Logger;
 
 /**
  * Class implementing an EGLRenderer on OpenGL ES 2.0
  * This class is used when controll of EGL is needed, for instance
  * when using a SurfaceView. Rendering is driven by the applications (clients) thread.
  * @author Richard Sahlin
  *
  */
 public class GLES20EGLRenderer extends GLES20Renderer implements EGLRenderer {
 
     /**
      * Number of times to retry eglMakeCurrent
      */
     private final int MAKE_CURRENT_TRIES = 3;
 
     /**
      * Number of millis to sleep between trying EGL make current.
      */
     private final int MAKE_CURRENT_RETRY_SLEEP = 100;
 
     private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098; // EGL 1.3 to set client version
 
     private final int EGL_OPENGL_ES2_BIT = 4;
 
     private final String TAG = getClass().getSimpleName();
 
     protected EGL10 mEgl;
 
     protected EGLDisplay mEglDisplay;
 
     protected int[] mVersion = new int[2];
 
     protected EGLConfig mEglConfig;
 
     protected EGLSurface mEglSurface;
 
     protected EGLContext mEglContext;
 
     protected SurfaceConfiguration  mSurfaceConfig;
 
     /**
      * Creates a new GLES20 EGL renderer, this shall be used when more controll of EGL is needed.
      * Renderring is done on a SurfaceView
      * createEGL must be called before the renderer is initialized.
      * @param surfaceConfig The surface configuration
      * @param renderSetting The renderer settings. May be null to create a default setting.
      * @throws IllegalArgumentException if surfaceConfig, surfaceHolder is NULL
      */
     public GLES20EGLRenderer(SurfaceConfiguration surfaceConfig,
             Context context,
             RenderSetting renderSetting)    {
         super(renderSetting);
         if (surfaceConfig == null)
             throw new IllegalArgumentException("SurfaceConfiguration or SurfaceHolder is NULL");
         mSurfaceConfig = surfaceConfig;
         //Graphics lib needed to create EGL - create now
         createGraphicsLibraryUtilities();
     }
 
     @Override
     public Object createEGL(Object display, Object surface) {
 
        Log.d(Renderer.OPENGLEN_TAG, "createEGL: surface = " + surface);
         mEgl = (EGL10)EGLContext.getEGL();
         if (display != null)
             mEglDisplay = mEgl.eglGetDisplay(display);
         else
             mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
 
         int[] mVersion = new int[2];
         boolean initialized = mEgl.eglInitialize(mEglDisplay, mVersion);
 
         if (!initialized){
             int error = mEgl.eglGetError();
             throw new IllegalArgumentException("Could not initialize display " + error);
         }
         Log.d(TAG, "EGL Initialized, version:" + mVersion[0] + "." + mVersion[1]);
         int[]    configSpec = new int[]{
                 EGL10.EGL_RED_SIZE, mSurfaceConfig.getRedBits(),
                 EGL10.EGL_GREEN_SIZE, mSurfaceConfig.getGreenBits(),
                 EGL10.EGL_BLUE_SIZE, mSurfaceConfig.getBlueBits(),
                 EGL10.EGL_ALPHA_SIZE, mSurfaceConfig.getAlphaBits(),
                 EGL10.EGL_DEPTH_SIZE, mSurfaceConfig.getDepthBits(),
                 EGL10.EGL_SAMPLES, mSurfaceConfig.getSamples(),
                 EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                 EGL10.EGL_CONFIG_CAVEAT, EGL10.EGL_NONE,
                 EGL10.EGL_NONE };
         //Fix for Tegra, reports no configs if EGL samples is 1 (or more).
         if (mSurfaceConfig.getSamples()<= 1) {
             configSpec = new int[]{
                     EGL10.EGL_RED_SIZE, mSurfaceConfig.getRedBits(),
                     EGL10.EGL_GREEN_SIZE, mSurfaceConfig.getGreenBits(),
                     EGL10.EGL_BLUE_SIZE, mSurfaceConfig.getBlueBits(),
                     EGL10.EGL_ALPHA_SIZE, mSurfaceConfig.getAlphaBits(),
                     EGL10.EGL_DEPTH_SIZE, mSurfaceConfig.getDepthBits(),
                     EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                     EGL10.EGL_CONFIG_CAVEAT, EGL10.EGL_NONE,
                     EGL10.EGL_NONE };
         }
         EGLConfig[] configs = new EGLConfig[20];
         int[] num_config = new int[1];
         mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 20, num_config);
         if (num_config[0] == 0) {
             throw new IllegalArgumentException("No EGL config, eglChooseConfig returns 0 configs.");
         }
 
         for (int i = 0; i < num_config[0]; i++) {
             mGraphicsUtilities.logConfig(mEgl, mEglDisplay, configs[i], Logger.LOGLEVEL_VERBOSE);
         }
 
         //Verify config so that we don't get a bogus config.
         mEglConfig = EGLUtils.selectConfig(mEgl, mEglDisplay, configs, configSpec, num_config[0]);
         if (mEglConfig == null) {
             throw new IllegalArgumentException("Could not select matching EGL config");
         }
         int[] attrib_list = {
                 EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
         };
         mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT,
                                             attrib_list);
         int error = mEgl.eglGetError();
         if (error != EGL10.EGL_SUCCESS) {
             throw new IllegalArgumentException("Could not create EGLContext:" + error);
         }
         mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null);
 
         //define EGL_SWAP_BEHAVIOR       0x3093
         int[] result = new int[1];
         if (mEgl.eglQuerySurface(mEglDisplay, mEglSurface, 0x3093, result)) {
             //#define EGL_BUFFER_PRESERVED        0x3094  /* EGL_SWAP_BEHAVIOR value */
             //#define EGL_BUFFER_DESTROYED        0x3095  /* EGL_SWAP_BEHAVIOR value */
             if (result[0] == 0x3094) {
                 Log.d(TAG, "EGL Swap buffer behavior=EGL_BUFFER_PRESERVED");
             } else if (result[0] == 0x3095) {
                 Log.d(TAG, "EGL Swap buffer behavior=EGL_BUFFER_DESTROYED");
             } else {
                 Log.d(TAG, "Unknown EGL Swap buffer behavior=" + result[0]);
             }
         } else {
             Log.d(TAG, "Could not get EGL_SWAP_BEHAVIOR");
         }
 
         //Log chosen configuration.
         Log.d(TAG, "Chosen EGL configuration:");
         mGraphicsUtilities.logConfig(mEgl, mEglDisplay, mEglConfig, Logger.LOGLEVEL_INFO);
 
         //If make current fails, then sleep and try again for a number of times.
         int tries = 0;
         boolean success = false;
         while (!(success = mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext))
                 && tries++ < MAKE_CURRENT_TRIES) {
             Log.d(TAG, "Could not make EGL current: " + mEgl.eglGetError());
             try {
                 JavaUtils.stabilizeFreeMemory();
                 Thread.sleep(MAKE_CURRENT_RETRY_SLEEP);
             }
             catch (InterruptedException ie) {
                 //Don't do anything if interrupted.
             }
 
         }
         if (!success) {
             throw new IllegalArgumentException("Could not make EGL current");
         }
 
 
         return mEglConfig;
 
     }
 
     @Override
     public void releaseEGL() {
         Log.d(TAG, "releaseEGL");
         //Release the current context.
         if (mEgl == null) {
             Log.d(TAG, "EGL is null, already released?");
             return;
         } else {
             if (!mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                             EGL10.EGL_NO_CONTEXT)) {
                 Log.d(TAG, "Could not make egl current with no display: " + mEgl.eglGetError() );
             }
             if (mEglSurface == null) {
                 Log.d(TAG, "EGLSurface is null");
             } else if (!mEgl.eglDestroySurface(mEglDisplay, mEglSurface)) {
                 Log.d(TAG, "Could not destroy surface: " + mEgl.eglGetError() );
             }
 
             if (mEglContext == null) {
                 Log.d(TAG, "EGLContext is null");
             } else if (!mEgl.eglDestroyContext(mEglDisplay, mEglContext)) {
                     Log.d(TAG, "Could not destroy context: " + mEgl.eglGetError() );
 
             }
             mEgl = null;
             mEglSurface = null;
             mEglContext = null;
             mEglDisplay = null;
         }
 
     }
 
     @Override
     public boolean swapBuffers() {
 
         if (mEgl != null) {
             boolean result = mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
             if (!result) {
                 Log.d(TAG, "Could not swap buffers: " + mEgl.eglGetError());
                 releaseEGL();
             }
             return result;
 
         } else {
             finish();
         }
         return true;
     }
 
 
     @Override
     public Object getEGLConfig() {
         return mEglConfig;
     }
 
 }
