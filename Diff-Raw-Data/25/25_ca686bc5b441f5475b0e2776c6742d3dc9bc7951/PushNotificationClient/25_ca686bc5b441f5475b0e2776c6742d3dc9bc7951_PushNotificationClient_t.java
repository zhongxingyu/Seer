 package com.tpsoft.pushnotification.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 import com.tpsoft.pushnotification.model.AppParams;
 import com.tpsoft.pushnotification.model.LoginParams;
 import com.tpsoft.pushnotification.model.MyMessage;
 import com.tpsoft.pushnotification.model.NetworkParams;
 import com.tpsoft.pushnotification.model.PublicAccount;
 import com.tpsoft.pushnotification.service.NotifyPushService;
 
 public class PushNotificationClient {
 
 	public static final String TAG_APILOG = "PushNotification-API";
 
 	private static boolean clientStarted = false;
 	private static boolean clientLogon = false;
 
	private Context context;
	private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
	private List<MessageTransceiverListener> listeners = new ArrayList<MessageTransceiverListener>();

 	public PushNotificationClient(Context context) {
 		this.context = context;
 
 		// ע㲥
 		IntentFilter filter = new IntentFilter();
 		filter.addAction("com.tpsoft.pushnotification.NotifyPushService");
 		context.registerReceiver(myBroadcastReceiver, filter);
 	}
 
	public void release() {
		// ע㲥
		context.unregisterReceiver(myBroadcastReceiver);

		for (MessageTransceiverListener listener : listeners) {
			removeListener(listener);
		}
	}

 	/**
 	 * 
 	 * 
 	 * @param listener
 	 *            
 	 */
 	public void addListener(MessageTransceiverListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * ɾ
 	 * 
 	 * @param listener
 	 *            
 	 */
 	public void removeListener(MessageTransceiverListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * Ϣշ
 	 * 
 	 * @param appParams
 	 *            Ӧò
 	 * @param loginParams
 	 *            ¼
 	 * @param networkParams
 	 *            
 	 */
 	public void startMessageTransceiver(AppParams appParams,
 			LoginParams loginParams, NetworkParams networkParams) {
 		if (clientStarted) {
 			for (MessageTransceiverListener listener : listeners)
 				listener.onTransceiverStatus(true);
 			return; // Ѿ
 		}
 
 		Intent serviceIntent = new Intent();
 		serviceIntent
 				.setAction("com.tpsoft.pushnotification.ServiceController");
 		serviceIntent.putExtra("command", "start");
 		serviceIntent.putExtra("com.tpsoft.pushnotification.AppParams",
 				appParams.getBundle());
 		serviceIntent.putExtra("com.tpsoft.pushnotification.LoginParams",
 				loginParams.getBundle());
 		serviceIntent.putExtra("com.tpsoft.pushnotification.NetworkParams",
 				networkParams.getBundle());
 		context.sendBroadcast(serviceIntent); // ͹㲥
 	}
 
 	/**
 	 * ֹͣϢշ
 	 */
 	public void stopMessageTransceiver() {
 		if (!clientStarted) {
 			for (MessageTransceiverListener listener : listeners)
 				listener.onTransceiverStatus(false);
 			return; // Ѿֹͣ
 		}
 
 		Intent serviceIntent = new Intent();
 		serviceIntent
 				.setAction("com.tpsoft.pushnotification.ServiceController");
 		serviceIntent.putExtra("command", "stop");
 		context.sendBroadcast(serviceIntent); // ͹㲥
 	}
 
 	/**
 	 * Ϣ
 	 * 
 	 * @param msgId
 	 *            ϢID
 	 * @param msg
 	 *            Ϣ
 	 */
 	public void sendMessage(int msgId, MyMessage msg) {
 		if (!clientLogon)
 			return;
 
 		Intent i = new Intent();
 		i.setAction("com.tpsoft.pushnotification.ServiceController");
 		i.putExtra("command", "send");
 		i.putExtra("msgId", msgId);
 		// i.putExtra("secure", false);
 		i.putExtra("com.tpsoft.pushnotification.MyMessage", msg.getBundle());
 		context.sendBroadcast(i);
 	}
 
 	/**
 	 * ѯں
 	 * 
 	 * @param queryId
 	 *            ѯʶ[ش]
 	 * @param condition
 	 *            ѯ:ں(֧ģƥ)
 	 */
 	public void queryPublic(int queryId, String condition) {
 		if (!clientLogon)
 			return;
 
 		Intent i = new Intent();
 		i.setAction("com.tpsoft.pushnotification.ServiceController");
 		i.putExtra("command", "query_public");
 		i.putExtra("queryId", queryId);
 		i.putExtra("condition", condition);
 		context.sendBroadcast(i);
 	}
 
 	/**
 	 * עں
 	 * 
 	 * @param account
 	 *            ں
 	 */
 	public void followPublic(String account) {
 		if (!clientLogon)
 			return;
 
 		Intent i = new Intent();
 		i.setAction("com.tpsoft.pushnotification.ServiceController");
 		i.putExtra("command", "follow_public");
 		i.putExtra("account", account);
 		context.sendBroadcast(i);
 	}
 
 	/**
 	 * ȡעں
 	 * 
 	 * @param account
 	 *            ں
 	 */
 	public void unfollowPublic(String account) {
 		if (!clientLogon)
 			return;
 
 		Intent i = new Intent();
 		i.setAction("com.tpsoft.pushnotification.ServiceController");
 		i.putExtra("command", "unfollow_public");
 		i.putExtra("account", account);
 		context.sendBroadcast(i);
 	}
 
 	/**
 	 * ȡѹעں
 	 */
 	public void getFollowed() {
 		if (!clientLogon)
 			return;
 
 		Intent i = new Intent();
 		i.setAction("com.tpsoft.pushnotification.ServiceController");
 		i.putExtra("command", "get_followed");
 		context.sendBroadcast(i);
 	}
 
 	private class MyBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(
 					"com.tpsoft.pushnotification.NotifyPushService")) {
 				// Ҫע...
 				String action = intent.getStringExtra("action");
 				if (action.equals("notify")) {
 					// Ϣ֪ͨ
 					String type = intent.getStringExtra("type");
 					if (NotifyPushService.NOTIFICATION_MESSAGE.equals(type)) {
 						// Ϣ
 						String msgText = intent.getStringExtra("content");
 						MyMessage msg;
 						try {
 							msg = MyMessage.extractMessage(msgText);
 						} catch (Exception e) {
 							Log.w(TAG_APILOG, msgText);
 							return;
 						}
 						for (MessageTransceiverListener listener : listeners)
 							listener.onNewMessageReceived(msg);
 					} else if (NotifyPushService.NOTIFICATION_PUBLIC_ACCOUNTS
 							.equals(type)) {
 						// ںϢб֪ͨ
 						String accountsText = intent.getStringExtra("content");
 						PublicAccount[] accounts;
 						try {
 							accounts = PublicAccount
 									.extractPublicAccounts(accountsText);
 						} catch (Exception e) {
 							Log.w(TAG_APILOG, accountsText);
 							return;
 						}
 						for (MessageTransceiverListener listener : listeners)
 							listener.onPublicAccountsReceived(accounts);
 					} else if (NotifyPushService.NOTIFICATION_FOLLOWED_ACCOUNTS
 							.equals(type)) {
 						// ѹעںϢб֪ͨ
 						String accountsText = intent.getStringExtra("content");
 						PublicAccount[] accounts;
 						try {
 							accounts = PublicAccount
 									.extractPublicAccounts(accountsText);
 						} catch (Exception e) {
 							Log.w(TAG_APILOG, accountsText);
 							return;
 						}
 						for (MessageTransceiverListener listener : listeners)
 							listener.onFollowedAccountsReceived(accounts);
 					}
 				} else if (action.equals("log")) {
 					// ־֪ͨ
 					int type = intent.getIntExtra("type", 0);
 					int code = intent.getIntExtra("code", 0);
 					String params = intent.getStringExtra("params");
 					switch (type) {
 					case NotifyPushService.LOG_CONNECT: // :
 						switch (code) {
 						case NotifyPushService.STATUS_CONNECT_CONNECTING: // ӷ...
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(1, "ӷ " + params
 										+ "...");
 							break;
 						case NotifyPushService.STATUS_CONNECT_CONNECTED: // Ѿӵ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(2, "ӵ");
 							break;
 						case NotifyPushService.STATUS_CONNECT_APP_CERTIFICATING: // Ӧ֤...
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(3, "УӦIDͽ...");
 							break;
 						case NotifyPushService.STATUS_CONNECT_APP_CERTIFICATED: // Ӧ֤ͨ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(4, "Ӧ֤ͨ");
 							break;
 						case NotifyPushService.STATUS_CONNECT_USER_CERTIFICATING: // û֤...
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(5, "Уû...");
 							break;
 						case NotifyPushService.STATUS_CONNECT_USER_CERTIFICATED: // û֤ͨ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(6, "û֤ͨ");
 							break;
 						case NotifyPushService.STATUS_CONNECT_MSGKEY_RECEIVED: // յϢԿ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(7, "յϢԿ");
 							break;
 						case NotifyPushService.STATUS_CONNECT_KEEPALIVEINTERVAL_RECEIVED: // յ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(8,
 										"յ: " + Integer.parseInt(params)
 												/ 1000 + "롣");
 							break;
 						case NotifyPushService.STATUS_CONNECT_LOGON: // ¼ɹ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(0, "¼ɹ");
 							clientLogon = true;
 							break;
 						case NotifyPushService.STATUS_CONNECT_KEEPALIVE: // ź
 							Log.d(TAG_APILOG, "ź...");
 							break;
 						case NotifyPushService.STATUS_CONNECT_KEEPALIVE_REPLIED: // յظź
 							Log.d(TAG_APILOG, "յظź");
 							break;
 						case NotifyPushService.ERROR_CONNECT_NETWORK_UNAVAILABLE: // 粻
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-1, "粻ã");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_BROKEN: // ж
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-2, "жϣ");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_SERVER_UNAVAILABLE: // 
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-3, "ã");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_LOGIN_TIMEOUT: // ¼ʱ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-4, "¼ʱ");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_IO_FAULT: // IO
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-5, "IOϣ");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_APP_CERTIFICATE: // Ӧ֤ʧ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-6, "Ӧ֤ʧܣ");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_USER_CERTIFICATE: // û֤ʧ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-7, "û֤ʧܣ");
 							clientLogon = false;
 							break;
 						case NotifyPushService.ERROR_CONNECT_SERVER: // 
 							for (MessageTransceiverListener listener : listeners)
 								listener.onLoginStatus(-8, "");
 							clientLogon = false;
 							break;
 						default:
 							break;
 						}
 						break;
 					case NotifyPushService.LOG_SENDMSG: // Ϣ:
 						switch (code) {
 						case NotifyPushService.STATUS_SENDMSG_SUBMIT: // ύϢ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), 1, "ύ...");
 							break;
 						case NotifyPushService.STATUS_SENDMSG_SUBMITTED: // ύϢ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), 2, "ύ");
 							break;
 						case NotifyPushService.STATUS_SENDMSG_OK: // յϢȷ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), 0, "ȷ");
 							break;
 						case NotifyPushService.ERROR_SENDMSG_NOT_LOGON: // δ¼ɹ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), -1, "δ¼");
 							break;
 						case NotifyPushService.ERROR_SENDMSG_DATA: // Ϣݴ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), -2, "ݴ");
 							break;
 						case NotifyPushService.ERROR_SENDMSG_SUBMIT: // ύϢʧ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(
 										Integer.parseInt(params), -3, "ύʧܣ");
 							break;
 						case NotifyPushService.ERROR_SENDMSG_FAILED: // Ϣʧ
 							int pos = params.indexOf(":");
 							int msgId = Integer.parseInt(params.substring(0,
 									pos));
 							String err = params.substring(pos + 1);
 							String errcode = err.substring(0, err.indexOf(","));
 							String errmsg = err.substring(err.indexOf(",") + 1);
 							for (MessageTransceiverListener listener : listeners)
 								listener.onMessageSendStatus(msgId, -4,
 										"ʧ(#" + errcode + ":" + errmsg
 												+ ")");
 							break;
 						default:
 							break;
 						}
 						break;
 					case NotifyPushService.LOG_QUERYPUBLIC: // ѯں:
 						switch (code) {
 						case NotifyPushService.STATUS_QUERYPUBLIC_SUBMIT: // ύ
 							break;
 						case NotifyPushService.STATUS_QUERYPUBLIC_SUBMITTED: // ύ
 							break;
 						case NotifyPushService.STATUS_QUERYPUBLIC_OK: // ɹ
 							break;
 						case NotifyPushService.ERROR_QUERYPUBLIC_NOT_LOGON: // δ¼ɹ
 							break;
 						case NotifyPushService.ERROR_QUERYPUBLIC_SUBMIT: // ύʧ
 							break;
 						case NotifyPushService.ERROR_QUERYPUBLIC_FAILED: // ʧ
 							break;
 						default:
 							break;
 						}
 						break;
 					case NotifyPushService.LOG_FOLLOWPUBLIC: // עں:
 						switch (code) {
 						case NotifyPushService.STATUS_FOLLOWPUBLIC_SUBMIT: // ύ
 							break;
 						case NotifyPushService.STATUS_FOLLOWPUBLIC_SUBMITTED: // ύ
 							break;
 						case NotifyPushService.STATUS_FOLLOWPUBLIC_OK: // ɹ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onPublicAccountFollowed(params);
 							break;
 						case NotifyPushService.ERROR_FOLLOWPUBLIC_NOT_LOGON: // δ¼ɹ
 							break;
 						case NotifyPushService.ERROR_FOLLOWPUBLIC_SUBMIT: // ύʧ
 							break;
 						case NotifyPushService.ERROR_FOLLOWPUBLIC_FAILED: // ʧ
 							break;
 						default:
 							break;
 						}
 						break;
 					case NotifyPushService.LOG_UNFOLLOWPUBLIC: // ȡעں:
 						switch (code) {
 						case NotifyPushService.STATUS_UNFOLLOWPUBLIC_SUBMIT: // ύ
 							break;
 						case NotifyPushService.STATUS_UNFOLLOWPUBLIC_SUBMITTED: // ύ
 							break;
 						case NotifyPushService.STATUS_UNFOLLOWPUBLIC_OK: // ɹ
 							for (MessageTransceiverListener listener : listeners)
 								listener.onPublicAccountUnfollowed(params);
 							break;
 						case NotifyPushService.ERROR_UNFOLLOWPUBLIC_NOT_LOGON: // δ¼ɹ
 							break;
 						case NotifyPushService.ERROR_UNFOLLOWPUBLIC_SUBMIT: // ύʧ
 							break;
 						case NotifyPushService.ERROR_UNFOLLOWPUBLIC_FAILED: // ʧ
 							break;
 						default:
 							break;
 						}
 						break;
 					case NotifyPushService.LOG_GETFOLLOWED: // ȡѹעĹں:
 						switch (code) {
 						case NotifyPushService.STATUS_GETFOLLOWED_SUBMIT: // ύ
 							break;
 						case NotifyPushService.STATUS_GETFOLLOWED_SUBMITTED: // ύ
 							break;
 						case NotifyPushService.STATUS_GETFOLLOWED_OK: // ɹ
 							break;
 						case NotifyPushService.ERROR_GETFOLLOWED_NOT_LOGON: // δ¼ɹ
 							break;
 						case NotifyPushService.ERROR_GETFOLLOWED_SUBMIT: // ύʧ
 							break;
 						case NotifyPushService.ERROR_GETFOLLOWED_FAILED: // ʧ
 							break;
 						default:
 							break;
 						}
 						break;
 					default:
 						break;
 					}
 				} else if (action.equals("status")) {
 					// Ϣ״̬֪ͨ
 					clientStarted = intent.getBooleanExtra("started", false);
 					if (!clientStarted)
 						clientLogon = false;
 					for (MessageTransceiverListener listener : listeners)
 						listener.onTransceiverStatus(clientStarted);
 				} else if (action.equals("logining")) {
 					// ¼״̬֪ͨ
 					boolean logining = intent
 							.getBooleanExtra("logining", false);
 					for (MessageTransceiverListener listener : listeners)
 						listener.onLogining(logining);
 				} else {
 					// δ֪
 				}
 			}
 		}
 	}
 }
