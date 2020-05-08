 package com.vitaltech.bioink;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.util.Log;
 import android.view.Menu;
 
 // master
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        //Log.e("ErrorType","Error msg here");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
