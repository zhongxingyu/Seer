 package carnero.battery;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.PixelFormat;
 import android.os.BatteryManager;
 import android.os.IBinder;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 
 public class Main extends Service {
 
 	private StatusView mStatusView;
 	private BatteryStatusReceiver mReceiver;
 	private NotificationManager mNotificationManager;
 	// constants
 	private static final int NOTIFICATION_ID = 47;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		// battery status view
 		mStatusView = new StatusView(this);
 
 		final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // over anything else (even lockscreen and expanded status bar)
 				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, // above status bar
 				PixelFormat.TRANSLUCENT
 		);
 		lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
 		lp.setTitle(getString(R.string.app_name));
 
 		final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
 		wm.addView(mStatusView, lp);
 
 		// notification
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		final Notification.Builder builder = new Notification.Builder(this)
 				.setOngoing(true)
 				.setSmallIcon(R.drawable.ic_notification)
 				.setTicker(getString(R.string.app_name))
 				.setContentTitle(getString(R.string.notification_loading))
 				.setContentText("");
 
 		startForeground(NOTIFICATION_ID, builder.build());
 
 		// battery status receiver
 		mReceiver = new BatteryStatusReceiver();
 
 		final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 		registerReceiver(mReceiver, filter);
 	}
 
 	@Override
 	public void onDestroy() {
 		if (mReceiver != null) {
 			unregisterReceiver(mReceiver);
 			mReceiver = null;
 		}
 
 		if (mStatusView != null) {
 			final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
 			wm.removeView(mStatusView);
 			mStatusView = null;
 		}
 
 		super.onDestroy();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	// classes
 
 	private class BatteryStatusReceiver extends BroadcastReceiver {
 
 		private int mStatus = -1;
 		private int mStatusOld = -1;
 		private int mHealth = -1;
 		private float mTemp = 0;
 		private int mLevel = 0;
 		private int mScale = 0;
 		private boolean mCharging = false;
 		private int mPercent;
 		private long mWhen = System.currentTimeMillis();
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			mStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
 			mHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
 			mTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10; // centigrade -> celsius
 			mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
 			mScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
 			mCharging = (mStatus == BatteryManager.BATTERY_STATUS_CHARGING || mStatus == BatteryManager.BATTERY_STATUS_FULL);
 
 			if (mStatusView != null) {
 				mStatusView.onBatteryChanged(mCharging, mLevel, mScale);
 			}
 
 			if (mStatus != mStatusOld) {
 				mWhen = System.currentTimeMillis();
 
 				mStatusOld = mStatus;
 			}
 
 			if (mNotificationManager != null) {
 				final StringBuilder sb = new StringBuilder();
 				final Notification.Builder nb = new Notification.Builder(Main.this)
 						.setOngoing(true)
 						.setSmallIcon(R.drawable.ic_notification);
 
 				nb.setContentTitle(Integer.toString(mPercent) + "%");
 
 				mPercent = (int) (((float) mLevel / (float) mScale) * 100);
 
 				if (mStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
 					sb.append(getString(R.string.notification_charging));
 				} else if (mStatus == BatteryManager.BATTERY_STATUS_FULL) {
 					sb.append(getString(R.string.notification_full));
 				} else {
 					sb.append(getString(R.string.notification_discharging));
 				}
 
 				if (mHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
 					sb.append(", ");
 					sb.append(mTemp);
 					sb.append("\u00B0C");
 
 					nb.setLights(getResources().getColor(R.color.led_critical), 250, 250);
				} else if (mPercent < 15) {
 					nb.setLights(getResources().getColor(R.color.led_critical), 1000, 500);
 				}
 				nb.setWhen(mWhen);
 				nb.setContentText(sb.toString());
 
 				mNotificationManager.notify(NOTIFICATION_ID, nb.build());
 			}
 		}
 	}
 }
