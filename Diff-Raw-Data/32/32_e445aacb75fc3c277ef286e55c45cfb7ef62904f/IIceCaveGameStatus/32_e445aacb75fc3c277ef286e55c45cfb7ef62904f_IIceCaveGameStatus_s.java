 package com.tas.icecaveLibrary.mapLogic;
 
 import com.tas.icecaveLibrary.utils.Point;
 
 /**
  * Interface for reporting on the current IceCaveGameStatus.
  * @author Tom
  *
  */
 public interface IIceCaveGameStatus
 {
 	/**
 	 * Get the current player point.
 	 * @return Get the player point.
 	 */
 	Point getPlayerPoint();
 	
 	/**
 	 * Get indication to whether or not this game has ended. 
 	 * @return true if the game has ended.
 	 */
 	boolean getIsStageEnded();
 
 	/**
	 * Get indication to whether the board has changed.
	 * @return true if should update the board.
 	 */
	boolean getBoardChanged();
 }
