 package com.tas.icecave.guiLogic;
 
import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import com.tas.icecave.gui.GameActivity;
 import com.tas.icecave.gui.GameTheme;
 import com.tas.icecave.gui.ILoadable;
 import com.tas.icecaveLibrary.general.Consts;
 import com.tas.icecaveLibrary.general.EDifficulty;
 import com.tas.icecaveLibrary.general.EDirection;
 import com.tas.icecaveLibrary.mapLogic.IIceCaveGameStatus;
 import com.tas.icecaveLibrary.mapLogic.IceCaveGame;
 import com.tas.icecaveLibrary.mapLogic.tiles.ITile;
 import com.tas.icecaveLibrary.utils.Point;
 import com.tas.icecaveLibrary.utils.bundle.BaseBundleMetaData;
 import com.tas.icecaveLibrary.utils.bundle.BundleHasher;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StreamCorruptedException;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * This class manages all GUI logic.
  * 
  * @author Tom
  * 
  */
 @SuppressWarnings("serial")
 public class GUIBoardManager implements Serializable, ILoadable
 {
 	private transient Bitmap[][] mTiles;
 	private IceCaveGame mIceCaveGame;
 	private final long HIDE_SHOW_TIME = 300;
 	private transient GameActivity mContext;
 	private int mBundleIndex;
 	private String[] mBundleFiles;
 	private String mBundleNameKey;
 	private transient LoadingThread mLoad;
 	private final int NO_KEY = -1;
 
 	public GUIBoardManager(GameActivity context)
 	{
 		mContext = context;
 	}
 
 	/**
 	 * Return the minimal moves for the current stage.
 	 * 
 	 * @return Minimal moves for stage.
 	 */
 	public int getMinimalMovesForStage()
 	{
 		return mIceCaveGame.getStageMoves();
 	}
 
 	/**
 	 * Resets the player location on the board.
 	 * 
 	 * @param startLoc
 	 *            - Starting location of the player to restart to.
 	 */
 	public void resetPlayer(Point startLoc)
 	{
 		mIceCaveGame.resetPlayer(startLoc);
 	}
 
 	/**
 	 * Return the overall moves for the current stage.
 	 * 
 	 * @return Moves player made in stage.
 	 */
 	public int getMovesCarriedOutThisStage()
 	{
 		return mIceCaveGame.getCurrentStageTakenMoves();
 	}
 
 	/**
 	 * Return the overall moves for the current game.
 	 * 
 	 * @return Overall moves in game.
 	 */
 	public int getOverAllMovesForGame()
 	{
 		return mIceCaveGame.getOverallMoves();
 	}
 
 	/**
 	 * Start a new game.
 	 * 
 	 * @param boulderNum
 	 *            - Number of boulders to place on map.
 	 * @param boardSizeHeight
 	 *            - Row length of the map.
 	 * @param difficulty
 	 *            - Difficulty level.
 	 * @param Context
 	 *            - activity creating the game
 	 */
 	public void startNewGame(int boardSizeHeight, EDifficulty difficulty)
 	{
 		// Make a square, always
 		int boardSizeWidth = boardSizeHeight;
 
 		mIceCaveGame =
 				new IceCaveGame(boardSizeHeight * boardSizeWidth / Consts.DEFAULT_BOULDER_RELATION,
 								boardSizeHeight,
 								boardSizeWidth,
 								difficulty);
 
 		// Get the tiles
 		mTiles = new Bitmap[boardSizeHeight][boardSizeWidth];
 
		String versionName = null;
		try
		{
			versionName =
					mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}

 		// Get the map bundle for the selected configurations
 		BaseBundleMetaData mData =
 				new BaseBundleMetaData(	Consts.DEFAULT_START_POS,
 										difficulty,
 										EDirection.RIGHT,
 										boardSizeHeight,
 										boardSizeWidth,
 										boardSizeHeight * boardSizeWidth / Consts.DEFAULT_BOULDER_RELATION,
										versionName,
 										Consts.DEFAULT_WALL_WIDTH);
 
 		try
 		{
 			BundleHasher hasher = new BundleHasher("md5");
 
 			// Get the list of files for the bundle
 			mBundleFiles =
 					mContext.getResources()
 							.getAssets()
 							.list(BundleHasher.hashToString(hasher.createMapBundleHash(mData)));
 
 			// Get the name of the hash and use it as the key for the index
 			mBundleNameKey = BundleHasher.hashToString(hasher.createMapBundleHash(mData));
 
 			// Attempt to get index of user progress in current difficulty
			mBundleIndex = mContext.getSharedPreferences(Consts.PREFS_FILE_TAG, 0).getInt(mBundleNameKey, NO_KEY);
 		} catch (NoSuchAlgorithmException e)
 		{
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Start a new stage.
 	 * 
 	 * @param mapFileStream
 	 *            - Map file stream.
 	 * @param context
 	 * @param gameTheme
 	 * @throws ClassNotFoundException
 	 * @throws IOException
 	 * @throws StreamCorruptedException
 	 */
 	public void newStage(InputStream mapFileStream, GameTheme gameTheme) throws StreamCorruptedException,
 			IOException,
 			ClassNotFoundException
 	{
 		mIceCaveGame.newStage(mapFileStream);
 
 		ITile[][] board = mIceCaveGame.getBoard();
 
 		// Get the tiles
 		mTiles = new Bitmap[board.length][board[0].length];
 
 		// Fill a square (by the smaller size of the screen in current orientation)
 		GUIScreenManager screenManager =
 				new GUIScreenManager(board[0].length, board.length, mContext.getWidth(), mContext.getWidth());
 
 		// Go through the game board.
 		for (int yAxis = 0; yAxis < board.length; yAxis++)
 		{
 			for (int xAxis = 0; xAxis < board[0].length; xAxis++)
 			{
 				mTiles[yAxis][xAxis] =
 						GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.getTiles(board[yAxis][xAxis], screenManager, gameTheme);
 			}
 		}
 	}
 
 	/**
 	 * Start a new stage.
 	 * 
 	 * @param playerStart
 	 *            - Starting location of the player.
 	 * @param wallWidth
 	 *            - Width of the wall in tiles.
 	 * @param context
 	 *            - Current activity context.
 	 */
 	public void newStage(Point playerStart, int wallWidth, GameTheme gameTheme)
 	{
 		mIceCaveGame.newStage(playerStart, wallWidth);
 
 		ITile[][] board = mIceCaveGame.getBoard();
 
 		// Fill a square (by the smaller size of the screen in current orientation)
 		GUIScreenManager screenManager =
 				new GUIScreenManager(board[0].length, board.length, mContext.getWidth(), mContext.getWidth());
 
 		// Go through the game board.
 		for (int yAxis = 0; yAxis < board.length; yAxis++)
 		{
 			for (int xAxis = 0; xAxis < board[0].length; xAxis++)
 			{
 				mTiles[yAxis][xAxis] =
 						GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.getTiles(board[yAxis][xAxis], screenManager, gameTheme);
 			}
 		}
 	}
 
 	/**
 	 * Get the tiles of the current board.
 	 * 
 	 * @return Map of the current board tiles.
 	 */
 	public Bitmap[][] getTiles()
 	{
 		return mTiles;
 	}
 
 	/**
 	 * Move the player on the board.
 	 * 
 	 * @param direction
 	 *            - Direction to move the player in.
 	 * 
 	 * @return new player point.
 	 */
 	public IIceCaveGameStatus movePlayer(EDirection direction)
 	{
 
 		return mIceCaveGame.movePlayer(direction);
 	}
 
 	@Override
 	public boolean isInitialLoading()
 	{
 		// If initialized, not initial loading.
 		return !mContext.isInitialized();
 	}
 
 	@Override
 	public long getAnimationDuration()
 	{
 		return HIDE_SHOW_TIME;
 	}
 
 	@Override
 	public GameTheme getGameTheme()
 	{
 		return mContext.getGameTheme();
 	}
 
 	@Override
 	public void onLoad()
 	{
 		// Start creating a new stage, cancel running one if running
 		mLoad =
 				new LoadingThread(	this,
 									Consts.DEFAULT_START_POS,
 									Consts.DEFAULT_WALL_WIDTH,
 									mContext,
 									getGameTheme());
 
		// Check if file exists for loading (make sure bundle index is not NO_KEY value)
		if (mBundleFiles != null && mBundleFiles.length > mBundleIndex && mBundleIndex != NO_KEY)
 		{
 			mLoad.loadStageFromFile(mBundleNameKey + "/" + mBundleFiles[mBundleIndex]);
 		}
 
 		mLoad.start();
 	}
 
 	/**
 	 * Save the stage index
 	 * @return Index of new stage. NO_KEY if not a loaded stage
 	 */
 	public int saveStageIndex()
 	{
 		int result = NO_KEY;
 		
 		// Check if file exists and if index is still within file range
 		if (mBundleFiles != null && mBundleFiles.length > mBundleIndex)
 		{
 			// Increase index
 			mBundleIndex++;
 			
 			// Set result
 			result = mBundleIndex;
 
 			// Set index of user progress in current difficulty
 			mContext.getSharedPreferences(Consts.PREFS_FILE_TAG, 0)
 					.edit()
 					.putInt(mBundleNameKey, mBundleIndex)
 					.commit();
 		}
 		
 		return result;
 	}
 	
	// Get the bunleIndex for the current difficulty
 	public int getBundleIndex() {
		return mBundleIndex;
 	}
 
 	public void resetMoves()
 	{
 		mIceCaveGame.resetMoves();
 	}
 }
