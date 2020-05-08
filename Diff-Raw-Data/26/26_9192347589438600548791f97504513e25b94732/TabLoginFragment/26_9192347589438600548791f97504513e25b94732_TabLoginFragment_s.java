 package com.teamluper.luper;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.UiLifecycleHelper;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.LoginButton;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 import com.teamluper.luper.rest.LuperRestClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.security.MessageDigest;
 import java.util.Arrays;
 
 @EFragment
 public class TabLoginFragment extends Fragment {
 
   // Values for email and password at the time of the login attempt.
   private String mEmail;
   private String mPassword;
 
   // UI references.
   private EditText mEmailView;
   private EditText mPasswordView;
   private View mLoginFormView;
   private View mLoginStatusView;
   private TextView mLoginStatusMessageView;
 
   private SQLiteDataSource dataSource;
 
   @RestService
   LuperRestClient rest;
 
   private static final int HASH_COUNT = 100;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      uiHelper = new UiLifecycleHelper(getActivity(), callback);
      uiHelper.onCreate(savedInstanceState);
   }
 
   @Override
   public void onResume() {
       super.onResume();
       // Gets called when login activity gets called and session
       // is not null
       Session session = Session.getActiveSession();
       if (session != null &&
              (session.isOpened() || session.isClosed()) ) {
           onSessionStateChange(session, session.getState(), null);
       }
       uiHelper.onResume();
       showProgress(false);
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       uiHelper.onActivityResult(requestCode, resultCode, data);
   }
 
   @Override
   public void onPause() {
       super.onPause();
       uiHelper.onPause();
   }
 
   @Override
   public void onDestroy() {
       super.onDestroy();
       uiHelper.onDestroy();
   }
 
   @Override
   public void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);
       uiHelper.onSaveInstanceState(outState);
   }
 
   @Override
   public View onCreateView(LayoutInflater infl, ViewGroup vg, Bundle state) {
     if(vg == null) return null;
     View v = infl.inflate(R.layout.tab_login_layout, vg, false);
     Activity a = getActivity();
 
     // set up the SQLite database access.
     dataSource = ((LuperLoginActivity)a).getDataSource();
     if(!dataSource.isOpen()) dataSource.open();
 
     // Set up the login form.
     mEmail = a.getIntent().getStringExtra("luperPrefilledEmail");
     mEmailView = (EditText) v.findViewById(R.id.login_email);
     mEmailView.setText(mEmail);
 
     mPasswordView = (EditText) v.findViewById(R.id.login_password);
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
 
     // Give facebook loginbutton email permissions
     LoginButton authButton = (LoginButton) v.findViewById(R.id.authButton);
     authButton.setFragment(this);
     authButton.setReadPermissions(Arrays.asList("email"));
 
     mLoginFormView = v.findViewById(R.id.login_form);
     mLoginStatusView = v.findViewById(R.id.login_status);
     mLoginStatusMessageView = (TextView) v.findViewById(R.id.login_status_message);
 
     showProgress(false);
 
     v.findViewById(R.id.login_button).setOnClickListener(
       new View.OnClickListener() {
         @Override
         public void onClick(View view) {
           attemptLogin();
         }
       });
 
     return v;
   }
   //Updates the Luper interface once usr is logged into Facebook.
   private static final String TAG = "LoginFragment";
 
   private Session.StatusCallback callback = new Session.StatusCallback() {
 	  @Override
 	  public void call(Session sesh, SessionState seshState, Exception e) {
 		  onSessionStateChange(sesh,seshState,e);
 	  }
   };
 
   private UiLifecycleHelper uiHelper;
 
   protected void onSessionStateChange(Session sesh, SessionState seshState, Exception e) {
 	  final LuperLoginActivity_ a = (LuperLoginActivity_)getActivity();
 	  if (seshState.isOpened()) {
 		  /** usr logged in **/
 		  // Create new request to facebook API
 		  Request.executeMeRequestAsync(sesh, new Request.GraphUserCallback() {
 
 		    // callback after Graph API response with user object
 		    @Override
 		    public void onCompleted(GraphUser user, Response response) {
 		    	if (user != null) {
 		    		  //loadActiveSession(user);
 //		    		  TextView welcome = (TextView) findViewById(R.id.welcome);
 //		    		  welcome.setText("Hello " + user.getName() + "!");
 
 		    		  // Let's snag the user's email address to create a unique ID for Mike's DB
 		    		  // updates the header on the login tab
 
 		    		  // If email doesn't work, let's use the GraphUser's facebook link as unique ID
 		    		  // naturally each link is associated with at most one user
 		    		  TextView header_login = (TextView) a.findViewById(R.id.header_login);
 		    		  header_login.setText(user.getName() + ", "
 		    				  + user.getUsername() + ", "
 		    				  + user.getId() + ", "
 		    				  + user.getLink() + ", "
 		    				  + user.getFirstName()+ user.asMap().get("email"));
 		    		}
 		    	a.completeFacebookLogin(user.asMap().get("email").toString(),user.getUsername());
 		    	// this shouldn't need to be called. but for some reason LuperLogin isn't switching
 		    	// over to Main automatically.
 		    	//a.startMainActivity();
 		    }
 
 		  });
 	  } else if (seshState.isClosed()) {
 		  // usr logged out
 		  TextView header_login = (TextView) a.findViewById(R.id.header_login);
 		  header_login.setText("Come back soon!");
 	  } else {
 		  // probably shouldn't be here
 	  }
   }
 
 //  @Override
 //  public void onResume() {
 //    super.onResume();
 //    showProgress(false);
 //  }
 
   /**
    * Attempts to sign in or register the account specified by the login form. If
    * there are form errors (invalid email, missing fields, etc.), the errors are
    * presented and no actual login attempt is made.
    */
   @UiThread
   public void attemptLogin() {
     Activity a = getActivity();
     if(!LuperMainActivity.deviceIsOnline(a)) {
       DialogFactory.alert(a, "Login Failed!", "You are not connected to the internet! " +
         "You must be online to log in.  Once logged in, however, you can use Lüper while offline.");
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
       mLoginStatusMessageView.setText(R.string.progress_logging_in);
       showProgress(true);
       completeLoginAttempt(mEmail, mPassword);
     }
   }
 
   @Background
   public void completeLoginAttempt(String email, String password) {
     // first, fetch a challenge salt to hash the password with.
     try {
       JSONObject challengeRequest = new JSONObject();
       challengeRequest.put("email", email);
       String challengeResponseJSON = rest.getLoginChallengeSalt(challengeRequest.toString());
       JSONObject challengeResponse = new JSONObject(challengeResponseJSON);
       if(!isResponseSuccessful(challengeResponse)) {
         loginFailure(challengeResponse.getString("message"));
         return;
       }
       // now we take the challenge salt and compute the login attempt hash.
       String salt = challengeResponse.getString("salt");
       String attemptHash = saltyHashBrowns(sha256(password), salt, HASH_COUNT);
       // assemble the actual login attempt request
       JSONObject loginRequest = new JSONObject();
       loginRequest.put("email", email);
       loginRequest.put("attemptHash", attemptHash);
       String loginResponseJSON = rest.validateLoginAttempt(loginRequest.toString());
       JSONObject loginResponse = new JSONObject(loginResponseJSON);
       if(!isResponseSuccessful(loginResponse)) {
         loginFailure(loginResponse.getString("message"));
         return;
       }
       if(!loginResponse.getBoolean("isLoginValid")) {
         loginFailure("The email address or password you entered was invalid!");
         return;
       }
       User existingUser = dataSource.getUserByEmail(email);
       if(existingUser == null) {
         // we need to create a user first, matching the user from the server
         String userFromServerJSON = rest.fetchUserByEmail(email);
         JSONObject userFromServer = new JSONObject(userFromServerJSON);
 
         existingUser = dataSource.createUser(userFromServer.getLong("_id"),userFromServer.getString("username"),email);
       }
       dataSource.setActiveUser(existingUser);
       loginSuccess();
     } catch(JSONException e) {
       Log.e("luper","=== JSON Exception while attempting to validate login === Exception: ", e);
       loginFailure("Something went wrong while logging in!");
       return;
     }
   }
 
   @UiThread
   public void loginSuccess() {
     if(mEmailView != null) mEmailView.setText("");
     if(mPasswordView != null) mPasswordView.setText("");
     Intent intent = new Intent(getActivity(), LuperMainActivity_.class);
     startActivity(intent);
   }
 
   @UiThread
   public void loginFailure(String errorMessage) {
     showProgress(false);
     DialogFactory.alert(getActivity(), "Login Failed!", errorMessage);
   }
 
   public boolean isResponseSuccessful(JSONObject response) {
     try {
       return response.getBoolean("success");
     } catch(Exception e) {
       Log.e("luper","=== A REGISTRATION API RESPONSE WAS UNSUCCESSFUL === Exception: ", e);
       return false;
     }
   }
 
   private String sha256(String password) {
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
       md.update(password.getBytes("UTF-8")); // Change this to "UTF-16" if needed
       byte[] digest = md.digest();
       return bytesToHex(digest);
     } catch(Exception e) {
       Log.e("luper", "=== ERROR COMPUTING SHA256 LOGIN ATTEMPT PASSWORD HASH === Exception: ", e);
       return "";
     }
   }
 
   public static String bytesToHex(byte[] bytes) {
     final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
     char[] hexChars = new char[bytes.length * 2];
     int v;
     for ( int j = 0; j < bytes.length; j++ ) {
       v = bytes[j] & 0xFF;
       hexChars[j * 2] = hexArray[v >>> 4];
       hexChars[j * 2 + 1] = hexArray[v & 0x0F];
     }
     return new String(hexChars);
   }
 
   public String saltyHashBrowns(String simpleHashedPassword, String salt, int hashCount) {
     // Prefix the password with the salt
     String hash = salt + simpleHashedPassword;
     // Hashing our hashes times a hundred thousand, for security!
     for(int i=0; i<hashCount; i++) {
       hash = sha256(hash); // hashity hash
     }
     return hash;
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
       mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
         .setListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
             mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
           }
         });
 
       mLoginFormView.setVisibility(View.VISIBLE);
       mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
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
 }
