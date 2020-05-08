 package com.menny.android.boxeethumbremote;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.KeyCharacterMap.KeyData;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class BoxeeActivity extends Activity implements
 		OnSharedPreferenceChangeListener, DiscovererThread.Receiver,
 		Remote.ErrorHandler, OnClickListener {
 
 	public final static String TAG = BoxeeActivity.class.toString();
 
 	// Messages
 	public static final int MESSAGE_UPDATE_ELAPSED = 100;
 
 	// Menu items
 	private static final int MENU_SETTINGS = Menu.FIRST;
 	// ViewFlipper
 	private static final int PAGE_NOTPLAYING = 0;
 	private static final int PAGE_NOWPLAYING = 1;
 	private ViewFlipper mFlipper;
 
 	// Other Views
 	ImageView mImageThumbnail;
 	Button mButtonPlayPause;
 	TextView mTextTitle;
 	TextView mTextElapsed;
 	TextView mDuration;
 	ProgressBar mElapsedBar;
 
 	private Settings mSettings;
 	private Remote mRemote;
 	private NowPlaying mNowPlaying = new NowPlaying();
 
 	private Point mTouchPoint = new Point();
 	private boolean mDragged = false;
 	private boolean mIsNowPlaying = false;
 	private ProgressDialog mPleaseWaitDialog;
 
 	Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case CurrentlyPlayingThread.MESSAGE_NOW_PLAYING_UPDATED:
 				refreshNowPlaying();
 				getNowPlayingAfter(5000);
 				break;
 			case CurrentlyPlayingThread.MESSAGE_THUMBNAIL_UPDATED:
 				refreshThumbnail();
 				break;
 			case MESSAGE_UPDATE_ELAPSED:
 				refreshElapsed();
 				break;
 			}
 		}
 	};
 
 	Thread mElapsedThread = new Thread(new Runnable() {
 		public void run() {
 			while (true) {
 				mHandler.sendMessage(mHandler
 						.obtainMessage(MESSAGE_UPDATE_ELAPSED));
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {
 					// ignore
 					e.printStackTrace();
 				}
 			}
 		}
 	});
 
 	Runnable mRunnableGetNowPlaying = new Runnable() {
 		public void run() {
 			new CurrentlyPlayingThread(mHandler, mRemote, mNowPlaying).start();
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mRemote = new Remote(this, this);
 
 		setContentView(R.layout.main);
 
 		// Setup flipper
 		mFlipper = (ViewFlipper) findViewById(R.id.now_playing_flipper);
 		mFlipper.setInAnimation(this, android.R.anim.slide_in_left);
 		mFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
 
 		// Find other views
 		mImageThumbnail = (ImageView) findViewById(R.id.thumbnail);
 		mButtonPlayPause = (Button) findViewById(R.id.buttonPlayPause);
 		mTextTitle = (TextView) findViewById(R.id.textNowPlayingTitle);
 		mTextElapsed = (TextView) findViewById(R.id.textElapsed);
 		mDuration = (TextView) findViewById(R.id.textDuration);
 		mElapsedBar = (ProgressBar) findViewById(R.id.progressTimeBar);
 
 		mSettings = new Settings(this);
 
 		loadPreferences();
 
 		setButtonAction(R.id.back, KeyEvent.KEYCODE_BACK);
 		setButtonAction(R.id.buttonPlayPause, 0);
 		setButtonAction(R.id.buttonStop, 0);
 		setButtonAction(R.id.buttonSmallSkipBack, 0);
 		setButtonAction(R.id.buttonSmallSkipFwd, 0);
 	}
 
 	@Override
 	protected void onPause() {
 		mSettings.unlisten(this);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSettings.listen(this);
 		getNowPlayingAfter(100);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.settings).setIcon(
 				android.R.drawable.ic_menu_preferences).setIntent(
 				new Intent(this, SettingsActivity.class));
 
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		int duration;
 
 		switch (v.getId()) {
 		
 		case R.id.buttonPlayPause:
 			mRemote.pause();
 			getNowPlayingAfter(100);
 			break;
 
 		case R.id.buttonStop:
 			mRemote.stop();
 			getNowPlayingAfter(100);
 			break;
 
 		case R.id.buttonSmallSkipBack:
 			// Seek backwards 10 seconds
 			duration = mNowPlaying.getDurationSeconds();
 			if (duration == 0)
 				break;
 			mRemote.seek(-10 * 100 / duration);
 			getNowPlayingAfter(100);
 			break;
 
 		case R.id.buttonSmallSkipFwd:
 			// Seek forwards 30 seconds
 			duration = mNowPlaying.getDurationSeconds();
 			if (duration == 0)
 				break;
 			mRemote.seek(30.0 * 100 / duration);
 			getNowPlayingAfter(100);
 			break;
 
 		case R.id.back:
 			mRemote.back();
 			break;
 		}
 
 	}
 
 	private void flipTo(int page) {
 		if (mFlipper.getDisplayedChild() != page)
 			mFlipper.setDisplayedChild(page);
 	}
 
 	private void refreshNowPlaying() {
 		mIsNowPlaying = mNowPlaying.isNowPlaying();
 
		flipTo(mNowPlaying.isNowPlaying() ? PAGE_NOWPLAYING : PAGE_NOTPLAYING);
 
 		if (!mIsNowPlaying) {
 			if (mElapsedThread.isAlive())
 				mElapsedThread.stop();
 			return;
 		}
 
 		if (!mElapsedThread.isAlive())
 			mElapsedThread.start();
 
 		mButtonPlayPause.setBackgroundDrawable(getResources().getDrawable(
 				mNowPlaying.isPaused() ? R.drawable.icon_osd_play
 						: R.drawable.icon_osd_pause));
 
 		String title = mNowPlaying.getTitle();
 		mTextTitle.setText(title);
 
 		mDuration.setText(mNowPlaying.getDuration());
 	}
 
 	private void refreshElapsed() {
 		String elapsed = mNowPlaying.getElapsed();
 		mTextElapsed.setText(elapsed);
 
 		mElapsedBar.setProgress(mNowPlaying.getPercentage());
 	}
 
 	private void refreshThumbnail() {
		mIsNowPlaying = mNowPlaying.isNowPlaying();
		if (mIsNowPlaying)
			mImageThumbnail.setImageBitmap(mNowPlaying.getThumbnail());
		else
			mImageThumbnail.setImageResource(R.drawable.boxee132);
 	}
 
 	/**
 	 * Schedule an attempt to get the currently-playing item.
 	 * 
 	 * @param delay_ms
 	 *            Delay before attempt in milliseconds
 	 */
 	private void getNowPlayingAfter(int delay_ms) {
 		mHandler.removeCallbacks(mRunnableGetNowPlaying);
 		mHandler.postDelayed(mRunnableGetNowPlaying, delay_ms);
 	}
 
 	/**
 	 * Handler an android keypress and send it to boxee if appropriate.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		int code = event.getKeyCode();
 
 		KeyData keyData = new KeyData();
 		event.getKeyData(keyData);
 		Log.d(TAG, "Unicode is " + event.getUnicodeChar());
 
 		String punctuation = "!@#$%^&*()[]{}/?|'\",.<>";
 		if (Character.isLetterOrDigit(keyData.displayLabel)
 				|| punctuation.indexOf(keyData.displayLabel) != -1) {
 			mRemote.keypress(event.getUnicodeChar());
 			return true;
 		}
 
 		switch (code) {
 
 		case KeyEvent.KEYCODE_DEL:
 			mRemote.sendBackspace();
 			return true;
 
 		case KeyEvent.KEYCODE_BACK:
 			mRemote.back();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 			mRemote.select();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_DOWN:
 			mRemote.down();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_UP:
 			mRemote.up();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 			mRemote.left();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 			mRemote.right();
 			return true;
 
 		case KeyEvent.KEYCODE_VOLUME_UP:
 			mRemote.changeVolume(mSettings.getVolumeStep());
 			return true;
 
 		case KeyEvent.KEYCODE_VOLUME_DOWN:
 			mRemote.changeVolume((-1)*mSettings.getVolumeStep());
 			return true;
 
 		case KeyEvent.KEYCODE_SPACE:
 		case KeyEvent.KEYCODE_ENTER:
 			// Some special keycodes we can translate from ASCII
 			mRemote.keypress(event.getUnicodeChar());
 			return true;
 
 		default:
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		int x = (int) event.getX();
 		int y = (int) event.getY();
 		int sensitivity = 30;
 		switch (event.getAction()) {
 
 		case MotionEvent.ACTION_UP:
 			if (!mDragged) {
 				mRemote.select();
 				return true;
 			}
 			break;
 
 		case MotionEvent.ACTION_DOWN:
 			mTouchPoint.x = x;
 			mTouchPoint.y = y;
 			mDragged = false;
 			return true;
 
 		case MotionEvent.ACTION_MOVE:
 			if (x - mTouchPoint.x > sensitivity) {
 				mRemote.right();
 				mTouchPoint.x += sensitivity;
 				mTouchPoint.y = y;
 				mDragged = true;
 				return true;
 			} else if (mTouchPoint.x - x > sensitivity) {
 				mRemote.left();
 				mTouchPoint.x -= sensitivity;
 				mTouchPoint.y = y;
 				mDragged = true;
 				return true;
 			} else if (y - mTouchPoint.y > sensitivity) {
 				mRemote.down();
 				mTouchPoint.y += sensitivity;
 				mTouchPoint.x = x;
 				mDragged = true;
 				return true;
 			} else if (mTouchPoint.y - y > sensitivity) {
 				mRemote.up();
 				mTouchPoint.y -= sensitivity;
 				mTouchPoint.x = x;
 				mDragged = true;
 				return true;
 			}
 			break;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Set up a navigation button in the UI. Sets the focus to false so that we
 	 * can capture KEYCODE_DPAD_CENTER.
 	 * 
 	 * @param id
 	 *            id of the button in the resource file
 	 * 
 	 * @param keycode
 	 *            keyCode we should send to Boxee when this button is pressed
 	 */
 	private void setButtonAction(int id, final int keyCode) {
 		Button button = (Button) findViewById(id);
 		button.setFocusable(false);
 		button.setTag(new Integer(keyCode));
 		button.setOnClickListener(this);
 	}
 
 	/**
 	 * Wrapper-function taking a KeyCode. A complete KeyStroke is DOWN and UP
 	 * Action on a key!
 	 */
 	/*
 	private void simulateKeystroke(int keyCode) {
 		onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
 		onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
 	}
 	 */
 	/**
 	 * Display an error from R.strings, may be called from any thread
 	 * 
 	 * @param id
 	 *            an id from R.strings
 	 */
 	public void ShowError(int id) {
 		ShowError(getString(id));
 	}
 
 	/**
 	 * sometimes, when there is no network, or stuff, I get lots of Toast windows
 	 * which do not disapear for a long time. I say, if the same error happens too often
 	 * there is no need to show it.
 	 */
 	private String mLastErrorMessage = null;
 	private long mLastErrorMessageTime = 0;
 	private static final long MINIMUM_ms_TIME_BETWEEN_ERRORS = 1000;//
 	/**
 	 * Display a short error via a popup message.
 	 */
 	private void ShowErrorInternal(String s) {
 		//checking for repeating error
 		final long currentTime = System.currentTimeMillis();
 		if ((!s.equals(mLastErrorMessage)) || ((currentTime - mLastErrorMessageTime) > MINIMUM_ms_TIME_BETWEEN_ERRORS))
 			Toast.makeText(this, s, Toast.LENGTH_SHORT).show();//we can show the error.
 		mLastErrorMessage = s;
 		mLastErrorMessageTime = currentTime;
 	}
 	/**
 	 * Show an error, may be called from any thread
 	 */
 	public void ShowError(final String s) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				ShowErrorInternal(s);
 			}
 		});
 	}
 
 	/**
 	 * Set the state of the application based on prefs. This should be called
 	 * after every preference change or when starting up.
 	 * 
 	 * @param prefs
 	 */
 	private void loadPreferences() {
 
 		// Setup the proper pageflipper page:
 		flipTo(PAGE_NOTPLAYING);
 
 		// Parse the credentials, if needed.
 		String user = mSettings.getUser();
 		String password = mSettings.getPassword();
 		if (!TextUtils.isEmpty(password)) {
 			HttpRequestBlocking.setUserPassword(user, password);
 		}
 
 		// Only set the host if manual. Otherwise we'll auto-detect it with
 		// Discoverer -> addAnnouncedServers
 		if (mSettings.isManual()) {
 			mRemote.setServer(mSettings.constructServer());
 			getNowPlayingAfter(100);
 		}
 		else {
 			mPleaseWaitDialog = ProgressDialog.show(this, "", "Connecting to "
 					+ mSettings.getServerName() + "...", true);
 			DiscovererThread discoverer = new DiscovererThread(this, this);
 			discoverer.start();
 		}
 		
 		// Setup the HTTP timeout.
 		int timeout_ms = mSettings.getTimeout();
 		HttpRequestBlocking.setTimeout(timeout_ms);
 
 		// Read the "require wifi" setting.
 		boolean requireWifi = mSettings.requiresWifi();
 		mRemote.setRequireWifi(requireWifi);
 	}
 
 	/**
 	 * Callback when user alters preferences.
 	 */
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String pref) {
 		loadPreferences();
 	}
 
 	/**
 	 * Called when the discovery request we sent in onCreate finishes. If we
 	 * find a server matching mAutoName, we use that.
 	 * 
 	 * @param servers
 	 *            list of discovered servers
 	 */
 	public void addAnnouncedServers(ArrayList<BoxeeServer> servers) {
 
 		// This condition shouldn't ever be true.
 		if (mSettings.isManual()) {
 			Log.d(TAG, "Skipping announced servers. Set manually");
 			return;
 		}
 
 		mPleaseWaitDialog.dismiss();
 
 		String preferred = mSettings.getServerName();
 
 		for (int k = 0; k < servers.size(); ++k) {
 			BoxeeServer server = servers.get(k);
 			if (server.name().equals(preferred)) {
 				if (!server.valid()) {
 					ShowError(String.format("Found '%s' but looks broken",
 							server.name()));
 					continue;
 				} else {
 					// Yay, found it and it works
 					mRemote.setServer(server);
 					mRemote.displayMessage("Connected to Boxee Remote");
 					getNowPlayingAfter(100);
 					if (server.authRequired())
 						passwordCheck();
 					return;
 				}
 			}
 		}
 
 		ShowError(String.format("Could not find preferred server '%s'",
 				preferred));
 	}
 
 	private void passwordCheck() {
 		// TODO: open a dialog box here instead
 		String password = HttpRequestBlocking.password();
 		if (password == null || password.length() == 0)
 			ShowError("Server requires password. Set one in preferences.");
 	}
 
 }
