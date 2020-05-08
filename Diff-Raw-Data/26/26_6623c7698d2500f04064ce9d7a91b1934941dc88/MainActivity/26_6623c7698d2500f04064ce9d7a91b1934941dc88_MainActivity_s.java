 package edu.vt.ece4564.cats_app;
 
 import java.util.concurrent.ExecutionException;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 	
 	private EditText usernameField;
 	private EditText passwordField;
 	private EditText phoneField;
 	private Button loginButton;
 	private Button createAccountButton;
 	private Button submitButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         usernameField = (EditText) findViewById(R.id.usernameField);
         passwordField = (EditText) findViewById(R.id.passwordField);
         phoneField = (EditText) findViewById(R.id.phoneField);
         loginButton = (Button) findViewById(R.id.loginButton);
         createAccountButton = (Button) findViewById(R.id.newAccountButton);
         submitButton = (Button) findViewById(R.id.submitButton);
         
         loginButton.setOnClickListener(this);
         createAccountButton.setOnClickListener(this);
         submitButton.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	public void onClick(View arg0) {
 		if(arg0.getId() == R.id.loginButton){
 			//validate login information, if good, then pass to next activity
 			String username = usernameField.getText().toString();
 			String password = passwordField.getText().toString();
 			String url = "http://chatallthestuff.appspot.com/user/validate?username="
 					+ username.trim() + "&pass=" + password;
 			SendRequestTask request = new SendRequestTask();
 			request.execute(url);
 			try {
 				String result = request.get();
 				Log.i("login", result);
 				if(result.contains("Valid")){
 					Toast toast = Toast.makeText(getApplicationContext(), 
 							"Successful login",Toast.LENGTH_SHORT);
 					toast.show();
 					//User is logged in, pass to group screen
 					Intent i = new Intent(this, SelectGroupActivity.class);
 					i.putExtra("username", username.trim());
 					startActivity(i);
 				}
 				else{
 					Toast toast = Toast.makeText(getApplicationContext(), 
 							"Your username and/or password is incorrect.",Toast.LENGTH_LONG);
 					toast.show();
 				}
 			} catch (InterruptedException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong! Please try again.",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong! Please try again.",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			}
 		}
 		else if(arg0.getId() == R.id.newAccountButton){
 			//prompt for phone number
 			phoneField.setVisibility(View.VISIBLE);
 			createAccountButton.setVisibility(View.GONE);
 			loginButton.setVisibility(View.GONE);
 			submitButton.setVisibility(View.VISIBLE);
 		}
 		else if(arg0.getId() == R.id.submitButton){
 			//save new account, go to next activity
 			String username = usernameField.getText().toString();
 			String password = passwordField.getText().toString();
 			String number = phoneField.getText().toString();
 			
 			String url = "http://chatallthestuff.appspot.com/user/new?username=" +
 					username + "&password=" + password + "&number=" + number;
 			SendRequestTask request = new SendRequestTask();
 			request.execute(url);
 			try {
 				String result = request.get();
 				Log.i("create", result);
 				if(result.contains("Success")){
					Toast toast = Toast.makeText(getApplicationContext(), 
							"YAY!",Toast.LENGTH_SHORT);
					toast.show();
 				}
 				else{
 					Toast toast = Toast.makeText(getApplicationContext(), 
 							"Sorry, that username is already taken!",Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			} catch (InterruptedException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong (InterruptedException)! Please try again",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong (ExecutionException)! Please try again",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			}
 		}
 	}
 }
