 package gps.impl;
 
 import gps.api.GPSProblem;
 import gps.api.SearchStrategy;
 import gps.api.StatsHolder;
 
 public class IDEngine extends DFSEngine {
 
 	private int depth = 0;
 
 	public IDEngine(GPSProblem problem, SearchStrategy strategy) {
 		super(problem, strategy);
 	}
 
 	@Override
 	public boolean engine(GPSProblem myProblem, SearchStrategy myStrategy,
 			StatsHolder holder) {
 		boolean solutionFound = false;
 		while (!solutionFound) {
 			holder.resetStats();
 			solutionFound = super.engine(myProblem, myStrategy, holder);
			depth++;
 		}
 		return solutionFound;
 	}
 
 	@Override
 	protected boolean explode(GPSNode node) {
 		if (node.getDepth() == depth) {
 			return false;
 		}
 		return super.explode(node);
 	}
 
 }
