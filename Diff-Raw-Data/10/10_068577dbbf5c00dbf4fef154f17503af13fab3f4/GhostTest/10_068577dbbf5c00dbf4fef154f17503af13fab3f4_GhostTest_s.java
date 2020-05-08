 /**
  * 
  */
 package edu.sjsu.cs.ghost151;
 
 import static org.junit.Assert.*;
 import java.util.Random;
 import org.junit.Test;
 
 /**
  * @author Alben Cheung
  * @author MD Ashfaqul Islam
  * @author Shaun Guth
  * @author Jerry Phul
  */
 public class GhostTest {
 
 	/**
 	 * Test method for {@link edu.sjsu.cs.ghost151.Ghost#getExploredPositions()}
 	 * .
 	 */
 	@Test
 	public void testGetExploredPositions() {
 		BoardObjectType[][] explored = new BoardObjectType[20][10];
 		Ghost testGhost = new Ghost();
 		testGhost.setExploredPositions(explored);
 		assertNotNull(testGhost.getExploredPositions());
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.sjsu.cs.ghost151.Ghost#setExploredPositions(edu.sjsu.cs.ghost151.BoardPosition[])}
 	 * .
 	 */
 	@Test
 	public void testSetExploredPositions() {
 		BoardObjectType[][] explored = new BoardObjectType[20][10];
 		Ghost testGhost = new Ghost();
 		testGhost.setExploredPositions(explored);
 		assertArrayEquals(explored, testGhost.getExploredPositions());
 	}
 	
 	/**
 	 * Test method for
 	 * {@link edu.sjsu.cs.ghost151.Ghost#isTargetAcquired()}
 	 * .
 	 */
 	@Test
 	public void testIsTargetAcquired(){
 		Ghost targetTest = new Ghost();
 		assertFalse(targetTest.IsTargetAcquired());
 	}
 	
 	/**
 	 * Test method for
 	 * {@link edu.sjsu.cs.ghost151.Ghost#Move()}
 	 * .
 	 */
 	/*
 	 * Bad logic to test?
 	 * @Test
 	public void testMove(){
 		Board newBoard = Board.INSTANCE;
 		BoardPosition testBP = new BoardPosition(10,5);
 		BoardObject testObj = new BoardObject(BoardObjectType.Target);
 		newBoard.SetObjectAt(testBP, testObj);
 		
 		Ghost moveTest = new Ghost();
 		BoardPosition testGBP = new BoardPosition(10,4);
 		moveTest.setPosition(testGBP);
 		while(!moveTest.IsTargetAcquired()){		
 			moveTest.Scan();
 			moveTest.Move();
 		}
 		assertEquals(testBP, moveTest.getPosition());
 	}*/
 	
 	/**
 	 * Test method for
 	 * {@link edu.sjsu.cs.ghost151.Ghost#Scan()}
 	 * .
 	 */
 	/*@Test
 	public void testScan(){
 		
 		
 	}*/
 	
 	/**
 	 * Test method for
 	 * {@link edu.sjsu.cs.ghost151.Ghost#CommunicateWith(edu.sjsu.cs.ghost151.Ghost)}
 	 * .
 	 */
 	@Test
 	public void testCommunicateWith(){
 		Ghost g1 = new Ghost();
		BoardObjectType[][] explored = new BoardObjectType[20][10];
 		g1.setExploredPositions(explored);
 		Ghost g2 = new Ghost();	
 		BoardObjectType[][] explored2 = new BoardObjectType[10][10];
 		g2.setExploredPositions(explored2);
 		g1.CommunicateWith(g2);
 		assertArrayEquals(g1.getExploredPositions(), g2.getExploredPositions());		
 	}
 	
 	
 }
