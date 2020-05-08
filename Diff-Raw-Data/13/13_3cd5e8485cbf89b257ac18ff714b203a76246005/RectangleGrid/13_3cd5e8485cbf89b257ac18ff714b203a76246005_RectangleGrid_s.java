 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class RectangleGrid extends Grid
 {
 
 	public RectangleGrid(Vector2i size) 
 	{
 		super(size);
 		this.setType('R');
 	}//end constructor
 	
 	public RectangleGrid(Grid g)
 	{
 		super(g);
 	}
 	
 	@Override
 	/**
 	 * Returns the possible neighbours of the a cell on a rectangular grid. Null if the neighbour is the target
 	 * cell itself.
 	 * 
 	 * @param pos - location of cell whose neighbours are requested.
 	 * @return - Cell array of 8 neighbours.
 	 * @author Apurva Kumar
 	 */
 	public Cell[] getNeighbours(Vector2i pos)
 	{
 		//Initialisations
 		Cell [] neighbours = new Cell[8];
 		int i = 0;//counter, goes till 8.
 		int xdim = getWidth();//get dimensions stored
 		int ydim = getHeight();
 		int x = pos.x;//get target cell's location stored.
 		int y = pos.y;
 		
 		//TOP LEFT: y-1, x-1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+xdim-1)%xdim, (y+ydim-1)%ydim), i++);
 		//TOP CENTRE: y-1, x
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+xdim-1)%xdim, y), i++);
 		//TOP RIGHT: y-1, x+1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+xdim-1)%xdim, (y+1)%ydim), i++);
 		//LEFT: y, x-1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i(x, (y+ydim-1)%ydim), i++);
 		//RIGHT: y, x+1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i(x, (y+1)%ydim), i++);
 		//BOTTOM LEFT: y+1, x-1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+1)%xdim, (y+ydim-1)%ydim), i++);
 		//BOTTOM CENTRE: y+1, x
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+1)%xdim, y), i++);
 		//BOTTOM RIGHT: y+1, x+1
 		neighbours = setNeighbours( pos, neighbours, new Vector2i((x+1)%xdim, (y+1)%ydim), i++);
 		
 		return neighbours;
 	}//end method getNeighbours
 	
 	/**
 	 * Private internal function. Chooses whether the neighbour is valid or should be set to null.
 	 * @param x - x position of target cell.
 	 * @param y - y position of target cell.
 	 * @param neighbours - array to be altered.
 	 * @param temp - Vector2i where the neighbour should be found.
 	 * @param i - number of neighbour.
 	 * @return - the updated neighbours array.
 	 * @author Apurva Kumar
 	 */
 	private Cell[] setNeighbours(Vector2i pos, Cell[] neighbours, Vector2i temp, int i)
 	{
 		if(pos.equals(temp))//if the neighbour is the same as the target cell
 			neighbours[i] = null;//set neighbour to null
 		else
 			neighbours[i] = getCell(temp);//else, get Cell.
 		return neighbours;
 	}//end method setNeighbours
 
 }
