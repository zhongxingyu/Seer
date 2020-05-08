 package uq.deco7381.runspyrun.activity;
 
 import uq.deco7381.runspyrun.R;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 import com.parse.RequestPasswordResetCallback;
 
 public class LoginActivity extends Activity {
 	
 	public static final String PREFS_NAME = "LoginInfo";
 	private static final String PREF_USERNAME = "username";
 	
 	private View mContentView;
 	private View mLoadingView;
 	private int mShortAnimationDuration;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		Parse.initialize(this, "2XLuNz2w0M4iTL5VwXY2w6ICc7aYPZfnr7xyB4EF", "6ZHEiV500losBP4oHmX4f1qVuct1VyRgOlByTVQB");
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		// Get the username for device's storage
 		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
 		String username = pref.getString(PREF_USERNAME, null);
 		setUsername(username);
 
 		mContentView = findViewById(R.id.login_content);
 		mLoadingView = findViewById(R.id.loading_spinner);
 		
 		mLoadingView.setVisibility(View.GONE);
 		mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
 	}
 
 	private void setUsername(String username){
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		usernameEditText.setText(username);
 	}
 	
 	// LoginActivity with Parse service
 	public void login(View view){
 			
 		showLoading();
 		final Intent intent = new Intent(this, DashboardActivity.class);
 			
 		//Retrieve user input
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		String usernameString = usernameEditText.getText().toString();
 		intent.putExtra("username", usernameString);
 		
 		EditText passwordEditText = (EditText) findViewById(R.id.password);
 		String passwordString = passwordEditText.getText().toString();
 		
 		// LoginActivity system by Parse
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		ParseUser.logInInBackground(usernameString, passwordString, new LogInCallback() {
 				
 			@Override
 			public void done(ParseUser user, ParseException e) {
 				// TODO Auto-generated method stub
 				if(user != null){
 					startActivity(intent);
					showContent();
 				}else{
 					//if there is a exception from parse, show in dialog box
 					builder.setTitle("Alert");
 					builder.setMessage("Username or Password is invalid");
 					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.cancel();
 							showContent();
 						}
 					});
 					//show the dialog box
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			}
 		});
 	}
 
 	// SignupActivity with Parse service
 	public void signup(View view){
 		Intent intent = new Intent(this, SignupActivity.class);
 		startActivity(intent);
 	}
 	
 	// Reset password with Parse service
 	public void resetPassword(View view){
 		// Set up a layout to input in Alert Dialog
 		LayoutInflater inflater = LayoutInflater.from(this);
 		final View v = inflater.inflate(R.layout.dialog_resetpassword, null);
 		
 		// Build a AlertDialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Reset the password");
 		builder.setView(v);
 		builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {		
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				// Get the email address from the AlertDialog and send a password reset email to user.
 				EditText email = (EditText)(v.findViewById(R.id.rpEmail));
 				ParseUser.requestPasswordResetInBackground(email.getText().toString(), new RequestPasswordResetCallback(){
 					@Override
 					public void done(ParseException e) {
 						// TODO Auto-generated method stub
 						// Avoid software keyboard remain on the screen after click "Send" button
 						InputMethodManager imm = ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
 						imm.hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
 						if(e == null){
 							Toast.makeText(getApplicationContext(), "Password reset mail sent", Toast.LENGTH_LONG).show();
 						}else{
 							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 						}
 					}
 					
 				});
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	// Show the Content of activity with animation
 	private void showContent(){
 		mContentView.setAlpha(0f);
 		mContentView.setVisibility(View.VISIBLE);
 		
 		mContentView.animate()
 					.alpha(1f)
 					.setDuration(mShortAnimationDuration)
 					.setListener(null);
 		
 		mLoadingView.animate()
 					.alpha(0f)
 					.setDuration(mShortAnimationDuration)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 	                    public void onAnimationEnd(Animator animation) {
 	                        mLoadingView.setVisibility(View.GONE);
 	                    }
 					});
 	}
 	// Show loading process with animation
 	private void showLoading(){
 		mLoadingView.setAlpha(0f);
 		mLoadingView.setVisibility(View.VISIBLE);
 		
 		mLoadingView.animate()
 					.alpha(1f)
 					.setDuration(mShortAnimationDuration)
 					.setListener(null);
 		
 		mContentView.animate()
 					.alpha(0.5f)
 					.setDuration(mShortAnimationDuration)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 	                    public void onAnimationEnd(Animator animation) {
 							mContentView.setVisibility(View.GONE);
 	                    }
 					});
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 
 		//Store the username into the device.
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		String usernameString = usernameEditText.getText().toString();
 		
 		getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
 		.edit()
 		.putString(PREF_USERNAME, usernameString)
 		.commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 
 }
