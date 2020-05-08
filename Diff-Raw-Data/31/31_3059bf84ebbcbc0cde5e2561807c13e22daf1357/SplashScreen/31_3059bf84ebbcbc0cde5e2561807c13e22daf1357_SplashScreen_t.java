 package edu.dartmouth.mhb;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class SplashScreen extends Activity {
 
	private final int DURATION = 3000;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.activity_splashscreen);
                 
         new Handler().postDelayed(new Runnable() {
                 public void run() {
                         startActivity(new Intent(SplashScreen.this, MainActivity.class));
                         finish();
                 }
         }, DURATION);
     }
 }
