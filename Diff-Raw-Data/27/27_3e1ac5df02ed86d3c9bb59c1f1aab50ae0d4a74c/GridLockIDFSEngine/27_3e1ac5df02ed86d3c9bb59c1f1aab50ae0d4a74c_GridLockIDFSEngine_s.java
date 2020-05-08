 package engine;
 
 import gridlock.GridLockProblem;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import api.GPSProblem;
 
 public class GridLockIDFSEngine extends GPSEngine {
 
 	private Set<GPSNode> visited = new HashSet<GPSNode>();
 	private int currentMaxDepth;
 
 	@Override
 	public void engine(GPSProblem problem, SearchStrategy strategy) {
 		((GridLockProblem) problem).invertRules();
 		visited.clear();
		currentMaxDepth = 1;
 		super.engine(problem, strategy);
 	}
 
 	@Override
 	public void addNode(GPSNode node) {
 		if (!visited.contains(node)) {
 			((LinkedList<GPSNode>) open).addFirst(node);
 			visited.add(node);
 		}
 	}
 
 	@Override
 	protected boolean explode(GPSNode node) {
 		if (node.getDepth() >= currentMaxDepth) {
 			open.clear();
 			visited.clear();
			open.add(new GPSNode(problem.getInitState(), 0,0));
			currentMaxDepth++;
 			return true;
 		}
 		return super.explode(node);
 	}
 
 	@Override
 	public String getStrategyName() {
 		return "Iterative DFS";
 	}
 
 }
