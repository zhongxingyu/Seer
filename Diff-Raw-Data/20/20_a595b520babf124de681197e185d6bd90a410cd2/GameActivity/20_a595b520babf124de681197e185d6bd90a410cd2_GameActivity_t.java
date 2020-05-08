 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.android.icecave.R;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.guiLogic.DrawablePlayer;
 import com.android.icecave.guiLogic.GUIBoardManager;
 import com.android.icecave.guiLogic.TilesView;
 import com.android.icecave.mapLogic.IIceCaveGameStatus;
 import java.util.Observable;
 import java.util.Observer;
 
 public class GameActivity extends Activity implements ISwipeDetector, Observer
 {
 	private static GUIBoardManager sGBM;
 	private DrawablePlayer mPlayer;
 	private GameTheme mGameTheme;
 	private TilesView mTilesView;
 	private RelativeLayout mActivityLayout;
 	private boolean mIsFlagReached;
 	private TextView mPlayerMoves, mMinimumMoves;
 	private ImageView mResetButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.tiles_layout);
 
 		mActivityLayout = ((RelativeLayout) findViewById(R.id.game_layout));
 		mPlayerMoves = (TextView) findViewById(R.id.player_moves);
 		mMinimumMoves = (TextView) findViewById(R.id.minimum_moves);
 		mResetButton = (ImageView) findViewById(R.id.reset_button);
 
 		// Hide the Status Bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		SharedPreferences mShared = getSharedPreferences(Consts.PREFS_FILE_TAG, 0);
 
 		mGameTheme =
 				new GameTheme(	BitmapFactory.decodeResource(getResources(),
 										(mShared.getInt(Consts.THEME_SELECT_TAG, Consts.DEFAULT_TILES))),
 								BitmapFactory.decodeResource(getResources(),
 										(mShared.getInt(Consts.PLAYER_SELECT_TAG, Consts.DEFAULT_PLAYER))));
 
 		// Set reset button effect
 		mResetButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				// Reset player position on logic level
				//sGBM. FIXME Complete
 				
 				// Re-initialize player on UI level
 				mPlayer.initializePlayer();
 				mPlayer.postInvalidate();
 			}
 		});
 	}
 
 	public int getHeight()
 	{
 		return mActivityLayout.getBottom();
 	}
 
 	public int getWidth()
 	{
 		return mActivityLayout.getWidth();
 	}
 
 	public TilesView getTilesView()
 	{
 		return mTilesView;
 	}
 
 	@Override
 	public void bottom2top(View v)
 	{
 		commitSwipe(EDirection.UP);
 	}
 
 	@Override
 	public void left2right(View v)
 	{
 		commitSwipe(EDirection.RIGHT);
 	}
 
 	@Override
 	public void right2left(View v)
 	{
 		commitSwipe(EDirection.LEFT);
 	}
 
 	@Override
 	public void top2bottom(View v)
 	{
 		commitSwipe(EDirection.DOWN);
 	}
 
 	/**
 	 * 
 	 */
 	private void commitSwipe(EDirection direction)
 	{
 		// Commit a swipe only if animation is not running
 		if (!mPlayer.isAnimationRunning())
 		{
 			// Get status
 			IIceCaveGameStatus iceCaveGameStatus = sGBM.movePlayer(direction);
 
 			// Set game ended value
 			mIsFlagReached = iceCaveGameStatus.getIsStageEnded();
 
 			// Make movement animation
 			mPlayer.movePlayer(direction, iceCaveGameStatus.getPlayerPoint());
 			mPlayer.invalidate();
 		}
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus)
 	{
 		// Create new stage here to make sure layout is made and active and
 		// visible
 		if (sGBM == null && !isFinishing())
 		{
 			// Create once
 			sGBM = new GUIBoardManager();
 
 			// Initialize flags (only the first time the activity is created)
 			mIsFlagReached = false;
 
 			// Initialize the game board & shit
 			sGBM.startNewGame(Consts.DEFAULT_BOULDER_NUM,
 					(Integer) getIntent().getExtras().get(Consts.SELECT_BOARD_SIZE_X),
 					(Integer) getIntent().getExtras().get(Consts.SELECT_BOARD_SIZE_Y),
 					EDifficulty.values()[(Integer) getIntent().getExtras().get(Consts.LEVEL_SELECT_TAG)]);
 
 			// Create first stage
 			sGBM.newStage(Consts.DEFAULT_START_POS, Consts.DEFAULT_WALL_WIDTH, this, mGameTheme);
 
 			setMinimumMoves();
 			setPlayerMoves();
 
 			// Create the tiles view and add it to the layout
 			mTilesView = new TilesView(this, sGBM.getTiles());
 			mTilesView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
 																	FrameLayout.LayoutParams.MATCH_PARENT));
 			mActivityLayout.addView(mTilesView);
 
 			// Create new player view
 			mPlayer = new DrawablePlayer(this, mGameTheme);
 			mPlayer.setLayoutParams(new FrameLayout.LayoutParams(	FrameLayout.LayoutParams.WRAP_CONTENT,
 																	FrameLayout.LayoutParams.WRAP_CONTENT));
 			mActivityLayout.addView(mPlayer);
 
 			// Register swipe events to the layout
 			mTilesView.setOnTouchListener(new ActivitySwipeDetector(this));
 
 			// Create player image and bring it to front
 			mPlayer.initializePlayer();
 			mPlayer.bringToFront();
 
 			super.onWindowFocusChanged(hasFocus);
 		}
 
 		// Resume drawing thread if was running
 		if (mPlayer != null)
 		{
 			mPlayer.resumeDrawingThread();
 		}
 	}
 
 	@Override
 	protected void onPause()
 	{
 		// Pause drawing thread if running
 		if (mPlayer != null)
 		{
 			mPlayer.pauseDrawingThread();
 		}
 
 		super.onPause();
 	}
 
 	// @Override
 	// protected void onResume()
 	// {
 	// // Resume drawing thread if was running
 	// if (mPlayer != null)
 	// {
 	// mPlayer.resumeDrawingThread();
 	// }
 	//
 	// super.onResume();
 	// }
 
 	@Override
 	public void onBackPressed()
 	{
 		// Stop thread if not stopped
 		if (mPlayer != null)
 		{
 			mPlayer.stopDrawingThread();
 		}
 		// Reset variable
 		sGBM = null;
 
 		super.onBackPressed();
 	}
 
 	@Override
 	public void update(Observable observable, Object data)
 	{
 		// Update counter
 		setPlayerMoves();
 
 		// Run a new game if flag is reached
 		if (mIsFlagReached)
 		{
 			// Reset reached flag
 			mIsFlagReached = false;
 
 			// Create new stage
 			sGBM.newStage(Consts.DEFAULT_START_POS, Consts.DEFAULT_WALL_WIDTH, this, mGameTheme);
 
 			// Reset move texts
 			setMinimumMoves();
 			setPlayerMoves();
 
 			// Re-initialize player
 			mPlayer.initializePlayer();
 			mPlayer.postInvalidate();
 		}
 	}
 
 	private void setPlayerMoves()
 	{
 		// Must run this update on UI thread because it may be called from the update() function
 		runOnUiThread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Set player moves value
 				mPlayerMoves.setText(getString(R.string.player_moves_text) + " " +
 						Integer.toString(sGBM.getMovesCarriedOutThisStage()));
 			}
 		});
 	}
 
 	private void setMinimumMoves()
 	{
 		// Must run this update on UI thread because it may be called from the update() function
 		runOnUiThread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Set minimum moves value
 				mMinimumMoves.setText(getString(R.string.minimum_moves_text) + " " +
 						Integer.toString(sGBM.getMinimalMovesForStage()));
 			}
 		});
 	}
 
 	// Draws tile background and other widgets on screen
 	public void drawBackground(Canvas canvas)
 	{
 		mTilesView.draw(canvas);
 
 		// Display text fields
 		mMinimumMoves.bringToFront();
 		mPlayerMoves.bringToFront();
 		mResetButton.bringToFront();
 	}
 }
