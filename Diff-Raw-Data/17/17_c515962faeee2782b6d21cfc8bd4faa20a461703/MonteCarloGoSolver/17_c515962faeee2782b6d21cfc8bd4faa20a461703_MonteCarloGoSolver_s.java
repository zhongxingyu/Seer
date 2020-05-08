 package player_utils;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import board_utils.GoCell;
 import board_utils.GoPlayingBoard;
 import custom_java_utils.CheckFailException;
 
 public class MonteCarloGoSolver {
 	private GoPlayingBoard board;
 	private GoCell cellToCapture;
 	private static final long infinity = Integer.MAX_VALUE;
 	private int gamesPerMove;
 	private int gamesPerThreadRun;
 	private long finishTime;
 	private Integer countGamesPlayed = 0;
 	private ArrayList<CellValuePair> monteCarloValues;
 	ArrayList<GoPlayingBoard> legalMoves;
 	private static final int THREAD_COUNT = 2;
 	
 	/**
 	 * Simple constructor to make a decision by using the MonteCarlo method. 
 	 * The default number of games per initial legal moves is 100 and the default 
 	 * number of games played by each thread at a time is 10.
 	 * @param board the starting position
 	 * @param cell the nominated victim cell
 	 */
 	public MonteCarloGoSolver(GoPlayingBoard board, GoCell cell) {
 		this.board = board.clone();
 		this.cellToCapture = cell.clone();
 		this.gamesPerMove = 100;
 		this.gamesPerThreadRun = 10;
 		this.finishTime = -1;
 	}
 	
 	/**
 	 * A constructor to limit the number of games played in total for every legal move.
 	 * The default number of games played by each thread per initial legal move is 10.
 	 * @param board the starting position
 	 * @param cell the nominated victim cell
 	 * @param gamesPerMove how may games should be played per legal move
 	 */
 	public MonteCarloGoSolver(GoPlayingBoard board, GoCell cell, int gamesPerMove) {
		this.board = board.clone();
		this.cellToCapture = cell.clone();
 		this.gamesPerMove = gamesPerMove;
		this.gamesPerThreadRun = 10;
		this.finishTime = -1;
 	}
 	
 	/**
 	 * A constructor to limit the amount of time MonteCarlo runs for
 	 * @param board the starting position
 	 * @param cell the nominated victim cell
 	 * @param gamesPerThreadRun the number of games to be played for each 
 	 * legal move by each thread
 	 * @param finishTime the system time in milliseconds when the decision 
 	 * should be retrieved
 	 */
 	public MonteCarloGoSolver(GoPlayingBoard board, GoCell cell, 
 			int gamesPerThreadRun, long finishTime) {
		this.board = board.clone();
		this.cellToCapture = cell.clone();
 		this.gamesPerThreadRun = gamesPerThreadRun;
 		this.gamesPerMove = -1;
 		this.finishTime = finishTime;
 	}
 	
 	private class CellValuePair implements Comparable<CellValuePair>{
 		public GoCell cell;
 		public double value;
 		
 		@Override
 		public int compareTo(CellValuePair other) {
 			return Double.compare(this.value, other.value);
 		}
 	}
 	
 	public boolean isGoalAchieved(GoPlayingBoard board) {
 		if (board.getCellAt(cellToCapture.getVerticalCoordinate(), 
 				cellToCapture.getHorizontalCoordinate()).isEmpty()) {
 			// the target stone was captured
 			return true;
 		}
 		// The algorithm itself checks if there are any further legal moves. There is
 		// no need to duplicate this here
 		return false;
 	}
 	
 	/**
 	 * Calculates what the best next move would be
 	 * @return the cell that is the best move according to the algorithm
 	 * @throws CheckFailException
 	 * @throws InterruptedException 
 	 */
 	public GoCell decision() throws CheckFailException, InterruptedException {
 		LegalMovesChecker checker = new LegalMovesChecker(board);
 		monteCarloValues = new ArrayList<MonteCarloGoSolver.CellValuePair>();
 		legalMoves = new ArrayList<GoPlayingBoard>();
 		// Find all initially legal moves
 		for (int i = 0; i < board.getWidth(); i++) {
 			for (int j = 0; j < board.getHeight(); j++) {
 				GoCell cell = new GoCell(board.toPlayNext(), i, j);
 				if (checker.isMoveLegal(cell)) {
 					CellValuePair cellValuePair = new CellValuePair();
 					cellValuePair.cell = cell;
 					GoPlayingBoard newBoard = checker.getNewBoard();
 					newBoard.oppositeToPlayNext();
 					//cellValuePair.value = monteCarloEvaluation(newBoard);
 					monteCarloValues.add(cellValuePair);
 					legalMoves.add(newBoard);
 					BoardHistory.getSingleton().remove(newBoard);
 				}
 				checker.reset();
 			}
 		}
 		
 		// Start threads to play random games.
 		ArrayList<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < THREAD_COUNT; i++) {
 			Thread t = new Thread(new MonteCarloInstanceRunner());
 			threads.add(t);
 			t.start();
 		}
 		System.out.println("Running with " + Thread.activeCount() + " threads");
 		for (Thread t : threads) {
 			t.join();
 		}
 		
 		GoCell bestMove = null;
 		double bestValue = (-infinity);
 		for (CellValuePair pair : monteCarloValues) {
 			if (pair.value > bestValue) {
 				bestValue = pair.value;
 				bestMove = pair.cell;
 			}
 			System.out.println(pair.cell.getVerticalCoordinate() + "," + 
 					pair.cell.getHorizontalCoordinate() + " -> " + pair.value);
 		}
 		return bestMove;
 	}
 	
 	/**
 	 * A method to play random games from a certain point, returning the number
 	 * of games won.
 	 * @param board the starting point of the algorithm
 	 * @param pair the corresponding CellValue pair that holds the value for the current move
 	 * @return the win ratio for the given starting position
 	 * @throws CheckFailException
 	 */
 	private void monteCarloEvaluation(GoPlayingBoard board, CellValuePair pair) 
 			throws CheckFailException {
 		int winCount = 0;
 		int gamesToPlay = gamesPerThreadRun;
 		// Play that many random games
 		for (;gamesToPlay > 0; gamesToPlay--) {
 			// Keep a record of all the board positions that were played, so we can later
 			// remove them from the history for the next iteration
 			ArrayList<GoPlayingBoard> boardsPlayed = new ArrayList<GoPlayingBoard>();
 			GoPlayingBoard newBoard = board.clone();
 			while (!isGoalAchieved(newBoard)) {
 				LegalMovesChecker checker = new LegalMovesChecker(newBoard);
 				// Find all legal moves for the current board
 				ArrayList<GoCell> legalMoves = new ArrayList<GoCell>();
 				for (int i = 0; i < newBoard.getWidth(); i++) {
 					for (int j = 0; j < newBoard.getHeight(); j++) {
 						GoCell currCell = new GoCell(newBoard.toPlayNext(), i, j);
 						if (checker.isMoveLegal(currCell)) {
 							legalMoves.add(currCell);
 						}
 						checker.reset();
 					}
 				}
 				// No more legal moves => this is a terminal state
 				if (legalMoves.isEmpty()) {
 					break;
 				}
 				// Pick a random move to be played next
 				Random randomGenerator = new Random();
 				checker.isMoveLegal(legalMoves.get(randomGenerator.nextInt(legalMoves.size())));
 				newBoard = checker.getNewBoard();
 				newBoard.oppositeToPlayNext();
 				boardsPlayed.add(newBoard);
 			}
 			if (newBoard.getCellAt(cellToCapture.getVerticalCoordinate(), 
 					cellToCapture.getHorizontalCoordinate()).isEmpty()) {
 				winCount++;
 			}
 			// Remove all played boards from history
 			for (GoPlayingBoard playedBoard : boardsPlayed) {
 				BoardHistory.getSingleton().remove(playedBoard);
 			}
 		}
 		synchronized(monteCarloValues) {
 			pair.value += winCount;
 		}
 	}
 	
 	/**
 	 * A simple Runnable class to iterate over all initial legal moves
 	 * and play a number of games each. Each instance of this class is to
 	 * be run as a separate thread.
 	 */
 	private class MonteCarloInstanceRunner implements Runnable {
 		public MonteCarloInstanceRunner() {
 		}
 
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					// Iterate over all initial legal moves and play a number of games for each
 					for (int i = 0; i < legalMoves.size(); i++) {
 						monteCarloEvaluation(legalMoves.get(i), monteCarloValues.get(i));
 					}
 					// Check if conditions hold for another loop to be performed.
 					synchronized (countGamesPlayed) {
 						countGamesPlayed += gamesPerThreadRun;
 						if (gamesPerMove > 0 && countGamesPlayed > gamesPerMove) {
 							break;
 						}
						if (finishTime > 0 && System.currentTimeMillis() > finishTime) {
							break;
						}
 					}
 				}
 			} catch (CheckFailException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 			// Wipe history for the current thread.
 			BoardHistory.wipeHistory();
 		}
 
 	}
 }
