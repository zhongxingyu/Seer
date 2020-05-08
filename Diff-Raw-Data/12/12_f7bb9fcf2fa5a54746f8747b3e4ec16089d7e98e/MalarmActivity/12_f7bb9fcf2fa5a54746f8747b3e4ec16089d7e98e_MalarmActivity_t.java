 package com.mamewo.malarm24;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  * http://www002.upp.so-net.ne.jp/mamewo/
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
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
 import android.view.GestureDetector;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import android.webkit.*;
 import android.net.Uri;
 import android.graphics.Bitmap;
 
 public final class MalarmActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener, OnLongClickListener {
 	public static final String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	public static final String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	public static final String WAKEUPAPP_ACTION = PACKAGE_NAME + ".WAKEUPAPP_ACTION";
 	public static final String SLEEP_ACTION = PACKAGE_NAME + ".SLEEP_ACTION";
 	public static final String LOADWEB_ACTION = PACKAGE_NAME + ".LOADWEB_ACTION";
 	private static final String TAG = "malarm";
 	private static final String MYURL = "http://www002.upp.so-net.ne.jp/mamewo/mobile_shop.html";
 	//e.g. /sdcard/music
 	public static final File DEFAULT_PLAYLIST_PATH = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_MUSIC);
 	
 	private static final long VIBRATE_PATTERN[] = { 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2121;
 	protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
 
 	public static final String WAKEUP_PLAYLIST_FILENAME = "wakeup.m3u";
 	public static final String SLEEP_PLAYLIST_FILENAME = "sleep.m3u";
 	//copy stop.m4a file to stop native player
 	protected static final String STOP_MUSIC_FILENAME = "stop.m4a";
 	private static final String NATIVE_PLAYER_KEY = "nativeplayer";
 	private static final String PLAYLIST_PATH_KEY = "playlist_path";
 	private static final String VOLUME_KEY = "volume";
 	private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)時((\\d+)分|半)?");
 	private static final Pattern AFTER_TIME_PATTERN = Pattern.compile("((\\d+)時間)?((\\d+)分|半)?.*");
 
 	protected static String pref_playlist_path;
 
 	private static String[] WEB_PAGE_LIST = new String []{
 		"http://www002.upp.so-net.ne.jp/mamewo/mobile_shop.html"
 	};
 	public static M3UPlaylist wakeup_playlist;
 	public static M3UPlaylist sleep_playlist;
 	private static boolean pref_use_native_player;
 	private static boolean pref_vibrate;
 	private static int pref_sleep_volume;
 	private static int pref_wakeup_volume;
 	private static Integer pref_default_hour;
 	private static Integer pref_default_min;
 	
 	public final static class MalarmState implements Serializable {
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
 	
 	public final class MyCallListener extends PhoneStateListener {
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
 				Log.i(TAG, "onCallStateChanged: RINGING");
 				stopVibrator();
 				//native player stops automatically
 				mIsPlaying = Player.isPlaying();
 				if (mIsPlaying) {
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
 			wakeup_playlist = new M3UPlaylist(pref_playlist_path, WAKEUP_PLAYLIST_FILENAME);
 		} catch (FileNotFoundException e) {
 			Log.i(TAG, "wakeup playlist is not found: " + WAKEUP_PLAYLIST_FILENAME);
 		}
 		try {
 			sleep_playlist = new M3UPlaylist(pref_playlist_path, SLEEP_PLAYLIST_FILENAME);
 		} catch (FileNotFoundException e) {
 			Log.i(TAG, "sleep playlist is not found: " + SLEEP_PLAYLIST_FILENAME);
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
 				loadWebPage();
 			}
 			return true;
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		pref.registerOnSharedPreferenceChangeListener(this);
 		syncPreferences(pref, "ALL");
 //		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
 //				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
 		setContentView(R.layout.main);
 		
 		mSetDefaultTime = true;
 		
 		mTimePicker = (TimePicker) findViewById(R.id.timePicker1);
 		mTimePicker.setIs24HourView(true);
 
 		mTimePicker.setLongClickable(true);
 		mTimePicker.setOnLongClickListener(this);
 
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
 		mAlarmButton.setLongClickable(true);
 		mAlarmButton.setOnLongClickListener(this);
 
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
 				Log.i(TAG, "onTouch: event " + event + " gd: " + mGD);
 				mGD.onTouchEvent(event);
 				return false;
 			}
 		});
 		
 		final Activity activity = this;
 		mWebview.setWebViewClient(new WebViewClient() {
 			String previous_url = "";
 
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				Log.i(TAG, "onPageStart: " + url);
 				mLoadingIcon.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onLoadResource (WebView view, String url) {
 				//addhoc polling...
 				//TODO: move to resource
 				final int height = view.getContentHeight();
 				if ((url.contains("bijint") || url.contains("bijo-linux")) && height > 400) {
 					if(url.contains("binan") && height > 420) {
 						view.scrollTo(0, 420);
 					} else if (url.contains("bijo-linux") && height > 100) {
 						view.scrollTo(310, 770);
 					} else if (height > 960) {
						view.scrollTo(0, 980);
 					}
 				}
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				Log.i(TAG, "onPageFinshed: " + url);
 				mLoadingIcon.setVisibility(View.INVISIBLE);
				if(url.contains("yahoo") && url.contains("weather")) {
					view.scrollTo(0, 200);
				}
 			}
 		});
 
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
 		Log.i(TAG, "onResume is called, start JavaScript");
 		super.onResume();
 
 		mAlarmButton.requestFocus();
 		CookieSyncManager.getInstance().startSync();
 		//WebView.onResume is hidden, why!?!?
 		mWebview.getSettings().setJavaScriptEnabled(true);
 		loadWebPage();
 
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
 		super.onPause();
 		CookieSyncManager.getInstance().stopSync();
 		//stop tokei
 		mWebview.getSettings().setJavaScriptEnabled(false);
 		mWebview.stopLoading();
 	}
 
 	@Override
 	protected void onStart () {
 		super.onStart();
 		if (mState.mTargetTime != null) {
 			updateAlarmUI(mState.mTargetTime);
 		}
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
 
 	//escape preference value into static value
 	//TODO: improve design
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
 		if (update_all || "url_list".equals(key)) {
 			String liststr = pref.getString("url_list", "http://twitter.com/");
 			liststr += MultiListPreference.SEPARATOR + MYURL;
 			WEB_PAGE_LIST = liststr.split(MultiListPreference.SEPARATOR);
 		}
 		if (update_all || "use_native_player".equals(key)) {
 			pref_use_native_player = pref.getBoolean("use_native_player", false);
 		}
 		if (update_all || "vibrate".equals(key)) {
 			pref_vibrate = pref.getBoolean(key, true);
 		}
 		if (update_all || "playlist_path".equals(key)) {
 			final String newpath = pref.getString(key, DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 			if (! newpath.equals(pref_playlist_path)) {
 				pref_playlist_path = newpath;
 				loadPlaylist();
 			}
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
 		Log.i(TAG, "onSharedPreferenceChanged is called: key = " + key);
 		syncPreferences(pref, key);
 	}
 
 	/**
 	 * load current web page
 	 */
 	private void loadWebPage() {
 		if (mState.mWebIndex < 0) {
 			mState.mWebIndex = WEB_PAGE_LIST.length - 1;
 		}
 		if (mState.mWebIndex >= WEB_PAGE_LIST.length) {
 			mState.mWebIndex = 0;
 		}
 		final String url = WEB_PAGE_LIST[mState.mWebIndex];
 		loadWebPage(url);
 	}
 
 	//TODO: move to resource
 	private void adjustWebviewSetting(String url) {
 		final WebSettings config = mWebview.getSettings();
		if (url.contains("bijo-linux") || url.contains("google") || url.contains("yahoo") || url.contains("so-net")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		} else {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 		}
 		if (url.contains("bijo-linux")) {
 			config.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 		} else {
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		}
 	}
 	
 	private void loadWebPage(String url) {
 		showMessage(this, "Loading... \n" + url);
 		adjustWebviewSetting(url);
 		mWebview.loadUrl(url);
 	}
 
 	private void clearAlarmUI() {
 		mTimePicker.setEnabled(true);
 		mTimeLabel.setText("");
 		mTimePicker.setCurrentHour(pref_default_hour);
 		mTimePicker.setCurrentMinute(pref_default_min);
 	}
 
 	private void cancelAlarm () {
 		final PendingIntent p = makePlayPintent(WAKEUP_ACTION, true);
 		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		clearAlarmUI();
 		mState.mTargetTime = null;
 		mgr.cancel(p);
 		
 		final NotificationManager notify_mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notify_mgr.cancel(PACKAGE_NAME, 0);
 	}
 
 	//TODO: this method is not called until home button is pressed
 	protected void onNewIntent (Intent intent) {
 		Log.i (TAG, "onNewIntent is called");
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
 			loadWebPage(url);
 		}
 	}
 
 	//TODO: design
 	private PendingIntent makePlayPintent(String action, boolean use_native) {
 		final Intent i = new Intent(this, Player.class);
 		i.setAction(action);
 		i.putExtra(NATIVE_PLAYER_KEY, use_native);
 		i.putExtra(PLAYLIST_PATH_KEY, pref_playlist_path);
 		i.putExtra(VOLUME_KEY, pref_wakeup_volume);
 		
 		final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
 				PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
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
 		mAlarmButton.setChecked(mState.mTargetTime != null);
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
 	
 	private void playSleepMusic(long target_millis) {
 		final long now_millis = System.currentTimeMillis();
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		final int min = Integer.valueOf(pref.getString("sleeptime", "60"));
 		if (Player.isPlaying()) {
 			Player.pauseMusic();
 		}
 		Player.playSleepMusic(this);
 		final long sleep_time_millis = min * 60 * 1000;
 		if (target_millis - now_millis >= sleep_time_millis) {
 			final PendingIntent sleepIntent = makePlayPintent(SLEEP_ACTION, pref_use_native_player);
 			final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 			mgr.set(AlarmManager.RTC_WAKEUP, now_millis+sleep_time_millis, sleepIntent);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return target time in epoch time (miliseconds)
 	 */
 	private long setAlarm() {
 		Log.i(TAG, "scheduleToPlaylist is called");
 		//set timer
 		final Calendar now = new GregorianCalendar();
 
 		//remove focus from timeticker to save time which is entered by software keyboard
 		mTimePicker.clearFocus();
 		final int target_hour = mTimePicker.getCurrentHour().intValue();
 		final int target_min = mTimePicker.getCurrentMinute().intValue();
 		final Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
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
 
 		showMessage(this, getString(R.string.alarm_set) + tommorow);
 		String text = getString(R.string.notify_waiting_text);
 		text += " (" + dateStr(target) +")";
 		final String title = getString(R.string.notify_waiting_title);
 		setNotification(title, text);
 		//umm.. 
 		return target_millis;
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
 			if (Player.isPlaying()) {
 				Player.pauseMusic();
 			}
 			Player.playWakeupMusic(this, pref_use_native_player);
 			break;
 		case R.id.pref:
 			startActivity(new Intent(this, MalarmPreference.class));
 			break;
 		case R.id.stop_music:
 			Player.pauseMusic();
 			break;
 		default:
 			Log.i(TAG, "Unknown menu");
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
 				playSleepMusic(setAlarm());
 			}
 		} else if (v == mSetNowButton) {
 			setNow();
 		}
 	}
 
 	public static void showMessage(Context c, String message) {
 		Toast.makeText(c, message, Toast.LENGTH_LONG).show();
 	}
 
 	private void shortVibrate() {
 		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator != null) {
 			vibrator.vibrate(150);
 		}
 	}
 	
 	@Override
 	public boolean onLongClick(View view) {
 		if (view == mLoadingIcon) {
 			shortVibrate();
 			mWebview.stopLoading();
 			showMessage(this, getString(R.string.stop_loading));
 			return true;
 		}
 		if (view == mAlarmButton) {
 			if (mAlarmButton.isChecked()) {
 				return false;
 			}
 			shortVibrate();
 			final long target_millis = setAlarm();
 			new AlertDialog.Builder(this)
 			.setTitle(R.string.ask_play_sleep_tune)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					playSleepMusic(target_millis);
 				}
 			})
 			.setNegativeButton(R.string.no, null)
 			.create()
 			.show();
 			return true;
 		}
 		if (view != mTimePicker || ! view.isEnabled()) {
 			return false;
 		}
 		if (mVoiceIntent == null) {
 			//to reduce task of onCreate method
 			final PackageManager pm = getPackageManager();
 			final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 			if (activities.isEmpty()) {
 				mTimePicker.setOnLongClickListener(null);
 				return false;
 			} else {
 				mVoiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 				mVoiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 				mVoiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_dialog));
 			}
 		}
 		shortVibrate();
 		mWebview.stopLoading();
 		startActivityForResult(mVoiceIntent, VOICE_RECOGNITION_REQUEST_CODE);
 		return true;
 	}
 
 	private static class TimePickerTime {
 		public final int mHour;
 		public final int mMin;
 		public final String mSpeach;
 		
 		public TimePickerTime(int hour, int min, String speach) {
 			mHour = hour;
 			mMin = min;
 			mSpeach = speach;
 		}
 	}
 
 	private class ClickListener implements DialogInterface.OnClickListener {
 		private ArrayList<TimePickerTime> mTimeList;
 
 		public ClickListener(ArrayList<TimePickerTime> time) {
 			mTimeList = time;
 		}
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			setTimePickerTime(mTimeList.get(which));
 		}
 	}
 	
 	private void setTimePickerTime(TimePickerTime time) {
 		mSetDefaultTime = false;
 		mTimePicker.setCurrentHour(time.mHour);
 		mTimePicker.setCurrentMinute(time.mMin);
 		MessageFormat mf = new MessageFormat(getString(R.string.voice_success_format));
 		showMessage(this, mf.format(new Object[]{ time.mSpeach }));
 	}
 	
 	//TODO: support english???
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			ArrayList<TimePickerTime> result = new ArrayList<TimePickerTime>();
 			for (String speach : matches) {
 				Matcher m = TIME_PATTERN.matcher(speach);
 				if (m.matches()) {
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
 					result.add(new TimePickerTime(hour, minute, speach));
 				} else {
 					Matcher m2 = AFTER_TIME_PATTERN.matcher(speach);
 					if (m2.matches()) {
 						final String hour_part = m2.group(2);
 						final String min_part = m2.group(3);
 						if (hour_part == null && min_part == null) {
 							continue;
 						}
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
 						result.add(new TimePickerTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), speach));
 					}
 				}
 			}
 			if (result.isEmpty()) {
 				showMessage(this, getString(R.string.voice_fail));
 			} else if (result.size() == 1) {
 				setTimePickerTime(result.get(0));
 			} else {
 				String [] speach_array = new String[result.size()];
 				for (int i = 0; i < result.size(); i++) {
 					TimePickerTime time = result.get(i);
 					speach_array[i] = time.mSpeach + String.format(" (%02d:%02d)", time.mHour, time.mMin);
 				}
 				//select from list dialog
 				new AlertDialog.Builder(this)
 				.setTitle(R.string.select_time_from_list)
 				.setItems(speach_array, new ClickListener(result))
 				.create()
 				.show();
 			}
 		}
 	}
 
 	@Override
 	public void onLowMemory () {
 		showMessage(this, getString(R.string.low_memory));
 	}
 	
 	//TODO: implement music player as Service to play long time
 	//Player now extends BrowdcastReceiver because to stop music this class should be loaded
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
 			Log.i(TAG, "onReceive!!: action: " + intent.getAction());
 			if (intent.getAction().equals(WAKEUP_ACTION)) {
 				//TODO: load optional m3u file to play by request from other application
 				//TODO: what to do if calling
 				//initialize player...
 				if (Player.isPlaying()) {
 					Player.pauseMusic();
 				}
 				if (pref_playlist_path == null) {
 					pref_playlist_path = intent.getStringExtra(PLAYLIST_PATH_KEY);
 				}
 				AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 				//following two methods require MODIFY_AUDIO_SETTINGS permissions...
 				//TODO: add preference to permit volume up when external speaker is connected
 				int wakeup_volume = 5;
 				if ((! mgr.isWiredHeadsetOn()) && (! mgr.isBluetoothA2dpOn())) {
 					wakeup_volume = intent.getIntExtra(VOLUME_KEY, 5);
 					Log.i(TAG, "playWakeupMusic: set volume: " + pref_wakeup_volume);
 				}
 				mgr.setStreamVolume(AudioManager.STREAM_MUSIC, wakeup_volume, AudioManager.FLAG_SHOW_UI);
 				playWakeupMusic(context, false);
 				
 				Intent i = new Intent(context, MalarmActivity.class);
 				i.setAction(WAKEUPAPP_ACTION);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(i);
 				//but this activity is not executed...(sleep before delivered?)
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
 			File f = new File(pref_playlist_path + STOP_MUSIC_FILENAME);
 			if(! f.isFile()) {
 				Log.i(TAG, "No stop play list is found");
 				return;
 			}
 			playMusicNativePlayer(context, f);
 		}
 
 		public static void playWakeupMusic(Context context, boolean use_native) {
 			File f = new File(WAKEUP_PLAYLIST_FILENAME);
 
 			if (use_native && f.isFile()) {
 				playMusicNativePlayer(context, f);
 			} else {
 				if(wakeup_playlist == null) {
 					loadPlaylist();
 					if (wakeup_playlist == null) {
 						Log.i(TAG, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				wakeup_playlist.reset();
 				playMusic(wakeup_playlist);
 			}
 		}
 
 		public static void playSleepMusic(Context context) {
 			Log.i(TAG, "start sleep music and stop");
 			File f = new File(pref_playlist_path + SLEEP_PLAYLIST_FILENAME);
 			AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 			mgr.setStreamVolume(AudioManager.STREAM_MUSIC, pref_sleep_volume, AudioManager.FLAG_SHOW_UI);
 			if (pref_use_native_player && f.isFile()) {
 				Log.i(TAG, "playSleepMusic: NativePlayer");
 				playMusicNativePlayer(context, f);
 			} else {
 				Log.i(TAG, "playSleepMusic: MediaPlayer");
 				if(sleep_playlist == null) {
 					loadPlaylist();
 					if (sleep_playlist == null) {
 						Log.i(TAG, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				sleep_playlist.reset();
 				playMusic(sleep_playlist);
 			}
 		}
 
 		public static void playNext() {
 			Log.i(TAG, "playNext is called: ");
 			if (Player.isPlaying()) {
 				stopMusic();
 			}
 			playMusic(current_playlist);
 		}
 
 		public static class MusicCompletionListener implements
 		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
 			public void onCompletion(MediaPlayer mp) {
 				Log.i(TAG, "onCompletion listener is called");
 				Player.playNext();
 			}
 
 			// This method is not called when DRM error occurs
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				//TODO: show error message to GUI
 				Log.i(TAG, "onError is called, cannot play this media");
 				Player.playNext();
 				return true;
 			}
 		}
 
 		public static void playMusic(Playlist playlist) {
 			current_playlist = playlist;
 			Log.i(TAG, "playMusic");
 			if (playlist == null || playlist.isEmpty()) {
 				Log.i(TAG, "playMusic: playlist is null");
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
 			Log.i(TAG, "playMusic (from pause) is called");
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
 			Log.i(TAG, "pause music is called");
 			try {
 				mPlayer.pause();
 			} catch (Exception e) {
 				//do nothing
 			}
 		}
 	}
 }
