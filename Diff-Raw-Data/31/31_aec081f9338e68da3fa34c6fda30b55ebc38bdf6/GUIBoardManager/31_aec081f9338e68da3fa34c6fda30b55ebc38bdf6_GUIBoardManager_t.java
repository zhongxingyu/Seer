 package com.android.icecave.guiLogic;
 
 import android.content.Context;
 import android.graphics.Point;
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.mapLogic.IIceCaveGameStatus;
 import com.android.icecave.mapLogic.IceCaveGame;
 import com.android.icecave.mapLogic.tiles.ITile;
 
 /**
  * This class manages all GUI logic.
  * @author Tom
  *
  */
 public class GUIBoardManager
 {
 	private TileImageView[][] mTiles;
 	private IceCaveGame mIceCaveGame;
 	
 	/**
 	 * Start a new game.
 	 * @param boulderNum - Number of boulders to place on map. 
 	 * @param boardSizeX - Row length of the map.
 	 * @param boardSizeY - Column length of the map.
 	 * @param difficulty - Difficulty level.
 	 * @param Context - activity creating the game
 	 */
 	public void startNewGame( int 		 boulderNum, 
 						 	  int 		 boardSizeX,
 						 	  int 	     boardSizeY,
						 	  EDifficulty difficulty){
 		mIceCaveGame = new IceCaveGame(boulderNum, boardSizeX, boardSizeY, difficulty);
 		
 		// Get the tiles
 		mTiles = new TileImageView[boardSizeY][boardSizeX];
	}
	
	/**
	 * Start a new stage.
	 * @param playerStart - Starting location of the player.
	 * @param wallWidth - Width of the wall in tiles.
	 * @param context - Current activity context.
	 */
	public void newStage(Point playerStart, 
	                     int wallWidth,
	                     Context context){
		mIceCaveGame.newStage(playerStart, wallWidth);
 		
 		ITile[][] board = mIceCaveGame.getBoard();
 		
 		// Go through the game board.
		for (int yAxis = 0; yAxis < board.length; yAxis++) {
			for (int xAxis = 0; xAxis < board[0].length; xAxis++) {
 				mTiles[yAxis][xAxis] =
 						GUILogicServiceProvider.
 							getInstance().
 								getTileFactory().
 									getTiles(board[yAxis]
 												  [xAxis],
 												  context);
 			}
 		}
 	
 	}
 	
 	/**
 	 * Move the player on the board.
 	 * 
 	 * @param direction - Direction to move the player in.
 	 * 
 	 * @return new player point.
 	 */
 	public IIceCaveGameStatus movePlayer(EDirection direction){
 	
 		return mIceCaveGame.movePlayer(direction);
 	}
 }
