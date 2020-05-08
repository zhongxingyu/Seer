 package BoatSearch;
 
 import java.util.Set;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  * A node in the search tree that contains a state and the total cost
  * to reach this state.
  */
 public class SearchNode extends DefaultMutableTreeNode
 		implements Comparable<SearchNode> {
 
 	private static final long serialVersionUID = 1845825236979052915L;
 	private final int totalCost;
 	private final State state;
 	
 	/**
 	 * Creates a SearchNode with no parent or children
 	 * @param state The state to associate with this node
 	 * @param totalCost The total cost to reach this node's state
 	 */
 	public SearchNode(State state, int totalCost) {
 		this.state = state;
 		this.totalCost = totalCost;
 	}
 
 	/**
 	 * @return the total cost to reach this node's state
 	 */
 	public int getTotalCost() {
 		return totalCost;
 	}
 
 	/**
 	 * @return the state associated with this node
 	 */
 	public State getState() {
 		return state;
 	}
 
 	@Override
 	public int hashCode() {
 		return state.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof SearchNode)) {
 			return false;
 		}
 		final SearchNode other = (SearchNode) obj;
 		if (state == null) {
 			if (other.state != null) {
 				return false;
 			}
 		} else if (!state.equals(other.state)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Insert successors of this node's state as children of this node.
 	 */
 	public void expand() {
 		final Set<State> successors = state.getAllSuccessors();
 		for(State i : successors) {
 			int cost = state.getTransitionCost(i) + totalCost;
 			this.insert(new SearchNode(i, cost), this.getChildCount());
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "SearchNode (" + totalCost + ", " + state + ")";
 	}
 
 	@Override
 	public int compareTo(SearchNode o) {
		if(this.equals(o)) {
			return 0;
		}
 		final int myCost = totalCost + state.getHeuristicCost();
 		final int otherCost = o.totalCost + o.getState().getHeuristicCost();
		int diff = myCost - otherCost;
		if(diff == 0) {
			/*
			 * @see AStarFrontier
			 * SortedSet considers two elements equal - and consequently not unique
			 * or both fit for inclusion in the set - if compareTo returns 0.
			 * (A normal set uses the result of the equals method.)
			 * Since there are cases where nodes can have the same cost but have
			 * very different states, we want to include both nodes in that case.
			 * This little hack does that.
			 * 
			 * http://stackoverflow.com/questions/1510503/java-sortedset-comparator-consistency-with-equals-question
			 */
			return System.identityHashCode(this) > System.identityHashCode(o) ? 1 : -1;
		}
		return diff;
 	}
 	
 	
 	
 }
