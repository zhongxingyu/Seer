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
 	
 	public int[] getPieces(final int side, final int type) {
 		return pieces[type][side];
 	}
 	
 	public int getKing(final int side) {
 		return pieces[KING][side][1]; // there is always exactly one king
 	}
 	
 	public void setState(final int state) {
 		this.state = state;
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
 		final int fromIndex = getMoveFromIndex(moveBase);
 		final int piece = board[fromIndex];
         assert piece != EMPTY: "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
 		final int moveType = (moveBase & MOVE_TYPE);
         final int signum = Integer.signum(piece);
         final int absPiece = signum * piece;
         final int toIndex = getMoveToIndex(moveBase);
         assert fromIndex != toIndex;
 		final int captured;
 		final int captureIndex;
 		if (moveType == MT_EN_PASSANT) {
 			captured = -signum * PAWN;
 			captureIndex = toIndex - signum * UP;
 		} else {
 			captured = board[toIndex];
 			captureIndex = toIndex;
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
             final int direction = toIndex - fromIndex;
 			if (direction == 0x20 || direction == -0x20) {
                 final int enPassantFile = getFile(fromIndex) + 1;
                 assert enPassantFile >= 1 && enPassantFile <= 8;
                 state |= enPassantFile << SHIFT_EN_PASSANT;
 			}
 		}
 
 		if (absCaptured != 0) {
 			// remove the captured piece from the piece list
             capturedValue[currentPlayer] +=
                 Evaluation.VAL_PIECE_INCREMENTS[absCaptured][pieces[absCaptured][nextPlayer][0]];
             removeFromPieceList(nextPlayer, absCaptured, captureIndex);
             state &= CLEAR_CASTLING[toIndex];
 		}
 		
         movePiece(absPiece, currentPlayer, fromIndex, toIndex);
 
         switch (moveType) {
         case MT_NORMAL:
             state &= CLEAR_CASTLING[fromIndex];
             break;
         case MT_CASTLING_KINGSIDE:
         case MT_CASTLING_QUEENSIDE:
             {
                 assert getRank(fromIndex) == getRank(toIndex);
                 assert captured == EMPTY;
                 assert Math.abs(board[toIndex]) == KING;
                 final int rookFromIndex =
                     toIndex + CASTLING_TO_ROOK_FROM_DELTA[(moveType & MT_CASTLING) >> SHIFT_MOVE_TYPE];
                 final int rookToIndex =
                     toIndex + CASTLING_TO_ROOK_TO_DELTA[(moveType & MT_CASTLING) >> SHIFT_MOVE_TYPE];
                 assert Math.abs(board[rookFromIndex]) == ROOK;
                 movePiece(ROOK, currentPlayer, rookFromIndex, rookToIndex);
                 state &= (fromIndex < 8)? (CLEAR_CASTLING_WHITE_KINGSIDE & CLEAR_CASTLING_WHITE_QUEENSIDE):
                     (CLEAR_CASTLING_BLACK_KINGSIDE & CLEAR_CASTLING_BLACK_QUEENSIDE);
             }
             break;
         case MT_EN_PASSANT:
             assert captured == -piece; assert getRank(fromIndex) == 4 || getRank(fromIndex) == 3;
             assert getRank(toIndex) == 5 || getRank(toIndex) == 2;
             assert piece == PAWN || piece == -PAWN;
             board[captureIndex] = EMPTY;
             break;
         case MT_PROMOTION_KNIGHT:
         case MT_PROMOTION_BISHOP:
         case MT_PROMOTION_ROOK:
         case MT_PROMOTION_QUEEN:
             assert piece == PAWN || piece == -PAWN;
             replacePromotedPawn(signum, toIndex, currentPlayer, PROMOTION_TO_PIECE[moveType >> SHIFT_MOVE_TYPE]);
             break;
         }
         zobrist = zobristIncremental ^ computeZobristNonIncremental(state);
         repetitionTable.increment(zobrist);
         assert zobrist == (computeZobristIncremental(this) ^ computeZobristNonIncremental(state));
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert pieces[PAWN][0][0] <= 8;
         assert pieces[PAWN][1][0] <= 8;
         assert pieceArrayPos[fromIndex] == EMPTY;
         assert pieceArrayPos[toIndex] != EMPTY;
         assert board[fromIndex] == EMPTY;
         assert board[toIndex] != EMPTY;
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this):
             "FEN: " + StringUtils.toFen(this) + ", move: " + StringUtils.toSimple(move);
 		return undo;
 	}
 
     private void replacePromotedPawn(final int signum, final int toIndex, final int toMove, final int piece) {
         board[toIndex] = signum * piece;
         removeFromPieceList(toMove, PAWN, toIndex);
         addToPieceList(toMove, piece, toIndex);
     }
 
     private void movePiece(final int absPiece, final int toMove, final int fromIndex, final int toIndex) {
         // update zobrist key
         final int fromIndex64 = convert0x88To64(fromIndex);
         final int toIndex64 = convert0x88To64(toIndex);
         final long zobristFrom = ZOBRIST_PIECES[absPiece][toMove][fromIndex64];
         final long zobristTo = ZOBRIST_PIECES[absPiece][toMove][toIndex64];
         final long zobristMove = zobristFrom ^ zobristTo;
         zobristIncremental ^= zobristMove;
         if (absPiece == PAWN || absPiece == KING) {
             zobristPawn ^= zobristMove;
         }
         // update the board
         board[toIndex] = board[fromIndex];
         board[fromIndex] = EMPTY;
         // update the piece array and the piece list
         final int pos = pieceArrayPos[fromIndex];
         pieceArrayPos[toIndex] = pos;
         pieceArrayPos[fromIndex] = EMPTY;
         pieces[absPiece][toMove][pos] = toIndex;
         bitboardAllPieces[toMove] ^= 1L << fromIndex64;
         bitboardAllPieces[toMove] ^= 1L << toIndex64;
     }
 
     private void removeFromPieceList(final int side, final int absPiece, final int index) {
         assert absPiece != EMPTY;
         final int[] pieceIndices = pieces[absPiece][side];
         final int pieceCount = pieceIndices[0];
         assert pieceCount > 0;
         assert pieceArrayPos[index] != EMPTY;
         final int lastPieceIdx = pieceIndices[pieceCount];
         final int lastPieceNewPos = pieceArrayPos[index];
         pieceIndices[lastPieceNewPos] = lastPieceIdx;
         pieceArrayPos[lastPieceIdx] = lastPieceNewPos;
         pieceArrayPos[index] = 0;
         pieceIndices[0]--;
         materialValue[side] -= Evaluation.VAL_PIECE_INCREMENTS[absPiece][pieceCount];
         final int index64 = convert0x88To64(index);
         final long zobristKey = ZOBRIST_PIECES[absPiece][side][index64];
         zobristIncremental ^= zobristKey;
         if (absPiece == PAWN) {
             zobristPawn ^= zobristKey;
         }
         bitboardAllPieces[side] ^= 1L << index64;
     }
 
     private void addToPieceList(final int side, final int absPiece, final int index) {
         final int[] pieceIndices = pieces[absPiece][side];
         pieceIndices[0]++;
         final int pieceCount = pieceIndices[0];
         assert pieceCount <= 10;
         assert absPiece != PAWN || pieceCount <= 8;
         pieceIndices[pieceCount] = index;
         pieceArrayPos[index] = pieceIndices[0];
         materialValue[side] += Evaluation.VAL_PIECE_INCREMENTS [absPiece][pieceCount];
         final int index64 = convert0x88To64(index);
         final long zobristKey = ZOBRIST_PIECES[absPiece][side][index64];
         zobristIncremental ^= zobristKey;
         if (absPiece == PAWN) {
             zobristPawn ^= zobristKey;
         }
         bitboardAllPieces[side] ^= 1L << index64;
     }
 
     public int getMaterialValue() {
         return (((state & WHITE_TO_MOVE) << 1) - 1) * getMaterialValueAsWhite();
     }
 
     public int getMaterialValueAsWhite() {
         return materialValue[WHITE_TO_MOVE] - materialValue[BLACK_TO_MOVE];
     }
 
     public int getStage() {
         final int fullMoves = (state & FULL_MOVES) >> SHIFT_FULL_MOVES;
         final int moveCount;
         if (fullMoves < STAGE_MOVE_MIN) {
             moveCount = 0;
         } else if (fullMoves > STAGE_MOVE_MAX) {
             moveCount =  STAGE_MOVE_RANGE;
         } else {
             moveCount = fullMoves - STAGE_MOVE_MIN;
         }
         final int capturedMax = Math.max(capturedValue[BLACK_TO_MOVE], capturedValue[WHITE_TO_MOVE]);
         if (capturedMax > STAGE_CAPTURED_MAX) {
             return STAGE_MAX;
         }
         return moveCount * STAGE_WEIGHT_MOVE / STAGE_MOVE_RANGE +
             capturedMax * STAGE_WEIGHT_CAPTURED / STAGE_CAPTURED_MAX; 
     }
 
     public void takeBack(final long undo) {
         assert pieces[PAWN][0][0] <= 8;
         assert pieces[PAWN][1][0] <= 8;
         assert zobristPawn == (computeZobrist(this, PAWN) ^ computeZobrist(this, KING));
         assert getMaterialValueAsWhite() == Evaluation.computeMaterialValueAsWhite(this);
         assert bitboardAllPieces[WHITE] == BitBoard.computeAllPieces(this, WHITE);
         assert bitboardAllPieces[BLACK] == BitBoard.computeAllPieces(this, BLACK);
         repetitionTable.decrement(zobrist);
         // restore the state and the move info
 		state = (int) (undo >>> 32);
         final int move = (int) undo;
 //        System.out.println("Undo: " + StringUtils.toSimple(move));
 
         // extract the info
         final int fromIndex = getMoveFromIndex(move);
 		final int toIndex = getMoveToIndex(move);
         assert board[fromIndex] == EMPTY;
         assert board[toIndex] != EMPTY;
         assert pieceArrayPos[fromIndex] == EMPTY;
         assert pieceArrayPos[toIndex] != EMPTY;
         final int moveType = move & MOVE_TYPE;
         final int piece = board[toIndex];
         final int signum = Integer.signum(piece);
         final int signumOpponent = -signum;
         final int currentPlayer = state & WHITE_TO_MOVE;
 
         zobristIncremental ^= ZOBRIST_WHITE_TO_MOVE;
         assert zobristIncremental == computeZobristIncremental(this);
 
         if ((moveType & MT_PROMOTION) == 0) {
             final int absPiece = -signumOpponent * piece;
             movePiece(absPiece, currentPlayer, toIndex, fromIndex);
             switch (moveType) {
                 case MT_EN_PASSANT:
                     final int enPassantIndex = toIndex + signumOpponent * 16;
                     final int opponent = currentPlayer ^ WHITE_TO_MOVE;
                     board[enPassantIndex] = signumOpponent * PAWN;
                     addToPieceList(opponent, PAWN, enPassantIndex);
                     break;
                 case MT_CASTLING_QUEENSIDE:
                     movePiece(ROOK, currentPlayer, fromIndex - 1, toIndex - 2);
                     break;
                 case MT_CASTLING_KINGSIDE:
                     movePiece(ROOK, currentPlayer, fromIndex + 1, toIndex + 1);
                     break;
                 default:
                     final int absCaptured = (move & CAPTURED) >> SHIFT_CAPTURED;
                     if (absCaptured != EMPTY) {
                         board[toIndex] = signumOpponent * absCaptured;
                         final int toMove = currentPlayer ^ WHITE_TO_MOVE;
                         // return the captured piece to the pieces list of the opponent
                         addToPieceList(toMove, absCaptured, toIndex);
                         assert zobristIncremental == computeZobristIncremental(this);
                         capturedValue[currentPlayer] -=
                             Evaluation.VAL_PIECE_INCREMENTS[absCaptured][pieces[absCaptured][toMove][0]];
                     }
                 break;
             }
         } else {
             // move was a promotion, need to update the piece list accordingly
             final int absPiece = signum * piece;
             board[toIndex] = EMPTY;
             final int pawn = signum * PAWN;
             board[fromIndex] = pawn;
             removeFromPieceList(currentPlayer, absPiece, toIndex);
             addToPieceList(currentPlayer, PAWN, fromIndex);
             final int absCaptured = (move & CAPTURED) >> SHIFT_CAPTURED;
             if (absCaptured != EMPTY) {
                 board[toIndex] = signumOpponent * absCaptured;
                 // return the captured piece to the pieces list of the opponent
                 addToPieceList(currentPlayer ^ WHITE_TO_MOVE, absCaptured, toIndex);
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
             final int rankIndex = (3 + toMove) << 4;
             final int leftIndex = rankIndex + enPassant - 1;
             final int rightIndex = leftIndex + 2;
             if (!((leftIndex & 0x88) == 0 && board[leftIndex] == pawn ||
                     (rightIndex & 0x88) == 0 && board[rightIndex] == pawn)) {
                 polyglotZobrist ^= ZOBRIST_EN_PASSANT[(state & EN_PASSANT) >> SHIFT_EN_PASSANT];
             }
         }
         return polyglotZobrist;
     }
 
     public boolean isAttacked(final int index, final int side) {
         final int[] knights = pieces[KNIGHT][side];
         for (int i = knights[0]; i > 0; i--) {
             if ((ATTACK_ARRAY[index - knights[i] + 120] & ATTACK_N) == ATTACK_N) {
                 return true;
             }
         }
         final int kingIdx = pieces[KING][side][1];
         if ((ATTACK_ARRAY[index - kingIdx + 120] & ATTACK_K) > 0) {
             return true;
         }
         if (isAttackedSliding(index, side, ROOK, ATTACK_R)) return true;
         if (isAttackedSliding(index, side, BISHOP, ATTACK_B)) return true;
         if (isAttackedSliding(index, side, QUEEN, ATTACK_Q)) return true;
         if (side == WHITE_TO_MOVE) {
             if (((index + DL) & 0x88) == 0 && board[index + DL] == PAWN) return true;
             if (((index + DR) & 0x88) == 0 && board[index + DR] == PAWN) return true;
         } else {
             if (((index + UL) & 0x88) == 0 && board[index + UL] == -PAWN) return true;
             if (((index + UR) & 0x88) == 0 && board[index + UR] == -PAWN) return true;
         }
         return false;
     }
 
     private boolean isAttackedSliding(final int index, final int side, final int absPiece, final int attackBits) {
         final int[] pieceIndices = pieces[absPiece][side];
         for (int i = pieceIndices[0]; i > 0; i--) {
             if (isAttackedBySliding(index, attackBits, pieceIndices[i])) return true;
         }
         return false;
     }
 
     public void getAttackers(final int index, final int side, final int[] attackers) {
 
         attackers[0] = 0;
         if (side == WHITE_TO_MOVE) {
             if (((index + DL) & 0x88) == 0 && board[index + DL] == PAWN) attackers[++attackers[0]] = index + DL;
             if (((index + DR) & 0x88) == 0 && board[index + DR] == PAWN) attackers[++attackers[0]] = index + DR;
         } else {
             if (((index + UL) & 0x88) == 0 && board[index + UL] == -PAWN) attackers[++attackers[0]] = index + UL;
             if (((index + UR) & 0x88) == 0 && board[index + UR] == -PAWN) attackers[++attackers[0]] = index + UR;
         }
         final int[] knights = pieces[KNIGHT][side];
         for (int i = knights[0]; i > 0; i--) {
             final int knightIndex = knights[i];
             if ((ATTACK_ARRAY[index - knightIndex + 120] & ATTACK_N) == ATTACK_N) {
                 attackers[++attackers[0]] = knightIndex;
             }
         }
         getAttackedSliding(index, side, BISHOP, ATTACK_B, attackers);
         getAttackedSliding(index, side, ROOK, ATTACK_R, attackers);
         getAttackedSliding(index, side, QUEEN, ATTACK_Q, attackers);
         final int kingIndex = pieces[KING][side][1];
         if ((ATTACK_ARRAY[index - kingIndex + 120] & ATTACK_K) > 0) {
             attackers[++attackers[0]] = kingIndex;
         }
     }
 
     private void getAttackedSliding(final int index, final int side, final int absPiece, final int attackBits,
                                     final int[] attackers) {
         final int[] pieceIndices = pieces[absPiece][side];
         for (int i = pieceIndices[0]; i > 0; i--) {
             final int pieceIndex = pieceIndices[i];
             if (isAttackedBySliding(index, attackBits, pieceIndex)) {
                 attackers[++attackers[0]] = pieceIndex;
             }
         }
     }
 
     public boolean isAttackedBySliding(final int targetIndex, final int attackBits, final int pieceIndex) {
         final int attackArrayIndex = targetIndex - pieceIndex + 120;
         final int attackValue = ATTACK_ARRAY[attackArrayIndex];
         if ((attackValue & attackBits) > 0) {
             final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
             int testIndex = pieceIndex + delta;
             while (testIndex != targetIndex && (testIndex & 0x88) == 0 && board[testIndex] == EMPTY) {
                 testIndex += delta;
             }
             if (testIndex == targetIndex) return true;
         }
         return false;
     }
 
     public boolean isAttackedByNonSliding(final int targetIndex, final int attackBits, final int pieceIndex) {
         assert Math.abs(board[pieceIndex]) == KNIGHT || Math.abs(board[pieceIndex]) == KING;
         final int attackArrayIndex = targetIndex - pieceIndex + 120;
         final int attackValue = ATTACK_ARRAY[attackArrayIndex];
         return (attackValue & attackBits) > 0;
     }
 
     public boolean isSliding(final int absPiece) {
         return SLIDING[absPiece];
     }
 
     public int getMinorMajorPieceCount(final int toMove) {
         return pieces[KNIGHT][toMove][0] + pieces[BISHOP][toMove][0] + pieces[ROOK][toMove][0] + pieces[QUEEN][toMove][0];
     }
 
     public boolean attacksKing(final int side) {
         final int kingIndex = pieces[KING][1 - side][1];
         return isAttacked(kingIndex, side);
     }
 
     public boolean isCheckingMove(final int move) {
         final int toMove = state & WHITE_TO_MOVE;
         final int fromIndex = getMoveFromIndex(move);
         final int signum = (toMove << 1) - 1;
         final int piece = board[fromIndex];
         final int absPiece = signum * piece;
         assert absPiece != EMPTY;
         final int kingIndex = pieces[KING][1 - toMove][1];
         final int toIndex = getMoveToIndex(move);
         final int attacked = board[toIndex];
         // if it we are moving on the line to the opponent king and it's not castling or capturing
         // then it's not a checking move (assuming that we start from a legal position)
        if (attacked == EMPTY && !Utils.isCastling(move) &&
                 (ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_Q & ATTACK_ARRAY[kingIndex - fromIndex + 120]) > 0) {
             return false;
         }
 
         if (isDiscoveredCheck(kingIndex, fromIndex, signum)) {
             return true;
         }
         switch (absPiece) {
             case PAWN:
                 final int toRank = getRank(toIndex);
                 if (toRank > 0 && toRank < 7) {
                     final int[] deltas = DELTA_PAWN_ATTACK[toMove];
                     for (int delta: deltas) {
                         if (toIndex + delta == kingIndex) {
                             return true;
                         }
                     }
                 } else {
                     // it's a promotion
                     final int promotedTo = PROMOTION_TO_PIECE[(move & MOVE_TYPE) >> SHIFT_MOVE_TYPE];
                     assert promotedTo == KNIGHT || promotedTo == BISHOP || promotedTo == ROOK || promotedTo == QUEEN;
                     if (promotedTo == KNIGHT) {
                         return ((ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_N) == ATTACK_N);
                     } else {
                         return isAttackedBySliding(kingIndex, ATTACK_BITS[promotedTo], toIndex);
                     }
                 }
                 return false;
             case KNIGHT:
                 return ((ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_N) == ATTACK_N);
             case BISHOP:
             case ROOK:
             case QUEEN:
                 return isAttackedBySliding(kingIndex, ATTACK_BITS[absPiece], toIndex);
             case KING:
                if (Utils.isCastling(move)) {
                     if ((move & MT_CASTLING_QUEENSIDE) > 0) {
                         return isAttackedBySliding(kingIndex, ATTACK_R, toIndex + 1);
                     } else {
                         return isAttackedBySliding(kingIndex, ATTACK_R, toIndex - 1);
                     }
                 }
                 return false;
         }
         return false;
     }
 
     public boolean isDiscoveredCheck(final int kingIndex, final int fromIndex, final int signum) {
         if (isAttackedBySliding(kingIndex, ATTACK_Q, fromIndex)) {
             // search for discovered check
             final int attackValue = ATTACK_ARRAY[fromIndex - kingIndex + 120];
             final int attackBits = attackValue & ATTACK_Q;
             assert attackBits != 0;
             final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
             int testIndex = fromIndex + delta;
             while ((testIndex & 0x88) == 0 && board[testIndex] == EMPTY) {
                 testIndex += delta;
             }
             if ((testIndex & 0x88) == 0) {
                 final int absAttacker = board[testIndex] * signum;
                 if (absAttacker > 0) {
                     return SLIDING[absAttacker] && (ATTACK_BITS[absAttacker] & attackBits) > 0;
                 }
             }
         }
         return false;
     }
 }
