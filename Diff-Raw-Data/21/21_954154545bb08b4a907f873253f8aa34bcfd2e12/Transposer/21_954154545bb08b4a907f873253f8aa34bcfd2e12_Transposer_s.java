 /**
  * Utility class for transposing a graph
  */
 package com.codemelon.graph.util;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.codemelon.graph.DiGraph;
 import com.codemelon.graph.vertex.Vertex;
 
 /**
  * @author Marshall Farrier
  * @version Nov 25, 2012
  *
  */
 public class Transposer {
 	private DiGraph graph;
	HashMap<Vertex, Vertex> vertexMap;
 	
 	public Transposer(DiGraph graph) {
 		this.graph = graph;
 	}
 	/**
 	 * All vertex data (label, color, distance, discoveryTime, etc.) is copied
 	 * into the transpose graph with the exception of parent, which is set to 
 	 * null for each vertex in the transpose graph. Edge data, however,
 	 * is <em>not</em> copied into the transpose graph. 	
 	 * @return a graph which is the transpose of the original graph
 	 */
 	public DiGraph transpose() {
 		DiGraph result = new DiGraph(graph.vertexCount());
 		// create a datastructure for tracking corresponding vertices
 		vertexMap = new HashMap<Vertex, Vertex>(graph.vertexCount());
 		// insert vertices into map and result graph
 		Iterator<Vertex> vertexIterator = graph.vertexIterator();
 		Vertex v;
 		while (vertexIterator.hasNext()) {
 			v = vertexIterator.next();
 			vertexMap.put(v, new Vertex(v));
 			result.addVertex(vertexMap.get(v));
 		}
 		Set<Vertex> adjacentVertices;
 		vertexIterator = graph.vertexIterator();
 		while (vertexIterator.hasNext()) {
 			v = vertexIterator.next();
 			adjacentVertices = v.getAdjacencies();			
 			for (Vertex to : adjacentVertices) {
 				result.addEdge(vertexMap.get(to), vertexMap.get(v));
 			}		
 		}
 		return result;
 	}
 	/**
 	 * Returns a mapping in which the keys are the vertices of the original graph
	 * and the values are the corresponding vertices in the transpose graph.
 	 * @return a HashMap from vertices in the original graph to vertices in the
	 * transpose graph.
 	 */
 	public HashMap<Vertex, Vertex> getVertexMap() {
 		return vertexMap;
 	}
 }
