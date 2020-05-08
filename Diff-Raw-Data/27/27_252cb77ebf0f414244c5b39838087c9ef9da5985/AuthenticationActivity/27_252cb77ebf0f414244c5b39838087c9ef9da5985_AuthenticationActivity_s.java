 package epfl.sweng.authentication;
 
 import epfl.sweng.R;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity to allow users to authenticate against the Tequila and SWENG servers
  */
 public class AuthenticationActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_authentication);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_authentication, menu);
         return true;
     }
     
     
     
 	/**
 	 * Display an error in case the authentication failed and clears the EditTexts
 	 */
     public void onAuthError() {
     	Toast.makeText(this, getString(R.string.auth_error_text), Toast.LENGTH_LONG).show();
     	TextView usernameText = (TextView) findViewById(R.id.auth_login);
 		TextView passwordText = (TextView) findViewById(R.id.auth_pass);
 		usernameText.setText("");
 		passwordText.setText("");
 		usernameText.requestFocus();
     }
 	/**
 	 * Display a confirmation when the authentication was successful
 	 */    
     public void onAuthSuccess() {
     	setResult(RESULT_OK);
     	Toast.makeText(this, getString(R.string.auth_success_text), Toast.LENGTH_LONG).show();
 		finish();
 	}
     
     /**
      * Method starting the authentication process
      */
     public void doAuthentication(View view) {
     	final TextView usernameText = (TextView) findViewById(R.id.auth_login);
 		final TextView passwordText = (TextView) findViewById(R.id.auth_pass);
 		
 		SessionManager.getInstance().authenticate(new ISessionCreationCallback() {
 
 			@Override
			public void onAuthSuccess() {
 				onAuthSuccess();
 			}
 
 			@Override
			public void onAuthError() {
 				onAuthError();
 			}
     		
     	}, usernameText.getText().toString(), passwordText.getText().toString());
     }
 
 }
