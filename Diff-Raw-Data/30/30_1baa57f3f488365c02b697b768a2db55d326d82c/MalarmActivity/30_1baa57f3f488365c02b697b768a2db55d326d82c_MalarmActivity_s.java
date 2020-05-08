 package com.mamewo.malarm;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  * http://www002.upp.so-net.ne.jp/mamewo/
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.GestureDetector;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView.HitTestResult;
 import android.widget.*;
 import android.webkit.*;
 import android.net.Uri;
 import android.graphics.Bitmap;
 
 public class MalarmActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener, OnLongClickListener {
 	private static final String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	public static final String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	public static final String WAKEUPAPP_ACTION = PACKAGE_NAME + ".WAKEUPAPP_ACTION";
 	public static final String SLEEP_ACTION = PACKAGE_NAME + ".SLEEP_ACTION";
 	public static final String LOADWEB_ACTION = PACKAGE_NAME + ".LOADWEB_ACTION";
 	//e.g. /sdcard/music
 	public static final File DEFAULT_PLAYLIST_PATH = new File(Environment.getExternalStorageDirectory(), "music");
 	
 	private static final long VIBRATE_PATTERN[] = { 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2121;
 	protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
 
 	public static final String WAKEUP_PLAYLIST_FILENAME = "wakeup.m3u";
 	public static final String SLEEP_PLAYLIST_FILENAME = "sleep.m3u";
 	//copy stop.m4a file to stop native player
 	protected static final String STOP_MUSIC_FILENAME = "stop.m4a";
 	private static final String NATIVE_PLAYER_KEY = "nativeplayer";
 	private static final String PLAYLIST_PATH_KEY = "playlist_path";
 	public static String version = "unknown";
 
 	protected static String playlist_path;
 	private static final String[] WEB_PAGE_LIST = new String []{
 		null,
 		"https://www.google.com/calendar/",
 		"http://www.google.com/reader/",
 		"http://www.google.com/mail/",
 		"http://www002.upp.so-net.ne.jp/mamewo/mobile_shop.html"
 	};
 	protected static Playlist wakeup_playlist;
 	protected static Playlist sleep_playlist;
 	private static boolean pref_use_native_player;
 	private static boolean pref_vibrate;
 	private static int pref_sleep_volume;
 	private static int pref_wakeup_volume;
 	private static Integer pref_default_hour = Integer.valueOf(7);
 	private static Integer pref_default_min = Integer.valueOf(0);
 	
 	public static class MalarmState implements Serializable {
 		private static final long serialVersionUID = 1L;
 		public Calendar mTargetTime;
 		public int mWebIndex;
 		
 		public MalarmState() {
 			mWebIndex = 0;
 			mTargetTime = null;
 		}
 	}
 
 	private MalarmState mState;
 	private Button mNextButton;
 	private TimePicker mTimePicker;
 	private TextView mTimeLabel;
 	private WebView mWebview;
 	private ToggleButton mAlarmButton;
 	private Button mSetNowButton;
 	private GestureDetector mGD;
 	private boolean mSetDefaultTime;
 	private Intent mVoiceIntent;
 	private ProgressBar mLoadingIcon;
 	
 	private PhoneStateListener mCallListener;
 
 	private static final int DOW_INDEX[] = {
 		Calendar.SUNDAY, 
 		Calendar.MONDAY, 
 		Calendar.TUESDAY, 
 		Calendar.WEDNESDAY, 
 		Calendar.THURSDAY, 
 		Calendar.FRIDAY, 
 		Calendar.SATURDAY, 
 	};
 	
 	public class MyCallListener extends PhoneStateListener {
 		private boolean mIsPlaying = false;
 
 		public MyCallListener(MalarmActivity context) {
 			super();
 			final TelephonyManager telmgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 			telmgr.listen(this, LISTEN_CALL_STATE);
 		}
 
 		public void onCallStateChanged (int state, String incomingNumber) {
 			switch (state) {
 			case TelephonyManager.CALL_STATE_RINGING:
 				//fall-through
 			case TelephonyManager.CALL_STATE_OFFHOOK:
 				Log.i(PACKAGE_NAME, "onCallStateChanged: RINGING");
 				stopVibrator();
 				//native player stops automatically
 				mIsPlaying = Player.isPlaying();
 				if (mIsPlaying) {
 					//pause
 					Player.pauseMusic();
 				}
 				break;
 			case TelephonyManager.CALL_STATE_IDLE:
 				if (mIsPlaying) {
 					Player.playMusic();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	//TODO: display toast if file is not found
 	public static void loadPlaylist() {
 		try {
 			wakeup_playlist = new M3UPlaylist(playlist_path, WAKEUP_PLAYLIST_FILENAME);
 		} catch (FileNotFoundException e) {
 			Log.i(PACKAGE_NAME, "wakeup playlist is not found: " + WAKEUP_PLAYLIST_FILENAME);
 		}
 		try {
 			sleep_playlist = new M3UPlaylist(playlist_path, SLEEP_PLAYLIST_FILENAME);
 		} catch (FileNotFoundException e) {
 			Log.i(PACKAGE_NAME, "sleep playlist is not found: " + SLEEP_PLAYLIST_FILENAME);
 		}
 	}
 
 	private class WebViewDblTapListener extends GestureDetector.SimpleOnGestureListener {
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			final int x = (int)e.getX();
 			final int width = mWebview.getWidth();
 			boolean start_browser = false;
 			final int side_width = width/3;
 			if (x <= side_width) {
 				mState.mWebIndex--;
 			} else if (x > width - side_width) {
 				mState.mWebIndex++;
 			} else {
 				start_browser = true;
 			}
 			if (start_browser) {
 				final String url = mWebview.getUrl();
 				final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(i);
 			} else {
 				loadWebPage(mWebview);
 			}
 			return true;
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i(PACKAGE_NAME, "onCreate is called");
 		
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		pref.registerOnSharedPreferenceChangeListener(this);
 		syncPreferences(pref, "ALL");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mSetDefaultTime = true;
 		
 		mTimePicker = (TimePicker) findViewById(R.id.timePicker1);
 		mTimePicker.setIs24HourView(true);
 		final PackageManager pm = getPackageManager();
 		final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.isEmpty()) {
 			mVoiceIntent = null;
 		} else {
 			//add listener
 			mTimePicker.setLongClickable(true);
 			mTimePicker.setOnLongClickListener(this);
 			mVoiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			mVoiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			mVoiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_dialog));
 		}
 
 		if (savedInstanceState == null) {
 			mState = new MalarmState();
 		} else {
 			mState = (MalarmState)savedInstanceState.get("state");
 		}
 		mLoadingIcon = (ProgressBar) findViewById(R.id.loading_icon);
 		mLoadingIcon.setOnLongClickListener(this);
 		
 		mNextButton = (Button) findViewById(R.id.next_button);
 		mNextButton.setOnClickListener(this);
 
 		mSetNowButton = (Button) findViewById(R.id.set_now_button);
 		mSetNowButton.setOnClickListener(this);
 
 		mTimeLabel = (TextView) findViewById(R.id.target_time_label);
 		mWebview = (WebView)findViewById(R.id.webView1);
 		mAlarmButton = (ToggleButton)findViewById(R.id.alarm_button);
 		mAlarmButton.setOnClickListener(this);
 		
 		CookieSyncManager.createInstance(this);
 		
 		final WebSettings config = mWebview.getSettings();
 		//to display twitter...
 		config.setDomStorageEnabled(true);
 		config.setJavaScriptEnabled(true);
 		config.setSupportZoom(true);
 		mWebview.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mWebview.requestFocus();
 				Log.i(PACKAGE_NAME, "onTouch: event " + event + " gd: " + mGD);
 				mGD.onTouchEvent(event);
 				return false;
 			}
 		});
 		
 		final Activity activity = this;
 		mWebview.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				Log.i(PACKAGE_NAME, "onPageStart: " + url);
 				mLoadingIcon.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 			}
 
 			String previous_url = "";
 			@Override
 			public void onLoadResource (WebView view, String url) {
 				//Log.i(PACKAGE_NAME, "loading: " + view.getHitTestResult().getType() + ": " + url);
 				if (url.contains("bijo-linux") && url.endsWith("/")) {
 					final HitTestResult result = view.getHitTestResult();
 					//TODO: why same event delivered many times?
 					if (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE && ! previous_url.equals(url)) {
 						mWebview.stopLoading();
 						previous_url = url;
 						loadWebPage(mWebview, url);
 						return;
 					}
 				}
 				//addhoc polling...
 				final int height = view.getContentHeight();
 				if ((url.contains("bijint") || url.contains("bijo-linux")) && height > 400) {
 					if(url.contains("binan") && height > 420) {
 						view.scrollTo(0, 420);
 					} else if (url.contains("bijo-linux") && height > 100) {
 						//TODO: open next page in same tab
 						view.scrollTo(0, 100);
 					} else if (height > 960) {
 						view.scrollTo(0, 960);
 					}
 				}
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				Log.i(PACKAGE_NAME, "onPageFinshed: " + url);
 				mLoadingIcon.setVisibility(View.INVISIBLE);
 			}
 		});
 
 		//load version
 		InputStream is = null;
 		try {
 			is = getResources().openRawResource(R.raw.app_version);
 			final Properties prop = new Properties();
 			prop.load(is);
 			version = prop.getProperty("app.version");
 		} catch (IOException e) {
 			Log.i(PACKAGE_NAME, "cannot get version: " + e.getMessage());
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 					Log.i(PACKAGE_NAME, "cannot close is: " + e.getMessage());
 				}
 			}
 		}
 		if (version == null) {
 			version = "unknown";
 		}
 
 		//stop alarm when phone call
 		mCallListener = new MyCallListener(this);
 		mGD = new GestureDetector(this, new WebViewDblTapListener());
 	}
 
 	public void startVibrator() {
 		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.vibrate(VIBRATE_PATTERN, 1);
 	}
 	
 	public void stopVibrator() {
 		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.cancel();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		pref.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		Log.i(PACKAGE_NAME, "onResume is called, start JavaScript");
 		super.onResume();
 
 		CookieSyncManager.getInstance().startSync();
 		//WebView.onResume is hidden, why!?!?
 		mWebview.getSettings().setJavaScriptEnabled(true);
 		loadWebPage(mWebview);
 
 		if (mTimePicker.isEnabled()) {
 			if (mSetDefaultTime) {
 				mTimePicker.setCurrentHour(pref_default_hour);
 				mTimePicker.setCurrentMinute(pref_default_min);
 			} else {
 				mSetDefaultTime = true;
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		Log.i(PACKAGE_NAME, "onPause is called, stop JavaScript");
 		super.onPause();
 		CookieSyncManager.getInstance().stopSync();
 		//stop tokei
 		mWebview.getSettings().setJavaScriptEnabled(false);
 		mWebview.stopLoading();
 	}
 
 	@Override
 	protected void onStart () {
 		Log.i(PACKAGE_NAME, "onStart is called");
 		super.onStart();
 		if (mState.mTargetTime != null) {
 			updateAlarmUI(mState.mTargetTime);
 		}
 		mAlarmButton.setChecked(mState.mTargetTime != null);
 		mAlarmButton.requestFocus();
 	}
 
 	//Avoid finishing activity not to lost _state
 	@Override
 	public void onBackPressed() {
 		if (mWebview.canGoBack() && mWebview.hasFocus()) {
 			mWebview.goBack();
 			return;
 		}
 		moveTaskToBack(false);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.i("malarm", "onSaveInstanceState is called");
 		outState.putSerializable("state", mState);
 	}
 
 	public void syncPreferences(SharedPreferences pref, String key) {
 		final boolean update_all = "ALL".equals(key);
 		if (update_all || "default_time".equals(key)) {
 			final String timestr = pref.getString("default_time", "7:00");
 			final String[] split_timestr = timestr.split(":");
 			if (split_timestr.length == 2) {
 				pref_default_hour = Integer.valueOf(split_timestr[0]);
 				pref_default_min = Integer.valueOf(split_timestr[1]);
 			}
 		}
 		if (update_all || "sleep_volume".equals(key)) {
 			pref_sleep_volume = Integer.parseInt(pref.getString("sleep_volume", "5"));
 		}
 		if (update_all || "wakeup_volume".equals(key)) {
 			pref_wakeup_volume = Integer.parseInt(pref.getString("wakeup_volume", "5"));
 		}
 		if (update_all || "url".equals(key)) {
 			WEB_PAGE_LIST[0] = pref.getString("url", "http://twitter.com/");
 		}
 		if (update_all || "use_native_player".equals(key)) {
 			pref_use_native_player = pref.getBoolean("use_native_player", false);
 		}
 		if (update_all || "vibrate".equals(key)) {
 			pref_vibrate = pref.getBoolean(key, true);
 		}
 		if (update_all || "playlist_path".equals(key)) {
 			final String newpath = pref.getString(key, DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 			if (! newpath.equals(playlist_path)) {
 				playlist_path = newpath;
 				loadPlaylist();
 			}
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
 		Log.i(PACKAGE_NAME, "onSharedPreferenceChanged is called: key = " + key);
 		syncPreferences(pref, key);
 	}
 
 	private void loadWebPage(WebView view) {
 		if (mState.mWebIndex < 0) {
 			mState.mWebIndex = WEB_PAGE_LIST.length - 1;
 		}
 		if (mState.mWebIndex >= WEB_PAGE_LIST.length) {
 			mState.mWebIndex = 0;
 		}
 		final String url = WEB_PAGE_LIST[mState.mWebIndex];
 		loadWebPage(view, url);
 	}
 
 	private void loadWebPage(WebView view, String url) {
 		Log.i(PACKAGE_NAME, "loadWebPage: " + url);
 		final WebSettings config = mWebview.getSettings();
 		if (url.contains("bijo-linux") || url.contains("google")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
 		} else if (! url.contains("mamewo")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 		}
 		if (url.contains("bijo-linux")) {
 			config.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 		} else {
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		}
 		showMessage(this, "Loading... \n" + url);
 		view.loadUrl(url);
 	}
 
 	private void clearAlarmUI() {
 		mTimePicker.setEnabled(true);
 		mTimeLabel.setText("");
 		mTimePicker.setCurrentHour(pref_default_hour);
 		mTimePicker.setCurrentMinute(pref_default_min);
 	}
 
 	private void cancelAlarm () {
 		Log.i(PACKAGE_NAME, "cancelAlarm");
 		final PendingIntent p = makePlayPintent(WAKEUP_ACTION, true);
 		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		clearAlarmUI();
 		mState.mTargetTime = null;
 		mgr.cancel(p);
 		
 		final NotificationManager notify_mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notify_mgr.cancel(PACKAGE_NAME, 0);
 	}
 
 	protected void onNewIntent (Intent intent) {
 		Log.i (PACKAGE_NAME, "onNewIntent is called");
 		final String action = intent.getAction();
 		if (action == null) {
 			return;
 		}
 		if (action.equals(WAKEUPAPP_ACTION)) {
 			//native player cannot start until lock screen is displayed
 			if (pref_vibrate) {
 				startVibrator();
 			}
 			setNotification(getString(R.string.notify_wakeup_title), getString(R.string.notify_wakeup_text));
 		} else if (action.equals(LOADWEB_ACTION)) {
 			final String url = intent.getStringExtra("url");
 			loadWebPage(mWebview, url);
 		}
 	}
 
 	private PendingIntent makePlayPintent(String action, boolean use_native) {
 		final Intent i = new Intent(this, Player.class);
 		i.setAction(action);
 		i.putExtra(NATIVE_PLAYER_KEY, use_native);
 		i.putExtra(PLAYLIST_PATH_KEY, playlist_path);
 
 		final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	//add menu to cancel alarm
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate our menu which can gather user input for switching camera
 		final MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.alarm_menu, menu);
 		return true;
 	}
 
 	private String dateStr(Calendar target) {
 		String dow_str = "";
 		final int dow_int = target.get(Calendar.DAY_OF_WEEK);
 		final String[] dow_name_table = getResources().getStringArray(R.array.day_of_week);
 		for (int i = 0; i < DOW_INDEX.length; i++) {
 			if (DOW_INDEX[i] == dow_int) {
 				dow_str = dow_name_table[i];
 				break;
 			}
 		}
 		return String.format("%2d/%2d %02d:%02d (%s)",
 				target.get(Calendar.MONTH)+1,
 				target.get(Calendar.DATE),
 				target.get(Calendar.HOUR_OF_DAY),
 				target.get(Calendar.MINUTE),
 				dow_str);
 	}
 
 	private void updateAlarmUI (Calendar target) {
 		mTimeLabel.setText(dateStr(target));
 		mTimePicker.setCurrentHour(target.get(Calendar.HOUR_OF_DAY));
 		mTimePicker.setCurrentMinute(target.get(Calendar.MINUTE));
 		mTimePicker.setEnabled(false);
 	}
 
 	private void stopAlarm() {
 		cancelAlarm();
 		stopVibrator();
 		if (! pref_use_native_player) {
 			Player.pauseMusic();
 			showMessage(this, getString(R.string.music_stopped));
 		}
 	}
 	
 	private void setNotification(String title, String text) {
 		final Notification note = new Notification(R.drawable.img, title, System.currentTimeMillis());
 		
 		final Intent ni = new Intent(this, MalarmActivity.class);
 		final PendingIntent npi = PendingIntent.getActivity(this, 0, ni, 0);
 		note.setLatestEventInfo(this, title, text, npi);
 		final NotificationManager notify_mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notify_mgr.notify(PACKAGE_NAME, 0, note);
 	}
 	
 	private void setAlarm() {
 		Log.i(PACKAGE_NAME, "scheduleToPlaylist is called");
 		//set timer
 		final Calendar now = new GregorianCalendar();
 
 		//remove focus from timeticker to save time which is entered by software keyboard
 		mTimePicker.clearFocus();
 		final int target_hour = mTimePicker.getCurrentHour().intValue();
 		final int target_min = mTimePicker.getCurrentMinute().intValue();
 		Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
 				now.get(Calendar.MONTH), now.get(Calendar.DATE), target_hour, target_min, 0);
 		long target_millis = target.getTimeInMillis();
 		String tommorow ="";
 		final long now_millis = System.currentTimeMillis();
 		if (target_millis <= now_millis) {
 			//tomorrow
 			target_millis += 24 * 60 * 60 * 1000;
 			target.setTimeInMillis(target_millis);
 			tommorow = " (" + getString(R.string.tomorrow) + ")";
 		}
 		mState.mTargetTime = target;
 		updateAlarmUI(target);
 
 		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		final PendingIntent pendingIntent = makePlayPintent(WAKEUP_ACTION, false);
 		mgr.set(AlarmManager.RTC_WAKEUP, target_millis, pendingIntent);
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		final int min = Integer.valueOf(pref.getString("sleeptime", "60"));
 		Player.playSleepMusic(this, min);
 		final long sleep_time_millis = min * 60 * 1000;
 		final String sleeptime_str = min + " " + getString(R.string.minutes);
 		if (target_millis - now_millis >= sleep_time_millis) {
 			final PendingIntent sleepIntent = makePlayPintent(SLEEP_ACTION, pref_use_native_player);
 			mgr.set(AlarmManager.RTC_WAKEUP, now_millis+sleep_time_millis, sleepIntent);
 		}
 		showMessage(this, getString(R.string.alarm_set) + tommorow + " " + sleeptime_str);
 		String text = getString(R.string.notify_waiting_text);
 		text += " (" + dateStr(target) +")";
 		final String title = getString(R.string.notify_waiting_title);
 		setNotification(title, text);
 	}
 
 	public void setNow() {
 		if (mTimePicker.isEnabled()) {
 			final Calendar now = new GregorianCalendar();
 			mTimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
 			mTimePicker.setCurrentMinute(now.get(Calendar.MINUTE));
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.stop_vibration:
 			stopVibrator();
 			showMessage(this, getString(R.string.notify_wakeup_text));
 			break;
 		case R.id.play_wakeup:
 			Player.playWakeupMusic(this, pref_use_native_player);
 			break;
 		case R.id.pref:
 			startActivity(new Intent(this, MyPreference.class));
 			break;
 		case R.id.stop_music:
 			Player.pauseMusic();
 			break;
 		default:
 			Log.i(PACKAGE_NAME, "Unknown menu");
 			return false;
 		}
 		return true;
 	}
 
 	public void onClick(View v) {
 		//to save time value edited by software keyboard
 		if (v == mNextButton) {
 			Player.playNext();
 		} else if (v == mAlarmButton) {
 			InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			mgr.hideSoftInputFromWindow(mTimePicker.getWindowToken(), 0);
 			if (mState.mTargetTime != null) {
 				stopAlarm();
 			} else {
 				setAlarm();
 			}
 		} else if (v == mSetNowButton) {
 			setNow();
 		}
 	}
 
 	public static void showMessage(Context c, String message) {
 		Toast.makeText(c, message, Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public boolean onLongClick(View view) {
 		if (view == mLoadingIcon) {
 			mWebview.stopLoading();
 			showMessage(this, getString(R.string.stop_loading));
 			return true;
 		}
 		if (view != mTimePicker || mVoiceIntent == null || ! view.isEnabled()) {
 			return false;
 		}
 		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		vibrator.vibrate(200);
 		startActivityForResult(mVoiceIntent, VOICE_RECOGNITION_REQUEST_CODE);
 		return true;
 	}
 
 	//TODO: support english???
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
 			final Pattern p = Pattern.compile("(\\d+)時((\\d+)分|半)?");
 			final Pattern p2 = Pattern.compile("((\\d+)時間)?((\\d+)分|半)?.*");
 			
 			//TODO: filter and list candidate
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			boolean is_matched = false;
 			for (String speach : matches) {
 				Matcher m = p.matcher(speach);
 				if (m.matches()) {
 					is_matched = true;
 					int hour = Integer.valueOf(m.group(1)) % 24;
 					int minute;
 					String min_part = m.group(2);
 					if (min_part == null) {
 						minute = 0;
 					} else if ("半".equals(min_part)) {
 						minute = 30;
 					} else {
 						minute = Integer.valueOf(m.group(3)) % 60;
 					}
 					mTimePicker.setCurrentHour(hour);
 					mTimePicker.setCurrentMinute(minute);
 					mSetDefaultTime = false;
 					break;
 				} else {
 					Matcher m2 = p2.matcher(speach);
 					if (m2.matches()) {
 						final String hour_part = m2.group(2);
 						final String min_part = m2.group(3);
 						if (hour_part == null && min_part == null) {
 							continue;
 						}
 						is_matched = true;
 						long after_millis = 0;
 						if (hour_part != null) {
 							after_millis += 60 * 60 * 1000 * Integer.valueOf(hour_part);
 						}
 						if (min_part != null){
 							if ("半".equals(min_part)) {
 								after_millis += 60 * 1000 * 30;
 							} else {
 								long int_data = Integer.valueOf(m2.group(4));
 								after_millis += 60 * 1000 * int_data;
 							}
 						}
 						final Calendar cal = new GregorianCalendar();
 						cal.setTimeInMillis(System.currentTimeMillis() + after_millis);
 						mTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
 						mTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
 						mSetDefaultTime = false;
 						MessageFormat mf = new MessageFormat(getString(R.string.voice_success_format));
 						showMessage(this, mf.format(new Object[]{ speach }));
 						break;
 					}
 				}
 			}
 			if (! is_matched) {
 				showMessage(this, getString(R.string.voice_fail));
 			}
 		}
 	}
 
 	//TODO: separate BroadcastReceiver
 	//TODO: implement music player as Service to play long time
 	public static class Player extends BroadcastReceiver {
 		private static Playlist current_playlist = sleep_playlist;
 		private static MediaPlayer mPlayer = null;
 
 		public static boolean isPlaying() {
 			return mPlayer != null && mPlayer.isPlaying();
 		}
 
 		/**
 		 * intent: com.mamewo.malarm.MalarmActivity.WAKEUP_ACTION
 		 * extra: playlist_path: path to playlist where wakeup.m3u exists
 		 */
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 			Log.i(PACKAGE_NAME, "onReceive!!: action: " + intent.getAction());
 			if (intent.getAction().equals(WAKEUP_ACTION)) {
 				//TODO: load optional m3u file to play by request from other application
 				//TODO: what to do if calling
 				if (playlist_path == null) {
 					playlist_path = intent.getStringExtra(PLAYLIST_PATH_KEY);
 				}
				if (wakeup_playlist == null) {
					loadPlaylist();
				}

				Log.i(PACKAGE_NAME, "Wakeup action");
				if (Player.isPlaying()) {
					stopMusic();
				}
				Player.playWakeupMusic(context, false);

 				Intent i = new Intent(context, MalarmActivity.class);
 				i.setAction(WAKEUPAPP_ACTION);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(i);
 			} else if (intent.getAction().equals(SLEEP_ACTION)) {
 				if (intent.getExtras().getBoolean(NATIVE_PLAYER_KEY)) {
 					Player.stopMusicNativePlayer(context);
 				} else {
 					Player.pauseMusic();
 				}
 				showMessage(context, context.getString(R.string.goodnight));
 			}
 		}
 
 		public static void stopMusic() {
 			if (mPlayer == null) {
 				return;
 			}
 			mPlayer.stop();
 		}
 
 		public static void playMusicNativePlayer(Context context, File f) {
 			Intent i = new Intent();
 			i.setAction(Intent.ACTION_VIEW);
 			i.setDataAndType(Uri.fromFile(f), "audio/*");
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(i);
 		}
 
 		public static void stopMusicNativePlayer(Context context) {
 			File f = new File(playlist_path + STOP_MUSIC_FILENAME);
 			if(! f.isFile()) {
 				Log.i(PACKAGE_NAME, "No stop play list is found");
 				return;
 			}
 			playMusicNativePlayer(context, f);
 		}
 
 		public static void playWakeupMusic(Context context, boolean use_native) {
 			File f = new File(WAKEUP_PLAYLIST_FILENAME);
			AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			//following two methods require MODIFY_AUDIO_SETTINGS permissions...
			if ((! mgr.isWiredHeadsetOn()) && (! mgr.isBluetoothA2dpOn())) {
				mgr.setStreamVolume(AudioManager.STREAM_MUSIC, pref_wakeup_volume, 0);
			}
 
 			if (use_native && f.isFile()) {
 				playMusicNativePlayer(context, f);
 			} else {
 				if(wakeup_playlist == null) {
 					loadPlaylist();
 					if (wakeup_playlist == null) {
 						Log.i(PACKAGE_NAME, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				wakeup_playlist.reset();
 				playMusic(wakeup_playlist);
 			}
 		}
 
 		public static void playSleepMusic(Context context, int min) {
 			Log.i(PACKAGE_NAME, "start sleep music and stop");
 			File f = new File(playlist_path + SLEEP_PLAYLIST_FILENAME);
 			AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 			mgr.setStreamVolume(AudioManager.STREAM_MUSIC, pref_sleep_volume, 0);
 			if (pref_use_native_player && f.isFile()) {
 				Log.i(PACKAGE_NAME, "playSleepMusic: NativePlayer");
 				playMusicNativePlayer(context, f);
 			} else {
 				Log.i(PACKAGE_NAME, "playSleepMusic: MediaPlayer");
 				if(sleep_playlist == null) {
 					loadPlaylist();
 					if (sleep_playlist == null) {
 						Log.i(PACKAGE_NAME, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				sleep_playlist.reset();
 				playMusic(sleep_playlist);
 			}
 		}
 
 		public static void playNext() {
 			Log.i(PACKAGE_NAME, "playNext is called: ");
 			if (Player.isPlaying()) {
 				stopMusic();
 			}
 			playMusic(current_playlist);
 		}
 
 		public static class MusicCompletionListener implements
 		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
 			public void onCompletion(MediaPlayer mp) {
 				Log.i(PACKAGE_NAME, "onCompletion listener is called");
 				Player.playNext();
 			}
 
 			// This method is not called when DRM error occurs
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				//TODO: show error message to GUI
 				Log.i(PACKAGE_NAME, "onError is called, cannot play this media");
 				Player.playNext();
 				return true;
 			}
 		}
 
 		public static void playMusic(Playlist playlist) {
 			current_playlist = playlist;
 			Log.i(PACKAGE_NAME, "playMusic");
 			if (playlist == null || playlist.isEmpty()) {
 				Log.i(PACKAGE_NAME, "playMusic: playlist is null");
 				return;
 			}
 			if (mPlayer == null) {
 				mPlayer = new MediaPlayer();
 				MusicCompletionListener l = new MusicCompletionListener();
 				mPlayer.setOnCompletionListener(l);
 				mPlayer.setOnErrorListener(l);
 			}
 			if (mPlayer.isPlaying()) {
 				return;
 			}
 			String path = "";
 			//skip unsupported files filtering by filename ...
 			for (int i = 0; i < 10; i++) {
 				path = playlist.next();
 				File f = new File(path);
 				// ....
 				if ((!path.endsWith(".m4p")) && f.exists()) {
 					break;
 				}
 			}
 			try {
 				mPlayer.reset();
 				mPlayer.setDataSource(path);
 				mPlayer.prepare();
 				mPlayer.start();
 			} catch (IOException e) {
 				//do nothing
 			}
 		}
 
 		public static void playMusic() {
 			Log.i(PACKAGE_NAME, "playMusic (from pause) is called");
 			if (current_playlist == null) {
 				return;
 			}
 			try {
 				mPlayer.start();
 			} catch (Exception e) {
 				//do nothing
 			}
 		}
 
 		public static void pauseMusic() {
 			Log.i(PACKAGE_NAME, "pause music is called");
 			try {
 				mPlayer.pause();
 			} catch (Exception e) {
 				//do nothing
 			}
 		}
 	}
 }
