 package project;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import project.Game.WallDirections;
 
 public class GameRunner {
 	
 	public static void runGame () {
 		GameState gs = new GameState();
 		BufferedReader is = new BufferedReader (new InputStreamReader(System.in));
 		while (!gs.isGameOver()) {
 			boolean madeMove = false;
 			boolean wasInvalid = false;
 			String input = "";
 			int x=-1;
 			int y=-1;
 			while (!madeMove) {
 				gs.print(); //print board
 				if (wasInvalid) {
 					System.out.println("\"" + input + "\" was invalid");
 				}
 				System.out.println("Enter your move ('h' for help, 'q' to quit):");
 				try {
 					input = is.readLine();
 				} catch (IOException e) {
 					System.out.println("Could not read input");
 					System.exit(1);
 				}
 				if (input.equals("q")) {
 					System.exit(0);
 				} else if (input.equals("h")) {
 					printHelp();
 				}
 				// give 'move' as coordinates to spot you want to move to
 				String[] tokens = input.split(" ");
 				if (tokens.length == 3) {
 					try {
 						x = Integer.parseInt(tokens[0]);
 						y = Integer.parseInt(tokens[1]);
 					} catch (Exception e) {
 						wasInvalid = true;
 					}
 					System.out.println(tokens[2]);
 					if (tokens[2].charAt(0)=='m') { // move
 						if (!wasInvalid) {
 							madeMove = gs.move(x, y);
 							wasInvalid = !madeMove;
 						}		
 					} else if (tokens[2].charAt(0)=='w') { // wall
 						if (tokens[2].charAt(1)=='h') {
 							//horizontal wall
 							madeMove = gs.placeWall(x, y, WallDirections.h);
 							wasInvalid = !madeMove;
 						} else if (tokens[2].charAt(1)=='v') {
 							//vertical wall
							madeMove = gs.placeWall(x, y, WallDirections.h);
 							wasInvalid = !madeMove;
 						}
 					} else {
 						wasInvalid = true;
 					}
 				} else {
 					wasInvalid = true;
 				}
 			}
 			//Game ends
 		}
 	}
 	
 	public static void printHelp () {
 		System.out.println("To make a move: x y m");
 		System.out.println("Give coordinates of the space you wish to move to, followed by an m.");
 		System.out.println("To place a wall: x y w[h|v]");
 		System.out.print("Give the coordinates of the top left space involved in your wall, ");
 		System.out.println("followed by wh for a horizonal wall or wv for a vertical wall.");
 	}
 
 }
