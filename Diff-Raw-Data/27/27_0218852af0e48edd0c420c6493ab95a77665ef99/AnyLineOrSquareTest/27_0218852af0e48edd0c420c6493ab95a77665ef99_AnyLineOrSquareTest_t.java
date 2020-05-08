 package pylos.test;
 
 import pylos.model.Model;
 import pylos.model.Position;
 
 public class AnyLineOrSquareTest extends PylosTestCase {
 
 	public void testAnySquare() {
 		MainTest.gameSample();
 
 		/**
 		 * ooo.
 		 * ooo. o..
 		 * ..o. ...
 		 * .... ...
 		 */
 
 		assertTrue(Model.currentPlayer.anyLineOrSquare(Position.at(0, 0, 0)));
 		assertTrue(Model.currentPlayer.anyLineOrSquare(Position.at(2, 1, 0)));
 		assertFalse(Model.currentPlayer.anyLineOrSquare(Position.at(2, 2, 0)));
 	}
 
 	public void testAnyLinesOrSquares2() {
 		MainTest.complexGameSample();
 
 		/**
 		 * o..o
 		 * ooo. ...
 		 * ooo. oo. ..
 		 * ooo. oo. .. .
 		 */
 
 		assertTrue(Model.player1.anyLineOrSquare(Position.at(2, 1, 0)));
 		assertTrue(Model.player1.anyLineOrSquare(Position.at(1, 1, 1)));
 		assertTrue(Model.player1.anyLineOrSquare(Position.at(0, 2, 1)));
 		assertTrue(Model.player1.anyLineOrSquare(Position.at(0, 0, 0)));
 		assertTrue(Model.player1.anyLineOrSquare(Position.at(3, 0, 0)));
 
 		Model.player2.putBallOnBoard(Position.at(0, 0, 2));
 		Model.player2.putBallOnBoard(Position.at(0, 1, 2));
 		Model.player2.putBallOnBoard(Position.at(1, 0, 2));
 		Model.player2.putBallOnBoard(Position.at(1, 1, 2));
 
 		assertFalse(Model.player2.anyLineOrSquare(Position.at(0, 0, 2)));
 	}

	public void testDiagonal() {
		Model.player2.putBallOnBoard(Position.at(0, 2, 1));
		Model.player2.putBallOnBoard(Position.at(1, 1, 1));

		// Model.player1.putBallOnBoard(Position.at(1, 0, 1));

		/**
		 * ...
		 * .o.
		 * o..
		 */
		assertFalse(Model.player2.anyLineOrSquare(Position.at(1, 1, 1)));
		assertFalse(Model.player2.anyLineOrSquare(Position.at(0, 2, 1)));

	}
 }
