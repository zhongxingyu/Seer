 package edu.berkeley.gamesman.game.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.game.util.Bullet;
 
 public class AlignmentState implements State {
 	Boolean[] guns = new Boolean[]{false, false, false, false};
 	//private ArrayList<Bullet> myBullets = new ArrayList<Bullet>();
 	public char[][] board; // chars are 'X', 'O' and ' ' (X plays first) should be char[] to accomodate Dead_squares and no corners
 	public char lastMove;
 	public int xDead;
 	public int oDead;
 	private Bullet[] bullets;
 
 	public AlignmentState(char[][] board, int xDead, int oDead, char lastMove) {
 		this.board = board;
 		this.xDead = xDead;
 		this.oDead = oDead;
 		this.lastMove = lastMove;
 		bullets =  new Bullet[board.length * board[0].length * 4];
 		for(int b = 0; b < bullets.length; b++) {
 			bullets[b] = new Bullet(0,0,0,'O');
 		}
 	}
 
 	public AlignmentState(AlignmentState pos) {
 		this.board = pos.board;
 		this.xDead = pos.xDead;
 		this.oDead = pos.oDead;
 		this.lastMove = pos.lastMove;
 		bullets =  new Bullet[board.length * board[0].length * 4];
 	}
 
 	public void set(State s) {
 		AlignmentState as = (AlignmentState) s;
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[row].length; col++) {
 				board[row][col] = as.board[row][col];
 			}
 		}
 		xDead = as.xDead;
 		oDead = as.oDead;
 		lastMove = as.lastMove;
 	}
 
 	public void set(char[][] board, int xDead, int oDead, char lastMove) {
 		this.board = board;
 		this.xDead = xDead;
 		this.oDead = oDead;
 		this.lastMove = lastMove;
 	}
 
 	public char get(int row, int col) {
 		return board[row][col];
 	}
 
 	public Boolean full() {
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[0].length; col++) {
 				if (board[row][col] == ' ') {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public void put(int row, int col, char piece) {
 		board[row][col] = piece;
 	}
 
 
 	public void setLastMove(char player) {
 		this.lastMove = player;
 	}
 
 	/** Returns true if the piece at (x0,y0) can be moved to (x1,y1)) */
 	public Boolean legalMove(int row0, int col0, int row1, int col1) {
 		return adjacent(row0, col0, row1, col1) && (board[row1][col1] == ' ');
 	}
 
 
 
 	//=======================================================================================
 
 
 	// Will never be called on a square without a non-emptcol, non-valid piece. REturns arracol of bools [N W E S]
 	void checkGun(int row, int col) { // Catch ArrayIndexOutOfBound 
 
 		char base = board[row][col];
 		char NW = ' ';
 		char NE = ' ';
 		char SW = ' ';
 		char SE =  ' ';
 
 		if (row-1 >= 0) {
 			if (col-1 >= 0) {
 				NW = board[row-1][col-1];
 			}
 			if (col+1 < board[0].length) {
 				NE = board[row-1][col+1];
 			}
 		}
 		if (row+1 < board.length) {
 			if (col-1 >= 0) {
 				SW = board[row+1][col-1];
 			}
 			if (col+1 < board[0].length) {
 				SE = board[row+1][col+1];
 			}
 		}
 
 
 		if (SE == base && SW == base){ guns[0] = true;}
 		if (NE == base && SE == base){ guns[1] = true;}
 		if (NW == base && SW == base){ guns[2] = true;}
 		if (NE == base && NW == base){ guns[3] = true;}
 
 	}
 
 	/**
 	 * Populates myBullets with all bullets of the current 
 	 * player's color.  Does not alter the board in any way.
 	 * 
 	 * return
 	 */
 	int makeBullets() {
 		int numBullets = 0;
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col< board[0].length; col++) {
 				if (board[row][col] == opposite(lastMove)) {
 					checkGun(row, col);
 					for (int dir  = 0; dir < 4; dir++) {
 						if (guns[dir]) {
 							bullets[numBullets] = bullets[numBullets].set(row, col, dir, opposite(lastMove));
 							numBullets++;
 							//myBullets.add(new Bullet(row,col,dir,opposite(lastMove)));
 						}
 					}
 					for (int i = 0; i < 4; i++) { //reset guns
 						guns[i] = false;
 					}
 
 				}
 
 			}
 		}
 		return numBullets;
 
 	}
 
 
 	static char opposite(char player) {
 		switch(player) {
 		case('X'):
 			return 'O';
 		case('O'):
 			return 'X';
 		default:
 			return player;
 		}
 	}
 
 	/**
 	 * moves the piece at (x0,y0) to (x1, y1)
 	 */
 	Boolean movePiece (int row0, int col0, int row1, int col1, AlignmentState pos) {
 		if (pos.legalMove(row0,col0,row1,col1)) {
 			pos.board[row1][col1] = pos.board[row0][col0];
 			pos.board[row0][col0] = ' ';
 			return true;
 		}
 		return false;
 	}
 
 
 	/** true if the square (x0,y0) is one of 8 points adjacent to (x1, y1) */
 	Boolean adjacent (int x0, int y0, int x1, int y1) {
 		return (Math.abs(y1-y0)<=1 && Math.abs(x1-x0)<=1 && !(x1==x0 && y1==y0));
 	}
 
 	/** Only the guns of the player whose turn it currently is 
 	 * fire at the end of a given turn.  Finds and fires guns, returning
 	 * the number of enemies removed from the board. Destructive*/
 	public void fireGuns (int piecesToWin) {
 		int numBullets = makeBullets();
 		char whoseTurn = opposite(lastMove);
 		int deathCount = 0;
 		while (numBullets > 0) {
 			Bullet b = bullets[numBullets - 1];
 			int row = b.row(); int col = b.col(); int drow = b.drow(); int dcol = b.dcol();
 			Boolean stillGoing = true;
 			while(stillGoing) {
 				row += drow; col += dcol;
 
 				if (row >= 0 && col >= 0 && row < board.length && col < board[0].length) {
 
 
 					if (board[row][col] == whoseTurn){// Catch ArrayException
 						stillGoing = false;
 						continue;
 					}
 					else {
 						if (board[row][col] == whoseTurn) {
 							stillGoing = false;
 						}
 						else if (board[row][col] == opposite(whoseTurn)) {
 							board[row][col] = ' ';
 							deathCount++;
 						}
 
 					}
 				}
 				else {
 					stillGoing = false;
 				}
 			}
 		}
 		switch(whoseTurn) {
 		case('X'):
 			oDead = Math.min(oDead + deathCount,piecesToWin );
 		break;
 		case('O'):
 			xDead = Math.min(xDead + deathCount,piecesToWin );
 		break;
 		}
 		//myBullets = new ArrayList<Bullet>();
 	}
 
 
 
 
 	//=======================================================================================
 
 }
