 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public abstract class Grid
 {
 	private Cell[][] cells = null;
 	private Vector2i size = null;
 	
 	public Grid(Vector2i size)
 	{
		this.size = new Vector2i(size);
 		this.cells = new Cell[size.get(0)][size.get(1)];
 		for(int x = 0; x < size.get(0); x++)
 			for(int y = 0; y < size.get(1); y++)
 				this.cells[x][y] = new Cell(1);
 	}
 	
 	public Grid(Vector2i size, Cell example)
 	{
 		this.size = size;
 		this.cells = new Cell[size.get(0)][size.get(1)];
 		for(int x = 0; x < size.get(0); x++)
 			for(int y = 0; y < size.get(1); y++)
 			{
 				this.cells[x][y] = new Cell(example.getValueCount());
 				for(int i = 0; i < example.getValueCount(); i++)
 					this.cells[x][y].setValue(i, example.getValue(i));
 			}
 	}
 	
 	public abstract Cell[] getNeighbours(Vector2i pos);
 	
 	public Cell getCell(Vector2i pos)
 	{
 		return cells[pos.get(0)][pos.get(1)];
 	}
 	
 	public int getWidth()
 	{
 		return size.get(1);
 	}
 	
 	public int getHeight()
 	{
 		return size.get(0);
 	}
 	
 	public Vector2i getSize()
 	{
 		return size;
 	}
 }
