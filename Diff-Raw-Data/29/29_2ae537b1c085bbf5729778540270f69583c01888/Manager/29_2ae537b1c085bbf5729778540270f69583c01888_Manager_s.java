 package quoridor;
 
 import java.util.Scanner;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 
 /**
  * 
  * This is the class that controls the creation of new games, as well as saving
  * and loading game states.
  * 
  * @author steve
  * 
  */
 public class Manager {
 
 	// Some constants to make the code easier to read
 
 	static final String WELCOME_MSG = "Welcome to Quoridor!\n";
 	static final String PROMPT = "> ";
 
 	static final short INVALID = -1;
 	static final short EXIT = 0;
 	static final short NEW_GAME = 1;
 	static final short HELP = 2;
 	static final short EMPTY = 3;
 	static final short LOAD_GAME = 4;
 
 	// this will store the current game being played
 	static Game currentGame = null;
 
 	// the input parser that parses user's commands
 	static Parser inputParser;
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		welcome();
 		
 		/*
 		 * This is the information required by the parser to create a syntax
 		 * table and know how many arguments each command has.
 		 */
 		String[] commands = { "exit", "newgame", "loadgame" ,"help", "" };
 		short[] tokens = { EXIT, NEW_GAME, LOAD_GAME, HELP, EMPTY };
 		short[] argCount = { 0, 2, 0, 0, 0 };
 
 		// initialise the input parser
 		inputParser = new ParserImpl(commands, tokens, argCount, INVALID);
 		// start up the input loop
 		inputLoop(inputParser);
 
 	}
 
 	/**
 	 * Prints the welcome message
 	 */
 	public static void welcome() {
 		System.out.println(WELCOME_MSG);
 	}
 
 	/**
 	 * Loops parsing input until the user invokes the exit command.
 	 * 
 	 * @param inputParser
 	 *            the input parser that parses users' input
 	 */
 	public static void inputLoop(Parser inputParser) {
 		boolean exit = false; // flag that tells the loop to terminate
 		short token; // holds the token of the command just read
 		System.out.println("newgame or loadgame?");
 		while (!exit) {
 			System.out.print(PROMPT); // print the prompt
 			// read the next token
 			token = inputParser.ensureCommand();
 			if (token == EXIT) {
 				exit = true;
 			} else if (token == NEW_GAME) {
 				Pair<Player> players;
 				players = initNewGame();
 				// initialise a game
 				currentGame = new GameImpl(players);
 
 				// tell the user the game is starting
 				System.out.println("\nStarting game: "
 						.concat(players._1().getName()).concat(" vs ")
 						.concat(players._2().getName()));
 
 				// play the game
 				currentGame.play();
 
 			} else if (token == LOAD_GAME) {
 				try {
 					loadFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else if (token == HELP) {
 				// this prints a list of valid commands
 				System.out.print(inputParser);
 			} else if (token == INVALID) {
 				System.out.println("Invalid command!");
 			}
 
 		}
 
 	}
 
 	private static Pair<Player> initNewGame(){
 		Pair<Player> players = null;
 		Player _1 = null;
 		Player _2 = null;
 		String tmp;
 		boolean exit  = false;		
 		
 		Scanner in = new Scanner(System.in);
 		String s = "";
 		while (!exit) {
 			System.out.println("What type of game would you like to play? \n 1. Human vs Human \n" +
 									" 2. Human vs Computer\n 3. Computer vs Computer \n (type number choice)");
 			if ((s = in.nextLine()) != null) {
 				if (s.contains("1") || s.contains("2") || s.contains("3")) {
 					exit = true;
 				}
 					
 			}
 		}
 		
 		if (s.contains("1")) {
 			/*
 			 * If the user gave arguments, we can create the players without
 			 * prompting for more input
 			 */
 			if (inputParser.hasRequiredArgs()) {
 				_1 = new PlayerImpl(inputParser.nextArg(), Player.TOP);
 				_2 = new PlayerImpl(inputParser.nextArg(), Player.BOTTOM);
 			} else {
 				System.out.println("How did i break this?");
 				/*
 				 * Give the user an error if they tried to give arguments
 				 * but gave the wrong amount.
 				 */
 				if (inputParser.hasErroneousArgs()) {
 					System.out
 							.println("Warning: Incorrect number of arguments");
 				}
 				// prompt for the user to enter player names
 				players = enterPlayers();
 			}
 			System.out.println("what about here?");
 		} else if (s.contains("2")) {
			if (inputParser.hasRequiredArgs()) {
				_1 = new PlayerImpl(inputParser.nextArg(), Player.TOP);
			} else {
				System.out.println("Player 1 eneter your name: ");
				if ((s = in.nextLine()) != null) {
					_1 = new PlayerImpl(s, Player.TOP);
				}
 			}
 			System.out.println("Which AI would you like to play against? \n 1. MinMax \n 2. Random");
 			tmp = chooseAI();
 			_2 = new PlayerImpl(Player.BOTTOM, tmp);
 		} else {
 			System.out.println("Which AI would you like to play against? \n 1. MinMax \n 2. Random");
 			tmp = chooseAI();
 			_1 = new PlayerImpl(Player.TOP, tmp);
 			System.out.println("Which AI would you like to play against? \n 1. MinMax \n 2. Random");
 			tmp = chooseAI();
			_2 = new PlayerImpl(Player.BOTTOM, tmp);		
		}

		if (s.contains("2") || s.contains("3")) {
 			players = new PairImpl<Player>(_1, _2);
 			_1.setOpponent(_2);
 			_2.setOpponent(_1);	
 		}	
 		
 		return players;
 	}
 	
 	/**
 	 * User input to chose which AI to play against
 	 * @return string name for the AI chosen
 	 */
 	private static String chooseAI() {
 		String retVal = "";
 		Scanner in = new Scanner(System.in);
 		String s;
		if ((s = in.nextLine()) != null) {
 			if (s.contains("1") || s.contains("minmax")) {
 				retVal = "minmax";
 			} else if (s.contains("2") || s.contains("random")){
 				retVal = "random";
 			}
 		} 
 		System.out.println("string is: " +retVal);
 		
 		return retVal;
 	}
 	
 	/**
 	 * Prompts the user to enter player names, then creates and returns a pair
 	 * of players with those names.
 	 * 
 	 * @return A pair of players with names specified by the user
 	 */
 	public static Pair<Player> enterPlayers() {
 
 		Player _1;
 		Player _2;
 
 		Scanner s = new Scanner(System.in);
 
 		// read player 1's name
 		System.out.print("Player 1 enter your name: ");
 		_1 = new PlayerImpl(s.nextLine(), Player.TOP);
 		System.out.println("_1 players name is: " +_1.getName());
 
 		// read player 2's name
 		System.out.print("Player 2 enter your name: ");
 		_2 = new PlayerImpl(s.nextLine(), Player.BOTTOM);
 		System.out.println("player 2 name? " +_2.getName());
 		//_2 = new PlayerImpl(Player.BOTTOM, "minimax");
 
 		_1.setOpponent(_2);
 		_2.setOpponent(_1);
 		
 		// create and return a pair containing those players
 		return new PairImpl<Player>(_1, _2);
 	}
 
 	/**
 	 * Loads any given file
 	 * @throws IOException
 	 */
 	public static void loadFile( ) throws IOException {
 		String fileMove = "";
 		String fileName = "";
 		File file = new File(fileName);
 		String path = ".";
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 		boolean exit = false;
 		String s;
 		Scanner in = new Scanner(System.in);
 		
 		//Print out a list of all the current saved game states
 		System.out.println("Saved game states:");
 		for (int i = 4; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].isFile()) {
 				System.out.println(listOfFiles[i].getName());
 			}
 		}
 		
 		//While loop for making sure they choose to load a game that already exists
 		while (!file.exists() && exit == false) {
 			System.out.println("Which game state would you like to load? Type bye to not load any.");
 			if ((s = in.nextLine()) != null) {
 				if (s.contains("bye")) {
 					exit = true;
 				} else {
 					fileName = s;
 					if (!fileName.contains(".quor")) {
 						fileName+=".quor";
 					}
 					file = new File(fileName);
 				}
 			}
 		}
 		
 		//Opening and obtaining data from the file they want to load
 		if (exit != true) {
 			file.getName(); 
 			FileInputStream fstream = new FileInputStream(fileName);
 			DataInputStream datain = new DataInputStream(fstream); 
 			//create a buffer reader to go through the file 
 			BufferedReader br = new BufferedReader(new InputStreamReader(datain)); 
 			String moves; 
 			//Read the file line by line 
 			while ((moves = br.readLine()) != null) {
 				fileMove+=moves; 
 			}
 			datain.close();
 		}
 		
 		//splitting the string into an array of moves
 	    String splitAt = " ";
 	    String[] moves = null;
 	    moves = fileMove.split(splitAt);
 	    createGame(moves);
 	}
 	
 	/**
 	 * Creates a game from a given file.
 	 * @param moves string taken from the saved file
 	 */
 	private static void createGame(String[] moves) {
 		Pair<Player> players;
 		String ai = moves[0];
 		Scanner in = new Scanner(System.in);
 		String s;
 		Player _1 = null;
 		Player _2 = null;
 		if (ai.contains("HA")) {
 			_1 = new PlayerImpl(moves[1], Player.TOP);
 			_2 = new PlayerImpl(Player.BOTTOM, "minmax");
 		} else {
 			//if the game to be loaded was human vs human, give them the option to change it to human vs computer
 			System.out.println("Players in this game are. Player1: " +moves[1] +" and Player2: " +moves[2]);
 			System.out.println("Would you like to change one player to AI?");
 			if ((s = in.nextLine()) != null) {
 				if (s.contains("yes")){
 					//determine which user to become the AI if they said yes
 					System.out.println("Which player would you like to become the AI? player1 or player2?");
 					if ((s = in.nextLine()) != null) {
 						if (s.contains("player1")) {
 							_1 = new PlayerImpl(Player.TOP, "minmax");
 							_2 = new PlayerImpl(moves[2], Player.BOTTOM);
 						} else {
 							_1 = new PlayerImpl(moves[1], Player.TOP);
 							_2 = new PlayerImpl(Player.BOTTOM, "minmax");
 						}
 					}
 				} else {
 					_1 = new PlayerImpl(moves[1], Player.TOP);
 					_2 = new PlayerImpl(moves[2], Player.BOTTOM);
 				}
 			}
 		}
 		
 		//creates the two players
 		players = new PairImpl<Player>(_1, _2);
 		_1.setOpponent(_2);
 		_2.setOpponent(_1);
 		//System.out.println("printing here?");
 		//Setting the list of moves to go through
 		String[] retVal = new String[moves.length-3];		
 		int j = 0;
 		for (int i = 3; i < moves.length; i++) {
 			retVal[j] = moves[i];
 			j++;
 		}
 		// initialise a game
 		currentGame = new GameImpl(players);
 		
 		//Plays a current game 
 		currentGame.loadGamePlay(retVal);
 	}
 
 	/**
 	 * Saves a gameState
 	 * @throws IOException
 	 */
 	public static void saveGame() throws IOException {
 	//public static void saveGameState(String filename, String moves){
 		Scanner in = new Scanner(System.in);
 		//BufferedReader in = new BufferedReader(new InputStreamReader(
 			//										System.in));
 		String s;
 		String filename="";
 		System.out.println("What name would you like to save game as?");
 		if ((s = in.nextLine()) != null) {
 			filename+=s;
 		}		
 		filename += ".quor";
 		// create a new file to be saved
 		File newFile;
 		newFile = new File(filename);
 		boolean exit = false;
 		
 		//loop for if they choose to save a file as something that already exists
 		while (newFile.exists() && exit == false) {
 			System.out.println("File already exists with that name. Would you like to replace? Yes or no?");
 			if ((s = in.nextLine()) != null) {
 				if (s.contains("no")) {
 					System.out.println("Please type a new name to save game as.");
 					newFile = new File(in.nextLine() + ".quor");
 				} else if (s.contains("yes")) {
 					exit = true;
 					newFile.delete();
 				}
 			}
 		}
 		
 		// create a new file with the filename
 		newFile.createNewFile();
 		// write the string of moves to the file
 		Writer output = null;
 		String text = currentGame.formatFile();
 		output = new BufferedWriter(new FileWriter(newFile));
 		output.write(text);
 		output.close();
 	}
 
 
 }
