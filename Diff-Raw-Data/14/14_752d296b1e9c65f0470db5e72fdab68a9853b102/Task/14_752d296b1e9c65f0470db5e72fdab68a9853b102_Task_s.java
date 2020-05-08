 package org.pileus.spades;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Messenger;
 import android.preference.PreferenceManager;
 
 public class Task extends Service implements Runnable
 {
 	/* Commands */
 	public static final int REGISTER   = 0;
 	public static final int MESSAGE    = 1;
 	public static final int CONNECT    = 2;
 	public static final int DISCONNECT = 3;
 	public static final int NOTIFY     = 4;
 
 	/* Private data */
 	private SharedPreferences prefs;
 	private Messenger         messenger;
 	private Thread            thread;
 	private Client            client;
 	private List<Object>      log;
 	private Lock              lock;
 
 	/* Private methods */
 	private void command(int cmd, Object value)
 	{
 		if (cmd == MESSAGE || cmd == NOTIFY) {
 			this.lock.lock();
 			this.log.add(value);
 			this.lock.unlock();
 		}
 		try {
 			android.os.Message msg = android.os.Message.obtain();
 			msg.what = cmd;
 			msg.obj  = value;
 			this.messenger.send(msg);
 		} catch (Exception e) {
			Os.debug("Task: error sending message");
 		}
 	}
 
 	private void notify(String text, int icon)
 	{
 		// Notify Main
 		this.command(NOTIFY, text);
 
 		// Notification bar
 		Notification  note   = new Notification(icon, null, 0);
 		Intent        intent = new Intent(this, Main.class);
 		PendingIntent pend   = PendingIntent.getActivity(this, 0, intent, 0);
 
 		note.setLatestEventInfo(this, "Spades!", text, pend);
 		this.startForeground(1, note);
 	}
 
 	private void handle(int cmd, Messenger mgr)
 	{
 		// Setup communication with Main
 		if (cmd == REGISTER) {
 			Os.debug("Task: handle - register");
 			this.messenger = mgr;
 			this.command(REGISTER, this);
 		}
 
 		// Create client thread
 		if (cmd == CONNECT && this.thread == null) {
 			Os.debug("Task: handle - connect");
 			this.thread = new Thread(this);
 			this.thread.start();
 		}
 
 		// Stop client thread
 		if (cmd == DISCONNECT && this.thread != null) {
 			Os.debug("Task: handle - register");
 			try {
 				this.client.abort();
 				this.thread.join();
 			} catch (Exception e) {
 				Os.debug("Task: error stopping service", e);
 			}
 		}
 	}
 
 	/* Public methods */
 	public Message send(String txt)
 	{
 		if (this.client == null)
 			return null;
 		Message msg = this.client.send(txt);
 		if (msg != null)
 			this.command(MESSAGE, msg);
 		return msg;
 	}
 
 	public List<Object> getLog()
 	{
 		this.lock.lock();
 		LinkedList<Object> out = new LinkedList<Object>(this.log);
 		this.lock.unlock();
 		return out;
 	}
 
 	public boolean isRunning()
 	{
 		return this.thread != null;
 	}
 
 	/* Runnable methods */
 	@Override
 	public void run()
 	{
 		Os.debug("Task: thread run");
 
 		// Setup notification bar
 		this.notify("Connecting..", android.R.drawable.presence_invisible);
 
 		// Grab preferences
 		String  server   = this.prefs.getString ("pref_server",   this.client.server);
 		String  port     = this.prefs.getString ("pref_port",     this.client.port + "");
 		String  nickname = this.prefs.getString ("pref_nickname", this.client.nickname);
 		String  channel  = this.prefs.getString ("pref_channel",  this.client.channel);
 		boolean usesasl  = this.prefs.getBoolean("pref_usesasl",  this.client.usesasl);
 		String  authname = this.prefs.getString ("pref_authname", this.client.authname);
 		String  password = this.prefs.getString ("pref_password", this.client.password);
 
 		// Update client settings
 		this.client.setServer(server, Integer.parseInt(port));
 		this.client.setUser(nickname, channel);
 		this.client.setAuth(usesasl, authname, password);
 
 		// Start connecting
 		if (!this.client.connect()) {
 			this.command(DISCONNECT, null);
 			this.notify("Unable to connect", android.R.drawable.presence_offline);
 			this.thread = null;
 			return;
 		}
 
 		// Wait for login
 		while (this.client.state == Client.State.SETUP) {
 			Message msg = this.client.recv();
 			if (msg == null)
 				break;
 			this.command(MESSAGE, msg);
 		}
 
 		// Notify connection status
 		if (this.client.state == Client.State.READY) {
 			this.command(CONNECT, null);
 			this.notify("Connected", android.R.drawable.presence_online);
 		} else {
 			this.command(DISCONNECT, null);
 			this.notify("Connetion aborted", android.R.drawable.presence_offline);
 		}
 
 		// Process messages
 		while (this.client.state == Client.State.READY) {
 			Message msg = this.client.recv();
 			if (msg == null)
 				break;
 			this.command(MESSAGE, msg);
 		}
 
 		// Notify disconnect disconnected
 		this.notify("Disconnected", android.R.drawable.presence_offline);
 		this.command(DISCONNECT, null);
 
 		// Shutdown the client
 		this.client.abort();
 		this.thread = null;
 
 		Os.debug("Task: thread exit");
 	}
 
 	/* Service Methods */
 	@Override
 	public void onCreate()
 	{
 		Os.debug("Task: onCreate");
 		super.onCreate();
 
 		this.log    = new LinkedList<Object>();
 		this.lock   = new ReentrantLock();
 		this.client = new Client();
 		this.prefs  = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		Os.debug("Task: onDestroy");
 		this.handle(DISCONNECT, null);
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId)
 	{
 		Os.debug("Task: onStart");
 		super.onStart(intent, startId);
 		int       cmd = intent.getExtras().getInt("Command");
 		Messenger mgr = (Messenger)intent.getExtras().get("Messenger");
 		this.handle(cmd, mgr);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		Os.debug("Task: onBind");
 		return null;
 	}
 }
