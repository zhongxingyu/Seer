 package Test;
 
 import TennisGame.TennisGame;
 import junit.framework.TestCase;
import TennisGame.*;
 
 public class TennisGameTest extends TestCase{
 	
 	public TennisGame testSetup() throws Exception{
 		TennisGame game = new TennisGame();
 		game.setUp();
 		return game;
 	}
 	
 	public void testTennisGame() throws Exception{
 		TennisGame game = testSetup();
 		assert(testEvaluateScore(game, 0, 0));
 	}
 	
 	public void testTennisScore() throws Exception{
 		TennisGame game = testSetup();
 		game.score("sideA");
 		game.score("sideA");
 		game.score("sideB");
 		testEvaluateScore(game, 30, 15);
 	}
 	
 	public boolean testEvaluateScore(TennisGame game, int expectedSideA, int expectedSideB) throws Exception{
 		int[] score = game.getScore();
 		return score[0] == expectedSideA && score[1] == expectedSideB;
 	}
 	
 
 }
