 package snozama.amazons.mechanics.algo;
 
 import java.util.Arrays;
 
 import snozama.amazons.global.GlobalFunctions;
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveManager;
 import snozama.amazons.mechanics.SnozamaHeuristic;
 
 /**
  * Class containing NegaScout search as described in Qian Liang's paper.
  * 
  * @author Cody Clerke
  *
  */
 
 public class NegaScout {
 	
 	public static int POS_INFINITY = Integer.MAX_VALUE-2;
 	public static int NEG_INFINITY = Integer.MIN_VALUE+2;
 	
 	public static int firstN = 500; //for possible use with N-best selection search
 	
 	// Statistical fields.
 	public int nodes = 0;
 	public int depthCompleted;
 	
 	int[] bestMoves = new int[20]; //FIXME hard-coded as 20 for testing
 	int[] scores = new int[2176];
 	
 	long endTime;
 	
 	int currentRoot;
 	
 	public NegaScout(long end)
 	{
 		endTime = end;
 
 		Arrays.fill(scores, NEG_INFINITY);
 		depthCompleted = 0;
 	}
 	
 	/**
 	 * Chooses the best move for Snozama based on search algorithm.
 	 * @param board		The current board position.
 	 * @param colour	The player for whom to find a move for.
 	 * @param turn		The current ply of the game.
 	 * @return			Returns the best move found by the search algorihm.
 	 */
 	public int chooseMove(Board board, int colour, int turn)
 	{
 		int maxDepth = 2;
 		//probably don't need to keep the score?
 		//NegaScoutSearch(board, 0, maxDepth, NEG_INFINITY, POS_INFINITY, colour, turn);
 		//return bestMoves[0];
 		
 		//Iterative deepening NegaScout (plays better than regular fixed depth search)
 		return IDNegaScoutSearch(board, colour, turn);
 	}
 	
 	/**
 	 * NegaScout search algorithm.
 	 * @param board			The current board position.
 	 * @param depth			The starting depth of the search. Should always start at <code>depth = 0</code>.
 	 * @param maxDepth		The maximum depth to be searched.
 	 * @param alpha			The lower bound of the search window.
 	 * @param beta			The upper bound of the search window.
 	 * @param colour		The active player's colour.
 	 * @param turn			The current ply of the game.
 	 * @return				Returns the score of the best move.
 	 */
 	public int NegaScoutSearch(Board board, int depth, int maxDepth, int alpha, int beta, int colour, int turn)
 	{
 		int next = 0;
 		
 		if (depth == maxDepth || board.isTerminal())
 		{
 			int value = SnozamaHeuristic.evaluateBoard(board, colour, turn);
 			
			if (value > scores[currentRoot])
 			{
				scores[currentRoot] = value;
 			}
 			return value;
 		}
 		
 		int score = Integer.MIN_VALUE;
 		int b = beta;
 		MoveManager successors = board.getSuccessors(colour); //generate successors
 		
 		// Move ordering for iterative deepening.
 		if (next == 0 && depth == 0 && maxDepth > 1)
 		{
 			successors.sort(scores);
 		}
 
 		while (successors.hasIterations() && System.currentTimeMillis() < endTime) //for each move or until turn time runs out
 		{
 			next = successors.nextIterableIndex();
 			
 			if (depth == 0)
 			{
 				currentRoot = next;
 				scores[currentRoot] = alpha;
 			}
 			
 			int row_s = Board.decodeAmazonRow(board.amazons[colour][successors.getAmazonIndex(next)]);
 			int col_s = Board.decodeAmazonColumn(board.amazons[colour][successors.getAmazonIndex(next)]);
 			successors.applyMove(board, next); //execute current move
 			nodes++;
 			
 			int current = -NegaScoutSearch(board, depth+1, maxDepth, -b, -alpha, GlobalFunctions.flip(colour), turn+1);
 			
 			if (current > score)
 			{
 				if (b == beta || maxDepth - depth <= 2)
 					score = current;
 				else
 					score = -NegaScoutSearch(board, depth+1, maxDepth, -beta, -current, GlobalFunctions.flip(colour), turn+1); //re-search
 			}
 			
 			if (score > alpha)
 			{
 				alpha = score; //adjust the search window
 				
 				bestMoves[depth] = successors.getMove(next);
 			}			
 			
 			successors.undoMove(board, next, row_s, col_s); //retract current move
 			
 			if (alpha >= beta)
 			{
 				return alpha; //cut off
 			}
 			
 			// Update scores array.
 			scores[currentRoot] = score;
 			
 			b = alpha + 1;
 		}
 		return score;
 	}
 	
 	/**
 	 * Iterative deepening NegaScout search.
 	 * @param board		The current board position.
 	 * @param colour	The active player's colour.
 	 * @param turn		The current ply of the game.
 	 * @return		Returns the best move found for the current turn from the deepest fully searched depth.
 	 */
 	public int IDNegaScoutSearch(Board board, int colour, int turn)
 	{
 		int depth = 1;
 		int[] bestScore = new int[20];	// Really an array of best moves at a given depth.  TODO: Rename.
 		while (depth <= 20 && System.currentTimeMillis() < endTime)
 		{
 			NegaScoutSearch(board, 0, depth, NEG_INFINITY, POS_INFINITY, colour, turn);
 			bestScore[depth-1] = bestMoves[0]; //store best move for each depth
 			depthCompleted = depth;
 			depth++;
 		}
 		boolean found = false;
 		for (int i = bestScore.length-1; i >= 0; i--)
 		{
 			if (found)
 				return bestScore[i]; //return best move for deepest complete search
 			else if (bestScore[i] != 0) //first non-zero result will be best move for deepest partially complete search
 				found = true;
 		}
 		return -1; //no move found, ERROR!
 	}
 }
