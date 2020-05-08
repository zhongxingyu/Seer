 package ca.hvsi.app;
 
 import ca.hvsi.lib.ApiClient;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends RoboSherlockActivity {
 
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
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 		setupActionBar();
 
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
 		new HvsIApp.setOptionsMenuTask().execute(this);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			// Show the Up button in the action bar.
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			// TODO: If Settings has multiple levels, Up should navigate up
 			// that hierarchy.
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
		case HvsIApp.FORGOT_ID:
 			new AsyncTask<String, Void, Boolean>() {
 				public Boolean doInBackground(String... args) {
 					return (Boolean)API.api().call("forgot_password", args[0]);
 				}
 				public void onPostExecute(Boolean val) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 					boolean result = val;
					String msg = getString(result?R.string.action_forgot_password_ok:R.string.action_forgot_password_fail);
 					builder.setMessage(msg).
							setTitle(getString(R.string.action_forgot_password_title)).
 							setPositiveButton("OK", null).
 							show();
 				}
 			}.execute(mEmail);
 			return true;
 		case HvsIApp.REGISTER_ID:
 			Intent do_register_intent = new Intent(getApplicationContext(), RegisterActivity.class);
 			startActivityForResult(do_register_intent, HvsIApp.REGISTER_REQUEST_CODE);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		menu.add(0, HvsIApp.FORGOT_ID, 0, getString(R.string.action_forgot_password_title));
 		ca.hvsi.lib.Account self = API.self();
 		if ((!API.logged_in() && API.can_register()) || (self != null && self.getClass().equals(ca.hvsi.lib.Admin.class)))
 			menu.add(0, HvsIApp.REGISTER_ID, 0, getString(R.string.register));
 		return super.onCreateOptionsMenu(menu);
 	}
 	@Override
 	protected void onActivityResult(int req, int res, Intent data) {
 		if (req == HvsIApp.REGISTER_REQUEST_CODE) {
 			if (res == HvsIApp.REGISTER_OK) {
 				setResult(res);
 				finish();
 			}
 		}
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
 			ApiClient api = API.api();
 			api.call("login", mEmail, mPassword);
 			return (Boolean)api.get("logged_in");
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 			if (success) {
 				setResult(HvsIApp.LOGIN_OK);
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
