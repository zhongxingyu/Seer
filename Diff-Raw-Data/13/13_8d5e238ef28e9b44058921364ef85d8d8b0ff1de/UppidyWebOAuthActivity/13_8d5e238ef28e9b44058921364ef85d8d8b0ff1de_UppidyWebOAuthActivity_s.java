 package com.uppidy.android.demo.app;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.social.connect.DuplicateConnectionException;
 import org.springframework.social.oauth2.AccessGrant;
 import org.springframework.social.oauth2.GrantType;
 import org.springframework.social.oauth2.OAuth2Parameters;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 import com.uppidy.android.sdk.R;
 import com.uppidy.android.sdk.api.ApiContainer;
 import com.uppidy.android.sdk.api.Uppidy;
 import com.uppidy.android.sdk.connect.UppidyConnectionFactory;
 
 /**
  * @author arudnev@uppidy.com
  */
 public class UppidyWebOAuthActivity extends AbstractWebViewActivity {
 
 	private static final String TAG = UppidyWebOAuthActivity.class.getSimpleName();
 
 	private ConnectionRepository connectionRepository;
 
 	private UppidyConnectionFactory connectionFactory;
 
 	// ***************************************
 	// Activity methods
 	// ***************************************
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Uppidy uses javascript to redirect to the success page
 		getWebView().getSettings().setJavaScriptEnabled(true);
 
 		// Using a custom web view client to capture the access token
 		getWebView().setWebViewClient(new UppidyOAuthWebViewClient());
 
 		this.connectionRepository = getApplicationContext().getConnectionRepository();
 		this.connectionFactory = getApplicationContext().getUppidyConnectionFactory();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		// display the Uppidy authorization page
 		getWebView().loadUrl(getAuthorizeUrl());
 	}
 
 	// ***************************************
 	// Private methods
 	// ***************************************
 	private String getAuthorizeUrl() {
 		String redirectUri = getString(R.string.uppidy_oauth_callback_url);
 		String scope = getString(R.string.uppidy_scope);
 
 		/* 
 		 * Generate the Uppidy authorization url to be used in the browser or web view the display=touch parameter 
 		 * requests the mobile formatted version of the Uppidy authorization page
 		 */
 		OAuth2Parameters params = new OAuth2Parameters();
 		params.setRedirectUri(redirectUri);
 		params.setScope(scope);
 		params.add("display", "touch");
 		return this.connectionFactory.getOAuthOperations().buildAuthorizeUrl(GrantType.IMPLICIT_GRANT, params);
 	}
 
 	private void displayUppidyOptions() {
 		Intent intent = new Intent();
 		intent.setClass(this, UppidyActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	// ***************************************
 	// Private classes
 	// ***************************************
 	private class UppidyOAuthWebViewClient extends WebViewClient {
 
 		/*
 		 * The WebViewClient has another method called shouldOverridUrlLoading which does not capture the javascript 
 		 * redirect to the success page. So we're using onPageStarted to capture the url.
 		 */
 		@Override
 		public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			// parse the captured url
 			Uri uri = Uri.parse(url);
 
 			Log.d(TAG, url);
 
 			/*
 			 * The access token is returned in the URI fragment of the URL. See the Desktop Apps section all the way 
 			 * at the bottom of this link:
 			 * 
 			 * http://developers.uppidy.com/docs/authentication/
 			 * 
 			 * The fragment will be formatted like this:
 			 * 
 			 * #access_token=A&expires_in=0
 			 */
 			String uriFragment = uri.getFragment();
 
 			// confirm we have the fragment, and it has an access_token parameter
 			if (uriFragment != null && uriFragment.startsWith("access_token=")) {
 				// split to get the two different parameters
 				new ConnectTask().execute(uriFragment.split("&"));
 			}
 
 			/*
 			 * if there was an error with the oauth process, return the error description
 			 * 
 			 * The error query string will look like this:
 			 * 
 			 * ?error_reason=user_denied&error=access_denied&error_description=The +user+denied+your+request
 			 */
 			if (uri.getQueryParameter("error") != null) {
 				CharSequence errorReason = uri.getQueryParameter("error_description").replace("+", " ");
 				Toast.makeText(getApplicationContext(), errorReason, Toast.LENGTH_LONG).show();
 				displayUppidyOptions();
 			}
 		}
 	}
 	
 
 	// ***************************************
 	// Private classes
 	// ***************************************
 	private class ConnectTask extends AsyncTask<String, Void, Connection<Uppidy>> {
 
 		@Override
 		protected void onPreExecute() {
 			// before the network request begins, show a progress indicator
 			showProgressDialog("Establishing connection to Uppidy...");
 		}
 
 		@Override
 		protected Connection<Uppidy> doInBackground(String... params) {
 			Connection<Uppidy> connection = null;
 			try {
 				/*
 				 * The fragment also contains an "expires_in" parameter. In this
 				 * example we requested the offline_access permission, which
 				 * basically means the access will not expire, so we're ignoring
 				 * it here
 				 */
 				try {
 					// split to get the access token parameter and value
 					String[] accessTokenParam = params[0].split("=");
 
 					// get the access token value
 					String accessToken = accessTokenParam[1];
 
 					// create the connection and persist it to the repository
 					AccessGrant accessGrant = new AccessGrant(accessToken);
 					connection = connectionFactory.createConnection(accessGrant);
 
 					try {
 						connectionRepository.addConnection(connection);
 					} catch (DuplicateConnectionException e) {
 						// connection already exists in repository!
 						Log.w(TAG, "Connection already exists in repository", e);
 					}
 				} catch (Exception e) {
 					// don't do anything if the parameters are not what is expected
 					Log.w(TAG, "Could not create connection", e);
 				}
 
 				initContainer();
 			} catch (Exception e) {
 				Log.e(TAG, e.getLocalizedMessage(), e);
 			}
 			return connection;
 		}
 		
 		private void initContainer() {
 			MainApplication ctx = getApplicationContext();
 			Uppidy uppidy = connectionRepository.findPrimaryConnection(Uppidy.class).getApi();
 			Map<String, String> queryParams = new HashMap<String, String>();
 			String containerId = ctx.getContainerId();
 			String phoneNumber = null;
 			String deviceId = null;
 			if(containerId != null) {
 				queryParams.put("id", containerId);
 			} else {
 				// Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. 
 				// Return null if it is unavailable.
 				phoneNumber = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
				if (phoneNumber == null) {
 					// Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones. 
 					// Return null if device ID is not available. 
 					deviceId = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					queryParams.put("deviceId", deviceId);
					
 				} else {
 					queryParams.put("number", phoneNumber);
 				}
 			}
 			ApiContainer container = null;
 			List<ApiContainer> containers = uppidy.backupOperations().listContainers(queryParams);
 			if(containers.isEmpty()) {
 				Map<String, Object> parameters = new HashMap<String, Object>();
				if(phoneNumber != null) parameters.put("number", phoneNumber);
				if(deviceId != null) parameters.put("deviceId", deviceId);
 				parameters.put("description", Build.MODEL);
 				container = uppidy.backupOperations().createContainer(parameters);
 			} else {
 				container = containers.get(0);
 			}
 			ctx.setContainer(container);
 			ctx.setContainerId(container.getId());
 		}
 
 		@Override
 		protected void onPostExecute(Connection<Uppidy> result) {
 			// after the network request completes, hide the progress indicator
 			dismissProgressDialog();
 			displayUppidyOptions();
 		}
 
 	}
 
 }
