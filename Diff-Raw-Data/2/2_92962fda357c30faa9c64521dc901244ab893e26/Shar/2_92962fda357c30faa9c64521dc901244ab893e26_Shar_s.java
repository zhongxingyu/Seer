 package com.semasoft.MODe;
 
 import impl.android.com.twitterapime.xauth.ui.WebViewOAuthDialogWrapper;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.twitterapime.rest.Credential;
 import com.twitterapime.rest.TweetER;
 import com.twitterapime.rest.UserAccountManager;
 import com.twitterapime.search.Tweet;
 import com.twitterapime.xauth.Token;
 import com.twitterapime.xauth.ui.OAuthDialogListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class Shar extends Activity implements OnClickListener, OAuthDialogListener {
 	Facebook mfacebook = new Facebook("372314599459769");
 	Button fb,Tw;
 	String FILENAME = "AndroidSSO_data";
     private SharedPreferences mPrefs;
     String access_token;
     String CONSUMER_KEY;
 	
 	
 	  String CONSUMER_SECRET;
 
 	
 	  String CALLBACK_URL;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.social_media);
 		fb = (Button)findViewById(R.id.socfb);
 		Tw = (Button)findViewById(R.id.soctw);
 		Tw.setOnClickListener(this);
 		fb.setOnClickListener(this);
 		mPrefs = getPreferences(MODE_PRIVATE);
 		access_token = mPrefs.getString("access_token", null);
 		
 		
 		
 		
 		
 		
 		
 		
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		mfacebook.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId()){
 		case R.id.socfb:
 			
 			long expires = mPrefs.getLong("access_expires", 0);
 			if( access_token !=null){
 				mfacebook.setAccessToken(access_token);
 			}
 			if(expires !=0)
 			{
 				mfacebook.setAccessExpires(expires);
 			}
 			if(!mfacebook.isSessionValid()){
 			mfacebook.authorize(Shar.this,new String[]{"publish_stream","publish_checkins"}, new DialogListener() {
 				
 				
 				@Override
 				public void onFacebookError(FacebookError e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void onError(DialogError e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void onComplete(Bundle values) {
 					SharedPreferences.Editor editor = mPrefs.edit();
 					editor.putString("access_token",
 							mfacebook.getAccessToken());
 					editor.putLong("access_expires",
 							mfacebook.getAccessExpires());
 					editor.commit();					
 				}
 				
 				@Override
 				public void onCancel() {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 			}
 		
 			try {
 				String response = mfacebook.request("me");
 				Bundle upDets = new Bundle();
 				upDets.putString("message", "this is a test update");
 				upDets.putString("description", "test test test");
 				upDets.putString(Facebook.TOKEN, access_token);
 				response = mfacebook.request("me/feed", upDets, "POST");
 				Toast.makeText(Shar.this, "posted to timeline", Toast.LENGTH_LONG).show();
 			} catch (Exception e) {
 				
 				Log.v("fail" ,e.toString());
 				
 				
 			} 
 			
 			
 			break;
 			
 		case R.id.soctw:
 			
 		 	CONSUMER_KEY = "gwmR0pP27BsuojnyuCNXg";
 			
 			
 			CONSUMER_SECRET = "2pnEQA5hL3WrsLhvFSuy7p4q4R0GEBNtfChBWnQhc";
 
 			
 			CALLBACK_URL = "http://semasoftltd.com";
 			  
 			  //do the actual login to twitter
 			  
 			  //
 		        WebView webView = new WebView(Shar.this);
 		        setContentView(webView);
 		        webView.requestFocus(View.FOCUS_DOWN);
 		        //
 		        WebViewOAuthDialogWrapper pageWrapper =
 		        	new WebViewOAuthDialogWrapper(webView);
 		        //
 				pageWrapper.setConsumerKey(CONSUMER_KEY);
 				pageWrapper.setConsumerSecret(CONSUMER_SECRET);
 				pageWrapper.setCallbackUrl(CALLBACK_URL);
 				pageWrapper.setOAuthListener(this);
 				//
 				pageWrapper.login();
 			  
 			break;
 			
 		}
 		
 	}
 
 	@Override
 	protected void onResume() {
 		 super.onResume();
 	     mfacebook.extendAccessTokenIfNeeded(Shar.this, null);
 	}
 
 	@Override
 	public void onAccessDenied(String message) {
 		showMessage(message);
 	}
 
 	@Override
 	public void onAuthorize(Token accessToken) {
 		
 		Credential c = new Credential(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
 		UserAccountManager uam = UserAccountManager.getInstance(c);
 		//
 		try {
 			if (uam.verifyCredential()) {
 				TweetER.getInstance(uam).post(new Tweet("Music on Demand check out soon " + System.currentTimeMillis()));
 				//
 				showMessage("tweet posted");
 			}
 		} catch (Exception e) {
 			showMessage(e.toString());
 		}
 		
 	}
 
 	@Override
 	public void onFail(String error, String message) {
		showMessage(message + " "+error);
 		
 	}
 	
 	private void showMessage(String msg) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg).setCancelable(false)
 				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		//
 		builder.create().show();
 	}
 
 }
