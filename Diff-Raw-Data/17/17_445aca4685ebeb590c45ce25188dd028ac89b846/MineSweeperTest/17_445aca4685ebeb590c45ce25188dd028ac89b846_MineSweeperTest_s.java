package de.darmstadt.tu.gdi1.ha10;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MineSweeperTest {
 
   MineSweeper ms = new MineSweeper();
   int nrRows, nrCols;
   boolean[][] board;
   MineSweeperBoard msb;
   @Before
   public void init() {
    board = new boolean[][]{
         { false, false },
         { true, false },
         { false, false },
         { false, true }};
     nrRows = board.length;
     nrCols = board[0].length;
     msb = new MineSweeperBoard(board);    
   }
   
   @Test
   public void checkMineCount() {
     assertEquals("Mine count mismatch", 2, msb.nrOfMines());
   }
 
   @Test
   public void checkTagsLeft() {
     assertEquals("Initial tag count mismatch", 6, msb.nrOfTagsLeft());
   }
 
   @Test
   public void checkNotWonOrOKAtStart() {
     assertFalse("Game should not be lost at start", msb.gameIsLost());
     assertFalse("Game should not be won at start", msb.gameIsWon());
   }
   
  @Test
  public void checkAllUntagged() {
    for (int r = 0; r < nrRows; r++)
      for (int c = 0; c < nrCols; c++)
        assertFalse("Game field ("+r +"," +c +") should not be tagged", 
              msb.isTagged(r, c));
  }
   
   @Test
   public void checkAllValuesAreInvisible() {
     for (int r = 0; r < nrRows; r++)
       for (int c = 0; c < nrCols; c++)
         assertEquals("All fields should be initially unvisited",
             20, msb.getVisibleValueFor(r, c));
   }
   
   @Test
   public void checkLostGame1() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(0, 2));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   
   @Test
   public void checkLostGame2() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(1, 0));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   
   @Test
   public void checkLostGame3() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(1, 2));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   @Test
   public void checkLostGame4() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(0, 2));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   @Test
   public void checkLostGame5() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(3, 1));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   @Test
   public void checkLostGame6() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(3, 2));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   @Test
   public void checkLostGame7() {
     assertTrue("Game should be lost when stepping on a mine",
         msb.stepOn(3, 3));
     assertTrue("Game should be flagged as lost", msb.gameIsLost());
   }
   
   @Test
   public void checkLostGame8() {
      assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(0, 0));
      assertFalse("Game should not be lost after stepping on a safe field",
          msb.gameIsLost());
     assertEquals("Wrong counter for neighbouring mines", 1,
         msb.getVisibleValueFor(0, 0));
 
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(0, 1));
     assertEquals("Wrong counter for neighbouring mines", 3,
         msb.getVisibleValueFor(0, 1));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(0, 3));
     assertEquals("Wrong counter for neighbouring mines", 2,
         msb.getVisibleValueFor(0, 3));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(1, 1));
     assertEquals("Wrong counter for neighbouring mines", 3,
         msb.getVisibleValueFor(1, 1));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(1, 3));
     assertEquals("Wrong counter for neighbouring mines", 2,
         msb.getVisibleValueFor(1, 3));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(2, 0));
     assertEquals("Wrong counter for neighbouring mines", 2,
         msb.getVisibleValueFor(2, 0));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(2, 1));
     assertEquals("Wrong counter for neighbouring mines", 4,
         msb.getVisibleValueFor(2, 1));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(2, 2));
     assertEquals("Wrong counter for neighbouring mines", 4,
         msb.getVisibleValueFor(2, 2));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(2, 3));
     assertEquals("Wrong counter for neighbouring mines", 3,
         msb.getVisibleValueFor(2, 3));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
 
     
     assertFalse("Game should not return false when stepping on safe field",
         msb.stepOn(3, 0));
     assertEquals("Wrong counter for neighbouring mines", 1,
         msb.getVisibleValueFor(3, 0));
     assertFalse("Game should not be lost after stepping on a safe field",
         msb.gameIsLost());
     assertFalse("Game should not be won after stepping on the last safe field",
         msb.gameIsWon());
   
   }
   
   @Test
   public void checkOutputNoMines() {
     String s = "   0000\n   0123\n00     \n01     \n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (0, 0) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(0, 0));
     s = "   0000\n   0123\n00 1   \n01     \n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (0, 1) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(0, 1));
     s = "   0000\n   0123\n00 13  \n01     \n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (0, 3) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(0, 3));
     s = "   0000\n   0123\n00 13 2\n01     \n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (1, 1) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(1, 1));
     s = "   0000\n   0123\n00 13 2\n01  3  \n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
     
     // step on (1, 1) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(1, 3));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02     \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
     
     // step on (2, 0) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(2, 0));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02 2   \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (2, 1) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(2, 1));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02 24  \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (2, 2) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(2, 2));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02 244 \n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (2, 3) - no mine
     assertFalse("Stepping on this field must be safe", msb.stepOn(2, 3));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02 2443\n03     ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // step on (3, 0) - no mine
     assertFalse("Stepping on this field must be safe",  msb.stepOn(3, 0));
     s = "   0000\n   0123\n00 13 2\n01  3 2\n02 2443\n03 1   ";
     assertEquals("Empty field should be shown correctly", s, msb.toString());
 
     // tag field (0, 2) - mine
     assertTrue("Tagging must be possible", msb.tagField(0, 2));
     s = "   0000\n   0123\n00 13?2\n01  3 2\n02 2443\n03 1   ";
     assertEquals("Tagged field should be shown correctly", s, msb.toString());
 
     // tag field (1, 0) - mine
     assertTrue("Tagging must be possible", msb.tagField(1, 0));
     s = "   0000\n   0123\n00 13?2\n01 ?3 2\n02 2443\n03 1   ";
     assertEquals("Tagged field should be shown correctly", s, msb.toString());
 
     // tag field (1, 2) - mine
     assertTrue("Tagging must be possible", msb.tagField(1, 2));
     s = "   0000\n   0123\n00 13?2\n01 ?3?2\n02 2443\n03 1   ";
     assertEquals("Tagged field should be shown correctly", s, msb.toString());
 
     // tag field (3, 1) - mine
     assertTrue("Tagging must be possible", msb.tagField(3, 1));
     s = "   0000\n   0123\n00 13?2\n01 ?3?2\n02 2443\n03 1?  ";
     assertEquals("Tagged field should be shown correctly", s, msb.toString());
 
     // tag field (3, 2) - mine
     assertTrue("Tagging must be possible", msb.tagField(3, 2));
     s = "   0000\n   0123\n00 13?2\n01 ?3?2\n02 2443\n03 1?? ";
     assertEquals("Tagged field should be shown correctly", s, msb.toString());
 
     // tag field (3, 3) - mine
     // note: this is the winning move!
     assertTrue("Tagging must be possible", msb.tagField(3, 3));
 
     assertTrue("Game should now be won", msb.gameIsWon());
     assertFalse("Game should not be lost", msb.gameIsLost());
     assertEquals("Mine count as expected", 6, msb.nrOfMines());
     assertEquals("Tag count as expected", 0, msb.nrOfTagsLeft());
     s = "   0000\n   0123\n00 13M2\n01 M3M2\n02 2443\n03 1MMM";
     assertEquals("Final game state must be correct", s, msb.toString());
   }
 
   @Test
   public void checkStringAtLoss() {
     assertTrue("Stepping on mine should end the game!", msb.stepOn(1, 0));
     assertTrue("Game should now be lost", msb.gameIsLost());
     assertFalse("Game must not be won after stepping on a mine", msb.gameIsWon());
     String s = "   0000\n   0123\n00 13M2\n01 M3M2\n02 2443\n03 1MMM";
     assertEquals("Final game state must be correct", s, msb.toString());
    }
 }
