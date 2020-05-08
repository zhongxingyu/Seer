 package org.lpgtug.farmacias;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Splash extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.splash);
         
         // Run next activity
         Intent intent = new Intent();
         intent.setClass(this, TurnoService.class);
         startService(intent);
         
         final Activity activity = this;        
         final Thread t = new Thread(){
             @Override
             public void run(){
                 try {
                     synchronized(this){
                         // Wait given period of time or exit on touch
                         wait(5000);
                     }
                 } catch(InterruptedException ex) {                    
                 }          
                 // Run next activity
                 Intent intent = new Intent();
                intent.setClass(activity, Main.class);
                 startActivity(intent);
                 finish();
             }
         };
         
         t.start();
     }
 
 }
