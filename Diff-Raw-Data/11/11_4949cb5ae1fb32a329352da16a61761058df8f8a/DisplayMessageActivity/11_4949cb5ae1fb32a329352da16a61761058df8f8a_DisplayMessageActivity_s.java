 package com.example.myfirstapp;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class DisplayMessageActivity extends Activity {
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Get the message from the Intent
 		Intent intent = getIntent();
 		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
 		
 		// Create the text view
 		TextView textView = new TextView(this);
 		textView.setTextSize(40);
 		textView.setText(message);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
