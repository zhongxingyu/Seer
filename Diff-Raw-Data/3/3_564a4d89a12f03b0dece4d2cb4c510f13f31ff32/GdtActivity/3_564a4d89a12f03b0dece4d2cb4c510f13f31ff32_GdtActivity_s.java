 /*
  * GdtActivity.java
  *
  * Copyright (c) 2011 Rickard Edström
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
  
 package gdt;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.net.Uri;
 import android.opengl.GLSurfaceView; 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 public abstract class GdtActivity extends Activity {
   private GdtView _view;
   
   @Override
   protected void onCreate(final Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);     
     
     requestWindowFeature(Window.FEATURE_NO_TITLE); 
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      
     _view = new GdtView(this);
     setContentView(_view); 
   }
   @Override
   protected void onPause() {
     super.onPause();
     _view.onPause();
   }
   @Override
   protected void onResume() {
     super.onResume();
     _view.onResume();
   }
 } 
  
 final class GdtView extends GLSurfaceView { 
   private final Object lock = new Object();
   
   public GdtView(final Context ctx) {
     super(ctx);
     //setEGLConfigChooser(8, 8, 8, 8, 24, 0);
     setRenderer(new Renderer() {
       public void onSurfaceCreated(GL10 _, EGLConfig __) {
         synchronized(lock) { Native.init(ctx); }
       }
       public void onSurfaceChanged(GL10 _, int width, int height) {
         synchronized(lock) { Native.eventResize(width, height); }
       } 
       public void onDrawFrame(GL10 _) {
         synchronized(lock) { Native.render(); }
       } 
     });
   } 
   public boolean onTouchEvent(final MotionEvent ev) {
     synchronized(lock) { 
       Native.eventTouch(ev.getAction(), ev.getX(), ev.getY());
     }
     return true;
   }
 }
 
 final class Native {
   private static Context _ctx;
 
   static {
     System.loadLibrary("native");
   }
   
   private Native() { } 
   
   static native void initialize();
   static native void render();
   static native void hide(boolean exitToo);   
   static native void eventTouch(int what, float x, float y);
   static native void eventResize(int width, int height);  
   
   static void init(Context ctx) {
     _ctx = ctx;
     initialize();
   }
   
   static Object[] openAsset(final String fileName) {
     final Object[] a = new Object[2];
     try { 
       final AssetFileDescriptor fd = _ctx.getAssets().openFd(fileName);
       final FileInputStream stream = fd.createInputStream();
       final FileChannel channel = stream.getChannel();
       a[0] = channel.map(FileChannel.MapMode.READ_ONLY, channel.position(), fd.getLength());// the bytebuffer
       a[1] = channel; // this will be closed when resource is unloaded
       return a;     
     } catch (final IOException e) {
       return null;
     } 
   }   
   
   static boolean cleanAsset(final Object arr) { 
       try {
         Object[] objArr = (Object[])arr;
         ((FileChannel)(objArr[1])).close();
       } catch (final Throwable t) {
         return false;
       }
       return true;
   }
   
   static void openUrl(final String url) {
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setData(Uri.parse(url));
     _ctx.startActivity(intent);
   }
   
   static void setVirtualKeyboardMode(int mode) {
     
   }
   
   static void gcCollect() {
     System.gc();
   }
 }
