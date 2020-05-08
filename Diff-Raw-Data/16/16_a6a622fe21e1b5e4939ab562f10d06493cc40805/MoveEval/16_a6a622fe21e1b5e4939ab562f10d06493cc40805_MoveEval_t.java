 package no.pdigre.chess.eval;
 
 
 public class MoveEval implements Comparable<MoveEval>{
     public final int bitmap;
     public final int[] board;
     public int score;
     
     public MoveEval(int bitmap, int[] board, int score) {
         super();
         this.bitmap = bitmap;
         this.board = board;
         this.score = score;
     }
 
     @Override
     public int compareTo(MoveEval o) {
        return intCompare(this.score, o.score);
    }

    public static int intCompare(int i1, int i2) {
        return i1 > i2?1:(i1 == i2?0:-1);
     }
     
 }   
