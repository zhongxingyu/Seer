 /**
  * A SAX Parser for GraphML data file.
  * @author Kozo.Nishida
  *
  */
 
 package org.cytoscape.data.reader.graphml;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNode;
 import cytoscape.CyEdge;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.logger.CyLogger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class GraphMLParser extends DefaultHandler {
 
 	// private static CyLogger logger = CyLogger.getLogger(GraphMLParser.class);
 
 	private String networkName = null;
 
 	/* Internal lists of the created nodes and edges */
 	private List<CyNode> nodeList = null;
 	private List<CyEdge> edgeList = null;
 
 	/* Map of XML ID's to nodes */
 	private Map<String, CyNode> nodeidMap = null;
 
 	/* Map of data type to nodes or edges */
 	private Map<String, String> datatypeMap = null;
 
 	private CyNode currentNode = null;
 	private CyEdge currentEdge = null;
 
 	/* Attribute values */
 	private String currentAttributeID = null;
 	private String currentAttributeKey = null;
 	private String currentAttributeData = null;
 	private String currentAttributeType = null;
 	private String currentEdgeSource = null;
 	private String currentEdgeTarget = null;
 	private String currentObjectTarget = null;
 	private String currentQname = null;
 
 	private CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 	private CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 
 	/* node, edge, data parsing */
 	private boolean directed = false;
 	
 	private static final String NODE = "node";
 	private static final String EDGE = "edge";
 
 	/********************************************************************
 	 * Routines to handle keys
 	 *******************************************************************/
 
 	/**
 	 * Main constructor for our parser. Initialize any local arrays. Note that
 	 * this parser is designed to be as memory efficient as possible. As a
 	 * result, a minimum number of local data structures
 	 */
 	GraphMLParser() {
 		nodeList = new ArrayList<CyNode>();
 		edgeList = new ArrayList<CyEdge>();
 		nodeidMap = new HashMap<String, CyNode>();
 		datatypeMap = new HashMap<String, String>();
 	}
 
 	/********************************************************************
 	 * Interface routines. These routines are called by the GraphMLReader to get
 	 * the resulting data.
 	 *******************************************************************/
 
 	int[] getNodeIndicesArray() {
 		
 		System.out.println("Got nodes: " + nodeList.size());
 		
 		int[] array = new int[nodeList.size()];
 		
 		for (int i = 0; i < nodeList.size(); i++) {
 			array[i] = nodeList.get(i).getRootGraphIndex();
 		}
 		return array;
 	}
 
 	int[] getEdgeIndicesArray() {
 		
 		System.out.println("Got edges: " + edgeList.size());
 		
 		int[] array = new int[edgeList.size()];
 		for (int i = 0; i < edgeList.size(); i++) {
 			array[i] = edgeList.get(i).getRootGraphIndex();
 		}
 		return array;
 	}
 
 	String getNetworkName() {
 		return networkName;
 	}
 
 	/********************************************************************
 	 * Handler routines. The following routines are called directly from the SAX
 	 * parser.
 	 *******************************************************************/
 
 	public void startDocument() {
 
 	}
 
 	public void endDocument() throws SAXException {
 
 	}
 
 	public void startElement(String namespace, String localName, String qName,
 			Attributes atts) throws SAXException {
 		if (qName.equals("graph")) {
 			currentQname = "graph";
 			// parse directed or undirected
 			String edef = atts.getValue("edgedefault");
 			directed = "directed".equalsIgnoreCase(edef);
 			
 			this.networkName = atts.getValue("id");
 			
 		} else if (qName.equals("key")) {
 			currentQname = "key";
 			if (atts.getValue("for").equals(NODE)) {
 				datatypeMap
 						.put(atts.getValue("id"), atts.getValue("attr.type"));
 			} else if (atts.getValue("for").equals(EDGE)) {
 				datatypeMap
 						.put(atts.getValue("id"), atts.getValue("attr.type"));
 			} else if (atts.getValue("for").equals("all")) {
 				datatypeMap
 						.put(atts.getValue("id"), atts.getValue("attr.type"));
 			}
 		} else if (qName.equals(NODE)) {
 			currentQname = NODE;
 			// Parse node entry.
 			currentObjectTarget = NODE;
 			currentAttributeID = atts.getValue("id");
 			currentNode = Cytoscape.getCyNode(currentAttributeID, true);
 			nodeList.add(currentNode);
 			nodeidMap.put(currentAttributeID, currentNode);
 		} else if (qName.equals(EDGE)) {
 			currentQname = EDGE;
 			// Parse edge entry
 			currentObjectTarget = EDGE;
 			currentEdgeSource = atts.getValue("source");
 			currentEdgeTarget = atts.getValue("target");
 			CyNode sourceNode = nodeidMap.get(currentEdgeSource);
 			CyNode targetNode = nodeidMap.get(currentEdgeTarget);
 			currentEdge = Cytoscape.getCyEdge(sourceNode, targetNode,
 					Semantics.INTERACTION, "pp", true);
 			edgeList.add(currentEdge);
 		} else if (qName.equals("data")) {
 			currentQname = "data";
 			currentAttributeKey = atts.getValue("key");
 			currentAttributeType = datatypeMap.get(currentAttributeKey);
 		}
 	}
 
 	public void characters(char[] ch, int start, int length) {
 		currentAttributeData = new String(ch, start, length);
 		
 		if (currentObjectTarget != null) {
 			if (currentObjectTarget.equals(NODE)) {
 				if (currentAttributeType != null) {
 					if (currentAttributeType.equals("string")) {
 						// debug
						System.out.println(currentAttributeData);
 						nodeAttributes.setAttribute(currentAttributeID,
 								currentAttributeKey, currentAttributeData);
 					} else if (currentAttributeType.equals("double")) {
 						// debug
						System.out.println(currentAttributeData);
 						nodeAttributes.setAttribute(currentAttributeID,
 								currentAttributeKey,
 								Double.parseDouble(currentAttributeData));
 					}
 				}
 			}
 			else if (currentObjectTarget.equals(EDGE)) {
 				if (currentAttributeType != null) {
 					if (currentAttributeType.equals("string")) {
 						// debug
						System.out.println(currentAttributeData);
 						edgeAttributes.setAttribute(currentEdge.getIdentifier(),
 								currentAttributeKey, currentAttributeData);
 					}
 					if (currentAttributeType.equals("double")) {
 						// debug
						System.out.println(currentAttributeData);
 						edgeAttributes.setAttribute(currentEdge.getIdentifier(),
								currentAttributeKey, currentAttributeData);
 					}					
 				}
 			}
 		}
 
 	}
 
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		if (currentQname != "data") {
 			currentObjectTarget = null;			
 		}
 		currentAttributeType = null;
 	}
 
 }
