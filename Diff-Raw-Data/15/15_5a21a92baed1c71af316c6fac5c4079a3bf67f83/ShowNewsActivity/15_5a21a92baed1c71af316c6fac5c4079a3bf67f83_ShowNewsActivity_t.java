 package com.kennyproductions.rtxapp;
 
 import android.annotation.TargetApi;
 import android.app.ListActivity;
 //import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 
 public class ShowNewsActivity extends ListActivity {
 
 	
 	protected String[] mDummyNewsItems ={
 		"RTX is awesome",
 		"More Zombies at RTX requested",
 		"Dancers go crazy at RTX"
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_show_news);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 //		Intent intent = getIntent();
 		
		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDummyNewsItems);
 		setListAdapter(adapter);
	
 	}
 
 	
 	//necessary code to support more SDKs. Target SDK is high enough but min is only 8
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	//Holding this here, going to put options to change data source here I think
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.show_news, menu);
 //		return true;
 //	}
 
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
