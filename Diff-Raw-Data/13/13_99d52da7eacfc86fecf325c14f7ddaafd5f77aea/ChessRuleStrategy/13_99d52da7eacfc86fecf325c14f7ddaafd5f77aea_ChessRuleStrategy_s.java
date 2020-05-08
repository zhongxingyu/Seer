 package se.lannstrom.chesssensei.model.rules;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.Castle;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 import se.lannstrom.chesssensei.model.Board.ChessPiece;
 import se.lannstrom.chesssensei.model.ChessMove.PromotionPiece;
 
 /**
  * Knows about how the pieces move in chess
  */
 public class ChessRuleStrategy {
 	private HashMap<ChessPiece, ChessPieceStrategy> pieceStrategies =
 			new HashMap<ChessPiece, ChessPieceStrategy>();
 
 	public ChessRuleStrategy() {
 		setupStandard();
 	}
 
 	/**
 	 * Setup standard rules of chess
 	 */
 	private void setupStandard() {
 		/* Rook */
 		register(ChessPiece.W_ROOK, new RookStrategy());
 		register(ChessPiece.B_ROOK, new RookStrategy());
 
 		/* Bishop */
 		register(ChessPiece.W_BISHOP, new BishopStrategy());
 		register(ChessPiece.B_BISHOP, new BishopStrategy());
 
 		/* Knight */
 		register(ChessPiece.W_KNIGHT, new KnightStrategy());
 		register(ChessPiece.B_KNIGHT, new KnightStrategy());
 
 		/* Queen */
 		register(ChessPiece.W_QUEEN, new QueenStrategy());
 		register(ChessPiece.B_QUEEN, new QueenStrategy());
 
 		/* King */
 		register(ChessPiece.W_KING, new KingStrategy());
 		register(ChessPiece.B_KING, new KingStrategy());
 
 		/* Pawn */
 		register(ChessPiece.W_PAWN, new PawnStrategy());
 		register(ChessPiece.B_PAWN, new PawnStrategy());
 	}
 
 	/**
 	 * Get a list of valid moves for the chesspiece at position from with color c
 	 * if from is null get valid castling
 	 *
 	 * @param b
 	 * @param from
 	 * @param c
 	 * @return
 	 */
 	public List<ChessMove> getValidMoves(Board b, BoardPosition from, ChessColor c) {
 		List<ChessMove> validMoves = new ArrayList<ChessMove>();
 		ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
 
 		/* Is it a king at from and it is the starting position */
 		BoardPosition kingPos = Board.getStartingKingPos(c);
 		boolean kingFrom = kingPos.equals(from) && b.isKingAt(from, c);

 		if (from == null || kingFrom) {
 			addAvailableCastling(moves, c, b);
 		}
 
 		/* Call getMove to get moves available from boardposition from */
 		if (from != null) {
 			moves.addAll(getMoves(b, from, c));
 		}
 
 		/* Check if added moves are legal */
 		for (ChessMove m : moves) {
 			if (isValidMove(b, m)) {
 				validMoves.add(m);
 			}
 		}
 
 		return validMoves;
 	}
 
 	private void addAvailableCastling(ArrayList<ChessMove> validMoves,
 									  ChessColor c, Board b) {
 		List<Castle> available = b.getAvailableCastle();
 		if (c == ChessColor.WHITE) {
 			if (available.contains(Castle.W_KINGSIDE)) {
 				validMoves.add(new ChessMove(Castle.W_KINGSIDE));
			} else if (available.contains(Castle.W_QUEENSIDE)) {
 				validMoves.add(new ChessMove(Castle.W_QUEENSIDE));
 			}
 		} else {
 			if (available.contains(Castle.B_KINGSIDE)) {
 				validMoves.add(new ChessMove(Castle.B_KINGSIDE));
			} else if (available.contains(Castle.B_QUEENSIDE)) {
 				validMoves.add(new ChessMove(Castle.B_QUEENSIDE));
 			}
 		}
 	}
 
 	/**
 	 * Make a move. Returns true if move was made or false if the move was illegal.
 	 * @param b
 	 * @param move
 	 * @return
 	 */
 	public boolean doMove(Board b, ChessMove move) {
 		List<ChessMove> validMoves = getValidMoves(b, move.getFrom(),
 													  move.getColor());
 		ChessMove realMove = findValidMove(move, validMoves);
 		if (realMove != null) {
 			executeMove(b, realMove);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Linear search after a valid move with the same start position such as move.
 	 *
 	 * This is needed as we don't set the enPassant flag from the GUI part
 	 *
 	 * @param move
 	 * @param validMoves
 	 * @return
 	 */
 	private ChessMove findValidMove(ChessMove move, List<ChessMove> validMoves) {
 		for (int i = 0; i < validMoves.size(); i++) {
 			ChessMove cm = validMoves.get(i);
 			if (cm.isEqualFromAndTo(move)) {
 				cm.setPromotion(move.getPromotion());
 				return cm;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Perform a move without controlling if it is legal
 	 *
 	 * @param b
 	 * @param move
 	 */
 	private void executeMove(Board b, ChessMove move) {
 		/* TODO: Update all status in board */
 
 		if (move.isCastle()) {
 			executeCastle(b, move);
 		} else {
 			executeSingleMove(b, move);
 		}
 
 		b.toggleActive();
 	}
 
 	private void executeSingleMove(Board b, ChessMove move) {
 		if (move.isEnPassant()) {
 			executeEnPassantCapture(b, move);
 		} else {
 			executeOrdinaryMove(b, move);
 		}
 
 		if (isPawnPromotionMove(move, b)) {
 			promotePawn(move, b);
 		}
 	}
 
 	private void executeEnPassantCapture(Board b, ChessMove move) {
 		BoardPosition from = move.getFrom();
 		BoardPosition to = move.getTo();
 		int targetX = to.getX();
 		int targetY = to.getY() + (-1) * getOrient(move.getColor());
 
 		b.setEmptyAt(targetX, targetY);
 
 		ChessPiece cp = b.getPieceAt(from);
 		b.setPieceAt(to, cp);
 		b.setEmptyAt(from);
 
 		/* Reset half-move count */
 		b.setHalfMoveCount(0);
 	}
 
 	private void executeOrdinaryMove(Board b, ChessMove move) {
 		ChessPiece cp = b.getPieceAt(move.getFrom());
 		if (cp != null && cp.isPawn()) {
 			updateEnPassantTarget(b, move);
 		}
 
 		b.setPieceAt(move.getTo(), cp);
 		b.setEmptyAt(move.getFrom());
 	}
 
 	private void executeCastle(Board b, ChessMove move) {
 		Castle castle = move.getCastling();
 		if (castle == Castle.B_KINGSIDE ||
 			castle == Castle.W_KINGSIDE) {
 			executeKingsideCastling(b, move);
 		} else if (castle == Castle.B_QUEENSIDE ||
 				   castle == Castle.W_QUEENSIDE) {
 			executeQueensideCastling(b, move);
 		}
 	}
 
 	private int getOrient(ChessColor c) {
 		if (c == ChessColor.WHITE) {
 			return -1;
 		} else {
 			return 1;
 		}
 	}
 
 	private void executeKingsideCastling(Board b, ChessMove move) {
 		ChessColor c = move.getColor();
 		BoardPosition kingFrom;
 		BoardPosition rookFrom;
 		BoardPosition kingTo;
 		BoardPosition rookTo;
 
 		if (c == ChessColor.WHITE) {
 			kingFrom = new BoardPosition("E1");
 			rookFrom = new BoardPosition("H1");
 			kingTo = new BoardPosition("G1");
 			rookTo = new BoardPosition("F1");
 			b.removeAvailableCastle(Castle.W_KINGSIDE);
 		} else {
 			kingFrom = new BoardPosition("E8");
 			rookFrom = new BoardPosition("H8");
 			kingTo = new BoardPosition("G8");
 			rookTo = new BoardPosition("F8");
 			b.removeAvailableCastle(Castle.B_KINGSIDE);
 		}
 
 		b.movePiece(kingFrom, kingTo);
 		b.movePiece(rookFrom, rookTo);
 	}
 
 	private void executeQueensideCastling(Board b, ChessMove move) {
 		ChessColor c = move.getColor();
 		BoardPosition kingFrom;
 		BoardPosition rookFrom;
 		BoardPosition kingTo;
 		BoardPosition rookTo;
 
 		if (c == ChessColor.WHITE) {
 			kingFrom = new BoardPosition("E1");
 			rookFrom = new BoardPosition("A1");
 			kingTo = new BoardPosition("C1");
 			rookTo = new BoardPosition("D1");
 			b.removeAvailableCastle(Castle.W_QUEENSIDE);
 		} else {
 			kingFrom = new BoardPosition("E8");
 			rookFrom = new BoardPosition("A8");
 			kingTo = new BoardPosition("C8");
 			rookTo = new BoardPosition("D8");
 			b.removeAvailableCastle(Castle.B_QUEENSIDE);
 		}
 
 		b.movePiece(kingFrom, kingTo);
 		b.movePiece(rookFrom, rookTo);
 	}
 
 	private void updateEnPassantTarget(Board b, ChessMove move) {
 		BoardPosition from = move.getFrom();
 		BoardPosition to = move.getTo();
 		ChessColor color = move.getColor();
 
 		boolean isDoubleStep = Math.abs(to.getY() - from.getY()) == 2;
 		if (isDoubleStep) {
 			int dir = 0;
 			if (color == ChessColor.WHITE) {
 				dir = -1;
 			} else {
 				dir = 1;
 			}
 			BoardPosition singleStep = new BoardPosition(from.getX(), from.getY() + dir);
 			b.setEnPassantTarget(singleStep);
 		}
 	}
 
 	/**
 	 * Check if a move returned from getValid moves is valid
 	 *
 	 * @param b
 	 * @param move
 	 * @return
 	 */
 	private boolean isValidMove(Board b, ChessMove move) {
 		if (b.getActive() == move.getColor()) {
 			if (move.isCastle()) {
 				return isCanCastle(b, move);
 			} else {
 				return !isInCheckAfterMove(b, move);
 			}
 		} else {
 			throw new IllegalArgumentException("ChessMove is not valid. Wrong color is active." +
 											   "In Board object active color is: " + b.getActive() +
 											   " and in ChessMove object: " + move.getColor());
 		}
 	}
 
 	/**
 	 * Check if it is possible to castle.
 	 *
 	 * We need to check that
 	 * 		1) Squares in between must be empty
 	 * 		2) King is not in check
 	 * 		3) King doesn't pass over any square that is in check
 	 * 		4) King doesn't end up in check
 	 *
 	 * @param b
 	 * @param move
 	 * @return
 	 */
 	private boolean isCanCastle(Board b, ChessMove move) {
 		List<ChessMove> moves = getAllMoves(b, ChessColor.getOpponent(move.getColor()));
 
 		/* King rank */
 		String rank = null;
 		if (move.getColor() == ChessColor.WHITE) {
 			rank = "1";
 		} else {
 			rank = "8";
 		}
 
 		/* Check condition 1 */
 		if (!isBetweenSquaresEmtpy(b, rank, move)) {
 			return false;
 		}
 
 		/* Is the forbidden squares in check? */
 		/* Condition 2-4 */
 		BoardPosition[] forbidden = new BoardPosition[3];
 		forbidden[0] = new BoardPosition("E" + rank);
 		if (Castle.isKingSide(move.getCastling())) {
 			forbidden[1] = new BoardPosition("F" + rank);
 			forbidden[2] = new BoardPosition("G" + rank);
 		} else {
 			forbidden[1] = new BoardPosition("D" + rank);
 			forbidden[2] = new BoardPosition("C" + rank);
 		}
 
 		for (ChessMove m : moves) {
 			BoardPosition to = m.getTo();
 			/* If the opponent can move to a forbidden square */
 			if (Arrays.asList(forbidden).contains(to))
 				return false;
 		}
 
 		/* Condition 1-4 holds */
 		return true;
 	}
 
 	private boolean isBetweenSquaresEmtpy(Board b, String rank, ChessMove move) {
 		boolean queenEmpty = b.isEmptyAt("B" + rank) &&
 							 b.isEmptyAt("C" + rank) &&
							 b.isEmptyAt("E" + rank);
 
 		boolean kingEmpty =  b.isEmptyAt("F" + rank) &&
 				 			 b.isEmptyAt("G" + rank);
 
 		if (Castle.isKingSide(move.getCastling())) {
 			return kingEmpty;
 		} else {
 			return queenEmpty;
 		}
 	}
 
 	private boolean isInCheckAfterMove(Board b, ChessMove move) {
 		/* Check if activeColor is in check after move */
 		Board copy = new Board(b);
 		executeMove(copy, move);
 		List<ChessMove> opponentMoves =
 				getAllMoves(copy, ChessColor.getOpponent(move.getColor()));
 
 		if (containsKingCapture(copy, move.getColor(), opponentMoves)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean containsKingCapture(Board b,
 										ChessColor c,
 										List<ChessMove> opponentMoves) {
 
 		BoardPosition kingPos = b.findKing(c);
 		if (kingPos != null) {
 			for (ChessMove move : opponentMoves) {
 				if (move.getTo().equals(kingPos)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private List<ChessMove> getMoves(Board b,
 								     BoardPosition from,
 								     ChessColor c) {
 
 		ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
 		ChessPiece cp = b.getPieceAt(from);
 		if (cp != null && cp.isColor(c)) {
 			ChessPieceStrategy cps = pieceStrategies.get(cp);
 			moves.addAll(cps.getValid(b, from, c));
 		}
 		return moves;
 	}
 
 	private List<ChessMove> getAllMoves(Board b, ChessColor c) {
 		ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
 
 		for (int i = 0; i < b.getSize(); i++) {
 			for (int j = 0; j < b.getSize(); j++) {
 				BoardPosition from = new BoardPosition(i, j);
 				ChessPiece cp = b.getPieceAt(from);
 				if (cp != null && cp.isColor(c)) {
 					moves.addAll(getMoves(b, from, c));
 				}
 			}
 		}
 		return moves;
 	}
 
 	private boolean isPawnPromotionMove(ChessMove m, Board b) {
 		/* NB: Move has already been executed at this point */
 		BoardPosition to = m.getTo();
 		ChessPiece cp = b.getPieceAt(to);
 		if (cp == ChessPiece.W_PAWN) {
 			/* Promotion only allowed if we move to the first rank */
 			return to.getY() == 0;
 		} else if (cp == ChessPiece.B_PAWN) {
 			/* Promotion only allowed if we move to the first rank */
 			return to.getY() == (b.getSize() - 1);
 		} else {
 			return false;
 		}
 	}
 
 	private void promotePawn(ChessMove m, Board b) {
 		BoardPosition to = m.getTo();
 		PromotionPiece pp = m.getPromotion();
 		if (pp != null) {
 			ChessPiece cp = pp.getChessPiece(m);
 			b.setPieceAt(to, cp);
 		}
 	}
 
 	private void register(ChessPiece cp, ChessPieceStrategy s) {
 		pieceStrategies.put(cp, s);
 	}
 
 }
