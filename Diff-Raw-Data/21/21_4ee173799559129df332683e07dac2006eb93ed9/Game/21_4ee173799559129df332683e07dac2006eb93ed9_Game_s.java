 package gameObjects;
 
import gameObjects.Board;
 import boardEditor.core.StaticBoards;
 
 /**
  * The class to play to play the game.
  */
public class Game 
 {
 	//<editor-fold defaultstate="collapsed" desc="MEMBER VARIABLES">
 	/**
 	 *The game base class
 	 */
 	protected Board board;
 	//</editor-fold>
 	
 	/**
 	 * The base class for playing the game.
 	 */
 	public Game()
 	{
 		board = StaticBoards.getEmptyBoard();
 	}
 	
	/**
	 *Main Method for testing
	 * @param args
	 */
	public static void main(String[] args)
 	{
		Game g = new Game();
 	}
 }
