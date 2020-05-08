 /*
  File: XGMMLWriter.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
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
 package cytoscape.data.writers;
 
 import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 
 import cytoscape.groups.CyGroup;
 import cytoscape.groups.CyGroupManager;
 
 import cytoscape.data.CyAttributes;
 
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CyNodeView;
 import cytoscape.view.CyEdgeView;
 
 import cytoscape.visual.LineStyle;
 
 import ding.view.DGraphView;
 import ding.view.DingCanvas;
 
 import giny.model.RootGraph;
 
 import giny.view.Bend;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Paint;
 import java.awt.geom.Point2D;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.io.UnsupportedEncodingException;
 
 import java.math.BigInteger;
 
 import java.net.URISyntaxException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Iterator;
 
 enum GraphicsType {
 	ARC("arc"),
 	BITMAP("bitmap"),
 	IMAGE("image"),
 	LINE("line"),
 	OVAL("oval"), 
 	POLYGON("polygon"),
   RECTANGLE("rectangle"),
   TEXT("text"),
   BOX("box"),
   CIRCLE("circle"),
   VER_ELLIPSIS("ver_ellipsis"),
   HOR_ELLIPSIS("hor_ellipsis"),
   RHOMBUS("rhombus"),
   TRIANGLE("triangle"),
   PENTAGON("pentagon"),
   HEXAGON("hexagon"),
   OCTAGON("octagon"),
   ELLIPSE("ellipse"),
   DIAMOND("diamond"),
   PARALLELOGRAM("parallelogram"),
   ROUNDED_RECTANGLE("rounded_rectangle");
   private final String value;
 
   GraphicsType(String v) {
   	value = v;
   }
 
 	String value() {
 		return value;
 	}
 }
 
 enum ObjectType {
 	LIST("list"),
 	STRING("string"),
 	REAL("real"),
 	INTEGER("integer"),
 	BOOLEAN("boolean"),
 	MAP("map"),
 	COMPLEX("complex");
 
 	private final String value;
 
 	ObjectType(String v) {
 		value = v;
 	}
 
 	String value() {
 		return value;
 	}
 
 	static ObjectType fromValue(String v) {
 		for (ObjectType c: ObjectType.values()) {
 			if (c.value.equals(v)) {
 				return c;
 			}
 		}
 		throw new IllegalArgumentException(v.toString());
 	}
 
 	public String toString() {
 		return value;
 	}
 }
 
 
 /**
  *
  * Write network and attributes in XGMML format and <br>
  * marshall it in a streme.<br>
  *
  * @version 1.2
  * @since Cytoscape 2.6
  * @see cytoscape.data.readers.XGMMLReader
  * @author kono
  * @author scooter
  *
  */
 public class XGMMLWriter {
 	// XML preamble information
 	private static final String XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
 
 	private static final String[] NAMESPACES = {
 		"xmlns:dc=\"http://purl.org/dc/elements/1.1/\"",
 		"xmlns:xlink=\"http://www.w3.org/1999/xlink\"",
 		"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"",
 		"xmlns:cy=\"http://www.cytoscape.org\"",
 		"xmlns=\"http://www.cs.rpi.edu/XGMML\""
 	};
 
 	// File format version. For compatibility.
 	private static final String FORMAT_VERSION = "documentVersion";
 	private static final float VERSION = (float) 1.1;
 	private static final String METADATA_NAME = "networkMetadata";
 	private static final String METADATA_ATTR_NAME = "Network Metadata";
 
 	// Node types
 	protected static final String NORMAL = "normal";
 	protected static final String METANODE = "group";
 	protected static final String REFERENCE = "reference";
 
 	// Object types
 	protected static final int NODE = 1;
 	protected static final int EDGE = 2;
 	protected static final int NETWORK = 3;
 
 	/**
 	 *
 	 */
 	public static final String BACKGROUND = "backgroundColor";
 
 	/**
 	 *
 	 */
 	public static final String GRAPH_VIEW_ZOOM = "GRAPH_VIEW_ZOOM";
 
 	/**
 	 *
 	 */
 	public static final String GRAPH_VIEW_CENTER_X = "GRAPH_VIEW_CENTER_X";
 
 	/**
 	 *
 	 */
 	public static final String GRAPH_VIEW_CENTER_Y = "GRAPH_VIEW_CENTER_Y";
 
 	private CyAttributes nodeAttributes;
 	private CyAttributes edgeAttributes;
 	private CyAttributes networkAttributes;
 	private String[] nodeAttNames = null;
 	private String[] edgeAttNames = null;
 	private String[] networkAttNames = null;
 	private CyNetwork network;
 	private CyNetworkView networkView;
 	private ArrayList <CyGroup>groupList;
 	private	HashMap <CyNode, CyNode>nodeMap;
 	private	HashMap <CyEdge, CyEdge>edgeMap;
 	private boolean noCytoscapeGraphics = false;
 
 	private int depth = 0; // XML depth
 	private String indentString = "";
 	private Writer writer = null;
 
 	/**
 	 * Constructor.<br>
 	 * Initialize data objects to be saved in XGMML file.<br>
 	 *
 	 * @param network
 	 *            CyNetwork object to be saved.
 	 * @param view
 	 *            CyNetworkView for the network.
 	 * @throws URISyntaxException
 	 * @throws JAXBException
 	 */
 	public XGMMLWriter(final CyNetwork network, final CyNetworkView view)
 	    throws IOException, URISyntaxException {
 		this.network = network;
 		this.networkView = view;
 
 		nodeAttributes = Cytoscape.getNodeAttributes();
 		edgeAttributes = Cytoscape.getEdgeAttributes();
 		networkAttributes = Cytoscape.getNetworkAttributes();
 
 		groupList = new ArrayList<CyGroup>();
 		nodeMap = new HashMap<CyNode, CyNode>();
 		edgeMap = new HashMap<CyEdge, CyEdge>();
 
 		nodeAttNames = nodeAttributes.getAttributeNames();
 		edgeAttNames = edgeAttributes.getAttributeNames();
 		networkAttNames = networkAttributes.getAttributeNames();
 		// Create our indent string (480 blanks);
 		for (int i = 0; i < 20; i++) 
 			indentString += "                        ";
 	}
 
 	/**
 	 * Constructor.<br>
 	 * Initialize data objects to be saved in XGMML file.<br>
 	 *
 	 * @param network
 	 *            CyNetwork object to be saved.
 	 * @param view
 	 *            CyNetworkView for the network.
 	 * @param noCytoscapeGraphics
 	 *            boolean to indicate whether cytoscape graphics
 	 *            attributes should be written
 	 * @throws URISyntaxException
 	 * @throws JAXBException
 	 */
 	public XGMMLWriter(final CyNetwork network, final CyNetworkView view,
 	                   boolean noCytoscapeGraphics) throws IOException, URISyntaxException {
 		this(network, view);
 		this.noCytoscapeGraphics = noCytoscapeGraphics;
 	}
 
 	/**
 	 * Write the XGMML file.<br>
 	 * This method creates all JAXB objects from Cytoscape internal<br>
 	 * data structure, and them marshall it into an XML (XGMML) document.<br>
 	 *
 	 * @param writer
 	 *            Witer to create XGMML file
 	 * @throws IOException
 	 */
 	public void write(final Writer writer) throws  IOException {
 		this.writer = writer;
 
 		// write out the XGMML preamble
 		writePreamble();
 		depth++;
 
 		// write out our metadata
 		writeMetadata();
 
 		// write out network attributes
 		writeNetworkAttributes();
 
 		// Output our nodes
 		writeNodes();
 		writeGroups();
 
 		// Create edge objects
 		writeEdges();
 
 		depth--;
 		// Wwrite final tag
 		writeElement("</graph>\n");
 
 		writer.flush();
 	}
 
 	/**
 	 * Output the XML preamble.  This includes the XML line as well as the initial
 	 * &lt;graph&gt; element, along with all of our namespaces.
 	 *
 	 * @throws IOException
 	 */
 	private void writePreamble() throws IOException {
 		writeElement(XML_STRING+"\n");
 		writeElement("<graph label=\""+network.getTitle()+"\" "); 
 		for (int ns = 0; ns < NAMESPACES.length; ns++)
 			writer.write(NAMESPACES[ns]+" ");
 		writer.write(">\n");
 	}
 
 	/**
 	 * Output the network metadata.  This includes our format version and our RDF
 	 * data.
 	 * 
 	 * @throws IOException
 	 */
 	private void writeMetadata() throws IOException {
 		writeElement("<att name=\""+FORMAT_VERSION+"\" value=\""+VERSION+"\"/>\n");
 		writeElement("<att name=\"networkMetadata\">\n");
 		depth++;
 		writeRDF();
 		depth--;
 		writeElement("</att>\n");
 	}
 
 	/**
 	 * Output the RDF information for this network.
    *     <rdf:RDF>
    *         <rdf:Description rdf:about="http://www.cytoscape.org/">
    *             <dc:type>Protein-Protein Interaction</dc:type>
    *             <dc:description>N/A</dc:description>
    *             <dc:identifier>N/A</dc:identifier>
    *             <dc:date>2007-01-16 13:29:50</dc:date>
    *             <dc:title>Amidohydrolase Superfamily--child</dc:title>
    *             <dc:source>http://www.cytoscape.org/</dc:source>
    *             <dc:format>Cytoscape-XGMML</dc:format>
    *         </rdf:Description>
    *     </rdf:RDF>
 	 *
 	 * @throws IOException
 	 */
 	private void writeRDF() throws IOException {
 		writeElement("<rdf:RDF>\n");
 		depth++;
 		writeElement("<rdf:Description rdf:about=\"http://www.cytoscape.org/\">\n");
 		depth++;
 		writeElement("<dc:type>Protein-Protein Interaction</dc:type>\n");
 		writeElement("<dc:description>N/A</dc:description>\n");
 		writeElement("<dc:identifier>N/A</dc:identifier>\n");
 		java.util.Date now = new java.util.Date();
 		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		writeElement("<dc:date>"+df.format(now)+"</dc:date>\n");
 		writeElement("<dc:title>"+network.getTitle()+"</dc:title>\n");
 		writeElement("<dc:source>http://www.cytoscape.org/</dc:source>\n");
		writeElement("<dc:format>Xytoscape-XGMML</dc:format>\n");
 		depth--;
 		writeElement("</rdf:Description>\n");
 		depth--;
 		writeElement("</rdf:RDF>\n");
 	}
 
 	/**
 	 * Output any network attributes we have defined, including
 	 * the network graphics information we encode as attributes:
 	 * backgroundColor, zoom, and the graph center.
 	 *
 	 * @throws IOException
 	 */
 	private void writeNetworkAttributes() throws IOException {
 		if (networkView != null) {
 			// Get our background color
 			DingCanvas backgroundCanvas = 
 				((DGraphView)Cytoscape.getCurrentNetworkView()).
 				getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS);
 			writeAttributeXML(BACKGROUND, ObjectType.STRING, paint2string(backgroundCanvas.getBackground()), true);
 
 			// lets also write the zoom
 			final Double dAttr = new Double(networkView.getZoom());
 			writeAttributeXML(GRAPH_VIEW_ZOOM, ObjectType.REAL, dAttr ,true);
 
 			final Point2D center = ((DGraphView) networkView).getCenter();
 			writeAttributeXML(GRAPH_VIEW_CENTER_X, ObjectType.REAL, new Double(center.getX()) ,true);
 			writeAttributeXML(GRAPH_VIEW_CENTER_Y, ObjectType.REAL, new Double(center.getY()) ,true);
 		}
 
 		// Now handle all of the other network attributes
 		for (int att = 0; att < networkAttNames.length; att++) {
 			if (networkAttributes.hasAttribute(network.getIdentifier(), networkAttNames[att]))
 				writeAttribute(network.getIdentifier(), networkAttributes, networkAttNames[att]);
 		}
 	}
 
 	/**
 	 * Output Cytoscape nodes as XGMML
 	 *
 	 * @throws IOException
 	 */
 	private void writeNodes() throws IOException {
 		for (CyNode curNode: (List<CyNode>)network.nodesList()) {
 			if (!curNode.isaGroup())
 				writeNode(curNode, null);
 		}
 	}
 
 	/**
 	 * Output a single CyNode as XGMML
 	 *
 	 * @param node the node to output
 	 * @throws IOException
 	 */
 	private void writeNode(CyNode node, List<CyNode> groupList) throws IOException {
 		// Remember that we've seen this node
 		nodeMap.put(node, node);
 
 		// Output the node
 		writeElement("<node label="+quote(node.getIdentifier()));
 		writer.write(" id="+quote(Integer.toString(node.getRootGraphIndex()))+">\n");
 		depth++;
 
 		// Output the node attributes
 		for (int att = 0; att < nodeAttNames.length; att++) {
 			if (nodeAttributes.hasAttribute(node.getIdentifier(), nodeAttNames[att]))
 				writeAttribute(node.getIdentifier(), nodeAttributes, nodeAttNames[att]);
 		}
 
 		if (groupList != null && groupList.size() > 0) {
 			// If we're a group, output the graph attribute now
 			writeElement("<att>\n");
 			depth++;
 			writeElement("<graph>\n");
 			depth++;
 			for (CyNode childNode: groupList) {
 				if (childNode.isaGroup()) {
 					// We have an embedded group -- recurse
 					CyGroup childGroup = CyGroupManager.getCyGroup(childNode);
 					writeNode(childGroup.getGroupNode(), childGroup.getNodes());
 				} else {
 					if (nodeMap.containsKey(childNode))
 						writeElement("<node xlink:href=\"#"+childNode.getRootGraphIndex()+"\"/>\n");
 					else
 						writeNode(childNode, null);
 				}
 			}
 			depth--; writeElement("</graph>\n");
 			depth--; writeElement("</att>\n");
 		}
 
 		// Output the node graphics if we have a view
 		writeNodeGraphics(node, (NodeView)networkView.getNodeView(node));
 
 		depth--;
 		writeElement("</node>\n");
 	}
 
 	/**
 	 * Output the node graphics
    *   <graphics y="17276.9" x="15552.7" width="5" outline="#000000" fill="#44f687" type="diamond" w="35.0" h="35.0">
    *      <att name="cytoscapeNodeGraphicsAttributes">
    *         <att value="1.0" name="nodeTransparency"/>
    *         <att value="Arial Bold-0-6" name="nodeLabelFont"/>
    *         <att value="solid" name="borderLineType"/>
    *      </att>
    *   </graphics>
 	 *
 	 * @param node the node whose graphics we're outputting
 	 * @param nodeView the view for this node
 	 *
 	 * @throws IOException
 	 */
 	private void writeNodeGraphics(CyNode node, NodeView nodeView) throws IOException {
 
 		/*
 		 * In case node is hidden, we cannot get the show and extract node
 		 * view.
 		 */
 		boolean hiddenNodeFlag = false;
 		if (nodeView == null) return;
 
 		if (nodeView.getWidth() == -1) {
 			networkView.showGraphObject(nodeView);
 			hiddenNodeFlag = true;
 		}
 
 		writeElement("<graphics");
 		// Node shape
 		GraphicsType shape = number2shape(nodeView.getShape());
 		if (shape == GraphicsType.PARALLELOGRAM)
 			writeAttributePair("type",GraphicsType.RHOMBUS);
 		else
 			writeAttributePair("type",shape);
 
 		// Node size and position
 		writeAttributePair("h",Double.toString(nodeView.getHeight()));
 		writeAttributePair("w",Double.toString(nodeView.getWidth()));
 		writeAttributePair("x",Double.toString(nodeView.getXPosition()));
 		writeAttributePair("y",Double.toString(nodeView.getYPosition()));
 
 		// Node color
 		writeAttributePair("fill", paint2string(nodeView.getUnselectedPaint()));
 
 		// Node border basic info.
 		final BasicStroke borderType = (BasicStroke) nodeView.getBorder();
 		writeAttributePair("width", Integer.toString(((int)borderType.getLineWidth())));
 		writeAttributePair("outline", paint2string(nodeView.getBorderPaint()));
 
 		// Write out the Cytoscape-specific attributes
 		if (!noCytoscapeGraphics) {
 			writeAttributePair("cy:nodeTransparency", Double.toString(nodeView.getTransparency()));
 			writeAttributePair("cy:nodeLabelFont", encodeFont(nodeView.getLabel().getFont()));
 
 			// Where should we store line-type info???
 			final float[] dash = borderType.getDashArray();
 
 			if (dash == null) {
 				// System.out.println("##Border is NORMAL LINE");
 				writeAttributePair("cy:borderLineType", "solid");
 			} else {
 				// System.out.println("##Border is DASHED LINE");
 				String dashArray = null;
 				final StringBuilder dashBuf = new StringBuilder();
 
 				for (int i = 0; i < dash.length; i++) {
 					dashBuf.append(Double.toString(dash[i]));
 
 					if (i < (dash.length - 1)) {
 						dashBuf.append(",");
 					}
 				}
 
 				dashArray = dashBuf.toString();
 				writeAttributePair("cy:borderLineType", dashArray);
 			}
 		}
 
 		writer.write("/>\n");
 	}
 
 	private void writeGroups() throws IOException {
 		RootGraph rootGraph = Cytoscape.getRootGraph();
 
 		// Two pass approach. First, walk through the list
 		// and see if any of the children of a group are
 		// themselves a group. If so, remove them from
 		// the list & will pick them up on recursion
 		groupList = (ArrayList) CyGroupManager.getGroupList();
 
 		if ((groupList == null) || groupList.isEmpty())
 			return;
 
 		HashMap embeddedGroupList = new HashMap();
 
 		for (CyGroup group: groupList) {
 			List<CyNode> childList = group.getNodes();
 
 			if ((childList == null) || (childList.size() == 0))
 				continue;
 
 			for (CyNode childNode: childList) {
 				if (CyGroupManager.isaGroup(childNode)) {
 					// Get the actual group
 					CyGroup embGroup = CyGroupManager.getCyGroup(childNode);
 					embeddedGroupList.put(embGroup, embGroup);
 				}
 			}
 		}
 
 		for (CyGroup group: groupList) {
 			// Is this an embedded group?
 			if (embeddedGroupList.containsKey(group))
 				continue; // Yes, skip it
 
 			writeGroup(group);
 		}
 	}
 
 	private void writeGroup(CyGroup group) throws IOException {
 		CyNode groupNode = group.getGroupNode();
 		writeNode(groupNode, group.getNodes());
 	}
 
 	/**
 	 * Output Cytoscape edges as XGMML
 	 *
 	 * @throws IOException
 	 */
 	private void writeEdges() throws IOException {
 		for (CyEdge curEdge: (List<CyEdge>)network.edgesList()) {
 			edgeMap.put(curEdge,curEdge);
 			writeEdge(curEdge);
 		}
 
 		// Now add all of the group edges in case they're hidden
 		for (CyGroup group: groupList) {
 			for (CyEdge edge: group.getInnerEdges()) {
 				if (!edgeMap.containsKey(edge)) {
 					edgeMap.put(edge,edge);
 					writeEdge(edge);
 				}
 			}
 			for (CyEdge edge: group.getOuterEdges()) {
 				if (!edgeMap.containsKey(edge)) {
 					edgeMap.put(edge,edge);
 					writeEdge(edge);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Output a Cytoscape edge as XGMML
 	 *
 	 * @param curEdge the edge to output
 	 *
 	 * @throws IOException
 	 */
 	private void writeEdge(CyEdge curEdge) throws IOException {
 		// Write the edge 
 		String target = quote(Integer.toString(curEdge.getTarget().getRootGraphIndex()));
 		String source = quote(Integer.toString(curEdge.getSource().getRootGraphIndex()));
 
 		// Make sure these nodes exist
 		if (!nodeMap.containsKey(curEdge.getTarget()) || !nodeMap.containsKey(curEdge.getSource()))
 			return;
 
 		writeElement("<edge label="+quote(curEdge.getIdentifier())+" source="+source+" target="+target+">\n");
 		depth++;
 
 		// Write the edge attributes
 		for (int att = 0; att < edgeAttNames.length; att++) {
 			if (edgeAttributes.hasAttribute(curEdge.getIdentifier(), edgeAttNames[att]))
 				writeAttribute(curEdge.getIdentifier(), edgeAttributes, edgeAttNames[att]);
 		}
 
 		// Write the edge graphics
 		writeEdgeGraphics(curEdge, (EdgeView)networkView.getEdgeView(curEdge));
 
 		depth--;
 		writeElement("</edge>\n");
 	}
 
 	/**
 	 * Output the edge graphics
 	 *
 	 * @param edge the edge whose graphics we're outputting
 	 * @param edgeView the view for this edge
 	 *
 	 * @throws IOException
 	 */
 	private void writeEdgeGraphics(CyEdge edge, EdgeView edgeView) throws IOException {
 		if (edgeView == null) 
 			return;
 
 		writeElement("<graphics");
 		// Width
 		writeAttributePair("width", Integer.toString((int) edgeView.getStrokeWidth()));
 		// Color
 		writeAttributePair("fill", paint2string(edgeView.getUnselectedPaint()));
 
 		// Store Cytoscape-local graphical attributes
 		if (!noCytoscapeGraphics) {
 			writeAttributePair("cy:sourceArrow", Integer.toString(edgeView.getSourceEdgeEnd()));
 			writeAttributePair("cy:targetArrow", Integer.toString(edgeView.getTargetEdgeEnd()));
 			writeAttributePair("cy:sourceArrowColor", paint2string(edgeView.getSourceEdgeEndPaint()));
 			writeAttributePair("cy:targetArrowColor", paint2string(edgeView.getTargetEdgeEndPaint()));
 
 			writeAttributePair("cy:edgeLabelFont", encodeFont(edgeView.getLabel().getFont()));
 			writeAttributePair("cy:edgeLineType", LineStyle.extractLineStyle(edgeView.getStroke()).toString());
 			// Set curved or not
 			if (edgeView.getLineType() == EdgeView.CURVED_LINES) {
 				writeAttributePair("cy:curved", "CURVED_LINES");
 			} else if (edgeView.getLineType() == EdgeView.STRAIGHT_LINES) {
 				writeAttributePair("cy:curved", "STRAIGHT_LINES");
 			}
 		}
 
 
 		// Handle bends
 		final Bend bendData = edgeView.getBend();
 		final List<Point2D> handles = bendData.getHandles();
 
 		if (handles.size() == 0) {
 			writer.write("/>\n");
 			return;
 		} else {
 			writer.write(">\n");
 		}
 
 		depth++;
 		writeElement("<att name=\"edgeBend\">\n");
 		depth++;
 		for (Point2D handle: handles) {
 			String x = Double.toString(handle.getX());
 			String y = Double.toString(handle.getY());
 			writeElement("<att name=\"handle\" x=\""+x+"\" y=\""+y+"\" />\n");
 		}
 		depth--;
 		writeElement("</att>\n");
 		depth--;
 		writeElement("</graphics>\n");
 	}
 
 	/**
 	 * Creates an attribute to write into XGMML file.
 	 *
 	 * @param id -
 	 *            id of node, edge or network
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param attributeName -
 	 *            attribute name
 	 * @return att - Att to return (gets written into xgmml file - CAN BE NULL)
 	 *
 	 * @throws IOException
 	 */
 	private void writeAttribute(final String id, final CyAttributes attributes,
 	                            final String attributeName) throws IOException {
 		// create an attribute and its type
 		final byte attType = attributes.getType(attributeName);
 		String value = null;
 		String type = null;
 
 		// process float
 		if (attType == CyAttributes.TYPE_FLOATING) {
 			Double dAttr = attributes.getDoubleAttribute(id, attributeName);
 			writeAttributeXML(attributeName, ObjectType.REAL, dAttr, true);
 		}
 		// process integer
 		else if (attType == CyAttributes.TYPE_INTEGER) {
 			Integer iAttr = attributes.getIntegerAttribute(id, attributeName);
 			writeAttributeXML(attributeName, ObjectType.INTEGER, iAttr, true);
 		}
 		// process string
 		else if (attType == CyAttributes.TYPE_STRING) {
 			String sAttr = attributes.getStringAttribute(id, attributeName);
 			if (sAttr != null) {
 				sAttr = sAttr.replace("\n", "\\n");
 			} 
 			writeAttributeXML(attributeName, ObjectType.STRING, sAttr, true);
 		}
 		// process boolean
 		else if (attType == CyAttributes.TYPE_BOOLEAN) {
 			Boolean bAttr = attributes.getBooleanAttribute(id, attributeName);
 			writeAttributeXML(attributeName, ObjectType.BOOLEAN, bAttr, true);
 		}
 		// process simple list
 		else if (attType == CyAttributes.TYPE_SIMPLE_LIST) {
 			// get the attribute list
 			final List listAttr = attributes.getListAttribute(id, attributeName);
 			writeAttributeXML(attributeName, ObjectType.LIST, null, false);
 
 			depth++;
 			// interate through the list
 			for (Object obj: listAttr) {
 				// set child attribute value & label
 				writeAttributeXML(attributeName, checkType(obj), obj.toString(), true);
 			}
 			depth--;
 			writeAttributeXML(null, null, null, true);
 		}
 		// process simple map
 		else if (attType == CyAttributes.TYPE_SIMPLE_MAP) {
 			// get the attribute map
 			final Map mapAttr = attributes.getMapAttribute(id, attributeName);
 			writeAttributeXML(attributeName, ObjectType.MAP, null, false);
 
 			depth++;
 			// interate through the map
 			for (Object obj: mapAttr.keySet()) {
 				// get the attribute from the map
 				String key = (String) obj;
 				Object val = mapAttr.get(key);
 
 				writeAttributeXML(key, checkType(val), val.toString(), true);
 			}
 			depth--;
 			writeAttributeXML(null, null, null, true);
 		}
 		// process complex map
 		else if (attType == CyAttributes.TYPE_COMPLEX) {
 			MultiHashMap mmap = attributes.getMultiHashMap();
 			MultiHashMapDefinition mmapDef = attributes.getMultiHashMapDefinition();
 
 			// get the number & types of dimensions
 			byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 			// Check to see if id has value assigned to attribute
 			if (!objectHasKey(id, attributes, attributeName)) {
 				return;
 			}
 			// Output the first <att>
 			writeAttributeXML(attributeName, ObjectType.COMPLEX, String.valueOf(dimTypes.length), false);
 
 			// grab the complex attribute structure
 			Map complexAttributeStructure = getComplexAttributeStructure(mmap, id, attributeName, null,
 			                                                             0, dimTypes.length);
 
 			// determine val type, get its string equivalent to store in XGMML
 			ObjectType valType = getType(mmapDef.getAttributeValueType(attributeName));
 
 			depth++;
 			// walk the structure
 			writeComplexAttribute(complexAttributeStructure, valType, dimTypes, 0);
 			depth--;
 			// Close
 			writeAttributeXML(null, null, null, true);
 		}
 	}
 
   /**
    * Returns a map where the key(s) are each key in the attribute key space,
    * and the value is another map or the attribute value.
 	 *
 	 * For example, if the following key:
 	 *
 	 * {externalref1}{authors}{1} pointed to the following value:
 	 *
 	 * "author 1 name",
 	 *
 	 * Then we would have a Map where the key is externalref1, the value is a
 	 * Map where the key is {authors}, the value is a Map where the key is {1},
 	 * the value is "author 1 name".
 	 *
 	 * @param mmap -
 	 *            reference to MultiHashMap used by CyAttributes
 	 * @param id -
 	 *            id of node, edge or network
 	 * @param attributeName -
 	 *            name of attribute
 	 * @param keys -
 	 *            array of objects which store attribute keys
 	 * @param keysIndex -
 	 *            index into keys array we should add the next key
 	 * @param numKeyDimensions -
 	 *            the number of keys used for given attribute name
 	 * @return Map - ref to Map interface
 	 */
   private Map getComplexAttributeStructure(MultiHashMap mmap, String id, String attributeName,
                                            Object[] keys, int keysIndex, int numKeyDimensions) {
 		// are we done?
 		if (keysIndex == numKeyDimensions)
 			return null;
 
 		// the hashmap to return
 		Map keyHashMap = new HashMap();
 
 		// create a new object array to store keys for this interation
 		// copy all existing keys into it
 		Object[] newKeys = new Object[keysIndex + 1];
 
 		for (int lc = 0; lc < keysIndex; lc++) {
 			newKeys[lc] = keys[lc];
 		}
 
 		// get the key span
 		Iterator keyspan = mmap.getAttributeKeyspan(id, attributeName, keys);
 
 		while (keyspan.hasNext()) {
 			Object newKey = keyspan.next();
 			newKeys[keysIndex] = newKey;
 
 			Map nextLevelMap = getComplexAttributeStructure(mmap, id, attributeName, newKeys,
 			                                                keysIndex + 1, numKeyDimensions);
 			Object objectToStore = (nextLevelMap == null)
 			                       ? mmap.getAttributeValue(id, attributeName, newKeys) : nextLevelMap;
 			keyHashMap.put(newKey, objectToStore);
 		}
 		return keyHashMap;
 	}
 
 
 	/**
 	 * This method is a recursive routine to output a complex attribute.
 	 *
 	 * @param complexAttributeStructure the structure of the attribute
 	 * @param type the type of the attribute
 	 * @param dimTypes the array of dimension types
 	 * @param dimTypesIndex which dimType we're working on
 	 */
 	private void writeComplexAttribute(Map complexAttributeStructure, ObjectType type,
 	                                   byte[] dimTypes, int dimTypesIndex) throws IOException {
 		for (Object key: complexAttributeStructure.keySet()) {
 			Object possibleAttributeValue = complexAttributeStructure.get(key);
 
 			// Is this a leaf or are we still dealing with maps?
 			if (possibleAttributeValue instanceof Map) {
 				// Another map
 				writeAttributeXML(key.toString(), getType(dimTypes[dimTypesIndex]),
 				                  String.valueOf(((Map) possibleAttributeValue).size()), false);
 				// Recurse
 				depth++;
 				writeComplexAttribute((Map)possibleAttributeValue, type, dimTypes, dimTypesIndex+1);
 				depth--;
 				// Close
 				writeAttributeXML(null, null, null, true);
 			} else {
 				// Final key
 				writeAttributeXML(key.toString(), getType(dimTypes[dimTypesIndex]),
 				                  String.valueOf(1), false);
 				depth++;
 				writeAttributeXML(null, type, possibleAttributeValue.toString(), true);
 				depth--;
 				writeAttributeXML(null, null, null, true);
 			}
 		}
 	}
 
 	/**
 	 * writeAttributeXML outputs an XGMML attribute
 	 *
 	 * @param name is the name of the attribute we are outputting
 	 * @param type is the XGMML type of the attribute
 	 * @param value is the value of the attribute we're outputting
 	 * @param end is a flag to tell us if the attribute should include a tag end
 	 *
 	 * @throws IOException
 	 */
 	private void writeAttributeXML(String name, ObjectType type, Object value, boolean end) throws IOException {
 		if (name == null && type == null)
 			writeElement("</att>\n");
 		else {
 			writeElement("<att type="+quote(type.toString()));
 			if (name != null)
 				writer.write(" name="+quote(name));
 			if (value != null)
 				writer.write(" value="+quote(value.toString()));
 			if (end)
 				writer.write("/>\n");
 			else
 				writer.write(">\n");
 		}
 	}
 
 	/**
 	 * writeAttributePair outputs the name,value pairs for an attribute
 	 *
 	 * @param name is the name of the attribute we are outputting
 	 * @param value is the value of the attribute we're outputting
 	 *
 	 * @throws IOException
 	 */
 	private void writeAttributePair(String name, Object value) throws IOException {
 		writer.write(" "+name+"=\""+value.toString()+"\"");
 	}
 
 	/**
 	 * writeElement outputs the name,value pairs for an attribute
 	 *
 	 * @param line is the element string to output
 	 *
 	 * @throws IOException
 	 */
 	private void writeElement(String line) throws IOException {
 		while ( depth*2 > indentString.length()-1 ) 
 			indentString = indentString + "                        ";
 		writer.write(indentString,0,depth*2);
 		writer.write(line);
 	}
 
 	/**
 	 * Convert enumerated shapes into human-readable string.<br>
 	 *
 	 * @param type
 	 *            Enumerated node shape.
 	 * @return Shape in string.
 	 */
 	private GraphicsType number2shape(final int type) {
 		switch (type) {
 			case NodeView.ELLIPSE:
 				return GraphicsType.ELLIPSE;
 
 			case NodeView.RECTANGLE:
 				return GraphicsType.RECTANGLE;
 
 			case NodeView.ROUNDED_RECTANGLE:
 				return GraphicsType.ROUNDED_RECTANGLE;
 
 			case NodeView.DIAMOND:
 				return GraphicsType.DIAMOND;
 
 			case NodeView.HEXAGON:
 				return GraphicsType.HEXAGON;
 
 			case NodeView.OCTAGON:
 				return GraphicsType.OCTAGON;
 
 			case NodeView.PARALELLOGRAM:
 				return GraphicsType.PARALLELOGRAM;
 
 			case NodeView.TRIANGLE:
 				return GraphicsType.TRIANGLE;
 
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * Convert color (paint) to RGB string.<br>
 	 *
 	 * @param p
 	 *            Paint object to be converted.
 	 * @return Color in RGB string.
 	 */
 	private String paint2string(final Paint p) {
 		final Color c = (Color) p;
 
 		return ("#" // +Integer.toHexString(c.getRGB());
 		       + Integer.toHexString(256 + c.getRed()).substring(1)
 		       + Integer.toHexString(256 + c.getGreen()).substring(1)
 		       + Integer.toHexString(256 + c.getBlue()).substring(1));
 	}
 
 	/**
 	 * Encode font into a human-readable string.<br>
 	 *
 	 * @param font
 	 *            Font object.
 	 * @return String extracted from the given Font object.
 	 */
 	private String encodeFont(final Font font) {
 		// Encode font into "fontname-style-pointsize" string
 		return font.getName() + "-" + font.getStyle() + "-" + font.getSize();
 	}
 
 	/**
 	 * Check the type of Attributes.
 	 *
 	 * @param obj
 	 * @return Attribute type in string.
 	 *
 	 */
 	private ObjectType checkType(final Object obj) {
 		if (obj.getClass() == String.class) {
 			return ObjectType.STRING;
 		} else if (obj.getClass() == Integer.class) {
 			return ObjectType.INTEGER;
 		} else if ((obj.getClass() == Double.class) || (obj.getClass() == Float.class)) {
 			return ObjectType.REAL;
 		} else if (obj.getClass() == Boolean.class) {
 			return ObjectType.BOOLEAN;
 		} else
 			return null;
 	}
 
 	/**
 	 * Given a byte describing a MultiHashMapDefinition TYPE_*, return the
 	 * proper XGMMLWriter type.
 	 *
 	 * @param dimType -
 	 *            byte as described in MultiHashMapDefinition
 	 * @return the type pointed to by this dim
 	 */
 	private ObjectType getType(final byte dimType) {
 		if (dimType == MultiHashMapDefinition.TYPE_BOOLEAN)
 			return ObjectType.BOOLEAN;
 
 		if (dimType == MultiHashMapDefinition.TYPE_FLOATING_POINT)
 			return ObjectType.REAL;
 
 		if (dimType == MultiHashMapDefinition.TYPE_INTEGER)
 			return ObjectType.INTEGER;
 
 		if (dimType == MultiHashMapDefinition.TYPE_STRING)
 			return ObjectType.STRING;
 
 		// houston we have a problem
 		return null;
 	}
 
 	/**
 	 * quote returns a quoted string appropriate for use as an XML attribute
 	 *
 	 * @param str the string to quote
 	 * @return the quoted string
 	 */
 	private String quote(String str) {
 		// Find and replace any "magic" characters
 		if (str.contains("&")) {
 			str = str.replaceAll("&", "&amp;");
 		}
 		if (str.contains("\"")) {
 			str = str.replaceAll("\"", "&quot;");
 		}
 		if (str.contains("'")) {
 			str = str.replaceAll("\'", "&apos;");
 		}
 		if (str.contains("<")) {
 			str = str.replaceAll("<", "&lt;");
 		}
 		if (str.contains(">")) {
 			str = str.replaceAll(">", "&gt;");
 		}
 		return "\""+str+"\"";
 	}
 
 	/**
 	 * Determines if object has key in multihashmap
 	 *
 	 * @param id -
 	 *            node, edge, network id
 	 * @param attributes -
 	 *            CyAttributes ref
 	 * @param attributeName -
 	 *            attribute name
 	 *
 	 * @return boolean
 	 */
 	private boolean objectHasKey(String id, CyAttributes attributes, String attributeName) {
 		MultiHashMap mmap = attributes.getMultiHashMap();
 
 		for (Iterator keysIt = mmap.getObjectKeys(attributeName); keysIt.hasNext();) {
 			String thisKey = (String) keysIt.next();
 
 			if ((thisKey != null) && thisKey.equals(id)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
