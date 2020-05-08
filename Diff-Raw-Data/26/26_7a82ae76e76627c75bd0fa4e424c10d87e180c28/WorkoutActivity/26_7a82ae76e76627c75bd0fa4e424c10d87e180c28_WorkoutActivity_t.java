 /*Copyright  2012 GivDev
  * 
  * This file is part of Gymapp.
  *
  *   Gymapp is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Gymapp is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *  along with Gymapp.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.Grupp01.gymapp.View.Workout;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.Exercise.ExerciseData;
 import com.Grupp01.gymapp.Controller.Workout.WorkoutDbHandler;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 
 /**
  * @author GivDev
  * @version 0.1
  * @peer reviewed by Robert(07/10-2012)
  * @date 04/10/12
  *
  * Class WorkoutActivity will be shown if the user selects a workout routine from the list in
  * ListWorkoutActivity. It will display all exercises in the specific workout routine
  * and enables the user to registering workout results by selecting an exercise from the list.
  *
  * <p>This class i a part of the </p><i>View</i><p> package, and a part of the </p><i>Workout</i>
  * <p> Subpackage</p>
  *
  */
 public class WorkoutActivity extends SherlockActivity {
 
	private final int cardioType = 1;
	private final int dynamicType = 2;
	private final int staticType = 3;
 	public final static String EXTRA_EXERCISE_ID = "com.Grupp01.gymapp.message.exercise.exercise";
 	public final static String EXTRA_WORKOUT_ID = "com.Grupp01.gymapp.message.exercise.workout";
 	private ListView listExercisesView;	//The listview that holds all the exercises for the workout
 	private int workoutId;
 	private List<ExerciseData> exerciseDataList;
 
 	/**
 	 * Set up the default layout and list all exercises
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		workoutId = getIntent().getIntExtra(ListWorkoutActivity.WORKOUT_ID, 0);
 		setContentView(R.layout.activity_workout);
 		getAndSetTitle();
 		getExerciseDataList();
 		listAllExercises();
 	}
 	/**
 	 * Set up the actionbar (layout xml and title)
 	 * @param Menu the actionbar menu
 	 * @return true to make the menu visible
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_workout, menu);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		//Set the title to the name of the workout
 
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
 			Intent intentHome = new Intent(this, MainActivity.class);
 			intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intentHome);
 			return true;
 		case R.id.menu_editWorkout:
 			Intent intentEditWorkout = new Intent(this, EditWorkoutActivity.class);
 			intentEditWorkout.putExtra(WorkoutActivity.EXTRA_WORKOUT_ID, workoutId);
 			startActivityForResult(intentEditWorkout, 0);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 
 
 	/**
 	 * Is called when the activity starts to set the listview to
 	 * show all exercises available in this workout.
 	 */
 	private void listAllExercises() {
 		listExercisesView = (ListView) findViewById(R.id.activeWorkoutList) ;
 		ArrayList<String> listExercises = new ArrayList<String>();
 
 		for(ExerciseData name: exerciseDataList)
 		{
 			listExercises.add(name.getName());
 		}
 		//Add all the exercises from the stringarray to the ArrayList and build the listview
 		ListAdapter listAdapter = new ArrayAdapter<String>(this, R.layout.list_simple_row, listExercises);
 		listExercisesView.setAdapter(listAdapter);
 
 
 		//Set up listener and action for pressing the listview
 		listExercisesView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View v, int position,
 					long id) {
 
 				//Get the text label of the row that has been clicked (will be used to open the correct workout)
 				ExerciseData exercise = exerciseDataList.get(position);
 				registerWorkoutResult(exercise);
 
 
 			}// onItemClick end
 		});// setOnItemClickListener end
 
 
 	}
 
 
 	/**
 	 * This is called when pressing a exercise from the list of exercises in the list.
 	 * Will open the correct registration activity depending on which type of exercise
 	 * it is (dynamic, static or cardio).
 	 * @param exerciseName The name of the exercise
 	 */
 	private void registerWorkoutResult(ExerciseData exercise){
 		//ONLY FOR TESTING DIFFERENT REGISTER ACTIVITY!!
		if(exercise.getTypeId() == cardioType){
 			Intent intent = new Intent(WorkoutActivity.this, RegisterCardioActivity.class);
 			intent.putExtra(EXTRA_EXERCISE_ID, exercise.getId());
 			intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
 			startActivity(intent);
 		}
		if(exercise.getTypeId() == dynamicType){
 			Intent intent = new Intent(WorkoutActivity.this, RegisterDynamicActivity.class);
 			intent.putExtra(EXTRA_EXERCISE_ID, exercise.getId());
 			intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
 			startActivity(intent);
 		}
		if(exercise.getTypeId() == staticType){
 			Intent intent = new Intent(WorkoutActivity.this, RegisterStaticActivity.class);
 			intent.putExtra(EXTRA_EXERCISE_ID, exercise.getId());
 			intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
 			startActivity(intent);
 		}
 	}
 
 	/**
 	 * Gets the name of the workout and sets that name to title in activity
 	 */
 	private void getAndSetTitle()
 	{
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		String title = dbHandler.getWorkoutIdNameById(workoutId).getName();
 		setTitle(title);
 		dbHandler.close();
 	}
 
 	/**
 	 * Gets a workouts exercises from database
 	 */
 	private void getExerciseDataList()
 	{
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		exerciseDataList = dbHandler.getExerciseIdNameById(dbHandler.getExercisesbyWorkoutId(workoutId));
 		dbHandler.close();
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		getExerciseDataList();
 		listAllExercises();
 	}
 
 
 }
 
 
