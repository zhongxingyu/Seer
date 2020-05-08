 package com.megadevs.socialwrapper.thefacebook;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Util;
 import com.megadevs.socialwrapper.SocialFriend;
 import com.megadevs.socialwrapper.SocialNetwork;
 import com.megadevs.socialwrapper.SocialSessionStore;
 import com.megadevs.socialwrapper.SocialWrapper;
 import com.megadevs.socialwrapper.exceptions.InvalidAuthenticationException;
 import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;
 
 /**
  * This class models a personal Facebook object. With an instance of 
  * TheFacebook it is possible to authenticate, post messages and retrieve
  * various informations.
  * @author dextor
  *
  */
 public class TheFacebook extends SocialNetwork {
 
 	private static TheFacebook iAmTheFacebook;
 	private Facebook mFacebook;
 	private AsyncFacebookRunner mAsyncRunner;
 
 	private ArrayList<SocialFriend> mFacebookFriends;
 
 	private Activity mActivity;
 
 	private String appID;
 	private String accessToken;
 	private long accessExpires;
 
 	// keys for data storing
 	private final String appIDKey = "app_id";
 	private final String accessTokenKey = "access_token";
 	private final String accessExpiresKey = "access_token_expires";
 
 	// statics for image size
 	public static final String FACEBOOK_PHOTO_SMALL 	= "small";
 	public static final String FACEBOOK_PHOTO_MEDIUM 	= "medium";
 	public static final String FACEBOOK_PHOTO_BIG 		= "big";
 	
 	private String facebookPhotoSize;
 	
 	// static callback refs
 	private TheFacebookLoginCallback loginCallback;
 	private TheFacebookPostCallback postCallback;
 	private TheFacebookFriendListCallback friendslistCallback;
 
 	/**
 	 * Default constructor for TheFacebook class. A context is
 	 * required in order to perform authentication and display
 	 * Facebook dialogs (if the official app is installed).
 	 * @param context
 	 */
 	public TheFacebook(String id, Activity a) {
 		this.id = id;
 		iAmTheFacebook = this;
 		mActivity = a;
 		context = mActivity.getApplicationContext();
 
 		SocialNetwork.tag = "SocialWrapper-Facebook";
 	}
 
 	/**
 	 * This method is used within the callback procedure to
 	 * set the authenticated Facebook object.
 	 * @return the existing instance of TheFacebook
 	 */
 	public static TheFacebook getInstance() {
 		return iAmTheFacebook;
 	}
 
 	/**
 	 * This method is called from outside the wrapper and it is used to
 	 * set the application ID; the instanciation of the other objects 
 	 * (mFacebook and mAsyncRunner) can be done only after this action 
 	 * is completed.
 	 * @param id the application id provided by Facebook
 	 */
 	public void setAppID(String id) {
 		appID = id;
 		mFacebook = new Facebook(appID);
 		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
 
 		// try to restore a previously saved session
 		SocialSessionStore.restore(SocialWrapper.FACEBOOK, this, context);
 	}
 
 	@Override
 	public Vector<String[]> getConnectionData() {
 		Vector<String[]> data = new Vector<String[]>();
 		data.add(new String[] {appIDKey, accessTokenKey});
 		data.add(new String[] {accessTokenKey, mFacebook.getAccessToken()});
 		data.add(new String[] {accessExpiresKey, String.valueOf(mFacebook.getAccessExpires())});
 		return data;
 	}
 
 	@Override
 	protected void setConnectionData(Map<String, String> connectionData) {
 		if (connectionData.size()==0) {
 			this.connectionData = null;
 			accessToken = "";
 			accessExpires = -1;
 		}
 		else {
 			appID = connectionData.get(appIDKey);
 			accessToken = connectionData.get(accessTokenKey);
 			accessExpires = (Long.valueOf(connectionData.get(accessExpiresKey)).longValue());
 			mFacebook.setAccessToken(accessToken);
 			mFacebook.setAccessExpires(accessExpires);
 		}
 	}
 
 	@Override
 	public void authenticate(SocialBaseCallback r) throws InvalidAuthenticationException {
 		// check if a valid session is already available, otherwise perform a full login
 		loginCallback = (TheFacebookLoginCallback)r;
 		if (mFacebook.isSessionValid()) {
 			Log.i(tag, "session valid, use it wisely ;)");
 			loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
 		}
 		else {
 			Log.i(tag, "valid session: " + mFacebook.isSessionValid());
 			Intent i = new Intent(mActivity, TheFacebookActivity.class);
 			mActivity.startActivity(i);
 		}
 	}
 
 	@Override
 	public void deauthenticate() {
		accessToken = "";
		accessExpires = -1;
 		// simply erases any previously stored session in the prefs
 		SocialSessionStore.clear(SocialWrapper.FACEBOOK, context);
 	}
 
 	/**
 	 * This method is used to post on the user's wall. There is no message, since a dialog
 	 * will appear and let the user insert a custom message to post.
 	 * @return a string containing the result of the operation
 	 * @throws InvalidSocialRequestException 
 	 */
 	public void postOnMyWall(SocialBaseCallback s) throws InvalidSocialRequestException {
 		actionResult = null;
 		postCallback = (TheFacebookPostCallback) s;
 		this.mFacebook.dialog(mActivity,
 				"stream.publish",
 				new Bundle(),
 				new PostOnWallDialogListener());
 	}
 
 	/**
 	 * This method is used to post on a friend's wall.
 	 * @param friendID the friend's ID provided by Facebook
 	 * @return a string containing the result of the operation
 	 * @throws InvalidSocialRequestException
 	 */
 	public void postToFriendsWall(String friendID, SocialBaseCallback s) throws InvalidSocialRequestException {
 		friendslistCallback = (TheFacebookFriendListCallback) s;
 		Bundle parameters = new Bundle();
 		parameters.putString("to", friendID);
 		mFacebook.dialog(mActivity,
 				"stream.publish",
 				parameters,
 				new PostOnWallDialogListener());
 	}
 
 	@Override
 	public void getFriendsList(SocialBaseCallback s) throws InvalidSocialRequestException {
 		friendslistCallback = (TheFacebookFriendListCallback) s;
 		mFacebookFriends = new ArrayList<SocialFriend>();
 		mAsyncRunner.request("me/friends", new Bundle(), new FriendListRequestListener());
 	}
 
 
 	/**
 	 * This method is used to let the listeners' implementations
 	 * post their result message: the 'actionResult' field will be
 	 * returned by the proper methods.
 	 * @param r
 	 */
 	public void setActionResult(String r, Exception e) {
 		actionResult = r;
 		actionException = e;
 	}
 
 	/**
 	 * @return the mFacebook
 	 */
 	public Facebook getmFacebook() {
 		return mFacebook;
 	}
 
 
 	@Override
 	public String getAccessToken() {
 		if (accessToken != null)
 			return accessToken;
 
 		return "";
 	}
 
 	@Override
 	public boolean isAuthenticated() {
 		if (accessToken != "") return true;
 		else return false;
 	}
 
 	/**
 	 * Sets the profile picture size for each friend returned from the
 	 * 'getFriendsList' method.
 	 * @param size
 	 */
 	public void setFacebookPhotoSize(String size) {
 		facebookPhotoSize = size;
 	}
 	
 	///
 	///	LISTENERS >IMPLEMENTATIONS< CLASSES, MAY HAVE PRIVATE ACCESS
 	///
 
 	public static abstract class TheFacebookLoginCallback implements SocialBaseCallback {
 		public abstract void onLoginCallback(String result);
 		public void onPostCallback(String result) {};
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error); 
 	}
 
 	public static abstract class TheFacebookPostCallback implements SocialBaseCallback {
 		public void onLoginCallback(String result) {};
 		public abstract void onPostCallback(String result);
 		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
 		public abstract void onErrorCallback(String error);
 	}
 
 	public static abstract class TheFacebookFriendListCallback implements SocialBaseCallback {
 		public void onLoginCallback(String result) {};
 		public void onPostCallback(String result) {};
 		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
 		public abstract void onErrorCallback(String error);
 	}
 	
 	// needs to have package visibility, otherwise TheFacebookActivity cannot perform the login process
 	class AuthDialogListener extends TheFacebookBaseDialogListener {
 
 		@Override
 		public void onComplete(Bundle values) {
 			Log.i(tag, "login performed");
 
 			connectionData = new HashMap<String, String>();
 			connectionData.put(appIDKey, appID);
 			connectionData.put(accessTokenKey, mFacebook.getAccessToken());
 			connectionData.put(accessExpiresKey, String.valueOf(mFacebook.getAccessExpires()));
 
 			// the valid session is saved in the app prefs
 			SocialSessionStore.save(SocialWrapper.FACEBOOK, iAmTheFacebook, context);
 			actionResult = SocialNetwork.ACTION_SUCCESSFUL;
 
 			if (loginCallback != null) {
 				loginCallback.onLoginCallback(actionResult);
 				loginCallback = null;
 			}
 		}
 	}
 
 	private class FriendListRequestListener extends TheFacebookBaseRequestListener {
 
 		@Override
 		public void onComplete(String response, Object state) {
 			// now the answer gets parsed (according to Facebook's APIs)
 			JSONObject json;
 			try {
 				json = Util.parseJson(response);
 				
 				final JSONArray friends = json.getJSONArray("data");
 
 				for (int i=0; i<friends.length(); i++) {
 					JSONObject j = friends.getJSONObject(i);
 
 					String id = j.getString("id");
 					String name = j.getString("name");
 					String img = "http://graph.facebook.com/"+id+"/picture?type="+facebookPhotoSize;
 
 					mFacebookFriends.add(new SocialFriend(id, name, img));
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			} catch (FacebookError e) {
 				e.printStackTrace();
 			}
 
 			actionResult = ACTION_SUCCESSFUL;
 			if (friendslistCallback != null) {
 				friendslistCallback.onFriendsListCallback(actionResult, mFacebookFriends);
 				friendslistCallback = null;
 			}
 		}	
 	}
 
 	private class PostOnWallDialogListener extends TheFacebookBaseDialogListener {
 
 		@Override
 		public void onComplete(Bundle values) {
 			actionResult = ACTION_SUCCESSFUL;
 
 			if (postCallback != null) {
 				postCallback.onPostCallback(actionResult);
 				postCallback = null;
 			}
 		}
 	}
 
 	///
 	///	LISTENERS >ABSTRACT< CLASSES, PRIVATE ACCESS
 	///
 
 	private abstract class TheFacebookBaseDialogListener implements DialogListener {
 
 		private void forwardResult() {
 			if (loginCallback != null)
 				loginCallback.onErrorCallback(actionResult);
 			else if (postCallback != null)
 				postCallback.onErrorCallback(actionResult);
 			else if (friendslistCallback != null)
 				friendslistCallback.onErrorCallback(actionResult);
 		}
 
 		public void onFacebookError(FacebookError e) {
 			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR,null);
 			forwardResult();
 			Log.d(tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
 		}
 
 		public void onError(DialogError e) {
 			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,null);
 			forwardResult();
 			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);   
 		}
 
 		public void onCancel() {
 			TheFacebook.this.setActionResult(SocialNetwork.ACTION_CANCELED,null);
 			forwardResult();
 			Log.d(tag, SocialNetwork.ACTION_CANCELED);
 		}
 	}
 
 	private abstract class TheFacebookBaseRequestListener implements RequestListener {
 
 		private void forwardResult() {
 			if (loginCallback != null)
 				loginCallback.onErrorCallback(actionResult);
 			else if (postCallback != null)
 				postCallback.onErrorCallback(actionResult);
 			else if (friendslistCallback != null)
 				friendslistCallback.onErrorCallback(actionResult);
 		}
 
 		public void onFacebookError(FacebookError e, final Object state) {
 			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR,null);
 			forwardResult();
 			Log.d(tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
 		}
 
 		public void onFileNotFoundException(FileNotFoundException e, final Object state) {
 			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
 			forwardResult();
 			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
 		}
 
 		public void onIOException(IOException e, final Object state) {
 			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
 			forwardResult();
 			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
 		}
 
 		public void onMalformedURLException(MalformedURLException e, final Object state) {
 			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
 			forwardResult();
 			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
 		}
 	}
 }
