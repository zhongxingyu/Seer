 package CluedoGame.Board;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.Stack;
 
 import CluedoGame.Character;
 import CluedoGame.Player;
 import CluedoGame.Room;
 import CluedoGame.Square;
 import CluedoGame.Weapon;
 
 /**
  * The Board class is responsible for keeping track of player locations, and
  * moving their locations when requested via a valid route, and setting Players'
  * locations for them when they are moved.
  * 
  * The Board is be responsible for setting the Player's location.
  * Eg. The Board is passed a player, it gets the players character, moves that
  * character on the board, and then changes the Player's location to match its
  * location on the board.
  * 
  * NOTE: Point (x,y) corresponds to (col, row) on the map, with (0,0) being the 
  * top left corner. 
  * 
  * 
  * @author Izzi
  * 
  */
 public class Board {
 
 	// board info
 	// -----------
 	private Cell[][] map; // a 2D array of Cell objects representing the map.  Every box will contain a Cell, although room cells will be duplicated.
 	private Map<Character, Cell> startingCells; // a map to keep track of where characters start. I won't make a new type of Cell for now.
 	private Set<CorridorCell> intrigueCells; // a set of the intrigue cells on the board
 	private Map<Room, Cell> rooms; // a map from 'Room' to cell
 
 	// dimensions of the board
 	private int cols;
 	private int rows;
 
 	// player info
 	// ------------
 	private Map<Character, Cell> playerPos;	// map from Player to their location cell
 	private Map<Character, MoveableCharacter> players;
 	
 	
 	/**
 	 * Constructor for Board. Takes a set of players as an argument(to ensure there are no duplicates).
 	 * 
 	 * @param currPlayers
 	 */
 	public Board(Set<? extends MoveableCharacter> currPlayers) {
 		// set up fields
 		this.playerPos = new HashMap<Character, Cell>();
 		this.rooms = new HashMap<Room, Cell>();
 		this.intrigueCells = new HashSet<CorridorCell>();
 		this.players = new HashMap<Character, MoveableCharacter>();
 
 		// create the board
 		char[][] rawData = this.readFromFile();
 		if (rawData == null){System.exit(1);}
 		
 		this.constructBoard(rawData);
 
 		// add the players
 		this.addPlayers(currPlayers);
 	}
 
 	// =============================================================================================
 	// Player movement methods
 	// =============================================================================================
 
 	/**
 	 * Moves the player's character to the given Square on the board.
 	 * 
 	 * @param player
 	 * @param newPos
 	 * @throws - IllegalArgumentException if the player cannot get to the square
 	 *         specified, or if it is already in the square specified.
 	 */
 	public void setPlayerPosition(Character player, Square newPosition) throws IllegalArgumentException {
 		Cell newPos = (Cell) newPosition;
 
 		// sanity checks	
 		if (newPos.equals(playerPos.get(player))) {
 			throw new IllegalArgumentException(
 					"You are already in that location: " + playerPos.get(player));
 		}
 		if (newPos instanceof CorridorCell
 				&& ((CorridorCell) newPos).isBlocked()) {
 			throw new IllegalArgumentException(
 					"The square specified is not empty.");
 		}
 		if (playerPos.containsKey(player) && this.getBestPathTo(player, newPosition).size() == 0) {
 			throw new IllegalArgumentException(
 					"There is no valid path to the square specified: " + newPos);
 		}
 	
 		// set current Cell to empty
 		Cell currPos = playerPos.get(player);
 		if (currPos != null) {
 			currPos.setBlocked(false);
 		}
 
 		// set new Cell as occupied
 		newPos.setBlocked(true);
 
 		// record that the character is in the new cell
 		playerPos.put(player, newPos);
 
 		// update the Player's position
 		players.get(player).setPosition(newPos);
 	}
 
 	
 	/**
 	 * This method summons the Player with the given character to the given room
	 * (if they are playing and not already there).
 	 * 
 	 * @param chara
 	 * @param room
 	 */
 	public void summonCharacter(Character chara, Room room) {
 		for (Character p : playerPos.keySet()) {
			if (p.equals(chara) && !playerPos.get(p).getRoom().equals(room)) {
 				this.setPlayerPosition(p, rooms.get(room));
 			}
 		}
 	}
 
 
 	/**
 	 * This method moves the given player through the secret passage in the room
 	 * they are in.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the player isn't in a room with a secret passage.
 	 */
 	public void useSecretPassage(Character chara) throws IllegalArgumentException{
 		Cell position = playerPos.get(chara);
 
 		// check they are in a corner room
 		if (!position.isCornerRoom()) {
 			throw new IllegalArgumentException("Player " + chara
 					+ "must be in a room with a secret passage.");
 		}
 
 		// if player is in corner room, move them
 		RoomCell room = (RoomCell) position;
 		setPlayerPosition(chara, room.getSecretPassageDest());
 	}
 
 	
 	
 	// ========================================================================
 	// Queries about the Player's location on the board
 	// ========================================================================
 
 	/**
 	 * This method returns a map containing the moves it would take the player
 	 * to reach each room on the board (and the closest intrigue). If there is no path to the 
 	 * room, the distance will be -1.
 	 * 
 	 * @param chara
 	 * @return - Map<Room, Integer> that maps number of moves to all rooms 
 	 */
 	public Map<Room, Integer> getDistanceToAllRooms(Character chara) {
 		Map<Room, Integer> options = new HashMap<Room, Integer>();
 		// calculate path length to each room and add to map.
 		Room[] rooms = Room.values();
 		for (int i = 0; i < rooms.length; i++) {
 			Room room = rooms[i];
 			if (room != Room.Corridor) {
 				List<Square> path = this.getBestPathTo(chara, room);
 				options.put(room, path.size() - 1);
 			}
 		}
 		return options;
 	}
 
 	/**
 	 * This method returns the optimum path from the player's position, and the
 	 * square at the point given. If the destination room or the players
 	 * position is a room with multiple entrances, it will find best path
 	 * between best entrances.
 	 * 
 	 * If a path cannot be found, it will return an empty list.
 	 * 
 	 * @param chara
 	 * @param p
 	 * @return
 	 */
 	public List<Square> getBestPathTo(Character chara, Point p) {
 		// get the start and goal cells
 		Cell s = (Cell) players.get(chara).getPosition();
 		Cell g = (Cell) this.getSquare(p);
 		return this.getBestPathBetween(s, g);
 	}
 
 	/**
 	 * This method returns the optimum path from the player's position, and the
 	 * Square given. If the destination room or the players position is a room
 	 * with multiple entrances, it will find best path between best entrances.
 	 * 
 	 * If a path cannot be found, it will return an empty list.
 	 * 
 	 * @param chara
 	 * @param p
 	 * @return
 	 */
 	public List<Square> getBestPathTo(Character chara, Square destination) {
 		// get the start cell
 		Cell start = (Cell) players.get(chara).getPosition();
 		return this.getBestPathBetween(start, (Cell) destination);
 	}
 
 	/**
 	 * This method returns the optimum path from the player's position, and the
 	 * room given. If the destination room or the players position is a room
 	 * with multiple entrances, it will find best path between best entrances.
 	 * 
 	 * If a path cannot be found, it will return an empty list.
 	 * 
 	 * @param chara
 	 * @param p
 	 * @return
 	 */
 	public List<Square> getBestPathTo(Character chara, Room room) {
 		// get the start cell
 		Cell s = (Cell) players.get(chara).getPosition();
 		Cell g = null;
 		List<Square> path = new ArrayList<Square>();
 		
 		// get goal cell
 		if (room == Room.Intrigue) {
 			g = this.getClosestIntrigue(chara);// if room is intrigue, goal is the closest one.
 		} else { // otherwise get the room
 			g = rooms.get(room);
 		}
 
 		if (g == null){ return path;}	
 		else {return this.getBestPathBetween(s, g);}
 	}
 
 	
 	// Not sure if this should be public or private yet.
 	/**
 	 * Returns the Square at the specified point on the board.
 	 * 
 	 * @return
 	 * @throws IllegalArgumentException if point is not on the board.
 	 */
 	public Square getSquare(Point p) throws IllegalArgumentException {
 		int col = p.x;
 		int row = p.y;
 		// check Point within bounds
 		if (!(row >= 0 && row < this.rows && col >= 0 && col < this.cols)) {
 			throw new IllegalArgumentException(
 					"Point (col, row) is out of bounds.");
 		}
 		return map[row][col];
 
 	}
 
 	
 	/**
 	 * Draw's a textual representation of the map, with the path given drawn in
 	 * asterisks.
 	 * 
 	 * @param pathToDraw
 	 * @return
 	 */
 	public void drawPath(List<Square> pathToDraw) {
 		char[][] path = this.readFromFile();
 		// add asterisks into the map where the path is
 		for (Square c : pathToDraw) {
 			if (c instanceof CorridorCell) {
 				Point p = c.getPosition();
 				path[p.y][p.x] = '*';
 			}
 		}
 		// draw the map (with corridors as whitespace)
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				if (path[i][j] == '.') {
 					System.out.print(' ');
 				} else {
 					System.out.print(path[i][j]);
 				}
 			}
 			System.out.println();
 		}
 		System.out.println();
 	}
 	
 	
 	/**
 	 * Draws a text based representation of the board, with player locations.  
 	 * @return
 	 */
 	public void drawBoard(){
 		char[][] path = this.readFromFile();
 
 		// clear staring positions.
 		for (Cell c: startingCells.values()){
 			Point p = c.getPosition();
 			int row = p.y;
 			int col = p.x;
 
 			path[row][col] = '.'; 
 		}
 
 		// draw in characters
 		for (Character c: playerPos.keySet()){
 			Cell cell = playerPos.get(c);
 			Point point = cell.getPosition();
 			if(c == Character.Scarlett){path[point.y][point.x] = '1';}
 			if(c == Character.Mustard){path[point.y][point.x] = '2';}
 			if(c == Character.White){path[point.y][point.x] = '3';}
 			if(c == Character.Green){path[point.y][point.x] = '4';}
 			if(c == Character.Peacock){path[point.y][point.x] = '5';}
 			if(c == Character.Plum){path[point.y][point.x] = '6';}
 		}
 		
 		// draw the map (with corridors as whitespace)
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 					System.out.print(path[i][j]);
 				
 			}
 			System.out.println();
 		}
 		System.out.println();
 
 	}
 		
 		
 	
 
 	// =============================================================================================
 	// Path finding methods (woo!)
 	// =============================================================================================
 
 	/**
 	 * 
 	 * This method returns the optimum path in the form of a List<Square>, where
 	 * the first entry is the start, and the last the goal.
 	 * 
 	 * This method is an overloaded wrapper method for the A* search algorithm, whose
 	 * heuristic getEstimate() algorithm can't handle rooms being in lots of
 	 * places at once. It takes the start cell and the goal cell, checks whether
 	 * either of them are rooms, and if so, it gets all optimum paths between
 	 * the room entrances, and the start/goal CorridorCell, picks the best one,
 	 * then makes sure to include the room Cell/s in the list before returning.
 	 * 
 	 * If there is no path, it will return an empty list.
 	 * 
 	 * @param s - the starting Cell
 	 * @param g - the goal Cell
 	 * @return - a list containing the Squares in the path (empty list if there isn't one)
 	 */
 	private List<Square> getBestPathBetween(Cell s, Cell g) {
 
 		// to hold the best path
 		List<Square> bestPath = new ArrayList<Square>();
 		int bestSize = Integer.MAX_VALUE;
 		
 		// check start and goal aren't the same - if so, add start and return
 		if (s.equals(g)){
 			bestPath.add(s);
 			return bestPath;
 		}
 		
 
 		// a. if both start and goal are RoomCells:
 		// -----------------------------------------
 		if (s instanceof RoomCell && g instanceof RoomCell) {
 			// get all the rooms entrances, and find best path between all start
 			// entrances, and goal entrances.
 			RoomCell start = (RoomCell) s;
 			RoomCell goal = (RoomCell) g;
 			List<CorridorCell> sEntrances = start.getEntrances();
 			List<CorridorCell> gEntrances = goal.getEntrances();
 
 			// check them all, and save the path if its better than already
 			// recorded
 			for (int i = 0; i < sEntrances.size(); i++) {
 				for (int j = 0; j < gEntrances.size(); j++) {
 					List<Square> path = this.findBestPath(
 							sEntrances.get(i), gEntrances.get(j));
 					if (path != null && path.size() < bestSize) {
 						bestPath = path;
 						bestSize = path.size();
 					}
 				}
 			}
 			// add start room and goal room back into the final list
 			if (!bestPath.isEmpty()) {
 				bestPath.add(0, start);
 				bestPath.add(goal);
 			}
 		}
 
 		// b. if start is a RoomCell:
 		// ----------------------------
 		else if (s instanceof RoomCell) {
 			RoomCell start = (RoomCell) s;
 			List<CorridorCell> sEntrances = start.getEntrances();
 			CorridorCell goal = (CorridorCell) g;
 
 			// check them all, and save the path if its better than already
 			// recorded
 			for (int i = 0; i < sEntrances.size(); i++) {
 				List<Square> path = this.findBestPath(sEntrances.get(i),
 						goal);
 				if (path != null && path.size() < bestSize) {
 					bestPath = path;
 					bestSize = path.size();
 				}
 			}
 			// add start room back into the final list
 			if (!bestPath.isEmpty()) {
 				bestPath.add(0, start);
 			}
 		}
 
 		// c. if goal is a RoomCell:
 		// --------------------------
 		else if (g instanceof RoomCell) {
 			RoomCell goal = (RoomCell) g;
 			List<CorridorCell> gEntrances = goal.getEntrances();
 			CorridorCell start = (CorridorCell) s;
 
 			// check them all, and save the path if its better than already
 			// recorded
 			for (int i = 0; i < gEntrances.size(); i++) {
 				List<Square> path = this.findBestPath(start,
 						gEntrances.get(i));
 				if (path != null && path.size() < bestSize) {
 					bestPath = path;
 					bestSize = path.size();
 				}
 			}
 			// add goal room back into the final list
 			if (!bestPath.isEmpty()) {
 				bestPath.add(goal);
 			}
 
 		}
 
 		// d. if both are corridors:
 		// -----------------------
 		else {
 			List<Square> path = this.findBestPath((CorridorCell) s,
 					(CorridorCell) g);
 			if (path != null) {
 				bestPath = path;
 			}
 		}
 
 		return bestPath; // <--- this will be an empty list if no path was found
 	}
 
 	/**
 	 * This method returns the optimum unobstructed path in the form of a
 	 * List<Square>, where the first entry is the starting Cell, and the last the
 	 * goal Cell.
 	 * 
 	 * This method implements the slow version of the A* search algorithm, and
 	 * can only calculate paths between CorridorCells. It returns a list of the
 	 * best path from the Player's current position to the cell at the given
 	 * point.
 	 * 
 	 * @param start - the starting Cell
 	 * @param goal - the goal Cell
 	 * @return
 	 */
 	private List<Square> findBestPath(CorridorCell start,
 			CorridorCell goal) {
 
 		// 1. initialise everything
 		// -------------------------
 		PriorityQueue<CellPathObject> fringe = new PriorityQueue<CellPathObject>();
 		List<Square> path = new ArrayList<Square>();
 		Set<CorridorCell> visited = new HashSet<CorridorCell>();
 		
 		// check that start doesn't equal goal - if so, add start and return
 		if (start == goal){
 			path.add(start);
 			return path;
 		}
 
 		// 2. put start on the fringe
 		// ---------------------------
 		fringe.add(new CellPathObject(start, null, 0, this.getEstimate(start,
 				goal)));
 
 		// 3. step through until find best path
 		// -----------------------------------------
 		while (fringe.size() != 0) {
 			// get next best node
 			CellPathObject nextBest = fringe.poll();
 			CorridorCell cell = nextBest.getCell();
 
 			// if the cell hasn't been visited yet
 			if (!visited.contains(cell)) {
 				// mark node as visited
 				visited.add(cell);
 				cell.setPathFrom(nextBest.getFrom());
 
 				// if cell == goal, break
 				if (cell.equals(goal)) {
 					break;
 				}
 
 				// add cell's neighbours to the fringe (if they aren't visited)
 				for (Cell c : cell.getNeighbours()) {
 					if (c instanceof CorridorCell && !visited.contains(c)
 							&& !((CorridorCell) c).isBlocked()) {
 						CorridorCell neigh = (CorridorCell) c;
 
 						// work out how far to the neighbour
 						int costToNeigh = nextBest.getCostToHere() + 1;
 
 						// work out how far to end
 						int estTotal = costToNeigh
 								+ this.getEstimate(neigh, goal);
 
 						// add neighbour to fringe
 						fringe.add(new CellPathObject(neigh, cell, costToNeigh,
 								estTotal));
 					}
 
 				}
 
 			}
 		}
 		// when we get to here, we have found the shortest path.
 
 		// 4. Step through path from goal to start, putting each cell on the
 		// stack
 		// ------------------------------------------------------------------------
 		Stack<Square> reverser = new Stack<Square>();
 		CorridorCell node = goal;
 		CorridorCell from = null;
 		while ((from = node.getPathFrom()) != null) {
 			reverser.push(node);
 			node = from;
 		}
 		reverser.push(node);
 
 		// 5. clear the paths recorded in all the cells
 		// ------------------------------------------
 		for (CorridorCell c : visited) {
 			c.resetPath();
 		}
 
 		// 6. check that the goal was reached (reverser will only contain
 		// 'goal') if it wasn't
 		// ---------------------------------------------------------------------------------
 
 		
 		if (reverser.size() == 1) {
 			return null;
 		}
 
 		// 7. Add the Cells in order to the list from start to goal and return
 		// --------------------------------------------------------------------
 
 		while (!reverser.isEmpty()) {
 			path.add(reverser.pop());
 		}
 		return path;
 
 	}
 
 	/**
 	 * Helper method for getBestPathTo.  Returns the optimal path (ignoring walls etc) between two Cells.
 	 * Calculated by horizontal difference + vertical difference (so it doesn't
 	 * try go diagonally).
 	 * 
 	 * Note: This should never be given a RoomCell - only an entrance to one.
 	 * 
 	 * @param start - the starting Cell.
 	 * @param goal - the end Cell.
 	 * @return - the estimated path length between the given cells
 	 * @throws - IllegalArgumentException if given a RoomCell
 	 */
 	private int getEstimate(Cell start, Cell goal) throws IllegalArgumentException {
 		if (start instanceof RoomCell || goal instanceof RoomCell) {
 			throw new IllegalArgumentException("Shouldn't be given a RoomCell.");
 		}
 
 		int sx = start.getPosition().x;
 		int gx = goal.getPosition().x;
 		int sy = start.getPosition().y;
 		int gy = goal.getPosition().y;
 
 		return (Math.abs(sx - gx) + Math.abs(sy - gy));
 	}
 
 	/**
 	 * This is a private method that returns the closest intrigue square to the
 	 * given player.
 	 * 
 	 * @param chara
 	 * @return - returns null if no intrigue square is accessible.
 	 */
 	private Cell getClosestIntrigue(Character chara) {
 		List<Square> bestPath = null;
 		int bestDistance = Integer.MAX_VALUE;
 
 		// go through and find the smallest path between current position and
 		// all the intrigue squares
 		for (CorridorCell intr : intrigueCells) {
 			List<Square> path = this.getBestPathBetween(this.playerPos.get(chara),
 					intr);
 			if (path.size() > 0 && path.size() < bestDistance) {
 				bestPath = path;
 				bestDistance = path.size();
 			}
 		}
 
 		// as long as a path was found, return the ingrigue square at the end.
 		if (bestPath != null) {
 			return (Cell) bestPath.get(bestDistance - 1);
 		}
 
 		// otherwise return null
 		return null;
 	}
 
 	// =============================================================================================
 	// Constructor helpers
 	// =============================================================================================
 
 	/**
 	 * This method reads in the map.data file, and constructs a 2D array of
 	 * char. It checks the dimensions of the Board against those in the file
 	 * (file reading will fail if different).
 	 * 
 	 * @return - will return null, and print a message if the file reading
 	 *         failed.
 	 */
 	private char[][] readFromFile() {
 		char[][] rawData = null;
 
 		try {
 			Scanner scan = new Scanner(new File("map.data"));
 
 			// read size of board (first line)
 			// --------------------------------
 			String line = scan.nextLine();
 			Scanner lineScan = new Scanner(line);
 			this.cols = lineScan.nextInt();
 			this.rows = lineScan.nextInt();
 
 			// initialise this.map and 2D
 			// ----------------------------------
 			rawData = new char[this.rows][this.cols];
 
 			// read cells into array
 			// ---------------------
 			for (int i = 0; i < this.rows; i++) {
 				if (!scan.hasNextLine()) {
 					break;
 				}
 				line = scan.nextLine();
 				for (int j = 0; j < this.cols; j++) {
 					rawData[i][j] = line.charAt(j);
 				}
 			}
 
 		} catch (IOException e) {
 			System.out.println(e);
 		}
 
 		return rawData;
 
 	}
 
 	/**
 	 * Reads the data from the given 2D char array and constructs the Board (a
 	 * 2D array of Cells each representing a square on the board). This method
 	 * puts the cells into the map array, connects the Cells together to form a
 	 * traversible graph, and puts the characters that are playing on their
 	 * starting positions.
 	 * 
 	 * Key 
 	 * ===
 	 * Rooms (entrance square lower case)
 	 * -----
 	 * s = spa
 	 * t = theatre
 	 * l = living room
 	 * o = observatory
 	 * p = patio
 	 * h = hall
 	 * k = kitchen
 	 * d = dining room
 	 * g = guest house
 	 * w = swimming pool
 	 *
 	 * Corridor (movable squares)
 	 * --------------------------
 	 * ? = question mark square
 	 * . = moveable square	(players can walk here)
 	 *
 	 * Player start positions:
 	 * -----------------------
 	 * 6 = Victor Plum
 	 * 5 = Eleanor Peacock
 	 * 4 = Jacob Green
 	 * 3 = Diane White
 	 * 2 = Jack Mustard
 	 * 1 = Kasandra Scarlett
 	 * 
 	 * 
 	 * @param - a 2D char[][] representing the board.
 	 */
 	private void constructBoard(char[][] rawData) {
 		// initialise the Board structure, and startingCells map
 		this.map = new Cell[this.rows][this.cols];
 		this.startingCells = new HashMap<Character, Cell>();
 
 		// Make all the RoomCell objects
 		// ----------------------
 		RoomCell s = new RoomCell(Room.Spa, new Point(3,5), false);
 		RoomCell t = new RoomCell(Room.Theatre, new Point(10,5), false);
 		RoomCell l = new RoomCell(Room.LivingRoom, new Point(17,5), false);
 		RoomCell o = new RoomCell(Room.Observatory, new Point(23,5),false);
 		RoomCell p = new RoomCell(Room.Patio, new Point(4,14),false);
 		RoomCell h = new RoomCell(Room.Hall, new Point(22,14),false);
 		RoomCell k = new RoomCell(Room.Kitchen, new Point(4,24),false);
 		RoomCell d = new RoomCell(Room.DiningRoom, new Point(10,24),false);
 		RoomCell g = new RoomCell(Room.GuestRoom, new Point(22,24),false);
 		RoomCell w = new RoomCell(Room.SwimmingPool, new Point(13,14),true);
 
 		// add to the map from Room to Cell
 		rooms.put(Room.Spa, s);
 		rooms.put(Room.Theatre, t);
 		rooms.put(Room.LivingRoom, l);
 		rooms.put(Room.Observatory, o);
 		rooms.put(Room.Patio, p);
 		rooms.put(Room.Hall, h);
 		rooms.put(Room.Kitchen, k);
 		rooms.put(Room.DiningRoom, d);
 		rooms.put(Room.GuestRoom, g);
 		rooms.put(Room.SwimmingPool, w);
 
 		// Put all Cells into the map, and add their positions, save starting
 		// cells
 		// -------------------------------------------------------------------------
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				char c = rawData[i][j];
 				switch (c) {
 				case 'S': // all the rooms
 					map[i][j] = s;
 					break;
 				case 'T':
 					map[i][j] = t;
 					break;
 				case 'L':
 					map[i][j] = l;
 					break;
 				case 'O':
 					map[i][j] = o;
 					break;
 				case 'P':
 					map[i][j] = p;
 					break;
 				case 'H':
 					map[i][j] = h;
 					break;
 				case 'K':
 					map[i][j] = k;
 					break;
 				case 'D':
 					map[i][j] = d;
 					break;
 				case 'G':
 					map[i][j] = g;
 					break;
 				case 'W':
 					map[i][j] = w;
 					break;
 				case '?': // intrigue square
 					map[i][j] = new CorridorCell(new Point(j, i), true);
 					this.intrigueCells.add((CorridorCell) map[i][j]);
 					break;
 				default: // otherwise assume its a corridor, starting point, or
 							// entrance
 					map[i][j] = new CorridorCell(new Point(j, i), false);
 					// if its a starting point, add it to the 'startingCells'
 					// map
 					if (java.lang.Character.isDigit(c)) {
 						int index = Integer.parseInt(java.lang.Character
 								.toString(c));
 						startingCells.put(Character.values()[index - 1],
 								map[i][j]);
 					}
 					break;
 				}
 				;
 			}
 		}
 
 		// Connect the entrance squares to the rooms
 		// ------------------------------------------
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				char c = rawData[i][j];
 				Cell entrance = map[i][j];
 
 				switch (c) {
 				case 's':
 					entrance.connectTo(s);
 					s.connectTo(entrance);
 					break;
 				case 't':
 					entrance.connectTo(t);
 					t.connectTo(entrance);
 					break;
 				case 'l':
 					entrance.connectTo(l);
 					l.connectTo(entrance);
 					break;
 				case 'o':
 					entrance.connectTo(o);
 					o.connectTo(entrance);
 					break;
 				case 'p':
 					entrance.connectTo(p);
 					p.connectTo(entrance);
 					break;
 				case 'h':
 					entrance.connectTo(h);
 					h.connectTo(entrance);
 					break;
 				case 'k':
 					entrance.connectTo(k);
 					k.connectTo(entrance);
 					break;
 				case 'd':
 					entrance.connectTo(d);
 					d.connectTo(entrance);
 					break;
 				case 'g':
 					entrance.connectTo(g);
 					g.connectTo(entrance);
 					break;
 				case 'w':
 					entrance.connectTo(w);
 					w.connectTo(entrance);
 					break;
 				}
 				;
 			}
 		}
 
 		// Connect corridor squares together
 		// -----------------------------------------------------
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				Cell c = map[i][j];
 				if (c instanceof CorridorCell) {
 					this.connectCorridors((CorridorCell) c);
 				}
 			}
 		}
 
 		// connect secret passages
 		s.setSecretPassage(g);
 		g.setSecretPassage(s);
 		k.setSecretPassage(o);
 		o.setSecretPassage(k);
 	}
 
 	/**
 	 * This method connects the given corridor cell to its surrounding
 	 * neighbours on the board (if the neighbour is a corridor cell, and not itself)
 	 * 
 	 * @param corridor
 	 */
 	private void connectCorridors(CorridorCell corridor) {
 		// get coordinates of corridor
 		Point position = corridor.getPosition();
 		int row = position.y;
 		int col = position.x;
 
 		// connect each Cell at Point (i,j) surrounding corridor if possible:
 		connectCorridorToNeighbour(corridor, row, col + 1);
 		connectCorridorToNeighbour(corridor, row, col - 1);
 		connectCorridorToNeighbour(corridor, row + 1, col);
 		connectCorridorToNeighbour(corridor, row - 1, col);
 	}
 
 	/**
 	 * Helper method for connectCorridors - it connects the given cell to it's
 	 * neighbour at position (row, col) on the board (if that point is on the
 	 * board, and the cell is a corridor cell, and not itself)
 	 * 
 	 * @param row
 	 * @param col
 	 * @return
 	 */
 	private void connectCorridorToNeighbour(Cell toConnect, int row, int col) {
 		// as long as (row, col) is within bounds of the board AND
 		// the Cell at that pos is a CorridorCell AND
 		// the cell isn't itself, then connect that cell to 'corridor'
 		if (row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
 			Cell other = map[row][col];
 			if (other instanceof CorridorCell && other != toConnect) {
 				toConnect.connectTo(other);
 				other.connectTo(toConnect);
 			}
 		}
 	}
 
 	/**
 	 * This adds the collection of players to the board.
 	 * 
 	 * @param currPlayers
 	 */
 	private void addPlayers(Collection<? extends MoveableCharacter> currPlayers) {
 		// put the characters on the board at their starting locations, and
 		// update the Player's position
 		// ----------------------------------------------------------------------------------------------
 		for (MoveableCharacter p : currPlayers) {
 			// set player position to their default start position.
 			Cell start = startingCells.get(p.getCharacter());
 			players.put(p.getCharacter(), p);
 			setPlayerPosition(p.getCharacter(), start);
 		}
 	}
 
 	
 	
 	/**
 	 * For testing :)
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		// create a set of players
 		Set<MoveableCharacter> players = new HashSet<MoveableCharacter>();
 
 		List<Weapon> weapon = new ArrayList<Weapon>();
 		List<Room> room = new ArrayList<Room>();
 		List<Character> chara = new ArrayList<Character>();
 
 		MoveableCharacter p1 = new Player(Character.Scarlett, chara, weapon, room);
 		MoveableCharacter p2 = new Player(Character.Green, chara, weapon, room);
 		MoveableCharacter p3 = new Player(Character.Peacock, chara, weapon, room);
 		players.add(p1);
 		players.add(p2);
 		players.add(p3);
 
 
 
 
 
 			
 			
 			
 		
 //		System.out.println(p3.getPosition());
 //		b.drawBoard();
 //		
 //		Map<Room, Integer> options = b.getDistanceToAllRooms(p3);
 //		
 //		for (Room r : options.keySet()) {	
 //			System.out.println(r + ": " + options.get(r));
 //		}
 		
 //		path = b.getBestPathTo(p1, Room.Spa);
 //		b.setPlayerPosition(p1, path.get(path.size()-1));
 //		System.out.println(p1.getPosition());
 //		b.drawBoard();
 //
 //		Map<Room, Integer> options = b.getDistanceToAllRooms(p1);
 //
 //		for (Room r : options.keySet()) {
 //			System.out.println(r + ": " + options.get(r));
 //		}
 
 	}
 
 }
