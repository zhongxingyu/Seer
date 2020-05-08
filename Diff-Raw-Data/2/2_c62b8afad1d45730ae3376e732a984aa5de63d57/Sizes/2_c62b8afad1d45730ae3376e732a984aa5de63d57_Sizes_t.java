 package kniemkiewicz.jqblocks.ingame;
 
 /**
  * User: krzysiek
  * Date: 08.07.12
  */
 public class Sizes {
   public static int SMALL_BLOCK = 10;
   public static float TIME_UNIT = 150f;
   public static float MAX_FALL_SPEED = Sizes.SMALL_BLOCK / TIME_UNIT * 20;
   public static float G = MAX_FALL_SPEED / TIME_UNIT / 10;
 
   public static int DEFAULT_WINDOW_WIDTH = 1000;
   public static int DEFAULT_WINDOW_HEIGHT = 800;
 
   public static int MIN_X = -3 * DEFAULT_WINDOW_WIDTH;
   public static int MAX_X = 3 * DEFAULT_WINDOW_WIDTH;
   public static int MIN_Y = 0;
   public static int MAX_Y = 10 * DEFAULT_WINDOW_HEIGHT;
 
  public static int DEFAULT_BLOCK_ENDURANCE = 250;
   public static int DEFAULT_PICKAXE_STRENGTH = 1;
 
   static public int roundToBlockSizeX(int x) {
     return (x - MIN_X) / SMALL_BLOCK * SMALL_BLOCK + MIN_X;
   }
 
   static public int roundToBlockSizeX(float x) {
     return Math.round((x - MIN_X) / SMALL_BLOCK) * SMALL_BLOCK + MIN_X;
   }
 
   static public int roundToBlockSizeY(int y) {
     return (y - MIN_Y) / SMALL_BLOCK * SMALL_BLOCK + MIN_Y;
   }
 
   static public int roundToBlockSizeY(float y) {
     return Math.round((y - MIN_Y) / SMALL_BLOCK) * SMALL_BLOCK + MIN_Y;
   }
 
   // For widths and heights
   static public int roundToBlockSize(int z) {
     return z / SMALL_BLOCK * SMALL_BLOCK;
   }
 
   static public int roundToBlockSize(float z) {
     return Math.round(z / SMALL_BLOCK) * SMALL_BLOCK;
   }
 }
