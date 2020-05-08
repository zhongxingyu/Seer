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
     private static final int MOVE_ORDER_CHECK_BONUS = 500;
     private static final int MOVE_ORDER_BLOCKED_CHECK_BONUS = 100;
     private static final int MOVE_ORDER_7TH_RANK_PAWN = 300;
     @Configurable(Configurable.Key.ENGINE_NULL_MOVE_MIN_DEPTH)
     private static int NULL_MOVE_MIN_DEPTH = 3 * PLY;
     @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_CHANGE_THRESHOLD)
     private static int NULL_MOVE_DEPTH_CHANGE_THRESHOLD = 6 * PLY;
     @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_HIGH)
     private static int NULL_MOVE_DEPTH_HIGH = 3 * PLY;
     @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_LOW)
     private static int NULL_MOVE_DEPTH_LOW = 2 * PLY;
     @Configurable(Configurable.Key.ENGINE_FUTILITY_THRESHOLD)
     private static int VAL_FUTILITY_THRESHOLD = 300;
     @Configurable(Configurable.Key.ENGINE_DEEP_FUTILITY_THRESHOLD)
     private static int VAL_DEEP_FUTILITY_THRESHOLD = 550;
     @Configurable(Configurable.Key.ENGINE_RAZORING_THRESHOLD)
     private static int VAL_RAZORING_THRESHOLD = 125;
     @Configurable(Configurable.Key.ENGINE_LMR_MIN_DEPTH)
     private static int LATE_MOVE_REDUCTION_MIN_DEPTH = 2 << SHIFT_PLY;
     @Configurable(Configurable.Key.ENGINE_LMR_MIN_MOVE)
     private static int LATE_MOVE_REDUCTION_MIN_MOVE = 3; //4?
 
     @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_CHECK)
     private static int DEPTH_EXT_CHECK = 16;
     @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_7TH_RANK_PAWN)
     private static int DEPTH_EXT_7TH_RANK_PAWN = 8;
     @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_MATE_THREAT)
     private static int DEPTH_EXT_MATE_THREAT = 20;
     @Configurable(Configurable.Key.ENGINE_ITERATIVE_DEEPENING_TIME_LIMIT)
     private static double ITERATIVE_DEEPENING_TIME_LIMIT = 0.9;
 
     private enum SearchStage {TRANS_TABLE, CAPTURES_WINNING, PROMOTION, KILLERS, NORMAL, CAPTURES_LOOSING}
     private static final SearchStage[] searchStages = SearchStage.values();
 
     private final MoveGenerator moveGenerator = new MoveGenerator();
     private final Evaluation evaluation = new Evaluation();
     private final TranspositionTable transpositionTable = new TranspositionTable();
     private final int[][][] history = new int[14][64][64];
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
             long result = negascoutRoot(board, depth << SHIFT_PLY, alpha, beta, 0);
             if (cancelled) {
                 final int move = getMoveFromSearchResult(result);
                 if (move != 0) {
                     searchResult = result;
                 }
                 break;
             }
             value = getValueFromSearchResult(result);
             if (value <= alpha) {
                 result = negascoutRoot(board, depth << SHIFT_PLY, INITIAL_ALPHA, alpha, 0);
                 value = getValueFromSearchResult(result);
             } else if (value >= beta) {
                 result = negascoutRoot(board, depth << SHIFT_PLY, beta, INITIAL_BETA, 0);
                 value = getValueFromSearchResult(result);
             }
             if (cancelled) {
                 final int move = getMoveFromSearchResult(result);
                 if (move != 0) {
                     searchResult = result;
                 }
                 break;
             }
             assert value == 0 || getMoveFromSearchResult(result) != 0 || value < -VAL_MATE_THRESHOLD:
                 "FEN: " + StringUtils.toFen(board) + ", score: " + value + ", depth: " + depth;
             searchResult = result;
             if (listener != null) {
                 final int bestMove = getMoveFromSearchResult(result);
                 assert bestMove != 0: StringUtils.toFen(board) + " / depth: " + depth + " / value: " + getValueFromSearchResult(result);
                 if (bestMove != 0) {
                     listener.bestMoveChanged(depth, bestMove, value, System.currentTimeMillis() - searchStartTime,
                         getBestLine(board, bestMove), nodeCount);
                 }
             }
             if (value > VAL_MATE_THRESHOLD) {
                 break;
             }
             if ((System.currentTimeMillis() - searchStartTime) > (searchEndTime - searchStartTime) * ITERATIVE_DEEPENING_TIME_LIMIT) {
                 break;
             }
         }
         assert getMoveFromSearchResult(searchResult) != 0 || getValueFromSearchResult(searchResult) == 0;
         return searchResult;
     }
 
     public long negascoutRoot(final Board board, final int depth, int alpha, final int beta, final int searchedPly) {
         if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
             // three-fold repetition
             return VAL_DRAW;
         }
         
         final long zobristKey = board.getZobristKey();
         final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
         int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         final int ttDepth = (int) (((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH) << SHIFT_PLY);
         if (ttValue != 0 && ttDepth >= depth) {
             final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 assert ttMove != 0;
                 return getSearchResult(ttMove, value);
             } else {
                 if (value > VAL_MATE_THRESHOLD) {
                     assert ttMove != 0;
                     return getSearchResult(ttMove, value);
                 }
                 alpha = value;
             }
         }
 
         if (depth > 3 * PLY && (ttMove == 0 || ttDepth < depth / 2)) {
             // internal iterative deepening
             final long searchResult = negascoutRoot(board, depth / 2, alpha, beta, searchedPly);
             ttMove = getMoveFromSearchResult(searchResult);
         }
 
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
         final boolean inCheck = board.attacksKing(1 - toMove);
 
         moveGenerator.pushFrame();
         int b = beta;
         final int origAlpha = alpha;
         int bestMove = ttMove;
         int legalMoveCount = 0;
         int quietMoveCount = 0;
         for (SearchStage searchStage: searchStages) {
             final boolean highPriorityStage =
                 searchStage != SearchStage.NORMAL && searchStage != SearchStage.CAPTURES_LOOSING;
             final int[] moves = getMoves(searchStage, board, ttMove, searchedPly);
             final boolean allowQuiescence = (searchStage == SearchStage.CAPTURES_WINNING ||
                 searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING ||
                 searchStage == SearchStage.TRANS_TABLE && moves[0] == 1 &&
                     (MoveGenerator.isCapture(board, moves[1]) || MoveGenerator.isPromotion(board, moves[1])));
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 // TODO instead: check if isCheck is true and the current move avoids the check or
                 // TODO if the move causes discovered check
                 if (board.attacksKing(1 - toMove)) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 // register that we had a legal move
                 legalMoveCount++;
 
                 final boolean opponentInCheck = (move & CHECKING) > 0;
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
 
                 // razoring
                 if (depthExt == 0 && depth <= (3 << SHIFT_PLY) && legalMoveCount > 1 && beta < VAL_MATE_THRESHOLD) {
                     final int value = -board.getMaterialValue();
                     if (value < beta - VAL_RAZORING_THRESHOLD) {
 //                        final int qscore = -quiescence(board, -b, -alpha);
                         final int qscore = board.getRepetitionCount() < 3? -quiescence(board, -b, -alpha): 0;
 //                        final int qscore = -negascout(board, PLY, -b, -alpha, false, false, searchedPly + 1);
                         if (cancelled) {
                             moveGenerator.popFrame();
                             board.takeBack(undo);
                             return bestMove > 0 && (depth <= PLY || legalMoveCount > 5)? getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                         }
                         if (qscore < b) {
                             board.takeBack(undo);
                             continue;
                         }
                     }
                 }
 
                 int a = alpha + 1;
                 if (!highPriorityStage) {
                     if (quietMoveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                             !Utils.isCastling(move) && !opponentInCheck && depthExt == 0) {
                         a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true, searchedPly + 1);
                         if (cancelled) {
                             board.takeBack(undo);
                             moveGenerator.popFrame();
                             return bestMove > 0 && (depth <= PLY || legalMoveCount > 5)? getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                         }
                     }
                 }
 
                 // evaluate the move
                 if (a > alpha) {
                     a = -negascout(board, depth - PLY + depthExt, -b, -alpha, allowQuiescence, true, searchedPly + 1);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return bestMove > 0 && (depth <= PLY || legalMoveCount > 5)? getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                     }
 
                     // the other player has a better option, beta cut off
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, move);
                         addMoveToKillers(searchedPly, searchStage, move);
                         assert move != 0;
                         return getSearchResult(move, a);
                     }
                 }
 
                 if (a >= b) {
                     // null-window was too narrow, try a full search
                     a = -negascout(board, depth - PLY + depthExt, -beta, -a, allowQuiescence, true, searchedPly + 1);
                     if (cancelled) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         return bestMove > 0 && (depth <= PLY || legalMoveCount > 5)? getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                     }
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, move);
                         addMoveToKillers(searchedPly, searchStage, move);
                         assert move != 0;
                         return getSearchResult(move, a);
                     }
                 }
 
                 board.takeBack(undo);
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     assert board.getRepetitionCount() < 3 || a == 0;
                     quietMoveCount = 0;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     addMoveToHistoryTable(board, move);
                     addMoveToKillers(searchedPly, searchStage, move);
                     if (alpha > VAL_MATE_THRESHOLD) {
                         break;
                     }
                 } else {
                     if (bestMove == 0) {
                         bestMove = move;
                     }
                     quietMoveCount++;
                 }
 
                 b = alpha + 1;
             }
             if (legalMoveCount > 0 && alpha > VAL_MATE_THRESHOLD) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (legalMoveCount == 0) {
             if (inCheck) {
                 return getSearchResult(0, -Evaluation.VAL_MATE);
             } else {
                 return getSearchResult(0, 0);
             }
         }
         if (bestMove != 0 && alpha > origAlpha) {
             transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, depth >> SHIFT_PLY, alpha - VAL_MIN, age);
         }
         return bestMove > 0? getSearchResult(bestMove, alpha): getSearchResult(0, 0);
     }
 
     public int negascout(final Board board, final int depth, int alpha, int beta, final boolean quiescence,
                          final boolean allowNull, final int searchedPly) {
         nodeCount++;
         if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
             // three-fold repetition
             return VAL_DRAW;
         }
         if (depth < PLY) {
             final int eval;
             if (!quiescence) {
                 eval = evaluation.evaluate(board);
             } else {
                 eval = quiescence(board, alpha, beta);
             }
             return eval;
         }
 
         // check the time
         if (nodeCount >= nodeCountAtNextTimeCheck) {
             calculateNextTimeCheck();
             if (cancelled) {
                 return alpha;
             }
         }
 
         final long zobristKey = board.getZobristKey();
         final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
         final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
         if (ttValue != 0 && ttDepth >= (depth >> SHIFT_PLY)) {
             final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                 return value;
             } else {
                 if (value > VAL_MATE_THRESHOLD) {
                     return value;
                 }
                 alpha = value;
             }
         }
 
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
         final boolean inCheck = board.attacksKing(1 - toMove);
 
         // null-move pruning
         int initialDepthExt = 0;
         if (depth > NULL_MOVE_MIN_DEPTH && !inCheck && allowNull && beta < VAL_MATE_THRESHOLD &&
                 board.getMinorMajorPieceCount(toMove) > 0) {
             final int r = depth > NULL_MOVE_DEPTH_CHANGE_THRESHOLD? NULL_MOVE_DEPTH_HIGH: NULL_MOVE_DEPTH_HIGH;
             final int prevState = board.nullMove();
             final int value = -negascout(board, depth - r, -beta, -beta + 1, false, false, searchedPly + 1);
             if (cancelled) {
                 board.nullMove(prevState);
                 return alpha;
             }
             if (value >= beta) {
                 board.nullMove(prevState);
 //                transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, 0, depth >> SHIFT_PLY, value - VAL_MIN, age);
                 return beta;
             }
             final int value2 = -negascout(board, depth - r, VAL_MATE_THRESHOLD, VAL_MATE_THRESHOLD + 1, false, false, searchedPly + 1);
             board.nullMove(prevState);
             if (cancelled) {
                 return alpha;
             }
             if (value2 < -VAL_MATE_THRESHOLD) {
                 initialDepthExt += DEPTH_EXT_MATE_THREAT;
             }
         }
 
         int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         assert (Utils.getMoveFromIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
         assert (Utils.getMoveToIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
         if (depth > 3 * PLY && ttMove == 0) {
             // internal iterative deepening
             final long searchResult = negascoutRoot(board, depth / 2, alpha, beta, searchedPly);
             ttMove = getMoveFromSearchResult(searchResult);
             assert (Utils.getMoveFromIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
             assert (Utils.getMoveToIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
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
         int b = beta;
         int bestMove = 0;
         int legalMoveCount = 0;
         int quietMoveCount = 0;
         boolean hasEvaluatedMove = false;
         for (SearchStage searchStage: searchStages) {
             final boolean highPriorityStage =
                 searchStage != SearchStage.NORMAL && searchStage != SearchStage.CAPTURES_LOOSING;
             final int[] moves = getMoves(searchStage, board, ttMove, searchedPly);
             final boolean allowQuiescence = (searchStage == SearchStage.CAPTURES_WINNING ||
                 searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING ||
                 searchStage == SearchStage.TRANS_TABLE && moves[0] == 1 &&
                     (MoveGenerator.isCapture(board, moves[1]) || MoveGenerator.isPromotion(board, moves[1])));
             final boolean allowToRecurseDown = !futility ||
                 (searchStage == SearchStage.TRANS_TABLE || searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION);
 
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 // TODO instead: check if isCheck is true and if the current move avoids the check
                 if (board.attacksKing(1 - toMove)) {
                     board.takeBack(undo);
                     continue;
                 }
 
                 // register that we had a legal move
                 legalMoveCount++;
 
                 final boolean opponentInCheck = (move & CHECKING) > 0;
                 if (!allowToRecurseDown && !opponentInCheck) {
                     board.takeBack(undo);
                     break;
                 }
 
                 int depthExt = initialDepthExt;
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
 
                 // razoring
                 if (depthExt == 0 && depth <= (3 << SHIFT_PLY) && b == alpha + 1) {
                     final int value = -board.getMaterialValue();
                     if (value < beta - VAL_RAZORING_THRESHOLD) {
 //                        final int qscore = -quiescence(board, -b, -alpha);
                         final int qscore = board.getRepetitionCount() < 3? -quiescence(board, -b, -alpha): 0;
 //                        final int qscore = -negascout(board, PLY, -b, -alpha, false, false, searchedPly + 1);
                         if (cancelled) {
                             moveGenerator.popFrame();
                             board.takeBack(undo);
                             return alpha;
                         }
                         if (qscore < b) {
                             board.takeBack(undo);
                             continue;
                         }
                     }
                 }
 
                 int a = alpha + 1;
                 if (!highPriorityStage) {
                     if (quietMoveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                             !Utils.isCastling(move) && !opponentInCheck && depthExt == 0) {
                         a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true, searchedPly + 1);
                         if (cancelled) {
                             board.takeBack(undo);
                             moveGenerator.popFrame();
                             return alpha;
                         }
                     }
                 }
 
                 // evaluate the move
                 if (a > alpha && b >= -VAL_MATE_THRESHOLD) {
                     a = -negascout(board, depth - PLY + depthExt, -b, -alpha, allowQuiescence, true, searchedPly + 1);
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
                         addMoveToHistoryTable(board, move);
                         addMoveToKillers(searchedPly, searchStage, move);
                         return a;
                     }
                 }
 
                 if (a >= b) {
                     // null-window was too narrow, try a full search
                     a = -negascout(board, depth - PLY + depthExt, -beta, -a, allowQuiescence, true, searchedPly + 1);
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
                         addMoveToHistoryTable(board, move);
                         addMoveToKillers(searchedPly, searchStage, move);
                         return a;
                     }
                 }
                 hasEvaluatedMove = true;
                 board.takeBack(undo);
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     quietMoveCount = 0;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                     addMoveToHistoryTable(board, move);
                     addMoveToKillers(searchedPly, searchStage, move);
                     if (alpha > VAL_MATE_THRESHOLD) {
                         break;
                     }
                 } else {
                     quietMoveCount++;
                 }
 
                 b = alpha + 1;
             }
             if (legalMoveCount > 0 && alpha > VAL_MATE_THRESHOLD) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (legalMoveCount == 0) {
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
         assert (Utils.getMoveFromIndex(bestMove) & 0x88) == 0: Integer.toHexString(bestMove) + "/" + StringUtils.toSimple(bestMove);
         assert (Utils.getMoveToIndex(bestMove) & 0x88) == 0: Integer.toHexString(bestMove) + "/" + StringUtils.toSimple(bestMove);
         assert (Utils.getMoveFromIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
         assert (Utils.getMoveToIndex(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
         return alpha;
     }
 
     public long removeThreefoldRepetition(final Board board, long ttValue) {
         int move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         if (move != 0) {
             final long undo = board.move(move);
             if (board.getRepetitionCount() >= 3) {
                 ttValue = 0;
             }
             board.takeBack(undo);
         }
         return ttValue;
     }
 
     public int quiescence(final Board board, int alpha, int beta) {
         nodeCount++;
 
         if (Evaluation.drawByInsufficientMaterial(board)) {
             return VAL_DRAW;
         }
         
         final int state = board.getState();
         final int toMove = state & WHITE_TO_MOVE;
 
         final int eval = evaluation.evaluate(board);
         if (eval > alpha) {
             alpha = eval;
             if (alpha >= beta && !board.attacksKing(1 - toMove)) {
                 return alpha;
             }
         }
 
         // check the time
         if (nodeCount >= nodeCountAtNextTimeCheck) {
             calculateNextTimeCheck();
             if (cancelled) {
                 return alpha;
             }
         }
 
         final long zobristKey = board.getZobristKey();
         final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
         if (ttValue != 0) {
             final long ttType = ttValue & TT_TYPE;
             if (ttType == TT_TYPE_EXACT) {
                 assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                 return (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
             } else if (ttType == TT_TYPE_ALPHA_CUT || ttType == TT_TYPE_BETA_CUT) {
                 alpha = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
                 if (alpha > VAL_MATE_THRESHOLD) {
                     return alpha;
                 }
             }
         }
 
         moveGenerator.pushFrame();
         boolean hasLegalMove = false;
         int b = beta;
         int bestMove = 0;
         for (SearchStage searchStage: searchStages) {
             final int[] moves = getMoves(searchStage, board, 0, MAX_SEARCH_DEPTH - 1);
 
             final boolean allowToRecurseDown = (searchStage == SearchStage.CAPTURES_WINNING ||
                     searchStage == SearchStage.PROMOTION);
 
             for (int i = moves[0]; i > 0; i--) {
                 final int move = moves[i];
 
                 assert (move & BASE_INFO) != 0;
 
                 // make the move
                 final long undo = board.move(move);
 
                 // check if the king remained in check
                 if (board.attacksKing(1 - toMove)) {
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
                     addMoveToHistoryTable(board, move);
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
                     if (a >= beta) {
                         board.takeBack(undo);
                         moveGenerator.popFrame();
                         assert board.getBoard()[getMoveFromIndex(move)] != EMPTY;
                         transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, 0, a - VAL_MIN, age);
                         addMoveToHistoryTable(board, move);
                         return a;
                     }
                 }
                 board.takeBack(undo);
                 if (a > alpha) {
                     bestMove = move;
                     alpha = a;
                     transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, move, 0, a - VAL_MIN, age);
                     addMoveToHistoryTable(board, move);
                     if (alpha > VAL_MATE_THRESHOLD) {
                         break;
                     }
                 }
 
                 b = alpha + 1;
             }
             if (hasLegalMove && alpha > VAL_MATE_THRESHOLD) {
                 break;
             }
         }
         moveGenerator.popFrame();
         if (!hasLegalMove) {
             final boolean inCheck = board.attacksKing(1 - toMove);
             if (inCheck) {
                 return -Evaluation.VAL_MATE;
             }
         }
 
         if (bestMove != 0) {
             transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, 0, alpha - VAL_MIN, age);
         }
         return alpha;
     }
 
     private void addMoveToHistoryTable(final Board board, final int move) {
         final int fromIndex = getMoveFromIndex(move);
         final int toIndex = getMoveToIndex(move);
         final int pieceHistoryIdx = board.getBoard()[fromIndex] + 7;
         final int fromIndex64 = convert0x88To64(fromIndex);
         final int toIndex64 = convert0x88To64(toIndex);
         history[pieceHistoryIdx][fromIndex64][toIndex64]++;
         if (history[pieceHistoryIdx][fromIndex64][toIndex64] > historyMax) {
             historyMax = history[pieceHistoryIdx][fromIndex64][toIndex64];
         }
     }
 
     private void addMoveToKillers(final int searchedPly, final SearchStage searchStage, final int move) {
         if (searchStage == SearchStage.NORMAL && (move & MOVE_TYPE) == MT_NORMAL) {
             final int fromTo = move & FROM_TO;
             if (killerMoves[searchedPly][0] != fromTo) {
                 assert (Utils.getMoveFromIndex(fromTo) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
                 assert (Utils.getMoveToIndex(fromTo) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
                 killerMoves[searchedPly][1] = killerMoves[searchedPly][0];
                 killerMoves[searchedPly][0] = fromTo;
             }
         }
     }
 
     private void calculateNextTimeCheck() {
         final long currentTime = System.currentTimeMillis();
         if (searchEndTime <= currentTime) {
             cancelled = true;
 //                System.out.printf("info string Cancelling after %d ms, search time: %d ms (node count: %d)\r\n", currentTime - searchStartTime, searchEndTime - searchStartTime, nodeCount);
             return;
         }
         long timeEllapsed = currentTime - searchStartTime;
 //        System.out.printf("info string Processed %d nodes in %d ms\r\n", nodeCount, timeEllapsed);
         if (timeEllapsed < 10) {
             timeEllapsed = 10;
         }
         final long timeLeft = searchEndTime - currentTime;
         long nodesToProcessUntilNextCheck = (timeLeft * nodeCount / timeEllapsed) >>> 1;
         if (nodesToProcessUntilNextCheck < 200) {
             nodesToProcessUntilNextCheck = 200;
         }
         nodeCountAtNextTimeCheck = nodeCount + nodesToProcessUntilNextCheck;
 //        System.out.printf("info string Next check at node count %d\r\n", nodeCountAtNextTimeCheck);
         lastCheckTime = currentTime;
     }
 
     private int[] getMoves(final SearchStage searchStage, final Board board, final int ttMove, final int searchedPly) {
         final int[] moves;
         switch (searchStage) {
             case TRANS_TABLE:
                 if ((ttMove & BASE_INFO) > 0) {
                     moves = new int[2];
                     moves[0] = 1;
                     if (board.isCheckingMove(ttMove)) {
                         moves[1] = ttMove | CHECKING;
                     } else {
                         moves[1] = ttMove & ~CHECKING;
                     }
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
                 for (int move: killerMoves[searchedPly]) {
                     if (isValidKillerMove(board, getMoveFromIndex(move), getMoveToIndex(move))) {
                         if (board.isCheckingMove(move)) {
                             moves[++killerCount] = move | CHECKING;
                         } else {
                             moves[++killerCount] = move & ~CHECKING;
                         }
                     }
                 }
                 moves[0] = killerCount;
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
                 searchStage == SearchStage.NORMAL? killerMoves[searchedPly]: NO_MOVE_ARRAY);
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
                 final int positionalGain = Evaluation.computePositionalGain(absPiece, toMove, fromIndex, toIndex, stage);
                 final int valPositional = ((positionalGain + 100) >> 2);
                 final int checkBonus;
                 final int checkingBit;
                 if (board.isCheckingMove(move)) {
                     checkBonus = MOVE_ORDER_CHECK_BONUS;
                     checkingBit = CHECKING;
                 } else {
                     checkingBit = 0;
                     if (SLIDING[absPiece] && (ATTACK_ARRAY[kingIndex - toIndex + 120] & ATTACK_BITS[absPiece]) > 0) {
                         checkBonus = MOVE_ORDER_BLOCKED_CHECK_BONUS;
                     } else {
                         checkBonus = 0;
                     }
                 }
                 final int toRank = getRank(toIndex);
                 final int pawnBonus;
                 if (absPiece == PAWN && (toRank == 1 || toRank == 6)) {
                     pawnBonus = MOVE_ORDER_7TH_RANK_PAWN;
                 } else {
                     pawnBonus = 0;
                 }
                 final int moveValue =
                     ((move & MOVE_VALUE) >> SHIFT_MOVE_VALUE) + historyValue + checkBonus + valPositional + pawnBonus;
                 moves[i] = (move & ~MOVE_VALUE) | checkingBit | (moveValue << SHIFT_MOVE_VALUE);
                 assert (moves[i] & (1 << 31)) == 0: Integer.toHexString(moves[i]); 
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
         evaluation.getEvalHashTable().clear();
         evaluation.getPawnHashTable().clear();
         for (int[][] arrays: history) {
             for (int[] array: arrays) {
                 Arrays.fill(array, 0);
             }
         }
         historyMax = 0;
     }
 
     public TranspositionTable getTranspositionTable() {
         return transpositionTable;
     }
 
     public Evaluation getEvaluation() {
         return evaluation;
     }
 
     public int[] getBestLine(final Board board) {
         return getBestLine(board, 0);
     }
 
     public int[] getBestLine(final Board board, final int defaultMove) {
         long zobristKey = board.getZobristKey();
         long ttValue = transpositionTable.read(zobristKey);
         final int depth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
         int move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
         if (move == 0 || depth == 0) {
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
  
     public static int getValueFromSearchResult(final long result) {
         return (int) (result & 0xFFFFFFFFL);
     }
 
     public static int getMoveFromSearchResult(final long result) {
         final int move = (int) (result >> 32);
         assert (Utils.getMoveFromIndex(move) & 0x88) == 0: Long.toHexString(result) + "/" + StringUtils.toSimple(move);
         assert (Utils.getMoveToIndex(move) & 0x88) == 0: Long.toHexString(result) + "/" + StringUtils.toSimple(move);
         return move;
     }
 
     public static long getSearchResult(final int move, final int value) {
         assert (Utils.getMoveFromIndex(move) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
         assert (Utils.getMoveToIndex(move) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
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
             // fromIndex is occupied by the opponent's piece
             return false;
         }
         final int absPiece = signum * piece;
         if (boardObj.isSliding(absPiece)) {
             return boardObj.isAttackedBySliding(toIndex, ATTACK_BITS[absPiece], fromIndex);
         } else if (absPiece == PAWN) {
             final int squareInFront = fromIndex + signum * UP;
             final int fromRank = getRank(fromIndex);
             final int toRank = getRank(toIndex);
             return toRank != 0 && toRank != 7 && (toIndex == squareInFront ||
                 (board[squareInFront] == EMPTY && toIndex == squareInFront + signum * UP && (fromRank == 1 || fromRank == 6)));
         } else {
             return boardObj.isAttackedByNonSliding(toIndex, ATTACK_BITS[absPiece], fromIndex);
         }
     }
 }
