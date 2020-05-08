 package com.atanas.tsankov.nfitness.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.atanas.tsankov.nfitness.R;
import com.atanas.tsankov.nfitness.constants.Constants;
 
 public class AddWorkoutActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_workout_screen);
 		final EditText addNameEditText = (EditText) findViewById(R.id.add_workout_name_edittext);
 		Button saveButton = (Button) findViewById(R.id.add_workout_save_button);
 		Button addExerciseButton = (Button) findViewById(R.id.add_workout_add_exercse_button);
 		saveButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String name = addNameEditText.getText().toString();
 				Intent returnIntent = new Intent();
 				returnIntent.putExtra(Constants.NAME, name);
 				setResult(RESULT_OK, returnIntent);
 				finish();
 			}
 		});
 		addExerciseButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(AddWorkoutActivity.this,
 						AddExerciseActivity.class);
 				startActivityForResult(intent, 0);
 			}
 		});
 	}
 }
