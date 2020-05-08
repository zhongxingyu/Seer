 package board;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import search.Expandable;
 import search.SearchNode;
 import utilities.SokobanUtil;
 import utilities.SokobanUtil.Action;
 import board.Symbol.Type;
 import exceptions.IllegalMoveException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  * Dynamic representation of the world.
  * 
  * Disclaimer: The representation can become totally wrong as the number of box
  * is not checked, neither is the fact that there is only one player, etc. The
  * dynamic map must be modified with care.
  */
 public class Board implements Expandable<Board, Action>{
 	private final Map<Point, Symbol> mObjects;
 	private Point playerPosition;
 	private boolean hasLockedBox;
 
 	private Board(Map<Point, Symbol> dynMap, Point playerPosition) {
 		this.mObjects = dynMap;
 		this.playerPosition = playerPosition;
 		this.hasLockedBox = false;
 		
 		// Scan for locked state
 		for (Point p: dynMap.keySet()) {
 			if (get(p) != Symbol.Box) {
 				// Player is ignored, BoxOnGoal too.
 				continue;
 			}
 						
 			hasLockedBox = isBoxLockedAtPoint(p);
 			if (hasLockedBox) break;
 		}
 		
 	}
 	
 	protected Board(Board original) {
 		this.mObjects = new HashMap<>(original.mObjects);
 		this.playerPosition = original.playerPosition;
 		this.hasLockedBox = original.hasLockedBox;
 	}
 
 	public Map<Point, Symbol> getDynamicObjects() {
 		return mObjects;
 	}
 
 	/** No search needed, the position is now always tracked. */
 	public Point getPlayerPosition() {
 		return playerPosition;
 	}
 	
 	public boolean isInvalid() {
 		return hasLockedBox;
 	}
 
 	/** @return Ascii art representation of the board (static + dynamic) */
 	@Override
 	public String toString() {
 		return toStringMarked(null);
 	}
     
     /**
      * Returns a string representation of the board, with points in the
      * given list marked with an X.
      * @param toMark
      * @return 
      */
     public String toStringMarked(List<Point> toMark){
         Symbol[][] grid = StaticBoard.getInstance().grid;
 		Symbol[][] gridCopy = new Symbol[grid.length][];
 		for (int i = 0; i < grid.length; i++) {
 			gridCopy[i] = grid[i].clone();
 		}
 
 		for (Point p : mObjects.keySet()) {
 			gridCopy[p.y][p.x] = mObjects.get(p);
 		}
         
         if (toMark != null){
             for (Point p : toMark) {
                 gridCopy[p.y][p.x] = Symbol.Mark;
             }
         }
         
         return SokobanUtil.stringifyGrid(gridCopy);
     }
 
 	/**
 	 * Initializes a new board and the singleton static map.
 	 * 
 	 * @param inputStreamReader
 	 *            input source
 	 */
 	public static Board read(BufferedReader br) {
 
 		// Step 1: read the input.
 		List<String> tmpStrMap = new ArrayList<>();
 		String line;
 		try {
 			while (br.ready()) {
 				line = br.readLine();
 				tmpStrMap.add(line);
 			}
 		} catch (IOException e) {
 			// Crash the program, there is nothing much to do but give a better
 			// input source.
 			throw new RuntimeException(e);
 		}
 
 		// Step 2: create the structures to be stored.
 		// To have the number of lines and initialize the array, it must be done
 		// in two steps.
 		Symbol[][] staticMap = new Symbol[tmpStrMap.size()][];
 		Map<Point, Symbol> dynamicMap = new HashMap<>();
 		List<Point> goals = new ArrayList<>();
 		Point playerPosition = null;
 
 		for (int y = 0; y < tmpStrMap.size(); ++y) {
 			String row = tmpStrMap.get(y);
 			Symbol[] outRow = new Symbol[row.length()];
 			for (int x = 0; x < row.length(); ++x) {
 				Symbol s = Symbol.get(row.charAt(x));
 				if (s.type != Symbol.Type.None) {
 
 					outRow[x] = Symbol.get(s.staticValue);
 					dynamicMap.put(new Point(x, y), s);
 
 					if (s.type == Symbol.Type.Player) {
 						playerPosition = new Point(x, y);
 					}
 				} else {
 					outRow[x] = s;
 				}
 
 				if (s == Symbol.Goal || s == Symbol.BoxOnGoal) {
 					goals.add(new Point(x, y));
 				}
 			}
 
 			staticMap[y] = outRow;
 		}
 
 		if (playerPosition == null)
 			throw new RuntimeException("Player position not detected");
 
 		StaticBoard.init(staticMap, goals);
 		return new Board(dynamicMap, playerPosition);
 	}
 
 	/**
 	 * Returns the Symbol obtained by combining the static and dynamic values at
 	 * that point
 	 */
 	public Symbol get(Point p) {
 		if (mObjects.containsKey(p)) {
 			return mObjects.get(p);
 		} else {
 			return StaticBoard.getInstance().get(p);
 		}
 	}
 
 	/**
 	 * Moves the element (box or player only!) from one point to another.
 	 * 
 	 * @return whether operation was successful
 	 */
 	public boolean moveElement(Point from, Point to) {
 		if (!mObjects.containsKey(from))
 			return false;
 
 		Symbol.Type element = mObjects.get(from).type;
 		Symbol destination = StaticBoard.getInstance().get(to);
 		Symbol finalState;
 
 		if (destination.isWalkable && !mObjects.containsKey(to)) {
 			if (destination == Symbol.Empty) {
 				switch (element) {
 				case Player:
 					finalState = Symbol.Player;
 					break;
 				case Box:
 					finalState = Symbol.Box;
 					break;
 				default:
 					return false;
 				}
 			} else if (destination == Symbol.Goal) {
 				switch (element) {
 				case Player:
 					finalState = Symbol.PlayerOnGoal;
 					break;
 				case Box:
 					finalState = Symbol.BoxOnGoal;
 					break;
 				default:
 					return false;
 				}
 			} else {
 				return false;
 			}
 
 			mObjects.remove(from);
 			mObjects.put(to, finalState);
 			if (element == Symbol.Type.Player) {
 				playerPosition = to;
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Moves the player in one of the directions according to the chosen action.
 	 * If as a result he moves to a box, the box will be pushed.
 	 * 
 	 * @param a
 	 *            The action to be applied to the board
 	 * @param destructive
 	 *            If true, the action is applied directly to the board that the
 	 *            function is called on, destroying the previous board state. If
 	 *            false, the board is cloned and the action is applied to the
 	 *            cloned board
 	 * @throws Exception
 	 *             If it is impossible to apply the action (moving into a wall,
 	 *             moving into a box that can't be pushed)
 	 * @return The original object if destructive is false, a cloned board
 	 *         otherwise
 	 * */
 	public Board applyAction(Action a, boolean destructive)
 			throws IllegalMoveException {
 		Board newBoard;
 		if (destructive) {
 			newBoard = this;
 		} else {
 			newBoard = new Board(this);
 		}
 
 		Point player = newBoard.getPlayerPosition();
 
 		Point destination = SokobanUtil.applyActionToPoint(a, player);
 		Symbol destObject = newBoard.get(destination);
 		if (destObject.type == Type.Box) {
 			Point boxDestination = SokobanUtil.applyActionToPoint(a,
 					destination);
 			Symbol boxDest = newBoard.get(boxDestination);
             // If the destination is not walkable or it puts the board into a locked
             // state, then we don't want to do this action
 			if (!boxDest.isWalkable) {// || (boxDest != Symbol.Goal && isBoxLockedAtPoint(boxDestination))) {
 				throw new IllegalMoveException("Direction " + SokobanUtil.actionToString(a) 
                         + " is a box, but the box would become locked or there is a wall blocking it from being pushed.");
 			}
 			// Move the box first, and then the player, so that the box
 			// symbol is not overwritten.
 			newBoard.moveElement(destination, boxDestination);
 			newBoard.moveElement(player, destination);
 		} else if (!destObject.isWalkable) {
 			throw new IllegalMoveException("Direction " + SokobanUtil.actionToString(a) 
                     + " is not a box, and is not walkable.");
 		} else {
 			newBoard.moveElement(player, destination);
 		}
 
 		return newBoard;
 	}
 
 	/**
 	 * Applies a series of actions to the board
 	 * 
 	 * @param aList
 	 *            A list of actions to apply
 	 * @param destructive
 	 *            If true, modify the board state, otherwise use a clone.
 	 * @return A board with all actions in the actionList applied.
 	 */
 	public Board applyActionChained(ArrayList<Action> actionList,
 			boolean destructive) throws IllegalMoveException {
 		Board newBoard;
 		if (destructive) {
 			newBoard = this;
 		} else {
 			newBoard = new Board(this);
 		}
 
 		for (Action action : actionList) {
 			// Don't care about modifying board state anymore, so use the
 			// destructive method
 			newBoard.applyAction(action, true);
 		}
 
 		return newBoard;
 	}
     
     /**
      * Gets the points on this board which are accessible from the given point.
      * This is done using something like a flood fill.
      * @param p The point for which to find accessible points
      * @return The points which are accessible from the given point. Accessible
      * points are those which are walkable.
      */
     public List<Point> getAccessiblePoints(Point p){
         Queue<Point> q = new LinkedList<>();
         Board ref = new Board(this);
         q.add(p);
         List<Point> accessible = new ArrayList<>();
         accessible.add(p);
         while(!q.isEmpty()){
             Point next = q.remove();
             if (ref.get(next).isWalkable){
                 List<Point> neighbours = getFreeNeighbours(next);
                 for (Point point : neighbours) {
                     if (!accessible.contains(point)){
                         accessible.add(point);
                         q.add(point);
                     }
                 }
             }
         }
         
         return accessible;
     }
 	
 	/** 
 	 * Returns which of the points around the one provided are free.
 	 * No check is done on whether the point is a box, a wall or anything else.
 	 */
 	public List<Point> getFreeNeighbours(Point p) {
 		List<Point> freeNeighbours = new ArrayList<>();
 		for (Action a : Action.values()) {
 			Point neighbour = SokobanUtil.applyActionToPoint(a, p);
 			if (get(neighbour).isWalkable) {
 				freeNeighbours.add(neighbour);
 			}
 		}
 		return freeNeighbours;
 	}
 	
 	/** Returns the list of access points for each box*/
 	public Map<Point, List<Point>> getMapBoxAccessPoints() {
 		Map<Point, List<Point>> boxtToAccessPoints = new HashMap<>();
 		for (Point p : mObjects.keySet()) {
 			boxtToAccessPoints.put(p, getFreeNeighbours(p));
 			
 		}
 		return boxtToAccessPoints;
 		
 	}
 	
 	/** Returns the all the walkable that allow to be next to a box */
 	public Set<Point> getBoxAccessPoints() {
 		Set<Point> freeNeighbours = new HashSet<>();
 		for (Point p : mObjects.keySet()) {
 			if (get(p).type != Symbol.Type.Box) continue;
 
 			for (Action a : Action.values()) {
 				Point neighbour = SokobanUtil.applyActionToPoint(a, p);
 				if (get(neighbour).isWalkable) {
 					freeNeighbours.add(neighbour);
 				}
 			}
 		}
 		return freeNeighbours;
 	}
 	
 	/** Basic implementation: checks only that the box is not blocked against walls. 
 	 * Boxes are not considered to be blocking, and assumes that the player can get 
 	 * to the free neighbours 
 	 * Also, a box can be considered locked even on a goal (maybe you locked the wrong box there?)
 	 * You can test that it is on a goad before running this method (no point doing it the other way)
 	 */
 	public boolean isBoxLockedAtPoint(Point p) {
 		// A box is locked when it can't be pushed anymore. It happens when two adjacent 
 		// sides of the box are not walkable. 
 		
 		// TODO: that's a wierd way to do it. Any idea of a more easily understandable
 		// way to do it?
 		
 		List<Point> surroundingWalls = new ArrayList<>();
 		for (Action a : Action.values()) {
 			Point neighbour = SokobanUtil.applyActionToPoint(a, p);
 			if (get(neighbour) == Symbol.Wall) {
 				surroundingWalls.add(neighbour);
 			}
 		}
 		
 		if (surroundingWalls.size() > 2 ) return true;
 		if (surroundingWalls.size() < 2 ) return false;
 		
 		// Only left are the cases with 2 free sides. If they are opposite it's a tunnel, still manageable.
 		int sumX = surroundingWalls.get(0).x + surroundingWalls.get(1).x;
 		int sumY = surroundingWalls.get(0).y + surroundingWalls.get(1).y;
 		
 		// For points with only one other point in between, the sum of the x and y will be even.
 		// Half of it will give the x and y of the middle point.
		if (sumX % 2 != 0 || sumY != 0 ) return true;
 		return false;
 	}
 
     @Override
     public ArrayList<SearchNode<Board, Action>> expand(SearchNode<Board, Action> parent) {
         ArrayList<SearchNode<Board, Action>> expanded = new ArrayList<>();
         
         for (Action a : Action.values()) {
             try {
                 expanded.add(new SearchNode<>(this.applyAction(a, false), parent, a, 1));
             } catch (IllegalMoveException ex) {
 //                System.out.println(ex.getMessage());
             }
         }
         
         return expanded;
     }
 
     /**
      * Checks whether the given object is equal to this Board. The position
      * of the player is ignored in the check.
      * @param obj An object to check for equality. Should be a board.
      * @return true if obj is a Board, and the dynamic object hashmap contains
      * boxes at the same positions as this instance of Board. false if obj is not
      * a Board, or the objects in the dynamic map are in different locations, or
      * there is a different number of them.
      */
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Board){
             Board comp = (Board)obj;
             
 //            System.out.println("Checking whether board");
 //            System.out.println(comp.toString());
 //            System.out.println("is equal to this:");
 //            System.out.println(this.toString());
             
             Map<Point, Symbol> compObjects = comp.getDynamicObjects();
             Map<Point, Symbol> thisObjects = this.getDynamicObjects();
             // If one of the dynamic maps has more objects than the other, they
             // cannot be the same.
             if (compObjects.size() != thisObjects.size())
                 return false;
             
             for (Point p : thisObjects.keySet()) {
                 Symbol thisSymbol = thisObjects.get(p);
                 // Ignore the player in the check
 //                if (thisSymbol == Symbol.Player || thisSymbol == Symbol.PlayerOnGoal)
 //                    continue;
                 // Get the symbol at the point on this board
                 Symbol compSymbol = compObjects.get(p);
                 if (compSymbol != null && thisSymbol == compSymbol)
                     continue; // The symbols at the two points match
                 
                 // If the dynamic map of the compared board does not contain the
                 // point we are looking at, or the symbols do not match, then
                 // the boards are not equal.
                 return false;
             }
             
             // If we get through the whole keySet without returning, then they
             // contain the same keys
             return true;
         }
         // obj is not an instance of Board.
         return false;
     }
 }
