 package algorithms;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import model.Vertex;
 import model.WeightedEdgeInterface;
 import model.WeightedGraph;
 
 /**
  * <p>
  * This class is an implementation of Dijkstra's single source shortest path
  * (SSSP) algorithm. The algorithm solves the SSSP on a weighted directed graph
  * for which edge weights are non-negative.
  * </p>
  * <p>
 * For more information on the algorith itself, please see:
  * </p>
  * <p>
  * Cormen et al. (2009). Single-Source Shortest Paths. <i>Introduction to
  * Algorithms, Third Edition</i>. (pp. 658-662). Cambridge, Massachusetts. The
  * MIT Press.
  * </p>
  * 
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2012-09-15
  */
 public class DijkstraSSSP<V extends Vertex, E extends WeightedEdgeInterface> {
 
 	// Passed in parameters.
 	private WeightedGraph<V, E> graph;
 	private V source;
 
 	// Fields used to create the sssp.
 	private HashMap<V, V> predecessor = new HashMap<V, V>();
 	private HashMap<V, Float> distance = new HashMap<V, Float>();
 
 	// Fields used to hold current generated path properties.
 	private ArrayList<V> currentPathVerts;
 	private ArrayList<E> currentPathEdges;
 	private float currentPathWeight;
 
 	// Allow the priority queue to be keyed by vertex distance values.
 	private class VComparator implements Comparator<V> {
 
 		@Override
 		public int compare(V o1, V o2) {
 
 			float result = distance.get(o1) - distance.get(o2);
 
 			if (result > 0) {
 				return 1;
 			} else if (result < 0) {
 				return -1;
 			}
 
 			return 0;
 		}
 	}
 
 	/**
 	 * Create a Dijkstra's single source shortest path algorithm instance from
 	 * the input graph and source vertex.
 	 * 
 	 * @param graph
 	 *            a directed weighed graph to use for Dijkstra's algorithm.
 	 * @param source
 	 *            the source vertex to use for Dijkstra's algorithm.
 	 */
 	public DijkstraSSSP(WeightedGraph<V, E> graph, V source) {
 
 		this.graph = graph;
 		this.source = source;
 
 		// Check the query is valid.
 		if (!graph.vertices().contains(source)) {
 			return;
 		}
 
 		// Calculate the vertex distances & predecessors.
 		// Basically we just run the algorithm.
 		dijkstra();
 	}
 
 	/**
 	 * Initialize the distance & previous node quantities for each vertex.
 	 */
 	private void initializeSingleSource() {
 
 		Iterator<V> verticesIter = graph.vertices().iterator();
 
 		while (verticesIter.hasNext()) {
 
 			V v = verticesIter.next();
 
 			distance.put(v, Float.MAX_VALUE);
 			predecessor.put(v, null);
 		}
 
 		distance.put(source, 0f);
 	}
 
 	/**
 	 * Relax an edge.
 	 * 
 	 * The edge to be relaxed has weight w, and is defined as the directed edge
 	 * from vertex u to vertex v.
 	 * 
 	 * @param u
 	 *            the 'from' vertex for the edge which is to be relaxed.
 	 * @param v
 	 *            the 'to' vertex for the edge which is to be relaxed.
 	 * @param w
 	 *            the weight of the edge connecting vertices u and v.
 	 */
 	private void relax(V u, V v, float w) {
 
 		if (distance.get(v) > (distance.get(u) + w)) {
 
 			distance.put(v, distance.get(u) + w);
 			predecessor.put(v, u);
 		}
 	}
 
 	/**
 	 * Run Dijkstra's algorithm on the graph with respect to the source vertex.
 	 * 
 	 * This method calculates the correct distance and predecessor values for
 	 * all vertices in the graph.
 	 */
 	private void dijkstra() {
 
 		initializeSingleSource();
 
 		// There isn't a priority queue constructor that allows us to specify
 		// both a comparator and a collection. :(
 		PriorityQueue<V> vertices;
 
 		int pqInitialCapacity = graph.vertices().size();
 		Comparator<V> pqComparator = new VComparator();
 
 		vertices = new PriorityQueue<V>(pqInitialCapacity, pqComparator);
 		vertices.addAll(graph.vertices());
 
 		while (!vertices.isEmpty()) {
 
 			// Get vertex with smallest distance to the source.
 			V u = vertices.poll();
 
 			// For each vertex adjacent to u (the closest vertex to source)
 			// relax the edge connecting them.
 			Iterator<E> edgesIter = graph.incidentEdges(u).iterator();
 
 			while (edgesIter.hasNext()) {
 
 				E e = edgesIter.next();
 				V v = graph.opposite(u, e);
 
 				// Only use outgoing edges from u to v. An edge from u -> v will
 				// show as having u in the first position of its connecting edge
 				// end vertices, with v in the second position. (we already know
 				// the edge has v in the second position as we used u and the
 				// edge to find it)
 				if (graph.endVertices(e).iterator().next().equals(u)) {
 					relax(u, v, e.getWeight());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Generate a path from the source vertex to the input destination vertex.
 	 * 
 	 * This method populates the current path values for vertices, edges, and
 	 * the length of the path. The values can then be returned via the objects
 	 * getter methods.
 	 * 
 	 * @param destination
 	 *            a vertex to use as the destination for the shortest path from
 	 *            the source.
 	 */
 	public void generatePath(V destination) {
 
 		// Reset the path properties.
 		currentPathVerts = new ArrayList<V>();
 		currentPathEdges = new ArrayList<E>();
 		currentPathWeight = 0f;
 
 		// Calculate the path.
 		while (destination != null) {
 
 			currentPathVerts.add(destination);
 			destination = predecessor.get(destination);
 		}
 
 		// Path is found in reverse order. Reverse it.
 		Collections.reverse(currentPathVerts);
 
 		// From the path of vertices generate the edge path. While doing this
 		// update the path length.
 		for (int i = 0; i < currentPathVerts.size() - 1; i++) {
 
 			V v = currentPathVerts.get(i);
 			V u = currentPathVerts.get(i + 1);
 
 			Iterator<E> incidentEdgesIter = graph.incidentEdges(v).iterator();
 
 			while (incidentEdgesIter.hasNext()) {
 
 				E e = incidentEdgesIter.next();
 
 				Iterator<V> endVerticesIter = graph.endVertices(e).iterator();
 
 				V from = endVerticesIter.next();
 				V to = endVerticesIter.next();
 
 				if (from.equals(v) && to.equals(u)) {
 					currentPathEdges.add(e);
 					currentPathWeight += e.getWeight();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get a list of the vertices on the current shortest path.
 	 * 
 	 * @return a List of the vertices on the current shortest path.
 	 */
 	public List<V> getPathVerts() {
 		return currentPathVerts;
 	}
 
 	/**
 	 * Get a list of the edges on the current shortest path.
 	 * 
 	 * @return a List of the edges on the current shortest path.
 	 */
 	public List<E> getPathEdges() {
 		return currentPathEdges;
 	}
 
 	/**
 	 * Get the weight of the current shortest path.
 	 * 
 	 * @return the weight of the current shortest path.
 	 */
 	public float getPathWeight() {
 		return currentPathWeight;
 	}
 
 }
