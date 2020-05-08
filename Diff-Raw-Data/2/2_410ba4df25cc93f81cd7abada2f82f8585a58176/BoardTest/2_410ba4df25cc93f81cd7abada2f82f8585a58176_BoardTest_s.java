 package iago;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import iago.Player.PlayerType;
 
 import org.junit.Test;
 
 public class BoardTest {
 
 	@Test
 	public void testValidMoves() {
 		Board.BoardState[][] testBoardData = DebugFunctions.makeSolidBoardStateArray(Board.BoardState.EMPTY);
 		testBoardData[0][0] = Board.BoardState.BLACK;
 		testBoardData[0][1] = Board.BoardState.WHITE;
 		testBoardData[1][0] = Board.BoardState.WHITE;
 		testBoardData[1][1] = Board.BoardState.WHITE;
 		testBoardData[4][4] = Board.BoardState.BLACK;
 		Board testBoard = new Board(testBoardData,5);
 		Set<Move> possibleMoves = testBoard.validMoves(PlayerType.BLACK);
 		assertEquals(possibleMoves.size(),3);
 		
 		Set<Move> correctPossibleMoves = new HashSet<Move>();
 		correctPossibleMoves.add(new Move(2,2));
 		correctPossibleMoves.add(new Move(2,0));
 		correctPossibleMoves.add(new Move(0,2));
 		assertEquals(possibleMoves,correctPossibleMoves);
 	}
 
 	@Test
 	public void testScoreMove() {
 		Board.BoardState[][] testBoardData = DebugFunctions.makeSolidBoardStateArray(Board.BoardState.EMPTY);
 		testBoardData[0][0] = Board.BoardState.BLACK;
 		testBoardData[0][1] = Board.BoardState.WHITE;
 		testBoardData[0][2] = Board.BoardState.BLACK;
 		testBoardData[0][3] = Board.BoardState.WHITE;
 		testBoardData[0][4] = Board.BoardState.WHITE;
 		testBoardData[1][3] = Board.BoardState.WHITE;
 		testBoardData[1][3] = Board.BoardState.BLACK;
 		Board testBoard = new Board(testBoardData,7);
 		assertEquals(testBoard.scoreMove(new Move(0,5), PlayerType.BLACK),2);
 		assertEquals(testBoard.scoreMove(new Move(2,3), PlayerType.WHITE),1);
 	}
 
 	@Test
 	public void testGet() {
 		Board testBoard = new Board();
 		assertEquals(testBoard.get(0,0),Board.BoardState.EMPTY);
 		
 	}
 
 
 	@Test
 	public void testScoreBoard() {
 		Board.BoardState[][] testBoardData = DebugFunctions.makeSolidBoardStateArray(Board.BoardState.EMPTY);
 		Random generator = new Random();
 		int seed = generator.nextInt();
 		Random seededGenerator = new Random(generator.nextInt());
 		for(int i = 0; i < seededGenerator.nextInt(50); i++)
 		{
 			int x = seededGenerator.nextInt(10);
 			int y = seededGenerator.nextInt(10);
 			boolean player = seededGenerator.nextBoolean();
 			testBoardData[x][y] = player?Board.BoardState.WHITE:Board.BoardState.BLACK;
 		}
 		Board testBoard = new Board(testBoardData,0);
 		//We're going to fail
 		if(testBoard.scoreBoard(PlayerType.BLACK) != -testBoard.scoreBoard(PlayerType.WHITE))
 		{
 			System.out.println("[BoardTest] testScoreBoard: Failure with seed "+seed); //Print the seed so the error is reproducible
 		}
 		assertEquals(testBoard.scoreBoard(PlayerType.BLACK),-testBoard.scoreBoard(PlayerType.WHITE));
 	}
 
 	@Test
 	public void testApply() {
 		Board.BoardState[][] testBoardData = DebugFunctions.makeSolidBoardStateArray(Board.BoardState.EMPTY);
 		testBoardData[4][5] = Board.BoardState.WHITE;
 		testBoardData[5][4] = Board.BoardState.WHITE;
 		testBoardData[4][4] = Board.BoardState.BLACK;
 		testBoardData[5][5] = Board.BoardState.BLACK;
 		Board testBoard = new Board(testBoardData,0);
		Board sameBoard = testBoard.apply(new Move(0,0), PlayerType.BLACK);
 		for(int x = 0; x < Board.BOARD_SIZE; x++)
 		{
 			for(int y = 0; y < Board.BOARD_SIZE; y++)
 			{
 				assertEquals(testBoard.get(x, y),sameBoard.get(x,y));
 			}
 		}
 	}
 
 }
