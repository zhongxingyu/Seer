 package com.storassa.android.scuolasci;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 
 public class MainActivity extends Activity {
 	
	RelativeLayout rl;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
		rl = (RelativeLayout)findViewById(R.id.main_screen);
 		
		rl.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(MainActivity.this, StartingActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
