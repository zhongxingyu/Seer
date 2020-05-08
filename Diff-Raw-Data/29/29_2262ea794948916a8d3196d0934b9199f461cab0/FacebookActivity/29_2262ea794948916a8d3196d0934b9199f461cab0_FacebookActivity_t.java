 package com.barview;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.barview.constants.BarviewConstants;
 import com.barview.mobile.BarviewMobileLoginTask;
 import com.barview.mobile.BarviewMobileUser;
 import com.barview.mobile.BarviewMobileUtility;
 import com.barview.rest.FavoritesListUpdater;
 import com.barview.utilities.FacebookUtility;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Facebook.DialogListener;
 
 public class FacebookActivity extends Activity {
 	
 	Facebook facebook = FacebookUtility.getFacebook();
 	
 	BarviewMobileUser barviewUser = BarviewMobileUtility.getUser();
 	
 	FacebookActivity activity = this;
 	
 	Button fbLoginButton;
 	Button bvLoginButton;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.facebook);
 		
 		/*
 		 * FACEBOOK LOGON BUTTON
 		 */
 		fbLoginButton = (Button) findViewById(R.id.fblogin);
 		fbLoginButton.setText( (facebook.isSessionValid()) ? R.string.fb_logout : R.string.fb_login );
 		fbLoginButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				/*
 				 * We're logged into Facebook, so this click will log us out
 				 * and change the button text back to "Log in".
 				 */
 				if(facebook.isSessionValid()) {
 					try {
 						facebook.logout(activity);
 						fbLoginButton.setText(R.string.fb_login);
 						bvLoginButton.setVisibility(View.VISIBLE);
 						
 						// Clear out preferences
 						SharedPreferences settings = getSharedPreferences(BarviewConstants.PREFS_NAME, Activity.MODE_PRIVATE);
 						SharedPreferences.Editor editor = settings.edit();
 						editor.remove(FacebookUtility.FB_ID)
 						.remove(BarviewConstants.LOGIN_TYPE)
 						.remove(FacebookUtility.FB_FIRST_NAME)
 						.remove(FacebookUtility.FB_LAST_NAME)
						.remove(FacebookUtility.FB_NAME)
						.remove(FacebookUtility.FB_ACCESS_TOKEN)
						.remove(FacebookUtility.FB_EXPIRES);
 						
 						editor.commit();
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				/*
 				 * We're currently logged out of Facebook, so this click will
 				 * bring up the dialog window to log us in and, if successful,
 				 * change the button text to "Log out".
 				 */
 				else {
 					facebook.authorize(activity, new DialogListener() {
 			            public void onComplete(Bundle values) {
 			            	Log.i(FacebookActivity.class.getName(), "Logged into Facebook and got user id of " + 
 			            			FacebookUtility.getAttribute(FacebookUtility.FB_ID));
 			            	fbLoginButton.setText(R.string.fb_logout);
 			            	bvLoginButton.setVisibility(View.INVISIBLE);
 			            	
 			            	// Set preferences
 			            	SharedPreferences settings = getSharedPreferences(BarviewConstants.PREFS_NAME, Activity.MODE_PRIVATE);
 							SharedPreferences.Editor editor = settings.edit();
 							editor.putString(BarviewConstants.LOGIN_TYPE, BarviewConstants.LOGIN_TYPE_FACEBOOK)
 							.putString(FacebookUtility.FB_ACCESS_TOKEN, facebook.getAccessToken())
 							.putLong(FacebookUtility.FB_EXPIRES, facebook.getAccessExpires())
 							.putString(FacebookUtility.FB_ID, FacebookUtility.getAttribute(FacebookUtility.FB_ID))
 							.putString(FacebookUtility.FB_NAME, FacebookUtility.getAttribute(FacebookUtility.FB_NAME))
 							.putString(FacebookUtility.FB_FIRST_NAME, FacebookUtility.getAttribute(FacebookUtility.FB_FIRST_NAME))
 							.putString(FacebookUtility.FB_LAST_NAME, FacebookUtility.getAttribute(FacebookUtility.FB_LAST_NAME));
 							
 							editor.commit();
 							
 							FavoritesListUpdater updater = new FavoritesListUpdater(activity);
 							updater.execute("");
 			            }
 	
 			            public void onFacebookError(FacebookError error) {
 			            	showFacebookErrorToast();
 			            	Log.e(FacebookActivity.class.getName(), error.getMessage());
 			            	error.printStackTrace();
 			            }
 	
 			            public void onError(DialogError e) {
 			            	showFacebookErrorToast();
 			            	Log.e(FacebookActivity.class.getName(), e.getMessage());
 			            	e.printStackTrace();
 			            }
 	
 			            public void onCancel() {
 			            	Log.i(FacebookActivity.class.getName(), "Authorization request was cancelled.");
 			            }
 					});
 				}
 			}
 		});
 		
 		/*
 		 * BARVIEW LOGON BUTTON
 		 */
 		bvLoginButton = (Button) findViewById(R.id.bvlogin);
 		bvLoginButton.setText( (barviewUser.isSessionValid()) ? R.string.bv_logout : R.string.bv_login );
 		bvLoginButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				
 				if(barviewUser.isSessionValid()) {
 					/*
                 	 * Make call-out to mobilelogin.php here.
                 	 */
                 	BarviewMobileLoginTask login = new BarviewMobileLoginTask();
                 	login.setActivity(activity);
                 	login.execute(BarviewConstants.BARVIEW_LOGOUT, barviewUser.getToken());
                 	
                 	// Clearing of preferences takes place in BarviewMobileLoginTask
 				}
 				else {
 					LayoutInflater factory = LayoutInflater.from(arg0.getContext());
 		            final View bvLogon = factory.inflate(R.layout.barviewlogin, null);
 		            
 		            AlertDialog d = new AlertDialog.Builder(arg0.getContext())
 		                .setTitle(R.string.bv_login_title)
 		                .setView(bvLogon)
 		                .setNeutralButton(R.string.bv_login_button, new DialogInterface.OnClickListener() {
 		                    public void onClick(DialogInterface dialog, int whichButton) {
 		                    	AlertDialog categoryDetail = (AlertDialog)dialog;
 		                    	
 		                    	EditText username = (EditText) categoryDetail.findViewById(R.id.bv_login_username);
 		                    	EditText password = (EditText) categoryDetail.findViewById(R.id.bv_login_password);
 		                    	
 		                    	/*
 		                    	 * Make call-out to mobilelogin.php here.
 		                    	 */
 		                    	BarviewMobileLoginTask login = new BarviewMobileLoginTask();
 		                    	login.setActivity(activity);
 		                    	login.execute(BarviewConstants.BARVIEW_LOGIN, username.getText().toString(), password.getText().toString());
 		                    	
 		                    }
 		                })
 		                .create();
 		            d.show();
 				}
 			}
 			
 		});
 		
 		/*
 		 * Toggle button visibility
 		 */
 		if(FacebookUtility.isLoggedIn())
 			bvLoginButton.setVisibility(View.INVISIBLE);
 		else if(BarviewMobileUtility.isLoggedIn())
 			fbLoginButton.setVisibility(View.INVISIBLE);
 	}
 	
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
     
     private void showFacebookErrorToast() {
     	Toast toast = Toast.makeText(this, "Unable to log into Facebook.  Please try again later.", Toast.LENGTH_SHORT);
     	toast.show();
     }
 }
