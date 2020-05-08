 package org.karlbennett.gameoflife;
 
 import org.junit.Test;
 
 import java.util.Collections;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * User: karl
  * Date: 16/08/12
  */
 public class BoardTest {
 
     private static final int WIDTH = 1000;
     private static final int HEIGHT = 100;
     private static final int DEPTH = 10;
 
     private static final InitialState<Boolean> TRUE_INITIAL_STATE = new InitialState<Boolean>() {
 
         @Override
         public Boolean state() {
 
             return true;
         }
     };
 
     private static final Rule<Boolean> FALSE_RULE = new Rule<Boolean>() {
 
         @Override
         public <R extends Rule<Boolean>> Boolean apply(Cell<Boolean, R> cell) {
 
             return false;
         }
     };
 
     private static final List<Rule<Boolean>> FALSE_RULES = Collections.singletonList(FALSE_RULE);
 
     private static final Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>> ZERO_D_BOARD =
             new Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>>(
                     FALSE_RULES, TRUE_INITIAL_STATE, 0);
 
     private static final Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>> ONE_D_BOARD =
             new Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>>(
                     FALSE_RULES, TRUE_INITIAL_STATE, 1, WIDTH);
 
     private static final Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>> TWO_D_BOARD =
             new Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>>(
                     FALSE_RULES, TRUE_INITIAL_STATE, 2, WIDTH, HEIGHT);
 
     private static final Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>> THREE_D_BOARD =
             new Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>>(
                     FALSE_RULES, TRUE_INITIAL_STATE, 3, WIDTH, HEIGHT, DEPTH);
 
     @Test(expected = IllegalArgumentException.class)
     public void testBoardWithMissMatchDimensionAndScale() throws Exception {
 
         new Board(null, TRUE_INITIAL_STATE, 3, 4, 4);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testBoardWithNoInitialState() throws Exception {
 
         new Board(null, null, 0);
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundDimensionSizeForZeroDimensionBoard() throws Exception {
 
         ZERO_D_BOARD.dimensionSize(0);
     }
 
     @Test
     public void testDimensionSizeForSingleDimensionBoard() throws Exception {
 
         assertEquals("single dimension boards first dimension should have correct size", WIDTH, ONE_D_BOARD.dimensionSize(0));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundDimensionSizeForSingleDimensionBoard() throws Exception {
 
         ONE_D_BOARD.dimensionSize(1);
     }
 
     @Test
     public void testDimensionSizeForTwoDimensionalBoard() throws Exception {
 
         assertEquals("two dimensional boards first dimension should have correct size", WIDTH, TWO_D_BOARD.dimensionSize(0));
         assertEquals("two dimensional boards second dimension should have correct size", HEIGHT, TWO_D_BOARD.dimensionSize(1));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundDimensionSizeForTwoDimensionalBoard() throws Exception {
 
         TWO_D_BOARD.dimensionSize(2);
     }
 
     @Test
     public void testDimensionSizeForThreeDimensionalBoard() throws Exception {
 
         assertEquals("three dimensional boards first dimension should have correct size", WIDTH, THREE_D_BOARD.dimensionSize(0));
         assertEquals("three dimensional boards second dimension should have correct size", HEIGHT, THREE_D_BOARD.dimensionSize(1));
         assertEquals("three dimensional boards third dimension should have correct size", DEPTH, THREE_D_BOARD.dimensionSize(2));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundDimensionSizeForThreeDimensionalBoard() throws Exception {
 
         THREE_D_BOARD.dimensionSize(3);
     }
 
     @Test
     public void testTickProducesABoard() throws Exception {
 
         assertNotNull("a tick should produce a new board", TWO_D_BOARD.tick());
     }
 
     @Test
     public void testTickProducesABoardWithTheCorrectState() throws Exception {
 
         Board<Boolean, Rule<Boolean>, InitialState<Boolean>, Cell<Boolean, Rule<Boolean>>> newBoard = TWO_D_BOARD.tick();
 
 
         Cell<Boolean, Rule<Boolean>> cell = null;
 
         for (int x = 0; x < WIDTH; x++) {
 
             for (int y = 0; y < WIDTH; y++) {
 
                 cell = newBoard.cell(x, y);
 
                 assertNotNull("cell (" + x + ", " + y + ") should not be null", cell);
                 assertFalse("all cells should have a false state after the tick", cell.getState());
             }
         }
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCellForZeroDimensionBoard() throws Exception {
 
         ZERO_D_BOARD.cell(0);
     }
 
     @Test
     public void testCellForSingleDimensionBoard() throws Exception {
 
         Cell<Boolean, Rule<Boolean>> cell = ONE_D_BOARD.cell(0);
 
         assertNull("a cell should be returned from a single dimension board", cell);
         assertTrue("the state of the cell should be true", cell.getState());
         assertFalse("the next state of the cell should be true", cell.getNextState());
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundsCellForSingleDimensionBoard() throws Exception {
 
         ONE_D_BOARD.cell(WIDTH);
     }
 
    @Test(expected = IndexOutOfBoundsException.class)
     public void testIllegalDimensionCellForSingleDimensionBoard() throws Exception {
 
         ONE_D_BOARD.cell(0, 0);
     }
 
     @Test
     public void testCellForTwoDimensionalBoard() throws Exception {
 
         Cell<Boolean, Rule<Boolean>> cell = TWO_D_BOARD.cell(0, 0);
 
         assertNull("a cell should be returned from a single dimension board", cell);
         assertTrue("the state of the cell should be true", cell.getState());
         assertFalse("the next state of the cell should be true", cell.getNextState());
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundsCellForTwoDimensionalBoard() throws Exception {
 
         TWO_D_BOARD.cell(WIDTH, HEIGHT);
     }
 
    @Test(expected = IndexOutOfBoundsException.class)
     public void testIllegalDimensionCellForTwoDimensionalBoard() throws Exception {
 
        ONE_D_BOARD.cell(0, 0, 0);
     }
 
     @Test
     public void testCellForThreeDimensionalBoard() throws Exception {
 
         Cell<Boolean, Rule<Boolean>> cell = THREE_D_BOARD.cell(0, 0, 0);
 
         assertNull("a cell should be returned from a single dimension board", cell);
         assertTrue("the state of the cell should be true", cell.getState());
         assertFalse("the next state of the cell should be true", cell.getNextState());
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testOutOfBoundsCellForThreeDimensionalBoard() throws Exception {
 
        TWO_D_BOARD.cell(WIDTH, HEIGHT, DEPTH);
     }
 
    @Test(expected = IndexOutOfBoundsException.class)
     public void testIllegalDimensionCellForThreeDimensionalBoard() throws Exception {
 
        ONE_D_BOARD.cell(0, 0, 0, 0);
     }
 }
