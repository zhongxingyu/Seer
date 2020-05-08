 package dna.graph.directed;
 
 import dna.graph.Node;
 
 public abstract class DirectedNode extends Node<DirectedEdge> {
 
 	public DirectedNode(int index) {
 		super(index);
 	}
 
 	public DirectedNode(String str) {
 		super(str);
 	}
 
 	public abstract Iterable<DirectedEdge> getIncomingEdges();
 
 	public abstract Iterable<DirectedEdge> getOutgoingEdges();
 
 	public abstract Iterable<DirectedNode> getNeighbors();
 
 	public abstract int getNeighborCount();
 
 	public abstract boolean hasNeighbor(DirectedNode n);
 
 	public abstract int getDegree();
 
 	public abstract int getInDegree();
 
 	public abstract int getOutDegree();
 
 	public void print() {
 		System.out.println(this.toString());
 		System.out.println("In: " + this.getIncomingEdges());
 		System.out.println("Out: " + this.getOutgoingEdges());
		System.out.println("Neighbors: " + this.getNeighbors());
 	}
 
 }
