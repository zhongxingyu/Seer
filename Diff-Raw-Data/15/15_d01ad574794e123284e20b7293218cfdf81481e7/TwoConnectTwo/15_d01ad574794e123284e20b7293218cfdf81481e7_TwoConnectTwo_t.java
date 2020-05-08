 /**
  * @author Steve Pennington and Julie Sparrow
  *
  */
 public class TwoConnectTwo {
 
 	private static final int BRUTE_FORCE_DEPTH = 17;
 	private static final int START_DEPTH = 6;
 	private static final int MILLIS_IN_MIN = 60000;
 	private static final int MILLIS_IN_SEC = 1000;
 	private static final int IDEAL_TIME = 12 * MILLIS_IN_SEC;
 	private static final int MAX_TIME = 3 * MILLIS_IN_MIN;
 	private static final int TIME_BUFFER = 5 * MILLIS_IN_SEC;
 	
 	/**
 	 * Main entry point into the application
 	 * @param args
 	 * 		command line arguments
 	 */
 	public static void main(String[] args) {
 		long startTime = System.currentTimeMillis();
 		IO.init(System.in);
 		GameState gameState = IO.parseGameState();
 		Board board = IO.parseBoard(gameState.numCols, gameState.numRows);
 		IO.close();
 		
		int theirTime = gameState.gameTime - gameState.playerOneTime;
 		
 		boolean takeMax = theirTime >= 10*MILLIS_IN_MIN;
 		int timeAllowed = takeMax ? MAX_TIME - TIME_BUFFER : IDEAL_TIME;
 		
 		Move move = null;
 		int piecesRemaining = board.getWidth() * board.getHeight() - board.getPieceCount();
 		if(piecesRemaining <= BRUTE_FORCE_DEPTH) {
 			Evaluator evaluator = WinEvaluator.getInstance();
 			move = AI.minMax(board, piecesRemaining, evaluator);
 		} else {
 			int depth = START_DEPTH;
 			long taken = System.currentTimeMillis() - startTime;
 			Evaluator evaluator = ChainEvaluator.getInstance();
 			for(; taken * 10 < timeAllowed; depth++){
 				System.err.println("calyspo-" + depth);
 				move = AI.minMax(board, depth, evaluator);
 				taken = System.currentTimeMillis() - startTime;
				if(move.score > 30) {
 					System.err.println("calypso getting in trouble");
 					depth++;
 					evaluator = ConsecutivePieceEvaluator.getInstance();
 					timeAllowed = Math.max(timeAllowed, MILLIS_IN_MIN << 1);
 				}
 			}
 		}
 		
 		if(takeMax) {
 			try {
 				System.err.println("Sleeping to win");
 				Thread.sleep(MAX_TIME - (System.currentTimeMillis() - startTime) - TIME_BUFFER);
 			} catch (InterruptedException e) {
 				//do nothing
 			}
 		}
 		
 		System.out.println("(" + (move.column + 1)+ ", " + IO.pieceMap[move.piece] + ")");
 	}
 }
