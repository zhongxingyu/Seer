 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Vector;
 
 public class MiniChessPlayer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		State gamestate = new State();
 		
 		// Print board starting state.
 		System.out.println("MiniChess board starting state:");
 		gamestate.WriteBoard();
 
 		test_ReadBoard(gamestate);
 
 		// Print ending starting state.
 		System.out.println("MiniChess board ending state:");
 		gamestate.WriteBoard(null);
 		
		//test_MoveScan(gamestate);
 		
 	}
 	
 	/* Function:
 	 *   test_ReadBoard(State gamestate)
 	 * Description:
 	 *   Run a series of tests against the ReadBoard function, passing it
 	 *   differently constructed MiniChess boards that are invalid in different
 	 *   ways, to ensure that the function detects these errors and returns
 	 *   appropriate return codes. 
 	 */
 	public static void test_ReadBoard(State gamestate) {
 		int retval = 0;
 		String test_file_path;
 		File test_file;
 		FileInputStream test_stream;
 		
 		System.out.println("Testing ReadBoard function...");
 		
 		System.out.print("Test 1: Argument passed to ReadBoard is null.\nTest 1 Result: ");
 		retval = gamestate.ReadBoard(null);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 			retval = gamestate.ReadBoard(test_stream);
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
 	}
 	
 	public static void test_MoveScan(State gamestate) {
 		int x = 4;
 		int y = 4;
 		int dx = -1;
 		int dy = 0;
 		boolean allow_capture = false;
 		
 		System.out.println("Test 1: Scanning for pieces from the pawn in square " + x + "," + y);
 		Vector<Move> valid_bpawn_moves = gamestate.MoveScan(new Square(x,y),dx,dy,allow_capture);
 		if (valid_bpawn_moves == null) {
 			System.out.println("Failed: MoveScan returned null.");
 		} else { 
 			for (int i = 0; i < valid_bpawn_moves.size(); i++) {
 				System.out.println(valid_bpawn_moves.elementAt(i));
 			}
 		}
 		
 		x = 0;
 		y = 5;
 		dx = 1;
 		dy = -1;
 		allow_capture = true;
 		System.out.println("Test 2: Scanning for pieces from the king in square " + x + "," + y);
 		Vector<Move> valid_bking_moves = gamestate.MoveScan(new Square(x,y),dx,dy,allow_capture);
 		if (valid_bking_moves == null) {
 			System.out.println("Failed: MoveScan returned null.");
 		} else { 
 			for (int i = 0; i < valid_bking_moves.size(); i++) {
 				System.out.println(valid_bking_moves.elementAt(i));
 			}
 		}
 	}
 }
