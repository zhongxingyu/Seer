 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginWindow extends Activity {
 
 	
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 	
 	/**
 	 * Login reference
 	 */
 	private Login log = new Login();
 	
 	/**
 	 * tracks number of login attempts
 	 */
 	private int attempts = 0;
 	
 	/**
 	 * Member reference
 	 */
 	private Member temp = null;
 	
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login_window);
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
 		getMenuInflater().inflate(R.menu.activity_login_window, menu);
 		return true;
 	}
 	
 	/**
 	 * Creates user object, will need to be modified once the admin class is created 
 	 * but I was having problems instantiating elsewhere.
 	 */
 	public void createUser() {
 		temp = new User(mEmail,mPassword);
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
 
 		boolean cancel = false;
 		View focusView = null;
 		
 		/**If pass/email fields both empty
 		 *or if email not empty and email hasn't been registered
 		 *go to register activity
 		 */
 		if ((!TextUtils.isEmpty(mEmail) && TextUtils.isEmpty(mPassword) && !log.exists(new Member(mEmail,""))) || (TextUtils.isEmpty(mEmail) && TextUtils.isEmpty(mPassword)))
 			//TODO: Carry over email to registration activity
 			register();
 		
		//Check for a valid password.
 		else if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		} else if (!mEmail.contains("@")) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
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
 				
 			Member chk = new User(mEmail,mPassword);
 			if(temp==null || !temp.equals(chk)) { 
 				//Instantiates member, checks to see if the username is the same on each attempt
 				createUser();
 				temp = log.update((User) temp); 
 				//updates locked status of account
 				attempts=0;
 			}
 			
 			if(log.exists(temp)) { 
 				// Checks for valid user name
 				mAuthTask = new UserLoginTask();
 				mAuthTask.execute((Void) null);
 			}
 			
 			else { 
 				//To register activity
 				register();
 				
 				
 				
 				
 			}
 			
 			
 		    
 			
 		}
 	}
 	
 
 	/**
 	 * Goes to register screen
 	 */
 	private void register() {
 		Intent goToNextActivity = new Intent(getApplicationContext(), Register.class);
 		startActivity(goToNextActivity);
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
 			// TODO: attempt authentication against a network service.
 			
 
 			try {
 				// Simulate network access.
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				return false;
 			}
 			if(temp instanceof User) // Won't be necessary for admin						
 				if(log.verifyUser(temp) && !((User)temp).locked())
 					//Validates user info and checks to see if their account is locked
 					return true;
 			return false;
 			
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 			
 			if (!((User)temp).locked() && success) { //User successfully logs in
 				attempts = 0;
 				//TODO: Go to main activity
 				Intent main = new Intent(getApplicationContext(), ItemListActivity.class);
 				finish();
 				startActivity(main);
 				
 				
 			} else {
 				if(attempts!=3 && !((User) temp).locked()) {  //Wrong password
 					mPasswordView
 						.setError(getString(R.string.error_incorrect_password) + " ");
 					mPasswordView.requestFocus();
 					attempts++;
 				}
 				else {
 					if(!((User) temp).locked()) //locks the account after three attempts
 						log.lock((User)temp);
 					mPasswordView
 					.setError("Exceeded login attempts, account locked");
 					mPasswordView.requestFocus();
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
