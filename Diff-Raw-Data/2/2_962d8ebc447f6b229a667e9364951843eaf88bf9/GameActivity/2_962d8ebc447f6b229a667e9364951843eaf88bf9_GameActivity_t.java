 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.graphics.Point;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import com.android.icecave.R;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.guiLogic.GUIBoardManager;
 import com.android.icecave.guiLogic.PlayerGUIManager;
 import com.android.icecave.guiLogic.TileImageView;
 import com.android.icecave.mapLogic.IIceCaveGameStatus;
 
 public class GameActivity extends Activity implements ISwipeDetector
 {
 	private static GUIBoardManager sGBM;
 	private PlayerGUIManager mPGM;
 	private Point mPlayerPosition;
 	private TileImageView mPlayer;
 
 	private final String POSITION_X = "posX";
 	private final String POSITION_Y = "posY";
 	private TableLayout mTilesTable;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.tiles_layout);
 
 		// Hide the Status Bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		mTilesTable = (TableLayout) findViewById(R.id.tilesTable);
 
 		// Register swipe events to the layout
 		mTilesTable.setOnTouchListener(new ActivitySwipeDetector(this));
 		
 		// Create the first row if none exist
 		if (mTilesTable.getChildCount() == 0)
 		{
 			createRows();
 		}
 
 		// Set up player position
 		if (savedInstanceState != null)
 		{
 			mPlayerPosition =
 					new Point(savedInstanceState.getInt(POSITION_X), savedInstanceState.getInt(POSITION_Y));
 		} else
 		{
 			mPlayerPosition = new Point(Consts.DEFAULT_START_POS);
 		}
 
 		// Create player
 		if (getIntent().getExtras() != null)
 		{
 			mPGM =
 					new PlayerGUIManager(getResources().getDrawable((Integer) getIntent().getExtras()
 							.get(Consts.PLAYER_SELECT_TAG)));
 		}
 
 	}
 
 	public int getHeight()
 	{
 		return mTilesTable.getBottom();
 	}
 
 	public int getWidth()
 	{
 		return mTilesTable.getWidth();
 	}
 
 	/***
 	 * Add the next tile to the table layout
 	 * 
 	 * @param tile
 	 *            Tile to add
 	 */
 	public void addNextTileToView(TileImageView tile)
 	{
 		// Add current tile to the row that matches its index
		((TableRow) mTilesTable.findViewById(tile.getCol())).addView(tile);
 	}
 	
 	private void createRows() {
 		// Create all rows by the value of board size rows
 		// TODO Change const value to an input
 		for (int i = 0; i < Consts.DEFAULT_BOARD_SIZE_Y; i++)
 		{
 			// Create new row and set its Id as the value of its index
 			TableRow newRow = new TableRow(this);
 			newRow.setId(i);
 			mTilesTable.addView(newRow);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 
 		// Put position data
 		outState.putInt(POSITION_X, mPlayerPosition.x);
 		outState.putInt(POSITION_Y, mPlayerPosition.y);
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
 		mPlayer = mPGM.getPlayerImage(mPlayerPosition.x, mPlayerPosition.y, direction, true);
 
 		IIceCaveGameStatus iceCaveGameStatus = sGBM.movePlayer(direction);
 
 		// TODO: Sagie make the animation.
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus)
 	{
 		// Create new stage here to make sure layout is made and active and visible
 		if (sGBM == null)
 		{
 			// Create once
 			sGBM = new GUIBoardManager();
 
 			// Initialize the game board & shit
 			// TODO Change const value to an input
 			sGBM.startNewGame(Consts.DEFAULT_BOULDER_NUM,
 					Consts.DEFAULT_BOARD_SIZE_X,
 					Consts.DEFAULT_BOARD_SIZE_Y,
 					EDifficulty.values()[(Integer) getIntent().getExtras().get(Consts.LEVEL_SELECT_TAG)]);
 
 			// Create first stage
 			sGBM.newStage(Consts.DEFAULT_START_POS, Consts.DEFAULT_WALL_WIDTH, this);
 		}
 
 		super.onWindowFocusChanged(hasFocus);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		// Reset variable
 		sGBM = null;
 		super.onDestroy();
 	}
 }
