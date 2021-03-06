 package com.android.icecave.mapLogic;
 
 import android.graphics.Point;
 
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.general.IFunction;
 import com.android.icecave.mapLogic.collision.BaseCollisionInvoker;
 import com.android.icecave.mapLogic.collision.CollisionManager;
 import com.android.icecave.mapLogic.collision.ICollisionable;
 import com.android.icecave.mapLogic.tiles.BoulderTile;
 import com.android.icecave.mapLogic.tiles.FlagTile;
 import com.android.icecave.mapLogic.tiles.ITile;
 import com.android.icecave.mapLogic.tiles.WallTile;
 
 public class IceCaveGame extends CollisionManager implements IIceCaveGameStatus
 {
 	private int mOverallMoves;
 	private int mCurrentStageMoves;
 	private EDirection mLastDirectionMoved;
 	private IceCaveStage mStage;
 	private Point mPlayerLocation;
 	private boolean mPlayerMoving;
 	private int mBoulderNum;
 	private int mBoardSizeX;
 	private int mBoardSizeY;
 	private EDifficulty mDifficulty;
 	private boolean mIsStageEnded;
 
 	/**
 	 * Create a new instance of the IceCaveGame object.
 	 * 
 	 * @param boulderNum
 	 *            - Number of boulders to place on board.
 	 * @param boardSizeX
 	 *            - Board width (in tiles).
 	 * @param boardSizeY
 	 *            - Board height (in tiles).
 	 * @param difficulty
 	 *            - Game difficulty.
 	 */
 	public IceCaveGame(int boulderNum, int boardSizeX, int boardSizeY, EDifficulty difficulty)
 	{
 		mBoulderNum = boulderNum;
 		mBoardSizeX = boardSizeX;
 		mBoardSizeY = boardSizeY;
 		mDifficulty = difficulty;
 		mIsStageEnded = false;
 		mStage = new IceCaveStage();
 		mPlayerLocation = new Point();
 
 		// Create invokers.
 		IFunction<Void> stopPlayer = new IFunction<Void>()
 		{
 			@Override
 			public Void invoke(Point collisionPoint)
 			{
 				mPlayerMoving = false;
 				collisionPoint.offset(
 						mLastDirectionMoved.getOpositeDirection().getDirection().x,
 						mLastDirectionMoved.getOpositeDirection().getDirection().y);
 				if(!collisionPoint.equals(mPlayerLocation.x, mPlayerLocation.y)){
 					mOverallMoves++;
 					mCurrentStageMoves++;
 					mPlayerLocation = collisionPoint;
 				}
 				
 				return null;
 			}
 		};
 
 		IFunction<Void> endStage = new IFunction<Void>()
 		{
 
 			@Override
 			public Void invoke(Point collisionPoint)
 			{
 				mPlayerMoving = false;
 				mIsStageEnded = true;
 				
 				mPlayerLocation = collisionPoint;
 				
 				mCurrentStageMoves++;
 				mOverallMoves++;
 				// TODO: Add report to the GUI logic on end stage.
 				return null;
 			}
 		};
 
 		// Add invokers.
 		mCollisionInvokers.put(BoulderTile.class, new BaseCollisionInvoker<Void>(stopPlayer));
 		mCollisionInvokers.put(WallTile.class, new BaseCollisionInvoker<Void>(stopPlayer));
 		mCollisionInvokers.put(FlagTile.class, new BaseCollisionInvoker<Void>(endStage));
 
 		MapLogicServiceProvider.getInstance().registerCollisionManager(this);
 	}
 
 	/**
 	 * Start a new stage.
 	 * 
 	 * @param playerStart
 	 *            - The starting position of the player.
 	 * @param wallWidth
 	 *            - Width of the walls in tiles.
 	 */
 	public void newStage(Point playerStart, int wallWidth)
 	{
 		mIsStageEnded = false;
 		mLastDirectionMoved = null;
 		mPlayerLocation = new Point(playerStart);
 		mStage.buildBoard(mDifficulty,
 				mBoardSizeX,
 				mBoardSizeY,
 				wallWidth,
 				new Point(playerStart),
 				mBoulderNum,
 				EDirection.RIGHT);
 		mCurrentStageMoves = 0;
 	}
 
 	/**
 	 * Get the number of moves that were done in the current stage.
 	 * 
 	 * @return Number of moves in current stage.
 	 */
 	public int getStageMoves()
 	{
 		return mStage.getMoves();
 	}
 
 	/**
 	 * Move the player on the board.
 	 * 
 	 * @param direction
 	 *            - Direction to move the player in.
 	 * @return Point - New location of the player.
 	 */
 	public IIceCaveGameStatus movePlayer(EDirection direction)
 	{
 		// Check if the requested direction is the last direction moved.
 		if (!direction.equals(mLastDirectionMoved))
 		{
 			// Set last move
 			mLastDirectionMoved = direction;
 			
 			// Start moving.
 			mPlayerMoving = true;
 
 			Point nextPlayerPoint = mStage.movePlayerOneTile(mPlayerLocation, direction);
 
 			// While we are moving.
 			while (mPlayerMoving)
 			{
 				nextPlayerPoint = mStage.movePlayerOneTile(nextPlayerPoint, direction);
 			}
 		}
 
 		return this;
 	}
 
 	/**
 	 * Return overall moves taken in game.
 	 * 
 	 * @return Overall moves.
 	 */
 	public int getOverallMoves()
 	{
 		return mOverallMoves;
 	}
 	
 	/**
 	 * Return number of moves taken in the current stage.
 	 * 
 	 * @return Steps in current stage.
 	 */
 	public int getCurrentStageTakenMoves()
 	{
 		return mCurrentStageMoves;
 	}
 
 	/**
 	 * Get the current board of the stage.
 	 * 
 	 * @return Board of the stage.
 	 */
 	public ITile[][] getBoard()
 	{
 		return mStage.getBoard();
 	}
 
 	@Override
 	public void handleCollision(ICollisionable collisionable)
 	{
 		if (mCollisionInvokers.containsKey(collisionable.getClass()))
 		{
 			mCollisionInvokers.get(collisionable.getClass()).
 								   onCollision(collisionable.getLocation());
 		}
 	}
 
 	@Override
 	public Point getPlayerPoint()
 	{
 		return mPlayerLocation;
 	}
 
 	@Override
 	public boolean getIsStageEnded()
 	{
 		return mIsStageEnded;
 	}
 
 	/**
 	 * Resets the player location.
 	 * @param startLoc - Starting position of the player to reset to.
 	 */
 	public void resetPlayer(Point startLoc)
 	{
 		mPlayerLocation = new Point(startLoc);
 	}
 }
