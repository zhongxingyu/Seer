 package com.chalmers.schmaps;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MenuActivity extends Activity implements View.OnClickListener {
 	Button searchHall;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_menu);
         assignInstances();
     }
 
     private void assignInstances() {
         searchHall = (Button) findViewById(R.id.hButton);
         searchHall.setOnClickListener(this);
 		
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_menu, menu);
         return true;
     }
 
 	public void onClick(View v) {
		Intent startMapActivity = new Intent("android.intent.action.MAPACTIVITY");
 		startActivity(startMapActivity);		
 	}
 }
