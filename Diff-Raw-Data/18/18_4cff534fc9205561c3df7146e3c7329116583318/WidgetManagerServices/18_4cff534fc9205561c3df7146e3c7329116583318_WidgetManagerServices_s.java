 /**
  *
  */
 package net.formula97.android.usbtetherwidget;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.widget.RemoteViews;
 
 /**
  * @author kazutoshi
  *
  */
 public class WidgetManagerServices extends Service {
 
 	private static String ACTION_WIDGET_TOUCH = "net.formula97.intent.ACTION_WIDGET_TOUCH";
 	private boolean isTetherConnected = false;
 	//private boolean isUsbTetherApiEnabled = false;
 
 	private String prefName = "TETHER_CONTROLLER";
 
 	RemoteViews rv = null;
 
 	/**
 	 *
 	 */
 	public WidgetManagerServices() {
 		// TODO 自動生成されたコンストラクター・スタブ
 	}
 
 	/* (非 Javadoc)
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO 自動生成されたメソッド・スタブ
 		return null;
 	}
 
 	/* (非 Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 //	@Override
 //	@Deprecated
 //	public void onStart(Intent intent, int flags) {
 //		// TODO onStartはDeprecatedなので、 onStartCommandに書き換える。
 //		super.onStart(intent, flags);
 //
 //		// RemoteViewsに、ウィジェットで使用するレイアウトを指定する
 //		rv = new RemoteViews(getPackageName(), R.layout.layout_1x1);
 //
 //		// インテントの定義
 //		Intent touchIntent = new Intent();
 //		touchIntent.setAction(ACTION_WIDGET_TOUCH);
 //
 //		// PendingIntentを使い、タッチイベントが発生したらクリック処理を行うようにする
 //		PendingIntent pendingIntent = PendingIntent.getService(this, 0, touchIntent, flags);
 //		rv.setOnClickPendingIntent(R.id.iv_connection_status, pendingIntent);
 //
 //		if (ACTION_WIDGET_TOUCH.equals(intent.getAction())) {
 //			onClick(rv);
 //		}
 //
 //		// ウィジェットの状態を更新する
 //		ComponentName cn = new ComponentName(getPackageName(), TetherChangeWidghet.class.getName());
 //		AppWidgetManager awm = AppWidgetManager.getInstance(this);
 //		awm.updateAppWidget(cn, rv);
 //
 //		// ブロードキャストレシーバーを定義する
 //		//   ちなみに、ここでaddActionしているブロードキャストは、API Level 14でDeprecatedに
 //		//   なっておる....
 //		String UMS_CONNECTED    = "android.intent.action.UMS_CONNECTED";
 //		String UMS_DISCONNECTED = "android.intent.action.UMS_DISCONNECTED";
 //
 //		IntentFilter intentFilter = new IntentFilter();
 //		intentFilter.addAction(UMS_CONNECTED);
 //		intentFilter.addAction(UMS_DISCONNECTED);
 //
 //		registerReceiver(usbConnEvtRcvr, intentFilter);
 //	}
 
 	/* (非 Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 
 		// RemoteViewsに、ウィジェットで使用するレイアウトを指定する
 		rv = new RemoteViews(getPackageName(), R.layout.layout_1x1);
 
 		// インテントの定義
 		Intent touchIntent = new Intent();
 		touchIntent.setAction(ACTION_WIDGET_TOUCH);
 
 		// PendingIntentを使い、タッチイベントが発生したらクリック処理を行うようにする
 		PendingIntent pendingIntent = PendingIntent.getService(this, 0, touchIntent, flags);
 		rv.setOnClickPendingIntent(R.id.iv_connection_status, pendingIntent);
 
 		if (ACTION_WIDGET_TOUCH.equals(intent.getAction())) {
 			onClick(rv);
 		}
 
 		// ウィジェットの状態を更新する
 		ComponentName cn = new ComponentName(getPackageName(), TetherChangeWidghet.class.getName());
 		AppWidgetManager awm = AppWidgetManager.getInstance(this);
 		awm.updateAppWidget(cn, rv);
 
 		// ブロードキャストレシーバーを定義する
 		//   ちなみに、ここでaddActionしているブロードキャストは、API Level 14でDeprecatedに
 		//   なっておる....
 		String UMS_CONNECTED    = "android.intent.action.UMS_CONNECTED";
 		String UMS_DISCONNECTED = "android.intent.action.UMS_DISCONNECTED";
 
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(UMS_CONNECTED);
 		intentFilter.addAction(UMS_DISCONNECTED);
 
 		// USBテザリングAPIが使用可能かどうかのフラグをセットする
 		Object objUsbTether = getUsbTetherApi(true);
 
 		registerReceiver(usbConnEvtRcvr, intentFilter);
 
 		return START_STICKY_COMPATIBILITY;
 	}
 
 	private void onClick(RemoteViews rv) {
 		// TODO クリック時にUSBテザリングをON/OFFする処理に変更する。
 
 		// View IDとイメージソースのIDを格納する変数を初期化
 		int viewId = R.id.iv_connection_status;
 		int srcId = 0;
 
 		// テザリング接続中か否かで、貼り付けるイメージソースを変える
 		if (isTetherConnected) {
 			srcId = R.drawable.pc_connected;
 		} else {
 			srcId = R.drawable.ic_launcher;
 		}
 
 		// イメージソースをウィジェットの描画エリアに張り付ける
 		rv.setImageViewResource(viewId, srcId);
 		isTetherConnected = !isTetherConnected;
 	}
 
 	/* (非 Javadoc)
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		// TODO 自動生成されたメソッド・スタブ
 		super.onDestroy();
 		startService(new Intent(this, WidgetManagerServices.class));
 	}
 
 	/**
 	 * USBテザリングAPIを呼び出す。
 	 * 非公開APIをリフレクションで呼び出しているので、動作しない場合もあるかもしれない。
 	 * @param doesSetPreference boolean型、trueならプリファレンスにUSBテザリングAPIが使用可能かどうかの設定を書く､falseなら書かない。
 	 * @return Object型、呼び出しに成功している場合はUSbテザリングAPIのオブジェクトを、失敗している場合はnullを返す。
 	 */
 	@SuppressWarnings("rawtypes")
 	public Object getUsbTetherApi(boolean doesSetPreference) {
 		Class c = null;
 		Field f = null;
 		Object iconn = null;
 
 		// ConnectivityManagerのサービス一覧を呼び出す
 		ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
 
 		// CONNECTIVITY_SERVICEのクラスを呼び出す
 		try {
 			c = Class.forName(cm.getClass().getName());
 		} catch (ClassNotFoundException e) {
 			// スタックトレースをLogCatに流す
 			e.printStackTrace();
 		}
 
 		// 呼びだしたクラスからフィールドを取得する
 		try {
 			f = c.getDeclaredField("mService");
 			f.setAccessible(true);
 		} catch (NoSuchFieldException e) {
 			// スタックトレースをLogCatに流す
 			e.printStackTrace();
 		}
 
 		try {
 			iconn = f.get(cm);
 		} catch (IllegalArgumentException e) {
 			// スタックトレースをLogCatに流す
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// スタックトレースをLogCatに流す
 			e.printStackTrace();
 		}
 
 		// Trueの場合は、プリファレンスに設定を残す。
 		if (doesSetPreference) {
 			// オブジェクトの取得に成功している場合はiconnがnullではないので、
 			// nullでない場合はプリファレンスのIsTetheringEnableにtrueをセットする。
 			if (iconn != null) {
 				setUsbTetherEnableFlags(true);
 			} else {
 				setUsbTetherEnableFlags(false);
 			}
 		}
 		return iconn;
 	}
 
 	/**
 	 * WiMAX接続のON/OFFを設定する。
 	 * 非公開APIをリフレクションで呼び出しているので、動作しない場合もあるかもしれない。
 	 * なお、WiMAXが使用可能か否かの判定は行っていないが、APIが用意されていない場合は
 	 * NoSuchMethodExceptionが発生するので、一応はわかるかも？
 	 * @param connectionEnabled boolean型、接続をONにするときはtrue、OFFにするときはfalseをセットする。
 	 * @return boolean型、WiMAX接続の切り替えに成功したらtrue、失敗したらfalseを返す。
 	 */
 	public boolean enableWimaxConnection(boolean connectionEnabled) {
 		boolean ret = false;
 
 		// WiMAXのサービスを呼び出す
 		Object wimaxmgr = (Object)getBaseContext().getSystemService("wimax");
 
 		// WiMAX接続を切り替える
 		// キャッチ可能な例外発生時は、スタックトレースをLogCatに流す
 		try {
 			Method wimaxEnable = wimaxmgr.getClass().getMethod("setWimaxEnabled", new Class[] { Boolean.TYPE });
 
 			wimaxEnable.invoke(wimaxmgr, new Object[] { connectionEnabled });
 
 			ret = true;
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		return ret;
 	}
 
 	/**
 	 * USB接続関連イベントを、ブロードキャストレシーバーで受信する。
 	 */
 	private BroadcastReceiver usbConnEvtRcvr = new BroadcastReceiver() {
 
 		// 画像リソースのID
 		int pictTetherConnected = R.drawable.ic_launcher;	// テザリング接続中
 		int pictPcConnected = R.drawable.pc_connected;		// PCと接続中
 		int pictNotConnected = R.drawable.not_connected;	// PCと切断中
 		int pictNotWork = R.drawable.not_work;				// 動作せず
 
 		/* (非 Javadoc)
 		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
 		 */
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO 自動生成されたメソッド・スタブ
 
 		}
 	};
 
 	/**
 	 * USBテザリングAPIが使用可能かどうかのフラグを、プリファレンスにセットする。
 	 * @param enabledFlag boolean型、USBテザリングAPIが使用可能な場合はtrueを、
 	 *                    使用不能な（＝APIがない）場合はfalseを、それぞれセットする。
 	 */
 	private void setUsbTetherEnableFlags(boolean enabledFlag) {
 		// デフォルトプレファレンスを呼び出す
 		String key = "IsTetheringEnable";
 
 		SharedPreferences pre = this.getSharedPreferences(prefName, MODE_PRIVATE);
 		//SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor edit = pre.edit();
 
 		// boolean値を書きこむ
 		edit.putBoolean(key, enabledFlag);
 		edit.commit();
 	}
 }
