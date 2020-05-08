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
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.Workout.WorkoutDbHandler;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 
 /** The Graphical layout where the user Edit a Workout
  * * @author Robert Blomberg
  * */	
 
 public class EditWorkoutActivity extends SherlockActivity
 {
 
 	private ListView listView;
 	private ArrayAdapter<ExerciseListElementData> listAdapter;
 	private int workoutId;
 	public ArrayList<ExerciseListElementData> exerciseList = new ArrayList<ExerciseListElementData>();
 
 
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		Intent intent = getIntent();
 		if(intent.getIntExtra(ListWorkoutActivity.WORKOUT_ID, 0) !=0)
 		{
 			workoutId = intent.getIntExtra(ListWorkoutActivity.WORKOUT_ID, 0);
 		}
 		else
 		{
 			workoutId = intent.getIntExtra(WorkoutActivity.EXTRA_WORKOUT_ID, 0);
 		}
 		setContentView(R.layout.editworkout);
 		createEditWorkout();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getSupportMenuInflater().inflate(R.menu.editworkout, menu);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		return false;
 	}
	
 	public void createEditWorkout()
 	{
 		// Find the ListView resource.
 		listView = (ListView) findViewById( R.id.mainListView );
 
 		// When item is tapped, toggle checked properties of CheckBox and Exercise.
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
 		{
 			/**Is called when the user press a Exercise in the workout, when
 			 * a user click on a exercise, the exercise is getting checked
 			 * If the exercise already was checked when clicked, it is getting unchecked
 			 * *
 			 * @param parent
 			 * @param item
 			 * @param position
 			 * @param id*/
 			@Override
 			public void onItemClick( AdapterView<?> parent, View item, int position, long id)
 			{
 
 				ExerciseListElementData exercise = listAdapter.getItem( position );
 				exercise.toogleChecked();
 				ExerciseViewHolder viewHolder = (ExerciseViewHolder) item.getTag();
 				viewHolder.getCheckBox().setChecked( exercise.isChecked() );
 			}//End of onItemClick
 		});//End of Listener
 
 
 					//Create spinners and add listeners to them.
 					/*
 			Spinner spinnerMuscleGroup = (Spinner) findViewById(R.id.spinnermusclegroup);
 			spinnerMuscleGroup.setOnItemSelectedListener((OnItemSelectedListener) this);
 			spinnerMuscleGroup.setVisibility(View.GONE);
 			
 			Spinner spinnerMuscle = (Spinner) findViewById(R.id.spinnermuscle);
 			spinnerMuscle.setOnItemSelectedListener(this);
 			spinnerMuscle.setVisibility(View.GONE);
 			
 			
 			//Creates ArrayAdapters that contains a String-Array
 			ArrayAdapter<String> muscleGroupSpinner = new ArrayAdapter<String>
 			(this,android.R.layout.simple_spinner_item,muscleGroups);
 			
 			ArrayAdapter<String> muscleSpinner = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,muscleGroups);
 			
 			//On click, make a dropdown menu with big buttons so the user won't missclick
 			muscleGroupSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			muscleSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			
 			//Set the Arrayadapter into the spinner
 			spinnerMuscleGroup.setAdapter(muscleGroupSpinner);
 			spinnerMuscle.setAdapter(muscleSpinner);
 					 */
 					// Since we don't have a database we manually put in exercises
 					//Could use a Arraylist directly but we use a String since we will load
 					//String-Arrays from the database we use a String-array here
 					/*if ( exerciseList.isEmpty() )
 			{
 			exerciseList.add(new ExerciseListElementData(2, "hejja",false));
 			
 			}*/
 
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		exerciseList = dbHandler.getExercisesCheckedByWorkoutTemplateId(workoutId);
 		dbHandler.close();
 
 
 		// Set our custom Arrayadapter as the ListView's adapter.
 		listAdapter = new ExerciseArrayAdapter(this, exerciseList);
 		listView.setAdapter( listAdapter );
 	}
 
 	/**
 	 * Saves an Edited Workout to Databse
 	 * @param view
 	 */
 	public void saveToDatabase(View view)
 	{
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		for(ExerciseListElementData exercise: exerciseList)
 		{
 			dbHandler.editWorkoutTemplate(exercise, workoutId);
 		}
 		dbHandler.close();
 		finish();
 	}
 
 	/**Is called if a user press the cancel button,
 	 * asks the user if it want to close the dialog.*/
 	public void cancelEditWorkoutDialog(View view)
 	{
 		final AlertDialog.Builder closeEditWorkoutDialog = new AlertDialog.Builder(this);
 
 		closeEditWorkoutDialog.setMessage("Are you sure you want to close workout?");
 
 		closeEditWorkoutDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 		{
 			/** When the user click the "Yes"-button, go back to ListWorkout*/
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				finish();
 			}
 		});
 		//If pressing "Cancel"
 		closeEditWorkoutDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
 		{
 			@Override
 			/** When the user click the "Cancel"-button, close the dialog*/
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				dialog.cancel();
 			}
 		});
 		closeEditWorkoutDialog.show();
 	}
 	/*
 public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
 {
 switch (parent.getId())
 {
 case R.id.spinnermusclegroup:
 //Implement later when we got a working database
 case R.id.spinnermuscle:
 //Implement later when we got a working database
 }
 }
 	 */
 
 } 
