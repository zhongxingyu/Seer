 package main;
 
 public class Game {
 	private static Game instance;
 	private Board board = Board.getInstance();
 	private Player redPlayer, blackPlayer, currentPlayer, defendingPlayer;
 	private boolean gameOver = false;
 	
 	private Game(){	}
 	
 	/**
 	 * Method to obtain the Singleton instance of the Game. If the Game is not yet instantiated, it will be.
 	 * @return instance the Singletom instance of the Game.
 	 */
 	public static synchronized Game getInstance(){
 		if(instance == null){
 			instance = new Game();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Initialization method for the game, to create the players.
 	 * @param mode Integer value corresponding to the number of human Players, 1 or 2. AI Players will fill the rest.
 	 */
 	public void initialize(int mode) {
 		switch(mode) {
 			case 1:
 				blackPlayer = new AIPlayer(Colour.BLACK,board);
 				redPlayer = new HumanPlayer(Colour.RED,board); 
 				break;
 			case 2: 
 				blackPlayer = new HumanPlayer(Colour.BLACK,board);
 				redPlayer = new HumanPlayer(Colour.RED,board); 
 				break;
 		}
 		currentPlayer = redPlayer;
 	}
 	
 	/**
 	 * Main Loop to continue playing the game until one player or the other can no longer move.
 	 */
 	public void play() {
 		int turn = 1;
 		while(!gameOver()) {
 			board.printArray();
 			board.resetTurn();
 			switch(turn) {
 				case 0: currentPlayer = blackPlayer;
 						System.out.println(currentPlayer + " has " + currentPlayer.getPieces().length + " pieces.");
 						turn +=1;
 						break;
 				case 1: currentPlayer = redPlayer;
 						System.out.println(currentPlayer + " has " + currentPlayer.getPieces().length + " pieces.");
 						turn -=1;
 						break;
 			}
 			System.out.println("Turn: "+ currentPlayer.toString());
 			currentPlayer.myTurn();
 			
 			// The following loop will only end when the Observable Board's state has changed, ie a piece has been moved.
 			while(currentPlayer.isMyTurn() == true) {
 				if(!currentPlayer.isHuman()){
 					currentPlayer.makeCurrentMove();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @return The Player whose turn it is.
 	 */
 	public Player currentPlayer() { 
 		return currentPlayer; 
 	}
 	
 	/**
 	 * <!--Accessor method-->
 	 * <ul><li><b>Game</b></li></ul>
 	 * <ul>
 	 * 	Checks whether the game is still happening or not.
 	 * 	<p>
 	 * 	@return isGameOver The state of whether the game is over or not.
 	 * </ul>
 	 */
 	public boolean gameOver(){
 		boolean isGameOver = false;
 		currentPlayer.updatePieces();
 		
 		if (currentPlayer.update().length > 0) {
 			isGameOver = false;
 		}
 		if (redPlayer.update().length == 0) {
 			System.out.println("Black is the winner!");
 			if(isSinglePlayer()) {
 				System.out.println("+ 1 point to lost games");
 			}
 			board.printArray();
 			isGameOver = true;
 		}
 		if(blackPlayer.update().length == 0) {
 			System.out.println("Red is the winner!");
 			if(isSinglePlayer()) {
 				System.out.println("+ 1 point to won games");
 			}
 			board.printArray();
 			isGameOver = true;
 		}
 		return isGameOver;
 	}
 	
 	private boolean isSinglePlayer() {
 		boolean isSinglePlayer = false;
 		if(redPlayer instanceof HumanPlayer && blackPlayer instanceof AIPlayer) {
			System.out.println("+ 1 point to lost games");
 		}
 		return isSinglePlayer;
 	}
 	
 }
