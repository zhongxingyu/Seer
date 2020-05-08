 package no.pdigre.chess.eval;
 
 import java.util.Arrays;
 import java.util.Comparator;
 
 import no.pdigre.chess.base.Bitmap;
 import no.pdigre.chess.base.IConst;
 import no.pdigre.chess.base.NodeGen;
 
 public class AlphaBeta {
 
     public int[] stack;
 
     public MoveEval[] moves;
 
     public AlphaBeta(int[] board, int inherit, int inital_depth) {
         int[] legalmoves = NodeGen.getAllMoves(board, inherit);
         moves = new MoveEval[legalmoves.length];
         for (int i = 0; i < moves.length; i++) {
             int bitmap = legalmoves[i];
             moves[i] = new MoveEval(bitmap, board, alphaBeta(inital_depth, board, bitmap));
         }
 
         if (Bitmap.white(inherit)) {
             Arrays.sort(moves, new Comparator<MoveEval>() {
 
                 @Override
                 public int compare(MoveEval o1, MoveEval o2) {
                    return Integer.compare(o1.score, o2.score);
                 }
             });
         } else {
             Arrays.sort(moves, new Comparator<MoveEval>() {
 
                 @Override
                 public int compare(MoveEval o1, MoveEval o2) {
                    return Integer.compare(o2.score, o1.score);
                 }
             });
         }
 
     }
 
     public final int alphaBeta(int depthleft, int[] board0, int bitmap0) {
         stack = new int[depthleft];
         if (Bitmap.white(bitmap0))
             return blackMove(-1000000, +1000000, depthleft, board0, bitmap0, 0);
         return whiteMove(-1000000, +1000000, depthleft, board0, bitmap0, 0);
     }
 
     public final int whiteMove(int whiteBest, int blackBest, int depthleft, int[] board0, int bitmap0, int score0) {
         int score1 = evalWhiteMove(bitmap0, board0, score0);
         if (depthleft == 0)
             return score1;
         depthleft--;
         stack[depthleft] = bitmap0;
         int[] board = Bitmap.apply(board0, bitmap0);
         NodeGen pull = new NodeGen(board, bitmap0);
         while (true) {
             int bitmap = pull.nextUnsafe();
             if (bitmap == 0)
                 break;
             int score = blackMove(whiteBest, blackBest, depthleft, board, bitmap, score1);
             if (score >= blackBest)
                 return blackBest; // fail hard beta-cutoff
             if (score > whiteBest)
                 whiteBest = score; // alpha acts like max in MiniMax
         }
         return whiteBest;
     }
 
     public final int blackMove(int whiteBest, int blackBest, int depthleft, int[] board0, int bitmap0, int score0) {
         int score1 = evalBlackMove(bitmap0, board0, score0);
         if (depthleft == 0)
             return score1;
         depthleft--;
         stack[depthleft] = bitmap0;
         int[] board = Bitmap.apply(board0, bitmap0);
         NodeGen pull = new NodeGen(board, bitmap0);
         while (true) {
             int bitmap = pull.nextUnsafe();
             if (bitmap == 0)
                 break;
             int score = whiteMove(whiteBest, blackBest, depthleft, board, bitmap, score1);
             if (score <= whiteBest)
                 return whiteBest; // fail hard alpha-cutoff
             if (score < blackBest)
                 blackBest = score; // beta acts like min in MiniMax
         }
         return blackBest;
     }
 
     /**
      * @param board
      */
     public final static int evalWhiteMove(int bitmap, int[] board, int score) {
         return score - capturedValue(bitmap);
     }
 
     /**
      * @param board
      */
     public final static int evalBlackMove(int bitmap, int[] board, int score) {
         return score + capturedValue(bitmap);
     }
 
     /**
      * @param board
      */
     public final static int evalBoard(int[] board) {
         int total = 0;
         for (int piece : board) {
             total += Bitmap.value(piece);
         }
         return total;
     }
 
     public final static int capturedValue(int bitmap) {
         return Bitmap.value((bitmap & IConst.CAPTURE) >>> IConst._CAPTURE);
     }
 
     public int[] getBitmaps() {
         int[] ret = new int[moves.length];
         for (int i = 0; i < moves.length; i++)
             ret[i] = moves[i].bitmap;
         return ret;
     }
 
     public MoveEval getMove(int bitmap) {
         for (MoveEval move : moves)
             if (move.bitmap == bitmap)
                 return move;
         return null;
     }
 
 }
