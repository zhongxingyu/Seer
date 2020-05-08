 package board;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import utilities.SokobanUtil;
 import utilities.SokobanUtil.Action;
 import utilities.WeightedPoint;
 
 public class StaticBoard {
 
 	private static StaticBoard INSTANCE = null;
 	
 	/** Warning, grid[y][x] is the value at the point (x,y)*/
 	public final Symbol[][] grid; 
 	public final List<Point> goals;
     public final Map<Point, Map<Point, Integer> > goalDistanceCost;
 	
 	public static void init(Symbol[][] grid, List<Point> goals) {
 		INSTANCE = new StaticBoard(grid, goals);
 	}
 	
 	public static StaticBoard getInstance() {
 		if (INSTANCE == null) throw new RuntimeException("The static map has not been initialized");
 		return INSTANCE;
 	}
 	
 	private StaticBoard(Symbol[][] grid, List<Point> goals) {
 		this.grid = grid;
 		this.goals = Collections.unmodifiableList(goals);
         this.goalDistanceCost = new HashMap<>();
         generatePathCosts();
 	}
     
     /**
      * Fills the goalDistanceCost map with a map for each point on the board. Each point
      * maps to another map, which contains a mapping from goal points to integers
      * representing the cost of getting to the given point from the given goal.
      */
     private void generatePathCosts(){
         for (Point goal : goals) {
             Queue<WeightedPoint> open = new LinkedList<>();
             List<Point> closed = new ArrayList<>();
             
             WeightedPoint wp = new WeightedPoint(goal, 0);
             open.add(wp);
             while (!open.isEmpty()) { // while there are unexpanded points
                 WeightedPoint next = open.remove();
                 closed.add(next.point);
                 // Only consider walkable positions
                 if (get(next.point).isWalkable){
                     // Make sure the hashmap for this point is initialised
                     Map<Point, Integer> thisPoint = goalDistanceCost.get(next.point);
                     if (thisPoint == null){
                         thisPoint = new HashMap<>();
                         goalDistanceCost.put(next.point, thisPoint);
                     }
                     
                     // Put the goal node being checked along with the cost of getting to
                     // this position from that goal into the map for this point.
                    thisPoint.put(goal, next.cost);
                     closed.add(next.point);
                     // expand the neighbours of this point and add them to the
                     // open list if they have not yet been visited.
                     List<WeightedPoint> neighbours = expandPoint(next);
                     for (WeightedPoint neighbour : neighbours) {
                         if (!closed.contains(neighbour.point)){
                             open.add(neighbour);
                         }
                     }
                 }
             }
         }
     }
     
     private List<WeightedPoint> expandPoint(WeightedPoint p){
         List<WeightedPoint> freeNeighbours = new ArrayList<>();
 		for (Action a : Action.values()) {
 			WeightedPoint neighbour = new WeightedPoint(SokobanUtil.applyActionToPoint(a, p.point), p.cost + 1);
 			if (get(neighbour.point).isWalkable) {
 				WeightedPoint farneighbour = new WeightedPoint(SokobanUtil.applyActionToPoint(a, neighbour.point), neighbour.cost + 1);
 				if (get(farneighbour.point).isWalkable)
				{freeNeighbours.add(neighbour);}
 			}
 		}
 		return freeNeighbours;
     }
 	
 	public Symbol get(Point point) {
 		return get(point.x, point.y);
 	}
 	
 	public Symbol get(int x, int y) {
 		return grid[y][x];
 	}
 	
 	/** @return Ascii art representation of the map (static only) */
 	public String toString() {
 		return SokobanUtil.stringifyGrid(grid);
 	}
 	
 }
