 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.regex.Pattern;
 import java.util.Random;
 
 /* Class:
  *   State
  * Description:
  *   Tracks the current state of the chess board, what turn it is and who is next
  *   to play. Also decides when there is a winner and who it is.
  */
 public class State implements Cloneable,Comparable<State> {
 	private char[][] board;  // Array grid representation of the chess board.
 	private int num_rows;    // Number of rows in the chess board.
 	private int num_columns; // Number of columns in the chess board.
 	private int num_turns;   // Number of turns taken in the current game.
 	private int max_turns;   // Maximum number of turns allowed before game end.
 	private int num_states_evaluated;  // Number of states looked at during recursive calls (just for stats).
 	private boolean white_is_next;     // It is White's turn to play (True/False).
 	private boolean game_is_over;      // This game is over.
 	private boolean white_wins;        // White has won this game.
 	private boolean black_wins;        // Black has won this game.
 	private Move best_move;            // Best move found in the time elapsed.
 	private long searchStartTime;      // When the move search timer was started.
 	private double searchElapsedTime;  // How much time has elapsed since the move search timer was started.
 	private double moveTimeLimit;      // How much time to allow the bot to come up with a move.
 	private int gameWinValue;          // State value for winning the game.
 	private long hash;                 // The Zobrist hash for this game state.
 	private long whiteHash;            // The long integer that indicates white is on move in a Zobrist hash.
 	private long blackHash;            // The long integer that indicates black is on move in a Zobrist hash.
 	private ZobristTable zob;          // The Zobrist hash generator.
 	private TTable tt;                 // The transposition table.
 	
 	/* Function:
 	 *   State
 	 * Description:
 	 *   Default constructor. Builds a fresh chess board for a new game.
 	 * Inputs:
 	 *   None.
 	 * Outputs:  
 	 *   A newly initialized board with starting game state.
 	 * Return values:
 	 *   None.
 	 */
 	public State() {
 		num_rows = 6;
 		num_columns = 5;
 		num_turns = 1;
 		max_turns = 40;
 		num_states_evaluated = 0;
 		searchStartTime = 0;
 		searchElapsedTime = 0;
 		moveTimeLimit = 1.0;
 		gameWinValue = 100000;
 		white_is_next = true;
 		game_is_over = false;
 		white_wins = false;
 		black_wins = false;
 		best_move = null;
 		board = new char[num_columns][num_rows];
 		hash = 0L;
 		zob = new ZobristTable();
 		Random rnd = new Random();
 		whiteHash = rnd.nextLong();
 		blackHash = rnd.nextLong();
 		tt = new TTable();
 		
 		/* Initialize board
 		 *	      4
 		 *    kqbnr 5
 		 *    ppppp
 		 *    .....
 		 *    .....
 		 *    PPPPP
 		 *  0 RNBQK
 		 *    0      
 		 * Indexing: [x][y]
 		 *           [col][row] 
 		 */
 		board[0][0] = 'R';
 		board[1][0] = 'N';
 		board[2][0] = 'B';
 		board[3][0] = 'Q';
 		board[4][0] = 'K';
 		for (int i = 0; i < num_columns; i++) {
 			board[i][1] = 'P';
 		}
 		for (int i = 0; i < num_columns; i++) {
 			board[i][2] = '.';
 			board[i][3] = '.';
 		}
 		for (int i = 0; i < num_columns; i++) {
 			board[i][4] = 'p';
 		}
 		board[0][5] = 'k';
 		board[1][5] = 'q';
 		board[2][5] = 'b';
 		board[3][5] = 'n';
 		board[4][5] = 'r';
 		genHash();
 	}
 
 	/* Function:
 	 *   clone
 	 * Description:
 	 *   Generates a copy of the current object.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A new State object containing the same data as the instance this function was called on.
 	 */
 	public State clone() {
 		State newState = new State();
 		newState.num_rows = this.num_rows;
 		newState.num_columns = this.num_columns;
 		newState.num_turns = this.num_turns;
 		newState.max_turns = this.max_turns;
 		newState.white_is_next = this.white_is_next;
 		newState.game_is_over = this.game_is_over;
 		newState.white_wins = this.white_wins;
 		newState.black_wins = this.black_wins;
 		newState.board = new char[newState.num_columns][newState.num_rows];
 		
 		for (int i = 0; i < num_columns; i++) {
 			for (int j = 0; j < num_rows; j++) {
 				newState.board[i][j] = this.board[i][j];
 			}
 		}
 		newState.zob = this.zob;
 		newState.tt = this.tt;
 		newState.hash = this.hash;
 		newState.whiteHash = this.whiteHash;
 		newState.blackHash = this.blackHash;
 		
 		return newState;
 	}
 
 	/* Function:
 	 *   whiteOnMove
 	 * Description:
 	 *   Returns true if White is next to play.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if White is next to play.
 	 *   False : Returned if White is not next to play.
 	 */
 	public boolean whiteOnMove() {
 		return white_is_next;
 	}
 	
 	/* Function:
 	 *   blackOnMove
 	 * Description:
 	 *   Returns true if Black is next to play.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if Black is next to play.
 	 *   False : Returned if Black is not next to play.
 	 */
 	public boolean blackOnMove() {
 		return !white_is_next;
 	}
 	
 	/* Function:
 	 *   gameOver
 	 * Description:
 	 *   Returns true if one of the game's victory conditions has been met.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   True : Returned if one of the following conditions is met:
 	 *          1. One side's king has been captured. (Victory condition #1.)
 	 *          2. One side is unable to make a valid move. (Victory condition #2.)
 	 *          3. The players have taken more than the number of allowed turns (i.e.
 	 *             the game is a draw).
 	 *  False : Returned if the game has not yet reached the maximum number of allowed
 	 *          turns (i.e. it is still in progress).
 	 */
 	public boolean gameOver() {
 		return game_is_over;
 	}
 	
 	/* Function:
 	 *   whiteWins
 	 * Description:
 	 *   Returns true if White has won the game.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if White has met one of the victory conditions shown in the
 	 *           description of GameOver.
 	 *   False : Returned if White has *not* met one of the victory conditions. 
 	 */
 	public boolean whiteWins() {
 		return white_wins;
 	}
 	
 	/* Function:
 	 *   blackWins
 	 * Description:
 	 *   Returns true if Black has won the game.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if Black has met one of the victory conditions shown in the
 	 *           description of GameOver.
 	 *   False : Returned if Black has *not* met one of the victory conditions. 
 	 */
 	public boolean blackWins() {
 		return black_wins;
 	}
 	
 	/* Function:
 	 *   readBoard
 	 * Description:
 	 *   Takes a byte array representation of a chess board from an InputStream 
 	 *   and uses it to construct a new board state.
 	 * Inputs:
 	 *   new_state : An InputStream to read a new board layout from. Expects a
 	 *               specific format, which is written out by the WriteBoard function.
 	 * Outputs:
 	 *   Overwrites this instance of a State object with new data. 
 	 * Returns:
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
 	public int readBoard(InputStream new_state) {
 		if (new_state == null) {
 			return -1;
 		}
 		/* Initialize local variables */
 		Scanner in = new Scanner(new_state); // Input stream Scanner.
 		int new_num_turns;                   // New number of turns taken this game.
 		boolean new_white_is_next;           // New value for white_is_next (i.e. next to play).
 		String raw_input;                    // String for holding/parsing input.
 		char[][] new_board = new char[num_columns][num_rows];  // New board layout.
 		
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
 				new_board[cur_column][cur_row] = piece;
 			}
 		}
 		
 		/* Step 4. All previous steps were successful, so commit new board state. */
 		board = new_board;
 		white_is_next = new_white_is_next;
 		num_turns = new_num_turns;
 		
 		in.close();
 		genHash();
 		return 0;
 	}
 	
 	/* Function:
 	 *   writeBoard
 	 * Description:
 	 *   Writes a byte array representation of a chess board and the current
 	 *   game state to an OutputStream. If the PrintStream argument passed in is null,
 	 *   then it will print to standard out.
 	 * Inputs:
 	 *   out : A PrintStream object that a new board layout can be written out to.
 	 * Outputs:
 	 *   A stream that represents the current state of the board and game.
 	 * Return values:
 	 *   None.
 	 */
 	public void writeBoard(PrintStream out) {
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
 				out.write(board[j][i]);
 			}
 			out.write('\n');
 		}
 		out.flush();
 	}
 	
 	/* Function:
 	 *   writeBoard
 	 * Description:
 	 *   Overloaded function which calls WriteBoard(PrintStream out), passing it
 	 *   standard out as an argument.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   Writes the current state of the board through standard out.
 	 * Return values:
 	 *   None.
 	 */
 	public void writeBoard() {
 		writeBoard(System.out);
 	}
 
 	/* Function:
 	 *   makeImcsMove
 	 * Description:
 	 *   Takes a String in the form "a1-b1" and attempts to execute it as a move.
 	 *   Columns (x) are parsed as A-E (1-5), and rows (y) are parsed as 1-6.
 	 * Inputs:
 	 *   rawmove : A String object containing a textual representation of the move to execute.
 	 *             Expects the format L#-L# where L is a letter and # is a number as defined above.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed.
 	 */
 	public State makeImcsMove(String rawmove) throws Exception {
 		if (rawmove == null) {
 			throw new Exception("Invalid Move.");
 		} else if (!rawmove.matches("\\w\\d-\\w\\d")) {
 			throw new Exception("Improperly formatted move.");
 		} else if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		
 		String[] move = rawmove.split("-");
 		
 		String fromCol = move[0].substring(0,1).toLowerCase();
 		String toCol = move[1].substring(0,1).toLowerCase();
 		
 		int from_x;
 		int to_x;
 		int from_y = Integer.parseInt(move[0].substring(1,2)) - 1;
 		int to_y = Integer.parseInt(move[1].substring(1,2)) - 1;
 		
 		switch(fromCol) {
 		case "a":
 			from_x = 0;
 			break;
 		case "b":
 			from_x = 1;
 			break;
 		case "c":
 			from_x = 2;
 			break;
 		case "d":
 			from_x = 3;
 			break;
 		case "e":
 			from_x = 4;
 			break;
 		default:
 			throw new Exception("Failed to parse column letter.");
 		}
 		
 		switch(toCol) {
 		case "a":
 			to_x = 0;
 			break;
 		case "b":
 			to_x = 1;
 			break;
 		case "c":
 			to_x = 2;
 			break;
 		case "d":
 			to_x = 3;
 			break;
 		case "e":
 			to_x = 4;
 			break;
 		default:
 			throw new Exception("Failed to parse row letter.");
 		}
 		
 		Move imcsMove = new Move(new Square(from_x, from_y), new Square(to_x, to_y));
 		return executeMove(imcsMove);
 	}
 	
 	/* Function:
 	 *   getImcsMove()
 	 * Description:
 	 *   Uses the negamax algorithm to explore move possibilities, then selects and returns a good
 	 *   move for the current side in IMCS move format.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A new String containing the move to be made in IMCS move format (A1-B1).
 	 */
 	public String getImcsMove() throws Exception {
 		if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		
 		searchElapsedTime = 0.0;
 		searchStartTime = System.nanoTime();
 		best_move = getBestMove();
 		
 		return best_move.toString();
 	}
 
 	/* Function:
 	 *   makeHumanMove
 	 * Description:
 	 *   Takes a String in the form "a0-b1" and attempts to execute it as a move.
 	 *   Columns (x) are parsed as A-E (0-4), and rows (y) are parsed as 0-5.
 	 * Inputs:
 	 *   rawmove : A String object containing a textual representation of the move to execute.
 	 *             Expects the format L#-L# where L is a letter and # is a number as defined above.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed.
 	 */
 	public State makeHumanMove(String rawmove) throws Exception {
 		if (rawmove == null) {
 			throw new IllegalArgumentException("Invalid Move.");
 		} else if (!rawmove.matches("\\w\\d-\\w\\d")) {
 			throw new IllegalArgumentException("Improperly formatted move.");
 		} else if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		
 		String[] move = rawmove.split("-");
 		
 		String fromCol = move[0].substring(0,1).toLowerCase();
 		String toCol = move[1].substring(0,1).toLowerCase();
 		
 		int from_x;
 		int to_x;
 		int from_y = Integer.parseInt(move[0].substring(1,2));
 		int to_y = Integer.parseInt(move[1].substring(1,2));
 		
 		switch(fromCol) {
 		case "a":
 			from_x = 0;
 			break;
 		case "b":
 			from_x = 1;
 			break;
 		case "c":
 			from_x = 2;
 			break;
 		case "d":
 			from_x = 3;
 			break;
 		case "e":
 			from_x = 4;
 			break;
 		default:
			throw new Exception("Failed to parse column letter.");
 		}
 		
 		switch(toCol) {
 		case "a":
 			to_x = 0;
 			break;
 		case "b":
 			to_x = 1;
 			break;
 		case "c":
 			to_x = 2;
 			break;
 		case "d":
 			to_x = 3;
 			break;
 		case "e":
 			to_x = 4;
 			break;
 		default:
			throw new Exception("Failed to parse row letter.");
 		}
 		Move humansMove = new Move(new Square(from_x, from_y), new Square(to_x, to_y));
 		return executeMove(humansMove);
 	}
 	
 	/* Function:
 	 *   makeRandomMove
 	 * Description:
 	 *   Selects and executes a random possible move for the current side.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.  
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed.
 	 */
 	public State makeRandomMove() throws Exception {
 		if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		
 		Vector<Move> possible_moves = getAllValidMoves();
 		
 		/* Select a random move to execute. */
 		Random generator = new Random();
 		int randomIndex = generator.nextInt(possible_moves.size());
 		Move selected_move = possible_moves.elementAt(randomIndex);
 		
 		return executeMove(selected_move);
 	}
 	
 	/* Function:
 	 *   makeRandomGoodMove
 	 * Description:
 	 *   Selects and executes a good move for the current side, using heuristics
 	 *   to determine the best next move.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.  
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed.
 	 */
 	public State makeRandomGoodMove() throws Exception {
 		if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		
 		Vector<Move> possibleMoves = getAllValidMoves();
 		/* Iterate through the list of all possible moves, and generate a list of all 
 		 * board states that result from executing those moves. */
 		Vector<State> possibleStates = new Vector<State>();
 		for (int i = 0; i < possibleMoves.size(); i++) {
 			possibleStates.add(executeMove(possibleMoves.elementAt(i)));
 		}
 		
 		int bestStateValue = 10000;
 		Vector<State> bestStates = new Vector<State>();
 		for (int i = 0; i < possibleStates.size(); i++) {
 			State curState = possibleStates.elementAt(i);
 			int curStateValue = curState.getStateValue();
 			if (curStateValue < bestStateValue) {
 				/* This state is better than the previous best. */
 				bestStateValue = curStateValue;
 				bestStates = new Vector<State>();
 				bestStates.add(curState);
 			} else if (curStateValue == bestStateValue) {
 				/* This state is as good as the previous best. */
 				bestStates.add(curState);
 			}
 		}
 		
 		if (bestStates.isEmpty()) {
 			/* If we didn't find anything better than average, just pick something. */
 			bestStates.addAll(possibleStates);
 		}
 		
 		/* Pick a random State from the best available. */
 		Random generator = new Random();
 		int randomIndex = generator.nextInt(bestStates.size());
 		
 		return bestStates.elementAt(randomIndex);
 	}
 	
 	/* Function:
 	 *   makeSmartMove
 	 * Description:
 	 *   Uses the negamax algorithm to explore move possibilities, then selects and executes a good
 	 *   move for the current side.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.  
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed.
 	 */
 	public State makeSmartMove() throws Exception {
 		if (gameOver()) {
 			throw new Exception("Game is over.");
 		}
 		searchElapsedTime = 0.0;
 		searchStartTime = System.nanoTime();
 		best_move = getBestMove();
 		State returnState = executeMove(best_move);
 		
 		return returnState;
 	}
 	
 	/* Function:
 	 *   pieceIsOnMove
 	 * Description:
 	 *   Returns true if the color of the piece given (i.e. the case) matches
 	 *   the color of the player who is next to move. Otherwise, returns false.
 	 * Inputs:
 	 *   ch : The character representation of a piece.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if the piece matches the color of the player who is on move.
 	 *   False : Returned if the piece is not a valid piece, OR it does not match the color
 	 *           of the player who is on move.
 	 */
 	private boolean pieceIsOnMove(char ch) {
 		if (ch == '.') {
 			return false;
 		} else if (Character.isLetter(ch)) {
 			if (Piece.isWhite(ch) && white_is_next) {
 				return true;
 			} else if (Piece.isBlack(ch) && !white_is_next) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}	
 	
 	/* Function:
 	 *   indexIsValid
 	 * Description:
 	 *   Ensures that the given coordinates reference a square on the board.
 	 * Inputs:
 	 *   x : An integer value indicating the x coordinate (column) of the square to check.
 	 *   y : An integer value indicating the y coordinate (row) of the square to check.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if the given coordinates indicate a valid square.
 	 *   False : Returned if the given coordinates indicate an invalid square.
 	 */
 	private boolean indexIsValid(int x, int y) {
 		if (x >= num_columns || x < 0) {
 			return false;
 		} else if (y >= num_rows || y < 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	/* Function:
 	 *   squareIsValid
 	 * Description:
 	 *   Ensures that the given square exists on the board.
 	 * Inputs:
 	 *   sq : A Square object containing the coordinates to check.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *    True : Returned if the given square is on the board.
 	 *   False : Returned if the given square is not on the board.
 	 */
 	private boolean squareIsValid(Square sq) {
 		if (sq == null) {
 			return false;
 		} else {
 			return indexIsValid(sq.x,sq.y);
 		}
 	}
 
 	/* Function:
 	 *   getPieceAtIndex
 	 * Description:
 	 *   Function which returns the character representation of the piece at the
 	 *   given coordinates.
 	 * Inputs:
 	 *   x : An integer value indicating the x coordinate (column) of the piece to get.
 	 *   y : An integer value indicating the y coordinate (row) of the piece to get.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   The character representation of the piece (or blank square) at the given indexes.
 	 */
 	private char getPieceAtIndex(int x, int y) throws Exception {
 		if (!indexIsValid(x,y)) {
			throw new Exception("Invalid coordinates.");
 		} else {
 			return board[x][y];
 		}
 	}
 	
 	/* Function:
 	 *   getPieceAtSquare
 	 * Description:
 	 *   Function which returns the character representation of the piece at the
 	 *   given Square.
 	 * Inputs:
 	 *   sq : A Square object containing the x and y indexes of the piece to get.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   The character representation of the piece (or blank square) at the given Square.
 	 */
 	private char getPieceAtSquare(Square sq) throws Exception {
 		if (!squareIsValid(sq)) {
			throw new Exception("Invalid square.");
 		} else {
 			return getPieceAtIndex(sq.x,sq.y);
 		}
 	}	
 	
 	/* Function:
 	 *   getStateValue
 	 * Description:
 	 *   Uses heuristics to generate an integer value that represents how advantageous the current
 	 *   state of the game is for the current player.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   An integer value between -100,000 and 100,000. A higher number means the game state is more
 	 *   advantageous to the player that is on move. A 100,000 (or -100,000) means a sure win (or loss).
 	 */
 	private int getStateValue() {
 		int stateValue = 0;
 		int pawnValue = 1000;
 		int knightValue = 3000;
 		int bishopValue = 3000;
 		int rookValue = 5000;
 		int queenValue = 9000;
 		int centerPieceValue = 50;
 		int developedPieceValue = 200;
 		int advancedPawnValue = 150; // multiplied by how far up it is.
 		int doubledPawnValue = -100;
 		int pawnChainValue = 100;
 		boolean black_king_taken = true;
 		boolean white_king_taken = true;
 		
 		for (int i = 0; i < num_rows; i++) {
 			for (int j = 0; j < num_columns; j++) {
 				Square cur_square = new Square(j,i);
 				try {
 					Piece cur_piece = new Piece(getPieceAtSquare(cur_square),cur_square);
 					
 					/* Sum up the values of all pieces left on the board. */
 					if (cur_piece.piece_ch != '.') {
 						switch (cur_piece.piece_ch) {
 						case 'K':
 							white_king_taken = false;
 							break;
 						case 'k':
 							black_king_taken = false;
 							break;
 						case 'Q':
 							if (whiteOnMove()) {
 								stateValue += queenValue;
 							} else {
 								stateValue -= queenValue;
 							}
 							break;
 						case 'q':
 							if (blackOnMove()) {
 								stateValue += queenValue;
 							} else {
 								stateValue -= queenValue;
 							}
 							break;
 						case 'R':
 							if (whiteOnMove()) {
 								stateValue += rookValue;
 							} else {
 								stateValue -= rookValue;
 							}
 							break;
 						case 'r':
 							if (blackOnMove()) {
 								stateValue += rookValue;
 							} else {
 								stateValue -= rookValue;
 							}
 							break;
 						case 'B':
 							if (whiteOnMove()) {
 								stateValue += bishopValue;
 							} else {
 								stateValue -= bishopValue;
 							}
 							break;
 						case 'b':
 							if (blackOnMove()) {
 								stateValue += bishopValue;
 							} else {
 								stateValue -= bishopValue;
 							}
 							break;
 						case 'N':
 							if (whiteOnMove()) {
 								stateValue += knightValue;
 							} else {
 								stateValue -= knightValue;
 							}
 							break;
 						case 'n':
 							if (blackOnMove()) {
 								stateValue += knightValue;
 							} else {
 								stateValue -= knightValue;
 							}
 							break;
 						case 'P':
 							if (whiteOnMove()) {
 								stateValue += pawnValue;
 							} else {
 								stateValue -= pawnValue;
 							}
 							break;
 						case 'p':
 							if (blackOnMove()) {
 								stateValue += pawnValue;
 							} else {
 								stateValue -= pawnValue;
 							}
 							break;
 						}
 						
 						/* Assign some value to having pieces in the center squares of the board. */
 						if (cur_square.y > 1 && cur_square.y < 4) {
 							if (cur_square.x > 0 && cur_square.x < 4) {
 								if (whiteOnMove()) {
 									if (cur_piece.isWhite()) {
 										stateValue += centerPieceValue;
 									} else {
 										stateValue -= centerPieceValue;
 									}
 								} else {
 									if (cur_piece.isBlack()) {
 										stateValue += centerPieceValue;
 									} else {
 										stateValue -= centerPieceValue;
 									}
 								}								
 							}
 						}
 						
 						/* Add value for developed major pieces. */
 						if (cur_piece.isDeveloped()) {
 							if(whiteOnMove()) {
 								if (cur_piece.isWhite()) {
 									stateValue += developedPieceValue;
 								} else {
 									stateValue -= developedPieceValue;
 								}
 							} else {
 								if (cur_piece.isBlack()) {
 									stateValue += developedPieceValue;
 								} else {
 									stateValue -= developedPieceValue;
 								}
 							}
 						}
 						
 						/* Add value to advanced pawns. */
 						if (cur_square.y > 0 && cur_square.y < 5) {
 							if (cur_piece.piece_ch == 'P') {
 								int modifier = cur_square.y - 1;
 								if (whiteOnMove()) {
 									stateValue += (advancedPawnValue * modifier);
 								} else {
 									stateValue -= (advancedPawnValue * modifier);
 								}
 							} else if (cur_piece.piece_ch == 'p') {
 								int modifier = 4 - cur_square.y;
 								if (blackOnMove()) {
 									stateValue += (advancedPawnValue * modifier);
 								} else {
 									stateValue -= (advancedPawnValue * modifier);
 								}
 							}
 						}
 						
 						/* Add negative value of doubled pawns. */
 						if (cur_square.y > 0 && cur_square.y < 5) {
 							if (cur_piece.piece_ch == 'P') {
 								for (int new_y = cur_square.y + 1; new_y < 6; new_y++) {
 									Square next_square = new Square(cur_square.x,new_y);
 									Piece next_piece = new Piece(getPieceAtSquare(next_square),next_square);
 									if(next_piece.piece_ch == 'P') {
 										if (whiteOnMove()) {
 											stateValue += doubledPawnValue;
 										} else {
 											stateValue -= doubledPawnValue;
 										}
 									}
 								}
 							} else if (cur_piece.piece_ch == 'p') {
 								for (int new_y = cur_square.y - 1; new_y > 0; new_y--) {
 									Square next_square = new Square(cur_square.x,new_y);
 									Piece next_piece = new Piece(getPieceAtSquare(next_square),next_square);
 									if(next_piece.piece_ch == 'p') {
 										if (blackOnMove()) {
 											stateValue += doubledPawnValue;
 										} else {
 											stateValue -= doubledPawnValue;
 										}
 									}
 								}
 							}
 						}
 						
 						/* Add value of pawn-chains. */
 						if (cur_square.y > 0 && cur_square.y < 5) {
 							if (cur_piece.piece_ch == 'P') {
 								int new_y = cur_square.y + 1;
 								for (int x_offset = -1; x_offset <= 1; x_offset += 2) {
 									int new_x = cur_square.x + x_offset;
 									if (new_x >= 0 && new_x <= 5) {
 										Square next_square = new Square(new_x,new_y);
 										Piece next_piece = new Piece(getPieceAtSquare(next_square),next_square);
 										if(next_piece.piece_ch == 'P') {
 											if (whiteOnMove()) {
 												stateValue += pawnChainValue;
 											} else {
 												stateValue -= pawnChainValue;
 											}
 										}
 									}
 								}
 							} else if (cur_piece.piece_ch == 'p') {
 								int new_y = cur_square.y - 1;
 								for (int x_offset = -1; x_offset <= 1; x_offset += 2) {
 									int new_x = cur_square.x + x_offset;
 									if (new_x >= 0 && new_x <= 5) {
 										Square next_square = new Square(new_x,new_y);
 										Piece next_piece = new Piece(getPieceAtSquare(next_square),next_square);
 										if(next_piece.piece_ch == 'p') {
 											if (blackOnMove()) {
 												stateValue += pawnChainValue;
 											} else {
 												stateValue -= pawnChainValue;
 											}
 										}
 									}
 								}
 							}
 						}
 						
 						/* Add any other state valuations here... */
 					}
 				} catch (Exception e) {
 					/* This shouldn't happen since we should only loop through
 					 * existing squares above. */
 					e.getStackTrace();
 				}		
 			}
 		}
 		
 		/* Check for game-winning states. */
 		if (white_king_taken) {
 			if (blackOnMove())
 				stateValue = gameWinValue;
 			else
 				stateValue = -gameWinValue;
 		} else if (black_king_taken) {
 			if (whiteOnMove())
 				stateValue = gameWinValue;
 			else
 				stateValue = -gameWinValue;
 		}
 		
 		return stateValue;
 	}
 	
 	/* Function:
 	 *   getMovesInDirection
 	 * Description:
 	 *   Function which generates a list of valid moves for a particular piece in
 	 *   a particular direction.
 	 * Inputs:
 	 *   init_position : A Square object which indicates the initial position of the
 	 *                   piece we wish to move.
 	 *              dx : Indicator of the direction and number of spaces we wish to move along
 	 *                   the x axis.
 	 *              dy : Indicator of the direction and number of spaces we wish to move along
 	 *                   the y axis.
 	 *   allow_capture : A boolean value which indicates if we want to consider possible captures
 	 *                   by the piece in the given direction.
 	 *         one_hop : Boolean value which, if true, instructs the function to only get moves
 	 *                   one hop in the given direction.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *            null : If the given square has no piece on it (represented by a '.'), or is
 	 *                   not on the board, there are no valid moves and the function returns null.
 	 *    Vector<Move> : A Vector object containing all valid Moves that the piece in the given
 	 *                   Square can make in the given direction.
 	 */
 	private Vector<Move> getMovesInDirection(Square init_position, int dx, int dy, boolean allow_capture, boolean one_hop) {
 		char piece;
 		try {
 			piece = getPieceAtSquare(init_position);
 			if (piece == '.') {
 				return null;
 			}
 		} catch (Exception e) {
 			return null;
 		}
 		
 		int x = init_position.x;
 		int y = init_position.y;	
 		boolean piece_is_white = Piece.isWhite(piece);
 		boolean more_moves = true;
 		Vector<Move> valid_moves = new Vector<Move>(6);
 		
 		/* Begin scanning in the given direction for valid moves. */
 		x += dx;
 		y += dy;
 		while (x < num_columns && x >= 0 && y < num_rows && y >= 0 && more_moves) {
 			char cur_square = board[x][y];
 			if (cur_square == '.') {
 				/* If nothing is in the target square, it's a valid place to move so add it. */
 				valid_moves.add(new Move(init_position,new Square(x,y)));
 				x += dx;
 				y += dy;
 			} else {
 				/* If there is another piece in the square, we can only move there if
 				 * we are allowed to capture it. */
 				boolean target_is_white = Piece.isWhite(cur_square);
 				if (piece_is_white != target_is_white) {
 					if (allow_capture) {
 						valid_moves.add(new Move(init_position,new Square(x,y)));
 					}
 				}
 				more_moves = false;
 			}
 			/* If this is a piece which only moves one hop per turn, stop after the
 			 * first iteration. */
 			if (one_hop) {
 				more_moves = false;
 			}
 		}
 		
 		return valid_moves;
 	}
 	
 	
 	/* Function:
 	 *   getMovesForPieceAtIndex
 	 * Description:
 	 *   Function which returns the valid moves for the piece at the
 	 *   given coordinates.
 	 * Inputs:
 	 *   x : An integer value indicating the x coordinate (column) of the piece to get moves for.
 	 *   y : An integer value indicating the y coordinate (row) of the piece to get moves for.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A Vector object containing all valid moves for the piece at the given coordinates.
 	 */
 	private Vector<Move> getMovesForPieceAtIndex(int x, int y) {
 		char piece;
 		try {
 			piece = getPieceAtIndex(x,y);
 		} catch (Exception e) {
 			return null;
 		}
 		
 		int dx;
 		int dy;
 		boolean allow_capture;
 		boolean one_hop;
 		Square sq = new Square(x,y);
 		Vector<Move> moves = new Vector<Move>(6,6);
 		Vector<Move> pawn_possible_caps = new Vector<Move>(2);
 		
 		switch(piece) {
 		
 		case 'K':
 		case 'k':
 			allow_capture = true;
 			one_hop = true;
 			for (dx = -1; dx <= 1; dx++) {
 				for (dy = -1; dy <= 1; dy++) {
 					if (!(dx == 0 && dy == 0)) {
 						moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 					}
 				}
 			}
 			break;
 		case 'Q':
 		case 'q':
 			allow_capture = true;
 			one_hop = false;
 			for (dx = -1; dx <= 1; dx++) {
 				for (dy = -1; dy <= 1; dy++) {
 					if (!(dx == 0 && dy == 0)) {
 						moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 					}
 				}
 			}
 			break;
 		case 'R':
 		case 'r':
 			allow_capture = true;
 			one_hop = false;
 			for (dx = -1; dx <= 1; dx++) {
 				dy = 0;
 				if (!(dx == 0)) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			for (dy = -1; dy <= 1; dy++) {
 				dx = 0;
 				if (!(dy == 0)) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			break;
 		case 'B':
 		case 'b':
 			allow_capture = true;
 			one_hop = false;
 			for (dx = -1; dx <= 1; dx++) {
 				for (dy = -1; dy <= 1; dy++) {
 					if (!(dx == 0 || dy == 0)) {
 						moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 					}
 				}
 			}
 			allow_capture = false;
 			one_hop = true;
 			for (dx = -1; dx <= 1; dx++) {
 				dy = 0;
 				if (!(dx == 0)) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			for (dy = -1; dy <= 1; dy++) {
 				dx = 0;
 				if (!(dy == 0)) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			break;
 		case 'N':
 		case 'n':
 			allow_capture = true;
 			one_hop = true;
 			for (dx = -1; dx <= 1; dx += 2) {
 				for (dy = -2; dy <= 2; dy += 4) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			for (dx = -2; dx <= 2; dx += 4) {
 				for (dy = -1; dy <= 1; dy += 2) {
 					moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 				}
 			}
 			break;
 		case 'P':
 			/* Get possible forward (non-capture) movement. */
 			allow_capture = false;
 			one_hop = true;
 			dx = 0;
 			dy = 1;
 			moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 			/* Get possible diagonal (capture) movement. */
 			allow_capture = true;
 			for (dx = -1; dx <= 1; dx += 2) {
 				pawn_possible_caps.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 			}
 			for (int i = 0; i < pawn_possible_caps.size(); i++) {
 				Square tgt_Square = pawn_possible_caps.elementAt(i).to_Square;
 				try {
 					char tgt_Piece = getPieceAtSquare(tgt_Square);
 					if (Piece.isBlack(tgt_Piece)) {
 						moves.add(pawn_possible_caps.elementAt(i));
 					}
 				} catch (Exception e1) {
 					continue;
 				}
 			}
 			break;
 		case 'p':
 			/* Get possible forward (non-capture) movement. */
 			allow_capture = false;
 			one_hop = true;
 			dx = 0;
 			dy = -1;
 			moves.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 			/* Get possible diagonal (capture) movement. */
 			allow_capture = true;
 			for (dx = -1; dx <= 1; dx += 2) {
 				pawn_possible_caps.addAll(getMovesInDirection(sq,dx,dy,allow_capture,one_hop));
 			}
 			for (int i = 0; i < pawn_possible_caps.size(); i++) {
 				Square tgt_Square = pawn_possible_caps.elementAt(i).to_Square;
 				try {
 					char tgt_Piece = getPieceAtSquare(tgt_Square);
 					if (Piece.isWhite(tgt_Piece)) {
 						moves.add(pawn_possible_caps.elementAt(i));
 					}
 				} catch (Exception e1) {
 					continue;
 				}
 			}
 			break;
 		default:
 			/* Empty space or something else. */
 			moves = null;
 			break;
 			
 		}
 		return moves;
 	}
 	
 	/* Function:
 	 *   getMovesForPieceAtSquare
 	 * Description:
 	 *   Overloaded function which calls getMovesForPieceAtIndex to find the valid
 	 *   moves for the piece at the given Square.
 	 * Inputs:
 	 *   sq : A Square object containing the indexes of the piece to get moves for.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A Vector object containing all valid moves for the piece at the given Square.
 	 */
 	private Vector<Move> getMovesForPieceAtSquare(Square sq) {
 		if (sq == null) {
 			return null;
 		} else {
 			return getMovesForPieceAtIndex(sq.x, sq.y);
 		}
 	}
 	
 	/* Function:
 	 *   getAllValidMoves
 	 * Description:
 	 *   Searches for and finds all valid moves for the pieces belonging to the player on move.
 	 * Inputs:
 	 *   None.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A Vector object containing all valid moves for all pieces belonging to the player on move.
 	 */
 	public Vector<Move> getAllValidMoves() {
 		/* Scan the board for all the pieces belonging to the player that is on move. */
 		Vector<Square> occupied_squares = new Vector<Square>();
 		for (int i = 0; i < num_rows; i++) {
 			for (int j = 0; j < num_columns; j++) {
 				Square cur_square = new Square(j,i);
 				try {
 					char cur_piece = getPieceAtSquare(cur_square);
 					if (cur_piece != '.') {
 						if (Piece.isWhite(cur_piece) && white_is_next) {
 							occupied_squares.add(cur_square);
 						} else if (Piece.isBlack(cur_piece) && !white_is_next) {
 							occupied_squares.add(cur_square);
 						}
 					}
 				} catch (Exception e) {
 					/* This shouldn't happen since we should only loop through
 					 * existing squares above. */
 					e.getStackTrace();
 				}
 			}
 		}
 		
 		/* Generate all possible moves for those pieces. */
 		Vector<Move> possible_moves = new Vector<Move>();
 		for (int i = 0; i < occupied_squares.size(); i++) {
 			possible_moves.addAll(getMovesForPieceAtSquare(occupied_squares.elementAt(i)));
 		}
 		
 		return possible_moves;
 	}
 	
 	/* Function:
 	 *   executeMove
 	 * Description:
 	 *   Verifies that the given move is valid to execute and then executes it,
 	 *   returning the new board state.
 	 * Inputs:
 	 *   move : A Move object containing the details of the move to execute.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A new State object containing the altered state of the game after the move
 	 *   has been executed. 
 	 */
 	private State executeMove(Move move) throws Exception {
 		if (move == null) {
 			throw new IllegalArgumentException("Invalid Move.");
 		}
 		
 		/* Check that additional moves are allowed. */
 		if (num_turns > max_turns) {
 			throw new Exception("Game Over");
 		}
 		
 		Square start_square = move.from_Square;
 		Square end_square = move.to_Square;
 		
 		char src_piece = getPieceAtSquare(start_square);
 		char tgt_piece = getPieceAtSquare(end_square);
 		if (src_piece == '.') {
 			throw new IllegalArgumentException("No piece at location.");
 		}
 		
 		/* Check that the piece in the originating square is on move. */
 		if (!pieceIsOnMove(src_piece)) {
 			throw new IllegalArgumentException("Piece not on move.");
 		}
 		
 		/* Check that the piece in the originating square can move to the target location. */
 		Vector<Move> valid_moves = new Vector<Move>();
 		valid_moves = getMovesForPieceAtSquare(start_square);
 		if (!valid_moves.contains(move)) {
 			throw new IllegalArgumentException("Move not allowed for given piece.");
 		}
 		
 		int from_x = move.from_Square.x;
 		int from_y = move.from_Square.y;
 		int to_x = move.to_Square.x;
 		int to_y = move.to_Square.y;
 		
 		/* Check for pawn promotion. */
 		if (src_piece == 'p' && to_y == 0) {
 			src_piece = 'q';
 		} else if (src_piece == 'P' && to_y == num_rows - 1) {
 			src_piece = 'Q';
 		}
 		
 		/* Generate new state to return. */
 		State new_gamestate = this.clone(); 
 		new_gamestate.board[to_x][to_y] = src_piece;
 		new_gamestate.board[from_x][from_y] = '.';
 		new_gamestate.white_is_next = !new_gamestate.white_is_next;
 		if (new_gamestate.white_is_next)
 			new_gamestate.num_turns += 1;
 		
 		/* Check for victory/draw. */
 		/* Draw condition: Too many moves. */ 
 		if (new_gamestate.num_turns > new_gamestate.max_turns) {
 			new_gamestate.game_is_over = true;
 			new_gamestate.white_wins = false;
 			new_gamestate.black_wins = false;
 		}
 		
 		/* Victory condition: Current side has no valid moves. */
 		Vector<Move> possible_moves = new_gamestate.getAllValidMoves();
 		if (possible_moves.size() == 0) {
 			new_gamestate.game_is_over = true;
 			new_gamestate.white_wins = !new_gamestate.white_is_next;
 			new_gamestate.black_wins = new_gamestate.white_is_next;
 		}
 		
 		/* Victory condition: Captured opposing king. */
 		if (tgt_piece == 'k') {
 			new_gamestate.game_is_over = true;
 			new_gamestate.white_wins = true;
 			new_gamestate.black_wins = false;
 		} else if (tgt_piece == 'K') {
 			new_gamestate.game_is_over = true;
 			new_gamestate.white_wins = false;
 			new_gamestate.black_wins = true;
 		}
 		
 		/* Iteratively generate the new hash value for the new state. */
 		long newHash = hash;
 		newHash = newHash ^ zob.getHash(end_square,tgt_piece);
 		newHash = newHash ^ zob.getHash(end_square, src_piece);
 		newHash = newHash ^ zob.getHash(start_square,src_piece);
 		newHash = newHash ^ zob.getHash(start_square, '.');
 		newHash = newHash ^ whiteHash;
 		newHash = newHash ^ blackHash;
 		new_gamestate.hash = newHash;
 		
 		return new_gamestate;
 	}
 
 	/* Function:
 	 *   negamax
 	 * Description:
 	 *   Recursively looks ahead at future possible moves to determine what the best
 	 *   move is for now. Returns that State's integer valuation. Uses alpha-beta pruning
 	 *   to drop portions of the tree of states that it deems aren't worth pursuing.
 	 * Inputs:
 	 *          s : A State object to examine.
 	 *      depth : The maximum depth to traverse before evaluating a State's value early
 	 *              (i.e. before the end of the entire move tree).
 	 *      alpha : The lowest score that the current player is guaranteed to get. For
 	 *              alpha-beta pruning.
 	 *       beta : The highest score that the opposing player is guaranteed to get. For
 	 *              alpha-beta pruning.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   An integer value representing how advantageous pursuing this direction of moves
 	 *   will be for the current player.
 	 */
 	private int negamax(State s, int depth, int alpha, int beta) {
 		num_states_evaluated++;
 		searchElapsedTime = (System.nanoTime() - searchStartTime) * 1.0e-9;
 		//if (s.gameOver() || depth <= 0 || searchElapsedTime >= moveTimeLimit)
 		if (s.gameOver() || depth <= 0)
 			return s.getStateValue();
 		
 		/* Check transposition table for a saved entry. */
 		TTableEntry entry = tt.getEntry(s.hash);
 		if (entry != null) {
 			if (entry.d >= depth) {
 				if ((entry.a < entry.v && entry.v < entry.b) || (entry.a <= alpha && beta <= entry.b)) {
 					return entry.v;
 				}
 			}
 		}
 		
 		int value = -gameWinValue;
 		int newAlpha = alpha;
 		
 		/* Get all possible next moves. */
 		Vector<Move> possibleMoves = s.getAllValidMoves();
 		int numMoves = possibleMoves.size();
 		State[] nextStates = new State[numMoves];
 		
 		/* Get all possible next states to be evaluated by negamax, then arrange
 		 * them in descending order by state value, to improve the performance of
 		 * alpha-beta pruning. */
 		try {
 			for (int i = 0; i < numMoves; i++) {
 				nextStates[i] = s.executeMove(possibleMoves.elementAt(i));
 			}
 			Arrays.sort(nextStates, Collections.reverseOrder());
 			/* Begin negamax search down the tree of possible moves. */
 			for (int i = 0; i < numMoves; i++) {
 				int newValue = -(negamax(nextStates[i], depth - 1, -beta, -newAlpha));
 				if (newValue > value)
 					value = newValue;
 				if (value > newAlpha)
 					newAlpha = value;
 				if (value >= beta)
 					return beta;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		entry = new TTableEntry(s.hash, depth, alpha, beta, value); 
 		tt.storeEntry(entry); 
 		return value;
 	}
 	
 	/* Function:
 	 *   getBestMove
 	 * Description:
 	 *   Initiates the recursive negamax search to seek out the best possible move.  
 	 * Inputs:
 	 *       s : A State object to examine.
 	 * Outputs:
 	 *   The return values.
 	 * Return values:
 	 *   A Move object that contains the source square and target square of the
 	 *   piece to move.
 	 */
 	Move getBestMove() {
 		num_states_evaluated = 0;
 		int bestValue = -gameWinValue;
 		Vector<Move> bestMoves = new Vector<Move>();
 		int curBestValue = bestValue;
 		Vector<Move> curBestMoves = null;
 		Move bestMove = null;
 		
 		/* Get all possible next moves. */
 		Vector<Move> possibleMoves = getAllValidMoves();
 		int numMoves = possibleMoves.size();
 		State[] nextStates = new State[numMoves];
 		int[] stateScores = new int[numMoves];
 		
 		try {
 			/* Get all possible next states to be evaluated by negamax. */
 			for (int i = 0; i < numMoves; i++) {
 				nextStates[i] = executeMove(possibleMoves.elementAt(i));
 				stateScores[i] = -(nextStates[i].getStateValue());
 				/* If we find a winning move, use it. */
 				if (stateScores[i] == gameWinValue)
 					return possibleMoves.elementAt(i);
 			}
 			
 			/* Sort moves in descending order by state value, to improve the
 			 * performance of alpha-beta pruning. */
 			for (int i = 1; i < numMoves; i++) {
 				Move curMove = possibleMoves.elementAt(i);
 				State curState = nextStates[i];
 				int curValue = stateScores[i];
 				int j = i;
 				while (j > 0 && stateScores[j] < curValue) {
 					stateScores[j] = stateScores[j-1];
 					nextStates[j] = nextStates[j-1];
 					possibleMoves.set(j, possibleMoves.elementAt(j-1));
 					j--;
 				}
 				stateScores[j] = curValue;
 				nextStates[j] = curState;
 				possibleMoves.set(j, curMove);
 			}			
 			
 			/* Begin an iterative deepening negamax search for the next best move, while
 			 * remaining within the time limit. */
 			int curDepth = 0;
 			searchElapsedTime = (System.nanoTime() - searchStartTime) * 1.0e-9;
 			while (searchElapsedTime < moveTimeLimit) {
 				curBestMoves = new Vector<Move>();
 				curBestValue = -gameWinValue;
 				curDepth++;
 				for (int i = 0; i < numMoves; i++) {
 					stateScores[i] = -(negamax(nextStates[i], curDepth, -gameWinValue, gameWinValue));
 					if (stateScores[i] > curBestValue) {
 						curBestValue = stateScores[i];
 						curBestMoves = new Vector<Move>();
 					}
 					if (stateScores[i] == curBestValue) {
 						curBestMoves.add(possibleMoves.elementAt(i));
 					}
 				}
 				/* Commit new search results. */
 				bestMoves.clear();
 				bestMoves.addAll(curBestMoves);
 				bestValue = curBestValue;
 			}
 			/* Pick a random move from the best options available. */
 			Random generator = new Random();
 			int randomIndex = generator.nextInt(bestMoves.size());
 			bestMove = bestMoves.elementAt(randomIndex);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return bestMove;
 	}
 	
 	public int compareTo(State s) {
 		int myStateValue = getStateValue();
 		int otherStateValue = s.getStateValue();
 		
 		if (myStateValue > otherStateValue)
 			return 1;
 		else if (myStateValue < otherStateValue)
 			return -1;
 		else
 			return 0;
 	}
 	
 	/* Generates a new Zobrist hash for the current State. */
 	private void genHash() {
 		long newHash = 0L;
 		char p = ' ';
 		Square sq = null;
 		try {
 			for (int i = 0; i < num_rows; i++) {
 				for (int j = 0; j < num_columns; j++) {
 					sq = new Square(j,i);
 					p = getPieceAtSquare(sq);
 					newHash = newHash ^ zob.getHash(sq, p);
 				}
 			}
 			if (whiteOnMove()) {
 				newHash = newHash ^ whiteHash;
 			} else {
 				newHash = newHash ^ blackHash;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		hash = newHash;
 	}
 }
