 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class HexagonGrid extends Grid
 {
 	public HexagonGrid(Vector2i size) 
 	{
 		super(size);
 		gridType = 'H';
 	}
 	
 	public HexagonGrid(Vector2i size, Cell example)
 	{
 		super(size, example);
 		gridType = 'H';
 	}
 	
 	public HexagonGrid(Grid g)
 	{
 		super(g);
 		gridType = 'H';
 	}
 	
 	public Grid clone()
 	{
 		return new HexagonGrid(this);
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
 		
 		if(wrap)
 		{
 			if((x%2) == 0)//if the column is even:
 			{
 				neighbours[0] = this.getCell(new Vector2i((x+xdim-1)%xdim, (y+ydim-1)%ydim));
 				neighbours[1] = this.getCell(new Vector2i(x, (y+ydim-1)%ydim));
 				neighbours[2] = this.getCell(new Vector2i((x+1)%xdim, (y+ydim-1)%ydim));
 				
 				neighbours[3] = this.getCell(new Vector2i((x+xdim-1)%xdim, y));
 				neighbours[4] = this.getCell(new Vector2i( (x+1)%xdim, y));
 				neighbours[5] = this.getCell(new Vector2i( x, (y+1)%ydim));
 			}//if
 			else//the column is odd
 			{
 				neighbours[0] = this.getCell(new Vector2i( x, (y+ydim-1)%ydim));
 				neighbours[1] = this.getCell(new Vector2i( (x+xdim-1)%xdim, y));
 				neighbours[2] = this.getCell(new Vector2i((x+1)%xdim, y));
 				
 				neighbours[3] = this.getCell(new Vector2i( (x+xdim-1)%xdim, (y+1)%ydim));
 				neighbours[4] = this.getCell(new Vector2i( x, (y+1)%ydim));
 				neighbours[5] = this.getCell(new Vector2i( (x+1)%xdim, (y+1)%ydim));
 			}//else
 		}//if
 		else
 		{
 			if((x%2) == 0)//if the column is even:
 			{
 				neighbours = setNeighbours(neighbours, x-1, y-1, 	xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x, y-1, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x+1, y-1, 	xdim, ydim, i++);
 				
 				neighbours = setNeighbours(neighbours, x-1, y, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x+1, y, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x, y+1, 		xdim, ydim, i++);
 			}//if
 			else//the column is odd
 			{
 				neighbours = setNeighbours(neighbours, x, y-1, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x-1, y, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x+1, y, 		xdim, ydim, i++);
 				
 				neighbours = setNeighbours(neighbours, x-1, y+1, 	xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x, y+1, 		xdim, ydim, i++);
 				neighbours = setNeighbours(neighbours, x+1, y+1, 	xdim, ydim, i++);
 			}//else
 		}//else
 		
 		return neighbours;
 	}//end method getNeighbours
 
 	/**
 	 * Private internal function. Chooses whether the neighbour is valid or should be set to null.
 	 * @param x - x position of target cell.
 	 * @param y - y position of target cell.
 	 * @param neighbours - array to be altered.
 	 * @param xdim - x dimension of the grid
 	 * @param ydim - y dimension of the grid.
 	 * @param i - number of neighbour.
 	 * @return - the updated neighbours array.
 	 * @author Apurva Kumar
 	 */
 	private Cell[] setNeighbours(Cell [] neighbours, int x, int y, int xdim, int ydim, int i)
 	{
		if((x < 0) || (x > xdim))
 		{
 			neighbours[i] = null;
 		}
		else if((y < 0) || (y > ydim))
 		{
 			neighbours[i] = null;
 		}
 		else
 			neighbours[i] = getCell( x, y);
 		
 		return neighbours;
 	}//end method setNeighbours
 	
 }
