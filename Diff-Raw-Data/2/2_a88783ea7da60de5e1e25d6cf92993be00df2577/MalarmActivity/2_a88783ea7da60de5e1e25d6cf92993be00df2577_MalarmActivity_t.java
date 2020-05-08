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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
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
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.GestureDetector;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import android.webkit.*;
 import android.net.Uri;
 import android.graphics.Bitmap;
 
 public final class MalarmActivity
 	extends Activity
 	implements OnClickListener, OnSharedPreferenceChangeListener,
 			OnLongClickListener, OnKeyListener
 {
 	public static final String PACKAGE_NAME = MalarmActivity.class.getPackage().getName();
 	public static final String WAKEUP_ACTION = PACKAGE_NAME + ".WAKEUP_ACTION";
 	public static final String WAKEUPAPP_ACTION = PACKAGE_NAME + ".WAKEUPAPP_ACTION";
 	public static final String SLEEP_ACTION = PACKAGE_NAME + ".SLEEP_ACTION";
 	public static final String LOADWEB_ACTION = PACKAGE_NAME + ".LOADWEB_ACTION";
 	private static final String TAG = "malarm";
 	private static final String MYURL = "http://www002.upp.so-net.ne.jp/mamewo/mobile_shop.html";
 	
 	private static final long VIBRATE_PATTERN[] = { 10, 1500, 500, 1500, 500, 1500, 500, 1500, 500 };
 	private static final int SPEECH_RECOGNITION_REQUEST_CODE = 2121;
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
 
 	protected static String prefPlaylistPath;
 
 	private static String[] WEB_PAGE_LIST = new String []{ MYURL };
 	public static M3UPlaylist wakeupPlaylist;
 	public static M3UPlaylist sleepPlaylist;
 	private static boolean pref_use_native_player;
 	private static boolean pref_vibrate;
 	private static int pref_sleep_volume;
 	private static int pref_wakeup_volume;
 	private static Integer pref_default_hour;
 	private static Integer pref_default_min;
 	private static MalarmState mState;
 
 	private ImageButton mSpeechButton;
 	private ImageButton mNextButton;
 	private TimePicker mTimePicker;
 	private TextView mTimeLabel;
 	private WebView mWebview;
 	private ToggleButton mAlarmButton;
 	private Button mSetNowButton;
 	private GestureDetector mGD;
 	private boolean mSetDefaultTime;
 	private Intent mSpeechIntent;
 	private ProgressBar mLoadingIcon;
 	private boolean mStartingSpeechActivity;
 	private TextView mPlaylistLabel;
 	private TextView mSleepTimeLabel;
 	
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
 
 	public final static class MalarmState implements Serializable {
 		private static final long serialVersionUID = 1L;
 		public Calendar mTargetTime;
 		public int mWebIndex;
 		public int mSleepMin;
 		
 		public MalarmState() {
 			mWebIndex = 0;
 			mTargetTime = null;
 			mSleepMin = 0;
 		}
 	}
 	
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
 				//TODO: play music
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	//TODO: display toast if file is not found
 	public static void loadPlaylist() {
 		try {
 			wakeupPlaylist = new M3UPlaylist(prefPlaylistPath, WAKEUP_PLAYLIST_FILENAME);
 		}
 		catch (FileNotFoundException e) {
 			Log.i(TAG, "wakeup playlist is not found: " + WAKEUP_PLAYLIST_FILENAME);
 		}
 		try {
 			sleepPlaylist = new M3UPlaylist(prefPlaylistPath, SLEEP_PLAYLIST_FILENAME);
 		}
 		catch (FileNotFoundException e) {
 			Log.i(TAG, "sleep playlist is not found: " + SLEEP_PLAYLIST_FILENAME);
 		}
 	}
 
 	private class WebViewDblTapListener 
 		extends GestureDetector.SimpleOnGestureListener
 	{
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			final int x = (int)e.getX();
 			final int y = (int)e.getY();
 			Log.i(TAG, "onDoubleTap: " + x + ", " + y);
 			final int width = mWebview.getWidth();
 			boolean start_browser = false;
 			final int side_width = width/3;
 			if (x <= side_width) {
 				mState.mWebIndex--;
 			}
 			else if (x > width - side_width) {
 				mState.mWebIndex++;
 			}
 			else {
 				start_browser = true;
 			}
 			if (start_browser) {
 				final String url = mWebview.getUrl();
 				final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(i);
 			}
 			else {
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
 		setContentView(R.layout.main);
 		mSetDefaultTime = true;
 				
 		mTimePicker = (TimePicker) findViewById(R.id.timePicker1);
 		mTimePicker.setIs24HourView(true);
 
 		if (savedInstanceState == null) {
 			mState = new MalarmState();
 		}
 		else {
 			mState = (MalarmState)savedInstanceState.get("state");
 		}
 		mLoadingIcon = (ProgressBar) findViewById(R.id.loading_icon);
 		mLoadingIcon.setOnLongClickListener(this);
 		
 		mPlaylistLabel = (TextView) findViewById(R.id.playlist_name_view);
 		mPlaylistLabel.setOnLongClickListener(this);
 		
 		mSpeechButton = (ImageButton) findViewById(R.id.set_by_voice);
 		mSpeechButton.setOnClickListener(this);
 		mStartingSpeechActivity = false;
 		
 		mNextButton = (ImageButton) findViewById(R.id.next_button);
 		mNextButton.setOnClickListener(this);
 		mNextButton.setOnLongClickListener(this);
 		
 		mSetNowButton = (Button) findViewById(R.id.set_now_button);
 		mSetNowButton.setOnClickListener(this);
 		
 		mTimeLabel = (TextView) findViewById(R.id.target_time_label);
 		mSleepTimeLabel = (TextView) findViewById(R.id.sleep_time_label);
 		
 		mWebview = (WebView)findViewById(R.id.webView1);
 		mAlarmButton = (ToggleButton)findViewById(R.id.alarm_button);
 		mAlarmButton.setOnClickListener(this);
 		mAlarmButton.setLongClickable(true);
 		mAlarmButton.setOnLongClickListener(this);
 		//umm...
 		mAlarmButton.setOnKeyListener(this);
 
 		CookieSyncManager.createInstance(this);
 		
 		//umm...
 		mWebview.setOnKeyListener(this);
 		final WebSettings webSettings = mWebview.getSettings();
 		//to display twitter...
 		webSettings.setDomStorageEnabled(true);
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setSupportZoom(true);
 		mWebview.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mWebview.requestFocus();
 				mGD.onTouchEvent(event);
 				return false;
 			}
 		});
 		
 		final Activity activity = this;
 		mWebview.setWebChromeClient(new WebChromeClient());
 		mWebview.setWebViewClient(new WebViewClient() {
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
 				if ((url.contains("bijint") ||
 						url.contains("bijo-linux")) && height > 400) {
 					if(url.contains("binan") && height > 420) {
 						view.scrollTo(0, 420);
 					}
 					else if (url.contains("bijo-linux") && height > 100) {
 						view.scrollTo(310, 740);
 					}
 					else if (height > 960) {
 						view.scrollTo(0, 980);
 					}
 				}
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				Log.i(TAG, "onPageFinshed: " + url);
 				mLoadingIcon.setVisibility(View.INVISIBLE);
 				if(url.contains("weather.yahoo")) {
 					view.scrollTo(0, 180);
 				}
 			}
 		});
 
 		//stop alarm when phone call
 		mCallListener = new MyCallListener(this);
 		mGD = new GestureDetector(this, new WebViewDblTapListener());
 	}
 
 	public void startVibrator() {
 		final Vibrator vibrator = 
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.vibrate(VIBRATE_PATTERN, 1);
 	}
 	
 	public void stopVibrator() {
 		final Vibrator vibrator =
 				(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (vibrator == null) {
 			return;
 		}
 		vibrator.cancel();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		final SharedPreferences pref = 
 				PreferenceManager.getDefaultSharedPreferences(this);
 		pref.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		Log.i(TAG, "onResume is called, start JavaScript");
 		super.onResume();
 		mStartingSpeechActivity = false;
 		
 		mAlarmButton.requestFocus();
 		//WebView.onResume is hidden, why!?!?
 		mWebview.getSettings().setJavaScriptEnabled(true);
 		updateUI();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		//stop tokei
 		mWebview.getSettings().setJavaScriptEnabled(false);
 		mWebview.stopLoading();
 	}
 
 	@Override
 	protected void onStart () {
 		super.onStart();
 		CookieSyncManager.getInstance().startSync();
 		loadWebPage();
 	}
 	
 	@Override
 	protected void onStop(){
 		CookieSyncManager.getInstance().stopSync();
 		super.onStop();
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
 			final String timestr = pref.getString("default_time", MalarmPreference.DEFAULT_WAKEUP_TIME);
 			final String[] split_timestr = timestr.split(":");
 			if (split_timestr.length == 2) {
 				pref_default_hour = Integer.valueOf(split_timestr[0]);
 				pref_default_min = Integer.valueOf(split_timestr[1]);
 			}
 		}
 		if (update_all || "sleep_volume".equals(key)) {
 			pref_sleep_volume =
 					Integer.valueOf(pref.getString("sleep_volume", MalarmPreference.DEFAULT_SLEEP_VOLUME));
 		}
 		if (update_all || "wakeup_volume".equals(key)) {
 			pref_wakeup_volume =
 					Integer.valueOf(pref.getString("wakeup_volume", MalarmPreference.DEFAULT_WAKEUP_VOLUME));
 		}
 		if (update_all || "url_list".equals(key)) {
 			String liststr = pref.getString("url_list", MalarmPreference.DEFAULT_WEB_LIST);
			if(0 < liststr.length()){
 				liststr += MultiListPreference.SEPARATOR;
 			}
 			liststr += MYURL;
 			WEB_PAGE_LIST = liststr.split(MultiListPreference.SEPARATOR);
 		}
 		if (update_all || "use_native_player".equals(key)) {
 			pref_use_native_player = pref.getBoolean("use_native_player", false);
 		}
 		if (update_all || "vibrate".equals(key)) {
 			pref_vibrate = pref.getBoolean(key, MalarmPreference.DEFAULT_VIBRATION);
 		}
 		if (update_all || "playlist_path".equals(key)) {
 			final String newpath = 
 					pref.getString(key, MalarmPreference.DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 			if (! newpath.equals(prefPlaylistPath)) {
 				prefPlaylistPath = newpath;
 				loadPlaylist();
 			}
 		}
 		Log.i(TAG, "syncPref: key " + key);
 		if("clear_webview_cache".equals(key)){
 			mWebview.clearCache(true);
 			mWebview.clearHistory();
 			mWebview.clearFormData();
 			CookieManager mgr = CookieManager.getInstance();
 			mgr.removeAllCookie();
 			showMessage(this, getString(R.string.webview_cache_cleared));
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
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
 		if (url.contains("bijo-linux") || 
 			url.contains("google") ||
 			url.contains("yahoo") ||
 			url.contains("so-net")) {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
 		}
 		else {
 			config.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
 		}
 		if (url.contains("bijo-linux") || url.contains("bijin-tokei")) {
 			config.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 		}
 		else {
 			config.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		}
 	}
 	
 	private void loadWebPage(String url) {
 		showMessage(this, "Loading... \n" + url);
 		adjustWebviewSetting(url);
 		mWebview.loadUrl(url);
 	}
 	
 	/**
 	 * call updateUI from caller
 	 */
 	private void cancelAlarmTimer() {
 		if(mState.mTargetTime == null) {
 			return;
 		}
 		final PendingIntent p = makePlayPintent(WAKEUP_ACTION, false);
 		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		mgr.cancel(p);
 		mState.mTargetTime = null;
 	}
 	
 	/**
 	 * call updateUI from caller
 	 */
 	private void cancelSleepTimer() {
 		if (mState.mSleepMin == 0) {
 			return;
 		}
 		final PendingIntent sleep = makePlayPintent(SLEEP_ACTION, false);
 		final AlarmManager mgr =
 				(AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		mgr.cancel(sleep);
 		mState.mSleepMin = 0;
 	}
 
 	//onResume is called after this method is called
 	//TODO: call setNewIntent and handle in onResume?
 	//TODO: this method is not called until home button is pressed
 	protected void onNewIntent (Intent intent) {
 		Log.i (TAG, "onNewIntent is called");
 		final String action = intent.getAction();
 		if (action == null) {
 			return;
 		}
 		if (action.equals(WAKEUPAPP_ACTION)) {
 			//native player cannot start until lock screen is displayed
 			if(mState.mSleepMin > 0) {
 				mState.mSleepMin = 0;
 			}
 			if (pref_vibrate) {
 				startVibrator();
 			}
 			setNotification(getString(R.string.notify_wakeup_title),
 							getString(R.string.notify_wakeup_text));
 		}
 		else if (action.equals(LOADWEB_ACTION)) {
 			final String url = intent.getStringExtra("url");
 			loadWebPage(url);
 		}
 	}
 
 	//TODO: design
 	private PendingIntent makePlayPintent(String action, boolean useNative) {
 		final Intent i = new Intent(this, Player.class);
 		i.setAction(action);
 		i.putExtra(NATIVE_PLAYER_KEY, useNative);
 		i.putExtra(PLAYLIST_PATH_KEY, prefPlaylistPath);
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
 
 	private void updateUI () {
 		Calendar target = mState.mTargetTime;
 		if(null != target) {
 			mTimeLabel.setText(dateStr(target));
 			mTimePicker.setCurrentHour(target.get(Calendar.HOUR_OF_DAY));
 			mTimePicker.setCurrentMinute(target.get(Calendar.MINUTE));
 			mTimePicker.setEnabled(false);
 		}
 		else {
 			mTimePicker.setEnabled(true);
 			mTimeLabel.setText("");
 			if (mSetDefaultTime) {
 				mTimePicker.setCurrentHour(pref_default_hour);
 				mTimePicker.setCurrentMinute(pref_default_min);
 			}
 			else {
 				mSetDefaultTime = true;
 			}
 		}
 		int sleepMin = mState.mSleepMin;
 		if(sleepMin > 0) {
 			mSleepTimeLabel.setText(MessageFormat.format(getString(R.string.unit_min), 
 								Integer.valueOf(sleepMin)));
 		}
 		else {
 			mSleepTimeLabel.setText("");
 		}
 		mAlarmButton.setChecked(mState.mTargetTime != null);
 		mPlaylistLabel.setText(Player.getCurrentPlaylistName());
 	}
 
 	private void stopAlarm() {
 		final NotificationManager notify_mgr = 
 				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notify_mgr.cancel(PACKAGE_NAME, 0);
 		cancelSleepTimer();
 		cancelAlarmTimer();
 		updateUI();
 		stopVibrator();
 		if (! pref_use_native_player) {
 			Player.pauseMusic();
 			showMessage(this, getString(R.string.music_stopped));
 		}
 	}
 	
 	private void setNotification(String title, String text) {
 		final Notification note =
 				new Notification(R.drawable.img, title, System.currentTimeMillis());
 		
 		final Intent ni = new Intent(this, MalarmActivity.class);
 		final PendingIntent npi = PendingIntent.getActivity(this, 0, ni, 0);
 		note.setLatestEventInfo(this, title, text, npi);
 		final NotificationManager notify_mgr =
 				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notify_mgr.notify(PACKAGE_NAME, 0, note);
 	}
 	
 	private void setSleepTimer() {
 		final SharedPreferences pref =
 				PreferenceManager.getDefaultSharedPreferences(this);
 		final long nowMillis = System.currentTimeMillis();
 		long target = 0;
 		if(mState.mTargetTime != null) {
 			target = mState.mTargetTime.getTimeInMillis();
 		}
 		final int min = Integer.valueOf(pref.getString("sleeptime", MalarmPreference.DEFAULT_SLEEPTIME));
 		final long sleepTimeMillis = min * 60 * 1000;
 		mState.mSleepMin = min;
 		if (target == 0 || target - nowMillis >= sleepTimeMillis) {
 			final PendingIntent sleepIntent = 
 					makePlayPintent(SLEEP_ACTION, pref_use_native_player);
 			final AlarmManager mgr = 
 					(AlarmManager) getSystemService(Context.ALARM_SERVICE);
 			mgr.set(AlarmManager.RTC_WAKEUP, nowMillis + sleepTimeMillis, sleepIntent);
 			updateUI();
 		}
 	}
 	
 	private void playSleepMusic(long targetMillis) {
 		if (Player.isPlaying()) {
 			Player.pauseMusic();
 		}
 		Player.playSleepMusic(this);
 		setSleepTimer();
 	}
 	
 	/**
 	 * 
 	 * @return target time in epoch time (miliseconds)
 	 */
 	private void setAlarm() {
 		Log.i(TAG, "scheduleToPlaylist is called");
 		//set timer
 		final Calendar now = new GregorianCalendar();
 
 		//remove focus from timeticker to save time which is entered by software keyboard
 		mTimePicker.clearFocus();
 		final int target_hour = mTimePicker.getCurrentHour().intValue();
 		final int target_min = mTimePicker.getCurrentMinute().intValue();
 		final Calendar target = new GregorianCalendar(now.get(Calendar.YEAR),
 				now.get(Calendar.MONTH), now.get(Calendar.DATE), target_hour, target_min, 0);
 		long targetMillis = target.getTimeInMillis();
 		String tommorow ="";
 		final long nowMillis = System.currentTimeMillis();
 		if (targetMillis <= nowMillis) {
 			//tomorrow
 			targetMillis += 24 * 60 * 60 * 1000;
 			target.setTimeInMillis(targetMillis);
 			tommorow = " (" + getString(R.string.tomorrow) + ")";
 		}
 		mState.mTargetTime = target;
 
 		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		final PendingIntent pendingIntent = makePlayPintent(WAKEUP_ACTION, false);
 		mgr.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent);
 
 		showMessage(this, getString(R.string.alarm_set) + tommorow);
 		String text = getString(R.string.notify_waiting_text);
 		text += " (" + dateStr(target) +")";
 		final String title = getString(R.string.notify_waiting_title);
 		//umm...
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
 			if (Player.isPlaying()) {
 				break;
 			}
 			Player.playMusic();
 			break;
 		case R.id.pref:
 			//TODO: use startActivityForResult
 			startActivity(new Intent(this, MalarmPreference.class));
 			break;
 		case R.id.stop_music:
 			Player.pauseMusic();
 			cancelSleepTimer();
 			updateUI();
 			break;
 		default:
 			Log.i(TAG, "Unknown menu");
 			return false;
 		}
 		return true;
 	}
 
 	public void onClick(View v) {
 		if (v == mNextButton) {
 			if(Player.isPlaying()) {
 				Player.playNext();
 			}
 			// otherwise confirm and play music?
 		}
 		else if (v == mSpeechButton) {
 			setTimeBySpeech();
 		}
 		else if (v == mAlarmButton) {
 			InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			mgr.hideSoftInputFromWindow(mTimePicker.getWindowToken(), 0);
 			if (mState.mTargetTime != null) {
 				stopAlarm();
 			}
 			else {
 				setAlarm();
 				playSleepMusic(mState.mTargetTime.getTimeInMillis());
 				updateUI();
 			}
 		}
 		else if (v == mSetNowButton) {
 			setNow();
 		}
 	}
 
 	@Override
 	public boolean onKey(View view, int keyCode, KeyEvent event) {
 		if(event.getAction() == KeyEvent.ACTION_UP) {
 			int index = mState.mWebIndex;
 			boolean handled = false;
 			if((KeyEvent.KEYCODE_0 <= keyCode) &&
 				(keyCode <= KeyEvent.KEYCODE_9)) {
 				mState.mWebIndex = (keyCode - KeyEvent.KEYCODE_0) % WEB_PAGE_LIST.length;
 				loadWebPage();
 				handled = true;
 			}
 			return handled;
 		}
 		return false;
 	}
 
 	private void setTimeBySpeech() {
 		if (! mTimePicker.isEnabled() || mStartingSpeechActivity) {
 			return;
 		}
 		if (mSpeechIntent == null) {
 			//to reduce task of onCreate method
 			showMessage(this, getString(R.string.init_voice));
 			final PackageManager pm = getPackageManager();
 			final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 			if (activities.isEmpty()) {
 				return;
 			}
 			mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_dialog));
 		}
 		mStartingSpeechActivity = true;
 		mWebview.stopLoading();
 		startActivityForResult(mSpeechIntent, SPEECH_RECOGNITION_REQUEST_CODE);
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
 			setAlarm();
 			new AlertDialog.Builder(this)
 			.setTitle(R.string.ask_play_sleep_tune)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					playSleepMusic(mState.mTargetTime.getTimeInMillis());
 					updateUI();
 				}
 			})
 			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					updateUI();
 				}
 			})
 			.create()
 			.show();
 			return true;
 		}
 		if (view == mPlaylistLabel) {
 			if (Player.isPlaying()) {
 				return false;
 			}
 			shortVibrate();
 			Player.switchPlaylist();
 			updateUI();
 			return true;
 		}
 		if (view == mNextButton) {
 			shortVibrate();
 			if(! Player.isPlaying()) {
 				Player.playMusic();
 			}
 			cancelSleepTimer();
 			setSleepTimer();
 			updateUI();
 			showMessage(this, getString(R.string.play_with_sleep_timer));
 			return true;
 		}
 		return false;
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
 
 	private class ClickListener
 		implements DialogInterface.OnClickListener
 	{
 		private TimePickerTime[] mTimeList;
 
 		public ClickListener(TimePickerTime[] time) {
 			mTimeList = time;
 		}
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			setTimePickerTime(mTimeList[which]);
 		}
 	}
 	
 	private void setTimePickerTime(TimePickerTime time) {
 		mSetDefaultTime = false;
 		mTimePicker.setCurrentHour(time.mHour);
 		mTimePicker.setCurrentMinute(time.mMin);
 		String msg = MessageFormat.format(getString(R.string.voice_success_format), time.mSpeach);
 		showMessage(this, msg);
 	}
 	
 	//TODO: support english???
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			//ArrayList<TimePickerTime> result = new ArrayList<TimePickerTime>();
 			Map<String, TimePickerTime> result = new HashMap<String, TimePickerTime>();
 			for (String speech : matches) {
 				Matcher m = TIME_PATTERN.matcher(speech);
 				if (m.matches()) {
 					int hour = Integer.valueOf(m.group(1)) % 24;
 					int minute;
 					String min_part = m.group(2);
 					if (min_part == null) {
 						minute = 0;
 					}
 					else if ("半".equals(min_part)) {
 						minute = 30;
 					}
 					else {
 						minute = Integer.valueOf(m.group(3)) % 60;
 					}
 					String key = hour + ":" + minute;
 					if(! result.containsKey(key)){
 						result.put(key, new TimePickerTime(hour, minute, speech));
 					}
 				}
 				else {
 					Matcher m2 = AFTER_TIME_PATTERN.matcher(speech);
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
 							}
 							else {
 								long int_data = Integer.valueOf(m2.group(4));
 								after_millis += 60 * 1000 * int_data;
 							}
 						}
 						final Calendar cal = new GregorianCalendar();
 						cal.setTimeInMillis(System.currentTimeMillis() + after_millis);
 						int hour = cal.get(Calendar.HOUR_OF_DAY);
 						int min = cal.get(Calendar.MINUTE);
 						String key = hour + ":" + min;
 						if(!result.containsKey(key)){
 							result.put(key, new TimePickerTime(hour, min, speech));
 						}
 					}
 				}
 			}
 			if (result.isEmpty()) {
 				showMessage(this, getString(R.string.voice_fail));
 			}
 			else if (result.size() == 1) {
 				setTimePickerTime(result.values().iterator().next());
 			}
 			else {
 				String [] speechArray = new String[result.size()];
 				Iterator<TimePickerTime> iter = result.values().iterator();
 				for(int i = 0; i < result.size(); i++){
 					TimePickerTime time = iter.next();
 					speechArray[i] = time.mSpeach + String.format(" (%02d:%02d)", time.mHour, time.mMin);
 				}
 				//select from list dialog
 				new AlertDialog.Builder(this)
 				.setTitle(R.string.select_time_from_list)
 				.setItems(speechArray, new ClickListener(result.values().toArray(new TimePickerTime[0])))
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
 	public static class Player
 		extends BroadcastReceiver
 	{
 		private static Playlist currentPlaylist = sleepPlaylist;
 		private static MediaPlayer mPlayer = null;
 
 		public static boolean isPlaying() {
 			return mPlayer != null && mPlayer.isPlaying();
 		}
 
 		public static String getCurrentPlaylistName() {
 			if(null == currentPlaylist){
 				return "None";
 			}
 			return currentPlaylist.getName();
 		}
 		
 		public static void switchPlaylist() {
 			if(isPlaying()) {
 				return;
 			}
 			if(currentPlaylist == sleepPlaylist) {
 				currentPlaylist = wakeupPlaylist;
 			}
 			else {
 				currentPlaylist = sleepPlaylist;
 			}
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
 				if (prefPlaylistPath == null) {
 					prefPlaylistPath = intent.getStringExtra(PLAYLIST_PATH_KEY);
 				}
 				AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 				//following two methods require MODIFY_AUDIO_SETTINGS permissions...
 				//TODO: add preference to permit volume up when external speaker is connected
 				int wakeupVolume = 5;
 				if ((! mgr.isWiredHeadsetOn()) && (! mgr.isBluetoothA2dpOn())) {
 					wakeupVolume = intent.getIntExtra(VOLUME_KEY, 5);
 					Log.i(TAG, "playWakeupMusic: set volume: " + pref_wakeup_volume);
 				}
 				mgr.setStreamVolume(AudioManager.STREAM_MUSIC, wakeupVolume, AudioManager.FLAG_SHOW_UI);
 				playWakeupMusic(context, false);
 				
 				Intent i = new Intent(context, MalarmActivity.class);
 				i.setAction(WAKEUPAPP_ACTION);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startActivity(i);
 				//but this activity is not executed...(sleep before delivered?)
 			}
 			else if (intent.getAction().equals(SLEEP_ACTION)) {
 				if (intent.getExtras().getBoolean(NATIVE_PLAYER_KEY)) {
 					Player.stopMusicNativePlayer(context);
 				}
 				else {
 					Player.pauseMusic();
 				}
 				if(mState != null) {
 					//TODO: BUG: update sleep label
 					mState.mSleepMin = 0;
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
 			File f = new File(prefPlaylistPath + STOP_MUSIC_FILENAME);
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
 			}
 			else {
 				if(wakeupPlaylist == null) {
 					loadPlaylist();
 					if (wakeupPlaylist == null) {
 						Log.i(TAG, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				wakeupPlaylist.reset();
 				playMusic(wakeupPlaylist);
 			}
 		}
 
 		public static void playSleepMusic(Context context) {
 			Log.i(TAG, "start sleep music and stop");
 			File f = new File(prefPlaylistPath + SLEEP_PLAYLIST_FILENAME);
 			AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 			mgr.setStreamVolume(AudioManager.STREAM_MUSIC, pref_sleep_volume, AudioManager.FLAG_SHOW_UI);
 			if (pref_use_native_player && f.isFile()) {
 				Log.i(TAG, "playSleepMusic: NativePlayer");
 				playMusicNativePlayer(context, f);
 			}
 			else {
 				Log.i(TAG, "playSleepMusic: MediaPlayer");
 				if(sleepPlaylist == null) {
 					loadPlaylist();
 					if (sleepPlaylist == null) {
 						Log.i(TAG, "playSleepMusic: SLEEP_PLAYLIST is null");
 						return;
 					}
 				}
 				sleepPlaylist.reset();
 				playMusic(sleepPlaylist);
 			}
 		}
 
 		public static void playNext() {
 			Log.i(TAG, "playNext is called: ");
 			if (Player.isPlaying()) {
 				stopMusic();
 			}
 			playMusic(currentPlaylist);
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
 			currentPlaylist = playlist;
 			playMusic();
 		}
 
 		public static void playMusic() {
 			Log.i(TAG, "playMusic");
 			if (currentPlaylist == null || currentPlaylist.isEmpty()) {
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
 				path = currentPlaylist.next();
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
