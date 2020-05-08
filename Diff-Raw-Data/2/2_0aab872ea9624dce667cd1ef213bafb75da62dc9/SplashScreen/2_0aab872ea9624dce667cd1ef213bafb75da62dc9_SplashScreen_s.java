 package com.qrsphere;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 public class SplashScreen extends Activity {
 
 	protected boolean _active = true;
     protected int _splashTime = 5000;
    
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         ImageView iv = new ImageView(this);
         iv.setImageResource(R.drawable.q_logo);	
         iv.setBackgroundColor(Color.TRANSPARENT);
         setContentView(iv);
        
         Thread splashTread = new Thread() {
 
 			@Override
             public void run() {
                 try {
                     int waited = 0;
                     while(_active && (waited < _splashTime)) {
                         sleep(100);
                         if(_active) {
                             waited += 100;
                         }
                     }
                 } catch(InterruptedException e) {
                     // do nothing
                 } finally {
                     finish();
                     // start mainActivity
                    startActivity(new Intent("com.example.tabview_test.MainActivity"));
                     // stop(); //android does not support stop() any more, following code could be a way to exit
 //                  shouldContinue = false;
 //                  join();
               }
           }
       };
       splashTread.start();
   }
  
   @Override
   public boolean onTouchEvent(MotionEvent event) {
       if (event.getAction() == MotionEvent.ACTION_DOWN) {
           _active = false;
       }
       return true;
   }
 }
 
