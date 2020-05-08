 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.activity;
 
 import java.net.URL;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.R;
 import org.mustard.android.provider.OAuthInstance;
 import org.mustard.android.provider.OAuthLoader;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.oauth.OAuthManager;
 import org.mustard.statusnet.StatusNetService;
 import org.mustard.statusnet.User;
 import org.mustard.util.MustardException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class OAuthCallback extends Activity {
 
 	private String TAG = getClass().getCanonicalName();
 
 	private String mSURL;
 	private MustardDbAdapter mDbHelper;
 	private StatusNet mStatusNet;
 	private String mUsername;
 
 	//	private CommonsHttpOAuthConsumer mOAuthConsumer; 
 
 	private SharedPreferences mSharedPreferences;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.oauthcallback);
 
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 
 		TextView welcomeMessage = (TextView) findViewById(R.id.welcome_label);
 		Button continueButton = (Button) findViewById(R.id.button_continue);
 		continueButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				try {
 					startOActivity();
 				} catch (Exception e) {
 					e.printStackTrace();
 					new AlertDialog.Builder(OAuthCallback.this)
 					.setTitle(getString(R.string.error))
 					.setMessage(e.toString())
 					.setNeutralButton(getString(R.string.close), null).show();
 					return;
 				}
 			}
 		});
 
 		//		continueButton.setEnabled(false);
 
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 
 		Uri uri = getIntent().getData();
 		if (uri != null && uri.toString().startsWith("statusnet://oauth")) {
 
 			String verifier = uri.getQueryParameter("oauth_verifier");
 			String requestTokenSaved = mSharedPreferences.getString("Request_token","");
 			String requestToken = uri.getQueryParameter("oauth_token");
 			if (requestToken != null && !"".equals(requestToken)) {
 				// This is a test...
 				// Start test -->
 				if (requestTokenSaved.equals("")) {
 					Log.e("Mustard", "savedToken: is null but requestToken is set. I try to proceed");
 					// <<-- End test
 				} else {
 					if(!requestToken.equals(requestTokenSaved)) {
 //						Log.e("Mustard", "savedToken: " + requestTokenSaved + " != requestToken: " + requestToken);
 						new AlertDialog.Builder(OAuthCallback.this)
 						.setTitle(getString(R.string.error))
 						.setMessage("Token saved and token returned are not the same!\nWhat's up?!?!?")
 						.setNeutralButton(getString(R.string.close), null).show();
 						resetSharedProperties(mSharedPreferences);
 						return;
 //					} else {
 //						Log.e("Mustard", "savedToken: " + requestTokenSaved + " == requestToken: " + requestToken);
 					}
 				}
 			}
 
 			mSURL=mSharedPreferences.getString("oauth_url","");
 
 			OAuthManager oauthManager = OAuthManager.getOAuthManager();
 
 			if(!oauthManager.isReady()) {
 				OAuthLoader om = new OAuthLoader(mDbHelper) ;
 				String instance = mSharedPreferences.getString("instance","");
 				boolean isTwitter = mSharedPreferences.getBoolean("is_twitter", false);
 				OAuthInstance oi =  om.get(instance);
 				if (oi != null) {
 
 					oauthManager.prepare(
 							oi.key,
 							oi.secret,
 							mSURL + (!isTwitter ? "/api" : "") + "/oauth/request_token",
 							mSURL + (!isTwitter ? "/api" : "") + "/oauth/access_token",
 							mSURL + (!isTwitter ? "/api" : "") + "/oauth/authorize");
 					
 					oauthManager.setConsumerTokenWithSecret(requestTokenSaved,
 								mSharedPreferences.getString("Request_token_secret",""),mSharedPreferences.getBoolean("oauth_10a", false));
 					
 				}
 			}
 
 			if (!oauthManager.retrieveAccessToken(verifier)) {
 				new AlertDialog.Builder(OAuthCallback.this)
 				.setTitle(getString(R.string.error))
 				.setMessage(getString(R.string.error_generic))
 				.setNeutralButton(getString(R.string.close), null).show();
 				resetSharedProperties(mSharedPreferences);
 				return;
 			}
 			try {
 
 				User u = verifyUser(oauthManager);
 				u.getName();
 				welcomeMessage.setText(getString(R.string.welcome_label,u.getName()));
 
 				mUsername = u.getScreen_name();
 				String consumerToken = oauthManager.getConsumer().getToken();
 				String consumerTokenSecret = oauthManager.getConsumer().getTokenSecret();
 				String version = "";
 				if (!mStatusNet.isTwitterInstance())
 					version = getVersion(oauthManager,mUsername);
 				else
 					version = "0.9.4";
 				if (mDbHelper.userExists(mUsername, mSURL) > 0) {
 					mDbHelper.updateAccount(mUsername, consumerToken, consumerTokenSecret, mSURL, 1, version);
 				} else {
 					mDbHelper.createAccount(u.getId(),mUsername,consumerToken, consumerTokenSecret, mSURL, 1, version);
 				}
 				resetSharedProperties(mSharedPreferences);
 
 				StatusNetService sns = null;
 				if(!mStatusNet.isTwitterInstance()) {
 					// Check if mURL is a statusnet instance
 					sns = mStatusNet.getConfiguration();
 //					Log.v("mustard", "############################## textlimit: " + sns.site.textlimit);
 
 					if (sns == null) {
 						throw new MustardException(getString(R.string.error_help_test));
 					}
 
 				} else {
 					sns = new StatusNetService();
 					sns.site.textlimit=140;
 				}
 				try {
 					mDbHelper.setTextlimitInstance(mSURL, sns.site.textlimit);
 				} catch(Exception e) {
 					Log.e("mustard", e.getMessage());
 				}
 
 			} catch (Exception e) {
 				new AlertDialog.Builder(OAuthCallback.this)
 				.setTitle(getString(R.string.error))
 				.setMessage(getString(R.string.error_generic_detail,e.toString()))
 				.setNeutralButton(getString(R.string.close), null).show();
 			}
 		}
 
 	}
 
 	private void startMainActivity() {
 		MustardMain.actionHandleTimeline(this);
 		finish();
 	}
 
 	private void startOActivity() {
 
 
 		if (mSURL.endsWith("identi.ca") && mStatusNet != null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.msg_welcome,mUsername))
 			.setTitle(getString(R.string.title_welcome))
 			.setCancelable(false)
 			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int id) {
 					try {
 						mStatusNet.doSubscribe("95570");
 						startMainActivity();
 					} catch (MustardException e) {
 						Log.e("mustard"," Error subscribing.. " + e.getMessage());
 					}
 					//					setResult(RESULT_OK);
 					//			        finish();
 				}
 			})
 			.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int id) {
 					xdialog.cancel();
 					startMainActivity();
 					//					setResult(RESULT_OK);
 					//			        finish();
 				}
 			});
 			builder.create();
 			builder.show();
 		} else if  (mSURL.endsWith("twitter.com") && mStatusNet != null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.msg_welcome,mUsername))
 			.setTitle(getString(R.string.title_welcome))
 			.setCancelable(false)
 			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int id) {
 					try {
 						mStatusNet.doSubscribe("179569425");
 						startMainActivity();
 					} catch (MustardException e) {
 						Log.e("mustard"," Error subscribing.. " + e.getMessage());
 					}
 					//					setResult(RESULT_OK);
 					//			        finish();
 				}
 			})
 			.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int id) {
 					xdialog.cancel();
 					startMainActivity();
 					//					setResult(RESULT_OK);
 					//			        finish();
 				}
 			});
 			builder.create();
 			builder.show();
		} else {
			startMainActivity();
 		}
 
 	}
 
 	private User verifyUser(OAuthManager oauthManager) throws Exception {
 		mStatusNet = new StatusNet(this);
 		mStatusNet.setURL(new URL(mSURL));
 		mStatusNet.setCredentials(oauthManager.getConsumer(), "");
 		return mStatusNet.checkUser();
 	}
 
 	private String getVersion(OAuthManager oauthManager,String username) throws Exception {
 		mStatusNet = new StatusNet(this);
 		mStatusNet.setURL(new URL(mSURL));
 		mStatusNet.setCredentials(oauthManager.getConsumer(), username);
 		return mStatusNet.getVersion();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(MustardApplication.DEBUG) Log.i(TAG, "onDestroy");
 		resetSharedProperties(mSharedPreferences);
 		if(mDbHelper != null) {
 			try {
 				mDbHelper.close();
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void resetSharedProperties(SharedPreferences sharedPreferences) {
 		sharedPreferences.edit()
 		.remove("Request_token")
 		.remove("Request_token_secret")
 		.remove("oauth_url")
 		.remove("is_twitter")
 		.remove("instance")
 		.remove("oauth_10a")
 		.commit();
 	}
 }
