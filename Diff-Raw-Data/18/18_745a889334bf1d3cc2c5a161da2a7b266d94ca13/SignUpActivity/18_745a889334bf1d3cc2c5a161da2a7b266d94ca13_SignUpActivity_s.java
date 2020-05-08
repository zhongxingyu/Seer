 package com.example.myvirtualcardwallet;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
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
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class SignUpActivity extends Activity {
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	private String mConfirmPassword;
 	private String mName;
 	private String mUsername;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mConfirmPasswordView;
 	private EditText mNameView;
 	private EditText mUsernameView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	
 	//Network Information
 	protected int port;
 	protected String ip;
 	protected Socket client;
 	protected ServerSocket server;
 	protected BufferedReader in;
 	protected PrintWriter out;
 	protected String reply;
 	protected JSONObject elements;
 	protected JSONObject req;
 	//Login Verified
 	protected boolean loginverified = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_sign_up);
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.email1);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password1);
 		mConfirmPasswordView = (EditText) findViewById(R.id.password2);
 		mNameView = (EditText) findViewById(R.id.name);
 		mUsernameView = (EditText) findViewById(R.id.username);
 		
 		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.sign_up_button || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form1);
 		mLoginStatusView = findViewById(R.id.login_status1);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message1);
 
 		findViewById(R.id.register).setOnClickListener(
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
 		getMenuInflater().inflate(R.menu.sign_up, menu);
 		return true;
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
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		mConfirmPassword = mConfirmPasswordView.getText().toString();
 		mName = mNameView.getText().toString();
 		mUsername = mUsernameView.getText().toString();
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
 		else if(!mPassword.equals(mConfirmPassword)){
 			mConfirmPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mConfirmPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		} else if ((!mEmail.contains("@")) || ((!mEmail.contains(".com")))) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
 		}
 		
 		//Check Name
 		if (TextUtils.isEmpty(mName)) {
 			mNameView.setError(getString(R.string.error_field_required));
 			focusView = mNameView;
 			cancel = true;
 		}
 		
 		//Check Username
 		if (TextUtils.isEmpty(mUsername)) {
 			mUsernameView.setError(getString(R.string.error_field_required));
 			focusView = mUsernameView;
 			cancel = true;
 		}
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
 									
 //						for (String credential : DUMMY_CREDENTIALS) {
 //							String[] pieces = credential.split(":");
 //							if (pieces[0].equals(mEmail)) {
 //								// Account exists, return true if the password matches.
 //								return pieces[1].equals(mPassword);
 //							}
 //						}
 
 						// TODO: register the new account here.
 						
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
 			req.put("request", "signup");
 			
 			//Create message to be sent username and pass
 			//elements = new JSONObject();
 
 			req.put("name", mName);
 			req.put("email", mEmail);
 			req.put("username", mUsername);
 			req.put("password", mPassword);
 		
 			try {
				 ip = new String("192.168.1.15");	 
 				 port = 1234;
 				 Log.d("ClientActivity", "C: Connecting...");
 				 client = new Socket(ip, port);
 				 server = null;	
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			//>>>>>>>>>End Setup>>>>>>>>>>>>>>>>>>
 			 
 			 //Setup the Input and Output Stream
 			 try {
 				out = new PrintWriter(client.getOutputStream());
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
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
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}while((!reply.equals("Yes")) && (!reply.equals("Exists")));
 			if(reply.equals("Yes")){
 				//Success Go to app
 				loginverified = true;
 			}
 			else{
 				//We need to go reg page
 				loginverified = false;
 			}
 			// TODO: attempt authentication against a network service	
 			return loginverified;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				//Toast.makeText(getApplicationContext(), "Success",Toast.LENGTH_LONG).show();
 				//Go to the App activity
 				Intent home = new Intent(getApplicationContext(), MakeCard.class);
 		        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		        Bundle b= new Bundle();
 		        b.putString("Username",mUsername);
 		        b.putString("Name", mName);
 		        home.putExtras(b);
 		        startActivity(home);
 		        
 		      //   Close Login Screen
 		     //   Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
 		       // startActivity(intent);
 			} else {
 				
 				//Go to the App activity
 				Intent home = new Intent(getApplicationContext(), SignUpActivity.class);
 		        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		        
 		        
 		        startActivity(home);
 //				mPasswordView
 //						.setError(getString(R.string.error_incorrect_password));
 //				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
