 package com.hexcore.cas.test;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.TriangleGrid;
 
 public class WaterFlow 
 {	
 	Grid	grid;
 		
 	public WaterFlow(Grid grid)
 	{
 		this.grid = grid;
 		
 		Cell cell = new Cell(3);
 		cell.setValue(0, 0); // Type
 		cell.setValue(1, 0); // Water
 		cell.setValue(2, 0); // Land
 				
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				grid.setCell(x, y, cell);
 				grid.getCell(x, y).setValue(2, (int)(
 						Math.cos((x + y) / 10.0) * 1.5 + 
 						Math.cos((x - y) / 9.0) * 1.5 +
 						Math.cos((x / 8.4 + y / 6.8 - x / 7.6)) * 4) + 7);
 			}
 		
 		grid.setCell(6, 24, new double[] {1, 8, 0});
 	}
 
 	public Grid getGrid()
 	{
 		return grid;
 	}
 	
 	public void generateNextGeneration()
 	{
 		Grid temp = grid.clone();
 		
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell	self = temp.getCell(x, y);
 				Cell [] neighbours = grid.getNeighbours(new Vector2i(x,y));
 				
 				if (self.getValue(0) == 0)
 				{
 					double maxWater = 0, minWater = 100;
 					
 					for (Cell n : neighbours)
 					{
						if (n == null) continue;
						
 						if (n.getValue(1) > 0)
 						{
 							double water = n.getValue(2) + n.getValue(1) - self.getValue(2);
 							if (water > maxWater) maxWater = water;
 							if (water < minWater) minWater = water;
 						}
 					}
 	
 					double ownHeight = self.getValue(1);
 					if (maxWater > ownHeight) 
 						self.setValue(1, self.getValue(1) + Math.floor(Math.max((maxWater - ownHeight) / 2, 1)));
 					else if (maxWater < ownHeight)
 						self.setValue(1, self.getValue(1) - 1);
 				}
 			}
 		
 		grid = temp;
 	}
 }
