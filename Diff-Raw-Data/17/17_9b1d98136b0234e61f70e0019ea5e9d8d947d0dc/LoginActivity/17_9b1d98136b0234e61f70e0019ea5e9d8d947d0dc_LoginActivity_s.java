 package br.itabits.cerberus.login;
 
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
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import br.itabits.cerberus.MenuActivity;
 import br.itabits.cerberus.R;
 import br.itabits.cerberus.network.NetworkManager;
 import br.itabits.cerberus.network.NetworkResponse;
 
 /**
  * Activity which displays a login screen to the user, offering registration as well.<br>
  * Basically, there are two types of login:<br>
  * - one local and easy connection that requires an email and the local password
  * - a connection based on a login with Facebook account
  * 
  * @author Marcelo
  */
 public class LoginActivity extends Activity implements NetworkResponse {
 	
 	/* * Constants * */
 	
 	/**
 	 * A Conscious Discipline authentication store containing known user names and passwords.
 	 */
 	private static final String[] MY_CREDENTIALS = new String[] {
 			"itabits@ita.br:xupadoente",
			"a@a:a" };
 
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_USER_ID = "br.itabits.cerberus.extra.EMAIL";
 	/**
 	 * The default field to know whether the connection with facebook is OK.
 	 */
 	public static final String EXTRA_FACEBOOK = "br.itabits.cerberus.extra.FACEBOOK";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	/**
 	 * When the login will be done with facebook.
 	 */
 	private static final int FACEBOOK_REQUEST = 1;
 	/**
 	 * The login with facebook account was successful.
 	 */
 	public static final int FACEBOOK_SUCCESS = 23;
 
 	/* * Fields * */
 	
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private Button mSignInButton;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	// Network validation
 	NetworkManager networkManager;
 
 	/* * Activity Methods * */
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 
 		// Set up the manager of network connection
 		networkManager = new NetworkManager(getApplicationContext());
 		networkManager.onCreate(savedInstanceState);
 		networkManager.setResponse(this);
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_USER_ID);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
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
 
 		mSignInButton = (Button) findViewById(R.id.sign_in_button);
 		mSignInButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				attemptLogin();
 			}
 		});
 
 		findViewById(R.id.buttonLoginFragment).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent openFacebookLogin = new Intent(LoginActivity.this, FacebookLoginFragmentActivity.class);
 				startActivityForResult(openFacebookLogin, FACEBOOK_REQUEST);
 			}
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == FACEBOOK_REQUEST && data != null) {
 			int value = data.getIntExtra(EXTRA_FACEBOOK, 0);
 			if (value == FACEBOOK_SUCCESS)
 				finish();
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		networkManager.onStart();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		networkManager.onDestroy();
 	}
 
 	/* * Methods * */
 	
 	/**
 	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email,
 	 * missing fields, etc.), the errors are presented and no actual login attempt is made.
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
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
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
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 
 	// Private Methods
 	
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/* * * * AssyncTask * * * */
 	
 	/**
 	 * Represents an asynchronous login/registration task used to 
 	 * authenticate the user locally and generate a ID.
 	 * 
 	 * @author Marcelo
 	 */
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 
 			try {
 				// Simulate network access.
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				return false;
 			}
 
 			for (String credential : MY_CREDENTIALS) {
 				String[] pieces = credential.split(":");
 
 				// return true if the password matches.
 				if (pieces[1].equals(mPassword))
 					return true;
 			}
 
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				Intent openBorrow = new Intent(LoginActivity.this, MenuActivity.class);
 				// put the identification by email to the program
 				openBorrow.putExtra(EXTRA_USER_ID, mEmail);
 				startActivity(openBorrow);
 				finish();
 			} else {
 				mPasswordView.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 
 	@Override
 	public void onChangesDetected(boolean active) {
 		View focusView = null;
 		// Check for a valid network connection.
 		if (!active) {
 			// There was an error;
 			// don't allow sign in without WIFIconn
 			mSignInButton.setError(getString(R.string.connection_error));
 			mSignInButton.setFocusableInTouchMode(true);
 			focusView = mSignInButton;
 			focusView.requestFocus();
 		} else {
 			// reset error
 			mSignInButton.setError(null);
 			mSignInButton.setFocusableInTouchMode(false);
 		}
 		mSignInButton.setClickable(active);
 	}
 }
