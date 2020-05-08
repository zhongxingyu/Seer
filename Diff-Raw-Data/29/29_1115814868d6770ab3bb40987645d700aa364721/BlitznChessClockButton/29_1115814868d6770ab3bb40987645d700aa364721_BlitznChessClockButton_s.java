 package com.barleysoft.blitzn;
 
 import com.barleysoft.blitzn.chessclock.ChessClock;
 import com.barleysoft.blitzn.chessclock.ChessPlayer;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.util.AttributeSet;
 import android.widget.Button;
 
 public class BlitznChessClockButton extends Button implements ChessClockButton {
	public static final long TIME_PRESSURE_SIREN_INTERVAL = 1000; // ms
 
 	private ChessClock mChessClock;
 	private ChessPlayer mChessPlayer;
 	private boolean mIsFlipped = false;
 	private boolean mIsSoundEnabled = false;
 	private boolean mIsTimePressureWarningEnabled = false;
 	private MediaPlayer mClicker;
 	private MediaPlayer mGameOverDinger;
 	private MediaPlayer mTimePressureSiren;
 	private long mTicks = 0L;
 	private long mClockResolution;
 
 	public BlitznChessClockButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		String fontLocation = context.getString(R.string.clock_font);
 		Typeface face = Typeface.createFromAsset(context.getAssets(),
 				fontLocation);
 		setTypeface(face);
 		initializeSound(context);
 	}
 
 	private void initializeSound(Context context) {
 		mClicker = createMediaPlayer(context, R.raw.click1);
 		mGameOverDinger = createMediaPlayer(context, R.raw.gameover);
 		mTimePressureSiren = createMediaPlayer(context, R.raw.timepressure);
 	}
 
 	private MediaPlayer createMediaPlayer(Context context, int resId) {
 		MediaPlayer player = MediaPlayer.create(context, resId);
 		// player will be null if sound is not supported on device
 		if (player != null) {
 			player.setVolume(1.0f, 1.0f);
 		}
 		return player;
 	}
 
 	private void play(MediaPlayer player) {
 		if ((player == null) || !mIsSoundEnabled) {
 			return;
 		}
 		player.start();
 	}
 
 	public void setIsFlipped(boolean isFlipped) {
 		mIsFlipped = isFlipped;
 	}
 
 	public boolean getIsFlipped() {
 		return mIsFlipped;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		if (mIsFlipped) {
 			Rect r = canvas.getClipBounds();
 			canvas.rotate(180, r.exactCenterX(), r.exactCenterY());
 		}
 		super.onDraw(canvas);
 	}
 
 	public void activate() {
 		setClickable(true);
 		setBackgroundColor(Color.BLACK);
 		updateTimeLeft();
 	}
 
 	public void deactivate() {
 		play(mClicker);
 		setBackgroundColor(Color.TRANSPARENT);
 		setClickable(false);
 		updateTimeLeft();
 	}
 
 	public void setIsSoundEnabled(boolean isSoundEnabled) {
 		mIsSoundEnabled = isSoundEnabled;
 	}
 
 	public boolean getIsSoundEnabled() {
 		return mIsSoundEnabled;
 	}
 
 	public void setChessPlayer(ChessPlayer chessPlayer) {
 		mChessPlayer = chessPlayer;
 	}
 
 	public ChessPlayer getChessPlayer() {
 		return mChessPlayer;
 	}
 
 	private void updateTimeLeft() {
 		if (mChessPlayer.isUnderTimePressure() && mIsTimePressureWarningEnabled) {
 			// SS.D
 			long timeLeft = mChessPlayer.getTimeLeft();
 			long seconds = timeLeft / 1000;
 			long remainder = (timeLeft % 1000) / 100;
 			String clockText = String.format("%02d.%d", seconds, remainder);
 			setText(clockText);
 		} else {
 			// MM:SS
 			long seconds = mChessPlayer.getTimeLeft() / 1000;
 			long minutes = seconds / 60;
 			seconds = seconds % 60;
 			String clockText = String.format("%02d:%02d", minutes, seconds);
 			setText(clockText);
 		}
 	}
 
 	public void setIsTimePressureWarningEnabled(
 			boolean isTimePressureWarningEnabled) {
 		mIsTimePressureWarningEnabled = isTimePressureWarningEnabled;
 	}
 
 	public boolean getIsTimePressureWarningEnabled() {
 		return mIsTimePressureWarningEnabled;
 	}
 
 	public void initialize() {
 		activate();
 		updateTimeLeft();
 	}
 
 	public void reset() {
 		activate();
 		updateTimeLeft();
 		mTicks = 0L;
 	}
 
 	public void tick() {
 		updateTimeLeft();
		if (mChessPlayer.isUnderTimePressure() && mIsTimePressureWarningEnabled) {
			if (((mTicks * mClockResolution) % TIME_PRESSURE_SIREN_INTERVAL) == 0) {
				play(mTimePressureSiren);
			}
		}
 		mTicks++;
 	}
 
 	public void stop() {
 		if (mChessPlayer.hasTimeExpired()) {
 			play(mGameOverDinger);
 			setBackgroundColor(Color.RED);
 			setClickable(false);
 		} else {
 			setBackgroundColor(Color.TRANSPARENT);
 			setClickable(false);
 		}
 	}
 
 	public void setClockResolution(long clockResolution) {
 		mClockResolution = clockResolution;
 	}
 
 	public long getClockResolution() {
 		return mClockResolution;
 	}
 
 	public void setChessClock(ChessClock chessClock) {
 		mChessClock = chessClock;
 	}
 
 	public ChessClock getChessClock() {
 		return mChessClock;
 	}
 
 }
