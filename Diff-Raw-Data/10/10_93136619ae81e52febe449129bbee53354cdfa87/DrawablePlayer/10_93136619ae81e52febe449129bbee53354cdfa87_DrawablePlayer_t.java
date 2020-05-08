 package com.android.icecave.guiLogic;
 
 import android.util.Log;
 
 import android.R.color;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.gui.GameActivity;
 import com.android.icecave.gui.GameTheme;
 import com.android.icecave.utils.AutoObservable;
 import java.lang.Thread.State;
 
 public class DrawablePlayer extends SurfaceView implements Callback
 {
 	private CanvasThread mCanvasThread;
 	private Bitmap mPlayerImage;
 	private PlayerGUIManager mPGM;
 	private Point mPlayerPosition;
 	private Point mPlayerPositionOnScreen;
 	private Point mPlayerNewPositionOnScreen;
 	private EDirection mDirection;
 	private GameTheme mGameTheme;
 	private int mHeight;
 	private int mWidth;
 	private GameActivity mContext;
 	private GUIScreenManager mScreenManager;
 	private boolean mSurfaceCreated = false;
 	private boolean mThreadRunning;
 	private boolean mPauseThreadFlag;
 	final float mSpeed = 5;
 	public AutoObservable mFinishAnimation;
 
 	public DrawablePlayer(Context context)
 	{
 		super(context);
 	}
 
 	public DrawablePlayer(Context context, AttributeSet attSet)
 	{
 		super(context, attSet);
 	}
 
 	public DrawablePlayer(Context context, GameTheme gameTheme)
 	{
 		super(context);
 
 		// Initialize data
 		mContext = (GameActivity) context;
 		this.getHolder().addCallback(this);
 		this.setBackgroundColor(color.transparent);
 		mFinishAnimation = new AutoObservable();
 		mPauseThreadFlag = false;
 
 		// Add game activity as an observer
 		mFinishAnimation.addObserver(mContext);
 
 		// Set player theme in manager
 		mPGM = new PlayerGUIManager();
 		mGameTheme = gameTheme;
 
 		// Set tile rows and columns into scale manager to fit the character into the tiles
 		mScreenManager =
 				new GUIScreenManager(	mContext.getTilesView().getBoardX(),
 										mContext.getTilesView().getBoardY(),
 										mContext.getWidth(),
 										mContext.getHeight());
 
 		// Get height and width of player image
 		mPlayerImage = mPGM.getPlayerImage(EDirection.DOWN, mThreadRunning, mGameTheme, mScreenManager);
 		mHeight = mPlayerImage.getHeight();
 		mWidth = mPlayerImage.getWidth();
 
 	}
 
 	public void initializePlayer()
 	{
 		// Initialize thread data
 		mThreadRunning = false;
 		mCanvasThread = new CanvasThread(getHolder());
 
 		// Init image (draw on the player's current position)
 		mPlayerImage = mPGM.getPlayerImage(EDirection.DOWN, mThreadRunning, mGameTheme, mScreenManager);
 
 		// Initialize new position as player position and draw
 		mPlayerPosition = new Point(Consts.DEFAULT_START_POS);
 		mPlayerPositionOnScreen = new Point(mPlayerPosition.x * mWidth, mPlayerPosition.y * mHeight);
 		mPlayerNewPositionOnScreen = new Point(mPlayerPositionOnScreen);
 		startDrawImage(EDirection.DOWN, mPlayerPosition);
 	}
 
 	public void stopDrawingThread()
 	{
 		// Turn flag to false to ensure that thread has stopped
 		mThreadRunning = false;
 	}
 	
 	public boolean isAnimationRunning() {
 		return mThreadRunning;
 	}
 
 	public void pauseDrawingThread()
 	{
 		// Pause thread if running and game was paused (not quit by back press)
 		if (mCanvasThread != null && mCanvasThread.isAlive() && mThreadRunning)
 		{
 			synchronized (mCanvasThread)
 			{
 				// Turn the pause flag on
 				mPauseThreadFlag = true;
 
 				// Turn surface created flag off again, as it will be needed in the future
 				mSurfaceCreated = false;
 			}
 		}
 	}
 
 	public void resumeDrawingThread()
 	{
 		// Resume thread if waiting
 		if (mCanvasThread != null && mCanvasThread.getState() == State.WAITING)
 		{
 			synchronized (mCanvasThread)
 			{
 				// Turn the pause flag off
 				mPauseThreadFlag = false;
 
 				// Resume drawing thread
 				mCanvasThread.notify();
 			}
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
 	{
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0)
 	{
 		mSurfaceCreated = true;
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0)
 	{
 		// boolean retry = true;
 		// mThreadRunning = false;
 		//
 		// while (retry)
 		// {
 		// try
 		// {
 		// mCanvasThread.join();
 		// retry = false;
 		// } catch (InterruptedException e)
 		// {
 		// // TODO Print to log
 		// e.printStackTrace();
 		// }
 		// }
 	}
 
 	public void startDrawImage(EDirection direction, Point newPosition)
 	{
 		mPlayerNewPositionOnScreen = new Point(newPosition.x * mWidth, newPosition.y * mHeight);
 		mDirection = direction;
 		mThreadRunning = true;
 
 		// Run on a new thread or on the UI thread
 		mCanvasThread.start();
 	}
 
 	public void movePlayer(EDirection direction, Point newPosition)
 	{
 		// Moving player to the new position in the new direction
 		mCanvasThread = new CanvasThread(getHolder());
 		startDrawImage(direction, newPosition);
 	}
 
 	public void update()
 	{
 		// If reached new position, stop
 		if (mPlayerPositionOnScreen.x == mPlayerNewPositionOnScreen.x &&
 				mPlayerPositionOnScreen.y == mPlayerNewPositionOnScreen.y)
 		{
 			// Stop moving and stop thread
 			mThreadRunning = false;
 		} else
 		{
 			// Move to direction by speed
 			mPlayerPositionOnScreen.x += mDirection.getDirection().x * mSpeed;
 			mPlayerPositionOnScreen.y += mDirection.getDirection().y * mSpeed;
 
 			// Check that move does not exceed new position
 			// No more than maximum, no less than minimum
 			if ((mDirection.getDirection().x > 0 && mPlayerPositionOnScreen.x > mPlayerNewPositionOnScreen.x) ||
 					(mDirection.getDirection().x < 0 && mPlayerPositionOnScreen.x < mPlayerNewPositionOnScreen.x))
 			{
 				mPlayerPositionOnScreen.x = mPlayerNewPositionOnScreen.x;
 			}
 
 			if ((mDirection.getDirection().y > 0 && mPlayerPositionOnScreen.y > mPlayerNewPositionOnScreen.y) ||
 					(mDirection.getDirection().y < 0 && mPlayerPositionOnScreen.y < mPlayerNewPositionOnScreen.y))
 			{
 				mPlayerPositionOnScreen.y = mPlayerNewPositionOnScreen.y;
 			}
 
 			mThreadRunning = true;
 		}
 
 		// Get the next player image
 		mPlayerImage = mPGM.getPlayerImage(mDirection, mThreadRunning, mGameTheme, mScreenManager);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		// Draw background after every move. It's a must
 		mContext.getTilesView().draw(canvas);
 
 		canvas.drawBitmap(mPlayerImage, mPlayerPositionOnScreen.x, mPlayerPositionOnScreen.y, null);
 
 		if (mThreadRunning)
 		{
 			update();
 			postInvalidate();
 		}
 	}
 
 	private class CanvasThread extends Thread
 	{
 		private SurfaceHolder surfaceHolder;
		final int FRAMES_PER_SECOND = 35;
 		final int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
 		long mStartFrame;
 
 		private void pauseThread()
 		{
 			// Wait as long as pause flag is true
 			synchronized (this)
 			{
 				while (mPauseThreadFlag)
 				{
 					try
 					{
 						this.wait();
 					} catch (InterruptedException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 
 			// Wait till surface holder is created
 			while (!mSurfaceCreated)
 			{
 				try
 				{
 					Thread.sleep(1);
 				} catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public CanvasThread(SurfaceHolder holder)
 		{
 			this.surfaceHolder = holder;
 		}
 
 		@Override
 		public void run()
 		{
 			Canvas c;
 
 			// Run while running flag is true and also do NOT attempt to run while surface is not yet created.
 			// Keep looping if pause flag is up (this is so that the observer update won't happen during a pause)
			//while ((mThreadRunning && mSurfaceCreated) || (mPauseThreadFlag)) NOT NEEDED BECAUSE ONDRAW IS RECURSIVE!!!
//			{
 				// Pause thread if necessary
 				pauseThread();
 
 				c = null;
 
 				// Initialize FPS timer
 				mStartFrame = SystemClock.uptimeMillis();
 
 				try
 				{
 					c = this.surfaceHolder.lockCanvas(null);
 
 					synchronized (surfaceHolder)
 					{
 						//DrawablePlayer.this.update(); Char position is being updated through onDraw
 						DrawablePlayer.this.draw(c);
 					}
 
 					// Count FPS. Put thread to sleep if going too fast
 					final long endFrame = SystemClock.uptimeMillis();
 					final long sleepTime = SKIP_TICKS - (endFrame - mStartFrame);
 					
 					Log.d("FPS DEBUGGING", "Sleep time: " + sleepTime + "miliseconds");
 
 					// Sleep if needed
 					if (sleepTime >= 0)
 					{
 						try
 						{
 							if (sleepTime > 0)
 								Thread.sleep(sleepTime);
 							else
 								Thread.sleep(1);
 						} catch (InterruptedException e)
 						{
 							// TODO Print to log
 							e.printStackTrace();
 						} catch (IllegalArgumentException e)
 						{
 							// TODO Print to log
 							e.printStackTrace();
 						}
 					} else
 					{
 						System.out.println("Shit, we are running behind!");
 					}
 				} finally
 				{
 					// Only draw if canvas isn't null (can't do && mSurfaceCreate for some reason... won't resume)
 					if (c != null)
 					{
 						surfaceHolder.unlockCanvasAndPost(c);
 					}
 				}
//			}
 
 			// Notify observers that animation has finished
 			mFinishAnimation.notifyObservers();
 		}
 	}
 }
