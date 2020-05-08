 package com.softwerk.coffee2;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.CompoundButton;
 import android.widget.ProgressBar;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import android.os.Handler;
 
 
 public class MainActivity extends Activity implements Switch.OnCheckedChangeListener {
     private TextView statusText;
     private ProgressBar progressBar;
     private Handler handler = new Handler();
     private Switch coffeeSwitch;
     boolean check;
     boolean error = false;
     Animation anim;
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.softwerkcoffee);
 
         statusText = (TextView) findViewById(R.id.statusText);
         progressBar = (ProgressBar) findViewById(R.id.progressBar);
         coffeeSwitch = (Switch) findViewById(R.id.coffeeSwitch);
 
         coffeeSwitch.setOnCheckedChangeListener(this);
         progressBar.setProgress(0);
         progressBar.setMax(100);
 
         anim = new AlphaAnimation(0.0f, 1.0f);
         anim.setDuration(50);
         anim.setStartOffset(20);
         anim.setRepeatMode(Animation.REVERSE);
         anim.setRepeatCount(5);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.softwerk_coffeee, menu);
         return true;
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         if(buttonView == coffeeSwitch){
             if(isChecked){
                 check = true;
             } else if(!isChecked){
                 check = false;
             }
 
             new Thread(new Runnable() {
                 int p = 0;
                 public void run() {
                     while (p < 100) {
 
                         try {
                             Thread.sleep(100);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
 
                         handler.post(new Runnable() {
                             public void run() {
                                 if(check && !error){
                                     progressBar.setProgress(p);
                                     setStatusText(p+"%", error);
                                 }
                                 if (!check && !error) {
                                     progressBar.setProgress(0);
                                     setStatusText("STOPPED!", error);
                                     p = 100;
                                 }
                                 if(p == 50/*this value only simulates the error message, in future put a another statement*/){
                                    progressBar.setProgress(50/*should be p*/);
                                     error = true;
                                     setStatusText("ERROR!", error);
                                     p = 100;
                                    error = false;
                                 }
                             }
                         });
                         p++;
                     }
                 }
             }).start();
         }
     }
 
     public void setStatusText(String status, boolean error){
         statusText.setVisibility(1);
         statusText.setText(status);
          if(error){
             statusText.startAnimation(anim);
              coffeeSwitch.setChecked(false);
         } if (!error){
             statusText.clearAnimation();
         }
     }
 }
