 package com.starlon.froyvisuals;
 
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.opengl.GLSurfaceView;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Typeface;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 import android.opengl.GLU;
 import android.opengl.GLSurfaceView.Renderer;
 import android.opengl.GLUtils;
 
 import javax.microedition.khronos.egl.EGL10;
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.egl.EGLContext;
 import javax.microedition.khronos.egl.EGLDisplay;
 import javax.microedition.khronos.opengles.GL10;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 
 class FroyVisualsView extends GLSurfaceView {
     private static final String TAG = "FroyVisuals/FroyVisualsView";
     private NativeHelper mNativeHelper;
     private FroyVisuals mActivity;
     private Stats mStats;
 
 
     //AudioRecord recorder = findAudioRecord();
     public FroyVisualsView(Context context) {
         super(context);
 
         mActivity = (FroyVisuals)context;
 
         init(false, 0, 0);
     }
 
     public FroyVisualsView(Context context, boolean translucent, int depth, int stencil) {
         super(context);
 
         mActivity = (FroyVisuals)context;
 
         init(translucent, depth, stencil);
     }
 
 
     private void init(boolean translucent, int depth, int stencil)
     {
         if (translucent) {
             this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
         }
 
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
     }
 
     private int direction = -1;
     @Override public boolean onTouchEvent (MotionEvent event) 
     {
         int action = event.getAction();
         float x = event.getX();
         float y = event.getY();
         switch(action)
         {
             case MotionEvent.ACTION_DOWN:
                 direction = -1;
             break;
             case MotionEvent.ACTION_UP:
                 if(direction >= 0) {
                    Log.w(TAG, "Switching actor: " + direction);
                     mNativeHelper.finalizeSwitch(direction);
                 }
             break;
             case MotionEvent.ACTION_MOVE:
                 //mNativeHelper.mouseMotion(x, y);
                 int size = event.getHistorySize();
                 if(size > 1)
                 {
                     if(event.getHistoricalX(0, 0) < event.getHistoricalX(0, 1))
                     {
                         direction = 0;
                     }
                     else
                     {
                         direction = 1;
                     }
                 }
             break;
         }
         return true;    
     }
 }
 
 
 
 
         
