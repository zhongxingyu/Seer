 package com.allplayers.android;
 
 import com.allplayers.rest.RestApiV1;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.accounts.AccountManager;
 import android.accounts.Account;
 
 import org.jasypt.util.text.BasicTextEncryptor;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Initial activity to handle login.
  *
  * TODO: Replace with AccountManager, loading only as required when an account
  * is needed.
  */
 public class Login extends Activity {
     EditText usernameEditText;
     EditText passwordEditText;
     TextView passwordLabel;
     TextView usernameLabel;
     Button button;
     static ProgressBar progressSpinner;
     AccountManager manager;
     private Context context;
    
    @Override  
    public void onBackPressed() {
    	
    	System.out.println("Pressed the back button");
        super.onBackPressed();
        // Do extra stuff here
    }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 
         // TODO - Temporarily disable StrictMode because all networking is
         // currently in the UI thread. Android now throws exceptions when
         // obvious IO happens in the UI thread, which is a good thing.
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         context = this.getBaseContext();
         manager = AccountManager.get(context);
         button = (Button)findViewById(R.id.loginButton);
         usernameEditText = (EditText)findViewById(R.id.usernameField);
         passwordEditText = (EditText)findViewById(R.id.passwordField);
         passwordLabel = (TextView)findViewById(R.id.passwordLabel);
         usernameLabel = (TextView)findViewById(R.id.usernameLabel);
         progressSpinner = (ProgressBar) findViewById(R.id.ctrlActivityIndicator);
 
         Account[] accounts = manager.getAccountsByType("com.allplayers.android");
         // There should only be one allplayers type account in the device at once.
         if (accounts.length == 1) {
             String storedEmail = accounts[0].name;
             String storedPassword = manager.getPassword(accounts[0]);
             String storedSecretKey = LocalStorage.readSecretKey(context);
 
             if (storedSecretKey == null || storedSecretKey.equals("")) {
                 LocalStorage.writeSecretKey(context);
                 storedSecretKey = LocalStorage.readSecretKey(context);
             }
 
             if (storedEmail != null && !storedEmail.equals("") && storedPassword != null && !storedPassword.equals("")) {
                 BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                 textEncryptor.setPassword(storedSecretKey);
                 String unencryptedPassword = textEncryptor.decrypt(storedPassword);
 
                 progressSpinner.setVisibility(View.VISIBLE);
                 AttemptLoginTask helper = new AttemptLoginTask();
                 helper.execute(storedEmail, unencryptedPassword);
             }
         } else {
             button.setVisibility(View.VISIBLE);
             usernameEditText.setVisibility(View.VISIBLE);
             passwordEditText.setVisibility(View.VISIBLE);
             passwordLabel.setVisibility(View.VISIBLE);
             usernameLabel.setVisibility(View.VISIBLE);
         }
 
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 String email = usernameEditText.getText().toString();
                 String password = passwordEditText.getText().toString();
 
                 progressSpinner.setVisibility(View.VISIBLE);
                 AttemptLoginTask helper = new AttemptLoginTask();
                 helper.execute(email, password);
                
             }
                      
         });
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {
             startActivity(new Intent(Login.this, FindGroupsActivity.class));
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
         }
         return super.onKeyUp(keyCode, event);
     }
 
     /**
      * Attempt a login, if successful, move to the real main activity.
      */
     public class AttemptLoginTask extends AsyncTask<String, Void, Boolean> {
 
         /**
          * @return
          *  0 - Was not able to log in successfully.
          *  1 - Was able to log a user back in.
          *  2 - Was able to log a user in
          */
         protected Boolean doInBackground(String... strings) {
 
             String email = strings[0];
             String pass = strings[1];
 
             RestApiV1 client = new RestApiV1();
             try {
                 if (client.isLoggedIn()) {
                     Intent intent = new Intent(Login.this, MainScreen.class);
                     startActivity(intent);
                     finish();
                     return true;
                 }
 
 
                 String result = client.validateLogin(email, pass);
                 JSONObject jsonResult = new JSONObject(result);
                 client.setCurrentUserUUID(jsonResult.getJSONObject("user").getString("uuid"));
 
                 // If we get to this point, then we encrypt their password and add a new account.
                 String key = LocalStorage.readSecretKey(context);
                 if (key == null || key.equals("")) {
                     LocalStorage.writeSecretKey(context);
                     key = LocalStorage.readSecretKey(context);
                 }
 
                 BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                 textEncryptor.setPassword(key);
                 String encryptedPassword = textEncryptor.encrypt(pass);
 
                 Account account = new Account(email, "com.allplayers.android");
                 manager.addAccountExplicitly(account, encryptedPassword, null);
 
                 Intent intent = new Intent(Login.this, MainScreen.class);
                 startActivity(intent);
                 finish();
                 return true;
             } catch (JSONException ex) {
                 System.err.println("Login/user_id/" + ex);
                 return false;
             }
         }
 
         protected void onPostExecute(Boolean ex) {
 
             if (!ex) {
                 Toast invalidLogin = Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG);
                 invalidLogin.show();
                 Login.progressSpinner.setVisibility(View.INVISIBLE);
             }
         }
     }
 }
