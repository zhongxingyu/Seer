 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Random;
 
 /**
  * Class used to initialize a board configuration. It contains 
  * all the static elements of the board and a <code>State</code>
  * object which holds the initial configuration of the dynamic elements.
  * 
  * @author Erik
  *
  */
 public class Board {
 	public static final byte FLOOR = (1 << 0);
 	public static final byte WALL  = (1 << 1);
 	public static final byte GOAL  = (1 << 2);
 	public static final byte DEAD  = (1 << 3);
 
 	private static final byte NOT_CORNERED = -1;
 	private static final byte NW = 0;
 	private static final byte NE = 1;
 	private static final byte SW = 2;
 	private static final byte SE = 3;
 
 	/**
 	 * Matrix of static elements on the board.
 	 */
 	private static byte[][] board;
 	/**
 	 * Random values used to calculate hash functions.
 	 */
 	public static long[][] zValues;
 	/**
 	 * Number of board rows.
 	 */
 	public static byte rows = 0;
 	/**
 	 * Number of row columns.
 	 */
 	public static byte cols = 0;
 	/**
 	 * Vector of goal positions.
 	 */
 	public static Collection<BoardPosition> goalPositions = new LinkedList<BoardPosition>();
 	/**
 	 * The initial state of the board.
 	 */
 	public static State initialState;
 
 	/**
 	 * Constructs a board using an vector of
 	 * strings supplied from the course server.
 	 * 
 	 * @param lines Lines from the server
 	 */
 	public Board(ArrayList<String> lines) {
 		rows = (byte) lines.size();
 		cols = (byte) 0;
 
 		for(String r : lines) {
 			if(r.length()>cols) {
 				cols = (byte) r.length();
 			}
 		}
 
 		/*
 		 * Pad the sides so we don't have to worry about edge effects.
 		 */
 		board   = new byte[rows+2][cols+2];
 		zValues = new long[rows+2][cols+2];
 
 		BoardPosition playerPosition = null;
 		Collection<BoardPosition> boxPositions = new LinkedList<BoardPosition>();
 
 		Random random = new Random();
 		for (byte i=1; i<=rows; i++) {
 			String line = lines.get(i-1);
 			for (byte j=1; j<=line.length(); j++) {
 				char character = line.charAt(j-1);
 
 				zValues[i][j] = random.nextLong();
 				board[i][j] = FLOOR;
 				switch (character) {
 				case '#':	// wall
 					board[i][j] = WALL;
 					break;
 				case '@':	// player
 					playerPosition = new BoardPosition(i, j);
 					break;
 				case '+':	// player on goal
 					board[i][j] |= GOAL;
 					goalPositions.add(new BoardPosition(i, j));
 					playerPosition = new BoardPosition(i, j);
 					break;
 				case '$':	// box
 					boxPositions.add(new BoardPosition(i, j));
 					break;
 				case '*':	// box on goal
 					board[i][j] |= GOAL;
 					goalPositions.add(new BoardPosition(i, j));
 					boxPositions.add(new BoardPosition(i, j));
 					break;
 				case '.':	// goal
 					board[i][j] |= GOAL;
 					goalPositions.add(new BoardPosition(i, j));
 					break;
 				case ' ':	// floor
 					board[i][j] = FLOOR;
 					break;
 				}
 			}
 		}
 
 		markDead();
 
 		initialState = new State(this, playerPosition, boxPositions);
 	}
 
 	public static boolean floorAt(byte row, byte col) {
 		return (board[row][col] & FLOOR) != 0;
 	}
 
 	public static boolean goalAt(byte row, byte col) {
 		return (board[row][col] & GOAL) != 0;
 	}
 
 	public static boolean wallAt(byte row, byte col) {
 		return board[row][col] == WALL;
 	}
 
 	public static final boolean deadAt(byte row, byte col) {
 		return (board[row][col] & DEAD) != 0;
 	}
 
     /**
      * Marks dead-end squares, from which a box cannot be moved to a goal.
      *
      * Example (view in fixed-width font!):
      * <pre><code>
      * #########
      * #       #  #####
      * #       ####   #
      * #              #
      * #   G          #
      * #              #
      * ################
      * <code></pre>
      * The above example, where <code>G</code> marks a goal, would be marked as:
      * <pre><code>
      * #########
      * #DDDDDDD#  #####
     * #D      ####DDD#
      * #D            D#
      * #D  G         D#
      * #DDDDDDDDDDDDDD#
      * ################
      * </code></pre>
      * Where <code>D</code> marks a dead end.
      */
 	private static void markDead() {
 		for (byte row = 1; row<=rows; row++) {
 			for (byte col = 1; col<=cols; col++) {
 				if (!floorAt(row, col)) {
 					continue;
 				}
 
 				byte isCornered = isCornered(row, col);
 				if (isCornered != NOT_CORNERED) {
 					if (goalAt(row, col)) {
 						continue;
 					}
 
 					// It's a non-goal corner => dead end
 					board[row][col] |= DEAD;
 
 					byte rInc = 0, cInc = 0;
 					switch (isCornered) {
 					case NW:
 						rInc = -1;
 						cInc = -1;
 						break;
 					case NE:
 						rInc = -1;
 						cInc = 1;
 						break;
 					case SW:
 						rInc = 1;
 						cInc = -1;
 						break;
 					case SE:
 						rInc = 1;
 						cInc = 1;
 						break;
 					}
 
 					byte currCol = col;
 					while (currCol>0 && currCol<=cols) {
 						currCol -= cInc;
 
 						if (goalAt(row, currCol)) {
 							break;
 						} else if (wallAt(row, currCol)) {
 							for (byte c = (byte) (currCol + cInc); c != col; c += cInc) {
 								board[row][c] |= DEAD;
 							}
 							break;
 						} else if (!wallAt((byte) (row + rInc), currCol)) {
 							break;
 						}
 					}
 
 					byte currRow = row;
 					while (currRow>0 && currRow<=rows) {
 						currRow -= rInc;
 						if (goalAt(currRow, col)) {
 							break;
 						} else if (wallAt(currRow, col)) {
 							for (byte r = (byte) (currRow + rInc); r != row; r += rInc) {
 								board[r][col] |= DEAD;
 							}
 							break;
 						} else if (!wallAt(currRow, (byte) (col + cInc))) {
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private static byte isCornered(byte r, byte c) {
 		if (wallAt((byte) (r-1), c) && wallAt(r, (byte) (c-1)))
 			return NW;
 		if (wallAt((byte) (r-1), c) && wallAt(r, (byte) (c+1)))
 			return NE;
 		if (wallAt(r, (byte) (c+1)) && wallAt((byte) (r+1), c))
 			return SE;
 		if (wallAt(r, (byte) (c-1)) && wallAt((byte) (r+1), c))
 			return SW;
 
 		return NOT_CORNERED;
 	}
 
 
 	@Override
 	public String toString() {
 		String result = "";
 
 		for(byte i=1; i<=rows; i++) {
 			for(byte j=1; j<=cols; j++) {
 
 				if(deadAt(i, j)) {
 					result += 'x';
 				}
 				else if(goalAt(i, j)) {
 					result += '.';
 				}
 				else if(floorAt(i, j)) {
 					result += ' ';
 				}
 				else if(wallAt(i, j)) {
 					result += '#';
 				}
 			}
 			result += "\n";
 		}
 
 		return result;
 	}
 }
