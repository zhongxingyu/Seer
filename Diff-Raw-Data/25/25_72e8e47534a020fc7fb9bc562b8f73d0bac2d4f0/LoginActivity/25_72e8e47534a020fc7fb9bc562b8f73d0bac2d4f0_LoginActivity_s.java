 package com.iSpraker.android.app;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 import com.iSpraker.android.R;
 import com.iSpraker.android.dao.IUsersDAO;
 import com.iSpraker.android.dao.impl.JsonUsersDAO;
 import com.iSpraker.android.dos.UsersResponse;
 
 public class LoginActivity extends FragmentActivity {
 	
 	public static final String PREFS_NAME = "PrefsFile";
 	
 	private static final String CONSUMER_KEY="AOMdfPY2WZ2vFNeOjVGQdw";
 	private static final String CONSUMER_SECRET="kBPBoZCRFOOktU8BHOt6isDDOCDgIGy64VbzrzaKo";
 	private static final String CALLBACK_URL="ispraker:///";
 	
 	private Twitter mTwitter;
 	private RequestToken requestToken;
 	private UsersResponse userResponse;
 	
     @Override
     public void onNewIntent(Intent intent) {
     	super.onNewIntent(intent);
     	
     	Uri uri=intent.getData();
     	if(uri != null && uri.toString().startsWith(CALLBACK_URL)) {
     		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
     		SharedPreferences.Editor editor = settings.edit();
     		
     		String oauthVerifier = uri.getQueryParameter("oauth_verifier");
     		try {
 				AccessToken accessToken = mTwitter.getOAuthAccessToken(requestToken, oauthVerifier);
 				editor.putString("twitter_access_token", accessToken.getToken());
 				editor.putString("twitter_access_token_secret", accessToken.getTokenSecret());
 				editor.commit();
 				
 				if(this.isUserExist(accessToken)) {
 					// Start the main activity
 					Intent intentMain = new Intent();
 					Bundle b = new Bundle();
 					b.putParcelable("user", userResponse.getUsers().get(0));
 					intentMain.setClassName("com.iSpraker.android", "com.iSpraker.android.app.ISprakerAndroidClientActivity");
 					intentMain.putExtras(b);
 					startActivity(intentMain);
 				} else {
 					if(userResponse.getResponseCode() == 404) {
 						// create user in the backend
 						User user = mTwitter.showUser(accessToken.getUserId());
 						com.iSpraker.android.dos.User newUser = new com.iSpraker.android.dos.User();
 						newUser.setDescription(user.getDescription());
 						newUser.setEmail(user.getScreenName() + "@ispraker.com");
 						newUser.setProfileImageURL(user.getProfileImageURL().toString());
 						newUser.setScreenName(user.getScreenName());
 						newUser.setTimeZone(user.getTimeZone());
 						newUser.setToken(accessToken.getToken());
 						newUser.setTwitterId(user.getName());
 						newUser.setUid(String.valueOf(user.getId()));
 						
 						String url = this.getResources().getString(R.string.api_users_local);
 				    	IUsersDAO userDAO = new JsonUsersDAO(url);
 						int responseCode = userDAO.signUpUser(newUser).getResponseCode();
 						if (responseCode != 200) {
 							Toast.makeText(this, "Something went wrong with the login, please try again later", Toast.LENGTH_LONG);
 					    	setContentView(R.layout.login);
 						}
 					}
 					
 				}
 				
 //				AccessToken accessToken2 = new AccessToken(settings.getString("twitter_access_token", null), settings.getString("twitter_access_token_secret", null));
 //				mTwitter.setOAuthAccessToken(accessToken);
 				User user = mTwitter.showUser(accessToken.getUserId());
 				user.getId();
 //				setContentView(R.layout.main);
 			} catch (TwitterException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     }
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	      super.onCreate(savedInstanceState);   
 	      setContentView(R.layout.login);
 	}
 	
 	public void onLoginClicked(View v) {
 		mTwitter = new TwitterFactory().getInstance();
         
         mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
         
         try {
 			requestToken= mTwitter.getOAuthRequestToken(CALLBACK_URL);
 			WebView twitterSite = new WebView(this);
 			twitterSite.loadUrl(requestToken.getAuthenticationURL());
 			setContentView(twitterSite);
 			
 		} catch (TwitterException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private boolean isUserExist(AccessToken accessToken) {
 		long uid = accessToken.getUserId();
 //		String url = "http://ispraker.heroku.com//api/9b02756d6564a40dfa6436c3001a1441/users.json"; //PeopleTabFragment.this.getResources().getString(R.string.api_users);
     	String url = this.getResources().getString(R.string.api_users_local);
     	IUsersDAO userDAO = new JsonUsersDAO(url);
 		userResponse = userDAO.getUserByUid(String.valueOf(uid));
 		switch(userResponse.getResponseCode()) {
 		case 404:
 			return false;
 		case 200:
 			return true;
 		default:
 	    	Toast.makeText(this, "Something went wrong with the login, please try again later", Toast.LENGTH_LONG);
 	    	setContentView(R.layout.login);
 		}
 		return false;
 	}
 }
