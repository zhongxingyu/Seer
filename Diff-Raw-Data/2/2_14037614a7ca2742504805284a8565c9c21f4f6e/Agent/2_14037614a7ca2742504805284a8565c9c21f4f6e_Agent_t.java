 package model;
 
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import model.Cell.ECell;
 
 import exception.PathNotFoundException;
 
 
 /**
  * 
  * Represents the Agent which is able to solve a given problem
  * The agent is the AI
  *
  */
 public class Agent {
 	
 	protected Map map;
 	protected Moves moves;
 	protected LinkedList<Node> nodes = new LinkedList<Node>();
 	protected AStarSearch astar = new AStarSearch();
 	
 	// TODO : represent a Node for the A*
 //	public class Node
 //	{
 //		protected Moves moves;
 //		
 //		public Node(Moves moves)
 //		{
 //			this.moves = moves;
 //		}
 //
 //		public Moves getMoves() {
 //			return moves;
 //		}
 //
 //		public void setMoves(Moves moves) {
 //			this.moves = moves;
 //		}
 //		
 //		
 //	}
 	
 	
 	public Agent()
 	{
 		map = new Map();
 		moves = new Moves();
 		nodes = new LinkedList<Node>();
 	}
 	
 	public Agent(ArrayList<String> stringMap)
 	{
 		this();
 		map = new Map(stringMap);
 	}
 	
 	/**
 	 * Computes the distance between two positions
 	 * @param position1
 	 * @param position2
 	 * @return int : distance
 	 */
 	public int distance(Position position1, Position position2)
 	{
 		return (position1.getI() - position2.getI())*(position1.getI() - position2.getI()) + (position1.getJ() - position2.getJ())*(position1.getJ() - position2.getJ());
 	}
 	
 	/**
 	 * Computes the minimum distance between on position and a list of positions
 	 * @param position
 	 * @param positions
 	 * @return the minimum distance
 	 *          if the list is empty, return -1
 	 */
 	public int distance(Position position, ArrayList<Position> positions)
 	{
 		if(positions != null && positions.size() > 0)
 		{
 			int result = distance(position, positions.get(0));
 			
 			for(Position pos : positions)
 			{
 				int dist = distance(position, pos);
 				if(result > dist)
 				{
 					result = dist;
 				}
 			}
 			
 			return result;
 		}
 		else
 		{
 			return -1;
 		}
 	}
 	
 	/**
 	 * Heuristic Function : sort the positionsStart according to the minimum distance of the goals
 	 * 
 	 * @param positionsStart
 	 * @param positionsGoal
 	 * @return ArrayList<Position>
 	 */
 	public ArrayList<Position> heuristic(ArrayList<Position> positionsStart, ArrayList<Position> positionsGoal)
 	{
 		TreeMap<Integer, ArrayList<Position>> hashMap = new TreeMap<Integer, ArrayList<Position>>();
 		
 		
 		for(Position position : positionsStart)
 		{
 			int distance = distance(position, positionsGoal);
 			if(hashMap.containsKey(distance))
 			{
 				ArrayList<Position> oldList = hashMap.get(distance);
 				oldList.add(position);
 				hashMap.put(distance, oldList);
 				
 			}
 			else
 			{
 				ArrayList<Position> newList = new ArrayList<Position>();
 				newList.add(position);
 				hashMap.put(distance, newList);
 			}
 		}
 		
 		
 		// We construct the final ArrayList
 		ArrayList<Position> result = new ArrayList<Position>();
 		
 		Iterator<Entry<Integer, ArrayList<Position>>> it = hashMap.entrySet().iterator();
 		while(it.hasNext())
 		{
 			Entry<Integer, ArrayList<Position>> entry = it.next();
 			if(entry.getValue().size() > 1)
 			{
 				for(Position position : entry.getValue())
 				{
 					result.add(position);
 				}
 			}
 			else
 			{
 				if(entry.getValue().size() != 0)
 				{
 					result.add(entry.getValue().get(0));
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public Map setCellAccessible(Map map)
 	{
 		
 		List<Node> lNodes = new LinkedList<Node>();
 		List<Node> lNodesVisited = new LinkedList<Node>();
 		
 		if(map.getPlayerPosition().equals(null))
 		{
 			// TODO : throw an exception instead
 			return null;
 		}
 		lNodes.add(new Node(map.getPlayerPosition()));
 		map.getCellFromPosition(map.getPlayerPosition()).setAccessible(true);
 		//System.out.println("Nodes : "+ lNodes);
 		
 		Position current;
 		
 		while(!lNodes.isEmpty())
 		{
 			current = ((LinkedList<Node>)lNodes).getFirst().getPosition();
 			//System.out.println(current);
 			
 			
 			for(Position p : findEmptySpacesAround(current, map))
 			{
 				Node n = new Node(p);
 				
 				//System.out.println("visited : "+ lNodesVisited);
 				if(!lNodesVisited.contains(n))
 				{
 					//System.out.println("test" + p);
 					map.getCellFromPosition(p).setAccessible(true);
 					
 					lNodes.add(n);
 				}	
 			}
 			
 			lNodesVisited.add(new Node(current));
 			lNodes.remove(new Node(current));
 		}
 		
 		//map.toStringAccessible();
 		return map;
 	}
 	
 	
 	public ArrayList<Position> findEmptySpacesAround(Position position)
 	{
 		return findEmptySpacesAround(position, map, Cell.ECell.BOX);
 	}
 	
 	/**
 	 * 
 	 * Finds the empty spaces around the position on the map
 	 * 
 	 * @param position
 	 * @param map
 	 * @return ArrayList<Position> list of the available positions given the previous state node
 	 */
 	public ArrayList<Position> findEmptySpacesAround(Position position, Map map)
 	{
 		ArrayList<Position> positions = new ArrayList<Position>();
 		
 		Position upPosition = new Position(position.getI(), position.getJ());
 		upPosition.up(map);
 		Position downPosition = new Position(position.getI(), position.getJ());
 		downPosition.down(map);
 		Position leftPosition = new Position(position.getI(), position.getJ());
 		leftPosition.left(map);
 		Position rightPosition = new Position(position.getI(), position.getJ());
 		rightPosition.right(map);
 		
 		if(
 				(upPosition.getI() != position.getI() || upPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(upPosition.getI()).get(upPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR) 
 					|| 
 				map.getMap().get(upPosition.getI()).get(upPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
 			positions.add(upPosition);
 		}
 		if(
 				(downPosition.getI() != position.getI() || downPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(downPosition.getI()).get(downPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
 					||
 				map.getMap().get(downPosition.getI()).get(downPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
                     //System.out.println("FAIL.");
 			positions.add(downPosition);
 		}
 		if(
 				(rightPosition.getI() != position.getI() || rightPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(rightPosition.getI()).get(rightPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
 					||
 				map.getMap().get(rightPosition.getI()).get(rightPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
 			positions.add(rightPosition);
 		}
 		if(
 				(leftPosition.getI() != position.getI() || leftPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(leftPosition.getI()).get(leftPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
 					||
 				map.getMap().get(leftPosition.getI()).get(leftPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
 			positions.add(leftPosition);
 		}
 		
 		return positions;
 	}
 
         /*
          * @author: Luis
          * This function should be used to move either the player or a box.
          */
         public ArrayList<Position> findEmptySpacesAround(Position position, Map map, Cell.ECell whoIsMoving)
 	{
 		ArrayList<Position> positions = new ArrayList<Position>();
 		
                 // Get the position.
 		Position upPosition = new Position(position.getI(), position.getJ());
 		upPosition.up(map);
 		Position downPosition = new Position(position.getI(), position.getJ());
 		downPosition.down(map);
 		Position leftPosition = new Position(position.getI(), position.getJ());
 		leftPosition.left(map);
 		Position rightPosition = new Position(position.getI(), position.getJ());
 		rightPosition.right(map);
 		
                 // Move UP!
 		if(
                     // It is not the same position. There was a wall.
                     (upPosition.getI() != position.getI() || upPosition.getJ() != position.getJ()) 
                     && 
                     // And it is an empty floor.
                     (map.getMap().get(upPosition.getI()).get(upPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR) 
                             || 
                     // Or a goal.
                     map.getMap().get(upPosition.getI()).get(upPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
                     )                     
 		)
 		{
                     if (whoIsMoving==Cell.ECell.PLAYER){
                         // Then add the position.
 			positions.add(upPosition);
                     }
                     else if (whoIsMoving==Cell.ECell.BOX)
                     {
                         // -------------------------------------
                         // If the box wants to move up --> Don't.
                         // -------------------------------------
                         // Check if the cell down is a wall, a box or a box on goal.
                         Cell wall = map.getCellFromPosition(downPosition);
                         if (!(    wall.getType()==Cell.ECell.WALL || 
                                 wall.getType()==Cell.ECell.BOX  ||
                                 wall.getType()==Cell.ECell.BOX_ON_GOAL
                             ))
                             positions.add(upPosition);
                         /*
                          * For Debbuging
                          */ 
                         //else{                             
                         //    System.out.println("Can't move up the box.");
                         //    System.out.println(map);
                         //}
                             
                     }
                                             
                           
 		}
                 // Move Down
 		if(
                     (downPosition.getI() != position.getI() || downPosition.getJ() != position.getJ()) 
                     && 
                     (map.getMap().get(downPosition.getI()).get(downPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
                             ||
                     map.getMap().get(downPosition.getI()).get(downPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
                     )
 		)
 		{
                     // If the cell is a player--> He can move.
                     if (whoIsMoving==Cell.ECell.PLAYER){                        
 			positions.add(downPosition);
                     }
                     // If the cell is a box...
                     else if (whoIsMoving==Cell.ECell.BOX)
                     {
                         // If the box wants to move down --> Don't.
                         // -------------------------------------
                         // Check if the cell up is a wall.                        
                         Cell wall = map.getCellFromPosition(upPosition);
                         if (!(    wall.getType()==Cell.ECell.WALL || 
                                 wall.getType()==Cell.ECell.BOX  ||
                                 wall.getType()==Cell.ECell.BOX_ON_GOAL
                             ))
                             positions.add(downPosition);
                             
                          /*
                          * For Debbuging
                          */ 
                         //else{
                         //    System.out.println("Can't move down the box.");
                         //    System.out.println(map);
                         //}
                         
                     }
 		}
                 
                 // Moving to the right!
 		if(
 				(rightPosition.getI() != position.getI() || rightPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(rightPosition.getI()).get(rightPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
 					||
 				map.getMap().get(rightPosition.getI()).get(rightPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
                     // -------------------------------------
                     // Again the same .... Check if there is
                     // a wall to the left and do not let it
                     // move if this is a box.
                     // -------------------------------------
                     if (whoIsMoving==Cell.ECell.PLAYER){
                         positions.add(rightPosition);
                     }
                     else if (whoIsMoving==Cell.ECell.BOX)
                     {   
                         Cell wall = map.getCellFromPosition(leftPosition);
                         if (!(    wall.getType()==Cell.ECell.WALL || 
                                 wall.getType()==Cell.ECell.BOX  ||
                                 wall.getType()==Cell.ECell.BOX_ON_GOAL
                             ))
                             positions.add(rightPosition);
                             
                         /*
                          * For debbuging
                          */
                         //else{
                         //    System.out.println("Can't move right the box.");
                         //    System.out.println(map);
                         //}
                     }
                     
 		}
                 // Moving to the left!
 		if(
 				(leftPosition.getI() != position.getI() || leftPosition.getJ() != position.getJ()) 
 				&& 
 				(map.getMap().get(leftPosition.getI()).get(leftPosition.getJ()).getType().equals(Cell.ECell.EMPTY_FLOOR)
 					||
 				map.getMap().get(leftPosition.getI()).get(leftPosition.getJ()).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
                     // -------------------------------------
                     // Again the same .... Check if there is
                     // a wall to the left and do not let it
                     // move if this is a box.
                     // -------------------------------------
                     if (whoIsMoving==Cell.ECell.PLAYER){
                         positions.add(leftPosition);
                     }
                     else if (whoIsMoving==Cell.ECell.BOX)
                     {   
                         Cell wall = map.getCellFromPosition(rightPosition);
                         if (!(  wall.getType()==Cell.ECell.WALL || 
                                 wall.getType()==Cell.ECell.BOX  ||
                                 wall.getType()==Cell.ECell.BOX_ON_GOAL
                             ))
                             positions.add(leftPosition);
                         /*else{                            
                             System.out.println("Can't move left the box.");
                             System.out.println(map);
                         }*/
                     }
 		
 		}
 		
 		return positions;
 	}
 
         
 	/**
 	 * 
 	 * Creates a new Map based on the previous map and simulates a move of the player
 	 * Everything is cloned, no edge effects
 	 * 
 	 * @param positionVisited
 	 * @param newPlayerPosition
 	 * @return Map modified
 	 * @throws CloneNotSupportedException
 	 */
 	public Map createMapWithVisitedOnThePostion(Position positionVisited, Position newPlayerPosition) throws CloneNotSupportedException
 	{
 		return createMapWithVisitedOnThePostion(map, positionVisited, newPlayerPosition);
 	}
 	
 	public Map createMapWithVisitedOnThePostion(Map map, Position positionVisited, Position newPlayerPosition) throws CloneNotSupportedException
 	{
 		if(map.isPositionOnTheMap(positionVisited) 
 				&&
 			map.isPositionOnTheMap(newPlayerPosition)
 				&&
 				(map.getCellFromPosition(positionVisited).getType().equals(Cell.ECell.EMPTY_FLOOR) 
 					|| 
 				map.getCellFromPosition(positionVisited).getType().equals(Cell.ECell.GOAL_SQUARE)
 					||
 				map.getCellFromPosition(positionVisited).getType().equals(Cell.ECell.PLAYER)
 				)
 				&&
 				(map.getCellFromPosition(newPlayerPosition).getType().equals(Cell.ECell.EMPTY_FLOOR) 
 					|| 
 				map.getCellFromPosition(newPlayerPosition).getType().equals(Cell.ECell.GOAL_SQUARE)
 				)
 		)
 		{
 			Map newMap = map.clone();
 			newMap.movePlayer(newPlayerPosition);
 			
 			return newMap;
 		}
 		else
 		{
 			return map.clone();
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * Finds a path to a goal according to the heuristic function
 	 * 
 	 * @param moves
 	 * @return String (Solution of the Maze : UDRLUUDD...)
 	 * @throws CloneNotSupportedException
 	 * @see heuristic()
 	 */
 	public String findPathToGoal(Moves moves) throws CloneNotSupportedException
 	{
 		return findPathToGoal(map, moves);
 	}
 	
 	public String findPathToGoal(Map map, Moves moves) throws CloneNotSupportedException
 	{
 		// End point : we've found a goal!
 		if(map.getCellFromPosition(map.getPlayerPosition()).getType().equals(Cell.ECell.PLAYER_ON_GOAL_SQUARE))
 		{
 			return moves.toString();
 		}
 		else
 		{
 			
 			//System.out.println("Node Size : " + nodes.size());
 			ArrayList<Position> positions = findEmptySpacesAround(map.getPlayerPosition(), map, Cell.ECell.BOX);
 			//System.out.println(map);
 //			for(Position position : heuristic2(findEmptySpacesAround(map.getPlayerPosition(), map), map.getGoals()))
 //			{
 //				//Map newMap = createMapWithVisitedOnThePostion(map, map.getPlayerPosition(), position);
 //				Moves newMoves = moves.clone();
 //				newMoves.addMove(map.getPlayerPosition(), position);
 //				
 //				nodes.add(new Node(newMoves));
 //			}
 			//nodes.addAll(heuristic2(findEmptySpacesAround(map.getPlayerPosition(), map), map.getGoals()));
 			
 			// We put the 
 			
 			if(positions == null || positions.isEmpty())
 			{
 				System.out.println("No more positions...");
 				// No empty spaces around : we backtrack in the tree
 				return "";
 			}
 			else
 			{
 				//Position position =  nodes.removeLast();
 				//Position position = null;
 				//while((position = nodes.removeLast()) != null)
 				for(Position position : heuristic(findEmptySpacesAround(map.getPlayerPosition(), map, Cell.ECell.BOX), map.getGoals()))
 				//for(Position position : findEmptySpacesAround(map.getPlayerPosition(), map))
 				{
 					moves.addMove(map.getPlayerPosition(), position);
 					//Moves newMoves = moves.clone();
 					//newMoves.addMove(map.getPlayerPosition(), position);
 					//nodes.add(new Node(moves));
 					//System.out.println(moves);
 					String result = findPathToGoal(createMapWithVisitedOnThePostion(map, map.getPlayerPosition(), position), 
 							moves.clone());
 					
 					// If we are in a dead end : we backtrack!!
 					if(!result.equals(""))
 					{
 						// No solution for the problem
 						return result;
 					}
 					else
 					{
 						// We pop the last move
 						moves.pop();
 						
 					}
 				}
 			}
 		}
 		
 		// Useless : only for the errors generated by Eclipse
 		String result = "";
 		return result;
 	}
 	
 	/**
 	 * 
 	 * Find a path from one position to another
 	 * @return 
 	 * @throws CloneNotSupportedException 
 	 * @throws PathNotFoundException 
 	 */
 	public String findPath(Position position1, Position position2, Cell.ECell cellType) throws CloneNotSupportedException, PathNotFoundException
 	{
 		moves = new Moves();
 		return findPath(map, position1, position2, cellType);
 	}
 	
 	/**
 	 * 
 	 * @author arthur
 	 * @param map
 	 * @param position1
 	 * @param position2
 	 * @return The moves that the player has to do to complete the path
 	 * @throws CloneNotSupportedException
 	 * @throws PathNotFoundException
 	 */
 	public String findPath(Map map, Position position1, Position position2, Cell.ECell cellType) throws CloneNotSupportedException, PathNotFoundException
 	{
 		clean();
 		
 		astar.setMap(map.clone());
 		astar.setStartAndGoalNode(new Node(position1), new Node(position2));
 		
 		return astar.search(cellType).toString();
 		
 	}
 	
 	/**
 	* Finds a box-to-goal path for each box.
 	*
 	* Result is only guaranteed to be accurate if the strings are accessed in the 
 	* order in which they are stored in the String array.
 	*
 	* @author Alden Coots <ialden.coots@gmail.com>
 	 * @throws CloneNotSupportedException 
 	*/
 	public String[] getBoxToGoalPaths(Map map) throws CloneNotSupportedException {
 		Map safeToAlterMap = map.clone();
 		String[] paths = new String[map.getNumberOfBoxes()];
 		//System.out.println(paths.length);
 		//pathExists(map, paths, 0, 0, ECell.BOX);
 		//System.out.println(paths[0]);
 		findBoxToGoalPaths(safeToAlterMap, paths, 0);
 		
 		return paths;
 	}
 
 	/**
 	* Finds a box-to-goal path for each box.
 	*
 	* Populates a String array with box-to-goal paths and returns true if a valid
 	* solution is found for all boxes.
 	*
 	* Result is only guaranteed to be accurate if the strings are accessed in the 
 	* order in which they are stored in the String array.
 	*
 	* @author Alden Coots <ialden.coots@gmail.com>
 	* @param map Should be a clone, as it is altered
 	* @param paths String array where box-to-goal paths are stored
 	* @param boxIndx index of initial box in map's box array (should be 0 initially)
 	 * @throws CloneNotSupportedException 
 	*/
 	private boolean findBoxToGoalPaths(Map map, String[] paths, int boxIndx) throws CloneNotSupportedException {
 		
 		if (map.getBoxes().isEmpty()) return true;
 		else {
 			boolean isSolved = false;
 			for (int g = 0; g<map.getNumberOfGoals(); g++) {
 				System.out.println("G : " + g);
 				if (pathExists(map,paths,boxIndx, g, Cell.ECell.BOX)) {
 					Map newMap = map.clone();
 					newMap.set(Cell.ECell.WALL, map.getGoals().get(g));
 					newMap.set(Cell.ECell.EMPTY_FLOOR, map.getBoxes().get(0).getPosition());
 					newMap.getGoals().remove(g);
 					newMap.getBoxes().remove(0);
 					isSolved = isSolved || findBoxToGoalPaths(newMap, paths, ++boxIndx);
 					if (isSolved) break;
 				}
 			return isSolved;
 			}
 		return false;	
 		}
 	}
 	//Converts a box path to the required player path.
 	public String findPlayerPathFromBoxPath(String BoxPath, Map StartMap, Position PlayerPos, Position BoxPos) throws CloneNotSupportedException{
 		String PlayerPath=new String();
 		char lastdir=' ';
 		Position newPlayerPos=new Position();
 		for(int i=0;i<BoxPath.length();i++){
 			char newdir=BoxPath.charAt(i);
 			if(lastdir==newdir){ //If the box path follows the same direction, just move the player one additional step in that direction.
 				PlayerPath=PlayerPath+newdir;
 				if(newdir=='U'){PlayerPos.up(StartMap);}
 				if(newdir=='D'){PlayerPos.down(StartMap);}
 				if(newdir=='L'){PlayerPos.left(StartMap);}
 				if(newdir=='R'){PlayerPos.right(StartMap);}
 			}	
 			else{   //Else find a path for the player to the correct side of the box.
 				newPlayerPos=BoxPos.clone();
 				if(newdir=='U'){newPlayerPos.down(StartMap);}
 				if(newdir=='D'){newPlayerPos.up(StartMap);}
 				if(newdir=='L'){newPlayerPos.right(StartMap);}
 				if(newdir=='R'){newPlayerPos.left(StartMap);}
 				try {
					PlayerPath=PlayerPath+findPath(StartMap,PlayerPos,newPlayerPos, ECell.PLAYER).toLowerCase(); 
 				} catch (PathNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				PlayerPos=newPlayerPos.clone();
 			}
 			
 			StartMap.set(Cell.ECell.EMPTY_FLOOR,BoxPos);
 			if(newdir=='U'){BoxPos.down(StartMap);}
 			if(newdir=='D'){BoxPos.up(StartMap);}
 			if(newdir=='L'){BoxPos.right(StartMap);}
 			if(newdir=='R'){BoxPos.left(StartMap);}
 			StartMap.set(Cell.ECell.BOX,BoxPos);
 			lastdir=newdir;
 		}
 		return PlayerPath;	
 	}
 	/**
 	* Encapsulates findPath() in a boolean function and stores its result in paths[boxIndx].
 	 * @throws CloneNotSupportedException 
 	*
 	*
 	*/
 	private boolean pathExists(Map m, String[] paths, int boxIndx, int g, Cell.ECell cellType) throws CloneNotSupportedException {
 		try {
 			
 			//System.out.println(m.getBoxes());
 			System.out.println(" Box Index : " +boxIndx);
 			paths[boxIndx] = findPath(m, m.getBoxes().get(0).getPosition(), m.getGoals().get(g), cellType);
 			System.out.println("path:" + paths[boxIndx]);
 			
 			return true;
 		} catch (PathNotFoundException e) {
 			System.out.print("some sort of fucking exception");
 			return false;
 		}
 	}
 	
 	/**
 	 * Clean the agent variables
 	 */
 	protected void clean()
 	{
 		map = null;
 		moves = new Moves();
 		nodes = new LinkedList<Node>();
 		astar = new AStarSearch();
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 	
 	public void setMap(ArrayList<String> sList) {
 		this.map = new Map(sList);
 	}
 	
 	public void setMap(BufferedReader br) {
 		this.map = new Map(br);
 	}
 
 	public Moves getMoves() {
 		return moves;
 	}
 
 	public void setMoves(Moves moves) {
 		this.moves = moves;
 	}
 	
 	
 	
 	
 }
