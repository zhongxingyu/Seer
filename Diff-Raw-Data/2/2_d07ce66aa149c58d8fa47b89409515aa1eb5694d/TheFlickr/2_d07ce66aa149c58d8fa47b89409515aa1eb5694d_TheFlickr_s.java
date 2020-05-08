 package com.megadevs.socialwrapper.theflickr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Vector;
 
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 import com.gmail.yuyang226.flickr.Flickr;
 import com.gmail.yuyang226.flickr.FlickrException;
 import com.gmail.yuyang226.flickr.oauth.OAuth;
 import com.gmail.yuyang226.flickr.oauth.OAuthToken;
 import com.gmail.yuyang226.flickr.uploader.UploadMetaData;
 import com.megadevs.socialwrapper.SocialFriend;
 import com.megadevs.socialwrapper.SocialNetwork;
 import com.megadevs.socialwrapper.SocialSessionStore;
 import com.megadevs.socialwrapper.SocialWrapper;
 
 public class TheFlickr extends SocialNetwork {
 
 	private String oauthTokenKey 		= "oauth_token";
 	private String oauthTokenSecretKey 	= "oauth_token_secret";
 	
 	private OAuth oauthAccess;
 	
 	private static TheFlickr iAmTheFlickr;
 	
 	protected static Uri OAUTH_CALLBACK_URI;
 	
 	private TheFlickrLoginCallback loginCallback;
 	private TheFlickrPostPictureCallback pictureCallback;
 	
 	/**
 	 * !-NOTE: THIS METHOD SHOULD NOT BE USED BY THE END USER!-!
 	 * The correct way is SocialWrapper.getSocialNetwork(SocialWrapper.THEFLICKR)
 	 * 
 	 * Default constructor for TheFlickr class. A context is
 	 * required in order to perform the authentication.
 	 * @param id the SocialNetwork ID
 	 * @param a the actual context (passed from the SocialWrapper)
 	 */
 	public TheFlickr(String id, Activity a) {
 		this.id = id;
 		this.mActivity = a;
 		
 		iAmTheFlickr = this;
 		
 		// restoring previous session, if there is any
 		SocialSessionStore.restore(SocialWrapper.THEFLICKR, this, mActivity);
 
 		tag = "[SW-THEFLICKR]";
 	}
 
 	
 	/**
 	 * This method must be invoked before starting the authentication process.
 	 * It is used to set the keys (public and secret) obtained from the app page
 	 * on Flickr.com
 	 * @param key the public key
 	 * @param secret the secret key
 	 */
 	public void setAuthParams(String key, String secret, String callback) {
 		TheFlickrHelper.setAPIKey(key);
 		TheFlickrHelper.setAPISec(secret);
 		OAUTH_CALLBACK_URI = Uri.parse(callback);
 	}
 	
 	@Override
 	public void authenticate(SocialBaseCallback s) {
 		loginCallback = (TheFlickrLoginCallback) s;
 		
 		if (oauthAccess != null) {
 			Log.i(tag, "you have a valid session, use it wisely!");
 			loginCallback.onLoginCallback(ACTION_SUCCESSFUL);
 		} else {
 			Intent i = new Intent(mActivity.getApplicationContext(), TheFlickrWebView.class);
 			mActivity.startActivity(i);
 		}
 	}
 
 	@Override
 	public void deauthenticate() {
 		oauthAccess = null;
 		
 		SocialSessionStore.clear(SocialWrapper.THEFLICKR, mActivity);
 	}
 
 	@Override
 	public void getFriendsList(SocialBaseCallback s) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	protected Vector<String[]> getConnectionData() {
 		Vector<String[]> v = new Vector<String[]>();
 		if (oauthAccess != null) {
 			v.add(new String[] {oauthTokenKey, oauthAccess.getToken().getOauthToken()});
 			v.add(new String[] {oauthTokenSecretKey, oauthAccess.getToken().getOauthTokenSecret()});
 		}
 		
 		return v;
 	}
 
 	@Override
 	protected void setConnectionData(Map<String, String> connectionData) {
 		if (!connectionData.isEmpty()) {
 			String token = connectionData.get(oauthTokenKey);
 			String secret = connectionData.get(oauthTokenSecretKey);
 			oauthAccess = new OAuth();
 			OAuthToken t = new OAuthToken(token, secret);
 			oauthAccess.setToken(t);
 		}
 	}
 
 	@Override
 	public String getAccessToken() {
 		if (oauthAccess != null)
 			return oauthAccess.getToken().getOauthToken()+";"+oauthAccess.getToken().getOauthTokenSecret();
 		else return null;
 	}
 
 	/**
 	 * !-NOTE: THIS METHOD SHOULD NOT BE USED BY THE END USER!-!
 	 * 
 	 * This method is used by an instance of TheFlickrWebView to pass the token
 	 * values. It is declared public and static so that the above activity can
 	 * call it without this instance of TheFlickr: to avoid misuse of this method,
 	 * it calls finalizeAuth which is private.
 	 * 
 	 * @param oauthToken a valid access token
 	 * @param oauthTokenSecret a valid secret token
 	 */
 	public static void setAccessToken(String oauthToken, String oauthTokenSecret) {
 		iAmTheFlickr.finalizeAuth(oauthToken, oauthTokenSecret);
 	}
 
 	/**
 	 * 
 	 * @param oauthToken
 	 * @param oauthTokenSecret
 	 */
 	private void finalizeAuth(String oauthToken, String oauthTokenSecret) {
 		// complete auth
 		if (oauthToken != null && oauthTokenSecret != null) {
 			oauthAccess = new OAuth();
 			OAuthToken t = new OAuthToken(oauthToken, oauthTokenSecret);
 			oauthAccess.setToken(t);
 			
 			loginCallback.onLoginCallback(ACTION_SUCCESSFUL);
 
 			SocialSessionStore.save(SocialWrapper.THEFLICKR, this, mActivity);
 		}
 		
 		else loginCallback.onErrorCallback("Could not finalize authentication: token / token secret are null", null);
 		loginCallback = null;
 	}
 
 	/**
 	 * This method uploads an image on the logged user's account, along with a message (if present).
 	 * 
 	 * @param image the image data to upload on Flickr.com
 	 * @param message the message along with the image (optional)
 	 * @param s the base callback object
 	 */
 	public void upload(byte[] image, String message, SocialBaseCallback s) {
 		pictureCallback = (TheFlickrPostPictureCallback) s;
 
 		// checking if TheFlickr is authenticated
 		if (oauthAccess != null) {
 			Flickr f = TheFlickrHelper.getInstance().getFlickrAuthed(oauthAccess.getToken().getOauthToken(), oauthAccess.getToken().getOauthTokenSecret());
 			if (image != null) {
 				// checking if a message was passed
 				if (message == null) message = "";
 				String result;
 				try {
 					UploadMetaData meta = new UploadMetaData();
 					meta.setTitle(message);
					result = f.getUploader().upload("", image, meta);
 
 					// parsing response: should return a valid photoID
 					if (result.matches("[0-9]*")) 
 						pictureCallback.onPostPictureCallback(ACTION_SUCCESSFUL);
 					else pictureCallback.onErrorCallback("Upload failed. Reason: " + SOCIAL_NETWORK_ERROR, null);
 					
 				} catch (FlickrException e) {
 					pictureCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
 				} catch (IOException e) {
 					pictureCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
 				} catch (SAXException e) {
 					pictureCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
 				}
 				
 			} else pictureCallback.onErrorCallback("Upload failed: image data is null", null);
 		}
 	}
 	
 	@Override
 	public String getId() {
 		return this.id;
 	}
 
 	@Override
 	public boolean isAuthenticated() {
 		
 		if (oauthAccess != null)
 			return true;
 		else return false;
 	}
 
 	public static abstract class TheFlickrLoginCallback implements SocialBaseCallback {
 		public abstract void onLoginCallback(String result);
 		public void onPostPictureCallback(String result) {};
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error, Exception e); 
 	}
 
 	public static abstract class TheFlickrPostPictureCallback implements SocialBaseCallback {
 		public void onLoginCallback(String result) {};
 		public abstract void onPostPictureCallback(String result);
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error, Exception e); 
 	}
 
 }
