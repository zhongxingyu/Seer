 package com.stiandrobak.FlashLight;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class HelloActivity extends Activity {
     /**
      * Called when the activity is first created.
      */



     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         /*
         LinearLayout x = (LinearLayout)findViewById(R.id.mainStyle);
         x.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 float x = event.getX();
                 float y = event.getY();
                 float width = v.getWidth();
                 float height = v.getHeight();
                 v.setBackgroundColor(0);
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
         */
         Window mWindow = getWindow();
         WindowManager.LayoutParams lp = mWindow.getAttributes();
         lp.screenBrightness = 1;
         mWindow.setAttributes(lp);
     }
 
 
 }
