 package de.unisiegen.informatik.bs.alvis.graph.datatypes;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCBoolean;
 
 
 /**
  * 
  * @author Dominik Dingel
  * 
  * @description implementing the Edge class TODO: add labels and weights
  * 
  */
 
 public class PCEdge extends PCObject {
 
 	protected static final String TYPENAME = "Edge";
 
 	// the used members
 	private PCVertex v1;
 	private PCVertex v2;
 	private PCBoolean isDirected;
 
 	/** 
 	 * 
 	 */
 	public PCEdge() {
 		v1 = new PCVertex();
 		v2 = new PCVertex();
 		isDirected = new PCBoolean(false);
 	}
 	
 	/**
 	 * Constructor to create new Edge (not directed)
 	 * 
 	 * @param v1
 	 * @param v2
 	 * @param edge
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2,
 			GraphicalRepresentationEdge edge) {
 		this.allGr.add(edge);
 		this.v1 = v1;
 		this.v2 = v2;
		isDirected.setLiteralValue(false);
 		notifyVertices();
 	}
 
 	/**
 	 * Constructor to create new Edge (not directed)
 	 * 
 	 * @param v1
 	 * @param v2
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2) {
 		this.v1 = v1;
 		this.v2 = v2;
 		isDirected = new PCBoolean(false);
 		notifyVertices();
 	}
 
 	/**
 	 * Constructor to create new Edge
 	 * 
 	 * @param v1
 	 * @param v2
 	 * @param isDirected
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2,
 			boolean isDirected) {
 		this.v1 = v1;
 		this.v2 = v2;
 		this.isDirected = new PCBoolean(false);
 		notifyVertices();
 	}
 
 	/**
 	 * private method to inform the vertices about the connection
 	 */
 	private void notifyVertices() {
 		if (!isDirected.getLiteralValue()) {
 			v2.addEdge(this, v1);
 		}
 		v1.addEdge(this, v2);
 	}
 
 	@Override
 	public String toString() {
 		String result = v1.toString();
 		if (isDirected.getLiteralValue()) {
 			result += " -> ";
 		} else {
 			result += " <-> ";
 		}
 		result += v2.toString();
 		return result;
 	}
 
 	@Override
 	public PCObject set(String memberName, PCObject value) {
 		// TODO check possible member access
 		return null;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (((PCEdge) toCheckAgainst).v1.equals(this.v1)
 				&& ((PCEdge) toCheckAgainst).v2.equals(this.v2)
 				&& ((PCEdge) toCheckAgainst).isDirected
 						.equals(this.isDirected)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public String getTypeName() {
 		return PCEdge.TYPENAME;
 	}
 }
