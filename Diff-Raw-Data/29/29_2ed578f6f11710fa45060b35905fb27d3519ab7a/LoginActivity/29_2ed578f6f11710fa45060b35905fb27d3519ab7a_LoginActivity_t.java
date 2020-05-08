 package com.plantec.uwm;
 
 import com.plantec.uwm.http.HttpManager;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LoginActivity extends Activity {
 	private HttpManager mHttp;
 	private String mUsername;
 	private String mPassword;
 	private UserLoginTask mAuthenticator;
 	
 	private EditText mUsernameField;
 	private EditText mPasswordField;
 	private View mLoginFormView;
 	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login_activity);
 	
 		mUsernameField = (EditText) findViewById(R.id.netLink_id);
 		mPasswordField = (EditText) findViewById(R.id.password);
 		mPasswordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
 		
 		mHttp = HttpManager.getInstance();
 		mAuthenticator = null;
 	}
 	
 	public void attemptLogin(){
 		if (mAuthenticator != null) {
 			return;
 		}
 		
 		mUsernameField.setError(null);
 		mPasswordField.setError(null);
 		
 		mUsername = mUsernameField.getText().toString();
 		mPassword = mPasswordField.getText().toString();
 		
 		boolean cancel = false;
 		View focusView = mUsernameField;
 		
 		if (TextUtils.isEmpty(mPassword)){
 			mPasswordField.setError(getString(R.string.error_field_required));
 			focusView = mPasswordField;
 			cancel = true;
 		}
 		
 		if (TextUtils.isEmpty(mUsername)){
 			mUsernameField.setError(getString(R.string.error_field_required));
 			cancel = true;
 		} else if (mUsername.contains("@")){
 			mUsernameField.setError(getString(R.string.error_netlink_format));
 			cancel = true;
 		}
 		
 		if (!isNetworkAvailable()){
 			mUsernameField.setError(getString(R.string.error_unavailable_network));
 			cancel = true;
 		}
 		
 		System.out.println("TESTING::" + cancel + " " + mUsername + " " + mPassword);
 		if (cancel){
 			focusView.requestFocus();
 		} else {
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthenticator = new UserLoginTask();
 			mAuthenticator.execute((Void) null);
 		}
 	}
 	
 	private boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null 
 	    		&& activeNetworkInfo.isConnectedOrConnecting() 
 	    		&& connectivityManager.getActiveNetworkInfo().isAvailable() 
 	    		&& connectivityManager.getActiveNetworkInfo().isConnected();
 	}
 	
	private void showKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
	}
	
	private void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mPasswordField.getWindowToken(), 0);
	}
	
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
		hideKeyboard();
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
 	
 	
 	class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			try {
 				if (!mHttp.login(mUsername, mPassword))
 					throw new Exception("Login failed");
 			} catch (Exception e) {
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthenticator = null;
 			showProgress(false);
 			System.out.println("PostExecute::" + success);
 			if (success) {
 				SharedPreferences settings = getSharedPreferences("UserInfo", 0);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString("Username", mUsername);
 				editor.putString("Password", mPassword);
 				editor.commit();
 				finish();
 			} else {
				mUsernameField.setText("");
				mPasswordField.setText("");
				mPasswordField.setError(getString(R.string.error_incorrect_credentials));
				mPasswordField.requestFocus();
				showKeyboard();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthenticator = null;
 			showProgress(false);
 		}
 	}
 }
