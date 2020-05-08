 import java.util.Vector;
 
 public class Board {
 	// Dimensions of board.
 	protected final int width, height;
 	protected Symbol[] cells;
 
 	// Save string path while searching through the game tree.
 	protected String path;
 
 	// Current player position
 	protected int playerPos;
 
 	Board(String boardRep, int height, int width){
 		this.width = width;
 		this.height = height;
 		cells = new Symbol[height*width];
 		
 		String[] rep = boardRep.split("|");
 		for (int j = 0; j < rep.length; j++){
 			for (int i = 0; i < rep[j].length()-1; i++) {
 				switch (rep[j].charAt(i)) {
 				case '#':
 					cells[i+j*width] = Symbol.WALL;
 					break;
 				case '$':
 					cells[i+j*width] = Symbol.BOX;
 					break;
 				case '.':
 					cells[i+j*width] = Symbol.GOAL;
 					break;
 				case '+':
 					cells[i+j*width] = Symbol.PLAYER_GOAL;
 					break;
 				case '*':
 					cells[i+j*width] = Symbol.BOX_GOAL;
 					break;
 				case ' ':
 					cells[i+j*width] = Symbol.FLOOR;
 					break;
 				case '@':
 					cells[i+j*width] = Symbol.PLAYER;
 					break;
 
 				default:
 					cells[i+j*width] = Symbol.WALL;
 					break;
 				}
 			}
 		}
 	}
 
 	Board(Board board, Direction dir, int height, int width) {
 		this.width = width;
 		this.height = height;
 
 	}
 
 	public void write() {
 		for (int i = 0; i < cells.length; i++) {
 			if(cells[i] == null)
 				System.out.print(".");
 			if(i%width == 0)
 				System.out.println();
 		}
 	}
 
 	/**
 	 * Returns a Vector<Direction> instance with all possible player moves
 	 * that are valid on this board.
 	 */
 	public Vector<Direction> findPossibleMoves() {
 		Vector<Direction> moves = new Vector<Direction>(4);
 		for (Direction dir : Direction.values()) {
 			if (canMove(playerPos, dir))
 				moves.add(dir);
 		}
 		return moves;
 	}
 
 	/**
 	 * Returns true if the cell at the specified position is empty,
 	 * and a valid target for movement.
 	 */
 	public boolean isEmptyCell(int pos) {
 		if (pos < 0 || pos >= cells.length)
 			return false;
 		Symbol c = cells[pos];
 		if (c == Symbol.WALL || c == Symbol.BOX)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Returns true if an object (player or box) can be moved from a
 	 * position towards a specified direction.
 	 */
 	public boolean canMove(int pos, Direction dir) {
 		switch (dir) {
 			case UP:
 				return isEmptyCell(pos - this.width);
 			case DOWN:
 				return isEmptyCell(pos + this.width);
 			case LEFT:
 				return isEmptyCell(pos - 1);
 			case RIGHT:
 				return isEmptyCell(pos + 1);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if this board represents a finished game,
 	 * where all boxes are placed on goals.
 	 */
 	public boolean isEOG() {
 		for (Symbol s : cells)
			if (s.BOX) return false;
 		return true;
 	}
 }
