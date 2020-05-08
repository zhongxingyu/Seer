 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class CustomAddActivity extends Activity implements OnClickListener {
 
 	private static final String tag = "AddCustomActivity";
 	// text fields
 	private EditText workoutTextField, nameTextField;
 	private InputMethodManager keyControl;
 
 	private int workoutConstant = WorkoutModel.TYPE_CUSTOM;
 	private int recordConstant = 0;
 	// Spinner workoutTypeDropDown; This constant belongs to commented out
 	// functionality
 	Spinner recordTypeDropDown;
 	WorkoutModel model = new WorkoutModel(this);
 
 	// onCreate method called at the beginning of activity
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d(tag,"### at least here 1 ###");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.custom_workout_add);
 
 		keyControl = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
 		View addCustomBg = findViewById(R.id.custom_add_background);
 		addCustomBg.setOnClickListener(this);
 
 		// button to save
 		View saveButton = findViewById(R.id.button_workout_form_save);
 		saveButton.setOnClickListener(this);
 		
 		// button to save and start workout
 		View saveAndStartButton = findViewById(R.id.button_workout_form_start);
 		saveAndStartButton.setOnClickListener(this);
 		
 		// text field for the workout description to be added
 		workoutTextField = (EditText) findViewById(R.id.description_edittext_add);
 		workoutTextField.setOnClickListener(this);
 
 		// text field for the workout name to be added
 		nameTextField = (EditText) findViewById(R.id.nameofworkout_edittext_add);
 		nameTextField.setOnClickListener(this);
 
 
 		// drop down menu for the record type to be added
 		recordTypeDropDown = (Spinner) findViewById(R.id.workout_form_recordtype_spinner);
 		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
 				R.array.recordtype_array, android.R.layout.simple_spinner_item);
 		adapter2
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		recordTypeDropDown.setAdapter(adapter2);
 		recordTypeDropDown
 				.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	}
 
 	private void hideKeyboard(EditText eBox) {
 		keyControl.hideSoftInputFromWindow(eBox.getWindowToken(), 0);
 	}
 
 	// called by the onClickListener
 	public void onClick(View v) {
 		Log.d(tag, "View : " + v.getId());
 
 		switch (v.getId()) {
 		// if user clicks the save button
 		case R.id.button_workout_form_save:
 			// validate that inputs are not junk
 
 			if (this.validateForm() == true) {
 
 				model.open();
 				// save data into database, saves data from textfields, and selected
 				// workout in dropdowns
 				model.insert(nameTextField.getText().toString(), workoutTextField
 						.getText().toString(), workoutConstant, recordConstant);
 				model.close();
 				// go back into the custom activity class
				finish();
 				Intent i = new Intent(this, CustomActivity.class);
 				startActivity(i);
 			} 
 			else 
 			{
 				// error prompt
 				Context context = getApplicationContext();
 				CharSequence text = "Please fill out all fields!";
 				int duration = Toast.LENGTH_SHORT;
 
 				Toast toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 
 			break;
 
 		// user clicks the save and start workout button
 		case R.id.button_workout_form_start:
 			if (this.validateForm() == true) {
 				model.open();
 				// save data into database, saves data from textfields, and selected
 				// workout in dropdowns
 				long newId = model.insert(nameTextField.getText().toString(), workoutTextField
 						.getText().toString(), workoutConstant, recordConstant);
 				model.close();
 
 				//pass in id to WorkoutProfileActivity and start activity
				finish();
 				Intent i = new Intent(this, WorkoutProfileActivity.class);
 				i.putExtra("ID", newId);
 				startActivity(i);
 			} 
 			else 
 			{
 				// error prompt
 				Context context = getApplicationContext();
 				CharSequence text = "Please fill out all fields!";
 				int duration = Toast.LENGTH_SHORT;
 
 				Toast toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 
 			break;
 
 		case R.id.custom_add_background:
 			hideKeyboard(workoutTextField);
 			hideKeyboard(nameTextField);
 			break;
 		}
 
 	}
 
 	public void keyboardHandler() {
 		//TODO
 	}
 
 	// listener for the spinner object
 	public class MyOnItemSelectedListener implements OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			// handles the first dropdown menu
 			// update the workoutConstant, which keeps track of what is selected in
 			// the workout dropdown.
 			/*
 			 * Commented out functionality
 			 * if(parent.getItemAtPosition(pos).toString().equals( "Custom" )) {
 			 * workoutConstant = WorkoutModel.TYPE_CUSTOM; } else
 			 * if(parent.getItemAtPosition(pos).toString().equals("WoD")) {
 			 * workoutConstant = WorkoutModel.TYPE_WOD; } else
 			 * if(parent.getItemAtPosition(pos).toString().equals( "Girls")) {
 			 * workoutConstant = WorkoutModel.TYPE_GIRL; } else
 			 * if(parent.getItemAtPosition(pos).toString().equals( "Heroes" )) {
 			 * workoutConstant = WorkoutModel.TYPE_HERO; }
 			 */
 
 			// handles when the second dropdown menu
 			// update the workoutConstant, which keeps track of what is selected in
 			// the record dropdown.
 			if (parent.getItemAtPosition(pos).toString()
 					.equals("Please select a record type")) {
 				recordConstant = 0;
 			} else if (parent.getItemAtPosition(pos).toString().equals("Timer")) {
 				recordConstant = WorkoutModel.SCORE_TIME;
 			} else if (parent.getItemAtPosition(pos).toString().equals("Weight")) {
 				recordConstant = WorkoutModel.SCORE_WEIGHT;
 			} else if (parent.getItemAtPosition(pos).toString().equals("Reps")) {
 				recordConstant = WorkoutModel.SCORE_REPS;
 			}
 		}
 
 		public void onNothingSelected(AdapterView parent) {
 			// Do nothing.
 		}
 	}
 
 	// method to make sure that input is valid, if false the save buttons should
 	// do nothing and prompt user to fill in all forms
 	// if true, buttons execute
 	private boolean validateForm() {
 		// assume form is valid
 		boolean isValidForm = true;
 		// makes sure user inputed something in the name field
 		if (nameTextField.getText().length() <= 0) {
 			isValidForm = false;
 		}
 		// makes sure user inputed something in the description field
 		if (workoutTextField.getText().length() <= 0) {
 			isValidForm = false;
 		}
 		// makes sure that the user selected something from the dropdown menu
 		if (recordConstant == 0) {
 			isValidForm = false;
 		}
 		return isValidForm;
 	}
 }
