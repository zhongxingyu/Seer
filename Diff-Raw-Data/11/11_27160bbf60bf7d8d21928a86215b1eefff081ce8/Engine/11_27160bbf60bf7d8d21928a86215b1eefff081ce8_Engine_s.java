 package sf.pnr.base;
 
 import sf.pnr.alg.TranspositionTable;
 
 import java.util.Arrays;
 
 import static sf.pnr.alg.TranspositionTable.*;
 import static sf.pnr.base.Evaluation.*;
 import static sf.pnr.base.Utils.*;
 
 public final class Engine {
 
     public static final int INITIAL_ALPHA = -Evaluation.VAL_MATE - 1;
     public static final int INITIAL_BETA = Evaluation.VAL_MATE + 1;
     public static final int ASPIRATION_WINDOW = 50;
 
     private static final int[] NO_MOVE_ARRAY = new int[] {0};
     private static final int VAL_CHECK_BONUS = 500 << SHIFT_MOVE_VALUE;
     private static final int VAL_BLOCKED_CHECK_BONUS = 100 << SHIFT_MOVE_VALUE;
     private static final int VAL_FUTILITY_THRESHOLD = 400;
     private static final int VAL_DEEP_FUTILITY_THRESHOLD = 675;
     private static final int LATE_MOVE_REDUCTION_MIN_DEPTH = 3 << SHIFT_PLY;
     private static final int LATE_MOVE_REDUCTION_MIN_MOVE = 4;
 
     private static final int DEPTH_EXT_CHECK = PLY;
     private static final int DEPTH_EXT_7TH_RANK_PAWN = PLY >> 1;
 
     private int searchDepth;
 
     private enum SearchStage {TRANS_TABLE, CAPTURES_WINNING, PROMOTION, KILLERS, NORMAL, CAPTURES_LOOSING}
     private static final SearchStage[] searchStages = SearchStage.values();
 
     private final MoveGenerator moveGenerator = new MoveGenerator();
     private final Evaluation evaluation = new Evaluation();
     private final TranspositionTable transpositionTable;
     private final int[][][] history;
     private final int[][] killerMoves = new int[MAX_SEARCH_DEPTH][2]; 
     private long searchStartTime;
     private long searchEndTime;
     private long lastCheckTime;
     private long nodeCount;
     private long nodeCountAtNextTimeCheck;
     private volatile boolean cancelled;
     private int historyMax = 0;
     private BestMoveListener listener;
     private int age;
 
     public Engine() {
         transpositionTable = new TranspositionTable();
         history = new int[14][64][64];
     }
 
     public long search(final Board board, int maxDepth, final long timeLeft) {
         age = (board.getState() & FULL_MOVES) >> SHIFT_FULL_MOVES;
         if (maxDepth == 0) {
             assert timeLeft > 0;
             maxDepth = MAX_SEARCH_DEPTH;
         }
         searchStartTime = System.currentTimeMillis();
         if (timeLeft == 0) {
             searchEndTime = Long.MAX_VALUE;
             nodeCountAtNextTimeCheck = Long.MAX_VALUE;
         } else {
             lastCheckTime = searchStartTime;
             searchEndTime = lastCheckTime + timeLeft;
             // assume we can easily do 5 nodes / ms
             nodeCountAtNextTimeCheck = timeLeft << 2;
         }
         nodeCount = 0;
         cancelled = false;
         for (int[] array: killerMoves) {
             Arrays.fill(array, 0);
         }
         int value = Evaluation.VAL_DRAW;
         long searchResult = getSearchResult(0, value);
         for (int depth = 1; depth <= maxDepth; depth++) {
             final int alpha;
             final int beta;
             if (depth > 1) {
                 alpha = value - ASPIRATION_WINDOW;
                 beta = value + ASPIRATION_WINDOW;
             } else {
                 alpha = INITIAL_ALPHA;
                 beta = INITIAL_BETA;
             }
             searchDepth = depth << SHIFT_PLY;
             long result = negascoutRoot(board, searchDepth, alpha, beta);
             if (cancelled) {
                 final int move = getMoveFromSearchResult(result);
                 if (move != 0) {
                     searchResult = result;
                 }
                 break;
             }
             value = getValueFromSearchResult(result);
             if (value <= alpha) {
                 result = negascoutRoot(board, searchDepth, INITIAL_ALPHA, alpha);
                 value = getValueFromSearchResult(result);
             } else if (value >= beta) {
                 result = negascoutRoot(board, searchDepth, beta, INITIAL_BETA);
                 value = getValueFromSearchResult(result);
             }
             assert cancelled || value == 0 || getMoveFromSearchResult(result) != 0;
             if (cancelled) {
                 final int move = getMoveFromSearchResult(result);
                 if (move != 0) {
                     searchResult = result;
                 }
                 break;
             }
             searchResult = result;
             if (listener != null) {
                 listener.bestMoveChanged(depth, getMoveFromSearchResult(result), value,
                     System.currentTimeMillis() - searchStartTime,
                     getBestLine(board, getMoveFromSearchResult(searchResult)), nodeCount);
             }
             if (value > VAL_MATE - 200) {
                 break;
             }
             if ((System.currentTimeMillis() - searchStartTime) > (searchEndTime - searchStartTime) * 0.9) {
                 break;
             }
         }
         assert getMoveFromSearchResult(searchResult) != 0 || getValueFromSearchResult(searchResult) == 0;
         return searchResult;
     }
 
     public long negascoutRoot(final Board board, final int depth, int alpha, final int beta) {
         if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
             // three-fold repetition
             return VAL_DRAW;
         }
         
         boolean hasLegalMove = false;
         int b = beta;
 
         final long zobristKey = board.getZobristKey();
         final long ttValue = transpositionTable.read(zobristKey);
         int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         int bestMove = 0;
         if (ttValue != 0) {
             final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
                 if (ttDepth >= (depth >> SHIFT_PLY)) {
                     assert ttMove != 0;
                     return getSearchResult(ttMove, value);
                 }
             } else if (ttType == TT_TYPE_ALPHA_CUT || ttType == TT_TYPE_BETA_CUT) {
                 final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
                 if (ttDepth >= (depth >> SHIFT_PLY)) {
                     if (value > VAL_MATE - 200) {
                         assert ttMove != 0;
                         return getSearchResult(ttMove, value);
                     }
                     alpha = value;
                     bestMove = ttMove;
                 }
             }
         }
 
         if (ttMove == 0 && depth > 3 * PLY) {
             // internal iterative deepening
             final long searchResult = negascoutRoot(board, depth / 2, alpha, beta);
             ttMove = getMoveFromSearchResult(searchResult);
         }
 
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
         final boolean inCheck = attacksKing(board, 1 - toMove);
 
         moveGenerator.pushFrame();
         int moveCount = 0;
         for (SearchStage searchStage: searchStages) {
             final boolean quiescenceChild =
                 depth < (2 << SHIFT_PLY) && (searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING);
             final int[] moves = getMoves(searchStage, board, ttMove, depth);
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 if (attacksKing(board, 1 - toMove)) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 int a = alpha + 1;
                 if (searchStage == SearchStage.NORMAL || searchStage == SearchStage.CAPTURES_LOOSING) {
                     if (moveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                             ((move & MT_CASTLING) == 0) && !attacksKing(board, toMove)) {
                         a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true);
                         if (cancelled) {
                             board.takeBack(undo);
                             moveGenerator.popFrame();
                             return moveCount > 5? getSearchResult(bestMove, alpha): getSearchResult(0, alpha);
                         }
                     }
                 }
                moveCount++;
 
                 // evaluate the move
                 if (a > alpha) {
                     a = -negascout(board, depth - PLY, -b, -alpha, quiescenceChild, hasLegalMove);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return moveCount > 5? getSearchResult(bestMove, alpha): getSearchResult(0, alpha);
                     }
 
                     // the other player has a better option, beta cut off
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, depth, searchStage, move);
                         assert move != 0;
                         return getSearchResult(move, a);
                     }
                 }
 
                 if (a >= b) {
                     // null-window was too narrow, try a full search
                     a = -negascout(board, depth - PLY, -beta, -a, quiescenceChild, hasLegalMove);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return moveCount > 5? getSearchResult(bestMove, alpha): getSearchResult(0, alpha);
                     }
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, depth, searchStage, move);
                         assert move != 0;
                         return getSearchResult(move, a);
                     }
                     if (a < beta && a > alpha) {
                         transpositionTable.set(zobristKey, TT_TYPE_EXACT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     }
                 }
 
                 // register that we had a legal move
                 hasLegalMove = true;
                 board.takeBack(undo);
 
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     if (alpha > VAL_MATE - 200) {
                         break;
                     }
                 }
 
                 b = alpha + 1;
             }
             if (alpha > VAL_MATE - 200) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (!hasLegalMove) {
             if (inCheck) {
                 return ((long) -Evaluation.VAL_MATE) & 0xFFFFFFFFL;
             } else {
                 return 0;
             }
         }
         if (bestMove != 0 && bestMove != ttMove) {
             transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, depth >> SHIFT_PLY, alpha - VAL_MIN, age);
         }
         return getSearchResult(bestMove, alpha);
     }
 
     public int negascout(final Board board, final int depth, int alpha, int beta, final boolean quiescence,
                          final boolean allowNull) {
         nodeCount++;
         if (depth < PLY) {
             final int eval;
             if (!quiescence) {
                 eval = evaluation.evaluate(board);
             } else {
                 eval = quiescence(board, alpha, beta);
             }
             return eval;
         }
         if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
             // three-fold repetition
             return VAL_DRAW;
         }
 
         // check the time
         if (nodeCount >= nodeCountAtNextTimeCheck) {
             calculateNextTimeCheck();
             if (cancelled) {
                 return alpha;
             }
         }
 
         final long zobristKey = board.getZobristKey();
         final long ttValue = transpositionTable.read(zobristKey);
         if (ttValue != 0) {
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
                 if (ttDepth >= (depth >> SHIFT_PLY)) {
                     assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                     return (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
                 }
             } else if (ttType == TT_TYPE_ALPHA_CUT || ttType == TT_TYPE_BETA_CUT) {
                 final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
                 if (ttDepth >= (depth >> SHIFT_PLY)) {
                     alpha = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
                     if (alpha > VAL_MATE - 200) {
                         return alpha;
                     }
                 }
             }
         }
 
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
         final boolean inCheck = attacksKing(board, 1 - toMove);
 
         // null-move pruning
         if (depth > (3 << SHIFT_PLY) && !inCheck && allowNull && beta < VAL_MATE - 200 &&
                 board.getMinorMajorPieceCount(toMove) > 0) {
             final int r = (depth > (6 << SHIFT_PLY)? 3: 2) << SHIFT_PLY;
             final int prevState = board.nullMove();
             final int value = -negascout(board, depth - r, -beta, -beta + 1, false, false);
             if (cancelled) {
                 board.nullMove(prevState);
                 return alpha;
             }
// TODO:    if (value >= beta) {
            if (value > beta) {
                 board.nullMove(prevState);
                 transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, 0, depth >> SHIFT_PLY, value - VAL_MIN, age);
                 return beta;
             }
             // TODO: mate threat detection
 //            final int value2 = -negascout(board, depth - r, -VAL_MATE / 2, -VAL_MATE, false, false);
             board.nullMove(prevState);
         }
 
         int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         if (ttMove == 0 && depth > 3 * PLY) {
             // internal iterative deepening
             final long searchResult = negascoutRoot(board, depth / 2, alpha, beta);
             ttMove = getMoveFromSearchResult(searchResult);
         }
 
         // futility pruning
         final boolean futility;
         if (depth < (3 << SHIFT_PLY) && !inCheck) {
             final int value = board.getMaterialValue();
             if (depth < (2 << SHIFT_PLY)) {
                 futility = value < alpha - VAL_FUTILITY_THRESHOLD;
             } else {
                 futility = value < alpha - VAL_DEEP_FUTILITY_THRESHOLD;
             }
         } else {
             futility = false;
         }
 
         moveGenerator.pushFrame();
         boolean hasLegalMove = false;
         boolean hasEvaluatedMove = false;
         int b = beta;
         int bestMove = 0;
         int moveCount = 0;
         for (SearchStage searchStage: searchStages) {
             final boolean startQuiescence =
                 depth < (2 << SHIFT_PLY) && (searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING);
             final boolean allowToRecurseDown = !futility ||
                 (searchStage == SearchStage.TRANS_TABLE || searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION);
 
             final int[] moves = getMoves(searchStage, board, ttMove, depth);
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
 
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 if (attacksKing(board, 1 - toMove)) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 // register that we had a legal move
                 hasLegalMove = true;
 
                 final boolean opponentInCheck = attacksKingAfterMove(board, toMove, move);
                 if (!allowToRecurseDown && !opponentInCheck) {
                     board.takeBack(undo);
                     if (nodeCount >= nodeCountAtNextTimeCheck) {
                         calculateNextTimeCheck();
                         if (cancelled) {
                             moveGenerator.popFrame();
                             return alpha;
                         }
                     }
                     break;
                 }
 
                 int a = alpha + 1;
                 if (searchStage == SearchStage.NORMAL || searchStage == SearchStage.CAPTURES_LOOSING) {
                     if (moveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                             ((move & MT_CASTLING) == 0) && !opponentInCheck) {
                         a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true);
                         if (cancelled) {
                             board.takeBack(undo);
                             moveGenerator.popFrame();
                             return alpha;
                         }
                     }
                 }
                moveCount++;
 
                 int depthExt = 0;
                 if (opponentInCheck) {
                     depthExt += DEPTH_EXT_CHECK;
                 }
                 final int toIndex = getMoveToIndex(move);
                 if (getRank(toIndex) == 1 || getRank(toIndex) == 6) {
                     final int piece = board.getBoard()[toIndex];
                     final int signum = (toMove << 1) - 1;
                     final int absPiece = signum * piece;
                     if (absPiece == PAWN) {
                         depthExt += DEPTH_EXT_7TH_RANK_PAWN;
                     }
                 }
 
                 // evaluate the move
                 if (a > alpha) {
                     a = -negascout(board, depth - PLY + depthExt, -b, -alpha, startQuiescence, true);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return alpha;
                     }
                     // the other player has a better option, beta cut off
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, depth, searchStage, move);
                         return a;
                     }
                 }
 
                 hasEvaluatedMove = true;
 
                 if (a >= b) {
                     // null-window was too narrow, try a full search
                     a = -negascout(board, depth - PLY + depthExt, -beta, -a, startQuiescence, true);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return alpha;
                     }
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, depth, searchStage, move);
                         return a;
                     }
                     if (a < beta && a > alpha && !startQuiescence) {
                         transpositionTable.set(zobristKey, TT_TYPE_EXACT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     }
                 }
                 board.takeBack(undo);
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     if (alpha > VAL_MATE - 200) {
                         break;
                     }
                 }
 
                 b = alpha + 1;
             }
             if (hasLegalMove && alpha > VAL_MATE - 200) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (!hasLegalMove) {
             if (inCheck) {
                 return -Evaluation.VAL_MATE;
             } else {
                 return 0;
             }
         } else if (!hasEvaluatedMove) {
             final int value = evaluation.evaluate(board);
             if (value > alpha) {
                 alpha = value;
             }
             if (alpha > beta) {
                 alpha = beta;
             }
         }
 
         if (bestMove != 0) {
             transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, depth >> SHIFT_PLY, alpha - VAL_MIN, age);
         }
         return alpha;
     }
 
     public int quiescence(final Board board, int alpha, int beta) {
         nodeCount++;
         final int eval = evaluation.evaluate(board);
         if (eval > alpha) {
             alpha = eval;
             if (alpha >= beta) {
                 return beta;
             }
         }
         if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
             // three-fold repetition
             return VAL_DRAW;
         }
 
         // check the time
         if (nodeCount >= nodeCountAtNextTimeCheck) {
             calculateNextTimeCheck();
             if (cancelled) {
                 return alpha;
             }
         }
 
         final long zobristKey = board.getZobristKey();
         final long ttValue = transpositionTable.read(zobristKey);
         if (ttValue != 0) {
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                 return (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
             } else if (ttType == TT_TYPE_ALPHA_CUT || ttType == TT_TYPE_BETA_CUT) {
                 alpha = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
                 if (alpha > VAL_MATE - 200) {
                     return alpha;
                 }
             }
         }
 
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
 
         moveGenerator.pushFrame();
         boolean hasLegalMove = false;
         int b = beta;
         int bestMove = 0;
         for (SearchStage searchStage: searchStages) {
             final int[] moves = getMoves(searchStage, board, 0, 0);
 
             final boolean allowToRecurseDown = (searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION);
 
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
 
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 if (attacksKing(board, 1 - toMove)) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 // register that we had a legal move
                 hasLegalMove = true;
 
                 if (!allowToRecurseDown) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 // evaluate the move
                 int a = -quiescence(board, -b, -alpha);
 
                 if (cancelled) {
                     board.takeBack(undo);
                     moveGenerator.popFrame();
                     return alpha;
                 }
 
                 // the other player has a better option, beta cut off
                 if (a >= beta) {
                     board.takeBack(undo);
                     moveGenerator.popFrame();
                     assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                     transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, 0, a - VAL_MIN, age);
                     addMoveToHistoryTable(board, 0, searchStage, move);
                     return a;
                 }
                 if (a >= b) {
                     // null-window was too narrow, try a full search
                     a = -quiescence(board, -beta, -a);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return alpha;
                     }
                     if (a < beta && a > alpha) {
                         transpositionTable.set(zobristKey, TT_TYPE_EXACT, move, 0, a - VAL_MIN, age);
                     }
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, 0, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, 0, searchStage, move);
                         return a;
                     }
                 }
                 board.takeBack(undo);
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, 0, a - VAL_MIN, age);
                     if (alpha > VAL_MATE - 200) {
                         break;
                     }
                 }
 
                 b = alpha + 1;
             }
             if (hasLegalMove && alpha > VAL_MATE - 200) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (!hasLegalMove) {
             final boolean inCheck = attacksKing(board, 1 - toMove);
             if (inCheck) {
                 return -Evaluation.VAL_MATE;
             } else {
                 return 0;
             }
         }
 
         if (bestMove != 0) {
             transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, 0, alpha - VAL_MIN, age);
         }
         return alpha;
     }
 
     private void addMoveToHistoryTable(final Board board, final int depth, final SearchStage searchStage, final int move) {
         final int fromIndex = getMoveFromIndex(move);
         final int toIndex = getMoveToIndex(move);
         final int pieceHistoryIdx = board.getBoard()[fromIndex] + 7;
         final int fromIndex64 = convert0x88To64(fromIndex);
         final int toIndex64 = convert0x88To64(toIndex);
         history[pieceHistoryIdx][fromIndex64][toIndex64]++;
         if (history[pieceHistoryIdx][fromIndex64][toIndex64] > historyMax) {
             historyMax = history[pieceHistoryIdx][fromIndex64][toIndex64];
         }
         if (searchStage == SearchStage.NORMAL) {
             final int fromTo = move & FROM_TO;
             final int searchedDepth = (searchDepth - depth) >> SHIFT_PLY;
             if (killerMoves[searchedDepth][0] != fromTo) {
                 killerMoves[searchedDepth][1] = killerMoves[searchedDepth][0];
                 killerMoves[searchedDepth][0] = fromTo;
             }
         }
     }
 
     private void calculateNextTimeCheck() {
         final long currentTime = System.currentTimeMillis();
         if (searchEndTime <= currentTime) {
             cancelled = true;
 //            System.out.printf("info string Cancelling after %d ms (node count: %d)\r\n", currentTime - searchStartTime, nodeCount);
             return;
         }
         long timeEllapsed = currentTime - searchStartTime;
         if (timeEllapsed < 10) {
             timeEllapsed = 10;
         }
         final long timeLeft = searchEndTime - currentTime;
         long nodesToProcessUntilNextCheck = (timeLeft * nodeCount / timeEllapsed) >>> 1;
         if (nodesToProcessUntilNextCheck < 200) {
             nodesToProcessUntilNextCheck = 200;
         }
         nodeCountAtNextTimeCheck = nodeCount + nodesToProcessUntilNextCheck;
         lastCheckTime = currentTime;
     }
 
     public static boolean attacksKing(final Board board, final int side) {
         final int kingIndex = board.getKing(1 - side);
         assert Math.abs(board.getBoard()[kingIndex]) == KING;
         return board.isAttacked(kingIndex, side);
     }
 
     public static boolean attacksKingAfterMove(final Board board, final int side, final int move) {
         final int kingIndex = board.getKing(1 - side);
         final int fromIndex = getMoveFromIndex(move);
         final int[] boardArray = board.getBoard();
         final int signum = (side << 1) - 1;
         final int toIndex = getMoveToIndex(move);
         final int piece = boardArray[toIndex];
         final int absPiece = signum * piece;
         if (absPiece == KING) {
             return false;
         }
         assert absPiece != EMPTY;
         if (isDiscoveredCheck(board, kingIndex, fromIndex, signum)) return true;
         switch (absPiece) {
             case PAWN:
                 final int[] deltas = DELTA_PAWN_ATTACK[side];
                 for (int delta: deltas) {
                     if (toIndex + delta == kingIndex) {
                         return true;
                     }
                 }
                 return false;
             case KNIGHT:
                 return ((ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_N) == ATTACK_N);
             case BISHOP:
             case ROOK:
             case QUEEN:
                 return board.isAttackedBySliding(kingIndex, ATTACK_BITS[absPiece], toIndex);
         }
         assert false;
         return false;
     }
 
     public static boolean isDiscoveredCheck(final Board board, final int kingIndex, final int fromIndex, final int signum) {
         if (board.isAttackedBySliding(kingIndex, ATTACK_Q, fromIndex)) {
             // search for discovered check
             final int[] boardArray = board.getBoard();
             final int attackValue = ATTACK_ARRAY[fromIndex - kingIndex + 120];
             final int attackBits = attackValue & ATTACK_Q;
             assert attackBits != 0;
             final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
             int testIndex = fromIndex + delta;
             while ((testIndex & 0x88) == 0 && boardArray[testIndex] == EMPTY) {
                 testIndex += delta;
             }
             if ((testIndex & 0x88) == 0 && boardArray[testIndex] * signum > 0) return true;
         }
         return false;
     }
 
     private int[] getMoves(final SearchStage searchStage, final Board board, final int ttMove, final int depth) {
         final int[] moves;
         switch (searchStage) {
             case TRANS_TABLE:
                 if (ttMove > 0 && (ttMove & BASE_INFO) > 0) {
                     moves = new int[2];
                     moves[0] = 1;
                     moves[1] = ttMove;
                 } else {
                     moves = NO_MOVE_ARRAY;
                 }
                 return moves;
             case CAPTURES_WINNING:
                 moveGenerator.generatePseudoLegalMoves(board);
                 moves = moveGenerator.getWinningCaptures();
                 break;
             case PROMOTION:
                 moveGenerator.generatePseudoLegalMovesNonAttacking(board);
                 moves = moveGenerator.getPromotions();
                 break;
             case KILLERS:
                 moves = new int[3];
                 int killerCount = 0;
                 final int searchedDepth = (searchDepth - depth) >> SHIFT_PLY;
                 if (killerMoves[searchedDepth][0] > 0) {
                     int move = killerMoves[searchedDepth][0];
                     if (isValidKillerMove(board, getMoveFromIndex(move), getMoveToIndex(move))) {
                         moves[++killerCount] = move;
                     }
                     if (killerMoves[searchedDepth][1] > 0) {
                         move = killerMoves[searchedDepth][1];
                         if (isValidKillerMove(board, getMoveFromIndex(move), getMoveToIndex(move))) {
                             moves[++killerCount] = move;
                         }
                     }
                     moves[0] = killerCount;
                 }
                 break;
             case NORMAL:
                 moves = moveGenerator.getMoves();
                 break;
             case CAPTURES_LOOSING:
                 moves = moveGenerator.getLoosingCaptures();
                 break;
             default:
                 throw new IllegalStateException("Unknow move generation stage: " + searchStage.name());
         }
         if (moves[0] > 0) {
             addMoveValuesAndRemoveTTMove(moves, board, ttMove,
                 searchStage == SearchStage.NORMAL? killerMoves[(searchDepth - depth) >> SHIFT_PLY]: NO_MOVE_ARRAY);
             Arrays.sort(moves, 1, moves[0] + 1);
         }
         return moves;
     }
 
     private void addMoveValuesAndRemoveTTMove(final int[] moves, final Board board, final int ttMove, final int[] killers) {
         int shift = 0;
         final int leadingZeros = Integer.numberOfLeadingZeros(historyMax);
         if (leadingZeros < 24) {
             // normalise history counts
             shift = 24 - leadingZeros;
         }
         final int toMove = board.getState() & WHITE_TO_MOVE;
         final int kingIndex = board.getKing(1 - toMove);
         final int signum = (toMove << 1) - 1;
         final int stage = board.getStage();
         for (int i = moves[0]; i > 0; i--) {
             final int move = moves[i];
             if ((move & BASE_INFO) != ttMove && !isKiller(killers, move)) {
                 final int fromIndex = getMoveFromIndex(move);
                 final int toIndex = getMoveToIndex(move);
                 final int fromIndex64 = convert0x88To64(fromIndex);
                 final int toIndex64 = convert0x88To64(toIndex);
                 final int piece = board.getBoard()[fromIndex];
                 final int historyValue = history[piece + 7][fromIndex64][toIndex64] >>> shift;
                 final int absPiece = piece * signum;
                 int checkBonus = 0;
                 final boolean sliding = board.isSliding(absPiece);
                 if (sliding && ((ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_BITS[absPiece]) > 0)) {
                     if (board.isAttackedBySliding(kingIndex, ATTACK_BITS[absPiece], toIndex)) {
                         checkBonus = VAL_CHECK_BONUS;
                     } else {
                         checkBonus = VAL_BLOCKED_CHECK_BONUS;
                     }
                 } else if (absPiece == KNIGHT && (ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_N) == ATTACK_N) {
                     checkBonus = VAL_CHECK_BONUS;
                 } else if (absPiece == PAWN) {
                     final int rowUp = toIndex + signum * UP;
                     checkBonus = ((rowUp - 1) == kingIndex || (rowUp + 1) == kingIndex)? VAL_CHECK_BONUS: 0;
                 }
                 if (checkBonus < VAL_CHECK_BONUS && isDiscoveredCheck(board, kingIndex, fromIndex, signum)) {
                     checkBonus = VAL_CHECK_BONUS;
                 }
                 final int positionalGain = Evaluation.computePositionalGain(absPiece, toMove, fromIndex, toIndex, stage);
                 final int valPositional;
                 if (positionalGain < 0) {
                     valPositional = 0;
                 } else {
                     valPositional = (positionalGain >> 2) << SHIFT_MOVE_VALUE;
                 }
                 moves[i] = (historyValue << SHIFT_MOVE_VALUE) + move + checkBonus + valPositional;
             } else {
                 moves[i] = moves[moves[0]];
                 moves[0]--;
             }
         }
     }
 
     private boolean isKiller(final int[] killers, final int move) {
         final int fromTo = move & FROM_TO;
         for (int killer: killers) {
             if (killer == fromTo) {
                 return true;
             }
         }
         return false;
     }
 
     public void setSearchEndTime(final long searchEndTime) {
         this.searchEndTime = searchEndTime;
     }
 
     public long getNodeCount() {
         return nodeCount;
     }
 
     public void clear() {
         transpositionTable.clear();
         for (int[][] arrays: history) {
             for (int[] array: arrays) {
                 Arrays.fill(array, 0);
             }
         }
         historyMax = 0;
     }
 
     public int[] getBestLine(final Board board) {
         return getBestLine(board, 0);
     }
 
     public int[] getBestLine(final Board board, final int defaultMove) {
         long zobristKey = board.getZobristKey();
         long ttValue = transpositionTable.read(zobristKey);
         final int depth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
         int move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         if (move == 0) {
             if (defaultMove == 0) {
                 throw new IllegalStateException("Failed to extract valid first move");
             } else {
                 return new int[] {defaultMove};
             }
         }
         final int[] line = new int[depth];
         final long[] undos = new long[depth];
         int len = 1;
         for (int i = 0; i < (depth - 1) && move != 0; i++, len++) {
             line[i] = move;
             undos[i] = board.move(move);
             zobristKey = board.getZobristKey();
             ttValue = transpositionTable.read(zobristKey);
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
             } else {
                 move = 0;
             }
         }
         if (move != 0) {
             line[len - 1] = move;
         } else {
             len--;
         }
         for (int i = len - 1; i >= 0; i--) {
             final long undo = undos[i];
             if (undo != 0) {
                 board.takeBack(undo);
             }
         }
         final int[] result;
         if (len < depth) {
             result = new int[len];
             System.arraycopy(line, 0, result, 0, len);
         } else {
             result = line;
         }
         return result;
     }
 
     public void cancel() {
         cancelled = true;
     }
 
     public void setRandomEval(final boolean random) {
         evaluation.setRandom(random);
     }
 
     public void setBestMoveListener(final BestMoveListener listener) {
         this.listener = listener;
     }
 
     public void setSearchDepth(final int searchDepth) {
         this.searchDepth = searchDepth;
     }
  
     public static int getValueFromSearchResult(final long result) {
         return (int) (result & 0xFFFFFFFFL);
     }
 
     public static int getMoveFromSearchResult(final long result) {
         return (int) (result >>> 32);
     }
 
     public static long getSearchResult(final int move, final int value) {
         return (((long) (move & BASE_INFO)) << 32) | (((long) value) & 0xFFFFFFFFL);
     }
 
     public boolean isValidKillerMove(final Board boardObj, final int fromIndex, final int toIndex) {
         final int[] board = boardObj.getBoard();
         final int piece = board[fromIndex];
         if (piece == EMPTY || board[toIndex] != EMPTY) {
             return false;
         }
         final int toMove = boardObj.getState() & WHITE_TO_MOVE;
         final int signum = Integer.signum(piece);
         if (((toMove << 1) - 1) != signum) {
             return false;
         }
         final int absPiece = signum * piece;
         if (boardObj.isSliding(absPiece)) {
             return boardObj.isAttackedBySliding(toIndex, ATTACK_BITS[absPiece], fromIndex);
         }
         if (absPiece == PAWN) {
             // intentionally ignored: toIndex - fromIndex == signum * UP * 2!
             return toIndex - fromIndex == signum * UP;
         } else {
             return boardObj.isAttackedByNonSliding(toIndex, ATTACK_BITS[absPiece], fromIndex);
         }
     }
 }
