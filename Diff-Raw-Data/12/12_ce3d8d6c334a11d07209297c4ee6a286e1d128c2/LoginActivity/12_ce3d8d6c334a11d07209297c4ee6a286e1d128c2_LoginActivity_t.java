 
 package eu.nerdz.app.authenticator;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import eu.nerdz.api.ContentException;
 import eu.nerdz.api.LoginException;
 import eu.nerdz.app.messenger.Messaging;
 import eu.nerdz.app.messenger.R;
 import eu.nerdz.app.messenger.UserInfo;
 import eu.nerdz.app.messenger.activities.PopupActivity;
 
 public class LoginActivity extends PopupActivity {
 
     public static final String PARAM_AUTHTOKEN_TYPE = "nerdzU";
     public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
     public static final String PARAM_PASSWORD = "password";
     public static final String PARAM_USERNAME = "username";
     private static final String TAG = "NdzLoginAct";
     private AccountManager mAccountManager;
     private UserLoginTask mAuthTask = null;
     private View mLoginFormView;
     private TextView mLoginStatusMessageView;
     private View mLoginStatusView;
     private String mPassword;
     private EditText mPasswordView;
     private String mUsername;
     private EditText mUsernameView;
 
     protected void onCreate(Bundle savedInstanceState) {
 
         Log.i(TAG, "onCreate(" + savedInstanceState + ")");
 
         super.onCreate(savedInstanceState);
 
         this.mAccountManager = AccountManager.get(this);
 
         this.setContentView(R.layout.activity_login);
 
         this.mUsernameView = ((EditText) findViewById(R.id.username));
         this.mUsernameView.setText(this.mUsername);
 
         this.mPasswordView = ((EditText) findViewById(R.id.password));
         this.mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
             @Override
             public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent) {
 
                 if ((paramAnonymousInt == R.id.login) || (paramAnonymousInt == EditorInfo.IME_NULL)) {
                     LoginActivity.this.attemptLogin();
                     return true;
                 }
                 return false;
             }
         });
         TextWatcher textWatcher = new TextWatcher() {
 
             @Override
             public void afterTextChanged(Editable editable) {
 
                 LoginActivity.this.mPasswordView.setError(null);
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {}
             
         };
         this.mPasswordView.addTextChangedListener(textWatcher);
         this.mUsernameView.addTextChangedListener(textWatcher);
 
         this.mLoginFormView = findViewById(R.id.login_form);
         this.mLoginStatusView = findViewById(R.id.login_status);
         this.mLoginStatusMessageView = ((TextView) findViewById(R.id.login_status_message));
         this.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View paramAnonymousView) {
 
                 LoginActivity.this.attemptLogin();
             }
         });
     }
 
     /**
      * Shows the progress UI and hides the login form.
      */
    @SuppressLint("Override")
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     private void showProgress(final boolean show) {
 
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             this.mLoginStatusView.setVisibility(View.VISIBLE);
             this.mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
 
                @SuppressLint("Override")
                 public void onAnimationEnd(Animator animation) {
 
                     LoginActivity.this.mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                 }
             });
 
             this.mLoginFormView.setVisibility(View.VISIBLE);
             this.mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
 
                @SuppressLint("Override")
                 public void onAnimationEnd(Animator animation) {
 
                     LoginActivity.this.mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                 }
             });
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
             this.mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
             this.mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
         }
     }
 
     public void attemptLogin() {
 
         Log.i(TAG, "attemptLogin()");
 
         // Hide input method from screen.
         ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
 
         if (this.mAuthTask != null)
             return;
 
         this.mUsernameView.setError(null);
         this.mPasswordView.setError(null);
 
         this.mUsername = this.mUsernameView.getText().toString();
         this.mPassword = this.mPasswordView.getText().toString();
 
         View focusView = null;
         boolean cancel = false;
 
         if (TextUtils.isEmpty(this.mPassword)) {
             this.mPasswordView.setError(this.getString(R.string.error_field_required));
             focusView = this.mPasswordView;
             cancel = true;
         } else if (this.mPassword.length() < 4) {
             this.mPasswordView.setError(getString(R.string.error_invalid_password));
             focusView = this.mPasswordView;
             cancel = true;
         }
 
         if (TextUtils.isEmpty(this.mUsername)) {
             this.mUsernameView.setError(this.getString(R.string.error_field_required));
             focusView = this.mUsernameView;
             cancel = true;
         }
 
         if (cancel)
             focusView.requestFocus();
         else {
 
             this.mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
             this.showProgress(true);
             this.mAuthTask = new UserLoginTask();
 
             String[] userData = new String[] {this.mUsername, this.mPassword};
 
             this.mAuthTask.execute(userData);
         }
     }
 
     public class UserLoginTask extends AsyncTask<String, Void, Throwable> {
 
         @Override
         protected Throwable doInBackground(String... params) {
 
             Log.i(TAG, "logging in...");
             try {
                 Messaging.get().initWithLoginData(Messaging.doLogin(LoginActivity.this.mUsername, LoginActivity.this.mPassword));
                 return null;
             } catch (Throwable t) {
                 return t;
             }
         }
 
         @Override
         protected void onCancelled() {
 
             LoginActivity.this.mAuthTask = null;
             LoginActivity.this.showProgress(false);
         }
 
         @Override
         protected void onPostExecute(Throwable throwable) {
 
             Log.i(TAG, "onPostExecute()");
 
             LoginActivity.this.showProgress(false);
             LoginActivity.this.mAuthTask = null;
 
             if (throwable != null) {
                 if ((throwable instanceof LoginException)) {
                     LoginActivity.this.mPasswordView.setError(LoginActivity.this.getString(R.string.error_invalid_login));
                     LoginActivity.this.mPasswordView.requestFocus();
                     return;
                 }
                 LoginActivity.this.popUp("OH NOES EXCEPTIONZ!!!!1111!\n" + throwable.getClass() + ": " + throwable.getLocalizedMessage());
                 return;
             }
             
             Log.d(TAG, "No exception raised . Going ahead...");
             
             UserInfo userInfo = Messaging.get().getUserInfo();
             String userName = userInfo.getUsername();
             Integer userID = Integer.valueOf(userInfo.getUserID());
             Account account = new Account(userName, LoginActivity.this.getString(R.string.account_type));
             
             try {
                 Bundle userData = new Bundle();
                 userData.putString(LoginActivity.this.getString(R.string.data_nerdzid), userID.toString());
                 userData.putString(LoginActivity.this.getString(R.string.data_nerdzu), userInfo.getToken());
                 boolean created = LoginActivity.this.mAccountManager.addAccountExplicitly(account, userInfo.getToken(), userData);
                 Bundle extras = LoginActivity.this.getIntent().getExtras();
                 
                 if (extras != null) {
                     Log.d(TAG, "account" + userName + "created: " + created);
                     
                     if (created) {
                         
                         AccountAuthenticatorResponse response = (AccountAuthenticatorResponse) extras.getParcelable("accountAuthenticatorResponse");
                         
                         Bundle result = new Bundle();
                         result.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, LoginActivity.this.getString(R.string.account_type));
                         result.putString(LoginActivity.this.getString(R.string.data_nerdzid), userID.toString());
                         result.putString(LoginActivity.this.getString(R.string.data_nerdzu), userInfo.getToken());
                         response.onResult(result);
                         
                         Log.d(TAG, "showing a Toast...");
                         
                         String msg = String.format(LoginActivity.this.getString(R.string.login_successful), userName);
                         LoginActivity.this.shortToast(msg);
                     }
                 }
                 
                 Log.d(TAG, "onPostExecute() has finished correctly.");
                 LoginActivity.this.finish();
                 return;
                 
             } catch (ContentException e) {
                     LoginActivity.this.popUp("OH NOES EXCEPTIONZ!!!!1111!\n" + e.getLocalizedMessage());
             }
         }
     }
 }
 
