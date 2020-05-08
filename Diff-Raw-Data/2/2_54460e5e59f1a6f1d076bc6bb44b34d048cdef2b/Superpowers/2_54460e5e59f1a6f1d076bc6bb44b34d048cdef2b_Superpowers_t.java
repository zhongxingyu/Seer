 package com.adaburrows.superpowers;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.os.Bundle;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 
 public class Superpowers extends Activity {    
   private Preview mPreview;
   private MagicOverlay mOverlay;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // Hide the window title.
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     mOverlay = new MagicOverlay(this);
     mPreview = new Preview(this, mOverlay);
     setContentView(mPreview);
    //setContentView(R.layout.main);
     addContentView(mOverlay, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
   }
 }
