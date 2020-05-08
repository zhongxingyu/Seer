 package com.qayto.mobile;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 
 public class WaitingActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     	public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.waiting);
             
             Button connect = (Button) findViewById(R.id.button1);
             connect.setOnClickListener(new OnClickListener() {
                 public void onClick(View v) {
                 	Intent intent = new Intent();
     				intent.setClass(WaitingActivity.this, RatingsActivity.class);
                     startActivity(intent);
                 }
             });
                 
     }
 }
