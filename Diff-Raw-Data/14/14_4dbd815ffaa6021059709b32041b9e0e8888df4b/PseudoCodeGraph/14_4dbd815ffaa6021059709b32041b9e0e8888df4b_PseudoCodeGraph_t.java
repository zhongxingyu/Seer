 package de.unisiegen.informatik.bs.alvis.graph.datatypes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import de.unisiegen.informatik.bs.alvis.primitives.Datatype;
 import de.unisiegen.informatik.bs.alvis.primitives.PseudoCodeList;
 
 public class PseudoCodeGraph extends Datatype {
 	protected static final String TYPENAME = "Graph";
 	private PseudoCodeList<PseudoCodeEdge> edges;
 	private PseudoCodeList<PseudoCodeVertex> vertices;
 
 	/**
 	 * Creates new Graph from PseudoCodeLists
 	 * 
 	 * @param vertices
 	 * @param edges
 	 */
 	public PseudoCodeGraph(PseudoCodeList<PseudoCodeVertex> vertices,
 			PseudoCodeList<PseudoCodeEdge> edges) {
 		this.edges = edges;
 		this.vertices = vertices;
 	}
 
 	/**
 	 * Creates new Graph from ArrayLists with PseudoCodeVertex and Edges
 	 * 
 	 * @param tmpvertices
 	 * @param tmpedges
 	 */
 	public PseudoCodeGraph(ArrayList<PseudoCodeVertex> tmpvertices,
 			ArrayList<PseudoCodeEdge> tmpedges) {
 		edges = new PseudoCodeList<PseudoCodeEdge>();
 		vertices = new PseudoCodeList<PseudoCodeVertex>();
 		for (PseudoCodeVertex vert : tmpvertices) {
 			vertices.add(vert);
 		}
 		for (PseudoCodeEdge ed : tmpedges) {
 			edges.add(ed);
 		}
 	}
 
 	/**
 	 * Creates new empty Graph
 	 */
 	public PseudoCodeGraph() {
 		edges = new PseudoCodeList<PseudoCodeEdge>();
 		vertices = new PseudoCodeList<PseudoCodeVertex>();
 	}
 
 	public PseudoCodeGraph(GraphicalRepresentationGraph gr) {
 
 	}
 
 	/**
 	 * Creates new Graph from HashSet GraphicalRepresentation of Nodes and
 	 * Vertices
 	 * 
 	 * @param allgNodes
 	 * @param allgConnections
 	 */
 	public PseudoCodeGraph(HashSet<GraphicalRepresentationVertex> allgNodes,
 			HashSet<GraphicalRepresentationEdge> allgConnections) {
 
 		edges = new PseudoCodeList<PseudoCodeEdge>();
 		vertices = new PseudoCodeList<PseudoCodeVertex>();
 		for (GraphicalRepresentationVertex node : allgNodes) {
 			vertices.add(new PseudoCodeVertex(node));
 		}
 		for (GraphicalRepresentationEdge conn : allgConnections) {
 			edges.add(new PseudoCodeEdge(this.getVertexFromGraphic((conn)
					.getSourceVertex()), this.getVertexFromGraphic((conn)
					.getDestinationVertex()), (conn)));
 		}
 	}
 
 	/**
 	 * returns Vertex from its GraphicalRepresentation
 	 * 
 	 * @param node
 	 * @return Vertex
 	 */
 	public PseudoCodeVertex getVertexFromGraphic(
 			GraphicalRepresentationVertex node) {
 		if (node == null) {
 			return null;
 		}
 		for (PseudoCodeVertex vnode : vertices) {
 			if (vnode.isGraphical(node)) {
 				return vnode;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @return the saved vertices as PseudoCodeList
 	 */
 	public PseudoCodeList<PseudoCodeVertex> getVertices() {
 		return this.vertices;
 	}
 
 	/**
 	 * 
 	 * @return the saved edges as PseudoCode
 	 */
 	public PseudoCodeList<PseudoCodeEdge> getEdges() {
 		return this.edges;
 	}
 
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Datatype set(String memberName, Datatype value) {
 		// TODO check possible member access
 		return null;
 	}
 
 	@Override
 	public Datatype get(String memberName) {
 		// TODO check possible member access
 		if (memberName.equals("vertices")) {
 			return vertices;
 		}
 		return null;
 	}
 
 	@Override
 	public Map<String, ? extends Datatype> getMembers() {
 		Map<String, Datatype> result = new HashMap<String, Datatype>();
 		result.put("vertices", this.vertices);
 		return result;
 	}
 
 	@Override
 	public boolean equals(Datatype toCheckAgainst) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
