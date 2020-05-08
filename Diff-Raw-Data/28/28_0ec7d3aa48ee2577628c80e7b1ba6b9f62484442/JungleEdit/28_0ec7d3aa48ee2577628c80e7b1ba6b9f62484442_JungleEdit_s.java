 package com.LoLCompanionApp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class JungleEdit extends Activity {
	
 	DatabaseExtra database;
 	String defaultCreatureOrder;
	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.jungleedit);
 
 		initializeHeader();
 
 		database = new DatabaseExtra(this);
 
 		// set the default creature order
 		defaultCreatureOrder = database.getDefaultCreatureOrder();
 	}
	
	public void saveSettings(View buttonSave)
	{
		
	}
	
	public void defaultSettings(View buttonDefault)
	{
		
 	}
 
 
 	public void initializeHeader() {
 		TextView title = (TextView) findViewById(R.id.HeaderTitle);
 		title.setText("Jungle Timer - Settings");
 	}
 
 	public void back(View view) {
 		finish();
 	}
 
 }
