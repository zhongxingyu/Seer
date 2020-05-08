 /*import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Vector;*/
 import java.util.Scanner;
 
 public class MiniChessPlayer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		//playSmartVsHuman();
 		playSmartVsSmart();
 		//playRandomVsHuman();
 		//playRandomVsRandom();
 		
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
 					gamestate = gamestate.makeHumanMove(in);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					gamestate = gamestate.makeSmartMove(4);
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
					gamestate = gamestate.makeSmartMove(1);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Black moves...");
 				try {
 					gamestate = gamestate.makeSmartMove(4);
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
 	
 	/* Function:
 	 *   test_ReadBoard(State gamestate)
 	 * Description:
 	 *   Run a series of tests against the ReadBoard function, passing it
 	 *   differently constructed MiniChess boards that are invalid in different
 	 *   ways, to ensure that the function detects these errors and returns
 	 *   appropriate return codes. 
 	 */
 	/*public static void test_ReadBoard(State gamestate) {
 		int retval = 0;
 		String test_file_path;
 		File test_file;
 		FileInputStream test_stream;
 		
 		System.out.println("Testing ReadBoard function...");
 		
 		System.out.print("Test 1: Argument passed to ReadBoard is null.\nTest 1 Result: ");
 		retval = gamestate.readBoard(null);
 		if (retval == -1) {
 			System.out.println("Passed.");
 		} else {
 			System.out.println("Failed! ReadBoard returned code: " + retval);
 		}
 		
 		System.out.print("Test 2: No turn counter.\nTest 2 Result: ");
 		test_file_path = ".\\tests\\board\\no_turn_counter.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -2) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 3: Turn counter beyond max allowed turns.\nTest 3 Result: ");
 		test_file_path = ".\\tests\\board\\over_max_turns.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -3) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 4: Missing next player indicator.\nTest 4 Result: ");
 		test_file_path = ".\\tests\\board\\no_player_turn.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -4) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 5: Invalid next player indicator.\nTest 5 Result: ");
 		test_file_path = ".\\tests\\board\\invalid_player.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -5) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 6: Not enough rows on board.\nTest 6 Result: ");
 		test_file_path = ".\\tests\\board\\not_enough_rows.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -6) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 7: Not enough columns in some row.\nTest 7 Result: ");
 		test_file_path = ".\\tests\\board\\not_enough_columns.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -7) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 8: Invalid piece on the board.\nTest 8 Result: ");
 		test_file_path = ".\\tests\\board\\invalid_piece.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -8) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.print("Test 9: Read valid board.\nTest 9 Result: ");
 		test_file_path = ".\\tests\\board\\valid_board.txt";
 		test_file = new File(test_file_path);
 		try {
 			test_stream = new FileInputStream(test_file);
 			retval = gamestate.readBoard(test_stream);
 			if (retval == -0) {
 				System.out.println("Passed.");
 			} else {
 				System.out.println("Failed! ReadBoard returned code: " + retval);
 			}
 			test_stream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to open test file " + test_file_path);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}*/
 	
 	/*public static void test_getMovesForPieceAtIndex(State gamestate) {
 		int x = 0;
 		int y = 3;
 		System.out.println("Test 1: Scanning for moves from the pawn in square " + x + "," + y);
 		Vector<Move> valid_bpawn_moves = gamestate.getMovesForPieceAtIndex(x,y);
 		if (valid_bpawn_moves == null) {
 			System.out.println("Failed: MoveScan returned null.");
 		} else { 
 			for (int i = 0; i < valid_bpawn_moves.size(); i++) {
 				System.out.println(valid_bpawn_moves.elementAt(i));
 			}
 		}
 		//gamestate.executeMove(valid_bpawn_moves.elementAt(0));
 		
 		x = 4;
 		y = 4;
 		System.out.println("Test 2: Scanning for moves from the pawn in square " + x + "," + y);
 		Vector<Move> valid_bking_moves = gamestate.getMovesForPieceAtIndex(x,y);
 		if (valid_bking_moves == null) {
 			System.out.println("Failed: MoveScan returned null.");
 		} else { 
 			for (int i = 0; i < valid_bking_moves.size(); i++) {
 				System.out.println(valid_bking_moves.elementAt(i));
 			}
 		}
 	}*/
 
 }
