 package edu.selu.android.classygames.games.checkers;
 
 
 import edu.selu.android.classygames.games.GenericBoard;
 
 
 /**
  * Class representing a Checkers board. This board is made up of a bunch of
  * positions. Checkers is 8 by 8, so that's 64 positions.
  */
 public class Board extends GenericBoard
 {
 
 
 	private final static byte LENGTH_HORIZONTAL = 8;
 	private final static byte LENGTH_VERTICAL = 8;
 
 
 
 
 	/**
 	 * Creates a Checkers board object.
 	 */
 	public Board()
 	{
 		super(LENGTH_HORIZONTAL, LENGTH_VERTICAL);
 	}
 
 
 	/**
 	 * Creates a Checkers board object using the given JSON String.
 	 * 
	 * @param boardJSON
 	 * JSON String that represents the board.
 	 */
	public Board(final String boardJSON)
 	{
 		super(LENGTH_HORIZONTAL, LENGTH_VERTICAL);
 
 		
 	}
 
 
 
 
 	@Override
 	protected void makeDefaultBoard()
 	{
 		// player team
 		getPosition(0, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(0, 3).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(0, 5).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(0, 7).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(1, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(1, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(1, 4).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(1, 6).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(2, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(2, 3).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(2, 5).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(2, 7).setPiece(new Piece(Piece.TEAM_PLAYER));
 
 		// opponent team
 		getPosition(0, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(2, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(4, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(6, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(1, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(3, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(5, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(7, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(0, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(2, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(4, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(6, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 	}
 
 
 }
