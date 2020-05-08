 /*Copyright  2012 GivDev
  *
  * This file is part of Gymapp.
  *
  * Gymapp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Gymapp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Gymapp. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.Grupp01.gymapp.View.Workout;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.Exercise.ExerciseData;
 import com.Grupp01.gymapp.Controller.Workout.RegisterDbHandler;
 import com.Grupp01.gymapp.Controller.Workout.SetsData;
 import com.Grupp01.gymapp.Controller.Workout.WorkoutDbHandler;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * @author GivDev
  * @version 0.1
  * @peer reviewed by
  * @date 04/10/12
  *
  * Class RegisterDynamicActivity is an activity that enables the user to
  * register his or hers result when performing a dynamic muscle exercise.
  * The user will be able to input repetitions and the weight.
  *
  * <p>This class i a part of the </p><i>View</i><p> package, and a part of the </p><i>Workout</i>
  * <p> Subpackage</p>
  *
  */
 public class RegisterDynamicActivity extends SherlockActivity {

 	private int exerciseId;
 	private int workoutId;
 	private ExerciseData exercise;
 	private List<SetsData> dynamicSetsList = new LinkedList<SetsData>();//The list where new sets are added and removed.
 
 	/**
 	 * Set up the default layout and call initiate method that is required.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		//workoutName = getIntent().getStringExtra("exercisename");
 		setContentView(R.layout.activity_register_dynamic);
		exerciseId = getIntent().getIntExtra(WorkoutActivity.EXTRA_EXERCISE_ID, 0);
		workoutId = getIntent().getIntExtra(WorkoutActivity.EXTRA_WORKOUT_ID, 0);
 		getExerciseData();
 		setTitle(exercise.getName());
 		setNoteText();
 		//Show the sets (reps and weight) for the last time this
 		//exercise was performed.
 		setLastSetString();
 	}
 
 
 	/**
 	 * Set up the actionbar (layout xml and title)
 	 * @param Menu the actionbar menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.universal_menu, menu);
 		return true;
 	}
 
 
 	/**
 	 * Set up actions for buttons in actionbar
 	 * @param MenuItem item - The menuitem thas has been pressed
 	 *
 	 */
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()){
 
 		//Make app icon navigate back to the applications start screen.
 		case	android.R.id.home:
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 
 	/**
 	 * This method will set the TextView in the layout
 	 * that shows the time and weight for when performing this exercise last time.
 	 */
 	private void setLastSetString() {
 		List<SetsData> dynamicSetsList = new LinkedList<SetsData>();
 		RegisterDbHandler dbHandler = new RegisterDbHandler(this);
 		StringBuffer sets = new StringBuffer();
 		TextView latestSets = (TextView) findViewById(R.id.lastTimeSets);
 		dbHandler.open();
 		dynamicSetsList = dbHandler.getPreviouslyDynamicSets(workoutId, exerciseId, exercise.getTypeId());
 		if(dynamicSetsList.size() == 0)
 		{
 			latestSets.setText("");
 		}
 		else
 		{
 			for (SetsData setData : dynamicSetsList)
 			{
 				sets.append(" ");
 				sets.append(setData.getReps());
 				sets.append("x");
 				sets.append(setData.getWeight());
 				sets.append(", ");
 			}
 
 			latestSets.setText(sets);
 		}
 		dbHandler.close();
 	}
 
 
 	/**
 	 * Method called when pressing any of the buttons in the view
 	 * @param View v - The view thas has been clicked
 	 */
 
 	public void onClick(View v){
 		switch(v.getId()){
 
 		//Cancel button pressed, exit without saving
 		case R.id.dynamicButtonCancel:
 			finish();
 			break;
 
 			//OK button pressed, exit and save to database
 		case R.id.dynamicButtonOK:
 			saveSetsToDatabase();
 			finish();
 			break;
 
 			//Finish set pressed, add the reps and weight to the array
 		case R.id.dynamicFinishSet:
 			addNewSet();
 			break;
 
 			//Undo set pressed, remove the latest set from the array.
 		case R.id.dynamicUndoSet:
 			removeLatestSet();
 			break;
 		}
 	}
 
 	private void setNoteText()
 	{
 		TextView notes = (TextView) findViewById(R.id.myNoteText);
 		notes.setText(exercise.getNote());
 	}
 
 	/**
 	 * THis is called when pressing the "OK" button, will save the sets
 	 * to the database before this activity terminates.
 	 */
 	private void saveSetsToDatabase() {
 		RegisterDbHandler dbHandler = new RegisterDbHandler(this);
 		dbHandler.open();
 		for(SetsData setData: dynamicSetsList)
 		{
 			dbHandler.addDynamicSet(setData.getWeight(), setData.getReps(), setData.getworkoutId(), setData.getexerciseid());
 		}	
 		dbHandler.close();
 	}
 
 
 
 	/**
 	 * Will read the input of reps and weight EditTexts, add it to the
 	 * array and update the view
 	 *
 	 */
 	private void addNewSet() {
 		//Input from the user, the reps and weight
 		EditText editReps = (EditText) findViewById(R.id.editReps);
 		EditText editWeight = (EditText) findViewById(R.id.editWeight);
 
 
 		//Fetch number of reps
 		String reps = editReps.getText().toString();
 
 		//Do not add set where the number of repetitions is 0 or blank ("")
 		if(!(reps.equals("")) && !(reps.equals("0"))){
 
 			//If the weight is blank, set it to zero
 			String weight = editWeight.getText().toString();
 			if(weight.equals(""))
 				weight = "0";

 			//Parses strings input to Integer.
 			Integer repsInt = Integer.parseInt(editReps.getText().toString());
 			Integer weightInt = Integer.parseInt(editWeight.getText().toString());
 			//Adds new sets to cardioSetsList
 			dynamicSetsList.add(new SetsData(weightInt,repsInt,workoutId,exerciseId));
 			updateView();
 
 		}
 		else
 			Toast.makeText(this, "Cant add set with 0 repetitions.", Toast.LENGTH_SHORT).show();

 	}
 
 	/**
 	 * This method will "translate" the items in the ArrayList to a string and separate each
 	 * item with a "," in the resulting string.
 	 * It will then call the method "setCurrentSetsView" with the resulting string.
 	 *
 	 */
 	private void updateView() {
 
 		//The textview where current sets will be showed (added when button "Finish set" is pressed)
 		TextView currentSetString = (TextView) findViewById(R.id.thisTimeSets);
 
 		//Strinbuffer that is shown in Sets
 		StringBuffer prefix = new StringBuffer();
 
 		for (SetsData setData : dynamicSetsList)
 		{
 			prefix.append(" ");
 			prefix.append(setData.getReps());
 			prefix.append("x");
 			prefix.append(setData.getWeight());
 			prefix.append(", ");
 		}
 		currentSetString.setText(prefix);
 	}
 
 
 	/**
 	 * Will remove the last set from the array and then update the view.
 	 *
 	 */
 	private void removeLatestSet() {
 		if(dynamicSetsList.size() > 0)
 			dynamicSetsList.remove(dynamicSetsList.size() -1);
 		else
 		{
 			Toast.makeText(this, "No sets to remove", Toast.LENGTH_SHORT).show();
 		}
 		updateView();
 	}
 
 	/**
 	 * Gets information about the exercise in a ExerciseData object
 	 */
 	private void getExerciseData()
 	{
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		exercise = dbHandler.getExerciseDataFromExerciseId(exerciseId);
 		dbHandler.close();
 	}
 }
