 package com.dreamhack.dhcc;
 
 import org.json.JSONObject;
 
 import junit.framework.Assert;
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 public class OAuthHelper extends Activity {
 	private static final String TAG = "OAUTH";
 
 	public static final String USER_TOKEN = "user_token";
 	public static final String USER_SECRET = "user_secret";
 	public static final String REQUEST_TOKEN = "request_token";
 	public static final String REQUEST_SECRET = "request_secret";
 
	public static final String DHCC_REQUEST_TOKEN_URL = "http://api.crew.dreamhack.se/oauth/1.0a/request_token";
	public static final String DHCC_ACCESS_TOKEN_URL = "http://api.crew.dreamhack.se/oauth/1.0a/access_token";
	public static final String DHCC_AUTHORIZE_URL = "http://api.crew.dreamhack.se/oauth/1.0a/authorize";
 
 	private static final Uri CALLBACK_URI = Uri.parse("dhcc://auth");
 
 	public static final String PREFS = "MyPrefsFile";
 
 	private OAuthConsumer mConsumer = null;
 	private OAuthProvider mProvider = null;
 	
 	SharedPreferences mSettings;
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 	
 		//setContentView(R.layout.oauthhelper);
 		
 		// We don't need to worry about any saved states: we can reconstruct the state
 		mConsumer = new CommonsHttpOAuthConsumer(
 				Keys.DHCC_CONSUMER_KEY, 
 				Keys.DHCC_CONSUMER_SECRET);
 		
 		mProvider = new CommonsHttpOAuthProvider (
 				DHCC_REQUEST_TOKEN_URL, 
 				DHCC_ACCESS_TOKEN_URL,
 				DHCC_AUTHORIZE_URL);
 		
 		// It turns out this was the missing thing to making standard Activity launch mode work
 		mProvider.setOAuth10a(true);
 		
 		mSettings = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
 		
 		
 		
 		Intent i = this.getIntent();
 		if (i.getData() == null) {
 			new retrieveRequestToken().execute();
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 				
 		Uri uri = getIntent().getData();
 		if (uri != null && CALLBACK_URI.getScheme().equals(uri.getScheme())) {	
 			retrieveAccessToken retrive = new retrieveAccessToken();
 			retrive.execute(uri);
 		}
 	}
 	@Override
 	protected void onPause(){
 		super.onPause();
 		finish();
 	}
 	
 	private class retrieveRequestToken extends AsyncTask<Void, Void, String>{
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 		
 			try {
 	            // This is really important. If you were able to register your real callback Uri with Twitter, and not some fake Uri
 	            // like I registered when I wrote this example, you need to send null as the callback Uri in this function call. Then
 	            // Twitter will correctly process your callback redirection
 				String authUrl = mProvider.retrieveRequestToken(mConsumer, CALLBACK_URI.toString());
 				saveRequestInformation(mSettings, mConsumer.getToken(), mConsumer.getTokenSecret());
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
 				/*	
 				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
 				sendIntent.setData(Uri.parse(authUrl));
 				startActivity(sendIntent);
 				*/
 				
 			} catch (OAuthMessageSignerException e) {
 			e.printStackTrace();
 			} catch (OAuthNotAuthorizedException e) {
 			e.printStackTrace();
 			} catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 				//Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 				startActivity(new Intent( null, MainActivity.class)); // Currently, how we get back to main activity
 			
 							
 			} catch (OAuthCommunicationException e) {
 			e.printStackTrace();
 			}
 			return null;
 		}
 	}
 	
 	private class retrieveAccessToken extends AsyncTask<Uri, Void, String>{
 		private String token;
 		private String secret;
 	
 		
 		@Override
 		protected String doInBackground(Uri... uri) {
 		
 			
 			String token = mSettings.getString(OAuthHelper.REQUEST_TOKEN, null);
 			String secret = mSettings.getString(OAuthHelper.REQUEST_SECRET, null);
 			
 			try {
 				if(!(token == null || secret == null)) {
 					mConsumer.setTokenWithSecret(token, secret);
 				}
 				String otoken = uri[0].getQueryParameter(OAuth.OAUTH_TOKEN);
 				String verifier = uri[0].getQueryParameter(OAuth.OAUTH_VERIFIER);
 
 				// We send out and save the request token, but the secret is not the same as the verifier
 				// Apparently, the verifier is decoded to get the secret, which is then compared - crafty
 				// This is a sanity check which should never fail - hence the assertion
 				Assert.assertEquals(otoken, mConsumer.getToken());
 
 				// This is the moment of truth - we could throw here
 				mProvider.retrieveAccessToken(mConsumer, verifier);
 				// Now we can retrieve the goodies
 				token = mConsumer.getToken();
 				secret = mConsumer.getTokenSecret();
 				OAuthHelper.saveAuthInformation(mSettings, token, secret);
 				// Clear the request stuff, now that we have the real thing
 				OAuthHelper.saveRequestInformation(mSettings, null, null);
 			} catch (OAuthMessageSignerException e) {
 				e.printStackTrace();
 			} catch (OAuthNotAuthorizedException e) {
 				e.printStackTrace();
 			} catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 			} catch (OAuthCommunicationException e) {
 				e.printStackTrace();
 			} 
 			
 			return "resultatet";
 		}
 		protected void onPostExecute(String result) {
 			Intent i = new Intent(getBaseContext(), MainActivity.class); // Currently, how we get back to main activity.
 			
 			i.putExtra(USER_TOKEN, token);
 			i.putExtra(USER_SECRET, secret);
 			Toast.makeText(getBaseContext(), "I'm back :)", Toast.LENGTH_LONG).show();
 			startActivity(i); // we either authenticated and have the extras or not, but we're going back
 
 		}
 	}
 
 
 	
 	public static void saveRequestInformation(SharedPreferences settings, String token, String secret) {
 		// null means to clear the old values
 		SharedPreferences.Editor editor = settings.edit();
 		if(token == null) {
 			editor.remove(OAuthHelper.REQUEST_TOKEN);
 			Log.d(TAG, "Clearing Request Token");
 		}
 		else {
 			editor.putString(OAuthHelper.REQUEST_TOKEN, token);
 			Log.d(TAG, "Saving Request Token: " + token);
 		}
 		if (secret == null) {
 			editor.remove(OAuthHelper.REQUEST_SECRET);
 			Log.d(TAG, "Clearing Request Secret");
 		}
 		else {
 			editor.putString(OAuthHelper.REQUEST_SECRET, secret);
 			Log.d(TAG, "Saving Request Secret: " + secret);
 		}
 		editor.commit();
 		
 	}
 	
 	public static void saveAuthInformation(SharedPreferences settings, String token, String secret) {
 		// null means to clear the old values
 		SharedPreferences.Editor editor = settings.edit();
 		if(token == null) {
 			editor.remove(OAuthHelper.USER_TOKEN);
 			editor.remove(OAuthHelper.USER_TOKEN);
 			Log.d(TAG, "Clearing OAuth Token");
 		}
 		else {
 			editor.putString(OAuthHelper.USER_TOKEN, token);
 			Log.d(TAG, "Saving OAuth Token: " + token);
 		}
 		if (secret == null) {
 			editor.remove(OAuthHelper.USER_SECRET);
 			Log.d(TAG, "Clearing OAuth Secret");
 		}
 		else {
 			editor.putString(OAuthHelper.USER_SECRET, secret);
 			Log.d(TAG, "Saving OAuth Secret: " + secret);
 		}
 		editor.commit();
 		
 	}
 	
 }
