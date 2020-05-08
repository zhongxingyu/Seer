 package com.veganjay.tictactoe;
 
 public class TicTacToeBoard {
 	enum Piece { EMPTY, X, O };
 
 	// Constants
 	public static final int SIZE = 3;
 
 	// Board
 	Piece board[][];
 	
 	// Inner class for position
 	protected class Position {
 		private int row;
 		private int col;
 		
 		public Position(int row, int col) {
 			this.row = row;
 			this.col = col;
 		}
 
 		public Position(int spaceNum) {
 			this.row = (spaceNum-1) / 3;
 			this.col = (spaceNum-1) % 3;
 		}
 
 		public int getRow() {
 			return row;
 		}
 
 		public void setRow(int row) {
 			this.row = row;
 		}
 
 		public int getCol() {
 			return col;
 		}
 
 		public void setCol(int col) {
 			this.col = col;
 		}		
 	} // End class Position
 
 	/**
 	 * Create an empty Tic Tac Toe board
 	 */
 	public TicTacToeBoard() {
 		this.board = new Piece[SIZE][SIZE];
 		this.initialize();
 	}
 
 	/**
 	 * Create a duplicate board
 	 * @param other tic tac toe board
 	 */
 	public TicTacToeBoard(TicTacToeBoard other) {
 		this.board = new Piece[SIZE][SIZE];
 		for (int i = 0; i < SIZE; i++) {
 			for (int j = 0; j < SIZE; j++) {
 				this.board[i][j] = other.board[i][j];
 			}
 		}
 	}
 	
 	/**
 	 * Set all positions to Empty
 	 */
 	public void initialize() {
 		for (int i = 0; i < SIZE; i++) {
 			for (int j = 0; j < SIZE; j++) {
 				this.board[i][j] = Piece.EMPTY;
 			}
 		}
 	}
 
 	/**
 	 * @param row 0-2
 	 * @param col 0-2
 	 * @return true if the space is occupied
 	 */
 	public boolean isOccupied(int row, int col) {
 		return this.board[row][col] != Piece.EMPTY;
 	}
 	
 	/**
 	 * 
 	 * @param spaceNum 1-9
 	 * @param p a piece: X or O
 	 * @return true if successful, false if already occupied
 	 */
 	public boolean addPiece(int spaceNum, Piece p) {
 		Position pos = new Position(spaceNum);
 		
 		return this.addPiece(pos.getRow(), pos.getCol(), p);
 	}
 	
 	/**
 	 * @param row 0-2
 	 * @param col 0-2
 	 * @param p a piece: X or O
 	 * @return true if successful, false if already occupied
 	 */
 	public boolean addPiece(int row, int col, Piece p) {
 		boolean success = true;
 		
 		if (isOccupied(row, col)) {
 			success = false;
 		} else {
 			board[row][col] = p;
 		}
 		
 		return success;
 	}
 	
 	/**
 	 * Remove a piece from the board
 	 * @param row 0-2
 	 * @param col 0-2
 	 */
 	public void removePiece(int row, int col) {
 		board[row][col] = Piece.EMPTY;
 	}
 
 	/**
 	 * @param p a piece: X or O
 	 * @return true if the piece is a winner
 	 */
 	public boolean isWinner(Piece p) {
 		boolean winner = false;
 
 		// Check Vertical Win
 		for (int col = 0; col < SIZE; col++) {
 			if (board[0][col] == p && board[1][col] == p && board[2][col] == p) {
 				winner = true;
 			}
 		}
 
 		// Check Horizontal Win
 		for (int row = 0; row < SIZE; row++) {
 			if (board[row][0] == p && board[row][1] == p && board[row][2] == p) {
 				winner = true;
 			}
 		}
 
 		// Check Diagonal Win
 		if (board[0][0] == p && board[1][1] == p && board[2][2] == p) {
 			winner = true;			
 		}
 		
 		// Check Diagonal Win
 		if (board[0][2] == p && board[1][1] == p && board[2][0] == p) {
 			winner = true;			
 		}
 				
 		return winner;
 	}
 	
 	public boolean isFull() {
 		boolean full = true;
 		for (int i = 0; i < SIZE; i++) {
 			for (int j = 0; j < SIZE; j++) {
 				if (board[i][j] == Piece.EMPTY) {
 					full = false;
 					break;
 				}
 			}
 		}		
 		return full;
 	}
 	
 	/**
 	 * @param row (0-2)
 	 * @param col (0-2)
 	 * @return the Piece name occupying the position, if Empty the place number
 	 */
 	public String getPieceName(int row, int col) {
 		String s = " ";
 		
 		Piece p = board[row][col];
 		
 		if (p == Piece.X || p == Piece.O) {
 			s = p.toString();
 		} else {
 			s = Integer.toString(getPlaceNum(row, col));
 		}
 		
 		return s;		
 	}
 	
 	/**
 	 * @param row (0-2)
 	 * @param col (0-2)
 	 * @return the Piece occupying the position
 	 */
 	public Piece getPiece(int row, int col) {
 		return board[row][col];
 	}
 
 	/**
 	 * Given (row,col) coordinates, return the place number
 	 * For example:
 	 * (0,0) = 1
 	 * (0,1) = 2
 	 * (2,2) = 9
 	 * 
 	 * @param row 0-2
 	 * @param col 0-2
 	 * @return the place number (1-9)
 	 */
 	public static int getPlaceNum(int row, int col) {
 		int pos = (row * 3 + col) + 1;
 		return pos;
 	}
 
 	/**
 	 * Display the board
 	 */
 	public void printBoard() {
 		
 		System.out.println(" " + getPieceName(0,0) + " | " + getPieceName(0,1) + " | " + getPieceName(0,2));
 		System.out.println("------------");
 		System.out.println(" " + getPieceName(1,0) + " | " + getPieceName(1,1) + " | " + getPieceName(1,2));
 		System.out.println("------------");
 		System.out.println(" " + getPieceName(2,0) + " | " + getPieceName(2,1) + " | " + getPieceName(2,2));
 		
 		System.out.println();
 	}
 }
