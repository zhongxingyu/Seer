 package com.veganjay.tictactoe;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.veganjay.tictactoe.TicTacToeBoard.Piece;
 
 /**
  * Tic Tac Toe Game
  * 
  * Computer AI using MiniMax with beta-alpha pruning and depth specification
  * @author jason
  * 
  * TODO
  * - Clean up code - main loop
  * - Set better objective value in AI
  * - Graphical version
  */
 public class TicTacToe {
 	public static final Piece START_PLAYER = Piece.X;
 	public static final Piece NEXT_PLAYER  = Piece.O;
 	
 	// Member variables
 	private BufferedReader input;
 	private TicTacToeBoard board;
 	
 	private Piece currentPlayer;
 
 	private Piece computer;
 	private Piece human;
 	
 	public TicTacToe() {
 		this.input= new BufferedReader(new InputStreamReader(System.in));
 
 		this.board = new TicTacToeBoard();
 	}
 	
 	private int getChoice() {
 		String choice;
 		int    col;
 				
 		// Read from input
 		choice = this.getInput();
 		System.out.println();
 		
 		try {
 			col = Integer.parseInt(choice);
 		} catch (NumberFormatException e) {
 			System.out.println("Enter (1-9): ");
 			return getChoice();
 		}
 		return col;
 	}
 	
 	private void getStartPlayer() {
 		String choice;
 		System.out.print("Do you want to go first? [y] ");
 		
 		choice = this.getInput();
 		
 		if (choice.equalsIgnoreCase("n") || choice.equalsIgnoreCase("no")) {
 			this.computer = START_PLAYER;
 			this.human    = NEXT_PLAYER;
 		} else {
 			this.human = START_PLAYER;
 			this.computer = NEXT_PLAYER;
 		}		
 	}
 	
 	private boolean playAgain() {
 		boolean playAgain = true;
 		System.out.print("Do you want to play again? [y] ");
 		String choice = this.getInput();
 		if (choice.equalsIgnoreCase("n") || choice.equalsIgnoreCase("no")) {
 			playAgain = false;
 		}
 		return playAgain;
 	}
 	
 	public String getInput() {
 		String line = "";
 		try {
 			return this.input.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return line;
 		
 	}
 	
 	public void play() {
 		// Initialize with start player
 		currentPlayer = START_PLAYER;
 		
 		// Determine the start player
 		this.getStartPlayer();
 
 		// Create computer AI with
 		TicTacToeAI ai = new TicTacToeAI(computer);
 				
 		// Display the board;
 		board.printBoard();
 		
 		// Enter main loop
 		mainLoop(ai);
 
 		if (playAgain()) {
 			// Reset the board
 			board.initialize();
 			play();
 		}
 		
 	} // End main loop
 
 	private void mainLoop(TicTacToeAI ai) {
 		while (true) {		
 			int spaceNum;
 			if (currentPlayer == human) {
 				// Get the choice
				System.out.print("Enter Space Number (1-9): ");
				spaceNum = getChoice();							
 			} else {
 				spaceNum = ai.getMove(board); 
 				System.out.println("Computer moves to " + spaceNum);
 			}
 
 			// Add the piece to the board
 			board.addPiece(spaceNum, currentPlayer);
 			
 			// Display the board;
 			board.printBoard();
 
 			// Check for winner
 			if (board.isWinner(currentPlayer)) {
 				System.out.println(currentPlayer + " is the winner!");
 				break;
 			}
 			
 			// Check for tie
 			if (board.isFull()) {
 				System.out.println("Tie game!");
 				break;
 			}
 						
 			// Switch players
 			this.switchPlayers();
 			
 		} // End while
 	}
 	
 	private void switchPlayers() {
 		if (currentPlayer == Piece.X) {
 			currentPlayer = Piece.O;
 		} else {
 			currentPlayer = Piece.X;
 		}
 	}
 	public static void main (String [] args) {
 		TicTacToe game = new TicTacToe();
 		game.play();
 	}
 }
