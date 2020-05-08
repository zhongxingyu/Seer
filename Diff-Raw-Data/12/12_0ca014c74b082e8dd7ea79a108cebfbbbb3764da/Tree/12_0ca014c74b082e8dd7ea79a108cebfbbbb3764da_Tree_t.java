 package model;
 
 import java.util.HashSet;
 
 //a tree contains a set of edges
 public class Tree {
 	private HashSet<Edge> edges = new HashSet<Edge>();
 	
 	public void add(Edge e) {
 		edges.add(e);
 	}
 	
 	public void remove(Edge e) {
 		edges.remove(e);
 	}
 	
 	public HashSet<Edge> getEdges() {
 		return edges;
 	}
 	
 	// contains edge or reverse-edge
 	public boolean contains(Edge edge) {
 		return edges.contains(edge);
 	}
 	
 	// does the edge touch the tree
 	public boolean connects(Edge edge) {
 		for (Edge e : edges) {
 			if (edge.getV1() == e.getV1() || edge.getV1() == e.getV2()) return true;
 			if (edge.getV2() == e.getV1() || edge.getV2() == e.getV2()) return true;
 		}
 		return false;
 	}
 	
 	// do both edges touch the tree
	// use a hashset to ensure vertices aren't checked twice
 	public boolean connectsBoth(Edge edge) {
		HashSet<Vertex> match = new HashSet<Vertex>();
 		for (Edge e : edges) {
			if (edge.getV1() == e.getV1() || edge.getV1() == e.getV2()) match.add(edge.getV1());
			if (edge.getV2() == e.getV1() || edge.getV2() == e.getV2()) match.add(edge.getV2());
 			
			if (match.size() >= 2) {
 				// both edges touch the tree
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	// number of edges
 	public int size() {
 		return edges.size();
 	}
 }
