 package de.fhb.mi.paperfly.user;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.auth.AuthStatus;
 import de.fhb.mi.paperfly.dto.RegisterAccountDTO;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 
 /**
  * This activity is for registration
  *
  * @author Andy Klay  klay@fh-brandenburg.de
  */
 public class UserRegisterActivity extends Activity {
 
 
     public static final String TAG = UserRegisterActivity.class.getSimpleName();
     private UserRegisterTask mRegisterTask = null;
 
     private String mEmail;
     private String mPassword;
     private String mPasswordRepeat;
     private String mFirstname;
     private String mLastname;
     private String mUsername;
 
     // UI references.
     private EditText mEmailView;
     private EditText mPasswordView;
     private EditText mUsernameView;
     private EditText mFirstnameView;
     private EditText mPasswordRepeatView;
     private EditText mLastnameView;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.user_register, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();
         if (id == R.id.action_settings) {
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_user_register);
 
         mEmailView = (EditText) findViewById(R.id.accountMail);
         mUsernameView = (EditText) findViewById(R.id.accountUserName);
         mFirstnameView = (EditText) findViewById(R.id.accountFirstName);
         mLastnameView = (EditText) findViewById(R.id.accountLastName);
         mPasswordView = (EditText) findViewById(R.id.password);
         mPasswordRepeatView = (EditText) findViewById(R.id.password_repeat);
     }
 
     /**
      * Attempt to login or register an user depending on which button was clicked.
      *
      * @param v the view which was clicked
      */
     public void register(View v) {
         Log.d(TAG, "attemptLoginRegister: " + ((Button) v).getText());
 
         // Store values at the time of the register action
         mEmail = mEmailView.getText().toString();
         mPassword = mPasswordView.getText().toString();
         mPasswordRepeat = mPasswordRepeatView.getText().toString();
         mFirstname = mFirstnameView.getText().toString();
         mLastname = mLastnameView.getText().toString();
         mUsername = mUsernameView.getText().toString();
 
         if (checkValues()) {
             mRegisterTask = new UserRegisterTask();
             mRegisterTask.execute(mEmail, mPassword, mPasswordRepeat, mFirstname, mLastname, mUsername);
         }
     }
 
    /**
     * finish this actitvity
     * @param v - View was clicked
     */
    public void backToActivityBefore(View v) {
        setResult(1);
        finish();
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
 
         // Check for a valid password.
         if (TextUtils.isEmpty(mPasswordRepeat)) {
             mPasswordRepeatView.setError(getString(R.string.error_field_required));
             focusView = mPasswordRepeatView;
             cancel = true;
         } else if (mPasswordRepeatView.length() < 4) {
             mPasswordRepeatView.setError(getString(R.string.error_invalid_password));
             focusView = mPasswordRepeatView;
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
 
         // Check for a valid username
         if (TextUtils.isEmpty(mUsername)) {
             mUsernameView.setError(getString(R.string.error_field_required));
             focusView = mUsernameView;
             cancel = true;
         } else if (mUsernameView.length() < 4) {
             mUsernameView.setError(getString(R.string.error_invalid_password));
             focusView = mUsernameView;
             cancel = true;
         }
 
         if (cancel) {
            // There was an error and focus the first
            // form field with an error
             focusView.requestFocus();
             return false;
         } else {
             return true;
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
             String pwRpt = params[2];
             String firstname = params[3];
             String lastname = params[4];
             String username = params[5];
 
             RegisterAccountDTO nextUser = new RegisterAccountDTO();
             nextUser.setLastName(lastname);
             nextUser.setFirstName(firstname);
             nextUser.setUsername(username);
             nextUser.setLastModified(new Date(System.currentTimeMillis()));
             nextUser.setCreated(new Date(System.currentTimeMillis()));
             nextUser.setEmail(mail);
             nextUser.setPassword(pw);
             nextUser.setPasswordRpt(pwRpt);
             nextUser.setEnabled(true);
 
             try {
                 RestConsumerSingleton.getInstance().register(nextUser);
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             } catch (RestConsumerException e) {
                 e.printStackTrace();
                 return AuthStatus.REGISTER_EMAIL_ALREADY_REGISTERED;
             }
 
             return AuthStatus.REGISTER_SUCCESSFUL;
         }
 
         @Override
         protected void onCancelled() {
             mRegisterTask = null;
         }
 
         @Override
         protected void onPostExecute(final AuthStatus authStatus) {
             mRegisterTask = null;
 
             switch (authStatus) {
                 case REGISTER_EMAIL_ALREADY_REGISTERED:
                     Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_info_error_email), Toast.LENGTH_LONG).show();
                     break;
                 case REGISTER_SUCCESSFUL:
                     Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_info_success), Toast.LENGTH_LONG).show();
                     break;
             }
         }
     }
 }
