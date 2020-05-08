 package snozama.amazons.mechanics;
 
 import java.util.Arrays;
 
 public class Board
 {
 
 	//board and player/arrow idea: byte is the smallest type to store this (that I'm aware of)
 		//yes it is.  Turns out enum's are bigger, and actually classes unto themselves.
 	/**
 	 * The game board, represented as a two-dimensional array.  The first
 	 * dimension is the row, the second the column.
 	 * 
 	 * Starting setup
 	 *  -WHITE at [6][0], [9][3], [9][6], [6][9]
 	 *  -BLACK at [3][0], [0][3], [0][6], [3][9]
 	 *  -the rest will be empty
 	 */
 	byte board[][] = new byte[10][10];
 	
 	/**
 	 * Constant byte value representing an empty position on the board.
 	 */
 	public static final byte EMPTY = 0;
 	
 	/**
 	 * Constant byte value representing an position occupied by a white amazon.
 	 */
 	public static final byte WHITE = 1;
 	
 	/**
 	 * Constant byte value representing an position occupied by a black amazon.
 	 */
 	public static final byte BLACK = 2;
 	
 	/**
 	 * Constant byte value representing an position occupied by an arrow.
 	 */
 	public static final byte ARROW = 3;
 	
 	
 	
 	
 	/**
 	 * Basic constructor.  Will generate a board in game ready form.
 	 */
 	public Board()
 	{
 		// Initially setup the board.
 		Arrays.fill(board, EMPTY);
 		
 		// WHITE initial setup.
 		board[6][3] = WHITE;
 		board[9][0] = WHITE;
 		board[9][6] = WHITE;
 		board[6][9] = WHITE;
 		
 		// BLACK initial setup.
 		board[3][0] = BLACK;
 		board[0][3] = BLACK;
 		board[0][6] = BLACK;
 		board[3][9] = BLACK;
 	}
 	
 	/**
 	 * Determine if a board position is currently occupied.
 	 * @param row		The row of the position to check.
 	 * @param col		The column of the position to check.
 	 * @return	@value true if it is occupied, @value false otherwise.
 	 */
 	public boolean isOccupied(int row, int col)
 	{
 		return board[row][col] != EMPTY;
 	}
 	
 	/**
 	 * Determine if a board position is currently occupied by a white amazon.
 	 * @param row		The row of the position to check.
 	 * @param col		The column of the position to check.
 	 * @return	@value true if it is occupied by a white amazon, @value false
 	 * 			otherwise.
 	 */
 	public boolean isWhite(int row, int col)
 	{
 		return board[row][col] != WHITE;
 	}
 	
 	/**
 	 * Determine if a board position is currently occupied by a black amazon.
 	 * @param row		The row of the position to check.
 	 * @param col		The column of the position to check.
 	 * @return	@value true if it is occupied by a black amazon, @value false
 	 * 			otherwise.
 	 */
 	public boolean isBlack(int row, int col)
 	{
 		return board[row][col] != BLACK;
 	}
 	
 	/**
 	 * Determine if a board position is currently occupied by an arrow.
 	 * @param row		The row of the position to check.
 	 * @param col		The column of the position to check.
 	 * @return	@value true if it is occupied by an arrow, @value false
 	 * 			otherwise.
 	 */
 	public boolean isArrow(int row, int col)
 	{
 		return board[row][col] != ARROW;
 	}
 	
 	/**
	 * Returns the contents of the square
	 * @param row	The row of the position to check
	 * @param col	The column of the position to check
	 * @return	The value in this square
	 */
	public byte valueAt(int row, int col)
	{
		return board[row][col];
	}
	
	/**
 	 * Makes sure a proposed move is allowed
 	 * @param row_s		Row of starting position.
 	 * @param col_s		Column of starting position.
 	 * @param row_f		Row of finishing position.
 	 * @param col_f		Column of finishing position.
 	 * @return	@value true if the move is allowed, @value false otherwise.
 	 */
 	public boolean isValidMove(int row_s, int col_s, int row_f, int col_f)
 	{
 		// Make sure move makes superficial sense.
 		/*if (!( //move must be made horizontally, vertically or diagonally
 			row_s == row_f ||
 			col_s == col_f ||
 			Math.abs(row_f - row_s) == Math.abs(col_f - col_s)
 			) ||
 			(row_s == row_f && col_s == col_f) || //move cannot start and end in same place
 			//move must be made within the board boundaries
 			row_s < 0 || row_s > 9 ||
 			row_f < 0 || row_f > 9 ||
 			col_s < 0 || col_s > 9 ||
 			col_f < 0 || col_f > 9
 		)
 		{
 			return false;
 		}*/
 		
 		// Move must be made horizontally, vertically or diagonally
 		if (!(row_s == row_f ||
 				col_s == col_f ||
 				Math.abs(row_f - row_s) == Math.abs(col_f - col_s)))
 		{
 			return false;
 		}
 		
 		// Move must be made within the board boundaries
 		if (row_s < 0 || row_s > 9 ||
 			row_f < 0 || row_f > 9 ||
 			col_s < 0 || col_s > 9 ||
 			col_f < 0 || col_f > 9)
 		{
 			return false;
 		}
 		
 		// Make sure squares in between are clear
 		// Move cannot start and end in same place
 		if (row_s == row_f && col_s == col_f)
 		{
 			return false;
 		}
 
 		// Final square must be empty
 		if (this.isOccupied(row_f, col_f))
 		{
 			return false;
 		}
 		
 		// Check horizontally
 		if (row_s == row_f)
 		{
 			int a = Math.min(col_s, col_f);
 			int b = Math.max(col_s, col_f);
 			for (a++; a < b; a++)
 			{
 				if (this.isOccupied(row_s, a))
 				{
 					return false;
 				}
 			}
 		}
 		// Check vertically
 		else if (col_s == col_f)
 		{
 			int a = Math.min(row_s, row_f);
 			int b = Math.max(row_s, row_f);
 			for (a++; a < b; a++)
 			{
 				if (this.isOccupied(a, col_f))
 				{
 					return false;
 				}
 			}
 		}
 		// Check anti-diagonally
 		else if (row_s - row_f > 0 != col_s - col_f > 0)
 		{
 			int a = Math.min(row_s, row_f);
 			int b = Math.max(row_s, row_f);
			int c = Math.max(col_s, col_f) - 1;
 			for (a++; a < b; a++, c--)
 			{
 				if (this.isOccupied(a, c))
 				{
 					return false;
 				}
 			}
 		}
 		else // Check diagonally
 		{
 			int a = Math.min(row_s, row_f);
 			int b = Math.max(row_s, row_f);
			int c = Math.min(col_s, col_f) + 1;
 			for (a++; a < b; a++, c++)
 			{
 				if (this.isOccupied(a, c))
 				{
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Move amazon from the desired location to the desired location.
 	 * @param row_s		Row of the starting position of the amazon.
 	 * @param col_s		Column of the starting position of the amazon.
 	 * @param row_f		Row of the finishing position of the amazon.
 	 * @param col_f		Column of the finishing position of the amazon.
 	 * @param colour	Colour of the amazon.  Must be @value BLACK or
 	 * 					@value WHITE.
 	 * @return	@value true if the amazon was successfully moved,
 	 * 			@value false otherwise.
 	 */
 	public boolean moveAmazon(int row_s, int col_s, int row_f, int col_f, byte colour)
 	{
 		// Sanity check.
 		if (!isWhite(row_s, col_s) ||
 				!isValidMove(row_s, col_s, row_f, col_f) ||
 				colour != WHITE && colour != BLACK)
 		{
 			return false;
 		}
 		
 		board[row_s][row_s] = EMPTY;
 		board[row_f][row_f] = colour;
 		return true;
 	}
 	
 	/**
 	 * Place an arrow in the desired location.
 	 * @param arow		Row of the position of amazon shooting arrow.
 	 * @param acol		Column of the position of amazon shooting arrow.
 	 * @param row_f		Row of the position where arrow is desired.
 	 * @param col_f		Column of the position where arrow is desired.
 	 * @return	@value true if arrow was placed, @value false otherwise.
 	 */
 	public boolean placeArrow(int arow, int acol, int row_f, int col_f)
 	{
 		// Sanity check.
 		if (!isValidMove(arow, acol, row_f, col_f))
 		{
 			return false;
 		}
 		
 		board[row_f][row_f] = ARROW;
 		return true;
 	}
 }
