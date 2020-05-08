 package com.template.android;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.view.MotionEvent;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.egl.EGLConfig;
 
 class BasicView extends GLSurfaceView {
 
     public BasicView(Context context) {
         super(context);
 
         setEGLConfigChooser(8, 8, 8, 0, 16, 0);
         setEGLContextClientVersion(2);
         setRenderer(new Renderer());
     }
 
     @Override
     public boolean onTouchEvent(final MotionEvent event) {
         NativeInterface.onTouchEvent(0, 0, 0, 0);
 
         return true;
     }
 
     private static class Renderer implements GLSurfaceView.Renderer {
 
         public void onDrawFrame(GL10 glUnused) {
             NativeInterface.onDrawFrame();
         }
 
         public void onSurfaceChanged(GL10 glUnused, int width, int height) {
             NativeInterface.onSurfaceChanged(width, height);
         }
 
         public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
             NativeInterface.onCreate();
         }
 
     }
 
 }
