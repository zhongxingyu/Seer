 package xiangqi.ai;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Comparator;
 import java.util.Collections;
 
 public class Agent {
     protected int turn;
     protected int[] score;
     protected static final int INFINITY = 10000, ABORTED = 20000;
     public Board board;
 
     protected int[] stat = new int[100];
 
     protected int transpSize = 1 << 20;
     // lower(16) upper(16) move(16) depth(16)
     protected long [][][] transposition = new long[transpSize][4][2];
     protected int[] moveScore = new int[65536], currentMoveScore = new int[65536];
 
     protected int checkTime = 0;
     protected static final int CHECK_TIME_CYCLE = 100000;
 
     protected Comparator<Integer> compare = new Comparator<Integer>() {
         public int compare(Integer o1, Integer o2) {
             if (o1 == 0)
                 return -1;
             if (o2 == 0)
                 return 1;
 
             return moveScore[o2] - moveScore[o1];
         }
     };
 
     public Agent() {
         board = new Board();
         turn = 0;
         score = new int[2];
     }
 
     public Agent(int[][] board, int turn) {
         this.board = new Board(board);
         this.turn = turn;
         score = new int[2];
     }
 
     public int turn() {
         return turn;
     }
 
     public void pass() {
         turn = 1 - turn;
     }
 
     public boolean checkedMove(Move move) {
         if (board.checkedMove(move.internalRepresentation())) {
             pass();
             return true;
         } else
             return false;
     }
 
     public void unmove() {
         board.unmove();
         pass();
         score = new int[2];
     }
 
     protected int evaluateCount = 0;
 
     public Move search(int depth, int time) {
         evaluateCount = 0;
         long startTime = System.nanoTime();
         int move = id(depth, turn, time * 1000000000L);
         long timeSpent = System.nanoTime() - startTime;
         System.out.println(evaluateCount + " evaluations in " + timeSpent / 1e9 + "s, " + (int) ((double) evaluateCount / (timeSpent / 1e6)) + " k/s");
 
         int statSum = 0;
         for (int i = 0; i < 10; ++i) {
             statSum += stat[i];
             System.out.print(stat[i] + " ");
         }
         System.out.println((double) stat[0] / (double) statSum + ", " + (double) stat[1] / (double) statSum);
         if (move != 0)
             return new Move(move);
         else
             return null;
     }
 
     protected int id(int depth, int turn, long timeLimit) {
         moveScore = new int[65536];
         currentMoveScore = new int[65536];
         int best = -INFINITY, bestMove = 0;
 
         long deadLine;
         if (timeLimit == 0)
             deadLine = Long.MAX_VALUE;
         else
             deadLine = System.nanoTime() + timeLimit;
 
         for (int d = 1; (depth == 0 ? true : (d <= depth)); ++d) {
             System.out.print("Depth: " + d);
 //            int t = MTDf(d, turn, score[turn], deadLine);
             int t = minimax(d, turn, -INFINITY, INFINITY, 0, false, deadLine);
             if (t == ABORTED) {
                 System.out.println(", aborted");
                 break;
             }
             moveScore = currentMoveScore;
             currentMoveScore = new int[65536];
 
             best = t;
             score[turn] = best;
             System.out.print(", value: " + best + ", move: ");
 
             t = turn;
             int i;
             for (i = 0; i < d; ++i) {
                 long history = loadTransposition(board.currentHash(t));
                 if (history == -1)
                     break;
                 int move = (int) (history >> 16) & 0xffff;
                 if (i == 0)
                     bestMove = move;
                 if (move == 0)
                     break;
                 board.move(move);
                 t = 1 - t;
                 if (i == 0)
                     System.out.print("0x" + Integer.toString(move, 16));
                 System.out.print(new Move(move) + " ");
             }
             while (i > 0) {
                 board.unmove();
                 --i;
             }
             System.out.println();
         }
         return bestMove;
     }
 
     protected int MTDf(int depth, int turn, int g, long deadLine) {
         int lower = -INFINITY, upper = INFINITY;
         while (lower < upper) {
             int beta;
             if (g == lower)
                 beta = g + 1;
             else
                 beta = g;
             g = minimax(depth, turn, beta - 1, beta, 0, false, deadLine);
             if (g == ABORTED)
                 return ABORTED;
 
             if (g < beta)
                 upper = g;
             else
                 lower = g;
         }
         // note: it is possible that lower > upper here
         // this is caused by having found an over-depth tranposition entry
         // thus the g here is more accureate than desired depth
         return g;
     }
 
     // returns -1 if entry not found
     protected long loadTransposition(long hash) {
         int p = (int) (hash & (transpSize - 1));
         for (int i = 0; i < 4; ++i)
             if (transposition[p][i][0] == hash) {
                 return transposition[p][i][1];
             }
         return -1;
     }
 
     protected void removeTransposition(long hash) {
         int p = (int) (hash & (transpSize - 1));
         for (int i = 0; i < 4; ++i)
             if (transposition[p][i][0] == hash) {
                 transposition[p][i][0] = 0;
                 return;
             }
     }
 
     protected long storeTransposition(long hash, int depth, int move, int upper, int lower) {
         long value = ((((long) lower & 0xffffL) << 48)
                     | (((long) upper & 0xffffL) << 32) | ((long) move << 16) | ((long) depth & 0xffffL));
         return storeTransposition(hash, value);
     }
 
     protected long storeTransposition(long hash, long value) {
         int p = (int) (hash & (transpSize - 1));
         int hole = -1, shallowest = INFINITY, si = 0;
         for (int i = 0; i < 4; ++i) {
             if (transposition[p][i][0] == 0 || transposition[p][i][0] == hash) {
                 hole = i;
                 break;
             } else {
                 int depth = (int) (transposition[p][i][1] << 48 >> 48);
                 if (depth < shallowest) {
                     shallowest = depth;
                     si = i;
                 }
             }
         }
        long ret = -1;
        if (hole == -1) {
             hole = si;
            ret = transposition[p][hole][1];
        }
         transposition[p][hole][0] = hash;
         transposition[p][hole][1] = value;
         return ret;
     }
 
     public int evaluate() {
         return minimax(0, turn, -INFINITY, INFINITY, 0, false, 0);
     }
 
     protected int minimax(int depth, int turn, int alpha, int beta, int level, boolean nullMove, long deadLine) {
         long hash = board.currentHash(turn);
 
         long history = loadTransposition(hash);
         int lower = -INFINITY, upper = INFINITY;
         if (history != -1) {
             int dHistory = (int) (history << 48 >> 48);
             if (dHistory >= depth) {
                 lower = (int) (history >> 48);
                 upper = (int) (history << 16 >> 48);
                 int move = (int) (history >> 16) & 0xffff;
                 if (nullMove || move != 0) {
                     if (lower == upper)
                         return lower;
                     if (lower >= beta)
                         return lower;
                     if (upper <= alpha)
                         return upper;
                 }
                 alpha = Math.max(alpha, lower);
                 beta = Math.min(beta, upper);
             }
         }
 
         boolean quiescence = (depth <= 0);
 
         ++evaluateCount;
         long oldHistory = storeTransposition(hash, depth, 0, 0, 0);
 
         List<Integer> moves;
         if (quiescence) {
             moves = board.generateAttacks(turn);
             if (moves.size() == 0) {
                 int score = board.staticValue(turn);
                 storeTransposition(hash, 0, 0, score, score);
                 return score;
             }
         } else
             moves = board.generateMoves(turn);
 
         if (nullMove && quiescence && moves.size() > 0 && !board.isChecked(turn))
             moves.add(0);
 
         Collections.sort(moves, compare);
 
         int best = -INFINITY + level, bestMove = 0, oldAlpha = alpha;
         int bestIndex = 0, i = 0;
         boolean aborted = false, pvFound = false;
         for (int move: moves) {
             ++checkTime;
             if (checkTime == CHECK_TIME_CYCLE) {
                 checkTime = 0;
                 if (System.nanoTime() >= deadLine) {
                     aborted = true;
                     break;
                 }
             }
 
             if (move != 0 && !board.move(move))
                 continue;
 
             int t;
             if (pvFound) {
                 t = -minimax(depth - 1, 1 - turn, -alpha - 1, -alpha, level + 1, true, deadLine);
                 if (t > alpha && t < beta)
                     t = -minimax(depth - 1, 1 - turn, -beta, -t, level + 1, true, deadLine);
             } else
                 t = -minimax(depth - 1, 1 - turn, -beta, -alpha, level + 1, true, deadLine);
 
             if (move != 0)
                 board.unmove();
 
             if (t == -ABORTED) {
                 aborted = true;
                 break;
             }
 
             if (move != 0) {
                 int bonus = 0;
                 if (t >= beta)
                     bonus += 100;
                 if (i == 0)
                     bonus += 20;
                 saveMoveScore(move, t + bonus);
             }
 
             if (t > best)
                 bestIndex = i;
 
             if (t > best || (t == best && bestMove == 0)) {
                 best = t;
                 bestMove = move;
             }
 
             if (best > alpha) {
                 alpha = best;
                 pvFound = true;
                 if (beta <= alpha)
                     break;
             }
 
             ++i;
         }
 
         if (!quiescence)
             ++stat[bestIndex];
         
         if (aborted) {
             if (oldHistory != -1)
                 storeTransposition(hash, oldHistory);
             else
                 removeTransposition(hash);
             return ABORTED;
         }
 
         // bug note: we don't use static values if there's an beta cutoff
         if (quiescence && best < beta && bestMove == 0) {
             best = board.staticValue(turn);
             storeTransposition(hash, 0, 0, best, best);
         } else {
             if (best <= oldAlpha)
                 storeTransposition(hash, depth, bestMove, best, lower);
             else if (best < beta)
                 storeTransposition(hash, depth, bestMove, best, best);
             else
                 storeTransposition(hash, depth, bestMove, upper, best);
         }
 
         return best;
     }
 
     protected void saveMoveScore(int move, int score) {
         if (currentMoveScore[move] < score)
             currentMoveScore[move] = score;
     }
 
     public void bestResponse() {
         long history = loadTransposition(board.currentHash(turn));
         if (history == -1)
             System.out.println("No result found");
         else {
             int lower = (int) (history >> 48), upper = (int) (history << 16 >> 48);
             int move = (int) (history >> 16) & 0xffff;
             System.out.println("Move: " + new Move(move) + ", score: [" + lower + ", " + upper + "]");
         }
     }
 
     public void clearTransposition() {
         transposition = new long[transpSize][4][2];
     }
 }
