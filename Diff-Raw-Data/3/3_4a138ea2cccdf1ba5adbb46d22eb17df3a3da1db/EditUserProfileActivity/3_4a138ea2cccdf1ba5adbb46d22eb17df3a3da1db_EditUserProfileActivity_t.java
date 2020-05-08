 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.ProfileModel;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class EditUserProfileActivity extends Activity implements OnClickListener 
 {
 	
 	private EditText nameTextField;
 	private EditText weightTextField;
 	private EditText heightTextField;
 	private EditText goalWeightTextField;
 	ProfileModel model = new ProfileModel(this);
 	
 	
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.userprofile_form);
 		model.open();
 	
 
 		// Save button
 		View saveButton = findViewById(R.id.button_userprofile_form_save);
 		saveButton.setOnClickListener(this);
 		
 		// Cancel button
 		View cancelButton = findViewById(R.id.button_userprofile_form_cancel);
 		cancelButton.setOnClickListener(this);
 		
 		// Name field
         nameTextField = (EditText) findViewById(R.id.user_name_field);
         nameTextField.setOnClickListener(this);
         if(model.getByAttribute("name") != null)
         	nameTextField.setText(model.getByAttribute("name").value);        
 
         // Weight field
         weightTextField = (EditText) findViewById(R.id.user_weight_field);
         weightTextField.setOnClickListener(this);
         if(model.getByAttribute("weight") != null)
         	weightTextField.setText(model.getByAttribute("weight").value);
 
         //Goal Weight field
         goalWeightTextField = (EditText) findViewById(R.id.user_goal_weight_field);
         goalWeightTextField.setOnClickListener(this);
         if(model.getByAttribute("goal_weight") != null)
         	goalWeightTextField.setText(model.getByAttribute("goal_weight").value);
         
         //Height field
         heightTextField = (EditText) findViewById(R.id.user_height_field);
         heightTextField.setOnClickListener(this);
         if(model.getByAttribute("height") != null)
         	heightTextField.setText(model.getByAttribute("height").value);
         
         model.close();
 	}
 
 	public void onClick(View v) 
 	{
 		Context context = getApplicationContext();
 		CharSequence text;
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast;
 		
 		switch(v.getId())
 		{
 		case R.id.button_userprofile_form_save:
 			
 			// Blank Checking Validation
 			if(this.isNotBlank() == false)
 			{
 				text = "Please fill out all fields!";
 				toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 			else{
 				model.open();
 				long name_id = model.updateInsert("name", nameTextField.getText().toString());
 				long height_id = model.updateInsert("height", heightTextField.getText().toString());
 				long current_weight_id = model.updateInsert("weight", weightTextField.getText().toString());
 				long goal_weight_id = model.updateInsert("goal_weight", goalWeightTextField.getText().toString());
 				model.close();
				finish();
 				Intent u = new Intent(this, UserProfileActivity.class);
 				startActivity(u);
 			}
 			break;
 		case R.id.button_userprofile_form_cancel:
 /*			model.open();
 			if(model.getByAttribute("name") != null &&
 			   model.getByAttribute("weight") != null &&
 			   model.getByAttribute("goal_weight") != null &&
 			   model.getByAttribute("height") != null){  
 				model.close();*/
				finish();
 				Intent u = new Intent(this, UserProfileActivity.class);
 				startActivity(u);
 /*			}
 			else{
 				text = "Please save your details!";
 				toast = Toast.makeText(context, text, duration);
 				toast.show();
 				model.close();
 			}*/
 		}
 		
 	}
 	
 	// If back is pressed and no information is saved, go directly to the frontpage.
 /*	public void onBackPressed(){
 		model.open();
 		if(model.getByAttribute("name") == null ||
 		   model.getByAttribute("weight") == null ||
 		   model.getByAttribute("goal_weight") == null ||
 		   model.getByAttribute("height") == null){  
 			model.close();
 			Intent u = new Intent(this, CrossFitrActivity.class);
 			startActivity(u);
 		}
 		else{
 			model.close();
 			Intent u = new Intent(this, UserProfileActivity.class);
 			startActivity(u);
 		}
 	}*/
 	
 	
 	private boolean isNotBlank(){
 		if (nameTextField.getText().length() <= 0)
 			return false;
 		else if(heightTextField.getText().length() <= 0)
 			return false;
 		else if(weightTextField.getText().length() <= 0)
 			return false;
 		else if(goalWeightTextField.getText().length() <= 0)
 			return false;
 		else
 			return true;
 	}
 }
 
