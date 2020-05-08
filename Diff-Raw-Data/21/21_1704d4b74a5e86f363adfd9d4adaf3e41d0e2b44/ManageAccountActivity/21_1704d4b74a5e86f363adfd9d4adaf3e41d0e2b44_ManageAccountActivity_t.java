 package com.example.restaurantreviewapplication;
 
 import java.util.concurrent.*;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ManageAccountActivity extends Activity {
 
 	private EditText oldPasswordText;
 	private EditText newPasswordText;
 	private EditText newPasswordConfirmText;
 	private ImageButton updatePasswordButton;
 	private EditText oldEmailText;
 	private EditText newEmailText;
 	private EditText newEmailConfirmText;
 	private ImageButton updateEmailButton;
 	private UserApplication app;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		app = (UserApplication)getApplication();
 		setContentView(R.layout.activity_manage_account);
 		setupViews();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_manage_account, menu);
 		return true;
 	}
 	
 	
 	public void updatePasswordButtonHandler(View v) {
 		String oldPassword = oldPasswordText.getText().toString();
 		String newPassword = newPasswordText.getText().toString();
 		String newConfirmPassword = newPasswordConfirmText.getText().toString();
 		
 		int result;
		if(newPassword.compareTo(newConfirmPassword) == 0 && oldPassword.compareTo(app.getPassword()) == 0){
 			Server.setPassword(oldPassword);
 			Server.setUsername(app.getUsername());
 			result = Server.changePassword(newPassword);
 			if(result == 0){
 				Intent intent = new Intent(this, MainMenuActivity.class);
 				Toast.makeText(getApplicationContext(), "Password changed successfully!",
 						Toast.LENGTH_SHORT).show();
 				startActivity(intent);
 			}else{
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle("Error changing password")
 						.setMessage(
 								"Error changing the password.")
 						.setCancelable(false)
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										dialog.cancel();
 									}
 								});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
		}else if(oldPassword.compareTo(app.getPassword()) != 0) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Incorrect Password")
 					.setMessage(
 							"Your current password was entered incorrectly")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
			alert.show();
 		}else{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Passwords Mismatch")
 					.setMessage(
 							"New passwords don't match")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 	
 	public void updateEmailButtonHandler(View v) throws InterruptedException, ExecutionException {
 		String oldEmail = oldEmailText.getText().toString();
 		String newEmail = newEmailText.getText().toString();
 		String newConfirmEmail = newEmailConfirmText.getText().toString();
 		int result;
 		if(newEmail.compareTo(newConfirmEmail) == 0){// && oldEmail == app.getEmail()){
 			Server.setEmail(oldEmail);
 			Server.setUsername(app.getUsername());
 			result = Server.changeEmail(newEmail);
 			if(result == 0){
 				Intent intent = new Intent(this, MainMenuActivity.class);
 				Toast.makeText(getApplicationContext(), "Email successfully changed to " + newEmail + "!",
 						Toast.LENGTH_SHORT).show();
 				startActivity(intent);
 			}else{
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle("Error changing email")
 						.setMessage(
 								"Error changing the email address.")
 						.setCancelable(false)
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										dialog.cancel();
 									}
 								});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		/*}else if(oldEmail != app.getEmail()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Incorrect Email")
 					.setMessage(
 							"Your current email address was entered incorrectly")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();*/
 		}else{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Email Mismatch")
 					.setMessage(
 							"New email addresses don't match")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 	
 	public void onBackPressed() {
     	finish();
         super.onBackPressed();
 	}
 
 	private void setupViews(){
 		oldPasswordText = (EditText) findViewById(R.id.oldPasswordText);
 		newPasswordText = (EditText) findViewById(R.id.newPasswordText);
 		newPasswordConfirmText = (EditText) findViewById(R.id.newPasswordConfirmText);
 		updatePasswordButton = (ImageButton) findViewById(R.id.updatePasswordButton);
 		oldEmailText = (EditText) findViewById(R.id.oldEmailText);
 		newEmailText = (EditText) findViewById(R.id.newEmailText);
 		newEmailConfirmText = (EditText) findViewById(R.id.newEmailConfirmText);
 		updateEmailButton = (ImageButton) findViewById(R.id.updateEmailButton);
 			
 	}
 
 }
