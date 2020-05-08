 package edu.cmu.cs211.pg.algorithms;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.TreeSet;
 
 import edu.cmu.cs211.pg.graph.Edge;
 import edu.cmu.cs211.pg.graph.Graph;
 import edu.cmu.cs211.pg.graph.MyDirectedGraph;
 import edu.cmu.cs211.pg.graph.Path;
 import edu.cmu.cs211.pg.graph.WeightedEdge;
 
 public class MstTspApproximation<V extends Comparable<V>>
 {
 	private Dijkstra dijkstra;
 	private Kruskal kruskal;
 	
 	protected MstTspApproximation()
 	{
 		kruskal = new Kruskal();
 		dijkstra = new Dijkstra();
 	}
 	
 	public MstTspApproximation(Kruskal kruskal, Dijkstra dijkstra)
 	{
 		this.kruskal = kruskal;
 		this.dijkstra = dijkstra;
 	}
 	
 	/**
 	 * Generates a tour of a specified set of nodes in the graph using the
 	 * MST-TSP 2-competitive approximation algorithm.
 	 * 
 	 * In order to aid in the unit testing of this class, we require
 	 * that the order you visit neighbors in the DFS portion of
 	 * the algorithm is the same as the natural ordering of the vertex
 	 * type V. You should also use the instances of Kruskal and
 	 * Dijkstra passed into the constructor of this class, not your own.
 	 * 
 	 * @param g the graph to generate the tour on. This UNDIRECTED
 	 *          graph may not be complete, but must be connected.
 	 *          
 	 * @param verts the vertices in the graph that you want to tour.
 	 *              This may be a subset of the vertices in the graph.
 	 *                       
 	 * @param start the vertex at which the tour must begin and end.
 	 *        start may not be in the set of vertices to tour, but
 	 *        your tour must begin and end at start.
 	 * 
 	 * @throws IllegalArgumentException if any of the vertices in the
 	 *              verts set are not in the graph. 
 	 * @throws NullPointerException if g, verts, or start are null
 	 * 
 	 * @return A list of vertices representing the order you would visit
 	 *         vertices in the original graph such that every vertex in
 	 *         the specified verts set is visited at least once.
 	 */
 	public List<V> approximateTour(Graph<V,WeightedEdge<V>> g, Set<V> verts, V start)
 	{
 		if (g == null || verts == null || start == null)
 			throw new NullPointerException("Null parameter to approximateTour()");
 		
 		// Do we need to come back to the start node (true cycle)?
 		boolean backToStart = false;
		if (verts.contains(start)) {
 			backToStart = true;
 		}
 		else {
 			// we need start in there anyway, for the MST
 			verts = new HashSet<V>(verts); 
 			verts.add(start);
 		}
 		
 		// form basic graph of ONLY verts with all edges directly between vertices
 		Graph<V, WeightedEdge<V>> reduced = new MyDirectedGraph<V, WeightedEdge<V>>(verts);
 		Object[] it = verts.toArray();
 		for (int i = 0; i < it.length; i++) {
 			for (int j = 0; j < it.length; j++) {
 				if (i == j)
 					continue;
 				
 				WeightedEdge<V> newEdge = g.adjacent((V)it[i], (V)it[j]);
 				if (newEdge != null)
 					reduced.addEdge(newEdge);
 			}
 		}
 		
 		// add on edges that do not exist in current graph
 		for (int i = 0; i < it.length; i++) {
 			for (int j = 0; j < it.length; j++) {
 				if (j == i)
 					continue;
 				
 				Path<V, WeightedEdge<V>> currentShortPath = dijkstra.shortestPath(reduced, (V)it[i], (V)it[j]);
 				Path<V, WeightedEdge<V>> shortPath = dijkstra.shortestPath(g, (V)it[i], (V)it[j]);
 				if (shortPath == null)
 					throw new IllegalArgumentException(); // cannot form mst
 				
 				if (currentShortPath == null || 
 						shortPath.pathWeight() < currentShortPath.pathWeight()) {
 					reduced.addEdge(new WeightedEdge<V>((V)it[i], (V)it[j], shortPath.pathWeight()));
 				}
 			}
 		}
 		
 		// Kruskal's to find MST of reduced graph
 		Graph<V,WeightedEdge<V>> mst = kruskal.MST(reduced);
 
 		// DFS to pre-order traversal of the MST
 		HashSet<V> visited = new HashSet<V>();
 		directedToUndirected(mst);
 		List<V> order = dfs(mst, start, visited); // the order in which we visit the nodes needed
 		order.add(start);
 		
 		// list of all nodes we visit, in order, in our traversal
 		List<V> traversal = new ArrayList<V>();
 		for (int i = 1; i < order.size(); i++)
 			traversal.addAll(vertices(dijkstra.shortestPath(g, order.get(i - 1), order.get(i)).edges()));
 		
 		if (backToStart)
 			traversal.addAll(vertices(dijkstra.shortestPath(g, order.get(order.size() - 1), start).edges()));
 		
 		return traversal;
 	}
 
 	/**
 	 * Depth First Search of a graph, to iterate through the MST
 	 * 
 	 * @param g the minimum spanning tree being traversed
 	 * @param start the node we are searching from now
 	 * @return a list of all nodes in g, preordered by depth first search
 	 */
 	private List<V> dfs(Graph<V, WeightedEdge<V>> mst, V start, Set<V> visited)
 	{
 		visited.add(start);
 		
 		List<V> ret = new ArrayList<V>();
 		ret.add(start);
 
 		PriorityQueue<V> neighbors = new PriorityQueue<V>(mst.outgoingNeighbors(start));
 		while (!neighbors.isEmpty()) {
 			V neighbor = neighbors.poll();
 			
 			// Have we already looked at the node?
 			if (!visited.contains(neighbor)) {
 				// Traverse via preorder
 				ret.addAll(dfs(mst, neighbor, visited));
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Turn a directed weighted graph into, practically, an undirected unweighted graph
 	 * by adding opposite-direction edges for each edge with weights of 0
 	 * @param g the graph we are transforming
 	 */
 	private void directedToUndirected(Graph<V, WeightedEdge<V>> g)
 	{
 		Object[] it = g.vertices().toArray();
 		
 		for (int i = 0; i < it.length; i++) {
 			for (int j = 0; j < it.length; j++) {
 				if (g.adjacent((V)it[i], (V)it[j]) != null)
 					g.addEdge(new WeightedEdge<V>((V)it[j], (V)it[i], 0));
 				else if (g.adjacent((V)it[j], (V)it[i]) != null)
 					g.addEdge(new WeightedEdge<V>((V)it[i], (V)it[j], 0));
 			}
 		}
 	}
 
 	
 	/**
 	 * list of vertices in a path, in order
 	 * @param edges list of the edges in the path
 	 * @return list of vertices (without start vertex)
 	 */
 	private List<V> vertices (List<WeightedEdge<V>> edges)
 	{
 		List<V> nodes = new ArrayList<V>();
 		for (int i = 0; i < edges.size(); i++) {
 			nodes.add(edges.get(i).dest());
 		}
 		
 		return nodes;
 	}
 }
