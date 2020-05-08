 package com.level42.mixit.activities;
 
 import roboguice.activity.RoboSplashActivity;
 import roboguice.inject.ContentView;
 import android.app.Application;
 import android.content.Intent;
import android.os.Bundle;
 import android.os.Looper;
 
 import com.level42.mixit.R;
 
 /**
  * Activité de lancement de l'application (Splash Screen).
  */
 @ContentView(R.layout.activity_splash)
 public class SplashActivity extends RoboSplashActivity {
 
     /*
      * (non-Javadoc)
     * @see roboguice.activity.RoboSplashActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
    
    /*
     * (non-Javadoc)
      * @see roboguice.activity.RoboSplashActivity#startNextActivity()
      */
     @Override
     protected void startNextActivity() {
         Intent intent = new Intent(this.getApplicationContext(),
                 PlanningActivity.class);
         this.startActivity(intent);
     }
 
     /*
      * (non-Javadoc)
      * @see roboguice.activity.RoboSplashActivity#doStuffInBackground(android.app.Application)
      */
     @Override
     protected void doStuffInBackground(Application app) {
         Looper.prepare();
         super.doStuffInBackground(app);
     }
 }
