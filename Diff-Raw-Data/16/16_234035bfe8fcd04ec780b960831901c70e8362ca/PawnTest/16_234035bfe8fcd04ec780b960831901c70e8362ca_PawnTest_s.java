 package model;
 
 import java.util.ArrayList;
 
 // -------------------------------------------------------------------------
 /**
  *  Tests the pawn class.
  *
  *  @author Robert Scheible (scheible)
  *  @author Jack Cobb (jack3)
  *  @author Loran Steinberger
  *  @version Apr 16, 2013
  */
 public class PawnTest
     extends student.TestCase
 {
     /**
      * The game on which the tests take place.
      */
     Game game;
     private Piece[] makeM;
 
 
     /**
      * Creates a new game to test.
      */
     public void setUp()
     {
         game = new Game();
         makeM = game.getBlackPieces();
     }
 
     /**
      * test for finding valid moves at the start
      */
     public void testFindMove()
     {
         ArrayList<Move> check = makeM[10].getPossibleMoves(game.getBoard());
 
         assertEquals(2, check.size());
     }
 
     /**
      * test for finding valid moves after a pawn has been moved to open up
      * the board a little bit more. It is also checking to make sure that the
      * pawn doesn't try to move 2 spaces again
      *
      */
     public void testFindMoreMoves()
     {
         Move move1 = new Move(makeM[8], new Location(0, 2));
         game.move(move1);
 
         ArrayList<Move> check = makeM[8].getPossibleMoves(game.getBoard());
 
         assertEquals(1, check.size());
        //saying there are no more possible moves after moving only once

     }
 
 
     /**
      * Tests the white pawn moves starting at b2
      */
 //    public void testPawnB2()
 //    {
 //        ArrayList<Location> moves =
 //            game.getWhitePieces()[9].getLegalMoves(game.getBoard());
 //        System.out.println(game.getWhitePieces()[9].getLocal().x());
 //        System.out.println(game.getWhitePieces()[9].getLocal().y());
 //        assertEquals(2, moves.size());
 //        assertEquals(new Location(1, 5), moves.get(0));
 //        assertEquals(new Location(1, 4), moves.get(1));
 //    }
 
 
     /**
      * Tests the white pawn moves at b2, when it can take pieces at a3 and c3
      */
 //    public void testPawnTakeB2()
 //    {
 //        Piece[] black = game.getBlackPieces();
 //
 //        black[8].setLocal(new Location(0, 5));
 //        black[10].setLocal(new Location(2, 5));
 //
 //        game.setBlackPieces(black);
 //        Piece[][] chessBoard = game.getBoard();
 //        chessBoard[0][1] = null;
 //        chessBoard[2][1] = null;
 //        chessBoard[0][5] = black[8];
 //        chessBoard[2][5] = black[10];
 //
 //        ArrayList<Location> moves =
 //            game.getWhitePieces()[9].getLegalMoves(game.getBoard());
 //        assertEquals(4, moves.size());
 //        assertEquals(new Location(1, 5), moves.get(0));
 //        assertEquals(new Location(2, 5), moves.get(1));
 //        assertEquals(new Location(0, 5), moves.get(2));
 //        assertEquals(new Location(1, 4), moves.get(3));
 //        game.print();
 //    }
 //
 //
 //    /**
 //     * Tests the pawn moving.
 //     */
 //    public void testMove()
 //    {
 //        assertNull(game.getBoard()[1][4]);
 //        game.move(game.getWhitePieces()[9], new Location(1, 4));
 //
 //        assertNotNull(game.getBoard()[1][4]);
 //        ArrayList<Location> moves =
 //            game.getWhitePieces()[9].getLegalMoves(game.getBoard());
 //        for (Location move : moves)
 //        {
 //            System.out.println("(" + move.x() + ", " + move.y() + ")");
 //        }
 //    }
 
 }
