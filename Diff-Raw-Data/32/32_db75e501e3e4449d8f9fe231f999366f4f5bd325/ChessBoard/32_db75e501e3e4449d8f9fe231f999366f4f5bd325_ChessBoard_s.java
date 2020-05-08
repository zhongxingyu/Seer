 package se.chalmers.chessfeud.model;
 
 import se.chalmers.chessfeud.constants.C;
 import se.chalmers.chessfeud.model.pieces.NoPiece;
 import se.chalmers.chessfeud.model.pieces.Piece;
 import se.chalmers.chessfeud.model.utils.Position;
 
 /**
  * A class for representing a chessboard.
  * 
  * @author Henrik Alburg
  * 
  *         Copyright (c) 2012 Henrik Alburg
  * 
  */
 public class ChessBoard {
 	private Piece[][] board;
 
 	/**
 	 * Creates a board with the starting pieces at the right places.
 	 */
 	public ChessBoard() {
 		board = new Piece[C.BOARD_LENGTH][C.BOARD_LENGTH];
 		for (int x = 0; x < board.length; x++) {
 			for (int y = 0; y < board[x].length; y++) {
 				board[x][y] = Rules.startBoard(x, y);
 			}
 		}
 	}
 
 	/**
 	 * Creates a clone of board given with the piece from oldPos moved to
 	 * newPos. This is a good way to test new scenarios which may happen if a
 	 * piece is moved like this.
 	 * 
 	 * @param cb
 	 *            The old board given
 	 * @param oldPos
 	 *            The position of the piece that should be moved
 	 * @param newPos
 	 *            Where the piece will be moved
 	 */
 	public ChessBoard(ChessBoard cb, Position oldPos, Position newPos) {
 		board = new Piece[C.BOARD_LENGTH][C.BOARD_LENGTH];
 		for (int x = 0; x < board.length; x++) {
 			for (int y = 0; y < board[x].length; y++) {
 				board[x][y] = cb.getPieceAt(x, y);
 			}
 		}
 		board[newPos.getX()][newPos.getY()] = board[oldPos.getX()][oldPos
 				.getY()];
 		board[oldPos.getX()][oldPos.getY()] = new NoPiece();
 	}
 
 	/**
 	 * Creates a chess board from the given string. The string should be on the
 	 * following format: Every letter symbolises a position and the order of
 	 * chars is the pieces starting position. The char's int is the position
 	 * where the top left tower stands on 0 and the black pawns from 8-15 and
 	 * the white pawns on 48-55. This is the string for the starting board:
 	 * <------------------------------------>
 	 * 
 	 * @param s
 	 *            The string which is gotten by using exportString()
 	 */
 	public ChessBoard(String s) {
 		String pieces[] = s.split(",");
 		PieceFactory[] pf = { new PieceFactory(C.TEAM_WHITE),
 				new PieceFactory(C.TEAM_BLACK) };
 		board = new Piece[C.BOARD_LENGTH][C.BOARD_LENGTH];
 		for (int x = 0; x < board.length; x++) {
 			for (int y = 0; y < board[x].length; y++) {
 				int team = Integer.parseInt(pieces[C.BOARD_LENGTH * x + y]) % 2;
 				int id = Integer.parseInt(pieces[C.BOARD_LENGTH * x + y])
 						- team;
 				board[x][y] = pf[team].createPiece(id);
 			}
 		}
 	}
 
 	/**
 	 * Returns a string representing a board. The String holds the position of
 	 * every Piece where a pos is between 0-63 and 65 is removed.
 	 * 
 	 * @return The String able to create a new ChessBoard with the String
 	 *         constructor.
 	 */
 	public String exportBoard() {
 		StringBuilder export = new StringBuilder();
 		for (int x = 0; x < board.length; x++) {
 			for (int y = 0; y < board[x].length; y++) {
 				int id = getPieceAt(x, y).getId() + getPieceAt(x, y).getTeam();
 				export.append("," + id);
 			}
 		}
 		export.deleteCharAt(0);
 		return export.toString();
 	}
 
 	/**
	 * Returns the piece at the given position
 	 * 
 	 * @param p
 	 *            The given position 0 <= x, y < 8
 	 * @return A piece object, which is at the given position
 	 */
 	public Piece getPieceAt(Position p) {
 		return getPieceAt(p.getX(), p.getY());
 	}
 
 	/**
	 * Returns the piece at the given position
 	 * 
 	 * @param x
 	 *            The positions x value, 0 <= x < 8
 	 * @param y
 	 *            The positions y value, 0 <= y < 8
 	 * @return A piece object from the position
 	 */
 	public Piece getPieceAt(int x, int y) {
 		return board[x][y];
 	}
 
 	/**
	 * Returns the width of the chessboard
 	 * @return An int representing the width
 	 */
 	public int getWidth() {
 		return board.length;
 	}
 
 	/**
	 * Returns the height of the chessboard
 	 * @return An int representing the height
 	 */
 	public int getHeight() {
 		return board[0].length;
 	}
 
 	/**
	 * Moves a piece from oldPos to newPos
 	 * 
 	 * @param oldPos
	 *            The current position of the piece
 	 * @param newPos
	 *            The new position of the piece
	 * @return The former piece at newPos (got taken)
 	 */
 	public Piece movePiece(Position oldPos, Position newPos) {
 		Piece piece = getPieceAt(newPos);
 		board[newPos.getX()][newPos.getY()] = getPieceAt(oldPos);
 		board[oldPos.getX()][oldPos.getY()] = new NoPiece();
 		return piece;
 	}
 
 	/**
	 * Check whether a position is empty or not
 	 * 
 	 * @param pos
	 *            The pos to be checked
	 * @return true if their is no piece at the given pos
 	 */
 	public boolean isEmpty(Position pos) {
 		return getPieceAt(pos) instanceof NoPiece;
 	}
 
 }
