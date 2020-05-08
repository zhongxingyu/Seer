 package com.example.grubber;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.example.grubber.R;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
import android.app.Fragment;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginFragment extends Fragment {
 			
 
     
 
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
 	private View loginView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    /* mPlusClient = new PlusClient.Builder(this, this, this)
 	            .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
 	            .build(); */
 
 		
 		
 	}
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
     	// Inflate the layout for this fragment
     	
 		loginView = inflater.inflate(R.layout.fragment_login, container, false);
 		
 		mEmail = getActivity().getIntent().getStringExtra(EXTRA_EMAIL);
     	mEmailView = (EditText) loginView.findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) loginView.findViewById(R.id.password);
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
 
 		mLoginFormView = loginView.findViewById(R.id.login_form);
 		mLoginStatusView = loginView.findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) loginView.findViewById(R.id.login_status_message);
 
 		loginView.findViewById(R.id.sign_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 		
     	return loginView;
 	}
 
 	/*
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getActivity().onCreateOptionsMenu(menu);
 		getActivity().getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}*/
 
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
 			final JsonObject obj = checkLogin(mEmail,mPassword);
 			
 			if(obj != null)
 			{
 				getActivity().runOnUiThread(new Runnable() {
 					public void run() {
 						//Strip off the quotes around the message!
 						String msg = obj.get("message").toString();
 						msg = msg.substring(1, msg.length()-1);
 					    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
 					    }
 					});
 				if (obj.get("result").getAsBoolean())
 				{
 					SaveSharedPreference.setUserId(getActivity(), obj.get("user_id").getAsInt());
 					return true;
 				}
 			}
 			else
 			{
 				getActivity().runOnUiThread(new Runnable() {
 					public void run() {
 					    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
 					    }
 					});
 				return false;
 			}
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				getActivity().finish();
 			} else {
 				mPasswordView
 						.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 		
 		protected JsonObject checkLogin(String email, String password){
 			URL url;
 			URLConnection uc;
 			BufferedReader data;
 			try {
 				url = new URL("http://cse190.myftp.org:8080/cse190/login?email=" + email + "&password=" + password);
 				uc = url.openConnection();
 				data = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 				String inputLine = data.readLine();
 				JsonParser parser = new JsonParser();
 				return (JsonObject) parser.parse(inputLine);
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				//No Connection
 				return null;
 			}
 		}
 		
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 		
 
 	}
 }
