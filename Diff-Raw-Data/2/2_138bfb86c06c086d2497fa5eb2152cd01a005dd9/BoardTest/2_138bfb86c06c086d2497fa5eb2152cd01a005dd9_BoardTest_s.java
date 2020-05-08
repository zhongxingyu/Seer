 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Notandi
  * Date: 12.11.2012
  * Time: 17:03
  */
 public class BoardTest extends TestCase {
     public BoardTest() {
     }
 
     public void testUpdateBoardOneInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         assertEquals("tests UpdateBoard wants true back", true, b.updateBoard(c, 1));
     }
 
     public void testUpdateBoardTwoInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         assertEquals("tests UpdateBoard wants true back", true, b.updateBoard(c, 1));
         assertEquals("tests UpdateBoard wants false back", false, b.updateBoard(c, 1));
     }
 
     public void testUpdateBoardTwoDiffInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         char d = 'O';
         assertEquals("tests UpdateBoard wants false back", true, b.updateBoard(d, 1));
         assertEquals("tests UpdateBoard wants true back", false, b.updateBoard(c, 1));
     }
 
     public void testUpdateBoardOuttaBoundInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         assertEquals("tests UpdateBoard wants true back", false, b.updateBoard(c, 10));
     }
 
     public void testCheckWinnerIfColumnIsAWinWithX() throws Exception {
         Board b = new Board();
         char c = 'X';
         b.updateBoard(c, 1);
         b.updateBoard(c, 4);
         b.updateBoard(c, 7);
         b.updateBoard(c, 2);
         b.updateBoard(c, 5);
         b.updateBoard(c, 8);
         b.updateBoard(c, 3);
         b.updateBoard(c, 6);
         b.updateBoard(c, 9);
 
         assertEquals("Test if the leftmost column full of X's is a win with move 1", 'X', b.checkWinner(c, 1, 1));
         assertEquals("Test if the leftmost column full of X's is a win with move 4", 'X', b.checkWinner(c, 4, 1));
         assertEquals("Test if the leftmost column full of X's is a win with move 7", 'X', b.checkWinner(c, 7, 1));
         assertEquals("Test if the middle column full of X's is a win with move 2", 'X', b.checkWinner(c, 2, 1));
         assertEquals("Test if the middle column full of X's is a win with move 5", 'X', b.checkWinner(c, 5, 1));
         assertEquals("Test if the middle column full of X's is a win with move 8", 'X', b.checkWinner(c, 8, 1));
         assertEquals("Test if the rightmost column full of X's is a win with move 3", 'X', b.checkWinner(c, 3, 1));
         assertEquals("Test if the rightmost column full of X's is a win with move 6", 'X', b.checkWinner(c, 6, 1));
         assertEquals("Test if the rightmost column full of X's is a win with move 9", 'X', b.checkWinner(c, 9, 1));
     }
 
     public void testCheckWinnerIfColumnIsAWinWithO() throws Exception {
         Board b = new Board();
         char c = 'O';
         b.updateBoard(c, 1);
         b.updateBoard(c, 4);
         b.updateBoard(c, 7);
 
         assertEquals("Test if leftmost column full of O's is a win with move 1", 'O', b.checkWinner(c, 1, 1));
     }
 
     public void testCheckWinnerIfRowIsAWin() throws Exception {
         Board b = new Board();
         char c = 'X';
         char d = 'O';
         b.updateBoard(c, 1);
         b.updateBoard(c, 2);
         b.updateBoard(c, 3);
         b.updateBoard(d, 4);
         b.updateBoard(d, 5);
         b.updateBoard(d, 6);
 
         assertEquals("Test if the top row full of X's is a win with move 1", 'X', b.checkWinner(c, 1, 1));
         assertEquals("Test if the top row full of X's is a win with move 2", 'X', b.checkWinner(c, 2, 1));
         assertEquals("Test if the top row full of X's is a win with move 3", 'X', b.checkWinner(c, 3, 1));
         assertEquals("Test if the middle row full of O's is a win with move 4", 'O', b.checkWinner(d, 4, 1));
         assertEquals("Test if the middle row full of O's is a win with move 5", 'O', b.checkWinner(d, 5, 1));
         assertEquals("Test if the middle row full of O's is a win with move 6", 'O', b.checkWinner(d, 6, 1));
     }
 
     public void testCheckWinnerIfDiagonalIsAWin() throws Exception {
         Board b = new Board();
         char c = 'X';
         b.updateBoard(c, 1);
         b.updateBoard(c, 5);
         b.updateBoard(c, 9);
 
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 1", 'X', b.checkWinner(c, 1, 1));
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 5", 'X', b.checkWinner(c, 5, 1));
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 9", 'X', b.checkWinner(c, 9, 1));
     }
 
     public void testCheckWinnerIfAntiDiagonalIsAWin() throws Exception {
         Board b = new Board();
         char c = 'X';
         b.updateBoard(c, 3);
         b.updateBoard(c, 5);
         b.updateBoard(c, 7);
 
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 3", 'X', b.checkWinner(c, 3, 1));
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 5", 'X', b.checkWinner(c, 5, 1));
         assertEquals("Test if a diagonal from top left to bottom right full of X's is a win with move 7", 'X', b.checkWinner(c, 7, 1));
     }
 
     public void testCheckWinnerForDraw() throws Exception {
         Board b = new Board();
 
        assertEquals("Test for a draw", 'D', b.checkWinner('X', 1, 8));
     }
     /* public void testCheckWinnerNoWinnerAndNothingPlaced() throws Exception {
         Board b = new Board();
         assertEquals("Test for winner when no winner should return .", '.', b.checkWinner());
     }
 
     public void testCheckWinnerWithWinner0to2() throws Exception {
         Board b = new Board();
 
         b.updateBoard('X', 0);
         b.updateBoard('X', 1);
         b.updateBoard('X', 2);
 
         assertEquals("Test for winner when no winner should return X", 'X', b.checkWinner());
     }
 
     public void testCheckWinnerWithWinner3to5() throws Exception {
         Board b = new Board();
 
         b.updateBoard('X', 3);
         b.updateBoard('X', 4);
         b.updateBoard('X', 5);
 
         assertEquals("Test for winner when no winner should return X", 'X', b.checkWinner());
     }
     public void testCheckWinnerWithWinner0to2O() throws Exception {
         Board b = new Board();
 
         b.updateBoard('O', 0);
         b.updateBoard('O', 1);
         b.updateBoard('O', 2);
 
         assertEquals("Test for winner when no winner should return O", 'O', b.checkWinner());
     }
 
     public void testCheckWinnerWithWinner3to5O() throws Exception {
         Board b = new Board();
 
         b.updateBoard('O', 3);
         b.updateBoard('O', 4);
         b.updateBoard('O', 5);
 
         assertEquals("Test for winner when no winner should return O", 'O', b.checkWinner());
     }
 
     public void testCheckWinnerWithWinner6to8() throws Exception {
         Board b = new Board();
 
         b.updateBoard('X', 6);
         b.updateBoard('X', 7);
         b.updateBoard('X', 8);
 
         assertEquals("Test for winner when no winner should return X", 'X', b.checkWinner());
     }
 
     public void testCheckWinnerWithNoWinner6to8() throws Exception {
         Board b = new Board();
 
         b.updateBoard('X', 6);
         b.updateBoard('O', 7);
         b.updateBoard('X', 8);
 
         assertEquals("Test for winner when no winner should return X", '.', b.checkWinner());
     }
 
     public void testCheckWinnerWithWinnerDiagonal0to8() throws Exception {
         Board b = new Board();
 
         b.updateBoard('X', 0);
         b.updateBoard('X', 2);
         b.updateBoard('X', 4);
         b.updateBoard('X', 8);
 
         assertEquals("Test for winner", 'X', b.checkWinner());
     }   */
 
     public void setUp() throws Exception {
         super.setUp();
     }
 
     public void tearDown() throws Exception {
         super.tearDown();
     }
 
     public static Test suite() {
         return new TestSuite(BoardTest.class);
     }
 }
