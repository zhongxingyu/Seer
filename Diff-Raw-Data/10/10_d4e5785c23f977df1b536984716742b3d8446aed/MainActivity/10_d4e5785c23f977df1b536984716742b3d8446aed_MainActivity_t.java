 package com.msoe.ce4960.sommere.lab5;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
 
 public class MainActivity extends Activity{
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity, menu);
	    return true;
	}
 }
