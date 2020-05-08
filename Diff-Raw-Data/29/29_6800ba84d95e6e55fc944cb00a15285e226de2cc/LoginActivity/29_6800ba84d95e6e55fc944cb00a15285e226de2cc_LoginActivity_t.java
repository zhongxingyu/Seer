 package com.example.mobileindia;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.telephony.PhoneNumberUtils;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.ParseException;
 import com.parse.ParseUser;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity {
 	private static boolean loginSuccess = false;
 	private static int loginErrorCode;
 	// USERNAME IS PHONE NUMBER
     // CREDENTIALS: username: '1111111', password: 'my pass'
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
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 
 		// Set up the login form.
 		mUsername = getIntent().getStringExtra(EXTRA_EMAIL);
 		mUsernameView = (EditText) findViewById(R.id.username);
 		mUsernameView.setText(mUsername);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
						if (id == R.id.btnLogin || id == EditorInfo.IME_NULL) {
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
 		getMenuInflater().inflate(R.menu.activity_login, menu);
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
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mUsername)) {
 			mUsernameView.setError(getString(R.string.error_field_required));
 			focusView = mUsernameView;
 			cancel = true;
 		}else if(!PhoneNumberUtils.isGlobalPhoneNumber(mUsername)){
 			mUsernameView.setError("Not a valid phone number!");
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
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			try {
 				ParseUser.logIn(mUsername, mPassword);
 				LoginActivity.loginSuccess = true;
 			} catch (ParseException e) {
 				Log.d("PARSE","Signup FAILED");
 				Log.d("PARSE", "ERROR CODE: " + String.valueOf(e.getCode()));
 				LoginActivity.loginErrorCode = e.getCode();
 			}
 			return LoginActivity.loginSuccess;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 			if (success) {
 				finish();
 				Context context = getApplicationContext();
 				Intent i = new Intent(context, Categories2.class);
 		        startActivity(i); 
 				int duration = Toast.LENGTH_LONG;
 				Toast toast = Toast.makeText(context, "Successfully logged in!", duration);
 				toast.show();
 			} else {
 				switch (LoginActivity.loginErrorCode) {
 				case ParseException.VALIDATION_ERROR:
 					mPasswordView.setError("Invalid password.");
 					mPasswordView.requestFocus();
 					break;
 				case ParseException.OBJECT_NOT_FOUND:
 					mUsernameView.setError("No account with this phone number and password found");
 					mUsernameView.requestFocus();
 					break;
 				default:
 					break;
 				}
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
