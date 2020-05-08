 /*
     GridRover -- A game to teach programming skills
     Copyright (C) 2008  "Lucas" Adam M. Paul
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 package gridrover;
 import java.util.ArrayList;
 
 /**
 * The MapGrid represents a complete 2D map of a mission zone.
 *
 * @author Lucas Adam M. Paul
 * @version 0.0.0
 */
 public class MapGrid
 {
 	private MapSquare grid[][];
 
 	/**
 	* Makes a new MapGrid with specified width and length.
 	*
 	* @param width East-West width of map
 	* @param length North-South length of map
 	*/
 	protected MapGrid(int width, int length)
 	{
 		grid = new MapSquare[width][length];
 		fillWithBlank();
 	}
 
 	/**
 	* Random constructor for MapGrid.  Creates a map with squares of random
 	* elevations.  Map will have the specified width and length.  Squares
 	* will not exceed maxElevation in elevation, and elevations will be rounded
 	* to the precision'th digit.  Elevations will also not be negative.
 	*
 	* @param width East-West width of map
 	* @param length North-South length of map
 	* @param maxElevation Upper bound on elevation
 	* @param precision Round elevations to this place
 	*/
 	protected MapGrid(int width, int length, double maxElevation, int precision)
 	{
 		grid = new MapSquare[width][length];
 		fillWithRandom(maxElevation, precision);
 	}
 
 	/**
 	* Get a square from the map.
 	*
 	* @param x X coordinate of the requested MapSquare
 	* @param y Y coordinate of the requested MapSquare
 	* @return The requested square
 	*/
 	protected MapSquare getSquare(int x, int y)
 	{
 		try
 		{
 			return grid[x][y];
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 			return null;
 		}
 	}
 
 	/**
 	* Get the width of the map
 	*
 	* @return East-West width of the map
 	*/
 	private int getWidth()
 	{
 		return grid.length;
 	}
 
 	/**
 	* Get the Length of the map.
 	*
 	* @return North-South length of the map
 	*/
 	private int getLength()
 	{
 		return grid[0].length;
 	}
 
 	/**
 	* Fill the map with blank squares
 	*/
 	private void fillWithBlank()
 	{
 		for (int x=0; x < grid.length; x++)
 		{
 			for (int y=0; y < grid[x].length; y++)
 			{
 				grid[x][y] = new MapSquare(this);
 			}
 		}
 	}
 
 	/**
 	* Fill the map with squares of random elevation
 	*
 	* @param maxElevation Upper bound on elevation
 	* @param precision Round elevations to this place
 	*/
 	private void fillWithRandom(double maxElevation, int precision)
 	{
 		for (int x=0; x < grid.length; x++)
 		{
 			for (int y=0; y < grid[x].length; y++)
 			{
 				grid[x][y] = new MapSquare(this, Math.floor(Math.random() * (maxElevation * Math.pow(10,  precision))) / Math.pow(10, precision));
 			}
 		}
 	}
 
 	/**
 	* Scatter random items from the given ArrayList of item prototypes across the
 	* map.
 	*
 	* @param itemPrototypes An ArrayList containing item types to be scattered
 	* @param rItemInSquare The liklihood that any given square will contain any number of items
 	* @param maxItemsInSquare The greatest number of items any square may contain
 	*/
 	protected void scatterItemsRandomly(ArrayList<Thing> itemPrototypes, double rItemInSquare, int maxItemsInSquare)
 	{
		if (!itemPrototypes.isEmpty())
 		{
			for (int x=0; x < grid.length; x++)
 			{
				for (int y=0; y < grid[x].length; y++)
 				{
					//TODO: Consolidate randomness
					if (Math.random() > rItemInSquare)
 					{
						int itemsInThisSquare = (int) Math.ceil(Math.random() * maxItemsInSquare);
						for (int n=0; n < itemsInThisSquare; n++)
						{
							int itemIndex = (int) Math.floor(Math.random() * itemPrototypes.size());
							grid[x][y].getInventory().add(new Thing(itemPrototypes.get(itemIndex)));
						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	* Get the square that is in the desired direction from the supplied square.
 	*
 	* @param direction One of "n", "s", "e", or "w".
 	* @param start The starting square.  Must be in this MapGrid object.
 	* @return the MapSquare direction from start.
 	*/
 	protected MapSquare getSquareDirFrom(String direction, MapSquare start)
 	{
 		int x=0, y=0;
 		boolean found = false;
 		for (int u=0; u < grid.length; u++)
 		{
 			for (int v=0; v < grid[u].length; v++)
 			{
 				if (start == grid[u][v])
 				{
 					found = true;
 					x = u;
 					y = v;
 				}
 			}
 		}
 		if (!found)
 			return null;
 		if (direction.equals("n"))
 			return getSquare(x, y+1);
 		if (direction.equals("s"))
 			return getSquare(x, y-1);
 		if (direction.equals("e"))
 			return getSquare(x+1, y);
 		if (direction.equals("w"))
 			return getSquare(x-1, y);
 		return null;
 	}
 }
