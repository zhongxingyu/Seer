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
	 * Get array of points to update on the board.
	 * @return Array of points.
 	 */
	Point[] getPointToUpdate();
 }
