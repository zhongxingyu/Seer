 package es.ucm.jorngeren13;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 
public class SplashActivity extends Activity {
 
    private final int SPLASH_DISPLAY_LENGHT = 1500;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.splash);
 
         /* New Handler to start the Menu-Activity 
          * and close this Splash-Screen after some seconds.*/
         new Handler().postDelayed(new Runnable(){
             @Override
             public void run() {
                 /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this, HomeActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
             }
         }, SPLASH_DISPLAY_LENGHT);
     }
 }
