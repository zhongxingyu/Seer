 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utilities;
 
 import java.awt.Point;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import board.Board;
 import board.Symbol;
 
 /**
  *
  * @author michal
  */
 public class SokobanUtil {
     
     public enum Action { UP, DOWN, LEFT, RIGHT };
     
     /**
      * Returns the inverse of the given action object.
      * @param a The action to invert
      * @return The action needed undo the given action
      */
     public static Action inverseAction(Action a){
         switch(a){
             case UP: return Action.DOWN;
             case DOWN: return Action.UP;
             case LEFT: return Action.RIGHT;
             case RIGHT: return Action.LEFT;
             default: return null;
         }
     }
     
     /**
      * @param aGrid
      * @return an ascii representation of the grid.
      */
     public static String stringifyGrid(Symbol[][] aGrid) {
 		StringBuilder sb = new StringBuilder();
 		for (int y = 0; y < aGrid.length; y++) {
 			for (int x = 0; x < aGrid[y].length; x++) {
 				sb.append(aGrid[y][x]);
 			}
 			if (y < aGrid.length - 1) sb.append('\n');
 		}
 		
 		return sb.toString();
 	}
 
     /**
      * Applies an action to the given point, returning the point which would be
      * moved to if the action was legal.
      * @param a An action
      * @param p The point to which to apply the action
      * @return A new point with the action applied.
      */
     public static Point applyActionToPoint(Action a, Point p){
         switch(a){
             case UP: return new Point(p.x, p.y - 1);
             case DOWN: return new Point(p.x, p.y + 1);
             case LEFT: return new Point(p.x - 1, p.y);
             case RIGHT: return new Point(p.x + 1, p.y);
             default: return null;
         }
     }
     
     public static char actionToString(Action a){
         if (a == null)
             return ' ';
         switch(a){
             case UP: return 'U';
             case DOWN: return 'D';
             case LEFT: return 'L';
             case RIGHT: return 'R';
             default: return ' ';
         }
     }
     
     
     public static Board readMap(String filename) {
         return readMap(Paths.get(filename));
     }
     
     public static Board readMap(Path filePath) {
     	try {
 			return Board.read( Files.newBufferedReader(filePath, Charset.defaultCharset()));
 		} catch (IOException e) {
 			throw new RuntimeException("File no found: " + filePath.toAbsolutePath(), e);
 		}
     }
     
     public static String readMapAsString(String filename) throws IOException {
         return new String(Files.readAllBytes(Paths.get(filename)));
     }
     
     /*
      * Returns the point of the given two points which is in the top left most position
      */
     public static Point pointMin(Point p1, Point p2){
//        System.out.println("comparing " + p1 + " with " + p2);
         if (p2.y > p1.y){
             return p1;
         } else if (p1.y > p2.y)
             return p2;
         else {
             return p2.x >= p1.x ? p1 : p2;
         }
     }
 
 }
