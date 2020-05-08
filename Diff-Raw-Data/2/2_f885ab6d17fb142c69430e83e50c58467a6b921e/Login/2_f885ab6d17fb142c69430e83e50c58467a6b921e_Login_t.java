 package com.allplayers.android;
  
 import com.allplayers.rest.RestApiV1;
  
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
  
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
  
     private Context context;
     
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
  
         String storedEmail = LocalStorage.readUserName(context);
         String storedPassword = LocalStorage.readPassword(context);
         String storedSecretKey = LocalStorage.readSecretKey(context);
  
         if (storedSecretKey == null || storedSecretKey.equals("")) {
             LocalStorage.writeSecretKey(context);
             storedSecretKey = LocalStorage.readSecretKey(context);
         }
  
         if (storedEmail != null && !storedEmail.equals("") && storedPassword != null && !storedPassword.equals("")) {
             BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
             textEncryptor.setPassword(storedSecretKey);
             String unencryptedPassword = textEncryptor.decrypt(storedPassword);
             
             AttemptLoginTask login = new AttemptLoginTask();
             login.execute(storedEmail, unencryptedPassword);
         }
  
         final Button button = (Button)findViewById(R.id.loginButton);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 EditText usernameEditText = (EditText)findViewById(R.id.usernameField);
                 EditText passwordEditText = (EditText)findViewById(R.id.passwordField);
  
                 String email= usernameEditText.getText().toString();
                 String password = passwordEditText.getText().toString();;
  
                 BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                 textEncryptor.setPassword(LocalStorage.readSecretKey(context));
                 String encryptedPassword = textEncryptor.encrypt(password);
  
                 LocalStorage.writeUserName(context, email);
                 LocalStorage.writePassword(context, encryptedPassword);
  
                 AttemptLoginTask login = new AttemptLoginTask();
                 login.execute(email, password);
             }
         });
     }
  
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {
             startActivity(new Intent(Login.this, FindGroupsActivity.class));
         }
         return super.onKeyUp(keyCode, event);
     }
 
     /**
      * Attempt a login, if successful, move to the real main activity.
      */ 
     public class AttemptLoginTask extends AsyncTask<String, Void, Boolean> {
  
        protected Boolean doInBackground(String... strings) {
             RestApiV1 client = new RestApiV1();
 			try {
 	            String result = client.validateLogin(strings[0], strings[1]);
 	            JSONObject jsonResult = new JSONObject(result);
 	            client.setCurrentUserUUID(jsonResult.getJSONObject("user").getString("uuid"));
 	
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
 			if(!ex) {
 				Toast invalidLogin = Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG);
 				invalidLogin.show();
 			}
     	}
     }
 }
