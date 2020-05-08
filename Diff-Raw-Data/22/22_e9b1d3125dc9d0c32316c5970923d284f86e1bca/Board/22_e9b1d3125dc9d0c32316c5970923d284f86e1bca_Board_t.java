 package main;
 
 import java.util.*;
 
 /*
  * The game board is represented by boolean[depth][depth]
  * The indices look like the following:
  * 				(0,0)
  * 			(1,0)	(1,1)
  *		(2,0)	(2,1)	(2,2)
  *	(3,0)	(3,1)	(3,2)	(3,3)
  * (and so on...)
  */
 
 
 public class Board {
 	
 	boolean[][] myBoard;
 	int depth;
 	Vector<Board> validForwardMoves = new Vector<Board>();
 	
 	public Board() {
 		
 	}
 	
 	public Board(Board other) {
 		depth = other.depth;
 		myBoard = new boolean[depth][depth];
 		for (int i=0; i<depth; i++) {
 			for (int j=0; j<i+1; j++)
 				myBoard[i][j] = other.myBoard[i][j];
 		}
 	}
 	
 	public Board(String root, int d) {
 		int row = 0;
 		myBoard = new boolean[d][d];
 		for (int i=0; i<d; i++) {
 			row += i;
 			// Fill myBoard with the appropriate bits
 			for (int j=0; j<i+1; j++) {
 				myBoard[i][j] = root.charAt(row+j) == '1';
 			}
 		}
 		depth = d;
 	}
 	
 	public Board nextMove() {
 		return (Board) validForwardMoves.remove(validForwardMoves.size()-1);
 	}
 	
 	public boolean hasNextMove() {
 		return validForwardMoves.size() > 0;
 	}
 	
 	public boolean findValidForwardMoves() {
 		// Pegs in the corners only have 2 possible moves, so they are easy to check for first
 		// CORNER MOVES START
 		// Start with the top corner
 		if (!myBoard[0][0]) {				// no peg exists at the top-most position
 			if (myBoard[1][0]) {
 				if (myBoard[2][0]) {		// 2 successively adjacent pegs must exist for move to exist
 					// We move the peg from (2,0) to (0,0)
 					validForwardMoves.add(new Board(this).move(2, 0, 0, 0));
 				}
 			}
 			if (myBoard[1][1]) {			// check if the next adjacent peg exists
 				if (myBoard[2][2]) {
 					// We move the peg from (2,2) to (0,0)
 					validForwardMoves.add(new Board(this).move(2, 2, 0, 0));
 				}
 			}
 		}
 		
 		// Then the left corner
 		if (!myBoard[depth-1][0]) {			// No peg exists at the left corner
 			if (myBoard[depth-2][0]) {		// depth-1 in the first line to account for zero index
 				if (myBoard[depth-3][0]) {
 					// We move the peg from (depth-3,0) to (depth-1,0)
 					validForwardMoves.add(new Board(this).move(depth-3, 0, depth-1, 0));
 				}
 			}
 			if (myBoard[depth-1][1]) {
 				if (myBoard[depth-1][2]) {
 					// We move the peg from (depth-1,2) to (depth-1,0)
 					validForwardMoves.add(new Board(this).move(depth-1, 2, depth-1, 0));
 				}
 			}
 		}
 		
 		// Now the right corner
 		if (!myBoard[depth-1][depth-1]) {			// No peg exists at the right corner
 			if (myBoard[depth-2][depth-2]) {		// depth-1 in the first line to account for zero index
 				if (myBoard[depth-3][depth-3]) {
 					// We move the peg from (depth-3,depth-3) to (depth-1,depth-1)
 					validForwardMoves.add(new Board(this).move(depth-3, depth-3, depth-1, depth-1));
 				}
 			}
 			if (myBoard[depth-1][depth-2]) {
 				if (myBoard[depth-1][depth-3]) {
 					// We move the peg from (depth-1,depth-3) to (depth-1,depth-1)
 					validForwardMoves.add(new Board(this).move(depth-1, depth-3, depth-1, depth-1));
 				}
 			}
 		}
 		// CORNER MOVES END
 		
 		// Now check all other positions
 		// Since the board is triangular, pegs can enter a position from 6 different directions:
 		// (In clockwise order:) NE, E, SE, SW, W, NW 
 		for (int i=1; i<depth; i++) {			// We skip the peg at (0,0) since already checked it above
 			for (int j=0; j<i+1; j++) {
 				if (!((i == depth-1 && j == 0) || (i == depth-1 && j == depth-1))) {		  // Skip the corner positions
 					if (!myBoard[i][j]) {			// No peg exists at the current positions
						if (!(i-2 < 0 || j > i-2)) {				// Check that NE move is not outside of the board
 							if (myBoard[i-1][j]) {		// Check NE adjacent peg exists for move to exist
 								if (myBoard[i-2][j]) {
 									validForwardMoves.add(new Board(this).move(i-2, j, i, j));
 								}
 							}
 						}
 						if (!(j+2 > i)) {				// Check the E move is not outside of the board
 							if (myBoard[i][j+1]){		// Check E adjacent peg exists for move to exist
 								if (myBoard[i][j+2]) {
 									validForwardMoves.add(new Board(this).move(i, j+2, i, j));
 								}
 							}
 						}
						if (!(i+2 > depth-1 || j+2 > i+2)) {	// Check the SE move is not outside of the board
 							if (myBoard[i+1][j+1]){			// Check SE adjacent peg exists for move to exist
 								if (myBoard[i+2][j+2]) {
 									validForwardMoves.add(new Board(this).move(i+2, j+2, i, j));
 								}
 							}
 						}
 						if (!(i+2 > depth-1)) {			// Check the SW move is not outside of the board
 							if (myBoard[i+1][j]) {		// Check SW adjacent peg exists for move to exist
 								if (myBoard[i+2][j]) {
 									validForwardMoves.add(new Board(this).move(i+2, j, i, j));
 								}
 							}
 						}
 						if (!(j-2 < 0)) {				// Check the W move is not outside of the board
 							if (myBoard[i][j-1]){		// Check W adjacent peg exists for move to exist
 								if (myBoard[i][j-2]) {
 									validForwardMoves.add(new Board(this).move(i, j-2, i, j));
 								}
 							}
 						}
 						if (!(i-2 < 0 || j-2 < 0)) {	// Check the NW move is not outside of the board
 							if (myBoard[i-1][j-1]){		// Check NW adjacent peg exists for move to exist
 								if (myBoard[i-2][j-2]) {
 									validForwardMoves.add(new Board(this).move(i-2, j-2, i, j));
 								}
 							}
 						}
 					}
 				}
 			}
 		}		
 		
 		return validForwardMoves.size() > 0;
 	}
 	
 	private Board move(int sRow, int sCol, int tRow, int tCol) {
 		myBoard[sRow][sCol] = false;			// Remove the peg from the source position (sRow, sCol)
 		myBoard[tRow][tCol] = true;				// Add a peg at the target position (tRow, tCol)
 		myBoard[(sRow + tRow)/2][(sCol + tCol)/2] = false;	// Remove the peg in between that was jumped over
 		return this;
 	}
 	
 	public boolean isGoal() {
 		boolean topOne = false;
 		boolean restZero = false;
 		topOne = topOne | myBoard[0][0];
 		for (int i=1; i<depth; i++)
 			for (int j=0; j<i+1; j++)
 				restZero = restZero | myBoard[i][j];
 		return topOne == true && restZero == false;
 	}
 	
 	public void printBoard() {
 		for (int i=0; i<depth; i++) {
 			for (int j=0; j<i+1; j++) {
 				if (myBoard[i][j])
 					System.out.print("X ");
 				else
 					System.out.print("O ");
 			}
 			System.out.println();
 		}
 	}
 	
 	public void printValidMoves() {
 		findValidForwardMoves();
 		System.out.println("Valid moves from the above configuration are:");
 		for (int i=0; i<validForwardMoves.size(); i++) {
 			validForwardMoves.elementAt(i).printBoard();
 			System.out.println("and");
 		}
 	}
 }
