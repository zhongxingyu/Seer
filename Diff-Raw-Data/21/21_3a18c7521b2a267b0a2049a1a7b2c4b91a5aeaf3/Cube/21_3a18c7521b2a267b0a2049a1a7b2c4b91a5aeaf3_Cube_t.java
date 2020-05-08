 import java.util.ArrayList;
 
 /**
  * Cube representing the n-puzzle.
  *  
  * @author tdebroc
  * @version 1.0
  */
 public class Cube {
 
  static int SIZE;
  int[][] grid; 
   // int[][] grid = {{0,8,2},{3,4,5},{6,7,1}};
   // int[][] grid = {{5,0,6},{8,3,7},{1,2,4}};
   // int[][] grid = {{4,7,0},{5,2,8},{3,1,6}};
   // int[][] grid = {{1,0},{2,3}};
   private static String[] moves = {"up", "bottom", "left", "right"};
   static ArrayList<Point> goodPosition = new ArrayList<Point>(); 
   
   /**
    * Empty Constructor for the Cube.
    */
   public Cube() {
     
   }
   
   /**
    * Constructor for the Cube with a choosen size.
    * @param size  Size of the grid.
    */
   public Cube(int size) {
     SIZE = size;
    grid = new int[SIZE][SIZE];
   }
   
   /**
    * Constructor for the Cube with a grid to set.
    * @param grid Grid for the cube.
    */
   public Cube(int[][] grid) {
    SIZE = grid.length;
    this.grid = new int[SIZE][SIZE];
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         this.grid[i][j] = grid[i][j];
       }
     }
     
   }
   
   /**
    * @return The String format of the Cube.
    */
   public String toString() {
     String s = "";
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         s += (grid[i][j] > 99 ? "" : (grid[i][j] > 9 ? " " : "  ")) + grid[i][j] + " | ";
       }
       s += "\n";
     }
     return s;
   }
   
   /**
    * Fill the grid randomly.
    */
   public void fillGridShuffle() {
     int value = 0;
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         goodPosition.add(new Point(j, i));
         grid[i][j] = value;
         value++;
       }
     }
     for (int i = 0; i < Math.pow(SIZE, 5); i++) {
       move((int) Math.floor(Math.random() * 4));
     }
   }
   
   /**
    * Apply the move to the grid.
    * @param move Move to apply ( 0 : up, 1 : bottom, 2 : left, 3 : right)
    * @return Whether the move has been applied to the cube.
    */
   public boolean move(int move) {
     Point blankPoint = getBlankPosition();
     if (move == 0) {
       if (blankPoint.getY() == 0) {
         return false;
       } else {
         grid[blankPoint.getY()][blankPoint.getX()] = 
             grid[blankPoint.getY() - 1][blankPoint.getX()];
         grid[blankPoint.getY() - 1][blankPoint.getX()] = 0;
         return true;
       }
     } else if (move == 1) {
       if (blankPoint.getY() == grid.length - 1) {
         return false;
       } else {
         grid[blankPoint.getY()][blankPoint.getX()] = 
             grid[blankPoint.getY() + 1][blankPoint.getX()];
         grid[blankPoint.getY() + 1][blankPoint.getX()] = 0;
         return true;
       }
     } else if (move == 2) {
       if (blankPoint.getX() == 0) {
         return false;
       } else {
         grid[blankPoint.getY()][blankPoint.getX()] = 
             grid[blankPoint.getY()][blankPoint.getX() - 1];
         grid[blankPoint.getY()][blankPoint.getX() - 1] = 0;
         return true;
       }
     } else if (move == 3) {
       if (blankPoint.getX() == grid.length - 1) {
         return false;
       } else {
         grid[blankPoint.getY()][blankPoint.getX()] = 
             grid[blankPoint.getY()][blankPoint.getX() + 1];
         grid[blankPoint.getY()][blankPoint.getX() + 1] = 0;
         return true;
       }
     }
     return false;
   }
   
   /**
    * Gets Blank position.
    * @return Position of the blank box.
    */
   public Point getBlankPosition () {
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         if (grid[i][j] == 0) {
           return new Point(j, i);
         }
       }
     }
     return null;
   }
   
   /**
    * Whether the grid is sorted.
    * @return
    */
   public boolean isGridGood() {
     int value = 0;
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         if (grid[i][j] != value) {
           return false;
         }
         value++;
       }
     }
     return true;
   }
   
   /**
    * Evaluation function of the grid (Manhattan distance).
    * @return Score with the Manhattan distance.
    */
   public int getScoreManhattan() {
     int score = 0;
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         score += Math.abs(goodPosition.get(grid[i][j]).getX() - j) +
             Math.abs(goodPosition.get(grid[i][j]).getY() - i);
       }
     }
     return score;
   }
   
   
   /**
    * Evaluation function of the grid (my personal one).
    * @return
    */
   public long getScore() {
     long score = 0;
     long value = 0;
     for (int i = 0; i < grid.length; i++) {
       for (int j = 0; j < grid[i].length; j++) {
         long pawnPower =  (long) ((grid[i][j] == value) ? 0 :
           Math.pow(SIZE, goodPosition.get(grid[i][j]).getY())) ;
         pawnPower += (grid[i][j] == value) ? 0 :
           Math.pow(SIZE, goodPosition.get(grid[i][j]).getX()) ;
       
         score += (Math.abs(goodPosition.get(grid[i][j]).getX() - j) +
             Math.abs(goodPosition.get(grid[i][j]).getY() - i)) * pawnPower;
         
         value++;
       }
     }
     
     return score;
   }
 
 
   /**
    * Gets moves array.
    * @return Moves arrays
    */
   public static String[] getMoves() {
     return moves;
   }
 
   
   
 }
