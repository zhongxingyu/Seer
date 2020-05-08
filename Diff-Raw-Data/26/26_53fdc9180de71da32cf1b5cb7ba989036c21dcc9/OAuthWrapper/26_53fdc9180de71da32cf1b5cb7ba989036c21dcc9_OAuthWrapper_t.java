 package com.zeyomir.ocfun.controller.helper;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import com.zeyomir.ocfun.dao.PreferencesDAO;
 import com.zeyomir.ocfun.gui.Settings;
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.basic.DefaultOAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 public class OAuthWrapper {
 	private static final String OAUTH_KEY = "YOUR_KEY";
 	private static final String OAUTH_SECRET = "YOUR_SECRET";
 	private static final String OAUTH_CALLBACK_SCHEME = "ocfun";
 	private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME
 			+ "://callback";
 	private final OAuthConsumer mConsumer;
 	private final OAuthProvider mProvider;
 	private static Context c;
 	private static OAuthWrapper instance = null;
 
 	private OAuthWrapper(Context context) {
 		c = context;
 		mConsumer = new CommonsHttpOAuthConsumer(OAUTH_KEY, OAUTH_SECRET);
 		mProvider = new DefaultOAuthProvider(
 				"http://opencaching.pl/okapi/services/oauth/request_token",
 				"http://opencaching.pl/okapi/services/oauth/access_token",
 				"http://opencaching.pl/okapi/services/oauth/authorize");
 	}
 
 	public static OAuthWrapper get(Context context) {
 		if (instance == null)
 			instance = new OAuthWrapper(context);
 		else
 			c = context;
 		return instance;
 	}
 
 	public String sign(String url) {
 		PreferencesDAO p = new PreferencesDAO(c);
 		if (p.isAuthenticated()) {
 			try {
 				mConsumer.setTokenWithSecret(p.getToken(), p.getSecret());
 				url = mConsumer.sign(url);
 			} catch (Exception e) {
 				e.printStackTrace();
 				url = null;
 			}
 		} else {
 			url += "&consumer_key=" + OAUTH_KEY;
 		}
 		return url;
 	}
 
 	public void startAuthentication() {
 		new RequestTokenGetter().execute();
 	}
 
 	public void authenticate(Intent i) {
 		Uri uri = i.getData();
 
 		if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) {
 			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
 			Log.i("OAuthVer", verifier);
 			new AccessTokenGetter().execute(verifier);
 
 		}
 	}
 
 	private class RequestTokenGetter extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			String authURL = null;
 			try {
 				authURL = mProvider.retrieveRequestToken(mConsumer,
 						OAUTH_CALLBACK_URL);
 				Log.i("OAuthReqParams", mConsumer.getRequestParameters()
 						.toString());
 			} catch (Exception e) {
 				Log.e("OAuth", e.getMessage());
				Toast.makeText(c, "Wystąpił błąd...", Toast.LENGTH_SHORT)
 						.show();
 			}
 			if (authURL != null) {
 				Intent intent = new Intent(Intent.ACTION_VIEW,
 						Uri.parse(authURL));
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 				c.startActivity(intent);
 			}
 			return null;
 		}
 	}
 
 	private class AccessTokenGetter extends
 			AsyncTask<String, Void, OAuthConsumer> {
 
 		@Override
 		protected OAuthConsumer doInBackground(String... params) {
 			try {
 				// mConsumer.setAdditionalParameters(reqParams);
 
 				Log.i("OAuthReqParams", mConsumer.getRequestParameters()
 						.toString());
 				mProvider.retrieveAccessToken(mConsumer, params[0]);
 			} catch (Exception e) {
 				//Log.e("OAuth", e.getMessage());
 			}
 			String token = mConsumer.getToken();
 			String tokenSecret = mConsumer.getTokenSecret();
 			Log.i("OAuth", "key:" + OAUTH_KEY + "\nsecret:" + OAUTH_SECRET
 					+ "\ntoken:" + token + "\ntoken secret:" + tokenSecret);
 			return mConsumer;
 		}
 
 		@Override
 		protected void onPostExecute(OAuthConsumer params) {
 			((Settings) c).authenticated(mConsumer.getToken(),
 					mConsumer.getTokenSecret());
 		}
 	}
 }
