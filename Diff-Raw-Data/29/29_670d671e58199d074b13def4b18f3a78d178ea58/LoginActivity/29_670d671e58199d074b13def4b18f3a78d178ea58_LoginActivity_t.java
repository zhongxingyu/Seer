 package realtalk.activities;
 import com.google.android.gcm.GCMRegistrar;
 
 import realtalk.controller.ChatController;
 import realtalk.util.ChatManager;
 import realtalk.util.RequestResultSet;
 import realtalk.util.UserInfo;
 import realtalk.util.gcm.GCMUtilities;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.realtalk.R;
 /**
  * Activity for login page. This page also sets up the device for GCM.
  * 
  * @author Brandon Lee & Colin Kho
  *
  */
 public class LoginActivity extends Activity {
     
 	private String stRegisteredId = "";
     private ProgressDialog progressdialog;
     private CheckBox checkboxRememberMe;
     private SharedPreferences sharedpreferencesLoginPrefs;
     private Editor editorLoginPrefs;
     private Boolean fRememberMe;
     private EditText edittextUser;
     private EditText edittextPword;
     private boolean fLoggedIn;
     
     
     /**
      * Sets up activity
      */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		// Make sure the device has the proper dependencies and manifest is properly set
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		setContentView(R.layout.activity_main);
 		
 		// Get the Registration ID
 		final String stRegId = GCMRegistrar.getRegistrationId(this);
 		registerReceiver(handleRegisterReceiver,
 		        new IntentFilter(GCMUtilities.GCM_REGISTER_RESULT_ACTION));
 		if (stRegId.equals("")) {
 		    // Register device for the first time.
 		    // Wait for registration result to complete.
 		    progressdialog = new ProgressDialog(LoginActivity.this);
             progressdialog.setMessage("Initializing RealTalk resources for the first time. Please wait.....");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
             
             // Register Device
             GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
 		} else {
 		    // Already registered on GCM server
 		    stRegisteredId = stRegId;
 		}
 		
 		// For remembering username/password and if user is already logged in
 		edittextUser = (EditText) findViewById(R.id.editQuery);
 		edittextPword = (EditText) findViewById(R.id.editPword);
 		checkboxRememberMe = (CheckBox)findViewById(R.id.rememberme );
 		sharedpreferencesLoginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
 		editorLoginPrefs = sharedpreferencesLoginPrefs.edit();
 		
 		//Uncomment line below if you need to clear preferences and start at the login page again
 		
 //		editorLoginPrefs.clear();
 //		editorLoginPrefs.commit();
 		
 		fLoggedIn = sharedpreferencesLoginPrefs.getBoolean("loggedIn", false);
 		String stUsername = sharedpreferencesLoginPrefs.getString("loggedin_username", null);
 		String stPassword = sharedpreferencesLoginPrefs.getString("loggedin_password", null);
 		
 		//if user is already logged in, redirect to select room page
 		if(fLoggedIn) {
 			UserInfo userinfo = new UserInfo(stUsername, stPassword, stRegisteredId);
 			updateRegId(userinfo);
 		} else {			
 			fRememberMe = sharedpreferencesLoginPrefs.getBoolean("saveLogin", false);
 			
 			if(fRememberMe) {	
 				//loading saved username/password if setting is on
 				edittextUser.setText(sharedpreferencesLoginPrefs.getString("savedUsername", null));
 				edittextPword.setText(sharedpreferencesLoginPrefs.getString("savedPassword", null));
 				checkboxRememberMe.setChecked(true);
 			}
 		}
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onDestroy() {
 	    super.onDestroy();
 	    unregisterReceiver(handleRegisterReceiver);
 	}
 	
 	/**
 	 * Called when user clicks "Create Account".  Redirects to Create Account page.
 	 * 
 	 * @param view
 	 */
 	public void addUser(View view) {
 		Intent itCreateAcc = new Intent(this, CreateAccountActivity.class);
 		this.startActivity(itCreateAcc);
 	}
 	
 	/** 
 	 * Called when user attempts to login.
 	 * 
 	 * @param view
 	 */
 	public void authenticateUser(View view) {
 	    String stUsername = edittextUser.getText().toString().trim();
 	    String stPword = edittextPword.getText().toString();
 	    
	    if(stUsername.trim().equals("") || stPword.trim().equals("")) {	    	
 	    	AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this);
 			//set title
 			alertdialogbuilder.setTitle("Invalid input");
 			
 			//set dialog message
 			alertdialogbuilder
 				.setMessage("Please enter a username & password.")
 				.setCancelable(false);
 			
 			alertdialogbuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 					dialog.cancel();
 				}
 			});
 			
 			//create alert dialog
 			AlertDialog alertdialogEmptyFields = alertdialogbuilder.create();
 			
 			//show alert dialog
 			alertdialogEmptyFields.show();	
 	    } else {
 	    	if(checkboxRememberMe.isChecked()) {
 	    		//stores login info if "Remember Me" checkbox is checked
 	    		editorLoginPrefs.putBoolean("saveLogin", true);
 	    		editorLoginPrefs.putString("savedUsername", stUsername);
 	    		editorLoginPrefs.putString("savedPassword", stPword);
 	    		editorLoginPrefs.commit();
 	    	} else {
 	    		//clears existing login info if "Remember Me" checkbox is not checked
 	    		editorLoginPrefs.putBoolean("saveLogin", false);
 	    		editorLoginPrefs.putString("savedUsername", null);
 	    		editorLoginPrefs.putString("savedPassword", null);
 	    		editorLoginPrefs.commit();
 	    	}
 	    	new Authenticator(new UserInfo(stUsername, stPword, stRegisteredId), this).execute();
 	    }
 	    
 	}
 	
 	/**
 	 * Called when user attempts to delete their account.
 	 * 
 	 * @param view
 	 */
 	public void removeUser(View view) {
 		EditText edittextUser = (EditText) findViewById(R.id.editQuery);
 	    EditText edittextPword = (EditText) findViewById(R.id.editPword);
 	    String stUsername = edittextUser.getText().toString();
 	    String stPword = edittextPword.getText().toString();
		if(stUsername.trim().equals("") || stPword.trim().equals("")) {
 			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 			//set title
 			alertDialogBuilder.setTitle("Invalid input");
 			
 			//set dialog message
 			alertDialogBuilder
 				.setMessage("Please enter a username & password.")
 				.setCancelable(false);
 			
 			
 			alertDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 					dialog.cancel();
 				}
 			});
 			
 			//create alert dialog
 			AlertDialog alertdialogEmptyFields = alertDialogBuilder.create();
 			
 			//show alert dialog
 			alertdialogEmptyFields.show();
 		} else {
 			//confirmation pop up
 	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 			//set title
 			alertDialogBuilder.setTitle("Delete account");
 			
 			//set dialog message
 			alertDialogBuilder
 				.setMessage("Are you sure you want to delete this account?")
 				.setCancelable(false);
 			
 			alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//close the dialog box if this button is clicked
 					EditText edittextUser = (EditText) findViewById(R.id.editQuery);
 				    EditText edittextPword = (EditText) findViewById(R.id.editPword);
 				    String stUsername = edittextUser.getText().toString();
 				    String stPword = edittextPword.getText().toString();
 				    new UserRemover(new UserInfo(stUsername, stPword, stRegisteredId), LoginActivity.this).execute();
 				}	
 			});
 			
 			alertDialogBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 					dialog.cancel();
 				}
 			});
 			
 			//create alert dialog
 			AlertDialog alertdialogDeleteAcc = alertDialogBuilder.create();
 			
 			//show alert dialog
 			alertdialogDeleteAcc.show();	
 		}
 	}
 	
 	/**
 	 * Method that starts an async task used to update the registration Id.
 	 * 
 	 * @param userinfo
 	 */
 	public void updateRegId(UserInfo userinfo) {
	    new UpdateRegId(userinfo, this).execute();
 	}
 	
 	/**
 	 * Receiver that handles a registration intent from GCM.
 	 */
 	private final BroadcastReceiver handleRegisterReceiver =
 	        new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     String stResultCode = intent.getExtras().getString(GCMUtilities.RESULT_MESSAGE);
                     CharSequence charsequenceText = "";
                     if (stResultCode.equals(GCMUtilities.SUCCESS)) {
                         stRegisteredId = intent.getExtras().getString(GCMUtilities.GCM_REG_ID);
                         charsequenceText = "Initialization Complete";
                     } else {
                         // Registration failed. Alert user and lock app asking them to try again later.
                         Button btnLogin = (Button) findViewById(R.id.loginButton);
                         btnLogin.setEnabled(false);
                         Button btnCreate = (Button) findViewById(R.id.createAccountBtn);
                         btnCreate.setEnabled(false);
                         charsequenceText = "Server is down. Please try again later";
                     }
                     Toast toastRegistration = Toast.makeText(context, charsequenceText, Toast.LENGTH_SHORT);
                     toastRegistration.show();
                     progressdialog.dismiss();
                 }	    
 	};
 	
 	/**
 	 * Updates the user to have the correct registration ID each time he/she logs in
 	 * 
 	 * @author Colin Kho
 	 *
 	 */
 	class UpdateRegId extends AsyncTask<String, String, RequestResultSet> {
 	    private UserInfo userinfo;
 	    private Activity activity;
 	    
 	    public UpdateRegId(UserInfo userinfo, Activity activity) {
 	        this.userinfo = userinfo;
 	        this.activity = activity;
 	    }
 	    
 	    @Override
 	    protected void onPreExecute() {
 	        super.onPreExecute();
 	        progressdialog = new ProgressDialog(LoginActivity.this);
             progressdialog.setMessage("Updating Server. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
 	    }
 	    
         @Override
         protected RequestResultSet doInBackground(String... params) {
             UserInfo loginUserinfo = new UserInfo(userinfo.stUserName(), userinfo.stPassword(), stRegisteredId);
             ChatController.getInstance().fInitialize(loginUserinfo);
             return ChatManager.rrsChangeID(userinfo, stRegisteredId);
         }
 	    
         @Override
         protected void onPostExecute(RequestResultSet requestresultset) {
             progressdialog.dismiss();
             if (requestresultset.getfSucceeded()) {                
                 Intent itRoomSelect = new Intent(activity, SelectRoomActivity.class);
                 UserInfo loginUserinfo = new UserInfo(userinfo.stUserName(), userinfo.stPassword(), stRegisteredId);
                 itRoomSelect.putExtra("USER", loginUserinfo);
                 activity.startActivity(itRoomSelect);
                 activity.finish();
             } else {
                 Toast serverToast = Toast.makeText(activity, "Server is down. Please try again later.", Toast.LENGTH_LONG);
                 serverToast.show();
             }
         }
 	}
 	
 	/**
 	 * Async task to remove user from server.
 	 * 
 	 * @author Brandon
 	 *
 	 */
 	class UserRemover extends AsyncTask<String, String, RequestResultSet> {
 		private UserInfo userinfo;
 		private Activity activity;
 		public UserRemover(UserInfo userinfo, Activity activity) {
 			this.userinfo = userinfo;
 			this.activity = activity;
 		}
 		
 	    @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressdialog = new ProgressDialog(LoginActivity.this);
             progressdialog.setMessage("Loading user details. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
         }
 	    
         @Override
         protected RequestResultSet doInBackground(String... params) {
         	return ChatManager.rrsRemoveUser(userinfo);
         }
         
         @Override
         protected void onPostExecute(RequestResultSet requestresultset) {
             progressdialog.dismiss();
             if(!requestresultset.getfSucceeded()) {
             	//invalid username or password
             	
             	AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertdialogbuilder.setTitle("Invalid fields");
 				
 				//set dialog message
 				alertdialogbuilder
 					.setMessage("Invalid username/password. Please try again.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							dialog.cancel();
 						}	
 				});
 				
 				//create alert dialog
 				AlertDialog alertdialogBadPword = alertdialogbuilder.create();
 				
 				//show alert dialog
 				alertdialogBadPword.show();	
             } else {
             	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertDialogBuilder.setTitle("Account deleted");
 				
 				//set dialog message
 				alertDialogBuilder
 					.setMessage("Your account has been deleted.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							dialog.cancel();
 						}	
 				});
 				
 				//create alert dialog
 				AlertDialog alertdialogAccDeleted = alertDialogBuilder.create();
 				
 				//show alert dialog
 				alertdialogAccDeleted.show();	
 				
 				TextView textviewUname = (TextView) findViewById(R.id.editQuery);
 	            textviewUname.setText("");
             }
             
             TextView textviewPword = (TextView) findViewById(R.id.editPword);
             textviewPword.setText("");
         }
 	}
 	
 	/**
 	 * Authenticates a user in the database 
 	 * 
 	 * @author Brandon
 	 *
 	 */
 	class Authenticator extends AsyncTask<String, String, RequestResultSet> {
 		private UserInfo userinfo;
 		private Activity activity;
 		
 		/**
 		 * Constructs an Authenticator object
 		 * 
 		 * @param userinfo user to be authenticated
 		 * @param activity the activity context
 		 */
 		public Authenticator(UserInfo userinfo, Activity activity) {
 			this.userinfo = userinfo;
 			this.activity = activity;
 		}
 		
 		/**
 		 * Pop up dialog while user is being authenticated 
 		 * 
 		 */
 	    @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressdialog = new ProgressDialog(LoginActivity.this);
             progressdialog.setMessage("Loading user details. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
         }
 	    
 	    /**
 	     * Authenticates user in the database
 	     */
         @Override
         protected RequestResultSet doInBackground(String... params) {
         	return ChatManager.rrsAuthenticateUser(userinfo);
         }
         
         /**
          * Prompts user if their username/password were incorrect.  Otherwise, redirects
          * to the Select Rooms activity.
          */
         @Override
         protected void onPostExecute(RequestResultSet requestresultset) {
 
             progressdialog.dismiss();
             //invalid username/password
             if(!requestresultset.getfSucceeded()) {
             	AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertdialogbuilder.setTitle("Invalid fields");
 				
 				//set dialog message
 				alertdialogbuilder
 					.setMessage("Invalid username/password. Please try again.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							dialog.cancel();
 						}	
 				});
 				
 				//create alert dialog
 				AlertDialog alertdialogBadPword = alertdialogbuilder.create();
 				
 				//show alert dialog
 				alertdialogBadPword.show();	
 				
 				TextView textviewPword = (TextView) findViewById(R.id.editPword);
 	            textviewPword.setText("");
             } else {           	
                 String username = userinfo.stUserName();
                 String password = userinfo.stPassword();
         		
         		//for when we have a logout button, so that pressing back on the rooms page 
         		//doesn't take you back to the login screen, but rather exits the app.
         		editorLoginPrefs.putBoolean("loggedIn", true);
 	    		editorLoginPrefs.putString("loggedin_username", username);
 	    		editorLoginPrefs.putString("loggedin_password", password);
 	    		editorLoginPrefs.commit();
 	    		
 	    		LoginActivity.this.updateRegId(new UserInfo(username, password, stRegisteredId));  
             }
         }
 	}
 }
