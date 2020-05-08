 package com.pandj.wewrite;
 
 import edu.umich.imlc.collabrify.client.CollabrifyClient;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
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
 public class GetEmailAndDisplayName extends Activity
 {
   /**
    * A dummy authentication store containing known user names and passwords.
    * TODO: remove after connecting to a real authentication system.
    */
   private static final String[] DUMMY_CREDENTIALS = new String[]{
       "foo@example.com:hello", "bar@example.com:world" };
 
   /**
    * The default email to populate the email field with.
    */
   public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
   /**
    * Keep track of the login task to ensure we can cancel it if requested.
    */
   // Values for email and password at the time of the login attempt.
   private String mEmail;
   private String mUserName;
 
   // UI references.
   private EditText mEmailView;
   private EditText mUsernameView;
   private View mLoginFormView;
   private View mLoginStatusView;
   private TextView mLoginStatusMessageView;
 
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.activity_get_email_and_display_name);
 
     // Set up the login form.
     mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
     mEmailView = (EditText) findViewById(R.id.email);
     mEmailView.setText(mEmail);
 
     mUsernameView = (EditText) findViewById(R.id.password);
     mLoginFormView = findViewById(R.id.login_form);
     mLoginStatusView = findViewById(R.id.login_status);
     mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
     findViewById(R.id.sign_in_button).setOnClickListener(
         new View.OnClickListener()
         {
           @Override
           public void onClick(View view)
           {
             attemptLogin();
           }
         });
   }
 
   @Override
   public void onBackPressed()
   {
     //Do nothing, avoiding the hairy situations that could arise
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
     super.onCreateOptionsMenu(menu);
     getMenuInflater().inflate(R.menu.get_email_and_display_name, menu);
     return true;
   }
 
   /**
    * Attempts to sign in or register the account specified by the login form. If
    * there are form errors (invalid email, missing fields, etc.), the errors are
    * presented and no actual login attempt is made.
    */
   public void attemptLogin()
   {
 
     // Reset errors.
     mEmailView.setError(null);
     mUsernameView.setError(null);
 
     // Store values at the time of the login attempt.
     mEmail = mEmailView.getText().toString();
     mUserName = mUsernameView.getText().toString();
 
     boolean cancel = false;
     View focusView = null;
 
     // Check for a valid password.
     if( TextUtils.isEmpty(mUserName) )
     {
       mUsernameView.setError(getString(R.string.error_field_required));
       focusView = mUsernameView;
       cancel = true;
     }
 
     // Check for a valid email address.
     if( TextUtils.isEmpty(mEmail) )
     {
       mEmailView.setError(getString(R.string.error_field_required));
       focusView = mEmailView;
       cancel = true;
     }
     else if( !mEmail.contains("@") )
     {
       mEmailView.setError(getString(R.string.error_invalid_email));
       focusView = mEmailView;
       cancel = true;
     }
 
     if( cancel )
     {
       // There was an error; don't attempt login and focus the first
       // form field with an error.
       focusView.requestFocus();
     }
     else
     {
       // Show a progress spinner, and kick off a background task to
       // perform the user login attempt.
       
       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("email", mEmail);
       editor.putString("username", mUserName);
       editor.commit();
       
       mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
      super.onBackPressed();
     }
   }
 }
