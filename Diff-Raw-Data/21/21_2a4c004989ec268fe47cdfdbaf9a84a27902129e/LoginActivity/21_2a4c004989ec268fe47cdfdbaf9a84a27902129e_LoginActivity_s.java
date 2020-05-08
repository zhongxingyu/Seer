 package org.rosedu.examplanner;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class LoginActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_login, menu);
         return true;
     }
 }
