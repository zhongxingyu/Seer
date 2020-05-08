 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Driver class, loops over input running the game.
  *
  * @author Sam Brown
  * @author Eebs Kobeissi
  * @author Caleb Cousins
  */
 public class Mastermind {
 
 	/**
 	 * Whether the user wants to quit or not.
 	 */
 	private static boolean quit = false;
 
 	/**
 	 * Game help text.
 	 */
 	private static String helptext = "Type 'rules', 'hint', or 'quit' for additional options. Enter all row guesses in the format 'R G B'.";
 
 	/**
 	 * Static Scanner class used for user input.
 	 */
 	private static Scanner scanner = new Scanner(System.in);
 
 	/**
 	 * Main function. Creates the game and loops until the user quits.
 	 *
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		StatGatherer statGatherer = new StatGatherer();
 
 		print("Hello, and welcome to Mastermind!");
 		
 		Game game = startNewGame();
 
 		while(!quit && !game.isSolved()){
 			String input = scanner.nextLine();
 			String response = parseInput(input, game);
 			print(response);
 
 			if (game.isSolved()){
				boolean validResp = false;
				//do you want to start a new game.
 				print("Your game has ended.  Would you like to play again? (y/n)");
 				String replay = scanner.nextLine();
				while(!validResp){
					if (replay.toLowerCase().equals("y")){
						validResp = true;
						statGatherer.logGame(game);
						game = startNewGame();
					}
					else if(replay.toLowerCase().equals("n")){
						validResp = true;
					}
					else{
						print("'"+replay+"' is not a valid response.  Would you like to play again? (y/n)");
						replay = scanner.nextLine();
					}
 				}
 			}
 		}
 		print("Thanks for playing Mastermind!\n\nCredits:\nSam 'Fancy Pants' Brown\nEebs 'Awesome Sauce' Kobeissi\nCaleb Cousins");
 		//log the final game
 		statGatherer.logGame(game);
 	}
 
 	/**
 	 * Parses user input, returning a string response.
 	 *
 	 * @param input
 	 * @param game
 	 * @return String
 	 */
 	private static String parseInput(String input, Game game){
 		input = input.trim();
 		Pattern pattern = Pattern.compile("^[a-zA-Z]( +[a-zA-Z])*$");
 		Matcher matcher = pattern.matcher(input);
 		String response = "";
 		if(matcher.matches()){
 			input = input.toUpperCase();
 			String[] guesses = input.split(" +");
 			for(int i = 0; i < guesses.length; i++){
 				if(!Arrays.asList(game.getMode().getAvailableColors()).contains(guesses[i])){
 					response = "Invalid color entered. Valid color choices are " + game.getMode().getAvailableColorsString();
 					return response;
 				}
 			}
 			if(!game.addRow(new Row(new ArrayList<String>(Arrays.asList(guesses))), game.getKey())){
 				response = "Incorrect number of pegs entered. Expecting " + game.getMode().getSlots() + " pegs.";
 				if(!game.isSolved()){
 					response += "\n\nEnter your next guess:";
 				}
 			}else{
 				response = game.toString();
 				response += "\n\nEnter your next guess. Valid color choices are " + game.getMode().getAvailableColorsString();
 			}
 		}else if(input.equalsIgnoreCase("quit")){
 			quit = true;
 		}else if(input.equalsIgnoreCase("rules")){
 			response = "\nThe rules of Mastermind are simple: guess what combination of colors the computer has in as few turns as possible." +
 					"\nIn every game, the computer will have a set combination of colors (chosen randomly each game), and it's your job to guess " +
 					"which colors those are and the order in which they appear. \nOn easy mode, the computer chooses 4 colors from a set of 6 colors: " +
 					"Red(R), Orange(O), Blue(B), Green(G), Yellow(Y), and Purple(P).\nRemember that the computer can choose more than one of the same color! " +
 					"Also, you should always use the single-character form of the colors when guessing.\nFor example, if you think that the combination " +
 					"is 'Red, Blue, Red, Yellow', you should type 'R B R Y' - leaving a space between each letter.\nOn hard mode, a new color, Cyan(C), " +
 					"is added, and the computer picks 5 colors instead of 4.\n\nAfter each guess, the computer will give you a certain number " +
 					"of white and black pegs.\nThe number of white pegs indicates how many colors you've guessed correctly that AREN'T in the right " +
 					"spot.\nThe number of black pegs indicates how many colors you've guessed correctly that ARE in the right spot.\nFor example, " +
 					"say the correct answer is 'R B Y Y' and you just guessed 'R O O B', you will be given the message (1W, 1B).\nThis means you " +
 					"have one white peg (you guessed 'Blue' but it wasn't in the right spot) and one black peg (you guessed 'Red' in the correct spot)." +
 					"\n\nThat's about all there is to it!\nGood luck and try to guess the right answer in as few turns as possible!\n\nOh, also, " +
 					"your stats (the number of turns taken, difficulty chosen, number of hints used) is recorded at the end of each game along with " +
 					"the time and date.\nSo shoot for the high score, and don't let anyone think they can beat you!\n\n";
 			response += helptext;
 		}else if(input.equalsIgnoreCase("hint")){
 			response = game.getHint();
 			response += "\n\nEnter your next guess. Valid color choices are " + game.getMode().getAvailableColorsString();
 		}else{
 			response = "Invalid input. ";
 			response += helptext;
 		}
 		return response;
 	}
 
 	/**
 	 * Creates a new game object.
 	 *
 	 * @return Game
 	 */
 	private static Game startNewGame(){
 		//enforce a valid game mode selection
 		print("Enter a game mode, either 'easy' or 'hard'.");
 		String mode = scanner.nextLine().toLowerCase();
 		while ( !(mode.equalsIgnoreCase("easy") || mode.equalsIgnoreCase("hard"))){
 			print("'" +mode+ "'" +" is not a valid game mode. Please choose either 'easy' or 'hard'.");
 			mode = scanner.nextLine();
 		}
 
 		GameMode gameMode = GameMode.factory(mode);
 		Game game = new Game(gameMode);
 
 		print("Game started on " + mode + " mode. Valid color choices are " + gameMode.getAvailableColorsString());
 		print(helptext);
 
 		return game;
 	}
 
 	/**
 	 * Proxy to System.out.println()
 	 *
 	 * @param s
 	 */
 	private static void print(String s){
 		System.out.println(s);
 	}
 }
