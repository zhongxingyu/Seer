 package com.geekytheory.miguelcatalandev.developerdays;
 
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

 import android.os.Bundle;
 
public class Main_Activity_Main extends SherlockActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return true;
 	}
 
 }
