 package com.dreamlink.game.kickball;
 
 import com.dreamlink.game.kickball.module.Ball;
 import com.dreamlink.game.kickball.module.Engine;
 import com.dreamlink.game.kickball.module.Player;
 import com.dreamlink.game.kickball.util.DisplayUtil;
 import com.dreamlink.game.kickball.util.Log;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	private static final String TAG = "GameView";
 
 	private SurfaceHolder mSurfaceHolder;
 	private Paint mPaint;
 
 	// Ball bitmap
 	private Bitmap mBallBitmap = BitmapFactory.decodeResource(getResources(),
 			R.drawable.ball);
 	// Player bitmap
 	private Bitmap mPlayerBitmap = BitmapFactory.decodeResource(getResources(),
 			R.drawable.player);
 	// Background bitmap
 	private Bitmap mBackgroundBitmap = BitmapFactory.decodeResource(
 			getResources(), R.drawable.bg);
 	// Goal bitmap
 	private Bitmap mGoalBitmap = BitmapFactory.decodeResource(getResources(),
 			R.drawable.goal);
 	private Bitmap mScoreBoxBitmap = BitmapFactory.decodeResource(
 			getResources(), R.drawable.score_box);
 	private Bitmap[] mScoreBitmap = {
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_0),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_1),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_2),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_3),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_4),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_5),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_6),
 			BitmapFactory.decodeResource(getResources(), R.drawable.score_7) };
 
 	private Bitmap[] mScoreRedBitmap = {
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_0),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_1),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_2),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_3),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_4),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_5),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_6),
 			BitmapFactory
 					.decodeResource(getResources(), R.drawable.score_red_7) };
 
 	// Ball
 	private Ball mBall;
 	private Player mPlayer;
 
 	// Score
 	private int mSoreWine = 7;
 	private int mScoreOfCompetitor = 0;
 	private int mScoreOfMine = 0;
 	private boolean mDrawScoreRead = false;
 
 	// Check the ball is clicked or not.
 	private boolean mIsBallTouched = false;
 	// Last touch time, it is use for calculate speed.
 	private long mLastTouchedMovingTime;
 
 	private Context mContext;
 	private BallCallback mBallCallback;
 
 	private float mScreenWidth;
 	private float mScreenHeight;
 
 	private DrawThread mDrawThread;
 
 	private boolean mStopBallMove = false;
 	private boolean mIsBallMoving = false;
 
 	public GameView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	private void init(Context context) {
 		mContext = context;
 		// init surface
 		mSurfaceHolder = getHolder();
 		mSurfaceHolder.addCallback(this);
 		// init paint
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
 
 		setFocusable(true);
 
 		mScreenWidth = DisplayUtil.getScreenWidth(context);
 		mScreenHeight = DisplayUtil.getScreenHeight(context);
 
 		resetBallAndPlayer();
 	}
 
 	private void resetBallAndPlayer() {
 		float playerX = mScreenWidth / 2;
 		float playerY = mScreenHeight - mPlayerBitmap.getHeight() / 2;
 		if (mPlayer == null) {
 			mPlayer = new Player(playerX, playerY,
 					mPlayerBitmap.getHeight() / 2, mPlayerBitmap);
 		} else {
 			mPlayer.setCenterX(playerX);
 			mPlayer.setCenterY(playerY);
 			mPlayer.setSpeed(0, 0);
 		}
 
 		float ballX = playerX;
 		float ballY = mScreenHeight - mPlayerBitmap.getHeight()
 				- mBallBitmap.getHeight() - 5;
 		if (mBall == null) {
 			mBall = new Ball(ballX, ballY, mBallBitmap.getHeight() / 2,
 					mBallBitmap);
 		} else {
 			mBall.setCenterX(ballX);
 			mBall.setCenterY(ballY);
 			mBall.setSpeed(0, 0);
 		}
 		mIsBallMoving = false;
 		mIsBallTouched = false;
 	}
 
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		mScreenHeight = height;
 		mScreenWidth = width;
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.d(TAG, "surfaceCreated");
 		mSurfaceHolder = holder;
 		mDrawThread = new DrawThread(this, holder);
 		mDrawThread.startDraw(true);
 		mDrawThread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d(TAG, "surface destoryed");
 		mSurfaceHolder = null;
 		mDrawThread.startDraw(false);
 		mDrawThread = null;
 
 		mStopBallMove = true;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			mIsBallTouched = isBallTouched(event.getX(), event.getY());
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mIsBallTouched) {
 				float x = event.getX();
 				float y = event.getY();
 
 				if (mLastTouchedMovingTime != 0) {
 					// The player is moving. Calculate the moving speed.
 					long t = System.currentTimeMillis()
 							- mLastTouchedMovingTime;
 					float speedX = (x - mPlayer.getCenterX()) / t * 20;
 					float speedY = (y - mPlayer.getCenterY()) / t * 20;
 					mPlayer.setSpeed(speedX, speedY);
 					Log.d(TAG, " player speed x = " + mPlayer.getSpeedX()
 							+ ", speed y = " + mPlayer.getSpeedY());
 				}
 				mPlayer.setCenterX(x);
 				mPlayer.setCenterY(y);
 				mLastTouchedMovingTime = System.currentTimeMillis();
 			}
 
 			break;
 		case MotionEvent.ACTION_UP:
 			mLastTouchedMovingTime = 0;
 			mIsBallTouched = false;
 			mPlayer.setSpeed(0, 0);
 			break;
 		default:
 			break;
 		}
 
 		return super.onTouchEvent(event);
 	}
 
 	/**
 	 * check whether the bitmap is checked or not.
 	 * 
 	 * @param touchX
 	 * @param touchY
 	 * @return
 	 */
 	private boolean isBallTouched(float touchX, float touchY) {
 		return Engine.detectPointInCircle(mPlayer, touchX, touchY);
 	}
 
 	public void drawGame(Canvas canvas) {
 		drawBackground(canvas);
 		drawGoal(canvas);
 		drawScore(canvas, mScoreOfCompetitor, mScoreOfMine, mDrawScoreRead);
 		drawBall(canvas);
 		drawPlayer(canvas);
 
 		if (Engine.detectCollision(mPlayer, mBall)) {
 			Log.d(TAG,
 					"Before collision. ball speed x = " + mBall.getSpeedX()
 							+ " speed y = " + mBall.getSpeedY()
 							+ " player speed x = " + mPlayer.getSpeedX()
 							+ ", speed y = " + mPlayer.getSpeedY());
 			Engine.circleCollide(mPlayer, mBall);
 			Log.d(TAG,
 					"After collision. ball speed x = " + mBall.getSpeedX()
 							+ " speed y = " + mBall.getSpeedY()
 							+ " player speed x = " + mPlayer.getSpeedX()
 							+ ", speed y = " + mPlayer.getSpeedY());
 			while (Engine.detectCollision(mPlayer, mBall)) {
 				moveBall();
 			}
 			if (!mIsBallMoving) {
 				startMove();
 			}
 		}
 	}
 
 	private void drawPlayer(Canvas canvas) {
 		float x = ensureX(mPlayer.getCenterX(), mPlayer.getRadius(),
 				mScreenWidth);
 		float y = ensurePlayerY(mPlayer.getCenterY(), mPlayer.getRadius(),
 				mScreenHeight);
 		canvas.drawBitmap(mPlayer.getPicture(), x, y, mPaint);
 	}
 
 	private void drawBall(Canvas canvas) {
 		float x = ensureX(mBall.getCenterX(), mBall.getRadius(), mScreenWidth);
 		float y = ensureBallY(mBall.getCenterY(), mBall.getRadius(),
 				mScreenHeight);
		Log.d(TAG, "drawBall x = " + x + ", y = " + y);
 		canvas.drawBitmap(mBall.getPicture(), x, y, mPaint);
 	}
 
 	private void drawBackground(Canvas canvas) {
 		RectF rectF = new RectF(0, 0, mScreenWidth, mScreenHeight);
 		canvas.drawBitmap(mBackgroundBitmap, null, rectF, null);
 	}
 
 	private void drawGoal(Canvas canvas) {
 		canvas.drawBitmap(mGoalBitmap,
 				mScreenWidth / 2 - mGoalBitmap.getWidth() / 2, mScreenHeight
 						- mGoalBitmap.getHeight(), mPaint);
 	}
 
 	/**
 	 * Make sure the player and ball is in screen.
 	 * 
 	 * @param x
 	 * @param r
 	 * @param screenWith
 	 * @return
 	 */
 	private float ensureX(float x, float r, float screenWith) {
 		return Math.min(Math.max(0, x - r), screenWith - 2 * r);
 	}
 
 	/**
 	 * Make sure the player is in screen.
 	 * 
 	 * @param y
 	 * @param r
 	 * @param screenHeight
 	 * @return
 	 */
 	private float ensurePlayerY(float y, float r, float screenHeight) {
 		return Math.min(Math.max(0, y - r), screenHeight - 2 * r);
 	}
 
 	/**
 	 * Make sure the ball is in screen.
 	 * 
 	 * @param y
 	 * @param r
 	 * @param screenHeight
 	 * @return
 	 */
 	private float ensureBallY(float y, float r, float screenHeight) {
 		return Math.min(y, screenHeight - 2 * r);
 	}
 
 	private void drawScore(Canvas canvas, int scoreOfCompetitor,
 			int scoreOfMine, boolean showRed) {
 		float screenCenterX = mScreenWidth / 2;
 		float screenCenterY = mScreenHeight / 2;
 
 		canvas.drawBitmap(mScoreBoxBitmap,
 				screenCenterX - mScoreBoxBitmap.getWidth() / 2, screenCenterY
 						- mScoreBoxBitmap.getHeight() / 2, mPaint);
 		Bitmap[] scoreBitmap = null;
 		if (showRed) {
 			scoreBitmap = mScoreRedBitmap;
 		} else {
 			scoreBitmap = mScoreBitmap;
 		}
 		// check the score
 		if (scoreOfCompetitor >= scoreBitmap.length
 				|| scoreOfMine >= scoreBitmap.length) {
 			Log.e(TAG, "drawScore() illeagal sore, scoreOfCompetitor = "
 					+ scoreOfCompetitor + ", scoreOfMine = " + scoreOfMine);
 			return;
 		}
 		canvas.drawBitmap(scoreBitmap[scoreOfCompetitor], screenCenterX
 				- scoreBitmap[scoreOfCompetitor].getWidth() / 2, screenCenterY
 				- mScoreBoxBitmap.getHeight() / 4
 				- scoreBitmap[scoreOfCompetitor].getHeight() / 2, mPaint);
 		canvas.drawBitmap(scoreBitmap[scoreOfMine], screenCenterX
 				- scoreBitmap[scoreOfMine].getWidth() / 2,
 				screenCenterY + mScoreBoxBitmap.getHeight() / 4
 						- scoreBitmap[scoreOfMine].getHeight() / 2, mPaint);
 	}
 
 	private void moveBall() {
 		if (mBall.getCenterX() + mBall.getRadius() >= mScreenWidth
 				|| mBall.getCenterX() - mBall.getRadius() <= 0) {
 			// Ball is coming into collision with with screen left or right.
 			// Change the ball's speed direction;
 			mBall.setSpeed(mBall.getSpeedX() * -1, mBall.getSpeedY());
 		}
 
 		float goalLeft = mScreenWidth / 2 - mGoalBitmap.getWidth() / 2;
 		float goalRight = mScreenWidth / 2 + mGoalBitmap.getWidth() / 2;
 		if (mBall.getCenterY() + mBall.getRadius() >= mScreenHeight
 				&& (mBall.getCenterX() <= goalLeft || mBall.getCenterX() >= goalRight)) {
 			// Ball is coming into collision with with screen bottom and does
 			// not in the goal. Change the ball's speed direction;
 			mBall.setSpeed(mBall.getSpeedX(), mBall.getSpeedY() * -1);
 		} else if (mBall.getCenterY() + mBall.getRadius() >= mScreenHeight
 				&& mBall.getCenterX() > goalLeft
 				&& mBall.getCenterX() < goalRight) {
 			// Ball is into the goal, we lose one point.
 			loseOneGoal();
 		} else if (mBall.getCenterY() - mBall.getRadius() <= 0
 				&& mBall.getSpeedY() < 0) {
 			// Ball is moving out into the competitor's screen.
 			moveBallToCompetitor(mBall.getCenterX(), mBall.getCenterY(),
 					mBall.getSpeedX(), mBall.getSpeedY());
 		}
		Log.d(TAG,
				"moveBall() from (" + mBall.getCenterX() + ", "
						+ mBall.getCenterY() + "), to ("
						+ (mBall.getCenterX() + mBall.getSpeedX()) + ", "
						+ (mBall.getCenterY() + mBall.getSpeedY()) + ").");
 		mBall.setCenterX(mBall.getCenterX() + mBall.getSpeedX());
 		mBall.setCenterY(mBall.getCenterY() + mBall.getSpeedY());
 
 	}
 
 	private void loseOneGoal() {
 		resetBallAndPlayer();
 		mScoreOfCompetitor++;
 		if (mScoreOfCompetitor >= mSoreWine) {
 			Log.d(TAG, "We lose.");
 			loseTheGame();
 		}
 
 		if (mBallCallback != null) {
 			mBallCallback.onLoseOneGoal();
 		}
 	}
 
 	public void winOneGoal() {
 		mScoreOfMine++;
 		if (mScoreOfMine >= mSoreWine) {
 			Log.d(TAG, "We win.");
 			winTheGame();
 		}
 	}
 
 	private void winTheGame() {
 		mScoreOfCompetitor = 0;
 		mScoreOfMine = 0;
 	}
 
 	private void loseTheGame() {
 		mScoreOfCompetitor = 0;
 		mScoreOfMine = 0;
 	}
 
 	private void moveBallToCompetitor(float x, float y, float speedX,
 			float speedY) {
 		if (y <= 0 - mBall.getRadius() * 2) {
 			mBall.setCenterY(0 - mBall.getRadius() * 2 - 1);
 			mBall.setSpeed(0, 0);
 			if (mBallCallback != null) {
 				mBallCallback.onBallOut(x, speedX, speedY);
 			}
 		}
 	}
 
 	public void ballComesFromCompetitor(float x, float speedX, float speedY) {
 		Log.d(TAG, "ballComesFromCompetitor x = " + x + ", speedX = " + speedX
 				+ ", speedY = " + speedY);
 		mBall.setCenterX(x);
 		mBall.setCenterY(0 - mBall.getRadius() * 2);
 		mBall.setSpeed(speedX, speedY);
 	}
 
 	private void startMove() {
 		mStopBallMove = false;
 		Thread thread = new Thread() {
 
 			public void run() {
 				mIsBallMoving = true;
 				while (!mStopBallMove && mBall.getSpeedX() != 0
 						&& mBall.getSpeedY() != 0) {
 					moveBall();
 				}
 				mIsBallMoving = false;
 			};
 		};
 		thread.start();
 	}
 
 	public void setBallCallback(BallCallback callback) {
 		mBallCallback = callback;
 	}
 
 	public interface BallCallback {
 		void onBallOut(float x, float speedX, float speedY);
 
 		void onLoseOneGoal();
 	}
 }
