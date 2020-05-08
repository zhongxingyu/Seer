 package com.wable;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.wable.apiproxy.APIProxyLayer;
 import com.wable.apiproxy.IAPIProxyCallback;
 import com.wable.login.PasswordFindActivity;
 import com.wable.login.RegisterActivity;
 import com.wable.main.MainActivity;
 import com.wable.mypage.MypageActivity;
 import com.wable.mypage.RequestListActivity;
 import com.wable.util.Logger;
 
 public class WableActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
 	
 	private Context context;
 	private Facebook facebook;
 	private SharedPreferences pref;
 	private Button loginOk;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login_main);  
         context = this;
         
         findViewById(R.id.btnFacebook).setOnClickListener(this);
         findViewById(R.id.btnLoginFind).setOnClickListener(this);
         findViewById(R.id.btnLoginRegister).setOnClickListener(this);
         
         loginOk = (Button)findViewById(R.id.btnLogin);
         loginOk.setOnClickListener(this);
         
         EditText et = (EditText)findViewById(R.id.editLoginPass);
         et.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_DONE) loginOk.performClick();				
 				return false;
 			}
 		});
         
              
         APIProxyLayer.Instance().Login("cc", "111111", new IAPIProxyCallback(){
 
 			@Override
 			public void OnCallback(boolean success, JSONObject json) {
 				// TODO Auto-generated method stub
 				if(success)
 				{
 					Logger.Instance().Write(json.toString());
 					
					APIProxyLayer.Instance().GetMyInfo(new IAPIProxyCallback(){
 
 						@Override
 						public void OnCallback(boolean success, JSONObject json) {
 							// TODO Auto-generated method stub
 							if(success)
 							{
 								Logger.Instance().Write(json.toString());
 							}
 							else Logger.Instance().Write("Fail to GetMyInfo");
 						}
 						
 					});
 				}else 	Logger.Instance().Write("Fail to login");
 			}
         	
         });
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
 
 	@Override
 	public void onClick(View v) {
 		
 		Intent intent;
 		switch (v.getId()) {
 		
 		case R.id.btnFacebook:
 			
 			facebook = new Facebook("180729825379631");
 	        
 	        pref = getPreferences(MODE_PRIVATE);
 	        String access_token = pref.getString("access_token", null);
 	        long expires = pref.getLong("access_expires", 0);
 	        if(access_token != null) {
 	            facebook.setAccessToken(access_token);
 	        }
 	        if(expires != 0) {
 	            facebook.setAccessExpires(expires);
 	        }
 
 	        
 	        
 			
 			facebook.authorize(WableActivity.this, new DialogListener() {
 		        @Override
 		        public void onComplete(Bundle values) {
 		        	
 		        	Editor editor = pref.edit();
 	                editor.putString("access_token", facebook.getAccessToken());
 	                editor.putLong("access_expires", facebook.getAccessExpires());
 	                editor.commit();
 		        }
 
 		        @Override
 		        public void onFacebookError(FacebookError error) {
 		        	
 		        }
 
 		        @Override
 		        public void onError(DialogError e) {
 		        	
 		        }
 
 		        @Override
 		        public void onCancel() {
 		        	
 		        }
 		    }); 
 			
 			
 			break;
 			
 		case R.id.btnLoginFind:
 			intent = new Intent(context, PasswordFindActivity.class);
 			startActivity(intent);
 			break;
 			
 		case R.id.btnLogin:
 			//Toast.makeText(context, "Login OK", Toast.LENGTH_SHORT).show();
 			intent = new Intent(context, MainActivity.class);
 			startActivity(intent);			
 			overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
 			finish();
 			
 			break;
 			
 		case R.id.btnLoginRegister:
 			intent = new Intent(context, RegisterActivity.class);
 			startActivity(intent);
 			break;
 
 		}
 	}
 }
