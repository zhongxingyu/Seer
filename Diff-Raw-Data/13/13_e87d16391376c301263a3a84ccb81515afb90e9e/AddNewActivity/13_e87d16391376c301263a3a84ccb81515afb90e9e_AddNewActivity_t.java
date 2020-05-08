 package com.catalyst.android.birdapp;
 
 import com.catalyst.android.birdapp.database.DatabaseHandler;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 public class AddNewActivity extends Activity {
 
 	private DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 	private Button saveButton;
 	private CheckBox addAnotherCheckBox;
 	private EditText activityName;
 	private String activityNameValue;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_new);
 		saveButton = (Button)findViewById(R.id.saveButton);
 		addAnotherCheckBox = (CheckBox)findViewById(R.id.add_another_button);
 		activityName = (EditText)findViewById(R.id.activityName);
 		
 		saveButton.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
             	 saveActivity();
                  checkBox(view);
              }
          });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_new, menu);
 		return true;
 	}
 	
 	/**
 	 * Saves the activity name to the database
 	 */
 	public void saveActivity(){
 		activityNameValue = activityName.getText().toString();
 		dbHandler.saveActivity(activityNameValue);
 	}
 	/**
 	 * checks the checkbox to see if it's checked
 	 * @param view
 	 */
 	public void checkBox(View view){
 		if(addAnotherCheckBox.isChecked()){
			activityName.setText("");
			addAnotherCheckBox.setChecked(false);
 		}else {
			finish();
 		}
 	}
 /**
  * Sends user back to AddNewActivity so they add another activity easily
  * @param view
  */
 	public void getAddNewActivity(View view){
 		Intent intent = new Intent(getApplication(), AddNewActivity.class);
 		startActivity(intent);
 	}
 	/**
 	 * Sends user to the BirdForm screen
 	 * @param view
 	 */
 	public void getBirdForm(View view){
 		Intent intent = new Intent(getApplication(), BirdFormActivity.class);
 		startActivity(intent);
 	}
 }
