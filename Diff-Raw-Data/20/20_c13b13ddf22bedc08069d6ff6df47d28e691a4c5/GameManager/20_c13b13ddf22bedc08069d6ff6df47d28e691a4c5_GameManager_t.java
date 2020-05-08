 /**
  * 
  */
 package Server;
 
 /**
  * Where the game logic is located. The user enters their course of action 
  * and the GameManager reacts accordingly by calling the required methods.
  * 
  * @author AlexanderMacKenzie
  * @author Matthew Rozon
  *
  */
 import java.util.Scanner;
 
 public class GameManager {
 	private BoardManager board;
 	private Dice dice;
 	private int player;
 	
 	/**
 	 * Class constructor.
 	 */
 	public GameManager(){
 		board = new BoardManager();
 		dice = new Dice();
 		player = 1;
 	}
 	
 	/**
 	 * Checks what the user's input is and calls the appropriate method.
 	 * @param choice the user's input
 	 * @return the server's response
 	 */
 	public String userChoice(String choice){
		switch (choice){
			case "roll":
				return this.roll();
			case "stop":
				return this.stopTurn();
			case "crap":
				return this.crapOut();
			default:
				return this.movePiece(choice);
		}
		/*
 		if(choice.equals("roll"))
 			return this.roll();
 		else if(choice.equals("stop"))
 			return this.stopTurn();
 		else if(choice.equals("crap"))
 			return this.crapOut();
 		else
 			return this.movePiece(choice);
			*/
 	}
 	
 	/**
 	 * Rolls the dice.
 	 * @return the dice roll results
 	 */
 	private String roll(){
 		return dice.rollDice();
 	}
 	
 	/**
 	 * Moves a player's game pieces along the columns chosen.
 	 * @param choices the columns the player wishes to move up
 	 * @return the server's response
 	 */
 	private String movePiece(String choices){
 		if(dice.checkLastSums(choices)){
 			Scanner sc = new Scanner(choices);
 			sc.useDelimiter(", ");
 			int choiceOne = sc.nextInt();
 			int choiceTwo = sc.nextInt();
 			int removed;
 			if(board.checkConquered(choiceOne) && board.checkConquered(choiceTwo))
 				return "err";
 			
 			if(board.getCounter() < 3){
 				if(!board.checkConquered(choiceOne)){
 					removed = board.removePiece(choiceOne, player);
 					board.addPiece(removed, choiceOne, false, player);
 				}
 			}
 			else{
 				if(!board.checkConquered(choiceOne)){
 					removed = board.removePiece(choiceOne, player);
 					if(removed > -1)
 						board.addPiece(removed, choiceOne, false, player);
 				}
 			}
 			
 			if(board.getCounter() < 3){
 				if(!board.checkConquered(choiceTwo)){
 					removed = board.removePiece(choiceTwo, player);
 					board.addPiece(removed, choiceTwo, false, player);
 				}
 			}
 			else{
 				if(!board.checkConquered(choiceTwo)){
 					removed = board.removePiece(choiceTwo, player);
 					if(removed > -1)
 						board.addPiece(removed, choiceTwo, false, player);
 				}
 			}
 			return "ack";
 		}
 		else
 			return "err";
 	}
 	
 	/**
 	 * Ends the current player's turn and checks to see if there is a winner.
 	 * @return if a player has won the game
 	 */
 	private String stopTurn(){
 		board.tempToPerm(player);
 		if(board.winCheck(player)){
 			return "you won";
 		}
 		else{
 			if(player == 1)
 				player = 2;
 			else
 				player = 1;
 			return "ack";
 		}
 	}
 	
 	/**
 	 * Ends the current player's turn and removes their last round's progress.
 	 * @return the server's response
 	 */
 	private String crapOut(){
 		for(int col = 2; col <= 12; col++){
 			board.removePiece(col, player);
 		}
 		if(player == 1)
 			player = 2;
 		else
 			player = 1;
 		return "ack";
 	}
 }
