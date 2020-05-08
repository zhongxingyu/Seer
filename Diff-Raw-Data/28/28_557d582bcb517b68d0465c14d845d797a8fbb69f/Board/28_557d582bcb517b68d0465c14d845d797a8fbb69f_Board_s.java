 package chessBackend;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Stack;
 import java.util.Vector;
 
 import chessAI.AI;
 import chessAI.AISettings;
 import chessIO.XMLParser;
 import chessPieces.*;
 
 public class Board {
 	private Piece[][] board;
 	private GameStatus boardStatus;
 	private ArrayList<Long> validMoves = new ArrayList<Long>(100);
 	private ArrayList<Piece>[] pieces = new ArrayList[2];
 	private Stack<Piece>[] piecesTaken = new Stack[2];
 	private int[] castleRights = new int[2];
 
 	private Piece[] kings = new Piece[2];
 	private int[][] rookStartCols = new int[2][2];
 	private int[] kingCols = new int[2];
 	private int[] materialRow = { 0, 7 };
 
 	private Side turn;
 	private RNGTable rngTable;
 	private Stack<Move> moveHistory;
 	private long hashCode;
 	private Stack<Long> hashCodeHistory;
 	private long[] nullMoveInfo = { 0, BitBoard.ALL_ONES, 0 };
 
 	// private long[] allPosBitBoard = { 0, 0 };
 	// private long[] pawnPosBitBoard = { 0, 0 };
 	// private long[] kingPosBitBoard = { 0, 0 };
 
 	private long[][] posBitBoard = new long[PieceID.values().length][2];
 	private long[] allPosBitBoard = new long[2];
 
 	public static void main(String[] args) {
 		Board board = BoardMaker.getStandardChessBoard();
 
 		ArrayList<Long> moves = board.generateValidMoves(true, 0, AI.noKillerMoves);
 
 		long m = moves.get(0);
 
 		int its = 1000000;
 
 		long t1 = System.currentTimeMillis();
 		for (int i = 0; i < its; i++) {
 			board.makeMove(m);
 			board.makeNullMove();
 			moves = board.generateValidMoves(true, 0, AI.noKillerMoves);
 			board.undoMove();
 		}
 
 		long A = System.currentTimeMillis() - t1;
 
 		t1 = System.currentTimeMillis();
 		for (int i = 0; i < its; i++) {
 			board.makeMove(m);
 			board.undoMove();
 		}
 
 		long B = System.currentTimeMillis() - t1;
 
 		System.out.println("A= " + A + ", B= " + B + " A/B=" + (double) A / (double) B);
 	}
 
 	public Board() {
 		this.board = new Piece[8][8];
 
 		this.pieces[Side.WHITE.ordinal()] = new ArrayList<Piece>();
 		this.pieces[Side.BLACK.ordinal()] = new ArrayList<Piece>();
 
 		this.piecesTaken[Side.WHITE.ordinal()] = new Stack<Piece>();
 		this.piecesTaken[Side.BLACK.ordinal()] = new Stack<Piece>();
 
 		this.moveHistory = new Stack<Move>();
 		this.hashCodeHistory = new Stack<Long>();
 		this.rngTable = RNGTable.getSingleton();
 		this.turn = Side.WHITE;
 		this.nullMoveInfo = new long[3];
 
 		kings[Side.BLACK.ordinal()] = new Piece(PieceID.KING, Side.BLACK, -1, -1, false);
 		kings[Side.WHITE.ordinal()] = new Piece(PieceID.KING, Side.WHITE, -1, -1, false);
 
 		placePiece(kings[Side.BLACK.ordinal()], 0, 0);
 		placePiece(kings[Side.WHITE.ordinal()], 7, 0);
 
 	}
 
 	public Board(ArrayList<Piece>[] pieces, Side turn, Stack<Move> moveHistory, int[][] rookStartCols, int[] kingCols) {
 		this.board = new Piece[8][8];
 		this.pieces[Side.WHITE.ordinal()] = new ArrayList<Piece>(pieces[Side.WHITE.ordinal()].size());
 		this.pieces[Side.BLACK.ordinal()] = new ArrayList<Piece>(pieces[Side.BLACK.ordinal()].size());
 
 		this.piecesTaken[Side.WHITE.ordinal()] = new Stack<Piece>();
 		this.piecesTaken[Side.BLACK.ordinal()] = new Stack<Piece>();
 
 		this.moveHistory = new Stack<Move>();
 		this.hashCodeHistory = new Stack<Long>();
 		this.rngTable = RNGTable.getSingleton();
 		this.turn = turn;
 		this.nullMoveInfo = new long[3];
 
 		long[][] posBitBoard = new long[PieceID.values().length][2];
 		// long[] pawnPosBitboard = { 0, 0 };
 		// long[] kingPosBitboard = { 0, 0 };
 
 		int[] pawnRow = new int[2];
 		pawnRow[Side.BLACK.ordinal()] = 1;
 		pawnRow[Side.WHITE.ordinal()] = 6;
 
 		Piece temp;
 
 		for (int i = 0; i < pieces.length; i++) {
 
 			for (int p = 0; p < pieces[i].size(); p++) {
 				temp = pieces[i].get(p).getCopy();
 
 				this.pieces[i].add(temp);
 
 				board[temp.getRow()][temp.getCol()] = temp;
 
 				posBitBoard[temp.getPieceID().ordinal()][i] |= temp.getBit();
 
 				if (temp.getPieceID() == PieceID.PAWN) {
 
 					if (temp.getRow() != pawnRow[i]) {
 						temp.setMoved(true);
 					}
 				}
 
 				if (temp.getPieceID() == PieceID.KING) {
 
 					kings[i] = temp;
 
 					if (temp.getRow() != materialRow[i]) {
 						temp.setMoved(true);
 					}
 				}
 			}
 
 		}
 
 		this.posBitBoard = posBitBoard;
 
 		for (int i = 0; i < PieceID.values().length; i++) {
 			this.allPosBitBoard[0] |= posBitBoard[i][0];
 			this.allPosBitBoard[1] |= posBitBoard[i][1];
 		}
 
 		this.hashCode = generateHashCode();
 
 		if (moveHistory.size() > 0) {
 			Long move;
 			Side moveSide;
 			if (moveHistory.size() % 2 == 0) {
 				moveSide = turn;
 			} else {
 				moveSide = turn.otherSide();
 			}
 
 			for (int i = 0; i < moveHistory.size(); i++) {
 				move = moveHistory.elementAt(i).getMoveLong();
 				this.moveHistory.push(new Move(move));
 				if (Move.hasPieceTaken(move)) {
 					piecesTaken[moveSide.otherSide().ordinal()].push(new Piece(Move.getPieceTakenID(move), moveSide.otherSide(), Move.getPieceTakenRow(move), Move
 							.getPieceTakenCol(move), Move.getPieceTakenHasMoved(move)));
 				}
 
 				moveSide = moveSide.otherSide();
 			}
 
 		} else {
 			loadPiecesTaken();
 		}
 
 		if (kingCols == null || rookStartCols == null) {
 			initializeCastleSetup();
 		} else {
 			this.kingCols = kingCols;
 			this.rookStartCols = rookStartCols;
 		}
 
 		// this.castleRights = castleRights;
 	}
 
 	public boolean makeMove(long move) {
 
 		int fromRow = Move.getFromRow(move);
 		int fromCol = Move.getFromCol(move);
 		int toRow = Move.getToRow(move);
 		int toCol = Move.getToCol(move);
 		MoveNote note = Move.getNote(move);
 
 		if (board[fromRow][fromCol].getSide() != turn) {
 			System.out.println("Problem with player ref");
 			return false;
 		}
 
 		// save off hashCode
 		hashCodeHistory.push(new Long(hashCode));
 
 		// remove previous castle options
 		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
 				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));
 
 		// remove taken piece first
 		if (Move.hasPieceTaken(move)) {
 
 			Piece pieceTaken = board[Move.getPieceTakenRow(move)][Move.getPieceTakenCol(move)];
 
 			// remove pieceTaken from vectors
 			pieces[turn.otherSide().ordinal()].remove(pieceTaken);
 			piecesTaken[turn.otherSide().ordinal()].push(pieceTaken);
 
 			// // remove bit position from appropriate side
 			// allPosBitBoard[pieceTaken.getSide().ordinal()] ^=
 			// pieceTaken.getBit();
 			//
 			// if (pieceTaken.getPieceID() == PieceID.PAWN) {
 			// pawnPosBitBoard[pieceTaken.getSide().ordinal()] ^=
 			// pieceTaken.getBit();
 			//
 			// }
 			//
 			// if (pieceTaken.getPieceID() == PieceID.KING) {
 			// kingPosBitBoard[pieceTaken.getSide().ordinal()] ^=
 			// pieceTaken.getBit();
 			// }
 
 			posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
 			allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
 
 			// remove ref to piecetaken on board
 			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;
 
 			// remove old hash from piece that was taken, if any
 			hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getSide(), pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());
 
 		}
 
 		if (note == MoveNote.CASTLE_NEAR || note == MoveNote.CASTLE_FAR) {
 
 			Piece king = kings[turn.ordinal()];
 			Piece rook;
 
 			if (note == MoveNote.CASTLE_NEAR) {
 				rook = board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][1]];
 
 				movePiece(king, materialRow[turn.ordinal()], 6, MoveNote.NONE);
 				movePiece(rook, materialRow[turn.ordinal()], 5, MoveNote.NONE);
 
 				board[materialRow[turn.ordinal()]][6] = king;
 				board[materialRow[turn.ordinal()]][5] = rook;
 
 				castleRights[turn.ordinal()] = 2;
 			} else {
 				rook = board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][0]];
 
 				movePiece(king, materialRow[turn.ordinal()], 2, MoveNote.NONE);
 				movePiece(rook, materialRow[turn.ordinal()], 3, MoveNote.NONE);
 
 				board[materialRow[turn.ordinal()]][2] = king;
 				board[materialRow[turn.ordinal()]][3] = rook;
 
 				castleRights[turn.ordinal()] = 1;
 			}
 
 		} else {
 
 			movePiece(board[fromRow][fromCol], toRow, toCol, note);
 
 		}
 
 		// if last move made is pawn leap, remove en passant file num
 		if (getLastMoveMade() != 0) {
 			if (Move.getNote(getLastMoveMade()) == MoveNote.PAWN_LEAP) {
 				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
 			}
 		}
 
 		// if new move is pawn leap, add en passant file num
 		if (note == MoveNote.PAWN_LEAP) {
 			hashCode ^= rngTable.getEnPassantFile(Move.getToCol(move));
 		}
 
 		// add new castle options
 		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
 				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));
 
 		// either remove black and add white or reverse. Same operation.
 		hashCode ^= rngTable.getBlackToMoveRandom();
 
 		// show that this move is now the last move made
 		moveHistory.push(new Move(move));
 
 		// move was made, next player's turn
 		turn = turn.otherSide();
 
 		// verifyBitBoards();
 
 		return true;
 
 	}
 
 	private void movePiece(Piece pieceMoving, int toRow, int toCol, MoveNote note) {
 
 		long bitMove = BitBoard.getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ BitBoard.getMask(toRow, toCol);
 
 		// remove bit position from where piece was and add where it is now
 		posBitBoard[pieceMoving.getPieceID().ordinal()][pieceMoving.getSide().ordinal()] ^= bitMove;
 		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
 
 		// remove old hash from where piece was
 		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), pieceMoving.getRow(), pieceMoving.getCol());
 
 		// remove pieces old position
 		board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
 		// update board to reflect piece's new position
 		board[toRow][toCol] = pieceMoving;
 
 		// tell piece its new position
 		pieceMoving.setPos(toRow, toCol);
 		pieceMoving.setMoved(true);
 
 		if (note == MoveNote.NEW_QUEEN) {
 			pieceMoving.setPieceID(PieceID.QUEEN);
 			posBitBoard[PieceID.PAWN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
 			posBitBoard[PieceID.QUEEN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
 		}
 
 		// if (pieceMoving == getKing(turn)) {
 		// setCastleRights(turn, 0);
 		// }
 
 		// add hash of piece at new location
 		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), toRow, toCol);
 	}
 
 	public long undoMove() {
 
 		// if no there is no last move then undoMove is impossible
 		if (moveHistory.empty()) {
 			// System.out.println("Can not undo move");
 			return 0;
 		}
 
 		// retrieve last move made
 		long lastMove = getLastMoveMade();
 
 		int fromRow = Move.getFromRow(lastMove);
 		int fromCol = Move.getFromCol(lastMove);
 		int toRow = Move.getToRow(lastMove);
 		int toCol = Move.getToCol(lastMove);
 		MoveNote note = Move.getNote(lastMove);
 
 		// last move made was made by previous player, which is also the next
 		// player
 		turn = turn.otherSide();
 
 		if (note == MoveNote.CASTLE_NEAR || note == MoveNote.CASTLE_FAR) {
 
 			Piece king = kings[turn.ordinal()];
 			Piece rook;
 
 			if (note == MoveNote.CASTLE_FAR) {
 				rook = board[materialRow[turn.ordinal()]][3];
 
 				undoMovePiece(king, materialRow[turn.ordinal()], kingCols[turn.ordinal()], MoveNote.NONE, false);
 				undoMovePiece(rook, materialRow[turn.ordinal()], rookStartCols[turn.ordinal()][0], MoveNote.NONE, false);
 
 				board[materialRow[turn.ordinal()]][kingCols[turn.ordinal()]] = king;
 				board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][0]] = rook;
 
 			} else {
 
 				rook = board[materialRow[turn.ordinal()]][5];
 
 				undoMovePiece(king, materialRow[turn.ordinal()], kingCols[turn.ordinal()], MoveNote.NONE, false);
 				undoMovePiece(rook, materialRow[turn.ordinal()], rookStartCols[turn.ordinal()][1], MoveNote.NONE, false);
 
 				board[materialRow[turn.ordinal()]][kingCols[turn.ordinal()]] = king;
 				board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][1]] = rook;
 
 			}
 
 			castleRights[turn.ordinal()] = 0;
 
 		} else {
 			undoMovePiece(board[toRow][toCol], fromRow, fromCol, note, Move.hadMoved(lastMove));
 		}
 
 		if (Move.hasPieceTaken(lastMove)) {
 
 			// add taken piece back to vectors and board
 			Piece pieceTaken = piecesTaken[turn.otherSide().ordinal()].pop();
 
 			pieces[turn.otherSide().ordinal()].add(pieceTaken);
 
 			// add piece taken to position bit board
 			posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();
 			allPosBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();
 
 			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;
 
 		}
 
 		// move was undone so show move made before that as the last move made
 		moveHistory.pop();
 
 		if (hashCodeHistory.empty()) {
 			// if no hashCode was saved then generate it the hard way
 			hashCode = generateHashCode();
 		} else {
 			// retrieve what the hashCode was before move was made
 			hashCode = hashCodeHistory.pop();
 		}
 
 		// verifyBitBoards();
 
 		return lastMove;
 
 	}
 
 	private void undoMovePiece(Piece pieceMoving, int fromRow, int fromCol, MoveNote note, boolean hadMoved) {
 
 		long bitMove = BitBoard.getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ BitBoard.getMask(fromRow, fromCol);
 
 		// remove bit position from where piece was and add where it is now
 		posBitBoard[pieceMoving.getPieceID().ordinal()][pieceMoving.getSide().ordinal()] ^= bitMove;
 		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
 
 		// remove old position
 		board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
 		// put piece in old position
 		board[fromRow][fromCol] = pieceMoving;
 
 		// tell piece where it was
 		pieceMoving.setPos(fromRow, fromCol);
 
 		// show whether piece had moved before this move was made
 		pieceMoving.setMoved(hadMoved);
 
 		if (note == MoveNote.NEW_QUEEN) {
 			pieceMoving.setPieceID(PieceID.PAWN);
 			posBitBoard[PieceID.PAWN.ordinal()][pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();
 			posBitBoard[PieceID.QUEEN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
 		}
 
 	}
 
 	private void verifyBitBoards() {
 
 		Piece piece;
 
 		long[][] allBitBoard = new long[PieceID.values().length][2];
 
 		for (int i = 0; i < PieceID.values().length; i++) {
 			allBitBoard[i][0] = posBitBoard[i][0];
 			allBitBoard[i][1] = posBitBoard[i][1];
 		}
 
 		for (int r = 0; r < 8; r++) {
 			for (int c = 0; c < 8; c++) {
 				piece = board[r][c];
 
 				if (piece != null) {
 					allBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);
 				}
 
 			}
 		}
 
 		for (int i = 0; i < PieceID.values().length; i++) {
 			if (allBitBoard[i][0] != 0) {
 				System.out.println("BitBoard Problem!!!");
 			}
 
 			if (allBitBoard[i][1] != 0) {
 				System.out.println("BitBoard Problem!!!");
 			}
 		}
 
 	}
 
 	public boolean canUndo() {
 		return (moveHistory.size() != 0);
 	}
 
 	public ArrayList<Long> generateValidMoves() {
 		return generateValidMoves(false, 0, AI.noKillerMoves);
 	}
 
 	public ArrayList<Long> generateValidMoves(boolean sort, long hashMove, long[] killerMoves) {
 
 		// find in check details. i.e. left and right castle info
 		// makeNullMove();
 
 		// System.out.println("Not safe areas");
 		// BitBoard.printBitBoard(nullMoveInfo[0]);
 		//
 		// System.out.println("in check vector");
 		// BitBoard.printBitBoard(nullMoveInfo[1]);
 
 		// ArrayList<Long> validMoves = new ArrayList<Long>(50);
 		validMoves.clear();
 
 		if (!hasSufficientMaterial() || drawByThreeRule()) {
 			setBoardStatus(GameStatus.DRAW);
 			return validMoves;
 		}
 
 		int prevMovesSize = 0;
 
 		Long move;
 		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {
 
 			pieces[turn.ordinal()].get(p).generateValidMoves(this, nullMoveInfo, allPosBitBoard, validMoves);
 
 			for (int m = prevMovesSize; m < validMoves.size(); m++) {
 				move = validMoves.get(m);
 
 				move = Move.setHadMoved(move, pieces[turn.ordinal()].get(p).hasMoved());
 
 				if (move == hashMove) {
 					move = Move.setValue(move, 10000);
 					validMoves.set(m, move);
 					continue;
 				}
 
 				for (int k = 0; k < killerMoves.length; k++) {
 					if (move == killerMoves[k]) {
 						move = Move.setValue(move, 9999 - k);
 						break;
 					}
 				}
 
 				validMoves.set(m, move);
 
 			}
 
 			prevMovesSize = validMoves.size();
 
 		}
 
 		if (sort) {
 			Collections.sort(validMoves, Collections.reverseOrder());
 		}
 
 		if (validMoves.size() == 0) {
 			if (isInCheck()) {
 				setBoardStatus(GameStatus.CHECKMATE);
 			} else {
 				setBoardStatus(GameStatus.STALEMATE);
 			}
 		}
 
 		return validMoves;
 	}
 
 	public boolean isAttacked(int r, int c) {
 		long allNot = ~(allPosBitBoard[1] | allPosBitBoard[0]);
 
 		// long kingPos = posBitBoard[PieceID.KING][]
 
 		for (int i = 0; i < 8; i++) {
 
 		}
 
 		return false;
 	}
 
 	private void addSortValidMove(ArrayList<Long> validMoves, Long move) {
 		int moveValue = Move.getValue(move);
 		for (int m = 0; m < validMoves.size(); m++) {
 			if (moveValue >= Move.getValue(validMoves.get(m))) {
 				validMoves.add(m, move);
 				return;
 			}
 		}
 
 		validMoves.add(move);
 	}
 
 	public long[] getAllPosBitBoard() {
 		return allPosBitBoard;
 	}
 
 	public long[][] getPosBitBoard() {
 		return posBitBoard;
 	}
 
 	public long[] makeNullMove() {
 		// long nullMoveAttacks = 0;
 		// long inCheckVector = BitBoard.ALL_ONES;
 		// long bitAttackCompliment = 0;
 		//
 		// nullMoveInfo[0] = nullMoveAttacks;
 		// nullMoveInfo[1] = inCheckVector;
 		// nullMoveInfo[2] = bitAttackCompliment;
 
 		// recalculating check info
 		clearBoardStatus();
 
 		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {
 			pieces[turn.ordinal()].get(p).clearBlocking();
 		}
 
 		nullMoveInfo[0] = BitBoard.getPawnAttacks(posBitBoard[PieceID.PAWN.ordinal()][turn.otherSide().ordinal()], turn.otherSide());
 		nullMoveInfo[0] |= BitBoard.getKnightAttacks(posBitBoard[PieceID.KNIGHT.ordinal()][turn.otherSide().ordinal()]);
 		nullMoveInfo[0] |= BitBoard.getKingAttacks(posBitBoard[PieceID.KING.ordinal()][turn.otherSide().ordinal()]);
 
 		nullMoveInfo[1] = BitBoard.getPawnAttacks(posBitBoard[PieceID.KING.ordinal()][turn.ordinal()], turn) & posBitBoard[PieceID.PAWN.ordinal()][turn.otherSide().ordinal()];
 
 		nullMoveInfo[1] |= BitBoard.getKnightAttacks(posBitBoard[PieceID.KING.ordinal()][turn.ordinal()]) & posBitBoard[PieceID.KNIGHT.ordinal()][turn.otherSide().ordinal()];
 
 		if (nullMoveInfo[1] == 0) {
 			nullMoveInfo[1] = BitBoard.ALL_ONES;
 		}
 
 		nullMoveInfo[2] = 0;
 
 		long updown = ~(allPosBitBoard[0] | allPosBitBoard[1]);
 		long left = 0xFEFEFEFEFEFEFEFEL & updown;
 		long right = 0x7F7F7F7F7F7F7F7FL & updown;
 
 		for (int p = 0; p < pieces[turn.otherSide().ordinal()].size(); p++) {
 
 			pieces[turn.otherSide().ordinal()].get(p).getNullMoveInfo(this, nullMoveInfo, updown, left, right, posBitBoard[PieceID.KING.ordinal()][turn.ordinal()],
 					King.getKingCheckVectors(posBitBoard[PieceID.KING.ordinal()][turn.ordinal()], updown, left, right), allPosBitBoard[turn.ordinal()]);
 
 		}
 
 		// for (int i = 0; i < 3; i++) {
 		// System.out.println(BitBoard.printBitBoard(nullMoveInfo[i]));
 		// }
 
 		if ((kings[turn.ordinal()].getBit() & nullMoveInfo[0]) != 0) {
 			setBoardStatus(GameStatus.CHECK);
 		}
 
 		return nullMoveInfo;
 
 	}
 
 	public boolean isVoi(Move[] voi) {
 
 		int historySize = moveHistory.size();
 
 		if (historySize < voi.length) {
 			return false;
 		}
 
 		for (int i = 0; i < voi.length; i++) {
 			if (!voi[i].equals(moveHistory.elementAt(historySize - i - 1))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public PositionStatus checkPiece(int row, int col, Side player) {
 
 		if (((row | col) & (~0x7)) != 0) {
 			return PositionStatus.OFF_BOARD;
 		}
 
 		if (board[row][col] != null) {
 			if (board[row][col].getSide() == player)
 				return PositionStatus.FRIEND;
 			else
 				return PositionStatus.ENEMY;
 		} else {
 			return PositionStatus.NO_PIECE;
 		}
 
 	}
 
 	public long getLastMoveMade() {
 		if (!moveHistory.empty()) {
 			return moveHistory.peek().getMoveLong();
 		} else {
 			return 0;
 		}
 	}
 
 	public Vector<Move> getMoveHistory() {
 		return moveHistory;
 	}
 
 	public int getPieceValue(int row, int col) {
 		return Values.getPieceValue(board[row][col].getPieceID()) + getOpeningPositionValue(board[row][col]);
 	}
 
 	public int getOpeningPositionValue(Piece piece) {
 
 		if (piece == null) {
 			return 0;
 		}
 
 		int value;
 		Side player = piece.getSide();
 		int row = piece.getRow();
 		int col = piece.getCol();
 
 		switch (piece.getPieceID()) {
 		case KNIGHT:
 			value = PositionBonus.getKnightPositionBonus(row, col, player);
 			break;
 		case PAWN:
 			value = PositionBonus.getPawnPositionBonus(row, col, player);
 			break;
 		case BISHOP:
 			value = 0;
 			break;
 		case KING:
 			value = 0;
 			value = PositionBonus.getKingOpeningPositionBonus(row, col, player);
 			break;
 		case QUEEN:
 			value = 0;
 			break;
 		case ROOK:
 			value = PositionBonus.getRookBonus(row, col);
 			break;
 		default:
 			value = 0;
 			System.out.println("Error: invalid piece value request!");
 		}
 
 		return value;
 
 	}
 
 	public int getEndGamePositionValue(Piece piece) {
 		int value;
 		Side player = piece.getSide();
 		int row = piece.getRow();
 		int col = piece.getCol();
 
 		switch (piece.getPieceID()) {
 		case KNIGHT:
 			value = PositionBonus.getKnightPositionBonus(row, col, player);
 			break;
 		case PAWN:
 			value = PositionBonus.getPawnPositionBonus(row, col, player);
 			break;
 		case BISHOP:
 			value = 50;
 			break;
 		case KING:
 			value = PositionBonus.getKingEndGamePositionBonus(row, col, player);
 			break;
 		case QUEEN:
 			value = 100;
 			break;
 		case ROOK:
 			value = PositionBonus.getRookBonus(row, col);
 			break;
 		default:
 			value = 0;
 			System.out.println("Error: invalid piece value request!");
 		}
 
 		return value;
 
 	}
 
 	public int openingPositionScore(Side side) {
 		int score = 0;
 
 		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
 			score += getOpeningPositionValue(pieces[side.ordinal()].get(i));
 		}
 
 		return score;
 	}
 
 	public int endGamePositionScore(Side side) {
 		int score = 0;
 
 		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
 			score += getEndGamePositionValue(pieces[side.ordinal()].get(i));
 		}
 
 		return score;
 	}
 
 	public int materialScore(Side side) {
 		int score = 0;
 
 		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
 			score += Values.getPieceValue(pieces[side.ordinal()].get(i).getPieceID());
 		}
 
 		return score;
 	}
 
 	public int castleScore(Side side) {
 		int score = 0;
 		int castleRights = this.castleRights[side.ordinal()];
 
 		if (castleRights != 0) {
 			if (castleRights == 1) {
 				score += Values.FAR_CASTLE_VALUE;
 			} else {
 				score += Values.NEAR_CASTLE_VALUE;
 			}
 		} else {
 			if (!kingHasMoved(side)) {
 				if (farRookHasMoved(side)) {
 					score -= Values.FAR_CASTLE_ABILITY_LOST_VALUE;
 				}
 
 				if (nearRookHasMoved(side)) {
 					score -= Values.NEAR_CASTLE_ABILITY_LOST_VALUE;
 				}
 			} else {
 				score -= Values.CASTLE_ABILITY_LOST_VALUE;
 			}
 		}
 
 		return score;
 	}
 
 	public int pawnStructureScore(Side side, int phase) {
 
 		long pawns = posBitBoard[PieceID.PAWN.ordinal()][side.ordinal()];
 		long otherPawns = posBitBoard[PieceID.PAWN.ordinal()][side.otherSide().ordinal()];
 
 		int occupiedCol = 0;
 		int passedPawns = 0;
 		for (int c = 0; c < 8; c++) {
 			if ((BitBoard.getColMask(c) & pawns) != 0) {
 				occupiedCol++;
 				if ((BitBoard.getColMask(c) & otherPawns) == 0) {
 					passedPawns++;
 				}
 			}
 		}
 
 		int doubledPawns = Long.bitCount(pawns) - occupiedCol;
 
 		return BitBoard.getBackedPawns(pawns) * Values.BACKED_PAWN_BONUS + doubledPawns * Values.DOUBLED_PAWN_BONUS + ((passedPawns * Values.PASSED_PAWN_BONUS * phase) / 256);
 	}
 
 	public int calcGamePhase() {
 
 		int phase = Values.TOTALPHASE;
 
 		for (int i = 0; i < 2; i++) {
 			for (int p = 0; p < pieces[i].size(); p++) {
 				phase -= Values.PIECE_PHASE_VAL[pieces[i].get(p).getPieceID().ordinal()];
 			}
 		}
 
 		phase = (phase * 256 + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;
 
 		return phase;
 	}
 
 	public int staticScore() {
 		int ptDiff = 0;
 
 		int phase = calcGamePhase();
 
 		int myPawnScore = pawnStructureScore(turn, phase);
 		int yourPawnScore = pawnStructureScore(turn.otherSide(), phase);
 
 		int openingMyScore = castleScore(turn) + openingPositionScore(turn);
 		int openingYourScore = castleScore(turn.otherSide()) + openingPositionScore(turn.otherSide());
 
 		int endGameMyScore = endGamePositionScore(turn);
 		int endGameYourScore = endGamePositionScore(turn.otherSide());
 
 		int myScore = (openingMyScore * (256 - phase) + endGameMyScore * phase) / 256 + materialScore(turn) + myPawnScore;
 		int yourScore = (openingYourScore * (256 - phase) + endGameYourScore * phase) / 256 + materialScore(turn.otherSide()) + yourPawnScore;
 
 		ptDiff = myScore - yourScore;
 
 		return ptDiff;
 	}
 
 	public Piece getPiece(int row, int col) {
 		return board[row][col];
 	}
 
 	public PieceID getPieceID(int row, int col) {
 		if (board[row][col] != null) {
 			return board[row][col].getPieceID();
 		} else {
 			return null;
 		}
 
 	}
 
 	public Side getPieceSide(int row, int col) {
 		if (board[row][col] != null) {
 			return board[row][col].getSide();
 		} else {
 			System.out.println("Error: requested player on null piece");
 			return null;
 		}
 
 	}
 
 	public Side getTurn() {
 		return turn;
 	}
 
 	public void setTurn(Side turn) {
 		this.turn = turn;
 	}
 
 	public boolean hasPiece(int row, int col) {
 		if ((row & ~0x7) != 0 || (col & ~7) != 0) {
 			return false;
 		} else {
 			return (board[row][col] != null);
 		}
 	}
 
 	public boolean hasMoved(int row, int col) {
 		if (hasPiece(row, col)) {
 			return board[row][col].hasMoved();
 		} else {
 			return true;
 		}
 	}
 
 	public Stack<Piece> getPiecesTakenFor(Side player) {
 		return piecesTaken[player.ordinal()];
 	}
 
 	private void initializeCastleSetup() {
 
 		int[][] rookCols = { { -1, -1 }, { -1, -1 } };
 		this.rookStartCols = rookCols;
 
		int sideOrd;
 		for (int s = 0; s < 2; s++) {
			sideOrd = Side.values()[s].ordinal();
			for (int c = kings[sideOrd].getCol() - 1; c >= 0; c--) {
 
				if (board[materialRow[sideOrd]][c] != null) {
					if (board[materialRow[sideOrd]][c].getPieceID() == PieceID.ROOK) {
						rookCols[sideOrd][0] = c;
 						break;
 					}
 				}
 			}
 
			for (int c = kings[sideOrd].getCol() + 1; c < 8; c++) {
				if (board[materialRow[sideOrd]][c] != null) {
					if (board[materialRow[sideOrd]][c].getPieceID() == PieceID.ROOK) {
						rookCols[sideOrd][1] = c;
 						break;
 					}
 				}
 			}
 		}
 
 		kingCols[Side.BLACK.ordinal()] = kings[Side.BLACK.ordinal()].getCol();
 		kingCols[Side.WHITE.ordinal()] = kings[Side.WHITE.ordinal()].getCol();
 
 	}
 
 	public boolean placePiece(Piece piece, int toRow, int toCol) {
 
 		if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
 			if (board[toRow][toCol] != null) {
 				if (board[toRow][toCol].getPieceID() == PieceID.KING) {
 					return false;
 				}
 			}
 		}
 
 		if (piece.getPieceID() == PieceID.KING) {
 			if (toRow < 0 || toCol < 0) {
 				return false;
 			} else {
 				kings[piece.getSide().ordinal()] = piece;
 			}
 		}
 
 		piece.setMoved(false);
 
 		if (piece.getRow() >= 0) {
 			// remove where piece was if it was on board
 			posBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] ^= piece.getBit();
 			allPosBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
 			board[piece.getRow()][piece.getCol()] = null;
 
 		} else {
 
 			pieces[piece.getSide().ordinal()].add(piece);
 		}
 
 		if (toRow >= 0) {
 			// remove where piece taken was
 			if (board[toRow][toCol] != null) {
 
 				Piece pieceTaken = board[toRow][toCol];
 
 				// remove bit position of piece taken
 				posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
 				allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
 
 				// remove ref to piece taken
 				pieces[pieceTaken.getSide().ordinal()].remove(pieceTaken);
 			}
 
 			// tell piece where it is now
 			piece.setPos(toRow, toCol);
 
 			// reflect new piece in position bitboard
 			posBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] |= piece.getBit();
 			allPosBitBoard[piece.getSide().ordinal()] |= piece.getBit();
 
 			// update board ref to show piece there
 			board[toRow][toCol] = piece;
 		} else {
 			// piece is being taken off the board. Remove
 			if (piece.getPieceID() != PieceID.KING) {
 				pieces[piece.getSide().ordinal()].remove(piece);
 			}
 		}
 
 		// basically start over with new board
 		this.moveHistory.clear();
 		this.hashCodeHistory.clear();
 
 		this.hashCode = generateHashCode();
 
 		initializeCastleSetup();
 
 		verifyBitBoards();
 
 		return true;
 
 	}
 
 	public boolean isInCheck() {
 		return (boardStatus == GameStatus.CHECK);
 	}
 
 	public boolean isInCheckMate() {
 		return (boardStatus == GameStatus.CHECKMATE);
 	}
 
 	public boolean isInStaleMate() {
 		return (boardStatus == GameStatus.STALEMATE);
 	}
 
 	public boolean isTimeUp() {
 		return (boardStatus == GameStatus.TIMES_UP);
 	}
 
 	public boolean isDraw() {
 		return (boardStatus == GameStatus.DRAW);
 	}
 
 	public boolean isInvalid() {
 		return (boardStatus == GameStatus.INVALID);
 	}
 
 	public boolean isGameOver() {
 		return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw() || isInvalid());
 	}
 
 	public void clearBoardStatus() {
 		boardStatus = GameStatus.IN_PLAY;
 	}
 
 	public GameStatus getBoardStatus() {
 		return boardStatus;
 	}
 
 	public void setBoardStatus(GameStatus boardStatus) {
 		this.boardStatus = boardStatus;
 	}
 
 	public boolean farRookHasMoved(Side player) {
 		return hasMoved(materialRow[player.ordinal()], rookStartCols[player.ordinal()][0]);
 	}
 
 	public boolean nearRookHasMoved(Side player) {
 		return hasMoved(materialRow[player.ordinal()], rookStartCols[player.ordinal()][1]);
 	}
 
 	public int getRookStartingCol(Side side, int near) {
 		return rookStartCols[side.ordinal()][near];
 	}
 
 	public int getKingStartingCol(Side side) {
 		return kingCols[side.ordinal()];
 	}
 
 	public boolean kingHasMoved(Side player) {
 		return kings[player.ordinal()].hasMoved();
 	}
 
 	public long farCastleMask(Side player) {
 		return BitBoard.getCastleMask(Math.min(rookStartCols[player.ordinal()][0], 2), kings[player.ordinal()].getCol(), player);
 	}
 
 	public long nearCastleMask(Side player) {
 		return BitBoard.getCastleMask(kings[player.ordinal()].getCol(), Math.max(rookStartCols[player.ordinal()][1], 6), player);
 	}
 
 	public Board getCopy() {
 		return new Board(pieces, this.turn, moveHistory, rookStartCols, kingCols);
 	}
 
 	public String toString() {
 		String stringBoard = "";
 		int pieceDetails = 0;
 		Piece p;
 
 		for (int row = 0; row < 8; row++) {
 			for (int col = 0; col < 8; col++) {
 				if (board[row][col] != null) {
 
 					p = board[row][col];
 
 					if (p.hasMoved() && (p.getPieceID() == PieceID.PAWN || p.getPieceID() == PieceID.KING || p.getPieceID() == PieceID.ROOK)) {
 						pieceDetails |= 1;
 					}
 
 					if (p.getPieceID() == PieceID.ROOK && kingHasMoved(p.getSide())) {
 						pieceDetails |= 1;
 					}
 
 					if (p.getPieceID() == PieceID.KING && nearRookHasMoved(p.getSide()) && farRookHasMoved(p.getSide())) {
 						pieceDetails |= 1;
 					}
 
 					long lastMove = getLastMoveMade();
 
 					if (getLastMoveMade() != 0) {
 						if (Move.getToRow(lastMove) == row && Move.getToCol(lastMove) == col && Move.getNote(lastMove) == MoveNote.PAWN_LEAP) {
 							pieceDetails |= 2;
 						}
 					}
 
 					stringBoard += board[row][col].toString() + pieceDetails + ",";
 					pieceDetails = 0;
 
 				} else {
 					stringBoard += "__,";
 				}
 
 			}
 			stringBoard += "\n";
 		}
 
 		return stringBoard;
 	}
 
 	public String toXML(boolean includeHistory) {
 		String xmlBoard = "<board>\n";
 
 		if (includeHistory) {
 
 			Stack<Move> movesToRedo = new Stack<Move>();
 			long m;
 			while ((m = undoMove()) != 0) {
 				movesToRedo.push(new Move(m));
 			}
 
 			xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
 			xmlBoard += "<turn>" + turn.toString() + "</turn>\n";
 
 			while (!movesToRedo.isEmpty()) {
 				makeMove(movesToRedo.pop().getMoveLong());
 			}
 
 			for (int i = 0; i < moveHistory.size(); i++) {
 				xmlBoard += Move.toXML(moveHistory.elementAt(i).getMoveLong());
 			}
 
 		} else {
 			xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
 			xmlBoard += "<turn>" + turn.toString() + "</turn>\n";
 		}
 
 		xmlBoard += "</board>";
 		return xmlBoard;
 	}
 
 	public long generateHashCode() {
 		long hashCode = 0;
 
 		if (turn == Side.BLACK) {
 			hashCode = rngTable.getBlackToMoveRandom();
 		}
 
 		Piece p;
 		for (int r = 0; r < 8; r++) {
 			for (int c = 0; c < 8; c++) {
 				p = board[r][c];
 				if (p != null) {
 					hashCode ^= rngTable.getPiecePerSquareRandom(p.getSide(), p.getPieceID(), r, c);
 				}
 			}
 		}
 
 		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
 				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));
 
 		if (getLastMoveMade() != 0) {
 			if (Move.getNote(getLastMoveMade()) == MoveNote.PAWN_LEAP) {
 				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
 			}
 		}
 
 		return hashCode;
 	}
 
 	public int getHashIndex() {
 		return (int) (hashCode & AISettings.hashIndexMask);
 	}
 
 	public long getHashCode() {
 		return hashCode;
 	}
 
 	public static Long getHashCode(String xmlBoard) {
 		Board board = XMLParser.XMLToBoard(xmlBoard);
 
 		if (board != null) {
 			return board.getHashCode();
 		} else {
 			return null;
 		}
 	}
 
 	public boolean drawByThreeRule() {
 
 		int count = 0;
 		int fiftyMove = 0;
 
 		for (int i = hashCodeHistory.size() - 1; i >= 0; i--) {
 
 			if (fiftyMove >= 101) {
 				return true;
 			}
 
 			if (hashCode == hashCodeHistory.elementAt(i)) {
 				count++;
 			}
 
 			if (count > 2) {
 				return true;
 			}
 
 			if (moveHistory.elementAt(i).hasPieceTaken()) {
 				return false;
 			}
 
 			fiftyMove++;
 		}
 
 		return false;
 	}
 
 	private boolean hasSufficientMaterial() {
 
 		boolean sufficient = true;
 
 		if (pieces[0].size() <= 2 && pieces[1].size() <= 2) {
 
 			sufficient = false;
 
 			for (int i = 0; i < pieces.length; i++) {
 				for (int p = 0; p < pieces[i].size(); p++) {
 					if ((pieces[i].get(p).getPieceID() == PieceID.PAWN) || (pieces[i].get(p).getPieceID() == PieceID.QUEEN) || (pieces[i].get(p).getPieceID() == PieceID.ROOK)) {
 						sufficient = true;
 					}
 				}
 			}
 
 		}
 
 		return sufficient;
 	}
 
 	private void loadPiecesTaken() {
 
 		for (int i = 0; i < pieces.length; i++) {
 
 			piecesTaken[i] = getFullPieceSet(Side.values()[i]);
 
 			Piece piecePresent;
 			for (int p = 0; p < pieces[i].size(); p++) {
 				piecePresent = pieces[i].get(p);
 
 				for (int t = 0; t < piecesTaken[i].size(); t++) {
 					if (piecesTaken[i].elementAt(t).getPieceID() == piecePresent.getPieceID()) {
 						piecesTaken[i].remove(t);
 						break;
 					}
 				}
 			}
 		}
 
 	}
 
 	public long resolveAlgebraicNotation(String notation) {
 
 		this.makeNullMove();
 		ArrayList<Long> moves = this.generateValidMoves();
 
 		// Board board2 = this.getCopy();
 		// board2.makeNullMove(false);
 		// ArrayList<Long> moves2 = board2.generateValidMoves();
 		//
 		// if (moves.size() == moves2.size()) {
 		// for (int i = 0; i < moves.size(); i++) {
 		// if (!moves.contains(moves2.get(i))) {
 		// System.out.println("Board\n" + board.toString());
 		//
 		// System.out.println("Missing " + (new Move(moves2.get(i))));
 		// }
 		// }
 		// }else{
 		// System.out.println("Board Problem \n" + board.toString());
 		// }
 
 		int fromRow = -1;
 		int fromCol = -1;
 		int toRow = -1;
 		int toCol = -1;
 
 		MoveNote note = null;
 		PieceID pieceMovingID = null;
 		boolean pieceTaken = false;
 
 		if (notation.equals("O-O")) {
 			note = MoveNote.CASTLE_NEAR;
 		} else {
 
 			if (notation.equals("O-O-O")) {
 				note = MoveNote.CASTLE_FAR;
 			} else {
 
 				if (notation.length() > 2) {
 
 					if (notation.length() == 3) {
 						pieceMovingID = Piece.charIDtoPieceID(notation.charAt(0));
 						toRow = 7 - (notation.charAt(2) - 49);
 						toCol = notation.charAt(1) - 97;
 					} else {
 						if (notation.contains("x")) {
 							pieceTaken = true;
 							String[] leftRight = notation.split("x");
 
 							toRow = 7 - (leftRight[1].charAt(1) - 49);
 							toCol = leftRight[1].charAt(0) - 97;
 
 							pieceMovingID = Piece.charIDtoPieceID(leftRight[0].charAt(0));
 
 							if (pieceMovingID == null) {
 								pieceMovingID = PieceID.PAWN;
 								fromCol = leftRight[0].charAt(0) - 97;
 							}
 
 							if (leftRight[0].length() > 1) {
 								if (leftRight[0].charAt(1) >= 97) {
 									fromCol = leftRight[0].charAt(1) - 97;
 								} else {
 									fromRow = 7 - (leftRight[0].charAt(1) - 49);
 								}
 							}
 						} else {
 							toRow = 7 - (notation.charAt(notation.length() - 1) - 49);
 							toCol = notation.charAt(notation.length() - 2) - 97;
 
 							pieceMovingID = Piece.charIDtoPieceID(notation.charAt(0));
 
 							if (notation.charAt(1) >= 97) {
 								fromCol = notation.charAt(1) - 97;
 							} else {
 								fromRow = 7 - (notation.charAt(1) - 49);
 							}
 
 						}
 					}
 
 				} else {
 					if (notation.length() == 2) {
 						toRow = 7 - (notation.charAt(1) - 49);
 						toCol = notation.charAt(0) - 97;
 						pieceMovingID = PieceID.PAWN;
 					} else {
 						System.out.println("Error resolving notation " + notation);
 					}
 				}
 
 			}
 		}
 
 		ArrayList<Long> matchMoves = new ArrayList<Long>();
 		boolean match;
 
 		for (int i = 0; i < moves.size(); i++) {
 
 			match = true;
 
 			if (note != null) {
 				if (Move.getNote(moves.get(i)) != note) {
 					match = false;
 				}
 			}
 
 			if (fromRow >= 0) {
 				if (Move.getFromRow(moves.get(i)) != fromRow) {
 					match = false;
 				}
 			}
 
 			if (fromCol >= 0) {
 				if (Move.getFromCol(moves.get(i)) != fromCol) {
 					match = false;
 				}
 			}
 
 			if (toCol >= 0) {
 				if (Move.getToCol(moves.get(i)) != toCol) {
 					match = false;
 				}
 			}
 
 			if (toRow >= 0) {
 				if (Move.getToRow(moves.get(i)) != toRow) {
 					match = false;
 				}
 			}
 
 			if (pieceMovingID != null) {
 				if (board[Move.getFromRow(moves.get(i))][Move.getFromCol(moves.get(i))].getPieceID() != pieceMovingID) {
 					match = false;
 				}
 			}
 
 			if (match) {
 				// System.out.println(new Move(moves.get(i)));
 				matchMoves.add(moves.get(i));
 			}
 		}
 
 		if (matchMoves.size() != 1) {
 			ArrayList<Move> movesDetailed = new ArrayList<Move>();
 			for (int i = 0; i < moves.size(); i++) {
 				movesDetailed.add(new Move(moves.get(i)));
 			}
 			System.out.println("ERROR resolving algebraic notation " + notation);
 			return 0;
 		}
 
 		return matchMoves.get(0);
 	}
 
 	public static Stack<Piece> getFullPieceSet(Side player) {
 		Stack<Piece> pieces = new Stack<Piece>();
 
 		for (int i = 0; i < 8; i++) {
 			pieces.add(new Piece(PieceID.PAWN, player, 0, 0, false));
 		}
 
 		for (int i = 0; i < 2; i++) {
 			pieces.add(new Piece(PieceID.BISHOP, player, 0, 0, false));
 		}
 
 		for (int i = 0; i < 2; i++) {
 			pieces.add(new Piece(PieceID.ROOK, player, 0, 0, false));
 		}
 
 		for (int i = 0; i < 2; i++) {
 			pieces.add(new Piece(PieceID.KNIGHT, player, 0, 0, false));
 		}
 
 		pieces.add(new Piece(PieceID.KING, player, 0, 0, false));
 		pieces.add(new Piece(PieceID.QUEEN, player, 0, 0, false));
 
 		return pieces;
 	}
 }
