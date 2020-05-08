 package com.chess.genesis;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
import android.preference.PreferenceManager;
 import java.io.DataInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.security.MessageDigest;
 import java.util.Calendar;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 public class GenesisNotifier extends Service implements Runnable
 {
 	private NetworkClient2 net2;
 	private boolean fromalarm;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			final JSONObject json = (JSONObject) msg.obj;
 
 		try {
 			if (json.getString("result").equals("error")) {
 				final String title = "Error in GenesisNotifier";
 				SendNotification(title, json.getString("reason"), Notification.FLAG_AUTO_CANCEL);
 				return;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 			switch (msg.what) {
 			case NetworkClient.READ_INBOX:
 				NewMSG(json);
 				break;
 			case NetworkClient.SYNC_LIST:
 				NewMove(json);
 				break;
 			}
 		}
 	};
 
 	public void run()
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (!pref.getBoolean("isLoggedIn", false) || !pref.getBoolean("noteEnabled", true)) {
 			stopSelf();
 			return;
		} else if (internetIsActive() && fromalarm) {
 			CheckServer();
 		}
 		ScheduleWakeup();
 	}
 
 	@Override
 	public void onStart(final Intent intent, final int startid)
 	{
 		super.onStart(intent, startid);
 		Bundle bundle = null;
 
 		if (intent == null)
 			fromalarm = false;
 		else if ((bundle = intent.getExtras()) == null)
 			fromalarm = false;
 		else
 			fromalarm = bundle.getBoolean("fromAlarm", false);
 
 		(new Thread(this)).start();
 	}
 
 	@Override
 	public IBinder onBind(final Intent intent)
 	{
 		return null;
 	}
 
 	public void ScheduleWakeup()
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		final Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, pref.getInt("notifierPolling", 60));
 
 		final Intent intent = new Intent(this, GenesisAlarm.class);
 		final PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
 	}
 
	public boolean internetIsActive()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return (netInfo != null && netInfo.isConnected());
	}

 	private void CheckServer()
 	{
 		final GameDataDB db = new GameDataDB(this);
 		final long time = db.getNewestOnlineTime();
 		db.close();
 
 		net2 = new NetworkClient2(new SocketClient2(), this, handle);
 		net2.sync_list(time + 1);
 		net2.run();
 	}
 
 	private void NewMove(final JSONObject json)
 	{
 	try {
 		final JSONArray ids = json.getJSONArray("gameids");
 
 		if (ids.length() > 0) {
 			SendNotification("It's Your turn", "It's your turn in a game you're in", 0);
 			net2.disconnect();
 			return;
 		}
 		net2.read_inbox();
 		net2.run();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void NewMSG(final JSONObject json)
 	{
 	try {
 		final JSONArray games = json.getJSONArray("games");
 		final JSONArray msgs = json.getJSONArray("msgs");
 
 		if (games.length() > 0)
 			SendNotification("Added To New Game", "You've been added to a new game", 0);
 		else if (msgs.length() > 0)
 			SendNotification("New MSG", "You have a new message", 0);
 		net2.disconnect();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void SendNotification(final String title, final String text, final int flags)
 	{
 		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		final Notification note = new Notification(R.drawable.icon, title, System.currentTimeMillis());
 		note.flags = flags;
 
 		final Bundle bundle = new Bundle();
 		bundle.putInt("type", Enums.ONLINE_GAME);
 
 		final Intent intent = new Intent(this, GameList.class);
 		intent.putExtras(bundle);
 
 		final PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		final Context context = getApplicationContext();
 
 		note.setLatestEventInfo(context, title, text, pintent);
 		nm.notify(1, note);
 	}
 
 	// Local copy of SocketClient since the GenesisNotifier
 	// must have a separate connection
 	private class SocketClient2
 	{
 		public boolean isLoggedin = false;
 
 		private String loginHash = null;
 		private Socket sock = new Socket();
 		private DataInputStream input;
 		private OutputStream output;
 
 		public String getHash() throws SocketException, IOException
 		{
 			if (loginHash == null)
 				connect();
 			return loginHash;
 		}
 
 		private void connect() throws SocketException, IOException
 		{
 			if (sock.isConnected())
 				return;
 			hard_disconnect();
 			sock.connect(new InetSocketAddress("genesischess.com", 8338));
 			input = new DataInputStream(sock.getInputStream());
 			output = sock.getOutputStream();
 
 			loginHash = input.readLine().trim();
 		}
 
 		public void hard_disconnect()
 		{
 		try {
 			sock.close();
 			sock = new Socket();
 			loginHash = null;
 			isLoggedin = false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 		}
 
 		public void disconnect()
 		{
 			hard_disconnect();
 		}
 
 		public void write(final JSONObject data) throws SocketException, IOException
 		{
 			connect();
 
 			final String str = data.toString() + "\n";
 
 			output.write(str.getBytes());
 		}
 
 		public JSONObject read() throws SocketException, IOException, JSONException
 		{
 			connect();
 
 			return (JSONObject) (new JSONTokener(input.readLine())).nextValue();
 		}
 	}
 
 	private final class NetworkClient2 implements Runnable
 	{
 		public final static int NONE = 0;
 		public final static int LOGIN = 1;
 		public final static int REGISTER = 2;
 		public final static int JOIN_GAME = 3;
 		public final static int NEW_GAME = 4;
 		public final static int READ_INBOX = 5;
 		public final static int CLEAR_INBOX = 6;
 		public final static int GAME_STATUS = 7;
 		public final static int GAME_INFO = 8;
 		public final static int SUBMIT_MOVE = 9;
 		public final static int SUBMIT_MSG = 10;
 		public final static int SYNC_GAMIDS = 11;
 		public final static int GAME_SCORE = 12;
 		public final static int GAME_DATA = 13;
 		public final static int RESIGN_GAME = 14;
 		public final static int SYNC_LIST = 15;
 
 		private final Context context;
 		private final Handler callback;
 		private final SocketClient2 net;
 
 		private JSONObject json;
 		private int fid = NONE;
 		private boolean loginRequired;
 		private boolean error = false;
 
 		public NetworkClient2(final SocketClient2 Net, final Context _context, final Handler handler)
 		{
 			callback = handler;
 			context = _context;
 			net = Net;
 		}
 
 		private boolean relogin()
 		{
 			if (net.isLoggedin)
 				return true;
 
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 			final String username = pref.getString("username", "!error!");
 			final String password = pref.getString("passhash", "!error!");
 
 			JSONObject json2 = new JSONObject();
 
 			try {
 				try {
 					final Crypto2 crypt = new Crypto2();
 					json2.put("request", "login");
 					json2.put("username", username);
 					json2.put("passhash", crypt.LoginKey(net, password));
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 			} catch (SocketException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Can't contact server for sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			} catch (IOException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Lost connection durring sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			}
 			if (error) {
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				error = false;
 				return false;
 			}
 
 			try {
 				net.write(json2);
 			} catch (SocketException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Can't contact server for sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			} catch (IOException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Lost connection durring sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			}
 			if (error) {
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				error = false;
 				return false;
 			}
 
 			try {
 				json2 = net.read();
 			} catch (SocketException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Can't contact server for recieving data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			} catch (IOException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Lost connection durring recieving data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			} catch (JSONException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Server response illogical");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			}
 			if (error) {
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				error = false;
 				return false;
 			}
 
 			try {
 				if (!json2.getString("result").equals("ok")) {
 					callback.sendMessage(Message.obtain(callback, fid, json2));
 					return false;
 				}
 				net.isLoggedin = true;
 				return true;
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 
 		public void run()
 		{
 			JSONObject json2 = null;
 
 			if (error) {
 				error = false;
 				net.disconnect();
 				return;
 			} else if (loginRequired && !relogin()) {
 				net.disconnect();
 				return;
 			}
 
 			try {
 				net.write(json);
 			} catch (SocketException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Can't contact server for sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			} catch (IOException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Lost connection durring sending data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 				error = true;
 			}
 			if (error) {
 				net.disconnect();
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				error = false;
 				return;
 			}
 
 			try {
 				json2 = net.read();
 			} catch (SocketException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Can't contact server for recieving data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			} catch (IOException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Lost connection durring recieving data");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			} catch (JSONException e) {
 				json2 = new JSONObject();
 				try {
 					json2.put("result", "error");
 					json2.put("reason", "Server response illogical");
 				} catch (JSONException j) {
 					j.printStackTrace();
 					throw new RuntimeException();
 				}
 			}
 			if (error)
 				error = false;
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 		}
 
 		public void read_inbox()
 		{
 			fid = READ_INBOX;
 			loginRequired = true;
 
 			json = new JSONObject();
 
 			try {
 				json.put("request", "inbox");
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 
 		public void sync_list(final long time)
 		{
 			fid = SYNC_LIST;
 			loginRequired = true;
 
 			json = new JSONObject();
 
 			try {
 				json.put("request", "synclist");
 				json.put("time", time);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		}
 
 		public void disconnect()
 		{
 			net.disconnect();
 			GenesisNotifier.this.stopSelf();
 		}
 	}
 
 	private final class Crypto2
 	{
 		private Crypto2()
 		{
 		}
 
 		private String Sha1Hash(final String str)
 		{
 		try {
 			final MessageDigest digst = MessageDigest.getInstance("SHA-1");
 
 			digst.update(str.getBytes());
 
 			final byte[] shabytes = digst.digest();
 			final StringBuffer buff = new StringBuffer();
 
 			for (int i = 0; i < shabytes.length; i++) {
 				final String n = Integer.toHexString(shabytes[i] & 0xff);
 				if (n.length() < 2)
 					buff.append('0');
 				buff.append(n);
 			}
 			return buff.toString();
 		} catch (java.security.NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 		}
 
 		public String HashPasswd(final String str)
 		{
 			return Sha1Hash(Sha1Hash(str));
 		}
 
 		public String LoginKey(final SocketClient2 net, final String str) throws SocketException, IOException
 		{
 		try {
 			final MessageDigest digst = MessageDigest.getInstance("SHA-1");
 
 			digst.update(HashPasswd(str).getBytes());
 			digst.update(net.getHash().getBytes());
 
 			final byte[] shabytes = digst.digest();
 			final StringBuffer buff = new StringBuffer();
 
 			for (int i = 0; i < shabytes.length; i++) {
 				final String n = Integer.toHexString(shabytes[i] & 0xff);
 				if (n.length() < 2)
 					buff.append('0');
 				buff.append(n);
 			}
 			return buff.toString();
 		} catch (java.security.NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 		}
 	}
 }
