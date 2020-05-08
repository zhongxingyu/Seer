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
 		
 		//get the message from the intent//
 		Intent intent = getIntent();
 		String Message = intent.getStringExtra (MainActivity.EXTRA_MESSAGE);
 		
 		// create the text view //
 		TextView textView = new TextView(this);
 		textView.setTextSize(40);
 		textView.setText(Message);
 		
 		//set the text as the activity layout //
		setContentView(R.layout.activity_display_message);
 		
 		
 		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
