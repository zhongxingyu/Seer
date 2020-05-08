 package com.smap.targetapp;
 
import java.util.Timer;
import java.util.TimerTask;

 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         Button button = (Button) findViewById(R.id.btnResult);
         button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.i("targetApp", "Set result: " + getIntent());
                 setResult(RESULT_OK, getIntent());
                 finish();
             }
         });
         
        Timer t = new Timer();
        t.schedule(new TimerTask() {
        	public void run() {
        		Log.i("targetApp", "Set result: " + getIntent());
                setResult(RESULT_OK, getIntent());
                finish();
        	}
        }, 5000);
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
