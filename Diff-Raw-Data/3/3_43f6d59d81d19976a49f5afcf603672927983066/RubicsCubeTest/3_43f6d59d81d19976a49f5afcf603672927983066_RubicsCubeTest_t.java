 package cyfn.rubics;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class RubicsCubeTest {
 	
 	RubicsCube testCube;
 	static final int DIMENSION = 3;
 
 	@Before
 	public void setUp() throws Exception {
 		testCube = new RubicsCube(DIMENSION);
 	}
 	
 	@Test
 	public void testRubicsCube() {
 		// Cube creation
 		assertNotNull(testCube);
 	}
 	
 	@Test
 	public void testIsSolved() {
 		// fresh cube must be solved
 		assertTrue(testCube.isSolved());
 	}
 
 	@Test
 	public void testGetSidePicesColor() {
 		// test for all sides
 		for(Side side: Side.values()) {
 			String[][] sideStringArray = testCube.getSidePicesColor(side);
 			// Dimensions of side array
 			assertEquals(DIMENSION, sideStringArray.length);
 			for(int i=0;i< sideStringArray.length;i++) {
 				assertEquals(DIMENSION, sideStringArray[i].length);
 			}
 			
 			// all pieces of a new cube must be of the same color
 			String prevValue = sideStringArray[0][0];
 			for(int i=0;i<sideStringArray.length;i++)
 				for(int j=0;j<sideStringArray[i].length;j++)
 					assertEquals(prevValue, sideStringArray[i][j]);
 		}			
 	}
 	
 	@Test
 	public void testTurnFaceDirectionSideInt() {
 		if(DIMENSION>3) fail("No test written for 4+ dim cube");
 	}
 
 	@Test
 	public void testTurnFaceDirectionSide() {
 		// R2 D D Fi F2 F F F D2 R R Ri R
 		testCube.turnFace(Direction.HALFTURN, Side.RIGHT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.DOWN);
 		testCube.turnFace(Direction.CLOCKWISE, Side.DOWN);
 		testCube.turnFace(Direction.COUNTERCLOCKWISE, Side.FRONT);
 		testCube.turnFace(Direction.HALFTURN, Side.FRONT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.FRONT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.FRONT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.FRONT);
 		testCube.turnFace(Direction.HALFTURN, Side.DOWN);
 		testCube.turnFace(Direction.CLOCKWISE, Side.RIGHT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.RIGHT);
 		testCube.turnFace(Direction.COUNTERCLOCKWISE, Side.RIGHT);
 		testCube.turnFace(Direction.CLOCKWISE, Side.RIGHT);
 		
 		assertTrue(testCube.isSolved());
 		
 	}
 
 	@Test(timeout=100)
 	public void testUndoLastTurn() {
 		// undo helps unscramble
 		testCube.scramble();
 		while(!testCube.isSolved()) {
 			testCube.undoLastTurn();
 		}
 	}
 
 	@Test
 	public void testScramble() {
 		// scrambled cube must not be solved
 		testCube.scramble();
 		assertFalse(testCube.isSolved());
 	}
 
 	@Test
 	public void testGetTurnLog() {
 		// initially turn log is empty
 		assertEquals("", testCube.getTurnLog());
 		// some turns and a test
 		testCube.turnFace(Direction.CLOCKWISE, Side.DOWN);
 		testCube.turnFace(Direction.COUNTERCLOCKWISE, Side.RIGHT);
 		testCube.turnFace(Direction.HALFTURN, Side.FRONT);
 		assertEquals("D Ri F2 ", testCube.getTurnLog());	
 	}
 	
 	@Test
 	public void testPerformTurnsWithBadInput() {
 		// Must throw an exception on bad input
 		try {
 			testCube.performTurns("R Ri Uo Ui");	// Uo is illegal
 			fail("Bad imput must result in IllegalArgumentException");
 			} catch(IllegalArgumentException e) {}
 		try {
 			testCube.performTurns("R Ri U 2 Ui");	// 2 is illegal
 			fail("Bad imput must result in IllegalArgumentException");
 			} catch(IllegalArgumentException e) {}
 	}
 	
 	@Test
 	public void testPerformTurns() {
 		// Turn test
		testCube.turnFace(Direction.HALFTURN, Side.RIGHT);
		testCube.performTurns("D D Fi F2 F F F D2 R R Ri R");
 		assertTrue(testCube.isSolved());
 	}
 
 }
