 package net.meneame.fisgodroid;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity
 {
     // Shared preferences settings for remember username/password feature.
     private static final String PREFS_NAME = "FisgoDroid";
     private static final String PREF_USERNAME = "username";
     private static final String PREF_PASSWORD = "password";
     
     /**
      * Keep track of the login task to ensure we can cancel it if requested.
      */
     private UserLoginTask mAuthTask = null;
 
     // Values for email and password at the time of the login attempt.
     private String mUsername;
     private String mPassword;
     private boolean mRememberMe;
 
     // UI references.
     private EditText mUsernameView;
     private EditText mPasswordView;
     private CheckBox mRememberMeCheckbox;
     private View mLoginFormView;
     private View mLoginStatusView;
     private TextView mLoginStatusMessageView;
 
     // Reference to the service binder
     private FisgoService.FisgoBinder mFisgoBinder = null;
     private ServiceConnection mServiceConn = new ServiceConnection()
     {
         @Override
         public void onServiceConnected(ComponentName arg0, IBinder binder)
         {
             mFisgoBinder = (FisgoService.FisgoBinder) binder;
             if (mFisgoBinder.isLoggedIn() == true)
             {
                 startActivity(new Intent(LoginActivity.this, ChatActivity.class));
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0)
         {
         }
     };
 
     @Override
     protected void onResume()
     {
         super.onResume();
        
        // Start and bind the chat service
        Intent intent = new Intent(this, FisgoService.class);
        startService(intent);
        bindService(intent, mServiceConn, BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop()
     {
         super.onStop();
         unbindService(mServiceConn);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_login);
 
         // Set up the login form.
         mUsernameView = (EditText) findViewById(R.id.chat_username);
         mPasswordView = (EditText) findViewById(R.id.password);
         mRememberMeCheckbox = (CheckBox)findViewById(R.id.checkbox_remember_me);
         
         mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
         {
             @Override
             public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
             {
                 if (id == R.id.login || id == EditorInfo.IME_NULL)
                 {
                     attemptLogin();
                     return true;
                 }
                 return false;
             }
         });
         
         
         // Restore remembered username / password
         SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);   
         String username = pref.getString(PREF_USERNAME, null);
         String password = pref.getString(PREF_PASSWORD, null);
         if ( username != null && password != null )
         {
             mUsernameView.setText(username);
             mPasswordView.setText(password);
             mRememberMeCheckbox.setChecked(true);
         }
 
 
         mLoginFormView = findViewById(R.id.login_form);
         mLoginStatusView = findViewById(R.id.login_status);
         mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
         findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View view)
             {
                 attemptLogin();
             }
         });
         
         
         // Handle clicks on the "Remember me" checkbox to
         // forget credentials when disabling it.
         mRememberMeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
                 if ( !isChecked )
                     rememberCredentials(null, null);
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.login, menu);
         return true;
     }
 
     /**
      * Attempts to sign in or register the account specified by the login form.
      * If there are form errors (invalid email, missing fields, etc.), the
      * errors are presented and no actual login attempt is made.
      */
     public void attemptLogin()
     {
         if (mAuthTask != null)
         {
             return;
         }
 
         // Hide the soft keyboard
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);
         imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
 
         // Reset errors.
         mUsernameView.setError(null);
         mPasswordView.setError(null);
 
         // Store values at the time of the login attempt.
         mUsername = mUsernameView.getText().toString();
         mPassword = mPasswordView.getText().toString();
         mRememberMe = mRememberMeCheckbox.isChecked();
         
         boolean cancel = false;
         View focusView = null;
 
         // Check for a valid password.
         if (TextUtils.isEmpty(mPassword))
         {
             mPasswordView.setError(getString(R.string.error_field_required));
             focusView = mPasswordView;
             cancel = true;
         }
 
         // Check for a valid email address.
         if (TextUtils.isEmpty(mUsername))
         {
             mUsernameView.setError(getString(R.string.error_field_required));
             focusView = mUsernameView;
             cancel = true;
         }
 
         if (cancel)
         {
             // There was an error; don't attempt login and focus the first
             // form field with an error.
             focusView.requestFocus();
         } else
         {
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
     private void showProgress(final boolean show)
     {
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
         {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             mLoginStatusView.setVisibility(View.VISIBLE);
             mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
             {
                 @Override
                 public void onAnimationEnd(Animator animation)
                 {
                     mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                 }
             });
 
             mLoginFormView.setVisibility(View.VISIBLE);
             mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
             {
                 @Override
                 public void onAnimationEnd(Animator animation)
                 {
                     mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                 }
             });
         } else
         {
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
     public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
     {
         @Override
         protected Boolean doInBackground(Void... params)
         {
             if (mFisgoBinder == null)
                 return false;
 
             return mFisgoBinder.logIn(mUsername, mPassword);
         }
 
         @Override
         protected void onPostExecute(final Boolean success)
         {
             mAuthTask = null;
             showProgress(false);
 
             if (success)
             {
                 if ( mRememberMe )
                 {
                     rememberCredentials(mUsername, mPassword);
                 }
                 startActivity(new Intent(LoginActivity.this, ChatActivity.class));
                 finish();
             } else
             {
                 mPasswordView.setError(getString(R.string.error_incorrect_password));
                 mPasswordView.requestFocus();
             }
         }
 
         @Override
         protected void onCancelled()
         {
             mAuthTask = null;
             showProgress(false);
         }
     }
     
     private void rememberCredentials ( String username, String password )
     {
         getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
         .edit()
         .putString(PREF_USERNAME, username)
         .putString(PREF_PASSWORD, password)
         .commit();
     }
 }
