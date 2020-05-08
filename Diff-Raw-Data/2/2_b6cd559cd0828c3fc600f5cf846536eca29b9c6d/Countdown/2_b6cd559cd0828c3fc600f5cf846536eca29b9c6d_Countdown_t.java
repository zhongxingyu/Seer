 package com.lyu.inoodles.presentation;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.widget.TextView;
 
 import com.lyu.inoodles.R;
 
 public class Countdown extends GlobalActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.countdown);
         Bundle extras = getIntent().getExtras();
         int millisTimer = extras.getInt("time", 180);
        
         new CountDownTimer(millisTimer * 1000, 1000) {
             TextView mTextField = (TextView) findViewById(R.id.mTextField);
 
             public void onTick(long millisUntilFinished) {
                 int seconds = (int) millisUntilFinished / 1000;
                 mTextField
                         .setText((seconds / 60) + "'" + (seconds % 60) + "\"");
             }
 
             public void onFinish() {
                 mTextField.setText("done!");
                 // (audio();
             }
         }.start();
 
     }
 
 }
