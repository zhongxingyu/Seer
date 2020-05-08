 
 package com.franktom.horizon;
 
 import com.franktom.horizon.util.SystemUiHider;
 
 //import android.annotation.TargetApi;
 import android.app.Activity;
 //import android.content.Context;
 import android.content.pm.ActivityInfo;
 //import android.database.Cursor;
 //import android.graphics.Bitmap;
 //import android.graphics.BitmapFactory;
 //import android.graphics.Canvas;
 //import android.graphics.BitmapFactory.Options;
 //import android.graphics.Color;
 //import android.graphics.Paint;
 //import android.graphics.Paint.Align;
 import android.hardware.Camera;
 //import android.hardware.Camera.PreviewCallback;
 //import android.net.Uri;
 //import android.os.Build;
 import android.os.Bundle;
 //import android.os.Handler;
 //import android.text.format.Formatter;
 //import android.util.DisplayMetrics;
 //import android.util.Log;
 //import android.view.MotionEvent;
 //import android.view.Surface;
 //import android.view.SurfaceHolder;
 //import android.view.SurfaceView;
 //import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 //import android.widget.FrameLayout;
 //import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 //import java.io.FileNotFoundException;
 //import java.io.FileOutputStream;
 //import java.io.IOException;
 //import java.lang.Math;
 //import java.util.ArrayList;
 //import java.util.Collections;
 //import java.util.Comparator;
 //import java.util.Iterator;
 //import java.util.List;
 //import java.util.Locale;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class CompassActivity extends Activity {
 
     private CompassView mSimulationView;
     private CameraPreview mCameraPreview;
     private RelativeLayout layout;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
       //Remove title bar
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
         //Remove notification bar
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         layout = new RelativeLayout(this);
 
 
 
         mCameraPreview = new CameraPreview(this);
         Camera.Parameters p = mCameraPreview.camera.getParameters();
 
 //      setContentView(mCameraPreview);
         layout.addView(mCameraPreview);
 
         mSimulationView = new CompassView(this);
         if (p.getHorizontalViewAngle()<360.0)
            mSimulationView.mCameraViewAngleHorizontal = p.getHorizontalViewAngle();
         if (p.getVerticalViewAngle()<360.0)
            mSimulationView.mCameraViewAngleVertical = p.getVerticalViewAngle();
 
 //      setContentView(mSimulationView);
         layout.addView(mSimulationView);
 
         setContentView(layout);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
//        mSimulationView.startSimulation();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
//        mSimulationView.stopSimulation();
 
     }
 
 }
