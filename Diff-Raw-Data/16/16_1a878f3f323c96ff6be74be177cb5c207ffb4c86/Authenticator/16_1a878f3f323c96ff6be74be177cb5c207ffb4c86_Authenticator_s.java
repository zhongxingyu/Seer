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
 
 import android.accounts.AbstractAccountAuthenticator;
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.DeviceUuidFactory;
import eu.trentorise.smartcampus.ac.model.UserData;
import eu.trentorise.smartcampus.ac.network.RemoteConnector;
 
 /**
  * {@link AbstractAccountAuthenticator} implementation for SmartCampus authentication mechanism. 
  * @author raman
  *
  */
 public class Authenticator extends AbstractAccountAuthenticator {
 
     // Authentication Service context
     private final Context mContext;
 
     public Authenticator(Context context) {
         super(context);
         mContext = context;
     }
 
     @Override
     public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
             String authTokenType, String[] requiredFeatures, Bundle options) 
     {
     	try {
 			return getAuthToken(response, null, authTokenType, options);
 		} catch (NetworkErrorException e) {
 			Log.e(Authenticator.class.getName(), ""+e.getMessage());
 			return null;
 		}
     }
 
     @Override
     public Bundle confirmCredentials(
             AccountAuthenticatorResponse response, Account account, Bundle options) {
         return null;
     }
 
     @Override
     public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
             String authTokenType, Bundle loginOptions) throws NetworkErrorException 
     {
         final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
         intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
         intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
         intent.putExtra(Constants.KEY_AUTHORITY, authTokenType);
         try {
 			intent.setData(Uri.parse(Constants.getRequestUrl(mContext)));
 		} catch (NameNotFoundException e) {
 			Log.e(getClass().getSimpleName(), "No auth url provided");
 			return null;
 		}
         final Bundle bundle = new Bundle();
         bundle.putParcelable(AccountManager.KEY_INTENT, intent);
         return bundle;
     }
 
     @Override
     public String getAuthTokenLabel(String authTokenType) {
     	return null;
     }
 
     @Override
     public Bundle hasFeatures(
             AccountAuthenticatorResponse response, Account account, String[] features) {
         final Bundle result = new Bundle();
         result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
         return result;
     }
 
     @Override
     public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
             String authTokenType, Bundle loginOptions) {
         return null;
     }
 }
