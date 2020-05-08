 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class HexagonGrid extends Grid
 {
 
 	public HexagonGrid(Vector2i size) 
 	{
 		super(size);
 		this.setType('H');
 	}
 	
 	public HexagonGrid(Grid g)
 	{
 		super(g);
 	}
 	
 	@Override
 	/**
 	 * Returns the possible neighbours of the a cell on a hexagonal grid. Null if the neighbour is the target
 	 * cell itself.
 	 * 
 	 * @param pos - location of cell whose neighbours are requested.
 	 * @return - Cell array of 6 neighbours.
 	 * @author Apurva Kumar
 	 */
 	public Cell[] getNeighbours(Vector2i pos)
 	{
 		//Initialisations
 		Cell [] neighbours = new Cell[6];
 		int i = 0;//counter, goes till 6.
 		int xdim = getWidth();//get dimensions stored
 		int ydim = getHeight();
 		int x = pos.x;//get target cell's location stored.
 		int y = pos.y;
 		
 		if((y%2) == 0)//if the row is even:
 		{
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+ydim-2)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i((x+xdim-1)%xdim, (y+ydim-1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+ydim-1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i((x+xdim-1)%xdim, (y+1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+2)%ydim), i++);
 		}//if
 		else//the row is odd
 		{
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+ydim-2)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+ydim-1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i((x+1)%xdim, (y+ydim-1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i((x+1)%xdim, (y+1)%ydim), i++);
 			neighbours = setNeighbours( pos, neighbours, new Vector2i( x, (y+2)%ydim), i++);
 		}//else
 		
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
