 package Algorithms;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Random;
 
 import Graphs.Edge;
 import Graphs.Graph;
 import Graphs.Vertex;
 
 /**
  * Advanced Graph Algorithms
  * @author RDrapeau
  *
  */
 public class AdvancedGraph {
 	/**
 	 * Random variable for algorithms.
 	 */
 	private static final Random r = new Random();
 	
 	/**
 	 * Returns a Minimum Spanning Tree as a graph using Prim's algorithm.
 	 * 
 	 * @param g - The connected undirected acyclic graph
 	 * @return The minimum spanning tree for the graph
 	 */
 	public Graph minSpanTree(Graph g) {
 		Graph result = new Graph();
 		if (g.numberOfVertices() > 0) {
 			Map<Vertex, Vertex> convert = new HashMap<Vertex, Vertex>();
 			Queue<Edge> q = new PriorityQueue<Edge>();
 			Vertex s = g.getVertex(r.nextInt(g.numberOfVertices()) + 1); // Numbered 1 to N
 			
 			convert.put(s, new Vertex(s.getID()));
 			q.addAll(s.getEdges());
 			result.addVertex(convert.get(s));
 			
			while (result.numberOfVertices() != g.numberOfEdges() && !q.isEmpty()) {
 				Edge e = q.remove();
 				if (!convert.containsKey(e.getHead()) || !convert.containsKey(e.getTail())) { // New Vertex Discovered
 					Vertex tail = e.getTail();
 					Vertex head = e.getHead();
 					
 					update(convert, q, result, tail, head);
 					
 					tail = convert.get(tail); // Grab new Tail
 					head = convert.get(head); // Grab new Head
 					
 					Edge shortest = new Edge(tail, head, e.getWeight());
 					tail.addEdge(shortest);
 					head.addEdge(shortest);
 					result.addEdge(shortest);
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Updates the Map, Heap, and the result so far to contain the newly discovered Vertex.
 	 * 
 	 * @param convert - Conversion from the old vertices to the new ones in the tree
 	 * @param q - The Heap of edges
 	 * @param result - The minimum spanning tree so far
 	 * @param tail - The tail of the edge
 	 * @param head - The head of the edge
 	 */
 	private void update(Map<Vertex, Vertex> convert, Queue<Edge> q, Graph result, Vertex tail, Vertex head) {
 		if (!convert.containsKey(tail)) { /// Doesn't contain tail
 			q.addAll(tail.getEdges());
 			convert.put(tail, new Vertex(tail.getID()));
 			result.addVertex(convert.get(tail));
 		} else { // Doesn't contain head
 			q.addAll(head.getEdges());
 			convert.put(head, new Vertex(head.getID()));
 			result.addVertex(convert.get(head));
 		}
 	}
 }
