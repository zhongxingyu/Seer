 package com.quanleimu.broadcast;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.app.NotificationManager;
 import android.app.Notification;
 import android.app.PendingIntent;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.Util;
 
 import android.net.ConnectivityManager;
 import android.content.BroadcastReceiver;
 import android.content.IntentFilter;
 
 public class BXNotificationService extends Service {
 	private static final String TAG = "BXService";
 	private static final int HELLO_ID = 0x11223344;
 	private static final int MSG_CHECK_UPDATE = 1;
 	private static final int MSG_PUSH_RETURN = 2;
 	private String json = null;
 	private BroadcastReceiver networkStateReceiver;
 
 	private void showNotification(String ticket, String title, String content) {
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		// Notification的滚动提示
 		String tickerText = (ticket == null || ticket.equals("")) ? "百姓网客户端有新版本更新"
 				: ticket;
 		// Notification的图标，一般不要用彩色的
 		int icon = R.drawable.app_icon;
 
 		// contentTitle和contentText都是标准的Notification View的内容
 		// Notification的内容标题，拖下来后看到的标题
 		String contentTitle = (title == null || title.equals("")) ? "百姓网有新版本啦"
 				: title;
 		// Notification的内容
 		String contentText = (content == null || content.equals("")) ? "赶紧去更新"
 				: content;
 
 		// Notification的Intent，即点击后转向的Activity
 		Intent notificationIntent = new Intent(this,
				com.quanleimu.activity.SplashActivity.class);
 		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				notificationIntent, 0);
 
 		// 创建Notifcation
 		Notification notification = new Notification(icon, tickerText,
 				System.currentTimeMillis());
 		// 设定Notification出现时的声音，一般不建议自定义
 		notification.defaults |= Notification.DEFAULT_SOUND;
 		// 设定如何振动
 		notification.defaults |= Notification.DEFAULT_VIBRATE;
 		// 指定Flag，Notification.FLAG_AUTO_CANCEL意指点击这个Notification后，立刻取消自身
 		// 这符合一般的Notification的运作规范
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		notification.setLatestEventInfo(this, contentTitle, contentText,
 				contentIntent);
 		// 显示这个notification
 		mNotificationManager.notify(HELLO_ID, notification);
 	}
 
 	class GetNotificationThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "pushNotification";
 			ArrayList<String> list = new ArrayList<String>();
 			UserBean user = (UserBean) Util.loadDataFromLocate(BXNotificationService.this, "user");
 			list.add("userid=" + (user == null ? "" : URLEncoder.encode(user.getId())));
 			
 			Object timeObj = Util.loadDataFromLocate(BXNotificationService.this, "pushNotification");
 			if(timeObj != null){
 				list.add("time=" + URLEncoder.encode((String)timeObj));
 			}
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				myHandler.sendEmptyMessage(MSG_PUSH_RETURN);
 
 			} catch (UnsupportedEncodingException e) {
 				myHandler
 						.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				myHandler
 						.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (Communication.BXHttpException e) {
 
 			}
 		}
 	}
 
 	private void doGetPushInfo() {
 		(new Thread(new GetNotificationThread())).start();
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_CHECK_UPDATE:
 				doGetPushInfo();
 				myHandler.sendEmptyMessageDelayed(MSG_CHECK_UPDATE, 3600000);
 				break;
 			case MSG_PUSH_RETURN:
 				if (json != null) {
 					String time = null, ticket = null, title = null, content = null;
 					try {
 						JSONObject jsonObject = new JSONObject(json);
 						
 						if(jsonObject.has("error")){
 							JSONObject errorjs = jsonObject.getJSONObject("error");
 							if(errorjs != null && errorjs.has("code")){
 								if(0 != errorjs.getInt("code"))
 									break;
 							}
 						}
 						
 						if (jsonObject.has("time")) {
 							time = jsonObject.getString("time");
 						}
 						if (jsonObject.has("ticket")) {
 							ticket = jsonObject.getString("ticket");
 						}
 						if (jsonObject.has("title")) {
 							title = jsonObject.getString("title");
 						}
 						if (jsonObject.has("content")) {
 							content = jsonObject.getString("content");
 						}
 
 						BXNotificationService.this.showNotification(ticket, title, content);
 						Util.saveDataToLocate(BXNotificationService.this, "pushNotification", time);
 						
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		networkStateReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String action = intent.getAction();
 
 				if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
 					myHandler.removeMessages(MSG_CHECK_UPDATE);
 					if (isInternetConnected()) {
 						myHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
 					}
 				}
 			}
 		};
 
 		IntentFilter filter = new IntentFilter(
 				ConnectivityManager.CONNECTIVITY_ACTION);
 		registerReceiver(networkStateReceiver, filter);
 	}
 
 	@Override
 	public void onDestroy() {
 		myHandler.removeMessages(MSG_CHECK_UPDATE);
 		unregisterReceiver(networkStateReceiver);
 		Log.d("service", "unregister receiver!!~~~~!!~~~");
 	}
 
 	@Override
 	public void onStart(Intent intent, int startid) {
 		if (isInternetConnected()) {
 			myHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
 		}
 	}
 
 	private boolean isInternetConnected() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		return cm.getActiveNetworkInfo() != null
 				&& cm.getActiveNetworkInfo().isAvailable()
 				&& cm.getActiveNetworkInfo().isConnected();
 	}
 }
