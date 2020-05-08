 package com.github.kpacha.jkata.tennis.test;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.kpacha.jkata.tennis.Tennis;
 
 public class TennisTest extends TestCase {
 
     private Tennis game;
 
     @Before
     public void setUp() {
 	game = new Tennis();
     }
 
     private void setScore(int pointsPlayerOne, int pointsPlayerTwo) {
 	for (int counter = 0; counter < pointsPlayerOne; counter++)
 	    game.playerOneScores();
 	for (int counter = 0; counter < pointsPlayerTwo; counter++)
 	    game.playerTwoScores();
     }
 
     @Test
     public void testInitialScore() {
 	assertEquals("0 - 0", game.getScore());
     }
 
     @Test
     public void testFirstPointScore() {
 	setScore(1, 0);
 	assertEquals("15 - 0", game.getScore());
     }
 
     @Test
     public void testSecondPointScore() {
 	setScore(2, 0);
 	assertEquals("30 - 0", game.getScore());
     }
 
     @Test
     public void testThirdPointScore() {
 	setScore(3, 0);
	assertEquals("40 - 0", game.getScore());
     }
 
     @Test
     public void testPlayerOneWinsScore() {
 	setScore(4, 0);
 	assertEquals("Player 1 wins", game.getScore());
     }
 
     @Test
     public void testPlayerTwoFirstPointScore() {
 	setScore(0, 1);
 	assertEquals("0 - 15", game.getScore());
     }
 
     @Test
     public void testFirstDeuceScore() {
 	setScore(3, 3);
 	assertEquals("Deuce", game.getScore());
     }
 
     @Test
     public void testAdvantageScore() {
 	setScore(4, 3);
 	assertEquals("Advantage Player 1", game.getScore());
     }
 
     @Test
     public void testSecondDeuceScore() {
 	setScore(4, 4);
 	assertEquals("Deuce", game.getScore());
     }
 
     @Test
     public void testSecondAdvantageScore() {
 	setScore(5, 4);
 	assertEquals("Advantage Player 1", game.getScore());
     }
 
     @Test
     public void testThirdAdvantageScore() {
 	setScore(5, 6);
 	assertEquals("Advantage Player 2", game.getScore());
     }
 
     @Test
     public void testWinAfterAdvantageScore() {
 	setScore(6, 4);
 	assertEquals("Player 1 wins", game.getScore());
     }
 
     @Test
     public void testPlayerTwoWinsAfterAdvantageScore() {
 	setScore(6, 8);
 	assertEquals("Player 2 wins", game.getScore());
     }
 }
