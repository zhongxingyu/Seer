 package engine;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import api.GPSProblem;
 import api.GPSRule;
 import api.GPSState;
 import exception.NotAppliableException;
 import gridlock.GridLockState;
 
 public abstract class GPSEngine {
 
 	protected List<GPSNode> open;
 
	private List<GPSNode> closed;
 
 	protected GPSProblem problem;
 
 	public GPSEngine() {
 		open = new LinkedList<GPSNode>();
 		closed = new ArrayList<GPSNode>();
 	}
 
 	// Use this variable in the addNode implementation
 	private SearchStrategy strategy;
 
 	public void engine(GPSProblem myProblem, SearchStrategy myStrategy) {
 		problem = myProblem;
 		strategy = myStrategy;
 
 		GPSNode rootNode = new GPSNode(problem.getInitState(), 0, 0);
 		boolean finished = false;
 		boolean failed = false;
 		long explosionCounter = 0;
 
 		open.add(rootNode);
 		long startTime = System.nanoTime();
 		while (!failed && !finished) {
 			if (open.size() <= 0) {
 				failed = true;
 			} else {
 				GPSNode currentNode = open.get(0);
 				closed.add(currentNode);
 				open.remove(0);
 				if (isGoal(currentNode)) {
 					finished = true;
 					System.out.println(currentNode.getSolution());
 					System.out.println("Expanded nodes: " + explosionCounter);
 					System.out.println("Depth: " + currentNode.getDepth());
 					System.out.println("Frontier nodes: " + open.size());
 					System.out.println("Total States: " + open.size() + closed.size());
 					long endTime = System.nanoTime();
 					long duration = endTime - startTime;
 					System.out.println("Processing Time: " + (duration/1000000000.0) + " sec");
 				} else {
 					explosionCounter++;
 					explode(currentNode);
 				}
 			}
 		}
 
 		if (finished) {
 			System.out.println("OK! solution found!");
 		} else if (failed) {
 			System.err.println("FAILED! solution not found!");
 		}
 	}
 
 	private boolean isGoal(GPSNode currentNode) {
 		return currentNode.getState() != null
 				&& currentNode.getState().isGoalState();// compare(problem.getGoalState());
 	}
 
 	protected boolean explode(GPSNode node) {
 		if (problem.getRules() == null) {
 			System.err.println("No rules!");
 			return false;
 		}
 
 		for (GPSRule rule : problem.getRules()) {
 			GPSState newState = null;
 			try {
 				newState = rule.evalRule(node.getState());

 			} catch (NotAppliableException e) {
 				// Do nothing
 			}
 			if (newState != null
 					&& !checkBranch(node, newState)
 					&& !checkOpenAndClosed(node.getCost() + rule.getCost(),
 							newState)) {
 				GPSNode newNode = new GPSNode(newState, node.getCost()
 						+ rule.getCost(), node.getDepth() + 1);
 				newNode.setParent(node);
 				// ((GridLockState) newNode.getState()).getBoard().printBoard();
 				addNode(newNode);
 			}
 		}
 		return true;
 	}
 
 	private boolean checkOpenAndClosed(Integer cost, GPSState state) {
 		//si ya se pasó por el mismo estado y el costo era menor no se tiene que 
 		//explotar de nuevo el estado
 		for (GPSNode openNode : open) {
 			if (openNode.getState().compare(state) && openNode.getCost() <= cost) {
 				return true;
 			}
 		}
 		for (GPSNode closedNode : closed) {
 			if (closedNode.getState().compare(state)
 					&& closedNode.getCost() <= cost) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// se fija que no haya un ciclo
 	private boolean checkBranch(GPSNode parent, GPSState state) {
 		if (parent == null) {
 			return false;
 		}
 		return checkBranch(parent.getParent(), state)
 				|| state.compare(parent.getState());
 	}
 
 	public abstract void addNode(GPSNode node);
 
 	public abstract String getStrategyName();
 
 }
