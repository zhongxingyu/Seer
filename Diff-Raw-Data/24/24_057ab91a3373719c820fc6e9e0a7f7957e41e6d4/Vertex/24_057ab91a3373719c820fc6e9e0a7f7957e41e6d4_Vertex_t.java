 package org.misera.android.cavenav.graph;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Vertex {
 
 	public ArrayList<Edge> edges;
 	public int x;
 	public int y;
 	
 	// to make life easier, here are values needed for A*
 	public double f;
	public double g = Double.MAX_VALUE;
 	public double h;
 	public Vertex prevOnRoute;
 	
 
 	public Vertex(int x, int y) {
 		this.x = x;
 		this.y = y;
 		this.edges = new ArrayList<Edge>();
 	}
 
 	public void addEdge(Edge edge) {
 		this.edges.add(edge);
 	}
 
 	public String toString() {
 		return String.format("<Vertex (%d,%d)>", this.x, this.y);
 	}
 
 	public Vertex[] getNeighbors() {
 		Set<Vertex> vertices = new HashSet<Vertex>();
 		for (Edge e : edges) {
 			vertices.add(e.startVertex);
 			vertices.add(e.endVertex);
 		}
 		vertices.remove(this);
 		return vertices.toArray(new Vertex[1]);
 	}
 }
