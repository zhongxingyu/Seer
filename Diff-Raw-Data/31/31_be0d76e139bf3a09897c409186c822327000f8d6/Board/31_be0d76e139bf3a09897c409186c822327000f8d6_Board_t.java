 import java.util.Vector;
 
 public class Board {
 	// Dimensions of board.
 	protected final int width, height;
 	protected Symbol[] cells;
 
 	// Save string path while searching through the game tree.
 	protected String path;
 
 	// Current player position
 	protected int playerPos;
 
 	Board(String boardRep, int width, int height) {
 		this.width = width;
 		this.height = height;
 
 
 		cells = new Symbol[width*height];
 
 		int rowMul = 0;
 		for (String row : boardRep.split("\n")) {
 			for (int k  = 0; k  < row.length(); k++) {
 				cells[rowMul+k] = Symbol.fromChar(row.charAt(k));
 			}
 			rowMul += width;
 
 		}
 	}
 
 
 
 	Board(Symbol[] cells, int width, int height) {
 		this.cells = cells;
 		this.width = width;
 		this.height = height;
 	}
 	
 	Board(Board board, Direction dir) {
 		this.width = board.width;
 		this.height = board.height;
 		this.path = board.path;
 		this.cells = board.cells;
 		updateBoard(dir);
 	}
 	
 	public void addDirectionToPath(Direction dir) {
 		switch (dir) {
 			case UP:
 				path += "U";
				break;
 			case DOWN:
 				path += "D";
				break;
 			case LEFT:
 				path += "L";
				break;
 			case RIGHT:
 				path += "R";
				break;
 		}
 	}
 	
 	private void updateBoard(Direction dir) {
 		cells[playerPos] = Symbol.FLOOR;
 		int maybeBoxPos = 0;
 		switch (dir) {
 		case UP:
 //			System.out.print("\nMove player from " + playerPos);
 			playerPos -= width;
 			maybeBoxPos = playerPos - width;
 //			System.out.print(" to " + playerPos);
			break;
 		case DOWN:
 //			System.out.print("\nMove player from " + playerPos);
 			playerPos += width;
 			maybeBoxPos = playerPos + width;
 //			System.out.print(" to " + playerPos);
			break;
 		case LEFT:
 //			System.out.print("\nMove player from " + playerPos);
 			playerPos--;
 			maybeBoxPos = playerPos - 1;
 //			System.out.print(" to " + playerPos);
			break;
 		case RIGHT:
 //			System.out.print("\nMove player from " + playerPos);
 			playerPos++;
 			maybeBoxPos = playerPos + 1;
 //			System.out.print(" to " + playerPos);
			break;
 		}
 		
 		if (cells[playerPos] == Symbol.BOX)
 			cells[maybeBoxPos] = Symbol.BOX;
 		cells[playerPos] = Symbol.PLAYER;
 	}
 	
 	//DO FUCKING MOVE!
 //	public Board move(Direction dir) {
 //		Board board = new Board(cells.clone(), width, height);
 //		// TODO: Do move
 //		return board;
 //	}
 
 	public int getWidth() {
 		return this.width;
 	}
 
 	public int getHeight() {
 		return this.height;
 	}
 
 	public Symbol[] getCells() {
 		return this.cells;
 	}
 
 	/**
 	 * Returns a string representation of this board.
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < cells.length; i++) {
 			if (i > 0 && i % width == 0) sb.append('\n');
 			sb.append(cells[i]);
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Outputs the string representation of this board to the console.
 	 */
 	public void write() {
 		System.out.println(toString());
 	}
 
 	/**
 	 * Returns a Vector instance with all possible player moves
 	 * (as Direction instances) that are valid on this board.
 	 */
 	public Vector<Direction> findPossibleMoves() {
 		Vector<Direction> moves = new Vector<Direction>(4);
 		for (Direction dir : Direction.values()) {
			int to = translatePos(playerPos, dir);
			if (isEmptyCell(to) ||
					(at(to) == Symbol.BOX &&
					isEmptyCell(translatePos(to, dir))))
 				moves.add(dir);
 		}
 		return moves;
 	}
 
 	/**
 	 * Returns true if the cell at the specified position is empty,
 	 * and a valid target for movement.
 	 */
 	public boolean isEmptyCell(int pos) {
 		Symbol c = at(pos);
		if (c == Symbol.FLOOR || c == Symbol.GOAL)
			return true;
		return false;
 	}
 
 	/**
 	 * Returns a position translated in a specified direction.
 	 */
 	public int translatePos(int pos, Direction dir) {
 		switch (dir) {
 		case UP:
 			return (pos - this.width);
 		case DOWN:
 			return (pos + this.width);
 		case LEFT:
 			return (pos - 1);
 		case RIGHT:
 			return (pos + 1);
 		}
		return pos;
 	}
 
 	/**
 	 * Returns the symbol located at the specified position.
 	 */
 	public Symbol at(int pos) {
 		if (pos < 0 || pos >= cells.length)
 			return Symbol.WALL;
 		return cells[pos];
 	}
 
 	/**
 	 * Returns the symbol located in the specified direction from a position.
 	 */
 	public Symbol at(int pos, Direction dir) {
 		return at(translatePos(pos, dir));
 	}
 
 	/**
 	 * Returns true if this board represents a finished game,
 	 * where all boxes are placed on goals.
 	 */
 	public boolean isEOG() {
 		for (Symbol s : cells)
 			if (s == Symbol.BOX) return false;
 				return true;
 	}
 }
