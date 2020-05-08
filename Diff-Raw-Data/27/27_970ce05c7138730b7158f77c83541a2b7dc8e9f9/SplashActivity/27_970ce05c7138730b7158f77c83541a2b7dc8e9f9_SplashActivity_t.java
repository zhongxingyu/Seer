 package es.ucm.jorngeren13;
 
import com.actionbarsherlock.view.Window;

 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 
public class SplashActivity extends JG13Activity {
 
    private final int SPLASH_DISPLAY_LENGHT = 3000;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.splash);
 
         /* New Handler to start the Menu-Activity 
          * and close this Splash-Screen after some seconds.*/
         new Handler().postDelayed(new Runnable(){
             @Override
             public void run() {
                 /* Create an Intent that will start the Menu-Activity. */
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
             }
         }, SPLASH_DISPLAY_LENGHT);
     }
 }
