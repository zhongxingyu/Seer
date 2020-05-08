 package com.seawolfsanctuary.keepingtracks.foursquare;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.Helpers;
 import com.seawolfsanctuary.keepingtracks.R;
 
 public class SetupActivity extends Activity {
 
 	private static final String CLIENT_ID = fetchClientID();
 	private static final String CLIENT_SECRET = fetchClientSecret();
 	private static final String REDIRECT_URI = fetchRedirectURI();
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.foursquare_context_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.foursquare_logout:
 			if (Helpers.removeAccessToken() == true) {
 				SetupActivity.this.finish();
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_logout_success),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_logout_failure),
 						Toast.LENGTH_LONG).show();
 			}
 
 			return true;
 		default:
 			System.out.println("Unkown action: " + item.getItemId());
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.foursquare_setup_activity);
 
 		String authentication_url = "https://foursquare.com/oauth2/authenticate"
 				+ "?client_id="
 				+ CLIENT_ID
 				+ "&response_type=token"
 				+ "&redirect_uri=" + REDIRECT_URI;
 
 		// If authentication works, we'll get redirected to a url with a pattern
 		// like:
 		//
 		// http://YOUR_REGISTERED_REDIRECT_URI/#access_token=ACCESS_TOKEN
 		//
 		// We can override onPageStarted() in the web client and grab the token
 		// out.
 		WebView webview = (WebView) findViewById(R.id.webview);
 		webview.getSettings().setJavaScriptEnabled(true);
 		webview.setWebViewClient(new WebViewClient() {
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				String fragment = "#access_token=";
 				int start = url.indexOf(fragment);
 				if (start > -1) {
 					if (Helpers.readAccessToken() == "") {
 						// Fetch an access token and reload
 						String accessToken = url.substring(
 								start + fragment.length(), url.length());
 						Helpers.writeAccessToken(accessToken);
 						reloadActivity();
 					} else {
 						Toast.makeText(
 								SetupActivity.this,
 								getString(R.string.foursquare_token_current,
 										Helpers.readAccessToken()),
 								Toast.LENGTH_SHORT).show();
 					}
 				}
 			}
 		});
 
 		webview.loadUrl(authentication_url);
 
 	}
 
 	private static String fetchClientID() {
 		String client_id = "";
 
 		try {
 			// Create a URL for the desired page
 			URL client_id_url = new URL(Helpers.foursquareClientID);
 
 			// Read all the text returned by the server
 			BufferedReader client_id_in = new BufferedReader(
 					new InputStreamReader(client_id_url.openStream()));
 			String str;
 			while ((str = client_id_in.readLine()) != null) {
 				client_id = str;
 			}
 			client_id_in.close();
 		} catch (Exception e) {
 			//
 		}
 
 		return client_id;
 	}
 
 	private static String fetchClientSecret() {
 		String client_secret = "";
 
 		try {
 			// Create a URL for the desired page
 			URL client_secret_url = new URL(Helpers.foursquareClientSecret);
 
 			// Read all the text returned by the server
 			BufferedReader client_secret_in = new BufferedReader(
 					new InputStreamReader(client_secret_url.openStream()));
 			String str;
 			while ((str = client_secret_in.readLine()) != null) {
 				client_secret = str;
 			}
 			client_secret_in.close();
 		} catch (Exception e) {
 			//
 		}
 
 		return client_secret;
 	}
 
 	private static String fetchRedirectURI() {
 		String redirect_uri = "";
 
 		try {
 			// Create a URL for the desired page
 			URL redirect_uri_url = new URL(Helpers.foursquareRedirectURI);
 
 			// Read all the text returned by the server
 			BufferedReader redirect_uri_in = new BufferedReader(
 					new InputStreamReader(redirect_uri_url.openStream()));
 			String str;
 			while ((str = redirect_uri_in.readLine()) != null) {
 				redirect_uri = str;
 			}
 			redirect_uri_in.close();
 		} catch (Exception e) {
 			//
 		}
 
 		return redirect_uri;
 	}
 
	public void authenticate(View v) {
		try {

			String client_id = fetchClientID();
			String client_secret = fetchClientSecret();
			String redirect_uri = fetchRedirectURI();

			System.out.println("Client ID: " + client_id);
			System.out.println("Client Secret: " + client_secret);
			System.out.println("Redirect URI: " + redirect_uri);

			String authentication_url = "https://foursquare.com/oauth2/authenticate?client_id="
					+ client_id
					+ "&response_type=token&redirect_uri="
					+ redirect_uri;

		} catch (Exception e) {
			//
		}
	}

 	private void reloadActivity() {
 		Intent intent = new Intent(this, SetupActivity.class);
 		SetupActivity.this.finish();
 		startActivity(intent);
 	}
 }
