 package de.fhb.mi.paperfly.auth;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 import de.fhb.mi.paperfly.MainActivity;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.dto.RegisterAccountDTO;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerService;
 import de.fhb.mi.paperfly.service.RestConsumerService.RestConsumerBinder;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity {
 
     /**
      * The default email to populate the email field with.
      */
     public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
     public static final String LOGIN_SUCCESFUL = "LOGIN_SUCCESFUL";
     private static final String TAG = "LoginActivity";
     /**
      * Keep track of the login task to ensure we can cancel it if requested.
      */
     private UserLoginTask mLoginTask = null;
     private UserRegisterTask mRegisterTask = null;
     // Values for email and password at the time of the login attempt.
     private String mEmail;
     private String mPassword;
     // UI references.
     private EditText mEmailView;
     private EditText mPasswordView;
     private View mLoginFormView;
     private View mLoginStatusView;
     private TextView mLoginStatusMessageView;
     private boolean mBound = false;
     private RestConsumerService mRestConsumerService;
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             RestConsumerBinder binder = (RestConsumerBinder) service;
             mRestConsumerService = binder.getServerInstance();
             mBound = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };
 
 
     /**
      * Attempt to login or register an user depending on which button was clicked.
      *
      * @param v the view which was clicked
      */
     public void attemptLoginRegister(View v) {
         Log.d(TAG, "attemptLoginRegister: " + ((Button) v).getText());
         if (v.getId() == R.id.login_button && mLoginTask != null) {
             return;
         }
         if (v.getId() == R.id.register_button && mRegisterTask != null) {
             return;
         }
 
         // Reset errors.
         mEmailView.setError(null);
         mPasswordView.setError(null);
 
         // Store values at the time of the login attempt.
         mEmail = mEmailView.getText().toString();
         mPassword = mPasswordView.getText().toString();
 
         if (checkValues()) {
             // Show a progress spinner, and kick off a background task to
             // perform the user login attempt.
             mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
             showProgress(true);
             if (v.getId() == R.id.login_button) {
                 mLoginTask = new UserLoginTask();
                 mLoginTask.execute(mEmail, mPassword);
             }
             if (v.getId() == R.id.register_button) {
                 mRegisterTask = new UserRegisterTask();
                 mRegisterTask.execute(mEmail, mPassword);
             }
         }
     }
 
     /**
      * Checks if the values in the form are valid.
      *
      * @return true if the values are valid, false if not
      */
     private boolean checkValues() {
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
             return false;
         } else {
             return true;
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_login);
 
         // Set up the login form.
         mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
         mEmailView = (EditText) findViewById(R.id.email);
         mEmailView.setText(mEmail);
 
         mPasswordView = (EditText) findViewById(R.id.password);
 
         mLoginFormView = findViewById(R.id.login_form);
         mLoginStatusView = findViewById(R.id.login_status);
         mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 //        getMenuInflater().inflate(R.menu.login, menu);
         return true;
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Intent serviceIntent = new Intent(this, RestConsumerService.class);
         bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
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
             int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             mLoginStatusView.setVisibility(View.VISIBLE);
             mLoginStatusView.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 1 : 0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                         }
                     });
 
             mLoginFormView.setVisibility(View.VISIBLE);
             mLoginFormView.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 0 : 1)
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
 
     /**
      * Represents an asynchronous login task used to authenticate
      * the user.
      */
     public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... params) {
             String mail = params[0];
             String pw = params[1];
 
             boolean success=false;
 
             try {
                 success= mRestConsumerService.login(mail, pw);
             } catch (RestConsumerException e) {
                 e.printStackTrace();
             }
             return success;
         }
 
         @Override
         protected void onCancelled() {
             mLoginTask = null;
             showProgress(false);
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mLoginTask = null;
             showProgress(false);
 
             if (success) {
                 Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                 intent.putExtra(LOGIN_SUCCESFUL, true);
                 startActivity(intent);
                 finish();
             } else {
                 mPasswordView.setError(getString(R.string.error_incorrect_password));
                 mPasswordView.requestFocus();
             }
         }
     }
 
     /**
      * Represents an asynchronous registration task used to authenticate the user.
      */
     public class UserRegisterTask extends AsyncTask<String, Void, AuthStatus> {
         @Override
         protected AuthStatus doInBackground(String... params) {
             String mail = params[0];
             String pw = params[1];
 
             RegisterAccountDTO nextUser= new RegisterAccountDTO();
             nextUser.setLastName("Mustermann");
             nextUser.setUsername("neuerUser");
             nextUser.setLastModified(new Date(System.currentTimeMillis()));
             nextUser.setCreated(new Date(System.currentTimeMillis()));
             nextUser.setEmail(mail);
             nextUser.setPassword(pw);
             nextUser.setPasswordRpt(pw);
             nextUser.setEnabled(true);
 
 
             try {
                 mRestConsumerService.register(nextUser);
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             } catch (RestConsumerException e) {
                 e.printStackTrace();
                 //TODO kann man nicht immer sagen an der Stelle
                 return AuthStatus.REGISTER_EMAIL_ALREADY_REGISTERED;
             }
 
             return AuthStatus.REGISTER_SUCCESSFUL;
         }
 
         @Override
         protected void onCancelled() {
             mRegisterTask = null;
             showProgress(false);
         }
 
         @Override
         protected void onPostExecute(final AuthStatus authStatus) {
             mRegisterTask = null;
             showProgress(false);
 
             switch (authStatus) {
                 case REGISTER_EMAIL_ALREADY_REGISTERED:
                     Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.register_info_error_email), Toast.LENGTH_LONG).show();
                     break;
                 case REGISTER_SUCCESSFUL:
                     ((TextView) findViewById(R.id.register_info)).setText(R.string.register_info_success);
                     break;
             }
         }
     }
 }
