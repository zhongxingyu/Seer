 // $Id: ChkConnectReceiver.java,v 1.4 2011/10/26 12:32:33 hiroaki-mac Exp hiroaki-mac $
 /**
  * 
  */
 package jp.group.home.android.chkConnect;
 
 import java.net.URI;
 import java.util.Date;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.PowerManager;
 import android.util.Log;
 
 /**
  * @author hiroaki0404@gmail.com
  *
  */
 public class ChkConnectReceiver extends BroadcastReceiver{
 	final long DEFAULT_INTERVAL = 10; // sec
 	
 	@Override
 	public void onReceive(final Context context, Intent intent) {
 		Log.d("chkConnect", "ChkConnectReceiver::onReceive");
 		ChkConnectUtil util = new ChkConnectUtil();
 		util.cancelNext(context);
 		Log.d("chkConnect", "Reset next launch");
 		final String intentAction = intent.getAction();
 		final long lastSentTime = intent.getLongExtra("time", 0);
 		if (intentAction != null) {
 			Log.d("chkConnect", intentAction);
 			if (intentAction.equals(Intent.ACTION_USER_PRESENT)||util.isWifiConnected(intent)) {
 	    		if (!util.getSettings(context)) {
 					util.notify(context, R.drawable.nowifi, context.getString(R.string.ntfy_seterror));
 					util.logging(context, "Settings are broken");
 	    			return;
 	    		}
 	    		long interval = util.getInterval();
 	    		if (intentAction.equals("android.net.wifi.STATE_CHANGE")) {
 //	    			util.notify(context, R.drawable.icon, "Set check timer for the Wifi on");
 	    			interval = util.getWifiDelay();
 	    		}
 	    		util.setNextLaunch(context.getApplicationContext(), interval);
 	    		util.logging(context, "Next check is " + String.valueOf(interval) + "sec later.");
 	    		return;
 			}
 		} else {
 			final String msg = (0L == lastSentTime)? "intent is null": "timer";
 //			util.notify(context, R.drawable.icon, msg);
 			Log.d("chkConnect", msg);
 		}
 		// intentの送信時間がセットされていなければ、timer起動ではないから接続チェックはしない
 		if (0L == lastSentTime) {
 			util.logging(context, "No intent");
 			return;
 		}
 		// Wifiが繋がっているか？状態が変わったらEventが投げられるので、拾っているはずだが、一応確認
 		WifiManager wMgr = (WifiManager)(context.getSystemService(Context.WIFI_SERVICE));
 		WifiInfo info = wMgr.getConnectionInfo();
 		if ((info != null)&&(info.getSSID() != null)&&(info.getSupplicantState() == SupplicantState.COMPLETED)) {
 			// Wifi接続している場合、接続確認のthreadをキックする。イベント処理ルーチンで余計な時間をかけない。
			SharedPreferences sp;
			sp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
			final long delay = sp.getLong("interval", 1L);
			
 			// 次回起動を暫定セット。キックしたthreadがシステムにkillされたら、次回の起動がセットされないかもしれないから。
			// 間隔が長い方をセットして、sleep中に次の起動が行われないようにする。
			util.setNextLaunch(context.getApplicationContext(), Math.max(DEFAULT_INTERVAL, delay));
 			util.notify(context, R.drawable.icon, "");
 			// 確認Thread(親)
 			(new Thread(new Runnable() {
 				public void run() {
 					final ChkConnectUtil util = new ChkConnectUtil();
 					if (util.getSettings(context)) {
 						// Screen on/offで間隔を変える
 						PowerManager pwMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 						final URI uri = util.getChkURL();
 						final long interval = pwMgr.isScreenOn()?util.getInterval():util.getScreenOffInterval();
 						final long now = (new Date()).getTime();
 						if (now - lastSentTime < interval) {
 							util.logging(context, "Too short interval.");
 							return;
 						}

						final long timeLimit = delay*1000-200;
 						Thread chkThread = new Thread(new Runnable() {
 							public void run() {
								util.chkConnect(context, uri, delay, timeLimit);
 							}
 						});
 						chkThread.start();
 						// waitする
 						try {
							util.logging(context, "Start waiting " + String.valueOf(timeLimit/1000) + " sec.");
							Thread.sleep(timeLimit);
 							util.logging(context, "Finish waiting");
 						} catch (InterruptedException e) {
 							util.notify(context, R.drawable.nowifi, context.getString(R.string.ntfy_error));
 							util.logging(context, "Try to kill check thread.");
 							chkThread.interrupt();
 							Log.d("chkConnect", e.getMessage());
 							e.printStackTrace();
 							return;
 						}
 						// まだthreadが生きているということは、接続できていないこと。
 						if (chkThread.isAlive()) {
 							util.setForceEnd(true);
 							chkThread.interrupt();
 							util.disconnectWifi(context);
 							util.notify(context, R.drawable.nowifi, context.getString(R.string.ntfy_timeout));
 							util.setNextLaunch(context, interval);
 							util.logging(context, "Connection failed. Try " + String.valueOf(interval) + " sec later.");
 						} else {
 //							util.notify(context, R.drawable.okwifi, context.getString(R.string.ntfy_alive));
 						}
 					} else {
 			        	// 設定がおかしくなったので、終了する
 						util.notify(context, R.drawable.nowifi, context.getString(R.string.ntfy_seterror));
 						util.logging(context, "Thread failed to get settings.");
 					}
 				}
 			})).start();
 			util.logging(context, "Thread start");
 		} else {
 			// Wifi接続していなかったから、何もしない。
 			util.notify(context, R.drawable.nowifi, context.getString(R.string.ntfy_discn));
 		}
 	}
 }
