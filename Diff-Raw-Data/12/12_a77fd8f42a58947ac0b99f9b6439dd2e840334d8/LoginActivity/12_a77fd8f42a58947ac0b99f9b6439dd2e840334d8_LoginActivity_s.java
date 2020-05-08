 package com.TeamKeyLimePie.KeyLifePlanner;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class LoginActivity extends Activity implements OnClickListener {
 	public EditText username;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		//interface components
 		TextView loginLabel = (TextView)findViewById(R.id.login_title);
 		TextView usernameLabel = (TextView)findViewById(R.id.UsernameLabel);
 		Button submit = (Button)findViewById(R.id.submitLogin);
 		submit.setOnClickListener(this);
 
 		//font stuff
 		Typeface tf1 = Typeface.createFromAsset(getAssets(), "fonts/Lorenabold.ttf");
 		Typeface tf2 = Typeface.createFromAsset(getAssets(), "fonts/governor.ttf");
 
 		//setting fonts
 		loginLabel.setTypeface(tf1);
 		usernameLabel.setTypeface(tf2);
 		submit.setTypeface(tf2);
		
 	}
 
 	@Override
 	public void onClick(View v) {
		String user = username.getText().toString();
 
 		if(v.getId() == R.id.submitLogin)
 		{
 			//check if login is valid
 			if(checkLogin(user) == false)
 			{
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Wrong username, try again.", Toast.LENGTH_LONG);
 				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 350);
 				toast.show();
 			}
 
 			else
 			{		
 				//success sound
 				MediaPlayer success = MediaPlayer.create(LoginActivity.this, R.raw.success);
 				success.start();
 
 				//toast to show successful login
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Successful Login, press back to access menu", Toast.LENGTH_LONG);
 				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 350);
 				toast.show();
 
 				//sets current user for the app
 				((GlobalApp)getApplication()).setUser(user);
 				
 				//assign all values associated with user into GlobalApp somewhere here?
 				
 				
 				//exit current screen
 				finish();
 			}
 		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 	public boolean checkLogin(String user)
 	{
 		return false;
 	}
 
 }
