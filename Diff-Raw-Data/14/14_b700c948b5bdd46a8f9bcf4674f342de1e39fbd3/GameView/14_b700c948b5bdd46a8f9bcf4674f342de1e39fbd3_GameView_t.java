 package com.dreamlink.game.kickball;
 
 import com.dreamlink.game.kickball.module.Ball;
 import com.dreamlink.game.kickball.module.Engine;
 import com.dreamlink.game.kickball.module.Player;
 import com.dreamlink.game.kickball.util.DisplayUtil;
 import com.dreamlink.game.kickball.util.DrawTextUtil;
 import com.dreamlink.game.kickball.util.Log;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	private static final String TAG = "GameView";
 
 	private SurfaceHolder mSurfaceHolder;
 	private Paint mPaint;
 
 	private Paint mTextPaint;
 
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
 	private String mWaitingForPlayerString = getResources().getString(
 			R.string.waiting_for_player);
 	// Ball
 	private Ball mBall;
 	private Player mPlayer;
 
 	// Score
 	private static final int SCORE_WIN = 7;
 	private int mScoreOfCompetitor = 0;
 	private int mScoreOfMine = 0;
 	private boolean mDrawScoreRead = false;
 
 	// Check the ball is clicked or not.
 	private boolean mIsBallTouched = false;
 	// Last touch time, it is use for calculate speed.
 	private long mLastTouchedMovingTime;
 
 	private Context mContext;
 	private BallCallback mCallback;
 
 	private float mScreenWidth;
 	private float mScreenHeight;
 
 	private DrawThread mDrawThread;
 
 	private boolean mStopBallMove = false;
 	private boolean mIsBallMoving = false;
 
 	private boolean mIsGameOver = false;
 	private boolean mIsCompetitorJoined = false;
 
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
 
 		mTextPaint = new Paint();
 		mTextPaint.setAntiAlias(true);
 		mTextPaint.setFilterBitmap(true);
 		mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
 		mTextPaint.setTextSize(mScreenWidth * 2 / 3
 				/ mWaitingForPlayerString.length());
 		mTextPaint.setTextAlign(Paint.Align.CENTER);
 		
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
 
	public void startGame(boolean holdTheBall, boolean isCompetitorJoined) {
		mIsCompetitorJoined = isCompetitorJoined;
 		resetGame(holdTheBall);
 		// TODO maybe show start animation.
 	}
 
 	public void resetGame(boolean holdTheBall) {
 		resetBallAndPlayer();
 		mScoreOfCompetitor = 0;
 		mScoreOfMine = 0;
 		mIsGameOver = false;
 		if (!holdTheBall) {
 			// Move the ball to a position we can not see.
 			mBall.setCenterX(0);
 			mBall.setCenterY(0 - mBall.getRadius() * 2);
 		}
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
 		if (!mIsCompetitorJoined) {
 			drawWaitForPlayerNotice(canvas);
 		}
 
 		if (!mIsGameOver) {
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
 	}
 
 	private void drawWaitForPlayerNotice(Canvas canvas) {
 		canvas.drawText(mWaitingForPlayerString, mScreenWidth / 2,
 				DrawTextUtil.getFontHeight(mTextPaint), mTextPaint);
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
 			// Change the ball's speedX direction;
 			mBall.setSpeed(mBall.getSpeedX() * -1, mBall.getSpeedY());
 		}
 
 		float goalLeft = mScreenWidth / 2 - mGoalBitmap.getWidth() / 2;
 		float goalRight = mScreenWidth / 2 + mGoalBitmap.getWidth() / 2;
 		if (mBall.getCenterY() + mBall.getRadius() >= mScreenHeight
 				&& (mBall.getCenterX() <= goalLeft || mBall.getCenterX() >= goalRight)) {
 			// Ball is coming into collision with with screen bottom and does
 			// not in the goal. Change the ball's speedY direction;
 			mBall.setSpeed(mBall.getSpeedX(), mBall.getSpeedY() * -1);
 		} else if (mBall.getCenterY() + mBall.getRadius() >= mScreenHeight
 				&& mBall.getCenterX() > goalLeft
 				&& mBall.getCenterX() < goalRight) {
 			// Ball is into the goal, we lose one point.
 			loseOneGoal();
 		} else if (mBall.getCenterY() - mBall.getRadius() <= 0
 				&& mBall.getSpeedY() < 0 && mIsCompetitorJoined) {
 			// Ball is moving out into the competitor's screen.
 			moveBallToCompetitor(mBall.getCenterX(), mBall.getCenterY(),
 					mBall.getSpeedX(), mBall.getSpeedY());
 		} else if (!mIsCompetitorJoined
 				&& mBall.getCenterY() - mBall.getRadius() <= 0) {
 			// Competitor is not joined, play with myself, so do not let ball
 			// out of screen.
 			mBall.setSpeed(mBall.getSpeedX(), mBall.getSpeedY() * -1);
 		}
 		mBall.setCenterX(mBall.getCenterX() + mBall.getSpeedX());
 		mBall.setCenterY(mBall.getCenterY() + mBall.getSpeedY());
 
 	}
 
 	private void loseOneGoal() {
 		Log.d(TAG, "loseOneGoal");
 		resetBallAndPlayer();
 		mScoreOfCompetitor++;
 		if (mScoreOfCompetitor > SCORE_WIN) {
 			Log.d(TAG, "We lose.");
 			mScoreOfCompetitor = SCORE_WIN;
 			loseTheGame();
 		}
 		if (mCallback != null) {
 			mCallback.onLoseOneGoal();
 		}
 	}
 
 	/**
 	 * The competitor lose one goal. So we win one goal.
 	 */
 	public void winOneGoal() {
 		Log.d(TAG, "winOneGoal");
 		mScoreOfMine++;
 		if (mScoreOfMine > SCORE_WIN) {
 			Log.d(TAG, "We win.");
 			mScoreOfMine = SCORE_WIN;
 			winTheGame();
 		}
 	}
 
 	private void winTheGame() {
 		mIsGameOver = true;
 		if (mCallback != null) {
 			mCallback.onGameOver(GameOverView.GAME_OVER_WIN);
 		}
 	}
 
 	private void loseTheGame() {
 		mIsGameOver = true;
 		if (mCallback != null) {
 			mCallback.onGameOver(GameOverView.GAME_OVER_LOSE);
 		}
 	}
 
 	private void moveBallToCompetitor(float x, float y, float speedX,
 			float speedY) {
 		// When ball is moving out completely, send message to competitor.
 		if (y <= 0 - mBall.getRadius() * 2) {
 			// Stop ball moving and stay in position that we cannot see it.
 			mBall.setCenterY(0 - mBall.getRadius() * 2 - 1);
 			mBall.setSpeed(0, 0);
 			if (mIsBallMoving) {
 				stopMove();
 			}
 			if (mCallback != null) {
 				Log.d(TAG,
 						"moveBallToCompetitor() send message to competitor x = "
 								+ x + ", speedX = " + speedX + ", speedY = "
 								+ speedY);
 				mCallback.onBallOut(x / mScreenWidth, speedX / mScreenWidth,
 						speedY / mScreenHeight);
 			}
 		}
 	}
 
 	public void ballComesFromCompetitor(float xPercentInScreenWidth,
 			float speedXPercentInScreenWidth, float speedYPercentInScreenHeight) {
 		Log.d(TAG, "ballComesFromCompetitor x = " + xPercentInScreenWidth
 				+ ", speedX = " + speedXPercentInScreenWidth + ", speedY = "
 				+ speedYPercentInScreenHeight);
 		mBall.setCenterX(xPercentInScreenWidth * mScreenWidth);
 		mBall.setCenterY(0 - mBall.getRadius() * 2);
 		mBall.setSpeed(speedXPercentInScreenWidth * mScreenWidth,
 				speedYPercentInScreenHeight * mScreenHeight);
 		if (!mIsBallMoving) {
 			startMove();
 		}
 	}
 
 	private void stopMove() {
 		mStopBallMove = true;
 	}
 
 	private void startMove() {
 		mStopBallMove = false;
 		Thread thread = new Thread() {
 
 			public void run() {
 				mIsBallMoving = true;
 				while (!mStopBallMove && mBall.getSpeedX() != 0
 						&& mBall.getSpeedY() != 0) {
 					moveBall();
 					try {
 						Thread.sleep(20);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				mIsBallMoving = false;
 			};
 		};
 		thread.start();
 	}
 
 	public void setBallCallback(BallCallback callback) {
 		mCallback = callback;
 	}
 
 	/**
 	 * Be careful. These callbacks is not run in UI Thread.
 	 * 
 	 */
 	public interface BallCallback {
 		/**
 		 * The ball is moving out to the competitor.
 		 * 
 		 * Because the competitor screen size maybe different from us, so we
 		 * should use the relative value not the absolute value.
 		 * 
 		 * @param xPercentInScreenWidth
 		 * @param speedXPercentInScreenWidth
 		 * @param speedYPercentInScreenHeight
 		 */
 		void onBallOut(float xPercentInScreenWidth,
 				float speedXPercentInScreenWidth,
 				float speedYPercentInScreenHeight);
 
 		/**
 		 * We lose one goal.
 		 */
 		void onLoseOneGoal();
 
 		/**
 		 * The game is over.
 		 * 
 		 * @param result
 		 */
 		void onGameOver(int result);
 	}
 }
