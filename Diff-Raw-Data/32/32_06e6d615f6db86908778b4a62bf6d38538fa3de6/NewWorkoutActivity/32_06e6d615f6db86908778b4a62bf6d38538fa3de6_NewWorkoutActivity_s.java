 package com.sloturtles.workout;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 
 public class NewWorkoutActivity extends Activity implements OnClickListener {
 	//Global Variable Declaration/Initialization
 	private LinearLayout mLayout;
 	private EditText mEditText;
 	private EditText mEditText2;
 	private Button mButton;
 	public String STORE_PREFERENCES = "StorePrefs";
 	String sWorkoutName;
 	List<EditText> excerciseList = new ArrayList<EditText>();
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		//Usual OnCreate Stuff
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_newworkout);
 
 		//Assign Java variables to UI elements
 		mLayout = (LinearLayout) findViewById(R.id.LinearLayout2);
 		mEditText = (EditText) findViewById(R.id.etWorkoutName);
 		mEditText2 = (EditText) findViewById(R.id.etExerciseLabel);
 		mButton = (Button) findViewById(R.id.bNewExercise);
 		mButton.setOnClickListener(this);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_newworkout, menu);
 		return true;
 	}
 
 	//Add+ Button onClick method
 	public void onClick(View v) {
 		mLayout.addView(createNewEditText(mEditText.getText().toString()));
 	}
 
 	//Create dynamic EditTexts
 	private View createNewEditText(String string) {
 		@SuppressWarnings("deprecation")
 		final LayoutParams lparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		final EditText editText = new EditText(this);
 		editText.setLayoutParams(lparams);
 		excerciseList.add(editText);
 		return editText;
 	}
 
 	//Save Button
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuitem2:
 			saveWorkout();
 			sWorkoutName = "";
 			WorkoutsActivity.superScreen = 1;
 			finish();
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	//Method for saving Workouts
 	public void saveWorkout() {
 		EditText etWorkoutName;
 		etWorkoutName = (EditText) findViewById(R.id.etWorkoutName);
 		sWorkoutName = etWorkoutName.getText().toString();
 		WorkoutsActivity.workoutList.add(new Workout(sWorkoutName, false));
 		for(int x = 0; x < excerciseList.size();x++) {
 			Exercise blah = new Exercise(excerciseList.get(x).getText().toString());
 			WorkoutsActivity.workoutList.get(WorkoutsActivity.workoutList.size()-1).exerciseList.add(blah);
 		}
		SharedPreferences sp = getSharedPreferences(STORE_PREFERENCES, MODE_PRIVATE); 
 		SharedPreferences.Editor spEditor = sp.edit();
 
		String workoutNames = ""; 
 		for(int x = 0; x < WorkoutsActivity.workoutList.size();x++)
 			workoutNames += WorkoutsActivity.workoutList.get(x).workoutTitle + "+";

 		String exerciseNames = "";
 		exerciseNames = mEditText2.getText().toString() + "+";
 		for(int x = 0; x < WorkoutsActivity.workoutList.size();x++)
 			for(int y = 0; y < WorkoutsActivity.workoutList.get(x).exerciseList.size(); y++)
 				exerciseNames += WorkoutsActivity.workoutList.get(x).exerciseList.get(y).exerciseLabel + "+";
 
 		spEditor.putString("workoutTag", workoutNames);
 		spEditor.putString("exerciseTag", exerciseNames);
 
 		//debug
		toast(workoutNames);
		toast(exerciseNames);

 
 		spEditor.commit();
 	}
 
 	//Debug Toast Notification
 	public void toast(String message){
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 }
