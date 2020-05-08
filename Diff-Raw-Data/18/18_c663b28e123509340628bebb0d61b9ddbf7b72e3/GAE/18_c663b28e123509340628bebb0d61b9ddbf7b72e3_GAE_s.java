 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 
 public final class GAE {
 
     private static final String PROD_URL = "https://ptwgame.appspot.com";
     private static final String AUTH_COOKIE_NAME = "SACSID";
     // Timeout in milliseconds until a connection is established.
     private static final int TIMEOUT_CONNECTION = 3000;
     // Timeout in milliseconds to wait for data.
     private static final int TIMEOUT_SOCKET = 5000;
 
     private static final Object sInstanceSync = new Object();
     private static GAE sInstance;
 
     private Context mContext;
     private Handler mHandler;
     private final Object mListenerSync = new Object();
     private GaeListener mListener;
     private String mAccountName;
     private boolean mNeedInvalidate = true;
     private String mGetPage;
     private String mJson;
 
     static interface GaeListener {
         void onFailedConnect();
         void onLaunchIntent(Intent launch);
         void onConnectSuccess(Context context, String json);
         void onGet(Context context, String json);
     }
 
     public static boolean isAccountSetupNeeded(Context context) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
         return prefs.getString(EditPreferences.KEY_ACCOUNT_EMAIL, "").length() == 0;
     }
 
     public static GAE getInstance(Context context) {
         synchronized (sInstanceSync) {
             if (sInstance == null) {
                 Util.log("Instantiate GAE object");
                 sInstance = new GAE(context);
             }
         }
         return sInstance;
     }
 
     private GAE(Context context) {
         mContext = context;
         mHandler = new Handler();
     }
 
     public List<String> getGoogleAccounts() {
         ArrayList<String> result = new ArrayList<String>();
         Account[] accounts = AccountManager.get(mContext).getAccountsByType("com.google");
         for (Account account : accounts) {
             result.add(account.name);
         }
 
         return result;
     }
 
     public void connect(final GaeListener listener, final String account) {
         if ((listener == null) || (account == null))
             throw new IllegalArgumentException("No null arguments allowed");
 
         final Runnable r = new Runnable() {
             public void run() {
                 synchronized (mListenerSync) {
                     if (mListener == null) {
                         Util.log("Connect using " + account);
                         mListener = listener;
                         mAccountName = account;
                         doConnect();
                     } else
                         mHandler.postDelayed(this, 100);
                 }
             }
         };
         mHandler.post(r);
     }
 
     public void getPage(final GaeListener listener, final String page) {
         if ((listener == null) || (page == null))
             throw new IllegalArgumentException("No null arguments allowed");
 
         final Runnable r = new Runnable() {
             public void run() {
                 synchronized (mListenerSync) {
                     if (mListener == null) {
                         Util.log("Getting page " + page);
                         mListener = listener;
                         new GetPageTask().execute(page);
                     } else
                         mHandler.postDelayed(this, 100);
                 }
             }
         };
         mHandler.post(r);
     }
 
     public void postPage(final GaeListener listener, final String page, final String json) {
         if ((listener == null) || (page == null) || (json == null))
             throw new IllegalArgumentException("No null arguments allowed");
 
         final Runnable r = new Runnable() {
             public void run() {
                 synchronized (mListenerSync) {
                     if (mListener == null) {
                         Util.log("Posting to " + page + " page: " + json);
                         mListener = listener;
                         new PostPageTask().execute(page, json);
                     } else
                         mHandler.postDelayed(this, 100);
                 }
             }
         };
         mHandler.post(r);
     }
 
     private void doConnect() {
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString(EditPreferences.KEY_ACCOUNT_EMAIL, null);
         editor.putString(EditPreferences.KEY_ACCOUNT_COOKIE, null);
         editor.commit();
 
         mContext.getSharedPreferences(Questions.ACACHE, Activity.MODE_PRIVATE).edit().clear().commit();
         reconnect();
     }
 
     @SuppressWarnings("deprecation")
     private void reconnect() {
         AccountManager mgr = AccountManager.get(mContext);
         Account[] accts = mgr.getAccountsByType("com.google");
         for (Account acct : accts) {
             if (acct.name.equals(mAccountName)) {
                 mgr.getAuthToken(acct, "ah", false, new AuthTokenCallback(), null);
                 break;
             }
         }
     }
 
     private class AuthTokenCallback implements AccountManagerCallback<Bundle> {
         public void run(AccountManagerFuture<Bundle> future) {
             try {
                 Bundle result = future.getResult();
 
                 Intent launch = (Intent) result.get(AccountManager.KEY_INTENT);
                 if (launch != null) {
                     Util.log("Need to launch activity before getting authToken");
                     // How can we get the result of the activity if it is a new task!?
                     launch.setFlags(launch.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                     cbLaunchIntent(launch);
                     return;
                 }
 
                 String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                 if (mNeedInvalidate) {
                     Util.log("Invalidating token and starting over");
                     mNeedInvalidate = false;
 
                     AccountManager mgr = AccountManager.get(mContext);
                     mgr.invalidateAuthToken("com.google", authToken);
                     reconnect();
                 } else {
                     Util.log("authToken=" + authToken);
                     mNeedInvalidate = true;
 
                     // Phase 2: get authCookie from PTW server
                     new GetCookieTask().execute(authToken);
                 }
             } catch (Exception e) {
                 Util.log("Get auth token failed with exception " + e);
                 cbFailedConnect();
             }
         }
     }
 
     private class GetCookieTask extends AsyncTask<String, Integer, Boolean> {
         protected Boolean doInBackground(String... tokens) {
             String authCookie = null;
 
             try {
                 DefaultHttpClient client = new DefaultHttpClient();
                 String continueURL = PROD_URL;
                 URI uri = new URI(PROD_URL + "/_ah/login?continue="
                         + URLEncoder.encode(continueURL, "UTF-8") + "&auth=" + tokens[0]);
                 HttpGet method = new HttpGet(uri);
                 final HttpParams getParams = new BasicHttpParams();
                 HttpClientParams.setRedirecting(getParams, false);
                 HttpConnectionParams.setConnectionTimeout(getParams, TIMEOUT_CONNECTION);
                 HttpConnectionParams.setSoTimeout(getParams, TIMEOUT_SOCKET);
                 method.setParams(getParams);
 
                 HttpResponse res = client.execute(method);
                 Header[] headers = res.getHeaders("Set-Cookie");
                 int statusCode = res.getStatusLine().getStatusCode();
                 if (statusCode != 302 || headers.length == 0) {
                     Util.log("Get auth cookie failed: statusCode=" + statusCode);
                     return false;
                 }
 
                 for (Cookie cookie : client.getCookieStore().getCookies()) {
                     if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                         authCookie = AUTH_COOKIE_NAME + "=" + cookie.getValue();
                         Util.log(authCookie);
                     }
                 }
             } catch (Exception e) {
                 Util.log("Get auth cookie failed with exception " + e);
                 return false;
             }
 
             final SharedPreferences prefs =
                     PreferenceManager.getDefaultSharedPreferences(mContext);
             SharedPreferences.Editor editor = prefs.edit();
             editor.putString(EditPreferences.KEY_ACCOUNT_EMAIL, mAccountName);
             editor.putString(EditPreferences.KEY_ACCOUNT_COOKIE, authCookie);
             editor.commit();
 
             return true;
         }
 
         protected void onPostExecute(Boolean success) {
             if (success) {
                 if (mJson != null)
                     new PostPageTask().execute(mGetPage, mJson);
                 else if (mGetPage != null)
                     new GetPageTask().execute(mGetPage);
                 else
                     cbConnectSuccess(null);
             } else
                 cbFailedConnect();
         }
     }
 
     private class GetPageTask extends AsyncTask<String, Integer, Boolean> {
         StringBuilder mBuilder = new StringBuilder();
 
         protected Boolean doInBackground(String... pages) {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
             DefaultHttpClient client = new DefaultHttpClient();
             HttpGet method = new HttpGet(PROD_URL + "/" + pages[0]);
 
             final HttpParams getParams = new BasicHttpParams();
             HttpClientParams.setRedirecting(getParams, false);
             HttpConnectionParams.setConnectionTimeout(getParams, TIMEOUT_CONNECTION);
             HttpConnectionParams.setSoTimeout(getParams, TIMEOUT_SOCKET);
             method.setParams(getParams);
             method.setHeader("Cookie", prefs.getString(EditPreferences.KEY_ACCOUNT_COOKIE, null));
 
             try {
                 HttpResponse resp = client.execute(method);
 
                 switch(resp.getStatusLine().getStatusCode()) {
                 case HttpStatus.SC_OK:
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(resp.getEntity().getContent()));
                     String line;
                     while ((line = reader.readLine()) != null) {
                         mBuilder.append(line);
                     }
                     break;
                 case HttpStatus.SC_MOVED_TEMPORARILY:
                     if (mAccountName != null) {
                         Util.log("Get page failed (status 302)");
                         return false;
                     }
                     Util.log("Cookie expired? Attempting reconnect");
                     mGetPage = pages[0];
                     mAccountName = prefs.getString(EditPreferences.KEY_ACCOUNT_EMAIL, null);
                     reconnect();
                     break;
                 default:
                     Util.log("Get page failed (invalid status code)");
                     return false;
                 }
             } catch (Exception e) {
                 Util.log("Get page failed with exception " + e);
                 return false;
             }
 
             return true;
         }
 
         protected void onPostExecute(Boolean success) {
             if (success) {
                 if (mGetPage == null)
                     cbGet(mBuilder.toString());
             } else
                 cbFailedConnect();
         }
     }
 
     private class PostPageTask extends AsyncTask<String, Integer, Boolean> {
         String mJsonReturned;
 
         protected Boolean doInBackground(String... args) {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
             DefaultHttpClient client = new DefaultHttpClient();
             HttpPost method = new HttpPost(PROD_URL + "/" + args[0]);
 
             final HttpParams postParams = new BasicHttpParams();
             HttpClientParams.setRedirecting(postParams, false);
             HttpConnectionParams.setConnectionTimeout(postParams, TIMEOUT_CONNECTION);
             HttpConnectionParams.setSoTimeout(postParams, TIMEOUT_SOCKET);
             method.setParams(postParams);
             method.setHeader("Cookie", prefs.getString(EditPreferences.KEY_ACCOUNT_COOKIE, null));
 
             try {
                 StringEntity se = new StringEntity(args[1]);
                 se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                 method.setEntity(se);
 
                 HttpResponse resp = client.execute(method);
 
                 switch(resp.getStatusLine().getStatusCode()) {
                 case HttpStatus.SC_OK:
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(resp.getEntity().getContent()));
                     mJsonReturned = reader.readLine();
                     break;
                 case HttpStatus.SC_MOVED_TEMPORARILY:
                     if (mAccountName != null) {
                         Util.log("Post page failed (status 302)");
                         return false;
                     }
                     Util.log("Cookie expired? Attempting reconnect");
                     mGetPage = args[0];
                     mJson = args[1];
                     mAccountName = prefs.getString(EditPreferences.KEY_ACCOUNT_EMAIL, null);
                     reconnect();
                     break;
                 default:
                     Util.log("Post page failed (invalid status code)");
                     return false;
                 }
             } catch (Exception e) {
                 Util.log("Post page failed with exception " + e);
                 return false;
             }
 
             return true;
         }
 
         protected void onPostExecute(Boolean success) {
             if (success) {
                 if (mJson == null)
                     cbConnectSuccess(mJsonReturned);
             } else
                 cbFailedConnect();
         }
     }
 
     private void cbFailedConnect() {
         mListener.onFailedConnect();
         synchronized (mListenerSync) {
             mGetPage = mJson = mAccountName = null;
             mListener = null;
         }
     }
 
     private void cbLaunchIntent(Intent launch) {
         mListener.onLaunchIntent(launch);
         synchronized (mListenerSync) {
             mGetPage = mJson = mAccountName = null;
             mListener = null;
         }
     }
 
     private void cbConnectSuccess(String json) {
         mListener.onConnectSuccess(mContext, json);
         synchronized (mListenerSync) {
             mGetPage = mJson = mAccountName = null;
             mListener = null;
         }
     }
 
     private void cbGet(String json) {
         mListener.onGet(mContext, json);
         synchronized (mListenerSync) {
             mGetPage = mJson = mAccountName = null;
             mListener = null;
         }
     }
 }
