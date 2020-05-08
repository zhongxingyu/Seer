 package com.example.therunningapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.RadioButton;
 import android.support.v4.app.NavUtils;
 
 public class Interval extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_interval);
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.interval, menu);
 		return true;
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
 	
 	public void onRadioButtonClicked(View view){
 		boolean checked = ((RadioButton) view).isChecked();
 		
 		switch(view.getId()){
 			case R.id.A_radiobutton_time:
				if(checked){
					findViewById(R.id.textView_time_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.editText_time_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.textView_distance_interval).setVisibility(View.GONE);
					findViewById(R.id.editText_distance_interval).setVisibility(View.GONE);
				}break;
 			case R.id.A_radiobutton_distance:
				if(checked){
					findViewById(R.id.textView_time_interval).setVisibility(View.GONE);
					findViewById(R.id.editText_time_interval).setVisibility(View.GONE);
					findViewById(R.id.textView_distance_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.editText_distance_interval).setVisibility(View.VISIBLE);
				}break;
 		}
 	}
 
 }
