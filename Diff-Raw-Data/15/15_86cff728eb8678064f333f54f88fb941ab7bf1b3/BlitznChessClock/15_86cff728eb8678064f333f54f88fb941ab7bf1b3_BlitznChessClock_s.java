 package com.barleysoft.blitzn;
 
 import com.barleysoft.blitzn.delay.DelayContext;
 import com.barleysoft.blitzn.delay.DelayStrategy;
 import com.barleysoft.blitzn.delay.DelayStrategyFactory;
 import com.barleysoft.motion.PitchFlipListener;
 import com.barleysoft.motion.ShakeListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class BlitznChessClock extends Activity implements ChessClock {
 	// NOTE(sirp); Android optimization document[1] considers enum bad since
 	// they add overhead. They recommend using a static final int instead.
 	// I am purposefully ignoring this advice since performance isn't
 	// critical here, and writing correct code is.
 	//
 	// [1]
 	// http://developer.android.com/guide/practices/design/performance.html#avoid_enums
 	public enum Player {
 		ONE, TWO
 	}
 
 	public enum ClockState {
 		NOSTATE, READY, PLAYER1_RUNNING, PLAYER2_RUNNING, PLAYER1_PAUSED, PLAYER2_PAUSED, STOPPED
 	}
 
 	// Constants
 	public static final int CLOCK_RESOLUTION = 100; // ms
 	public static final String PREFS_NAME = "BlitznPrefs";
 	public static final int ACTIVITY_SET_TIME = 0;
 
 	// Member variables
 	private ClockState mClockState = ClockState.NOSTATE;
 
 	private ClockButton mPlayer1Clock;
 	private long mPlayer1TimeLeft = 0L;
 
 	private long mPlayer2TimeLeft = 0L;
 	private ClockButton mPlayer2Clock;
 
 	private DelayContext mDelayContext;
 
 	private ShakeListener mShakeListener;
 	private PitchFlipListener mPitchFlipListener;
 
 	private Handler mHandler = new Handler();
 	private AlertDialog mPausedDialog;
 
 	// Two players are used to get fast playback
 	private MediaPlayer mClockClicker1;
 	private MediaPlayer mClockClicker2;
 	private MediaPlayer mGameOverSoundPlayer;
 
 	// Configurations
 	private long mDuration = 5 * 1 * 1000L;
 	private boolean mShakeEnabled = true;
 	private boolean mFlipEnabled = true;
 	private boolean mSoundEnabled = true;
 	private boolean mShowIntroDialog = true;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Go full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.main);
 
 		// Setup Player 1
 		mPlayer1Clock = (ClockButton) findViewById(R.id.player1Clock);
 		mPlayer1Clock.setIsFlipped(true);
 		OnClickListener player1ClickListener = new OnClickListener() {
 			public void onClick(View v) {
 				onPlayerClick(Player.ONE);
 			}
 		};
 		mPlayer1Clock
 				.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);
 
 		// Setup Player 2
 		mPlayer2Clock = (ClockButton) findViewById(R.id.player2Clock);
 		OnClickListener player2ClickListener = new OnClickListener() {
 			public void onClick(View v) {
 				onPlayerClick(Player.TWO);
 			}
 		};
 
 		mPlayer2Clock
 				.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);
 
 		// Initialize everything
 
 		mPausedDialog = createPausedDialog();
 		restorePreferences();
 		initializeSound();
 		installShakeListener();
 		installPitchFlipListener();
 		resetClock();
 
 		if (mShowIntroDialog)
 			showIntroDialogBox();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if ((mShakeListener != null) && !mShakeListener.isListening())
 			mShakeListener.resume();
 
 		if ((mPitchFlipListener != null) && !mPitchFlipListener.isListening())
 			mPitchFlipListener.resume();
 
 		// When a paused game is resumed, we don't want to immediately start the
 		// clock,
 		// lest the player is not ready. Rather, we re-emit the paused dialog
 		// box. Since the
 		// clock was placed into the PAUSED state in onPause, we're in a
 		// consistent state.
 		if (isGamePaused() && !mPausedDialog.isShowing())
 			mPausedDialog.show();
 	}
 
 	@Override
 	protected void onPause() {
 		if (mShakeListener != null)
 			mShakeListener.pause();
 		if (mPitchFlipListener != null)
 			mPitchFlipListener.pause();
 
 		// Don't show the dialog box on a system initiated pause
 		if (isGameInProgress())
 			pauseClock(false);
 
 		super.onPause();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (isGameInProgress())
 			showExitDialog();
 		else
 			super.onBackPressed();
 	}
 
 	private AlertDialog createPausedDialog() {
 		AlertDialog alertDialog = new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(R.string.paused).create();
 		alertDialog
 				.setOnDismissListener(new DialogInterface.OnDismissListener() {
 					public void onDismiss(DialogInterface dialog) {
 						if (isGamePaused())
 							unPauseClock();
 					}
 				});
 		return alertDialog;
 	}
 
 	private void showIntroDialogBox() {
 		// Ask the user if they want to quit
 		new AlertDialog.Builder(this)
 				// .setIcon(android.R.drawable.ic_dialog_info)
 				.setTitle(R.string.intro_title).setMessage(R.string.intro_text)
 				.setNegativeButton(R.string.ok, null).show();
 		mShowIntroDialog = false;
 	}
 
 	private void initializeSound() {
 		mClockClicker1 = createMediaPlayer(R.raw.click1);
 		mClockClicker2 = createMediaPlayer(R.raw.click1);
 		mGameOverSoundPlayer = createMediaPlayer(R.raw.gameover);
 	}
 
 	private MediaPlayer createMediaPlayer(int resId) {
 		MediaPlayer clicker = MediaPlayer.create(this, resId);
 		// clicker will be null if sound is not supported on device
 		if (clicker != null)
 			clicker.setVolume(1.0f, 1.0f);
 		return clicker;
 	}
 
 	void playClick() {
 		if ((mClockClicker1 == null) || !mSoundEnabled)
 			return;
 
 		// Fall-back to second clicker so that we can play clicks nearly
 		// simultaneously
 		if (mClockClicker1.isPlaying())
 			mClockClicker2.start();
 		else
 			mClockClicker1.start();
 	}
 
 	void playGameOverSound() {
 		if ((mGameOverSoundPlayer == null) || !mSoundEnabled)
 			return;
 		mGameOverSoundPlayer.start();
 	}
 
 	void setDelayMethodFromInt(int delayMethod) {
 		// TODO(sirp): rename setDelayMethodById
 		DelayStrategy delayStrategy = DelayStrategyFactory
 				.getDelayStrategyById(delayMethod);
 		// TODO(sirp): should we just use setStrategy here
 		mDelayContext = new DelayContext(this, delayStrategy);
 	}
 
 	int getDelayMethodAsInt() {
 		DelayStrategy delayStrategy = mDelayContext.getStrategy();
 		return DelayStrategyFactory.getIdForDelayStrategy(delayStrategy);
 	}
 
 	void restorePreferences() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		mDuration = settings.getLong("duration", 5 * 60 * 1000L);
 		setDelayMethodFromInt(settings.getInt("delayMethod", 0));
 		mDelayContext.setDelay(settings.getLong("delay", 0L));
 		mShakeEnabled = settings.getBoolean("shakeEnabled", true);
 		mFlipEnabled = settings.getBoolean("flipEnabled", true);
 		mSoundEnabled = settings.getBoolean("soundEnabled", true);
 		mShowIntroDialog = settings.getBoolean("showIntroDialog", true);
 	}
 
 	void savePreferences() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putLong("duration", mDuration);
 		editor.putLong("delay", mDelayContext.getDelay());
 		editor.putInt("delayMethod", getDelayMethodAsInt());
 		editor.putBoolean("shakeEnabled", mShakeEnabled);
 		editor.putBoolean("flipEnabled", mFlipEnabled);
 		editor.putBoolean("soundEnabled", mSoundEnabled);
 		editor.putBoolean("showIntroDialog", mShowIntroDialog);
 		editor.commit();
 	}
 
 	void installShakeListener() {
 		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		// TODO(sirp): add configuration setting for shake
 		mShakeListener = new ShakeListener(this);
 		mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
 			public void onShake() {
 				if (mShakeEnabled && !isGamePaused() && !isGameReady()) {
 					vibe.vibrate(100);
 					resetClock();
 				}
 			}
 		});
 	}
 
 	void installPitchFlipListener() {
 		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		// TODO(sirp): add configuration setting for shake
 		mPitchFlipListener = new PitchFlipListener(this);
 		mPitchFlipListener
 				.setOnPitchFlipListener(new PitchFlipListener.OnPitchFlipListener() {
 					public void onPitchFlip(PitchFlipListener.State state) {
 						if (mFlipEnabled && isGameInProgress()) {
 							if ((state == PitchFlipListener.State.UP)
 									&& isGamePaused()) {
 								Log.i("Blitzn", "flip detected, unpausing");
 								vibe.vibrate(100);
 								unPauseClock();
 							} else if ((state == PitchFlipListener.State.DOWN)
 									&& !isGamePaused()) {
 								Log.i("Blitzn", "flip detected, pausing");
 								vibe.vibrate(100);
 								pauseClock(true);
 							}
 						}
 					}
 				});
 	}
 
 	void onPlayerClick(Player which) {
 		if (which == Player.ONE) {
 			switch (mClockState) {
 			case READY:
 				initiateClock(Player.ONE);
 				break;
 			case PLAYER1_RUNNING:
 				toggleClock();
 				break;
 			}
 		} else {
 			switch (mClockState) {
 			case READY:
 				initiateClock(Player.TWO);
 				break;
 			case PLAYER2_RUNNING:
 				toggleClock();
 				break;
 			}
 		}
 
 	}
 
 	void startClockTimer() {
 		mHandler.removeCallbacks(updateTimeTask);
 		mHandler.postDelayed(updateTimeTask, CLOCK_RESOLUTION);
 	}
 
 	void stopClockTimer() {
 		mHandler.removeCallbacks(updateTimeTask);
 	}
 
 	void initiateClock(Player which) {
 		if (mClockState != ClockState.READY) {
 			// throw new ClockStateException("already started");
 		}
 
 		activateClock(which);
 		startClockTimer();
 		setKeepScreenOn(true);
 	}
 
 	void activateClock(Player which) {
 		if (which == Player.ONE) {
 			mPlayer1Clock.setIsActivated(true);
 			mPlayer2Clock.setIsActivated(false);
 			playClick();
 			mClockState = ClockState.PLAYER1_RUNNING;
 		} else {
 			mPlayer2Clock.setIsActivated(true);
 			mPlayer1Clock.setIsActivated(false);
 			playClick();
 			mClockState = ClockState.PLAYER2_RUNNING;
 		}
 	}
 
 	void resetClock() {
 		setKeepScreenOn(false);
 		stopClockTimer();
 
 		mDelayContext.resetDelayForPlayer(Player.ONE);
 		mDelayContext.resetDelayForPlayer(Player.TWO);
 
 		mPlayer1TimeLeft = mDuration;
 		mPlayer1Clock.setIsActivated(true);
 
 		mPlayer2TimeLeft = mDuration;
 		mPlayer2Clock.setIsActivated(true);
 
 		updateClockDisplays();
 		mClockState = ClockState.READY;
 	}
 
 	void toggleClock() {
 		switch (mClockState) {
 		case PLAYER1_RUNNING:
 			mDelayContext.stopDelayForPlayer(Player.ONE);
 			mDelayContext.startDelayForPlayer(Player.TWO);
 			activateClock(Player.TWO);
 			break;
 		case PLAYER2_RUNNING:
 			mDelayContext.stopDelayForPlayer(Player.TWO);
 			mDelayContext.startDelayForPlayer(Player.ONE);
 			activateClock(Player.ONE);
 			break;
 		default:
 			// throw ClockStateException("wrong state");
 		}
 	}
 
 	void pauseClock(boolean showDialog) {
 		// There are two types of pauses:
 		// a) Player initiated, where the player flips the phone over
 		// or hits pause from the menu
 		// b) Where the system pauses the clock because the clock is
 		// no longer visible (hit the settings page or the home button)
 		//
 		// For a) we want to display a paused dialog; however, for b)
 		// we do not.
 		switch (mClockState) {
 		case PLAYER1_RUNNING:
 			stopClockTimer();
 			mClockState = ClockState.PLAYER1_PAUSED;
 			if (showDialog)
 				mPausedDialog.show();
 			break;
 		case PLAYER2_RUNNING:
 			stopClockTimer();
 			mClockState = ClockState.PLAYER2_PAUSED;
 			if (showDialog)
 				mPausedDialog.show();
 			break;
 		default:
 			// throw ClockStateException("wrong state");
 		}
 	}
 
 	void unPauseClock() {
 		switch (mClockState) {
 		case PLAYER1_PAUSED:
 			startClockTimer();
 			mClockState = ClockState.PLAYER1_RUNNING;
 			if (mPausedDialog.isShowing())
 				mPausedDialog.dismiss();
 			break;
 		case PLAYER2_PAUSED:
 			startClockTimer();
 			mClockState = ClockState.PLAYER2_RUNNING;
 			if (mPausedDialog.isShowing())
 				mPausedDialog.dismiss();
 			break;
 		default:
 			// throw ClockStateException("wrong state");
 		}
 	}
 
 	void setKeepScreenOn(boolean keepScreenOn) {
 		View layout = (View) findViewById(R.id.mainLayout);
 		layout.setKeepScreenOn(keepScreenOn);
 	}
 
 	void stopClock() {
 		setKeepScreenOn(false);
 		stopClockTimer();
 		mClockState = ClockState.STOPPED;
 
 		boolean player1Lost = hasPlayerLost(Player.ONE);
 		boolean player2Lost = hasPlayerLost(Player.TWO);
 
 		if (player1Lost && player2Lost) {
 			Log.e("Blitzn", "both players lost, we have a problem!");
 		}
 
 		if (player1Lost) {
 			mPlayer1Clock.setLost();
 		}
 
 		if (player2Lost) {
 			mPlayer2Clock.setLost();
 		}
 		playGameOverSound();
 	}
 
 	boolean hasPlayerLost(Player which) {
 		long timeLeft = (which == Player.ONE) ? mPlayer1TimeLeft
 				: mPlayer2TimeLeft;
 		return (timeLeft < CLOCK_RESOLUTION);
 	}
 
 	void tickForPlayer(Player player) {
 		if (player == Player.ONE)
 			mPlayer1TimeLeft -= CLOCK_RESOLUTION;
 		else
 			mPlayer2TimeLeft -= CLOCK_RESOLUTION;
 	}
 
 	private Runnable updateTimeTask = new Runnable() {
 		public void run() {
 			// Check for either clock expiring
 			if (hasPlayerLost(Player.ONE) || hasPlayerLost(Player.TWO)) {
 				stopClock();
 				return;
 			}
 
 			switch (mClockState) {
 			case PLAYER1_RUNNING:
 				tickForPlayer(Player.ONE);
 				mDelayContext.tickForPlayer(Player.ONE);
 				break;
 			case PLAYER2_RUNNING:
 				tickForPlayer(Player.TWO);
 				mDelayContext.tickForPlayer(Player.TWO);
 				break;
 			}
 
 			updateClockDisplays();
 
 			// Reschedule the next tick
 			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
 			mHandler.postAtTime(this, nextUpdate);
 		}
 	};
 
 	private void updateClockDisplays() {
 		updateClockForPlayer(mPlayer1Clock, mPlayer1TimeLeft);
 		updateClockForPlayer(mPlayer2Clock, mPlayer2TimeLeft);
 	}
 
 	private void updateClockForPlayer(ClockButton clockView, long timeLeft) {
 		int seconds = (int) timeLeft / 1000;
 		int minutes = seconds / 60;
 		seconds = seconds % 60;
 		String clockText = String.format("%02d:%02d", minutes, seconds);
 		clockView.setText(clockText);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		// Back button on SetTime sub-Activity is overridden to bundle the
 		// results and RETURN_OK
 		Bundle extras = intent.getExtras();
 		long old_duration = mDuration;
 		mDuration = extras.getLong("durationMinutes") * 60 * 1000;
 
		int old_delayMethod = getDelayMethodAsInt();
 		setDelayMethodFromInt(extras.getInt("delayMethod"));
 
		long old_delay = mDelayContext.getDelay();
 		mDelayContext.setDelay(extras.getLong("delaySeconds") * 1000);
 
 		mShakeEnabled = extras.getBoolean("shakeEnabled");
 		mFlipEnabled = extras.getBoolean("flipEnabled");
 		mSoundEnabled = extras.getBoolean("soundEnabled");
 
 		// Only reset the clock if we changed something related to time-keeping
 		if ((old_duration != mDuration)
				|| (old_delay != mDelayContext.getDelay())
				|| (old_delayMethod != getDelayMethodAsInt()))
 			resetClock();
 	}
 
 	private void setTime() {
 		Intent setTimeIntent = new Intent(this, SetTime.class);
 		setTimeIntent.putExtra("delayMethod", getDelayMethodAsInt());
 		setTimeIntent.putExtra("durationMinutes", mDuration / 60 / 1000);
 		setTimeIntent.putExtra("delaySeconds", mDelayContext.getDelay() / 1000);
 		setTimeIntent.putExtra("shakeEnabled", mShakeEnabled);
 		setTimeIntent.putExtra("flipEnabled", mFlipEnabled);
 		setTimeIntent.putExtra("soundEnabled", mSoundEnabled);
 		startActivityForResult(setTimeIntent, ACTIVITY_SET_TIME);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.resetMenu:
 			resetClock();
 			return true;
 		case R.id.settingsMenu:
 			setTime();
 			return true;
 		case R.id.pauseMenu:
 			pauseClock(true);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem pauseMenu = menu.findItem(R.id.pauseMenu);
 		pauseMenu.setEnabled(isGameInProgress() && !isGamePaused());
 
 		MenuItem resetMenu = menu.findItem(R.id.resetMenu);
 		resetMenu.setEnabled((mClockState != ClockState.READY)
 				&& !isGamePaused());
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		savePreferences();
 	}
 
 	private void showExitDialog() {
 		// Ask the user if they want to quit
 		new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(R.string.quit)
 				.setMessage(R.string.leave_in_progress)
 				.setPositiveButton(R.string.yes,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int which) {
 
 								// Stop the activity
 								BlitznChessClock.this.finish();
 							}
 						}).setNegativeButton(R.string.no, null).show();
 	}
 
 	public boolean isGameInProgress() {
 		switch (mClockState) {
 		case NOSTATE:
 		case READY:
 		case STOPPED:
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isGamePaused() {
 		switch (mClockState) {
 		case PLAYER1_PAUSED:
 		case PLAYER2_PAUSED:
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isGameReady() {
 		return (mClockState == ClockState.READY);
 	}
 
 	public boolean isDelayEnabled() {
 		return mDelayContext.isDelayEnabled();
 	}
 
 	public long getTimeLeftForPlayer(Player player) {
 		return (player == Player.ONE) ? mPlayer1TimeLeft : mPlayer2TimeLeft;
 	}
 
 	public void adjustTimeLeftForPlayer(Player player, long adjustment) {
 		if (player == Player.ONE)
 			mPlayer1TimeLeft += adjustment;
 		else
 			mPlayer2TimeLeft += adjustment;
 	}
 
 }
