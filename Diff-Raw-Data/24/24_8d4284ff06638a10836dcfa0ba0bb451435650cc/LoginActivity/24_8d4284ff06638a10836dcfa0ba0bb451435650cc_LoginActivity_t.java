 package scrum.support;
 
 import scrum.support.model.User;
import scrum.support.services.ContentProvider;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * The login activity which is the first activity seen by 
  * users who are not logged in automatically.
  * 
  * @author Dave W
  *
  */
 public class LoginActivity extends Activity {
 	
 	private Button loginButton;
 	private Button registerButton;
 	private ProgressDialog pd;
 	private Activity activity;
 	
     /** Called when the activity is first created. 
      * Is the first (main) activity
      * */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	activity = this;
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main); 
 
 		loginButton = (Button)findViewById(R.id.loginButton);
 		loginButton.setOnClickListener(new OnClickListener(){  
             public void onClick(View v) { 
             	authUser(false);
             }  
         });
 		
 		registerButton = (Button) findViewById(R.id.registerButton);
 		registerButton.setOnClickListener(new OnClickListener(){  
             public void onClick(View v) { 
             	authUser(true);
             }  
         });
     }
 	
     /**
      * Pull the user fields out of the UI and attempt to authenticate them
      * @param registerUser
      */
 	private void authUser(boolean registerUser) {
 		String username = ((EditText) findViewById(R.id.usernameField)).toString();
     	String password = ((EditText) findViewById(R.id.passwordField)).toString();
     	new AuthenicateUser().execute(new User(username, password, true));
 	}
 	
 	/**
 	 * A worker thread to manage the authenticate and network activity
 	 *
 	 */
 	private class AuthenicateUser extends AsyncTask<User, Integer, Boolean> {
 		
 		@Override
 		protected void onPreExecute() {
 	    	 pd = ProgressDialog.show(activity, "Authenicating..", "Authenicating...", true, false);			
 		}
 
 		@Override
 		protected Boolean doInBackground(User... params) {
 			if(params.length == 1) {
				return ContentProvider.getInstance().validateUser(params[0]);
 			}
 			return false;
 		}
 
 		@Override
 	     protected void onPostExecute(Boolean result) {
 	    	 pd.dismiss();
 	    	 if(result) {
 	    		activity.startActivity(new Intent(activity, ProjectActivity.class));
 	    	 } else {
 	    		 
     	 		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
 				alertDialog.setTitle("Authenicaton");
 				alertDialog.setMessage("Authenicated Failed.");
 				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						alertDialog.dismiss();
 					}
 				});
     	 		alertDialog.show();
 	    	 }
 	     }
 	 }
 }
