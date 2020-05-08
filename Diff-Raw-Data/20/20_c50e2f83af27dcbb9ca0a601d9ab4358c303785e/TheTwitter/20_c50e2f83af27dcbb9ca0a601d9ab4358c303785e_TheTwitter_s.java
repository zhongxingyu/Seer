 package com.megadevs.socialwrapper.thetwitter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import twitter4j.IDs;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.conf.Configuration;
 import twitter4j.conf.ConfigurationBuilder;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.megadevs.socialwrapper.SocialFriend;
 import com.megadevs.socialwrapper.SocialNetwork;
 import com.megadevs.socialwrapper.SocialSessionStore;
 import com.megadevs.socialwrapper.SocialWrapper;
 import com.megadevs.socialwrapper.exceptions.InvalidAuthenticationException;
 import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;
 import com.megadevs.socialwrapper.exceptions.NetworkErrorException;
 
 public class TheTwitter extends SocialNetwork {
 
 	public final static String consumerKey = "lfG3DKCO5sXrpuBQx2FQ";
 	public final static String consumerSecret = "z7FJ2HDuVxxd3yIXmFk6k5Vt8C4yVdnCXlW9GUMDF4";
 	private final String CALLBACKURL = "T4JOAuth://main";
 
 	private final String accessTokenKey = "accessTokenKey";
 	private final String accessTokenSecretKey = "accessTokenSecretKey";
 
 	private static Twitter twitter;
 	private static TwitterFactory twitterFactory;
 	private static AccessToken accessToken;
 
 	private Activity mActivity;
 
 	public TheTwitter(String id, Activity activity) {
 		this.id = id;
 		mActivity = activity;
 
 		SocialSessionStore.restore(SocialWrapper.TWITTER, this, mActivity);	
 
 		accessToken = getAccessTokenInternal();
 
 		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
 		configurationBuilder.setOAuthConsumerKey(consumerKey);
 		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
 		Configuration configuration = configurationBuilder.build();
 		twitterFactory = new TwitterFactory(configuration);
 
 		tag = "Corso12-Social-Twitter";
 	}
 
 	public Twitter getTwitter() {
 		if(twitter == null) {
 			Log.i(tag, "twitter non autenticato. twitter == null");
 			if(accessToken == null) {
 				Log.i(tag, "Non hai l'accessToken");
 				return twitter = null;
 			} else {
 				Log.i(tag, "Mi autentico con l'accessToken");
 				connectionData.put(accessTokenKey, accessToken.getToken());
 				connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
 				SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
 				return twitter = twitterFactory.getInstance(accessToken);
 			}
 		} else {
 			setPropers();
 			Log.i(tag, "twitter già autenticato");
 			return twitter;	
 		}
 	}
 
 	private void setPropers() {
 		connectionData.put(accessTokenKey, accessToken.getToken());
 		connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
 		SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
 	}
 
 	public static void setPropersAccessToken(AccessToken accessTokenTemp) {
 		accessToken = accessTokenTemp;
 		twitter = twitterFactory.getInstance(accessToken);
 	}
 
 	public static void deletePropers() {
 		Log.i(tag, "elimino l'istanza di twitter che ho istanziato non autenticata");
 		twitter = null;
 	}
 
 	private AccessToken getAccessTokenInternal() {
 		if (connectionData == null)
 			connectionData = new HashMap<String, String>();
 		String s0 = connectionData.get(accessTokenKey);
 		String s1 = connectionData.get(accessTokenSecretKey);
 		if(s0 == null && s1 == null) {
 			Log.i(tag, "AccessToken null");
 			return null;
 		}
 		Log.i(tag, "AccessToken valido, lo ritorno");
 		return new AccessToken(s0, s1);
 	} 
 
 	private void removeAccessToken() {
 		connectionData.remove(accessTokenKey);
 		connectionData.remove(accessTokenSecretKey);
 
 		SocialSessionStore.clear(SocialWrapper.TWITTER, mActivity);
 	}
 
 	private Boolean checkIstanceTwitter() {
 		accessToken = getAccessTokenInternal();
 
 		if(accessToken == null)
 			return false;
 		else 
 			return true;
 	}
 
 	private void OAuthLogin() throws InvalidAuthenticationException {
 		try {				
 			accessToken = getAccessTokenInternal();
 
 			if (accessToken != null) {
 				Log.i(tag, "accessToken già salvato, istanzio twitter");
 				twitter = twitterFactory.getInstance(accessToken);
 			} else {
 				Log.i(tag, "Effettuo il logIn via WebView");
 				// Non ho già l'accessToken, devo recuperarmelo
 				twitter = twitterFactory.getInstance();
 				RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
 				String authUrl = requestToken.getAuthenticationURL();
 				Intent i = new Intent(mActivity.getApplicationContext(), TheTwitterWebView.class);
 				Bundle b = new Bundle();
 				b.putString("url", authUrl);
 				b.putSerializable("twitter", twitter);
 				b.putSerializable("requestToken", requestToken);
 				i.putExtras(b);
 				mActivity.startActivity(i);
 			}
 
 		} catch (TwitterException ex) {
 			throw new InvalidAuthenticationException("Authentication could not be performed", ex);
 		}
 	}
 
 	@Override
 	public void authenticate(SocialBaseCallback r) throws InvalidAuthenticationException {
 
 		OAuthLogin();
 
 		if(!checkIstanceTwitter()) {
 			Log.i(tag, "Autenticazione non avvenuta!");
 			throw new InvalidAuthenticationException("Authentication could not be performed", null);
 		}
 		
 	}
 
 	@Override
 	public void deauthenticate() {
 		deletePropers();
 		removeAccessToken();
 	}
 
 	public void selfPost(String msg) throws InvalidAuthenticationException {
 		try {
 			if(getTwitter() != null) {
 				twitter.updateStatus(msg);
 				Log.i(tag, ":-)");
 			} else {
 				Log.i(tag, ":-(");
 			}
 		} catch (TwitterException e) {
 			Log.i(tag, ":-( exception", e);
 			removeAccessToken();
 			throw new InvalidAuthenticationException("Tweet could not be performed, try to reauthenticate", e);
 		}
 	}
 
 	public void postToFriend(String friendID, String msg) throws InvalidSocialRequestException {
 		try {
 			if(getTwitter() != null) {
 				twitter.updateStatus("@" + friendID + " " + msg);
 				Log.i(tag, ":-)");
 			}
 		} catch (TwitterException e) {
 			Log.i(tag, ":-( exception", e);
 			removeAccessToken();
 			throw new InvalidSocialRequestException("Could not tweet this friend, try to reauthenticate", e);
 		}
 	}
 
 	@Override
 	public void getFriendsList(SocialBaseCallback s) throws InvalidSocialRequestException {
 		ArrayList<SocialFriend> friendList = new ArrayList<SocialFriend>();
 
 		if(getTwitter() != null) {
 			long cursor = -1;
 			IDs ids;
 			try {
 				do {
 					ids = twitter.getFollowersIDs(cursor);
 					for (long id : ids.getIDs()) {
 						friendList.add(
 								new SocialFriend(
 										Long.toString(twitter.showUser(id).getId()),
 										twitter.showUser(id).getScreenName(),
 										twitter.showUser(id).getProfileImageURL().toString()));
 					}
 				} while ((cursor = ids.getNextCursor()) != 0);
 				actionResult = ACTION_SUCCESSFUL;
 				Log.i(tag, ":-)");
 				actionResult = ACTION_SUCCESSFUL;
 
 			} catch (TwitterException e) {
 				Log.i(tag, ":-( exception", e);
 				removeAccessToken();
 				throw new InvalidSocialRequestException("Could not retrive the friends list, try to reauthenticate", e);
 			}
 		} else {
 			Log.i(tag, ":-( - ritorno una lista vuota");
 			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
 			removeAccessToken();
 		}
 	}
 
 	@Override
 	public Vector<String[]> getConnectionData() {
 		Vector<String[]> connList = new Vector<String[]>();
 		connList.add(new String[] {accessTokenKey, connectionData.get(accessTokenKey)});
 		connList.add(new String[] {accessTokenSecretKey, connectionData.get(accessTokenSecretKey)});
 		return connList;
 	}
 
 	@Override
 	protected void setConnectionData(Map<String, String> connectionData) {
 		this.connectionData = connectionData;
 	}
 
 	@Override
 	public String getAccessToken() {
 		if (accessToken != null)
 			return accessToken.getToken()+';'+accessToken.getTokenSecret();
 
 		return null;
 	}
 
 	@Override
 	public boolean isAuthenticated() {
		if (!accessToken.getToken().equals("") && (!accessToken.getTokenSecret().equals("")))
 			return true;
 		else
 			return false;
 	}
 }
