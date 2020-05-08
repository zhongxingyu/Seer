 package com.hades.grid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
import android.widget.ImageButton;
 
 public class Level1 extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_level_1);
 		
		ImageButton[][] iButton = new ImageButton[5][5];
		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		return true;
 	}
 }
