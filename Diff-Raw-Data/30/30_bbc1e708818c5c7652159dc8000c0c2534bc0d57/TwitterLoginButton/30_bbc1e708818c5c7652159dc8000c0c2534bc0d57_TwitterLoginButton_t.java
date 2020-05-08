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
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 import com.awl.tumlabs.twitter.android.TwitterAuth.AuthListener;
 import com.awl.tumlabs.twitter.android.TwitterAuth.TwitterLoginListener;
 
 /***
  * Create a login button to sign in with Twitter
  * 
  * @author Xavier Balloy
  * 
  */
 public class TwitterLoginButton extends Button {
 
 	private static final String TAG = TwitterLoginButton.class.getSimpleName();
 
 	private TwitterAuth twitter;
 	private Activity activity;
 	private AuthListener listener;
 	private SessionListener sessionListener;
 
 	private Handler hander = new Handler();
 
 	public TwitterLoginButton(Context context) {
 		super(context);
 	}
 
 	public TwitterLoginButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public TwitterLoginButton(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	/***
 	 * Initialize the button
 	 * 
 	 * @param activiy
 	 *            The activity where the button is set
 	 * @param twitter
 	 *            The Twitter used to store the token and token secret
 	 */
 	public void init(final Activity activiy, final TwitterAuth twitter,
 			final AuthListener listener) {
 		this.twitter = twitter;
 		this.activity = activiy;
 		this.listener = listener;
 		this.sessionListener = new SessionListener();
 
 		TwitterSessionStore.restore(twitter, getContext());
 		setOnClickListener(new ButtonOnClickListener());
 	}
 
 	final class ButtonOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 
 			if (twitter.isSessionValid()) {
 				Log.d(TAG, "Session valid: Already logged in");
 				//TwitterSessionStore.clear(twitter, getContext());
 				//listener.onLogoutSucceed();
 				sessionListener.onAccessTokenRetrieved();
 			} else {
 				Log.d(TAG, "Session not valid: connection");
 				twitter.getAuthorizeUrl(sessionListener);
 			}
 		}
 	}
 
 	protected final class SessionListener implements TwitterLoginListener {
 
 		@Override
 		public void onAuthorizeUrlRetrieved(String url) {
 			Log.d(TAG, "onAuthorizeUrlRetrieved: " + url);
 			final String _url = url;
 			hander.post(new Runnable() {
 
 				@Override
 				public void run() {
 					twitter.autorize(activity, _url,
 							sessionListener);
 				}
 			});
 		}
 
 		@Override
 		public void onDialogComplete(String oauthVerifier) {
 			Log.d(TAG, "onDialogComplete: " + oauthVerifier);
 			twitter.retrieveAccessToken(oauthVerifier, sessionListener);
 		}
 		
 		@Override
 		public void onAccessTokenRetrieved() {
 			Log.d(TAG, "onAccessTokenRetrieved");
 			hander.post(new Runnable() {
 				
 				@Override
 				public void run() {
 					TwitterSessionStore.save(twitter, getContext());
					listener.onAuthSucceed(twitter);
 				}
 			});
 		}
 
 		@Override
 		public void onError(String message) {
 			listener.onError(message);
 		}
 
 	}
 }
