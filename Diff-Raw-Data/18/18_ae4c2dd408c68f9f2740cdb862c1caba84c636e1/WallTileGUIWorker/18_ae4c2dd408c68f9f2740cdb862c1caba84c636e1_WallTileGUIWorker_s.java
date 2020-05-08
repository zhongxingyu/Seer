 package com.android.icecave.guiLogic.tiles;
 
import android.graphics.Bitmap;

import com.android.icecave.utils.Point;
 import com.android.icecave.general.Consts;
import com.android.icecave.guiLogic.ITileScale;
 
 public class WallTileGUIWorker extends BaseTileGUIWorker
 {
 	@Override
 	public Point getTilePointInSprite()
 	{
 		return Consts.WALL_TILE_IN_SPRITE;
 	}
 }
