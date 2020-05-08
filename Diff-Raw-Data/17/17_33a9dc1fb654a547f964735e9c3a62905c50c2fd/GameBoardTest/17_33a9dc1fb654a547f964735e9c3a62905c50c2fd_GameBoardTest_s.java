 package no.hist.aitel.android.tictactoe;
 
 import org.junit.Test;
 
 import static no.hist.aitel.android.tictactoe.GameState.*;
 import static org.junit.Assert.assertEquals;
 
 public class GameBoardTest {
 
     @Test
     public void testPut() {
         GameBoard b = new GameBoard(3);
         b.setInRow(3);
         assertEquals(VALID_MOVE, b.put(0, 0, PLAYER1));
         assertEquals(PLAYER1, b.get(0, 0));
         assertEquals(INVALID_MOVE, b.put(0, 0, PLAYER2));
         assertEquals(PLAYER1, b.get(0, 0));
     }
 
     @Test
     public void testGame3x3() {
         GameBoard b = new GameBoard(3);
         b.setInRow(3);
         assertEquals(VALID_MOVE, b.put(0, 0, PLAYER1));
         assertEquals(PLAYER1, b.get(0, 0));
         assertEquals(NEUTRAL, b.getState());
         assertEquals(VALID_MOVE, b.put(0, 1, PLAYER2));
         assertEquals(PLAYER2, b.get(0, 1));
         assertEquals(NEUTRAL, b.getState());
         assertEquals(VALID_MOVE, b.put(1, 0, PLAYER1));
         assertEquals(PLAYER1, b.get(1, 0));
         assertEquals(NEUTRAL, b.getState());
         assertEquals(VALID_MOVE, b.put(1, 1, PLAYER2));
         assertEquals(PLAYER2, b.get(1, 1));
         assertEquals(NEUTRAL, b.getState());
         assertEquals(VALID_MOVE, b.put(2, 0, PLAYER1));
         assertEquals(PLAYER1, b.get(2, 0));
         assertEquals(WIN, b.getState());
     }
 }
