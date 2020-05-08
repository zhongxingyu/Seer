 package com.barleysoft.blitzn;
 
 import com.barleysoft.blitzn.chessclock.BlitznChessClock;
 import com.barleysoft.blitzn.chessclock.BlitznChessPlayer;
 import com.barleysoft.blitzn.chessclock.ChessClock;
 import com.barleysoft.blitzn.chessclock.ChessPlayer;
 import com.barleysoft.blitzn.chessclock.OnChessClockStopListener;
 import com.barleysoft.blitzn.chessclock.ChessClock.DelayMode;
 import com.barleysoft.blitzn.chessclock.ChessClock.Player;
 import com.barleysoft.motion.PitchFlipListener;
 import com.barleysoft.motion.ShakeListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
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
 
 public class Blitzn extends Activity {
 
 	// Constants
 	public static final long CLOCK_RESOLUTION = 100L; // ms
 	public static final String PREFS_NAME = "BlitznPrefs";
 	public static final int ACTIVITY_SET_TIME = 0;
 
 	// Member variables
 	private ChessClock mChessClock;
 	private BlitznChessClockButton mPlayer1ClockButton;
 	private BlitznChessClockButton mPlayer2ClockButton;
 
 	private ShakeListener mShakeListener;
 	private PitchFlipListener mPitchFlipListener;
 
 	private Handler mTimerHandler = new Handler();
 	private AlertDialog mPausedDialog;
 
 	// Configurations
 	private long mDuration = 5 * 1 * 1000L;
 	private boolean mShakeEnabled = true;
 	private boolean mFlipEnabled = true;
 	private boolean mSoundEnabled = true;
 	private boolean mShowIntroDialog = true;
 	private boolean mTimePressureWarningEnabled = true;
 	private DelayMode mDelayMode = DelayMode.NODELAY;
 	private long mDelayTime = 0L;
 
 	private void initializeMainWindow() {
 		// Go full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.main);
 		mPausedDialog = createPausedDialog();
 
 	}
 
 	private BlitznChessClockButton initializeClockButton(final Player player,
 			int resId, boolean isFlipped, ChessClock chessClock,
 			ChessPlayer chessPlayer) {
 		BlitznChessClockButton button = (BlitznChessClockButton) findViewById(resId);
 		button.setIsFlipped(isFlipped);
 		button.setChessClock(chessClock);
 		button.setChessPlayer(chessPlayer);
 		button.setIsSoundEnabled(mSoundEnabled);
 		button.setIsTimePressureWarningEnabled(mTimePressureWarningEnabled);
 		OnClickListener clickListener = new OnClickListener() {
 			public void onClick(View v) {
 				deactivatePlayer(player);
 			}
 		};
 		button.setOnClickListener(clickListener);
 		button.initialize();
 		return button;
 	}
 
 	void initializeChessClock() {
 		mChessClock = new BlitznChessClock();
 
 		ChessPlayer chessPlayer1 = new BlitznChessPlayer();
 		ChessPlayer chessPlayer2 = new BlitznChessPlayer();
 		mChessClock.setChessPlayer(Player.ONE, chessPlayer1);
 		mChessClock.setChessPlayer(Player.TWO, chessPlayer2);
 
 		mChessClock.setClockResolution(CLOCK_RESOLUTION);
 		mChessClock.setDuration(mDuration);
 		mChessClock.setDelayMode(mDelayMode);
 		mChessClock.setDelayTime(mDelayTime);
 
 		mChessClock.initialize();
 
 		mPlayer1ClockButton = initializeClockButton(Player.ONE,
 				R.id.player1Clock, true, mChessClock, chessPlayer1);
 		mPlayer2ClockButton = initializeClockButton(Player.TWO,
 				R.id.player2Clock, false, mChessClock, chessPlayer2);
 
 		mChessClock.setOnChessClockStopListener(new OnChessClockStopListener() {
 			public void onStop() {
 				stopClock();
 			}
 		});
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initializeMainWindow();
 		restorePreferences();
 		initializeChessClock();
 		installShakeListener();
 		installPitchFlipListener();
 		if (mShowIntroDialog) {
 			showIntroDialogBox();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if ((mShakeListener != null) && !mShakeListener.isListening()) {
 			mShakeListener.resume();
 		}
 
 		if ((mPitchFlipListener != null) && !mPitchFlipListener.isListening()) {
 			mPitchFlipListener.resume();
 		}
 
 		// When a paused game is resumed, we don't want to immediately start the
 		// clock,
 		// lest the player is not ready. Rather, we re-emit the paused dialog
 		// box. Since the
 		// clock was placed into the PAUSED state in onPause, we're in a
 		// consistent state.
 		if (mChessClock.isPaused() && !mPausedDialog.isShowing()) {
 			mPausedDialog.show();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		if (mShakeListener != null) {
 			mShakeListener.pause();
 		}
 
 		if (mPitchFlipListener != null) {
 			mPitchFlipListener.pause();
 		}
 
 		// Don't show the dialog box on a system initiated pause
 		if (mChessClock.isStarted()) {
 			pauseClock(false);
 		}
 
 		super.onPause();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mChessClock.isStarted()) {
 			showExitDialog();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	private AlertDialog createPausedDialog() {
 		AlertDialog alertDialog = new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(R.string.paused).create();
 		alertDialog
 				.setOnDismissListener(new DialogInterface.OnDismissListener() {
 					public void onDismiss(DialogInterface dialog) {
 						if (mChessClock.isPaused()) {
 							unPauseClock();
 						}
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
 
 	void restorePreferences() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		mDuration = settings.getLong("duration", 5 * 60 * 1000L);
 		mDelayMode = DelayMode.fromOrdinal(settings.getInt("delayMethod", 0));
 		mDelayTime = settings.getLong("delay", 0L);
 		mShakeEnabled = settings.getBoolean("shakeEnabled", true);
 		mFlipEnabled = settings.getBoolean("flipEnabled", true);
 		mSoundEnabled = settings.getBoolean("soundEnabled", true);
 		mShowIntroDialog = settings.getBoolean("showIntroDialog", true);
 		mTimePressureWarningEnabled = settings.getBoolean(
 				"mTimePressureWarningEnabled", true);
 	}
 
 	void savePreferences() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putLong("duration", mDuration);
 		editor.putLong("delay", mDelayTime);
 		editor.putInt("delayMethod", mDelayMode.ordinal());
 		editor.putBoolean("shakeEnabled", mShakeEnabled);
 		editor.putBoolean("flipEnabled", mFlipEnabled);
 		editor.putBoolean("soundEnabled", mSoundEnabled);
 		editor.putBoolean("timePressureWarningEnabled",
 				mTimePressureWarningEnabled);
 		editor.putBoolean("showIntroDialog", mShowIntroDialog);
 		editor.commit();
 	}
 
 	void installShakeListener() {
 		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		mShakeListener = new ShakeListener(this);
 		mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
 			public void onShake() {
 				if (mShakeEnabled && !mChessClock.isPaused()
 						&& !mChessClock.isReady()) {
 					vibe.vibrate(100);
 					resetClock();
 				}
 			}
 		});
 	}
 
 	void installPitchFlipListener() {
 		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		mPitchFlipListener = new PitchFlipListener(this);
 		mPitchFlipListener
 				.setOnPitchFlipListener(new PitchFlipListener.OnPitchFlipListener() {
 					public void onPitchFlip(PitchFlipListener.State state) {
 						if (mFlipEnabled && mChessClock.isStarted()) {
 							if ((state == PitchFlipListener.State.UP)
 									&& mChessClock.isPaused()) {
 								Log.i("Blitzn", "flip detected, unpausing");
 								vibe.vibrate(100);
 								unPauseClock();
 							} else if ((state == PitchFlipListener.State.DOWN)
 									&& !mChessClock.isPaused()) {
 								Log.i("Blitzn", "flip detected, pausing");
 								vibe.vibrate(100);
 								pauseClock(true);
 							}
 						}
 					}
 				});
 	}
 
 	void startClockTimer() {
 		mTimerHandler.removeCallbacks(updateTimeTask);
 		mTimerHandler.postDelayed(updateTimeTask, CLOCK_RESOLUTION);
 	}
 
 	void stopClockTimer() {
 		mTimerHandler.removeCallbacks(updateTimeTask);
 	}
 
 	void deactivatePlayer(Player player) {
 		if (mChessClock.isReady()) {
 			startClockTimer();
 			setKeepScreenOn(true);
 		}
 
 		mChessClock.deactivatePlayer(player);
 
 		if (player == Player.ONE) {
 			mPlayer2ClockButton.activate();
 			mPlayer1ClockButton.deactivate();
 		} else {
 			mPlayer2ClockButton.deactivate();
 			mPlayer1ClockButton.activate();
 		}
 	}
 
 	void resetClock() {
 		setKeepScreenOn(false);
 		stopClockTimer();
 		mChessClock.reset();
 		mPlayer1ClockButton.reset();
 		mPlayer2ClockButton.reset();
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
 		stopClockTimer();
 		mChessClock.pause();
 		if (showDialog) {
 			mPausedDialog.show();
 		}
 	}
 
 	void unPauseClock() {
 		if (mPausedDialog.isShowing()) {
 			mPausedDialog.dismiss();
 		}
 		mChessClock.unpause();
 		startClockTimer();
 	}
 
 	void setKeepScreenOn(boolean keepScreenOn) {
 		findViewById(R.id.mainLayout).setKeepScreenOn(keepScreenOn);
 	}
 
 	void stopClock() {
 		setKeepScreenOn(false);
 		stopClockTimer();
 		mPlayer1ClockButton.stop();
 		mPlayer2ClockButton.stop();
 	}
 
 	private Runnable updateTimeTask = new Runnable() {
 		public void run() {
 			mChessClock.tick();
 
 			if (mChessClock.isRunningForPlayer(Player.ONE)) {
 				mPlayer1ClockButton.tick();
 			} else if (mChessClock.isRunningForPlayer(Player.TWO)) {
 				mPlayer2ClockButton.tick();
 			}
 
 			// Reschedule the next tick
 			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
 			mTimerHandler.postAtTime(this, nextUpdate);
 		}
 	};
 
 	private void setDuration(long duration) {
 		mDuration = duration;
 		mChessClock.setDuration(duration);
 	}
 
 	private void setDelayTime(long delayTime) {
 		mDelayTime = delayTime;
 		mChessClock.setDelayTime(delayTime);
 	}
 
 	private void setDelayMode(DelayMode delayMode) {
 		mDelayMode = delayMode;
 		mChessClock.setDelayMode(delayMode);
 	}
 
 	private void setSoundEnabled(boolean soundEnabled) {
 		mSoundEnabled = soundEnabled;
 		mPlayer1ClockButton.setIsSoundEnabled(soundEnabled);
 		mPlayer2ClockButton.setIsSoundEnabled(soundEnabled);
 	}
 
 	private void setTimePressureWarningEnabled(boolean timePressure) {
 		mTimePressureWarningEnabled = timePressure;
 		mPlayer1ClockButton.setIsTimePressureWarningEnabled(timePressure);
 		mPlayer2ClockButton.setIsTimePressureWarningEnabled(timePressure);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		// Back button on SetTime sub-Activity is overridden to bundle the
 		// results and RETURN_OK
 		Bundle extras = intent.getExtras();
 		long old_duration = mDuration;
 		setDuration(extras.getLong("durationMinutes") * 60 * 1000);
 
 		// NOTE(sirp): setDelayMethodFromInt has the side-effect of clearing the
 		// delay so we must store the old delay before calling it
 		long oldDelayTime = mDelayTime;
 		setDelayTime((extras.getLong("delaySeconds") * 1000));
 
 		DelayMode oldDelayMode = mDelayMode;
 		setDelayMode(DelayMode.fromOrdinal(extras.getInt("delayMethod")));
 
 		mShakeEnabled = extras.getBoolean("shakeEnabled");
 		mFlipEnabled = extras.getBoolean("flipEnabled");
 
 		setSoundEnabled(extras.getBoolean("soundEnabled"));
 		setTimePressureWarningEnabled(extras
 				.getBoolean("timePressureWarningEnabled"));
 
 		// Only reset the clock if we changed something related to time-keeping
 		if ((old_duration != mDuration) || (oldDelayTime != mDelayTime)
 				|| (oldDelayMode != mDelayMode)) {
 			resetClock();
 		}
 	}
 
 	private void setTime() {
 		Intent setTimeIntent = new Intent(this, SetTime.class);
 		setTimeIntent.putExtra("delayMethod", mDelayMode.ordinal());
 		setTimeIntent.putExtra("durationMinutes", mDuration / 60 / 1000);
 		setTimeIntent.putExtra("delaySeconds", mDelayTime / 1000);
 		setTimeIntent.putExtra("shakeEnabled", mShakeEnabled);
 		setTimeIntent.putExtra("flipEnabled", mFlipEnabled);
 		setTimeIntent.putExtra("soundEnabled", mSoundEnabled);
 		setTimeIntent.putExtra("timePressureWarningEnabled",
 				mTimePressureWarningEnabled);
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
 		pauseMenu
 				.setEnabled(mChessClock.isStarted() && !mChessClock.isPaused());
 
 		MenuItem resetMenu = menu.findItem(R.id.resetMenu);
 		resetMenu.setEnabled(!mChessClock.isReady() && !mChessClock.isPaused());
 
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
 								Blitzn.this.finish();
 							}
 						}).setNegativeButton(R.string.no, null).show();
 	}
 }
