 package com.hexcore.cas.rulesystems;
 
 import com.hexcore.cas.model.Cell;
 
 /**
  * Class StdLib
 
  * @authors Karl Zoller
  */
 
 public class StdLib
 {
 	public static double max(double[] values)
 	{
 		double highest = -Double.MAX_VALUE;
 		
 		for(double x : values)
 		{
 			if(x > highest)
 				highest = x;
 		}
 		
 		return highest;
 	}
 	
 	public static double min(double[] values)
 	{
 		double lowest = Double.MAX_VALUE;
 		
 		for(double x : values)
 		{
 			if(x < lowest)
 				lowest = x;
 		}
 		
 		return lowest;
 	}
 	
 	/* Log base 10*/
 	public static double log(double value)
 	{
 		return Math.log10(value);
 	}
 	
 	/* Natural log*/
 	public static double ln(double value)
 	{
 		return Math.log(value);
 	}
 	
 	public static double sin(double value)
 	{
 		return Math.sin(value);
 	}
 	
 	public static double cos(double value)
 	{
 		return Math.cos(value);
 	}
 	
 	public static double sum(double[] values)
 	{
 		double sum = 0;
 		
 		for(double x : values)
 			sum += x;
 				
 		return sum;
 	}
 	
 	public static int count(double[] values)
 	{
 		return values.length;
 	}
 	
 	public static double random(double val)
 	{
 		return Math.random() * val;
 	}
 	
 	public static int round(double val)
 	{
 		return (int) Math.round(val);
 	}
 	
 	public int exists(Cell c)
 	{
 		return c == null ? 0 : 1;
 	}
 	
 	public static double[] generatePropertyArray(Cell[] cells, int propertyIndex)
 	{
 		double[] values = new double[cells.length];
 		
 		for(int i = 0; i < cells.length; i++)
 			if(cells[i] != null)
 				values[i] = cells[i].getValue(propertyIndex);
 		
 		return values;
 	}
 	
	//TODO: Add access to neighbours
	//TODO: Change private props to use strings
	//TODO: Add 3-step movementStep
	//TODO: ??
	//TODO: Profit?
 	public static void move(boolean active, double target, int step, Cell cell, Cell[] neighbours)
 	{
		System.err.println("MOVE CALLED: " + active + " " + target + " " + step);
 		if(!active)
 		{
 			cell.setPrivateProperty("target", -1);
 			return;
 		}
 		
 		if(step == 0)
 		{
			System.err.println("Setting target: " + target);
 			cell.setPrivateProperty("target", target);
 		}
 		else if(step == 1)
 		{
 			cell.setPrivateProperty("parent", -1);
			System.err.println("My target: " + cell.getPrivateProperty("target"));
 		}
 		else if(step == 2)
 		{
 			if(active)
 			{
				System.out.println("Checking...");
 				for(int i = 0; i < neighbours.length; i++)
 				{
 					int j = neighbours.length - (i+1);
					System.err.println(neighbours[i].getPrivateProperty("parent"));
					if(neighbours[i].getPrivateProperty("parent") == j)
 					{
						System.out.println("MOVING TO: " + i);
 						cell.setValue(0, neighbours[i].getValue(0));
 						cell.setPrivateProperty("target", -1);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Behaviour function. Cell acceptor for movement engine.
 	 * @param active Indicates if the behaviour is currently active for this cell
 	 * @param step The current movementStep
 	 * @param cell 
 	 * @param neighbours
 	 */
 	public static void accept(boolean active, int step, Cell cell, Cell[] neighbours)
 	{
		System.err.println("ACCEPT CALLED: " + active + " " + step);
 		if(!active)
 		{
 			return;
 		}
 		
 		if(step == 0)
 		{
 			cell.setPrivateProperty("target", -1);
 		}
 		else if(step == 1)
 		{
 			boolean done = false;
			System.err.println("Neighbours lenght: " + neighbours.length);
 			for(int i = 0; i < neighbours.length; i++)
 			{
 				
 				int j = neighbours.length - (i+1);
				if(neighbours[i].getPrivateProperty("target") == j)
 				{
					System.err.println("Parent found! " + i);
 					cell.setPrivateProperty("parent", i);
 					done = true;
 				}
 			}
 			
 			if(!done)
 				cell.setPrivateProperty("parent", -1);
 		}
 		else if(step == 2)
 		{
 			if(cell.getPrivateProperty("parent") != -1)
 			{
 				cell.setValue(0, neighbours[(int)cell.getPrivateProperty("parent")].getValue(0));
 			}
 			cell.setPrivateProperty("parent", -1);
 		}
 		
 	}
 }
