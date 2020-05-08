 package workflow;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import solver.Solver;
 import model.Cell;
 import model.Game;
 import model.Solution;
 
 public class WebInteractionImpl implements WebInteraction {
 	
 	private static final Logger LOG = Logger.getLogger(WebInteractionImpl.class.getName());
 	
 	private final Set<Cell> unsolvedGame = new TreeSet<Cell>();
 	private Solution solution;
 	private Iterator<Cell> iter;
 	private long elapsedTime;
 

 	@Override
 	public boolean addCellOfUnsolvedGame(int col, int row, String valueAsString) {
 		if (valueAsString != "") {
 			try {
 				int value = Integer.valueOf(valueAsString);
 				unsolvedGame.add(new Cell(col, row, Integer.valueOf(value)));
 				return true;
 			} catch (IllegalArgumentException e) {
 				LOG.severe("Could not add cell of unsolved game: " + e);
 				return false;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void solveIt() throws Exception {
 		LOG.info("about to solve game: " + unsolvedGame);
 		Game game = new Game() {
 
 			@Override
 			public Set<Cell> getUnsolvedGame() {
 				return unsolvedGame;
 			}
 			
 		};
 		
 		Solver solver = new Solver(game, true);
 		solver.timedSolveIt();
 		solution = solver.getSolution();
 		LOG.info("solution found: " + solution);
 		if (solution==null) {
 			throw new NoSolutionFound("No solutions for this game!");
 		}
 		elapsedTime = solver.getElapsed();
 		iter = solution.getSolution().iterator();
 	}
 	
 	@SuppressWarnings("serial")
 	public static class NoSolutionFound extends Exception {
 		
 		public NoSolutionFound(String msg) {
 			super(msg);
 		}
 	}
 
 	@Override
 	public int getNextCellValueFromSolvedGame() {
 		if (iter.hasNext()) {
 			return iter.next().getValue();
 		} else {
 			throw new NoMoreCellsException();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	static class NoMoreCellsException extends IllegalStateException {
 		
 	}
 
 	@Override
 	public long getElapsedTime() {
 		return elapsedTime;
 	}
 
 }
