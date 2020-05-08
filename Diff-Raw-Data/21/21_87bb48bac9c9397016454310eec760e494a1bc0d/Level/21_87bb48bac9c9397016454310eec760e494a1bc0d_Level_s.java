 package edu.itba.skbsolver;
 
 import java.awt.Point;
 import java.io.File;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A Scenario is a data structure to keep a map in screen. A map is a set of
  * walls and targets, in this case represented by chars in a array.
  * 
  * @author eordano
  */
 public class Level extends LevelParser {
 
 	final static Logger logger = LoggerFactory.getLogger(Level.class);
 
 	public static final int[] dx = { 0, 1, 0, -1 };
 	public static final int[] dy = { 1, 0, -1, 0 };
 
 	// The entire list of capacitors in a map.
 	private List<Capacitor> capacitors;
 
 	// This should be List<Capacitor>[][] but Java doesn't allow it
 	private Object[][] capacitorMap;
 
 	// This map returns what tiles are "step-able" by boxes without triggering
 	// a simple deadlock. Example: a box trapped in a corner
 	private boolean[][] isDeadlock;
 
 	// TODO: a vector with distances to the nearest targets to use in heuristics
 	public int[][] heuristicDistance;
 
 	// A representation of the initial status
 	public State initial;
 
 	/**
 	 * Create a Scenario from a filename. The initial position of things is
 	 * stored locally and can be accessed with getInitialSnap().
 	 * 
 	 * @param fileName
 	 */
 	Level(File file) {
 		super(file);
 
 		int[] initialBoxes = new int[boxesBuffer.size()];
 		int j = 0;
 		for (Integer box : boxesBuffer) {
 			initialBoxes[j++] = box;
 		}
 
 		// Zobrist Keys must be created before creating the initial State
 		createZobristKeys();
 
 		initial = new State(initialBoxes, playerBuffer.get(0), this, 0);
 
 		capacitors = new LinkedList<Capacitor>();
 
 		capacitorMap = new Object[xsize][ysize];
 
 		isDeadlock = new boolean[xsize][ysize];
 
 		for (int i = 0; i < xsize; i++) {
 			for (j = 0; j < ysize; j++) {
 				isDeadlock[i][j] = true;
 				capacitorMap[i][j] = new LinkedList<Capacitor>();
 			}
 		}
 
 		
 		calculateDeadlocks();
 
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < xsize; i++){
 			for (j = 0; j < ysize; j++){
 				if (map[i][j] == '#') s.append('#');
 				if (map[i][j] == ' ' || map[i][j] == '.') s.append(isDeadlock[i][j]?'X':'O');
 			}
 			s.append('\n');
 		}
 		logger.info("These are deadlocks: \n" + s.toString());
 		
 		
 		calculateHallwayCapacitors();
 		calculateTwinsCapacitors();
 		calculateCornerCapacitors();
 		return;
 	}
 
 	public State getInitialState() {
 		return initial;
 	}
 
 	/**
 	 * Return the tile in a given position
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public char get(int x, int y) {
 		try {
 			return map[x][y];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return (' ');
 		}
 	}
 
 	/**
 	 * Is the game over if I'm in a given status?
 	 */
 	public boolean playerWin(State snap) {
 		for (int box : snap.boxes) {
 			if (map[box >> 16][box & 0xFFFF] != '.') {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Get all capacitors for the level
 	 * 
 	 * @return
 	 */
 	public List<Capacitor> getCapacitors() {
 		return this.capacitors;
 	}
 
 	/**
 	 * Return the list of capacitors generated for a random position.
 	 * 
 	 * This method retrieves a list of capacitors involved with this tile, so
 	 * another method can check if he can "step into" this tile.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Capacitor> getCapacitorsByPos(int x, int y) {
 		return (List<Capacitor>) this.capacitorMap[x][y];
 	}
 
 	/**
 	 * Check if a given position is a basic dead lock
 	 * 
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean isBasicDeadlock(int x, int y) {
 		try {
 			return this.isDeadlock[x][y];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return true;
 		}
 
 	}
 
 	/**
 	 * Calculate simple Deadlocks.
 	 * 
 	 * This method figures out in what cells can a block be before going to a
 	 * target. Given the 1 to 1 relationship that exists between boxes an
 	 * targets, if a box is in, say, a corner, it will never be able to get to a
 	 * target and the game is over.
 	 * 
 	 * This algorithm starts with a box in every target and does a BFS by
 	 * placing a box in the target and after that, branches out placing a player
 	 * in every direction, with roles swaped (the box pushes the player). If the
 	 * player ends up in a legal position, the tile in which the box is gets
 	 * marked as "step-able" by a box.
 	 */
 	private void calculateDeadlocks() {
 		List<Point> targets = new LinkedList<Point>();
 		Deque<Point> queue;
 		heuristicDistance = new int[xsize][ysize];
 
 		int rx, ry;
 
 		for (int i = 0; i < xsize; i++) {
 			for (int j = 0; j < ysize; j++) {
 				heuristicDistance[i][j] = -1;
 				if (get(i, j) == '.') {
 					heuristicDistance[i][j] = 0;
 					isDeadlock[i][j] = false;
 					targets.add(new Point(i, j));
 				}
 			}
 		}
 
 		for (Point p : targets) {
 			queue = new LinkedList<Point>();
 
 			queue.addLast(p);
 
 			// While queue is not empty:
 			while (!queue.isEmpty()) {
 
 				// Get the last element
 				p = queue.removeFirst();
 
 				// For each direction
 				for (int d = 0; d < 4; d++) {
 					rx = p.x + dx[d];
 					ry = p.y + dy[d];
 
 					if (heuristicDistance[rx][ry] == -1 // If I never visited
 														// that spot
 							// And it doesn't have a wall
 							&& get(rx, ry) != '#'
 							// And there is no wall behind that spot
 							&& get(rx + dx[d], ry + dy[d]) != '#') {
 						// It's a safe point
 						queue.addLast(new Point(rx, ry));
 						heuristicDistance[rx][ry] = heuristicDistance[p.x][p.y] + 1;
 						isDeadlock[rx][ry] = false;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Create Zobrist hash random strings.
 	 * 
 	 * This works the following way:
 	 * 
 	 * For the entire game, each tile is asigned a random string generated at
 	 * the begining of the game. When a piece gets into that tile, the State
 	 * key gets XORed with the Zobrist key of that tile. A piece in Sokoban can
 	 * be either a player or a box, so we create two different zobrist keys.
 	 * 
 	 * This program asumes that if two Zobrist keys are the same, the states are
 	 * the same (you have to be very unlucky to hit the same hash with two
 	 * different States)
 	 */
 	private void createZobristKeys() {
 		Random randGen = new Random(xsize + ysize);
 		playerZobrist = new int[xsize][ysize];
 		boxZobrist = new int[xsize][ysize];
 		for (int i = 0; i < xsize; i++) {
 			for (int j = 0; j < ysize; j++) {
 				playerZobrist[i][j] = randGen.nextInt();
 				boxZobrist[i][j] = randGen.nextInt();
 			}
 		}
 	}
 
 	/**
 	 * This method scans the map for a "Hallway capacitor".
 	 * 
 	 * These are a set of tiles that, once a box gets into some tile, it can not
 	 * escape, but there can be a target in that set.
 	 * 
 	 * Example:    
 	 *          ########
 	 * 		    # 1.$2 #
 	 * 
 	 * There, the sign "$" represents a box. The white spaces, and the point,
 	 * and the tile under the '$', is a "Hallway Set" of capacity 1. If another
 	 * box wants to get into the Set, it raises a Deadlock.
 	 * 
 	 */
 	private void calculateHallwayCapacitors() {
 		// TODO
 	}
 
 	/**
 	 * This method scans the map for "Corner capacitors".
 	 * 
 	 * When the map contains a corner of this form:
 	 * 
 	 * ###
 	 * # 2
 	 * #1
 	 * 
 	 * And the tiles 1 and 2 are occupied by boxes, that is a deadlock right
 	 * there, except in the case that the player is in the tile 3. Since that
 	 * can be only possible if that is the initial position, we'll be careful
 	 * not to count it.
 	 */
 	private void calculateCornerCapacitors() {
 		// TODO
 	}
 
 	/**
 	 * This method scans the map for "Twin capacitors".
 	 * 
 	 * A set of tiles is said to be a "Twin capacitor set" if it is of this
 	 * form:
 	 * 
 	 * ###
 	 * # 1
 	 * # 2
 	 * # 3
 	 * ###
 	 * 
 	 * In this case, moving any of these boxes triggers corner capacitors. But
 	 * it may be the case that they're of this form:
 	 * 
 	 * ###
 	 * #.3
 	 * #.2
 	 * #.1
 	 * ###
 	 * 
 	 * So the amount of targets inside the area is also counted.
 	 * 
 	 * A Twin set can also be of these forms:
 	 * 
 	 * ####         ####             ######
	 * # 31         # 41             # 1 <- this one looks like a corner capacitor
 	 * # 42         # 52             # 52 <- or a freeze deadlock
 	 * ####         # 63             #43
 	 * cap: 1       ####             # cap: 2
 	 *               cap: 3
 	 */
 	private void calculateTwinsCapacitors() {
 		// TODO
 	}
 
 	/**
 	 * This method is used to dinamically add new capacitors.
 	 * 
 	 * @param boxesAsWalls
 	 */
 	public void addNewCapacitor(List<Integer> boxesAsWalls, int targets) {
 		// TODO Auto-generated method stub
		logger.info("New Capacitor :)");
		Capacitor cap = new Capacitor(boxesAsWalls.size() - targets - 1);
 		capacitors.add(cap);
 		for (Integer Box : boxesAsWalls) {
 			int box = Box;
 			this.getCapacitorsByPos(box >> 16, box & 0xFFFF).add(cap);
 		}
 	}
 }
