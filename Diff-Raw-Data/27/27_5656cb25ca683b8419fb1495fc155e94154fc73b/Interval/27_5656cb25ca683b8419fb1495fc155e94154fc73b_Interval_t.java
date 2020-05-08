 package com.example.therunningapp;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
import android.widget.ToggleButton;
 import android.support.v4.app.NavUtils;
 
 
 public class Interval extends Activity {
 	String intervalType;
 	String Interval = "Interval";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_interval);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		intervalType = "time";
 		
 		//Get the DB
 		TrappDBHelper mDbHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		
 		ListView workoutList = (ListView) findViewById(R.id.listViewInterval); 
 		workoutList.setClickable(true);
 		
 		String sortOrder = TrappEntry._ID + " DESC";
 		
 		//Query the DB
 				final Cursor c = db.query(TrappEntry.TABLE_NAME_INTERVAL, null, null, null, null, null, sortOrder); 
 				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
 				
 				//Display the name of interval
 				if(c.moveToFirst()){
 					do{
 						String name = c.getString(c.getColumnIndex(TrappEntry.COLUMN_NAME_NAME));
 						adapter.add(name);
 					}while(c.moveToNext());
 					
 					workoutList.setAdapter(adapter);
 					
 					workoutList.setOnItemClickListener(new OnItemClickListener() {
 						  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 							  c.moveToPosition(position);	
 						      Intent intent = new Intent(Interval.this, WorkoutStart.class);
 						      intent.putExtra("run", c.getInt(c.getColumnIndex(TrappEntry.COLUMN_NAME_RUN_TIME)));	
 							  intent.putExtra("pause", c.getInt(c.getColumnIndex(TrappEntry.COLUMN_NAME_PAUSE_TIME)));
 							  intent.putExtra("rep", c.getInt(c.getColumnIndex(TrappEntry.COLUMN_NAME_REPETITION)));
 							  intent.putExtra("workoutType", Interval);
 						      startActivity(intent);
 						      finish();          
 						  }
 					});	
 				} 
 				db.close();
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
 	
 	//switch for radiobuttons, which only hide/show elements on the screen 
 	// based on what radiobutton is checked.
 	public void onRadioButtonClicked(View view){
 		boolean checked = ((RadioButton) view).isChecked();
 		
 		switch(view.getId()){
 			case R.id.A_radiobutton_time:
 				if(checked){
 					findViewById(R.id.textView_time_run_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.editText_time_run_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.textView_time_pause_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.editText_time_pause_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.ToggleButton_time_run_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.ToggleButton_time_pause_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.textView_distance_run_interval).setVisibility(View.GONE);
 					findViewById(R.id.editText_distance_run_interval).setVisibility(View.GONE);
 					findViewById(R.id.textView_distance_pause_interval).setVisibility(View.GONE);
 					findViewById(R.id.editText_distance_pause_interval).setVisibility(View.GONE);
 					intervalType = "time";
 				}break;
 			case R.id.A_radiobutton_distance:
 				if(checked){
 					findViewById(R.id.textView_time_run_interval).setVisibility(View.GONE);
 					findViewById(R.id.editText_time_run_interval).setVisibility(View.GONE);
 					findViewById(R.id.textView_time_pause_interval).setVisibility(View.GONE);
 					findViewById(R.id.editText_time_pause_interval).setVisibility(View.GONE);
					findViewById(R.id.ToggleButton_time_run_interval).setVisibility(View.GONE);
					findViewById(R.id.ToggleButton_time_pause_interval).setVisibility(View.GONE);
 					findViewById(R.id.textView_distance_run_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.editText_distance_run_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.textView_distance_pause_interval).setVisibility(View.VISIBLE);
 					findViewById(R.id.editText_distance_pause_interval).setVisibility(View.VISIBLE);
 					intervalType = "distance";
 				}break;
 		}
 	}
 	
 	public void cancel(View view){
 		finish();
 	}
 	
 	public void save(View view){
 		int run = 0;
 		int pause = 0;
 		int rep = 0;
 		
 		TrappDBHelper mDBHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDBHelper.getWritableDatabase();
 		
 		if(intervalType == "time")
 		{
 			EditText run_time = (EditText) findViewById(R.id.editText_time_run_interval);
 			EditText pause_time = (EditText) findViewById(R.id.editText_time_pause_interval);
			
 			run = Integer.parseInt(run_time.getText().toString());
 			pause = Integer.parseInt(pause_time.getText().toString());

			run = onToggleClicked(findViewById(R.id.ToggleButton_time_run_interval), run);
			pause = onToggleClicked(findViewById(R.id.ToggleButton_time_pause_interval), pause);
 		}
 		else if(intervalType == "distance")
 		{
 			EditText run_distance = (EditText) findViewById(R.id.editText_distance_run_interval);
 			EditText pause_distance = (EditText) findViewById(R.id.editText_distance_pause_interval);
 	
 			run = Integer.parseInt(run_distance.getText().toString());
 			pause = Integer.parseInt(pause_distance.getText().toString());
 		}
 		
 		EditText repitition = (EditText) findViewById(R.id.editText_repetition_interval);
 		EditText name1 = (EditText) findViewById(R.id.editText_name_interval);
 		
 		rep = Integer.parseInt(repitition.getText().toString());
 		String name = name1.getText().toString();
 		
 		ContentValues values = new ContentValues();
 		values.put(TrappEntry.COLUMN_NAME_NAME, name);
 		values.put(TrappEntry.COLUMN_NAME_RUN_TIME, run);
 		values.put(TrappEntry.COLUMN_NAME_PAUSE_TIME, pause);
 		values.put(TrappEntry.COLUMN_NAME_REPETITION, rep);
 		db.insert(TrappEntry.TABLE_NAME_INTERVAL, null, values);
 		
 		Intent intent = new Intent(this, WorkoutStart.class);
 		intent.putExtra("run", run);	
 		intent.putExtra("pause", pause);
 		intent.putExtra("rep", rep);
 		intent.putExtra("workoutType", Interval);
 		startActivity(intent); 
 		finish(); 
 	}
	
	public int onToggleClicked(View view, int time) {
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	        return time*60; // Minutes
	    } else {
	        return time; // Seconds
	    }
	}
 }
