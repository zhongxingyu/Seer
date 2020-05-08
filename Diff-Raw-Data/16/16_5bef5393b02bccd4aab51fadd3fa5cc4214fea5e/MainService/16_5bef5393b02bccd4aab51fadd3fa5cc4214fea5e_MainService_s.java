 package jp.tkgktyk.wimaxhelperforaterm;
 
import java.util.Calendar;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import jp.tkgktyk.wimaxhelperforaterm.my.MyApplication;
 import jp.tkgktyk.wimaxhelperforaterm.my.MyFunc;
 import jp.tkgktyk.wimaxhelperforaterm.my.MyLog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.IBinder;
 
 /**
  * A background service for application. This service will be running always.
  * This acts an event listener that listen some broadcast and start threads
  * which access to WiMAX Router(Aterm). The thread returns results by
  * broadcasting(explicit or secondary casting).
  */
 public class MainService extends Service {
 	
 	private static final int NOTIFICATION_ID = R.xml.main_preference;
 
 	/**
 	 * Receives these actions.
 	 *	ACTION_GET_INFO
 	 *		When Aterm's information is updated, notify the new information.
 	 *	ACTION_SCREEN_ON
 	 *		If WiFi is not connecting, try waking up the router. otherwise try
 	 *		getting Aterm's new information.
 	 *	NETWORK_STATE_CHANGED_ACTION
 	 *		When network state is changed, change the notification.
 	 *	WIFI_STATE_CHANGED_ACTION
 	 *		When WiFi state is changed, change the notification.
 	 */
 	private final BroadcastReceiver _receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			final String action = intent.getAction();
 			
 			if (action.equals(AtermHelper.ACTION_GET_INFO)) {
 				_showNotification();
 			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
 				_wakeUp();
 			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
 				// execute updateInfo() once in interval
 //				long elapsed = MyFunc.elapsedTimeInMillis(_getAterm().getInfo().timeInMillis);
 //				long delay = Const.UPDATE_INTERVAL_IN_MILLIS - elapsed;
 //				if (delay < 0)
 //					delay = 0;
 //				Timer timer = new Timer(true);
 //				timer.schedule(new TimerTask() {
 //					@Override
 //					public void run() {
 //						if (_getAterm().isWifiConnected())
 //							_getAterm().updateInfo();
 //					}
 //				}, delay + 1);
 			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
 				// wifi event only
 				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 				switch (info.getState()) {
 				case CONNECTED:
 					MyLog.d("wifi is connected.");
 					_getAterm().forceUpdateInfo();
 					break;
 				case DISCONNECTED:
 					MyLog.d("wifi is disconnected.");
 					_showPrepareNotification();
 					break;
 				}
 			} else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
 				int state
 				= intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
 				switch (state) {
 				case WifiManager.WIFI_STATE_DISABLING:
 				case WifiManager.WIFI_STATE_DISABLED:
 				case WifiManager.WIFI_STATE_UNKNOWN:
 					_showPrepareNotification();
 					break;
 				}
 			}
 		}
 	};
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO ꂽ\bhEX^u
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		MyLog.v("start main service");
 		
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(AtermHelper.ACTION_GET_INFO);
 		filter.addAction(Intent.ACTION_SCREEN_ON);
 //		filter.addAction(Intent.ACTION_SCREEN_OFF);
 		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		this.registerReceiver(_receiver, filter);
 
 		_showPrepareNotification();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		MyLog.v("destroy main service");
 		
 		this.unregisterReceiver(_receiver);
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return Service.START_STICKY;
 	}
 
 	private AtermHelper _getAterm() { return ((MyApplication)this.getApplication()).getAterm(); }
 	
 	/**
 	 * a helper function to show the notification.
 	 * @param content
 	 * specify the notification's text.
 	 * @param antenna
 	 * this number is shown on status bar. if set less than equal 0, is not shown.
 	 */
 	private void _showNotification(String content, int antenna) {
 		// if API level is greater than 11, use Notification.Builder.
 		int icon = 0;
 		if (antenna > 0)
 			icon = R.drawable.ic_stat_connected;
 		else if (antenna == 0)
 			icon = R.drawable.ic_stat_disconnected;
 		else
 			icon = R.drawable.ic_stat_unknown;
 		if (_getAterm().getInfo().charging)
 			content += "i[dj";
 		else if (_getAterm().isRouterDocked())
 			content += "i[dHj";
 		Notification notification = new Notification(
 				icon,
 				content,
 				System.currentTimeMillis()
 				);
 //		notification.number = (antenna > 0)? antenna: 0;
 		Intent intent = new Intent(this, MainActivity.class);
 		PendingIntent contentIntent
 		= PendingIntent.getActivity(this, 0, intent, 0);
 		notification.setLatestEventInfo(
 				this,
 				MyFunc.getAppTitle(),
 				content,
 				contentIntent
 				);
 
 		this.startForeground(NOTIFICATION_ID, notification);
 	}
 	
 	/**@Notify the Aterm's information. */
 	private void _showNotification() {
 		AtermHelper.Info info = _getAterm().getInfo();
 		if (info.isValid()) {
 			String content = String.format("dg: %d{Aobe[: %d%%", info.antenna, info.battery);
 			_showNotification(content, info.antenna);
 		} else {
 			_showUnsupportedNotification();
 		}
 	}
 
 	/** Notify that under preparation. */
 	private void _showPrepareNotification() {
 		_showNotification("WiMAX", 0);
 	}
 	
 	/** Notify that now waking up.@*/
 	private void _showWakeUpNotification() {
 		_showNotification("[gN", 0);
 	}
 	
 	/** Notify that unsupported. */
 	private void _showUnsupportedNotification() {
 		_showNotification("Ή̐ڑł", -1);
 	}
 	
 	/**
 	 * After a wait that is specified Preference, check the WiFi connection.
 	 * if WiFi connection is invalid, try to wake up the router.
 	 * or else update router's information.
 	 */
 	private void _wakeUp() {
 		// After a wait, check the WiFi connection by thread.;
 		AtermHelper aterm = _getAterm();
 		if (aterm.isWifiConnected()) {
 			aterm.updateInfo();
 		} else if (!aterm.isRouterDocked()) {
 			_showWakeUpNotification();
 			_getAterm().wakeUp();
 		} else {
 			long delay = MyFunc.getLongPreference(R.string.pref_key_wifi_scan_wait);
 			Timer timer = new Timer(true);
 			final WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
 			if (wm.isWifiEnabled()) {
 				((WifiManager)getSystemService(WIFI_SERVICE)).startScan();
 				timer.schedule(new TimerTask() {
 					@Override
 					public void run() {
 						MyLog.d("WiFi scan end.");
 						if (wm.isWifiEnabled()) {
 							Set<String> ssids = _getAterm().getInfo().getSsidSet();
 							for (ScanResult r : wm.getScanResults()) {
 								if (ssids.contains(r.SSID)) {
 									MyLog.d("catch the router's radio.");
 									return;
 								}
 							}
 						}
 						MyLog.d("router is not found.");
 						_showWakeUpNotification();
 						_getAterm().wakeUp();
 					}
 				}, delay);
 			}
 		}
 	}
 }
