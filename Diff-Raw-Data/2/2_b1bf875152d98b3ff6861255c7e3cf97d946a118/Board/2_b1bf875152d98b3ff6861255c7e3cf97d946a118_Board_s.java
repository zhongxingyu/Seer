 package board;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import pathfinding.BoardAction;
 import search.AStar;
 import search.Heuristic;
 import search.SearchMethod;
 import search.SearchNode;
 import utilities.SokobanUtil;
 import utilities.SokobanUtil.Action;
 import utilities.WeightedPoint;
 import board.Symbol.Type;
 import exceptions.IllegalMoveException;
 import utilities.Pair;
 
 /**
  * Dynamic representation of the world.
  * 
  * Disclaimer: The representation can become totally wrong as the number of box
  * is not checked, neither is the fact that there is only one player, etc. The
  * dynamic map must be modified with care.
  */
 public class Board {
     
     private static final Action pushableTestDirections[] = {Action.UP, Action.LEFT};
 	protected final Map<Point, Symbol> mObjects;
 	private Point playerPosition;
 	public List<List<Point>> availablePosition  = new ArrayList<List<Point>>();
     // Top left most position accessible from the player position.
     private Point topLeftPosition;
     private ArrayList<Pair<BoardAction, Integer> > possibleActions = null;
     private boolean floodFillRequired = true;
     private String stringHash = null;
     /** Used only for profiling */
     public static int lockedStatesIgnored = 0;
 
 	private Board(Map<Point, Symbol> dynMap, Point playerPosition) {
 		this.mObjects = dynMap;
 		this.playerPosition = playerPosition;
 		floodFillRequired = true;
 	}
 	
 	public Board(Board original) {
 		this.mObjects = new HashMap<>(original.mObjects);		//TODO is a shallow copy enough?
 		this.playerPosition = new Point(original.playerPosition);
         this.topLeftPosition = original.topLeftPosition;
         floodFillRequired = original.floodFillRequired;
 	}
 
 	public Map<Point, Symbol> getDynamicObjects() {
 		return mObjects;
 	}
 
 	/** No search needed, the position is now always tracked. */
 	public Point getPlayerPosition() {
 		return playerPosition;
 		
 	}
  
 	public void setAvailablePosition(){
 		try{
 		List<Point> Goals = StaticBoard.getInstance().goals;
 		for(int i = 0 ; i < Goals.size() ; i++){
 			 availablePosition.add(getAccessiblePointsfromGoal(Goals.get(i)));
 		}
 		}catch(Exception e){
 			
 		}
 	}
 	
 	public List<Point> getGoalPoints(){
 		return StaticBoard.getInstance().goals;
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
 		List<String> tmpStrMap = new ArrayList<String>();
 		String line;
 		try {
 			while (br.ready()) {
 				line = br.readLine();
 				if (line == null) break; // happens when the source is a StringReader
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
 
         int maxX = -1; // x dimension depends on the longest string.
         int maxY = tmpStrMap.size(); // the y dimension is the number of strings we have
 		for (int y = 0; y < tmpStrMap.size(); ++y) {
 			String row = tmpStrMap.get(y);
             int rowLength = row.length();
             // update the maximum x dimension
             if (rowLength > maxX)
                 maxX = rowLength;
 			Symbol[] outRow = new Symbol[rowLength];
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
 
 				if (s == Symbol.Goal || s == Symbol.BoxOnGoal|| s == Symbol.PlayerOnGoal) {
 					goals.add(new Point(x, y));
 				}
 			}
 
 			staticMap[y] = outRow;
 		}
 
 		if (playerPosition == null)
 			throw new RuntimeException("Player position not detected");
 
 		StaticBoard.init(staticMap, goals, new Dimension(maxX, maxY));
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
 			floodFillRequired = true;
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
 		Board newBoard = destructive ? this : new Board(this);
 
         Point player = newBoard.getPlayerPosition();
 
 		Point destination = SokobanUtil.applyActionToPoint(a, player);
 		Symbol destObject = newBoard.get(destination);
 //		System.out.println(newBoard);
 //		System.out.println("dest object " + destObject);
 		if (destObject.type == Type.Box) {
 			Point boxDestination = SokobanUtil.applyActionToPoint(a,
 					destination);
 			Symbol boxDest = newBoard.get(boxDestination);
             // If the destination is not walkable or it puts the board into a locked
             // state, then we don't want to do this action
 			if (!boxDest.isWalkable || (boxDest != Symbol.Goal && isBoxLocked(boxDestination))) {
 				throw new IllegalMoveException("Direction " + SokobanUtil.actionToString(a) 
                         + " is a box, but the box would become locked or there is a wall "
                         + "blocking it from being pushed.");
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
 	public Board applyActionChained(List<Action> actionList,
 			boolean destructive) throws IllegalMoveException {
 		Board newBoard = destructive ? this : new Board(this);
 
 		for (Action action : actionList) {
 			// Don't care about modifying board state anymore, so use the
 			// destructive method
 			newBoard.applyAction(action, true);
 		}
 
 		return newBoard;
 	}    
 
 	public Board applyPullAction(Action a, boolean destructive, Point Goal)
 			throws IllegalMoveException {
 		Board newBoard;
 		if (destructive) {
 			newBoard = this;
 		} else {
 			newBoard = new Board(this);
 		}
 
 //		System.out.println(a.toString());
 //		System.out.println(Goal.toString());
 		Point destination = SokobanUtil.applyActionToPoint(a, Goal);
 		Symbol destObject = newBoard.get(destination);
 //		System.out.println(destObject.toString());
 		if (destObject.equals(Symbol.Wall)) {
 			System.out.println("Cannot be reached");
 			throw new IllegalMoveException("Cannot be reached");
 		} else {
 			Point onestepmore = SokobanUtil.applyActionToPoint(a, destination);
 			Symbol onestepmoreObject = newBoard.get(onestepmore);
 			if (onestepmoreObject.equals(Symbol.Wall)) {
 				System.out.println("Cannot be reached as deadlock");
 				throw new IllegalMoveException("Cannot be reached");
 			}
 
 			newBoard.moveElement(Goal, destination);
 		}
 
 		return newBoard;
 	}	
 	public Board reverseAction(Action a, boolean destructive) throws IllegalMoveException {
 		Board newBoard = destructive ? this : new Board(this);
 
 		Point player = newBoard.getPlayerPosition();
 		
 		Point previousPosition = SokobanUtil.applyActionToPoint(SokobanUtil.inverseAction(a), player);
 		Point pushedBoxPosition = SokobanUtil.applyActionToPoint(a, player);
 		
 		Symbol previousPositionSymbol = newBoard.get(previousPosition); 
 		if (!previousPositionSymbol.isWalkable) {
 			throw new IllegalMoveException("Unable to revert the action '" + a.name() + "': State at point (" 
 					+ previousPosition.x + "," + previousPosition.y + ") is :" + previousPositionSymbol);
 		}
 		
 		newBoard.moveElement(player, previousPosition);
 		
 		if (get(pushedBoxPosition).type == Symbol.Type.Box) {
 			newBoard.moveElement(pushedBoxPosition, player);
 		}
 
 		return newBoard;
 	}
 	
 	public Board reverseActionChained(List<Action> actions, boolean destructive) 
 			throws IllegalMoveException {
 		Board newBoard = destructive ? this : new Board(this);
 		
 		List<Action> reversedActionList = new ArrayList<>(actions);
 		Collections.reverse(reversedActionList);
 
 		for (Action action : reversedActionList) {
 			// Don't care about modifying board state anymore, so use the
 			// destructive method
 			newBoard.reverseAction(action, true);
 		}
 
 		return newBoard;
 	}
 	
 	
     
     public ArrayList<Pair<BoardAction, Integer>> getPossibleActions()
 	{
     	if (floodFillRequired)
     		floodFillInitialise();
     	
 		return possibleActions;
 	}
 
 	/**
      * Initialises the top left position and the list of actions that can be taken
      * from the current position of the player, in terms of a flood fill. This means that
      * all boxes that are in the accessible area which can be pushed are checked for which
      * actions can be applied to them.
      */
     private void floodFillInitialise(){
         System.out.println(this);
         possibleActions = new ArrayList<>();
         Queue<Pair<Point,Integer>> q = new LinkedList<>();
         // We start the fill at the player position, which has cost zero.
         Pair<Point, Integer> start = new Pair(playerPosition, 0);
         // The queue stores points which we have not yet expanded.
 		q.add(start);
         ArrayList<Point> accessible = new ArrayList<>();
         accessible.add(start.first);
 		// Track the minimum values of point positions so that we can see which
 		// point is the top left of the flood filled region
 		Point minPoint = playerPosition;
         Point neighbours[] = new Point[4];
         int count = 0;
 		while (!q.isEmpty()) { // Continue until there are no more points to expand
 			Pair<Point, Integer> next = q.remove();
 //            System.out.println("next point " + next);
             // If the point is walkable, then we need to examine its neighbours.
             // Otherwise, we cannot move to that point, so we ignore it.
 			if (this.get(next.first).isWalkable) {
                 // Add each neighbour point to the neighbour array by applying
                 // each possible action to the point currently being evaluated.
                 // The order of evaluation is UP, DOWN, LEFT, RIGHT
                 for (Action action : Action.values()) {
 //                    System.out.println("Applying action " + action + " to point " + next.first);
                     neighbours[count++] = SokobanUtil.applyActionToPoint(action, next.first);
 //                    System.out.println("Point " + (count - 1) + " is " + neighbours[count - 1]);
                 }
                 count = 0; // reset the count so that next loop we refill from zero.
                 // We look at each point to determine whether we should add it to the
                 // list of accessible positions (if it is walkable), or what action
                 // we can take to push a neighbouring box.
                 for (int i = 0; i < 4; i++) {
                     Symbol thisSym = get(neighbours[i]);
 //                    System.out.println("Analysing point " + i + ", " + neighbours[i]);
 //                    System.out.println("Symbol: " + thisSym);
                     if (!accessible.contains(neighbours[i])){
 //                        System.out.println("Not in closed list.");
                         if (thisSym.isWalkable){
                             // Only add the neighbour to the list of accessible points
                             // if it is a walkable space. Otherwise we will not look
                             // at box locations more than once.
                             accessible.add(neighbours[i]);
 //                            System.out.println("Is walkable.");
                             // Add the neighbour to the queue to be examined, with
                             // a cost that is one greater than the cost to get to the
                             // currently examined point
                             q.add(new Pair(neighbours[i], next.second + 1));
                             // See if the neighbour we are looking at is smaller than
                             // the current minimum (top leftmost) point.
                             minPoint = SokobanUtil.pointMin(minPoint, neighbours[i]);
                         } else if (thisSym.type == Type.Box){
 //                            System.out.println("Is a box.");
                             // We do not want to add boxes to the accessible points
                             // list, but we need to add them to the list of accessible
                             // boxes along with the action that was applied to get to
                             // to that box. This is an action that we might be able to
                             // apply to the box, but we don't check that now.
                             possibleActions.add(new Pair(new BoardAction(Action.values()[i], neighbours[i]), next.second + 1));
 //                            System.out.println("Possible actions now " + possibleActions);
                         }
                     }
                 }
 
             }
             floodFillRequired = false;
 		}
 
 		// Move the point with the minimum value to the beginning of the list.
         topLeftPosition = minPoint;
     }
     
 	/**
 	 * Gets the points on this board which are accessible from the given point.
 	 * This is done using something like a flood fill.
 	 * 
 	 * @param p
 	 *            The point for which to find accessible points
 	 * @return The points which are accessible from the given point. Accessible
 	 *         points are those which are walkable. The top left most point in
 	 *         the accessible area will be the first element of the list.
 	 */
 	public List<Point> getAccessiblePoints(Point p) {
 		Queue<Point> q = new LinkedList<>();
 		q.add(p);
 		List<Point> accessible = new ArrayList<>();
 		accessible.add(p);
 		// Track the minimum values of point positions so that we can see which
 		// point is the top left of the flood filled region
 		Point minPoint = p;
 		while (!q.isEmpty()) {
 			Point next = q.remove();
 			if (this.get(next).isWalkable) {
 				List<Point> neighbours = getFreeNeighbours(next);
 				for (Point point : neighbours) {
 					if (!accessible.contains(point)) {
 						accessible.add(point);
 						q.add(point);
 						// Modify the minimum point location if the current
 						// point
 						// is "lower" than the current minimum
 						minPoint = SokobanUtil.pointMin(minPoint, point);
 						// System.out.println("min point is: " + minPoint);
 					}
 				}
 			}
 		}
 
 		// Move the point with the minimum value to the beginning of the list.
 		accessible.remove(minPoint);
 		accessible.add(0, minPoint);
 
 		return accessible;
 	}
     
 	// Get the reachable point from one point by pulling
 	public List<Point> getAccessiblePointsfromGoal(Point p) {
 		Queue<Point> q = new LinkedList<>();
 		q.add(p);
 		List<Point> accessible = new ArrayList<>();
 		accessible.add(p);
 		while (!q.isEmpty()) {
 			Point next = q.remove();
 			for (Action a : Action.values()) {
 				try {
 					applyPullAction(a, true, next);
 					Point ReachPoint = SokobanUtil.applyActionToPoint(a, next);
 					System.out.print("Reach:" + ReachPoint.toString());
 					
 					if(!accessible.contains(ReachPoint))
 						{accessible.add(ReachPoint);
 						q.add(ReachPoint);}
 					
 				} catch (IllegalMoveException e) {
 					
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
 			if (get(p).type != Symbol.Type.Box) continue;
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
 
 	/**
 	 * Gets a map of points to actions which indicate the directions in which
 	 * each box on the map can be pushed. Assuming that the player is able to
 	 * teleport, a box can always be pushed in either 2 or 4 directions. If you
 	 * can push it in one direction, then the player must be able to access the
 	 * point that is being pushed from, and if the box is pushable in that
 	 * direction, it means that the space it is being pushed into is empty,
 	 * which means that the box could be pushed from that direction as well.
 	 * 
 	 * @return A map of points, indicating box locations, with a list of actions
 	 *         indicate which direction the box can successfuly be pushed from.
 	 *         Boxes which cannot be pushed are not included in the map.
 	 */
 	public Map<Point, List<Action>> getBoxPushableDirections() {
 		Map<Point, List<Action>> pushableDirections = new HashMap<>();
 		for (Point p : mObjects.keySet()) {
             Symbol thisPos = get(p);
             if (thisPos == Symbol.Player || thisPos == Symbol.PlayerOnGoal)
 				continue;
 			List<Action> possiblePushDirections = getSingleBoxPushableDirections(p);
 			pushableDirections.put(p, possiblePushDirections);
 		}
 		return pushableDirections;
 	}
     
     /**
      * Gets a list of actions which can be applied to a box at the given point.
      * @param p A point, which should contain a box symbol.
      * @return A list of actions which can be applied to the box at the point
      * in question.
      */
     public List<Action> getSingleBoxPushableDirections(Point p){
         Symbol pSymb = get(p);
         
         if (pSymb != Symbol.Box && pSymb != Symbol.BoxOnGoal){
             throw new IllegalArgumentException("The point provided must contain a box.");
         }
         List<Action> possiblePushDirections = new ArrayList<>();
         for (Action a : pushableTestDirections) {
             Point neighbour = SokobanUtil.applyActionToPoint(a, p);
             Point opposite = SokobanUtil.applyActionToPoint(
                     SokobanUtil.inverseAction(a), p);
             // If you can push the box in one direction, then you can push
             // it in the opposite
             // direction as well. Boxes can only be pushed in either 2 or 4
             // directions, assuming
             // that the player is able to teleport.
             if (get(neighbour).isWalkable && get(opposite).isWalkable){
                 possiblePushDirections.add(a);
                 possiblePushDirections.add(SokobanUtil.inverseAction(a));
             }
         }
         
         return possiblePushDirections;
     }
 
 	/**
 	 * Basic implementation: checks only that the box is not blocked against
 	 * walls. Boxes are not considered to be blocking, and assumes that the
 	 * player can get to the free neighbours Also, a box can be considered
 	 * locked even on a goal (maybe you locked the wrong box there?) You can
 	 * test that it is on a goad before running this method (no point doing it
 	 * the other way)
 	 */
     @Deprecated
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
 		if (sumX % 2 != 0 || sumY %2 != 0 ) return true;
 		return false;
 	}
 	
 	/**
 	 * Checks if the state contains any locked box (statically or dynamically locked).
 	 * A box locked on a goal is not considered locked.
 	 * 
 	 * @return If true, means that the state and its childs won't lead to any solution.
 	 */
     public boolean isLockedState()
     {
     	for (Point p : mObjects.keySet())
     	{
     		if (p != playerPosition)			//escaping player
     		{
     			if (get(p) != Symbol.BoxOnGoal)	//escaping boxes on goals
     			{
     				if (isBoxLocked(p))			//if any of the boxes is locked, the state is locked
     					return true;
     			}
     		}
     	}
     	return false;
     }
     
     
 	/** Used to check if a box is locked, statically (by walls, in a corner for example) or dynamically (by surrounding boxes configuration).
 	 * 	If called on a cell that's not containing a box, will return false.
 	 * 
 	 * @param p Point that we want to check
 	 * @return true if the box at point p is locked for ever, by walls and/or boxes configuration.
 	 */
 	public boolean isBoxLocked(Point p)
 	{
 		//functor class used to hide the recursive method
 		class Functor
 		{
 			boolean isMovable(Point p, HashSet<Point> exploredPoints)
 			{
 				Symbol sym = Board.this.get(p);
 				if (sym == Symbol.Wall)
 					return false;
 				else if (sym.type == Symbol.Type.Box)
 				{
 					if (exploredPoints.contains(p))
 						return false;
 					else
 					{
 						exploredPoints.add(p);
 						Point upPoint = new Point(p.x,p.y-1);
 						Point downPoint = new Point(p.x,p.y+1);
 						Point leftPoint = new Point(p.x-1,p.y);
 						Point rightPoint = new Point(p.x+1,p.y);
 						return (isMovable(leftPoint,exploredPoints) && isMovable(rightPoint,exploredPoints) || 	//horizontally free
 								isMovable(upPoint,exploredPoints) && isMovable(downPoint,exploredPoints));		//vertically free
 					}
 				}
 				else
 					return true;
 			}	
 		};
 		
 		Functor fun = new Functor();
 		
 		//if there's no box on point p, return false
 		if (this.get(p).type != Symbol.Type.Box)
 			return false;
 		
 		//initialize recursion
 		HashSet<Point> exploredPoints = new HashSet<Point>();
 		return StaticBoard.isLocked(p) || !fun.isMovable(p,exploredPoints);
 
 	}
 	
 
     
     public ArrayList<SearchNode> expandPlayerSpace(SearchNode parent) {
         ArrayList<SearchNode> expanded = new ArrayList<>();
         
         for (Action a : Action.values()) {
             try {
                 expanded.add(new SearchNode(this.applyAction(a, false), parent, new BoardAction(a, playerPosition), 1, false));
             } catch (IllegalMoveException ex) {
 //                System.out.println(ex.getMessage());
             }
         }
         
         return expanded;
     }
     
     public ArrayList<SearchNode> expandBoardSpace(SearchNode parent){
     	ArrayList<SearchNode> nodes = new ArrayList<>();
     	ArrayList<Pair<BoardAction,Integer> > boxList = getPossibleActions();
     	for (Pair<BoardAction,Integer> boxAction : boxList)
     	{
     		int dx = boxAction.first.action.dx;	//push direction
     		int dy = boxAction.first.action.dy;
     		Point boxPos = boxAction.first.position;
     		Point pushPos = new Point(boxPos.x - dx, boxPos.y - dy);	//position where the player stands before pushing the box
     		Point finalPos = new Point(boxPos.x + dx, boxPos.y + dy);
     		if (get(finalPos).type != Symbol.Type.Box || get(finalPos) != Symbol.Wall)
     		{
     			//Generating new Board
 				Board newBoard = new Board(this);
 				
 				newBoard.moveElement(playerPosition, pushPos); //moving player to pushing point
 				newBoard.moveElement(boxPos, finalPos); //moving crate
 				newBoard.moveElement(pushPos, boxPos);//moving player to it's end location
 				//if the board is a locked state, just ignore it
 				if (!newBoard.isLockedState())
 				{
 		    		nodes.add(new SearchNode(newBoard, parent, boxAction.first, boxAction.second, true));
 				}
 				else
 				{
 					++lockedStatesIgnored;
 				}
     		}
     	}
     	return nodes;
     }
     
     
     public ArrayList<SearchNode> expandBoardSpaceBackwards(SearchNode parent){
     	ArrayList<SearchNode> nodes = new ArrayList<>();
     	ArrayList<Pair<BoardAction,Integer> > boxList = getPossibleActions();
     	for (Pair<BoardAction,Integer> boxAction : boxList)
     	{
     		int dx = boxAction.first.action.dx;	//push direction
     		int dy = boxAction.first.action.dy;
     		Point boxPos = boxAction.first.position;
     		Point pushPos = new Point(boxPos.x - 2*dx, boxPos.y - 2*dy);	//position where the player stands before pulling the box
     		Point finalPos = new Point(boxPos.x - dx, boxPos.y - dy);
    		if (get(finalPos).type == Symbol.Type.Box || get(finalPos) == Symbol.Wall)
     		{
     			//Generating new Board
     			Board newBoard = new Board(this);
     			
     			newBoard.moveElement(playerPosition, pushPos); //moving player to pushing point
     			newBoard.moveElement(boxPos, finalPos); //moving crate
     			
     			//if the board is a locked state, just ignore it
     			if (!newBoard.isLockedState())
     			{
     				nodes.add(new SearchNode(newBoard, parent, boxAction.first, boxAction.second, true));
     			}
     			else
     			{
     				++lockedStatesIgnored;
     			}
     		}
     	}
     	return nodes;
     }
     
     
     public Point getTopLeftPosition()
     {
     	if (floodFillRequired)
     		floodFillInitialise();
 
     	return topLeftPosition;
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
             
             Map<Point, Symbol> compObjects = comp.getDynamicObjects();
             Map<Point, Symbol> thisObjects = this.getDynamicObjects();
             // If one of the dynamic maps has more objects than the other, they
             // cannot be the same.
             if (compObjects.size() != thisObjects.size()){
                 return false;
             }
             
             for (Point p : thisObjects.keySet()) {
                 Symbol thisSymbol = thisObjects.get(p);
 
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
     
     /**
      * Checks the equality of this and another object. This method ignores the 
      * exact position of the player and instead takes into account only the player's
      * reachable area. The player is represented by the the top left most position
      * in this area. If both boards have the same for this position and all the
      * boxes are also in the same position, the boards are considered equal.
      * @param obj An object to which to compare this board.
      * @return Whether the two objects are equivalent.
      */
     public boolean equalsPlayerFill(Object obj){
         if (obj instanceof Board){
             Board comp = (Board)obj;
 //            System.out.println("comparing ignore player");
             // The top left positions accessible to the player must be the same
             // if the boards are to be equal.
             if (!this.getTopLeftPosition().equals(comp.getTopLeftPosition()))
                 return false;
 //            System.out.println("Top left positions match");
             Map<Point, Symbol> compObjects = comp.getDynamicObjects();
             Map<Point, Symbol> thisObjects = this.getDynamicObjects();
             // If one of the dynamic maps has more objects than the other, they
             // cannot be the same.
             if (compObjects.size() != thisObjects.size()){
                 return false;
             }
 //            System.out.println("Dynamic map sizes match");
             for (Point p : thisObjects.keySet()) {
                 Symbol thisSymbol = thisObjects.get(p);
                 // Ignore the player in the check
                 if (thisSymbol == Symbol.Player || thisSymbol == Symbol.PlayerOnGoal)
                     continue;
 //                System.out.println("symbol is not player");
                 // Get the symbol at the point on this board
                 Symbol compSymbol = compObjects.get(p);
                 if (compSymbol != null && thisSymbol == compSymbol)
                     continue; // The symbols at the two points match
 //                System.out.println("symbols at point do not match");
                 // If the dynamic map of the compared board does not contain the
                 // point we are looking at, or the symbols do not match, then
                 // the boards are not equal.
                 return false;
             }
 //            System.out.println("all keys matched.");
             // If we get through the whole keySet without returning, then they
             // contain the same keys
             return true;
         }
         
 //        System.out.println("non-board object");
         // obj is not an instance of Board.
         return false;
     }
     
     /**
      * Uses the stringhash object to decide whether two boards are equal.
      * @param obj 
      */
     public boolean equalsHash(Object obj){
         if (obj instanceof Board){
             Board other = (Board)obj;
             return other.getStringHash().equals(this.getStringHash());
         }
         return false;
     }
     
     public Board prepareNextBoxMove(Action action, Point movedBox, boolean destructive) throws IllegalMoveException {
     	Board newBoard = destructive ? this : new Board(this);
     	
     	// No check here, we just teleport the player.
     	newBoard.moveElement(playerPosition, SokobanUtil.applyActionToPoint(SokobanUtil.inverseAction(action), movedBox));
     	
     	return newBoard;
     }
 
     @Override
     public int hashCode() {
 //        int result = 29;
 //        int code = 0;
 //        
 //        code += mObjects.hashCode();
 //        
 //        return 37 * result + code;
         return getStringHash().hashCode();
     }
 
     public String getStringHash() {
         if (stringHash == null)
             makeStringHash();
         return stringHash;
     }
     
     public void makeStringHash(){
         Dimension boardDim = StaticBoard.getInstance().mapDim;
 //        System.out.println("Board dim " + boardDim.width + "," + boardDim.height);
         
         // Make a char array which can contain all squares on the board, and then
         // fill it with spaces so that when converted to a string the blanks are
         // there. Without the fill, null characters are not displayed.
         char base[] = new char[boardDim.height * boardDim.width];
         Arrays.fill(base, ' ');
 
         // Initialise the player position as the top left position of the board.
         // The highest current index is also the index of this position.
         Point topLeft = this.getTopLeftPosition();
         int highestIndex = topLeft.y * boardDim.width + topLeft.x;
 
         // The player is positioned at the top left accessible position, and we
         // ignore the actual position
         base[highestIndex] = '@';
         for (Point point : this.mObjects.keySet()) {
 //            System.out.println("Point is " + point);
             int index;
             char posType = this.get(point).type.toChar();
             if (posType == '@') // Skip the actual player position in the creation
                 continue;
             // Set the index of this position in the char array to the char which
             // represents the symbol type.
             base[(index = point.y * boardDim.width + point.x)] = posType;
             // Keep track of the highest index so that we know when to cut off.
             if (index > highestIndex)
                 highestIndex = index;
 //            System.out.println("Index is " + index);
         }
         
         // We don't care about anything past the last position which contains a dynamic object
         String hash = new String(base, 0, highestIndex + 1);
 //        System.out.println("Final hash " + hash);
         stringHash = hash;
     }
 
     /**
      * 
      * Gets the first empty point on this board.
      * @return The first empty point on the board, or null if there are no such 
      * points.
      */
     public Point getFirstEmpty(){
         Symbol[][] boardState = StaticBoard.getInstance().grid;
         for (int y = 0; y < boardState.length; y++) {
             for (int x = 0; x < boardState[y].length; x++) {
                 if (boardState[y][x] == Symbol.Empty)
                     return new Point(y,x);
             }
         }
         return null;
     }
     
     /** 
      * Method to be called on the initial board after the main tree search. 
      * Builds the complete list of actions, by generating the ones for the player's
      * movements between the boxes. 
      * @throws IllegalMoveException 
      */
     public List<Action> generateFullActionList(List<BoardAction> boxActions) 
     		throws IllegalMoveException {
     	
     	Board intermediateBoard;
     	Board currentBoard = new Board(this);
     	
     	boolean doubleCheck = true; // Double check mode: actually execute all the steps before adding them to the list.
     	
     	
     	SearchMethod aStar = new AStar(new Heuristic.DiagonalDistanceHeuristic());
     	List<Action> completeActionList = new ArrayList<>();
     	for (BoardAction bm : boxActions) { // loop through all the box actions
     		// Get the board state after that action.
     		intermediateBoard = currentBoard.prepareNextBoxMove(bm.action, bm.position, false);
     		
     		if (! currentBoard.equals(intermediateBoard)) { // the player moved in-between. 
     			// get the list of moves made.
     			ArrayList<BoardAction> foundPath = aStar.findPath(currentBoard, intermediateBoard, false);
     			
     			if (doubleCheck) {
     				currentBoard.applyActionChained(BoardAction.convertToActionList(foundPath), true);
     				if (! currentBoard.equals(intermediateBoard)) 
     					throw new RuntimeException("The intermediate path obtained is not valid.");
     				
     			}
     			
     			completeActionList.addAll(BoardAction.convertToActionList(foundPath));
     		}
             
     		// Now push the box
     		if (doubleCheck) {
     			currentBoard.applyAction(bm.action, true);
     		} else {
     			currentBoard = intermediateBoard.applyAction(bm.action, true);
     		}
     		
     		completeActionList.add(bm.action);
     		
     	}
     	
     	return completeActionList;
     }
     
     
     
     /**
      * Returns a list of Boards that are the results of the possible moves regarding the player and the crates position.
      * A new state means that a box has been moved and that the player position has changed.
      * Checks if the box is reachable, and if the move is valid.
      * @return List of boards that can be resulting from this state.
      * @param rRelatedBoxMovements if not null, will be filled with the BoxMovements that led to the child states.
      * @param rRelatedCosts if not null, will be filled with the cost (player moves count) to reach each child state.
      */
     public ArrayList<Board> generateChildStates(ArrayList<BoardAction> rRelatedBoxMovements, ArrayList<Integer> rRelatedCosts)
     {  	
     	ArrayList<Board> result = new ArrayList<>();
 
     	//Visitable positions will go through openList and end up in closedList.
     	LinkedList<WeightedPoint> openList = new LinkedList<>();
     	LinkedList<WeightedPoint> closedList = new LinkedList<>();
     	
     	
     	//initialise openList with playerPosition.
     	WeightedPoint start = new WeightedPoint(playerPosition, 0);
     	openList.add(start);
     	
     	
     	while (!openList.isEmpty())
     	{
     		WeightedPoint center = openList.removeFirst();
     		closedList.add(center);
 //    		System.out.println("center x:"+center.point.x + " y:" + center.point.y);
     		
     		WeightedPoint[] neighbours = new WeightedPoint[4];
     		
     		neighbours[0] = new WeightedPoint(center.point.x, center.point.y-1, center.cost+1);	//UP
     		neighbours[1] = new WeightedPoint(center.point.x, center.point.y+1, center.cost+1);	//DOWN
     		neighbours[2] = new WeightedPoint(center.point.x-1, center.point.y, center.cost+1);	//LEFT
     		neighbours[3] = new WeightedPoint(center.point.x+1, center.point.y, center.cost+1);	//RIGHT
     		
     		for (WeightedPoint neighbour : neighbours)
     		{
     			Symbol nSymb = this.get(neighbour.point);
 //    			System.out.println("at x:"+ neighbour.point.x + " y:" + neighbour.point.y +"symbol: "+ "'"+ nSymb +"'");
 	    		if (nSymb != Symbol.Wall && nSymb.type != Symbol.Type.Box)
 	    		{
 	    			//adding open neighbour only if not visited and not already in openList
 	    			if (!openList.contains(neighbour) && !closedList.contains(neighbour))
 	    			{
 	        			openList.addLast(neighbour);
 	    			}
 	    		}
 	    		else if (nSymb.type == Symbol.Type.Box)
 	    		{
 	    			//check if push is possible
 	    			int dirX = neighbour.point.x - center.point.x;
 	    			int dirY = neighbour.point.y - center.point.y;
 	    			Point endLocation = new Point(neighbour.point.x + dirX, neighbour.point.y + dirY);
                     Symbol endSym = this.get(endLocation);
                     if (endSym != Symbol.Wall // cannot push into walls or other boxes
                             && endSym.type != Symbol.Type.Box)
                             // Don't push boxes onto locked points, unless the point is a goal
                             // && (!isBoxLocked(endLocation) || endSym == Symbol.Goal))
 	    			{
 	    				//Generating new Board
 	    				Board newBoard = new Board(this);
 	    				
 	    				newBoard.moveElement(playerPosition, center.point); //moving player to pushing point
 	    				newBoard.moveElement(neighbour.point, endLocation); //moving crate
 	    				newBoard.moveElement(center.point, neighbour.point);//moving player to it's end location
 	    				
 	    				//if the board is a locked state, just ignore it
 	    				if (!newBoard.isLockedState())
 	    				{
 //                            newBoard.makeStringHash();
 		    				result.add(newBoard);
 		    				
 		    				//Generating BoxMovement
 		    				if (rRelatedBoxMovements != null)
 		    				{
 		    					Action action;
 		    					if (dirX>0)
 		    						action = Action.RIGHT;
 		    					else if (dirX<0)
 		    						action = Action.LEFT;
 		    					else if (dirY>0)
 		    						action = Action.DOWN;
 		    					else	//if (dirY<0)
 		    						action = Action.UP;
 		    					BoardAction move = new BoardAction(action, neighbour.point);
 		    					rRelatedBoxMovements.add(move);
 		    				}
 		    				
 		    				if(rRelatedCosts != null)
 		    				{
 		    					rRelatedCosts.add(new Integer(center.cost+1));
 		    				}
 	    				}
 	    				else
 	    				{
 	    					++lockedStatesIgnored;
 	    				}
 	    			}
 	    		}
     		}
     	}
     	return result;
     }
     
 }
 
 //  public ArrayList<Board> generateChildStates(ArrayList<BoardAction> rRelatedBoxMovements, ArrayList<Integer> rRelatedCosts)
 //    {  	
 //    	ArrayList<Board> result = new ArrayList<>();
 //
 //    	//Visitable positions will go through openList and end up in closedList.
 //    	LinkedList<WeightedPoint> openList = new LinkedList<>();
 //    	LinkedList<WeightedPoint> closedList = new LinkedList<>();
 //    	
 //    	
 //    	//initialise openList with playerPosition.
 //    	WeightedPoint start = new WeightedPoint(playerPosition, 0);
 //    	openList.add(start);
 //    	
 //    	
 //    	while (!openList.isEmpty())
 //    	{
 //    		WeightedPoint center = openList.removeFirst();
 //    		closedList.add(center);
 ////    		System.out.println("center x:"+center.point.x + " y:" + center.point.y);
 //    		
 //    		WeightedPoint[] neighbours = new WeightedPoint[4];
 //    		
 //    		neighbours[0] = new WeightedPoint(center.point.x, center.point.y-1, center.cost+1);	//UP
 //    		neighbours[1] = new WeightedPoint(center.point.x, center.point.y+1, center.cost+1);	//DOWN
 //    		neighbours[2] = new WeightedPoint(center.point.x-1, center.point.y, center.cost+1);	//LEFT
 //    		neighbours[3] = new WeightedPoint(center.point.x+1, center.point.y, center.cost+1);	//RIGHT
 //    		
 //    		for (WeightedPoint neighbour : neighbours)
 //    		{
 //    			Symbol nSymb = this.get(neighbour.point);
 ////    			System.out.println("at x:"+ neighbour.point.x + " y:" + neighbour.point.y +"symbol: "+ "'"+ nSymb +"'");
 //	    		if (nSymb != Symbol.Wall && nSymb.type != Symbol.Type.Box)
 //	    		{
 //	    			//adding open neighbour only if not visited and not already in openList
 //	    			if (!openList.contains(neighbour) && !closedList.contains(neighbour))
 //	    			{
 //	        			openList.addLast(neighbour);
 //	    			}
 //	    		}
 //	    		else if (nSymb.type == Symbol.Type.Box)
 //	    		{
 //	    			//check if push is possible
 //	    			int dirX = neighbour.point.x - center.point.x;
 //	    			int dirY = neighbour.point.y - center.point.y;
 //	    			Point endLocation = new Point(neighbour.point.x + dirX, neighbour.point.y + dirY);
 //                    Symbol endSym = this.get(endLocation);
 //                    if (endSym != Symbol.Wall // cannot push into walls or other boxes
 //                            && endSym.type != Symbol.Type.Box)
 //                            // Don't push boxes onto locked points, unless the point is a goal
 //                            // && (!isBoxLocked(endLocation) || endSym == Symbol.Goal))
 //	    			{
 //	    				//Generating new Board
 //	    				Board newBoard = new Board(this);
 //	    				
 //	    				newBoard.moveElement(playerPosition, center.point); //moving player to pushing point
 //	    				newBoard.moveElement(neighbour.point, endLocation); //moving crate
 //	    				newBoard.moveElement(center.point, neighbour.point);//moving player to it's end location
 //	    				
 //	    				//if the board is a locked state, just ignore it
 //	    				if (!newBoard.isLockedState())
 //	    				{
 ////                            newBoard.makeStringHash();
 //		    				result.add(newBoard);
 //		    				
 //		    				//Generating BoxMovement
 //		    				if (rRelatedBoxMovements != null)
 //		    				{
 //		    					Action action;
 //		    					if (dirX>0)
 //		    						action = Action.RIGHT;
 //		    					else if (dirX<0)
 //		    						action = Action.LEFT;
 //		    					else if (dirY>0)
 //		    						action = Action.DOWN;
 //		    					else	//if (dirY<0)
 //		    						action = Action.UP;
 //		    					BoardAction move = new BoardAction(action, neighbour.point);
 //		    					rRelatedBoxMovements.add(move);
 //		    				}
 //		    				
 //		    				if(rRelatedCosts != null)
 //		    				{
 //		    					rRelatedCosts.add(new Integer(center.cost+1));
 //		    				}
 //	    				}
 //	    				else
 //	    				{
 //	    					++lockedStatesIgnored;
 //	    				}
 //	    			}
 //	    		}
 //    		}
 //    	}
 //    	return result;
 //    }
 //    
 //}
 
