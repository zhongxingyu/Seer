 package org.illithid.cccp.world;
 
 import java.util.Random;
 
 public class RandomLevel extends BaseLevel {
     
     
 	Random rand = new Random();
 
 	public RandomLevel() {
 		super();
 		for(int i=0;i<700;i++)
 		    place(new Wall());
 	}
 
 	@Override
 	public void place(Occupier a) {
 		boolean done = false;
 		while (!done) {
 			int x = rand.nextInt(X);
 			int y = rand.nextInt(Y);
			if (getCell(x, y).isWalkable()) {
 				getCell(x, y).place(a);
 				done = true;
 			}
 		}
 	}
 
 	public Cell getCell(int x, int y) {
 		return cells[x][y];
 	}
 }
