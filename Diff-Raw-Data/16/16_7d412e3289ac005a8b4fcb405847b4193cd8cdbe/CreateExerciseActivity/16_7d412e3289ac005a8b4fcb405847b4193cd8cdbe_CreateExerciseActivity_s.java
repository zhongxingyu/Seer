 package com.example.usabilityapp;
 
 import Logic.ExerciseLogic.Exercise;
 import Logic.ExerciseLogic.ExerciseDataSource;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class CreateExerciseActivity extends Activity {
 	private ExerciseDataSource datasource;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_exercise);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.create_exercise, menu);
 		return true;
 	}
 	
 	public void createExercise(View view){
 		EditText nameText = (EditText)findViewById(R.id.createNameField);
 		EditText typeText = (EditText)findViewById(R.id.createTypeField);
 		
 		String name = nameText.getText().toString();
 		String type = typeText.getText().toString();
 		Exercise exercise;
 		
 		if(!name.equals("") && !type.equals("")){
 			datasource = new ExerciseDataSource(this);
 			datasource.open();
 			exercise = datasource.createExercise(name, type);
 		
 			if(exercise != null){
 				Intent intent = new Intent(this, ExerciseActivity.class);
 				startActivity(intent);
 			}
 		}	
 	}
 
 }
