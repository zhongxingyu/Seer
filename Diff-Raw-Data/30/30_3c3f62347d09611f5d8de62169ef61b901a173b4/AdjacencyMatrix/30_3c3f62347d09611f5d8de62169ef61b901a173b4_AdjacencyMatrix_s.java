 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 /**
  * AdjacencyMatrix.
  * 
  * @author not attributable
  * @version $Id$
  */
 
 public class AdjacencyMatrix implements Serializable {
 
 	private static final long serialVersionUID = 2986018836859246187L;
 
 	private static Log log = LogFactory.getLog(AdjacencyMatrix.class);
 
 	public static class EdgeInfo implements Serializable {
 		private static final long serialVersionUID = 8884626375173162244L;
 		public float value;
 		public String type;
 
 		EdgeInfo(float value, String type) {
 			this.value = value;
 			this.type = type;
 		}
 	}
 
 	public enum NodeType {
 		MARKER, GENE_SYMBOL, PROBESET_ID, STRING, NUMERIC, OTHER
 	};
 
 	// integer Id is not not used for now.
 	public static class Node {
 		public NodeType type;
 		public DSGeneMarker marker;
 		public String stringId;
 		public int intId;
 
 		public Node(DSGeneMarker marker) {
 			this.type = NodeType.MARKER;
 			this.marker = marker;
 			stringId = null;
 			intId = -1;
 		}
 
 		public Node(NodeType type, String id) {
 			this.type = type;
 			stringId = id;
 			intId = -1;
 			marker = null;
 		}
 
 		Node(NodeType type, int id) {
 			this.type = type;
 			intId = id;
 			stringId = null;
 			marker = null;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof Node))
 				return false;
 
 			Node node = (Node) obj;
 			if (node.type == NodeType.MARKER && this.type == NodeType.MARKER
 					&& node.marker.equals(this.marker)) {
 				return true;
 			} else if (node.type == this.type
 					&& node.stringId.equals(this.stringId))
 				return true;
 			else
 				return false;
 		}
 	}
 
 	private HashMap<Node, HashMap<Node, EdgeInfo>> geneRows = new HashMap<Node, HashMap<Node, EdgeInfo>>();
 
 	private final DSMicroarraySet<DSMicroarray> maSet;
 
 	private final String name;
 
 	private final Map<String, String> interactionTypeSifMap; // TODO check ?
 
 	private int edgeNumber = 0;
 	private int nodeNumber = 0;
 
 	public AdjacencyMatrix(String name,
 			final DSMicroarraySet<DSMicroarray> microarraySet) {
 		this.name = name;
 		maSet = microarraySet;
 		interactionTypeSifMap = null;
 		log.debug("AdjacencyMatrix created with label " + name
 				+ " and microarray set " + maSet.getDataSetName());
 	}
 
 	public AdjacencyMatrix(String name,
 			final DSMicroarraySet<DSMicroarray> microarraySet,
 			Map<String, String> interactionTypeSifMap) {
 		this.name = name;
 		maSet = microarraySet;
 		this.interactionTypeSifMap = interactionTypeSifMap;
 		log.debug("AdjacencyMatrix created with label " + name
 				+ " and microarray set " + maSet.getDataSetName()
 				+ ", with interaction type map");
 	}
 
 	/**
 	 * Returns a map with all the edges to geneId. This is only used by MRA
 	 * analysis.
 	 * 
 	 */
 	// this replace the original public HashMap<Integer, Float> get(int geneId)
 	public Set<DSGeneMarker> get(DSGeneMarker marker) {
 		Set<DSGeneMarker> set = new HashSet<DSGeneMarker>();
 		HashMap<Node, EdgeInfo> row = geneRows.get(new Node(marker));
 		if (row == null) {
 			return null;
 		}
 		for (Node id : row.keySet()) {
 			if (id.type == NodeType.MARKER)
 				set.add(id.marker);
 		}
 		return set;
 	}
 
 	/**
 	 * Add a node only. This is useful only when there is no edged from this
 	 * node.
 	 * 
 	 * @param geneId
 	 */
 	// this replaces public void addGeneRow(int geneId) {
 	public void addGeneRow(Node node) {
 		HashMap<Node, EdgeInfo> row = geneRows.get(node);
 		if (row == null) {
 			geneRows.put(node, new HashMap<Node, EdgeInfo>());
 			nodeNumber++;
 		}
 	}
 
 	public String getLabel() {
 		return name;
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2, indexes into the microarray
 	 * dataset
 	 * 
 	 * @param geneId1
 	 *            int
 	 * @param geneId2
 	 *            int
 	 * @param edge
 	 *            float
 	 */
 	/*
 	 * This replaces two methods: public void add(int geneId1, int geneId2,
 	 * float edge, String interaction) {
 	 * 
 	 * public void add(String geneId1, String geneId2, boolean
 	 * isGene1InMicroarray, boolean isGene2InMicroarray, float edge, String
 	 * interaction) {
 	 * 
 	 * the getMappedId functionality is supported by the node itself
 	 */
 	public void add(Node node1, Node node2, float edge, String interaction) {
 
 		HashMap<Node, EdgeInfo> row = geneRows.get(node1);
 		if (row == null) {
 			row = new HashMap<Node, EdgeInfo>();
 			geneRows.put(node1, row);
 			nodeNumber++;
 		}
 		row.put(node2, new EdgeInfo(edge, interaction));
 
 		// doing it both ways; [gene2 -> (gene1, edge)]
 		row = geneRows.get(node2);
 		if (row == null) {
 			row = new HashMap<Node, EdgeInfo>();
 			geneRows.put(node2, row);
 			nodeNumber++;
 		}
 		row.put(node1, new EdgeInfo(edge, interaction));
 
 		edgeNumber++;
 	}
 
 	// this method is not needed any more
 	// public int getMappedId(int geneId)
 	// }
 
 	public int getConnectionNo() {
 		return edgeNumber;
 	}
 
 	public int getNodeNumber() {
 		return nodeNumber;
 	}
 
 	public DSMicroarraySet<DSMicroarray> getMicroarraySet() {
 		return maSet;
 	}
 
 	public Map<String, String> getInteractionTypeSifMap() {
 		return interactionTypeSifMap;
 	}
 
 	public static class Edge {
 		public Node node1;
 		public Node node2;
 		public EdgeInfo info;
 
 		Edge(Node node1, Node node2, EdgeInfo info) {
 			this.node1 = node1;
 			this.node2 = node2;
 			this.info = info;
 		}
 	}
 
 	/**
 	 * Edge for those not from the input microarray dataset.
 	 * 
 	 */
 	public static class EdgeWithStringNode {
 		public String node1;
 		public String node2;
 		public EdgeInfo info;
 
 		EdgeWithStringNode(String node1, String node2, EdgeInfo info) {
 			this.node1 = node1;
 			this.node2 = node2;
 			this.info = info;
 		}
 	}
 
 	/**
 	 * 
 	 * @return all edges
 	 */
 	public List<Edge> getEdges() {
 		List<Edge> list = new ArrayList<Edge>();
 		for (Node node1 : geneRows.keySet()) {
 			Map<Node, AdjacencyMatrix.EdgeInfo> destGenes = geneRows.get(node1);
 			for (Node node2 : destGenes.keySet()) {
 				list.add(new Edge(node1, node2, destGenes.get(node2)));
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * 
 	 * @return edges from a given node
 	 */
 	// replace public List<Edge> getEdges(int node1) {
 	// replace as well public List<EdgeWithStringNode>
 	// getEdgesNotInMicroarray(String node1) {
 	public List<Edge> getEdges(Node node1) {
 		List<Edge> list = new ArrayList<Edge>();
 		Map<Node, AdjacencyMatrix.EdgeInfo> destGenes = geneRows.get(node1);
 		for (Node node2 : destGenes.keySet()) {
 			list.add(new Edge(node1, node2, destGenes.get(node2)));
 		}
 
 		return list;
 	}
 
 	// replace public List<Integer> getNodes() and public List<String>
 	// getNodesNotInMicroarray()
 	public List<Node> getNodes() {
 		return new ArrayList<Node>(geneRows.keySet());
 	}
 
 }
