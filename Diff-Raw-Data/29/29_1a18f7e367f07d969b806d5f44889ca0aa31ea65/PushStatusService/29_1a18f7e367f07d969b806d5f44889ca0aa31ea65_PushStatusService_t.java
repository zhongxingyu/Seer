 package com.renoqiu;
 
 import java.io.IOException;
 
 import com.weibo.sdk.android.Oauth2AccessToken;
 import com.weibo.sdk.android.WeiboException;
 import com.weibo.sdk.android.api.StatusesAPI;
 import com.weibo.sdk.android.net.RequestListener;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 public class PushStatusService extends Service {
 	private Handler handler;
 	private com.renoqiu.LooperThread thread;
 	private SharedPreferences sharedPreferences;
 	private boolean syncFlagForRenren;
 	private boolean syncFlagForWeibo;
 	private ConnectivityManager cm;
 	private NetworkInfo ni;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	private boolean checkNet() {
 		cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (cm == null) {
 			return false;
 	    }
 		ni = cm.getActiveNetworkInfo();
 		if (ni == null || !ni.isAvailable()) {     
 			return false;     
 		}     
 		return true;     
 	}    
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		
 		syncFlagForRenren = sharedPreferences.getBoolean("syncFlag",false);
 		syncFlagForWeibo = sharedPreferences.getBoolean("syncFlagForWeibo",false);
 		if((syncFlagForRenren || syncFlagForWeibo) && checkNet()){
 			if(intent != null){
 				Bundle myBundle = intent.getExtras();
 				String trackName = myBundle.getString("trackName");
 				String artist = myBundle.getString("artist");
 				Log.v("com.renoqiu", trackName);
 				Log.v("com.renoqiu", artist);
 				//Renren
 				String accessTokenForRenren = sharedPreferences.getString("accessToken","");
 				//Weibo
 				String accessTokenForWeibo = sharedPreferences.getString("accessTokenForWeibo","");
 				String expiresTimeForWeibo = sharedPreferences.getString("expiresTimeForWeibo","0");
 	    		if (syncFlagForRenren){
 	    			if(accessTokenForRenren != null && !accessTokenForRenren.equals("")){
 						String requestMethod = "status.set";
 						//接口名称  
 						String url = RenrenPublishHelper.API_URL;
 						String secretKey = RenrenPublishHelper.SECRET_KEY;
 						if(artist == null || artist.equals("")){
 							artist = "unknown";
 						}
 						if(trackName == null || trackName.equals("")){
 							trackName = "xxx";
 						}
 						String message = "我在听" + artist + "的《" + trackName + "》。\r\n 通过我在听发布！";
 						thread = new LooperThread(handler, requestMethod, "1.0", url, accessTokenForRenren, message, secretKey);
 						thread.start(); /* 启动线程 */
 					}else{
 						Toast.makeText(PushStatusService.this, "请登陆人人先！", Toast.LENGTH_SHORT).show();
 						SharedPreferences.Editor editor = sharedPreferences.edit();
 						editor.putBoolean("syncFlag", false);
 						editor.commit();
 					}
 	    		}
 				if (syncFlagForWeibo){
 					if(accessTokenForWeibo != null && !accessTokenForWeibo.equals("")){
 						//expiresTimeForWeibo TODO
 						WeiboActivity.accessToken = new Oauth2AccessToken(accessTokenForWeibo, expiresTimeForWeibo);
 						StatusesAPI api = new StatusesAPI(WeiboActivity.accessToken);
 						String message = "我在听" + artist + "的《" + trackName + "》。\r\n 通过我在听发布！";
			            api.update(message, "", "", new RequestListener(){
 							@Override
 							public void onComplete(String arg0) {
 								// TODO Auto-generated method stub
 								Log.v("com.renoqiu","同步微博成功！");
								Message msg = new Message();
								msg.what = 1;
								msg.obj = "同步微博成功！";
								handler.sendMessage(msg);
 							}
 
 							@Override
 							public void onError(WeiboException arg0) {
 								// TODO Auto-generated method stub
 								Log.v("com.renoqiu","同步微博失败！");
								Message msg = new Message();
								msg.what = 1;
								msg.obj = "同步微博失败！";
								handler.sendMessage(msg);
 							}
 
 							@Override
 							public void onIOException(IOException arg0) {
 								// TODO Auto-generated method stub
 								Log.v("com.renoqiu","同步微博IOException！");
								Message msg = new Message();
								msg.what = 1;
								msg.obj = "同步微博出错了！";
								handler.sendMessage(msg);
 							}});
 
 					}else{
 						Toast.makeText(PushStatusService.this, "请登陆微博先！", Toast.LENGTH_SHORT).show();
 						SharedPreferences.Editor editor = sharedPreferences.edit();
 						editor.putBoolean("syncFlagForWeibo", false);
 						editor.commit();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
 		
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
				Log.v("com.renoqiu",msg.toString());
 				switch (msg.what) {
 					case 0:
 						if((Integer)msg.obj != 1){
 							Toast.makeText(PushStatusService.this, "同步失败！请检查网络是否打开...", Toast.LENGTH_SHORT).show();
 						}else{
 							Toast.makeText(PushStatusService.this, "同步成功！", Toast.LENGTH_SHORT).show();
 						}
 						break;
					case 1:
						Toast.makeText(PushStatusService.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
						break;
 				}
 			}
 		};
 	}
 }
