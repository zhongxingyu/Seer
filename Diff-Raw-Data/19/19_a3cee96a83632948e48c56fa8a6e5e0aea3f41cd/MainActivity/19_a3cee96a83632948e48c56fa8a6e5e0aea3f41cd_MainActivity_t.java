 package com.allen.mapple;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
     private static final String TAG = "com.allen.mapple.MainActivity";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         String token = null;
         if(savedInstanceState != null) {
             token = savedInstanceState.getString("auth_token");
         }
         if(token == null) {
             Intent intent = new Intent(this, SignupActivity.class);
             startActivityForResult(intent, 0);
         } else {
             setupActivity();
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(resultCode == RESULT_CANCELED) {
             finish();
         } else {
             setupActivity();
         }
     }
 
     protected void setupActivity() {
        Log.d(TAG, "setting up map");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
