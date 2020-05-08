 package org.gots.ui;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.gots.R;
 import org.gots.garden.provider.nuxeo.NuxeoGardenProvider;
 import org.gots.preferences.GotsPreferences;
 import org.nuxeo.ecm.automation.client.jaxrs.Constants;
 import org.nuxeo.ecm.automation.client.jaxrs.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
 import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.TokenRequestInterceptor;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.util.Base64;
 //import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 
 public class LoginActivity extends AbstractActivity {
     private TextView loginText;
 
     private TextView passwordText;
 
     // String myToken = GotsPreferences.getInstance(this).getToken();
     // String myLogin = GotsPreferences.getInstance(this).getNUXEO_LOGIN();
     // String myPassword =
     // GotsPreferences.getInstance(this).getNUXEO_PASSWORD();
     // String myDeviceId = GotsPreferences.getInstance(this).getDeviceId();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         ActionBar bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
         bar.setTitle(R.string.app_name);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         loginText = (TextView) findViewById(R.id.edittextLogin);
         loginText.setText(GotsPreferences.getInstance(this).getNuxeoLogin());
         passwordText = (TextView) findViewById(R.id.edittextPassword);
         passwordText.setText(GotsPreferences.getInstance(this).getNuxeoPassword());
 
         LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.idLayoutConnection);
         buttonLayout.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Toast.makeText(LoginActivity.this,
                         getResources().getString(R.string.feature_unavalaible),
                         Toast.LENGTH_SHORT).show();
 
                 // launchGoogle();
                 // tokenNuxeoConnect();
 
                 // finish();
 
             }
 
         });
 
         Button connect = (Button) findViewById(R.id.buttonConnect);
         connect.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String login = loginText.getText().toString();
                 String password = passwordText.getText().toString();
 
                 if ("".equals(login) || "".equals(password))
                     Toast.makeText(
                             LoginActivity.this,
                             getResources().getString(
                                     R.string.login_missinginformation),
                             Toast.LENGTH_SHORT).show();
                 else {
                     basicNuxeoConnect(login, password);
                     //TODO test if connexion is OK, then finish, else ask for login modification
 //                    new NuxeoGardenProvider(LoginActivity.this);
                     finish();
 
                 }
 
             }
 
             protected void basicNuxeoConnect(String login, String password) {
                 String device_id = getDeviceID();
                 GotsPreferences.getInstance(LoginActivity.this).setDeviceId(
                         device_id);
 
                 String token = request_basicauth_token(false);
                 if (token == null) {
                     Toast.makeText(LoginActivity.this, "Error logging",
                             Toast.LENGTH_SHORT).show();
                 } else {
                     GotsPreferences.getInstance(LoginActivity.this).setToken(
                             token);
 
                     GotsPreferences.getInstance(LoginActivity.this).setNuxeoLogin(
                             login);
                     GotsPreferences.getInstance(LoginActivity.this).setNuxeoPassword(
                             password);
                     GotsPreferences.getInstance(LoginActivity.this).setConnectedToServer(
                             true);
                 }
             }
 
         });
 
     }
 
     protected String getDeviceID() {
         String device_id = Secure.getString(getContentResolver(),
                 Secure.ANDROID_ID);
         return device_id;
     }
 
     protected void tokenNuxeoConnect() {
         String device_id = getDeviceID();
         GotsPreferences.getInstance(LoginActivity.this).setDeviceId(device_id);
 
         String tmp_token = request_temporaryauth_token(false);
         if (tmp_token == null) {
             Toast.makeText(LoginActivity.this, "Authentication ",
                     Toast.LENGTH_SHORT).show();
         } else {
             Toast.makeText(LoginActivity.this, tmp_token, Toast.LENGTH_SHORT).show();
         }
     }
 
     public String request_temporaryauth_token(boolean revoke) {
 
         AsyncTask<Object, Void, String> task = new AsyncTask<Object, Void, String>() {
             String token = null;
 
             @Override
             protected String doInBackground(Object... objects) {
                 try {
                     String email = "toto.tata@gmail.com";
                     HttpAutomationClient client = new HttpAutomationClient(
                            GotsPreferences.getGardeningManagerServerURI()+"/site/automation");
                     client.setRequestInterceptor(new TokenRequestInterceptor(
                             "myApp",
                             "myToken",
                             "myLogin",
                             GotsPreferences.getInstance(LoginActivity.this).getDeviceId()));
 
                     Session session = client.getSession();
 
                     Documents docs = (Documents) session.newRequest(
                             "Document.Email").setHeader(
                             Constants.HEADER_NX_SCHEMAS, "*").set("email",
                             email).execute();
 
                     // String uri =
                     // GotsPreferences.getInstance(getApplicationContext())
                     // .getGardeningManagerNuxeoAuthentication();
                     //
                     // List<NameValuePair> params = new
                     // LinkedList<NameValuePair>();
                     // params.add(new BasicNameValuePair("deviceId",
                     // GotsPreferences.getInstance(getApplicationContext())
                     // .getDeviceId()));
                     // params.add(new BasicNameValuePair("applicationName",
                     // GotsPreferences.getInstance(
                     // getApplicationContext()).getGardeningManagerAppname()));
                     // params.add(new BasicNameValuePair("deviceDescription",
                     // Build.MODEL + "(" + Build.MANUFACTURER +
                     // ")"));
                     // params.add(new BasicNameValuePair("permission",
                     // "ReadWrite"));
                     // params.add(new BasicNameValuePair("revoke", "false"));
                     //
                     // String paramString = URLEncodedUtils.format(params,
                     // "utf-8");
                     // uri += paramString;
                     // URL url = new URL(uri);
                     //
                     // URLConnection urlConnection;
                     // urlConnection = url.openConnection();
                     //
                     // urlConnection.addRequestProperty("X-User-Id",
                     // loginText.getText().toString());
                     // urlConnection.addRequestProperty("X-Device-Id",
                     // GotsPreferences
                     // .getInstance(getApplicationContext()).getDeviceId());
                     // urlConnection.addRequestProperty("X-Application-Name",
                     // GotsPreferences.getInstance(getApplicationContext()).getGardeningManagerAppname());
                     // urlConnection.addRequestProperty(
                     // "Authorization",
                     // "Basic "
                     // + Base64.encodeToString((loginText.getText().toString() +
                     // ":" + passwordText
                     // .getText().toString()).getBytes(), Base64.NO_WRAP));
 
                     // urlConnection.addRequestProperty(
                     // "Authorization",
                     // "Basic "
                     // + Base64.encodeBase64((loginText.getText().toString() +
                     // ":" + passwordText.getText()
                     // .toString()).getBytes()));
 
                     // InputStream in = new
                     // BufferedInputStream(urlConnection.getInputStream());
                     // try {
                     // // readStream(in);
                     // StringBuilder builder = new StringBuilder();
                     // String line;
                     // BufferedReader reader = new BufferedReader(new
                     // InputStreamReader(in, "UTF-8"));
                     // while ((line = reader.readLine()) != null) {
                     // builder.append(line);
                     // }
                     //
                     // token = builder.toString();
                     // Log.d("LoginActivity", "Token acquired: " + token);
                     //
                     // } finally {
                     // in.close();
                     // }
                 } catch (IOException e) {
                     Log.e("LoginActivity", e.getMessage(), e);
                     return null;
 
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 return token;
             }
         }.execute(new Object());
         String tokenAcquired = null;
         try {
             tokenAcquired = task.get();
         } catch (InterruptedException e) {
             Log.e("LoginActivity", e.getMessage(), e);
         } catch (ExecutionException e) {
             Log.e("LoginActivity", e.getMessage(), e);
         }
         return tokenAcquired;
 
     }
 
     public String request_basicauth_token(boolean revoke) {
 
         AsyncTask<Object, Void, String> task = new AsyncTask<Object, Void, String>() {
             String token = null;
 
             @Override
             protected String doInBackground(Object... objects) {
                 try {
                     String uri = GotsPreferences.getInstance(
                             getApplicationContext()).getGardeningManagerNuxeoAuthentication();
 
                     List<NameValuePair> params = new LinkedList<NameValuePair>();
                     params.add(new BasicNameValuePair(
                             "deviceId",
                             GotsPreferences.getInstance(getApplicationContext()).getDeviceId()));
                     params.add(new BasicNameValuePair(
                             "applicationName",
                             GotsPreferences.getInstance(getApplicationContext()).getGardeningManagerAppname()));
                     params.add(new BasicNameValuePair("deviceDescription",
                             Build.MODEL + "(" + Build.MANUFACTURER + ")"));
                     params.add(new BasicNameValuePair("permission", "ReadWrite"));
                     params.add(new BasicNameValuePair("revoke", "false"));
 
                     String paramString = URLEncodedUtils.format(params, "utf-8");
                     uri += paramString;
                     URL url = new URL(uri);
 
                     URLConnection urlConnection;
                     urlConnection = url.openConnection();
 
                     urlConnection.addRequestProperty("X-User-Id",
                             loginText.getText().toString());
                     urlConnection.addRequestProperty(
                             "X-Device-Id",
                             GotsPreferences.getInstance(getApplicationContext()).getDeviceId());
                     urlConnection.addRequestProperty(
                             "X-Application-Name",
                             GotsPreferences.getInstance(getApplicationContext()).getGardeningManagerAppname());
                     urlConnection.addRequestProperty(
                             "Authorization",
                             "Basic "
                                     + Base64.encodeToString(
                                             (loginText.getText().toString()
                                                     + ":" + passwordText.getText().toString()).getBytes(),
                                             Base64.NO_WRAP));
 
                     // urlConnection.addRequestProperty(
                     // "Authorization",
                     // "Basic "
                     // + Base64.encodeBase64((loginText.getText().toString() +
                     // ":" + passwordText.getText()
                     // .toString()).getBytes()));
 
                     InputStream in = new BufferedInputStream(
                             urlConnection.getInputStream());
                     try {
                         // readStream(in);
                         StringBuilder builder = new StringBuilder();
                         String line;
                         BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(in, "UTF-8"));
                         while ((line = reader.readLine()) != null) {
                             builder.append(line);
                         }
 
                         token = builder.toString();
                         Log.d("LoginActivity", "Token acquired: " + token);
 
                     } finally {
                         in.close();
                     }
                 } catch (IOException e) {
                     Log.e("LoginActivity", e.getMessage(), e);
                     return null;
 
                 }
                 return token;
             }
         }.execute(new Object());
         String tokenAcquired = null;
         try {
             tokenAcquired = task.get();
         } catch (InterruptedException e) {
             Log.e("LoginActivity", e.getMessage(), e);
         } catch (ExecutionException e) {
             Log.e("LoginActivity", e.getMessage(), e);
         }
         return tokenAcquired;
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
         case android.R.id.home:
             finish();
             return true;
 
             // case R.id.help:
             // Intent browserIntent = new Intent(Intent.ACTION_VIEW,
             // Uri.parse(HelpUriBuilder.getUri(getClass()
             // .getSimpleName())));
             // startActivity(browserIntent);
             //
             // return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     void launchGoogle() {
         // // if (isChecked) {
         // // loginBox.setVisibility(View.VISIBLE);
         // //
         // // // Create an instance of SocialAuthConfgi object
         // SocialAuthConfig config = SocialAuthConfig.getDefault();
         // //
         // // // load configuration. By default load the configuration
         // // // from oauth_consumer.properties.
         // // // You can also pass input stream, properties object or
         // // // properties file name.
         // try {
         // config.load();
         //
         // // Create an instance of SocialAuthManager and set
         // // config
         // SocialAuthManager manager = new SocialAuthManager();
         // manager.setSocialAuthConfig(config);
         //
         // // URL of YOUR application which will be called after
         // // authentication
         // String successUrl =
         // "http://srv2.gardening-manager.com:8090/nuxeo/nxstartup.faces?provider=GoogleOpenIDConnect";
         //
         // // get Provider URL to which you should redirect for
         // // authentication.
         // // id can have values "facebook", "twitter", "yahoo"
         // // etc. or the OpenID URL
         // String url = manager.getAuthenticationUrl("google",
         // successUrl);
         //
         // // Store in session
         // // session.setAttribute("authManager", manager);
         // } catch (Exception e) {
         // // TODO Auto-generated catch block
         // e.printStackTrace();
         // }
     }
 
 }
