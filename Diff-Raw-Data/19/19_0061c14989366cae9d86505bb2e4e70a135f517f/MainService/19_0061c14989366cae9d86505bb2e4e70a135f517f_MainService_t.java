 package com.dary.xmpp.service;
 
 import java.util.Locale;
 
 import org.jivesoftware.smack.AndroidConnectionConfiguration;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.SmackAndroid;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.packet.Presence;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 import com.dary.xmpp.application.MyApp;
 import com.dary.xmpp.cmd.CmdBase;
 import com.dary.xmpp.receivers.BatteryReceiver;
 import com.dary.xmpp.receivers.SMSReceiver;
 import com.dary.xmpp.tools.Tools;
 import com.dary.xmpp.ui.MainActivity;
 import com.dary.xmpp.ui.PreferencesActivity;
 import com.dary.xmpp.xmpp.MsgListener;
 
 public class MainService extends Service {
 
 	public static Connection connection;
 	public static String notifiedAddress;
 	public static String loginAddress;
 	private String password;
 	private String serverHost;
 	private String serverPort;
 	private String serverDomain;
 	private String autoServerHost;
 	private int autoServerPort;
 	private String autoServerDomain;
 	private String resource;
 	private boolean isAutoReconnect;
 	private boolean isDebugMode;
 	public SMSReceiver smsReceiver = new SMSReceiver();
 	private BatteryReceiver batteryReceiver = new BatteryReceiver();
 	private boolean isCustomServer;
 	private int tryReconnectCount;
 	public static Chat chat;
 
 	public static Handler tryReconnectHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Tools.doLogAll("Try Relogin");
 			Intent mainServiceIntent = new Intent();
 			mainServiceIntent.setClass(MyApp.getContext(), MainService.class);
 			MyApp.getContext().startService(mainServiceIntent);
 
 			Intent incallServiceIntent = new Intent();
 			incallServiceIntent.setClass(MyApp.getContext(), IncallService.class);
 			MyApp.getContext().startService(incallServiceIntent);
 			super.handleMessage(msg);
 		}
 	};
 
 	@Override
 	public void onCreate() {
 		// 启动InCallService
 		Intent incallserviceIntent = new Intent();
 		incallserviceIntent.setClass(MainService.this, IncallService.class);
 		startService(incallserviceIntent);
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// 需要移除Handler队列中的消息,以防出现用户点击登录,随后又因超时时见到再次登录的情况.
 		tryReconnectHandler.removeMessages(0);
 		if (switchPrefs()) {
 			if (isSetComplete()) {
 				// 启动登录线程
 				LoginInThread loginInThread = new LoginInThread();
 				Thread thread = new Thread(loginInThread);
 				thread.setName("LoginThread");
 				thread.start();
 			}
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	// 登录线程
 	class LoginInThread implements Runnable {
 
 		public void run() {
 
 			// 登录中,发送消息,更新UI
 			sendMsg(MainActivity.LOGGING);
 			// 尝试将登录的记录存储下来,先暂时只存储到普通的文本文件中
 			Tools.doLogAll("Login");
 
 			ConnectionConfiguration config = null;
 			MyApp myApp = (MyApp) getApplication();
 			myApp.setIsShouldRunning(true);
 			SmackAndroid.init(myApp);
 			if (isCustomServer) {
 				config = new AndroidConnectionConfiguration(serverHost, Integer.parseInt(serverPort), serverDomain);
 			} else {
 				try {
 					config = new AndroidConnectionConfiguration(autoServerHost, autoServerPort, autoServerDomain);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			// config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
 			// config.setReconnectionAllowed(false);
 			// config.setSendPresence(false);
 			// config.setCompressionEnabled(false);
 			// config.setSecurityMode(SecurityMode.enabled);
 			// config.setSASLAuthenticationEnabled(true);
 
 			connection = new XMPPConnection(config);
 			try {
 				Tools.doLogJustPrint("Connect to Server");
 				connection.connect();
 				// SASLAuthentication.supportSASLMechanism("PLAIN", 0);
 			} catch (Exception e) {
 				Tools.doLogAll("Connection Failed");
 				doNotification(MainService.this, "Connection Failed");
 				sendMsg(MainActivity.CONNECTION_FAILED);
 				tryReconnect();
 				e.printStackTrace();
 				return;
 			}
 			Tools.doLogPrintAndFile("Verify Password");
 			try {
 				connection.login(loginAddress, password, resource);
 				// connection.login(loginAddress, password, Tools.getTimeStr());
 			} catch (Exception e) {
 				Tools.doLogAll("Login Failed");
 				doNotification(MainService.this, "Login Failed");
 				sendMsg(MainActivity.LOGIN_FAILED);
 				e.printStackTrace();
 				return;
 			}
 			// 如果用户在登录时取消了登录,并且登录成功,需要Disconnect
 			if (!myApp.getIsShouldRunning()) {
 				if (connection.isConnected()) {
 					Presence presence = new Presence(Presence.Type.unavailable);
 					connection.sendPacket(presence);
 					connection.disconnect();
 				}
 			}
 			// 需要先判断用户是否已经停止登录
 			if (myApp.getIsShouldRunning()) {
 				// Tools.Vibrator(MainService.this, 500);
 				Tools.doLogAll("Login Successful");
 				doNotification(MainService.this, "Login Successful");
 
 				// 登录成功后发送消息通知Activity改变按钮状态
 				sendMsg(MainActivity.LOGIN_SUCCESSFUL);
 				// 登录成功后将tryReconnectCount置0
 				tryReconnectCount = 0;
 				ChatManager chatmanager = connection.getChatManager();
 
 				// 注册消息监听器
 				chat = chatmanager.createChat(notifiedAddress.toLowerCase(Locale.getDefault()), new MsgListener());
 				// 登录成功之后再在程序动态的注册电量改变,短信的广播接收器,注册电量改变的接收器时会设置Presence
 				registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 				registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
 
 				// 登录成功后发送消息,用于测试
 				if (isDebugMode) {
 					CmdBase.sendMessageAndUpdateView(chat, "Login Successful");
 				}
 			}
 		}
 	}
 
 	// 如果正在登录中时进行中断,并不会立即处理
 	@Override
 	public void onDestroy() {
 		Tools.doLogAll("Service Destroy");
 		MyApp myApp = (MyApp) getApplication();
 		myApp.setIsShouldRunning(false);
 		sendMsg(MainActivity.NOT_LOGGED_IN);
 		if (connection.isConnected()) {
 			Presence presence = new Presence(Presence.Type.unavailable);
 			connection.sendPacket(presence);
 			connection.disconnect();
 		}
 		Intent incallserviceIntent = new Intent();
 		incallserviceIntent.setClass(MainService.this, IncallService.class);
 		stopService(incallserviceIntent);
 		// 反注册广播接收器
 		try {
 			unregisterReceiver(batteryReceiver);
 			unregisterReceiver(smsReceiver);
 		} catch (Exception e) {
 			// 尚未注册
 			e.printStackTrace();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private void getSetting() {
 		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		isCustomServer = mPrefs.getBoolean("isCustomServer", false);
 		Tools.doLogJustPrint("isCustomServer " + isCustomServer);
 		serverHost = mPrefs.getString("serverHost", "");
 		Tools.doLogJustPrint("serverHost " + serverHost);
 		serverPort = mPrefs.getString("serverPort", "5222");
 		Tools.doLogJustPrint("serverPort " + serverPort);
 		serverDomain = mPrefs.getString("serverDomain", "");
 		Tools.doLogJustPrint("serverDomain " + serverDomain);
 		loginAddress = mPrefs.getString("loginAddress", "");
 		Tools.doLogJustPrint("loginAddress " + loginAddress);
 		password = mPrefs.getString("password", "");
 		Tools.doLogJustPrint("password " + password);
 		notifiedAddress = mPrefs.getString("notifiedAddress", "");
 		Tools.doLogJustPrint("notifiedAddress " + notifiedAddress);
 		resource = mPrefs.getString("resource", "");
 		Tools.doLogJustPrint("resource " + resource);
 		isAutoReconnect = mPrefs.getBoolean("isAutoReconnect", true);
 		Tools.doLogJustPrint("isAutoReconnect " + isAutoReconnect);
 		isDebugMode = mPrefs.getBoolean("isDebugMode", false);
 		Tools.doLogJustPrint("isDebugMode " + isDebugMode);
 		if (loginAddress.length() != 0) {
 			String address[] = loginAddress.split("@");
 			autoServerHost = address[address.length - 1];
 			autoServerDomain = autoServerHost;
 			autoServerPort = 5222;
 			Tools.doLogJustPrint("autoServerHost " + autoServerHost);
 			Tools.doLogJustPrint("autoServerPort " + autoServerPort);
 			Tools.doLogJustPrint("autoServerDomain " + autoServerDomain);
 		}
 	}
 
 	private String getCurrentNetwork() {
 		// TODO 这里判断的并不完整,以后再修改
 		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
 		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (wifi.equals(State.CONNECTED)) {
 			WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
 			return wifiManager.getConnectionInfo().getSSID();
 		} else if (mobile.toString().equals(State.CONNECTED)) {
 			return "Mobile";
 		}
 		return "";
 	}
 
 	public static void sendMsg(int tag) {
 		MyApp myApp = (MyApp) MyApp.getContext();
 		myApp.setStatus(tag);
 		// 登录中,发送消息,更新UI
 		if (null != MainActivity.MsgHandler) {
 			// 考虑修改为,当Activity启动的时候去读取状态
 			Message msg = new Message();
 			msg.what = tag;
 			MainActivity.MsgHandler.sendMessage(msg);
 		}
 	}
 
 	private void doNotification(Context context, String str) {
 		// 需要先判断用户是否已经停止登录
 		if (((MyApp) getApplication()).getIsShouldRunning()) {
 			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 			String notificationType = mPrefs.getString("notificationType", "none");
 			if (notificationType.equals("none")) {
 
 			} else if (notificationType.equals("notification")) {
 				Tools.makeNotification(context, str);
 			} else if (notificationType.equals("vibrate")) {
 				Tools.Vibrator(context, 1000);
 			} else if (notificationType.equals("sound")) {
 				Tools.makeSound(context);
 			} else if (notificationType.equals("notification,vibrate")) {
 				Tools.makeNotification(context, str);
 				Tools.Vibrator(context, 1000);
 			} else if (notificationType.equals("notification,sound")) {
 				Tools.makeNotification(context, str);
 				Tools.makeSound(context);
 			} else if (notificationType.equals("vibrate,sound")) {
 				Tools.Vibrator(context, 1000);
 				Tools.makeSound(context);
 			} else if (notificationType.equals("notification,vibrate,sound")) {
 				Tools.makeNotification(context, str);
 				Tools.Vibrator(context, 1000);
 				Tools.makeSound(context);
 			}
 		}
 	}
 
 	// 如果配置不全,显示Toast
 	// 如果是自定义服务器,判断应有区别
 	private boolean isSetComplete() {
 		getSetting();
 		if (isCustomServer) {
 			if (serverHost.equals("") || serverPort.equals("") || serverDomain.equals("")) {
 				sendMsg(MainActivity.SET_INCOMPLETE);
 				Tools.doLogAll("Set Incomplete");
 				return false;
 			}
 		}
 		if (loginAddress.equals("") || password.equals("") || notifiedAddress.equals("")) {
 			sendMsg(MainActivity.SET_INCOMPLETE);
 			Tools.doLogAll("Set Incomplete");
 			return false;
 		}
 		return true;
 	}
 
 	private void tryReconnect() {
 		int timeout;
 		tryReconnectCount += 1;
 		if (tryReconnectCount < 10) {
 			timeout = 5000 * tryReconnectCount;
 		} else {
 			timeout = 1000 * 60 * 5;
 		}
 		tryReconnectHandler.sendEmptyMessageDelayed(0, timeout);
 	}
 
 	private boolean switchPrefs() {
 		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean autoSwitch = mPrefs.getBoolean("autoSwitchPreferencesBetweenDifferentNetwork", false);
 		if (autoSwitch) {
 			String value = mPrefs.getString("switchPreferencesBetweenDifferentNetwork", "");
 			if (value != "") {
 				String cs[] = value.split("\\|");
 				String net;
 				String prefs = "";
                String currentNetwork = getCurrentNetwork();
 				for (int i = 0; i < cs.length; i++) {
 					net = cs[i].split("\\^")[0];
					if (currentNetwork.equals(net)) {
 						prefs = cs[i].split("\\^")[1];
 						break;
 					} else {
 						if (i == cs.length - 1) {
 							return true;
 						}
 					}
 				}
 				Toast.makeText(MainService.this, "Switch Prefs To " + prefs, Toast.LENGTH_LONG).show();
 				// 需判断为Nothing的情况
 				if (prefs.equals("Nothing")) {
 					return false;
 				}
 				// Do Switch
 				else {
 					PreferencesActivity.switchPreferences(prefs);
 					return true;
 				}
 			}
 			// 这里暂时返回true,让程序在"switchPreferencesBetweenDifferentNetwork"项为空时仍去登录.
 			return true;
 		}
 		return true;
 	}
}
