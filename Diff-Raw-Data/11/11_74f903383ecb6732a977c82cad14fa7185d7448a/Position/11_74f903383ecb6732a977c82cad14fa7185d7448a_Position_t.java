 package mitzi;
 
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import mitzi.IrreversibleMoveStack.MoveInfo;
 
 /**
  * The class implements the position of the figures on a chess board. The board
  * is represented as two 8*8 +1 arrays - one for the sides, one for the pieces.
  * All accesses to a square outside the chessboard are mapped to the 65th entry
  * of the board, which is always null. This map from square to array index is
  * performed by the function <code>squareToArrayIndex(square) </code>, which
  * looks up in the <code>square_to_array_index array</code>. For informations
  * about the <code>int</code> value of a square, see
  * <code>SqaureHelper.java</code>.
  * 
  */
 public class Position implements IPosition {
 
 	/**
 	 * the initial position of the sides
 	 */
 	protected static Side[] initial_side_board = { Side.BLACK, Side.BLACK,
 			Side.BLACK, Side.BLACK, Side.BLACK, Side.BLACK, Side.BLACK,
 			Side.BLACK, Side.BLACK, Side.BLACK, Side.BLACK, Side.BLACK,
 			Side.BLACK, Side.BLACK, Side.BLACK, Side.BLACK, null, null, null,
 			null, null, null, null, null, null, null, null, null, null, null,
 			null, null, null, null, null, null, null, null, null, null, null,
 			null, null, null, null, null, null, null, Side.WHITE, Side.WHITE,
 			Side.WHITE, Side.WHITE, Side.WHITE, Side.WHITE, Side.WHITE,
 			Side.WHITE, Side.WHITE, Side.WHITE, Side.WHITE, Side.WHITE,
 			Side.WHITE, Side.WHITE, Side.WHITE, Side.WHITE, null };
 
 	/**
 	 * the initial position of the pieces
 	 */
 	protected static Piece[] initial_piece_board = { Piece.ROOK, Piece.KNIGHT,
 			Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT,
 			Piece.ROOK, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN,
 			Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, null, null, null,
 			null, null, null, null, null, null, null, null, null, null, null,
 			null, null, null, null, null, null, null, null, null, null, null,
 			null, null, null, null, null, null, null, Piece.PAWN, Piece.PAWN,
 			Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN,
 			Piece.PAWN, Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN,
 			Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK, null };
 
 	/**
 	 * this array maps the integer value of an square to the array index of
 	 * array representation of the board in this class
 	 */
 	protected static int[] square_to_array_index = { 64, 64, 64, 64, 64, 64,
 			64, 64, 64, 64, 64, 56, 48, 40, 32, 24, 16, 8, 0, 64, 64, 57, 49,
 			41, 33, 25, 17, 9, 1, 64, 64, 58, 50, 42, 34, 26, 18, 10, 2, 64,
 			64, 59, 51, 43, 35, 27, 19, 11, 3, 64, 64, 60, 52, 44, 36, 28, 20,
 			12, 4, 64, 64, 61, 53, 45, 37, 29, 21, 13, 5, 64, 64, 62, 54, 46,
 			38, 30, 22, 14, 6, 64, 64, 63, 55, 47, 39, 31, 23, 15, 7, 64, 64,
 			64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
 			64, 64, 64 };
 
 	/**
 	 * the array of Sides, containing the information about the position of the
 	 * sides of the pieces
 	 */
 	private Side[] side_board = new Side[65];
 
 	/**
 	 * the array of Pieces, containing the information about the position of the
 	 * pieces
 	 */
 	private Piece[] piece_board = new Piece[65];
 
 	/**
 	 * squares c1, g1, c8 and g8 in ICCF numeric notation. do not change the
 	 * squares' order or bad things will happen! set to -1 if castling not
 	 * allowed.
 	 */
 	private int[] castling = { -1, -1, -1, -1 };
 
 	/**
 	 * the square of the en_passant_target, -1 if none.
 	 */
 	private int en_passant_target = -1;
 
 	/**
 	 * the side, which has to move
 	 */
 	private Side active_color;
 
 	/**
 	 * contains the information about the value of the position.
 	 */
 	private AnalysisResult analysis_result = null;
 
 	/**
 	 * This is the number of halfmoves since the last pawn advance or capture.
 	 * This is used to determine if a draw can be claimed under the fifty-move
 	 * rule.
 	 */
 	public int half_move_clock;
 
 	// The following class members are used to prevent multiple computations
 	/**
 	 * caching of the possible moves
 	 */
 	private SoftReference<List<IMove>> possible_moves;
 
 	/**
 	 * caching if the current position is check.
 	 */
 	private Boolean is_check;
 
 	/**
 	 * caching if the current position is mate.
 	 */
 	private Boolean is_mate;
 
 	/**
 	 * caching if the current position is stalemate.
 	 */
 	private Boolean is_stale_mate;
 
 	// the following maps takes and Integer, representing the color, type or
 	// PieceValue and returns the set of squares or the number of squares!
 	/**
 	 * this map maps the PieceValue, i.e. 10*side.ordinal + piece.ordinal, to
 	 * the set of squares where the pieces of the side are positioned.
 	 */
 	private Map<Integer, Set<Integer>> occupied_squares_by_color_and_type = new HashMap<Integer, Set<Integer>>();
 
 	/**
 	 * this map maps the side, i.e. side.ordinal, to the set of squares where
 	 * the side has pieces.
 	 */
 	private Map<Side, Set<Integer>> occupied_squares_by_color = new HashMap<Side, Set<Integer>>();
 
 	/**
 	 * this map maps the piece, i.e. piece.ordinal, to the set of squares where
 	 * the pieces are positioned.
 	 */
 	private Map<Piece, Set<Integer>> occupied_squares_by_type = new HashMap<Piece, Set<Integer>>();
 
 	/**
 	 * caching the number of occupied squares for each side of an piece in an
 	 * small array.
 	 */
 	private byte[] num_occupied_squares_by_color_and_type = new byte[16];
 
 	/**
 	 * caching the positions of the kings. (indexed by the ordinal of the side)
 	 */
 	private byte[] king_pos = new byte[2];
 
 	/**
 	 * saves the side, which got captured by the last tinyDoMove
 	 */
 	private Side side_capture;
 
 	/**
 	 * saves the piece, which got captured by the last tinyDoMove
 	 */
 	private Piece piece_capture;
 	
 	Boolean old_check;
 
 	// -----------------------------------------------------------------------------------------
 
 	/**
 	 * Resets and clears the stored class members.
 	 */
 	private void resetCache() {
 		possible_moves = null;
 		is_check = null;
 		is_mate = null;
 		is_stale_mate = null;
 		analysis_result = null;
 		occupied_squares_by_color_and_type.clear();
 		occupied_squares_by_type.clear();
 		occupied_squares_by_color.clear();
 	}
 
 	/**
 	 * computes the index for the internal array representation of an square
 	 * 
 	 * @param square
 	 *            the given square
 	 * @return the index
 	 */
 	private int squareToArrayIndex(int square) {
 		if (square < 0)
 			return 64;
 		return square_to_array_index[square];
 	}
 
 	/**
 	 * computes a copy of the actual board, only the necessary informations are
 	 * copied, plus <code>num_occupied_squares_by_color_and_type</code>
 	 * 
 	 * @return a incomplete copy of the board.
 	 */
 	private Position returnCopy() {
 		Position newBoard = new Position();
 
 		newBoard.active_color = active_color;
 		newBoard.en_passant_target = en_passant_target;
 		System.arraycopy(castling, 0, newBoard.castling, 0, 4);
 
 		System.arraycopy(side_board, 0, newBoard.side_board, 0, 65);
 		System.arraycopy(piece_board, 0, newBoard.piece_board, 0, 65);
 
 		System.arraycopy(num_occupied_squares_by_color_and_type, 0,
 				newBoard.num_occupied_squares_by_color_and_type, 0, 16);
 
 		System.arraycopy(king_pos, 0, newBoard.king_pos, 0, 2);
 		return newBoard;
 	}
 
 	/**
 	 * returns the Side, which occupies a given square
 	 * 
 	 * @return the side of the piece which is on the square
 	 */
 	public Side getSideFromBoard(int square) {
 		int i = squareToArrayIndex(square);
 		return side_board[i];
 	}
 
 	/**
 	 * returns the piece, which occupies a given square
 	 * 
 	 * @return the piece which is on the square
 	 */
 	public Piece getPieceFromBoard(int square) {
 		int i = squareToArrayIndex(square);
 		return piece_board[i];
 	}
 
 	/**
 	 * sets a piece on the board.
 	 * 
 	 * @param square
 	 *            the square, were the piece should be set
 	 * @param side
 	 *            the given side
 	 * @param piece
 	 *            the given piece
 	 */
 	private void setOnBoard(int square, Side side, Piece piece) {
 		int i = squareToArrayIndex(square);
 		side_board[i] = side;
 		piece_board[i] = piece;
 	}
 
 	/**
 	 * returns the opponents side of the actual board
 	 * 
 	 * @return the side of the opponent
 	 */
 	public Side getOpponentsColor() {
 		if (active_color == Side.BLACK)
 			return Side.WHITE;
 		else
 			return Side.BLACK;
 	}
 
 	/**
 	 * returns the eventual result of the position evaluation
 	 */
 	public AnalysisResult getAnalysisResult() {
 		return analysis_result;
 	}
 
 	/**
 	 * updates the result of the board. (only if it more valuable, i.e.
 	 * comparison of the depth)
 	 * 
 	 * @param analysis_result
 	 *            the new analysis result
 	 */
 	public void updateAnalysisResult(AnalysisResult analysis_result) {
 		if (analysis_result == null)
 			throw new NullPointerException();
 
 		if (this.analysis_result == null
 				|| this.analysis_result.compareQualityTo(analysis_result) <= 0) {
 			this.analysis_result = analysis_result;
 		}
 	}
 
 	/**
 	 * checks is a move is a hit. there is no check, that the move is legal!.
 	 * 
 	 * @param move
 	 *            the move to be checked
 	 * @return true, if it is a hit, false otherwise
 	 */
 	public boolean isHit(IMove move) {
 		int dest = move.getToSquare();
 		int src = move.getFromSquare();
 
 		// a hit happens iff the dest is an enemy or its en passant
 		if (getSideFromBoard(dest) == Side.getOppositeSide(active_color)
 				|| (getPieceFromBoard(src) == Piece.PAWN && dest == this
 						.getEnPassant()))
 			return true;
 		return false;
 	}
 
 	@Override
 	public void setToInitial() {
 		System.arraycopy(initial_side_board, 0, side_board, 0, 65);
 		System.arraycopy(initial_piece_board, 0, piece_board, 0, 65);
 
 		castling[0] = 31;
 		castling[1] = 71;
 		castling[2] = 38;
 		castling[3] = 78;
 
 		half_move_clock = 0;
 		en_passant_target = -1;
 		active_color = Side.WHITE;
 
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.KING.ordinal()] = 1;
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.QUEEN.ordinal()] = 1;
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.ROOK.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.BISHOP.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.KNIGHT.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.WHITE.ordinal() * 10
 				+ Piece.PAWN.ordinal()] = 8;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.KING.ordinal()] = 1;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.QUEEN.ordinal()] = 1;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.ROOK.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.BISHOP.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.KNIGHT.ordinal()] = 2;
 		num_occupied_squares_by_color_and_type[Side.BLACK.ordinal() * 10
 				+ Piece.PAWN.ordinal()] = 8;
 
 		king_pos[Side.WHITE.ordinal()] = 51;
 		king_pos[Side.BLACK.ordinal()] = 58;
 		resetCache();
 	}
 
 	@Override
 	public void setToFEN(String fen) {
 		side_board = new Side[65];
 		piece_board = new Piece[65];
 
 		castling[0] = -1;
 		castling[1] = -1;
 		castling[2] = -1;
 		castling[3] = -1;
 		en_passant_target = -1;
 
 		resetCache();
 
 		String[] fen_parts = fen.split(" ");
 
 		// populate the squares
 		String[] fen_rows = fen_parts[0].split("/");
 		char[] pieces;
 		for (int row = 1; row <= 8; row++) {
 			int offset = 0;
 			for (int column = 1; column + offset <= 8; column++) {
 				pieces = fen_rows[8 - row].toCharArray();
 				int square = (column + offset) * 10 + row;
 				switch (pieces[column - 1]) {
 				case 'P':
 					setOnBoard(square, Side.WHITE, Piece.PAWN);
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.PAWN.ordinal()]++;
 					break;
 				case 'R':
 					setOnBoard(square, Side.WHITE, Piece.ROOK);
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.ROOK.ordinal()]++;
 					break;
 				case 'N':
 					setOnBoard(square, Side.WHITE, Piece.KNIGHT);
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.KNIGHT.ordinal()]++;
 					break;
 				case 'B':
 					setOnBoard(square, Side.WHITE, Piece.BISHOP);
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.BISHOP.ordinal()]++;
 					break;
 				case 'Q':
 					setOnBoard(square, Side.WHITE, Piece.QUEEN);
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.QUEEN.ordinal()]++;
 					break;
 				case 'K':
 					setOnBoard(square, Side.WHITE, Piece.KING);
 					king_pos[Side.WHITE.ordinal()] = (byte) square;
 					num_occupied_squares_by_color_and_type[Side.WHITE.ordinal()
 							* 10 + Piece.KING.ordinal()]++;
 					break;
 				case 'p':
 					setOnBoard(square, Side.BLACK, Piece.PAWN);
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.PAWN.ordinal()]++;
 					break;
 				case 'r':
 					setOnBoard(square, Side.BLACK, Piece.ROOK);
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.ROOK.ordinal()]++;
 					break;
 				case 'n':
 					setOnBoard(square, Side.BLACK, Piece.KNIGHT);
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.KNIGHT.ordinal()]++;
 					break;
 				case 'b':
 					setOnBoard(square, Side.BLACK, Piece.BISHOP);
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.BISHOP.ordinal()]++;
 					break;
 				case 'q':
 					setOnBoard(square, Side.BLACK, Piece.QUEEN);
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.QUEEN.ordinal()]++;
 					break;
 				case 'k':
 					setOnBoard(square, Side.BLACK, Piece.KING);
 					king_pos[Side.BLACK.ordinal()] = (byte) square;
 					num_occupied_squares_by_color_and_type[Side.BLACK.ordinal()
 							* 10 + Piece.KING.ordinal()]++;
 					break;
 				default:
 					offset += Character.getNumericValue(pieces[column - 1]) - 1;
 					break;
 				}
 			}
 		}
 
 		// set active color
 		switch (fen_parts[1]) {
 		case "b":
 			active_color = Side.BLACK;
 			break;
 		case "w":
 			active_color = Side.WHITE;
 			break;
 		}
 
 		// set possible castling moves
 		if (!fen_parts[2].equals("-")) {
 			char[] castlings = fen_parts[2].toCharArray();
 			for (int i = 0; i < castlings.length; i++) {
 				switch (castlings[i]) {
 				case 'K':
 					castling[1] = 71;
 					break;
 				case 'Q':
 					castling[0] = 31;
 					break;
 				case 'k':
 					castling[3] = 78;
 					break;
 				case 'q':
 					castling[2] = 38;
 					break;
 				}
 			}
 		}
 
 		// set en passant square
 		if (!fen_parts[3].equals("-")) {
 			en_passant_target = SquareHelper.fromString(fen_parts[3]);
 		}
 	}
 
 	@Override
 	public IPosition doMove_copy(IMove move) {
 		Position newBoard = this.returnCopy();
 
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		Piece piece = getPieceFromBoard(src);
 		Piece capture = getPieceFromBoard(dest);
 		boolean resets_half_move_clock = false;
 		// if promotion
 		if (move.getPromotion() != null) {
 			newBoard.setOnBoard(src, null, null);
 			newBoard.setOnBoard(dest, active_color, move.getPromotion());
 			resets_half_move_clock = true;
 			newBoard.num_occupied_squares_by_color_and_type[active_color
 					.ordinal() * 10 + Piece.PAWN.ordinal()]--;
 			newBoard.num_occupied_squares_by_color_and_type[active_color
 					.ordinal() * 10 + move.getPromotion().ordinal()]++;
 		}
 		// If castling
 		else if (piece == Piece.KING && Math.abs((src - dest)) == 20) {
 			newBoard.setOnBoard(dest, active_color, Piece.KING);
 			newBoard.setOnBoard(src, null, null);
 			newBoard.setOnBoard((src + dest) / 2, active_color, Piece.ROOK);
 			if (SquareHelper.getColumn(dest) == 3)
 				newBoard.setOnBoard(src - 40, null, null);
 			else
 				newBoard.setOnBoard(src + 30, null, null);
 
 		}
 		// If en passant
 		else if (piece == Piece.PAWN && dest == this.getEnPassant()) {
 			newBoard.setOnBoard(dest, active_color, Piece.PAWN);
 			newBoard.setOnBoard(src, null, null);
 			if (active_color == Side.WHITE) {
 				capture = getPieceFromBoard(dest - 1);
 				newBoard.setOnBoard(dest - 1, null, null);
 			} else {
 				capture = getPieceFromBoard(dest + 1);
 				newBoard.setOnBoard(dest + 1, null, null);
 			}
 			resets_half_move_clock = true;
 		}
 		// Usual move
 		else {
 			Side side = getSideFromBoard(src);
 			newBoard.setOnBoard(dest, side, piece);
 			newBoard.setOnBoard(src, null, null);
 			if (this.getSideFromBoard(dest) != null || piece == Piece.PAWN)
 				resets_half_move_clock = true;
 		}
 
 		if (resets_half_move_clock)
 			newBoard.half_move_clock = 0;
 
 		// update counters
 		if (capture != null) {
 			newBoard.num_occupied_squares_by_color_and_type[Side
 					.getOppositeSide(active_color).ordinal()
 					* 10
 					+ capture.ordinal()]--;
 		}
 
 		// Change active_color after move
 		newBoard.active_color = Side.getOppositeSide(active_color);
 
 		// Update en_passant
 		if (piece == Piece.PAWN && Math.abs(dest - src) == 2)
 			newBoard.en_passant_target = (dest + src) / 2;
 		else
 			newBoard.en_passant_target = -1;
 
 		// Update castling
 		if (piece == Piece.KING) {
 			newBoard.king_pos[active_color.ordinal()] = (byte) dest;
 			if (active_color == Side.WHITE && src == 51) {
 				newBoard.castling[0] = -1;
 				newBoard.castling[1] = -1;
 			} else if (active_color == Side.BLACK && src == 58) {
 				newBoard.castling[2] = -1;
 				newBoard.castling[3] = -1;
 			}
 		} else if (piece == Piece.ROOK) {
 			if (active_color == Side.WHITE) {
 				if (src == 81)
 					newBoard.castling[1] = -1;
 				else if (src == 11)
 					newBoard.castling[0] = -1;
 			} else {
 				if (src == 88)
 					newBoard.castling[3] = -1;
 				else if (src == 18)
 					newBoard.castling[2] = -1;
 			}
 		}
 		if (capture == Piece.ROOK) {
 			if (active_color == Side.BLACK) {
 				if (dest == 81)
					newBoard.castling[1] = -1;
 				else if (dest == 11)
					newBoard.castling[0] = -1;
 			} else {
 				if (dest == 88)
					newBoard.castling[3] = -1;
 				else if (dest == 18)
					newBoard.castling[2] = -1;
 			}
 		}
 
 		return newBoard;
 	}
 
 	@Override
 	public int getEnPassant() {
 		return en_passant_target;
 	}
 
 	@Override
 	public boolean canCastle(int king_to) {
 		if ((king_to == 31 && castling[0] != -1)
 				|| (king_to == 71 && castling[1] != -1)
 				|| (king_to == 38 && castling[2] != -1)
 				|| (king_to == 78 && castling[3] != -1)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public Boolean colorCanCastle(Side color) {
 
 		// Set the right color
 		if (active_color != color)
 			active_color = getOpponentsColor();
 
 		// check for castling
 		if (!isCheckPosition()) {
 			int off = 0;
 			int square = 51;
 
 			if (color == Side.BLACK) {
 				off = 2;
 				square = 58;
 			}
 
 			for (int i = 0; i < 2; i++) {
 				int castle_flag = 0;
 				Integer new_square = castling[i + off];
 				// castling must still be possible to this side
 				if (new_square != -1) {
 
 					Direction dir;
 					if (i == 0)
 						dir = Direction.WEST;
 					else
 						dir = Direction.EAST;
 
 					List<Integer> line = SquareHelper.getAllSquaresInDirection(
 							square, dir);
 
 					// Check each square if it is empty
 					for (Integer squ : line) {
 						if (getSideFromBoard(squ) != null) {
 							castle_flag = 1;
 							break;
 						}
 						if (squ == new_square)
 							break;
 					}
 					if (castle_flag == 1)
 						continue;
 
 					// Check each square if the king on it would be check
 					for (Integer squ : line) {
 						setOnBoard(squ, active_color, Piece.KING);
 						setOnBoard(square, null, null);
 
 						if (isCheckPosition()) {
 							setOnBoard(square, active_color, Piece.KING);
 							setOnBoard(squ, null, null);
 							break;
 						}
 						setOnBoard(square, active_color, Piece.KING);
 						setOnBoard(squ, null, null);
 						if (squ == new_square) {
 							// If the end is reached, then stop checking.
 
 							// undoing change of color
 							if (active_color == color)
 								active_color = getOpponentsColor();
 
 							return true;
 						}
 					}
 				}
 			}
 		}
 
 		// undoing change of color
 		if (active_color == color)
 			active_color = getOpponentsColor();
 
 		return false;
 	}
 
 	@Override
 	public Set<Integer> getOccupiedSquaresByColor(Side color) {
 
 		if (occupied_squares_by_color.containsKey(color) == false) {
 			Set<Integer> set = new HashSet<Integer>();
 
 			for (int square : SquareHelper.all_squares)
 				if (getSideFromBoard(square) == color)
 					set.add(square);
 
 			occupied_squares_by_color.put(color, set);
 			return set;
 		}
 		return occupied_squares_by_color.get(color);
 	}
 
 	@Override
 	public Set<Integer> getOccupiedSquaresByType(Piece type) {
 
 		if (occupied_squares_by_type.containsKey(type) == false) {
 			Set<Integer> set = new HashSet<Integer>();
 
 			for (int square : SquareHelper.all_squares)
 				if (getPieceFromBoard(square) == type)
 					set.add(square);
 
 			occupied_squares_by_type.put(type, set);
 			return set;
 		}
 		return occupied_squares_by_type.get(type);
 
 	}
 
 	@Override
 	public Set<Integer> getOccupiedSquaresByColorAndType(Side color, Piece type) {
 
 		int value = color.ordinal() * 10 + type.ordinal();
 
 		if (occupied_squares_by_color_and_type.containsKey(value) == false) {
 			Set<Integer> set = new HashSet<Integer>();
 			if (type == Piece.KING)
 				set.add((int) king_pos[color.ordinal()]);
 			else {
 				for (int square : SquareHelper.all_squares)
 					if (type == getPieceFromBoard(square)
 							&& color == getSideFromBoard(square))
 						set.add(square);
 			}
 			occupied_squares_by_color_and_type.put(value, set);
 			return set;
 		}
 		return occupied_squares_by_color_and_type.get(value);
 	}
 
 	@Override
 	public int getNumberOfPiecesByColor(Side side) {
 		int result = 0;
 		for (Piece piece : Piece.values()) {
 			result += num_occupied_squares_by_color_and_type[side.ordinal()
 					* 10 + piece.ordinal()];
 		}
 		return result;
 	}
 
 	@Override
 	public int getNumberOfPiecesByType(Piece piece) {
 		int result = 0;
 		for (Side side : Side.values()) {
 			result += num_occupied_squares_by_color_and_type[side.ordinal()
 					* 10 + piece.ordinal()];
 		}
 		return result;
 	}
 
 	@Override
 	public int getNumberOfPiecesByColorAndType(Side color, Piece type) {
 		int value = color.ordinal() * 10 + type.ordinal();
 		return num_occupied_squares_by_color_and_type[value];
 	}
 
 	@Override
 	public List<IMove> getPossibleMoves() {
 		if (possible_moves == null) {
 			List<IMove> total_list = new ArrayList<IMove>(40);
 
 			// loop over all squares
 			for (int square : SquareHelper.all_squares) {
 				if (getSideFromBoard(square) == active_color)
 					total_list.addAll(getPossibleMovesFrom(square));
 			}
 
 			// cache it
 			possible_moves = new SoftReference<List<IMove>>(total_list);
 			return total_list;
 		} else {
 			// return from cache
 			return possible_moves.get();
 		}
 	}
 
 	@Override
 	public List<IMove> getPossibleMovesFrom(int square) {
 		// The case, that the destination is the opponents king cannot happen.
 
 		Piece type = getPieceFromBoard(square);
 		Side opp_color = getOpponentsColor();
 
 		ArrayList<List<Integer>> all_squares = SquareHelper
 				.getSquaresAllDirections(square);
 		List<Integer> squares;
 		List<IMove> moves = new ArrayList<IMove>();
 		Move move;
 
 		// Types BISHOP, QUEEN, ROOK
 		if (type == Piece.BISHOP || type == Piece.QUEEN || type == Piece.ROOK) {
 
 			// Loop over all directions and skip not appropriate ones
 			for (Direction direction : Direction.values()) {
 
 				// Skip N,W,E,W with BISHOP and skip NE,NW,SE,SW with ROOK
 				if (((direction == Direction.NORTH
 						|| direction == Direction.EAST
 						|| direction == Direction.SOUTH || direction == Direction.WEST) && type == Piece.BISHOP)
 						|| ((direction == Direction.NORTHWEST
 								|| direction == Direction.NORTHEAST
 								|| direction == Direction.SOUTHEAST || direction == Direction.SOUTHWEST) && type == Piece.ROOK)) {
 
 					continue;
 				} else {
 					// do stuff
 					squares = SquareHelper.getAllSquaresInDirection(
 							all_squares, direction);
 
 					for (Integer new_square : squares) {
 						Piece piece = getPieceFromBoard(new_square);
 						Side color = getSideFromBoard(new_square);
 						if (piece == null || color == opp_color) {
 
 							move = new Move(square, new_square);
 							moves.add(move);
 							if (piece != null && color == opp_color)
 								// not possible to go further
 								break;
 						} else
 							break;
 					}
 				}
 
 			}
 
 		}
 
 		if (type == Piece.PAWN) {
 			// If Pawn has not moved yet (steps possible)
 			if ((SquareHelper.getRow(square) == 2 && active_color == Side.WHITE)
 					|| (SquareHelper.getRow(square) == 7 && active_color == Side.BLACK)) {
 
 				if (getSideFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == null) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset);
 					moves.add(move);
 					if (getSideFromBoard(square + 2
 							* Direction.pawnDirection(active_color).offset) == null) {
 						move = new Move(square, square + 2
 								* Direction.pawnDirection(active_color).offset);
 						moves.add(move);
 					}
 				}
 
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if (getSideFromBoard(square + direction.offset) == getOpponentsColor()) {
 						move = new Move(square, square + direction.offset);
 						moves.add(move);
 					}
 				}
 
 			}
 			// if Promotion will happen
 			else if ((SquareHelper.getRow(square) == 7 && active_color == Side.WHITE)
 					|| (SquareHelper.getRow(square) == 2 && active_color == Side.BLACK)) {
 				if (getSideFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == null) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							Piece.QUEEN);
 					moves.add(move);
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							Piece.KNIGHT);
 					moves.add(move);
 
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							Piece.ROOK);
 					moves.add(move);
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							Piece.BISHOP);
 					moves.add(move);
 
 				}
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if (getSideFromBoard(square + direction.offset) == getOpponentsColor()) {
 						move = new Move(square, square + direction.offset,
 								Piece.QUEEN);
 						moves.add(move);
 						move = new Move(square, square + direction.offset,
 								Piece.KNIGHT);
 						moves.add(move);
 
 						move = new Move(square, square + direction.offset,
 								Piece.ROOK);
 						moves.add(move);
 						move = new Move(square, square + direction.offset,
 								Piece.BISHOP);
 						moves.add(move);
 					}
 				}
 
 			}
 			// Usual turn and en passant is possible, no promotion
 			else {
 				if (getSideFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == null) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset);
 					moves.add(move);
 				}
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if ((getSideFromBoard(square + direction.offset) == getOpponentsColor())
 							|| square + direction.offset == getEnPassant()) {
 						move = new Move(square, square + direction.offset);
 						moves.add(move);
 					}
 				}
 			}
 
 		}
 		if (type == Piece.KING) {
 			for (Direction direction : Direction.values()) {
 				Integer new_square = square + direction.offset;
 
 				if (SquareHelper.isValidSquare(new_square)) {
 					move = new Move(square, new_square);
 					Side side = getSideFromBoard(new_square);
 					// if the new square is empty or occupied by the opponent
 					if (side != active_color)
 						moves.add(move);
 				}
 			}
 
 			// Castle Moves
 			// If the King is not check now, try castle moves
 			if (!isCheckPosition()) {
 				int off = 0;
 				if (active_color == Side.BLACK)
 					off = 2;
 
 				for (int i = 0; i < 2; i++) {
 					int castle_flag = 0;
 					Integer new_square = castling[i + off];
 					// castling must still be possible to this side
 					if (new_square != -1) {
 
 						Direction dir;
 						if (i == 0)
 							dir = Direction.WEST;
 						else
 							dir = Direction.EAST;
 
 						List<Integer> line = SquareHelper
 								.getAllSquaresInDirection(square, dir);
 
 						// Check each square if it is empty
 						int last_squ = line.get(line.size() - 1);
 						for (Integer squ : line) {
 							if (squ == last_squ)
 								break;
 							if (getSideFromBoard(squ) != null) {
 								castle_flag = 1;
 								break;
 							}
 
 						}
 						if (castle_flag == 1)
 							continue;
 
 						// Check each square if the king on it would be check
 						for (Integer squ : line) {
 
 							setOnBoard(squ, active_color, Piece.KING);
 							setOnBoard(square, null, null);
 
 							if (isCheckPosition()) {
 								setOnBoard(square, active_color, Piece.KING);
 								setOnBoard(squ, null, null);
 								break;
 							}
 							setOnBoard(square, active_color, Piece.KING);
 							setOnBoard(squ, null, null);
 							if (squ == new_square) {
 								// if everything is right, then add the move
 								move = new Move(square, squ);
 								moves.add(move);
 								break;
 							}
 
 						}
 					}
 				}
 			}
 		}
 		if (type == Piece.KNIGHT) {
 			squares = SquareHelper.getAllSquaresByKnightStep(square);
 			for (Integer new_square : squares) {
 				Side side = getSideFromBoard(new_square);
 				if (side != active_color) {
 					move = new Move(square, new_square);
 					moves.add(move);
 				}
 			}
 		}
 
 		// remove invalid positions
 		Iterator<IMove> iter = moves.iterator();
 		IMove mv;
 		while (iter.hasNext()) {
 			mv = iter.next();
 			tinyDoMove(mv);
 			active_color = Side.getOppositeSide(active_color);
 			if (isCheckPosition()) {
 				iter.remove();
 			}
 			active_color = Side.getOppositeSide(active_color);
 			tinyUndoMove(mv);
 		}
 
 		return moves;
 	}
 
 	@Override
 	public List<IMove> getPossibleMovesTo(int square) {
 		List<IMove> possible_moves = getPossibleMoves();
 		List<IMove> result = new ArrayList<IMove>(possible_moves.size());
 
 		for (IMove move : possible_moves) {
 			if (move.getToSquare() == square)
 				result.add(move);
 		}
 
 		return result;
 	}
 
 	@Override
 	public boolean isCheckPosition() {
 		if (is_check == null) {
 			is_check = true;
 			int king_pos = getKingPos(active_color);
 			ArrayList<List<Integer>> all_squares = SquareHelper
 					.getSquaresAllDirections(king_pos);
 			// go in each direction
 			for (Direction direction : Direction.values()) {
 				List<Integer> line = SquareHelper.getAllSquaresInDirection(
 						all_squares, direction);
 				// go until…
 				int iter = 0;
 				for (int square : line) {
 					iter++;
 					// …some piece is found
 					Piece piece = getPieceFromBoard(square);
 					if (piece != null) {
 						Side side = getSideFromBoard(square);
 						if (side == active_color) {
 							break;
 						} else {
 							if (piece == Piece.PAWN && iter == 1) {
 								if (((direction == Direction.NORTHEAST || direction == Direction.NORTHWEST) && active_color == Side.WHITE)
 										|| ((direction == Direction.SOUTHEAST || direction == Direction.SOUTHWEST) && active_color == Side.BLACK)) {
 									return true;
 								}
 							} else if (piece == Piece.ROOK) {
 								if (direction == Direction.EAST
 										|| direction == Direction.WEST
 										|| direction == Direction.NORTH
 										|| direction == Direction.SOUTH) {
 									return true;
 								}
 							} else if (piece == Piece.BISHOP) {
 								if (direction == Direction.NORTHEAST
 										|| direction == Direction.NORTHWEST
 										|| direction == Direction.SOUTHEAST
 										|| direction == Direction.SOUTHWEST) {
 									return true;
 								}
 							} else if (piece == Piece.QUEEN) {
 								return true;
 							} else if (piece == Piece.KING && iter == 1) {
 								return true;
 							}
 							break;
 						}
 					}
 				}
 			}
 
 			// check for knight attacks
 			List<Integer> knight_squares = SquareHelper
 					.getAllSquaresByKnightStep(king_pos);
 			for (int square : knight_squares) {
 				Piece piece = getPieceFromBoard(square);
 				if (piece != null) {
 					Side side = getSideFromBoard(square);
 					if (side != active_color && piece == Piece.KNIGHT) {
 						return true;
 					}
 				}
 			}
 			is_check = false;
 		}
 		return is_check.booleanValue();
 
 	}
 
 	@Override
 	public boolean isMatePosition() {
 		if (is_mate == null) {
 			is_mate = true;
 			List<IMove> moves = getPossibleMoves();
 			if (moves.isEmpty() && isCheckPosition())
 				return true;
 			is_mate = false;
 		}
 		return is_mate.booleanValue();
 	}
 
 	@Override
 	public boolean isStaleMatePosition() {
 		if (is_stale_mate == null) {
 			is_stale_mate = true;
 			List<IMove> moves = getPossibleMoves();
 			if (moves.isEmpty())
 				return true;
 			is_stale_mate = false;
 		}
 		return is_stale_mate.booleanValue();
 	}
 
 	@Override
 	public boolean isPossibleMove(IMove move) {
 
 		List<IMove> possible_moves = getPossibleMoves();
 
 		return possible_moves.contains(move);
 	}
 
 	public String toString() {
 		return toFEN();
 	}
 
 	@Override
 	public String toFEN() {
 		StringBuilder fen = new StringBuilder();
 
 		// piece placement
 		for (int row = 0; row < 8; row++) {
 
 			int counter = 0;
 
 			for (int column = 0; column < 8; column++) {
 
 				if (side_board[row * 8 + column] == null) {
 					counter++;
 				} else {
 					if (counter != 0) {
 						fen.append(counter);
 						counter = 0;
 					}
 					fen.append(PieceHelper.toString(
 							side_board[row * 8 + column], piece_board[row * 8
 									+ column]));
 				}
 				if (column == 7 && counter != 0) {
 					fen.append(counter);
 				}
 			}
 
 			if (row != 7) {
 				fen.append("/");
 			}
 		}
 		fen.append(" ");
 
 		// active color
 		if (active_color == Side.WHITE) {
 			fen.append("w");
 		} else {
 			fen.append("b");
 		}
 		fen.append(" ");
 
 		// castling availability
 		boolean castle_flag = false;
 		if (castling[1] != -1) {
 			fen.append("K");
 			castle_flag = true;
 		}
 		if (castling[0] != -1) {
 			fen.append("Q");
 			castle_flag = true;
 		}
 		if (castling[3] != -1) {
 			fen.append("k");
 			castle_flag = true;
 		}
 		if (castling[2] != -1) {
 			fen.append("q");
 			castle_flag = true;
 		}
 		if (!castle_flag) {
 			fen.append("-");
 		}
 		fen.append(" ");
 
 		// en passant target square
 		if (en_passant_target == -1) {
 			fen.append("-");
 		} else {
 			fen.append(SquareHelper.toString(en_passant_target));
 		}
 
 		return fen.toString();
 	}
 
 	@Override
 	public Side getActiveColor() {
 		return active_color;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 
 		for (Side element : side_board)
 			result = prime * result
 					+ (element == null ? 0 : element.ordinal() + 1);
 
 		for (Piece element : piece_board)
 			result = prime * result
 					+ (element == null ? 0 : element.ordinal() + 1);
 
 		for (int element : castling)
 			result = prime * result + element;
 
 		result = prime * result + active_color.ordinal();
 
 		result = prime * result + en_passant_target;
 
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Position other = (Position) obj;
 		if (!Arrays.equals(side_board, other.side_board)
 				|| !Arrays.equals(piece_board, other.piece_board)
 				|| !Arrays.equals(castling, other.castling)
 				|| en_passant_target != other.en_passant_target
 				|| active_color != other.active_color) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public List<IMove> generateCaptures() {
 		List<IMove> poss_moves = getPossibleMoves();
 		List<IMove> result = new ArrayList<IMove>(poss_moves.size());
 
 		for (IMove move : poss_moves)
 			if (isHit(move) || move.getPromotion() != null)
 				result.add(move);
 		return result;
 	}
 
 	@Override
 	public long hashCode2() {
 		final int prime = 23;
 		long result = 1;
 
 		for (Side element : side_board)
 			result = prime * result
 					+ (element == null ? 0 : element.ordinal() + 1);
 
 		for (Piece element : piece_board)
 			result = prime * result
 					+ (element == null ? 0 : element.ordinal() + 1);
 
 		for (int element : castling)
 			result = prime * result + element;
 
 		result = prime * result + active_color.ordinal();
 
 		result = prime * result + en_passant_target;
 
 		return result;
 	}
 
 	@Override
 	public int getKingPos(Side side) {
 		return king_pos[side.ordinal()];
 	}
 
 	@Override
 	public void cacheOccupiedSquares() {
 
 		Side s;
 		Piece p;
 		Set<Integer> w_pawn = new HashSet<Integer>();
 		Set<Integer> w_rook = new HashSet<Integer>();
 		Set<Integer> w_bishop = new HashSet<Integer>();
 		Set<Integer> w_knight = new HashSet<Integer>();
 		Set<Integer> w_queen = new HashSet<Integer>();
 
 		Set<Integer> b_pawn = new HashSet<Integer>();
 		Set<Integer> b_rook = new HashSet<Integer>();
 		Set<Integer> b_bishop = new HashSet<Integer>();
 		Set<Integer> b_knight = new HashSet<Integer>();
 		Set<Integer> b_queen = new HashSet<Integer>();
 
 		for (int square : SquareHelper.all_squares) {
 
 			s = getSideFromBoard(square);
 			if (s == null)
 				continue;
 			p = getPieceFromBoard(square);
 			switch (s) {
 			case WHITE:
 				switch (p) {
 				case PAWN:
 					w_pawn.add(square);
 					break;
 				case ROOK:
 					w_rook.add(square);
 					break;
 				case BISHOP:
 					w_bishop.add(square);
 					break;
 				case KNIGHT:
 					w_knight.add(square);
 					break;
 				case QUEEN:
 					w_queen.add(square);
 					break;
 				default:
 					break;
 				}
 				break;
 			case BLACK:
 				switch (p) {
 				case PAWN:
 					b_pawn.add(square);
 					break;
 				case ROOK:
 					b_rook.add(square);
 					break;
 				case BISHOP:
 					b_bishop.add(square);
 					break;
 				case KNIGHT:
 					b_knight.add(square);
 					break;
 				case QUEEN:
 					b_queen.add(square);
 					break;
 				default:
 					break;
 				}
 				break;
 			}
 
 		}
 
 		occupied_squares_by_color_and_type.put(Side.WHITE.ordinal() * 10
 				+ Piece.PAWN.ordinal(), w_pawn);
 		occupied_squares_by_color_and_type.put(Side.WHITE.ordinal() * 10
 				+ Piece.ROOK.ordinal(), w_rook);
 		occupied_squares_by_color_and_type.put(Side.WHITE.ordinal() * 10
 				+ Piece.BISHOP.ordinal(), w_bishop);
 		occupied_squares_by_color_and_type.put(Side.WHITE.ordinal() * 10
 				+ Piece.KNIGHT.ordinal(), w_knight);
 		occupied_squares_by_color_and_type.put(Side.WHITE.ordinal() * 10
 				+ Piece.QUEEN.ordinal(), w_queen);
 
 		occupied_squares_by_color_and_type.put(Side.BLACK.ordinal() * 10
 				+ Piece.PAWN.ordinal(), b_pawn);
 		occupied_squares_by_color_and_type.put(Side.BLACK.ordinal() * 10
 				+ Piece.ROOK.ordinal(), b_rook);
 		occupied_squares_by_color_and_type.put(Side.BLACK.ordinal() * 10
 				+ Piece.BISHOP.ordinal(), b_bishop);
 		occupied_squares_by_color_and_type.put(Side.BLACK.ordinal() * 10
 				+ Piece.KNIGHT.ordinal(), b_knight);
 		occupied_squares_by_color_and_type.put(Side.BLACK.ordinal() * 10
 				+ Piece.QUEEN.ordinal(), b_queen);
 
 	}
 
 	@Override
 	public void doMove(IMove move) {
 
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		Piece piece = getPieceFromBoard(src);
 		Piece capture = getPieceFromBoard(dest);
 
 		setOnBoard(dest, active_color, piece);
 		setOnBoard(src, null, null);
 
 		boolean resets_half_move_clock = false;
 
 		// if promotion
 		if (move.getPromotion() != null) {
 			setOnBoard(dest, active_color, move.getPromotion());
 			resets_half_move_clock = true;
 			num_occupied_squares_by_color_and_type[active_color.ordinal() * 10
 					+ Piece.PAWN.ordinal()]--;
 			num_occupied_squares_by_color_and_type[active_color.ordinal() * 10
 					+ move.getPromotion().ordinal()]++;
 		}
 		// If castling
 		else if (piece == Piece.KING && Math.abs((src - dest)) == 20) {
 			setOnBoard((src + dest) / 2, active_color, Piece.ROOK);
 			if (SquareHelper.getColumn(dest) == 3)
 				setOnBoard(src - 40, null, null);
 			else
 				setOnBoard(src + 30, null, null);
 		}
 		// If en passant
 		else if (piece == Piece.PAWN && dest == en_passant_target) {
 			if (active_color == Side.WHITE) {
 				setOnBoard(dest - 1, null, null);
 			} else {
 				setOnBoard(dest + 1, null, null);
 			}
 			num_occupied_squares_by_color_and_type[Side.getOppositeSide(
 					active_color).ordinal()
 					* 10 + Piece.PAWN.ordinal()]--;
 			resets_half_move_clock = true;
 		}
 		// Usual move
 		else {
 			if (capture != null || piece == Piece.PAWN)
 				resets_half_move_clock = true;
 		}
 
 		// update counters
 		if (capture != null) {
 			num_occupied_squares_by_color_and_type[Side.getOppositeSide(
 					active_color).ordinal()
 					* 10 + capture.ordinal()]--;
 		}
 		
 		IrreversibleMoveStack.addInfo(half_move_clock, castling,
 				en_passant_target, capture, is_check);
 
 		// reset half move clock
 		if (resets_half_move_clock)
 			half_move_clock = 0;
 
 		// Update en_passant
 		if (piece == Piece.PAWN && Math.abs(dest - src) == 2)
 			en_passant_target = (dest + src) / 2;
 		else
 			en_passant_target = -1;
 
 		// Update castling
 		if (piece == Piece.KING) {
 			king_pos[active_color.ordinal()] = (byte) dest;
 			if (active_color == Side.WHITE && src == 51) {
 				castling[0] = -1;
 				castling[1] = -1;
 			} else if (active_color == Side.BLACK && src == 58) {
 				castling[2] = -1;
 				castling[3] = -1;
 			}
 		} else if (piece == Piece.ROOK) {
 			if (active_color == Side.WHITE) {
 				if (src == 81)
 					castling[1] = -1;
 				else if (src == 11)
 					castling[0] = -1;
 			} else {
 				if (src == 88)
 					castling[3] = -1;
 				else if (src == 18)
 					castling[2] = -1;
 			}
 		}
 		if (capture == Piece.ROOK) {
 			if (active_color == Side.BLACK) {
 				if (dest == 81)
 					castling[1] = -1;
 				else if (dest == 11)
 					castling[0] = -1;
 			} else {
 				if (dest == 88)
 					castling[3] = -1;
 				else if (dest == 18)
 					castling[2] = -1;
 			}
 		}
 
 		// Change active_color after move
 		active_color = Side.getOppositeSide(active_color);
 
 		resetCache();
 
 	}
 
 	@Override
 	public void undoMove(IMove move) {
 		
 		resetCache();
 		
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		Piece piece = getPieceFromBoard(dest);
 
 		// Change active_color after move
 		active_color = Side.getOppositeSide(active_color);
 
 		// get the missing information
 		MoveInfo inf = IrreversibleMoveStack.irr_move_info.removeLast();
 
 		en_passant_target = inf.en_passant_square;
 		Piece capture = inf.capture;
 		half_move_clock = inf.half_move_clock;
 		System.arraycopy(inf.castling, 0, castling, 0, 4);
 		is_check = inf.is_check;		
 
 		setOnBoard(src, active_color, piece);
 		if (capture != null)
 			setOnBoard(dest, Side.getOppositeSide(active_color), capture);
 		else
 			setOnBoard(dest, null, null);
 
 		// if promotion
 		if (move.getPromotion() != null) {
 			setOnBoard(src, active_color, Piece.PAWN);
 			num_occupied_squares_by_color_and_type[active_color.ordinal() * 10
 					+ Piece.PAWN.ordinal()]++;
 			num_occupied_squares_by_color_and_type[active_color.ordinal() * 10
 					+ move.getPromotion().ordinal()]--;
 		}
 		// If castling
 		else if (piece == Piece.KING && Math.abs((src - dest)) == 20) {
 			setOnBoard((src + dest) / 2, null, null);
 			if (SquareHelper.getColumn(dest) == 3)
 				setOnBoard(src - 40, active_color, Piece.ROOK);
 			else
 				setOnBoard(src + 30, active_color, Piece.ROOK);
 
 		}
 		// If en passant
 		else if (piece == Piece.PAWN && dest == en_passant_target) {
 			if (active_color == Side.WHITE) {
 				setOnBoard(dest - 1, Side.getOppositeSide(active_color),
 						Piece.PAWN);
 			} else {
 				setOnBoard(dest + 1, Side.getOppositeSide(active_color),
 						Piece.PAWN);
 			}
 			num_occupied_squares_by_color_and_type[Side.getOppositeSide(
 					active_color).ordinal()
 					* 10 + Piece.PAWN.ordinal()]++;
 		}
 
 		// update counters
 		if (capture != null) {
 			num_occupied_squares_by_color_and_type[Side.getOppositeSide(
 					active_color).ordinal()
 					* 10 + capture.ordinal()]++;
 		}
 
 		if (piece == Piece.KING) {
 			king_pos[active_color.ordinal()] = (byte) src;
 		}
 
 		is_mate = false;
 		is_stale_mate = false;
 
 	}
 
 	/**
 	 * Performs a incomplete version of doMove. This function only sets the new
 	 * figure, deletes the captures ones (are saved in side_capture and
 	 * piece_capture) and changes the active color. Note that it is not possible
 	 * to perform tinyDoMove twice, because the captured figure of the first
 	 * application will be lost.
 	 * 
 	 * @param move
 	 *            the move to be performed, must be a legal move
 	 */
 	private void tinyDoMove(IMove move) {
 
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		Piece piece = getPieceFromBoard(src);
 		piece_capture = getPieceFromBoard(dest);
 		side_capture = getSideFromBoard(dest);
 
 		setOnBoard(dest, active_color, piece);
 		setOnBoard(src, null, null);
 
 		// if promotion
 		if (move.getPromotion() != null) {
 			setOnBoard(dest, active_color, move.getPromotion());
 		}
 		// If castling
 		else if (piece == Piece.KING && Math.abs((src - dest)) == 20) {
 			setOnBoard((src + dest) / 2, active_color, Piece.ROOK);
 			if (SquareHelper.getColumn(dest) == 3)
 				setOnBoard(src - 40, null, null);
 			else
 				setOnBoard(src + 30, null, null);
 
 		}
 		// If en passant
 		else if (piece == Piece.PAWN && dest == en_passant_target) {
 			if (active_color == Side.WHITE)
 				setOnBoard(dest - 1, null, null);
 			else
 				setOnBoard(dest + 1, null, null);
 		}
 
 		// Update castling
 		if (piece == Piece.KING)
 			king_pos[active_color.ordinal()] = (byte) dest;
 
 		// Change active_color after move
 		active_color = Side.getOppositeSide(active_color);
 
 		old_check = is_check;
 		
 		is_check = null;
 		is_mate = null;
 		is_stale_mate = null;
 
 	}
 
 	/**
 	 * inverts the function tinyDoMove(), note that only one application can be
 	 * inverted!
 	 * 
 	 * @param move
 	 *            the move to be inverted.
 	 */
 	private void tinyUndoMove(IMove move) {
 
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		Piece piece = getPieceFromBoard(dest);
 
 		// Change active_color after move
 		active_color = Side.getOppositeSide(active_color);
 
 		setOnBoard(dest, side_capture, piece_capture);
 		setOnBoard(src, active_color, piece);
 		// if promotion
 		if (move.getPromotion() != null) {
 			setOnBoard(src, active_color, Piece.PAWN);
 		}
 		// If castling
 		else if (piece == Piece.KING && Math.abs((src - dest)) == 20) {
 			setOnBoard((src + dest) / 2, null, null);
 			if (SquareHelper.getColumn(dest) == 3)
 				setOnBoard(src - 40, active_color, Piece.ROOK);
 			else
 				setOnBoard(src + 30, active_color, Piece.ROOK);
 
 		}
 		// If en passant
 		else if (piece == Piece.PAWN && dest == en_passant_target) {
 			if (active_color == Side.WHITE)
 				setOnBoard(dest - 1, Side.getOppositeSide(active_color),
 						Piece.PAWN);
 			else
 				setOnBoard(dest + 1, Side.getOppositeSide(active_color),
 						Piece.PAWN);
 		}
 
 		// Update king position
 		if (piece == Piece.KING)
 			king_pos[active_color.ordinal()] = (byte) src;
 
 		is_check = old_check;
 		is_mate = false;
 		is_stale_mate = false;
 		
 	}
 
 	@Override
 	public void setHalfMoveClock(int parseInt) {
 		half_move_clock = parseInt;
 
 	}
 
 	@Override
 	public int getHalfMoveClock() {
 		return half_move_clock;
 	}
 }
