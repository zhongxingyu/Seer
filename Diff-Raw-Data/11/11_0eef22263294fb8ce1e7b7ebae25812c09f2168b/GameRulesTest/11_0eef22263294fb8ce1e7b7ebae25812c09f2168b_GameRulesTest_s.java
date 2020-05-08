 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package capstone.game;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author rjmurphy
  */
 public class GameRulesTest {
 
     public GameRulesTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     /**
      * Test of checkStatusBoard method, of class GameRules.
      */
     @Test
     public void testCheckStatusBoard() {
         int[][] board = new int[3][3];
         ArrayList result = new ArrayList();
         ArrayList expResult = new ArrayList();
         board[0][0] = 1;
         board[1][0] = 1;
         board[2][0] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][1] = 1;
         board[1][1] = 1;
         board[2][1] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][2] = 1;
         board[1][2] = 1;
         board[2][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][0] = 1;
         board[0][1] = 1;
         board[0][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[1][0] = 1;
         board[1][1] = 1;
         board[1][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[2][0] = 1;
         board[2][1] = 1;
         board[2][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][0] = 1;
         board[1][1] = 1;
         board[2][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][2] = 1;
         board[1][1] = 1;
         board[2][0] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(1);
 
         board = new int[3][3];
         board[0][0] = 1;
         board[0][1] = 1;
         board[0][2] = 2;
         board[1][0] = 2;
         board[1][1] = 2;
         board[1][2] = 1;
         board[2][0] = 1;
         board[2][1] = 2;
         board[2][2] = 1;
         result.add(GameRules.checkStatusBoard(board));
         expResult.add(3);
 
         assertEquals(expResult, result);
     }
 
     /**
      * Test of isDone method, of class GameRules.
      */
     @Test
     public void testIsDone() {
        System.out.println("isDone");
        GameState board = null;
         boolean expResult = false;
        boolean result = GameRules.isDone(board);
         assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 
     /**
      * Test of validMove method, of class GameRules.
      */
     @Test
     public void testValidMove() {
         ArrayList results = new ArrayList();
         ArrayList expResults = new ArrayList();
         GameState state = new GameState();
         Coordinates coord = new Coordinates(0,0,0,0);
 
         if (GameRules.validMove(state, coord))
         {
             results.add(1);
             state.PlacePiece(coord, 2);
         }
         else results.add(0);
         expResults.add(1);
 
         if (GameRules.validMove(state, coord))
         {
             results.add(1);
         }
         else results.add(0);
         expResults.add(0);
         
         coord = new Coordinates(0,0,1,1);
         if (GameRules.validMove(state, coord))
         {
             results.add(1);
             state.PlacePiece(coord, 2);
         }
         else results.add(0);
         expResults.add(1);
 
         coord = new Coordinates(0,0,2,2);
         if (GameRules.validMove(state, coord))
         {
             results.add(1);
             state.PlacePiece(coord, 2);
         }
         else results.add(0);
         expResults.add(1);
 
         coord = new Coordinates(0,0,1,2);
         if (GameRules.validMove(state, coord))
         {
             results.add(1);
         }
         else results.add(0);
         expResults.add(0);
 
         assertEquals(expResults, results);
     }
 
 }
