 package com.android.icecave.guiLogic.theme;
 
 import com.android.icecave.utils.Point;
 
 /**
  * A basic implementation of the IObjectTheme.
  * @author Tom
  *
  */
 public abstract class BaseObjectTheme implements IObjectTheme
 {
 	/**
 	 * The tile locations.
 	 */
 	protected Point[] mTileLocations;
 	
 	@Override
	public Point[] getTilesPositions()
 	{
 		// Return a random tile location
		return mTileLocations;
 	}
 }
