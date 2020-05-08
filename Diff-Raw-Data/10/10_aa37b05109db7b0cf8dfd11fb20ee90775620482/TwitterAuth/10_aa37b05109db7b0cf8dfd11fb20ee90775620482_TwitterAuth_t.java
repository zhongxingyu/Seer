 /*
  * Copyright 2011 Atos Wordline
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.awl.tumlabs.twitter.android;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 import android.Manifest;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.awl.tumlabs.twitter.android.TwitterLoginButton.SessionListener;
 import com.twitter.android.R;
 
 /***
  * OAuth for Twitter
  * 
  * @author Xavier Balloy
  * 
  */
 public class TwitterAuth {
 
 	private static final String TAG = TwitterAuth.class.getSimpleName();
 
 	private static final String REQUEST_TOKEN_URL = "https://twitter.com/oauth/request_token";
 	private static final String ACCESS_TOKEN_URL = "https://twitter.com/oauth/access_token";
 	private static final String AUTHORIZE_URL = "https://twitter.com/oauth/authorize";
	private static final String CALLBACK_URL = "x-oauthflow-twitter://callback";
 
 	private String token;
 	private String tokenSecret;
 	private String callback_url = CALLBACK_URL;
 
 	private CommonsHttpOAuthConsumer consumer;
 	private CommonsHttpOAuthProvider provider;
 
 	/***
 	 * Create a new Twitter object Go to https://dev.twitter.com/ to get a
 	 * consumer key and a consumer secret
 	 * 
 	 * @param consumerKey
 	 *            the Consumer Key of the application
 	 * @param consumerSecret
 	 *            the Consumer Secret of the application
 	 * @param callbackUrl
 	 *            the Callback URL of the application
 	 */
 	public TwitterAuth(String consumerKey, String consumerSecret, String callback) {
 		this.callback_url = callback;
 		Log.d(TAG, consumerKey +" " + consumerSecret);
 		consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
 		provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
		//Now that's really important. Because you don't perform the retrieveRequestToken method at this moment, the OAuth method is not detected automatically (there is no communication with Twitter). So, the default is 1.0 which is wrong because the initial request was performed with 1.0a.
		provider.setOAuth10a(true);
 	}
 
 	public String getToken() {
 		return this.token;
 	}
 
 	protected void setToken(String token) {
 		this.token = token;
 	}
 
 	public String getTokenSecret() {
 		return this.tokenSecret;
 	}
 
 	protected void setTokenSecret(String tokenSecret) {
 		this.tokenSecret = tokenSecret;
 	}
 
 	protected void getAuthorizeUrl(final SessionListener callback) {
 		new Thread() {
 			@Override
 			public void run() {
 				try {
					String url = provider.retrieveRequestToken(consumer, callback_url);
 					callback.onAuthorizeUrlRetrieved(url);
 				} catch (OAuthMessageSignerException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthNotAuthorizedException e) {
					e.printStackTrace();
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthExpectationFailedException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthCommunicationException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				}
 			}
 		}.start();
 	}
 
 	protected void retrieveAccessToken(String oauthVerifier, final SessionListener callback) {
 		final String _oauthVerifier = oauthVerifier;
 		new Thread() {
 			@Override
 			public void run() {
 
 				try {
 					provider.retrieveAccessToken(consumer, _oauthVerifier);
 					setToken(consumer.getToken());
 					setTokenSecret(consumer.getTokenSecret());
 					
 					callback.onAccessTokenRetrieved();
 				} catch (OAuthMessageSignerException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthNotAuthorizedException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthExpectationFailedException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				} catch (OAuthCommunicationException e) {
 					Log.e(TAG, e.getMessage(), e.getCause());
 					callback.onError(e.getMessage());
 				}
 			}
 		}.start();
 	}
 
 	protected void autorize(Context context, String url,
 			SessionListener listener) {
 		if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
 			Util.showAlert(context, context.getString(R.string.error),
 					context.getString(R.string.internet_permission_required));
 		} else {
 			new TwitterDialog(context, url, CALLBACK_URL, listener).show();
 		}
 	}
 
 	public boolean isSessionValid() {
 		return getToken() != null && getTokenSecret() != null;
 	}
 
 	protected static interface DialogListener {
 		public void onComplete(Bundle values);
 	}
 
 	protected static interface TwitterLoginListener {
 		public void onAuthorizeUrlRetrieved(String url);
 
 		public void onDialogComplete(String oauthVerifier);
 
 		public void onAccessTokenRetrieved();
 
 		public void onError(String message);
 	}
 
 	public static interface AuthListener {
 		public void onAuthSucceed();
 
 		public void onLogoutSucceed();
 
 		public void onError(String error);
 	}
 
 }
