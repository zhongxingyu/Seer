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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  *
  * @author michal
  */
 public class SokobanUtil {
     
     public enum Action { 
     	UP(0,-1),
     	DOWN(0,1),
     	LEFT(-1,0),
    	RIGHT(1,0);
     	
 	    	public final int dx;
 	    	public final int dy;
 			
 			
 			private Action(int dx, int dy)
 			{
 				this.dx = dx;
 				this.dy = dy;
 			}
 			
 			
     	};
     
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
      * Returns the point of the given two points which is in the top left most position.
      * The top left most point is decided based on the distance to the point 0,0. The
      * space is assumed to be discrete with all coordinates non-negative. Distance
      * is calculated by summing the x and y coordinates of the points. If the sum
      * is equal, the point with the smallest y coordinate is returned.
      */
     public static Point pointMin(Point p1, Point p2){
         if (p1.equals(p2))
             return p1;
         
         int p1sum = p1.x + p1.y;
         int p2sum = p2.x + p2.y;
         
         if (p1sum < p2sum)
             return p1;
         else if (p2sum < p1sum)
             return p2;
         // Below here the sums are equal
         else if (p1.y < p2.y)
             return p1;
         else 
             // p2.y < p1.y. There is no p1.y = p2.y because we checked equality at
             // the start, and the only way for two points with one equal coordinate
             // to be equal is for them both to have the other coordinate equal as well.
             return p2;
     }
     
     /**
      * @return A solved board which has boxes on every goal and the player in
      * some unspecified position.
      */
     public static Board getSolvedBoard(Board b) {
         Board solved = new Board(b);
         List<Point> goalList = new ArrayList<>(solved.getGoalPoints());
         if (goalList.size() != solved.getDynamicObjects().size() - 1)
             throw new RuntimeException("The number of goals and boxes did not match while constructing a solved board.");
         // Map to store the pairs of points - cannot modify the mObjects set while
         // going through it.
         HashMap<Point, Point> pointMap = new HashMap<>();
         if (solved.get(solved.getPlayerPosition()) == Symbol.PlayerOnGoal){
             solved.moveElement(solved.getPlayerPosition(), solved.getFirstEmpty());
         }
         for (Point p : solved.getDynamicObjects().keySet()) {
             if (goalList.isEmpty())
                 break;
 //            System.out.println(p);
             Symbol pointSymbol = solved.get(p);
             if (pointSymbol == Symbol.Player){
                 // Ignore the player position
 //                System.out.println("player, ignoring");
                 continue;
             } else if (pointSymbol == Symbol.BoxOnGoal){
                 // If this is a box on a goal, the goal is filled, so remove it
                 // from the goal list and continue.
 //                System.out.println("this object is a box on a goal.");
                 goalList.remove(p);
                 continue;
             }
             Point goal;
             int i = -1;
             // go through the goal list until we find one that is unoccupied.
             while(solved.get((goal = goalList.get(++i))) == Symbol.BoxOnGoal);
             // remove the unoccupied goal from the list
             goalList.remove(i);
 //            System.out.println("putting box at " + p + " onto goal at " + goal);
             // Move the box we are looking at onto the empty goal position.
             pointMap.put(p, goal);
         }
         for (Point point : pointMap.keySet()) {
             solved.moveElement(point, pointMap.get(point));
         }
         
         return solved;
     }
     
     public static String actionListAsString(List<Action> aList){
         StringBuilder sb = new StringBuilder();
         for (Action action : aList) {
             sb.append(actionToString(action));
         }
         sb.append('\n');
         return sb.toString();
     }
 
 }
