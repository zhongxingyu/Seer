 package com.mamewo.hello;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.*;
 import android.webkit.*;
 import android.net.http.*;
 
 public class MalarmActivity extends Activity implements OnClickListener {
 	/** Called when the activity is first created. */
 	//TODO: add to resource?
 	public static final String WAKEUP_ACTION = "com.mamewo.hello.WAKEUP_ACTION";
 	public static final String WAKEUPAPP_ACTION = "com.mamewo.hello.WAKEUPAPP_ACTION";
 	public static final String SLEEP_ACTION = "com.mamewo.hello.SLEEP_ACTION";
 	// 1 hour
 	//TODO: add to preference
 	public static long SLEEP_TIME = 60 * 60 * 1000;
 	private static final Integer DEFAULT_HOUR = new Integer(7);
 	private static final Integer DEFAULT_MIN = new Integer(0);
 	
 	private Button _next_button;
 	private Button _sleep_wakeup_button;
 	private TimePicker _time_picker;
 	private TextView _time_label;
 	private WebView _webview;
 	private Vibrator _vibrator;
 	@SuppressWarnings("unused")
 	private PhoneStateListener _calllistener;
 	
     public class MyCallListener extends PhoneStateListener{
     	MalarmActivity _activity;
     	public MyCallListener(MalarmActivity context) {
     		_activity = context;
     		TelephonyManager telmgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
     		telmgr.listen(this, LISTEN_CALL_STATE);
     	}
     	
     	public void onCallStateChanged (int state, String incomingNumber) {
     		if (state == TelephonyManager.CALL_STATE_RINGING) {
     			//stop vibration
     			if (_vibrator != null) {
     				_vibrator.cancel();
     			}
     			_activity.cancelAlarm();
     			//TODO: fix cancelAlarm or design
     			Player.stopMusic();
     		}
     	}
     }
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i("Hello", "onCreate is called");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		_time_picker = (TimePicker) findViewById(R.id.timePicker1);
 		_time_picker.setIs24HourView(true);
 		_sleep_wakeup_button = (Button) findViewById(R.id.play_button);
 		_sleep_wakeup_button.setOnClickListener(this);
 		_next_button = (Button) findViewById(R.id.next_button);
 		_next_button.setOnClickListener(this);
 		_time_label = (TextView) findViewById(R.id.target_time_label);
 		// set default time
 		_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 		_webview = (WebView)findViewById(R.id.webView1);
 		WebSettings config = _webview.getSettings();
 		config.setJavaScriptEnabled(true);
 		//to display twitter...
 		config.setDomStorageEnabled(true);
 		config.setJavaScriptEnabled(true);
 		config.setSupportZoom(true);
 		_webview.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				_webview.requestFocus();
 				return false;
 			}
 		});
 		final Activity activity = this;
 		_webview.setWebViewClient(new WebViewClient() {
 		   public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 		     Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 		   }
 		   //for debug
 		   public void onLoadResource (WebView view, String url) {
 			   Log.i("Hello", "loading: " + url);
 		   }
 		   public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
 			 Toast.makeText(activity, "SSL error " + error, Toast.LENGTH_SHORT).show();
 		   }
 		   public void onPageFinished(WebView view, String url) {
 			   Log.i("Hello", "onPageFinshed: " + url);
 			   if (url.indexOf("bijint") > 0) {
 				   //disable touch event on view?
 				   //for normal layout
 				   //view.scrollTo(480, 330);
 				   //TODO: get precise position....
 				   if(url.indexOf("binan") > 0) {
 					   //TODO: fix scroll problem
 					   view.scrollTo(0, 420);
 				   } else {
 					   //Log.i("Hello", "bijin");
 					   view.scrollTo(0, 960);
 				   }
 			   }
 		   }
 		 });
 		_calllistener = new MyCallListener(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		//start tokei
 		Log.i("Hello", "onPause is called, start JavaScript");
 		super.onResume();
 		//WebView.onResume is hidden, why!?!?
 		_webview.getSettings().setJavaScriptEnabled(true);
 		loadWebPage(_webview);
 	}
 	
 	@Override
 	protected void onPause() {
 		Log.i("Hello", "onPause is called, stop JavaScript");
 		super.onPause();
 		//stop tokei
 		_webview.getSettings().setJavaScriptEnabled(false);
 	}
 	
 	private void loadWebPage (WebView view) {
 		//for bijin-tokei
 		String url = "http://www.bijint.com/jp/";
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		url = pref.getString("url", "http://twitter.com/");
 		WebSettings config = _webview.getSettings();
 		if (url.indexOf("bijint") > 0) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		} else {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
 		}
 		//TODO: show loading toast
 		showMessage(this, "Loading...");
 		_webview.loadUrl(url);
 	}
 	
 	@Override
 	protected void onStart () {
 		Log.i("Hello", "onStart is called");
 		super.onStart();
 		Log.i("Hello", "onStart is called");
 		if (_time_picker.isEnabled()) {
 			_time_picker.setCurrentHour(DEFAULT_HOUR);
 			_time_picker.setCurrentMinute(DEFAULT_MIN);
 		}
 	}
 	
 	protected void onNewIntent (Intent intent) {
 		String action = intent.getAction();
 		if (_time_picker.isEnabled()) {
 			_time_picker.setCurrentHour(DEFAULT_HOUR);
 			_time_picker.setCurrentMinute(DEFAULT_MIN);
 		}
 		if (action != null && action.equals(WAKEUPAPP_ACTION)) {
 			if (_vibrator != null) {
 				long pattern[] = { 10, 2000, 500, 1500, 1000, 2000 };
 				_vibrator.vibrate(pattern, 1);
 			}
 		}
 	}
 	
 	private PendingIntent makePintent(String action) {
 		Intent i = new Intent(this, Player.class);
 		i.setAction(action);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	//add menu to cancel alarm
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate our menu which can gather user input for switching camera
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.alarm_menu, menu);
         return true;
     }
 
     //TODO: cancel or stop? (playing alarm?)
     private void cancelAlarm () {
 		PendingIntent p = makePintent(WAKEUP_ACTION);
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		// TODO: adjust time of _time_picker
 		_time_picker.setEnabled(true);
 		_time_label.setText("");
 		mgr.cancel(p);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	case R.id.set_now:
     		Calendar now = new GregorianCalendar();
     		_time_picker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
     		_time_picker.setCurrentMinute(now.get(Calendar.MINUTE));
     		break;
        	case R.id.stop_vibration:
     		if (_vibrator != null) {
     			_vibrator.cancel();
     		}
     		break;
     	case R.id.play_wakeup:
     		Player.reset();
     		Player.startMusic(Playlist.WAKEUP_PLAYLIST);
     		break;
     	case R.id.pref:
     		startActivity(new Intent(this, MyPreference.class));
     		break;
     	default:
     		Log.i("Hello", "Unknown menu");
     		return false;
     	}
     	return true;
     }
 	
 	public void scheduleToPlaylist() {
 		Log.i("Hello", "scheduleToPlaylist is called");
 		//TODO: hide keyboard?
 		if (Player.isPlaying()) {
 			if (_vibrator != null) {
 				_vibrator.cancel();
 			}
 			Player.stopMusic();
 			//TODO: what's happen if now playing alarm sound?
 			cancelAlarm();
 			showMessage(this, getString(R.string.stop_music));
 			return;
 		}
 		//set timer
 		Calendar now = new GregorianCalendar();
 		int target_hour = _time_picker.getCurrentHour().intValue();
 		int target_min = _time_picker.getCurrentMinute().intValue();
 		Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
 				now.get(Calendar.MONTH), now.get(Calendar.DATE), target_hour, target_min, 0);
 		long target_millis = target.getTimeInMillis();
 		String tommorow ="";
 		long now_millis = System.currentTimeMillis();
 		if (target_millis <= now_millis) {
 			//tomorrow
 			target_millis += 24 * 60 * 60 * 1000;
 			tommorow = " (" + getString(R.string.tomorrow) + ")";
 		}
 		//TODO: make function
 		_time_picker.setEnabled(false);
 		_time_label.setText(String.format("%2d/%2d %02d:%02d", target.get(Calendar.MONTH)+1, target.get(Calendar.DATE), target_hour, target_min));
 		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = makePintent(WAKEUP_ACTION);
 		mgr.set(AlarmManager.RTC_WAKEUP, target_millis, pendingIntent);
		showMessage(this, getString(R.string.alarm_set) + tommorow);
		Player.startSleepMusic();

 		if (target_millis - now_millis >= SLEEP_TIME) {
 			pendingIntent = makePintent(SLEEP_ACTION);
 			mgr.set(AlarmManager.RTC_WAKEUP, now_millis+SLEEP_TIME, pendingIntent);
 		}
 	}
 
 	public void onClick(View v) {
 		if (v == _next_button) {
 			Player.playNext();
 		} else if (v == _sleep_wakeup_button) {
 			scheduleToPlaylist();
 		} else {
 			showMessage(v.getContext(), getString(R.string.unknown_button));
 		}
 	}
 
 	private static void showMessage(Context c, String message) {
 		Toast.makeText(c, message, Toast.LENGTH_LONG).show();
 	}
 
 	//TODO: implement music player as Service to play long time
 	public static class Player extends BroadcastReceiver {
 		private static final String MUSIC_PATH = "/sdcard/music/";
 		private static MediaPlayer _player = null;
 		private static int _index = 0;
 
 		private static final String[] SLEEP_PLAYLIST = Playlist.SLEEP_PLAYLIST;
 		private static final String[] WAKEUP_PLAYLIST = Playlist.WAKEUP_PLAYLIST;
 
 		private static String[] current_playlist = SLEEP_PLAYLIST;
 
 		public static boolean isPlaying() {
 			return _player != null && _player.isPlaying();
 		}
 
 		public static void reset () {
 			_index = 0;
 		}
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 			Log.i("Hello", "action: " + intent.getAction());
 			if (intent.getAction().equals(WAKEUP_ACTION)) {
 				if (Player.isPlaying()) {
 					stopMusic();
 				}
 				Log.i("Hello", "Wakeup action");
 				AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				mgr.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
 				Player.reset();
 				startMusic(WAKEUP_PLAYLIST);
 				Intent i = new Intent(context, MalarmActivity.class);
 				i.setAction(WAKEUPAPP_ACTION);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(i);
 			} else if (intent.getAction().equals(SLEEP_ACTION)) {
 				stopMusic();
 				showMessage(context, context.getString(R.string.goodnight));
 				//TODO: power off?
 			}
 		}
 
 		public static void stopMusic() {
 			if (_player == null) {
 				return;
 			}
 			_player.stop();
 			// TODO: delete?
 		}
 
 		private static class SleepThread extends Thread {
 			long _sleeptime;
 
 			public SleepThread(long sleeptime) {
 				Log.i("Hello", "SleepThread is created");
 				_sleeptime = sleeptime;
 			}
 
 			public void run() {
 				Log.i("Hello", "SleepThread run");
 				try {
 					Thread.sleep(_sleeptime);
 					Player.stopMusic();
 					// TODO: sleep device?
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
		public static void startSleepMusic() {
 			Log.i("Hello", "start sleep music and stop");
			//long playtime_millis = System.currentTimeMillis() + 60 * 60 * 1000;
			long playtime_millis = System.currentTimeMillis() + 10 * 1000;
 			// TODO: use Alarm instead of Thread
 			SleepThread t = new SleepThread(playtime_millis);
 			t.start();
 			reset();
 			startMusic(SLEEP_PLAYLIST);
 		}
 
 		public static void playNext() {
 			_index++;
 			Log.i("Hello", "playNext is called: " + _index);
 			if (Player.isPlaying()) {
 				stopMusic();
 			}
 			startMusic(current_playlist);
 		}
 
 		public static class MusicCompletionListener implements
 			MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
 			public void onCompletion(MediaPlayer mp) {
 				// TODO: manage index....(sleep / wakeup)
 				Log.i("Hello", "onCompletion listener is called");
 				Player.playNext();
 			}
 
 			// This method is not called when DRM error is occured
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				// TODO: notify error
 				Log.i("Hello", "onError is called");
 				return false;
 			}
 		}
 
 		public static void startMusic(String[] playlist) {
 			current_playlist = playlist;
 			Log.i("Hello", "startMusic");
 			if (_player == null) {
 				_player = new MediaPlayer();
 				MusicCompletionListener l = new MusicCompletionListener();
 				_player.setOnCompletionListener(l);
 				_player.setOnErrorListener(l);
 			}
 			if (_player.isPlaying()) {
 				return;
 			}
 			String path = "";
 			for (int i = 0; i < playlist.length; i++) {
 				path = MUSIC_PATH + playlist[_index];
 				File f = new File(path);
 				// ....
 				if ((!path.endsWith(".m4p")) && f.exists()) {
 					break;
 				}
 				// TODO: use resource music
 				_index++;
 				if (_index >= playlist.length) {
 					_index = 0;
 				}
 			}
 			// TODO: handle error
 			try {
 				_player.reset();
 				_player.setDataSource(path);
 				_player.prepare();
 				_player.start();
 			} catch (IOException e) {
 
 			}
 		}
 	}
 }
