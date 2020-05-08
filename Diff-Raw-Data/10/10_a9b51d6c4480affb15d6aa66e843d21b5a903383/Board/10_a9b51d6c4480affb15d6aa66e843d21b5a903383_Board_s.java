 package com.diycomputerscience.minesweeper;
 
 public class Board {
 
 	public static final int MAX_ROWS = 6;
 	public static final int MAX_COLS = 6;
 	
 	private Square squares[][];
 	
 	public Board() {
		this.squares = new Square[MAX_ROWS][MAX_COLS];
		for(int row=0; row<MAX_ROWS; row++) {
			for(int col=0; col<MAX_COLS; col++) {
				squares[row][col] = new Square();
			}
		}
 	}
 	
 	public Square[][] getSquares() {
		return this.squares;		
 	}
 
 }
