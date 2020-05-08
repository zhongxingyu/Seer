 package com.company.tweeter;
 
 import java.util.List;
 
 import twitter4j.Status;
 import twitter4j.TwitterException;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.company.tweeter.accountmanager.AccountManager;
 import com.company.tweeter.accountmanager.TwitterAccount;
 
 public class TimelineActivity extends Activity {
     /** Called when the activity is first created. */
 	
 	private SharedPreferences mPrefs;
 	
 	private AccountManager manager;
 	private TwitterAccount account;
 	
 	private List<Status> statuses;
 	
 	private ListView timelineList;
 	private ImageView userImageView;
 	private TextView username;
 	private TextView time;
 	private TextView tweetText;
 	private TextView retweetedBy;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         mPrefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
         
         manager = AccountManager.getInstance();
         account = manager.getAccount();
         
         if(!mPrefs.contains(Constants.ACCESS_TOKEN)) {
         	getAccessTokenForTwitterAccount();
         } else {
         	setContentView(R.layout.timeline_layout);
         	initializeUI();
         	populateTimelineData(statuses);
         }
     }
     
     private void populateTimelineData(List<Status> statusList) {
     	
     }
     
     private void initializeUI() {
     	timelineList = (ListView) findViewById(R.id.tweetList);
     	userImageView = (ImageView) findViewById(R.id.userImageView);
     	username = (TextView) findViewById(R.id.username);
     	time = (TextView) findViewById(R.id.time);
     	tweetText = (TextView) findViewById(R.id.tweetMessage);
     	retweetedBy = (TextView) findViewById(R.id.retweetedBy);
     }
     
     private void getAccessTokenForTwitterAccount() {
 
     	WebView webView = new WebView(this);
     	webView.getSettings().setJavaScriptEnabled(true);
     	webView.setWebViewClient(new WebViewClient() {
     		@Override
     		public void onPageFinished(WebView view, String url) {
     			super.onPageFinished(view, url);
     			if(url.contains(Constants.CALLBACK_URL)) {
     				Uri uri = Uri.parse(url);
     				String accessTokenString = uri.getQueryParameter(Constants.OAUTH_TOKEN_KEY);
     				
     				SharedPreferences.Editor editor = mPrefs.edit();
     				editor.putString(Constants.ACCESS_TOKEN, accessTokenString);
     				editor.commit();
     				
     				setContentView(R.layout.timeline_layout);
     				initializeUI();
     				
     				try {
 						statuses = account.getPublicTimeline();
 					} catch (TwitterException e) {
 						// TODO Auto-generated catch block
 						Toast.makeText(getApplicationContext(), e.getErrorMessage(), Toast.LENGTH_LONG).show();
 					}
     			}
     		}
     		
     		@Override
     		public void onReceivedError(WebView view, int errorCode,
     				String description, String failingUrl) {
     			super.onReceivedError(view, errorCode, description, failingUrl);
     			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_LONG).show();
     		}
     	});
     	try {
 			webView.loadUrl(account.getAuthenticationUrl());
 		} catch (TwitterException e) {
 			Toast.makeText(getApplicationContext(), e.getErrorMessage(), Toast.LENGTH_LONG).show();
 		}
     	setContentView(webView);
     
     }
 }
