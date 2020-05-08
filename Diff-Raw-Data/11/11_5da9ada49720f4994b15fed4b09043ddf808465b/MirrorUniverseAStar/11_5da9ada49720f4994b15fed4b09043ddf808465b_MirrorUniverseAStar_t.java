 package mirroruniverse.g2.astar;
 
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import mirroruniverse.g2.Config;
 import mirroruniverse.g2.Map;
 import mirroruniverse.g2.Position;
 import mirroruniverse.sim.MUMap;
 
 public class MirrorUniverseAStar extends AStar<State> {
 	Map leftMap;
 	Map rightMap;
 	PriorityQueue<Node> hangingNodes;
 	protected int hangingNodesExpandedCounter;
 	boolean needExplored;
 
 	public class hangingNodesComparator implements Comparator<Node> {
 		@Override
 		public int compare(Node o1, Node o2) {
 			double h1 = o1.f - o1.g;
 			double h2 = o2.f - o2.g;
 			return (int) (h1 - h2);
 		}
 	}
 
 	public MirrorUniverseAStar(Map leftMap, Map rightMap) {
 		this.leftMap = leftMap;
 		this.rightMap = rightMap;
 		hangingNodes = new PriorityQueue<Node>(1024, new hangingNodesComparator());
 		fringe = new PriorityQueue<Node>();
 		closedStates = new HashSet<State>();
 		expandedCounter = 0;
 		hangingNodesExpandedCounter = 0;
 		this.needExplored = true;
 	}
 
 	public boolean isGoal(State node) {
 		// if we haven't found the exits yet
 		if (leftMap.exitPos == null || rightMap.exitPos == null)
 			return false;
 
 		// now we know the position of the exits
 		Position posLeft = node.posLeft;
 		Position posRight = node.posRight;
 		Position exitLeft = leftMap.exitPos;
 		Position exitRight = rightMap.exitPos;
 
 		// if they are the same, both left and right
 		if (posLeft.x == exitLeft.x && posLeft.y == exitLeft.y
 				&& posRight.x == exitRight.x && posRight.y == exitRight.y)
 			return true;
 		return false;
 	}
 
 	protected Double g(State from, State to) {
 		return 1.0;
 	}
 
 	protected Double h(State from) {
 
 		double x1, x2, y1, y2, deltaX, deltaY, diagonal;
 
 		x1 = from.posLeft.x;
 		y1 = from.posLeft.y;
 		x2 = leftMap.exitPos.x;
 		y2 = leftMap.exitPos.y;
 		deltaX = Math.abs(x1 - x2);
 		deltaY = Math.abs(y1 - y2);
 		diagonal = Math.max(deltaX, deltaY);
 		// double orthogonal = Math.abs(deltaX - deltaY);
 		double distanceLeft = diagonal;// + orthogonal;
 
 		x1 = from.posRight.x;
 		y1 = from.posRight.y;
 		x2 = rightMap.exitPos.x;
 		y2 = rightMap.exitPos.y;
 		deltaX = Math.abs(x1 - x2);
 		deltaY = Math.abs(y1 - y2);
 		diagonal = Math.max(deltaX, deltaY);
 		// orthogonal = Math.abs(deltaX - deltaY);
 		double distanceRight = diagonal;// + orthogonal;
 
 		return Math.max(distanceLeft, distanceRight);
 	}
 
 	protected List<State> generateSuccessors(Node node) {
 		List<State> successors = new LinkedList<State>();
 
 		if (closedStates.contains(node.state))
 			return successors;
 
 		Position posLeft = node.state.posLeft;
 		Position posRight = node.state.posRight;
 
 		// if one of the players has reached the exit
 		if (leftMap.isExit(posLeft) || rightMap.isExit(posRight)) {
 			// put it in hanging state
 			hangingNodes.add(node);
 			// do not expand it, return directly
 			return successors;
 		}
 
 		if (Config.DEBUG)
 			System.out.println("Expand:\n" + node.state);
 
 		// now none of the player is on the exit
 
 		for (int i = 1; i != MUMap.aintDToM.length; ++i) {
 			int[] aintMove = MUMap.aintDToM[i];
 			int deltaX = aintMove[0];
 			int deltaY = aintMove[1];
 
 			Position newPosLeft;
 			newPosLeft = new Position(posLeft.y + deltaY, posLeft.x + deltaX);
 			// if there is a position that is unknown on the left map
 			if (leftMap.isUnknown(newPosLeft))
 				this.needExplored = true;
 			if (!leftMap.isValid(newPosLeft)) {
 				// if it's not valid, roll back
 				newPosLeft.x -= deltaX;
 				newPosLeft.y -= deltaY;
 			}
 
 			Position newPosRight;
 			newPosRight = new Position(posRight.y + deltaY, posRight.x + deltaX);
 			// if there is a position that is unknown on the right map
 			if (rightMap.isUnknown(newPosRight))
 				this.needExplored = true;
 			if (!rightMap.isValid(newPosRight)) {
 				// if it's not valid, roll back
 				newPosRight.x -= deltaX;
 				newPosRight.y -= deltaY;
 			}
 			State newState = new State(newPosLeft, newPosRight);
 			if (!newState.equals(node.state)
 					&& (!closedStates.contains(newState)))
 				successors.add(newState);
 		}
 
 		return successors;
 	}
 
 	public List<State> compute(State start) {
 		try {
 			this.needExplored = false;
 			Node root = new Node();
 			root.setState(start);
			this.closedStates.clear();
			this.fringe.clear();
			this.hangingNodes.clear();
 
 			fringe.offer(root);
 
 			for (;;) {
 				Node p = fringe.poll();
 
 				if (p == null) {
 					// if there are still unknown area during the search
 					if (this.needExplored)
 						return null;
 
 					// search all the hanging nodes
 					if (Config.DEBUG)
 						System.out.println("No perfect path");
 
 					ImperfectSolutionAStar isa = new ImperfectSolutionAStar(
 							leftMap, rightMap, hangingNodes);
 					List<State> bestSolution = isa.getBestSolution();
 					this.hangingNodesExpandedCounter = isa.expandedCounter;
 					return bestSolution;
 				}
 
 				State last = p.getState();
 
 				if (isGoal(last)) {
 					LinkedList<State> retPath = new LinkedList<State>();
 					for (Node i = p; i != null; i = i.parent)
 						retPath.addFirst(i.getState());
 					closedStates.clear();
 					return retPath;
 				}
 
 				expand(p);
 				closedStates.add(p.state);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
