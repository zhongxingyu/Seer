 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class GameBoardTest {
 
 	@Test
 	public void testCreateBoard() {
		String[][] map = new String();
 		GameBoard gameBoard = new GameBoard(map);
 	}
 
 }
