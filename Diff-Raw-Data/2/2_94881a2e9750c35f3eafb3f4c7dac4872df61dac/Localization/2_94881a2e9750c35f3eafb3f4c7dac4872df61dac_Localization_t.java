 /**
  * Localization.java
  * @author jmd
  * 
  * Main class for localization. Herp derp.
  */
  
  import javax.imageio.ImageIO;
  import java.awt.image.ColorConvertOp;
  import java.awt.color.ColorSpace;
  import java.io.File;
  import java.io.IOException;
  import java.awt.image.BufferedImage;
  import java.util.Arrays;               // for testing
  
  // Player imports
 import javaclient3.*;
 import javaclient3.structures.PlayerConstants;
 import javaclient3.structures.ranger.*;
  
  public class Localization {
 
     /**
      * Used for determining the angle that each laser beam fires at.
      */
     public final static double RADIAN_PER_LASER = 0.00615094;
     /**
      * This is the offset in radians needed because the 0th sample is not at 0 radians
      */
     public final static double LASER_ROBOT_OFFSET = 2.094395102;
     /**
      * Scale of the input map in meters/pixel (pulled from the .world file for the project)
      */
     public final static double MAP_METERS_PER_PIXEL = 0.02;
 
     /**
      * Constant for potential field calculation.
      */
     public final static double OBSTACLE_POTENTIAL_CONSTANT = 1.0;
 
     /**
      * Constant for potential field calculation.
      */
     public final static double GOAL_POTENTIAL_CONSTANT = 1.0;
     
     /**
      * Loads in the map from an external file. Does some number fudging,
      * because the expected file is a PNG and the RGB value grabbed from it
      * for white is -1 (representing free space) and the rest (some other
      * negative number) is black (representing obstacles).
      *
      * @param filename  the name of the file containing the map
      * @return  a 2D integer array [x][y] containing the grayscale rgb value
      *          at (x,y) where the origin is the top-left-most pixel
      */
     public static int[][] getMap(String filename) {
         int[][] map;
         BufferedImage img = null;
         try {
             img = ImageIO.read(new File(filename));
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         map = new int[img.getWidth()][img.getHeight()];
         
         int pixel;
         for (int x = 0; x < img.getWidth(); x++) {
             for (int y = 0; y < img.getHeight(); y++) {
                 pixel = img.getRGB(x,y);
                 map[x][y] = (pixel == -1) ? 255 : 0;    // some number fudging
             }
         }
         
         return map;
     }
     
     /**
      * Takes in a configuration space map (assuming the robot is a point) and
      * returns a workspace map (assuming the robot has a 10cm radius). This
      * lazily expands all the obstacles by 5cm in all directions
      *
      * @param map   the configuration space map as a 2D integer array
      * @return      the workspace map as a 2D integer array
      */
     public static int[][] getWorkspaceMap(int[][] map) {
         int[][] csMap = new int[map.length][map[0].length];
         
         for (int x = 0; x < csMap.length; x++) {
             for (int y = 0; y < csMap[0].length; y++) {
                 if (map[x][y] == 0) {
                     for (int i = -9; i < 9; i++) {
                         for (int j = -9; j < 9; j++) {
                             if (x+i < 0 || y+j < 0) continue;
                             if (map[x+i][y+j] != 0)
                                 csMap[x+i][y+j] = 0;
                         }
                     }
                 } else {
                     csMap[x][y] = map[x][y];
                 }
             }
         }
         
         return csMap;
     }
     
     
     public static void main(String[] args) {
         int[][] map = getMap(args[0]);
         int[][] csMap = getWorkspaceMap(map);
 //        System.out.println("map = " + Arrays.deepToString(map));
 //        System.out.println();
         System.out.println("csMap = " + Arrays.deepToString(csMap));
         
         // Testing junk...
         // @TODO Remove this at some point
         int mapObs = 0, csMapObs = 0;
         for (int i = 0; i < map.length; i++) {
             for (int j = 0; j < map[0].length; j++) {
                 if (map[i][j] == 0) mapObs++;
                 if (csMap[i][j] == 0) csMapObs++;
             }
         }
         System.out.printf("Original Obstacle Pixels: %d\n" +
             "Workspace Obstacle Pixels: %d\n", mapObs, csMapObs);
         
         // copypasta initalization stuff from other jawns
         /*
         PlayerClient pc;
         if (args.length == 1)
             pc = new PlayerClient("localhost",6665);
         else
             pc = new PlayerClient(args[1],Integer.valueOf(args[2]));
     
         Position2DInterface pos = 
             pc.requestInterfacePosition2D(0,PlayerConstants.PLAYER_OPEN_MODE);
     
         RangerInterface ranger = 
             pc.requestInterfaceRanger(0,PlayerConstants.PLAYER_OPEN_MODE);
         */
         
         /* So as not to potentially overload the ranger interface, we should
          * probably pass the localizer to the wanderer and have the wanderer
          * update the localizer as it receives info from the ranger and pos.
          *
          * Or we would just let them independently abuse the ranger and pos.
          * Whatever.
          */
        Localizer loc = new Localizer(map);
         //Wanderer w = new Wanderer(pc,pos,ranger,loc);
         
         // loc.start();
         // w.start();
         
     }
 }
