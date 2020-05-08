 public class Board {
 	// Dimensions of board.
 	protected int width, height;
 
 	protected Symbol[] cells;
 
 	// Save string path while searching through the game tree.
 	// To avoid unnecessary time waste, save player's position.
 	String path;
 	int playerIndex;
 
 	Board(Board board, Direction dir) {
 	}
 
 	public void doMove(Direction dir) {
 	}
 
 	public Direction[] findPossibleMoves() {
 		return null;
 	}
 
 	/**
 	 * Returns true if the cell at the specified position is empty,
 	 * and a valid target for movement.
 	 */
	public isEmptyCell(int pos) {
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
 }
