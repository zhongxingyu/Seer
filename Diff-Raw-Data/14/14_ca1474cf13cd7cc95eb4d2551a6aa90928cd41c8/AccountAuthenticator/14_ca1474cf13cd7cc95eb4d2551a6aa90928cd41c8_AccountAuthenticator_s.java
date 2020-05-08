 /*Follows tutorials found at
  * http://www.lukeslog.de/?page_id=1052 ---> much more implemenataion, less explanation
  * http://www.finalconcept.com.au/article/view/android-account-manager-step-by-step ---> Explains more
  */
 
 package com.rogoapp.auth;
 
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.rogoapp.CacheClient;
 import com.rogoapp.R;
 import com.rogoapp.ServerClient;
 
 import android.accounts.AbstractAccountAuthenticator;
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 
 public class AccountAuthenticator extends AbstractAccountAuthenticator {
 
         final Context mContext;
         final static String ACCOUNT_TYPE = "com.rogoapp";
         private CacheClient cache;
         public AccountAuthenticator(Context context) {
                 super(context);
                 mContext = context; //added for use when adding an account
         }
 
         @Override
         public Bundle addAccount(AccountAuthenticatorResponse response,
                         String accountType, String authTokenType,
                         String[] requiredFeatures, Bundle options)
                         throws NetworkErrorException {
                 final Bundle result;
                 final Intent intent; //Creates a new intent that point to the login page
                 
                 //adds authTokenType and response information to the intent
                 intent = new Intent(this.mContext, RogoAuthenticatorActivity.class);
                 intent.putExtra(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
                 intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                 
                 //creates a bundle with the created intent in it and returns the result of that intent
                 result = new Bundle();
                 result.putParcelable(AccountManager.KEY_INTENT, intent);
                 
                 return result;
         }
 
         @Override
         public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                         Account account, Bundle options) throws NetworkErrorException {
             
         	//checks for empty bundles and bundles without cached passwords
         	if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
         		
         		//retrieves and authenticates user data with the server
         		final String password = options.getString(AccountManager.KEY_PASSWORD);
                 final boolean verified = sValidate(account.name, password);
                 
                 final Bundle result = new Bundle();
                 result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
                 return result;
             }
             // Launch AuthenticatorActivity to confirm credentials
             final Intent intent = new Intent(mContext, RogoAuthenticatorActivity.class);
             intent.putExtra(RogoAuthenticatorActivity.PARAM_USERNAME, account.name);
             intent.putExtra(RogoAuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, true);
             intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
             
             final Bundle bundle = new Bundle();
             bundle.putParcelable(AccountManager.KEY_INTENT, intent);
             return bundle;
         }
 
         @Override
         public Bundle editProperties(AccountAuthenticatorResponse response,
                         String accountType) {
         	
             throw new UnsupportedOperationException();
         }
 
         @Override
         public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
          
         	//check the token type to make sure it matches the expected type
         	if(!authTokenType.equals(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE)){
         		//if it doesn't match, return an error message
         		final Bundle result = new Bundle();
                 result.putString(AccountManager.KEY_ERROR_MESSAGE,
                     "invalid authTokenType");
                 return result;
         	}
         	
             // Extract the username and password from the Account Manager, and ask
             // the server for an appropriate AuthToken.
             final AccountManager am = AccountManager.get(mContext);
          
             String authToken = am.peekAuthToken(account, authTokenType);
          
             // Lets give another try to authenticate the user
             if (TextUtils.isEmpty(authToken)) {
                 final String password = am.getPassword(account);
                 if (password != null) {
                     authToken = authRetrieve(account.name, password, authTokenType);
                 }
             }
          
             // If we get an authToken - we return it
             if (!TextUtils.isEmpty(authToken)) {
                 final Bundle result = new Bundle();
                 result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                 result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                 result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                 return result;
             }
          
             // If we get here, then we couldn't access the user's password - so we
             // need to re-prompt them for their credentials. We do that by creating
             // an intent to display our AuthenticatorActivity.
             final Intent intent = new Intent(mContext, RogoAuthenticatorActivity.class);
             intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
             //intent.putExtra(RogoAuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
             intent.putExtra(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
             intent.putExtra(RogoAuthenticatorActivity.PARAM_USERNAME, account.name); 
             
             final Bundle bundle = new Bundle();
             bundle.putParcelable(AccountManager.KEY_INTENT, intent);
             return bundle;
         }
 
         @Override
         public String getAuthTokenLabel(String authTokenType) {
         	
         	if (authTokenType.equals(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE)) {
                 return mContext.getString(R.string.app_name);
             }
             return null;
         }
 
         @Override
         public Bundle hasFeatures(AccountAuthenticatorResponse response,
                         Account account, String[] features) throws NetworkErrorException {
         	final Bundle result = new Bundle();
             result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
             return result;
         }
 
         @Override
         public Bundle updateCredentials(AccountAuthenticatorResponse response,
                         Account account, String authTokenType, Bundle options)
                         throws NetworkErrorException {
                 
         		final Intent intent = new Intent(mContext, RogoAuthenticatorActivity.class);
                 intent.putExtra(RogoAuthenticatorActivity.PARAM_USERNAME, account.name);
                 intent.putExtra(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
                 intent.putExtra(RogoAuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, false);
                 final Bundle bundle = new Bundle();
                 bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                 
                 return bundle;
         }
         
         public Boolean sValidate(String name, String password){
         	
         	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
         	nameValuePairs.add(new BasicNameValuePair("email", name));
         	nameValuePairs.add(new BasicNameValuePair("password", password));
 			
     		try {
 				return !ServerClient.genericPostRequest("login", nameValuePairs, mContext).getString("data").equals("Email or password is incorrect!");
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return false;
 
 			}
     		
         }
  
         public String authRetrieve(String name, String password, String authToken){
         	
         	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
         	nameValuePairs.add(new BasicNameValuePair("email", name));
         	nameValuePairs.add(new BasicNameValuePair("password", password));
 			
     		try {
 				JSONObject json = (ServerClient.genericPostRequest("login", nameValuePairs, mContext));
 				String token = /*json.getJSONObject("data").getString("session") + */json.getJSONObject("data").getString("secret");
 				cache.saveFile(CacheClient.SESSION_CACHE, json.getJSONObject("data").getString("session"));
 				return token;
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return null;
 
 			}
         }
         
         public static String hashPassword(String pass){
             MessageDigest md = null;
             try{
                 md = MessageDigest.getInstance("SHA-512");
             }
             catch(Exception e){
                 e.printStackTrace();
             }
             
             md.update(pass.getBytes());
              
             byte byteData[] = md.digest();
             
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < byteData.length; i++) {
              sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
             }
             
             return sb.toString();
         }
         
         public String changeSession(){
 			
         	String cSession = cache.loadFile(CacheClient.SESSION_CACHE);
         	String token = "";
         	
         	AccountManager am = AccountManager.get(mContext);
         	
         	Account[] accounts = am.getAccountsByType(RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE);    	
         	Account account = null;
         	
         	if(accounts.length != 0){
         		account = accounts[0];
         		token = am.peekAuthToken(account, RogoAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE);
         	}
             
         	cache.saveFile(CacheClient.SESSION_CACHE, hashPassword(cSession + token));
         	return hashPassword(cSession);
         }
         
 }
