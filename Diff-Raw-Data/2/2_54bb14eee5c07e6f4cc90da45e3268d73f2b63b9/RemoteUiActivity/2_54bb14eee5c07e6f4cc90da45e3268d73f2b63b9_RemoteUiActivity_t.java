 /* The following code was written by Menny Even Danan
  * and is released under the APACHE 2.0 license
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.menny.android.thumbremote.ui;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.menny.android.thumbremote.R;
 import com.menny.android.thumbremote.RemoteApplication;
 import com.menny.android.thumbremote.ServerAddress;
 import com.menny.android.thumbremote.ServerConnector;
 import com.menny.android.thumbremote.ServerState;
 import com.menny.android.thumbremote.ServerStatePoller;
 import com.menny.android.thumbremote.ShakeListener.OnShakeListener;
 import com.menny.android.thumbremote.UiView;
 import com.menny.android.thumbremote.boxee.BoxeeConnector;
 import com.menny.android.thumbremote.boxee.BoxeeDiscovererThread;
 import com.menny.android.thumbremote.network.HttpRequest;
 
 public class RemoteUiActivity extends Activity implements
 		OnSharedPreferenceChangeListener, BoxeeDiscovererThread.Receiver, OnClickListener, OnShakeListener, UiView {
 
 	public final static String TAG = RemoteUiActivity.class.toString();
 
 	//
 	private final static HashSet<Character> msPunctuation = new HashSet<Character>();
 	static
 	{
 		String punctuation = "!@#$%^&*()[]{}/?|'\",.<>\n ";
 		for(char c : punctuation.toCharArray())
 			msPunctuation.add(c);
 	}
 	
 	private static RemoteUiActivity msActivity = null;
 
 	public static void onExternalImportantEvent(String event) {
 		final RemoteUiActivity realActivity = msActivity;
 		if (realActivity != null)
 		{
 			Log.i(TAG, "Got an important external event '"+event+"'!");
 			realActivity.pauseIfPlaying();
 		}
 	}
 	
 	public static void onNetworkAvailable()
 	{
 		final RemoteUiActivity realActivity = msActivity;
 		if (realActivity != null && !realActivity.mThisAcitivityPaused)
 		{
 			Log.i(TAG, "Got network! Trying to reconnect...");
 			realActivity.mRemote = new BoxeeConnector();
 			realActivity.mRemote.setUiView(realActivity);
 			realActivity.setServer();
 		}
 	}
 	
 	private static final int MESSAGE_MEDIA_PLAYING_CHANGED = 97565;
 	private static final int MESSAGE_MEDIA_PLAYING_PROGRESS_CHANGED = MESSAGE_MEDIA_PLAYING_CHANGED + 1;
 	private static final int MESSAGE_MEDIA_METADATA_CHANGED = MESSAGE_MEDIA_PLAYING_PROGRESS_CHANGED + 1;
 	
 	// Menu items
 	private static final int MENU_SETTINGS = Menu.FIRST;
 	private static final int MENU_HELP = MENU_SETTINGS+1;
 	private static final int MENU_RESCAN = MENU_HELP+1;
 	private static final int MENU_EXIT = MENU_RESCAN+1;
 	
 	// ViewFlipper
 	private static final int PAGE_NOTPLAYING = 0;
 	private static final int PAGE_NOWPLAYING = 1;
 	private ViewFlipper mFlipper;
 
 	// Other Views
 	ImageView mImageThumbnail;
 	Button mButtonPlayPause;
 	TextView mTextTitle;
 	TextView mUserMessage;
 	String mUserMessageString = "";
 	TextView mTextElapsed;
 	TextView mDuration;
 	ProgressBar mElapsedBar;
 	TextView mMediaDetails = null;
 	
 	private static final int NOTIFICATION_PLAYING_ID = 1;
 
 	private static final int DIALOG_NO_PASSWORD = 1;
 	private static final int DIALOG_NO_SERVER = 2;
 	
 	private NotificationManager mNotificationManager;
 
 	boolean mThisAcitivityPaused = true;
 	
 	private ServerConnector mRemote;
 	private BoxeeDiscovererThread mServerDiscoverer;
 	private ServerAddress mServerAddress = null;
 	private ServerStatePoller mStatePoller = null; 
 
 	//Not ready for prime time
 	//private ShakeListener mShakeDetector;
 	
 	private Point mTouchPoint = new Point();
 	private boolean mDragged = false;
 	private boolean mIsMediaActive = false;
 	private ProgressDialog mPleaseWaitDialog;
 	private int mDialogToDismiss = -1;
 	
 	private Handler mHandler;
 
 	private final Runnable mRequestStatusUpdateRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (mStatePoller != null)
 				mStatePoller.checkStateNow();
 		}
 	};
 
 	private Animation mInAnimation;
 
 	private Animation mOutAnimation;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case MESSAGE_MEDIA_PLAYING_CHANGED:
 					refreshPlayingStateChanged(false);
 					break;
 				case MESSAGE_MEDIA_PLAYING_PROGRESS_CHANGED:
 					refreshPlayingProgressChanged();
 					break;
 				case MESSAGE_MEDIA_METADATA_CHANGED:
 					refreshMetadataChanged();
 					break;
 				}
 			}
 		};
 		
 		mInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left);
 		mOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_out_right);
 		mOutAnimation.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {mUserMessage.setText(""); mUserMessage.setVisibility(View.INVISIBLE);}
 		});
 		
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		mRemote = new BoxeeConnector();
 		mRemote.setUiView(this);
 
 		setContentView(R.layout.main);
 
 		// Setup flipper
 		mFlipper = (ViewFlipper) findViewById(R.id.now_playing_flipper);
 		mFlipper.setInAnimation(this, android.R.anim.slide_in_left);
 		mFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
 
 		// Find other views
 		mImageThumbnail = (ImageView) findViewById(R.id.thumbnail);
 		mButtonPlayPause = (Button) findViewById(R.id.buttonPlayPause);
 		mTextTitle = (TextView) findViewById(R.id.textNowPlayingTitle);
 		mUserMessage = (TextView)findViewById(R.id.textMessages);
 		mTextElapsed = (TextView) findViewById(R.id.textElapsed);
 		mDuration = (TextView) findViewById(R.id.textDuration);
 		mElapsedBar = (ProgressBar) findViewById(R.id.progressTimeBar);
 		mMediaDetails = (TextView)findViewById(R.id.textMediaDetails);
 		
 		loadPreferences();
 
 		setButtonAction(R.id.back, KeyEvent.KEYCODE_BACK);
 		setButtonAction(R.id.buttonPlayPause, 0);
 		setButtonAction(R.id.buttonStop, 0);
 		setButtonAction(R.id.buttonSmallSkipBack, 0);
 		setButtonAction(R.id.buttonSmallSkipFwd, 0);
 		
 		//mShakeDetector = new ShakeListener(getApplicationContext());
 		//mShakeDetector.setOnShakeListener(this);
 		msActivity = this;
 		
 		mStatePoller = new ServerStatePoller(mRemote, getApplicationContext());
 		mStatePoller.poll();
 		
 		startHelpOnFirstRun();
 	}
 
 	private void startHelpOnFirstRun() {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		final boolean ranBefore = preferences.getBoolean("has_ran_before", false);
 		if (!ranBefore)
 		{
 			Editor e = preferences.edit();
 			e.putBoolean("has_ran_before", true);
 			e.commit();
 			
 			startHelpActivity();
 		}
 	}
 
 	private void startHelpActivity() {
 		Intent i = new Intent(getApplicationContext(), HelpUiActivity.class);
 		startActivity(i);
 	}
 	
 	private void startSetupActivity() {
 		Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 		startActivity(i);
 	}
 	
 	@Override
 	protected void onPause() {
 		mSwipeStepSize = -1;
 		mThisAcitivityPaused = true;
 		//mShakeDetector.pause();
 		RemoteApplication.getConfig().unlisten(this);
 		mHandler.removeCallbacks(mRequestStatusUpdateRunnable);
 		
 		super.onPause();
 		if (mStatePoller != null)
 			mStatePoller.moveToBackground();
 		
 		if (mPleaseWaitDialog != null)
 			mPleaseWaitDialog.dismiss();
 		mPleaseWaitDialog = null;
 		
 		stopPollerIfPossible();
 		
 		if (mServerDiscoverer != null) mServerDiscoverer.setReceiver(null);
 		mServerDiscoverer = null;
 	}
 	
 	@Override
 	protected void onDestroy() {
 		msActivity = null;
 		mNotificationManager.cancel(NOTIFICATION_PLAYING_ID);
 		mRemote.setServer(null);
 		stopPollerIfPossible();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mThisAcitivityPaused = false;
 		
 		RemoteApplication.getConfig().listen(this);
 		
 		//mShakeDetector.resume();
 		
 		mImageThumbnail.setKeepScreenOn(RemoteApplication.getConfig().getKeepScreenOn());
 		
 		if (mStatePoller == null)
 		{
 			mStatePoller = new ServerStatePoller(mRemote, getApplicationContext());
 			mStatePoller.poll();
 		}
 		
 		mStatePoller.comeBackToForeground();
 		
 		if (mServerDiscoverer == null || !mServerDiscoverer.isDiscoverying())
 		{
 			if (mServerAddress == null || !mServerAddress.valid())
 			{
 				setServer();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.settings).setIcon(
 				android.R.drawable.ic_menu_preferences);
 		
 		menu.add(Menu.NONE, MENU_HELP, 0, R.string.help).setIcon(
 				android.R.drawable.ic_menu_help);
 		
 		menu.add(Menu.NONE, MENU_RESCAN, 0, R.string.rescan_servers).setIcon(
 				android.R.drawable.ic_menu_search);
 		
 		menu.add(Menu.NONE, MENU_EXIT, 0, R.string.exit_app).setIcon(
 				android.R.drawable.ic_menu_close_clear_cancel);
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId())
 		{
 		case MENU_EXIT:
 			finish();
 			return true;
 		case MENU_HELP:
 			startHelpActivity();
 			return true;
 		case MENU_SETTINGS:
 			startSetupActivity();
 			return true;
 		case MENU_RESCAN:
 			setServer();
 			return true;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
 	}
 	
 	private void pauseIfPlaying()
 	{
 		if (mRemote.isMediaPlaying())
 		{
 			remoteFlipPlayPause();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		final int id = v.getId();
 		switch (id) {
 		
 		case R.id.buttonPlayPause:
 			remoteFlipPlayPause();
 			break;
 
 		case R.id.buttonStop:
 			remoteStop();
 			break;
 
 		case R.id.buttonSmallSkipBack:
 		case R.id.buttonSmallSkipFwd:
 			final int duration = hmsToSeconds(mRemote.getMediaTotalTime());
 			if (duration > 0)
 			{
 				final double howFar = (id == R.id.buttonSmallSkipFwd)? 30f : -30f;
 				final double newSeekPosition = howFar * 100f / duration;
 				remoteSeek(newSeekPosition);
 			}
 			break;
 
 		case R.id.back:
 			remoteBack();
 			break;
 		}
 
 	}
 
 	void requestUpdateASAP(int delay_ms) {
 		mHandler.postDelayed(mRequestStatusUpdateRunnable,delay_ms);
 	}
 
 	private void flipTo(int page) {
 		if (mFlipper.getDisplayedChild() != page)
 			mFlipper.setDisplayedChild(page);
 	}
 
 	@Override
 	public void onPlayingStateChanged(ServerState serverState) {
 		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_MEDIA_PLAYING_CHANGED));
 	}
 	
 	@Override
 	public void onPlayingProgressChanged(ServerState serverState) {
 		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_MEDIA_PLAYING_PROGRESS_CHANGED));		
 	}
 	
 	@Override
 	public void onMetadataChanged(ServerState serverState) {
 		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_MEDIA_METADATA_CHANGED));
 	}
 	
 	private void refreshPlayingStateChanged(boolean forceChanged) {
 		final boolean isPlaying = mRemote.isMediaPlaying();
 		final boolean newIsMediaActive = mRemote.isMediaActive();
 		final boolean mediaActiveChanged = forceChanged || (newIsMediaActive != mIsMediaActive);
 		mIsMediaActive = newIsMediaActive;
 
 		if (!mediaActiveChanged) return;
 		
 		if (mIsMediaActive) {
 			mButtonPlayPause.setBackgroundDrawable(getResources().getDrawable(
 					isPlaying ? R.drawable.icon_osd_pause : R.drawable.icon_osd_play));
 	
 			final String title = getMediaTitle();
 			mTextTitle.setText(title);
 			if (mMediaDetails != null) mMediaDetails.setText(mRemote.getMediaPlot());
 			refreshPlayingProgressChanged();
 			
 			flipTo(PAGE_NOWPLAYING);
 			Notification notification = new Notification(R.drawable.notification_playing, getString(R.string.server_is_playing, title), System.currentTimeMillis());
 
 			Intent notificationIntent = new Intent(this, RemoteUiActivity.class);
 			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
			notification.setLatestEventInfo(this,
 					getText(R.string.app_name), getString(R.string.server_is_playing, title),
 					contentIntent);
 			notification.flags |= Notification.FLAG_ONGOING_EVENT;
 			notification.flags |= Notification.FLAG_NO_CLEAR;
 			//notification.defaults = 0;// no sound, vibrate, etc.
 			// notifying
 			mNotificationManager.notify(NOTIFICATION_PLAYING_ID, notification);
 		}
 		else
 		{
 			flipTo(PAGE_NOTPLAYING);
 			
 			mTextTitle.setText("");
 			if (mMediaDetails != null) mMediaDetails.setText("");
 			mImageThumbnail.setImageResource(R.drawable.remote_background);
 			mNotificationManager.cancel(NOTIFICATION_PLAYING_ID);
 			//no need to keep this one alive. Right?
 			stopPollerIfPossible();
 		}
 	}
 	
 	private void stopPollerIfPossible() {
 		if (mThisAcitivityPaused && !mRemote.isMediaPlaying())
 		{
 			if (mStatePoller != null)
 				mStatePoller.stop();
 			mStatePoller = null;
 		}
 	}
 
 	private void refreshPlayingProgressChanged()
 	{
 		mDuration.setText(mRemote.getMediaTotalTime());
 		
 		mTextElapsed.setText(mRemote.getMediaCurrentTime());
 
 		mElapsedBar.setProgress(mRemote.getMediaProgressPercent());
 	}
 	
 	private void refreshMetadataChanged() {
 		mIsMediaActive = mRemote.isMediaActive();
 		if (mIsMediaActive)
 		{
 			mImageThumbnail.setImageBitmap(mRemote.getMediaPoster());
 			mTextTitle.setText(getMediaTitle());
 			if (mMediaDetails != null) mMediaDetails.setText(mRemote.getMediaPlot());
 		}
 		else
 		{
 			mImageThumbnail.setImageResource(R.drawable.remote_background);
 			mTextTitle.setText("");
 			if (mMediaDetails != null) mMediaDetails.setText("");
 		}
 	}
 
 	private String getMediaTitle() {
 		String showTitle = mRemote.getShowTitle();
 		String title = mRemote.getMediaTitle();
 		String filename = mRemote.getMediaFilename();
 		String season = mRemote.getShowSeason();
 		String episode = mRemote.getShowEpisode();
 		String mediaTitle = "";
 		if (!TextUtils.isEmpty(showTitle))
 		{
 			mediaTitle = showTitle;
 			if (!TextUtils.isEmpty(season)) mediaTitle += " S"+season;
 			if (!TextUtils.isEmpty(episode)) mediaTitle += "E"+episode;
 		}
 		
 		if (!TextUtils.isEmpty(title))
 		{
 			if (!TextUtils.isEmpty(mediaTitle)) mediaTitle += ": ";
 			mediaTitle += title;
 		}
 		
 		if (!TextUtils.isEmpty(mediaTitle))
 			return mediaTitle;
 
 		if (!TextUtils.isEmpty(filename))
 			return filename;
 
 		return "";
 	}
 
 	/**
 	 * Handler an android keypress and send it to boxee if appropriate.
 	 */
 	@Override
 	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
 		final char unicodeChar = (char)event.getUnicodeChar();
 		
 		Log.d(TAG, "Unicode is " + ((int)unicodeChar));
 
 		
 		if (Character.isLetterOrDigit(unicodeChar) || msPunctuation.contains(unicodeChar)) {
 			remoteKeypress(unicodeChar);
 			
 			return true;
 		}
 
 		switch (keyCode) {
 
 		case KeyEvent.KEYCODE_BACK:
 			if (RemoteApplication.getConfig().getHandleBack())
 			{
 				remoteBack();
 				return true;
 			}
 			else
 			{
 				super.onKeyDown(keyCode, event);
 			}
 
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 			remoteSelect();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_DOWN:
 			remoteDown();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_UP:
 			remoteUp();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 			remoteLeft();
 			return true;
 
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 			remoteRight();
 			return true;
 
 		case KeyEvent.KEYCODE_VOLUME_UP:
 		case KeyEvent.KEYCODE_VOLUME_DOWN:
 			final int volumeFactor = (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)? -1 : 1;
 				new DoServerRemoteAction(this, false) {
 					private int mNewVolume = 0;
 				@Override
 				protected void callRemoteFunction() throws Exception {
 					int volume = mRemote.getVolume();
 					mNewVolume = Math.max(0, Math.min(100, volume + (volumeFactor * RemoteApplication.getConfig().getVolumeStep())));
 					mRemote.setVolume(mNewVolume);
 				}
 				@Override
 					protected void onPostExecute(Exception result) {
 						showMessage(getString(R.string.new_volume_toast, mNewVolume), 500);
 						super.onPostExecute(result);
 					}
 			}.execute();
 			return true;
 			
 		default:
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	private Runnable mClearUserMessageRunnable = new Runnable() {
 		@Override
 		public void run() {
 			mUserMessage.startAnimation(mOutAnimation);
 		}
 	};
 
 	private Runnable mSetUserMessageRunnable = new Runnable() {
 		@Override
 		public void run() {
 			mUserMessage.setText(mUserMessageString);
 			mUserMessage.setVisibility(View.VISIBLE);
 			mUserMessage.startAnimation(mInAnimation);
 		}
 	};
 	
 	void showMessage(final String userMessage, final int messageTime) {
 		mUserMessageString = userMessage;
 		mHandler.removeCallbacks(mClearUserMessageRunnable);
 		mHandler.removeCallbacks(mSetUserMessageRunnable);
 		mHandler.postAtFrontOfQueue(mSetUserMessageRunnable);
 		mHandler.postDelayed(mClearUserMessageRunnable, messageTime);
 	}
 
 	private int mSwipeStepSize = -1;
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		int x = (int) event.getX();
 		int y = (int) event.getY();
 		if (mSwipeStepSize < 1)
 			mSwipeStepSize = RemoteApplication.getConfig().getSwipeStepSize(getApplicationContext());
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_UP:
 			if (!mDragged) {
 				remoteSelect();
 				return true;
 			}
 			break;
 
 		case MotionEvent.ACTION_DOWN:
 			mTouchPoint.x = x;
 			mTouchPoint.y = y;
 			mDragged = false;
 			return true;
 
 		case MotionEvent.ACTION_MOVE:
 			if (x - mTouchPoint.x > mSwipeStepSize) {
 				remoteRight();
 				mTouchPoint.x += mSwipeStepSize;
 				mTouchPoint.y = y;
 				mDragged = true;
 				return true;
 			} else if (mTouchPoint.x - x > mSwipeStepSize) {
 				remoteLeft();
 				mTouchPoint.x -= mSwipeStepSize;
 				mTouchPoint.y = y;
 				mDragged = true;
 				return true;
 			} else if (y - mTouchPoint.y > mSwipeStepSize) {
 				remoteDown();
 				mTouchPoint.y += mSwipeStepSize;
 				mTouchPoint.x = x;
 				mDragged = true;
 				return true;
 			} else if (mTouchPoint.y - y > mSwipeStepSize) {
 				remoteUp();
 				mTouchPoint.y -= mSwipeStepSize;
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
 	 * Set the state of the application based on prefs. This should be called
 	 * after every preference change or when starting up.
 	 * 
 	 * @param prefs
 	 */
 	private void loadPreferences() {
 		mSwipeStepSize = RemoteApplication.getConfig().getSwipeStepSize(getApplicationContext());
 		// Setup the proper pageflipper page:
 		flipTo(PAGE_NOTPLAYING);
 		
 		// Setup the HTTP timeout.
 		int timeout_ms = RemoteApplication.getConfig().getTimeout();
 		HttpRequest.setTimeout(timeout_ms);
 
 		// Parse the credentials, if needed.
 		String user = RemoteApplication.getConfig().getUser();
 		String password = RemoteApplication.getConfig().getPassword();
 		if (!TextUtils.isEmpty(password))
 			HttpRequest.setUserPassword(user, password);
 		else
 			HttpRequest.setUserPassword(null, null);
 	}
 
 	private void setServer() {
 		// Only set the host if manual. Otherwise we'll auto-detect it with
 		// Discoverer -> addAnnouncedServers
 		if (RemoteApplication.getConfig().isManuallySetServer()) {
 			mRemote.setServer(RemoteApplication.getConfig().constructServer());
 			requestUpdateASAP(100);
 		}
 		else {
 			if (mDialogToDismiss > 0)
 				dismissDialog(mDialogToDismiss);
 			
 			if (mPleaseWaitDialog != null)
 				mPleaseWaitDialog.dismiss();
 			
 			mPleaseWaitDialog = ProgressDialog.show(this, "", "Looking for a server...", true);
 			
 			if (mServerDiscoverer != null)
 				mServerDiscoverer.setReceiver(null);
 			
 			mServerDiscoverer = new BoxeeDiscovererThread(this, this);
 			mServerDiscoverer.start();
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog d = null;
 		switch(id)
 		{
 		case DIALOG_NO_PASSWORD:
 			mDialogToDismiss = DIALOG_NO_PASSWORD;
 			d = createCredentialsRequiredDialog();
 		case DIALOG_NO_SERVER:
 			mDialogToDismiss = DIALOG_NO_SERVER;
 			d =  createNoServerDialog();
 		}
 		if (d != null)
 		{
 			d.setOnDismissListener(new OnDismissListener() {
 				@Override
 				public void onDismiss(DialogInterface dialog) {
 					mDialogToDismiss = -1;
 				}
 			});
 			return d;
 		}
 		else
 			return super.onCreateDialog(id);
 	}
 
 	private Dialog createCredentialsRequiredDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder
 			.setTitle("Credentials required")
 			.setMessage("The server "+mServerAddress.name()+" requires username and password in order to be controlled.\nWould you like to enter them now?")
 		       .setCancelable(true)
 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   startSetupActivity();
 		        	   dialog.dismiss();
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();
 		
 		return alert;
 	}
 
 	private Dialog createNoServerDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder
 			.setTitle("No server found")
 			.setMessage("I was unable to find a Boxee server in your network.\n\nWould you like to manually set an IP address? Or maybe have me rescan again?")
 		       .setCancelable(true)
 		       .setPositiveButton("Manual", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   startSetupActivity();
 		        	   dialog.dismiss();
 		           }
 		       })
 		       .setNeutralButton("Rescan", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   setServer();
 		        	   dialog.dismiss();
 		           }
 		       })
 				.setNegativeButton("Neither", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 		AlertDialog alert = builder.create();
 		
 		return alert;
 	}
 
 	/**
 	 * Callback when user alters preferences.
 	 */
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String pref) {
 		loadPreferences();
 		setServer();
 	}
 
 	/**
 	 * Called when the discovery request we sent in onCreate finishes. If we
 	 * find a server matching mAutoName, we use that.
 	 * 
 	 * @param servers
 	 *            list of discovered servers
 	 */
 	public void addAnnouncedServers(ArrayList<ServerAddress> servers) {
 		
 		if (mPleaseWaitDialog != null)
 			mPleaseWaitDialog.dismiss();
 		mPleaseWaitDialog = null;
 		
 		
 		// This condition shouldn't ever be true.
 		if (RemoteApplication.getConfig().isManuallySetServer()) {
 			Log.d(TAG, "Skipping announced servers. Set manually");
 			return;
 		}
 
 		String preferred = RemoteApplication.getConfig().getServerName();
 
 		for (int k = 0; k < servers.size(); ++k) {
 			final ServerAddress server = servers.get(k);
 			if (server.name().equals(preferred) || TextUtils.isEmpty(preferred)) {
 				if (!server.valid()) {
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							showMessage(String.format("Found '%s' but looks broken", server.name()), 3000);
 						}
 					});
 					continue;
 				} else {
 					mServerAddress = server;
 					mRemote.setServer(mServerAddress);
 					final String serverName = server.name();
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							setTitle(getString(R.string.app_name)+" - "+serverName);
 						}
 					});
 					
 
 					if (server.authRequired())
 					{
 						if (!HttpRequest.hasCredentials())
 						{
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									showDialog(DIALOG_NO_PASSWORD);
 								}
 							});
 						}
 					}
 					
 					requestUpdateASAP(100);
 					return;
 				}
 			}
 		}
 
 		mServerAddress = null;
 		
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				setTitle(getString(R.string.app_name));
 				showDialog(DIALOG_NO_SERVER);
 			}
 		});
 	}
 	
 	@Override
 	public void onShake() {
 		Log.d(TAG, "Shake detect!");
 		pauseIfPlaying();
 	}
 		
 	private void remoteFlipPlayPause() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.flipPlayPause();
 			}
 		}.execute();
 	}
 
 	private void remoteBack() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.back();
 			}
 		}.execute();
 	}
 
 	private void remoteSeek(final double newSeekPosition) {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.seekRelative(newSeekPosition);
 			}
 		}.execute();
 	}
 
 	private void remoteStop() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.stop();
 			}
 		}.execute();
 	}
 
 	private void remoteLeft() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.left();
 			}
 		}.execute();
 	}
 
 	private void remoteRight() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.right();
 			}
 		}.execute();
 	}
 
 	private void remoteUp() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.up();
 			}
 		}.execute();
 	}
 
 	private void remoteDown() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.down();
 			}
 		}.execute();
 	}
 
 	private void remoteSelect() {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.select();
 			}
 		}.execute();
 	}
 
 	private void remoteKeypress(final char unicodeChar) {
 		new DoServerRemoteAction(this, false) {
 			@Override
 			protected void callRemoteFunction() throws Exception {
 				mRemote.keypress(unicodeChar);
 			}
 		}.execute();
 	}
 
 	private static int hmsToSeconds(String hms) {
 		if (TextUtils.isEmpty(hms))
 			return 0;
 
 		int seconds = 0;
 		String[] times = hms.split(":");
 
 		// seconds
 		seconds += Integer.parseInt(times[times.length - 1]);
 
 		// minutes
 		if (times.length >= 2)
 			seconds += Integer.parseInt(times[times.length - 2]) * 60;
 
 		// hours
 		if (times.length >= 3)
 			seconds += Integer.parseInt(times[times.length - 3]) * 3600;
 
 		return seconds;
 	}
 }
