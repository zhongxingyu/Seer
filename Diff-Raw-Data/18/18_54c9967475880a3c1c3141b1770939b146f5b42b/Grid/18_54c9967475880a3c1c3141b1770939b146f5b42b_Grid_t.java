 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public abstract class Grid
 {
 	protected Cell[][] 		cells = null;
 	protected char 			gridType = 'x';
 	protected Vector2i 		size = null;
 	protected int			numProperties = 0;
 	protected boolean		wrap = true;
 	
 	public Grid(Vector2i size)
 	{
 		this.numProperties = 1;
 		
 		this.size = new Vector2i(size);
 		this.cells = new Cell[size.y][size.x];
 		for(int y = 0; y < size.y; y++)
 			for(int x = 0; x < size.x; x++)
 				this.cells[y][x] = new Cell(numProperties);
 	}
 	
 	public Grid(Vector2i size, int numProperties)
 	{
 		this.numProperties = numProperties;
 		
 		this.size = new Vector2i(size);
 		this.cells = new Cell[size.y][size.x];
 		for(int y = 0; y < size.y; y++)
 			for(int x = 0; x < size.x; x++)
 				this.cells[y][x] = new Cell(numProperties);
 	}	
 	
 	public Grid(Vector2i size, Cell example)
 	{
 		this.numProperties = example.getValueCount();
 		
 		this.size = new Vector2i(size);
 		this.cells = new Cell[size.y][size.x];
 		for(int y = 0; y < size.y; y++)
 			for(int x = 0; x < size.x; x++)
 			{
 				this.cells[y][x] = new Cell(example.getValueCount());
 				for(int i = 0; i < example.getValueCount(); i++)
 					this.cells[y][x].setValue(i, example.getValue(i));
 			}
 	}
 	
 	public Grid(Grid grid)
 	{
 		this.numProperties = grid.numProperties;
		this.wrap = grid.wrap;
 		
 		this.size = new Vector2i(grid.size);
 		this.cells = new Cell[grid.size.y][grid.size.x];
 		for(int y = 0; y < size.y; y++)
 			for(int x = 0; x < size.x; x++)
 			{
 				this.cells[y][x] = new Cell(grid.getCell(x, y));
 			}
 	}
 	
 	public abstract Grid 	clone();
 	public abstract Cell[] 	getNeighbours(Vector2i pos);
 	
 	public Cell getCell(int x, int y)
 	{
 		return cells[y][x];
 	}
 	
 	public Cell getCell(Vector2i pos)
 	{
 		return cells[pos.y][pos.x];
 	}
 	
 	public void setCell(int x, int y, double[] vals)
 	{
 		cells[y][x] = new Cell(vals);
 	}	
 	
 	public void setCell(Vector2i pos, double[] vals)
 	{
 		cells[pos.y][pos.x] = new Cell(vals);
 	}
 	
 	public void setCell(int x, int y, Cell cell)
 	{
 		cells[y][x] = new Cell(cell);
 	}	
 	
 	public void setCell(Vector2i pos, Cell cell)
 	{
 		cells[pos.y][pos.x] = new Cell(cell);
 	}	
 	
 	public int getWidth()
 	{
 		return size.x;
 	}
 	
 	public int getHeight()
 	{
 		return size.y;
 	}
 	
 	public Vector2i getSize()
 	{
 		return size;
 	}
 		
 	public char getType()
 	{
 		return gridType;
 	}
 	
 	public int getNumProperties()
 	{
 		return numProperties;
 	}
 	
 	public boolean getWrappable()
 	{
 		return wrap;
 	}
 	
 	public void setWrappable(boolean wrappable)
 	{
 		wrap = wrappable;
 	}
 }
