 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.world;
 
 import java.util.Iterator;
 import java.util.Set;
 import student.grid.ArrayHexGrid;
 import student.config.Constants;
 import student.grid.Critter;
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
 
     public boolean isRunning() {
         return RUNNING;
     }
 
     public boolean shouldStep() {
         return STEP;
     }
 
     public boolean shouldWait() {
         return WAIT;
     }
 
     public void toggleRun() {
         RUNNING = !RUNNING;
     }
 
     public void toggleWait() {
         WAIT = !WAIT;
     }
 
     public void doStep() { //TODO: eileen: what the **** does this do?
         STEP = true;
     }
 
     public void step() {
         for (int i = 0; i < Constants.PLANTS_CREATED_PER_TURN; i++) {
             int r = (int) (grid.nRows() * Math.random());
             int c = (int) (grid.nCols() * Math.random());
             if (!grid.get(c, r).plant() && !grid.get(c, r).rock()) {
                 grid.get(c, r).putPlant();
             } else {
                 i--;
             }
         }
         int cr = 0;
         for (Reference<Tile> e : grid) {
             if (e.contents() != null && e.contents().critter()) {
                 cr++;
             }
         }
         double prob = Constants.PLANT_GROW_PROB / (cr == 0 ? 1 : cr);
         for (Reference<Tile> e : grid) {
             Tile t = e.contents();
             if (t.plant()) {
                 for (HexDir d : HexDir.values()) {
                     if (e.adj(d) != null
                             && !e.adj(d).contents().plant()
                            && !e.adj(d).contents().rock()
                             && Math.random() < prob) {
                         e.adj(d).contents().putPlant();
                     }
                 }
             }
             if (t.critter()) {
                 if (!WAIT) {
                     t.getCritter().randomAct();
                 }
                 if (t.critter()) {
                     t.getCritter().timeStep();
                 }
             }
         }
         timesteps++;
         System.out.println("-----------------" + timesteps);
     }
 
     public int getTimesteps() {
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
     
     public void addCritter(Critter c, int row, int col) throws InvalidWorldAdditionException {
         HexGrid.Reference<Tile> loc = grid.ref(col, row);
         if (loc != null) {
             if (loc.contents() == null) {
                 loc.setContents(new Tile(false, 0));
             }
             grid.ref(col, row).contents().putCritter(c);
         }else {
             throw new InvalidWorldAdditionException();
         }
     }
 
     public void add(String type, int row, int col) throws InvalidWorldAdditionException {
         HexGrid.Reference<Tile> loc = grid.ref(col, row);
         if (loc != null) {
             if (loc.contents() == null) {
                 loc.setContents(new Tile(false, 0));
             }
             if (type.equals("plant")) {
                 System.out.println(grid);
                 System.out.println(grid.ref(col, row));
                 System.out.println(grid.ref(col, row).contents());
                 loc.contents().putPlant();
             } //TODO: fix null pointer exception
             else if (type.equals("rock")) {
                 loc.setContents(new Tile.Rock());
             } else {
                 throw new InvalidWorldAdditionException();
             }
         } else {
             throw new InvalidWorldAdditionException();
         }
         //TODO: Throw some kind of exception
     }
 
     /**
      * Retrieves the default reference at 0, 0
      *
      * @return the default reference
      */
     public HexGrid.Reference<Tile> defaultLoc() {
         return grid.ref(0, 0);
     }
 
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
 
     public static class InvalidWorldAdditionException extends Exception {
 
         public InvalidWorldAdditionException() {
         }
     }
 }
