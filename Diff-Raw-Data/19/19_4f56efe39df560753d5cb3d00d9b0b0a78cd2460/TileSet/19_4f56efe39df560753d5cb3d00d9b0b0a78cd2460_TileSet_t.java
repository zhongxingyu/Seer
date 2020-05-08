 /*
  * TileSet.java
  *
  * Copyright (C) 2012 Matt Falkoski
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.falko.android.snowball.core.zoneloder;
 
 import java.util.ArrayList;
 
 import com.falko.android.snowball.core.BaseObject;
 import com.falko.android.snowball.core.graphics.Texture;
 import com.falko.android.snowball.core.graphics.TextureLibrary;
 import com.falko.android.snowball.utility.FixedSizeArray;
 
 public class TileSet extends BaseObject{
 
 	public TileSet() {
 		sheets_ = new FixedSizeArray<Sheet>(3);
 	}
 	
 	@Override
 	public void reset() {
 	}
 
 	public int GIDToUV(long GID, float[][] uv, Sheet[] sheets) {
 
 		Sheet tileSheet = null;
 		long id = GID &= ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG);
 		int sheetIndex = getSheetIndex(GID);
 		tileSheet = sheets[sheetIndex];
 		
 		assert tileSheet != null;
 
 		final int tileHeight = tileSheet.tileHeight_;
 		final int tileWidth = tileSheet.tileWidth_;
 
 		final int pixelWidth = tileSheet.texture_.width;
 		final int pixelHeight = tileSheet.texture_.height;
 		final int width = pixelWidth / tileWidth;
 
 //		final boolean flippedHorizontally = (GID & FLIPPED_HORIZONTALLY_FLAG) > 1;
 //		final boolean flippedVertically = (GID & FLIPPED_VERTICALLY_FLAG) > 1;
 //		final boolean flippedDiagonally = (GID & FLIPPED_DIAGONALLY_FLAG) > 1;
 
 		long gid = id - tileSheet.firstGID_;// zero based
 		if (gid < 0) gid = 0;
 		final float y = (gid / width) * tileHeight;
 		final float x = (gid % width) * tileWidth;
 
		float[] uv0 = { (x + GL_MAGIC_OFFSET) / pixelWidth ,
						(y + GL_MAGIC_OFFSET) / pixelHeight};
		
		float[] uv1 = { (x + GL_MAGIC_OFFSET) / pixelWidth, 
						(y + tileHeight - GL_MAGIC_OFFSET) / pixelHeight};
		
		float[] uv2 = { (x + tileWidth - GL_MAGIC_OFFSET) / pixelWidth,
						(y + tileHeight - GL_MAGIC_OFFSET) / pixelHeight };
		
		float[] uv3 = { (x + tileWidth - GL_MAGIC_OFFSET) / pixelWidth,
						(y + GL_MAGIC_OFFSET) / pixelHeight };
 
 		uv[0] = uv0;
 		uv[1] = uv1;
 		uv[2] = uv2;
 		uv[3] = uv3;
 		
 		return sheetIndex;
 	}
 
 	public void addSheet(int firstGID, int tileWidth, int tileHeight,
 			int resourceID, int width, int height) {
 		TextureLibrary library = sSystemRegistry.shortTermTextureLibrary; 
 		Texture texture = library.allocateTexture(resourceID);
 		texture.width = width;
 		texture.height = height;
 		
 		Sheet sheet = new Sheet(firstGID, texture, tileWidth, tileHeight);
 		sheets_.add(sheet);
 	}
 	
 	private int getSheetIndex(long GID) {
 		long id = GID &= ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG);
 		int retVal = 0;
 
 		for (int i = 0; i < sheets_.getCount(); ++i) {	
 			if (i + 1 < sheets_.getCount()) {		
 				int fid = sheets_.get(i).firstGID_;
 				int eid = sheets_.get(i + 1).firstGID_;
 				
 				if (id >= fid && id < eid) {
 					retVal = i;
 					break;
 				}
 			} else {
 				retVal = i;
 				break;
 			}
 		}
 		return retVal;
 	}
 	
 	public Sheet[] getSheets(long[][] data) {
 		boolean[] totalShets = new boolean[sheets_.getCount()];
 		ArrayList<Sheet> list = new ArrayList<Sheet>();
 		for (int i = 0; i < data.length; ++i) {
 			for (int j = 0; j < data[i].length; ++j) {
 				totalShets[getSheetIndex(data[i][j])] = true;
 			}
 		}
 		
 		int retVal = 0;
 		for (int i = 0; i < totalShets.length; ++i) {
 			if (totalShets[i]) {
 				retVal++;
 				list.add(sheets_.get(i));
 				
 			}
 		}
 		Sheet[] sheets = new Sheet[retVal];
 		list.toArray(sheets);
 		return sheets;
 	}
 	
 	public int getTileWidth(long GID) {
 		return sheets_.get(getSheetIndex(GID)).tileWidth_;
 	}
 	
 	public int getTileHeight(long GID) {
 		return sheets_.get(getSheetIndex(GID)).tileHeight_;
 	}
 	
 	public Texture[] getTextures() {
 		Texture[] textures = new Texture[sheets_.getCount()];
 		for (int i = 0; i < textures.length; i++) {
 			textures[i] = sheets_.get(i).texture_;
 		}
 		return textures;
 	}
 	
	private static final float GL_MAGIC_OFFSET = 0.375f;
 	private FixedSizeArray<Sheet> sheets_;
 	
 	private static long FLIPPED_HORIZONTALLY_FLAG = 0x80000000;
 	private static long FLIPPED_VERTICALLY_FLAG = 0x40000000;
 	private static long FLIPPED_DIAGONALLY_FLAG = 0x20000000;
 
 	protected class Sheet {
 		
 		public Sheet(int firstGID, Texture texture, int tileWidth, int tileHeight) {
 			firstGID_ = firstGID;
 			texture_ = texture;
 			tileWidth_ = tileWidth;
 			tileHeight_ = tileHeight;
 		}
 		
 		public int firstGID_;
 		public Texture texture_;
 		public int tileWidth_;
 		public int tileHeight_;
 	}
 
 	
 }
