 package org.serviterobotics.friarbots;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class FriarBotsActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.menu);
     }
    public void onActionBarButtonClick(View view) {
     	startActivity(new Intent(FriarBotsActivity.this, StatusActivity.class));
     }
 }
