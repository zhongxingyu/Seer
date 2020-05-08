 package com.android.icecave.guiLogic.tiles;
 
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import com.android.icecave.utils.Point;
 import com.android.icecave.guiLogic.ITileScale;
 import com.android.icecave.mapLogic.tiles.ITile;
 
 public abstract class BaseTileGUIWorker implements IGUITileWorker
 {
 	/**
 	 * Get the tile point in the sprite.
 	 * @return
 	 */
 	public abstract Point getTilePointInSprite();
 
 	@Override
 	public Bitmap makeTile(ITileScale scale, 
 	                       Bitmap theme, 
 	                       int themeRows,
						   int themeCol,
						   ITile[] tiles)
 	{
 		throw new UnsupportedOperationException(); 
 	}
 	
 	@Override
 	public Bitmap makeTile(ITileScale scaler, 
 	                       Bitmap theme, 
 	                       int themeRows, 
	                       int themeCols)
 	{
 		// Get the width and height of each tile
 		int width = theme.getWidth() / themeCols;
 		int height = theme.getHeight() / themeRows;
 
 		// Crop theme bitmap to select the specific tile wanted
 		Bitmap croppedTile =
 				Bitmap.createBitmap(theme, width * getTilePointInSprite().x, height *
 						getTilePointInSprite().y, width, height);
 
 		// Create matrix to change the scale of the bitmap to fit the screen
 		Matrix matrix = new Matrix();
 		matrix.postScale((float) scaler.getTileWidth() / width, (float) scaler.getTileHeight() / height);
 
 		// Resize
 		croppedTile = Bitmap.createBitmap(croppedTile, 0, 0, width, height, matrix, true);
 
 		return croppedTile;
 	}
 }
