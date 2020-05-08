 package edu.colorado.trackers.workout;
 
 import edu.colorado.trackers.R;
 import edu.colorado.trackers.db.*;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.DialogFragment;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 
 @TargetApi(11)
 public class EditWorkout extends Activity implements OnDateSetListener {
 
 	private String exercise;
 	private String date;
 	private boolean saveData = true;
 	private Database db;
 	private String tableName = "workout";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.w_activity_edit_item);
 		exercise = getIntent().getStringExtra("exercise");
 		date = getIntent().getStringExtra("date");
 		db = new Database(this, "trackers.db");
 		
         final Button button = (Button) findViewById(R.id.button1);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	showDatePickerDialog(v);
             }
         });		
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.w_activity_edit_item, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected (MenuItem item) {
 		if (item.getItemId() == R.id.menu_ok) {
 			saveData = true;
 			EditText exercise_e = (EditText) findViewById(R.id.exercise);		
 			String exer = exercise_e.getText().toString();	
 			finish();
 			return true;
 				
 		} else if (item.getItemId() == R.id.menu_cancel) {
 			saveData = false;
 			finish();
 			return true;
 		}
 		return true;
 	}
 	
 	private void showAlert() {
 		
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
  
 			// set title
 			alertDialogBuilder.setTitle("Error");
  
 			// set dialog message
 			alertDialogBuilder
 				.setMessage("Click yes to exit!")
 				.setCancelable(false)
 				.setNeutralButton("Yes",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						// if this button is clicked, close
 						// current activity
 						finish();
 					}
 				  })
 				.setNegativeButton("No",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						// if this button is clicked, just close
 						// the dialog box and do nothing
 						dialog.cancel();
 					}
 				});
  
 				// create alert dialog
 				AlertDialog alertDialog = alertDialogBuilder.create();
  
 				// show it
 				alertDialog.show();
 	}
 	
 
 	public void onResume() {
 		super.onResume();
 		
 		if (exercise != null) {
 			saveData = true;
 			EditText exer = (EditText) findViewById(R.id.exercise);
 			EditText weight = (EditText) findViewById(R.id.weight);			
 			EditText reps  = (EditText) findViewById(R.id.reps);
 			EditText sets      = (EditText) findViewById(R.id.sets);
 			EditText dt      = (EditText) findViewById(R.id.date);
 			
 			Selector sel = db.selector(tableName);
 			sel.addColumns(new String[] { "exercise", "weight", "reps", "sets", "dt" });
 			sel.where("exercise == ? and dt = ?", new String[] {exercise, date});
 			sel.execute();
 			ResultSet cursor = sel.getResultSet();
 			while (cursor.moveToNext()) {
 				exer.setText(cursor.getString(0));
 				weight.setText(cursor.getString(1));
 				reps.setText(cursor.getString(2));
 				sets.setText(cursor.getString(3));
 				dt.setText(cursor.getString(4));				
 			}
 			cursor.close();
 		}
 		else {
 			saveData = false;
 		}
 
 	}
 	
 	public void onPause() {
 		super.onPause();
 		if (saveData) {
 
 			EditText exercise_e = (EditText) findViewById(R.id.exercise);
 			EditText weight_e = (EditText) findViewById(R.id.weight);			
 			EditText reps_e  = (EditText) findViewById(R.id.reps);
 			EditText sets_e = (EditText) findViewById(R.id.sets);
 			EditText date_e = (EditText) findViewById(R.id.date);			
 
 			String exer = exercise_e.getText().toString();	
 			String weight = weight_e.getText().toString();				
 			String reps = reps_e.getText().toString();
 			String sets = sets_e.getText().toString();
 			String dt = date_e.getText().toString();
 
 			if (exer != null) {
 				ContentValues values = new ContentValues();
 				values.put("exercise", exer);
 				values.put("weight", weight);			
 				values.put("reps", reps);
 				values.put("sets", sets);
 				values.put("dt", dt);
 				
 				Selector sel = db.selector(tableName);
 				sel.where("exercise = ? and dt = ?",  new String[] { exer, dt });
 				int count = sel.execute();
 				if (count != 0) {
 					Updater upd = db.updater(tableName);
 					upd.columnNameValues(values);
 					upd.where("exercise = ? and dt = ?", new String[] {exer, dt});
 					upd.execute();
 				}
 				else {
 					insert(values);
 				}
 					
 			}
 
 		}
 	}
 
 	public void insert(ContentValues values) {
 		Inserter ins = db.inserter(tableName);
 		ins.columnNameValues(values);
 		ins.execute();
 	}
 	
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		db.close();
 	}
 	
 	@SuppressLint("NewApi")
 	public void showDatePickerDialog(View v) {
 	    DialogFragment newFragment = new DatePicker();
 	    newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		System.out.print("Positive Clicked");	
 		
 	}
 
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		System.out.print("Negative Clicked");		
 	}
 
 	public void onDateSet(android.widget.DatePicker view, int year,
 			int monthOfYear, int dayOfMonth) {
 		EditText dt      = (EditText) findViewById(R.id.date);
		String date = monthOfYear + "/" + dayOfMonth + "/" + year;
 		dt.setText(date);
 	}	
 
 }
