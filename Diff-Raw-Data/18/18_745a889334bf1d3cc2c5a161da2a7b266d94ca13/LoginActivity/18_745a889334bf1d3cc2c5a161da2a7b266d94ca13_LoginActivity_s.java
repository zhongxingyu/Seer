 package com.example.myvirtualcardwallet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.*;
 import java.io.*;
 import java.net.UnknownHostException;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseObject;
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity {
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mUsername;
 	private String mPassword;
 
 	// UI references.
 	private EditText mUsernameView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	
 	//Network Information
 	 int port;
 	 String ip;
 	 Socket client;
 	 ServerSocket server;
 	 BufferedReader in;
 	 PrintWriter out;
 	 String reply;
 	 JSONObject elements;
 	 JSONObject req;
 	//Login Verified
 	boolean loginverified = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		Parse.initialize(this, "HIWiddAFOEElxePoa7qHp72CHvwtNuqOXf1bXkjf", "aJpMrnM1WfUOpLGGSBUSl4pLPh4vVSdQrgL4VBi3"); 
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		// Set up the login form.
 		mUsername = getIntent().getStringExtra(EXTRA_EMAIL);
 		mUsernameView = (EditText) findViewById(R.id.username);
 		mUsernameView.setText(mUsername);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.sign_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 	/**
 	 *	Register Button 
 	 */
 	public void OnClick1(View view){
 		Intent intent = new Intent(this, SignUpActivity.class);
 		startActivity(intent);
 	}
 	
 	
 	
 	
 	/**
 	 * Attempts to sign in or register the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin() {
 		if (mAuthTask != null) {
 			return;
 		}
 
 		// Reset errors.
 		mUsernameView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mUsername = mUsernameView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mUsername)) {
 			mUsernameView.setError(getString(R.string.error_field_required));
 			focusView = mUsernameView;
 			cancel = true;
 		}
 //		} else if (!mEmail.contains("@")) {
 //			mEmailView.setError(getString(R.string.error_invalid_email));
 //			focusView = mEmailView;
 //			cancel = true;
 //		}
 
 		if (cancel) {
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);			
 		}
 	}
 
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			
 			//Create Request message
 			req = new JSONObject();
 			req.put("request", "signin");
 			
 			//Create message to be sent username and pass
 //			elements = new JSONObject();
 			req.put("username", mUsername);
 			req.put("password", mPassword);
 		
 			try {
				 ip = new String("192.168.1.15");	 
 				 port = 1234;
 				 Log.d("ClientActivity", "C: Connecting...");
 				 client = new Socket(ip, port);
 				 server = null;	
 			} catch (UnknownHostException e) {
 				
 				e.printStackTrace();
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			}
 			//>>>>>>>>>End Setup>>>>>>>>>>>>>>>>>>
 			 
 			 //Setup the Input and Output Stream
 			 try {
 				out = new PrintWriter(client.getOutputStream());
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			}
 			 /**
 			  *  Communicate with the server
 			  *  Send the initial username and password
 			  */
 			 out.write(req.toString());
 			 out.flush();
 //			 out.write(elements.toString());
 //			 out.flush();
 			/**
 			 * 	 Wait for server to respond
 			 */
 			 JSONParser parser = new JSONParser();
 				JSONObject tmp = new JSONObject();
 				do{
 					try {
 						reply = new String(in.readLine());
 						tmp = (JSONObject) parser.parse(reply);
 						reply = (String) tmp.get("response");
 					} catch (IOException e) {
 				
 						e.printStackTrace();
 					} catch (ParseException e) {
 						
 						e.printStackTrace();
 					}
 				}while((!reply.equals("Yes")) && (!reply.equals("No")));
 			if(reply.equals("Yes")){
 				//Success Go to app
 				loginverified = true;
 			}
 			else{
 				//We need to go reg page
 				loginverified = false;
 			}
 			// TODO: attempt authentication against a network service
 			try {
 				client.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return loginverified;
 			
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				
 				//Go to the App activity
 				Intent home = new Intent(getApplicationContext(), Options.class);
 				
 				//home.putExtra("username", mUsername );
 				//home.putExtra("ip", ip);
 				//home.putExtra("port", new Integer(port).toString());
 				Log.d("USERNAME-LOGIN_AC", mUsername);
 				
 				//Creating Bundle
 				Bundle b = new Bundle();
 				b.putString("Username", mUsername);
 				b.putString("ip", ip);
 				b.putInt("port", port);
 				
 				//adding bundle to intent
 				home.putExtras(b);
 		        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		        startActivity(home);
 		        // Close Login Screen
 		        finish();
 			} else {
 
 				mPasswordView
 						.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 
 	}
 }
 
