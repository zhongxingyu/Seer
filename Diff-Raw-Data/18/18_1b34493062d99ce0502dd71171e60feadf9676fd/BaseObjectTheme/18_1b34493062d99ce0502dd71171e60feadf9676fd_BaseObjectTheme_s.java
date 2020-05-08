 package com.android.icecave.guiLogic.theme;
 
import com.android.icecave.general.GeneralServiceProvider;

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
	public Point getRandomTilePosition()
 	{
 		// Return a random tile location
		return mTileLocations[GeneralServiceProvider.
		                      getInstance().
		                      getRandom().
		                      nextInt(mTileLocations.length - 1)];
 	}
 }
