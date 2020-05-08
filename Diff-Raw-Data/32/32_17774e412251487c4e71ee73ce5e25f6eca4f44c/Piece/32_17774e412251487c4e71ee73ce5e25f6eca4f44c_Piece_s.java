 package com.chess.genesis;
 
class Piece
 {
 	private Piece()
 	{
 	}
 
 	public static final int WHITE = 1;
 	public static final int BLACK = -1;
 
 	public static final int NONE = -1;
 	public static final int PLACEABLE = -2;
 	public static final int DEAD = -4;
 
 	public static final int PAWN = 1;
 	public static final int KNIGHT = 2;
 	public static final int BISHOP = 3;
 	public static final int ROOK = 4;
 	public static final int QUEEN = 5;
 	public static final int KING = 6;
 
 	public static final int BLACK_KING = -6;
 	public static final int BLACK_QUEEN = -5;
 	public static final int BLACK_ROOK = -4;
 	public static final int BLACK_BISHOP = -3;
 	public static final int BLACK_KNIGHT = -2;
 	public static final int BLACK_PAWN = -1;
 	public static final int EMPTY = 0;
 	public static final int WHITE_PAWN = 1;
 	public static final int WHITE_KNIGHT = 2;
 	public static final int WHITE_BISHOP = 3;
 	public static final int WHITE_ROOK = 4;
 	public static final int WHITE_QUEEN = 5;
 	public static final int WHITE_KING = 6;
 }
