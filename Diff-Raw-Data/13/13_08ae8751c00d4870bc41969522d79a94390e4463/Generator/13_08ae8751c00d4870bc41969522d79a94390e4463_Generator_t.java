 package player.millitta;
 
 import java.util.ArrayList;
 
 public class Generator implements Constants, algds.Constants {
 
     private long board_  = 0L;
     private long action = -1L;
     private int playerID = -1;
 
     //private ArrayList<Long> possNextBoards = null;
     private static long[] nextBoards = new long[100]; // TODO find max. size
     private int boardPointer = 0;
 
 
    public Generator(long board) {
         board_ = board;
 
         calcAction();
         calcPlayerID();
     }
 
 
     private void calcPlayerID() {
         playerID = (board_ & (1L << BIT_PLAYER)) == 0 ? 0 : 1;
     }
 
 
     private void calcAction() {
        if ((board_ & ((1L << BIT_ACTION))) != 0L || ((board_ & (1L << (BIT_ACTION + 1))) != 0L) ) {
             action = REMOVE_MAN;
         } else if ((board_ & (1L << BIT_ACTION)) != 0L ) {
             action = SET_MAN;
         } else {
             action = MOVE_MAN;
         }
     }
 
     public long[] getNextBoards() {
         boardPointer = 0;
 
        if( (board_ & (1L << BIT_PHASE)) != 0 && (board_ & (1L << (BIT_PHASE+1))) != 0) { // Flugphase
 
 
         } else if ((board_ & (1L << BIT_PHASE)) != 0) { // Setzphase
             int rest = 9 - Helper.getMyMen(board_);
 
             if ( rest <= 0 ) {
                 System.out.println("Something went wrong!\nGamephase says I have to set a man, but no men left to set :/");
                 return nextBoards;
             }
 
            long mergedBoardPoints = (board_ & BITS_MENS1) | ((board_ & BITS_MENS2) >> 24);
 
             for( int i = 0; i < 24; i++ ) {
                 if ((mergedBoardPoints & (1L << i)) == 0) {
                     nextBoards[boardPointer++] = Helper.setMan(board_, i);
                 }
             }
 
         } else { // Zugphase
 
         }
 
         nextBoards[boardPointer] = MAGIC_NO_BOARD;
         return nextBoards;
     }
 
 
 
 }
