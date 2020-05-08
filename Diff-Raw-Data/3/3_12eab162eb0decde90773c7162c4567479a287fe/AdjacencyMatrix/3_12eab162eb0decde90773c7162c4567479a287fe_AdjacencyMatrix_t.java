 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 
 	private static final long serialVersionUID = -4163326138016520666L;
 
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
 	
 	private HashMap<Integer, HashMap<Integer, EdgeInfo>> geneRows = new HashMap<Integer, HashMap<Integer, EdgeInfo>>();
 	private HashMap<String, HashMap<String, EdgeInfo>> geneRowsNotInMicroarray = new HashMap<String, HashMap<String, EdgeInfo>>();
 
 	private Map<String, Integer> idToGeneMapper = new HashMap<String, Integer>();
 	private Map<String, Integer> snToGeneMapper = new HashMap<String, Integer>();
 
 	private final DSMicroarraySet<DSMicroarray> maSet;
 
 	private final String name;
 
 	private final Map<String, String> interactionTypeSifMap;
 
 	private int edgeNumber = 0;
 	private int nodeNumber = 0;
 	private int nodeNumberNotInMicroarray = 0;
 
 	public AdjacencyMatrix(String name, final DSMicroarraySet<DSMicroarray> microarraySet) {
 		this.name = name;
 		maSet = microarraySet;
 		interactionTypeSifMap = null;
 		log.debug("AdjacencyMatrix created with label "+name+" and microarray set "+maSet.getDataSetName());
 	}
 	
 	public AdjacencyMatrix(String name, final DSMicroarraySet<DSMicroarray> microarraySet, Map<String, String> interactionTypeSifMap) {
 		this.name = name;
 		maSet = microarraySet;
 		this.interactionTypeSifMap = interactionTypeSifMap;
 		log.debug("AdjacencyMatrix created with label "+name+" and microarray set "+maSet.getDataSetName()+", with interaction type map");
 	}
 
 	/**
 	 * Returns a map with all the edges to geneId.
 	 * This is only used by MRA analysis.
 	 * 
 	 * @param geneId
 	 *            int
 	 * @return HashMap
 	 */
 	public HashMap<Integer, Float> get(int geneId) {
 		geneId = getMappedId(geneId);
 		if (geneId > 0) {
 			HashMap<Integer, Float> map = new HashMap<Integer, Float>();
 			HashMap<Integer, EdgeInfo> row = geneRows.get(new Integer(geneId));
			if(row==null) {
				return null;
			}
 			for(Integer id: row.keySet()) {
 				EdgeInfo e = row.get(id);
 				map.put(id, e.value);
 			}
 			return map;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Add a node only. 
 	 * This is useful only when there is no edged from this node.
 	 * 
 	 * @param geneId
 	 */
 	public void addGeneRow(int geneId) {
 		HashMap<Integer, EdgeInfo> row = geneRows.get(new Integer(geneId));
 		if (row == null) {
 			row = new HashMap<Integer, EdgeInfo>();
 			geneRows.put(new Integer(geneId), row);
 			nodeNumber++;
 		}
 	}
 
 	public String getLabel() {
 		return name;
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2, indexes into the microarray dataset
 	 * 
 	 * @param geneId1
 	 *            int
 	 * @param geneId2
 	 *            int
 	 * @param edge
 	 *            float
 	 */
 	public void add(int geneId1, int geneId2, float edge, String interaction) {
 		geneId1 = getMappedId(geneId1);
 		geneId2 = getMappedId(geneId2);
 
 		HashMap<Integer, EdgeInfo> row = geneRows.get(new Integer(geneId1));
 		if (row == null) {
 			row = new HashMap<Integer, EdgeInfo>();
 			geneRows.put(new Integer(geneId1), row);
 			nodeNumber++;
 		}
 		row.put(new Integer(geneId2), new EdgeInfo(edge, interaction) );
 
 		// doing it both ways; [gene2 -> (gene1, edge)]
 		row = geneRows.get(new Integer(geneId2));
 		if (row == null) {
 			row = new HashMap<Integer, EdgeInfo>();
 			geneRows.put(new Integer(geneId2), row);
 			nodeNumber++;
 		}
 		row.put(new Integer(geneId1), new EdgeInfo(edge, interaction) );
 
 		edgeNumber++;
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2, gene names that are not part of the microarray dataset
 	 * 
 	 * @param geneId1
 	 *            String
 	 * @param geneId2
 	 *            String
 	 * @param edge
 	 *            float
 	 */
 	public void add(String geneId1, String geneId2,
 			boolean isGene1InMicroarray, boolean isGene2InMicroarray, float edge, String interaction) {
 
 		if (isGene1InMicroarray == true)
 			geneId1 = String.valueOf(getMappedId(new Integer(geneId1)));
 		if (isGene2InMicroarray == true)
 			geneId2 = String.valueOf(getMappedId(new Integer(geneId2)));
 
 		HashMap<String, EdgeInfo> row = (HashMap<String, EdgeInfo>) geneRowsNotInMicroarray
 				.get(geneId1);
 		if (row == null) {
 			row = new HashMap<String, EdgeInfo>();
 			geneRowsNotInMicroarray.put(geneId1, row);
 			nodeNumberNotInMicroarray++;
 		}
 		row.put(new String(geneId2), new EdgeInfo(edge, interaction));
 
 		// doing it both ways; [gene2 -> (gene1, edge)]
 		row = (HashMap<String, EdgeInfo>) geneRowsNotInMicroarray.get(geneId2);
 		if (row == null) {
 			row = new HashMap<String, EdgeInfo>();
 			geneRowsNotInMicroarray.put(geneId2, row);
 			nodeNumberNotInMicroarray++;
 		}
 		row.put(geneId1, new EdgeInfo(edge, interaction));
 
 		edgeNumber++;
 	}
 
 	/**
 	 * Return an index to the microarray set's marker list.
 	 * 
 	 * If the index points to a marker that has a previously seen label (probeset ID)
 	 * or a previously seen gene name (gene symbol), return the index of the previously seen one;
 	 * if it is a new one, return the input.
 	 * 
 	 * @param geneId - an index to the microarray set's marker list
 	 * @return an different index if the probeset or the gene symbol was seen before.
 	 */
 	public int getMappedId(int geneId) {
 		if (geneId < 0 || geneId >= maSet.getMarkers().size())
 			return geneId; // garbage in, garbage out
 
 		DSGeneMarker gm = maSet.getMarkers().get(geneId);
 		// bug 2000, replaced getShortName() with getGeneName()
 		String geneName = gm.getGeneName();
 		if (geneName == null || geneName.trim().length() == 0 || geneName.equals("---")) {
 			return geneId;
 		}
 
 		String label = gm.getLabel();
 		// Test if a gene with the same label was mapped before.
 		Integer prevId = (Integer) idToGeneMapper.get(label);
 		if (prevId != null) {
 			// This gene was mapped before. Replace with mapped one
 			return prevId.intValue();
 		} 
 		
 		// Test if a gene with the same name was reported before.
 		prevId = (Integer) snToGeneMapper.get(geneName);
 		if (prevId != null) {
 			// There was a previous gene with the same name.
 			// add a new mapping to idToGeneMapper
 			idToGeneMapper.put(label, prevId);
 			return prevId;
 		} else { // new name never seen before
 			snToGeneMapper.put(geneName, new Integer(geneId));
 			idToGeneMapper.put(label, new Integer(geneId));
 			return geneId;
 		}
 	}
 
 	public int getConnectionNo() {
 		return edgeNumber ;
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
 		public int node1;
 		public int node2;
 		public EdgeInfo info;
 		
 		Edge(int node1, int node2, EdgeInfo info) {
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
 		for (Integer node1 : geneRows.keySet()) {
 			Map<Integer, AdjacencyMatrix.EdgeInfo> destGenes = geneRows
 					.get(node1);
 			for (Integer node2 : destGenes.keySet()) {
 				list.add(new Edge(node1, node2, destGenes.get(node2)));
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * 
 	 * @return edges from a given node
 	 */
 	public List<Edge> getEdges(int node1) {
 		List<Edge> list = new ArrayList<Edge>();
 		Map<Integer, AdjacencyMatrix.EdgeInfo> destGenes = geneRows.get(node1);
 		for (Integer node2 : destGenes.keySet()) {
 			list.add(new Edge(node1, node2, destGenes.get(node2)));
 		}
 
 		return list;
 	}
 
 	public List<Integer> getNodes() {
 		return new ArrayList<Integer>(geneRows.keySet());
 	}
 
 	public int getNodeNumberNotInMicroarray() {
 		return nodeNumberNotInMicroarray ;
 	}
 
 	/**
 	 * 
 	 * @return edges from a given node for those not in input microarray dataset
 	 */
 	public List<EdgeWithStringNode> getEdgesNotInMicroarray(String node1) {
 		List<EdgeWithStringNode> list = new ArrayList<EdgeWithStringNode>();
 		Map<String, AdjacencyMatrix.EdgeInfo> destGenes = geneRowsNotInMicroarray.get(node1);
 		for (String node2 : destGenes.keySet()) {
 			list.add(new EdgeWithStringNode(node1, node2, destGenes.get(node2)));
 		}
 
 		return list;
 	}
 
 	public List<String> getNodesNotInMicroarray() {
 		return new ArrayList<String>(geneRowsNotInMicroarray.keySet());
 	}
 }
