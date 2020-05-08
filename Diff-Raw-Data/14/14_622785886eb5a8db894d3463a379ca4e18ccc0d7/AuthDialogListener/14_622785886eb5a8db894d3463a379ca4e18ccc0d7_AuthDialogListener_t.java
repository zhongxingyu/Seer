 package com.quanleimu.entity;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.util.Helper;
 import com.weibo.net.AccessToken;
 import com.weibo.net.DialogError;
 import com.weibo.net.Weibo;
 import com.weibo.net.WeiboDialogListener;
 import com.weibo.net.WeiboException;
 import com.weibo.net.WeiboParameters;
 import android.content.Context;
 
 public class AuthDialogListener implements WeiboDialogListener {
 
 	public interface AuthListener{
 		public void onComplete();
 	}
 	private Context context = null;
 	private AuthListener listener = null;
 	public AuthDialogListener(Context ctx, AuthListener listener){
 		context = ctx;
 		this.listener = listener;
 	}
 	private boolean inAuthorize = false;
 	
 	public void setInAuthrize(boolean in){
 		inAuthorize = in;
 	}
 	@Override
 	public void onComplete(Bundle values) {		
 		if(!inAuthorize) return;
 		inAuthorize = false;
 		String token = values.getString("access_token");
 		String expires_in = values.getString("expires_in");
 		AccessToken accessToken = new AccessToken(token, QuanleimuApplication.kWBBaixingAppSecret);
 		accessToken.setExpiresIn(expires_in);			
 		Weibo.getInstance().setAccessToken(accessToken);
 		QuanleimuApplication.setWeiboAccessToken(accessToken);
		Helper.saveDataToLocate(context, "weiboToken", new WeiboAccessTokenWrapper(token, accessToken.getExpiresIn()));
 
 		
 		String url="https://api.weibo.com/2/account/get_uid.json";
         WeiboParameters wp=new WeiboParameters();
         wp.add("access_token",accessToken.getToken());
         try {
             String s = 
             		Weibo.getInstance().request(context, url, wp, "GET", Weibo.getInstance().getAccessToken());
             JSONObject obj = new JSONObject(s);
             if(obj != null){
             	String uid = obj.getString("uid");
             	url = "https://api.weibo.com/2/users/show.json";
             	wp.add("uid", uid);
             	s = Weibo.getInstance().request(context, url, wp, "GET", Weibo.getInstance().getAccessToken());
             	JSONObject user = new JSONObject(s);
             	String nick = user.getString("screen_name");
     			Helper.saveDataToLocate(context, "weiboNickName", nick);
     			if(null != listener){
     				listener.onComplete();
     			}
             }
         } catch (WeiboException e) {
             e.printStackTrace();
         }
         catch(JSONException e){
         	e.printStackTrace();
         }
 	}
 
 	@Override
 	public void onError(DialogError e) {
 		inAuthorize = false;
 		Toast.makeText(context,
 				"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onCancel() {
 		inAuthorize = false;
 		Toast.makeText(context, "Auth cancel",
 				Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onWeiboException(WeiboException e) {
 		inAuthorize = false;
 		Toast.makeText(context,
 				"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
 				.show();
 	}
 
 }
