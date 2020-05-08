 package com.tabbie.android.radar;
 
 /**
  * AuthenticateActivity.java
  * 
  * Created on: August 17, 2012
  * Author: Justin Knutson
  * 
  * 
  * Launch this activity to authenticate
  * against facebook and tabbie, returning
  * the login info (name, token, etc.) to
  * the calling activity so it can request
  * updates from the tabbie server
  */
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.tabbie.android.radar.http.ServerGetRequest;
 import com.tabbie.android.radar.http.ServerPostRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 public class AuthenticateActivity extends ServerThreadActivity {
 	
 	private static final String TAG = "Authenticate Activity";
 	
 	private final Facebook facebook = new Facebook("217386331697217");
 	
 	private String fbAccessToken = null;
 	private String tabbieAccessToken = null;
 	private String facebookName = null;
 	private long expires = 0;
 	
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.authenticate_background);
 	    ((ImageView) findViewById(R.id.loading_spin)).startAnimation(AnimationUtils
 	            .loadAnimation(this, R.anim.rotate));
 		
 		final Intent data = this.getIntent();
 		
 		fbAccessToken = data.getStringExtra("token");
 		expires = data.getLongExtra("expires", 0);
 		
 		if (fbAccessToken!=null) {
 		  facebook.setAccessToken(fbAccessToken);
 		}
 		
 		if (expires!=0) {
 		  facebook.setAccessExpires(expires);
 		}
 		
 		if (facebook.isSessionValid()) {
 			Log.i(TAG, "Session is valid");
 			sendServerRequest(new ServerGetRequest(
 			    "https://graph.facebook.com/me/?access_token="
 			    		+ facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
 		} else {
 			facebook.authorize(this, new String[] { "email" }, new DialogListener() {
 			    public void onComplete(final Bundle values) {
 				    sendServerRequest(new ServerGetRequest("https://graph.facebook.com/me/?access_token="
 				    		+ facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
 			    }
 			    public void onFacebookError(final FacebookError e) {
 			    	Log.e(TAG, "Facebook Error");
 			    }
 			    public void onError(final DialogError e) {
 			    	Log.e(TAG, "DialogError");
 			    }
 			    public void onCancel() {
 			    	Log.i(TAG, "Facebook Login Canceled");
 			    }
 			});
 		}
 	}
 
 	@Override
 	protected synchronized boolean handleServerResponse(final ServerResponse resp) {
 		
 		final JSONObject json = resp.parseJsonContent();
 		
 		if(json==null) {
 			return false;
 		}
 		
 		switch(resp.responseTo) {
 		case FACEBOOK_LOGIN:
 			if(json.has("id"))
 			{
 				try {
 					facebookName = json.getString("first_name") + " "
 					+ json.getString("last_name").substring(0, 1) + ".";
 				} catch (final JSONException e) {
 					e.printStackTrace();
 					return false;
 				}
 				this.runOnUiThread(new Runnable() {
 					public void run() {
 						final ServerPostRequest req = new ServerPostRequest(
 							ServerThread.TABBIE_SERVER + "/mobile/auth.json",
 							MessageType.TABBIE_LOGIN);
 						
 						req.params.put("fb_token", facebook.getAccessToken());
 						
 						sendServerRequest(req);
 					}
 				});
 			} else return false;
 			break;
 			
 		case TABBIE_LOGIN:
 			if (json.has("token")) {
 				try {
 					tabbieAccessToken = json.getString("token");
 				} catch (final JSONException e) {
 					e.printStackTrace();
 					return false;
 				} finally {
 					final Intent data = new Intent();
 					data.putExtra("fbAccessToken", fbAccessToken);
 					data.putExtra("tabbieAccessToken", tabbieAccessToken);
 					data.putExtra("facebookName", facebookName);
 					data.putExtra("expires", expires);
 					this.setResult(RESULT_OK, data);
 					finish();
 				}
 			} else {
 				return false;
 			}
 			break;
 		}
 		return true;
 	}
 }
