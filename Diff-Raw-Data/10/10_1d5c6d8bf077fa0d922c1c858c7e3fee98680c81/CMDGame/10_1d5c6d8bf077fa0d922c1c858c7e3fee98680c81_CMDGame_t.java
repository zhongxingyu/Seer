 package CommandLine;
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 import CluedoGame.CluedoGame;
 
 
 public class CMDGame {
 
 	public static void main(String[] args) {
 		System.out.println("Welcome to Cluedo!\n");
 		
 		//construct the game object
 		CluedoGame game = new CluedoGame(getNumberPlayers());
 
 		System.out.println("Starting a new game of Cluedo!");
 		
 		//play a real game now
 		startGame(game);
 	}
 
 
 	/**
 	 * Plays a command line based game of cluedo given the current game object.
 	 * 
 	 * @param game the game we wish to play
 	 */
 	public static void startGame(CluedoGame game) {
 		Scanner scan = new Scanner(System.in);
 		String command;
 		// lots of game logic here
 		
		//while (game.hasNotEnded()) 
 			
 			//get player
 			//print instructions
 			
			//while (game.isTurn(player)) 
 				
 				
 				
 				
			

 		
 	}
 	
 	
 	/**
 	 * Prompts the users and returns a number between 3 - 6 inclusive.
 	 * 
 	 * @return number of players
 	 */
 	private static int getNumberPlayers() {
 		Scanner scan = new Scanner(System.in);
 		int number = 0;
 
 		System.out.println("How many people want to play?");
 		
 		do {	
 			System.out.println("Number must be between 3 and 6 inclusive.");
 			
 			try {
 				number = scan.nextInt();
 			} catch (InputMismatchException e) {
 				//default to a value which will cause loop to iterate again
 				number = 0;
 				//get rid of scanner data so it doesn't block
 				scan.skip(".*");
 			}
 		} while (number < 3 || number > 6);
 		
 		scan.close();
 		return number;
 	}
 }
