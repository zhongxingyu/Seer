 package com.Alberto97.torch;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class MainActivity extends Activity 
   {
     @Override
     public void onCreate(Bundle savedInstanceState) 
      {
         super.onCreate(savedInstanceState);
         // remove title
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.activity_main);
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1L;
        getWindow().setAttributes(layout);
 		
 	 }
   }
