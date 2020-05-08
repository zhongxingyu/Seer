 package org.pileus.spades;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Messenger;
 import android.preference.PreferenceManager;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.format.DateFormat;
 import android.text.style.BackgroundColorSpan;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StrikethroughSpan;
 import android.text.style.StyleSpan;
 import android.text.style.UnderlineSpan;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends Activity
 {
 	/* Private data */
 	private Handler      handler;
 	private Messenger    messenger;
 	private Task         task;
 	private Toast        toast;
 	private boolean      running;
 	private String       topic;
 	private String       names;
 	private Cards        cards;
 	private Spades       game;
 
 	/* Widgets */
 	private TabHost      window;
 	private TabWidget    tabs;
 	private LinearLayout chat;
 	private TextView     log;
 	private EditText     input;
 	private Button       send;
 	private LinearLayout spades;
 	private TextView     debug;
 
 	private ScrollView   lscroll;
 	private ScrollView   dscroll;
 
 	/* Private helper methods */
 	private int hsv2rgb(int hsv)
 	{
 		int h  = (hsv & 0xff0000) >> 16;
 		int s  = (hsv & 0x00ff00) >>  8;
 		int v  = (hsv & 0x0000ff) >>  0;
 
 		int c  = (v * s) / 256;
 		int h1 = (h * 6) / 256;
 		int x  = c * (1 - Math.abs((h1%2)-1));
 		int m  = v - c;
 
 		int rgb = 0;
 
 		if (0 <= h1 && h1 <= 1) rgb = (c << 16) | (x << 8) | 0;
 		if (1 <= h1 && h1 <= 2) rgb = (x << 16) | (c << 8) | 0;
 		if (2 <= h1 && h1 <= 3) rgb = (0 << 16) | (c << 8) | x;
 		if (3 <= h1 && h1 <= 4) rgb = (0 << 16) | (x << 8) | c;
 		if (4 <= h1 && h1 <= 5) rgb = (x << 16) | (0 << 8) | c;
 		if (5 <= h1 && h1 <= 6) rgb = (c << 16) | (0 << 8) | x;
 
 		return rgb + (m << 16) + (m << 8) + m;
 	}
 
 	private void notice(String text)
 	{
 		String    msg  = "*** " + text + "\n";
 		Spannable span = new SpannableString(msg);
 		span.setSpan(new StyleSpan(Typeface.BOLD), 0, msg.length(), 0);
 		this.log.append(span);
 	}
 
 	private void display(Message msg)
 	{
 		String date = DateFormat.format("hh:mm:ss", msg.time).toString();
 		String text = String.format("(%s) %s: %s\n", date, msg.from, msg.msg);
 		Spannable span = new SpannableString(text);
 
 		// Determin positions
 		int de  = 1 + date.length() + 1;
 		int ne  = de + 1 + msg.from.length() + 1;
 		int pos = ne + 1;
 
 		// Get user color
 		int hash  = msg.from.hashCode();
 		int color = this.hsv2rgb(hash | 0x8080) | 0xff000000;
 
 		// Format date and name
 		span.setSpan(new ForegroundColorSpan(0xffffff88), 0,    de, 0);
 		span.setSpan(new ForegroundColorSpan(color),      de+1, ne, 0);
 
 		// Format IRC Colors
 		for (Message.Format fmt : msg.parts) {
 			int len = fmt.txt.length();
 
 			// Bold/italics
 			if (fmt.bold && fmt.italic)
 				span.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), pos, pos+len, 0);
 			else if (fmt.bold)
 				span.setSpan(new StyleSpan(Typeface.BOLD), pos, pos+len, 0);
 			else if (fmt.italic)
 				span.setSpan(new StyleSpan(Typeface.ITALIC), pos, pos+len, 0);
 
 			// Striketrough / underline
 			if (fmt.strike)
 				span.setSpan(new StrikethroughSpan(), pos, pos+len, 0);
 			if (fmt.underline)
 				span.setSpan(new UnderlineSpan(), pos, pos+len, 0);
 
 			// Colors (reverse not supported)
 			if (fmt.fg!=null)
 				span.setSpan(new ForegroundColorSpan(fmt.fg.color), pos, pos+len, 0);
 			if (fmt.bg!=null)
 				span.setSpan(new BackgroundColorSpan(fmt.bg.color), pos, pos+len, 0);
 
 			pos += len;
 		}
 
 		// Append the message
 		this.log.append(span);
 	}
 
 	/* Private handler methods */
 	private void onRegister(Task task)
 	{
 		Os.debug("Main: onRegister");
 		this.task      = task;
 		this.game.task = task;
 		this.running = this.task.isRunning();
 		for (Object obj : this.task.getLog()) {
 			if (String.class.isInstance(obj))
 				this.notice((String)obj);
 			if (Message.class.isInstance(obj))
 				this.onMessage((Message)obj);
 		}
 	}
 
 	private void onMessage(Message msg)
 	{
 		// Debug
 		this.debug.append("> " + msg.line + "\n");
 		this.dscroll.smoothScrollTo(0, this.debug.getBottom());
 
 		// Chat
 		switch (msg.type) {
 			case PRIVMSG:
 				this.display(msg);
 				this.game.onMessage(msg);
 				break;
 			case TOPIC:
 				if (!msg.txt.equals(this.topic))
 					this.notice("Topic for " + msg.arg + ": " + msg.txt);
 				this.topic = msg.txt;
 				break;
 			case NAMES:
 				if (!msg.txt.equals(this.names))
 					this.notice("Users in " + msg.arg + ": " + msg.txt);
 				this.names = msg.txt;
 				break;
 			case ERROR:
 				this.notice("Error: " + msg.txt);
 				break;
 			case AUTHOK:
 				this.notice("Authentication succeeded: " + msg.txt);
 				break;
 			case AUTHFAIL:
 				this.notice("Authentication failed: " + msg.txt);
 				break;
 		}
 		this.lscroll.smoothScrollTo(0, this.log.getBottom());
 	}
 
 	private void onNotify(String text)
 	{
 		Os.debug("Main: onNotify - " + text);
 		this.notice(text);
 		this.toast.setText(text);
 		this.toast.show();
 	}
 
 	/* Private service methods */
 	private void register()
 	{
 		Os.debug("Main: register");
 		startService(new Intent(this, Task.class)
 				.putExtra("Command",   Task.REGISTER)
 				.putExtra("Messenger", this.messenger));
 	}
 
 	private void connect()
 	{
 		Os.debug("Main: connect");
 		startService(new Intent(this, Task.class)
 				.putExtra("Command", Task.CONNECT));
 		this.running = true;
 	}
 
 	private void disconnect()
 	{
 		Os.debug("Main: disconnect");
 		startService(new Intent(this, Task.class)
 				.putExtra("Command", Task.DISCONNECT));
 		this.running = false;
 	}
 
 	private void quit()
 	{
 		stopService(new Intent(this, Task.class));
 		Intent intent = new Intent(Intent.ACTION_MAIN);
 		intent.addCategory(Intent.CATEGORY_HOME);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
 	}
 
 	/* Widget callback functions */
 	public void onSend(View btn)
 	{
 		if (this.task == null)
 			return;
 		String  txt = this.input.getText().toString();
 		Message msg = this.task.send(txt);
 		if (msg == null)
 			return;
 		this.input.setText("");
 	}
 
 	/* Activity Methods */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		try {
 			super.onCreate(savedInstanceState);
 			Os.debug("Main: onCreate");
 
 			// Setup preferences
 			PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
 
 			// Setup main layout
 			this.setContentView(R.layout.main);
 
 			// Setup toast
 			this.toast     = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 
 			// Setup communication
 			this.handler   = new MainHandler();
 			this.messenger = new Messenger(this.handler);
 
 			// Find widgets
 			this.window    = (TabHost)      findViewById(android.R.id.tabhost);
 			this.tabs      = (TabWidget)    findViewById(android.R.id.tabs);
 			this.chat      = (LinearLayout) findViewById(R.id.chat);
 			this.log       = (TextView)     findViewById(R.id.log);
 			this.input     = (EditText)     findViewById(R.id.input);
 			this.send      = (Button)       findViewById(R.id.send);
 			this.spades    = (LinearLayout) findViewById(R.id.spades);
 			this.debug     = (TextView)     findViewById(R.id.debug);
 
 			this.lscroll   = (ScrollView)   findViewById(R.id.log_scroll);
 			this.dscroll   = (ScrollView)   findViewById(R.id.debug_scroll);
 
 			// Add window tabs
 			this.window.setup();
 
 			this.window.addTab(this.window
 					.newTabSpec("chat")
 					.setIndicator("Chat")
 					.setContent(R.id.chat));
 			this.window.addTab(this.window
 					.newTabSpec("spades")
 					.setIndicator("Spades")
 					.setContent(R.id.spades));
 			this.window.addTab(this.window
 					.newTabSpec("debug")
 					.setIndicator("Debug")
 					.setContent(R.id.debug));
 
 			// Setup Spades game and cards view
 			this.game  = new Spades(PreferenceManager
 					.getDefaultSharedPreferences(this)
 					.getString("pref_referee", "rhawk"));
 			this.cards = new Cards(this);
 
 			this.game.cards = this.cards;
 			this.cards.game = this.game;
 
 			this.spades.addView(cards);
 
 			// Attach to background service
 			this.register();
 
 		} catch (Exception e) {
 			Os.debug("Error setting content view", e);
 			return;
 		}
 	}
 
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		Os.debug("Main: onStart");
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		Os.debug("Main: onResume");
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		Os.debug("Main: onPause");
 	}
 
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 		Os.debug("Main: onStop");
 	}
 
 	@Override
 	public void onRestart()
 	{
 		super.onRestart();
 		Os.debug("Main: onRestart");
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		Os.debug("Main: onDestroy");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		menu.findItem(R.id.connect).setVisible(!this.running);
 		menu.findItem(R.id.disconnect).setVisible(this.running);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId()) {
 			case R.id.connect:
 				this.connect();
 				return true;
 			case R.id.disconnect:
 				this.disconnect();
 				return true;
 			case R.id.settings:
 				this.startActivity(new Intent(this, Prefs.class));
 				return true;
 			case R.id.quit:
 				this.quit();
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	/* Handler class */
 	class MainHandler extends Handler
 	{
 		public void handleMessage(android.os.Message msg)
 		{
 			switch (msg.what) {
 				case Task.REGISTER:
 					Main.this.onRegister((Task)msg.obj);
 					break;
 				case Task.MESSAGE:
 					Main.this.onMessage((Message)msg.obj);
 					break;
 				case Task.CONNECT:
 					Main.this.running = true;
 					break;
 				case Task.DISCONNECT:
 					Main.this.running = false;
 					break;
 				case Task.NOTIFY:
 					Main.this.onNotify((String)msg.obj);
 					break;
 				default:
 					Os.debug("Main: unknown message - " + msg.what);
 					break;
 			}
 		}
 	}
 }
