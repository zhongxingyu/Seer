 package com.example.usabilityapp;
 
 import java.util.ArrayList;
 
 import Logic.ExerciseLogic.Exercise;
 import Logic.ExerciseLogic.ExerciseDataSource;
 import Logic.Workout.Workout;
 import Logic.Workout.WorkoutExerciseDataSource;
 import Logic.Workout.WorkoutNameDataSource;
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class CreateWorkoutActivity extends Activity {
 
 	private ArrayList<Exercise> selectedExercises = new ArrayList<Exercise>();	
 	private ExerciseDataSource eDatasource;
 	private WorkoutNameDataSource nDatasource;
 	private WorkoutExerciseDataSource wDatasource;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_workout);
 
 		eDatasource = new ExerciseDataSource(this);
 		eDatasource.open();
 		
 		ArrayList<Exercise> allExercises = eDatasource.getAllExercises();
 		final ListView lv1 = (ListView) findViewById(R.id.custom_workout_exercise_list);
 		lv1.setAdapter(new CustomExerciseCheckListAdapter(this,allExercises));
 		lv1.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 					Exercise selected = (Exercise) (lv1.getItemAtPosition(arg2));	   
 					CustomExerciseCheckListAdapter.ViewHolder holder;
 		            holder = (CustomExerciseCheckListAdapter.ViewHolder) arg1.getTag();
 		           
 		            if(holder.nameView.isChecked() ){
 		            	holder.nameView.setChecked(false);  
 		            	selectedExercises.remove(selected);
 		            	arg1.setBackgroundColor(0);
 		            }else{
 		               	holder.nameView.setChecked(true);
 		            	selectedExercises.add(selected);
 		         	    arg1.setBackgroundColor(Color.RED);
 		            }
 			}}); 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_workout, menu);
 		return true;
 	}
 	
 	public void createWorkout(View view){
 		EditText nameText = (EditText)findViewById(R.id.workoutTitleText);		
 		String name = nameText.getText().toString();
 		
 		if(!name.equals("") && selectedExercises.size() > 0){
 			nDatasource = new WorkoutNameDataSource(this);
 			wDatasource = new WorkoutExerciseDataSource(this);
 			nDatasource.open();
 			wDatasource.open();
 			
 			Workout workout = nDatasource.createWorkout(name);
 			
 			for(Exercise e: selectedExercises){
 				wDatasource.addExercise(workout.getId(), e.getId());
 			}
 		}
 
 	}
 
 }
