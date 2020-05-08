 /* The following code was written by Menny Even Danan
  * and is released under the APACHE 2.0 license
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.menny.android.boxeethumbremote;
 
 import java.util.ArrayList;
 
 import com.menny.android.boxeethumbremote.R;
 import com.menny.android.boxeethumbremote.ShakeListener.OnShakeListener;
 
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
 
 public class RemoteUiActivity extends Activity implements
 		OnSharedPreferenceChangeListener, DiscovererThread.Receiver,
 		BoxeeRemote.ErrorHandler, OnClickListener, OnShakeListener {
 
 	public final static String TAG = RemoteUiActivity.class.toString();
 
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
 	private BoxeeRemote mRemote;
 	private NowPlaying mNowPlaying = new NowPlaying();
 	private ServerStatePoller mStatePoller = null; 
 
 	//Not ready for prime time
 	//private ShakeListener mShakeDetector;
 	
 	private Point mTouchPoint = new Point();
 	private boolean mDragged = false;
 	private boolean mIsNowPlaying = false;
 	private ProgressDialog mPleaseWaitDialog;
 
 	Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case ServerStatePoller.MESSAGE_NOW_PLAYING_UPDATED:
 				refreshNowPlaying();
 				break;
 			case ServerStatePoller.MESSAGE_MEDIA_METADATA_UPDATED:
 				refreshMediaMetdata();
 				break;
 			}
 		}
 	};
 
 	private final Runnable mRequestStatusUpdateRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (mStatePoller != null)
 				mStatePoller.checkStateNow();
 		}
 	};
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mRemote = new BoxeeRemote(this, this);
 
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
 		
 		//mShakeDetector = new ShakeListener(getApplicationContext());
 		//mShakeDetector.setOnShakeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		//mShakeDetector.pause();
 		mSettings.unlisten(this);
 		mHandler.removeCallbacks(mRequestStatusUpdateRunnable);
 		
 		super.onPause();
 		if (mStatePoller != null)
 			mStatePoller.stop();
 		mStatePoller = null;
 		
 		if (mPleaseWaitDialog != null)
 			mPleaseWaitDialog.dismiss();
 		mPleaseWaitDialog = null;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSettings.listen(this);
 		
 		if (!mRemote.hasServers() && !mServerDiscoverer.isLookingForServers())
 			setServer();
 		
 		mStatePoller = new ServerStatePoller(mHandler, mRemote, mNowPlaying);
 		mStatePoller.poll();
 		
 		//mShakeDetector.resume();
 		
 		mImageThumbnail.setKeepScreenOn(mSettings.getKeepScreenOn());
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
 			mRemote.flipPlayPause();
 			requestUpdateASAP(100);
 			break;
 
 		case R.id.buttonStop:
 			mRemote.stop();
 			requestUpdateASAP(100);
 			break;
 
 		case R.id.buttonSmallSkipBack:
 			// Seek backwards 10 seconds
 			duration = mNowPlaying.getDurationSeconds();
 			if (duration == 0)
 				break;
 			mRemote.seek(-10 * 100 / duration);
 			requestUpdateASAP(100);
 			break;
 
 		case R.id.buttonSmallSkipFwd:
 			// Seek forwards 30 seconds
 			duration = mNowPlaying.getDurationSeconds();
 			if (duration == 0)
 				break;
 			mRemote.seek(30.0 * 100 / duration);
 			requestUpdateASAP(100);
 			break;
 
 		case R.id.back:
 			mRemote.back();
 			break;
 		}
 
 	}
 
 	private void requestUpdateASAP(int delay_ms) {
 		mHandler.postDelayed(mRequestStatusUpdateRunnable,delay_ms);
 	}
 
 	private void flipTo(int page) {
 		if (mFlipper.getDisplayedChild() != page)
 			mFlipper.setDisplayedChild(page);
 	}
 
 	private void refreshNowPlaying() {
 		mIsNowPlaying = mNowPlaying.isNowPlaying();
 
 		flipTo(mNowPlaying.isNowPlaying() ? PAGE_NOWPLAYING : PAGE_NOTPLAYING);
 
 		if (mIsNowPlaying) {
 			mButtonPlayPause.setBackgroundDrawable(getResources().getDrawable(
 					mNowPlaying.isPaused() ? R.drawable.icon_osd_play
 							: R.drawable.icon_osd_pause));
 	
 			String title = mNowPlaying.getTitle();
 			mTextTitle.setText(title);
 	
 			mDuration.setText(mNowPlaying.getDuration());
 			
 			String elapsed = mNowPlaying.getElapsed();
 			mTextElapsed.setText(elapsed);
 
 			mElapsedBar.setProgress(mNowPlaying.getPercentage());			
 		}
 		else
 		{
 			mTextTitle.setText("");
 		}
 	}
 	
 	private void refreshMediaMetdata() {
 		mIsNowPlaying = mNowPlaying.isNowPlaying();
 		if (mIsNowPlaying)
 		{
 			mImageThumbnail.setImageBitmap(mNowPlaying.getThumbnail());
 			String title = mNowPlaying.getTitle();
 			mTextTitle.setText(title);
			//this will fire up the marquee thingy
			mTextTitle.setSelected(true);
 		}
 		else
 		{
 			mImageThumbnail.setImageResource(R.drawable.remote_background);
 			mTextTitle.setText("");
 		}
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
 			if (mSettings.getHandleBack())
 			{
 				mRemote.back();
 				return true;
 			}
 			else
 			{
 				super.onKeyDown(keyCode, event);
 			}
 
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
 	public void ShowError(int id, boolean longDelay) {
 		ShowError(getString(id), longDelay);
 	}
 
 	/**
 	 * sometimes, when there is no network, or stuff, I get lots of Toast windows
 	 * which do not disapear for a long time. I say, if the same error happens too often
 	 * there is no need to show it.
 	 */
 	private String mLastErrorMessage = null;
 	private long mLastErrorMessageTime = 0;
 	private static final long MINIMUM_ms_TIME_BETWEEN_ERRORS = 1000;//
 
 	private DiscovererThread mServerDiscoverer;
 	/**
 	 * Display a short error via a popup message.
 	 */
 	private void ShowErrorInternal(String s, boolean longDelay) {
 		//checking for repeating error
 		final long currentTime = System.currentTimeMillis();
 		if ((!s.equals(mLastErrorMessage)) || ((currentTime - mLastErrorMessageTime) > MINIMUM_ms_TIME_BETWEEN_ERRORS))
 			Toast.makeText(this, s, longDelay? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();//we can show the error.
 		mLastErrorMessage = s;
 		mLastErrorMessageTime = currentTime;
 	}
 	/**
 	 * Show an error, may be called from any thread
 	 */
 	public void ShowError(final String s, final boolean longDelay) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				ShowErrorInternal(s, longDelay);
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
 
 		// Read the "require wifi" setting.
 		boolean requireWifi = mSettings.requiresWifi();
 		mRemote.setRequireWifi(requireWifi);
 		
 		// Setup the HTTP timeout.
 		int timeout_ms = mSettings.getTimeout();
 		HttpRequestBlocking.setTimeout(timeout_ms);
 
 		// Parse the credentials, if needed.
 		String user = mSettings.getUser();
 		String password = mSettings.getPassword();
 		if (!TextUtils.isEmpty(password)) {
 			HttpRequestBlocking.setUserPassword(user, password);
 		}
 
 		setServer();
 	}
 
 	private void setServer() {
 		// Only set the host if manual. Otherwise we'll auto-detect it with
 		// Discoverer -> addAnnouncedServers
 		if (mSettings.isManual()) {
 			mRemote.setServer(mSettings.constructServer());
 			requestUpdateASAP(100);
 		}
 		else {
 			mPleaseWaitDialog = ProgressDialog.show(this, "", "Looking for a server...", true);
 			mServerDiscoverer = new DiscovererThread(this, this);
 			mServerDiscoverer.start();
 		}
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
 		
 		if (mPleaseWaitDialog != null)
 			mPleaseWaitDialog.dismiss();
 		mPleaseWaitDialog = null;
 		
 		
 		// This condition shouldn't ever be true.
 		if (mSettings.isManual()) {
 			Log.d(TAG, "Skipping announced servers. Set manually");
 			return;
 		}
 
 		String preferred = mSettings.getServerName();
 
 		for (int k = 0; k < servers.size(); ++k) {
 			BoxeeServer server = servers.get(k);
 			if (server.name().equals(preferred) || TextUtils.isEmpty(preferred)) {
 				if (!server.valid()) {
 					ShowError(String.format("Found '%s' but looks broken", server.name()), false);
 					continue;
 				} else {
 					// Yay, found it and it works
 					mRemote.setServer(server);
 					final String serverName = server.name();
 					mRemote.displayMessage("Connected to server "+serverName);
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							setTitle(getString(R.string.app_name)+" - "+serverName);
 						}
 					});
 					
 					requestUpdateASAP(100);
 					if (server.authRequired())
 						passwordCheck();
 					return;
 				}
 			}
 		}
 
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				setTitle(getString(R.string.app_name));
 			}
 		});
 		ShowError("Could not find any servers. Try specifying it in the Settings (press MENU)", true);
 	}
 
 	private void passwordCheck() {
 		// TODO: open a dialog box here instead
 		String password = HttpRequestBlocking.password();
 		if (password == null || password.length() == 0)
 			ShowError("Server requires password. Set one in preferences.", true);
 	}
 	
 	@Override
 	public void onShake() {
 		Log.d(TAG, "Shake detect! Fullscreen? "+mNowPlaying.isOnNowPlayingScreen());
 		mRemote.flipPlayPause();
 		//if (mNowPlaying.isOnNowPlayingScreen())
 			
 	}
 
 }
