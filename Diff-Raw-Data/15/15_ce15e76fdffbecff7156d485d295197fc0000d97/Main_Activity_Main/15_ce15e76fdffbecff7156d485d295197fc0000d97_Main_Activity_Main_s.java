 package com.geekytheory.miguelcatalandev.developerdays;
 
 import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
 
public class Main_Activity_Main extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.main__activity__main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main__activity__main, menu);
 		return true;
 	}
 
 }
