 package model;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * graph
  * contains vertices and edges
  */
 public class Graph {
 	/**
 	 * list of vertices
 	 */
 	private Vector<Vertex> vertices = new Vector<Vertex>();
 	
 	/**
 	 * list of edges
 	 */
 	private Vector<Edge> edges = new Vector<Edge>();
 	
 	/**
 	 * add any number of vertices
 	 * 
 	 * @param vertices vertices
 	 */
 	public void add(Vertex... vertices) {
 		for (Vertex vertex : vertices) {
 			this.vertices.add(vertex);
 		}
 	}
 	
 	/**
 	 * create and add edge
 	 * add vertices if needed
 	 * 
 	 * @param v1 v1
 	 * @param v2 v2
 	 * @param weight weight
 	 * @return edge
 	 */
 	public Edge connect(Vertex v1, Vertex v2, int weight) {
 		if (!vertices.contains(v1)) {
 			add(v1);
 		}
 		if (!vertices.contains(v2)) {
 			add(v2);
 		}
 		
 		Edge e = new Edge(v1, v2, weight);
 		edges.add(e);
 		return e;
 	}
 	
 	/**
 	 * create and add edge
 	 * 
 	 * @param v1 v1
 	 * @param v2 v2
 	 * @return edge
 	 */
 	public Edge connect(Vertex v1, Vertex v2) {
 		Edge e = new Edge(v1, v2);
 		edges.add(e);
 		return e;
 	}
 	
 	/**
 	 * getter for vertex list
 	 * @return vertices
 	 */
 	public Vector<Vertex> getVertices() {
 		return vertices;
 	}
 	
 	/**
 	 * getter for edge list
 	 * @return edges
 	 */
 	public Vector<Edge> getEdges() {
 		return edges;
 	}
 	
 	/**
 	 * remove vertex and connecting edges
 	 * 
 	 * @param v vertex
 	 */
 	public void remove(Vertex v) {
 		HashSet<Edge> removeEdges = new HashSet<Edge>();
 		for (Edge e : edges) {
 			if (e.getV1() == v || e.getV2() == v) {
 				removeEdges.add(e);
 			}
 		}
 		edges.removeAll(removeEdges);
 		
 		vertices.remove(v);
 	}
 	
 	/**
 	 * remove edge
 	 * 
 	 * @param e edge
 	 */
 	public void remove(Edge e) {
 		edges.remove(e);
 	}
 	
 	/**
 	 * remove vertices
 	 * 
 	 * @param vv vertices
 	 */
 	public void removeAll(Vector<Vertex> vv) {
 		for (Vertex v : vv) {
 			remove(v);
 		}
 	}
 	
 	/**
 	 * returns set of directed edges from vertex
 	 * 
 	 * @param v vertex
 	 * @return directed edges
 	 */
 	public HashSet<DirectedEdge> getVertexEdges(Vertex v) {
 		HashSet<DirectedEdge> vEdges = new HashSet<DirectedEdge>();
 		for (Edge e : edges) {
 			if (e.getV1() == v) {
 				vEdges.add(new DirectedEdge(e.getV1(), e.getV2(), e));
 			} else if (e.getV2() == v) {
 				vEdges.add(new DirectedEdge(e.getV2(), e.getV1(), e));
 			}
 		}
 		return vEdges;
 	}
 	
 	/**
 	 * get all edges directly connecting v1 and v2
 	 * 
 	 * @param v1 v1
 	 * @param v2 v2
 	 * @return connecting edges
 	 */
 	public HashSet<Edge> getVerticesEdges(Vertex v1, Vertex v2) {
 		HashSet<Edge> vEdges = new HashSet<Edge>();
 		for (Edge e : edges) {
 			if (e.getV1() == v1 && e.getV2() == v2 || e.getV2() == v1 && e.getV1() == v2) {
 				vEdges.add(e);
 			}
 		}
 		return vEdges;
 	}
 	
 	/**
 	 * get initial random vertex
 	 * 
 	 * @return random vertex
 	 */
 	public Vertex getRandomVertex() {
 		int index = new Random().nextInt(vertices.size() - 1) + 1;
 		return vertices.get(index);
 	}
 	
 	/**
 	 * sort vertices
 	 */
 	public void sortVertices() {
 		Collections.sort(vertices);
 	}
 	
 	/**
 	 * sort edges
 	 */
 	public void sortEdges() {
 		Collections.sort(edges);
 	}
 	
 	/**
 	 * reset to a neutral state
 	 */
 	public void reset() {
 		for (Vertex v : vertices) {
 			v.reset();
 		}
 		for (Edge e : edges) {
 			e.reset();
 		}
 	}
 	
 	/**
 	 * get the shortest edge
 	 * 
 	 * @param edges edges
 	 * @return shortest edge
 	 */
 	public static Edge getShortestEdge(Set<Edge> edges) {
 		Edge shortest = null;
 		for (Edge e : edges) {
			if (shortest == null || e.getWeight() > shortest.getWeight()) {
 				shortest = e;
 			}
 		}
 		return shortest;
 	}
 }
