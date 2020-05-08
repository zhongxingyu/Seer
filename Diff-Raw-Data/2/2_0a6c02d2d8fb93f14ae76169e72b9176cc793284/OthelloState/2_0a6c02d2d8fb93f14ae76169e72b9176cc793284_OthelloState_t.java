 package othello;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import gamePlayer.Action;
 import gamePlayer.State;
 
 /**
  * A State in the board game Othello.
  * @author Ashoat Tevosyan
  * @author Peter Brook
  * @since Sat April 23 2011
  * @version CSE 473
  */
 public class OthelloState implements State {
 	
 	// Debug?
 	private static final boolean DEBUG = false;
 	// The dimension of this board. Since we're using shorts, must be less than equal to 8.
 	private static final int dimension = 8;
 	// A lookup table to figure out the point differential in a line 
 	private static byte[] pointTable = new byte[65536];
 	
 	/**
 	 * Lookup table that has all results for lines from moves.
 	 * 00 = empty; 01 = ignored; 10 = white; 11 = black.
 	 * First index is the current line... 2^16; all possible shorts.
 	 * Second index is the index of the move... dimension possibilities.
 	 * Third index is the player making the move... 2 players.
 	 */
 	private static short[][][] lookupTable = new short[65536][dimension][2];
 	
 	/**
 	 * The static constructor to generate our tables.
 	 */
 	static {
 		generatePointsTable((short)0, (byte)0, (byte)0);
 		generateLookupTable((short)0, (byte)0);
 		System.out.println("Finished generating tables!");
 	}
 	
 	/**
 	 * Recursively generate the points table.
 	 * @param line   The line, as so far specified.
 	 * @param points The points, as so far counted.
 	 * @param depth  The depth we have so far traversed.
 	 */
 	private static void generatePointsTable(short line, byte points, byte depth) {
 		// End case?
 		if (depth == dimension) {
 			pointTable[line + 32768] = points;
 			return;
 		}
 		// Recursive!
 		line = (short)(line << 2);
 		generatePointsTable(line, points, (byte)(depth + 1));
 		generatePointsTable((short)(line | 1), points, (byte)(depth + 1));
 		generatePointsTable((short)(line | 2), (byte)(points + 1), (byte)(depth + 1));
 		generatePointsTable((short)(line | 3), (byte)(points - 1), (byte)(depth + 1));
 	}
 	
 	/**
 	 * Get an index on a line.
 	 * @param line  The line to draw from.
 	 * @param index The index on the line we want.
 	 * @return The value at the index on this line.
 	 */
 	private static byte getSpotOnLine(short line, byte index) {
 		if (index >= dimension) return 1;
 		return (byte)((line >> (index * 2)) & 3);
 	}
 	
 	/**
 	 * Set a value at an index on a line, and return the resultant line.
 	 * @param line  The input line.
 	 * @param index The input to set the value at.
 	 * @param value The value to set.
 	 * @return A new line that results from the specified mutation.
 	 */
 	private static short setSpotOnLine(short line, byte index, byte value) {
 		if (index >= dimension) return line;
 		short mask = (short)(3 << (index * 2)); 
 		return (short)((line & ~mask) | (value << (index * 2)));
 	}
 	
 	/**
 	 * Combine the left side, center spot, and right side of a line. 
 	 * @param left   The spots to the left of the center spot.
 	 * @param center The center spot.
 	 * @param right  The spots to the right of the center spot.
 	 * @param index  The index at which the center spot is located.
 	 * @return The combined line. 
 	 */
 	private static short combineIntoLine(short left, byte center, short right, byte index) {
 		short leftMask = (short)((1 << (index * 2)) - 1);
 		short rightMask = (short)~((1 << (index * 2 + 2)) - 1);
 		return (short)((right & rightMask) | (left & leftMask) | (center << (index * 2)));
 	}
 	
 	/**
 	 * Parse a line into a String.
 	 * @param line The line we are parsing.
 	 * @return The String we have parsed into.
 	 */
 	private static String lineToString(short line) {
 		StringBuilder builder = new StringBuilder();
 		for (byte i = 0; i < dimension; i++) {
 			builder.append(getSpotOnLine(line, i));
 		}
 		return builder.toString();
 	}
 	
 	/**
 	 * Recursively generate the lookup table for moves.
 	 * @param line  The line, as so far specified.
 	 * @param depth The depth we have so far traversed.
 	 */
 	private static void generateLookupTable(short line, byte depth) {
 		// End case?
 		if (depth == dimension) {
 			for (byte i = 0; i < dimension; i++) {
 				// Is this spot already set?
 				byte spot = getSpotOnLine(line, i);
 				if (spot != 0) {
 					if (DEBUG) System.out.println("Can't move on index " + i + "! Already occupied.");
 					if (DEBUG) System.out.println(lineToString(line) + " resolves to itself");
 					lookupTable[line + 32768][i][0] = line;
 					lookupTable[line + 32768][i][1] = line;
 					continue;
 				}
 				// Build the right side of possible moves
 				byte j = (byte)(i + 1);
 				short p1Right = line, p2Right = line;
 				byte firstByte = getSpotOnLine(line, j);
 				// Is the first byte to the right of this one actually set?
 				if (firstByte > 1 && j < dimension) {
 					// Is the first byte to the right player 1 or player 2? 
 					boolean first = firstByte == 2;
 					while (true) {
 						j++;
 						if (j >= dimension) break;
 						byte thisByte = getSpotOnLine(line, j); 
 						if (thisByte < 2) break;
 						if (first && thisByte == 2) continue;
 						if (!first && thisByte == 3) continue;
 						// The flips shall occur!
 						if (DEBUG) System.out.println("Flips will occur on right!");
 						j--;
 						if (first) for (; j > i; j--) p2Right = setSpotOnLine(p2Right, j, (byte)3); 
 						else for (; j > i; j--) p1Right = setSpotOnLine(p1Right, j, (byte)2);
 						break;
 					}
 				}
 				// Build the left side of possible moves
 				j = (byte)(i - 1);
 				short p1Left = line, p2Left = line;
 				firstByte = getSpotOnLine(line, j);
 				// Is the first byte to the right of this one actually set?
 				if (firstByte > 1 && j > 0) {
 					// Is the first byte to the right player 1 or player 2? 
 					boolean first = firstByte == 2;
 					while (true) {
 						j--;
 						if (j < 0) break;
 						byte thisByte = getSpotOnLine(line, j); 
 						if (thisByte < 2) break;
 						// Keep going!
 						if (first && thisByte == 2) continue;
 						if (!first && thisByte == 3) continue;
 						// The flips shall occur!
 						if (DEBUG) System.out.println("Flips will occur on left!");
 						j++;
 						if (first) for (; j < i; j++) p2Left = setSpotOnLine(p2Left, j, (byte)3); 
 						else for (; j < i; j++) p1Left = setSpotOnLine(p1Left, j, (byte)2);
 						break;
 					}
 				}
 				short p1 = combineIntoLine(p1Left, (byte)2, p1Right, i);
 				short p2 = combineIntoLine(p2Left, (byte)3, p2Right, i);
 				lookupTable[line + 32768][i][1] = p1;
 				lookupTable[line + 32768][i][0] = p2;
 				if (DEBUG) System.out.println(lineToString(line) + " resolves to " + lineToString(p1));
 				if (DEBUG) System.out.println(lineToString(line) + " resolves to " + lineToString(p2));
 			}
 			return;
 		}
 		// Recursive!
 		line = (short)(line << 2);
 		generateLookupTable(line, (byte)(depth + 1));
 		generateLookupTable((short)(line | 1), (byte)(depth + 1));
 		generateLookupTable((short)(line | 2), (byte)(depth + 1));
 		generateLookupTable((short)(line | 3), (byte)(depth + 1));
 	}
 	
 	// Whose move is it? True = player 1; false = player 2.
 	public final boolean move;
 	// The bit-board storing horizontal lines
 	private short[] hBoard; /* board 0 */
 	// The bit-board storing vertical lines
 	private short[] vBoard; /* board 1 */
 	// The bit-boards storing diagonal lines
 	private short[] dBoard1 /* board 2 */, dBoard2; /* board 3 */
 	// Bit-boards representing valid moves
 	private short[] p1MoveBoard, p2MoveBoard;
 	
 	/**
 	 * Initialize this child OthelloState.
 	 * @param move The player whose move it is.
 	 */
 	private OthelloState(boolean move) {
 		this.move = move;
 	}
 	
 	/**
 	 * Initialize a start-game OthelloState.
 	 */
 	public OthelloState() {
 		this.move = true;
 		this.hBoard = new short[dimension];
 		this.vBoard = new short[dimension];
 		// Diagonal boards have more shorts to store
 		this.dBoard1 = new short[2 * dimension - 1];
 		this.dBoard2 = new short[2 * dimension - 1];
 		// Move boards
 		this.p1MoveBoard = new short[dimension];
 		this.p2MoveBoard = new short[dimension];
 		this.generateMoveBoards();
 	}
 	
 	/**
 	 * Set the standard start state for Othello.
 	 */
 	public void setStandardStartState() {
 		this.executeMove(false, (byte)3, (byte)3);
 		this.executeMove(false, (byte)4, (byte)4);
 		this.executeMove(true, (byte)3, (byte)4);
 		this.executeMove(true, (byte)4, (byte)3);		
 		this.generateMoveBoards();
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public int compareTo(State state) {
 		float heuristic = this.heuristic() - state.heuristic();
 		return heuristic >= 0 ? (int)Math.ceil(heuristic) : (int)Math.floor(heuristic);
 	}
 	
 	/**
 	 * Get the lines relevant to a particular spot on the board.
 	 * @param x The x-coordinate of the spot.
 	 * @param y The y-coordinate of the spot.
 	 * @return The four lines relevant to the specified spot.
 	 */
 	private short[] getLines(byte x, byte y) {
 		short[] lines = new short[4];
 		lines[0] = this.hBoard[x];
 		lines[1] = this.vBoard[y];
 		lines[2] = this.dBoard1[x + y];
 		lines[3] = this.dBoard2[x - y + dimension - 1];
 		return lines;
 	}
 	
 	/**
 	 * Set a line on a board.
 	 * @param board The bit-board to set the line on.
 	 * @param line  The actual value to set that line to.
 	 * @param x     The x-coordinate of the spot the move was on, so we can figure out the index of the line to change.
 	 * @param y     The y-coordinate of the spot the move was on; see above.
 	 */
 	private void setLine(byte board, short line, short x, short y) {
 		switch (board) {
 			case 0:
 				hBoard[x] = line;
 				return;
 			case 1:
 				vBoard[y] = line;
 				return;
 			case 2:
 				dBoard1[x + y - 1] = line;
 				return;
 			case 3:
 				dBoard2[x - y + dimension - 1] = line;
 				return;
 		}
 	}
 	
 	/**
 	 * Get the index of a spot within a line.
 	 * Different boards will use different indexes for their lines.
 	 * @param board The board we are checking.
 	 * @param x     The x-coordinate of the spot.
 	 * @param y     The y-coordinate of the spot.
 	 * @return The index used for this spot.
 	 */
 	private static byte getIndex(byte board, byte x, byte y) {
 		// Only boards 1 and 2 care about which is used as the index
 		if (board == 0) return y;
 		return x;
 	}
 	
 	/**
 	 * Get a short representing only the spots that have changed between two lines.
 	 * @param line    The original state of the line.
 	 * @param newLine The new state of the line.
 	 * @param index   The index of the actual move.
 	 * @return A short representing only the changed spots.
 	 */
 	private static short getFlippedSpots(short line, short newLine, byte index) {
 		short mask = (short)~(3 << (index * 2));
 		return (short)((newLine ^ line) & mask);
 	}
 	
 	/**
 	 * Checks if a move on a spot by the current player is valid.
 	 * @param x      The x-coordinate of the move.
 	 * @param y      The y-coordinate of the move.
 	 * @return       True if valid; false otherwise.
 	 */
 	public boolean moveIsValid(byte x, byte y, boolean player) {
 		// Pass?
 		if (x < 0) {
 			short[] moveBoard = player ? this.p1MoveBoard : this.p2MoveBoard;
 			for (short line : moveBoard) if (line != 0) return false;
 			return true;
 		}
 		// Get current lines on bit-board
 		short[] lines = this.getLines(x, y);
 		// Check all bit-boards
 		for (byte i = 0; i < 4; i++) {
 			byte index = getIndex(i, x, y);
 			short newLine = lookupTable[lines[i] + 32768][index][player ? 1 : 0];
 			// You're moving on top of an occupied space
 			if (newLine == lines[i]) return false;
 			// A flip occurred, so this move is legal. Ignore changes in index spot.
 			if (getFlippedSpots(lines[i], newLine, index) != 0) return true; 
 		}
 		// No flips, so illegal
 		return false;
 	}
 
 	/**
 	 * Generate bit-boards representing the validity of moves for each player.
 	 * 00 = invalid; 10 = valid. 2 bits so we can use point lookup table.
 	 */
 	private void generateMoveBoards() {
 		for (byte i = 0; i < dimension; i++) {
 			for (byte j = 0; j < dimension; j++) {
 				if (this.moveIsValid(i, j, true))
 					this.p1MoveBoard[i] = (short)(this.p1MoveBoard[i] | 2);
 				if (this.moveIsValid(i, j, false))
 					this.p2MoveBoard[i] = (short)(this.p2MoveBoard[i] | 2);
 				if (j + 1 == dimension) break;
 				this.p1MoveBoard[i] = (short)(this.p1MoveBoard[i] << 2);
 				this.p2MoveBoard[i] = (short)(this.p2MoveBoard[i] << 2);
 			}
 		}
 	}
 	
 	/**
 	 * Flip the value on a line at a particular index.
 	 * @param line  The line to use.
 	 * @param index The index to flip at.
 	 * @return The resultant line.
 	 */
 	private static short flipValueOnLine(short line, byte index) {
 		return (short)(line ^ (1 << (2 * index)));
 	}
 	
 	/**
 	 * Change a value on all boards.
 	 * @param x     The x-coordinate to set at.
 	 * @param y     The y-coordinate to set at.
 	 * @param skip  A board to skip setting.
 	 * @param value The value to set.
 	 */
 	private void flipValueOnBoards(byte x, byte y, byte skip) {
 		short[] lines = this.getLines(x, y);
 		for (byte i = 0; i < 4; i++) {
 			if (i == skip) continue; 
 			byte index = getIndex(i, x, y);
 			short newLine = flipValueOnLine(lines[i], index);
 			this.setLine(i, newLine, x, y);
 		}
 	}
 	
 	/**
 	 * Set a spot to a value without repercussions.
 	 * @param x     The x-coordinate of the spot.
 	 * @param y     The y-coordinate of the spot.
 	 * @param value The value to set to.
 	 */
 	public void setValueOnBoards(byte x, byte y, byte value) {
 		short[] lines = this.getLines(x, y);
 		for (byte i = 0; i < 4; i++) {
 			byte index = getIndex(i, x, y);
			short newLine = setSpotOnLine(lines[i], index, value);
 			this.setLine(i, newLine, x, y);
 		}
 	}
 	
 	/**
 	 * Execute a move and mutate our bit-boards to represent this.
 	 * We assume that the other player is making this move.
 	 * @param x The x-coordinate of the move.
 	 * @param y The y-coordinate of the move.
 	 */
 	private void executeMove(boolean player, byte x, byte y) {
 		short[] lines = this.getLines(x, y);
 		// Check all bit-boards
 		for (byte i = 0; i < 4; i++) {
 			byte index = getIndex(i, x, y);
 			short newLine = lookupTable[lines[i] + 32768][index][player ? 1 : 0];
 			// Get a short representing changed spots
 			short flippedSpots = getFlippedSpots(lines[i], newLine, index);
 			// Find the changed spots
 			for (byte j = 0; j < dimension; j++) {
 				// Is this spot changed?
 				if (getSpotOnLine(flippedSpots, j) == 0) continue;
 				// If it's changed, then flip it on all other boards!
 				if (i == 0) this.flipValueOnBoards(x, j, i);
 				else if (i == 1) this.flipValueOnBoards(j, y, i);
 				else if (i == 2) this.flipValueOnBoards(j, (byte)(x + y - j), i);
 				else if (i == 3) this.flipValueOnBoards(j, (byte)(j - x + y), i);
 			}
 			// Set the line that actually changed
 			this.setLine(i, newLine, x, y);
 		}
 	}
 	
 	/**
 	 * Get a new OthelloState that results from a move by the current player.
 	 * @param x The x-coordinate of the move.
 	 * @param y The y-coordinate of the move.
 	 * @return A new OthelloState.
 	 */
 	public OthelloState childOnMove(byte x, byte y) {
 		OthelloState state = (OthelloState)this.clone();
 		// Pass?
 		if (x < 0) {
 			state.p1MoveBoard = this.p1MoveBoard.clone();
 			state.p2MoveBoard = this.p2MoveBoard.clone();
 			return state;
 		} else {
 			state.p1MoveBoard = new short[dimension];
 			state.p2MoveBoard = new short[dimension];
 		}
 		// Make the move
 		state.executeMove(!this.move, x, y);
 		// Get move boards
 		state.generateMoveBoards();
 		return state;
 	}
 	
 	/**
 	 * Returns the difference between player 1 and player 2's total piece count.
 	 * @return The difference between player 1 and player 2's total piece count.
 	 */
 	private float pieceDifferential() {
 		float difference = 0;
 		for (short line : hBoard) difference += pointTable[line + 32768];
 		return difference;
 	}
 	
 	/**
 	 * Returns the difference between the amount of moves player one and player two have available.
 	 * @return the difference between the amount of moves player one and player two have available.
 	 */
 	private float moveDifferential() {
 		float differential = 0;
 		for (byte i = 0; i < dimension; i++)
 			differential += pointTable[p1MoveBoard[i] + 32768] - pointTable[p2MoveBoard[i] + 32768]; 
 		return differential;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public float heuristic() {
 		return this.pieceDifferential() + this.moveDifferential();
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	@SuppressWarnings({ "rawtypes" })
 	public List<Action> getActions() {
 		List<Action> actions = new LinkedList<Action>();
 		short[] moveBoard = this.move ? p1MoveBoard : p2MoveBoard;
 		for (byte i = 0; i < dimension; i++) {
 			short mask = 3;
 			for (byte j = 0; j < dimension; j++) {
 				if ((moveBoard[i] & mask) != 0)
 					actions.add(new OthelloAction(this.move, i, j));
 				mask = (short)(mask << 2);
 			}
 		}
 		if (actions.isEmpty()) actions.add(new OthelloAction(this.move, (byte)-1, (byte)-1));
 		return actions;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Status getStatus() {
 		// Make sure you only run this after generateMoveBoards!
 		// Are there still moves to make?
 		for (short line : p1MoveBoard) if (line != 0) return Status.Ongoing;
 		for (short line : p2MoveBoard) if (line != 0) return Status.Ongoing;
 		// Game over!
 		float differential = this.pieceDifferential();
 		if (differential == 0) return Status.Draw;
 		return differential > 0 ? Status.PlayerOneWon : Status.PlayerTwoWon;
 	}
 	
 	/**
 	 * Returns a deep copy of this OthelloState.
 	 * @return A deep copy of this OthelloState.
 	 */
 	@Override
 	public Object clone() {
 		OthelloState state = new OthelloState(!this.move);
 		state.hBoard = this.hBoard.clone();
 		state.vBoard = this.vBoard.clone();
 		state.dBoard1 = this.dBoard1.clone();
 		state.dBoard2 = this.dBoard2.clone();
 		return state;
 	}
 	
 	/**
 	 * Returns a String representation of this OthelloState.
 	 * @return a String representation of this OthelloState.
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		for (short line : hBoard) {
 			String lineString = lineToString(line);
 			for (int i = 0; i < lineString.length(); i++) {
 				char c = lineString.charAt(i);
 				if (c == '0') builder.append("   ");
 				else if (c == '2') builder.append(" O ");
 				else if (c == '3') builder.append(" X ");
 				if (i + 1 == lineString.length()) break;
 				builder.append("|");
 			}
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 
 }
