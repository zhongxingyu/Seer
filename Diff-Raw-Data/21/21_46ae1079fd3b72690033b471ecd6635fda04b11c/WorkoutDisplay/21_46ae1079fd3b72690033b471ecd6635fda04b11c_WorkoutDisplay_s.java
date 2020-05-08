 package com.example.therunningapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 
 public class WorkoutDisplay extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_workout_display);
 		// Show the Up button in the action bar.
 		TrappDBHelper mDbHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		setupActionBar();
 		Intent intent = getIntent();
 		String temp_2 = intent.getStringExtra("id");
		TextView viewDate = (TextView) findViewById(R.id.date);
		TextView viewCalories = (TextView) findViewById(R.id.calories);
		TextView viewDistance = (TextView) findViewById(R.id.distance);
 
		String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_DATE, TrappEntry.COLUMN_NAME_CALORIES, TrappEntry.COLUMN_NAME_DISTANCE};
 		final Cursor c = db.query(TrappEntry.TABLE_NAME, projection, "_ID=?", new String[] { temp_2 }, null,null,null,null);
 		
 		if(c.moveToFirst()){
 			String date = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DATE));
 			String calories = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_CALORIES));
 			String distance = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DISTANCE));
 			viewDate.setText(date);
 			viewCalories.setText(calories);
 			viewDistance.setText(distance);
 	}
 		
 		
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
 		getMenuInflater().inflate(R.menu.workout_display, menu);
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
 
 }
