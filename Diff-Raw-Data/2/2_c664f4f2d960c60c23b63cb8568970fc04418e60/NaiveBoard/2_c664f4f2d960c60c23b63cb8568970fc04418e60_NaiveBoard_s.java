 package mitzi;
 
 //import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class NaiveBoard implements IBoard {
 
 	protected static int[][] initial_board = {
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 12, 13, 14, 15, 16, 14, 13, 12, 00, 00 },
 			{ 00, 00, 11, 11, 11, 11, 11, 11, 11, 11, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 01, 01, 01, 01, 01, 01, 01, 01, 00, 00 },
 			{ 00, 00, 02, 03, 04, 05, 06, 04, 03, 02, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 },
 			{ 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 } };
 
 	private int[][] board = new int[12][12];
 
 	private int full_move_clock;
 
 	private int half_move_clock;
 
 	// squares c1, g1, c8 and g8 in ICCF numeric notation
 	// do not change the squares' order or bad things will happen!
 	// set to -1 if castling not allowed
 	private int[] castling = { -1, -1, -1, -1 };
 
 	private int en_passant_target = -1;
 
 	private int active_color;
 
 	private NaiveBoard returnCopy() {
 		NaiveBoard newBoard = new NaiveBoard();
 
 		newBoard.active_color = active_color;
 		newBoard.en_passant_target = en_passant_target;
 		newBoard.full_move_clock = full_move_clock;
 		newBoard.half_move_clock = half_move_clock;
 		System.arraycopy(castling, 0, newBoard.castling, 0, 4);
 
 		for (int i = 2; i < 11; i++)
 			System.arraycopy(board[i], 0, newBoard.board[i], 0, 12);
 
 		return newBoard;
 	}
 
 	private int getFromBoard(int square) {
 		int row = SquareHelper.getRow(square);
 		int column = SquareHelper.getColumn(square);
 		return board[10 - row][1 + column];
 	}
 
 	private void setOnBoard(int square, int piece_value) {
 		int row = SquareHelper.getRow(square);
 		int column = SquareHelper.getColumn(square);
 		board[10 - row][1 + column] = piece_value;
 	}
 
 	public int getOpponentsColor() {
 		if (active_color == PieceHelper.BLACK)
 			return PieceHelper.WHITE;
 		else
 			return PieceHelper.BLACK;
 	}
 
 	@Override
 	public void setToInitial() {
 		board = initial_board;
 
 		full_move_clock = 1;
 
 		half_move_clock = 0;
 
 		castling[0] = 31;
 		castling[1] = 71;
 		castling[2] = 38;
 		castling[3] = 78;
 
 		en_passant_target = -1;
 
 		active_color = PieceHelper.WHITE;
 	}
 
 	@Override
 	public void setToFEN(String fen) {
 		board = new int[12][12];
 		castling[0] = -1;
 		castling[1] = -1;
 		castling[2] = -1;
 		castling[3] = -1;
 		en_passant_target = -1;
 
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
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.PAWN,
 							PieceHelper.WHITE));
 					break;
 				case 'R':
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.ROOK,
 							PieceHelper.WHITE));
 					break;
 				case 'N':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.KNIGHT, PieceHelper.WHITE));
 					break;
 				case 'B':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.BISHOP, PieceHelper.WHITE));
 					break;
 				case 'Q':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.QUEEN, PieceHelper.WHITE));
 					break;
 				case 'K':
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.KING,
 							PieceHelper.WHITE));
 					break;
 				case 'p':
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.PAWN,
 							PieceHelper.BLACK));
 					break;
 				case 'r':
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.ROOK,
 							PieceHelper.BLACK));
 					break;
 				case 'n':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.KNIGHT, PieceHelper.BLACK));
 					break;
 				case 'b':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.BISHOP, PieceHelper.BLACK));
 					break;
 				case 'q':
 					setOnBoard(square, PieceHelper.pieceValue(
 							PieceHelper.QUEEN, PieceHelper.BLACK));
 					break;
 				case 'k':
 					setOnBoard(square, PieceHelper.pieceValue(PieceHelper.KING,
 							PieceHelper.BLACK));
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
 			active_color = PieceHelper.BLACK;
 			break;
 		case "w":
 			active_color = PieceHelper.WHITE;
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
 
 		// set half move clock
 		half_move_clock = Integer.parseInt(fen_parts[4]);
 
 		// set full move clock
 		full_move_clock = Integer.parseInt(fen_parts[5]);
 	}
 
 	@Override
 	public NaiveBoard doMove(IMove move) {
 
 		NaiveBoard newBoard = this.returnCopy();
 
 		int src = move.getFromSquare();
 		int dest = move.getToSquare();
 
 		// if promotion
 		if (move.getPromotion() != 0) {
 			newBoard.setOnBoard(src, 0);
 			newBoard.setOnBoard(dest,
 					PieceHelper.pieceValue(move.getPromotion(), active_color));
 
 			newBoard.half_move_clock = 0;
 
 		}
 		// If rochade
 		else if (PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.KING
 				&& Math.abs((src - dest)) == 20) {
 			newBoard.setOnBoard(dest,
 					PieceHelper.pieceValue(PieceHelper.KING, active_color));
 			newBoard.setOnBoard(src, 0);
 			newBoard.setOnBoard((src + dest) / 2,
 					PieceHelper.pieceValue(PieceHelper.ROOK, active_color));
 			if (SquareHelper.getColumn(dest) == 3)
 				newBoard.setOnBoard(src - 40, 0);
 			else
 				newBoard.setOnBoard(src + 30, 0);
 
 			newBoard.half_move_clock++;
 
 		}
 		// If en passant
 		else if (PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.PAWN
 				&& dest == this.getEnPassant()) {
 			newBoard.setOnBoard(dest,
 					PieceHelper.pieceValue(PieceHelper.PAWN, active_color));
 			newBoard.setOnBoard(src, 0);
 			if (active_color == PieceHelper.WHITE)
 				newBoard.setOnBoard(dest - 1, 0);
 			else
 				newBoard.setOnBoard(dest + 1, 0);
 
 			newBoard.half_move_clock = 0;
 		}
 		// Usual move
 		else {
 			newBoard.setOnBoard(dest, this.getFromBoard(src));
 			newBoard.setOnBoard(src, 0);
 
 			if (this.getFromBoard(dest) != 0
 					|| PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.PAWN)
 				newBoard.half_move_clock = 0;
 			else
 				newBoard.half_move_clock++;
 
 		}
 
 		// Change active_color after move
 		newBoard.active_color = PieceHelper.pieceOppositeColor(this
 				.getFromBoard(src));
 		if (active_color == PieceHelper.BLACK)
 			newBoard.full_move_clock++;
 
 		// Update en_passant
 		if (PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.PAWN
 				&& Math.abs(dest - src) == 2)
 			newBoard.en_passant_target = (dest + src) / 2;
 		else
 			newBoard.en_passant_target = -1;
 
 		// Update castling
 		if (PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.KING)
 			if (active_color == PieceHelper.WHITE && src == 51) {
 				newBoard.castling[0] = -1;
 				newBoard.castling[1] = -1;
 			} else if (active_color == PieceHelper.BLACK && src == 58) {
 				newBoard.castling[2] = -1;
 				newBoard.castling[3] = -1;
 			} else if (PieceHelper.pieceType(this.getFromBoard(src)) == PieceHelper.ROOK)
 				if (active_color == PieceHelper.WHITE) {
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
 	public Set<Integer> getOccupiedSquaresByColor(int color) {
 
 		int square;
 		Set<Integer> set = new HashSet<Integer>();
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceColor(this.getFromBoard(square)) == color)
 					set.add(square);
 			}
 
 		return set;
 	}
 
 	@Override
 	public Set<Integer> getOccupiedSquaresByType(int type) {
 		int square;
 		Set<Integer> set = new HashSet<Integer>();
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceType(this.getFromBoard(square)) == type)
 					set.add(square);
 			}
 
 		return set;
 	}
 
 	@Override
 	public Set<Integer> getOccupiedSquaresByColorAndType(int color, int type) {
 		int square;
 		Set<Integer> set = new HashSet<Integer>();
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceValue(type, color) == this
 								.getFromBoard(square))
 					set.add(square);
 			}
 
 		return set;
 	}
 
 	@Override
 	public int getNumberOfPiecesByColor(int color) {
 		int square;
 		int num = 0;
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceColor(this.getFromBoard(square)) == color)
 					num++;
 			}
 
 		return num;
 	}
 
 	@Override
 	public int getNumberOfPiecesByType(int type) {
 		int square;
 		int num = 0;
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceType(this.getFromBoard(square)) == type)
 					num++;
 			}
 
 		return num;
 	}
 
 	@Override
 	public int getNumberOfPiecesByColorAndType(int color, int type) {
 		int square;
 		int num = 0;
 
 		for (int i = 1; i < 9; i++)
 			for (int j = 1; j < 9; j++) {
 				square = SquareHelper.getSquare(i, j);
 				if (this.getFromBoard(square) > 0
 						&& PieceHelper.pieceValue(type, color) == this
 								.getFromBoard(square))
 					num++;
 			}
 
 		return num;
 	}
 
 	@Override
 	public Set<IMove> getPossibleMoves() {
 		Set<IMove> total_set = new HashSet<IMove>();
 		Set<IMove> temp_set;
 
 		// all the active squares
 		Set<Integer> squares = getOccupiedSquaresByColor(active_color);
 
 		// loop over all squares
 		for (int square : squares) {
 			temp_set = getPossibleMovesFrom(square);
 			total_set.addAll(temp_set);
 		}
 
 		return total_set;
 	}
 
 	@Override
 	public Set<IMove> getPossibleMovesFrom(int square) {
 		// The case, that the destination is the opponents king cannot happen.
 
 		int type = PieceHelper.pieceType(getFromBoard(square));
 		int opp_color = getOpponentsColor();
 		List<Integer> squares;
 		Set<IMove> moves = new HashSet<IMove>();
 		Move move;
 
 		// Types BISHOP, QUEEN, ROOK
 		if (type == PieceHelper.BISHOP || type == PieceHelper.QUEEN
 				|| type == PieceHelper.ROOK) {
 
 			// Loop over all directions and skip not appropriate ones
 			for (Direction direction : Direction.values()) {
 
 				// Skip N,W,E,W with BISHOP and skip NE,NW,SE,SW with ROOK
 				if (((direction == Direction.NORTH
 						|| direction == Direction.EAST
 						|| direction == Direction.SOUTH || direction == Direction.WEST) && type == PieceHelper.BISHOP)
 						|| ((direction == Direction.NORTHWEST
 								|| direction == Direction.NORTHEAST
 								|| direction == Direction.SOUTHEAST || direction == Direction.SOUTHWEST) && type == PieceHelper.ROOK)) {
 
 					continue;
 				} else {
 					// do stuff
 					squares = SquareHelper.getAllSquaresInDirection(square,
 							direction);
 
 					for (Integer new_square : squares) {
 						if (getFromBoard(new_square) == 0
 								|| PieceHelper
 										.pieceColor(getFromBoard(new_square)) == opp_color) {
 
 							move = new Move(square, new_square);
 							moves.add(move);
 							if (getFromBoard(new_square) != 0
 									&& PieceHelper
 											.pieceColor(getFromBoard(new_square)) == opp_color)
 								break;
 						} else
 							break;
 					}
 				}
 
 			}
 
 		}
 
 		if (type == PieceHelper.PAWN) {
 			// If Pawn has not moved yet (steps possible)
 			if ((SquareHelper.getRow(square) == 2 && active_color == PieceHelper.WHITE)
 					|| (SquareHelper.getRow(square) == 7 && active_color == PieceHelper.BLACK)) {
 
 				if (getFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == 0) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset);
 					moves.add(move);
 					if (getFromBoard(square + 2
 							* Direction.pawnDirection(active_color).offset) == 0) {
 						move = new Move(square, square + 2
 								* Direction.pawnDirection(active_color).offset);
 						moves.add(move);
 					}
 				}
 
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if (getFromBoard(square + direction.offset) != 0
 							&& PieceHelper.pieceColor(getFromBoard(square
 									+ direction.offset)) == getOpponentsColor()) {
 						move = new Move(square, square + direction.offset);
 						moves.add(move);
 					}
 				}
 
 			}
 			// if Promotion will happen
 			else if ((SquareHelper.getRow(square) == 7 && active_color == PieceHelper.WHITE)
 					|| (SquareHelper.getRow(square) == 2 && active_color == PieceHelper.BLACK)) {
 				if (getFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == 0) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							PieceHelper.QUEEN);
 					moves.add(move);
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							PieceHelper.KNIGHT);
 					moves.add(move);
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							PieceHelper.ROOK);
 					moves.add(move);
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset,
 							PieceHelper.BISHOP);
 					moves.add(move);
 				}
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if (getFromBoard(square + direction.offset) != 0
 							&& PieceHelper.pieceColor(getFromBoard(square
 									+ direction.offset)) == getOpponentsColor()) {
 						move = new Move(square, square + direction.offset,
 								PieceHelper.QUEEN);
 						moves.add(move);
 						move = new Move(square, square + direction.offset,
 								PieceHelper.KNIGHT);
 						moves.add(move);
 					}
 				}
 
 			}
 			// Usual turn and en passente is possible, no promotion
 			else {
 				if (getFromBoard(square
 						+ Direction.pawnDirection(active_color).offset) == 0) {
 					move = new Move(square, square
 							+ Direction.pawnDirection(active_color).offset);
 					moves.add(move);
 				}
 				Set<Direction> pawn_capturing_directions = Direction
 						.pawnCapturingDirections(active_color);
 				for (Direction direction : pawn_capturing_directions) {
 					if ((getFromBoard(square + direction.offset) != 0 && PieceHelper
 							.pieceColor(getFromBoard(square + direction.offset)) == getOpponentsColor())
							|| getFromBoard(square + direction.offset) == getEnPassant()) {
 						move = new Move(square, square + direction.offset);
 						moves.add(move);
 					}
 				}
 			}
 
 		}
 		if (type == PieceHelper.KING) {
 			for (Direction direction : Direction.values()) {
 				Integer new_square = square + direction.offset;
 				move = new Move(square, new_square);
 				if (SquareHelper.isValidSquare(new_square)) {
 					// if the new square is empty or occupied by the opponent
 					if (getFromBoard(new_square) == 0
 							|| PieceHelper.pieceColor(getFromBoard(new_square)) != active_color)
 						moves.add(move);
 				}
 			}
 
 			// Castle Moves
 			// If the King is not check now, try castle moves
 			if (!isCheckPosition()) {
 				int off = 0;
 				if (active_color == PieceHelper.BLACK)
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
 						for (Integer squ : line) {
 							if (getFromBoard(squ) != 0) {
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
 							move = new Move(square, squ);
 							NaiveBoard board = doMove(move);
 							board.active_color = active_color; // TODO: ugly
 																// solution ! ;)
 							if (board.isCheckPosition())
 								break;
 							if (squ == new_square) { // if EVERYTHING is right,
 														// then add the move
 								moves.add(move);
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		if (type == PieceHelper.KNIGHT) {
 			squares = SquareHelper.getAllSquaresByKnightStep(square);
 			for (Integer new_square : squares) {
 				if (getFromBoard(new_square) == 0
 						|| PieceHelper.pieceColor(getFromBoard(new_square)) != active_color) {
 					move = new Move(square, new_square);
 					moves.add(move);
 				}
 			}
 		}
 
 		// remove invalid positions
 		// TODO do this in a more efficient way
 		Iterator<IMove> iter = moves.iterator();
 		while (iter.hasNext()) {
 			NaiveBoard temp_board = this.doMove(iter.next());
 			temp_board.active_color = active_color; // ugly solution…
 			if (temp_board.isCheckPosition()) {
 				iter.remove();
 			}
 		}
 
 		return moves;
 	}
 
 	@Override
 	public Set<IMove> getPossibleMovesTo(int square) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isCheckPosition() {
 		Set<Integer> temp_king_pos = getOccupiedSquaresByColorAndType(
 				active_color, PieceHelper.KING);
 		int king_pos = temp_king_pos.iterator().next();
 
 		// go in each direction
 		for (Direction direction : Direction.values()) {
 			List<Integer> line = SquareHelper.getAllSquaresInDirection(
 					king_pos, direction);
 			// go until…
 			int iter = 0;
 			for (int square : line) {
 				iter++;
 				// …some piece is found
 				int piece = getFromBoard(square);
 				if (piece != 0) {
 					if (PieceHelper.pieceColor(piece) == active_color) {
 						break;
 					} else {
 						if (PieceHelper.pieceType(piece) == PieceHelper.PAWN
 								&& iter == 1) {
 							if (((direction == Direction.NORTHEAST || direction == Direction.NORTHWEST) && active_color == PieceHelper.WHITE)
 									|| ((direction == Direction.SOUTHEAST || direction == Direction.SOUTHWEST) && active_color == PieceHelper.BLACK)) {
 								return true;
 							}
 						} else if (PieceHelper.pieceType(piece) == PieceHelper.ROOK) {
 							if (direction == Direction.EAST
 									|| direction == Direction.WEST
 									|| direction == Direction.NORTH
 									|| direction == Direction.SOUTH) {
 								return true;
 							}
 						} else if (PieceHelper.pieceType(piece) == PieceHelper.BISHOP) {
 							if (direction == Direction.NORTHEAST
 									|| direction == Direction.NORTHWEST
 									|| direction == Direction.SOUTHEAST
 									|| direction == Direction.SOUTHWEST) {
 								return true;
 							}
 						} else if (PieceHelper.pieceType(piece) == PieceHelper.QUEEN) {
 							return true;
 						} else if (PieceHelper.pieceType(piece) == PieceHelper.KING && iter==1){
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
 			int piece = getFromBoard(square);
 			if (piece != 0) {
 				if (PieceHelper.pieceColor(piece) != active_color
 						&& PieceHelper.pieceType(piece) == PieceHelper.KNIGHT) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean isMatePosition() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isPossibleMove(IMove move) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String toString() {
 		return toFEN();
 	}
 
 	@Override
 	public String toFEN() {
 		StringBuilder fen = new StringBuilder();
 
 		// piece placement
 		for (int row = 2; row < 10; row++) {
 
 			int counter = 0;
 
 			for (int column = 2; column < 10; column++) {
 				if (board[row][column] == 0) {
 					counter++;
 				} else {
 					if (counter != 0) {
 						fen.append(counter);
 						counter = 0;
 					}
 					fen.append(PieceHelper.toString(board[row][column]));
 				}
 				if (column == 9 && counter != 0) {
 					fen.append(counter);
 				}
 			}
 
 			if (row != 9) {
 				fen.append("/");
 			}
 		}
 		fen.append(" ");
 
 		// active color
 		if (active_color == PieceHelper.WHITE) {
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
 		fen.append(" ");
 
 		// halfmove clock
 		fen.append(half_move_clock);
 		fen.append(" ");
 
 		// fullmove clock
 		fen.append(full_move_clock);
 
 		return fen.toString();
 	}
 
 	@Override
 	public int getActiveColor() {
 		return active_color;
 	}
 
 	@Override
 	public int getHalfMoveClock() {
 		return half_move_clock;
 	}
 
 	@Override
 	public int getFullMoveClock() {
 		return full_move_clock;
 	}
 
 }
