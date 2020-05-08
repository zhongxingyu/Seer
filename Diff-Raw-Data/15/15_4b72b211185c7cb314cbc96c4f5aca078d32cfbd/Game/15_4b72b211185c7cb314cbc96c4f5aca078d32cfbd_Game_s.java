 package game;
 
 public class Game {
 
 	boolean playerOneTurn = true;
 	
 	public boolean isEmpty() {
 		return true;
 	}
 
 	public boolean PlayerOnesTurn() {
 		return playerOneTurn;
 	}
 
 	public boolean play(int column) {
		playerOneTurn=false;
 		return playerOneTurn;
 	}
 
 }
