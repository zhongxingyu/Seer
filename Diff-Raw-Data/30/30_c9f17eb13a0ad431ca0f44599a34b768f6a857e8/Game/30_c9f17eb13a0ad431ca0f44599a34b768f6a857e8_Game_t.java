 package main;
 
 public class Game {
 	private static Game instance;
 	private Board mainBoard;
 	private Player redPlayer, blackPlayer, currentPlayer;
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
 		mainBoard = new Board();
 		mainBoard.initializeBoard();
 		mainBoard.printArray();
 		switch(mode) {
 			case 1:
 				blackPlayer = new AIPlayer(Colour.BLACK,mainBoard);
 				redPlayer = new HumanPlayer(Colour.RED,mainBoard); 
 				break;
 			case 2: 
 				blackPlayer = new HumanPlayer(Colour.BLACK,mainBoard);
 				redPlayer = new HumanPlayer(Colour.RED,mainBoard); 
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
 			mainBoard.printArray();
 			mainBoard.resetTurn();
 			
 			switch(turn) {
 				case 0: currentPlayer = blackPlayer;
 						turn +=1;
 						break;
 				case 1: currentPlayer = redPlayer;
 						turn -=1;
 						break;
 			}
 			
 			if(gameOver()) break;
 			
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
 	
 	public Player getRed() {
 		return redPlayer;
 	}
 	
 	public Player getBlack() {
 		return blackPlayer;
 	}

	public Board getBoard() {
		return this.mainBoard;
	}
 	/**
 	 * @return True if the current Player can no longer move.
 	 */
 	public boolean gameOver(){
 		currentPlayer.updatePieces();
 		
 		if (redPlayer.getPieces().length == 0 || blackPlayer.getPieces().length == 0){
 			return true;
 		}
 		
 		for(int i = 0; i < currentPlayer.getPieces().length; i++){
 			if (currentPlayer.getPieces()[i].getAllMoves(currentPlayer, mainBoard, false).length > 0) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 }
