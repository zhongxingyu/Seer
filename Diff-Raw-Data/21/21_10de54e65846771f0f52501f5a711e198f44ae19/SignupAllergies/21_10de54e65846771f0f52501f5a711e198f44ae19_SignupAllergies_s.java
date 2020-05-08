 package com.trydish.main;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public class SignupAllergies extends Activity {
 	
 	private int leftRight = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signup_allergies);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.allergies_warning)
 			   .setTitle(R.string.important_allergies);
 		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		
 		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
 		
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 	
 	// used to add allergies on-the-fly
 	public void addAllergy(View view) {
     	EditText editText = (EditText) findViewById(R.id.add_new_allergy_box);
     	String message = editText.getText().toString();
     	LinearLayout allergiesList;
     	// leftRight is used to switch between the two LinearLayouts
     	// holding the checkboxes
     	if (leftRight == 0) {
     		allergiesList = (LinearLayout) findViewById(R.id.left_check_boxes);
     		leftRight++;
     	} else {
     		allergiesList = (LinearLayout) findViewById(R.id.right_check_boxes);
     		leftRight--;
     	}
     	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
     	params.setMargins(13, 13, 13, 13);
     	CheckBox newAllergy = new CheckBox(this);
     	newAllergy.setText(message);
     	newAllergy.setChecked(true);
     	newAllergy.setTextColor(Color.WHITE);
     	newAllergy.setTextSize(30);
     	allergiesList.addView(newAllergy, params);
 	}
 	
 	public void signupNext(View view) {
 		Intent intent = new Intent(this, PostLoginHome.class);
 		startActivity(intent);
 	}
 
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.activity_sign_up_allergies, menu);
 //		return true;
 //	}
 
 }
