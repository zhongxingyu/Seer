 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.world;
 
 import java.util.Iterator;
 import java.util.Set;
 import student.grid.ArrayHexGrid;
 import student.grid.Constants;
 import student.grid.HexGrid;
 import student.grid.HexGrid.HexDir;
 import student.grid.HexGrid.Reference;
 import student.grid.Tile;
 
 /**
  *
  * @author Panda
  */
 public class World {
 
     private static final int DEFAULT_ROWS = 6;
     private static final int DEFAULT_COLS = 6;
     HexGrid<Tile> grid;
     private int timesteps = 0;
     private boolean RUNNING = false;
     private boolean STEP = false;
     private boolean WAIT = true; //if false, random action
     public World() {
         this(DEFAULT_ROWS, DEFAULT_COLS);
     }
 
     public World(int _r, int _c) {
         grid = new ArrayHexGrid<Tile>(_r, _c);
     }
     
     public String getStatus() {
         return "Timesteps: " + timesteps + "\n" + population();
     }
     public boolean isRunning()
     {
         return RUNNING;
     }
     public boolean shouldStep()
     {
         return STEP;
     }
     public boolean shouldWait()
     {
         return WAIT;
     }
     public void toggleRun()
     {
         RUNNING = !RUNNING;
     }
     public void toggleWait()
     {
         WAIT = !WAIT;
     }
 
     public void doStep() {
         STEP = true;
      }
     
    public void step() {
         for(int i = 0; i < Constants.PLANTS_CREATED_PER_TURN; i++) {
             int r = (int) (grid.nRows()*Math.random());
             int c = (int) (grid.nCols()*Math.random());
             if(!grid.get(c, r).plant() && !grid.get(c, r).rock())
                 grid.get(c, r).putPlant();
             else
                 i--;
         }
         int cr = 0;
         for(Reference<Tile> e : grid)
             if(e.contents()!=null && e.contents().critter()) cr++;
         double prob = Constants.PLANT_GROW_PROB/ (cr == 0?1:cr);
         for(Reference<Tile> e : grid) {
             Tile t = e.contents();
             if(t.plant())
                 for(HexDir d : HexDir.values())
                     if (e.adj(d) != null
                      && !e.adj(d).contents().plant() 
                      && Math.random() < prob)
                        e.adj(d).contents().putPlant();
             if(t.critter()) {
                if(!WAIT) {
                     switch((int)(Math.random()*6)) {
                         case 0:
                             t.getCritter()._wait();
                         case 1:
                             t.getCritter().forward();
                             break;
                         case 2:
                             t.getCritter().backward();
                             break;
                         case 3:
                             t.getCritter().eat();
                             break;
                         case 4:
                             t.getCritter().left();
                             break;
                         case 5:
                             t.getCritter().right();
                             break;
                     }
                 } else
                     t.getCritter()._wait();
             }
         }
         timesteps++;
         System.out.println("-----------------"+timesteps);
     }
     
     public int getTimesteps()
     {
         return timesteps;
     }
     public int height() {
         return grid.nRows();
     }
 
     public int width() {
         return grid.nCols();
     }
 
     public Reference<Tile> at(int r, int c) {
         return grid.ref(c, r);
     }
 
     /**
      * Retrieves the default reference at 0, 0
      *
      * @return the default reference
      */
     public HexGrid.Reference<Tile> defaultLoc() {
         return grid.ref(0, 0);
     }
 
     /*public String population() {
      int[] population = new int[4]; //[critters, plants, food, rocks]
      Iterator<Tile> it = grid.iterator();
      while (it.hasNext()) {
      Tile t = it.next();
      if(t==null)continue;
      population[0] = population[0] + (t.critter() ? 1 : 0);
      population[1] = population[1] + (t.plant() ? 1 : 0);
      population[2] = population[2] + (t.food() ? 1 : 0);
      population[3] = population[3] + (t.rock() ? 1 : 0);
      }
      String pop = "Population\n"
      + "\n\tCritters: "+population[0]
      + "\n\tPlants: "+population[1]
      + "\n\tFood: "+population[2]
      + "\n\tRocks: "+population[3];
      return pop;
      }*/
     public int[] population() {
         int[] population = new int[4]; //[critters, plants, food, rocks]
         Iterator<Reference<Tile>> it = grid.iterator();
         while (it.hasNext()) {
             Tile t = it.next().contents();
             if (t == null) {
                 continue;
             }
             population[0] = population[0] + (t.critter() ? 1 : 0);
             population[1] = population[1] + (t.plant() ? 1 : 0);
             population[2] = population[2] + (t.food() ? 1 : 0);
             population[3] = population[3] + (t.rock() ? 1 : 0);
         }
         return population;
     }
 }
