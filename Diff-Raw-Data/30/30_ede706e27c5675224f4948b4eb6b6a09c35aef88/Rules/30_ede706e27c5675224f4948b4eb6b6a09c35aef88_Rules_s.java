 package se.chalmers.chessfeud.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import se.chalmers.chessfeud.model.pieces.Bishop;
 import se.chalmers.chessfeud.model.pieces.King;
 import se.chalmers.chessfeud.model.pieces.Knight;
 import se.chalmers.chessfeud.model.pieces.NoPiece;
 import se.chalmers.chessfeud.model.pieces.Pawn;
 import se.chalmers.chessfeud.model.pieces.Piece;
 import se.chalmers.chessfeud.model.pieces.Queen;
 import se.chalmers.chessfeud.model.pieces.Rook;
 import se.chalmers.chessfeud.model.utils.Position;
 import se.chalmers.chessfeud.utils.C;
 
 /**
  * A class for managing the Rules in a chessgame.
  * 
  * @author Henrik Alburg, Arvid Karlsson
  * 
  *         Copyright (c) 2012 Henrik Alburg, Arvid Karlsson
  * 
  */
 public final class Rules {
 
 	private static final int[] HORSE_X = C.KNIGHT_X;
 	private static final int[] HORSE_Y = C.KNIGHT_Y;
 
 	/* The start board for a regular chess game */
 	private static final Piece[][] START_BOARD = {
 			{ new Rook(C.TEAM_BLACK), new Knight(C.TEAM_BLACK),
 					new Bishop(C.TEAM_BLACK), new Queen(C.TEAM_BLACK),
 					new King(C.TEAM_BLACK), new Bishop(C.TEAM_BLACK),
 					new Knight(C.TEAM_BLACK), new Rook(C.TEAM_BLACK) },
 			{ new Pawn(C.TEAM_BLACK), new Pawn(C.TEAM_BLACK),
 					new Pawn(C.TEAM_BLACK), new Pawn(C.TEAM_BLACK),
 					new Pawn(C.TEAM_BLACK), new Pawn(C.TEAM_BLACK),
 					new Pawn(C.TEAM_BLACK), new Pawn(C.TEAM_BLACK) },
 			{ new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece(),
 					new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece() },
 			{ new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece(),
 					new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece() },
 			{ new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece(),
 					new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece() },
 			{ new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece(),
 					new NoPiece(), new NoPiece(), new NoPiece(), new NoPiece() },
 			{ new Pawn(C.TEAM_WHITE), new Pawn(C.TEAM_WHITE),
 					new Pawn(C.TEAM_WHITE), new Pawn(C.TEAM_WHITE),
 					new Pawn(C.TEAM_WHITE), new Pawn(C.TEAM_WHITE),
 					new Pawn(C.TEAM_WHITE), new Pawn(C.TEAM_WHITE) },
 			{ new Rook(C.TEAM_WHITE), new Knight(C.TEAM_WHITE),
 					new Bishop(C.TEAM_WHITE), new Queen(C.TEAM_WHITE),
 					new King(C.TEAM_WHITE), new Bishop(C.TEAM_WHITE),
 					new Knight(C.TEAM_WHITE), new Rook(C.TEAM_WHITE) } };
 
 	/*
 	 * A private constructor since you don't have to create an instance of this
 	 * class.
 	 */
 	private Rules() {
 
 	}
 
 	/**
 	 * Returns the piece at the given position when a game of chess is
 	 * initiated. The black Rook is on (0,0) The white king is on (4,7)
 	 * 
 	 * @param x
 	 *            The x position of the piece.
 	 * @param y
 	 *            The y position of the piece
 	 * @return a new Piece-object, at the start position.
 	 */
 	public static Piece startBoard(int x, int y) {
 		return START_BOARD[y][x];
 	}
 
 	/**
 	 * Returns true if the current board is in a check state.
 	 * 
 	 * @param cb
 	 *            The board to be checked
 	 * @param team
 	 *            The currentPlayer (C.TEAM_WHITE)
 	 * @return true if it is check
 	 */
 	public static boolean isCheck(ChessBoard cb, int team) {
 		for (int x = 0; x < cb.getWidth(); x++) {
 			for (int y = 0; y < cb.getHeight(); y++) {
 				Piece kingPiece = cb.getPieceAt(x, y);
 				if (kingPiece.getId() == C.PIECE_KING
 						&& kingPiece.getTeam() == team) {
 					return isCheck(cb, team, x, y);
 				}
 			}
 		}
 		return false;
 	}
 
 	/* Checks if the given king is check */
 	private static boolean isCheck(ChessBoard cb, int team, int kingX, int kingY) {
 		for (int dx = -1; dx <= 1; dx++) {
 			for (int dy = -1; dy <= 1; dy++) {
 				if (inBounds(kingX + dx, kingY + dy) && !(dx == 0 && dy == 0)) {
					checkDirection(cb, team, kingX, kingY, dx, dy);
 				}
 			}
 		}
 		if (doHorsesCheck(cb, team, kingX, kingY)) {
 			return true;
 		}
 		if (doPawnsCheck(cb, team, kingX, kingY)) {
 			return true;
 		}
 		return false;
 	}
 
 	/* Checks if it is check in different directions from the king */
 	private static boolean checkDirection(ChessBoard cb, int team, int kingX,
 			int kingY, int dx, int dy) {
 		int dir = 1;
 		while (inBounds(kingX + dx * dir, kingY + dy * dir)
 				&& cb.isEmpty(new Position(kingX + dx * dir, kingY + dy * dir))) {
 			dir++;
 		}
 		if (inBounds(kingX + dx * dir, kingY + dy * dir)) {
 			Piece pi = cb.getPieceAt(kingX + dx * dir, kingY + dy * dir);
 			if (checkStraight(dx, dy, pi, team)) {
 				return true;
 			}
 			if (checkDiagonally(dx, dy, pi, team)) {
 				return true;
 			}
 			if (checkKingsDistance(dir, pi, team)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/* Checks if a horse is checking the king */
 	private static boolean doHorsesCheck(ChessBoard cb, int team, int kingX,
 			int kingY) {
 		for (int i = 0; i < HORSE_X.length; i++) {
 			int dx = HORSE_X[i];
 			int dy = HORSE_Y[i];
 			if (inBounds(kingX + dx, kingY + dy)
 					&& cb.getPieceAt(kingX + dx, kingY + dy).getId() == C.PIECE_KNIGHT
 					&& cb.getPieceAt(kingX + dx, kingY + dy).getTeam() != team) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/* Check if any pawns checks the king */
 	private static boolean doPawnsCheck(ChessBoard cb, int team, int kingX,
 			int kingY) {
 		int forward = team == C.TEAM_WHITE ? -1 : 1;
 		if (inBounds(kingX + 1, kingY + forward)
 				&& cb.getPieceAt(kingX + 1, kingY + forward).getId() == C.PIECE_PAWN
 				&& cb.getPieceAt(kingX + 1, kingY + forward).getTeam() != team) {
 			return true;
 		}
 		if (inBounds(kingX - 1, kingY + forward)
 				&& cb.getPieceAt(kingX - 1, kingY + forward).getId() == C.PIECE_PAWN
 				&& cb.getPieceAt(kingX - 1, kingY + forward).getTeam() != team) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the game is in a draw state.
 	 * 
 	 * @param cb
 	 *            The board to be checked.
 	 * @param nextTurn
 	 *            Who is to move next time
 	 * @return true if the game is in a draw state.
 	 */
 	public static boolean isDraw(ChessBoard cb, int nextTurn) {
 		if (!isCheck(cb, nextTurn)) {
 			return noPiecesMovable(cb, nextTurn);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the game is over and someone has won.
 	 * 
 	 * @param cb
 	 *            The board to be checked.
 	 * @param nextTurn
 	 *            The player to move next time
 	 * @return true if it is check mate.
 	 */
 	public static boolean isCheckMate(ChessBoard cb, int nextTurn) {
 		if (isCheck(cb, nextTurn)) {
 			return noPiecesMovable(cb, nextTurn);
 		}
 		return false;
 	}
 
 	/* Returns true if all the pieces are stuck for the next turn player */
 	private static boolean noPiecesMovable(ChessBoard cb, int nextTurn) {
 		for (int x = 0; x < cb.getWidth(); x++) {
 			for (int y = 0; y < cb.getHeight(); y++) {
 				if (cb.getPieceAt(x, y).getTeam() == nextTurn) {
 					if(isPossibleToMove(cb, x, y)){
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Returns a list of possible moves for a certain piece. This will return a
 	 * list of position of the possible moves.
 	 * 
 	 * @param cb
 	 *            The current gameboard
 	 * @param selected
 	 *            The selected position of the piece
 	 * @return a list of positions which it can go to.
 	 */
 	public static List<Position> getPossibleMoves(ChessBoard cb,
 			Position selected) {
 		List<Position> pm = new LinkedList<Position>();
 		if (cb.getPieceAt(selected).getId() != C.PIECE_PAWN) {
 			List<List<Position>> tempMoves = cb.getPieceAt(selected)
 					.theoreticalMoves(selected);
 			pm = regularMovement(cb, selected, tempMoves);
 		} else { // ID == PAWN
 			int dy = cb.getPieceAt(selected).getTeam() == C.TEAM_WHITE ? -1 : 1;
 			pm = pawnMovementForward(cb, selected, dy);
 			// Check if there is pieces to take diagonally forward
 			Position tryPos = new Position(selected.getX() - 1, selected.getY()
 					+ dy);
 			if (pawnMovableTo(cb, selected, tryPos)) {
 				pm.add(tryPos);
 			}
 			tryPos = new Position(selected.getX() + 1, selected.getY() + dy);
 			if (pawnMovableTo(cb, selected, tryPos)) {
 				pm.add(tryPos);
 			}
 		}
 		return pm;
 	}
 
 	/* Returns the positions forward a pawn is able to take. */
 	private static List<Position> pawnMovementForward(ChessBoard cb,
 			Position selected, int dy) {
 		Piece piece = cb.getPieceAt(selected);
 		List<Position> moveList = new LinkedList<Position>();
 
 		int startY = piece.getTeam() == C.TEAM_WHITE ? C.STARTING_POSITION_BLACK_PAWN
 				: 1;
 
 		Position tryPos = new Position(selected.getX(), selected.getY() + dy);
 		if (inBounds(tryPos) && cb.isEmpty(tryPos)){
 			ChessBoard tmpBoard = new ChessBoard(cb, selected, tryPos);
 			if( !isCheck(tmpBoard, piece.getTeam())) {
 				moveList.add(tryPos);
 		}
 			tryPos = new Position(selected.getX(), selected.getY() + 2 * dy);
 			if (inBounds(tryPos) && cb.isEmpty(tryPos)
 					&& selected.getY() == startY) {
 				tmpBoard = new ChessBoard(cb, selected, tryPos);
 				if(!isCheck(tmpBoard, piece.getTeam())){
 					moveList.add(tryPos);
 				}
 			}
 		}
 		return moveList;
 	}
 
 	/* Returns true if the pawn is movable to the given position */
 	private static boolean pawnMovableTo(ChessBoard cb, Position pawnPosition,
 			Position moveTo) {
 		int team = cb.getPieceAt(pawnPosition).getTeam();
 
 		if (inBounds(moveTo)) {
 			ChessBoard tmpBoard = new ChessBoard(cb, pawnPosition, moveTo);
 			if (!cb.isEmpty(moveTo) && cb.getPieceAt(moveTo).getTeam() != team
 					&& !isCheck(tmpBoard, team)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/* Returns the moves a piece is able to do considering all rules in chess */
 	private static List<Position> regularMovement(ChessBoard cb,
 			Position piecePos, List<List<Position>> tempMoves) {
 		int team = cb.getPieceAt(piecePos).getTeam();
 		List<Position> moveList = new LinkedList<Position>();
 
 		for (List<Position> l : tempMoves) {
 			boolean canMove = true;
 			for (Position p : l) {
 				ChessBoard tmpBoard = new ChessBoard(cb, piecePos, p);
 				if (canMove && !Rules.isCheck(tmpBoard, team)) {
 					if (cb.isEmpty(p)) {
 						moveList.add(p);
 					} else {
 						if (team != cb.getPieceAt(p).getTeam()) {
 							moveList.add(p);
 						}
 						canMove = false;
 					}
 				} else {
 					if (!cb.isEmpty(p)) {
 						canMove = false;
 					}
 				}
 			}
 		}
 		return moveList;
 	}
 
 	/* Returns true if any piece on the board can moce for the currrent team */
 	private static boolean isPossibleToMove(ChessBoard cb, int x, int y) {
 		return getPossibleMoves(cb, new Position(x, y)).size() > 0;
 	}
 
 	/* Returns true if the position is inbounds */
 	private static boolean inBounds(int x, int y) {
 		return 0 <= x && x < C.BOARD_LENGTH && 0 <= y && y < C.BOARD_LENGTH;
 	}
 
 	/* Returns true if the position is inbounds */
 	private static boolean inBounds(Position p) {
 		return 0 <= p.getX() && p.getX() < C.BOARD_LENGTH && 0 <= p.getY()
 				&& p.getY() < C.BOARD_LENGTH;
 	}
 
 	/* Checks the vertically and horizontally squares */
 	private static boolean checkStraight(int dx, int dy, Piece pi, int team) {
 		return Math.abs(dx * dy) == 0
 				&& (pi.getId() == C.PIECE_QUEEN || pi.getId() == C.PIECE_ROOK)
 				&& pi.getTeam() != team;
 	}
 
 	/* Checks the diagonally squares */
 	private static boolean checkDiagonally(int dx, int dy, Piece pi, int team) {
 		return Math.abs(dx * dy) == 1
 				&& (pi.getId() == C.PIECE_QUEEN || pi.getId() == C.PIECE_BISHOP)
 				&& pi.getTeam() != team;
 	}
 
 	/* Checks so there's at least one square between the kings */
 	private static boolean checkKingsDistance(int dir, Piece pi, int team) {
 		return dir == 1 && pi.getId() == C.PIECE_KING && pi.getTeam() != team;
 	}
 }
