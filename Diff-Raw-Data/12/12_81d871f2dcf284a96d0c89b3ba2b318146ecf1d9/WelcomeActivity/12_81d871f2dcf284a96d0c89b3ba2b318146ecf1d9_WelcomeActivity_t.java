 package com.ben.software.activity;
 
 //import java.util.Timer;
 //import java.util.TimerTask;
 
 import com.ben.software.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Window;
 
 public class WelcomeActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
        Intent intent = new Intent(WelcomeActivity.this, FunctionListActivity.class);
         startActivity(intent);
        finish();
 
 //        TimerTask timerTask = new TimerTask() {
 //            @Override
 //            public void run() {
 //                Intent intent = new Intent(Welcome.this, FunctionList.class);
 //                startActivity(intent);
 //                Welcome.this.finish();
 //            }
 //        };
 //        Timer timer = new Timer();
 //        timer.schedule(timerTask, 2000);
     }
 }
