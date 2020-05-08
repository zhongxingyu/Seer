 package edu.gatech.oad.rocket.findmythings;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import edu.gatech.oad.rocket.findmythings.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.service.EndpointUtils;
 import edu.gatech.oad.rocket.findmythings.service.Fmthings;
 import edu.gatech.oad.rocket.findmythings.util.*;
 import edu.gatech.oad.rocket.findmythings.util.validation.EmailValidator;
 
 import java.io.IOException;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * Activity that takes care of registration of a new member
  *
  * @author TeamRocket
  * */
 public class RegisterActivity extends Activity {
 
 	/**
 	 * Form values.
 	 */
 	private String mEmail, mPassword, mCon, mPhone, mName, mAddress;
 
 	/**
 	 * Copied email address from the login window.
 	 */
 	public static String rEmail = "";
 
 	/**
 	 * UI references.
 	 */
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mPhoneView;
 	private EditText mAddressView;
 	private EditText mNameView;
 	private EditText mConfirmView;
 
 	private View focusView;
 
 	private View mStatusForm;
 	private View mStatusView;
 	private TextView mStatusMessageView;
 
 	/**
 	 * Keep track of the task to ensure we can cancel it if requested.
 	 */
 	private RegisterUserTask mSubmitTask = null;
 
 	/**
 	 * creates new window with correct layout
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register);
 
 		// Hide the Up button in the action bar.
 		setupActionBar();
 
 		// Gets mEmail from LoginActivity
 		Intent i = getIntent();
 		String s = i.getExtras().getString("email");
 		if (s == null) s = "";
 
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(s);
 		rEmail = s;
 
 		mPasswordView = (EditText) findViewById(R.id.pass);
 		mAddressView = (EditText) findViewById(R.id.address);
 		mNameView = (EditText) findViewById(R.id.lookingfor);
 		mConfirmView = (EditText) findViewById(R.id.confirmpass);
 		mPhoneView = (EditText) findViewById(R.id.phone);
 		mPhoneView.addTextChangedListener(new PhoneNumberTextWatcher());
		mPhoneView.setFilters(new InputFilter[] { new PhoneNumberFilter(), new InputFilter.LengthFilter(14) });
 
 		mStatusForm = findViewById(R.id.register_form);
 		mStatusView = findViewById(R.id.register_status);
 		mStatusMessageView = (TextView) findViewById(R.id.register_status_message);
 	}
 
 	/**
 	 * deals with action to do once a key is pressed down
 	 * @param keyCode - key pressed
 	 * @param event - event to do in case of pressed
 	 * @return true when done
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			super.onBackPressed();
 			return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setDisplayShowHomeEnabled(true);
 	}
 
 	private boolean containsNoErrors() {
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			return false;
 		} else if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			return false;
 		} else if (!EmailValidator.getInstance().isValid(mEmail)) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			return false;
 		} else if (!mPassword.equals(mCon)) {
 			mPasswordView.setError(getString(R.string.error_passwords_match));
 			focusView = mPasswordView;
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * RegisterActivity new user and return to login screen
 	 * or just move on to the main screen with the newly created user
 	 * already logged in.
 	 */
 	private boolean attemptToRegister() {
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		mPhone = mPhoneView.getText().toString();
 		mCon = mConfirmView.getText().toString();
 		mName = mNameView.getText().toString();
 		mAddress = mAddressView.getText().toString();
 
 		if (mSubmitTask != null) {
 			return false;
 		}
 
 		if (containsNoErrors()) {
 			mStatusMessageView.setText(R.string.submit_progress_message);
 			showProgress(true);
 
 			mSubmitTask = new RegisterUserTask();
 			mSubmitTask.execute();
 
 			return true;
 		} else {
 			focusView.requestFocus();
 			return false;
 		}
 	}
 
 	/**
 	 * creates the options menu 
 	 * @param menu
 	 * @return true when done
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.register, menu);
 		return true;
 	}
 
 	/**
 	 * deals with action when an options button is selected
 	 * @param item
 	 * @return boolean  
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.register_ok:
 			return attemptToRegister();
 		case R.id.register_cancel:
 		case android.R.id.home:
 			return toLogin(false);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	/**
 	 * Shows the progress UI and hides the form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mStatusView.setVisibility(View.VISIBLE);
 			mStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 						}
 					});
 
 			mStatusForm.setVisibility(View.VISIBLE);
 			mStatusForm.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mStatusForm.setVisibility(show ? View.GONE : View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mStatusForm.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous submission task used to upload an item.
 	 */
 	public class RegisterUserTask extends AsyncTask<Void, Void, MessageBean> {
 
 		@Override
 		protected MessageBean doInBackground(Void... param) {
 			try {
 				Fmthings.Account.Register op = EndpointUtils.getEndpoint().account().register(mEmail, mPassword, mCon);
 				if (mPhone != null) op.setPhone(mPhone);
 				if (mName != null) op.setName(mName);
 				if (mAddress != null) op.setAddress(mAddress);
 				return op.execute();
 			} catch (IOException e) {
 				return null;
 			}
 		}
 
 		/**
 		 * deals with action when registered either successfully or not
 		 * @param output - response from the API method
 		 */
 		@Override
 		protected void onPostExecute(final MessageBean output) {
 			mSubmitTask = null;
 
 			String status = null, failureMessage = null;
 			if (output != null) {
 				status = output.getMessage();
 				failureMessage = output.getFailureReason();
 			}
 			Messages.Register failureType = EnumHelper.forTextString(Messages.Register.class, failureMessage);
 
 			if (status != null && status.equals(Messages.Status.OK.getText())) {
 				toLogin(true);
 			} else if (failureType != null){
 				switch (failureType) {
 					case ALREADY_USER:
 						mEmailView.setError(getString(R.string.error_already_user));
 						mEmailView.requestFocus();
 						break;
 					case BAD_EMAIL_ADDRESS:
 						mEmailView.setError(getString(R.string.error_invalid_email));
 						mEmailView.requestFocus();
 						break;
 					case BAD_PASSWORD:
 						mPasswordView.setError(getString(R.string.error_short_password));
 						mPasswordView.requestFocus();
 						break;
 					case PASSWORDS_MATCH:
 						mPasswordView.setError(getString(R.string.error_passwords_match));
 						mPasswordView.requestFocus();
 						break;
 					case INVALID_PHONE:
 						mPhoneView.setError(getString(R.string.error_invalid_phone));
 						mPhoneView.requestFocus();
 						break;
 					default:
 						ToastHelper.showError(RegisterActivity.this, getString(R.string.error_invalid_data));
 						break;
 				}
 			} else {
 				ToastHelper.showError(RegisterActivity.this, getString(R.string.error_no_response));
 			}
 
 			showProgress(false);
 		}
 
 		/**
 		 * deals with action when task cancelled
 		 */
 		@Override
 		protected void onCancelled() {
 			mSubmitTask = null;
 			showProgress(false);
 		}
 	}
 	
 	/**
 	 * Called to pop the login window from the navigation stack
 	 */
 	@Override 
     public void finish() {
         super.finish();
         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
     }
 	
 	/**
 	 * Returns to the login window either as a result of registering or hitting back
 	 * @param registered true to push log in, false to pop
 	 * @return true, always true. Why? Because.
 	 */
 	private boolean toLogin(boolean registered) {
 		finish();
 	    if (registered) overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 	    return true;
 	}
 
 }
