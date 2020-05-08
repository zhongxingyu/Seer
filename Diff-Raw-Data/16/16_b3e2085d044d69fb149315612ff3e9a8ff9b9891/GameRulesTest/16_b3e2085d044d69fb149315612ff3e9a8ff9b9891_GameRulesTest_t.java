 package com.cozycoding.crea1.tetris;
 
 import com.cozycoding.crea1.tetris.blocks.Cell;
 import com.cozycoding.crea1.tetris.blocks.OBlock;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author Marius Kristensen
  */
 public class GameRulesTest {
 
     private Cell filledCell = new Cell(true,0,0);
     private Cell emptyCell = new Cell(false,0,0);
     GameRules gameRules;
 
     @Before
     public void setUp() throws Exception {
         gameRules = new GameRules();
     }
 
     @Test
     public void testGameBoardIsFilledWithEmptyCells() throws Exception {
         for (int row = 0; row < gameRules.getGameBoard().length; row++) {
             assertFalse(gameRules.gameBoardz.isRowFilled(gameRules.getGameBoard()[row]));
         }
     }
 
     @Test
     public void testGameBoardFullOfEmptyRowsOnGameStart() {
         for (Cell[] row : gameRules.getGameBoard()) {
             assertFalse(gameRules.gameBoardz.isRowFilled(row));
         }
     }
 
     @Test
     public void testRowIsFilledReturnTrue() throws Exception {
         Cell[] cellRow = {filledCell, filledCell, filledCell, filledCell};
         assertTrue(gameRules.gameBoardz.isRowFilled(cellRow));
     }
 
     @Test
     public void testOneRowIsNotFilledReturnsFalse() throws Exception {
         Cell[] cellRow = {filledCell, emptyCell, filledCell, filledCell};
         assertFalse(gameRules.gameBoardz.isRowFilled(cellRow));
     }
 
     @Test
     public void testMoveBlockUntilBottomButNoFurther() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         while (!gameRules.isActiveBlockAtBottom()) {
             gameRules.getActiveBlock().moveDown();
            if (gameRules.getActiveBlock().getShape().get(3).getY() == GameRules.noOfRows) {
                 fail();
             }
         }
        assertEquals(GameRules.noOfRows -1, gameRules.getActiveBlock().getShape().get(3).getY());
     }
 
     @Test
     public void testMoveBlockLeftToWallButNoFurther() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         while (!gameRules.isActiveBlockAtLeftWall()) {
             gameRules.getActiveBlock().moveLeft();
             if (gameRules.getActiveBlock().getShape().get(0).getX() < 0) {
                 fail();
             }
         }
         assertEquals(0, 0);
     }
 
     @Test
     public void testMoveBlockRightToWallButNoFurther() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         while (!gameRules.isActiveBlockAtRightWall()) {
             gameRules.getActiveBlock().moveRight();
            if (gameRules.getActiveBlock().getShape().get(1).getX() == GameRules.noOfColumns) {
                 fail();
             }
         }
        assertEquals(GameRules.noOfColumns - 1, gameRules.getActiveBlock().getShape().get(2).getX());
     }
 
     @Test
     public void testMergeActiveBlockWithGameboard() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         gameRules.stopActiveBlockAndMergeItWithBoard();
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
     }
 
     @Test
     public void testCrashWithOtherBlock() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         while (!gameRules.isActiveBlockAtBottom()) {
             gameRules.getActiveBlock().moveDown();
         }
         gameRules.stopActiveBlockAndMergeItWithBoard();
         OBlock secondBlock = new OBlock();
         secondBlock.moveLeft();
         gameRules.placeBlockOnGameBoard(secondBlock);
         while (!gameRules.hasCrashedWithOtherCells()) {
             gameRules.getActiveBlock().moveDown();
         }
         gameRules.stopActiveBlockAndMergeItWithBoard();
 
         assertTrue(gameRules.getGameBoard()[14][3].isFilled());
         assertTrue(gameRules.getGameBoard()[15][3].isFilled());
         assertTrue(gameRules.getGameBoard()[14][4].isFilled());
         assertTrue(gameRules.getGameBoard()[15][4].isFilled());
     }
 
     @Test
     public void testCrashWithOtherBlockOnTheRight() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         gameRules.getActiveBlock().moveRight();
         gameRules.getActiveBlock().moveRight();
         gameRules.stopActiveBlockAndMergeItWithBoard();
         OBlock secondBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(secondBlock);
         gameRules.moveActiveBlockRight();
         gameRules.stopActiveBlockAndMergeItWithBoard();
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
     }
 
     @Test
     public void testCrashWithOtherBlockOnTheLeft() throws Exception {
         OBlock oBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(oBlock);
         gameRules.getActiveBlock().moveLeft();
         gameRules.getActiveBlock().moveLeft();
         gameRules.stopActiveBlockAndMergeItWithBoard();
         OBlock secondBlock = new OBlock();
         gameRules.placeBlockOnGameBoard(secondBlock);
         gameRules.moveActiveBlockLeft();
         gameRules.stopActiveBlockAndMergeItWithBoard();
         printGameBoard(gameRules.getGameBoard());
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[0][4].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
         assertTrue(gameRules.getGameBoard()[1][5].isFilled());
     }
 
     private void printGameBoard(Cell[][] gameBoard) {
         System.out.print(" |");
         for (int i = 0; i < gameBoard[0].length; i++) {
             System.out.print(i);
         }
         for (int i = 0; i < gameBoard.length; i++) {
             System.out.print("\n"+ i +"|");
             Cell[] row = gameBoard[i];
            for (Cell column : row) {
                if (column.isFilled()) {
                     System.out.print("#");
                 } else {
                     System.out.print(" ");
                 }
             }
             System.out.print("|");
         }
     }
 
 
 }
