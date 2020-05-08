 
 /**
  * This Abstract class provides a schema that all players
  * adhere to. They need a nextMove() method which they can
  * determine. The thought process for doing this is to provide
  * multiple game play modes. Right now this allows for a 
  * Sudoku solver and also a single player style. In the future
  * it could also provide a helper player which could provide 
  * hints if the user gets stuck. 
  * 
  * @author Andrew Hood
  * @version 0.1
  */
 public abstract class Player
 {
	Gameboard board;
 	
 	public Player(Gameboard board)
 	{
 		this.board = board;
 	}
 	
     abstract Move nextMove();
     
     public void displayBoard()
     {
         System.out.println(board);
     }
 }
