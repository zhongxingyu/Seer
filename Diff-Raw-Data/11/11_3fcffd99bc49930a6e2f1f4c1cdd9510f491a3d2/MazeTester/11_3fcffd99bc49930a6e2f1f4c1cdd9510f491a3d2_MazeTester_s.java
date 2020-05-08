 package maze;
 import maze.RandomMaze.Cell;
 
 public class MazeTester {
 	//Declare instance variables
 	private int colN;
 	private int rowN;
 	private Cell[][] board;
 	//Constructor
 	public MazeTester(Cell[][] nBoard, int row, int col){
 		board = nBoard;
 		colN = col;
 		rowN = row;
 	}
 	//Method that determines if a maze is traversable
 	public boolean isTraversable(){
 		return testPath(0,0);
 	}
 	//Method used to recursively find a traversable path
 	public boolean testPath(int row, int col) {
 		board[row][col].visit();
 		//Base case
 		//If the path is at the end, return true
 		if ((col == colN-1) && (row == rowN-1)) {
 			board[row][col].selectCell();
 			return true;
 		}
 		//If the path can move down one cell, do so
 		if ((row < rowN-1) && !board[row + 1][col].marked() && !board[row + 1][col].blocked() && !board[row + 1][col].visited()) {
 			block(row, col);
 			if (testPath(row + 1, col)) {
 				board[row][col].selectCell();
 				return true;
 			}
 			unblock(row, col);
 		}
 		//If the path can move to the right one cell, do so
 		if ((col < colN-1) && !board[row][col + 1].marked() && !board[row][col + 1].blocked() && !board[row][col + 1].visited()) {
 			block(row,col);
 			if (testPath(row, col + 1)) {
 				board[row][col].selectCell();
 				return true;
 			}
 			unblock(row,col);
 		}
 		//If the path can move up one cell, do so
 		if ((row > 0) && !board[row - 1][col].marked() && !board[row - 1][col].blocked() && !board[row - 1][col].visited()) {
 			block(row, col);
 			if (testPath(row - 1, col)) {
 				board[row][col].selectCell();
 				return true;
 			}
 			unblock(row, col);
 		}
 		//If the path can move to the left one cell, do so.
 		if ((col > 0) && !board[row][col - 1].marked() && !board[row][col - 1].blocked() && !board[row][col - 1].visited()) {
 			block(row,col);
 			if (testPath(row, col - 1)) {
 				board[row][col].selectCell();
 				return true;
 			}
 			unblock(row,col);
 		}
 		return false;
 	}
 	//Block adjacent cell to prevent neighboring path
 	public void block(int row, int col) {
 		if (row > 0)
 			board[row - 1][col].block();
 		if (row < rowN-1)
 			board[row + 1][col].block();
 		if (col > 0)
 			board[row][col - 1].block();
 		if (col < colN-1)
 			board[row][col + 1].block();
 	}
 	//Remove the block if within range of the array
 	public void unblock(int row, int col) {
 		if (row > 0)
 			board[row - 1][col].unblock();
 		if (row < rowN-1)
 			board[row + 1][col].unblock();
 		if (col > 0)
 			board[row][col - 1].unblock();
 		if (col < colN-1)
 			board[row][col + 1].unblock();
 	}
}
