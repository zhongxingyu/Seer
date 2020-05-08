 /*
  * Copyright (C) 2012-2013 Martin Steiger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package terrain;
 
 import static terrain.TerrainType.UNDEFINED;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import tiles.HexTileSet;
 import tiles.TileSet;
 
 import com.google.common.base.Optional;
 import common.GridData;
 
 import dirs.OctDirection;
 
 /**
  * TODO Type description
  * @author Martin Steiger
  */
 public class HexTerrainModel implements TerrainModel
 {
 	private final GridData<Tile> tiles;
 
 	private final HexTileSet tileSet;
 
 	private int mapWidth;
 	private int mapHeight;
 
 
 	/**
 	 * @param data the terrain data
 	 */
 	public HexTerrainModel(GridData<TerrainType> terrainData, HexTileSet tileSet)
 	{
 		mapWidth = terrainData.getWidth();
 		mapHeight = terrainData.getHeight();
 
 		this.tiles = new GridData<Tile>(mapWidth, mapHeight, null);
 		this.tileSet = tileSet;
 
 		for (int y = 0; y < mapHeight; y++)
 		{
 			for (int x = 0; x < mapWidth; x++)
 			{
 				TerrainType terrain = terrainData.getData(x, y);
 				Tile tile = new Tile(x, y, terrain);
 				tiles.setData(x, y, tile);
 			}
 		}
 		
 	}
 
 	private static boolean isOdd(int v)
 	{
 		return v % 2 == 1;
 	}
 
 	@Override
 	public int getMapHeight()
 	{
 		return mapHeight;
 	}
 
 	@Override
 	public int getMapWidth()
 	{
 		return mapWidth;
 	}	
 	
 	@Override
 	public Tile getTile(int x, int y)
 	{
 		return tiles.getData(x, y);
 	}
 
 	@Override
 	public int getWorldX(int x, int y)
 	{
 		return x * (tileSet.getTopLength() + tileSet.getTileWidth()) / 2;
 	}
 
 	@Override
 	public int getWorldY(int x, int y)
 	{
 		return y * tileSet.getTileHeight() + (x % 2) * tileSet.getTileHeight() / 2;
 	}
 	
 	@Override
 	public Optional<Tile> getTileAtWorldPos(int x, int y)
 	{
		double w = tileSet.getTileWidth();
		double h = tileSet.getTileHeight();

		double a = w/2;
		double b = h/2;
		
 		double c = tileSet.getTopLength() / 2.0;
 		
 		x -= tileSet.getTileWidth() / 2;
 		y -= tileSet.getTileHeight() / 2;
 		
 		// Find out which major row and column we are on:
 	    int row = (int)(y / b);
 	    int col = (int)(x / (a + c));
 	 
 	    // Compute the offset into these row and column:
 	    double dy = y - row * b;
 	    double dx = x - col * (a + c);
 	 
 	    // Are we on the left of the hexagon edge, or on the right?
 	    if (((row ^ col) & 1) == 0)
 	        dy = b - dy;
 	    int right = dy * (a - c) < b * (dx - c) ? 1 : 0;
 	 
 	    // Now we have all the information we need, just fine-tune row and column.
 	    row += (col ^ row ^ right) & 1;
 	    col += right;
 		
 		if (col >= 0 && col < mapWidth &&
			row >= 0 && row < mapHeight)
 
 		{
			return Optional.of(getTile(col, row));
 		}
 		
 		return Optional.absent();
 	}
 
 	@Override
 	public List<Tile> getTilesInRect(int worldX0, int worldY0, int worldX1, int worldY1)
 	{
 		int tileHeight = tileSet.getTileHeight();
 		
 		int avgWidth = (tileSet.getTopLength() + tileSet.getTileWidth()) / 2;
 		
 		// this computes the map-y based on rectangular shapes 
 		// it is then independent of x - basically the inverse of getWorldY()
 		int y0 = worldY0 / tileHeight - 1;
 		int y1 = worldY1 / tileHeight;
 		int x0 = worldX0 / avgWidth;
 		int x1 = worldX1 / avgWidth;
 		
 		// Restrict to map bounds
 		int minY = Math.max(y0, 0);
 		int maxY = Math.min(y1, mapHeight - 1);
 		int minX = Math.max(x0, 0);
 		int maxX = Math.min(x1, mapWidth - 1);
 
 		List<Tile> result = new ArrayList<Tile>();
 
 		for (int y = minY; y <= maxY; y++)
 		{
 			for (int x = minX; x <= maxX; x++)
 			{
 				result.add(getTile(x, y));
 			}
 		}
 
 		return result;
 	}
  
 	@Override
 	public Map<OctDirection, TerrainType> getNeighbors(int mapX, int mapY)
 	{
 		Map<OctDirection, TerrainType> pattern = new HashMap<OctDirection, TerrainType>();
 		
 		for (OctDirection dir : OctDirection.values())
 		{
 			TerrainType neigh = getNeighborFor(mapX, mapY, dir);
 			
 			if (neigh != UNDEFINED)
 				pattern.put(dir, neigh);
 		}
 
 		return pattern;
 	}
 	
 	/**
 	 * @param x the x coord
 	 * @param y the y coord
 	 * @return the type or UNDEFINED if x or y are invalid
 	 */
 	private TerrainType getTerrain(int x, int y)
 	{
 		// x and y are often invalid map coords.
 		if (x < 0 || x >= getMapWidth())
 			return UNDEFINED;
 	
 		if (y < 0 || y >= getMapHeight())
 			return UNDEFINED;
 		
 		return getTile(x,y).getTerrain();
 	}
 
 	private TerrainType getNeighborFor(int x, int y, OctDirection dir)
 	{
 		switch (dir)
 		{
 		case NORTH:
 			return getTerrain(x, y - 1);
 
 		case SOUTH:
 			return getTerrain(x, y + 1);
 			
 		case NORTH_EAST:
 			if (isOdd(y)) 
 				return getTerrain(x + 1, y); else 
 				return getTerrain(x + 1, y - 1);
 			
 		case NORTH_WEST:
 			if (isOdd(y))
 				return getTerrain(x - 1, y); else
 				return getTerrain(x - 1, y - 1);
 			
 		case SOUTH_EAST:
 			if (isOdd(y))
 				return getTerrain(x + 1, y + 1); else
 				return getTerrain(x + 1, y);
 			
 		case SOUTH_WEST:
 			if (isOdd(y))
 				return getTerrain(x - 1, y + 1); else
 				return getTerrain(x - 1, y);
 			
 		case EAST:
 			return UNDEFINED;
 			
 		case WEST:
 			return UNDEFINED;
 		}
 		
 		return UNDEFINED;
 	}
 	
 }
