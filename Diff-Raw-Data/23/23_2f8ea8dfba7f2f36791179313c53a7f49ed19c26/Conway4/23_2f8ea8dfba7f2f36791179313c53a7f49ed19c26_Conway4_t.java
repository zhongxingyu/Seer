 package edu.stuy.goldfish.rules;
 
import java.util.Random;

 import edu.stuy.goldfish.Grid;
 import edu.stuy.goldfish.Patch;
 
 // An implementation of conway using von Neumann Neighborhoods.
 public class Conway4 extends RuleSet {
     public static int states = 2;
 
     public static Grid run(Grid g) {
         Grid newGrid = new Grid(g.getWidth(), g.getHeight(), false);
         for (int i = 0; i < g.getWidth(); i++) {
             for (int j = 0; j < g.getHeight(); j++) {
                 Patch orig = g.getPatch(i, j);
                 int numAlive = orig.get4Neighbors(1);
                 Patch p = orig.clone(newGrid);
                 if (numAlive < 2)
                     p.setState(0); // Dies by underpopulation
                 else if (numAlive > 3)
                     p.setState(0); // Dies by overpopulation
                 else if (numAlive == 3)
                     p.setState(1); // Born with 3 neighbors
                 newGrid.setPatch(i, j, p);
             }
         }
         return newGrid;
     }
 
     public static void setup(Grid g) {
        Random random = new Random();
        for (int i = 0; i < g.getWidth(); i++) {
            for (int j = 0; j < g.getHeight(); j++) {
                g.getPatch(i, j).setState(random.nextInt(2));
             }
         }
     }
 }
