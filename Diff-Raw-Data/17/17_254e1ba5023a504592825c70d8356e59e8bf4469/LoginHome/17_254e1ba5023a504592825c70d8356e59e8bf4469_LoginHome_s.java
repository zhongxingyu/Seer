 /**
  * 
  */
 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import twitter4j.http.AccessToken;
 import whyq.WhyqApplication;
 import whyq.WhyqMain;
 import whyq.controller.WhyqListController;
 import whyq.interfaces.IServiceListener;
 import whyq.interfaces.LoginTWDelegate;
 import whyq.model.ResponseData;
 import whyq.model.Store;
 import whyq.model.User;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.Constants;
 import whyq.utils.SharedPreferencesManager;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.utils.facebook.FacebookConnector;
 import whyq.utils.facebook.sdk.DialogError;
 import whyq.utils.facebook.sdk.Facebook;
 import whyq.utils.facebook.sdk.Facebook.DialogListener;
 import whyq.utils.facebook.sdk.FacebookError;
 import android.app.Activity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.share.twitter.TwitterActivity;
 import com.whyq.R;
 
 /**
  * @author Linh Nguyen This activity supports to authenticate the user by
  *         user-name and password. Also, it can communicate to FB and Twitter to
  *         do authentication as well
  */
 public class LoginHome extends Activity
 		implements IServiceListener,LoginTWDelegate {
 
 	static final int LOGIN_TWITTER = 1;
 	private Button facebookLogin;
 	private Button twitterLogin;
 	public static boolean isLoginFb = false;
 	public static boolean isTwitter = false;
 	// private ProgressDialog loadingDialog;
 	private ProgressBar progressBar;
 	private SharedPreferences prefs;
 	private FacebookConnector facebookConnector;
 	public static Context context;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login_home);
 
 		TextView textView = (TextView) findViewById(R.id.permpingTitle);
 		Typeface tf = Typeface.createFromAsset(getAssets(),
 				"ufonts.com_franklin-gothic-demi-cond-2.ttf");
 		if (textView != null) {
 			textView.setTypeface(tf);
 		}
 		context = LoginHome.this;
 		prefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		facebookLogin = (Button) findViewById(R.id.btnFbLogin);
 		twitterLogin = (Button) findViewById(R.id.btnLoginTw);
 		// login = (Button) findViewById(R.id.loginPerm);
 		progressBar = (ProgressBar) findViewById(R.id.progressBar);
 		facebookConnector = new FacebookConnector(Constants.FACEBOOK_APP_ID,
 				this, getApplicationContext(), new String[] { Constants.EMAIL,
 						Constants.PUBLISH_STREAM });
 		// Login with Facebook button
 		facebookLogin.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(final View v) {
 				// TODO Auto-generated method stub
 
 
 				Facebook mFacebook;
 				String token = null;
 				mFacebook = new Facebook(Constants.FACEBOOK_APP_ID);
 				final Activity activity = LoginHome.this;
 				mFacebook.authorize(activity, new String[] { "email",
 						"status_update", "user_birthday" },
 						new DialogListener() {
 							@Override
 							public void onComplete(Bundle values) {
 								// Log.d("", "=====>"+values.toString());
 								WhyqUtils permutils = new WhyqUtils();
 								String accessToken = values
 										.getString(Facebook.TOKEN);
 								permutils.saveFacebookToken("oauth_token",
 										accessToken, getApplication());
 								// // Check on server
 								HashMap<String, String> params = new HashMap<String, String>();
 								params.put("access_token", accessToken);
 								exeLoginFacebook(accessToken);
 //								exeSendMessage(true, params);
 							}
 
 							@Override
 							public void onFacebookError(FacebookError error) {
 
 							}
 
 							@Override
 							public void onError(DialogError e) {
 
 							}
 
 							@Override
 							public void onCancel() {
 								// cancel press or back press
 							}
 						});
 			}
 		});
 
 		// Twitter Login button
 		twitterLogin.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				isTwitter = true;
 				SharedPreferencesManager shareManager = new SharedPreferencesManager(
 						WhyqApplication.Instance().getApplicationContext());
 				AccessToken a = shareManager.loadTwitterToken();
 				if(a!=null){
 		        	String token = a.getToken();
 		        	String tokenSecret = a.getTokenSecret();
 		        	exeLoginTwitter(token, tokenSecret);
 				}else{
 					Intent i = new Intent(LoginHome.this, TwitterActivity.class);
 					startActivityForResult(i, LOGIN_TWITTER);					
 				}
 				
 			}
 		});
 		
 		String token = WhyqApplication.Instance().getToken();
 		if (token == null || token.equals("")) {
 
 		} else {
 			Intent intent = new Intent(LoginHome.this, WhyqMain.class);
 			startActivity(intent);
 			finish();
 		}
 	}
 //	public Handler loginFbTw = new Handler() {
 //		@Override
 //		public void handleMessage(Message msg) {
 //			HashMap<String, String> params = (HashMap<String, String>) msg.obj;
 //			int isFb = msg.what;
 //			if(isFb == 1){
 //				String access_token = params.get("access_token");
 //				exeLoginFacebook(access_token);
 //			}else{
 //				String token = params.get("token");
 //				String tokenSecret = params.get("token_secret");
 //				exeLoginTwitter(token, tokenSecret);
 //			}
 //			
 //		}
 //	};
 //	public void exeSendMessage(boolean isFb, HashMap<String, String> params){
 //		Message message = new Message().obtain();
 //		message.obj = params ;
 //		if(isFb){
 //			message.what = 1;
 //			loginFbTw.sendMessage(message);
 //		}else{
 //			message.what = 0;
 //			loginFbTw.sendMessage(message);
 //		}
 //	}
 //	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    // Check which request we're responding to
 	    if (requestCode == LOGIN_TWITTER) {
 	        // Make sure the request was successful
 	        if (resultCode == RESULT_OK) {
 	        	String token = data.getStringExtra("token");
 	        	String tokenSecret = data.getStringExtra("token_secret");
 //	        	HashMap<String, String>params = new HashMap<String, String>();
 //	        	params.put("token", token);
 //	        	params.put("token_secret", tokenSecret);
 //	        	exeSendmessage
 	        	exeLoginTwitter(token, tokenSecret);
 	        }
 	    }
 	}
 	@Override
 	public void onResume() {
 		super.onResume();
 
 	}
 
 	public void onPause() {
 		super.onPause();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	private void showLoadingDialog(String title, String msg) {
 		// loadingDialog = new ProgressDialog(getParent());
 		// loadingDialog.setMessage(msg);
 		// loadingDialog.setTitle(title);
 		// loadingDialog.setCancelable(true);
 		// loadingDialog.show();
 		progressBar.setVisibility(View.VISIBLE);
 	}
 
 	private void dismissLoadingDialog() {
 		// if (loadingDialog != null && loadingDialog.isShowing())
 		// loadingDialog.dismiss();
 		if (progressBar.getVisibility() == View.VISIBLE) {
 			progressBar.setVisibility(View.GONE);
 		}
 	}
 
 	public void onClickedSignup(View v) {
 		Intent intent = new Intent(LoginHome.this, JoinWhyqActivity.class);
 		startActivity(intent);
 	}
 
 	public void onClickedLogin(View v) {
 		Intent intent = new Intent(LoginHome.this, LoginWhyqActivity.class);
 		startActivity(intent);
 	}
 
 	public void onClickedSkipLogin(View v) {
 		Intent intent = new Intent(LoginHome.this, WhyqMain.class);
 		startActivity(intent);
 	}
 
 	public void exeLoginFacebook(String accessToken) {
 		Service service = new Service(LoginHome.this);
 		service.loginFacebook(accessToken);
 	}
 
 	public void exeLoginTwitter(String oauthToken, String oauthTokenSecret) {
 		Service service = new Service(LoginHome.this);
 		service.loginTwitter(oauthToken, oauthTokenSecret);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			WhyqMain.back();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		// TODO Auto-generated method stub
 		if (result.isSuccess() == true  && result.getAction() == ServiceAction.ActionLoginFacebook) {
 			ResponseData data = (ResponseData)result.getData();
 			if(data.getStatus().equals("200")){
 				User user = (User)data.getData();
 //				if(user.isLogined()){
 					ListActivity.isLogin = true;
 					ListActivity.loginType = 1;
 					isLoginFb = true;
 					Log.d("LoginHome by Facebook", "result: ");
 					WhyqApplication.Instance().setToken(user);
 //					XMLParser.storePermpingAccount(WhyqApplication._instance.getApplicationContext(), user);
 					Intent intent = new Intent(LoginHome.this, WhyqMain.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);	
 //				}else{
 //					
 //				}
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(context, data.getMessage());
 			}else{
 				Util.showDialog(context, data.getMessage());
 			}
 
 		} else if (result.isSuccess() == true && result.getAction() == ServiceAction.ActionLoginTwitter) {
 
 			ResponseData data = (ResponseData)result.getData();
 			if(data.getStatus().equals("200")){
 				User user = (User)data.getData();
 //				if(user.isLogined()){
 					WhyqApplication.Instance().setToken(user);
 					ListActivity.isLogin = true;
 					ListActivity.loginType = 2;
 //					dismissLoadingDialog();
 					Intent intent = new Intent(LoginHome.this, WhyqMain.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);
 //				}
 //				}else{
 //					String mes = user.getMessageLogin();
 //					android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
 //					builder.setTitle(context.getString(R.string.app_name_title));
 //					builder.setMessage(mes);
 //					final android.app.AlertDialog alertError = builder.create();
 //					alertError.setButton("Login", new DialogInterface.OnClickListener() {
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							alertError.dismiss();
 //						}
 //					});
 //					alertError.setButton2("Cancel", new DialogInterface.OnClickListener() {
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							alertError.dismiss();
 //						}
 //					});
 //					alertError.show();
 //				}
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(context, data.getMessage());
 			}else{
 				Util.showDialog(context, data.getMessage());
 			}
 
 		}
 	}
 
 	@Override
 	public void onLoginTWSuccess(AccessToken accessToken) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onLoginTWError() {
 		// TODO Auto-generated method stub
 		
 	}
 }
