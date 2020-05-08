 package simpleboard;
 
 import base.testcase.OthelloTestCase;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Sep 20, 2009
  * Time: 6:32:24 PM
  * This is a unit test for SetupBoard
  */
 public class SetupBoardTest extends OthelloTestCase {
     public void testCalcBlackMinusWhite() {
         SetupBoard board = new SetupBoard();
         board.resetToStart();
         assertEquals(0,board.calcBlackMinusWhite());
 
         board.makeMove(1,34);
         assertEquals(3,board.getBlackMinusWhite());
         assertEquals(3,board.calcBlackMinusWhite());
 
         board.makeMove(-1,53);
         assertEquals(0,board.getBlackMinusWhite());
         assertEquals(0,board.calcBlackMinusWhite());
 
         board.makeMove(1,66);
         assertEquals(3,board.getBlackMinusWhite());
         assertEquals(3,board.calcBlackMinusWhite());
     }
 
     public void testSetSquare() {
         SetupBoard board = new SetupBoard();
         board.resetToStart();
         assertEquals(0,board.calcBlackMinusWhite());
 
         assertEquals(-1,board.getSquare(44));
         board.setSquare(1,44);
         assertEquals(1,board.getSquare(44));
         assertEquals(2,board.getBlackMinusWhite());
 
         //test invalid inputs
         try {
             board.setSquare(0,44);
             fail("Should have throw an exception for setting the board wtih an invalid color");
         } catch (Exception e) {
             assertTrue(true);
         }
 
         try {
             board.setSquare(1,8);
             fail("Should have throw an exception for setting the board wtih an invalid location");
         } catch (Exception e) {
             assertTrue(true);
         }
 
         try {
             board.setSquare(-1,91);
             fail("Should have throw an exception for setting the board wtih an invalid location");
         } catch (Exception e) {
             assertTrue(true);
         }
 
         try {
             board.setSquare(1,30);
             fail("Should have throw an exception for setting the board wtih an invalid location");
         } catch (Exception e) {
             assertTrue(true);
         }
 
         try {
             board.setSquare(-1,59);
             fail("Should have throw an exception for setting the board wtih an invalid location");
         } catch (Exception e) {
             assertTrue(true);
         }
     }
 
     public void testSetBoard() {
         int[] myBoard = {
                 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 0, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 0, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 0, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, -1, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, -1, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
                 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
                 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
 
         SetupBoard board = new SetupBoard();
        board.resetToStart();
         board.setBoard(myBoard);
 
         for (int curLocation = 0 ; curLocation<100; curLocation++) {
             assertEquals(myBoard[curLocation],board.getSquare(curLocation));
         }
     }
 }
