 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import uk.ac.gla.dcs.tp3.w.league.League;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class Algorithm {
 
 	private Graph g;
 	private League l;
 
 	public Algorithm() {
 		this(null);
 	}
 
 	public Algorithm(League league) {
 		g = null;
 		l = league;
 	}
 
 	public Graph getG() {
 		return g;
 	}
 
 	public void setG(Graph g) {
 		this.g = g;
 	}
 
 	public League getL() {
 		return l;
 	}
 
 	public void setL(League l) {
 		this.l = l;
 	}
 
 	public boolean isEliminated(Team t) {
 		return t.isEliminated() ? true : fordFulkerson(t);
 	}
 
 	private boolean fordFulkerson(Team t) {
 		g = new Graph(l, t);
 		// For each edge in the graph g, set the flow to be 0.
 		for (Vertex v : g.getV())
 			for (AdjListNode n : v.getAdjList())
 				n.setFlow(0);
 		// While there is a path p from the source to the sink in the residual graph
 			// Find the capacity c of the residual graph
 			// For each edge in the path p
 				// If p is a forward edge, add c to flow of the edge in graph g.
 				// If p is a backward edge, remove c from the flow of the edge in graph g.
 		// If final flow of graph is saturating, team has not been eliminated, return false.
 		// Otherwise, team has been eliminated, return true.
 
 		// Extension: Max Flow-Min Cut Theorem.
 		// Overview:
 		// The value of a maximum flow is equal to the capacity of a minimum cut.
 		// Obtain two sets of vertices, A and B. The source is a member of A. The sink is a member of B.
 		// The team nodes that are in B are the teams responsible for the elimination of team t. These
		// teams for the certificate of elimintation.
 		return false;
 	}
 
 }
