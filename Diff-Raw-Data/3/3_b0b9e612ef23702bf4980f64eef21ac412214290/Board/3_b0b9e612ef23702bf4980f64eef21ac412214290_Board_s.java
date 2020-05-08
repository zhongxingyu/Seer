 package sf.pnr.base;
 
 import sf.pnr.alg.RepetitionTable;
 
 import java.util.Arrays;
 
 import static sf.pnr.base.Evaluation.*;
 import static sf.pnr.base.Utils.*;
 
 public final class Board {
 
     private static final int STAGE_WEIGHT_MOVE = 20;
     private static final int STAGE_WEIGHT_CAPTURED = STAGE_MAX - STAGE_WEIGHT_MOVE;
     private static final int STAGE_MOVE_MIN = 10;
     private static final int STAGE_MOVE_RANGE = 50;
     private static final int STAGE_MOVE_MAX = STAGE_MOVE_MIN + STAGE_MOVE_RANGE;
     private static final int STAGE_CAPTURED_MAX = 5 * VAL_PAWN + VAL_KNIGHT + VAL_BISHOP + VAL_ROOK + VAL_QUEEN;
 
     private final int[] board = new int[128];
 	private int state;
 	private int state2;
 	private final int[][][] pieces = new int[7][2][11];
     private final int[] pieceArrayPos = new int[128];
     private final long[] bitboardAllPieces = new long[2];
     private long zobristIncremental = computeZobristIncremental(this);
     private long zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
     private long zobristPawn;
     private final RepetitionTable repetitionTable = new RepetitionTable();
     private final int[] materialValue = new int[2];
     private final int[] capturedValue = new int[2];
 
     public void restart() {
 		System.arraycopy(INITIAL_BOARD, 0, board, 0, board.length);
 		System.arraycopy(INITIAL_PIECE_ARRAY_POS, 0, pieceArrayPos, 0, pieceArrayPos.length);
 		state = INITIAL_STATE;
         state2 = 0;
 
 		pieces[PAWN][0][0] = 8;
 		pieces[PAWN][0][1] = A[6]; pieces[PAWN][0][2] = B[6]; pieces[PAWN][0][3] = C[6]; pieces[PAWN][0][4] = D[6];
 		pieces[PAWN][0][5] = E[6]; pieces[PAWN][0][6] = F[6]; pieces[PAWN][0][7] = G[6]; pieces[PAWN][0][8] = H[6];
 		pieces[PAWN][1][0] = 8;
 		pieces[PAWN][1][1] = A[1]; pieces[PAWN][1][2] = B[1]; pieces[PAWN][1][3] = C[1]; pieces[PAWN][1][4] = D[1];
 		pieces[PAWN][1][5] = E[1]; pieces[PAWN][1][6] = F[1]; pieces[PAWN][1][7] = G[1]; pieces[PAWN][1][8] = H[1];
 
 		pieces[ROOK][0][0] = 2; pieces[ROOK][0][1] = A[7]; pieces[ROOK][0][2] = H[7];
 		pieces[ROOK][1][0] = 2; pieces[ROOK][1][1] = A[0]; pieces[ROOK][1][2] = H[0];
 
 		pieces[KNIGHT][0][0] = 2; pieces[KNIGHT][0][1] = B[7]; pieces[KNIGHT][0][2] = G[7];
 		pieces[KNIGHT][1][0] = 2; pieces[KNIGHT][1][1] = B[0]; pieces[KNIGHT][1][2] = G[0];
 
 		pieces[BISHOP][0][0] = 2; pieces[BISHOP][0][1] = C[7]; pieces[BISHOP][0][2] = F[7];
 		pieces[BISHOP][1][0] = 2; pieces[BISHOP][1][1] = C[0]; pieces[BISHOP][1][2] = F[0];
 
 		pieces[QUEEN][0][0] = 1; pieces[QUEEN][0][1] = D[7];
 		pieces[QUEEN][1][0] = 1; pieces[QUEEN][1][1] = D[0];
 
         pieces[KING][0][0] = 1; pieces[KING][0][1] = E[7];
         pieces[KING][1][0] = 1; pieces[KING][1][1] = E[0];
 
         zobristIncremental = computeZobristIncremental(this);
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         zobristPawn = computeZobrist(this, PAWN) ^ computeZobrist(this, KING);
         repetitionTable.clear();
         repetitionTable.increment(zobrist);
         materialValue[WHITE] = Evaluation.computeMaterialValueOneSide(this, WHITE);
         materialValue[BLACK] = Evaluation.computeMaterialValueOneSide(this, BLACK);
         capturedValue[WHITE] = 0;
         capturedValue[BLACK] = 0;
         bitboardAllPieces[WHITE] = BitBoard.computeAllPieces(this, WHITE);
         bitboardAllPieces[BLACK] = BitBoard.computeAllPieces(this, BLACK);
 	}
 
 	public void clear() {
 		Arrays.fill(board, 0);
 		Arrays.fill(pieceArrayPos, 0);
 		state = 0;
         state2 = 0;
 		Arrays.fill(pieces[PAWN][0], 0); Arrays.fill(pieces[PAWN][1], 0);
 		Arrays.fill(pieces[ROOK][0], 0); Arrays.fill(pieces[ROOK][1], 0);
 		Arrays.fill(pieces[KNIGHT][0], 0); Arrays.fill(pieces[KNIGHT][1], 0);
 		Arrays.fill(pieces[BISHOP][0], 0); Arrays.fill(pieces[BISHOP][1], 0);
 		Arrays.fill(pieces[QUEEN][0], 0); Arrays.fill(pieces[QUEEN][1], 0);
 		Arrays.fill(pieces[KING][0], 0); Arrays.fill(pieces[KING][1], 0);
         zobristIncremental = computeZobristIncremental(this);
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         zobristPawn = computeZobrist(this, PAWN) ^ computeZobrist(this, KING);
         repetitionTable.clear();
         repetitionTable.increment(zobrist);
         materialValue[WHITE] = 0;
         materialValue[BLACK] = 0;
         capturedValue[WHITE] = INITIAL_MATERIAL_VALUE;
         capturedValue[BLACK] = INITIAL_MATERIAL_VALUE;
         bitboardAllPieces[WHITE] = 0L;
         bitboardAllPieces[BLACK] = 0L;
 	}
 	
 	public int[] getBoard() {
 		return board;
 	}
 	
 	public int getState() {
 		return state;
 	}
 
 	public int getState2() {
 		return state2;
 	}
 
     public int getFullMoveCount() {
         return (state & FULL_MOVES) >> SHIFT_FULL_MOVES;
     }
 	
 	public int[] getPieces(final int side, final int type) {
         assert pieces[type][side][0] >= 0 && pieces[type][side][0] <= 16;
 		return pieces[type][side];
 	}
 	
 	public int getKing(final int side) {
 		return pieces[KING][side][1]; // there is always exactly one king
 	}
 	
 	public void setState(final int state) {
 		this.state = state;
         state2 = 0;
 	}
 
     public void recompute() {
         zobristIncremental = computeZobristIncremental(this);
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         zobristPawn = computeZobrist(this, PAWN) ^ computeZobrist(this, KING);
         materialValue[WHITE] = Evaluation.computeMaterialValueOneSide(this, WHITE);
         materialValue[BLACK] = Evaluation.computeMaterialValueOneSide(this, BLACK);
         capturedValue[WHITE] = Math.max(Evaluation.INITIAL_MATERIAL_VALUE - materialValue[BLACK], 0);
         capturedValue[BLACK] = Math.max(Evaluation.INITIAL_MATERIAL_VALUE - materialValue[WHITE], 0);
         bitboardAllPieces[WHITE] = BitBoard.computeAllPieces(this, WHITE);
         bitboardAllPieces[BLACK] = BitBoard.computeAllPieces(this, BLACK);
     }
 
     public long move(final int move) {
         final int moveBase = move & BASE_INFO;
 //        System.out.println("Move: " + StringUtils.toSimple(moveBase));
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this):
             "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
 		final int fromPos = getFromPosition(moveBase);
 		final int piece = board[fromPos];
         assert piece != EMPTY: "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
 		final int moveType = (moveBase & MOVE_TYPE);
         final int signum = Integer.signum(piece);
         assert (((state & WHITE_TO_MOVE) << 1) - 1) == signum:
             "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
         final int absPiece = signum * piece;
         final int toPos = getToPosition(moveBase);
         assert fromPos != toPos;
 		final int captured;
 		final int capturePos;
 		if (moveType == MT_EN_PASSANT) {
 			captured = -signum * PAWN;
 			capturePos = toPos - signum * UP;
 		} else {
 			captured = board[toPos];
 			capturePos = toPos;
 		}
         assert piece * captured <= 0;
 		final int absCaptured = -signum * captured;
         assert absCaptured != KING;
 		final long undo = (((long) state) << 32) | moveBase | (absCaptured << SHIFT_CAPTURED);
 		
 		// update the "to move" state, the move counters and the en passant flag
 		final int currentPlayer = state & WHITE_TO_MOVE;		
 		state ^= WHITE_TO_MOVE;
 		final int nextPlayer = state & WHITE_TO_MOVE;
 		state += nextPlayer * UNIT_FULL_MOVES;
         zobristIncremental ^= ZOBRIST_WHITE_TO_MOVE;
         assert zobristIncremental == computeZobristIncremental(this);
 		state &= CLEAR_EN_PASSANT;
 		if (absPiece != PAWN) {
 			if (captured == EMPTY) {
 				state += UNIT_HALF_MOVES;
 			} else {
 				state &= CLEAR_HALF_MOVES;
 			}
 		} else {
 			state &= CLEAR_HALF_MOVES;
             final int direction = toPos - fromPos;
 			if (direction == 0x20 || direction == -0x20) {
                 final int enPassantFile = getFile(fromPos) + 1;
                 assert enPassantFile >= 1 && enPassantFile <= 8;
                 state |= enPassantFile << SHIFT_EN_PASSANT;
 			}
 		}
 
 		if (absCaptured != 0) {
 			// remove the captured piece from the piece list
             capturedValue[currentPlayer] +=
                 Evaluation.VAL_PIECE_INCREMENTS[absCaptured][pieces[absCaptured][nextPlayer][0]];
             removeFromPieceList(nextPlayer, absCaptured, capturePos);
             state &= CLEAR_CASTLING[toPos];
 		}
 		
         movePiece(absPiece, currentPlayer, fromPos, toPos);
 
         switch (moveType) {
         case MT_NORMAL:
             state &= CLEAR_CASTLING[fromPos];
             break;
         case MT_CASTLING_KINGSIDE:
         case MT_CASTLING_QUEENSIDE:
             {
                 assert getRank(fromPos) == getRank(toPos);
                 assert captured == EMPTY;
                 assert Math.abs(board[toPos]) == KING;
                 final int rookFromPos =
                     toPos + CASTLING_TO_ROOK_FROM_DELTA[(moveType & MT_CASTLING) >> SHIFT_MOVE_TYPE];
                 final int rookToPos =
                     toPos + CASTLING_TO_ROOK_TO_DELTA[(moveType & MT_CASTLING) >> SHIFT_MOVE_TYPE];
                 assert Math.abs(board[rookFromPos]) == ROOK;
                 movePiece(ROOK, currentPlayer, rookFromPos, rookToPos);
                 state &= (fromPos < 8)? (CLEAR_CASTLING_WHITE_KINGSIDE & CLEAR_CASTLING_WHITE_QUEENSIDE):
                     (CLEAR_CASTLING_BLACK_KINGSIDE & CLEAR_CASTLING_BLACK_QUEENSIDE);
                 state2 |= (currentPlayer + 1);
             }
             break;
         case MT_EN_PASSANT:
             assert captured == -piece; assert getRank(fromPos) == 4 || getRank(fromPos) == 3;
             assert getRank(toPos) == 5 || getRank(toPos) == 2;
             assert piece == PAWN || piece == -PAWN;
             board[capturePos] = EMPTY;
             break;
         case MT_PROMOTION_KNIGHT:
         case MT_PROMOTION_BISHOP:
         case MT_PROMOTION_ROOK:
         case MT_PROMOTION_QUEEN:
             assert piece == PAWN || piece == -PAWN;
             replacePromotedPawn(signum, toPos, currentPlayer, PROMOTION_TO_PIECE[moveType >> SHIFT_MOVE_TYPE]);
             break;
         }
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         repetitionTable.increment(zobrist);
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert pieces[PAWN][0][0] <= 8;
         assert pieces[PAWN][1][0] <= 8;
         assert pieceArrayPos[fromPos] == EMPTY;
         assert pieceArrayPos[toPos] != EMPTY;
         assert board[fromPos] == EMPTY;
         assert board[toPos] != EMPTY;
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this):
             "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
         assert (((state & WHITE_TO_MOVE) << 1) - 1) == -signum;
 		return undo;
 	}
 
     private void replacePromotedPawn(final int signum, final int toPos, final int toMove, final int piece) {
         board[toPos] = signum * piece;
         removeFromPieceList(toMove, PAWN, toPos);
         addToPieceList(toMove, piece, toPos);
     }
 
     private void movePiece(final int absPiece, final int toMove, final int fromPos, final int toPos) {
         // update zobrist key
         final int fromPos64 = convert0x88To64(fromPos);
         final int toPos64 = convert0x88To64(toPos);
         final long zobristFrom = ZOBRIST_PIECES[absPiece][toMove][fromPos64];
         final long zobristTo = ZOBRIST_PIECES[absPiece][toMove][toPos64];
         final long zobristMove = zobristFrom ^ zobristTo;
         zobristIncremental ^= zobristMove;
         if (absPiece == PAWN || absPiece == KING) {
             zobristPawn ^= zobristMove;
         }
         // update the board
         board[toPos] = board[fromPos];
         board[fromPos] = EMPTY;
         // update the piece array and the piece list
         final int pos = pieceArrayPos[fromPos];
         pieceArrayPos[toPos] = pos;
         pieceArrayPos[fromPos] = EMPTY;
         pieces[absPiece][toMove][pos] = toPos;
         bitboardAllPieces[toMove] ^= 1L << fromPos64;
         bitboardAllPieces[toMove] ^= 1L << toPos64;
     }
 
     private void removeFromPieceList(final int side, final int absPiece, final int position) {
         assert absPiece != EMPTY;
         final int[] pieceIndices = pieces[absPiece][side];
         final int pieceCount = pieceIndices[0];
         assert pieceCount > 0;
         assert pieceArrayPos[position] != EMPTY;
         final int lastPieceIdx = pieceIndices[pieceCount];
         final int lastPieceNewPos = pieceArrayPos[position];
         pieceIndices[lastPieceNewPos] = lastPieceIdx;
         pieceArrayPos[lastPieceIdx] = lastPieceNewPos;
         pieceArrayPos[position] = 0;
         pieceIndices[0]--;
         materialValue[side] -= Evaluation.VAL_PIECE_INCREMENTS[absPiece][pieceCount];
         final int position64 = convert0x88To64(position);
         final long zobristKey = ZOBRIST_PIECES[absPiece][side][position64];
         zobristIncremental ^= zobristKey;
         if (absPiece == PAWN) {
             zobristPawn ^= zobristKey;
         }
         bitboardAllPieces[side] ^= 1L << position64;
     }
 
     private void addToPieceList(final int side, final int absPiece, final int position) {
         final int[] pieceIndices = pieces[absPiece][side];
         pieceIndices[0]++;
         final int pieceCount = pieceIndices[0];
         assert pieceCount <= 10;
         assert absPiece != PAWN || pieceCount <= 8;
         pieceIndices[pieceCount] = position;
         pieceArrayPos[position] = pieceIndices[0];
         materialValue[side] += Evaluation.VAL_PIECE_INCREMENTS [absPiece][pieceCount];
         final int position64 = convert0x88To64(position);
         final long zobristKey = ZOBRIST_PIECES[absPiece][side][position64];
         zobristIncremental ^= zobristKey;
         if (absPiece == PAWN) {
             zobristPawn ^= zobristKey;
         }
         bitboardAllPieces[side] ^= 1L << position64;
     }
 
     public int getMaterialValue() {
         return (((state & WHITE_TO_MOVE) << 1) - 1) * getMaterialValueAsWhite();
     }
 
     public int getMaterialValueAsWhite() {
         return materialValue[WHITE_TO_MOVE] - materialValue[BLACK_TO_MOVE];
     }
 
     public int getMaterialValueWhite() {
         return materialValue[WHITE_TO_MOVE];
     }
 
     public int getMaterialValueBlack() {
         return materialValue[BLACK_TO_MOVE];
     }
 
     public int getStage() {
         final int capturedMax = Math.max(capturedValue[BLACK_TO_MOVE], capturedValue[WHITE_TO_MOVE]);
         if (capturedMax > STAGE_CAPTURED_MAX) {
             return STAGE_MAX;
         }
         return capturedMax * STAGE_MAX / STAGE_CAPTURED_MAX;
     }
 
     public void takeBack(final long undo) {
         assert pieces[PAWN][0][0] <= 8;
         assert pieces[PAWN][1][0] <= 8;
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this);
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
         assert zobristIncremental == computeZobristIncremental(this);
         repetitionTable.decrement(zobrist);
         // restore the state and the move info
 		state = (int) (undo >>> 32);
         final int move = (int) undo;
 
         // extract the info
         final int fromPos = getFromPosition(move);
 		final int toPos = getToPosition(move);
         assert board[fromPos] == EMPTY;
         assert board[toPos] != EMPTY;
         assert pieceArrayPos[fromPos] == EMPTY;
         assert pieceArrayPos[toPos] != EMPTY;
         final int moveType = move & MOVE_TYPE;
         final int piece = board[toPos];
         final int signum = Integer.signum(piece);
         final int signumOpponent = -signum;
         final int currentPlayer = state & WHITE_TO_MOVE;
         assert ((currentPlayer << 1) - 1) == signum;
 
         zobristIncremental ^= ZOBRIST_WHITE_TO_MOVE;
         assert zobristIncremental == computeZobristIncremental(this);
 
         if ((moveType & MT_PROMOTION) == 0) {
             final int absPiece = -signumOpponent * piece;
             movePiece(absPiece, currentPlayer, toPos, fromPos);
             switch (moveType) {
                 case MT_EN_PASSANT:
                     final int enPassantPosition = toPos + signumOpponent * 16;
                     final int opponent = currentPlayer ^ WHITE_TO_MOVE;
                     board[enPassantPosition] = signumOpponent * PAWN;
                     addToPieceList(opponent, PAWN, enPassantPosition);
                     break;
                 case MT_CASTLING_QUEENSIDE:
                     movePiece(ROOK, currentPlayer, fromPos - 1, toPos - 2);
                     state2 &= ~(currentPlayer + 1);
                     break;
                 case MT_CASTLING_KINGSIDE:
                     movePiece(ROOK, currentPlayer, fromPos + 1, toPos + 1);
                     state2 &= ~(currentPlayer + 1);
                     break;
                 default:
                     final int absCaptured = (move & CAPTURED) >> SHIFT_CAPTURED;
                     if (absCaptured != EMPTY) {
                         board[toPos] = signumOpponent * absCaptured;
                         final int toMove = currentPlayer ^ WHITE_TO_MOVE;
                         // return the captured piece to the pieces list of the opponent
                         addToPieceList(toMove, absCaptured, toPos);
                         assert zobristIncremental == computeZobristIncremental(this);
                         capturedValue[currentPlayer] -=
                             Evaluation.VAL_PIECE_INCREMENTS[absCaptured][pieces[absCaptured][toMove][0]];
                     }
                 break;
             }
         } else {
             // move was a promotion, need to update the piece list accordingly
             final int absPiece = signum * piece;
             board[toPos] = EMPTY;
             final int pawn = signum * PAWN;
             board[fromPos] = pawn;
             removeFromPieceList(currentPlayer, absPiece, toPos);
             addToPieceList(currentPlayer, PAWN, fromPos);
             final int absCaptured = (move & CAPTURED) >> SHIFT_CAPTURED;
             if (absCaptured != EMPTY) {
                 board[toPos] = signumOpponent * absCaptured;
                 // return the captured piece to the pieces list of the opponent
                 addToPieceList(currentPlayer ^ WHITE_TO_MOVE, absCaptured, toPos);
             }
         }
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert pieces[PAWN][0][0] <= 8;
         assert pieces[PAWN][1][0] <= 8;
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this);
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
 	}
 
     public int getRepetitionCount() {
         return repetitionTable.get(zobrist);
     }
 
     public int nullMove() {
         final int prevState = state;
         state ^= WHITE_TO_MOVE;
         zobristIncremental ^= ZOBRIST_WHITE_TO_MOVE;
         zobrist ^= ZOBRIST_WHITE_TO_MOVE;
         zobrist ^= ZOBRIST_EN_PASSANT[(state & EN_PASSANT) >> SHIFT_EN_PASSANT];
         state ^= state & EN_PASSANT;
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
         return prevState;
     }
 
     public void nullMove(final int prevState) {
         state ^= WHITE_TO_MOVE;
         zobristIncremental ^= ZOBRIST_WHITE_TO_MOVE;
         zobrist ^= ZOBRIST_WHITE_TO_MOVE;
         zobrist ^= ZOBRIST_EN_PASSANT[(prevState & EN_PASSANT) >> SHIFT_EN_PASSANT];
         state ^= prevState & EN_PASSANT;
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
     }
 
     public int[] getPieceArrayPositions() {
 		return pieceArrayPos;
 	}
 
     public long getBitboard(final int side) {
         return bitboardAllPieces[side];
     }
 
     public long getZobristKey() {
         return zobrist;
     }
 
     public long getZobristPawn() {
         return zobristPawn;
     }
 
     public long getPolyglotZobristKey() {
         long polyglotZobrist = this.zobrist;
         if ((state & EN_PASSANT) > 0) {
             final int enPassant = ((state & EN_PASSANT ) >> SHIFT_EN_PASSANT) - 1;
             final int toMove = state & WHITE_TO_MOVE;
             final int signum = (toMove << 1) - 1;
             final int pawn = signum * PAWN;
             final int rankStartPos = (3 + toMove) << 4;
             final int leftPos = rankStartPos + enPassant - 1;
             final int rightPos = leftPos + 2;
             if (!((leftPos & 0x88) == 0 && board[leftPos] == pawn ||
                     (rightPos & 0x88) == 0 && board[rightPos] == pawn)) {
                 polyglotZobrist ^= ZOBRIST_EN_PASSANT[(state & EN_PASSANT) >> SHIFT_EN_PASSANT];
             }
         }
         return polyglotZobrist;
     }
 
     public boolean isAttacked(final int position, final int side) {
         final int[] knights = pieces[KNIGHT][side];
         for (int i = knights[0]; i > 0; i--) {
             if ((ATTACK_ARRAY[position - knights[i] + 120] & ATTACK_N) == ATTACK_N) {
                 return true;
             }
         }
         final int kingIdx = pieces[KING][side][1];
         if ((ATTACK_ARRAY[position - kingIdx + 120] & ATTACK_K) > 0) {
             return true;
         }
         if (isAttackedSliding(position, side, ROOK, ATTACK_R)) return true;
         if (isAttackedSliding(position, side, BISHOP, ATTACK_B)) return true;
         if (isAttackedSliding(position, side, QUEEN, ATTACK_Q)) return true;
         if (side == WHITE_TO_MOVE) {
             if (((position + DL) & 0x88) == 0 && board[position + DL] == PAWN) return true;
             if (((position + DR) & 0x88) == 0 && board[position + DR] == PAWN) return true;
         } else {
             if (((position + UL) & 0x88) == 0 && board[position + UL] == -PAWN) return true;
             if (((position + UR) & 0x88) == 0 && board[position + UR] == -PAWN) return true;
         }
         return false;
     }
 
     private boolean isAttackedSliding(final int position, final int side, final int absPiece, final int attackBits) {
         final int[] pieceIndices = pieces[absPiece][side];
         for (int i = pieceIndices[0]; i > 0; i--) {
             if (isAttackedBySliding(position, attackBits, pieceIndices[i])) return true;
         }
         return false;
     }
 
     public void getAttackers(final int position, final int side, final int[] attackers) {
 
         attackers[0] = 0;
         if (side == WHITE_TO_MOVE) {
             if (((position + DL) & 0x88) == 0 && board[position + DL] == PAWN) attackers[++attackers[0]] = position + DL;
             if (((position + DR) & 0x88) == 0 && board[position + DR] == PAWN) attackers[++attackers[0]] = position + DR;
         } else {
             if (((position + UL) & 0x88) == 0 && board[position + UL] == -PAWN) attackers[++attackers[0]] = position + UL;
             if (((position + UR) & 0x88) == 0 && board[position + UR] == -PAWN) attackers[++attackers[0]] = position + UR;
         }
         final int[] knights = pieces[KNIGHT][side];
         for (int i = knights[0]; i > 0; i--) {
             final int knightPos = knights[i];
             if ((ATTACK_ARRAY[position - knightPos + 120] & ATTACK_N) == ATTACK_N) {
                 attackers[++attackers[0]] = knightPos;
             }
         }
         getAttackedSliding(position, side, BISHOP, ATTACK_B, attackers);
         getAttackedSliding(position, side, ROOK, ATTACK_R, attackers);
         getAttackedSliding(position, side, QUEEN, ATTACK_Q, attackers);
         final int kingPos = pieces[KING][side][1];
         if ((ATTACK_ARRAY[position - kingPos + 120] & ATTACK_K) > 0) {
             attackers[++attackers[0]] = kingPos;
         }
     }
 
     private void getAttackedSliding(final int targetPos, final int side, final int absPiece, final int attackBits,
                                     final int[] attackers) {
         final int[] positions = pieces[absPiece][side];
         for (int i = positions[0]; i > 0; i--) {
             final int pos = positions[i];
             if (isAttackedBySliding(targetPos, attackBits, pos)) {
                 attackers[++attackers[0]] = pos;
             }
         }
     }
 
     public boolean isAttackedBySliding(final int targetPos, final int attackBits, final int piecePos) {
         final int attackValue = ATTACK_ARRAY[(targetPos - piecePos + 120)];
         if ((attackValue & attackBits) > 0) {
             final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
             int testPos = piecePos + delta;
             while (testPos != targetPos && (testPos & 0x88) == 0 && board[testPos] == EMPTY) {
                 testPos += delta;
             }
             if (testPos == targetPos) return true;
         }
         return false;
     }
 
     public boolean isAttackedByNonSliding(final int targetPos, final int attackBits, final int piecePos) {
         assert Math.abs(board[piecePos]) == KNIGHT || Math.abs(board[piecePos]) == KING;
         final int attackArrayIndex = targetPos - piecePos + 120;
         final int attackValue = ATTACK_ARRAY[attackArrayIndex];
         return (attackValue & attackBits) > 0;
     }
 
     public int getMinorMajorPieceCount(final int toMove) {
         return pieces[KNIGHT][toMove][0] + pieces[BISHOP][toMove][0] + pieces[ROOK][toMove][0] + pieces[QUEEN][toMove][0];
     }
 
     public boolean attacksKing(final int side) {
         final int kingPos = pieces[KING][1 - side][1];
         return isAttacked(kingPos, side);
     }
 
     public boolean isCheckingMove(final int move) {
         return isCheckingMove(move, true);
     }
 
     public boolean isCheckingMove(final int move, final boolean testDiscoveredCheck) {
         final int toMove = state & WHITE_TO_MOVE;
         final int fromPos = getFromPosition(move);
         final int signum = (toMove << 1) - 1;
         final int piece = board[fromPos];
         final int absPiece = signum * piece;
         assert absPiece != EMPTY: StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
         final int kingPos = pieces[KING][1 - toMove][1];
         final int toPos = getToPosition(move);
         final int attacked = board[toPos];
         // if it we are moving on the line to the opponent king and it's not castling or capturing
         // then it's not a checking move (assuming that we start from a legal position)
         if (attacked == EMPTY && !Utils.isCastling(move) &&
                 (ATTACK_ARRAY[kingPos - toPos + 120] & ATTACK_Q & ATTACK_ARRAY[kingPos - fromPos + 120]) > 0) {
             return false;
         }
         // if we are in the attack line (blocked check) no need to look for discovered check and also if we move
         // somewhere where we can't attack the king then it can't be a check
         if (absPiece != PAWN && (ATTACK_ARRAY[kingPos - fromPos + 120] & ATTACK_BITS[absPiece]) > 0) {
             if (!Utils.isCastling(move) && (ATTACK_ARRAY[kingPos - toPos + 120] & ATTACK_BITS[absPiece]) == 0) {
                 return false;
             }
         } else if (testDiscoveredCheck && isDiscoveredCheck(kingPos, fromPos, signum)) {
             return true;
         }
         switch (absPiece) {
             case PAWN:
                 final int toRank = getRank(toPos);
                 if (toRank > 0 && toRank < 7) {
                     final int[] deltas = DELTA_PAWN_ATTACK[toMove];
                     for (int delta: deltas) {
                         if (toPos + delta == kingPos) {
                             return true;
                         }
                     }
                 } else {
                     // it's a promotion
                     final int promotedTo = PROMOTION_TO_PIECE[(move & MOVE_TYPE) >> SHIFT_MOVE_TYPE];
                     assert promotedTo == KNIGHT || promotedTo == BISHOP || promotedTo == ROOK || promotedTo == QUEEN:
                         StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
                     if (promotedTo == KNIGHT) {
                         return ((ATTACK_ARRAY[kingPos - toPos + 120] & ATTACK_N) == ATTACK_N);
                     } else {
                         return isAttackedBySliding(kingPos, ATTACK_BITS[promotedTo], toPos);
                     }
                 }
                 return false;
             case KNIGHT:
                 return ((ATTACK_ARRAY[kingPos - toPos + 120] & ATTACK_N) == ATTACK_N);
             case BISHOP:
             case ROOK:
             case QUEEN:
                 return isAttackedBySliding(kingPos, ATTACK_BITS[absPiece], toPos);
             case KING:
                 if (Utils.isCastling(move)) {
                     if ((move & MT_CASTLING_QUEENSIDE) > 0) {
                         return isAttackedBySliding(kingPos, ATTACK_R, toPos + 1);
                     } else {
                         return isAttackedBySliding(kingPos, ATTACK_R, toPos - 1);
                     }
                 }
                 return false;
         }
         return false;
     }
 
     public boolean isDiscoveredCheck(final int kingPos, final int fromPos, final int signum) {
         if (isAttackedBySliding(kingPos, ATTACK_Q, fromPos)) {
             // search for discovered check
             final int attackValue = ATTACK_ARRAY[fromPos - kingPos + 120];
             final int attackBits = attackValue & ATTACK_Q;
             assert attackBits != 0;
             final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
             int testPos = fromPos + delta;
             while ((testPos & 0x88) == 0 && board[testPos] == EMPTY) {
                 testPos += delta;
             }
             if ((testPos & 0x88) == 0) {
                 final int absAttacker = board[testPos] * signum;
                 if (absAttacker > 0) {
                    assert (ATTACK_BITS[absAttacker] & attackBits) < 0 || SLIDING[absAttacker];
                    return (ATTACK_BITS[absAttacker] & attackBits) > 0;
                 }
             }
         }
         return false;
     }
 
     public boolean isMate() {
         final int toMove = state & WHITE_TO_MOVE;
         final int kingPos = pieces[KING][toMove][1];
         if (isAttacked(kingPos, 1 - toMove)) {
             final MoveGenerator moveGenerator = new MoveGenerator();
             moveGenerator.pushFrame();
             moveGenerator.generatePseudoLegalMoves(this);
             boolean hasLegalMove = hasLegalMove(moveGenerator.getWinningCaptures()) ||
                 hasLegalMove(moveGenerator.getLoosingCaptures());
             if (!hasLegalMove) {
                 moveGenerator.generatePseudoLegalMovesNonAttacking(this);
                 hasLegalMove = hasLegalMove(moveGenerator.getMoves()) || hasLegalMove(moveGenerator.getPromotions());
             }
             return !hasLegalMove;
         }
         return false;
     }
 
     public boolean hasLegalMove(final int[] moves) {
         boolean found = false;
         for (int i = moves[0]; i > 0 && !found; i--) {
             final long undo = move(moves[i]);
             final int toMove = state & WHITE_TO_MOVE;
             final int kingPos = pieces[KING][(1 - toMove)][1];
             found = !isAttacked(kingPos, toMove);
             takeBack(undo);
         }
         return found;
     }
 
     public RepetitionTable getRepetitionTable() {
         return repetitionTable;
     }
 }
