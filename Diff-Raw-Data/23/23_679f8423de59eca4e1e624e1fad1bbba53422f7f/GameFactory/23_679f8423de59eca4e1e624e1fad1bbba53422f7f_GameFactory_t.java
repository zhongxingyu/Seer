 package quoridor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 import quoridor.Command.CommandType;
 
 import util.Two;
 
 /**
  * Game factory is in charge of input, creating games, loading games. 
  * 
  * <h2>Goals</h2>
  * <ul>
  * <li>Get Input.</li>
  * <li>Send the Input to get parsed.</li>
  * <li>Creates games and is able to load them from a file or initialize them from a list of moves.</li>
  * </ul> 
  * 
  * <h2>Implementation</h2>
  * <ul>
  * <li>Main Class of the program. Display prompts and ask for input.</li>
  * <li>Uses the Command class to parse a String into an actual Command.</li>
  * <li>Creates new and loaded Games, and initializes them with a Command.</li>
  * </ul>
  * 
  * 
  * @author Sacha Braud <sacha.beraud@gmail.com>
  *
  */
 
 public class GameFactory {
 	
 	
 //	final static Player playerOne = new Human("Player 1");
 //	final static Player playerTwo = new Human("Player 2");
 
 	
 	
 	/**
 	 * Main Function of the program. It's where it all start.
 	 * @param args Arguments passed to the main function of the program to trigger specific use of the program. Not used here.
 	 */
 	public static void main(String[] args){
 		
 		System.out.println("Welcome to Quoridor AssQuad666 !");
 		while(true){
 			run();
 		}
 	}
 	
 	
 	/**
 	 * Simply scan the stdin to get a line the user typed.
 	 * @return The String the user input in the command line.
 	 */
 	public static void run(){
 		Scanner input = new Scanner (System.in);
 		Two<Player> players = null;
 		System.out.println("Input a command:");
 		String line = input.nextLine ().toLowerCase ();
 		Command command = new Command(line);
 		
 		while(command.type().equals(CommandType.INVALID) 
 				|| command.type().equals(CommandType.MOVE) 
 				|| command.type().equals(CommandType.SAVE_GAME)
 				|| command.type().equals(CommandType.UNDO)
 				|| command.type().equals(CommandType.REDO)) {
 			if (command.type().equals(CommandType.MOVE)){
 				System.out.println("You need to make a new game before making a move, try again:");
 			} else if(command.type().equals(CommandType.SAVE_GAME)){
 				System.out.println("You need to make a new game before saving, try again:");
 			} else if(command.type().equals(CommandType.UNDO)){
 				System.out.println("You need to make a new game before using undo, try again:");
 			} else if(command.type().equals(CommandType.REDO)){
 				System.out.println("You need to make a new game before using redo, try again:");
 			} else{
 				System.out.println("Input a command:");
 			}
 			line = input.nextLine ().toLowerCase ();	
 			command = new Command(line);
 		}
 		
		//while(players == null)
 			players = getPlayers();
 		
 		if(command.type().equals(CommandType.NEW_GAME)){
 			newGame(players);
 			
 		} else if(command.type().equals(CommandType.LOAD_GAME)){
 			loadGame(command.fileName(), players);
 			
 		} else if(command.type().equals(CommandType.NEW_WITH_MOVES)){
 			newGameWithMoves(command.moves(), players);	
 		} 
 		
 
 	}
 	
 	
 	public static Two<Player> getPlayers(){
 		Scanner input = new Scanner (System.in);
 		Player playerOne;
 		Player playerTwo;
 		
 		System.out.println("How many AI players in this game? Enter 0, 1, or 2.");
 		String line = input.nextLine ().toLowerCase ();
		if(line.isEmpty())
			return getPlayers();
		
		
 		if(line.charAt(0) == '0'){
 			System.out.println("Enter your name, player one:");
 			line = input.nextLine ().toLowerCase ();
 			playerOne = new Human(line);
 			System.out.println("Enter your name, player two:");
 			line = input.nextLine ().toLowerCase ();
 			playerTwo = new Human(line);
 		} else if (line.charAt(0) == '1'){
 			System.out.println("Enter your name:");
 			line = input.nextLine ().toLowerCase ();
 			playerOne = new Human(line);
 			playerTwo = new AIPlayer("Computer");
 		} else if (line.charAt(0) == '2'){
 			playerOne = new AIPlayer("Computer 1");
 			playerTwo = new AIPlayer("Computer 2");
 		} else {
 			System.out.println(line+" is not a valid number of AI players.");
			return getPlayers();
 		}
 		return Two.two(playerOne, playerTwo);
 	}
 	
 	
 	/**
 	 * Creates a new Game. Initializes it.
 	 */
 	public static void newGame(Two<Player> players) {
 		System.out.println("Making a new game...");
 		Game game = new Game(players);
 		game.initGame(null);
 		game.play();
 	}
 	
 	/**
 	 * Creates a Game. Initializes it to the state given in the file.
 	 * @param fileName The name of the file from which the game will be loaded.
 	 */
 	public static void loadGame(String fileName, Two<Player> players){
 		System.out.println("Loading a game...");
 		String line = readFromFile(fileName);
 		if (line == null){
 			return;
 		}
 		Command c = new Command(line);
 		if(c.type().equals(CommandType.MOVES) || c.type().equals(CommandType.MOVE)){
 			Validator v = new Validator();
 			if(v.check(c.moves())){
 				Game game = new Game(players);
 				game.initGame(c.moves);
 				game.play();
 			} else {
 				System.out.println("Invalid sequence of moves in the file.");
 			}
 		}
 		
 	}
 	
 	/**
 	 * Creates a Game. Initializes it to the state given by a list of moves input in stdin.
 	 * @param moves The moves input to which the game should be initialized.
 	 */
 	public static void newGameWithMoves(LinkedList<Move> moves, Two<Player> players){
 		System.out.println("Making a new game with initialisation...");
 		Validator v = new Validator();
 		if (v.check(moves)){
 			Game game = new Game(players);
 			game.initGame(moves);
 			game.play();
 		} else {
 			System.out.println("This not a valid sequence of moves.");
 		}
 	}
 	
 	
 	
 
 	/**
 	 * Reads data from a given file.
 	 * @param fileName the name of the file
 	 * @return the first line from the file.
 	 */
 	public static String readFromFile(String fileName) {
 	    String DataLine = "";
 	    try {
 	      File inFile = new File(fileName);
 	      BufferedReader br = new BufferedReader(new InputStreamReader(
 	          new FileInputStream(inFile)));
 
 	      DataLine = br.readLine();
 	      br.close();
 	    } catch (FileNotFoundException ex) {
 	    	System.out.println("This file cannot be found.");
 	      return (null);
 	    } catch (IOException ex) {
 	    	System.out.println("IO exception.");
 	      return (null);
 	    }
 	    return (DataLine);
 
 	  }
 }
