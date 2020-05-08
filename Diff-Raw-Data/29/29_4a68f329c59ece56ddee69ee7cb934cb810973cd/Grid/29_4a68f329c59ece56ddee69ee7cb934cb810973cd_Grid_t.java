 package grid;
 
 import java.util.*;
 import obstacle.Obstacle;
 import exception.*;
 import game.Game;
 import game.Colour;
 
 /**
  * A class that represents a grid in the game.
  * 
  * @invar	This grid has proper squares.
  * 			| hasProperSquares() == true
  * @invar	This grid has proper starting positions.
  * 			| hasProperStartingPositions() == true
  * 
  * @author	Groep 8
  * @version	May 2013
  */
 public class Grid implements Iterable<Square> {
 	
 	/**
 	 * Represents the game.
 	 */
 	private Game game;
 	
 	/**
 	 * Represents the dimension of the grid.
 	 */
 	private final Dimension dimension;
 
 	/**
 	 * The squares that cover the grid.
 	 */
 	private final Square[][] squares;
 	
 	/**
 	 * The starting positions on the grid.
 	 */
 	private final HashMap<Colour, Coordinate> startingPositions = new HashMap<Colour, Coordinate>();
 	
 	/**
 	 * The primary power failure generator of this grid
 	 */
 	private PowerFailureSpreader powerFailureSpreader;
 
 	/**
 	 * Initialize this grid with a given width and height
 	 * 
 	 * @param 	width 
 	 * 			The width of this grid
 	 * @param 	height
 	 * 			The height of this grid
 	 * @post 	The dimension of the grid is set.
 	 * 			| this.dimension = dimension.
 	 * @post	The matrix of squares in the grid is initialized.
 	 * 			| this.squares = new Square[width][height]
 	 */
 	public Grid(Dimension dimension) {
 		this.dimension = dimension;
 		this.squares = new Square[dimension.getX()][dimension.getY()];
 	}
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 	
 	/**
 	 * Return the game of which this grid is part.
 	 */
 	public Game getGame() {
 		return game;
 	}
 	
 	/**
 	 * Set the game to which this grid belongs to the given game.
 	 * 
 	 * @param 	game
 	 * 			The game to which this grid belongs.
 	 * 
 	 * @note 	This setter is used to maintain a bidirectional association
 	 * 			and should not be used in another way.
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	/**
 	 * Returns dimension of the grid.
 	 */
 	public Dimension getDimension() {
 		return dimension;
 	}
 	
 	/**
 	 * Returns the primary power failure spreader.
 	 */
 	public PowerFailureSpreader getPowerFailureSpreader() {
 		return powerFailureSpreader;
 	}
 	
 	/**
 	 * Set the power failure spreader to the given power failure spreader.
 	 * 
 	 * @param 	pfs
 	 * 			The power failure spreader to set.
 	 * @effect	This grid is set as the grid of the power failure spreader.
 	 * 			| pfs.setGrid(this);
 	 */
 	public void setPowerFailureSpreader(PowerFailureSpreader pfs) {
 		this.powerFailureSpreader = pfs;
 		pfs.setGrid(this);
 	}
 
 	/**
 	 * Return the square at the given coordinate.
 	 * 
 	 * @param 	c 
 	 * 			The coordinate location of the square you're looking for.
 	 * @return 	The square at coordinate 
 	 * @throws 	OutsideTheGridException [must]
 	 * 			When the coordinate does not belong to a square that is on the grid.
 	 * 		 	| !this.containsCoordinate(c)
 	 * 			Or the requested square is an outer square (null).
 	 */
 	public Square getSquareAtCoordinate(Coordinate c) throws OutsideTheGridException {
 		if(!this.containsCoordinate(c)){
 			throw new OutsideTheGridException();			
 		}
 		Square square =  squares[c.getX()-1][c.getY()-1];
 		if(square == null) throw new OutsideTheGridException();
 		return square;
 	}
 	
 	/**
 	 * Add the given square to this grid at the given coordinate.
 	 * 
 	 * @param 	s 
 	 * 			The square to be added.
 	 * @param 	c 
 	 * 			The coordinate at which the square should be added.
 	 * @post 	The given square is built at the given coordinate in the grid.
 	 * 			| squares[c.getX()-1][c.getY()-1] = s
 	 * @post	This grid is set as the grid of the given square.
 	 * 			| s.setGrid(this)
 	 * @throws 	IllegalArgumentException [must] 
 	 * 			This grid cannot have the given square at the given coordinate.
 	 * 			| !canHaveAsSquareAtCoordinate(s,c)
 	 */
 	void setSquareAtCoordinate(Square s, Coordinate c) throws IllegalArgumentException{
 		s.setCoordinate(c);
 		if(!canHaveAsSquareAtCoordinate(s,c)){
 			throw new IllegalArgumentException();
 		}
 		squares[c.getX()-1][c.getY()-1] = s;
 		s.setGrid(this);
 	}
 	
 	/**
 	 * Returns the starting positions of players on the grid.
 	 * 
 	 * @return The starting positions on the grid.
 	 */
 	public Map<Colour, Coordinate> getStartingPositions() {
 		return Collections.unmodifiableMap(startingPositions);
 	}
 	
 	/**
 	 * Add the given coordinate to the starting positions of this grid
 	 * 
 	 * @param 	startingPosition
 	 * @param 	playerColour
 	 * @effect 	The given position is added to the list of starting positions
 	 * 			| startingPositions.put(playerColour, startingPosition);
 	 * @effect	The square at the given coordinate is set as a starting position.
 	 * 			| setSquareAtCoordinate(new StartingPosition(playerColour), startingPosition);
 	 */
 	void addStartingPosition(Colour playerColour, Coordinate startingPosition) {
 		startingPositions.put(playerColour, startingPosition);
 		setSquareAtCoordinate(new StartingPosition(playerColour), startingPosition);
 	}
 	
 	/**
 	 * Check whether the given coordinate is a starting position for this grid.
 	 * 
 	 * @param 	startingPosition
 	 * @return 	True iff the given coordinate is a starting position.
 	 */
 	public boolean isStartingPosition(Coordinate startingPosition) {
 		return startingPositions.containsValue(startingPosition);
 	}	
 
 	/**
 	 * Check whether this grid can have the given square at the given coordinate.
 	 * 
 	 * @param 	s 
 	 * 			The square to be checked.
 	 * @param 	c 
 	 * 			The coordinate to be checked.
 	 * @return 	True if and only if the coordinate of the given square is the same as the given coordinate. False if not.
 	 */
 	public boolean canHaveAsSquareAtCoordinate(Square s, Coordinate c) {
 		return s.getCoordinate().equals(c);
 	}
 		
 	/**
 	 * Check whether this grid contains the given square.
 	 * 
 	 * @param 	s 
 	 * 			The square to be checked.
 	 * @return 	True if the grid contains the given square, false if not.
 	 */
 	public boolean hasAsSquare(Square s){
 		Iterator<Square> it = this.iterator();
 		while(it.hasNext()){
 			Square square = it.next();
 			if(square == s){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Return whether this grid has proper squares.
 	 * 
 	 * @return True if and only if every square on the grid has the right coordinate and points to this grid as its grid. False if not.
 	 */
 	public boolean hasProperSquares(){
 		for(int x = 1;x <= dimension.getX(); x++){
 			for(int y = 1; y <= dimension.getY(); y++){
 				Coordinate c = new Coordinate(x,y);
 				Square s = null;
 				try {
 					s = getSquareAtCoordinate(c);
 				} catch (OutsideTheGridException e) {
 					continue;
 				}
 				if(!canHaveAsSquareAtCoordinate(s,c)){
 					return false;
 				}
 				if(s.getGrid() != this){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Return the square that is in the given direction of the given square.
 	 * 
 	 * @param 	square 
 	 * 			The square you start from.
 	 * @param 	direction 
 	 * 			The direction you go to.
 	 * @return 	A square in the given direction of the given square.
 	 * @throws 	OutsideTheGridException [must]
 	 * 			When the coordinate does not belong to a square that is on the grid.
 	 */
 	public Square getSquareAtDirection(Square square, Direction direction) throws OutsideTheGridException {
 		return getSquareAtCoordinate(square.getCoordinate().getCoordinateAtDirection(direction));
 	}
 	
 	/**
 	 * Return whether this grid contains the given coordinate.
 	 * 
 	 * @param 	c 
 	 * 			The coordinate to check.
 	 * 
 	 * @return 	True if this grid contains the given coordinate, else false.
 	 */
 	public boolean containsCoordinate(Coordinate c){
 		try {
 			return squares[c.getX()-1][c.getY()-1] != null;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return false;
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Return if a move from a given location in a given direction is valid.
 	 *
 	 * @param	location
 	 * 			The square to start from.
 	 * @param 	direction
 	 * 			The direction from the current location to be checked.
 	 * @return	True iff the square you are moving to doesn't have an obstacle or
 	 * 			player on it and you don't cross an obstacle when moving to that square
 	 * 			and you don't stand on a force field.
 	 */
 	public boolean isValidMove(Square location, Direction direction) {
 		try {
 			Square goalSquare = getSquareAtDirection(location, direction);
 			if(!cannotCross(location, direction) && goalSquare.canBeSteppedOn() && location.canBeLeaved()) {
 				return true;
 			} else return false;
 		} catch(OutsideTheGridException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Check if a certain move would make this player cross something
 	 * that he cannot cross.
 	 * 
 	 * @param	location
 	 * 			The square to start from.
 	 * @param 	direction
 	 * 			The direction from the current location to be checked.
 	 * 
 	 * @return	True if the squares are both covered with the same obstacle and they are
 	 *			next to each other.
 	 * @return 	False if the squares are both covered with different obstacles.
 	 * @return 	True if one of both squares is covered with an obstacle,
 	 * 			the other with the player that owns the obstacle,
 	 * 			the square covered with a obstacle is the last
 	 * 			square added to the obstacle.
 	 * @return 	False if one of both squares is covered with an obstacle,
 	 * 			the other with the player that owns the obstacle,
 	 * 			the square covered with an obstacle is not the last
 	 * 			square added to the obstacle the player can cross.
 	 * @return 	False in all other cases.
 	 */
 	public boolean cannotCross(Square location, Direction direction)	{
 		Direction xComponent = direction.getxComponent();
 		Direction yComponent = direction.getyComponent();
 		
 		/**
 		 * Direction is not diagonal
 		 */
 		if(!direction.isDiagonal()){
 			return false;
 		}
 		
 		/**
 		 * Direction is diagonal
 		 */
 		Square squareX = null;
 		Square squareY = null;
 		
 		// Select the squares that lay between the current location and the location the player wants to move to.
 		try {
 			squareX = this.getSquareAtDirection(location,xComponent);
 			squareY = this.getSquareAtDirection(location,yComponent);
 		} catch (OutsideTheGridException e) {
			//nothing happened, move along
 		}
 		
 		if (squareX == null || squareY == null) return false; // outer square
 		
 		for(Obstacle obstacleX: squareX.getObstacles()) {
 			for(Obstacle obstacleY: squareY.getObstacles()) {
 				if(obstacleX == obstacleY && !obstacleX.canEnter()){
 					// Sooo... this must be an obstacle that is placed diagonally!
 					if(Math.abs(obstacleX.indexOf(squareX) -
 							obstacleY.indexOf(squareY)) == 1){
 						
 						/*
 						 * CASE 1: the squares are both covered with the same obstacle and 
 						 *         they are next to each other 
 						 *         --> the player cannot cross; this is no valid move 
 						 */
 						return true;	
 						
 					}
 				} else {
 					
 					/*
 					 * CASE 2: the squares are both covered with different obstacles
 					 *         --> the player can cross
 					 */
 					return false;
 					
 				}
 			}
 		}
 		
 		/*
 		 * CASE 3: there are no obstacles that cross a corner of a square
 		 */
 		return false;
 	}	
 	
 	
 	/**
 	 * Comparator for the nodes when looking for the shortest path.
 	 */
 	private class NodeComparator implements Comparator<Square> {
 		
 		private HashMap<Square,Integer> fScore;
 		
 		NodeComparator(HashMap<Square,Integer> fScore){
 			this.fScore = fScore;
 		}
 		
 		public int compare(Square s1, Square s2){
 			return Integer.compare(fScore.get(s1), fScore.get(s2));
 		}
 	}
 	
 	/**
 	 * Return the distance of the shortest path between the two given squares.
 	 * @param 	fromSquare 
 	 * 			The square to start from.
 	 * @param 	toSquare 
 	 * 			The goal square
 	 * @return 	The length of the shortest path.
 	 * @throws 	InvalidMoveException 
 	 * 			If there is no path between the two squares.
 	 */
 	public int getPathDistance(Square fromSquare, Square toSquare) throws InvalidMoveException{
 		// closedset := the empty set    // The set of nodes already evaluated.
 	    // openset := {start}    // The set of tentative nodes to be evaluated, initially containing the start node
 	    // came_from := the empty map    // The map of navigated nodes.
 		HashSet<Square> closedSet = new HashSet<Square>();
 		HashMap<Square,Integer> gScore = new HashMap<Square,Integer>();
 		HashMap<Square,Integer> fScore = new HashMap<Square,Integer>();
 		PriorityQueue<Square> openSet = new PriorityQueue<Square>(10, new NodeComparator(fScore));
 		openSet.add(fromSquare);
 		HashMap<Square,Square> cameFrom = new HashMap<Square,Square>();
 		
 	    //g_score[start] := 0    // Cost from start along best known path.
 	    // Estimated total cost from start to goal through y.
 	    //f_score[start] := g_score[start] + heuristic_cost_estimate(start, goal)
 		gScore.put(fromSquare, 0);
 		fScore.put(fromSquare, Grid.getChebyshevDistance(fromSquare, toSquare));
 		
 		//while openset is not empty
 		while(!openSet.isEmpty()){
 			//current := the node in openset having the lowest f_score[] value
 			//remove current from openset
 			Square current = openSet.poll();
 			
 			
 			//if current = goal
 			if(current == toSquare){
 				//return reconstruct_path(came_from, goal)
 				return gScore.get(current);
 			}        
 
 			// add current to closedset
 			closedSet.add(current);
 			
 			//for each neighbor in neighbor_nodes(current)
 			ArrayList<Square> neighbours = getNeighbours(current);
 			for(Square neighbour: neighbours){
 				//tentative_g_score := g_score[current] + dist_between(current,neighbor)
 				int tentative_gScore = gScore.get(current) + 1;
 				//if neighbor in closedset, continue
 				if(closedSet.contains(neighbour)){
 					//if tentative_g_score >= g_score[neighbor]
 					if(tentative_gScore>= gScore.get(neighbour)){
 						continue;
 					}
 				}
 				
 				//if neighbor not in openset or tentative_g_score < g_score[neighbor] 			
 				if(!openSet.contains(neighbour) || tentative_gScore < gScore.get(neighbour)){
 					//came_from[neighbor] := current
 					cameFrom.put(neighbour, current);
 					//g_score[neighbor] := tentative_g_score
 					gScore.put(neighbour, tentative_gScore);
 					// f_score[neighbor] := g_score[neighbor] + heuristic_cost_estimate(neighbor, goal)
 					fScore.put(neighbour,gScore.get(neighbour) + Grid.getChebyshevDistance(neighbour,toSquare));               
 	                
 	                //if neighbor not in openset
 					if(!openSet.contains(neighbour)){
 						//add neighbor to openset
 						openSet.add(neighbour);
 					}
 				}                 
 			}            
 		}
 		//no shortest path found, fail
 	    throw new InvalidMoveException();
 	}
 	
 	/**
 	 * Return all the neighbours of this square which are reachable from the given square.
 	 * 
 	 * @param 	square 
 	 * 			The square you are on
 	 * @return 	A list of squares you can move to.
 	 */
 	private ArrayList<Square> getNeighbours(Square square){
 		ArrayList<Square> neighbours = new ArrayList<Square>();
 		
 		Direction[] directions = Direction.values();
 		//for each direction, try to generate a child path
 		for(Direction d: directions){
 			//first check if you can move in the given direction
 			if(isValidMove(square,d)){
 				Square newSquare = null;
 				try {
 					newSquare = getSquareAtDirection(square,d);
 				} catch (OutsideTheGridException e) {
 					throw new AssertionError("Outside the grid. Cannot occur.");
 				}
 				neighbours.add(newSquare);		
 			}
 		}
 		return neighbours;
 	}
 	
 	/**
 	 * Return the Chebyshev distance from a given square to a given square.
 	 * @param 	fromSquare 
 	 * 			The first square.
 	 * @param 	toSquare 
 	 * 			The second square.
 	 * @return 	The Chebyshev distance between the two squares
 	 * 			(the maximum of their distances along the two axes)
 	 */
 	private static int getChebyshevDistance(Square fromSquare,Square toSquare){
 		int xDistance = Math.abs(fromSquare.getCoordinate().getX() - toSquare.getCoordinate().getX());
 		int yDistance = Math.abs(fromSquare.getCoordinate().getY() - toSquare.getCoordinate().getY());
 		return Math.max(xDistance, yDistance);
 	}
 	
 	/**
 	 * Return an iterator for this grid (row per row, from bottom to top).
 	 */
 	@Override
     public Iterator<Square> iterator() {
 		
         List<Square> list = new ArrayList<Square>();  
         for(int row = 0; row < dimension.getY(); row++){  
             for(int column = 0; column < dimension.getX(); column++){  
             	Square sq = squares[column][row];
                 if(sq != null)  {
                     list.add(sq);
                 }
             }  
         }
         return list.iterator(); 
     }
 
 	
 }
