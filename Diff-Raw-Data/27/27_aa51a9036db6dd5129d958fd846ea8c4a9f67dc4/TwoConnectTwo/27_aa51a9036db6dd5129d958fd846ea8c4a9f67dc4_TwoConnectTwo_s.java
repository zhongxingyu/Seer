 /**
  * @author Steve Pennington and Julie Sparrow
  *
  */
 public class TwoConnectTwo {
 
 	private static final int BRUTE_FORCE_DEPTH = 17;
 	private static final int START_DEPTH = 6;
 	private static final int MILLIS_IN_MIN = 60000;
 	private static final int MILLIS_IN_SEC = 1000;
 	private static final int MAX_TIME = 3 * MILLIS_IN_MIN;
	private static final int TIME_BUFFER = 30 * MILLIS_IN_SEC;
 	
 	/**
 	 * Main entry point into the application
 	 * @param args
 	 * 		command line arguments
 	 */
 	public static void main(String[] args) {
 		long startTime = System.currentTimeMillis();
 		Move move = null;
 		
 		IO.init(System.in);
 		GameState gameState = IO.parseGameState();
 		Board board = IO.parseBoard(gameState.numCols, gameState.numRows);
 		IO.close();
 		
 		TimeoutThread timeout = new TimeoutThread();
		timeout.setTimeout(MAX_TIME - TIME_BUFFER);
 		timeout.setBoard(board.clone());
 		timeout.start();
 		
 		int theirTime = gameState.gameTime - gameState.playerOneTime;
 		boolean takeMax = theirTime >= 10*MILLIS_IN_MIN;
 		int piecesRemaining = board.getWidth() * board.getHeight() - board.getPieceCount();
 		int timeAllowed = takeMax ? MAX_TIME - TIME_BUFFER : (MILLIS_IN_MIN * 10 - gameState.playerOneTime) / (piecesRemaining>>1);
 		
 		Evaluator evaluator = AdvancedChainEvaluator.getInstance();
 		
 		if(piecesRemaining <= BRUTE_FORCE_DEPTH) {
 			move = AI.minMax(board, piecesRemaining, WinEvaluator.getInstance());
 		} else {
 			int depth = START_DEPTH;
 			long taken = System.currentTimeMillis() - startTime;
			for(; taken * 4 < timeAllowed && depth < piecesRemaining; depth++) {
 				System.err.println("calyspo-" + depth);
 				move = AI.minMax(board, depth, evaluator);
 				System.err.println("(" + (move.column + 1)+ ", " + IO.pieceMap[move.piece] + "): score=" + move.score);
 				timeout.setMove(move);
 				taken = System.currentTimeMillis() - startTime;
 				if(move.isWin && move.score < 0) {
 					break;
 				}
 			}
 		}
 		
 		System.out.println("(" + (move.column + 1)+ ", " + IO.pieceMap[move.piece] + ")");
 		timeout.kill();
 		try {
 			timeout.join();
 		} catch (InterruptedException e) {
 			//don't care
 		}
 	}
 	
 	static class TimeoutThread extends Thread {
 		private Move move;
 		private long timeout;
 		private boolean kill = false;
 		private Board board;
 		
 		public void run() {
 			long start = System.currentTimeMillis();
 			
 			while(System.currentTimeMillis() - start < timeout && !kill) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					//do nothing
 				}
 			}
 			
 			if(!kill) {
 				System.err.println("Timeout");
 				if(move == null) {
					move = AI.minMax(board, 4, ConsecutivePieceEvaluator.getInstance());
 				}
 				System.out.println("(" + (move.column + 1)+ ", " + IO.pieceMap[move.piece] + ")");
 			}
 		}
 		
 		public void setBoard(Board board) {
 			this.board = board;
 		}
 
 		public void setTimeout(long timeout) {
 			this.timeout = timeout;
 		}
 		
 		public void setMove(Move move) {
 			this.move = move;
 		}
 		
 		public void kill() {
 			this.kill = true;
 		}
 	}
 }
