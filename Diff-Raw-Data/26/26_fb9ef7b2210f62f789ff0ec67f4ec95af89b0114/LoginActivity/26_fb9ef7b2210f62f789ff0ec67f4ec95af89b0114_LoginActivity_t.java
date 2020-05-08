 package de.fhb.mi.paperfly.auth;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import de.fhb.mi.paperfly.MainActivity;
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.TokenDTO;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import de.fhb.mi.paperfly.user.UserRegisterActivity;
 import de.fhb.mi.paperfly.util.ValidateUtil;
 
 /**
  * Activity which displays a login screen to the user
  */
 public class LoginActivity extends Activity {
 
     public static final int REQUESTCODE_REGISTER_USER = 101;
 
     /**
      * The default email to populate the email field with.
      */
     public static final String LOGIN_SUCCESFUL = "LOGIN_SUCCESFUL";
     public static final String ARGS_REGISTER_EMAIL = "ARGS_REGISTER_EMAIL";
     public static final String PREFS_EMAIL = "email";
     private static final String TAG = LoginActivity.class.getSimpleName();
     /**
      * Keep track of the login task to ensure we can cancel it if requested.
      */
     private UserLoginTask mLoginTask = null;
 
     // Values for email and password at the time of the login attempt.
     private String mEmail;
     private String mPassword;
     // UI references.
     private EditText mEmailView;
     private EditText mPasswordView;
     private View mLoginFormView;
     private View mLoginStatusView;
     private TextView mLoginStatusMessageView;
 
     /**
      * Attempt to login an user
      *
      * @param v the view which was clicked
      */
     public void attemptLoginRegister(View v) {
         Log.d(TAG, "attemptLoginRegister: " + ((Button) v).getText());
         if (v.getId() == R.id.login_button && mLoginTask != null) {
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
         }
 
 
     }
 
     /**
      * Attempt to register an user
      *
      * @param v the view which was clicked
      */
     public void attemptRegister(View v) {
 
         if (v.getId() == R.id.register_button) {
 
             Intent intent = new Intent(LoginActivity.this, UserRegisterActivity.class);
             startActivityForResult(intent, REQUESTCODE_REGISTER_USER);
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
         } else if (!ValidateUtil.isValidEmailAddress(mEmail)) {
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
 
     private void loadSavedPreferences() {
         SharedPreferences sharedPreferences = PreferenceManager
                 .getDefaultSharedPreferences(this);
         String email = sharedPreferences.getString(PREFS_EMAIL, "mail@mail.de");
         mEmailView.setText(email);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUESTCODE_REGISTER_USER:
                if (resultCode == RESULT_OK && data.hasExtra(ARGS_REGISTER_EMAIL)) {
                     String email = data.getStringExtra(ARGS_REGISTER_EMAIL);
                     mEmailView.setText(email);
                     mPasswordView.setText("");
                 }
                 break;
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_login);
 
         // Set up the login form.
         mEmailView = (EditText) findViewById(R.id.accountMail);
         mPasswordView = (EditText) findViewById(R.id.password);
 
         mLoginFormView = findViewById(R.id.login_form);
         mLoginStatusView = findViewById(R.id.login_status);
         mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
         loadSavedPreferences();
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
     }
 
     private void savePreferences(String key, boolean value) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putBoolean(key, value);
         editor.commit();
     }
 
     private void savePreferences(String key, String value) {
         SharedPreferences sharedPreferences = PreferenceManager
                 .getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(key, value);
         editor.commit();
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
 
             TokenDTO tokenDTO;
 
             try {
                 tokenDTO = RestConsumerSingleton.getInstance().login(mail, pw);
                 if (tokenDTO != null) {
                     AccountDTO accountDTO = RestConsumerSingleton.getInstance().setMyAccountStatus(de.fhb.mi.paperfly.dto.Status.ONLINE);
                     ((PaperFlyApp) getApplication()).setAccount(accountDTO);
                     return true;
                 } else {
                     return false;
                 }
             } catch (RestConsumerException e) {
                 e.printStackTrace();
             }
             return false;
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
                 savePreferences(PREFS_EMAIL, mEmailView.getText().toString());
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
 
 
 }
