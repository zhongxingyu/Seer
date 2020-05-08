 package com.dary.xmpp.receivers;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 
 import com.dary.xmpp.IncallService;
 import com.dary.xmpp.MainService;
 import com.dary.xmpp.MyApp;
 import com.dary.xmpp.Tools;
 import com.dary.xmpp.ui.MainActivity;
 
 public class ConnectionChangeReceiver extends BroadcastReceiver {
 
 	private static Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
			Tools.doLogAll("Try Relogin");
 			Intent mainserviceIntent = new Intent();
 			mainserviceIntent.setClass(MyApp.getContext(), MainService.class);
 			MyApp.getContext().startService(mainserviceIntent);
 
 			Intent incallserviceIntent = new Intent();
 			incallserviceIntent.setClass(MyApp.getContext(), IncallService.class);
 			MyApp.getContext().startService(incallserviceIntent);
 			super.handleMessage(msg);
 		}
 	};
 
 	@Override
 	public void onReceive(final Context context, Intent intent) {
		Tools.doLogAll("Connectivty Change");
 		MyApp myApp = (MyApp) context.getApplicationContext();
 		if (myApp.getStatus() != MainActivity.DEBUG) {
 			MainService.sendMsg(MainActivity.NOT_LOGGED_IN);
 
 			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 			boolean isAutoReconnect = mPrefs.getBoolean("isAutoReconnect", true);
 			if (isAutoReconnect && myApp.getIsShouldRunning()) {
 
 				NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 
 				// ConnectivityManager conManager = (ConnectivityManager)
 				// MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 				// NetworkInfo activeNetInfo =
 				// conManager.getActiveNetworkInfo();
 				// Tools.doLogJustPrint("netInfo " + netInfo.isConnected() + " "
 				// + netInfo.getType());
 				// Tools.doLogJustPrint("activeNetInfo " +
 				// activeNetInfo.isConnected() + " " + activeNetInfo.getType());
 
 				if (netInfo != null && netInfo.isAvailable() && !netInfo.isFailover() && netInfo.isConnected()
 						&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
 					if (null == MainService.connection || MainService.connection.isAuthenticated() != true) {
 						// 通过延迟发消息的方式使得在短时间如果收到多次连接改变的广播(并且网络可用),仅登录一次
 						int delay = 3000;
 						handler.removeMessages(0);
 						handler.sendEmptyMessageDelayed(0, delay);
 					}
 				}
 			}
 		}
 	}
 }
