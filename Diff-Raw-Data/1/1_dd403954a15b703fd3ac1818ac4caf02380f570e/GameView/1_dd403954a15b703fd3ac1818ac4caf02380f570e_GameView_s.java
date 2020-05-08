 package com.luzi82.game;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	// final private GameActivity mGameActivity;
 	final private AbstractState<?> mGame;
 	private SurfaceHolder mHolder;
 	// private State mState;
 	private boolean mActive = false;
 	private boolean mSurfaceAvailable = false;
 	private boolean mRunning = false;
 	private long mRefreshPeriodMs;
 	private boolean mStartDone = false;
 
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		GameActivity gameActivity = (GameActivity) context;
 		mGame = gameActivity.createGame();
 		mRefreshPeriodMs = gameActivity.getPeriodMs();
 
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 
 		setFocusable(true);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		super.onKeyDown(keyCode, event);
 		synchronized (mGame) {
 			mGame.onKeyDown(keyCode, event);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		super.onKeyUp(keyCode, event);
 		synchronized (mGame) {
 			mGame.onKeyUp(keyCode, event);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		super.onTouchEvent(event);
 		synchronized (mGame) {
 			mGame.onTouchEvent(event);
 		}
 		return true;
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasWindowFocus) {
 		refreshState();
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		synchronized (mGame) {
 			mGame.surfaceChanged(format, width, height);
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		mSurfaceAvailable = true;
 		refreshState();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		mSurfaceAvailable = false;
 		refreshState();
 	}
 
 	public void onResume() {
 		mActive = true;
 		refreshState();
 	}
 
 	public void onPause() {
 		mActive = false;
 		refreshState();
 	}
 
 	public void onStop() {
 		if (mStartDone) {
 			mGame.onStateEnd();
 		}
 	}
 
 	Timer mTimer;
 
 	private void setTimerEnabled(boolean aEnabled) {
 		if (aEnabled && (mTimer == null)) {
 			mTimer = new Timer();
 			mTimer.scheduleAtFixedRate(new TimerTask() {
 				@Override
 				public void run() {
 					synchronized (mGame) {
 						mGame.tick();
 						Canvas c = null;
 						try {
 							c = mHolder.lockCanvas(null);
 							synchronized (mHolder) {
 								mGame.draw(c);
 							}
 						} finally {
 							if (c != null) {
 								mHolder.unlockCanvasAndPost(c);
 							}
 						}
 					}
 				}
 			}, 0, mRefreshPeriodMs);
 		} else if ((!aEnabled) && (mTimer != null)) {
 			mTimer.cancel();
 			mTimer = null;
 		}
 	}
 
 	private void refreshState() {
 		boolean currentState = mActive && mSurfaceAvailable && hasWindowFocus();
 		if (currentState == mRunning) {
 			return;
 		}
 		mRunning = currentState;
 		if (mRunning) {
 			if (!mStartDone) {
 				mGame.onStateStart();
 			}
 			setTimerEnabled(true);
 			mGame.onGameResume();
 		} else {
 			setTimerEnabled(false);
 			mGame.onGamePause();
 		}
 	}
 
 }
