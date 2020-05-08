 package com.hexcore.cas.model;
 
 import com.hexcore.cas.math.Vector2i;
 
 public enum GridType
 {
 	RECTANGLE('R', RectangleGrid.class),
 	HEXAGON('H', HexagonGrid.class),
 	TRIANGLE('T', TriangleGrid.class);
 	
 	char symbol;
 	Class<? extends Grid> clazz;
 	
 	GridType(char symbol, Class<? extends Grid> clazz)
 	{
 		this.symbol = symbol;
 		this.clazz = clazz;
 	}

 	public Grid create(Vector2i size, int numProperties)
 	{
 		try
 		{
 			return (Grid)clazz.getDeclaredConstructor(Vector2i.class, int.class).newInstance(size, numProperties);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}	
 	
 	public Grid create(Vector2i size, Cell example)
 	{
 		try
 		{
 			return (Grid)clazz.getDeclaredConstructor(Vector2i.class, Cell.class).newInstance(size, example);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;	
 	}
 }
