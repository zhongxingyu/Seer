 package com.teamluper.luper;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 import com.teamluper.luper.rest.LuperRestClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.security.MessageDigest;
 
 @EFragment
 public class TabRegisterFragment extends Fragment {
 
   @RestService
   LuperRestClient rest;
 
   @Override
   public View onCreateView(LayoutInflater infl, ViewGroup vg, Bundle state) {
     if(vg == null) return null;
     View v = infl.inflate(R.layout.tab_register_layout, vg, false);
 
     v.findViewById(R.id.register_button).setOnClickListener(
       new View.OnClickListener() {
         @Override
         public void onClick(View view) {
           attemptRegistration();
         }
       });
 
     return v;
   }
 
   @UiThread
   public void alert(String title, String message) {
     DialogFactory.alert(getActivity(), title, message);
   }
 
   @UiThread
   public void attemptRegistration() {
     Activity a = getActivity();
     EditText editEmail     = (EditText) a.findViewById(R.id.register_email);
     EditText editPassword  = (EditText) a.findViewById(R.id.register_password);
     EditText editPassword2 = (EditText) a.findViewById(R.id.register_password2);
     EditText editUsername  = (EditText) a.findViewById(R.id.register_username);
 
     editEmail.setError(null);
     editPassword.setError(null);
     editPassword2.setError(null);
     editUsername.setError(null);
 
     String email = editEmail.getText().toString();
     String password = editPassword.getText().toString();
     String password2 = editPassword2.getText().toString();
     String username = editUsername.getText().toString();
 
     boolean cancel = false;
     View focusView = null;
 
     // Check that the passwords match and are valid.
     if(!password.equals(password2)) {
       editPassword.setError("Passwords do not match!");
       focusView = editPassword;
       cancel = true;
     } else if(password.length() < 4) {
       editPassword.setError("Password must be greater than 4 characters.");
       focusView = editPassword;
       cancel = true;
     }
 
     // Check that the email is valid.
     if(TextUtils.isEmpty(email)) {
       editEmail.setError("A valid email address is required!");
       focusView = editEmail;
       cancel = true;
     } else if(!email.contains("@")) {
       editEmail.setError("This email address is invalid!");
       focusView = editEmail;
       cancel = true;
     }
 
     // Check that the username is specified
     if(TextUtils.isEmpty(username) || username.length() < 4) {
       editUsername.setError("A display name of at least 4 characters is required.");
       focusView = editUsername;
       cancel = true;
     }
 
     if(cancel) {
       focusView.requestFocus();
       return;
     }
 
     completeRegistration(email, password, username);
   }
 
   @Background
   public void completeRegistration(String email, String password, String username) {
     JSONObject request = new JSONObject();
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
       md.update(password.getBytes("UTF-8")); // Change this to "UTF-16" if needed
       byte[] digest = md.digest();
       String passwordHash = bytesToHex(digest);
       request.put("passwordHash",passwordHash);
       request.put("username",username);
       request.put("email",email);
     } catch(Exception e) {
       Log.e("luper","=== Exception while trying to hash password for JSON request ===", e);
     }
     String requestJSON = request.toString();
     showProgress(true);
 
     String responseJSON = rest.registerNewAccount(requestJSON);
 
     JSONObject response = null;
     try {
       response = new JSONObject(responseJSON);
     } catch(JSONException e) {
       Log.e("luper","=== FAILED TO PARSE RESPONSE JSON FROM /api/auth/register", e);
       Log.e("luper","=== RESPONSE (which failed to parse as JSON) WAS:");
       Log.e("luper","=== "+responseJSON);
     }
     showProgress(false);
     try {
       if(isResponseSuccessful(response)) {
         registrationSuccess(response.getLong("insertId"));
       } else {
         registrationFailure(response.getString("message"));
       }
     } catch(JSONException e) {
       Log.e("luper", "=== POST-PARSE JSON EXCEPTION: ", e);
     }
   }
 
   @UiThread
   public void registrationSuccess(long userid) {
     DialogFactory.alert(getActivity(), "Registration Complete!",
       "Your new account has been registered!  Please use the login form.");
     // TODO automatically switch to login form, or just automatically log in.
   }
 
   @UiThread
   public void registrationFailure(String errorMessage) {
     DialogFactory.alert(getActivity(), "Registration Failed!", errorMessage);
   }
 
   public boolean isResponseSuccessful(JSONObject response) {
     try {
       return response.getBoolean("success");
     } catch(Exception e) {
       Log.e("luper","=== A REGISTRATION API RESPONSE WAS UNSUCCESSFUL === Exception: ", e);
       return false;
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
 
   /**
    * Shows the progress UI and hides the register form.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
   @UiThread
   public void showProgress(final boolean show) {
     Activity a = getActivity();
     final View mRegisterFormView = a.findViewById(R.id.register_form);
     final View mRegisterStatusView = a.findViewById(R.id.register_status);
     final TextView mRegisterStatusMessageView = (TextView) a.findViewById(R.id.register_status_message);
 
     // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
     // for very easy animations. If available, use these APIs to fade-in
     // the progress spinner.
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
       int shortAnimTime = getResources().getInteger(
         android.R.integer.config_shortAnimTime);
 
       mRegisterStatusView.setVisibility(View.VISIBLE);
       mRegisterStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
         .setListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
             mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
           }
         });
 
       mRegisterFormView.setVisibility(View.VISIBLE);
       mRegisterFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
         .setListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
             mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
           }
         });
     } else {
       // The ViewPropertyAnimator APIs are not available, so simply show
       // and hide the relevant UI components.
       mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
       mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
     }
   }
 }
