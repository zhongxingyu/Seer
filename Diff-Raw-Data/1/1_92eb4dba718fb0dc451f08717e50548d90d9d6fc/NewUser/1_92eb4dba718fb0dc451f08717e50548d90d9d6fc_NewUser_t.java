 package com.memomeme.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class NewUser extends Activity {
 	
 	EditText etNewUser;
 	Button bSubmit;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_user);
 
 		initViews();
 		initListeners();
 	}
 
 	private void initViews() {
 		etNewUser = (EditText) findViewById(R.id.editTextNewUser);
 		bSubmit = (Button) findViewById(R.id.buttonSubmitUser);
 	}
 
 	private void initListeners() {
 		bSubmit.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO creating db user
 				startActivity(new Intent(v.getContext(), Game.class));
				finish();
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 }
