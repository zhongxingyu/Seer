 /**
  * 
  * @author Nathan Hyatt
  * 
  * File: TestGameOver.java
  * Purpose: Test game over status of game.
  * 
  */
 package com.team14.tests;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import com.team14.Game;
 
 public class TestGameOver
 {
 	/**
 	 * Test getters and setters of Game class.
 	 */
 	@Test
 	public void test()
 	{
		Game testGame = new Game();
 		boolean gameOver = true;		
 		
 		// Should not be equal. Lives have not been lost.
 		boolean g = testGame.isGameOver();
 		assertNotSame(gameOver, g);
 		
 		// Now lose all the lives. New game defaults to 3 lives (0-2)
		testGame.loseLife();
		testGame.loseLife();
		testGame.loseLife();
 		
 		assertTrue(testGame.isGameOver());
 	}
 }
