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
        return Integer.compare(this.score, o.score);
     }
     
 }   
