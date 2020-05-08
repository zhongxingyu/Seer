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
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.IntentSender.SendIntentException;
 import android.os.Bundle;
 import eu.trentorise.smartcampus.ac.Constants;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.ac.model.UserData;
 
 /**
  *  Implementation of the {@link SCAccessProvider} interface relying on the token
  * stored {@link AccountManager} and using the {@link AuthenticatorActivity} activity.
 
  * @author raman
  *
  */
 public class AMSCAccessProvider implements SCAccessProvider {
 	
 	@Override
 	public String readToken(Context ctx, String inAuthority) {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		AccountManager am = AccountManager.get(ctx);
 		return am.peekAuthToken(new Account(Constants.getAccountName(ctx), Constants.getAccountType(ctx)), authority);
 	}
 
 	@Override
 	public String getAuthToken(Context ctx, String inAuthority) throws OperationCanceledException, AuthenticatorException,
 			IOException {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		AccountManager am = AccountManager.get(ctx);
 		AccountManagerFuture<Bundle> future = am.getAuthToken(new Account(Constants.getAccountName(ctx), Constants.getAccountType(ctx)), authority, true, null, null); 
 		String token = null;
 		if (future.isDone()) {
 			token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
 		}
 		return token;
 	}
 
 	@Override
 	public String getAuthToken(final Activity activity, String inAuthority) throws OperationCanceledException, AuthenticatorException, IOException {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		final AccountManager am = AccountManager.get(activity);
 		String token = am.peekAuthToken(new Account(Constants.getAccountName(activity), Constants.getAccountType(activity)), authority);
 		if (token == null)
 		{
 			final Account a = new Account(Constants.getAccountName(activity), Constants.getAccountType(activity));
 			am.getAuthToken(a, authority, false, 
 					new AccountManagerCallback<Bundle>() {
 	
 						@Override
 						public void run(AccountManagerFuture<Bundle> result) {
 							Bundle bundle = null;
 							try {
 								bundle = result.getResult();
 								Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
 								if (launch != null) {
 										launch.putExtra(Constants.KEY_AUTHORITY, authority);
 										activity.startActivityForResult(launch, SC_AUTH_ACTIVITY_REQUEST_CODE);
 //								} else if (bundle.getString(AccountManager.KEY_AUTHTOKEN) != null){
 //									 am.setAuthToken(a, authority, bundle.getString(AccountManager.KEY_AUTHTOKEN));
 //									 am.addAccountExplicitly(a, null, null);
 								}
 							} catch (Exception e) {
 								return;
 							}
 						}
 					}
 			, null);
 			return null;
 		}
 		return token;		
 
 	}
 
 	@Override
 	public String getAuthToken(Context ctx, String inAuthority, IntentSender intentSender) throws OperationCanceledException, AuthenticatorException, IOException {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		AccountManager am = AccountManager.get(ctx);
 		AccountManagerFuture<Bundle> future = am.getAuthToken(new Account(Constants.getAccountName(ctx), Constants.getAccountType(ctx)), authority, false, new OnTokenAcquired(ctx, authority, intentSender), null);
 		String token = null;
 		if (future.isDone()) {
 			token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
 		}
 		return token;
 	}
 
 	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
 		private Context context;
 		private String authority; 
 		private IntentSender intentSender;
 
 		public OnTokenAcquired(Context context, String authority,IntentSender intentSender) {
 			super();
 			this.context = context;
 			this.authority = authority;
 			this.intentSender = intentSender;
 		}
 
 		@Override
 		public void run(AccountManagerFuture<Bundle> result) {
 			Bundle bundle = null;
 			String token = null;
 			try {
 				bundle = result.getResult();
 			} catch (Exception e) {
 				return;
 			}
 			token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
 			if (token != null) {
 				if (intentSender != null) {
 					try {
 						Intent add = new Intent();
 						add.putExtra(AccountManager.KEY_AUTHTOKEN, token);
 						intentSender.sendIntent(context, SC_AUTH_ACTIVITY_REQUEST_CODE, add, null, null);
 					} catch (SendIntentException e) {
 						return;
 					}
 				}
 				return;
 			}
 
 			Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
 			if (launch != null) {
 				if (context != null) {
 					launch.putExtra(Constants.KEY_AUTHORITY, authority);
 					launch.putExtra(Constants.CALLBACK_INTENT, intentSender);
 					context.startActivity(launch);
 				} 
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void invalidateToken(Context context, String inAuthority) {
 //		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		AccountManager am = AccountManager.get(context);
 		am.invalidateAuthToken(Constants.getAccountType(context), readToken(context, inAuthority));
 		am.removeAccount(new Account(Constants.getAccountName(context), Constants.getAccountType(context)), null, null);
 	}
 
 	@Override
 	public String promote(final Activity activity, String inAuthority, final String token) {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		final AccountManager am = AccountManager.get(activity);
 		Bundle options = new Bundle();
 		if (token != null) {
 			options.putString(Constants.PROMOTION_TOKEN, token);
 		}
 		final Account a = new Account(Constants.getAccountName(activity), Constants.getAccountType(activity));
		invalidateToken(activity, authority);
 		
 		am.getAuthToken(
 				new Account(Constants.getAccountName(activity), Constants.getAccountType(activity)), 
 				authority, 
 				options, 
 				activity,
 				new AccountManagerCallback<Bundle>() {
 					@Override
 					public void run(AccountManagerFuture<Bundle> result) {
 						Bundle bundle = null;
 						try {
 							bundle = result.getResult();
 							Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
 							if (launch != null) {
 									launch.putExtra(Constants.KEY_AUTHORITY, authority);
 									activity.startActivityForResult(launch, SC_AUTH_ACTIVITY_REQUEST_CODE);
 							} else if (bundle.getString(AccountManager.KEY_AUTHTOKEN) != null){
//								 am.setAuthToken(a, authority, bundle.getString(AccountManager.KEY_AUTHTOKEN));
 //								 am.addAccountExplicitly(a, null, null);
 							// no token acquired
 							} else {
 								storeAnonymousToken(token, authority, am, a);
 							}
 						} catch (Exception e) {
 							// revert the invalidated token
 							storeAnonymousToken(token, authority, am, a);
 							return;
 						}
 					}
 				}
 				, 
 				null);
 		return null;
 	}
 
 	private void storeAnonymousToken(final String token,
 			final String authority, final AccountManager am,
 			final Account a) {
 		if (token == null) return;
 		am.setAuthToken(a, authority, token);
 		am.setAuthToken(a, Constants.TOKEN_TYPE_ANONYMOUS, token);
 		am.addAccountExplicitly(a, null, null);
 	}
 
 	@Override
 	public UserData readUserData(Context ctx, String inAuthority) {
 		final String authority = inAuthority == null ? Constants.AUTHORITY_DEFAULT : inAuthority;
 		AccountManager am = AccountManager.get(ctx);
 		Account account = new Account(Constants.getAccountName(ctx), Constants.getAccountType(ctx));
 		String token = am.peekAuthToken(account, authority);
 		if (token == null) return null;
 		String userDataString = am.getUserData(account, AccountManager.KEY_USERDATA);
 		if (userDataString == null) return null;
 		try {
 			return UserData.valueOf(new JSONObject(userDataString));
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean isUserAnonymous(Context ctx) {
 		UserData data = readUserData(ctx, Constants.TOKEN_TYPE_ANONYMOUS);
 		if (data == null)  return false;
 		return data.getAttributes()== null || (data.getAttributes().size() == 1 && data.getAttributes().get(0).getAuthority().getName().equals(Constants.TOKEN_TYPE_ANONYMOUS));
 	}
 
 	
 	
 }
