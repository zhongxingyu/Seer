 package com.pandanomic.hologoogl;
 /*
  * Copyright 2013 Chris Banes
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.provider.SyncStateContract;
 import android.text.Html;
 import android.text.InputType;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        mailTo = "pandanomic@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
 public class URLListActivity extends ListActivity
         implements PullToRefreshAttacher.OnRefreshListener {
 
     private static final int AUTHORIZATION_CODE = 1993;
     private static final int ACCOUNT_CODE = 1601;
     private AuthPreferences authPreferences;
     private AccountManager accountManager;
     private final String SCOPE = "https://www.googleapis.com/auth/urlshortener";
     private boolean loggedIn = false;
     private int APIVersion;
     private Menu optionsMenu;
    private ArrayAdapter<String> stringAdapter;
     private static ArrayList<String> ITEMS = new ArrayList<String>();
     static {
         ITEMS.add("http://goo.gl/SYFV4");
         ITEMS.add("http://goo.gl/4DR2e");
         ITEMS.add("http://goo.gl/0XsgU");
     }
 
     private PullToRefreshAttacher mPullToRefreshAttacher;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        ACRA.init(getApplication());

         /**
          * Get ListView and give it an adapter to display the sample items
          */
         ListView listView = getListView();
         stringAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                 ITEMS);
         listView.setAdapter(stringAdapter);
 
         /**
          * Here we create a PullToRefreshAttacher manually without an Options instance.
          * PullToRefreshAttacher will manually create one using default values.
          */
         mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
 
         // Set the Refreshable View to be the ListView and the refresh listener to be this.
         mPullToRefreshAttacher.addRefreshableView(listView, this);
 
         DefaultHeaderTransformer ht = (DefaultHeaderTransformer) mPullToRefreshAttacher.getHeaderTransformer();
         ht.setPullText("Swipe down to refresh");
         ht.setRefreshingText("Refreshing");
 
         APIVersion = Build.VERSION.SDK_INT;
 
         accountManager = AccountManager.get(this);
         authPreferences = new AuthPreferences(this);
         loggedIn = authPreferences.loggedIn();
         if (authPreferences.getUser() != null && authPreferences.getToken() != null) {
             // Account exists, refresh stuff and make button log out
             reauthorizeGoogle();
             loggedIn = true;
         } else {
             // No account, refresh only anonymous ones and leave button alone
         }
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         // Do something when a list item is clicked
         Log.i("mainTag", ITEMS.get(position));
         refreshMetrics task = new refreshMetrics();
         task.execute(ITEMS.get(position));
     }
 
     @Override
     public void onRefreshStarted(View view) {
         if (!checkNetwork()) {
             mPullToRefreshAttacher.setRefreshComplete();
             return;
         }
 
         if (!loggedIn) {
             Toast.makeText(this, "Please log in first", Toast.LENGTH_LONG).show();
             mPullToRefreshAttacher.setRefreshComplete();
             return;
         }
 
         String authToken = authPreferences.getToken();
         new RefreshListTask().execute(authToken);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.shorten_new_URL:
                 newURLDialog();
                 return true;
             case R.id.refresh_url_list:
                 mPullToRefreshAttacher.setRefreshing(true);
                 onRefreshStarted(getListView());
                 return true;
             case R.id.login:
                 accountSetup();
                 return true;
             case R.id.logout:
                 logout();
                 Toast.makeText(this, "You are now logged out", Toast.LENGTH_LONG).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.action_settings:
                 return true;
             case R.id.send_feedback:
                 sendFeedback();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         this.optionsMenu = menu;
         getMenuInflater().inflate(R.menu.urllist_menu, menu);
 
         if (loggedIn) {
             menu.findItem(R.id.login).setVisible(false);
             menu.findItem(R.id.logout).setVisible(true);
         } else {
             menu.findItem(R.id.login).setVisible(true);
             menu.findItem(R.id.logout).setVisible(false);
         }
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (loggedIn) {
             menu.findItem(R.id.login).setVisible(false);
             menu.findItem(R.id.logout).setVisible(true);
         } else {
             menu.findItem(R.id.login).setVisible(true);
             menu.findItem(R.id.logout).setVisible(false);
         }
         return super.onPrepareOptionsMenu(menu);
     }
 
     public void accountSetup() {
         Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[] {"com.google"}, false, null, null, null, null);
         startActivityForResult(intent, ACCOUNT_CODE);
     }
 
     private void requestToken(boolean passive) {
         Account userAccount = null;
         String user = authPreferences.getUser();
         for (Account account : accountManager.getAccountsByType("com.google")) {
             if (account.name.equals(user)) {
                 userAccount = account;
                 break;
             }
         }
 
         accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this,
                 new OnTokenAcquired(passive), null);
     }
 
     /**
      * call this method if your token expired, or you want to request a new
      * token for whatever reason. call requestToken() again afterwards in order
      * to get a new token.
      */
     private void invalidateToken() {
         AccountManager accountManager = AccountManager.get(this);
         accountManager.invalidateAuthToken("com.google",
                 authPreferences.getToken());
 
         authPreferences.setToken(null);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
             if (requestCode == AUTHORIZATION_CODE) {
                 requestToken(false);
             } else if (requestCode == ACCOUNT_CODE) {
                 String accountName = data
                         .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                 authPreferences.setUser(accountName);
 
                 // invalidate old tokens which might be cached. we want a fresh
                 // one, which is guaranteed to work
                 invalidateToken();
 
                 requestToken(false);
             }
         }
     }
 
     private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
 
         private boolean passive = false;
 
         public OnTokenAcquired(boolean passive) {
             this.passive = passive;
         }
 
         @Override
         public void run(AccountManagerFuture<Bundle> result) {
             try {
                 Bundle bundle = result.getResult();
 
                 Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                 if (launch != null) {
                     startActivityForResult(launch, AUTHORIZATION_CODE);
                 } else {
                     String token = bundle
                             .getString(AccountManager.KEY_AUTHTOKEN);
 
                     authPreferences.setToken(token);
                     if (!passive) {
                         Intent intent = new Intent(URLListActivity.this, URLListActivity.class);
                         startActivity(intent);
                         finish();
                     }
                 }
             } catch (Exception e) {
                 Log.e("OnTokenAcquired run", "Failed to acquire token");
             }
         }
     }
 
     private void logout() {
         invalidateToken();
         authPreferences.logout();
         loggedIn = false;
         Intent intent = new Intent(URLListActivity.this, URLListActivity.class);
         startActivity(intent);
         finish();
     }
 
     public void refreshCallback(JSONObject result) {
         if (result == null) {
             Toast.makeText(this, "Error retrieving data", Toast.LENGTH_LONG).show();
             return;
         }
 
         try {
             int totalItems = result.getInt("totalItems");
 
             JSONArray array = result.getJSONArray("items");
             Log.d("array", totalItems + " " + array.toString());
 
             JSONObject tmpobj = array.getJSONObject(0);
             Log.d("object", tmpobj.getString("id"));
             ITEMS.clear();
             for (int i = 0; i < 30; ++i) {
                 ITEMS.add(array.getJSONObject(i).getString("id"));
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         stringAdapter.notifyDataSetChanged();
 
 
         mPullToRefreshAttacher.setRefreshComplete();
     }
 
     private void newURLDialog() {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Shorten New URL");
         alert.setCancelable(true);
 
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
         input.setHint("Type or paste a URL here");
         alert.setView(input);
         alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 String urlToShare = input.getText().toString();
 
                 // Make sure it's not empty
                 if (urlToShare == null || urlToShare.matches("")) {
                     Toast.makeText(getBaseContext(), "Please enter a URL!", Toast.LENGTH_LONG).show();
                 } else if (!Patterns.WEB_URL.matcher(urlToShare).matches()) {
                     // Validate URL pattern
                     Toast.makeText(getBaseContext(), "Please enter a valid URL!", Toast.LENGTH_LONG).show();
                 } else {
                     hideKeyboard(input);
                     // Let's go get that URL!
                     // Trim any trailing spaces (sometimes keyboards will autocorrect .com with a space at the end)
                     generateShortenedURL(urlToShare.trim());
                 }
             }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 hideKeyboard(input);
             }
         });
 
         // hide keyboard if the dialog is dismissed
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     hideKeyboard(input);
                 }
             });
         }
 
         alert.show();
 
         // Show keyboard
         input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 input.post(new Runnable() {
                     @Override
                     public void run() {
                         InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                         imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                     }
                 });
             }
         });
         input.requestFocus();
     }
 
     private void hideKeyboard(EditText input) {
         InputMethodManager imm = (InputMethodManager)getSystemService(
                 Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
     }
 
     private void generateShortenedURL(String input) {
         if (!checkNetwork()) {
             return;
         }
 
         URLShortener shortener = new URLShortener(authPreferences.getToken());
         Log.d("hologoogl", "generating");
         final String resultURL = shortener.generate(input);
         Log.d("hologoogl", "done generating");
 
         Log.d("hologoogl", "Generated " + resultURL);
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(resultURL)
                 .setCancelable(true)
                 .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         shareURL(resultURL);
                     }
                 });
 
         alert.setNegativeButton("Copy", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 copyURL(resultURL);
             }
         });
 
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     if (!loggedIn) {
                         stringAdapter.add(resultURL.substring(7));
                     }
                 }
             });
         }
         alert.show();
 
     }
 
     private void copyURL(String input) {
         ClipboardManager clipboard = (ClipboardManager)
                 getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
         ClipData clip = ClipData.newPlainText("Shortened URL", input);
         clipboard.setPrimaryClip(clip);
         Toast.makeText(getBaseContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
     }
 
     private void shareURL(String input) {
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("text/plain");
         intent.putExtra(Intent.EXTRA_TEXT, input);
         intent.putExtra(Intent.EXTRA_SUBJECT, "Shared from Holo Goo.gl");
         startActivity(Intent.createChooser(intent, "Share"));
     }
 
     private void sendFeedback() {
         Intent gmail = new Intent(Intent.ACTION_VIEW);
         gmail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
         gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "pandanomic@gmail.com" });
         gmail.setData(Uri.parse("pandanomic@gmail.com"));
         gmail.putExtra(Intent.EXTRA_SUBJECT, "Holo Goo.gl Feedback");
         gmail.setType("plain/text");
         startActivity(gmail);
     }
 
     private boolean checkNetwork() {
         ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         boolean airplaneMode = checkAirplaneMode();
         if (!(networkInfo != null && networkInfo.isConnected())) {
             if (airplaneMode) {
                 Toast.makeText(this, "Please disable airplane mode or turn on WiFi first!", Toast.LENGTH_LONG).show();
                 return false;
             }
             Toast.makeText(this, "Could not connect, please check your internet connection.", Toast.LENGTH_LONG).show();
             return false;
         }
 
         return true;
     }
 
     private boolean checkAirplaneMode() {
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             return Settings.System.getInt(getBaseContext().getContentResolver(),
                     Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
         } else {
             return Settings.System.getInt(getBaseContext().getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) != 0;
         }
     }
 
     private void reauthorizeGoogle() {
         // if response is a 401-unauthorized
         Log.i("URLList", "reauthorizing");
         invalidateToken();
         requestToken(true);
     }
 
     private class RefreshListTask extends AsyncTask<String, Void, JSONObject> {
         private String GETURL = "https://www.googleapis.com/urlshortener/v1/url/history";
         private final String LOGTAG = "RefreshHistory";
 
         @Override
         protected void onPreExecute() {
 //            this.progressDialog.show();
         }
 
         @Override
         protected JSONObject doInBackground(String... params) {
             String authToken;
 
             if (!checkNetwork()) {
                 Toast.makeText(getParent(), "Not connected to the internet", Toast.LENGTH_LONG).show();
                 Log.e(LOGTAG, "No connection");
                 return null;
             }
 
             authToken = params[0];
 
             JSONObject results;
 
             Log.d(LOGTAG, "Fetching data");
             try {
                 HttpParams httpParams = new BasicHttpParams();
                 HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                 HttpConnectionParams.setSoTimeout(httpParams, 5000);
 
                 DefaultHttpClient client = new DefaultHttpClient(httpParams);
                 HttpGet get = new HttpGet(GETURL);
 
                 get.setHeader("Authorization", "Bearer " + authToken);
 
                 Log.d(LOGTAG, "Requesting");
                 HttpResponse response = client.execute(get);
 
                 if (response.getStatusLine().getStatusCode() == 404) {
                     Log.e(LOGTAG, "404 Not found");
                     return null;
                 }
 
                 Log.d(LOGTAG, "Parsing response");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                 StringBuilder builder = new StringBuilder();
                 for (String line; (line = reader.readLine()) != null;) {
                     builder.append(line).append("\n");
                 }
 
                 results = new JSONObject(new JSONTokener(builder.toString()));
                 Log.d(LOGTAG, "Finished parsing");
             } catch (Exception e) {
                 e.printStackTrace();
                 String errorMessage = "Error: ";
                 if (e instanceof UnsupportedEncodingException) {
                     errorMessage += "Encoding exception";
                 } else if (e instanceof ClientProtocolException) {
                     errorMessage += "POST exception";
                 } else if (e instanceof IOException) {
                     errorMessage += "IO Exception in parsing response";
                 } else {
                     errorMessage += "JSON parsing exception";
                 }
 
                 Log.e("history", errorMessage);
                 return null;
             }
             return results;
         }
 
         /**
          * Post-execution stuff
          * @param result JSONObject result received from Goo.gl
          */
         protected void onPostExecute(JSONObject result) {
             refreshCallback(result);
         }
     }
 
     private void metricsDialog(URLMetrics metrics) {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Analytics")
                 .setCancelable(true)
                 .setNeutralButton("Done", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                     }
                 })
                 .setMessage(Html.fromHtml("<b><u> Short URL </u></b><br>" + metrics.getShortURL()
                                 + "<br><br><b><u>Long URL </u></b><br>" + metrics.getLongURL()
                                 + "<br><br><b><u>Clicks </u></b><br>" + metrics.getClicks()));
 
         alert.show();
 //        MetricsDialogFragment dialog = new MetricsDialogFragment();
 //        dialog.show(getFragmentManager().beginTransaction(), "changelogdemo");
     }
 
     private class refreshMetrics extends AsyncTask<String, Void, URLMetrics> {
         private final String LOGTAG = "RefreshHistory";
         private String GETURL = "https://www.googleapis.com/urlshortener/v1/url";
         @Override
         protected void onPreExecute() {
             mPullToRefreshAttacher.setRefreshing(true);
             DefaultHeaderTransformer ht = (DefaultHeaderTransformer) mPullToRefreshAttacher.getHeaderTransformer();
             ht.setRefreshingText("Retrieving analytics");
         }
 
         @Override
         protected URLMetrics doInBackground(String... params) {
             String shortenedURL = params[0];
             GETURL += "?shortUrl=" + shortenedURL;
             GETURL += "&projection=FULL";
 
             JSONObject results;
 
             Log.d("googl", "Fetching data");
             try {
                 HttpParams httpParams = new BasicHttpParams();
                 HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                 HttpConnectionParams.setSoTimeout(httpParams, 5000);
 
                 DefaultHttpClient client = new DefaultHttpClient(httpParams);
                 HttpGet get = new HttpGet(GETURL);
 
                 HttpResponse response = client.execute(get);
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                 StringBuilder builder = new StringBuilder();
                 for (String line; (line = reader.readLine()) != null;) {
                     builder.append(line).append("\n");
                 }
 
                 results = new JSONObject(new JSONTokener(builder.toString()));
                 Log.d("hologoogl", results.toString());
             } catch (Exception e) {
                 e.printStackTrace();
                 String errorMessage = "Error: ";
                 if (e instanceof UnsupportedEncodingException) {
                     errorMessage += "Encoding exception";
                 } else if (e instanceof ClientProtocolException) {
                     errorMessage += "POST exception";
                 } else if (e instanceof IOException) {
                     errorMessage += "IO Exception in parsing response";
                 } else {
                     errorMessage += "JSON parsing exception";
                 }
 
                 Log.e("googl:retrieveURLTask", errorMessage);
                 return null;
             }
 
             URLMetrics metrics = new URLMetrics(shortenedURL);
             try {
                 metrics.setLongURL(results.getString("longUrl"));
                 JSONObject analytics = results.getJSONObject("analytics");
                 JSONObject allTime = analytics.getJSONObject("allTime");
                 metrics.setClicks(allTime.getInt("shortUrlClicks"));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
             return metrics;
         }
 
         /**
          * Post-execution stuff
          * @param result JSONObject result received from Goo.gl
          */
         protected void onPostExecute(URLMetrics result) {
             mPullToRefreshAttacher.setRefreshComplete();
             metricsDialog(result);
         }
     }
 }
