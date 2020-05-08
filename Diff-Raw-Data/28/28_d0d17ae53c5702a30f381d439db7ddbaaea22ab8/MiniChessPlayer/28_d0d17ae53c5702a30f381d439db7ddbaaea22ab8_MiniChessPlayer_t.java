 /*import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Vector;*/
 import java.io.IOException;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Vector;
 
 public class MiniChessPlayer {
 
 	public static String server = "imcs.svcs.cs.pdx.edu";
 	public static String port = "3589";
 	public static String user = "pfaffle";
 	public static String pass = "foo";
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		//playSmartVsHuman();
 		//playSmartVsSmart();
 		//playRandomVsHuman();
 		//playRandomVsRandom();
 		playSmartVsImcs();
 	}
 	
 	public static void playSmartVsImcs() {
 		System.out.println("Connecting to server.");
 		State gamestate = new State();
 		char my_color;
 		
 		try {
 			Client connection = new Client(server,port,user,pass);
 			Vector<Game> available_games = connection.list();
 			if (available_games.size() == 0) {
 				// No game offers currently available to accept. Create one!
 				my_color = connection.offer('?');
 			} else {
 				// Find and accept one of the existing game offers.
 				Random generator = new Random();
 				int randomIndex = generator.nextInt(available_games.size());
 				Game selected_game = available_games.elementAt(randomIndex);
 				//Game selected_game = new Game(7315,'W',"custom_opponent");
 				String game_id = String.valueOf(selected_game.id);
 				String my_opponent = selected_game.opponent;
 				if (selected_game.color == 'B') {
 					my_color = connection.accept(game_id,'W');
 				} else {
 					my_color = connection.accept(game_id,'B');
 				}
 				System.out.println("My opponent: " + my_opponent + ".");
 			}
 			
 			if (my_color == 'W') {
 				System.out.println("I am White!");
 			} else {
 				System.out.println("I am Black!");
 			}
 			
 			gamestate.writeBoard();
 			while (!gamestate.gameOver()) {
 				if (my_color == 'W') {
 					if (gamestate.whiteOnMove()) {
 						// make a move
 						String my_move = gamestate.getImcsMove();
 						System.out.println("My move: " + my_move);
 						gamestate = gamestate.makeImcsMove(my_move);
 						connection.sendMove(my_move);
 						//gamestate.writeBoard();
 					} else {
 						// wait for opponent's move.
 						String opp_move = connection.getMove();
 						System.out.println("Black moves: " + opp_move);
 						gamestate = gamestate.makeImcsMove(opp_move);
 						//gamestate.writeBoard();
 					}
 				} else {
 					if (gamestate.blackOnMove()) {
 						// make a move
 						String my_move = gamestate.getImcsMove();
 						System.out.println("My move: " + my_move);
 						gamestate = gamestate.makeImcsMove(my_move);
 						connection.sendMove(my_move);
 						//gamestate.writeBoard();
 					} else {
 						// wait for opponent's move.
 						String opp_move = connection.getMove();
 						System.out.println("White moves: " + opp_move);
 						gamestate = gamestate.makeImcsMove(opp_move);
 						//gamestate.writeBoard();
 					}	
 				}
 				gamestate.writeBoard();
 			}
 			connection.close();
 			System.out.println("Game over!");
 			if (gamestate.whiteWins()) {
 				if (my_color == 'W')
 					System.out.println("I win!");
 				else
 					System.out.println("I lose!");
 			} else if (gamestate.blackWins()) {
 				if (my_color == 'B')
 					System.out.println("I win!");
 				else
 					System.out.println("I lose!");
 			} else {
 				System.out.println("Game is a draw.");
 			}
 		} catch (Exception e) {
 			System.out.println("An error occurred!");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void playRandomVsHuman() {
 		State gamestate = new State();
 		Scanner scan = new Scanner(System.in);
 		gamestate.writeBoard();
 		// Play the game.
 		while (!gamestate.gameOver()) {
 			if (gamestate.whiteOnMove()) {
 				System.out.print("Please enter a move in the form A1-B2: ");
 				String in = scan.nextLine();
 				try {
 					gamestate = gamestate.makeHumanMove(in);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					gamestate = gamestate.makeRandomGoodMove();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			gamestate.writeBoard();
 		}
 		System.out.println("Game over!");
 		if (gamestate.whiteWins()) {
 			System.out.println("White wins!");
 		} else if (gamestate.blackWins()) {
 			System.out.println("Black wins!");
 		} else {
 			System.out.println("Game is a draw.");
 		}
 		scan.close();
 	}
 	
 	public static void playRandomVsRandom() {
 		State gamestate = new State();
 		gamestate.writeBoard();
 		// Play the game.
 		while (!gamestate.gameOver()) {
 			if (gamestate.whiteOnMove()) {
 				System.out.println("White moves...");
 				try {
 					gamestate = gamestate.makeRandomGoodMove();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					gamestate = gamestate.makeRandomGoodMove();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			gamestate.writeBoard();
 		}
 		System.out.println("Game over!");
 		if (gamestate.whiteWins()) {
 			System.out.println("White wins!");
 		} else if (gamestate.blackWins()) {
 			System.out.println("Black wins!");
 		} else {
 			System.out.println("Game is a draw.");
 		}
 	}
 	
 	public static void playSmartVsHuman() {
 		State gamestate = new State();
 		Scanner scan = new Scanner(System.in);
 		gamestate.writeBoard();
 		// Play the game.
 		while (!gamestate.gameOver()) {
 			if (gamestate.whiteOnMove()) {
 				System.out.print("Please enter a move in the form A1-B2: ");
 				String in = scan.nextLine();
 				try {
 					gamestate = gamestate.makeImcsMove(in);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					String move = gamestate.getImcsMove(); 
 					gamestate = gamestate.makeImcsMove(move);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			gamestate.writeBoard();
 		}
 		System.out.println("Game over!");
 		if (gamestate.whiteWins()) {
 			System.out.println("White wins!");
 		} else if (gamestate.blackWins()) {
 			System.out.println("Black wins!");
 		} else {
 			System.out.println("Game is a draw.");
 		}
 		scan.close();
 	}
 	
 	public static void playSmartVsSmart() {
 		State gamestate = new State();
 		gamestate.writeBoard();
 		// Play the game.
 		while (!gamestate.gameOver()) {
 			if (gamestate.whiteOnMove()) {
 				System.out.println("White moves...");
 				try {
 					gamestate = gamestate.makeSmartMove();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					gamestate = gamestate.makeSmartMove();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			gamestate.writeBoard();
 		}
 		System.out.println("Game over!");
 		if (gamestate.whiteWins()) {
 			System.out.println("White wins!");
 		} else if (gamestate.blackWins()) {
 			System.out.println("Black wins!");
 		} else {
 			System.out.println("Game is a draw.");
 		}
 	}
 }
