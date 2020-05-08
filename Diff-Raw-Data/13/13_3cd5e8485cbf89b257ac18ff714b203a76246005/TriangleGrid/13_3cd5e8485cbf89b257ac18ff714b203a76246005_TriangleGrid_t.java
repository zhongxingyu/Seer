 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class TriangleGrid extends Grid
 {
 	public TriangleGrid(Vector2i size) 
 	{
 		super(size);
 		this.setType('T');
 	}
 	
 	public TriangleGrid(Vector2i size, Cell example)
 	{
 		super(size, example);
 		this.setType('T');
 	}
 	
 	public TriangleGrid(Grid g)
 	{
 		super(g);
		this.setType('T');
 	}
 
 	private Cell[] getNeighbours(boolean up, Vector2i pos)
 	{
 		Cell[] n = new Cell[12];
 		int xSize = this.getWidth();
 		int ySize = this.getHeight();
 		int x = pos.x;
 		int y = pos.y;
 		
 		int yMIN1 = y - 1;
 		int yPLUS1 = y + 1;
 		if(yMIN1 < 0)
 			yMIN1 += ySize;
 		if(yPLUS1 >= ySize)
 			yPLUS1 -= ySize;
 		
 		int xMIN2 = x - 2;
 		int xMIN1 = x - 1;
 		int xPLUS1 = x + 1;
 		int xPLUS2 = x + 2;
 		if(xMIN2 < 0)
 			xMIN2 += xSize;
 		if(xMIN1 < 0)
 			xMIN1 += xSize;
 		if(xPLUS1 >= xSize)
 			xPLUS1 -= xSize;
 		if(xPLUS2 >= xSize)
 			xPLUS2 -= xSize;
 
 		if(up)
 		{
 			n[0] = this.getCell(new Vector2i(xMIN1, yMIN1));
 			n[1] = this.getCell(new Vector2i(x, yMIN1));
 			n[2] = this.getCell(new Vector2i(xPLUS1, yMIN1));
 			n[3] = this.getCell(new Vector2i(xMIN2, y));
 			n[4] = this.getCell(new Vector2i(xMIN1, y));
 			n[5] = this.getCell(new Vector2i(xPLUS1, y));
 			n[6] = this.getCell(new Vector2i(xPLUS2, y));
 			n[7] = this.getCell(new Vector2i(xMIN2, yPLUS1));
 			n[8] = this.getCell(new Vector2i(xMIN1, yPLUS1));
 			n[9] = this.getCell(new Vector2i(x, yPLUS1));
 			n[10] = this.getCell(new Vector2i(xPLUS1, yPLUS1));
 			n[11] = this.getCell(new Vector2i(xPLUS2, yPLUS1));
 		}
 		else
 		{
 			n[0] = this.getCell(new Vector2i(xMIN2, yMIN1));
 			n[1] = this.getCell(new Vector2i(xMIN1, yMIN1));
 			n[2] = this.getCell(new Vector2i(x, yMIN1));
 			n[3] = this.getCell(new Vector2i(xPLUS1, yMIN1));
 			n[4] = this.getCell(new Vector2i(xPLUS2, yMIN1));
 			n[5] = this.getCell(new Vector2i(xMIN2, y));
 			n[6] = this.getCell(new Vector2i(xMIN1, y));
 			n[7] = this.getCell(new Vector2i(xPLUS1, y));
 			n[8] = this.getCell(new Vector2i(xPLUS2, y));
 			n[9] = this.getCell(new Vector2i(xMIN1, yPLUS1));
 			n[10] = this.getCell(new Vector2i(x, yPLUS1));
 			n[11] = this.getCell(new Vector2i(xPLUS1, yPLUS1));
 		}
 		return n;
 	}
 	
 	@Override
 	public Cell[] getNeighbours(Vector2i pos)
 	{
 		int x = pos.x;
 		int y = pos.y;
 		if((y % 2) == 0)
 		{
 			//Upside-down first triangle
 			if((x % 2) == 0)
 				return getNeighbours(false, pos);
 			else
 				return getNeighbours(true, pos);
 		}
 		else
 		{
 			//Right-side up first triangle
 			if((x % 2) == 0)
 				return getNeighbours(true, pos);
 			else
 				return getNeighbours(false, pos);
 		}
 	}
 
 }
