 /*
  * Copyright (C) 2010 The Android Open Source Project
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.nbos.phonebook.sync.client;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.content.Context;
 import android.os.Handler;
 import android.util.Log;
 
 import com.facebook.android.Util;
 import com.nbos.phonebook.ValidationActivity;
 import com.nbos.phonebook.sync.authenticator.AuthenticatorActivity;
 import com.nbos.phonebook.sync.platform.Cloud;
 import com.nbos.phonebook.util.ThreadUtil;
 
 /**
  * Provides utility methods for communicating with the server.
  */
 public class Net {
     private static final String tag = "NetworkUtilities";
     public static final String PARAM_USERNAME = "username";
     public static final String PARAM_PASSWORD = "password";
     public static final String PARAM_PHONE_NUMBER = "ph";
     public static final String PARAM_VALIDATION_CODE = "valid";
     public static final String PARAM_UPDATED = "timestamp";
     public static final String USER_AGENT = "AuthenticationService/1.0";
     public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms
     public static final String BASE_URL = Cloud.BASE_URL;
     public static final String AUTH_URI = BASE_URL + "/mobile/index",
     	REG_URL = BASE_URL + "/mobile/register",
     	VALIDATION_URI = BASE_URL + "/mobile/validate",
     	NEW_VALIDATION_CODE_URI = BASE_URL + "/mobile/newValidationCode",
     	CHECK_VALID_ACCOUNT_URI = BASE_URL + "/mobile/valid";
     /**
      * Connects to the Voiper server, authenticates the provided username and
      * password.
      * 
      * @param username The user's username
      * @param password The user's password
      * @param handler The hander instance from the calling UI thread.
      * @param context The context of the calling Activity.
      * @return boolean The boolean result indicating whether the user was
      *         successfully authenticated.
      * @throws IOException 
      * @throws JSONException 
      * @throws ClientProtocolException 
      */
     public static boolean authenticate(String username, String password, String ph,
         Handler handler, final Context context) {
     	Log.i(tag, "Authenticate");
 
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, username));
         params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
         params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, ph));
         try {
 			final HttpResponse response = new Cloud(null, username, password).postHttp(AUTH_URI, params);
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	Log.i(tag, "Successful authentication");
            	Log.i(tag, "data: "+EntityUtils.toString(response.getEntity()));
                 sendResult(true, handler, context, null);
                 return true;
             } else {
                 // if (Log.isLoggable(TAG, Log.VERBOSE)) {
                     Log.v(tag, "Error authenticating" + response.getStatusLine());
                 // }
                sendResult(false, handler, context, "Error authenticating: " + response.getStatusLine());
                 return false;
             }
 			
 		} catch (Exception e) {
 			e.printStackTrace();
             Log.v(tag, "Exception: ", e);
             // }
             sendResult(false, handler, context, e.getMessage());
             return false;
 		}        
     }
 
     public static boolean register(String username, String password, String ph, Handler handler, final Context context) {
     	Log.i(tag, "Register" +" ,ph:"+ph);
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, username));
         params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
         params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, ph));
         
         try {
 			final HttpResponse response = new Cloud(null, null, null).postHttp(REG_URL, params);
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String data = EntityUtils.toString(response.getEntity());
                Log.i(tag, "data: "+data);
                JSONObject resp = new JSONObject(data);
                Boolean registered = resp.getBoolean("registered");
                String message = resp.getString("message");
                if(registered)
                	sendResult(true, handler, context, null);
                else
                	sendResult(false, handler, context, message);
                 return true;
             } else {
                 // if (Log.isLoggable(TAG, Log.VERBOSE)) {
                     Log.v(tag, "Error registering: " + response.getStatusLine());
                 // }
                 sendResult(false, handler, context, "Error authenticating" + response.getStatusLine());
                 return false;
             }
 			
 		} catch (Exception e) {
 			e.printStackTrace();
             Log.v(tag, "Exception: ", e);
             // }
             sendResult(false, handler, context, e.getMessage());
             return false;
 		}        
         
     }
 
     public static boolean validate(String username, String password, String ph, String validationCode, Handler handler, final Context context) {
     	Log.i(tag, "Validate, ph: "+ph+", code: "+validationCode);
         final HttpResponse resp;
 
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, ph));
         params.add(new BasicNameValuePair(PARAM_VALIDATION_CODE, validationCode));
         try {
             resp = new Cloud(null, username, password).postHttp(VALIDATION_URI, params);// httpClient.execute(post);
             if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
             	String response = EntityUtils.toString(resp.getEntity());
                 Log.i(tag, "data: "+response);
                 JSONObject result = new JSONObject(response);
                 boolean validated = result.getBoolean("validated");
                 if(validated)
                 	sendValidationResult(true, "Validation was successful", handler, context);
                 else
                 	sendValidationResult(false, "Validation was not successful", handler, context);
                 return true;
             } else {
                 // if (Log.isLoggable(TAG, Log.VERBOSE)) {
                     Log.v(tag, "Error authenticating: " + resp.getStatusLine());
                 // }
                 sendValidationResult(false, "Error authenticating: " + resp.getStatusLine(), handler, context);
                 return false;
             }
         } catch (final Exception e) {
             Log.v(tag, "IOException when getting authtoken", e);
             sendValidationResult(false, "Exception: "+e.getMessage(), handler, context);
             return false;
         } 
     }
 
     
     public static boolean newValidateCode(String username, String password, String ph,
             Handler handler, final Context context) {
         	Log.i(tag, "Validate, ph: "+ph);
             final HttpResponse resp;
 
             final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
             params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, ph));
 
             try {
                 resp = new Cloud(null, username, password).postHttp(NEW_VALIDATION_CODE_URI, params);//httpClient.execute(post);
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 	String response = EntityUtils.toString(resp.getEntity());
                 	Log.i(tag, "Successful authentication");
                     Log.i(tag, "data: "+response);
                     sendNewValidationCodeResult(true, "New validation code was sent to "+ph, handler, context);
                     return true;
                 } else {
                     Log.v(tag, "Error authenticating: " + resp.getStatusLine());
                     sendNewValidationCodeResult(false, "Error authenticating: " + resp.getStatusLine(), handler, context);
                     return false;
                 }
             } catch (final Exception e) {
                 Log.v(tag, "Exception: ", e);
                 sendNewValidationCodeResult(false, "Exception: "+e.getMessage(), handler, context);
                 return false;
             } 
         }
     
     /**
      * Sends the authentication response from server back to the caller main UI
      * thread through its handler.
      * 
      * @param result The boolean holding authentication result
      * @param handler The main UI thread's handler instance.
      * @param context The caller Activity's context.
      */
     private static void sendResult(final Boolean result, final Handler handler,
         final Context context, final String message) {
     	Log.i(tag, "sendResult("+result+")");
         if (handler == null || context == null) {
             return;
         }
         handler.post(new Runnable() {
             public void run() {
                 ((AuthenticatorActivity) context).onAuthenticationResult(result, message);
             }
         });
     }
 
     private static void sendValidationResult(final Boolean result, final String message, final Handler handler,
             final Context context) {
         	Log.i(tag, "sendValidationResult("+result+")");
             if (handler == null || context == null) {
                 return;
             }
             handler.post(new Runnable() {
                 public void run() {
                     ((ValidationActivity) context).onValidationResult(result, message);
                 }
             });
         }
 
     private static void sendNewValidationCodeResult(final Boolean result, final String message, final Handler handler,
             final Context context) {
         	Log.i(tag, "sendValidationResult("+result+")");
             if (handler == null || context == null) {
                 return;
             }
             handler.post(new Runnable() {
                 public void run() {
                     ((ValidationActivity) context).onNewValidationCodeResult(result, message);
                 }
             });
         }
 
     /**
      * Attempts to authenticate the user credentials on the server.
      * 
      * @param username The user's username
      * @param password The user's password to be authenticated
      * @param handler The main UI thread's handler instance.
      * @param context The caller Activity's context
      * @return Thread The thread on which the network mOperations are executed.
      */
     public static Thread attemptAuth(final String username,
         final String password, final String ph, final Handler handler, final Context context) {
         final Runnable runnable = new Runnable() {
             public void run() {
                 authenticate(username, password, ph, handler, context);
             }
         };
         // run on background thread.
         return ThreadUtil.performOnBackgroundThread(runnable);
     }
 
     public static Thread attemptRegister(final String username,
         final String password, final String ph, final Handler handler, final Context context) {
         final Runnable runnable = new Runnable() {
             public void run() {
                 register(username, password, ph, handler, context);
             }
         };
         // run on background thread.
         return ThreadUtil.performOnBackgroundThread(runnable);
     }
     
 	public static boolean checkValidAccount(Account account, String authtoken, String phone) throws ClientProtocolException, JSONException, IOException {
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, phone));
         JSONObject response = new JSONObject(new Cloud(null, account.name, authtoken).post(CHECK_VALID_ACCOUNT_URI, params));
         return response.getBoolean("valid");
 	}
 
 	public static Thread attemptValidate(final String userName, final String password,
 			final String phoneNumber, final String validationCode, final Handler handler, final Context context) {
         final Runnable runnable = new Runnable() {
             public void run() {
                 validate(userName, password, phoneNumber, validationCode, handler, context);
             }
         };
         // run on background thread.
         return ThreadUtil.performOnBackgroundThread(runnable);
 	}
 
 	public static Thread attemptNewValidateCode(final String userName,
 			final String password, final String phoneNumber, final Handler handler,
 			final Context context) {
         final Runnable runnable = new Runnable() {
             public void run() {
                 newValidateCode(userName, password, phoneNumber, handler, context);
             }
         };
         // run on background thread.
         return ThreadUtil.performOnBackgroundThread(runnable);
 	}
 }
