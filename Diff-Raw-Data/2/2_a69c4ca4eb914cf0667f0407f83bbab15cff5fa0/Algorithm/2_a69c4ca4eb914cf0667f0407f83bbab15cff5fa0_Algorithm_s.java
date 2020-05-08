 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.LinkedList;
 import java.util.Stack;
 
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class Algorithm {
 
 	private Graph g;
 	private Division d;
 	private static boolean verbose = false;
 
 	/**
 	 * No argument constructor.
 	 */
 	public Algorithm() {
 		this(null);
 	}
 
 	/**
 	 * Parameterised constructor.
 	 * 
 	 * @param Division
 	 *            div
 	 */
 	public Algorithm(Division div) {
 		g = null;
 		d = div;
 	}
 
 	/**
 	 * Returns this algorithms Graph
 	 * 
 	 * @return Graph
 	 */
 	public Graph getG() {
 		return g;
 	}
 
 	/**
 	 * Sets this algorithms Graph
 	 * 
 	 * @param Graph
 	 *            g
 	 */
 	public void setG(Graph g) {
 		this.g = g;
 	}
 
 	/**
 	 * Returns this algorithms Division
 	 * 
 	 * @return Division
 	 */
 	public Division getD() {
 		return d;
 	}
 
 	/**
 	 * Sets this algorithms Division
 	 * 
 	 * @param Division
 	 *            d
 	 */
 	public void setD(Division d) {
 		this.d = d;
 	}
 
 	/**
 	 * Checks whether or not a specified team is eliminated.
 	 * 
 	 * The algorithm is only run if the teams status is currently false.
 	 * 
 	 * @param Team
 	 *            t
 	 * @return boolean
 	 */
 	public boolean isEliminated(Team t) {
 		// If the team is already known to be eliminated, just return true.
 		return t.isEliminated() ? true : fordFulkerson(t);
 	}
 
 	/**
 	 * Sets the verbose flag for the algorithm, useful information is printed on
 	 * System.out
 	 */
 	public void setVerbose() {
 		verbose = true;
 	}
 
 	private boolean fordFulkerson(Team t) {
 		// Create initial graph
 		g = new Graph(d, t);
 		// For each edge in the graph g, set the flow to be 0.
 		for (Vertex v : g.getV())
 			for (AdjListNode n : v.getAdjList())
 				n.setFlow(0);
 		// Cap states the maximum possible flow away from source.
 		int cap = 0;
 		for (AdjListNode a : g.getSource().getAdjList())
 			cap += a.getCapacity();
 		// Create initial residual graph for algorithm.
 		ResidualGraph residual = new ResidualGraph(g);
 		// Path will store the residual path (if it exists)
 		Path path;
 		// Algorithm continues while a residual path exists
 		while ((path = residualPath(residual)) != null) {
 			// Reduce cap by the amount of flow that can be added in this
 			// iteration.
 			cap -= path.getCapacity();
 			// For each node in the path, add/remove the path's capacity from
 			// the edges flow. Add to forward edges, remove from backward edges.
 			int[] pathnodes = path.getPath();
 			for (int j = 1; j < pathnodes.length; j++) {
 				int i = j - 1;
 				if (pathnodes[i] < pathnodes[j]) {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList())
 						// Forward edge, add flow.
 						if (a.getVertex().getIndex() == pathnodes[j])
 							a.setFlow(a.getFlow() + path.getCapacity());
 				} else {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList())
 						// Backward edge, remove flow.
 						if (a.getVertex().getIndex() == pathnodes[j])
 							a.setFlow(a.getFlow() - path.getCapacity());
 				}
 			}
 			// Update residual graph based on new original graph's flow data.
 			residual = new ResidualGraph(g);
 		}
 		if(verbose){
 		    int teamToSinkRemain=0;
 		    for (Vertex v : g.getV()){
 		    	if(g.getV()[v.getIndex()] instanceof TeamVertex){
 		    		TeamVertex TV = (TeamVertex) g.getV()[v.getIndex()];
 		    		for(AdjListNode a: TV.getAdjList()){
 		    			teamToSinkRemain+=(a.getCapacity()-a.getFlow());
 		    		}
 		    	}
 		    }
 			System.out.println("Total Remaining Games: " +teamToSinkRemain);   
 		}
 		// If final flow of graph is saturating, team has not been eliminated,
 		// return false.
 		// Otherwise, team has been eliminated, return true.
 		if (cap != 0) {
 			residual.certificateOfEliminationHelper();
 			for (Vertex v : residual.getV()) {
 				if (g.getV()[v.getIndex()] instanceof TeamVertex) {
 					TeamVertex elim = (TeamVertex) g.getV()[v.getIndex()];
 					if (v.getVisited())
 						t.getEliminatedBy().add(elim.getTeam());
 				}
 			}
 			if (verbose) {
 				// Test output from above
 				System.out.println("\n" + t.getName() + " eliminated by:");
 				for (Team elimBy : t.getEliminatedBy())
 					System.out.println(elimBy.getName());
 				System.out.println();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private static Path residualPath(ResidualGraph g) {
 		// Perform BFS from source on graph
 		g.bfs();
 		// Since we'll work out a path from sink to source, it will be added to
 		// a
 		// stack then popped until empty to store path in correct direction.
 		Stack<Integer> backPath = new Stack<Integer>();
 		// Use matrix representation of edge capacities to speed up this method.
 		int[][] matrix = g.getMatrix();
 		int next, current, capacity = Integer.MAX_VALUE;
 		// Start at sink, try to work back up to source (vertex 0)
 		Vertex v[] = g.getV();
 		next = current = g.getSink().getIndex();
 		// Until we either get stuck, or get to the source, keep going.
 		while (true) {
 			// If we're stuck outside the source, we're finished.
 			if (v[next].getPred() == next && next != 0)
 				return null;
 			// Add next node to the path.
 			backPath.add(next);
 			// Ensure current path capacity is at it's lowest possible value
 			if (current != next && matrix[next][current] < capacity)
 				capacity = matrix[next][current];
 			// If we're at the source, one path has been found.
 			if (next == 0)
 				break;
 			// Bump up one step up the path
 			current = next;
 			next = v[next].getPred();
 		}
 		// Pop everything from the stack to make the path in source->sink order.
 		int[] path = new int[backPath.size()];
 		for (int i = 0; i < path.length; i++)
 			path[i] = backPath.pop();
 		return new Path(path, capacity);
 	}
 
 	/**
	 * Updates the entire Divisions elimination status.
 	 * 
 	 * This method sorts the Teams in non-descending order by wins and games
 	 * remaining and then uses binary search to eliminate teams.
 	 * 
 	 * @param Division
 	 *            d
 	 */
 	public void updateDivisionElim(Division d) {
 		Team[] teams = d.teamsToArray();
 		// Sorts teams into non-descending order by wins and games remaining
 		for (int i = 0; i < teams.length; i++) {
 			for (int j = i; j < teams.length; j++) {
 				if (teams[i].compareTo(teams[j]) > 0) {
 					Team temp = teams[i];
 					teams[i] = teams[j];
 					teams[j] = temp;
 				}
 			}
 		}
 		// Determine the highest team that has been eliminated.
 		int lastElim = binaryDetermine(teams, 0, teams.length, -1);
 		// Eliminate this team and all teams below it.
 		for (int i = 0; i <= lastElim; i++) {
 			teams[i].setEliminated(true);
 		}
 	}
 
 	private int binaryDetermine(Team[] T, int s, int e, int highestElim) {
 		// Stop conditions
 		if (e < s || s > e || s < 0 || e > T.length)
 			return highestElim;
 		// Binary search
 		int mid = (s + e) / 2;
 		if (mid >= T.length)
 			return highestElim;
 		if (fordFulkerson(T[mid])) {
 			// only updates highestElim in the upper sections.
 			highestElim = mid;
 			return binaryDetermine(T, mid + 1, e, highestElim);
 		} else
 			return binaryDetermine(T, s, mid - 1, highestElim);
 	}
 }
