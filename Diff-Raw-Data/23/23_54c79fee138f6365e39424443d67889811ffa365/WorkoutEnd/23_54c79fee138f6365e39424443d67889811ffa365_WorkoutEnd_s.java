 package com.example.therunningapp;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class WorkoutEnd extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_workout_end);
 		TrappDBHelper mDbHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		
 		TextView viewDate = (TextView) findViewById(R.id.textdate_display);
 		TextView viewCalories = (TextView) findViewById(R.id.textcalories_display);
 		TextView viewDistance = (TextView) findViewById(R.id.textdistance_display);
 		TextView viewTime = (TextView) findViewById(R.id.texttime_display);
 
 		String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_DATE, TrappEntry.COLUMN_NAME_CALORIES, TrappEntry.COLUMN_NAME_DISTANCE, TrappEntry.COLUMN_NAME_TIME};
 		final Cursor c = db.query(TrappEntry.TABLE_NAME, projection, null, null,null,null,null);
 		
 		if(c.moveToLast()){
 			String date = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DATE));
 			String calories = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_CALORIES));
 			String distance = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_DISTANCE));
 			String time = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_TIME));
 			viewDate.setText(date);
 			viewCalories.setText(calories);
 			viewDistance.setText(distance);
 			viewTime.setText(time);
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.workout_end, menu);
 		return true;
 	}
 
 }
