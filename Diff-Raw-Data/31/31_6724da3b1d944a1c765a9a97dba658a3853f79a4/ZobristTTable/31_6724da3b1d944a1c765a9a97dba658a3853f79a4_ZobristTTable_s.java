 package snozama.amazons.mechanics.transtable;
 
 import java.util.Random;
 
 import snozama.amazons.mechanics.Board;
 
 /**
  * A transposition table using Zobrist hashing.
  * 
  * @author Graeme Douglas
  *
  */
 public class ZobristTTable implements TranspositionTable
 {
 	/**
 	 * White amazon index for Zobrist values.
 	 */
 	public static final int WHITE_AMAZON = 0;
 	
 	/**
 	 * Black amazon index for Zobrist values.
 	 */
 	public static final int BLACK_AMAZON = 1;
 	
 	/**
 	 * Arrow index for Zobrist values.
 	 */
 	public static final int ARROW = 2;
 	
 	/**
 	 * Size of the hash table.
 	 */
 	public final int size;
 	
 	/**
 	 * The position of the score within the stored record.
 	 */
 	public static final int POS_INFO = 0;
 	
 	/**
 	 * The position of the depth within the stored record.
 	 */
 	public static final int DEPTH = 1;
 	
 	/**
 	 * Determines whether entry is upper exact, or lower bound.
 	 */
 	public static final int FLAG  = 2;
 	
 	/**
	 * The position of the upper bound within the stored record.
 	 */
 	public static final int SCORE = 3;
 	
 	/**
 	 * The position of the move within the stored record.
 	 */
 	public static final int MOVE  = 4;
 	
 	/**
 	 * The number of collisions that have occurred so far.
 	 */
 	public int collisions;
 	
 	public static final int LOWER_BOUND = -1;
 	public static final int EXACT_SCORE = 0;
 	public static final int UPPER_BOUND = 1;
 	
 	// TODO: Can we make this general to all ZobristTTables?  Does it matter? (should it be static)
 	/**
 	 * Table storing all possible Zobrist values.
 	 */
 	private final int[][][] zobristValues;
 	
 	/**
 	 * The hash table.  The key is the result of the Zobrist hash; the record
 	 * is a long with all the relevant information.
 	 */
 	public int[][] hashTable;
 	
 	/**
 	 * Default constructor for the transposition table.
 	 */
 	public ZobristTTable(int size)
 	{
 		zobristValues = new int[3][10][10];
 		
 		// 
 		Random rand = new Random();
 		//for (int i = 0; i < 10000; i++)
 		//	rand.nextInt();
 		
 		// Create the Zobrist value lookup.
 		for (int t = 0; t < zobristValues.length; t++)
 		{
 			for (int i = 0; i < zobristValues[t].length; i++)
 			{
 				for (int j = 0; j < zobristValues[t][i].length; j++)
 				{
 					zobristValues[t][i][j] = rand.nextInt(Integer.MAX_VALUE);
 				}
 			}
 		}
 		
 		this.size = size;
 		
 		collisions = 0;
 		
 		hashTable = new int[size][5];
 		for (int i = 0; i < hashTable.length; i++)
 		{
 			hashTable[i][DEPTH] = -1;
 		}
 	}
 	
 	/**
 	 * Computes the hash of the board.
 	 * 
 	 * @param board		The board compute the hash of.
 	 * @return			The computed zobrist hash of the board.
 	 */
 	public int computeBoardHash(Board board)
 	{
 		int hashed = 0;
 		
 		int i = 0;
 		int j = 0;
 		
 		// Find first value that needs to be hashed.
 		for (; i < 10; i++)
 		{
 			for (; j < 10; j++)
 			{
 				if (!board.isOccupied(i, j))
 					continue;
 				
 				
 				if (board.isWhite(i, j))
 				{
 					hashed = zobristValues[WHITE_AMAZON][i][j];
 				}
 				else if (board.isBlack(i, j))
 				{
 					hashed = zobristValues[BLACK_AMAZON][i][j];
 				}
 				else
 				{
 					hashed = zobristValues[ARROW][i][j];
 				}
 				
 				if (hashed != 0)
 				{
 					break;
 				}
 			}
 			
 			if (hashed != 0)
 			{
 				j++;
 				break;
 			}
 		}
 		
 		// Proceed as normal.
 		for (; i < 10; i++)
 		{
 			for (; j < 10; j++)
 			{
 				if (board.isWhite(i, j))
 				{
 					hashed ^= zobristValues[WHITE_AMAZON][i][j];
 				}
 				else if (board.isBlack(i, j))
 				{
 					hashed ^= zobristValues[BLACK_AMAZON][i][j];
 				}
 				else if (board.isOccupied(i, j))
 				{
 					hashed ^= zobristValues[ARROW][i][j];
 				}
 			}
 			j = 0;
 		}
 		
 		return hashed;
 	}
 	
 	/**
 	 * Update a generated key based on move values. Can be used to apply
 	 * or undo a move. Note that the garbage in, garbage out principle
 	 * applies.
 	 * 
 	 * @param key		The key to be updated.
 	 * @param colour	The player colour (note, should use static constants
 	 * 					from this class)
 	 * @param row_s		The row that the moved amazon started from.
 	 * @param col_s		The column that the moved amazon started from.
 	 * @param row_f		The row that the moved amazon finished at.
 	 * @param col_f		The column that the moved amazon finished at.
 	 * @param arow		The row the arrow was placed.
 	 * @param acol		The column the arrow was placed in.
 	 * @return			The updated key.
 	 */
 	public int updateHashKeyByMove(int key, int colour, int row_s, int col_s, int row_f, int col_f, int arow, int acol)
 	{
 		// General principle here: garbage in, garbage out.  Don't make bad calls.
 		key ^= zobristValues[colour][row_s][col_s];
 		key ^= zobristValues[colour][row_f][col_f];
 		key ^= zobristValues[ARROW][arow][acol];
 		
 		return key;
 	}
 	
 	/**
 	 * Add entry to transposition table.
 	 * 
 	 * @param board		The board (position) that is to be hashed.
 	 * @param prev_move	The move that produced this board.
 	 * @param depth		The depth of the board to be hashed.
 	 * @param lower		The lower bound used to find the score of the hashed
 	 * 					board.
 	 * @param upper		The upper bound used to find the value of the hashed
 	 * 					board.
 	 * @param move		The {@code MoveManager} encoded move data that represents
 	 * 					the best move for this position.
 	 * @return			{@code true} if the value was added properly,
 	 * 					{@code false} otherwise.
 	 */
 	public boolean put(Board board, int prev_move, int depth, int lower, int upper, int move)
 	{
 		int key = computeBoardHash(board) % size;
 		
 		// For now, we will use the "newest" replacement strategy.
 		if (get(key)[DEPTH] != -1)
 		{
 			collisions++;
 		}
 		
 		hashTable[key][POS_INFO] = prev_move;
 		hashTable[key][DEPTH] = depth;
 		hashTable[key][FLAG] = lower;
 		hashTable[key][SCORE] = upper;
 		hashTable[key][MOVE]  = move;
 		
 		return true;
 	}
 	
 	/**
 	 * Add entry to transposition table.
 	 * 
 	 * @param key		The key of the location to place the value in.
 	 * @param prev_move	The score of the board to be hashed.
 	 * @param depth		The depth of the board to be hashed.
 	 * @param lower		The lower bound used to find the score of the hashed
 	 * 					board.
 	 * @param upper		The upper bound used to find the value of the hashed
 	 * 					board.
 	 * @param move		The {@code MoveManager} encoded move data that
 	 * 					represents the best move for this board.
 	 * @return			{@code true} if the value was added properly,
 	 * 					{@code false} otherwise.
 	 */
 	public boolean put(int key, int prev_move, int depth, int lower, int upper, int move)
 	{
 		key %= size;
 		// For now, we will use the "newest" replacement strategy.
 		if (get(key)[DEPTH] != -1)
 		{
 			collisions++;
 		}
 		
 		hashTable[key][POS_INFO] = prev_move;
 		hashTable[key][DEPTH] = depth;
 		hashTable[key][FLAG] = lower;
 		hashTable[key][SCORE] = upper;
 		hashTable[key][MOVE]  = move;
 		
 		return true;
 	}
 	
 	/**
 	 * Add entry to transposition table.
 	 * 
 	 * @param key		The key of the location to place the value in.
 	 * @param record	The record that is to be stored.
 	 * @return			{@code true} if the value was added properly,
 	 * 					{@code false} otherwise.
 	 */
 	public boolean put(int key, int[] record)
 	{
 		key %= size;
 		// For now, we will use the "newest" replacement strategy.
 		if (get(key)[DEPTH] != -1)
 		{
 			collisions++;
 		}
 		
 		hashTable[key] = record;
 		
 		return true;
 	}
 	
 	/**
 	 * Get an entry from the transposition table, based on hashed key.
 	 * 
 	 * @param key		The key of the entry to retrieve.
 	 * @return			The record of values stored.
 	 */
 	public int[] get(int key)
 	{
 		key %= size;
 		return hashTable[key].clone();
 	}
 	
 	/**
 	 * Get an entry from the transposition table, based on board to be hashed.
 	 * 
 	 * @param board		The board that determines the hash key.
 	 * @return			The record of values stored.
 	 */
 	public int[] get(Board board)
 	{
 		int key = computeBoardHash(board) % size;
 		
 		return hashTable[key].clone();
 	}
 	
 	/**
 	 * Get the number of entries of the transposition table.
 	 * @return
 	 */
 	public int size()
 	{
 		return this.hashTable[0].length;
 	}
 }
