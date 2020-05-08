 package edu.gatech.cs4261.LAWN;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import edu.gatech.cs4261.LAWN.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Login extends CustomActivity {
 	private static final String TAG = "Login";
 	
 	//globals
 	CheckBox cbox;
 	
 	/* what to do when the activity is first made*/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		
 		Log.d(TAG, "inside onCreate of Login");
 		
 		/* set up the button listeners*/
 		Button btnLogin = (Button)findViewById(R.id.LogInButton);
 		btnLogin.setOnClickListener(LoginListener);
 		
 		//check a boolean in preferences to see if remember was used last time
 		boolean prevSavedLogin;
 		prevSavedLogin = getPreferences().getBoolean("RememberMe", false);
 		
 		//initialize the checkbox
 		cbox = (CheckBox) findViewById(R.id.RememberMeCheck);
 		
 		Log.d(TAG, "after find checkbox");
 		
 		/* auto login or not based on save preference*/
 		if(prevSavedLogin) {
 			/*start the main activity*/
 			//make the intent to call the new screen
 			Intent KingRaw = new Intent(Login.this, ProjectLAWNActivity.class);
 			
 			//start the new activity
 			Login.this.startActivity(KingRaw);
 			
			Log.v(TAG, "Successful autolog in");
 		} else {
 			/* wipe the username in preferences and wait for the new one*/
 		    SharedPreferences.Editor editor = getPreferences().edit();
 		    editor.remove("username");
 		    editor.commit();
 		    
			Log.v(TAG, "autologin didn't pass: " + prevSavedLogin);
 		}
 	}
 	
 	/* make the login click listener*/
 	private OnClickListener LoginListener = new OnClickListener() {
 		public void onClick(View v) {
 			/*what to do when the button is clicked*/
 			
 			/* set up the input listeners*/
 			EditText userField = (EditText)findViewById(R.id.UsernameInput);
 			
 			/* get the data from the input fields*/
 			String username = userField.getText().toString();
 			
 			/*call the main activity with the username if it's hreichenba3 or hbaker3*/
 			if(username.equals("hreichenba3") || username.equals("hbaker3")) {
 				//make the intent to call the new screen
 				Intent KingRaw = new Intent(Login.this, ProjectLAWNActivity.class);
 				
 				//save the username to preferences
 				SharedPreferences.Editor editor = getPreferences().edit();
 				editor.putString("username", username);
 				
 				//check if remember me was checked
 		        if (cbox.isChecked()) {
 		            editor.putBoolean("RememberMe", true);
 		        } else {
 		            editor.putBoolean("RememberMe", false);
 		        }
		        
		        //commit the changes to the preferences
		        editor.commit();
 				
 				//start the new activity
 				Login.this.startActivity(KingRaw);
 				
 				Log.i(TAG, "Successful log in");
 			} else {
 				Log.i(TAG, "Login failed");
 				
 				/*display a message indicating failure to login*/
 				Toast display = Toast.makeText(getBaseContext(), "Failed to login", Toast.LENGTH_SHORT);
 				display.show();
 			}
 		}
 	};
 }
