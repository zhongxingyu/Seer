 package gameObjects;
 
 import boardEditor.core.StaticBoards;
 
 /**
  * The class to play to play the game.
  */
public abstract class Game 
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
 	
 	public void menu()
 	{
		
 	}
 }
