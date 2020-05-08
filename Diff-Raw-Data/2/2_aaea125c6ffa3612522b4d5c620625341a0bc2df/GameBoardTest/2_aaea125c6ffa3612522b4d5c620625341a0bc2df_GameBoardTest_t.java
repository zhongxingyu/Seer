 package life.board;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This Class tests the GameBoard.
  * @author Katy Groves
  *
  */
 public class GameBoardTest {
 
 	private GameBoardImpl classUndTest;
 	private int[][] board1 = { { 1, 0, 0, 0, 1 }, { 0, 1, 0, 0, 0 },
 			{ 1, 1, 0, 0, 1 }, { 1, 0, 0, 1, 1 }, { 0, 1, 0, 0, 0 } };
 
 	@Before
 	public void setUp() throws Exception {
 		classUndTest = new GameBoardImpl(board1, 5, 5);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		classUndTest = null;
 	}
 
 	@Test
 	public void testGetLiveNeighborsMiddleOfBoard() {
 		int result = classUndTest.getLiveNeighbors(2, 2);
 		assertEquals(3, result);
 	}
 
 	@Test
 	public void testGetLiveNeighborsBottomLeftCornerOfBoard() {
 
 		int result = classUndTest.getLiveNeighbors(0, 0);
 		assertEquals(1, result);
 	}
 
 	@Test
 	public void testGetLiveNeighborsBottomRightCornerOfBoard() {
 
 		int result = classUndTest.getLiveNeighbors(0, 4);
 		assertEquals(0, result);
 	}
 
 	@Test
 	public void testGetLiveNeighborsTopLeftCornerOfBoard() {
 
 		int result = classUndTest.getLiveNeighbors(4, 0);
 		assertEquals(2, result);
 	}
 
 	@Test
 	public void testGetLiveNeighborsTopRightCornerOfBoard() {
 
 		int result = classUndTest.getLiveNeighbors(4, 4);
 		assertEquals(2, result);
 	}
 
 	@Test
 	public void testGetLiveNeighborsMidBoard() {
 
 		int result = classUndTest.getLiveNeighbors(2, 1);
 		assertEquals(3, result);
 	}
 
 	@Test
 	public void testInitBoardWithZeros() {
 		String data = "0000000000000000000000000";
 
 		try {
 			classUndTest.setBoard(data);
 			String result = classUndTest.toString();
 			assertEquals(data, result);
 		} catch (Exception e) {
 			fail("Catch an exception: " + e.getMessage());
 		}
 
 	}
 
 	@Test
 	public void testInitBoardWithValidData() {
 		String data = "0100100100010010010100101";
 
 		try {
 			classUndTest.setBoard(data);
 			String result = classUndTest.toString();
 			assertEquals(data, result);
 		} catch (Exception e) {
 			fail("Catch an exception: " + e.getMessage());
 		}
 
 	}
 
 	@Test
 	public void testInitBoardWithAlphaData() {
 		String data = "abcdefg100010010010100101";
 
 		try {
 			classUndTest.setBoard(data);
 			fail("should have caught exception");
 		} catch (Exception e) {
 			assertEquals("Input data must only consist of 1s and 0s", e.getMessage());
 		}
 
 	}
 	
 	@Test
 	public void testInitBoardWithBadData() {
 		String data = "abcdefg100010010010100777";
 
 		try {
 			classUndTest.setBoard(data);
 			fail("should have caught exception");
 		} catch (Exception e) {
 			assertEquals("Input data must only consist of 1s and 0s", e.getMessage());
 		}
 
 	}
 	
 	@Test
 	public void testInitBoardWithInvalidNumData() {
 		String data = "1234567890123456789012345";
 
 		try {
 			classUndTest.setBoard(data);
 			fail("should have caught exception");
 		} catch (Exception e) {
 			assertEquals("Input data must only consist of 1s and 0s", e.getMessage());
 		}
 
 	}
 
 	@Test
 	public void testInitBoardWithTooFewCells() {
		String data = "0000000011111";
 
 		try {
 			classUndTest.setBoard(data);
 			fail("should have caught exception");
 		} catch (Exception e) {
 			assertEquals(true, e.getMessage().contains("Input data does not match board size of"));
 		}
 
 	}
 	
 	@Test
 	public void testGetNextGenNominalCase() {
 		String expected = "0000001000111111011100000";
 			GameBoard result = classUndTest.getNextGeneration();
 			assertEquals(expected, result.toString());
 	}
 	
 	@Test
 	public void testGetNextGenInitAllDead() {
 		String initBoard = "0000000000000000000000000";
 		String expected = "0000000000000000000000000";
 
 		try {
 			classUndTest.setBoard(initBoard);
 			GameBoard result = classUndTest.getNextGeneration();
 			assertEquals(expected, result.toString());
 		} catch (Exception e) {
 			fail("Caught and exception");
 		}
 	}
 	
 	@Test
 	public void testGetNextGenInitAllAlive() {
 		String initBoard = "1111111111111111111111111";
 		String expected =  "1000100000000000000010001";
 
 		try {                                 
 			classUndTest.setBoard(initBoard);
 			GameBoard result = classUndTest.getNextGeneration();
 			assertEquals(expected, result.toString());
 		} catch (Exception e) {
 			fail("Caught and exception");
 		}
 	}
 	
 	@Test
 	public void testGetNextGenInitAllAliveTwoGens() {
 		String initBoard = "1111111111111111111111111";
 		String expected =  "0000000000000000000000000";
 
 		try {                                 
 			classUndTest.setBoard(initBoard);
 			GameBoard firstGen = classUndTest.getNextGeneration();
 			GameBoard result = firstGen.getNextGeneration();
 			assertEquals(expected, result.toString());
 		} catch (Exception e) {
 			fail("Caught and exception");
 		}
 	}
 	
 }
