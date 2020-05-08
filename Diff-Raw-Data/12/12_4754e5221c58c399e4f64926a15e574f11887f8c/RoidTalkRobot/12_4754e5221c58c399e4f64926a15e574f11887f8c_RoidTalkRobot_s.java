 package com.zhuri.talk;
 
 import java.nio.channels.*;
 import java.net.InetAddress;
 import java.io.IOException;
 import com.zhuri.util.DEBUG;
 import com.zhuri.slot.SlotSlot;
 import com.zhuri.slot.SlotWait;
 import com.zhuri.slot.SlotTimer;
 import com.zhuri.slot.SlotAsync;
 import com.zhuri.pstcp.AppFace;
 import com.zhuri.util.InetUtil;
 
 import com.zhuri.talk.TalkClient;
 import com.zhuri.talk.UpnpRobot;
 import com.zhuri.talk.StunRobot;
 import com.zhuri.talk.TalkRobot;
 import com.zhuri.talk.protocol.Body;
 import com.zhuri.talk.protocol.Packet;
 import com.zhuri.talk.protocol.Message;
 
 import android.net.Uri;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ComponentName;
 import android.content.SharedPreferences;
 import android.os.PowerManager;
 import android.database.Cursor;
 import android.preference.PreferenceManager;
 
 import java.net.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 class MyInvoke implements TalkRobot.IReplyable {
 	private Context mContext;
 	private String[] mParamers;
 	private PowerManager.WakeLock mWakeLock;
 
 	private String mFrom;
 	private SlotSlot mCancel;
 	private TalkClient mClient;
 
 	public MyInvoke(String[] params, Context context, PowerManager.WakeLock lock) {
 		mParamers = params;
 		mContext = context;
 		mWakeLock = lock;
 		return;
 	}
 
 	@Override
 	public void setCancel(SlotSlot cancel) {
 		mCancel = cancel;
 		return;
 	}
 
 	@Override
 	public void setTalk(TalkClient client) {
 		mClient = client;
 		return;
 	}
 
 	@Override
 	public void setReply(String reply) {
 		mFrom = reply;
 		return;
 	}
 
 	@Override
 	public void invoke() {
 		String output = "";
 		String method = mParamers[0];
 		Message reply = new Message();
 
 		reply.setTo(mFrom);
 		if (method.equals("am")) {
 			output = amStart(mParamers);
 		} else if (method.equals("sms")) {
 			output = readSMS(mParamers);
 		} else if (method.equals("version")) {
 			output = "VERSION: 3.0";
 		} else if (method.equals("forward")) {
 			output = doTcpForward(mParamers);
 		} else if (method.equals("ifconfig")) {
 			output = getNetworkConfig(mParamers);
 		} else if (method.equals("stun-send")) {
 			output = doStunSend(mParamers);
 		} else if (method.equals("stun-name")) {
 			output = AppFace.stunGetName();
 		} else if (method.equals("acquire")) {
 			int timeout = 40 * 60 * 1000;
 			try {
				timeout = Integer.parseInt(mParamers[1]) * 60 * 1000;
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 			mWakeLock.acquire(timeout);
 		} else if (method.equals("release")) {
 			mWakeLock.release();
 		}
 
 		reply.add(new Body(output));
 		mClient.put(reply);
 		return;
 	}
 
 	private Intent parseIntent(String[] args) {
 		Intent intent = new Intent();
 		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 		for (int i = 2; i < args.length; i++) {
 			if (args[i].equals("-a")) {
 				if (++i < args.length)
 					intent.setAction(args[i]);
 			} else if (args[i].equals("-d")) {
 				if (++i < args.length)
 					intent.setData(Uri.parse(args[i]));
 			} else if (args[i].equals("-t")) {
 				if (++i < args.length)
 					intent.setType(args[i]);
 			} else if (args[i].equals("-c")) {
 				if (++i < args.length)
 					intent.addCategory(args[i]);
 			} else if (args[i].equals("-e")) {
 				i += 2;
 			} else if (args[i].equals("--es")) {
 				i += 2;
 			} else if (args[i].equals("--esn")) {
 				i += 1;
 			} else if (args[i].equals("--ez")) {
 				i += 2;
 			} else if (args[i].equals("--ei")) {
 				i += 2;
 			} else if (args[i].equals("-n")) {
 				if (++i < args.length) {
 					String[] parts = args[i].split("/");
 					if (parts.length == 2) {
 						ComponentName n = new ComponentName(parts[0], parts[1]);
 						intent.setComponent(n);
 					}
 				}
 			} else if (args[i].equals("-f")) {
 				++i;
 			} else if (args[i].equals("--grant-read-uri-permission")) {
 				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 			} else if (args[i].equals("--grant-write-uri-permission")) {
 				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 			} else if (args[i].equals("--debug-log-resolution")) {
 				intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
 			} else if (args[i].equals("--activity-brought-to-front")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			} else if (args[i].equals("--activity-clear-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			} else if (args[i].equals("--activity-clear-when-task-reset")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			} else if (args[i].equals("--activity-exclude-from-recents")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 			} else if (args[i].equals("--activity-launched-from-history")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
 			} else if (args[i].equals("--activity-multiple-task")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 			} else if (args[i].equals("--activity-no-animation")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			} else if (args[i].equals("--activity-no-history")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 			} else if (args[i].equals("--activity-no-user-action")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
 			} else if (args[i].equals("--activity-previous-is-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
 			} else if (args[i].equals("--activity-reorder-to-front")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			} else if (args[i].equals("--activity-reset-task-if-needed")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			} else if (args[i].equals("--activity-single-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			} else if (args[i].equals("--receiver-registered-only")) {
 				intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
 			} else if (args[i].equals("--receiver-replace-pending")) {
 				intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
 			} else if (!args[i].startsWith("-")) {
 				intent.setData(Uri.parse(args[i]));
 			}
 		}
 
 		return intent;
 	}
 
 	private String amStart(String[] args) {
 		if (args.length < 3) {
 			return "am: inval argument";
 		}
 
 		try {
 			Intent intent = parseIntent(args);
 
 			if (args[1].equals("start")) {
 				mContext.startActivity(intent);
 			} else if (args[1].equals("startservice")) {
 				mContext.startService(intent);
 			} else if (args[1].equals("broadcast")) {
 				mContext.sendBroadcast(intent);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return e.getMessage();
 		}
 
 		return "am: OK";
 	}
 
 	private String doTcpForward(String[] parts) {
 
 		try {
 			if (parts.length >= 3) {
 				int port = Integer.parseInt(parts[2]);
 				InetAddress addr = InetUtil.getInetAddress(parts[1]);
 				AppFace.setForward(addr.getAddress(), port);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "forward: failure";
 		}
 
 		return "forward: OK";
 	}
 
 	private String doStunSend(String[] parts) {
 		if (parts.length >= 2)
 			AppFace.stunSendRequest(parts[1], 1);
 		return "stun-send: OK";
 	}
 
 	final static String SMS_URI_ALL =  "content://sms/";
 	final static String SMS_URI_INBOX = "content://sms/inbox/";
 	final static String SMS_URI_SEND = "content://sms/sent/";
 	final static String SMS_URI_DRAFT = "content://sms/draft/";
 	final static String SMS_URI_OUTBOX = "content://sms/outbox/";
 	final static String SMS_URI_FAILED = "content://sms/failed/";
 	final static String SMS_URI_QUEUED = "content://sms/queued/";
 	final static String UNREAD_SELECTION = "(read=0 OR seen=0)";
 	private String readSMS(String[] parts) {
 		int bodyIndex, addressIndex, idIndex;
 		String messageContent = "";
 		String[] projection = new String[] {
 			"_id", "address", "person", "body", "date", "type", "read"
 		};
 
 		String index = null;
 		String smsUri = "content://sms/";
 		String selection = null;
 
 		for (String part: parts) {
 			if (part.matches("^[0-9]*$"))
 				index = part;
 			else if (part.equals("unread"))
 				selection = UNREAD_SELECTION;
 			else if (part.equals("inbox"))
 				smsUri = SMS_URI_INBOX;
 			else if (part.equals("sent"))
 				smsUri = SMS_URI_SEND;
 			else if (part.equals("draft"))
 				smsUri = SMS_URI_DRAFT;
 			else if (part.equals("outbox"))
 				smsUri = SMS_URI_OUTBOX;
 			else if (part.equals("failed"))
 				smsUri = SMS_URI_FAILED;
 			else if (part.equals("queued"))
 				smsUri = SMS_URI_QUEUED;
 		}
 
 		if (index != null)
 			smsUri += index;
 
 		Uri uri = Uri.parse(smsUri);
 		Cursor cursor = mContext.getContentResolver().query(uri,
 				projection, selection, null, "date desc");
 		idIndex = cursor.getColumnIndex("_id");
 		bodyIndex = cursor.getColumnIndex("body");
 		addressIndex = cursor.getColumnIndex("address");
 
 		int last_id = 0;
 		int first_id = 0;
 		if (cursor.moveToFirst()) {
 			int count = 0;
 			first_id = cursor.getInt(idIndex);
 			do {
 				if (count++ < 10) {
 					messageContent += "address: " + cursor.getString(addressIndex) + "\r\n";
 					messageContent += "body: " + cursor.getString(bodyIndex) + "\r\n";
 				}
 				last_id = cursor.getInt(idIndex);
 			} while (cursor.moveToNext());
 		}
 
 		messageContent += "sms: first=" + first_id + " last="  + last_id;
 		return messageContent;
 	}
 
 	private String getNetworkConfig(String[] parts) {
 		String config = "";
 
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface
 					.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				List<InterfaceAddress> ifaddrs = intf.getInterfaceAddresses();
 				for (InterfaceAddress ifaddr: ifaddrs) {
 					InetAddress iaddr = ifaddr.getAddress();
 					if (iaddr != null && !iaddr.isLoopbackAddress()) {
 						config += "ifconfig: " + ifaddr.getAddress().getHostAddress() + "\r\n";
 					}
 				}   
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "ifconfig: exception";
 		}
 
 		return config;
 	}
 }
 
 class MyTalkRobot extends TalkRobot {
 	private Context mContext;
 	private PowerManager.WakeLock mWakeLock;
 	final private SlotSlot mDisconnect = new SlotSlot();
 
 	final private Scriptor.ICommandInterpret mMyInterpret = new Scriptor.ICommandInterpret() {
 		public Scriptor.IInvokable createInvoke(List<String> params) {
 			String[] arr = new String[params.size()];
 			return new MyInvoke(params.toArray(arr), mContext, mWakeLock);
 		}
 	};
 
 	public MyTalkRobot(Context context, PowerManager.WakeLock lock) {
 		super(new TalkClient());
 		mContext = context;
 		mWakeLock = lock;
 		mScriptor.registerCommand("am", mMyInterpret);
 		mScriptor.registerCommand("sms", mMyInterpret);
 		mScriptor.registerCommand("version", mMyInterpret);
 		mScriptor.registerCommand("forward", mMyInterpret);
 		mScriptor.registerCommand("ifconfig", mMyInterpret);
 		mScriptor.registerCommand("stun-send", mMyInterpret);
 		mScriptor.registerCommand("stun-name", mMyInterpret);
 		mScriptor.registerCommand("acquire", mMyInterpret);
 		mScriptor.registerCommand("release", mMyInterpret);
 	}
 
 	public void start() {
 		String port;
 		SharedPreferences pref;
 		String user, domain, server, password, resource;
 
 		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
 		port = pref.getString("port", "5222");
 		user = pref.getString("user", "dupit8");
 		domain = pref.getString("domain", "gmail.com");
 		server = pref.getString("server", "xmpp.l.google.com");
 		password = pref.getString("password", "L8PaPUL1nfQT");
		resource = pref.getString("resource", "robot");
 
 		mClient.start(user, domain, password, server + ":" + port);
 		mClient.setResource(resource);
 		return;
 	}
 }
 
 public class RoidTalkRobot {
 	private Context mContext;
 	private MyTalkRobot mRobot;
 	private PowerManager.WakeLock mWakeLock = null;
 	
 	public RoidTalkRobot(Context context) {
 		mContext = context;
 		mPresence.setup();
 
 		PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
 		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.zhuri.talk");
 		return;
 	}
 
 	final private SlotTimer mDelay = new SlotTimer() {
 		public void invoke() {
 			start();
 			return;
 		}
 	};
 
 	final private SlotWait onDisconnect = new SlotWait() {
 		public void invoke() {
 			mDelay.reset(5000);
 			return;
 		}
 	};
 
 	private String newPresence = "";
 	final private Runnable updater = new Runnable() {
 		public void run() {
 			if (mRobot != null)
 				mRobot.presence(newPresence);
 			return;
 		}
 	};
 
 	final private SlotAsync mPresence = new SlotAsync(updater);
 	public void updatePresence(String presence) {
 		newPresence = presence;
 		mPresence.toggle();
 		return;
 	}
 
 	public void start() {
 		mRobot = new MyTalkRobot(mContext, mWakeLock);
 		mRobot.onDisconnect(onDisconnect);
 		mRobot.start();
 		return;
 	}
 
 	public void close() {
 		if (mWakeLock.isHeld())
 			mWakeLock.release();
 		mRobot.close();
 		mDelay.clean();
 	}
 }
