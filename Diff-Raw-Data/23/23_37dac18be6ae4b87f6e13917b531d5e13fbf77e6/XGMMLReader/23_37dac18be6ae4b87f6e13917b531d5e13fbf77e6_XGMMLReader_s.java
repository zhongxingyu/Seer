 /*
  File: XGMMLReader.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute of Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.data.readers;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.model.RootGraph;
 import giny.view.EdgeView;
 import giny.view.GraphView;
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import cern.colt.list.IntArrayList;
 import cern.colt.map.OpenIntIntHashMap;
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.writers.XGMMLWriter;
 import cytoscape.generated2.Att;
 import cytoscape.generated2.Date;
 import cytoscape.generated2.Description;
 import cytoscape.generated2.Format;
 import cytoscape.generated2.Graph;
 import cytoscape.generated2.Graphics;
 import cytoscape.generated2.Identifier;
 import cytoscape.generated2.RdfDescription;
 import cytoscape.generated2.RdfRDF;
 import cytoscape.generated2.Source;
 import cytoscape.generated2.Title;
 import cytoscape.generated2.Type;
 import cytoscape.generated2.impl.AttImpl;
 import cytoscape.visual.LineType;
 
 /**
  * XGMMLReader. This version is Metanode-compatible.
  * 
  * @version 1.0
  * @since Cytoscape 2.3
  * @see cytoscape.data.writers.XGMMLWriter
  * @author kono
  * 
  */
 public class XGMMLReader implements GraphReader {
 
 	private static final String METADATA_ATTR_NAME = "Network Metadata";
 	// Graph Tags
 	protected static final String GRAPH = "graph";
 	protected static final String NODE = "node";
 	protected static final String EDGE = "edge";
 	protected static final String GRAPHICS = "graphics";
 	protected static final String LABEL = "label";
 	protected static final String SOURCE = "source";
 	protected static final String TARGET = "target";
 
 	// Shapes used in Cytoscape (not GML standard)
 	// In GML, they are called "type"
 	protected static final String RECTANGLE = "rectangle";
 	protected static final String ELLIPSE = "ellipse";
 	protected static final String LINE = "Line"; // This is the Polyline object.
 	protected static final String POINT = "point";
 	protected static final String DIAMOND = "diamond";
 	protected static final String HEXAGON = "hexagon";
 	protected static final String OCTAGON = "octagon";
 	protected static final String PARALELLOGRAM = "parallelogram";
 	protected static final String TRIANGLE = "triangle";
 
 	// These types are permitted by the XGMML standard
 	protected static final String FLOAT_TYPE = "real";
 	protected static final String INT_TYPE = "integer";
 	protected static final String STRING_TYPE = "string";
 	protected static final String LIST_TYPE = "list";
 
 	// These types are not permitted by the XGMML standard
 	protected static final String BOOLEAN_TYPE = "boolean";
 	protected static final String MAP_TYPE = "map";
 	protected static final String COMPLEX_TYPE = "complex";
 
 	// XGMML file name to be loaded.
 	private String networkName;
 
 	// Package name generated by JAXB based on XGMML schema file
 	private final String XGMML_PACKAGE = "cytoscape.generated2";
 
 	private List nodes;
 	private List edges;
 
 	private RdfRDF metadata;
 	private String backgroundColor;
 	private Double graphViewZoom;
 	private Double graphViewCenterX;
 	private Double graphViewCenterY;
 
 	InputStream networkStream;
 
 	OpenIntIntHashMap nodeIDMap;
 	IntArrayList giny_nodes, giny_edges;
 
 	ArrayList nodeIndex, edgeIndex;
 
 	Graph network;
 
 	ArrayList rootNodes;
 
 	HashMap metanodeMap;
 
 	CyAttributes nodeAttributes;
 	CyAttributes edgeAttributes;
 	CyAttributes networkCyAttributes;
 
 	HashMap nodeGraphicsMap;
 	HashMap edgeGraphicsMap;
 
 	private HashMap nodeMap;
 
 	Properties prop = CytoscapeInit.getProperties();
 	String vsbSwitch = prop.getProperty("visualStyleBuilder");
 
 	public XGMMLReader(String fileName) {
 
 		try {
 			// System.out.println("Opening stream for " + fileName);
 			networkStream = new FileInputStream(fileName);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.networkName = null;
 
 		this.metanodeMap = new HashMap();
 		this.nodeGraphicsMap = new HashMap();
 		this.edgeGraphicsMap = new HashMap();
 	}
 
 	public XGMMLReader(InputStream is) {
 
 		this.networkName = null;
 		this.networkStream = is;
 
 		this.metanodeMap = new HashMap();
 		this.nodeGraphicsMap = new HashMap();
 		this.edgeGraphicsMap = new HashMap();
 	}
 
 	public void read() throws IOException {
 		try {
 			this.readXGMML();
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void read(boolean canonicalizeNodeNames) throws IOException {
 		// TODO Auto-generated method stub
 		try {
 			this.readXGMML();
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Actual method to unmarshall XGMML documents.
 	 * 
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	private void readXGMML() throws JAXBException, IOException {
 
 		nodeAttributes = Cytoscape.getNodeAttributes();
 		edgeAttributes = Cytoscape.getEdgeAttributes();
 		networkCyAttributes = Cytoscape.getNetworkAttributes();
 
 		// Use JAXB-generated methods to create data structure
		JAXBContext jc = JAXBContext.newInstance(XGMML_PACKAGE);
 		// Unmarshall the XGMML file
 		Unmarshaller u = jc.createUnmarshaller();
 
 		// Read the file and map the entire XML document into
 		// data structure.
 		network = (Graph) u.unmarshal(networkStream);
 		networkName = network.getLabel();
 
 		rootNodes = new ArrayList();
 
 		// Get all nodes and edges as one List object.
 		List nodesAndEdges = network.getNodeOrEdge();
 
 		// Split the list into two: node and edge list
 		nodes = new ArrayList();
 		edges = new ArrayList();
 		Iterator it = nodesAndEdges.iterator();
 		while (it.hasNext()) {
 			Object curObj = it.next();
 			if (curObj.getClass() == cytoscape.generated2.impl.NodeImpl.class) {
 				nodes.add(curObj);
 			} else {
 				edges.add(curObj);
 			}
 		}
 
 		// Build the network
 		createGraph(network);
 	}
 
 	/**
 	 * Create graph directly from JAXB objects
 	 * 
 	 * @param network
 	 */
 	protected void createGraph(Graph network) {
 
 		// Check capacity
 		Cytoscape.ensureCapacity(nodes.size(), edges.size());
 
 		// Extract nodes
 		nodeIDMap = new OpenIntIntHashMap(nodes.size());
 		giny_nodes = new IntArrayList(nodes.size());
 		// OpenIntIntHashMap gml_id2order = new OpenIntIntHashMap(nodes.size());
 
 		HashMap gml_id2order = new HashMap(nodes.size());
 
 		Set nodeNameSet = new HashSet(nodes.size());
 
 		nodeMap = new HashMap(nodes.size());
 
 		// Add All Nodes to Network
 		for (int idx = 0; idx < nodes.size(); idx++) {
 
 			// Get a node object (NOT a giny node. XGMML node!)
 			cytoscape.generated2.Node curNode = (cytoscape.generated2.Node) nodes
 					.get(idx);
 
 			String nodeType = curNode.getName();
 
 			String label = (String) curNode.getLabel();
 
 			readAttributes(label, curNode.getAtt(), NODE);
 
 			// System.out.println("Node Name: " + label + ", ID is "
 			// + Cytoscape.getCyNode(label, true).getRootGraphIndex());
 
 			nodeMap.put(curNode.getId(), label);
 
 			// nodeMap.put(curNode.getId(), label);
 			if (nodeType != null) {
 				if (nodeType.equals("metaNode")) {
 
 					// List of child nodes under this parent node.
 					List children = null;
 
 					Iterator it = curNode.getAtt().iterator();
 					while (it.hasNext()) {
 						Att curAttr = (Att) it.next();
 
 						if (curAttr.getName().equals("metanodeChildren")) {
 							Graph subgraph = (Graph) curAttr.getContent()
 									.get(0);
 							children = subgraph.getNodeOrEdge();
 							metanodeMap.put(label, children);
 						}
 					}
 				}
 
 			}
 			if (nodeNameSet.add(curNode.getId())) {
 				Node node = (Node) Cytoscape.getCyNode(label, true);
 
 				giny_nodes.add(node.getRootGraphIndex());
 				nodeIDMap.put(idx, node.getRootGraphIndex());
 
 				// gml_id2order.put(Integer.parseInt(curNode.getId()), idx);
 
 				gml_id2order.put(curNode.getId(), Integer.toString(idx));
 
 				// ((KeyValue) node_root_index_pairs.get(idx)).value = (new
 				// Integer(
 				// node.getRootGraphIndex()));
 			} else {
 				throw new GMLException("GML id " + nodes.get(idx)
 						+ " has a duplicated label: " + label);
 				// ((KeyValue)node_root_index_pairs.get(idx)).value = null;
 			}
 		}
 		nodeNameSet = null;
 
 		// Extract edges
 		giny_edges = new IntArrayList(edges.size());
 		Set edgeNameSet = new HashSet(edges.size());
 
 		CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 
 		// Add All Edges to Network
 		for (int idx = 0; idx < edges.size(); idx++) {
 
 			cytoscape.generated2.Edge curEdge = (cytoscape.generated2.Edge) edges
 					.get(idx);
 
 			// if
 			// (gml_id2order.containsKey(Integer.parseInt(curEdge.getSource()))
 			// && gml_id2order.containsKey(Integer.parseInt(curEdge
 			// .getTarget()))) {
 
 			if (gml_id2order.containsKey(curEdge.getSource())
 					&& gml_id2order.containsKey(curEdge.getTarget())) {
 
 				// String label = curEdge.getLabel();
 
 				String edgeName = curEdge.getLabel();
 
 				if (edgeName == null) {
 					edgeName = "N/A";
 				}
 
 				int duplicate_count = 1;
 				while (!edgeNameSet.add(edgeName)) {
 					edgeName = edgeName + "_" + duplicate_count;
 					duplicate_count += 1;
 				}
 
 				Edge edge = Cytoscape.getRootGraph().getEdge(edgeName);
 
 				if (edge == null) {
 
 					String sourceName = (String) nodeMap.get(curEdge
 							.getSource());
 					String targetName = (String) nodeMap.get(curEdge
 							.getTarget());
 
 					Node node_1 = Cytoscape.getRootGraph().getNode(sourceName);
 					Node node_2 = Cytoscape.getRootGraph().getNode(targetName);
 
 					// edge = Cytoscape.getCyEdge(node_1, node_2,
 					// Semantics.INTERACTION, edgeName, true);
 
 					Iterator it = curEdge.getAtt().iterator();
 					Att interaction = null;
 					String itrValue = "pp";
 					while (it.hasNext()) {
 						interaction = (Att) it.next();
 						if (interaction.getName().equals("interaction")) {
 							itrValue = interaction.getValue();
 							if (itrValue == null) {
 								itrValue = "pp";
 							}
 
 							break;
 						}
 					}
 
 					edge = Cytoscape.getCyEdge(node_1, node_2,
 							Semantics.INTERACTION, itrValue, true);
 
 					// Add interaction to CyAttributes
 					edgeAttributes.setAttribute(edge.getIdentifier(),
 							Semantics.INTERACTION, itrValue);
 				}
 
 				// Set correct ID, canonical name and interaction name
 				edge.setIdentifier(edgeName);
 				// System.out.println("Edge Data: " + edge.getIdentifier());
 
 				readAttributes(edgeName, curEdge.getAtt(), EDGE);
 
 				giny_edges.add(edge.getRootGraphIndex());
 				// ((KeyValue) edge_root_index_pairs.get(idx)).value = (new
 				// Integer(
 				// edge.getRootGraphIndex()));
 			} else {
 				throw new GMLException(
 						"Non-existant source/target node for edge with gml (source,target): "
 								+ nodeMap.get(curEdge.getSource()) + ","
 								+ nodeMap.get(curEdge.getTarget()));
 			}
 		}
 		edgeNameSet = null;
 		//
 		// Iterator nit = Cytoscape.getRootGraph().nodesIterator();
 		// while (nit.hasNext()) {
 		// CyNode testnode = (CyNode) nit.next();
 		// // System.out.println("ROOT LIST: " + testnode.getIdentifier());
 		// }
 
 		if (metanodeMap.size() != 0) {
 
 			Set keySet = metanodeMap.keySet();
 			Iterator it = keySet.iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				createMetaNode(key, (List) metanodeMap.get(key));
 			}
 		}
 
 	}
 
 	private CyNode createMetaNode(String name, List children) {
 		Iterator it = children.iterator();
 		// System.out.println("Metanode = " + name);
 
 		int[] childrenNodeIndices = new int[children.size()];
 		int counter = 0;
 
 		while (it.hasNext()) {
 
 			cytoscape.generated2.Node childNode = (cytoscape.generated2.Node) it
 					.next();
 
 			Node targetChildNode = Cytoscape.getRootGraph().getNode(
 					childNode.getId());
 
 			// System.out.println("+------- child = "
 			// + targetChildNode.getRootGraphIndex() + ": "
 			// + targetChildNode.getIdentifier());
 
 			childrenNodeIndices[counter] = targetChildNode.getRootGraphIndex();
 			counter++;
 
 		}
 
 		int[] edgeIndices = Cytoscape.getRootGraph()
 				.getConnectingEdgeIndicesArray(childrenNodeIndices);
 
 		if (edgeIndices != null) {
 			for (int i = 0; i < edgeIndices.length; i++) {
 
 				// System.out.println("!! Edge: "
 				// + Cytoscape.getRootGraph().getEdge(edgeIndices[i])
 				// .getIdentifier());
 
 				// if (edgeIndices[i] > 0) {
 				// int rootEdgeIndex =
 				// Cytoscape.getRootGraph().getgetRootGraphEdgeIndex(edgeIndices[i]);
 				// edgeIndices[i] = rootEdgeIndex;
 				// }// if rootEdgeIndex > 0
 			}// for i
 		}
 
 		rootNodes.add(Cytoscape.getRootGraph().getNode(name));
 
 		return null;
 	}
 
 	public List getMetanodeList() {
 		return rootNodes;
 	}
 
 	protected void readAttributes() {
 
 	}
 
 	protected void readNode() {
 
 	}
 
 	protected void readEdge() {
 
 	}
 
 	/**
 	 * Returns the zoom level read from the the xgmml file.
 	 * 
 	 * @return Double
 	 */
 	public Double getGraphViewZoomLevel() {
 
 		// lets be explicit
 		return (graphViewZoom != null) ? graphViewZoom : null;
 	}
 
 	/**
 	 * Returns the graph view center.
 	 * 
 	 * @return Double
 	 */
 	public Point2D getGraphViewCenter() {
 
 		// be explicit
 		return (graphViewCenterX != null && graphViewCenterY != null) ? new Point2D.Double(
 				graphViewCenterX.doubleValue(), graphViewCenterY.doubleValue())
 				: null;
 	}
 
 	/**
 	 * Based on the graphic attribute, layout the graph.
 	 * 
 	 * @param myView
 	 */
 
 	public void layout(GraphView myView) {
 		if (myView == null || myView.nodeCount() == 0) {
 			return;
 		}
 
 		// Set background clolor
 		if (backgroundColor != null) {
 			myView.setBackgroundPaint(getColor(backgroundColor));
 		}
 		// myView.setBackgroundPaint(new Color(230, 230, 240));
 		// Layout nodes
 		layoutNode(myView);
 
 		// Generate Visual Style
 
 		// Layout edges
 		layoutEdge(myView);
 
 		// Build Style if needed.
 		if (vsbSwitch != null) {
 			if (vsbSwitch.equals("on")) {
 				VisualStyleBuilder vsb = new VisualStyleBuilder(networkName
 						+ ".style", nodeGraphicsMap, edgeGraphicsMap, null);
 				vsb.buildStyle();
 			}
 		}
 
 	}
 
 	/**
 	 * Layout nodes
 	 * 
 	 */
 	protected void layoutNode(GraphView myView) {
 
 		String label = null;
 		int tempid = 0;
 
 		NodeView view = null;
 
 		for (Iterator it = nodes.iterator(); it.hasNext();) {
 			// Extract a node from JAXB-generated object
 			cytoscape.generated2.Node curNode = (cytoscape.generated2.Node) it
 					.next();
 
 			label = curNode.getLabel();
 			Graphics graphics = (Graphics) curNode.getGraphics();
 
 			nodeGraphicsMap.put(label, graphics);
 
 			int nodeID = Cytoscape.getRootGraph().getNode(label)
 					.getRootGraphIndex();
 
 			view = myView.getNodeView(nodeID);
 
 			if (label != null && view != null) {
 				view.getLabel().setText(label);
 
 			} else if (view != null) {
 				view.getLabel().setText("node(" + tempid + ")");
 			}
 
 			if (graphics != null && view != null) {
 				layoutNodeGraphics(myView, graphics, view);
 			} else if (graphics == null) {
 				System.out.println("Graphics info is not available for "
 						+ view.getLabel().getText());
 			}
 
 			// layoutNodeGraphics(myView, graphics, view);
 
 		}
 	}
 
 	//
 	// Extract node graphics information and display it.
 	//
 	protected void layoutNodeGraphics(GraphView myView, Graphics graphics,
 			NodeView nodeView) {
 
 		// Location and size of the node
 		double x, y, h, w;
 
 		x = graphics.getX();
 		y = graphics.getY();
 		h = graphics.getH();
 		w = graphics.getW();
 
 		nodeView.setXPosition(x);
 		nodeView.setYPosition(y);
 		// nodeView.setHeight(h * (rdn.nextInt(4)+1) * rdn.nextGaussian());
 		// nodeView.setWidth(w * (rdn.nextInt(4)+1) * rdn.nextGaussian());
 
 		nodeView.setHeight(h);
 		nodeView.setWidth(w);
 
 		// Set color
 		String nodeColor = graphics.getFill();
 		nodeView.setUnselectedPaint(getColor(nodeColor));
 
 		// Set border line
 		nodeView.setBorderPaint(getColor(graphics.getOutline()));
 		if (graphics.getWidth() != null) {
 			nodeView.setBorderWidth(graphics.getWidth().floatValue());
 			// nodeView.setBorderWidth(rdn.nextInt(10));
 		}
 
 		String type = graphics.getType();
 
 		if (type != null) {
 
 			if (type.equals(ELLIPSE)) {
 				nodeView.setShape(NodeView.ELLIPSE);
 			} else if (type.equals(RECTANGLE)) {
 				nodeView.setShape(NodeView.RECTANGLE);
 			} else if (type.equals(DIAMOND)) {
 				nodeView.setShape(NodeView.DIAMOND);
 			} else if (type.equals(HEXAGON)) {
 				nodeView.setShape(NodeView.HEXAGON);
 			} else if (type.equals(OCTAGON)) {
 				nodeView.setShape(NodeView.OCTAGON);
 			} else if (type.equals(PARALELLOGRAM)) {
 				nodeView.setShape(NodeView.PARALELLOGRAM);
 			} else if (type.equals(TRIANGLE)) {
 				nodeView.setShape(NodeView.TRIANGLE);
 			}
 
 		}
 
 		// This object includes non-GML graphics property.
 
 		if (graphics.getAtt().size() != 0) {
 			Att localGraphics = (Att) graphics.getAtt().get(0);
 			Iterator it = localGraphics.getContent().iterator();
 
 			// Extract edge graphics attributes one by one.
 			while (it.hasNext()) {
 				Object obj = it.next();
 
 				if (obj.getClass() == AttImpl.class) {
 					AttImpl nodeGraphics = (AttImpl) obj;
 					String attName = nodeGraphics.getName();
 					String value = nodeGraphics.getValue();
 
 					if (attName.equals("nodeTransparency")) {
 						nodeView.setTransparency(Float.parseFloat(value));
 					}
 				}
 			}
 		}
 
 	}
 
 	protected void layoutEdge(GraphView myView) {
 
 		EdgeView view = null;
 
 		// Extract an edge from JAXB-generated object
 		for (Iterator it = edges.iterator(); it.hasNext();) {
 			cytoscape.generated2.Edge curEdge = (cytoscape.generated2.Edge) it
 					.next();
 
 			Graphics graphics = (Graphics) curEdge.getGraphics();
 			String edgeID = curEdge.getId();
 
 			edgeGraphicsMap.put(edgeID, graphics);
 
 			// System.out.println("Edge info@@@: " + edgeID +", " +
 			// curEdge.getSource() + "-" + curEdge.getTarget());
 
 			int rootindex = 0;
 			view = null;
 			CyEdge testEdge = Cytoscape.getRootGraph().getEdge(edgeID);
 			if (testEdge != null) {
 				rootindex = testEdge.getRootGraphIndex();
 				view = myView.getEdgeView(rootindex);
 			} else {
 				String sourceNodeName = (String) nodeMap.get(curEdge
 						.getSource());
 				String targetNodeName = (String) nodeMap.get(curEdge
 						.getTarget());
 
 				Iterator itrIt = curEdge.getAtt().iterator();
 				Att interaction = null;
 				String itrValue = "pp";
 				while (itrIt.hasNext()) {
 					interaction = (Att) itrIt.next();
 					if (interaction.getName().equals("interaction")) {
 						itrValue = interaction.getValue();
 
 						break;
 					}
 				}
 				edgeID = sourceNodeName + " (" + itrValue + ") "
 						+ targetNodeName;
 				System.out.println("Edge info2: " + edgeID);
 				testEdge = Cytoscape.getRootGraph().getEdge(edgeID);
 				if (testEdge != null) {
 					rootindex = testEdge.getRootGraphIndex();
 					view = myView.getEdgeView(rootindex);
 				}
 
 			}
 
 			if (graphics != null && view != Cytoscape.getNullNetworkView()) {
 				layoutEdgeGraphics(myView, graphics, view);
 			}
 		}
 
 	}
 
 protected void layoutEdgeGraphics(GraphView myView, Graphics graphics,
 			EdgeView edgeView) {
 
 		edgeView.setStrokeWidth(((Number) graphics.getWidth()).floatValue());
 		edgeView.setUnselectedPaint(getColor((String) graphics.getFill()));
 
 		if (graphics.getAtt().size() != 0) {
 			// This object includes non-GML graphics property.
 			Att localGraphics = (Att) graphics.getAtt().get(0);
 
 			Iterator it = localGraphics.getContent().iterator();
 
 			// Extract edge graphics attributes one by one.
 			while (it.hasNext()) {
 				Object obj = it.next();
 
 				if (obj.getClass() == AttImpl.class) {
 					AttImpl edgeGraphics = (AttImpl) obj;
 					String attName = edgeGraphics.getName();
 					String value = edgeGraphics.getValue();
 
 					if (attName.equals("sourceArrow")) {
 						edgeView.setSourceEdgeEnd(Integer.parseInt(value));
 					} else if (attName.equals("targetArrow")) {
 						edgeView.setTargetEdgeEnd(Integer.parseInt(value));
 					} else if (attName.equals("sourceArrowColor")) {
 						edgeView.setSourceEdgeEndPaint(getColor(value));
 					} else if (attName.equals("targetArrowColor")) {
 						edgeView.setTargetEdgeEndPaint(getColor(value));
 					} else if (attName.equals("edgeLineType")) {
 						edgeView.setStroke(LineType.parseLineTypeText(value)
 								.getStroke());
 					} else if(attName.equals("curved")) {
 						if(value.equals("STRAIGHT_LINES")) {
 							edgeView.setLineType(EdgeView.STRAIGHT_LINES);
 						} else if(value.equals("CURVED_LINES")) {
 							edgeView.setLineType(EdgeView.CURVED_LINES);
 						}
 					} else if(attName.equals("edgeBend")) {
 						// Restore bend info
 						
 						Iterator handleIt = edgeGraphics.getContent().iterator();
 						List handleList = new ArrayList();
 						while(handleIt.hasNext()) {
 							Object curObj = handleIt.next();
 							if(curObj.getClass() == AttImpl.class) {
 								
 								AttImpl handle = (AttImpl) curObj;
 								Iterator pointIt = handle.getContent().iterator();
 								double x = 0;
 								double y = 0;
 								boolean xFlag = false, yFlag = false;
 								while(pointIt.hasNext()) {
 									Object coordObj = pointIt.next();
 									if(coordObj.getClass() == AttImpl.class) {
 										AttImpl point = (AttImpl) coordObj;
 										if(point.getName().equals("x")) {
 											x = Double.parseDouble(point.getValue());
 											xFlag = true;
 										} else if(point.getName().equals("y")) {
 											y = Double.parseDouble(point.getValue());
 											yFlag = true;
 										}
 									}
 								}
 								if(xFlag == true && yFlag == true) {
 									Point2D handlePoint = new Point2D.Double(x, y);
 									handleList.add(handlePoint);
 								}
 								//System.out.println("Restore bend: " + x + ", " + y);
 							}
 						}
 						if(handleList.size() != 0) {
 							edgeView.getBend().setHandles(handleList);
 						}
 					}
 				}
 			}
 		}
 
 		// edgeView.setSourceEdgeEnd(((Number) graphics.get)
 		// .intValue());
 	}	/**
 		 * Part of interface contract
 		 */
 	public int[] getNodeIndicesArray() {
 		giny_nodes.trimToSize();
 		return giny_nodes.elements();
 	}
 
 	/**
 	 * Part of interace contract
 	 */
 	public int[] getEdgeIndicesArray() {
 		giny_edges.trimToSize();
 		return giny_edges.elements();
 	}
 
 	public String getNetworkID() {
 		return networkName;
 	}
 
 	public String getNetworkName() {
 		return networkName;
 	}
 
 	public RdfRDF getNetworkMetadata() {
 		return metadata;
 	}
 
 	public Color getColor(String colorString) {
 		return new Color(Integer.parseInt(colorString.substring(1), 16));
 		// return new Color(rdn.nextInt(255),rdn.nextInt(255),rdn.nextInt(255),
 		// rdn.nextInt(255));
 	}
 
 	public Color getBackgroundColor() {
 		return getColor(backgroundColor);
 	}
 
 	
 
 	public RootGraph getRootGraph() {
 		// TODO Auto-generated method stub
 		return Cytoscape.getRootGraph();
 	}
 
 	public CyAttributes getNodeAttributes() {
 		// TODO Auto-generated method stub
 		return Cytoscape.getNodeAttributes();
 	}
 
 	public CyAttributes getEdgeAttributes() {
 		// TODO Auto-generated method stub
 		return Cytoscape.getEdgeAttributes();
 	}
 
 	private void readAttributes(String targetName, List attrList, String type) {
 
 		CyAttributes attributes = null;
 
 		if (type.equals(EDGE)) {
 			attributes = Cytoscape.getEdgeAttributes();
 		} else {
 			attributes = Cytoscape.getNodeAttributes();
 		}
 		Iterator it = attrList.iterator();
 
 		while (it.hasNext()) {
 			Object curAtt = it.next();
 			readAttribute(attributes, targetName, (Att) curAtt);
 		}
 	}
 
 	/**
 	 * Reads an attribute from the xggml file and sets it within CyAttributes.
 	 * 
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param targetName -
 	 *            key into CyAttributes
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 */
 	private void readAttribute(CyAttributes attributes, String targetName,
 			Att curAtt) {
 
 		// check args
 		String dataType = curAtt.getType();
 		if (dataType == null)
 			return;
 		// null value only ok when type is list or map
 		if (!dataType.equals(LIST_TYPE) && !dataType.equals(MAP_TYPE)
 				&& curAtt.getValue() == null)
 			return;
 
 		// string
 		if (dataType.equals(STRING_TYPE)) {
 			attributes.setAttribute(targetName, curAtt.getName(), curAtt
 					.getValue());
 		}
 		// integer
 		else if (dataType.equals(INT_TYPE)) {
 			attributes.setAttribute(targetName, curAtt.getName(), new Integer(
 					curAtt.getValue()));
 		}
 		// float
 		else if (dataType.equals(FLOAT_TYPE)) {
 			attributes.setAttribute(targetName, curAtt.getName(), new Double(
 					curAtt.getValue()));
 		}
 		// boolean
 		else if (dataType.equals(BOOLEAN_TYPE)) {
 			attributes.setAttribute(targetName, curAtt.getName(), new Boolean(
 					curAtt.getValue()));
 		}
 		// list
 		else if (dataType.equals(LIST_TYPE)) {
 			ArrayList listAttr = new ArrayList();
 			Iterator listIt = curAtt.getContent().iterator();
 			while (listIt.hasNext()) {
 				Object listItem = listIt.next();
 				if (listItem != null && listItem.getClass() == AttImpl.class) {
 					Object itemClassObject = createObjectFromAttValue((AttImpl) listItem);
 					if (itemClassObject != null)
 						listAttr.add(itemClassObject);
 				}
 			}
 			attributes.setAttributeList(targetName, curAtt.getName(), listAttr);
 		}
 		// map
 		else if (dataType.equals(MAP_TYPE)) {
 			HashMap mapAttr = new HashMap();
 			Iterator mapIt = curAtt.getContent().iterator();
 			while (mapIt.hasNext()) {
 				Object mapItem = mapIt.next();
 				if (mapItem != null && mapItem.getClass() == AttImpl.class) {
 					Object mapClassObject = createObjectFromAttValue((AttImpl) mapItem);
 					if (mapClassObject != null) {
 						mapAttr.put(((AttImpl) mapItem).getName(),
 								mapClassObject);
 					}
 				}
 			}
 			attributes.setAttributeMap(targetName, curAtt.getName(), mapAttr);
 		}
 		// complex type
 		else if (dataType.equals(COMPLEX_TYPE)) {
 			String attributeName = curAtt.getName();
 			int numKeys = new Integer((String) curAtt.getValue()).intValue();
 			defineComplexAttribute(attributeName, attributes, curAtt, null, 0,
 					numKeys);
 			createComplexAttribute(attributeName, attributes, targetName,
 					curAtt, null, 0, numKeys);
 		}
 	}
 
 	/**
 	 * Determines the complex attribute keyspace and defines a cyattribute for
 	 * the complex attribute based on its keyspace.
 	 * 
 	 * @param attributeName -
 	 *            attribute name
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param attributeDefintion -
 	 *            byte[] which stores attribute key space definition
 	 * @param attributeDefinitionCount -
 	 *            the number of keys we've discovered so far
 	 * @param numKeys -
 	 *            the number of keys to discover
 	 */
 	private void defineComplexAttribute(String attributeName,
 			CyAttributes attributes, Att curAtt, byte[] attributeDefinition,
 			int attributeDefinitionCount, int numKeys) {
 
 		// if necessary, init attribute definition
 		if (attributeDefinition == null) {
 			attributeDefinition = new byte[numKeys];
 		}
 
 		// get current interaction interator and attribute
 		Iterator complexIt = curAtt.getContent().iterator();
 		Att complexItem = (Att) complexIt.next();
 		if (attributeDefinitionCount < numKeys) {
 			attributeDefinition[attributeDefinitionCount++] = getMultHashMapTypeFromAtt(complexItem);
 			if (attributeDefinitionCount < numKeys) {
 				defineComplexAttribute(attributeName, attributes, complexItem,
 						attributeDefinition, attributeDefinitionCount, numKeys);
 			}
 		}
 
 		// ok, if we are here, we've gotten all the keys
 		if (attributeDefinitionCount == numKeys) {
 			// go one more level deep to get value(s) type
 			Iterator nextComplexIt = complexItem.getContent().iterator();
 			Att nextComplexItem = (Att) nextComplexIt.next();
 			byte valueType = getMultHashMapTypeFromAtt(nextComplexItem);
 			MultiHashMapDefinition mhmd = attributes
 					.getMultiHashMapDefinition();
 			mhmd.defineAttribute(attributeName, valueType, attributeDefinition);
 		}
 	}
 
 	/**
 	 * Reads a complex attribute from the xggml file and sets it within
 	 * CyAttributes.
 	 * 
 	 * @param attributeName -
 	 *            attribute name
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param targetName -
 	 *            the key for the complex attribute we set (node id, edge id,
 	 *            network id, etc)
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param keySpace -
 	 *            byte[] which stores keyspace
 	 * @param keySpaceCount -
 	 *            the number of keys in the keyspace we've discovered so far
 	 * @param numKeySpaceKeys -
 	 *            the number of key space keys to discover
 	 */
 	private void createComplexAttribute(String attributeName,
 			CyAttributes attributes, String targetName, Att curAtt,
 			Object[] keySpace, int keySpaceCount, int numKeySpaceKeys) {
 
 		// if necessary, init keySpace array
 		if (keySpace == null) {
 			keySpace = new Object[numKeySpaceKeys];
 		}
 
 		// get this attributes iterator
 		Iterator complexIt = curAtt.getContent().iterator();
 		// interate over this attributes tree
 		while (complexIt.hasNext()) {
 			Object complexItemObj = complexIt.next();
 			if (complexItemObj != null
 					&& complexItemObj.getClass() == AttImpl.class) {
 				AttImpl complexItem = (AttImpl) complexItemObj;
 				// add this key to the keyspace
 				keySpace[keySpaceCount] = createObjectFromAttName(complexItem);
 				// recurse if we still have keys to go
 				if (keySpaceCount + 1 < numKeySpaceKeys - 1) {
 					createComplexAttribute(attributeName, attributes,
 							targetName, complexItem, keySpace,
 							keySpaceCount + 1, numKeySpaceKeys);
 				} else {
 					// ok, if we are here, we've gotten all the keys but the
 					// last level
 					MultiHashMap mhm = attributes.getMultiHashMap();
 					// interate through last level keys
 					Iterator nextComplexIt = complexItem.getContent()
 							.iterator();
 					while (nextComplexIt.hasNext()) {
 						Object nextComplexItemObj = nextComplexIt.next();
 						if (nextComplexItemObj != null
 								&& nextComplexItemObj.getClass() == AttImpl.class) {
 							// get last level key and set in keyspace
 							AttImpl nextComplexItem = (AttImpl) nextComplexItemObj;
 							keySpace[keySpaceCount + 1] = createObjectFromAttName(nextComplexItem);
 							// now grab the value - there can only be one
 							Iterator complexValueIterator = nextComplexItem
 									.getContent().iterator();
 							Object complexValue = complexValueIterator.next();
 							Object valueToStore = createObjectFromAttValue((AttImpl) complexValue);
 							mhm.setAttributeValue(targetName, attributeName,
 									valueToStore, keySpace);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates an object of an appropriate class, with value derived from
 	 * attribute name.
 	 * 
 	 * @param item -
 	 *            AttImpl
 	 * @return - Object
 	 */
 	private Object createObjectFromAttName(AttImpl item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return item.getName();
 		} else if (item.getType().equals(INT_TYPE)) {
 			return new Integer(item.getName());
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return new Double(item.getName());
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return new Boolean(item.getName());
 		}
 
 		// outta here
 		return null;
 	}
 
 	/**
 	 * Creates an object of an appropriate class, with value derived from
 	 * attribute value.
 	 * 
 	 * @param item -
 	 *            AttImpl
 	 * @return - Object
 	 */
 	private Object createObjectFromAttValue(AttImpl item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return item.getValue();
 		} else if (item.getType().equals(INT_TYPE)) {
 			return new Integer(item.getValue());
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return new Double(item.getValue());
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return new Boolean(item.getValue());
 		}
 
 		// outta here
 		return null;
 	}
 
 	/**
 	 * Given an attribute, method returns a MultiHashMapDefinition byte
 	 * corresponding to its type.
 	 * 
 	 * @param item -
 	 *            Att
 	 * @return - byte
 	 */
 	private byte getMultHashMapTypeFromAtt(Att item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return MultiHashMapDefinition.TYPE_STRING;
 		} else if (item.getType().equals(INT_TYPE)) {
 			return MultiHashMapDefinition.TYPE_INTEGER;
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return MultiHashMapDefinition.TYPE_FLOATING_POINT;
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return MultiHashMapDefinition.TYPE_BOOLEAN;
 		}
 
 		// outta here
 		return -1;
 	}
 
 	/**
 	 * Convert RDF object into Map
 	 */
 	private Map rdf2map(RdfRDF rdf) {
 
 		HashMap map = new HashMap();
 		RdfDescription dc = (RdfDescription) rdf.getDescription().get(0);
 
 		Iterator it = dc.getDcmes().iterator();
 		while (it.hasNext()) {
 			Object entry = it.next();
 			// This is a hack: extract label from class name
 			String className = entry.getClass().toString();
 			String[] parts = className.split("\\.");
 			parts = parts[parts.length - 1].split("Impl");
 
 			String label = parts[0];
 			if (label.startsWith("Date")) {
 				map.put(label, ((Date) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Title")) {
 				Title title = (Title) entry;
 				map.put(label, title.getContent().get(0).toString());
 			} else if (label.startsWith("Identifier")) {
 				map.put(label, ((Identifier) entry).getContent().get(0)
 						.toString());
 			} else if (label.startsWith("Description")) {
 				map.put(label, ((Description) entry).getContent().get(0)
 						.toString());
 			} else if (label.startsWith("Source")) {
 				map.put(label, ((Source) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Type")) {
 				map.put(label, ((Type) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Format")) {
 				map.put(label, ((Format) entry).getContent().get(0).toString());
 			} else {
 				//
 			}
 
 		}
 		return map;
 	}
 
 	public void setNetworkAttributes(CyNetwork cyNetwork) {
 		// Extract Network Attributes
 		// Currently, supported attribute data type is RDF metadata only.
 		List networkAttributes = network.getAtt();
 
 		for (int i = 0; i < networkAttributes.size(); i++) {
 			Att curAtt = (Att) networkAttributes.get(i);
 
 			if (curAtt.getName().equals("networkMetadata")) {
 				metadata = (RdfRDF) (curAtt.getContent().get(0));
 				networkCyAttributes.setAttributeMap(cyNetwork.getIdentifier(),
 						METADATA_ATTR_NAME, this.rdf2map(metadata));
 			} else if (curAtt.getName().equals(XGMMLWriter.BACKGROUND)) {
 				backgroundColor = curAtt.getValue();
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_ZOOM)) {
 				graphViewZoom = new Double(curAtt.getValue());
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_CENTER_X)) {
 				graphViewCenterX = new Double(curAtt.getValue());
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_CENTER_Y)) {
 				graphViewCenterY = new Double(curAtt.getValue());
 			} else {
 				readAttribute(networkCyAttributes, cyNetwork.getIdentifier(),
 						curAtt);
 			}
 		}
 	}
 
 }
