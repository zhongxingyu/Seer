 /*
  File: XGMMLWriter.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
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
 
 package cytoscape.data.writers;
 
 import giny.view.EdgeView;
 import giny.view.NodeView;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Paint;
 import java.io.IOException;
 import java.io.Writer;
 import java.math.BigInteger;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.PropertyException;
 
 import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.ExpressionData;
 import cytoscape.data.Semantics;
 import cytoscape.data.readers.MetadataParser;
 import cytoscape.generated2.Att;
 import cytoscape.generated2.Edge;
 import cytoscape.generated2.Graph;
 import cytoscape.generated2.Graphics;
 import cytoscape.generated2.Node;
 import cytoscape.generated2.ObjectFactory;
 import cytoscape.generated2.RdfRDF;
 import cytoscape.view.CyNetworkView;
 import cytoscape.visual.LineType;
 
 /**
  * 
  * Write network and attributes in a streme.
  * 
  * @author kono
  * 
  */
 public class XGMMLWriter {
 
 	// Package to be used for data binding.
 	static final String PACKAGE_NAME = "cytoscape.generated2";
 	static final String METADATA_NAME = "networkMetadata";
 
 	// GML-Compatible Pre-defined Shapes
 	protected static String RECTANGLE = "rectangle";
 	protected static String ELLIPSE = "ellipse";
 	protected static String LINE = "Line"; // This is the Polyline object.
 	protected static String POINT = "point";
 	protected static String DIAMOND = "diamond";
 	protected static String HEXAGON = "hexagon";
 	protected static String OCTAGON = "octagon";
 	protected static String PARALELLOGRAM = "parallelogram";
 	protected static String TRIANGLE = "triangle";
 
 	// Node types
 	protected static String NORMAL = "normal";
 	protected static String METANODE = "metanode";
 	protected static String REFERENCE = "reference";
 
 	// Object types
 	protected static int NODE = 1;
 	protected static int EDGE = 2;
 	protected static int NETWORK = 3;
 
 	protected static final String BACKGROUND = "backgroundColor";
 
 	private CyAttributes nodeAttributes;
 	private CyAttributes edgeAttributes;
 	private CyAttributes networkAttributes;
 	private String[] nodeAttNames = null;
 	private String[] edgeAttNames = null;
 	private String[] networkAttNames = null;
 
 	private CyNetwork network;
 	private CyNetworkView networkView;
 
 	private ArrayList nodeList;
 	private ArrayList metanodeList;
 	private ArrayList edgeList;
 
 	JAXBContext jc;
 	ObjectFactory objFactory;
 
 	MetadataParser mdp;
 	Graph graph = null;
 	ExpressionData expression;
 
 	// Default CSS file name. Will be distributed with Cytoscape 2.3.
 	private static final String CSS_FILE = "base.css";
 
 	protected static final String FLOAT_TYPE = "float";
 	protected static final String INT_TYPE = "int";
 	protected static final String STRING_TYPE = "string";
 	protected static final String BOOLEAN_TYPE = "boolean";
 	protected static final String LIST_TYPE = "list";
 	protected static final String MAP_TYPE = "map";
 	protected static final String COMPLEX_TYPE = "complex";
 
 	public XGMMLWriter(CyNetwork network, CyNetworkView view) {
 		this.network = network;
 		this.networkView = view;
 		expression = Cytoscape.getExpressionData();
 
 		nodeAttributes = Cytoscape.getNodeAttributes();
 		edgeAttributes = Cytoscape.getEdgeAttributes();
 		networkAttributes = Cytoscape.getNetworkAttributes();
 
 		nodeList = new ArrayList();
 		metanodeList = new ArrayList();
 		edgeList = new ArrayList();
 
 		nodeAttNames = nodeAttributes.getAttributeNames();
 		edgeAttNames = edgeAttributes.getAttributeNames();
 		networkAttNames = networkAttributes.getAttributeNames();
 
 		try {
 			initialize();
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Generate JAXB objects for the XGMML file.
 	 * 
 	 * @throws JAXBException
 	 * @throws URISyntaxException
 	 */
 	private void initialize() throws JAXBException, URISyntaxException {
 		objFactory = new ObjectFactory();
 		RdfRDF metadata = null;
 		Att graphAtt = null;
 		Att globalGraphics = null;
 
 		jc = JAXBContext.newInstance(PACKAGE_NAME);
 		graph = objFactory.createGraph();
 
 		graphAtt = objFactory.createAtt();
 		graph.setId(network.getIdentifier());
 		graph.setLabel(network.getTitle());
 
 		// Metadata
 		mdp = new MetadataParser(network);
 		metadata = mdp.getMetadata();
 
 		graphAtt.setName(METADATA_NAME);
 		graphAtt.getContent().add(metadata);
 		graph.getAtt().add(graphAtt);
 
 		// Store background color
 		if (networkView != null) {
 			globalGraphics = objFactory.createAtt();
 			globalGraphics.setName(BACKGROUND);
 
 			globalGraphics.setValue(paint2string(networkView
 					.getBackgroundPaint()));
 			graph.getAtt().add(globalGraphics);
 		}
 
 	}
 
 	/**
 	 * Write the XGMML file.
 	 * 
 	 * @param writer
 	 *            :Witer to create XGMML file
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	public void write(Writer writer) throws JAXBException, IOException {
 
 		// writeNodes(nodeIt);
 		writeBaseNodes();
 		writeMetanodes();
 
 		// Create edge objects
 		writeEdges();
 
 		// write out network attributes
 		writeNetworkAttributes();
 
 		// This creates the header of the XML document.
 		writer.write("<?xml version='1.0'?>\n");
 
 		// Will be restored when CSS is ready.
 		// writer.write("<?xml-stylesheet type='text/css' href='" + CSS_FILE
 		// + "' ?>\n");
 
 		Marshaller m = jc.createMarshaller();
 
 		// Set proper namespace prefix (mainly for metadata)
 		try {
 			m.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
 			m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
 					new NamespacePrefixMapperImpl());
 		} catch (PropertyException e) {
 			// if the JAXB provider doesn't recognize the prefix mapper,
 			// it will throw this exception. Since being unable to specify
 			// a human friendly prefix is not really a fatal problem,
 			// you can just continue marshalling without failing
 			;
 		}
 
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		m.marshal(graph, writer);
 
 	}
 
 	private void writeEdges() throws JAXBException {
 		Iterator it = network.edgesIterator();
 
 		CyEdge curEdge = null;
 		Edge jxbEdge = null;
 
 		while (it.hasNext()) {
 			curEdge = (CyEdge) it.next();
 			jxbEdge = objFactory.createEdge();
 
 			jxbEdge.setId(curEdge.getIdentifier());
 			jxbEdge.setLabel(curEdge.getIdentifier());
 
 			// jxbEdge.setSource(curEdge.getSource().getIdentifier());
 			// jxbEdge.setTarget(curEdge.getTarget().getIdentifier());
 
 			jxbEdge.setSource(Integer.toString(curEdge.getSource()
 					.getRootGraphIndex()));
 			jxbEdge.setTarget(Integer.toString(curEdge.getTarget()
 					.getRootGraphIndex()));
 
 			if (networkView != null) {
 				EdgeView curEdgeView = networkView.getEdgeView(curEdge);
 
 				jxbEdge.setGraphics(getGraphics(EDGE, curEdgeView));
 			}
 			attributeWriter(EDGE, curEdge.getIdentifier(), jxbEdge);
 
 			edgeList.add(curEdge);
 			graph.getNodeOrEdge().add(jxbEdge);
 
 		}
 
 	}
 
 	// write out network attributes
 	protected void writeNetworkAttributes() throws JAXBException {
 
 		attributeWriter(NETWORK, network.getIdentifier(), null);
 	}
 
 	// Expand metanode information
 	protected void writeMetanode() {
 
 	}
 
 	/**
 	 * Extract attributes and map it to JAXB object
 	 * 
 	 * @param type    - type of attribute (node, edge, network)
 	 * @param id      - id of node, edge, network (key into CyAttributes)
      * @param target  - jaxb object
 	 *
 	 * @throws JAXBException
 	 */
 	protected void attributeWriter(int type, String id, Object target)
 			throws JAXBException {
 
 		// process type node
 		if (type == NODE) {
 			Node targetNode = (Node) target;
 			// process each attribute type
 			for (int i = 0; i < nodeAttNames.length; i++) {
 				if (nodeAttNames[i] == "node.width"
 						|| nodeAttNames[i] == "node.height") {
 					// Ignore
 				} else if (nodeAttNames[i] == "nodeType") {
 					String nType = nodeAttributes.getStringAttribute(id,
 							nodeAttNames[i]);
 					if (nType != null) {
 						targetNode.setName(nType);
 					} else {
 						targetNode.setName("base");
 					}
 				} else {
 					Att attr = createAttribute(id, nodeAttributes, nodeAttNames, i);
 					targetNode.getAtt().add(attr);
 				}
 			}
 		}
 		// process type edge
 		else if (type == EDGE){
 			// process each attribute type
 			for (int i = 0; i < edgeAttNames.length; i++) {
 				Att attr = createAttribute(id, edgeAttributes, edgeAttNames, i);
 				Edge targetEdge = (Edge) target;
 				targetEdge.getAtt().add(attr);
 			}
 		}
 		// process type network
 		else if (type == NETWORK) {
 			// process each attribute type
 			for (int i = 0; i < networkAttNames.length; i++) {
 				Att attr = createAttribute(id, networkAttributes, networkAttNames, i);
 				graph.getAtt().add(attr);
 			}
 		}
 	}
 
 	/**
 	 * Creates an attribute to write into XGMML file.
 	 * 
 	 * @param id            - id of node, edge or network
 	 * @param attributes    - CyAttributes to load
 	 * @param attNames      - ref to attribute names array
 	 * @param attNamesIndex - index into attribute names array
 	 * @return att          - Att to return (gets written into xgmml file)
 	 *
 	 * @throws JAXBException
 	 */
 	private Att createAttribute(String id, CyAttributes attributes, String[] attNames, int attNamesIndex)
 		throws JAXBException {
 
 		// set attribute name
 		String attributeName = attNames[attNamesIndex];
 
 		// create an attribute and its type
 		Att attr = objFactory.createAtt();
 		byte attType = attributes.getType(attributeName);
 
 		// process float
 		if (attType == CyAttributes.TYPE_FLOATING) {
 			Double dAttr = attributes.getDoubleAttribute(id, attributeName);
 			attr.setName(attributeName);
 			attr.setLabel(FLOAT_TYPE);
 			if (dAttr != null) attr.setValue(dAttr.toString());
 		}
 		// process integer
 		else if (attType == CyAttributes.TYPE_INTEGER) {
 			Integer iAttr = attributes.getIntegerAttribute(id, attributeName);
 			attr.setName(attributeName);
 			attr.setLabel(INT_TYPE);
 			if (iAttr != null) attr.setValue(iAttr.toString());
 		}
 		// process string
 		else if (attType == CyAttributes.TYPE_STRING) {
 			String sAttr = attributes.getStringAttribute(id, attributeName);
 			attr.setName(attributeName);
 			attr.setLabel(STRING_TYPE);
 			if (sAttr != null) {
 				attr.setValue(sAttr.toString());
 			}
 			else if (attributeName == "nodeType"){
 				attr.setValue(NORMAL);
 			}
 		}
 		// process boolean
 		else if (attType == CyAttributes.TYPE_BOOLEAN) {
 			Boolean bAttr = attributes.getBooleanAttribute(id, attributeName);
 			attr.setName(attributeName);
 			attr.setLabel(BOOLEAN_TYPE);
 			if (bAttr != null) attr.setValue(bAttr.toString());
 		}
 		// process simple list
 		else if (attType == CyAttributes.TYPE_SIMPLE_LIST) {
 			// get the attribute list
 			List listAttr = attributes.getAttributeList(id, attributeName);
 			// set attribute name and label
 			attr.setName(attributeName);
 			attr.setLabel(LIST_TYPE);
 			// interate through the list
 			Iterator listIt = listAttr.iterator();
 			while (listIt.hasNext()) {
 				// get the attribute from the list
 				Object obj = listIt.next();
 				// create a "child" attribute to store in xgmml file
 				Att memberAttr = objFactory.createAtt();
 				// set child attribute value & label
 				memberAttr.setValue(obj.toString());
 				memberAttr.setLabel(checkType(obj));
 				// add child attribute to parent
 				attr.getContent().add(memberAttr);
 			}
 		}
 		// process simple map
 		else if (attType == CyAttributes.TYPE_SIMPLE_MAP) {
 			// get the attribute map
 			Map mapAttr = attributes.getAttributeMap(id, attributeName);
 			// set our attribute name and label
 			attr.setName(attributeName);
 			attr.setLabel(MAP_TYPE);
 			// interate through the map
 			Iterator mapIt = mapAttr.keySet().iterator();
 			while (mapIt.hasNext()) {
 				// get the attribute from the map
 				Object obj = mapIt.next();
 				String key = (String)obj;
 				// create a "child" attribute to store in xgmml file
 				Att memberAttr = objFactory.createAtt();
 				// set child attribute name, label, and value
 				memberAttr.setName(key);
 				memberAttr.setLabel(checkType(mapAttr.get(key)));
 				memberAttr.setValue(mapAttr.get(key).toString());
 				// add child attribute to parent
 				attr.getContent().add(memberAttr);
 			}
 		}
 		// process complex type
 		else if (attType == CyAttributes.TYPE_COMPLEX) {
 			attr = createComplexAttribute(id, attributes, attributeName);
 		}
 
 		// outta here
 		return attr;
 	}
 
 	/**
 	 * Creates an attribute to write into XGMML file from an attribute whose type is COMPLEX.
 	 * 
 	 * @param id            - id of node, edge or network
 	 * @param attributes    - CyAttributes to load
 	 * @param attributeName - name of attribute
 	 * @return att          - Att to return (gets written into xgmml file)
 	 *
 	 * @throws JAXBException
 	 */
 	private Att createComplexAttribute(String id, CyAttributes attributes, String attributeName)
 		throws JAXBException {
 
 		// the attribute to return
 		Att attrToReturn = objFactory.createAtt();
 
 		// get the multihashmap definition
 		MultiHashMap mmap = attributes.getMultiHashMap();
 		MultiHashMapDefinition mmapDef = attributes.getMultiHashMapDefinition();
 
 		// get number & types of dimensions
 		byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attributeName);
 
 		// set top level attribute name, label
 		attrToReturn.setLabel(COMPLEX_TYPE);
 		attrToReturn.setName(attributeName);
 		attrToReturn.setValue(String.valueOf(dimTypes.length));
 
 		// grab the complex attribute structure
 		Map complexAttributeStructure = getComplexAttributeStructure(mmap, id, attributeName, null, 0, dimTypes.length);
 
 		// determine val type, get its string equilvalent to store in xgmml
 		String valTypeStr = getType(mmapDef.getAttributeValueType(attributeName));
 
 		// walk the structure
 		Iterator complexAttributeIt = complexAttributeStructure.keySet().iterator();
 		while (complexAttributeIt.hasNext()) {
 			// grab the next key and map to add to xgmml
 			Object key = complexAttributeIt.next();
 			Map thisKeyMap = (Map)complexAttributeStructure.get(key);
 			// create an Att instance for this key
 			// and set its name, label, & value
 			Att thisKeyAttr = objFactory.createAtt();
 			thisKeyAttr.setLabel(getType(dimTypes[0]));
 			thisKeyAttr.setName(key.toString());
 			thisKeyAttr.setValue(String.valueOf(thisKeyMap.size()));
 			// now lets walk the keys structure and add to its attributes content
 			thisKeyAttr.getContent().add(walkComplexAttributeStructure(null, thisKeyMap, valTypeStr, dimTypes, 1));
 			// this keys attribute should get added to the attribute we wil return
 			attrToReturn.getContent().add(thisKeyAttr);
 		}
 
 		// outta here
 		return attrToReturn;
 	}
 
 	/**
 	 * Returns a map where the key(s) are each key in the attribute key space,
 	 * and the value is another map or the attribute value.
 	 *
 	 * For example,  if the following key:
 	 * 
 	 * {externalref1}{authors}{1} pointed to the following value:
 	 *
 	 * "author 1 name",
 	 *
 	 * Then we would have a Map where the key is externalref1,
 	 * the value is a Map where the key is {authors},
 	 * the value is a Map where the key is {1},
 	 * the value is "author 1 name".
 	 * 
 	 * @param mmap          - reference to MultiHashMap used by CyAttributes
 	 * @param id            - id of node, edge or network
 	 * @param attributeName - name of attribute
 	 * @param keys          - array of objects which store attribute keys
 	 * @param keysIndex     - index into keys array we should add the next key
 	 * @param numKeyDimensions - the number of keys used for given attribute name
 	 * @return Map             - ref to Map interface 
 	 */
 	private Map getComplexAttributeStructure(MultiHashMap mmap, String id, String attributeName, Object[] keys, int keysIndex, int numKeyDimensions) {
 
 		// out of here if we've interated through all dimTypes
 		if (keysIndex == numKeyDimensions) return null;
 
 		// the hashmap to return
 		Map keyHashMap = new HashMap();
 
 		// create a new object array to store keys for this interation
 		// copy all exisiting keys into it
 		Object[] newKeys = new Object[keysIndex+1];
 		for (int lc = 0; lc < keysIndex; lc++) {
 			newKeys[lc] = keys[lc];
 		}
 
 		// get the key span
 		Iterator keyspan = mmap.getAttributeKeyspan(id, attributeName, keys);
 		while (keyspan.hasNext()) {
 			Object newKey = keyspan.next();
 			newKeys[keysIndex] = newKey;
 			Map nextLevelMap = getComplexAttributeStructure(mmap, id, attributeName, newKeys, keysIndex+1, numKeyDimensions);
 			Object objectToStore = (nextLevelMap == null) ? mmap.getAttributeValue(id, attributeName, newKeys) : nextLevelMap;
 			keyHashMap.put(newKey, objectToStore);
 		}
 
 		// outta here
 		return keyHashMap;
 	}
 
 	/**
 	 * Walks a complex attribute map and creates a complex attribute on behalf of createComplexAttribute().
 	 * 
 	 * @param parentAttr                - ref to a parentAttr we will be adding to (in certain cases this can be null)
 	 * @param complexAttributeStructure - ref to Map returned from a prior call to getComplexAttributeStructure.
 	 * @param attributeType             - the type (string, boolean, float, int) of the attribute value this tree describes
 	 * @param dimTypes                  - a byte array returned from a prior call to getAttributeKeyspaceDimensionTypes(attributeName);
 	 * @param dimTypesIndex             - the index into the dimTypes array we are should work on
 	 * @return att                      - ref to Att which describes the complex type attribute.  An example/description is as follows:
 	 *
 	 * For an arbitrarily complex data structure, like a pseudo hash with the following structure:
 	 *
 	 * {"externalref1"}->{"authors"}->{1}->"author1 name";
 	 * {"externalref1"}->{"authors"}->{2}->"author2 name";
 	 * {"externalref1"}->{"authors"}->{3}->"author3 name";
 	 *
 	 * where the keys externalref1 and authors are strings, and keys 1, 2, 3 are integers,
 	 * and the values (author1 name, author2 name, author3 name) are strings, we would have the 
 	 * following attributes written to the xgmml file:
 	 * 
 	 *    <att label="complex" name="publication references" value="3">
      *        <att label="string" name="externalref1" value="1">
      *            <att label="string" name="authors" value="3">
      *                <att label="int" name="2" value="1">
      *                    <att label="string" value="author2 name"/>
      *                </att>
      *                <att label="int" name="1" value="1">
      *                    <att label="string" value="author1 name"/>
 	 *                </att>
      *                <att label="int" name="3" value="1">
      *                    <att label="string" value="author3 name"/>
      *                </att>
      *            </att>
      *        </att>
      *    </att>
 	 *
 	 * Notes:
 	 * - value attribute property for keys is assigned the number of sub-elements the key references
 	 * - value attribute property for values is equal to the value
 	 * - name attribute property for attributes is only set for keys, and the value of this property is the key name.
 	 * - label attribute property is equal to the data type of the key or value.
 	 * - name attribute properties are only set for keys
 	 *
 	 * @throws JAXBException
 	 * @throws IllegalArgumentException
 	 */
 	private Att walkComplexAttributeStructure(Att parentAttr, Map complexAttributeStructure, String attributeType, byte[] dimTypes, int dimTypesIndex) 
 		throws JAXBException, IllegalArgumentException {
 
 		// att to return
 		Att attrToReturn = null;
 
 		Iterator mapIt = complexAttributeStructure.keySet().iterator();
 		while (mapIt.hasNext()) {
 			Object key = mapIt.next();
 			Object possibleAttributeValue = complexAttributeStructure.get(key);
 			if (possibleAttributeValue instanceof Map){
 				// we need to create an instance of Att to return
 				attrToReturn = objFactory.createAtt();
 				// we have a another map
 				attrToReturn.setLabel(getType(dimTypes[dimTypesIndex]));
 				attrToReturn.setName((String)key);
 				attrToReturn.setValue(String.valueOf(((Map)possibleAttributeValue).size()));
 				// walk the next map
 				// note: we check returned attribute address to make sure we are not adding to ourselves
 				Att returnedAttribute = walkComplexAttributeStructure(attrToReturn,
 																	  (Map)possibleAttributeValue,
 																	  attributeType, dimTypes, dimTypesIndex+1);
 				// if this is a new att, add it to the Att we will be returning
 				if (returnedAttribute != attrToReturn) attrToReturn.getContent().add(returnedAttribute);
 			}
 			else {
 				// if we are here, we must be adding attributes to the parentAttr
 				if (parentAttr == null) {
 					throw new IllegalArgumentException("Att argument should not be null.");
 				}
 				// the attribute to return this round is our parent, we just attach stuff to it
 				attrToReturn = parentAttr;
 				// create our key attribute
 				Att keyAttr = objFactory.createAtt();
 				keyAttr.setLabel(getType(dimTypes[dimTypesIndex]));
 				keyAttr.setName(key.toString());
 				keyAttr.setValue(String.valueOf(1));
 				// create our value attribute
 				Att valueAttr = objFactory.createAtt();
 				valueAttr.setLabel(attributeType);
				valueAttr.setValue((String)possibleAttributeValue);
 				keyAttr.getContent().add(valueAttr);
 				attrToReturn.getContent().add(keyAttr);
 			}
 		}
 
 		// outta here
 		return attrToReturn;
 	}
 
 	protected Graphics getGraphics(int type, Object target)
 			throws JAXBException {
 
 		Graphics graphics = objFactory.createGraphics();
 
 		if (type == NODE) {
 			NodeView curNodeView = (NodeView) target;
 
 			/**
 			 * GML compatible attributes
 			 */
 			// Node shape
 			graphics.setType(number2shape(curNodeView.getShape()));
 
 			// Node size and position
 			graphics.setH(curNodeView.getHeight());
 			graphics.setW(curNodeView.getWidth());
 			graphics.setX(curNodeView.getXPosition());
 			graphics.setY(curNodeView.getYPosition());
 
 			// Node color
 			graphics.setFill(paint2string(curNodeView.getUnselectedPaint()));
 
 			// Node border basic info.
 			BasicStroke borderType = (BasicStroke) curNodeView.getBorder();
 
 			float borderWidth = borderType.getLineWidth();
 			BigInteger intWidth = BigInteger.valueOf((long) borderWidth);
 			graphics.setWidth(intWidth);
 			graphics.setOutline(paint2string(curNodeView.getBorderPaint()));
 
 			/**
 			 * Extended attributes supported by GINY
 			 */
 			// Store Cytoscap-local graphical attributes
 			Att cytoscapeNodeAttr = objFactory.createAtt();
 			cytoscapeNodeAttr.setName("cytoscapeNodeGraphicsAttributes");
 
 			Att transparency = objFactory.createAtt();
 			Att nodeLabelFont = objFactory.createAtt();
 			Att borderLineType = objFactory.createAtt();
 
 			transparency.setName("nodeTransparency");
 			nodeLabelFont.setName("nodeLabelFont");
 			borderLineType.setName("borderLineType");
 
 			transparency.setValue(Double
 					.toString(curNodeView.getTransparency()));
 			nodeLabelFont
 					.setValue(encodeFont(curNodeView.getLabel().getFont()));
 
 			// Where should we store line-type info???
 			float[] dash = borderType.getDashArray();
 			if (dash == null) {
 				// System.out.println("##Border is NORMAL LINE");
 				borderLineType.setValue("solid");
 			} else {
 				// System.out.println("##Border is DASHED LINE");
 				String dashArray = null;
 				for (int i = 0; i < dash.length; i++) {
 					dashArray = Double.toString(dash[i]);
 					if (i < dash.length - 1) {
 						dashArray = dashArray + ",";
 					}
 				}
 				borderLineType.setValue(dashArray);
 			}
 			cytoscapeNodeAttr.getContent().add(transparency);
 			cytoscapeNodeAttr.getContent().add(nodeLabelFont);
 			cytoscapeNodeAttr.getContent().add(borderLineType);
 
 			graphics.getAtt().add(cytoscapeNodeAttr);
 
 			return graphics;
 		} else if (type == EDGE) {
 			EdgeView curEdgeView = (EdgeView) target;
 
 			/**
 			 * GML compatible attributes
 			 */
 			// Width
 			graphics.setWidth(BigInteger.valueOf((long) curEdgeView
 					.getStrokeWidth()));
 			// Color
 			graphics.setFill(paint2string(curEdgeView.getUnselectedPaint()));
 
 			/**
 			 * Extended attributes supported by GINY
 			 */
 			// Store Cytoscap-local graphical attributes
 			Att cytoscapeEdgeAttr = objFactory.createAtt();
 			cytoscapeEdgeAttr.setName("cytoscapeEdgeGraphicsAttributes");
 
 			Att sourceArrow = objFactory.createAtt();
 			Att targetArrow = objFactory.createAtt();
 			Att edgeLabelFont = objFactory.createAtt();
 			Att edgeLineType = objFactory.createAtt();
 			Att sourceArrowColor = objFactory.createAtt();
 			Att targetArrowColor = objFactory.createAtt();
 
 			sourceArrow.setName("sourceArrow");
 			targetArrow.setName("targetArrow");
 			edgeLabelFont.setName("edgeLabelFont");
 			edgeLineType.setName("edgeLineType");
 			sourceArrowColor.setName("sourceArrowColor");
 			targetArrowColor.setName("targetArrowColor");
 
 			sourceArrow.setValue(Integer.toString(curEdgeView
 					.getSourceEdgeEnd()));
 			targetArrow.setValue(Integer.toString(curEdgeView
 					.getTargetEdgeEnd()));
 
 			edgeLabelFont
 					.setValue(encodeFont(curEdgeView.getLabel().getFont()));
 
 			edgeLineType.setValue(lineTypeBuilder(curEdgeView).toString());
 
 			// System.out.println("Source Color is :" +
 			// curEdgeView.getSourceEdgeEndPaint().toString());
 			// System.out.println("Target Color is :" +
 			// curEdgeView.getTargetEdgeEndPaint().toString());
 			// System.out.println("Source Type is :" +
 			// curEdgeView.getSourceEdgeEnd());
 			// System.out.println("Target Type is :" +
 			// curEdgeView.getTargetEdgeEnd());
 			//			
 
 			sourceArrowColor.setValue(paint2string(curEdgeView
 					.getSourceEdgeEndPaint()));
 			targetArrowColor.setValue(paint2string(curEdgeView
 					.getTargetEdgeEndPaint()));
 
 			cytoscapeEdgeAttr.getContent().add(sourceArrow);
 			cytoscapeEdgeAttr.getContent().add(targetArrow);
 			cytoscapeEdgeAttr.getContent().add(edgeLabelFont);
 			cytoscapeEdgeAttr.getContent().add(edgeLineType);
 			cytoscapeEdgeAttr.getContent().add(sourceArrowColor);
 			cytoscapeEdgeAttr.getContent().add(targetArrowColor);
 
 			graphics.getAtt().add(cytoscapeEdgeAttr);
 
 			return graphics;
 		}
 
 		return null;
 	}
 
 	protected void expand(CyNode node, Node metanode, int[] childrenIndices)
 			throws JAXBException {
 		CyNode childNode = null;
 		Att children = objFactory.createAtt();
 		children.setName("metanodeChildren");
 		Graph subGraph = objFactory.createGraph();
 		Node jxbChildNode = null;
 
 		// test
 
 		for (int i = 0; i < childrenIndices.length; i++) {
 			childNode = (CyNode) Cytoscape.getRootGraph().getNode(
 					childrenIndices[i]);
 
 			jxbChildNode = objFactory.createNode();
 			jxbChildNode.setId(childNode.getIdentifier());
 			jxbChildNode.setLabel(childNode.getIdentifier());
 			subGraph.getNodeOrEdge().add(jxbChildNode);
 			int[] grandChildrenIndices = network
 					.getRootGraph()
 					.getNodeMetaChildIndicesArray(childNode.getRootGraphIndex());
 			if (grandChildrenIndices == null
 					|| grandChildrenIndices.length == 0) {
 				attributeWriter(NODE, childNode.getIdentifier(), jxbChildNode);
 				metanode.setGraphics(getGraphics(NODE, networkView
 						.getNodeView(node)));
 			} else {
 
 				// System.out.print("This is a metanode!: "
 				// + jxbChildNode.getLabel() + ", number = "
 				// + childrenIndices.length);
 				expand(childNode, jxbChildNode, grandChildrenIndices);
 			}
 
 		}
 		attributeWriter(NODE, metanode.getId(), metanode);
 
 		children.getContent().add(subGraph);
 		metanode.getAtt().add(children);
 	}
 
 	// Convert number to shape string
 	protected String number2shape(int type) {
 		if (type == NodeView.ELLIPSE) {
 			return ELLIPSE;
 		} else if (type == NodeView.RECTANGLE) {
 			return RECTANGLE;
 		} else if (type == NodeView.DIAMOND) {
 			return DIAMOND;
 		} else if (type == NodeView.HEXAGON) {
 			return HEXAGON;
 		} else if (type == NodeView.OCTAGON) {
 			return OCTAGON;
 		} else if (type == NodeView.PARALELLOGRAM) {
 			return PARALELLOGRAM;
 		} else if (type == NodeView.TRIANGLE) {
 			return TRIANGLE;
 		} else {
 			return null;
 		}
 	}
 
 	protected String paint2string(Paint p) {
 
 		Color c = (Color) p;
 		return ("#"// +Integer.toHexString(c.getRGB());
 				+ Integer.toHexString(256 + c.getRed()).substring(1)
 				+ Integer.toHexString(256 + c.getGreen()).substring(1) + Integer
 				.toHexString(256 + c.getBlue()).substring(1));
 	}
 
 	/**
 	 * Returns all nodes in this network.
 	 * 
 	 * @throws JAXBException
 	 * 
 	 * 
 	 */
 	private void writeBaseNodes() throws JAXBException {
 
 		Node jxbNode = null;
 
 		CyNode curNode = null;
 
 		Iterator it = network.nodesIterator();
 
 		while (it.hasNext()) {
 			curNode = (CyNode) it.next();
 			jxbNode = objFactory.createNode();
 
 			String targetnodeID = Integer.toString(curNode.getRootGraphIndex());
 			// System.out.println("nodeID ======== " + targetnodeID);
 
 			// jxbNode.setId(curNode.getIdentifier());
 
 			jxbNode.setId(targetnodeID);
 			jxbNode.setLabel(curNode.getIdentifier());
 			jxbNode.setName("base");
 
 			// Add graphics if available
 			if (networkView != null) {
 				NodeView curNodeView = networkView.getNodeView(curNode);
 				jxbNode.setGraphics(getGraphics(NODE, curNodeView));
 			}
 			
 			attributeWriter(NODE, curNode.getIdentifier(), jxbNode);
 			if (isMetanode(curNode)) {
 				nodeList.add(curNode);
 				metanodeList.add(curNode);
 				expandChildren(curNode);
 			} else {
 				nodeList.add(curNode);
 				graph.getNodeOrEdge().add(jxbNode);
 			}
 
 		}
 
 		// int count = 0;
 		// Iterator it2 = metanodeList.iterator();
 		// while (it2.hasNext()) {
 		// CyNode test = (CyNode) it2.next();
 		// count++;
 		// System.out.println("%%%%% it test count: " + count + ", "
 		// + test.getIdentifier());
 		// }
 
 	}
 
 	private Node buildJAXBNode(CyNode node) throws JAXBException {
 		Node jxbNode = null;
 
 		jxbNode = objFactory.createNode();
 		String targetnodeID = Integer.toString(node.getRootGraphIndex());
 		jxbNode.setId(targetnodeID);
 		jxbNode.setLabel(node.getIdentifier());
 		
 		if(networkView != null) {
 			NodeView curNodeView = networkView.getNodeView(node);
 			jxbNode.setGraphics(getGraphics(NODE, curNodeView));
 		}
 		attributeWriter(NODE, node.getIdentifier(), jxbNode);
 		return jxbNode;
 	}
 
 	private void expandChildren(CyNode node) throws JAXBException {
 
 		CyNode childNode = null;
 		Node jxbNode = null;
 
 		int[] childrenIndices = network.getRootGraph()
 				.getNodeMetaChildIndicesArray(node.getRootGraphIndex());
 
 		for (int i = 0; i < childrenIndices.length; i++) {
 			childNode = (CyNode) network.getRootGraph().getNode(
 					childrenIndices[i]);
 
 			if (isMetanode(childNode)) {
 				metanodeList.add(childNode);
 				nodeList.add(childNode);
 				expandChildren(childNode);
 
 			} else {
 				nodeList.add(childNode);
 				jxbNode = buildJAXBNode(childNode);
 				jxbNode.setName("base");
 				graph.getNodeOrEdge().add(jxbNode);
 			}
 		}
 	}
 
 	/**
 	 * Metanode has different format in XML. It is a node with subgraph.
 	 * 
 	 * @throws JAXBException
 	 * 
 	 */
 	private void writeMetanodes() throws JAXBException {
 		Iterator it = metanodeList.iterator();
 
 		while (it.hasNext()) {
 			CyNode curNode = (CyNode) it.next();
 			Node jxbNode = null;
 			jxbNode = buildJAXBNode(curNode);
 
 			jxbNode.setName("metaNode");
 
 			int[] childrenIndices = network.getRootGraph()
 					.getNodeMetaChildIndicesArray(curNode.getRootGraphIndex());
 			Att children = objFactory.createAtt();
 			children.setName("metanodeChildren");
 			Graph subGraph = objFactory.createGraph();
 
 			for (int i = 0; i < childrenIndices.length; i++) {
 				CyNode childNode = null;
 				Node childJxbNode = null;
 
 				childNode = (CyNode) network.getRootGraph().getNode(
 						childrenIndices[i]);
 				childJxbNode = objFactory.createNode();
 				childJxbNode.setId(childNode.getIdentifier());
 
 				childJxbNode.setName("reference");
 				subGraph.getNodeOrEdge().add(childJxbNode);
 
 			}
 			children.getContent().add(subGraph);
 			jxbNode.getAtt().add(children);
 			graph.getAtt().add(jxbNode);
 		}
 
 	}
 
 	/**
 	 * Returns true if the node is a metanode.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private boolean isMetanode(CyNode node) {
 
 		int[] childrenIndices = network.getRootGraph()
 				.getNodeMetaChildIndicesArray(node.getRootGraphIndex());
 		if (childrenIndices == null || childrenIndices.length == 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private String encodeFont(Font font) {
 		// Encode font into "fontname-style-pointsize" string
 		String fontString = font.getName() + "-" + font.getStyle() + "-"
 				+ font.getSize();
 
 		return fontString;
 	}
 
 	private String checkType(Object obj) {
 		if (obj.getClass() == String.class) {
 			return STRING_TYPE;
 		} else if (obj.getClass() == Integer.class) {
 			return INT_TYPE;
 		} else if (obj.getClass() == Double.class
 				|| obj.getClass() == Float.class) {
 			return FLOAT_TYPE;
 		} else if (obj.getClass() == Boolean.class) {
 			return BOOLEAN_TYPE;
 		} else
 			return null;
 	}
 
 	/**
 	 * Given a byte describing a MultiHashMapDefinition TYPE_*,
 	 * return the proper XGMMLWriter type.
 	 *
 	 * @param dimType - byte as described in MultiHashMapDefinition
 	 * @return String
 	 */
 	private String getType(byte dimType) {
 
 		if (dimType == MultiHashMapDefinition.TYPE_BOOLEAN) return BOOLEAN_TYPE;
 		if (dimType == MultiHashMapDefinition.TYPE_FLOATING_POINT) return FLOAT_TYPE;
 		if (dimType == MultiHashMapDefinition.TYPE_INTEGER) return INT_TYPE;
 		if (dimType == MultiHashMapDefinition.TYPE_STRING) return STRING_TYPE;
 
 		// houston we have a problem
 		return null;
 	}
 
 	private LineType lineTypeBuilder(EdgeView view) {
 
 		LineType lineType = LineType.LINE_1;
 		BasicStroke stroke = (BasicStroke) view.getStroke();
 
 		float[] dash = stroke.getDashArray();
 
 		float width = stroke.getLineWidth();
 		if (dash == null) {
 			// Normal line. check width
 			if (width == 1.0) {
 				lineType = LineType.LINE_1;
 			} else if (width == 2.0) {
 				lineType = LineType.LINE_2;
 			} else if (width == 3.0) {
 				lineType = LineType.LINE_3;
 			} else if (width == 4.0) {
 				lineType = LineType.LINE_4;
 			} else if (width == 5.0) {
 				lineType = LineType.LINE_5;
 			} else if (width == 6.0) {
 				lineType = LineType.LINE_6;
 			} else if (width == 7.0) {
 				lineType = LineType.LINE_7;
 			}
 			// System.out.println("SOLID: " + width);
 		} else {
 			if (width == 1.0) {
 				lineType = LineType.DASHED_1;
 			} else if (width == 2.0) {
 				lineType = LineType.DASHED_2;
 			} else if (width == 3.0) {
 				lineType = LineType.DASHED_3;
 			} else if (width == 4.0) {
 				lineType = LineType.DASHED_4;
 			} else if (width == 5.0) {
 				lineType = LineType.DASHED_5;
 			}
 			// System.out.println("DASH: " + width);
 		}
 
 		return lineType;
 	}
 
 }
 
 class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
 
 	/**
 	 * Returns a preferred prefix for the given namespace URI.
 	 * 
 	 * This method is intended to be overrided by a derived class.
 	 * 
 	 * @param namespaceUri
 	 *            The namespace URI for which the prefix needs to be found.
 	 *            Never be null. "" is used to denote the default namespace.
 	 * @param suggestion
 	 *            When the content tree has a suggestion for the prefix to the
 	 *            given namespaceUri, that suggestion is passed as a parameter.
 	 *            Typicall this value comes from the QName.getPrefix to show the
 	 *            preference of the content tree. This parameter may be null,
 	 *            and this parameter may represent an already occupied prefix.
 	 * @param requirePrefix
 	 *            If this method is expected to return non-empty prefix. When
 	 *            this flag is true, it means that the given namespace URI
 	 *            cannot be set as the default namespace.
 	 * 
 	 * @return null if there's no prefered prefix for the namespace URI. In this
 	 *         case, the system will generate a prefix for you.
 	 * 
 	 * Otherwise the system will try to use the returned prefix, but generally
 	 * there's no guarantee if the prefix will be actually used or not.
 	 * 
 	 * return "" to map this namespace URI to the default namespace. Again,
 	 * there's no guarantee that this preference will be honored.
 	 * 
 	 * If this method returns "" when requirePrefix=true, the return value will
 	 * be ignored and the system will generate one.
 	 */
 	public String getPreferredPrefix(String namespaceUri, String suggestion,
 			boolean requirePrefix) {
 		// I want this namespace to be mapped to "xsi"
 		if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri))
 			return "xsi";
 
 		// For RDF.
 		if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri))
 			return "rdf";
 
 		// Dublin core semantics.
 		if ("http://purl.org/dc/elements/1.1/".equals(namespaceUri))
 			return "dc";
 
 		// otherwise I don't care. Just use the default suggestion, whatever it
 		// may be.
 		return suggestion;
 	}
 
 	public String[] getPreDeclaredNamespaceUris() {
 		return new String[] { "http://www.w3.org/2001/XMLSchema-instance",
 				"http://www.w3.org/1999/xlink",
 				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
 				"http://purl.org/dc/elements/1.1/" };
 	}
 }
