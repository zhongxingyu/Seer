 package com.rxp.transactionmonitor;
 
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

 import net.oauth.OAuth;
 import net.oauth.OAuthAccessor;
 import net.oauth.OAuthConsumer;
 import net.oauth.OAuthException;
 import net.oauth.OAuthMessage;
 import net.oauth.OAuthProblemException;
 import net.oauth.OAuthServiceProvider;
 import net.oauth.client.OAuthClient;
 import net.oauth.client.httpclient4.HttpClient4;
 
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

 import com.rxp.realcontrol.api.Client;
 import com.rxp.realcontrol.api.ClientAccounts;
 
 public class Main extends Activity {
 	private static final String requestToken = "https://api.realexpayments.com/IPS-Reporting/oauth/request_token";
 	private static final String authorize = "https://api.realexpayments.com/IPS-Reporting/oauth/confirm_access";
 	private static final String accessToken = "https://api.realexpayments.com/IPS-Reporting/oauth/access_token";
 
 	// private static final String requestToken = "http://payb.in/oauth/request_token";
 	// private static final String authorize = "http://payb.in/oauth/confirm_access";
 	// private static final String accessToken = "http://payb.in/oauth/access_token";
 
 	SharedPreferences preferences;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		String access_token = preferences.getString("access_token", "");
 		String access_secret = preferences.getString("access_secret", "");
 
 		Log.d("TMS", "In onCreate: " + access_token + "/" + access_secret);
 
 		Uri uri = this.getIntent().getData();
 		if (uri != null) {
 			Log.d("TMS", "In onCreate - Coming back from the authentication: " + uri.toString());
 
 			String authorised_request_token = uri.getQueryParameter("oauth_token");
 			String verifier = uri.getQueryParameter("oauth_verifier");
 
 			Log.d("TMS", "Blessed Request Credentials: " + authorised_request_token + "/" + verifier);
 			new SwapForAccessTokenTask().execute(verifier);
 
 		} else if (access_token.equals("")) {
 			Log.d("TMS", "Don't have any stored creds - going to get some.");
 			new RetrieveRequestTokenTask().execute("");
 
 		} else {
 			Log.d("TMS", "Got stored creds - using them.");
 			OAuthAccessor accessor = defaultAccessor();
 
 			accessor.accessToken = access_token;
 			accessor.tokenSecret = access_secret;
 
 			if (new ClientTask().execute("") != null) {
 				// null would mean we had an invalid token - don't continue.
 				new TransactionsTask().execute("");
 			}
 		}
 
 		/*
 		 * GCMRegistrar.checkDevice(this); GCMRegistrar.checkManifest(this); final String regId = GCMRegistrar.getRegistrationId(this); if
 		 * (regId.equals("")) { GCMRegistrar.register(this, "541821719407"); } else { Log.v("Realex Transaction Monitor", "Already registered"); }
 		 */
 
 	}
 
 	public void onResume() {
 		super.onResume();
 		Log.d("TMS", "in onResume");
 		// extract the OAUTH access token if it exists
 		// Uri uri = this.getIntent().getData();
 		// if (uri != null) {
 		// Log.d("TMS", "Coming back from the authentication: " + uri.toString());
 		//
 		// String authorised_request_token = uri.getQueryParameter("oauth_token");
 		// String verifier = uri.getQueryParameter("oauth_verifier");
 		//
 		// Log.d("TMS", "Blessed Request Credentials: " + authorised_request_token + "/" + verifier);
 		// new SwapForAccessTokenTask().execute(authorised_request_token, verifier);
 		//
 		// }
 
 	}
 
 	public OAuthServiceProvider defaultProvider() {
 		OAuthServiceProvider provider = new OAuthServiceProvider(requestToken, authorize, accessToken);
 		return provider;
 	}
 
 	public OAuthAccessor defaultAccessor() {
 		String callbackUrl = "realex-android-app:///";
 		OAuthServiceProvider provider = defaultProvider();
 		// OAuthConsumer consumer = new OAuthConsumer(callbackUrl, "tonr-consumer-key", "SHHHHH!!!!!!!!!!", provider);
 		OAuthConsumer consumer = new OAuthConsumer(callbackUrl, "realcontrol", "mysecret", provider);
 		OAuthAccessor accessor = new OAuthAccessor(consumer);
 
 		String access_token = preferences.getString("access_token", "");
 		String access_secret = preferences.getString("access_secret", "");
 		if (!access_token.equals("")) {
 			Log.d("TMS", "Populating accessor with stored cred...");
 			accessor.accessToken = access_token;
 			accessor.tokenSecret = access_secret;
 		}
 
 		return accessor;
 	}
 
 	class RetrieveRequestTokenTask extends AsyncTask<String, Void, Void> {
 
 		private Exception exception;
 
 		protected Void doInBackground(String... urls) {
 			OAuthAccessor accessor = defaultAccessor();
 
 			OAuthClient oclient = new OAuthClient(new HttpClient4());
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			List<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
 			params.add(new OAuth.Parameter("oauth_callback", accessor.consumer.callbackURL));
 
 			try {
 				Log.d("TMS", "In RetrieveRequestTokenTask: " + accessor.requestToken + "/" + accessor.tokenSecret);
 
 				oclient.getRequestToken(accessor, "POST", params);
 
 				Editor edit = preferences.edit();
 				edit.putString("request_token", accessor.requestToken);
 				edit.putString("request_secret", accessor.tokenSecret);
 				edit.commit();
 
 				Log.d("TMS", "In RetrieveRequestTokenTask: " + accessor.requestToken + "/" + accessor.tokenSecret);
 				intent.setData(Uri.parse(accessor.consumer.serviceProvider.userAuthorizationURL + "?oauth_token=" + accessor.requestToken));
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OAuthException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			startActivity(intent);
 			return null;
 		}
 
 		protected void onPostExecute() {
 			// TODO: check this.exception
 			// TODO: do something with the feed
 		}
 	}
 
 	class SwapForAccessTokenTask extends AsyncTask<String, Void, Void> {
 
 		private Exception exception;
 
 		protected Void doInBackground(String... credentials) {
 			OAuthClient oclient = new OAuthClient(new HttpClient4());
 			OAuthAccessor accessor = defaultAccessor();
 
 			String request_token = preferences.getString("request_token", "");
 			String request_secret = preferences.getString("request_secret", "");
 			accessor.requestToken = request_token;
 			accessor.tokenSecret = request_secret;
 
 			Log.d("TMS", "Before SwapForAccessTokenTask: " + accessor.requestToken + "/" + accessor.tokenSecret);
 
 			List<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
 			params.add(new OAuth.Parameter("oauth_verifier", credentials[0]));
 
 			try {
 				oclient.getAccessToken(accessor, "POST", params);
 
 				Log.d("TMS", "After SwapForAccessTokenTask: " + accessor.accessToken + "/" + accessor.tokenSecret);
 
 				Editor edit = preferences.edit();
 				edit.putString("access_token", accessor.accessToken);
 				edit.putString("access_secret", accessor.tokenSecret);
 				edit.commit();
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			new ClientTask().execute("");
 
 			return null;
 
 		}
 
 		protected void onPostExecute() {
 			// TODO: check this.exception
 			// TODO: do something with the feed
 		}
 	}
 
 	class ClientTask extends AsyncTask<String, Void, String> {
 
 		private Exception exception;
 
 		protected String doInBackground(String... urls) {
 			OAuthClient oclient = new OAuthClient(new HttpClient4());
 			OAuthAccessor accessor = defaultAccessor();
 			// Log.d("TMS", "ClientTask: "+ accessor.accessToken + "/" + accessor.tokenSecret);
 
 			try {
 				OAuthMessage omessage = oclient.invoke(accessor, "GET", "https://api.realexpayments.com/IPS-Reporting/api/v1.0/~/client", null);
 
 				String xml = omessage.readBodyAsString(); // can only read once
 				Log.d("TMS", "Client: " + xml);
 
 				return xml;
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OAuthProblemException e) {
 				Log.w("TMS", "Credentials no longer valid - reauthenticate and authorise...");
 				Editor edit = preferences.edit();
 				edit.clear(); // wipe the stored credentials
 				edit.commit();
 
 				Log.d("TMS", "No longer have and stored creds - going to get some.");
 				new RetrieveRequestTokenTask().execute("");
 
 			} catch (OAuthException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onPostExecute(String clientXML) {
 			Log.d("TMS", "In onPoseExecute: " + clientXML);
 
 			Serializer serializer = new Persister();
 			Reader reader = new StringReader(clientXML);
 			Client client = null;
 			try {
 				client = serializer.read(Client.class, reader, false);
 			} catch (Exception e) {
 				Log.e("TMS", "Crud! " + e.getLocalizedMessage());
 			}
 			Log.d("TMS", "Got Client: " + client.companyName);
 
 			TextView tvClientId = (TextView) findViewById(R.id.clientId);
 			tvClientId.setText(client.companyName);
 		}
 	}
 
 	class TransactionsTask extends AsyncTask<String, Void, String> {
 
 		private Exception exception;
 
 		protected String doInBackground(String... urls) {
 			OAuthClient oclient = new OAuthClient(new HttpClient4());
 			OAuthAccessor accessor = defaultAccessor();
 			// Log.d("TMS", "ClientTask: "+ accessor.accessToken + "/" + accessor.tokenSecret);
 
 			try {
 				OAuthMessage omessage = oclient.invoke(accessor, "GET", "https://api.realexpayments.com/IPS-Reporting/api/v1.0/~/accounts", null);
 
 				String xml = omessage.readBodyAsString(); // can only read once
 				Log.d("TMS", "Accounts: " + xml);
 
 				return xml;
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OAuthException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onPostExecute(String accountsXML) {
 			// Log.d("TMS", "In onPostExecute: " + accountsXML);
 
 			Serializer serializer = new Persister();
 			Reader reader = new StringReader(accountsXML);
 			ClientAccounts ca = null;
 			try {
 				ca = serializer.read(ClientAccounts.class, reader, false);
 			} catch (Exception e) {
 				Log.e("TMS", "Crud! " + e.getLocalizedMessage());
 			}
 			Log.d("TMS", "Got Client: " + ca.account.get(0).accountName);
 
 			TextView tvAccounts = (TextView) findViewById(R.id.transactions);
 			tvAccounts.setText(ca.account.get(0).accountName);
 		}
 	}
 
 }
