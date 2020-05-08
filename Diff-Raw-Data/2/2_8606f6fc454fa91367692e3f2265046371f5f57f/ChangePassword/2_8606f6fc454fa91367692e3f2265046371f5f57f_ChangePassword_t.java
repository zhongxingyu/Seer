 package com.derpicons.gshelf;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
public class ChangePassword extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_change_password);
 
 		final Network Net = new Network(this);
 		Bundle bundle = getIntent().getExtras();
 		final String un = bundle.getString("UserName");
 		final String qu = bundle.getString("Question");
 
 		TextView loginScreen = (TextView) findViewById(R.id.login);
 		Button submitPass = (Button) findViewById(R.id.submission);
 		final TextView errorDis = (TextView) findViewById(R.id.errorDisplay);
 
 		// GET QUESTION
 		// Sends user to login screen.
 		loginScreen.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				finish();
 			}
 		});
 
 		submitPass.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				final TextView answerTextView = (TextView) findViewById(R.id.answer);
 				final EditText answer = (EditText) findViewById(R.id.answerField);
 				final EditText password = (EditText) findViewById(R.id.newPasswordField);
 				final TextView passwordTextView = (TextView) findViewById(R.id.newPassword);
 				final EditText ConPassword = (EditText) findViewById(R.id.confirmNewPasswordField);
 				final TextView confirmPasswordTextView = (TextView) findViewById(R.id.confirmPassword);
 				boolean complete = true;
 				
 				// Check that all fields are filled.
 				if (password.getText().toString().length() == 0) {
 					passwordTextView.setTextColor(Color.RED);
 					complete = false;
 				} else
 					passwordTextView.setTextColor(Color.WHITE);
 				if (ConPassword.getText().toString().length() == 0) {
 					confirmPasswordTextView.setTextColor(Color.RED);
 					complete = false;
 				} else
 					confirmPasswordTextView.setTextColor(Color.WHITE);
 				if (answer.getText().toString().length() == 0) {
 					answerTextView.setTextColor(Color.RED);
 					complete = false;
 				} else
 					answerTextView.setTextColor(Color.WHITE);
 				
 				// test password
 				if (complete) {
 					if (!password.getText().toString()
 							.equals(ConPassword.getText().toString())) {
 						confirmPasswordTextView.setTextColor(Color.RED);
 					} else {
 						String ChangeResult = Net.changePassword(answer
 								.getText().toString(), password.getText()
 								.toString());
 						if (ChangeResult == "null") {
 							Intent i = new Intent(getApplicationContext(),
 									MainMenu.class);
 							i.putExtra("UserName", un);
 							startActivity(i);
 						} else {
 							errorDis.setText("Invalid answer.");
 						}
 					}
 				}
 			}
 		});
 	}
 	/*
 	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
 	 * menu; this adds items to the action bar if it is present.
 	 * getMenuInflater().inflate(R.menu.activity_change_password, menu); return
 	 * true; }
 	 */
 
 }
