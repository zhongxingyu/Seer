 package com.beautifulpromise.application;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.beautifulpromise.R;
 import com.beautifulpromise.application.intro.NotificationService;
 import com.beautifulpromise.common.Var;
 import com.beautifulpromise.common.repository.Repository;
 import com.beautifulpromise.common.utils.MessageUtils;
 import com.beautifulpromise.facebooklibrary.DialogError;
 import com.beautifulpromise.facebooklibrary.Facebook;
 import com.beautifulpromise.facebooklibrary.Facebook.DialogListener;
 import com.beautifulpromise.facebooklibrary.FacebookError;
 import com.beautifulpromise.facebooklibrary.SessionStore;
 import com.beautifulpromise.parser.HttpClients;
 import com.facebook.halo.application.types.User;
 import com.facebook.halo.framework.common.AccessToken;
 
 public class Intro extends Activity {
 	
 	Facebook mFacebook;
 	
 	Button loginButton;
 	
 	Intent intent = new Intent();
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mFacebook = new Facebook(Var.APP_ID);
 		
 		/**
 		 * splash activity 
 		 */
 		setContentView(R.layout.intro);
 		//이미 로긴 되있으면 바로 시작(자동 로그인 처리)
 		if(SessionStore.restore(mFacebook, this)) {
 			//TODO
 			AccessToken.setAccessToken(mFacebook.getAccessToken());
 			loadHandler.sendEmptyMessage(0);
 			handler.sendEmptyMessageDelayed(0, 3000);
 		} else {
 			//Login Button
 			loginButton = (Button) findViewById(R.id.loginButton);
 			loginButton.setVisibility(View.VISIBLE);
 			loginButton.setOnClickListener(buttonClickListener);
 		}
 	}
 	
 	/**
 	 * delay 후 home activity 실행하는 handler
 	 */
 	Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			finish();
             if(setNewMember())
             	Toast.makeText(Intro.this, "Success", Toast.LENGTH_SHORT).show();
            intent.setAction("HomeActivity");
             startActivity(intent);
 		}
 	};
 
 	/**
 	 * 서버에서 데이터 가져오는 handler
 	 */
 	Handler loadHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			Repository.getInstance().setUser();
 		}
 	};
 
 	View.OnClickListener buttonClickListener = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			mFacebook.authorize(Intro.this, Var.PERMISSIONS , new DialogListener() {
                 @Override
                 public void onComplete(Bundle values) {
                 	SessionStore.save(mFacebook, getBaseContext());
                 	//TODO
                     AccessToken.setAccessToken(mFacebook.getAccessToken());
                     Repository.getInstance().setUser();
                     if(setNewMember())
                     	Toast.makeText(Intro.this, "Success", Toast.LENGTH_SHORT).show();
                 	intent.setAction("HomeActivity");
                     startActivity(intent);
                 	finish();
                 }
     
                 @Override
                 public void onFacebookError(FacebookError error) {}
     
                 @Override
                 public void onError(DialogError e) {}
     
                 @Override
                 public void onCancel() {}
             });
 		}
 	};
 	
 	//콜백함수를 사용하기위해
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
         mFacebook.authorizeCallback(requestCode, resultCode, data);
 	}
 	
 	private boolean setNewMember() {
 		User user = Repository.getInstance().getUser();
 		
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("fb_key", user.getId()));
 		params.add(new BasicNameValuePair("fb_name", user.getName()));
 		if(user.getGender().equals("female"))
 			params.add(new BasicNameValuePair("fb_gender", "F"));
 		else if(user.getGender().equals("male"))
 			params.add(new BasicNameValuePair("fb_gender", "M"));
 		else
 			params.add(new BasicNameValuePair("fb_gender", "0"));
 		
 		if(user.getBirthday() != null)
 			params.add(new BasicNameValuePair("fb_birth", user.getBirthday()));
 		else
 			params.add(new BasicNameValuePair("fb_birth", "0000-00-00"));
 		
 		HttpClients client = new HttpClients();
 		String data = client.getUrlToJson(MessageUtils.SET_NEW_MEMBER, params);
 		
 		if(data != null){
 			return client.getResult(data);
 		} else 
 			return false;
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 //		stopService(new Intent(this, NotificationService.class));
 	}
 }
