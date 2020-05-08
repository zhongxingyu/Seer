 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.ac.authenticator;
 
 import org.json.JSONException;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.IntentSender.SendIntentException;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.util.Log;
 import eu.trentorise.smartcampus.ac.AuthActivity;
 import eu.trentorise.smartcampus.ac.AuthListener;
 import eu.trentorise.smartcampus.ac.Constants;
 import eu.trentorise.smartcampus.ac.DeviceUuidFactory;
 import eu.trentorise.smartcampus.ac.R;
 import eu.trentorise.smartcampus.ac.model.UserData;
 import eu.trentorise.smartcampus.ac.network.RemoteConnector;
 
 /**
  *  Implementation of the {@link AuthActivity} storing the acquired token
  * in the {@link AccountManager} infrastructure and broadcasting the result event.
  * @author raman
  *
  */
 public class AuthenticatorActivity  extends AuthActivity {
 	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
     public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
 
 	private AccountManager mAccountManager;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
       mAccountManager = AccountManager.get(this);
       super.onCreate(savedInstanceState);
     }
 
 	@Override
 	protected void setUp() {
 	     Intent request = getIntent();
 	     String authTokenType = request.getStringExtra(Constants.KEY_AUTHORITY)!=null ? 
 	    		 request.getStringExtra(Constants.KEY_AUTHORITY) : Constants.AUTHORITY_DEFAULT;
     	if (Constants.TOKEN_TYPE_ANONYMOUS.equals(authTokenType)) {
     		new AsyncTask<Void, Void, UserData>() {
     			private ProgressDialog progress = null;
 
 				protected void onPostExecute(UserData result) {
 					if (progress != null) {
 						try {
 							progress.cancel();
 						} catch (Exception e) {
 							Log.w(getClass().getName(),"Problem closing progress dialog: "+e.getMessage());
 						}
 					}
 					if (result != null && result.getToken() != null) {
 						getAuthListener().onTokenAcquired(result);
 					} else {
 						getAuthListener().onAuthFailed("Failed to create anonymous account");
 					}
 					// TODO
 				}
 				@Override
 				protected void onPreExecute() {
 					progress  = ProgressDialog.show(AuthenticatorActivity.this, "", AuthenticatorActivity.this.getString(R.string.auth_in_progress), true);
 					super.onPreExecute();
 				}
 
 				@Override
 				protected UserData doInBackground(Void... params) {
 					try {
 						return RemoteConnector.createAnonymousUser(Constants.getAuthUrl(AuthenticatorActivity.this), new DeviceUuidFactory(AuthenticatorActivity.this).getDeviceUuid().toString());
 					} catch (NameNotFoundException e) {
 						Log.e(Authenticator.class.getName(), "Failed to create anonymous user: "+ e.getMessage());
 						return null;
 					}
 				}
 			}.execute();
     	} else { 
     		super.setUp();
     	}
 	}
 
 
 
 	@Override
 	protected AuthListener getAuthListener() {
 		return new AMAuthListener();
 	}
 
 	private class AMAuthListener implements AuthListener {
 
 		@Override
 		public void onTokenAcquired(UserData data) {
 			 final Account account = new Account(Constants.getAccountName(AuthenticatorActivity.this), Constants.getAccountType(AuthenticatorActivity.this));
 			 Bundle dataBundle = new Bundle();
 			 try {
 				dataBundle.putString(AccountManager.KEY_USERDATA, data.toJSON().toString());
 			} catch (JSONException e1) {
 				Log.e(AuthenticatorActivity.class.getName(), "Failed to write UserData: "+e1.getMessage());
 			}
			 Account[] accounts = mAccountManager.getAccountsByType(Constants.getAccountType(AuthenticatorActivity.this));
 			 if (accounts != null) {
 				for (int i = 0; i < accounts.length; i++) {
 					mAccountManager.removeAccount(accounts[i], null, null);
 				}
 			 }
 			 mAccountManager.addAccountExplicitly(account, null, dataBundle);
 			 
 	         ContentResolver.setSyncAutomatically(account,ContactsContract.AUTHORITY, true);
 	          
 		     Intent request = getIntent();
 		     final String authority = request.getStringExtra(Constants.KEY_AUTHORITY)!=null && 
 		    		 !request.getStringExtra(Constants.KEY_AUTHORITY).equals(Constants.TOKEN_TYPE_ANONYMOUS) ? 
 		    		 request.getStringExtra(Constants.KEY_AUTHORITY) : Constants.AUTHORITY_DEFAULT;
 				 
 		     mAccountManager.setAuthToken(account, authority, data.getToken());
 		     if (request.getStringExtra(Constants.KEY_AUTHORITY).equals(Constants.TOKEN_TYPE_ANONYMOUS)) mAccountManager.setAuthToken(account, Constants.TOKEN_TYPE_ANONYMOUS, data.getToken());
 
 			 final Intent intent = new Intent();
			 intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Constants.getAccountName(AuthenticatorActivity.this));
			 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.getAccountType(AuthenticatorActivity.this));
 			 intent.putExtra(AccountManager.KEY_AUTHTOKEN, data.getToken());
 
 			 // this is workaround that is needed on some devices: without it the 
 			 // getuserdata may return null. the problem is with the bug in the in-memory account data caching
 			 mAccountManager.setUserData(account, AccountManager.KEY_USERDATA, dataBundle.getString(AccountManager.KEY_USERDATA));
 			 
 			 setAccountAuthenticatorResult(intent.getExtras());
 			 setResult(RESULT_OK, intent);
 			 
 			 IntentSender sender = request.getParcelableExtra(Constants.CALLBACK_INTENT);
 			 if (sender != null) {
 				 try {
 						Intent add = new Intent();
 						add.putExtra(AccountManager.KEY_AUTHTOKEN, data.getToken());
 						add.putExtra(AccountManager.KEY_USERDATA, data);
 						sender.sendIntent(AuthenticatorActivity.this, 0, add, null, null);
 					} catch (SendIntentException e) {
 						e.printStackTrace();
 					}
 			 }
 			 
 			 finish();  	    		  
 		}
 
 		@Override
 		public void onAuthFailed(String error) {
 			 final Intent intent = new Intent();
 			 intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Constants.getAccountName(AuthenticatorActivity.this));
 			 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.getAccountType(AuthenticatorActivity.this));
 			 intent.putExtra(AccountManager.KEY_AUTH_FAILED_MESSAGE, error);
 			 setAccountAuthenticatorResult(intent.getExtras());
 			 setResult(Constants.RESULT_FAILURE, intent);
 			 finish();  	    		  
 		}
 
 		@Override
 		public void onAuthCancelled() {
 			 final Intent intent = new Intent();
 			 intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Constants.getAccountName(AuthenticatorActivity.this));
 			 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.getAccountType(AuthenticatorActivity.this));
 			 setAccountAuthenticatorResult(intent.getExtras());
 			 setResult(RESULT_CANCELED, intent);
 			 finish();  	    		  
 		}
 		
 		
     	
     }
     
 }
