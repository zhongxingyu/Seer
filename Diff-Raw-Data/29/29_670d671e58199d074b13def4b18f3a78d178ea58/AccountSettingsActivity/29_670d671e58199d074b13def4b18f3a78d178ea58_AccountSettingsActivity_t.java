 package realtalk.activities;
 
 import realtalk.controller.ChatController;
 import realtalk.util.ChatManager;
 import realtalk.util.RequestResultSet;
 import realtalk.util.UserInfo;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.realtalk.R;
 
 /**
  * Activity for Account Settings
  * 
  * @author Brandon
  *
  */
 public class AccountSettingsActivity extends Activity {
 	private static final String DEFAULT_ID = "someID";
 	private static final int MAX_PASSWORD_LENGTH = 20;
 	private ProgressDialog progressdialog;
     private SharedPreferences sharedpreferencesLoginPrefs;
     private Editor editorLoginPrefs;
 
 	/**
 	 * Sets up activity
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_account_settings);
 		sharedpreferencesLoginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
 		editorLoginPrefs = sharedpreferencesLoginPrefs.edit();
 		
 		String stUser = sharedpreferencesLoginPrefs.getString("loggedin_username", null);
 		TextView textviewUserTitle = (TextView) findViewById(R.id.userTitle);
 		textviewUserTitle.setText(stUser);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.account_settings, menu);
 		return true;
 	}
 	
 	/**
 	 * Method called when user submits to change his/her password
 	 * 
 	 * @param view
 	 */
 	public void changePword(View view) {
 		EditText edittextOldPword = (EditText) findViewById(R.id.oldpword);
 		EditText edittextNewPword = (EditText) findViewById(R.id.newpword);
 		EditText edittextConfPword = (EditText) findViewById(R.id.confpword);
 		String stOldPword = edittextOldPword.getText().toString();
 		String stNewPword = edittextNewPword.getText().toString();
 		String stConfPword = edittextConfPword.getText().toString();
 
 		//check to see if the old password is correct
 
 		//if any fields are blank, dialog box pops up
		if(stNewPword.trim().equals("") || stOldPword.trim().equals("") || stConfPword.trim().equals("")) {
 			AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this);
 			//set title
 			alertdialogbuilder.setTitle("Invalid fields");
 
 			//set dialog message
 			alertdialogbuilder
 			.setMessage("Please fill in all of the fields.")
 			.setCancelable(false)
 			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//close the dialog box if this button is clicked
 					dialog.cancel();
 				}	
 			});
 
 			//create alert dialog
 			AlertDialog alertdialogEmptyField = alertdialogbuilder.create();
 
 			//show alert dialog
 			alertdialogEmptyField.show();
 		} else if(!stNewPword.equals(stConfPword)) {
 			AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this);
 			//set title
 			alertdialogbuilder.setTitle("Invalid input");
 
 			//set dialog message
 			alertdialogbuilder
 			.setMessage("New Password and Confirmation Password do not match.  Please try again.")
 			.setCancelable(false)
 			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//close the dialog box if this button is clicked
 					dialog.cancel();
 				}	
 			});
 
 			//create alert dialog
 			AlertDialog alertdialogEmptyField = alertdialogbuilder.create();
 
 			//show alert dialog
 			alertdialogEmptyField.show();
 		} else if(stNewPword.length() > MAX_PASSWORD_LENGTH) {
 			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 			//set title
 			alertDialogBuilder.setTitle("Invalid Password");
 
 			//set dialog message
 			alertDialogBuilder
 			.setMessage("Password must not exceed 20 characters.  Please try again.")
 			.setCancelable(false)
 			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//close the dialog box if this button is clicked
 					dialog.cancel();
 				}	
 			});
 
 			//create alert dialog
 			AlertDialog alertdialogBadPword = alertDialogBuilder.create();
 
 			//show alert dialog
 			alertdialogBadPword.show();
 		} else {
 			String stUsername = sharedpreferencesLoginPrefs.getString("loggedin_username", null);
 			new PwordChanger(new UserInfo(stUsername, stOldPword, DEFAULT_ID), this, stNewPword).execute();
 		}
 
 	}
 	
 	
 	/**
 	 * Method called when user wants to delete account
 	 * 
 	 * @param view
 	 */
 	public void deleteAccount(View view) {
 		//confirmation pop up
     	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		//set title
 		alertDialogBuilder.setTitle("Delete account");
 		
 		//set dialog message
 		alertDialogBuilder
 			.setMessage("Are you sure you want to delete your account?")
 			.setCancelable(false);
 		
 		alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				String stUsername = sharedpreferencesLoginPrefs.getString("loggedin_username", null);
 				String stPassword = sharedpreferencesLoginPrefs.getString("loggedin_password", null);
 			    new UserRemover(new UserInfo(stUsername, stPassword, DEFAULT_ID), AccountSettingsActivity.this).execute();
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
 
 	
 	/**
 	 * Changes a user's password
 	 * 
 	 * @author Brandon
 	 *
 	 */
 	class PwordChanger extends AsyncTask<String, String, RequestResultSet> {
 		private UserInfo userinfo;
 		private Activity activity;
 		private String stNewPword;
 		
 		/**
 		 * Constructs a PwordChanger object
 		 * 
 		 * @param user	the user to add
 		 * @param activity	the activity context
 		 */
 		public PwordChanger(UserInfo user, Activity activity, String stNewPword) {
 			this.userinfo = user;
 			this.activity = activity;
 			this.stNewPword = stNewPword;
 		}
 		
 		/**
 		 * Displays a popup dialogue while changing the user's password
 		 */
 	    @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressdialog = new ProgressDialog(AccountSettingsActivity.this);
             progressdialog.setMessage("Loading user details. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
         }
 	    
 	    /**
 	     * Changes the user's password in database
 	     */
         @Override
         protected RequestResultSet doInBackground(String... params) {
             RequestResultSet requestresultset = ChatManager.rrsChangePassword(userinfo, stNewPword);
             if (requestresultset.getfSucceeded()) {
                 ChatController.getInstance().uninitialize();
                 UserInfo updatedUserinfo = new UserInfo(userinfo.stUserName(), userinfo.stPassword(), stNewPword);
                 ChatController.getInstance().fInitialize(updatedUserinfo);
             }
             return requestresultset;
         }
         
         /**
          * Closes the dialogue, and lets the user know if they have input
          * invalid fields
          */
         @Override
         protected void onPostExecute(RequestResultSet requestresultset) {
             progressdialog.dismiss();
             if(!requestresultset.getfSucceeded()) {
             	//invalid password pop up
             	AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertdialogbuilder.setTitle("Invalid fields");
 				
 				//set dialog message
 				alertdialogbuilder
 					.setMessage("Old password incorrect.  Please try again.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							dialog.cancel();
 						}	
 				});
 				
 				//create alert dialog
 				AlertDialog alertdialogBadUname = alertdialogbuilder.create();
 				
 				//show alert dialog
 				alertdialogBadUname.show();	
             } else {
             	editorLoginPrefs.putString("loggedin_password", stNewPword);
             	editorLoginPrefs.commit();
             	
             	//password change successful pop up
             	AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertdialogbuilder.setTitle("Password Changed");
 				
 				//set dialog message
 				alertdialogbuilder
 					.setMessage("Password changed.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							TextView textviewNewPword = (TextView) findViewById(R.id.newpword);
 				            textviewNewPword.setText("");
 				            TextView textviewConfPword = (TextView) findViewById(R.id.confpword);
 				            textviewConfPword.setText("");
 				            TextView textviewOldPword = (TextView) findViewById(R.id.oldpword);
 				            textviewOldPword.setText("");
 							dialog.cancel();
 						}	
 				});
 				
 				//create alert dialog
 				AlertDialog alertdialogBadUname = alertdialogbuilder.create();
 				
 				//show alert dialog
 				alertdialogBadUname.show();	
             }
         }
 	}
 	
 	
 	/**
 	 * Removes a user from the database 
 	 * 
 	 * @author Brandon
 	 *
 	 */
 	class UserRemover extends AsyncTask<String, String, RequestResultSet> {
 		private UserInfo userinfo;
 		private Activity activity;
 		
 		/**
 		 * Constructs a UserRemover object
 		 * 
 		 * @param userinfo the user to remove
 		 * @param activity the activity context
 		 */
 		public UserRemover(UserInfo userinfo, Activity activity) {
 			this.userinfo = userinfo;
 			this.activity = activity;
 		}
 		
 		/**
 		 * Displays a popup dialog while account is being removed
 		 */
 	    @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressdialog = new ProgressDialog(AccountSettingsActivity.this);
             progressdialog.setMessage("Loading user details. Please wait...");
             progressdialog.setIndeterminate(false);
             progressdialog.setCancelable(true);
             progressdialog.show();
         }
 	    
 	    /**
 	     * Removes account from the database
 	     */
         @Override
         protected RequestResultSet doInBackground(String... params) {
         	return ChatManager.rrsRemoveUser(userinfo);
         }
         
         /**
          * Prompts the user that their account has been deleted and redirects them to login page
          */
         @Override
         protected void onPostExecute(RequestResultSet requestresultset) {
         	progressdialog.dismiss();
         	if(requestresultset.getfSucceeded()) {
 	        	editorLoginPrefs.clear();
 	        	editorLoginPrefs.commit();
 	        	
 	            progressdialog.dismiss();
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
 							
							Intent itLogin = new Intent(activity, LoginActivity.class);
							itLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
							itLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 							ChatController.getInstance().uninitialize();
							activity.startActivity(itLogin);
 							activity.finish();
 						}	
 				});
 					
 				//create alert dialog
 				AlertDialog alertdialogAccDeleted = alertDialogBuilder.create();
 					
 				//show alert dialog
 				alertdialogAccDeleted.show();	
         	} else {
         		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
 				//set title
 				alertDialogBuilder.setTitle("Account not deleted");
 				
 				//set dialog message
 				alertDialogBuilder
 					.setMessage("Error.  Your account has not been deleted.")
 					.setCancelable(false)
 					.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							//close the dialog box if this button is clicked
 							dialog.cancel();
 						}	
 				});
 					
 				//create alert dialog
 				AlertDialog alertdialogAccNotDeleted = alertDialogBuilder.create();
 					
 				//show alert dialog
 				alertdialogAccNotDeleted.show();
         	}
 			
         }
 	}
 }
 
 
