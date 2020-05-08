 package org.gots.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gots.R;
 import org.gots.authentication.GoogleAuthentication;
 import org.gots.authentication.NuxeoAuthentication;
 import org.gots.broadcast.BroadCastMessages;
 import org.gots.preferences.GotsPreferences;
 import org.nuxeo.ecm.automation.client.jaxrs.RemoteException;
 import org.nuxeo.ecm.automation.client.jaxrs.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.impl.NotAvailableOffline;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 import com.google.android.gms.auth.GoogleAuthException;
 import com.google.android.gms.auth.UserRecoverableAuthException;
 
 public class LoginActivity extends AbstractActivity {
     protected static final String TAG = "LoginActivity";
 
     private Spinner loginSpinner;
 
     private TextView passwordText;
 
     private ActionBar bar;
 
     private Menu mMenu;
 
     private NuxeoAuthentication nuxeoAuthentication;
 
     protected int AUTHTOKEN_CODE_RESULT = 1;
 
     Account selectedAccount = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         this.registerReceiver(seedBroadcastReceiver, new IntentFilter(BroadCastMessages.CONNECTION_SETTINGS_CHANGED));
 
         bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
         bar.setTitle(R.string.app_name);
         nuxeoAuthentication = new NuxeoAuthentication(this);
 
         // credential = GoogleAccountCredential.usingOAuth2(context, scopes)
     }
 
     public BroadcastReceiver seedBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (BroadCastMessages.CONNECTION_SETTINGS_CHANGED.equals(intent.getAction())) {
                 refreshConnectionState();
             }
         }
 
     };
 
     protected void refreshConnectionState() {
         if (mMenu == null)
             return;
         MenuItem connectionItem = mMenu.findItem(R.id.connection);
         if (gotsPrefs.isConnectedToServer()) {
             connectionItem.setIcon(getResources().getDrawable(R.drawable.garden_connected));
         } else {
             connectionItem.setIcon(getResources().getDrawable(R.drawable.garden_disconnected));
 
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // hide keyboard
         InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         if (this.getCurrentFocus() != null) {
             inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                     InputMethodManager.HIDE_NOT_ALWAYS);
         }
 
         if (gotsPrefs.isConnectedToServer()) {
             buildLayoutConnected();
             return;
         }
 
         buildLayoutDisconnected();
 
     }
 
     @Override
     protected void onDestroy() {
         unregisterReceiver(seedBroadcastReceiver);
         super.onDestroy();
 
     }
 
     public List<String> getAccounts(String account_type) {
         AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
         Account[] accounts = manager.getAccounts();
         List<String> accountString = new ArrayList<String>();
         for (int i = 0; i < accounts.length; i++) {
             if (accounts[i].type.equals(account_type))
                 accountString.add(accounts[i].name);
         }
 
         return accountString;
     }
 
     protected void buildLayoutDisconnected() {
 
         if (GotsPreferences.isDevelopment()) {
             findViewById(R.id.tableDebug).setVisibility(View.VISIBLE);
 
         }
         loginSpinner = (Spinner) findViewById(R.id.spinnerLogin);
         ArrayAdapter<String> account_name_adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, getAccounts("com.google"));
         loginSpinner.setAdapter(account_name_adapter);
         passwordText = (TextView) findViewById(R.id.edittextPassword);
         passwordText.setText(gotsPrefs.getNuxeoPassword());
 
         LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.idLayoutOAuth2);
         buttonLayout.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // Toast.makeText(LoginActivity.this, getResources().getString(R.string.feature_unavalaible),
                 // Toast.LENGTH_SHORT).show();
                 // GoogleAnalyticsTracker.getInstance().trackEvent("Login", "GoogleAuthentication",
                 // "Request this new feature", 0);
 
                 selectAccount();
                 // tokenNuxeoConnect();
 
                 // finish();
 
             }
 
         });
 
         Button buttoncreate = (Button) findViewById(R.id.buttonCreate);
         buttoncreate.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 GoogleAnalyticsTracker.getInstance().trackEvent("Authentication", "Login", "Request account", 0);
                 sendEmail();
             }
 
         });
 
         Button connect = (Button) findViewById(R.id.buttonConnect);
         connect.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 connect();
 
             }
 
         });
     }
 
     protected void sendEmail() {
         // send mail
         Intent i = new Intent(Intent.ACTION_SEND);
         i.setType("message/rfc822");
         i.putExtra(Intent.EXTRA_EMAIL, new String[] { "account@gardening-manager.com" });
         i.putExtra(Intent.EXTRA_SUBJECT, "Gardening Manager / Account / Ask for new account");
         i.putExtra(Intent.EXTRA_TEXT,
                 "Hello,\n\nI want to participate to the Gardening Manager beta version.\n\nMy Google account is: "
                         + loginSpinner.getSelectedItem().toString()
                         + "\n\nI know I will receive my password quickly.\n\n");
         try {
             startActivity(Intent.createChooser(i, "Send mail..."));
         } catch (android.content.ActivityNotFoundException ex) {
             Toast.makeText(LoginActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
         }
     }
 
     protected void buildLayoutConnected() {
         findViewById(R.id.layoutConnect).setVisibility(View.GONE);
         View disconnectLayout = findViewById(R.id.layoutDisconnect);
         disconnectLayout.setVisibility(View.VISIBLE);
 
         TextView text = (TextView) findViewById(R.id.idConnectedDescription);
         String textContent = String.valueOf(text.getText());
         text.setText(textContent.replace("_ACCOUNT_", gotsPrefs.getNuxeoLogin()));
 
         Button buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
         buttonDisconnect.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 disconnect();
             }
 
         });
     }
 
     protected void connect() {
         new AsyncTask<Void, Integer, Session>() {
             private ProgressDialog dialog;
 
             private String login;
 
             private String password;
 
             @Override
             protected void onPreExecute() {
 
                 login = String.valueOf(loginSpinner.getSelectedItem());
                 if (GotsPreferences.isDevelopment()) {
                     EditText logindebug = (EditText) findViewById(R.id.edittextLoginDebug);
                     login = logindebug.getText().toString();
                 }
                 password = passwordText.getText().toString();
 
                 if ("".equals(login) || "".equals(password)) {
                     Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_missinginformation),
                             Toast.LENGTH_SHORT).show();
                     cancel(true);
                 } else {
                     dialog = ProgressDialog.show(LoginActivity.this, "",
                             getResources().getString(R.string.gots_loading), true);
                     dialog.setCanceledOnTouchOutside(true);
                 }
             };
 
             @Override
             protected Session doInBackground(Void... params) {
                 Session session = null;
                 try {
                     if (nuxeoAuthentication.basicNuxeoConnect(login, password)) {
 
                         nuxeoManager.shutdown();
                         session = nuxeoManager.getSession();
 
                         if ("Guest".equals(session.getLogin().getUsername())) {
                             return null;
                         }
                     } else
                         cancel(true);
                 } catch (IOException e) {
                     Log.e(TAG, e.getMessage(), e);
                 } catch (NotAvailableOffline e) {
                     Log.e(TAG, e.getMessage(), e);
                 } catch (RemoteException e) {
                     Log.e(TAG, e.getMessage(), e);
                 }
                 return session;
             }
 
             @Override
             protected void onPostExecute(Session result) {
                 if (dialog != null && dialog.isShowing())
                     dialog.dismiss();
                 if (result == null) {
                     Toast.makeText(LoginActivity.this, "Error logging", Toast.LENGTH_SHORT).show();
                     LoginActivity.this.findViewById(R.id.textConnectError).setVisibility(View.VISIBLE);
                     gotsPrefs.setConnectedToServer(false);
                     gotsPrefs.setNuxeoLogin(null);
                     gotsPrefs.setLastSuccessfulNuxeoLogin(null);
                     GoogleAnalyticsTracker.getInstance().trackEvent("Authentication", "Login", "Failure", 0);
 
                 } else {
                     LoginActivity.this.findViewById(R.id.textConnectError).setVisibility(View.GONE);
                     gotsPrefs.setConnectedToServer(true);
                     gotsPrefs.setLastSuccessfulNuxeoLogin(login);
                     gardenManager.getMyGardens(true);
                     GoogleAnalyticsTracker.getInstance().trackEvent("Authentication", "Login", "Success", 0);
                 }
 
                 onResume();
             };
 
             @Override
             protected void onCancelled(Session result) {
                 if (dialog != null && dialog.isShowing())
                     dialog.dismiss();
             }
 
         }.execute();
         onResume();
     }
 
     protected void disconnect() {
         gotsPrefs.setNuxeoLogin(null);
         gotsPrefs.setNuxeoPassword("");
         gotsPrefs.setConnectedToServer(false);
         findViewById(R.id.layoutConnect).setVisibility(View.VISIBLE);
         findViewById(R.id.layoutDisconnect).setVisibility(View.GONE);
         onResume();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
         case android.R.id.home:
             finish();
             return true;
         case R.id.help:
             Intent browserIntent = new Intent(this, WebHelpActivity.class);
             browserIntent.putExtra(WebHelpActivity.URL, getClass().getSimpleName());
             startActivity(browserIntent);
             return true;
         case R.id.connection:
             if (gotsPrefs.isConnectedToServer())
                 disconnect();
             else
                 connect();
             return true;
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (AUTHTOKEN_CODE_RESULT == requestCode) {
             if (resultCode == Activity.RESULT_OK)
                 requestOAuth2Token(selectedAccount);
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     protected void requestOAuth2Token(final Account account) {
         new AsyncTask<String, Integer, String>() {
 
             @Override
             protected String doInBackground(String... params) {
 
                 GoogleAuthentication authentication = new GoogleAuthentication(getApplicationContext());
                 String googleToken = null;
                 String nuxeoToken = null;
                 String accountName = params[0];
                 try {
                     googleToken = authentication.getToken(accountName);
                     if (googleToken != null) {
                         NuxeoAuthentication nuxeoAuthentication = new NuxeoAuthentication(getApplicationContext());
                         nuxeoToken = nuxeoAuthentication.request_oauth2_token(googleToken);
                     }
                 } catch (UserRecoverableAuthException e) {
                     startActivityForResult(e.getIntent(), AUTHTOKEN_CODE_RESULT);
                 } catch (IOException e) {
                     Log.e(TAG, e.getMessage(), e);
                 } catch (GoogleAuthException e) {
                     Log.e(TAG, e.getMessage(), e);
                 }
                 return nuxeoToken;
             }
 
             @Override
             protected void onPostExecute(String resultToken) {
                 if (resultToken != null) {
                     Toast.makeText(LoginActivity.this, resultToken, Toast.LENGTH_SHORT).show();
                     gotsPrefs.setNuxeoLogin(account.name);
                     gotsPrefs.setToken(resultToken);
                     gotsPrefs.setConnectedToServer(true);
                     onResume();
                 } else {
                     Toast.makeText(LoginActivity.this, "Error requesting GoogleAuthUtil.getToken", Toast.LENGTH_SHORT).show();
                 }
                 super.onPostExecute(resultToken);
             }
         }.execute(account.name);
     }
 
     void selectAccount() {
         AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
         Account[] accounts = manager.getAccounts();
         final List<Account> usableAccounts = new ArrayList<Account>();
         List<String> items = new ArrayList<String>();
         for (Account account : accounts) {
             if ("com.google".equals(account.type)) {
                 usableAccounts.add(account);
                 items.add(String.format("%s (%s)", account.name, account.type));
             }
         }
         if (usableAccounts.size() > 1)
             new AlertDialog.Builder(this).setTitle("Action").setItems(items.toArray(new String[items.size()]),
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int item) {
                             selectedAccount = usableAccounts.get(item);
                             requestOAuth2Token(selectedAccount);
                         }
 
                     }).show();
        else if (usableAccounts.size() == 1 && usableAccounts.get(0) != null) {
             selectedAccount = usableAccounts.get(0);
             requestOAuth2Token(usableAccounts.get(0));
         }
 
     }
 
     // protected void getRefreshAccessToken(String token) {
     // new AsyncTask<String, Void, String>() {
     // @Override
     // protected String doInBackground(String... params) {
     // try {
     // HttpClient httpclient = new DefaultHttpClient();
     // HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/token");
     // httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
     // // Add your data
     // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     // nameValuePairs.add(new BasicNameValuePair("code", params[0]));
     // nameValuePairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
     // nameValuePairs.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
     // nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
     //
     // UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
     // httppost.setEntity(entity);
     // HttpResponse response = httpclient.execute(httppost);
     // StatusLine serverCode = response.getStatusLine();
     // int code = serverCode.getStatusCode();
     // if (code == 200) {
     // InputStream is = response.getEntity().getContent();
     // JSONArray jsonArray = new JSONArray(convertStreamToString(is));
     // String refreshToken = (String) jsonArray.opt(4);
     // String accessToken = (String) jsonArray.opt(0);
     // return accessToken;
     // // bad token, invalidate and get a new one
     // } else if (code == 401) {
     // GoogleAuthUtil.invalidateToken(LoginActivity.this, params[0]);
     // Log.e(TAG, "Server auth error: " + response.getStatusLine());
     // return null;
     // // unknown error, do something else
     // } else {
     // InputStream is = response.getEntity().getContent();
     // String error = convertStreamToString(is);
     // Log.e("Server returned the following error code: " + serverCode, "");
     // return null;
     // }
     // } catch (MalformedURLException e) {
     // } catch (IOException e) {
     // } catch (JSONException e) {
     // } finally {
     // }
     // return null;
     // }
     //
     // @Override
     // protected void onPostExecute(String accessToken) {
     // Log.d("AccessToken", " " + accessToken);
     // }
     // }.execute(token);
     // }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu_login, menu);
         mMenu = menu;
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         refreshConnectionState();
         return super.onPrepareOptionsMenu(menu);
     }
 }
