 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 /* Class:
  *   State
  * Description:
  *   Tracks the current state of the chess board, what turn it is and who is next
  *   to play. Also decides when there is a winner and who it is.
  */
 public class State {
 	private char[][] board;  // Array grid representation of the chess board.
 	private int num_rows;    // Number of rows in the chess board.
 	private int num_columns; // Number of columns in the chess board.
 	private int num_turns;   // Number of turns taken in the current game.
 	private int max_turns;   // Maximum number of turns allowed before game end.
 	private boolean white_is_next; // It is White's turn to play (True/False).
 	
 	/* Function:
 	 *   State()
 	 * Description:
 	 *   Default constructor. Builds a fresh chess board for a new game.
 	 */
 	public State() {
 		num_rows = 6;
 		num_columns = 5;
 		num_turns = 0;
 		max_turns = 40;
 		white_is_next = true;
 		board = new char[num_rows][num_columns];
 		
 		/* Initialize board */
 		/*	      4
 			  kqbnr 5
 			  ppppp
 			  .....
 			  .....
 			  PPPPP
 			0 RNBQK
 		 	  0      */
 		board[0][0] = 'R';
 		board[0][1] = 'N';
 		board[0][2] = 'B';
 		board[0][3] = 'Q';
 		board[0][4] = 'K';
 		for (int i = 0; i < num_columns; i++) {
 			board[1][i] = 'P';
 		}
 		for (int i = 0; i < num_columns; i++) {
 			board[2][i] = '.';
 			board[3][i] = '.';
 		}
 		for (int i = 0; i < num_columns; i++) {
 			board[4][i] = 'p';
 		}
 		board[5][0] = 'k';
 		board[5][1] = 'q';
 		board[5][2] = 'b';
 		board[5][3] = 'n';
 		board[5][4] = 'r';
 	}
 	
 	/* Function:
 	 *   int ReadBoard(InputStream new_state)
 	 * Description:
 	 *   Takes a byte array representation of a chess board from an InputStream 
 	 *   and uses it to construct a new board state.
 	 * Return values:
 	 *   0 : Success.
 	 *  -1 : Cannot read new_state because it is null.
 	 *  -2 : Invalid input: No turn counter, or turn counter is in the wrong location.
 	 *  -3 : Invalid input: Current turn is greater than maximum allowed turns.
 	 *  -4 : Invalid input: No current player turn indicator, or current player turn
 	 *       indicator is in the wrong location.
 	 *  -5 : Invalid input: Current player must be 'W' for White or 'B' for Black.
 	 *  -6 : Invalid input: Not enough rows on the board.
 	 *  -7 : Invalid input: Not enough columns in a row on the board.
 	 *  -8 : Invalid input: Invalid piece on the board. 
 	 */
 	public int ReadBoard(InputStream new_state) {
 		if (new_state == null) {
 			return -1;
 		}
 		/* Initialize local variables */
 		Scanner in = new Scanner(new_state); // Input stream Scanner.
 		int new_num_turns;                   // New number of turns taken this game.
 		boolean new_white_is_next;           // New value for white_is_next (i.e. next to play).
 		String raw_input;                    // String for holding/parsing input.
 		char[][] new_board = new char[num_rows][num_columns];  // New board layout.
 		
 		/* Begin reading and parsing input */
 		
 		/* Step 1. Get new turn number. */
 		if (!(in.hasNextInt())) {
 			in.close();
 			return -2;
 		}
 		new_num_turns = in.nextInt();
 		/* Input validation: Check that num_turns <= max_turns. */
 		if (new_num_turns > max_turns) {
 			in.close();
 			return -3;
 		}
 		
 		/* Step 2. Get current player's turn. */
 		if (!(in.hasNext(Pattern.compile("\\w")))) {
 			in.close();
 			return -4;
 		}
 		raw_input = in.nextLine();
 		/* Would be nice if this handled some arbitrary number of spaces. */
 		if (raw_input.equalsIgnoreCase(" W")) {
 			new_white_is_next = true;
 		} else if (raw_input.equalsIgnoreCase(" B")) {
 			new_white_is_next = false;
 		} else {
 			in.close();
 			return -5;
 		}
 		
 		/* Step 3. Parse the layout of the board. */
 		char[] new_row = new char[num_columns];
		for (int cur_row = num_rows - 1; cur_row >= 0; cur_row--) {
 			if (!(in.hasNextLine())) {
 				in.close();
 				return -6;
 			}
 			
 			/* Step 3a. Get next row. */
 			raw_input = in.nextLine();
 			/* Step 3b. Verify next row has the correct number of columns. */
 			if (!(raw_input.length() == num_columns)) {
 				in.close();
 				return -7;
 			}
 			/* Step 3c. Verify that all pieces in this row are valid and
 			 * add them to the new board. */
 			new_row = raw_input.toCharArray();
 			char[] valid_pieces = {'.','K','Q','R','B','N','P'};
 			int num_valid_pieces = valid_pieces.length;
 			
 			for (int cur_column = 0; cur_column < num_columns; cur_column++) {
 				char piece = new_row[cur_column];
 				boolean piece_is_valid = false;
 				int cur_valid_piece = 0;
 				
 				/* While we haven't confirmed that the current piece is valid,
 				 * iterate through the list of valid pieces and check if it is
 				 * one of them. */
 				while (!(piece_is_valid) && (cur_valid_piece < num_valid_pieces)) {
 					if (Character.toUpperCase(piece) == valid_pieces[cur_valid_piece]) {
 						piece_is_valid = true;
 					} else {
 						cur_valid_piece++;
 					}
 				}
 				if (!(piece_is_valid)) {
 					in.close();
 					return -8;
 				}
 				/* Piece is valid, add it to the new board. */
 				new_board[cur_row][cur_column] = piece;
 			}
 		}
 		
 		/* Step 4. All previous steps were successful, so commit new board state. */
 		board = new_board;
 		white_is_next = new_white_is_next;
 		num_turns = new_num_turns;
 		
 		in.close();
 		return 0;
 	}
 	
 	/* Function:
 	 *   void WriteBoard(PrintStream out)
 	 * Description:
 	 *   Writes a byte array representation of a chess board and the current
 	 *   game state to an OutputStream. If the PrintStream argument passed in is null,
 	 *   then it will print to standard out.
 	 */
 	public void WriteBoard(PrintStream out) {
 		if (out == null) {
 			out = new PrintStream(System.out);
 		}
 		String str_num_turns = Integer.toString(num_turns);
 		char[] char_num_turns = str_num_turns.toCharArray();
 		for (int i = 0; i < char_num_turns.length; i++) {
 			out.write(char_num_turns[i]);
 		}
 		out.write(' ');
 		if (white_is_next) {
 			out.write('W');
 		} else {
 			out.write('B');
 		}
 		out.write('\n');
		for (int i = num_rows - 1; i >= 0; i--) {
 			for (int j = 0; j < num_columns; j++) {
 				out.write(board[i][j]);
 			}
 			out.write('\n');
 		}
 		out.flush();
 	}
 	
 	/* Function:
 	 *   void WriteBoard()
 	 * Description:
 	 *   Overloaded function which calls WriteBoard(PrintStream out), passing it
 	 *   standard out.
 	 */
 	public void WriteBoard() {
 		WriteBoard(System.out);
 	}
 
 	/* Function:
 	 *   Vector<Move> MoveScan(Square init_position, int dx, int dy, boolean allow_capture)
 	 * Description:
 	 *   Function which generates a list of valid moves for a particular piece in
 	 *   a particular direction.
 	 * Arguments:
 	 *   init_position : A Square object which indicates the initial position of the
 	 *                   piece we wish to move.
 	 *              dx : Indicator of the direction and number of spaces we wish to move along
 	 *                   the x axis.
 	 *              dy : Indicator of the direction and number of spaces we wish to move along
 	 *                   the y axis.
 	 *   allow_capture : A boolean value which indicates if we want to consider possible captures
 	 *                   by the piece in the given direction.
 	 * Return values:
 	 *            null : If the given square has no piece on it (represented by a '.'), or is
 	 *                   not on the board, there are no valid moves and the function returns null.
 	 *    Vector<Move> : A Vector object containing all valid Moves that the piece in the given
 	 *                   Square can make in the given direction.
 	 */
 	public Vector<Move> MoveScan(Square init_position, int dx, int dy, boolean allow_capture) {
 		int x = init_position.x;
 		int y = init_position.y;
 		
 		/* Ensure that the square we're checking actually exists and has a piece in it. */ 
 		if (x >= num_columns || y >= num_rows || !(Character.isLetter(board[x][y]))) {
 			return null;
 		}
 		
 		boolean piece_is_white = Character.isUpperCase(board[x][y]);
 		boolean more_moves = true;
 		Vector<Move> valid_moves = new Vector<Move>(6);
 		
 		
 		
 		/* Begin scanning in the given direction for valid moves. */
 		x += dx;
 		y += dy;
 		while (x < num_columns && y < num_rows && more_moves) {
 			char cur_square = board[x][y];
 			if (cur_square == '.') {
 				/* If nothing is in the target square, it's a valid place to move so add it. */
 				valid_moves.add(new Move(init_position,new Square(x,y)));
 				x += dx;
 				y += dy;
 			} else {
 				/* If there is another piece in the square, we can only move there if
 				 * we are allowed to capture it. */
 				boolean target_is_white = Character.isUpperCase(cur_square);
 				if (piece_is_white != target_is_white) {
 					if (allow_capture) {
 						valid_moves.add(new Move(init_position,new Square(x,y)));
 					}
 				}
 				more_moves = false;
 			}
 		}
 		
 		return valid_moves;
 	}
 }
