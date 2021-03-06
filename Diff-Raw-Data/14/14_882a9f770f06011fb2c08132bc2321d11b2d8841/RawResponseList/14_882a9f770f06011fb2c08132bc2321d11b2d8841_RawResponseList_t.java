 package orego.response;
 
 /**
  * This object stores information about this move, and potentially stores
  * information about moves made with a relevant history.
  */
 
 import orego.core.Board;
 import orego.core.Coordinates;
 import orego.util.IntSet;
 import ec.util.MersenneTwisterFast;
 
 /**
  * This object stores information about the past success of moves. It is usually
  * (externally) associated with some context, such as a 2-move history.
  */
 public class RawResponseList extends AbstractResponseList {
 
 	protected int[] wins;
 	protected int[] runs;
 	private long totalRuns;
 
 	public RawResponseList() {
 		wins = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
 		runs = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
 		for (int p: Coordinates.ALL_POINTS_ON_BOARD) {
 			wins[p] = NORMAL_WINS_PRIOR;
 			runs[p] = NORMAL_RUNS_PRIOR;
 		}
 		wins[Coordinates.PASS] = PASS_WINS_PRIOR;
 		runs[Coordinates.PASS] = PASS_RUNS_PRIOR;
		totalRuns = NORMAL_RUNS_PRIOR * Coordinates.BOARD_AREA;
 	}
 
 	// TODO: these array getters are only used in tests
 	// could probably remove and just use move-specific getters
 	protected int[] getWins() {
 		return wins;
 	}
 
 	protected void setWins(int[] wins) {
 		this.wins = wins;
 	}
 
 	protected int[] getRuns() {
 		return runs;
 	}
 
 	protected void setRuns(int[] runs) {
 		this.runs = runs;
 	}
 
 	public long getTotalRuns() {
 		return totalRuns;
 	}
 
 	protected void setTotalRuns(int totalRuns) {
 		this.totalRuns = totalRuns;
 	}
 
 	/**
 	 * Add a win and run to this move.
 	 */
 	public void addWin(int p) {
 		// TODO Should we replace the first four lines with a call to addLoss()?
 		// It's not clear if the clarity would be worth the extra function call
 		runs[p]++;
 		assert runs[p] > 0 : "runs overflowed";
 		totalRuns++;
 		assert totalRuns > 0 : "totalRuns overflowed";
 		wins[p]++;
 	}
 
 	/**
 	 * Add a run to this move.
 	 */
 	public void addLoss(int p) {
 		runs[p]++;
 		assert runs[p] > 0 : "runs overflowed";
 		totalRuns++;
 		assert totalRuns > 0 : "totalRuns overflowed";
 	}
 
 	public int getWins(int p) {
 		return wins[p];
 	}
 
 	public int getRuns(int p) {
 		return runs[p];
 	}
 
 	public double getWinRate(int p) {
 		return wins[p] / (double)runs[p];
 	}
 
 	public int bestMove(Board board, MersenneTwisterFast random) {
 		IntSet vacantPoints = board.getVacantPoints();
 		int start = random.nextInt(vacantPoints.size());
 		int i = start;
 		double bestValue = PASS_WINS_PRIOR / (double) PASS_RUNS_PRIOR;
 		int bestMove = Coordinates.PASS;
 		do {
 			int move = vacantPoints.get(i);
 			double searchValue = getWinRate(move);
 			if (searchValue > bestValue) {
 				if (board.isFeasible(move) && board.isLegal(move)) {
 					bestValue = searchValue;
 					bestMove = move;
 				}
 			}
 			i = (i + 457) % vacantPoints.size();
 		} while (i != start);
 		return bestMove;
 	}
 
 }
