 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.socialnetworking;
 
 import java.util.HashMap;
import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
import java.util.Map;
 import java.util.Properties;
import java.util.Set;
 
 import javax.xml.transform.OutputKeys;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPNamedEntity;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.seasr.meandre.support.generic.text.XMLUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Lily Dong
  */
 
 @Component(
         creator = "Lily Dong",
 		description = "<p>Overview: <br> This component creates " +
 		              "link between tuples and outputs it " +
 		              "as GraphML document.</p>",
         name = "Link Creation",
         tags = "tuple, link, GraphML",
         firingPolicy = FiringPolicy.all,
         rights = Licenses.UofINCSA,
         baseURL="meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 
 public class LinkCreation extends AbstractExecutableComponent {
 
 	//------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TUPLES,
 			description = "Set of tuples." +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentInput(
 			name = Names.PORT_META_TUPLE,
 			description = "Meta data for tuples." +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        name = Names.PORT_XML,
 	        description = "XML document created from tuples." +
 	            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_XML = Names.PORT_XML;
 
 	//------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        description = "Entity types (comma delimited list).",
             name = Names.PROP_ENTITIES,
             defaultValue =  "person"
 	)
 	protected static final String PROP_ENTITIES = Names.PROP_ENTITIES;
 
 	@ComponentProperty(
	        description = "If the distance between two entities is within the offset," +
 	        "then they are considered to be adjacent and are marked linked. " ,
             name = Names.PROP_OFFSET,
             defaultValue =  "10"
 	)
 	protected static final String PROP_OFFSET = Names.PROP_OFFSET;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	private String _entities;
 	private int offset;
 
 	private HashMap<String, LinkedList<Integer>> map; //store position
 	private Document outputDoc; //store output document
 	private Element elGraph; //store graph node
 	private Hashtable<String, String> table; //store id
 	private int index; //store number
 
 	private Properties _xmlProperties;
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		offset = Integer.parseInt(ccp.getProperty(PROP_OFFSET));
 		_entities = ccp.getProperty(PROP_ENTITIES);
 
 		_xmlProperties = new Properties();
         _xmlProperties.put(OutputKeys.OMIT_XML_DECLARATION, "no");
         _xmlProperties.put(OutputKeys.INDENT, "yes");
         _xmlProperties.put(OutputKeys.ENCODING, "UTF-8");
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		//initialize
 		index = 0;
 		map = new HashMap<String, LinkedList<Integer>>();
 		table = new Hashtable<String, String>();
 		initialzeDoc();
 
 		//get input data
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
 		console.fine(tuplePeer.toString());
 
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
 
 		int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.SENTENCE_ID_FIELD);
 		int TEXT_START_IDX = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TEXT_START_FIELD);
         int TYPE_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TYPE_FIELD);
         int TEXT_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TEXT_FIELD);
 
 		// construct map
 		for (Strings ss: in) {
 			String[] s = BasicDataTypesTools.stringsToStringArray(ss);
 			String type = s[TYPE_IDX]; //type
 			if(_entities.indexOf(type) != -1) {
 				String vertex = s[TEXT_IDX]; //value
 				Integer pos = Integer.valueOf(s[TEXT_START_IDX]); //position
 				calculateAdjacence(vertex, pos);
 				addVertex(vertex, pos); //add to map
 			}
 		}
 
 		String xmlString = DOMUtils.getString(outputDoc, _xmlProperties);
 		xmlString = XMLUtils.stripNonValidXMLCharacters(xmlString);
 		cc.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(xmlString));
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
 	//--------------------------------------------------------------------------------------------
 
 	/**
 	 *
 	 * @param vertex to be calculated against the other vertices.
 	 * @param pos of vertex
 	 */
 	private void calculateAdjacence(String vertex, Integer pos) {
 		Set set = map.entrySet();
 		Iterator iterator = set.iterator();
 		while(iterator.hasNext()){
 			Map.Entry entry = (Map.Entry)iterator.next();
 			String theVertex = (String)entry.getKey();
 			LinkedList theList = (LinkedList)entry.getValue();
 
 			if(!theVertex.equals(vertex)) { //not the same vertex
 				Object[] objs = theList.toArray();
 				for(Object thePos: objs) {
 					if(Math.abs(pos-(Integer)thePos)<offset) {//adjacent
 						if(!isNodeAvailable(vertex)) {//add to output document
 							String id = "n"+(index++);
 							table.put(vertex, id);
 							addNode(id, vertex);
 						}
 						if(!isNodeAvailable(theVertex)) {//add to output document
 							String id = "n"+(index++);
 							table.put(theVertex, id);
 							addNode(id, theVertex);
 						}
 						if(!isEdgeAvailable(vertex, theVertex))//add to output document
 							addEdge(vertex, theVertex);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @param vertex to be retrieved.
 	 * @param pos to be inserted into the linked list.
 	 */
 	private void addVertex(String vertex, Integer pos) {
 		//<vertex, linked list>
 		if (map.containsKey(vertex)) {//retrieve vertex
			LinkedList<Integer> value = map.get(vertex);
 			if(value.indexOf(pos)!=-1)
 				value.add(pos);
 		} else { //insert vertex
 			LinkedList<Integer> value = new LinkedList<Integer>();
 			value.add(pos); //position
 			map.put(vertex, value);
 		}
 	}
 
 	/**
 	 * initialize the output doucment.
 	 */
 	private void initialzeDoc() {
 		outputDoc = DOMUtils.createNewDocument();
 	    Element root = outputDoc.createElement("graphml");
 	    outputDoc.appendChild(root);
 
 	    Element elKey = outputDoc.createElement("key");
 	    elKey.setAttribute("id", "site");
 	    elKey.setAttribute("for", "node");
 	    elKey.setAttribute("attr.name", "label");
 	    elKey.setAttribute("attr.type", "string");
 	    elKey.setTextContent("unknown");
 	    root.appendChild(elKey);
 
 	    elGraph = outputDoc.createElement("graph");
 	    elGraph.setAttribute("id", "G");
 	    elGraph.setAttribute("edgedefault", "directed");
 	    root.appendChild(elGraph);
 	}
 
 	/**
 	 *
 	 * @param id to identify node.
 	 * @param value to specify data.
 	 */
 	private void addNode(String id, String value) {
 		Element elNode = outputDoc.createElement("node");
 		elNode.setAttribute("id", id);
 		Element elData = outputDoc.createElement("data");
 		elData.setAttribute("key", "site");
 		elData.setTextContent(value);
 		elNode.appendChild(elData);
 		elGraph.appendChild(elNode);
 	}
 
 	/**
 	 *
 	 * @param source of one endpoint of an edge
 	 * @param target of the other endpoint of an edge
 	 */
 	private void addEdge(String source, String target) {
 		Element elNode = outputDoc.createElement("edge");
 		elNode.setAttribute("source", table.get(source));
 		elNode.setAttribute("target", table.get(target));
 		elGraph.appendChild(elNode);
 	}
 
 	/**
 	 *
 	 * @param value of node
 	 * @return true if node exit
 	 */
 	private boolean isNodeAvailable(String value) {
 		NodeList nodes = outputDoc.getElementsByTagName("data");
 		for (int i=0; i<nodes.getLength(); i++) {
 			Element elEntity = (Element)nodes.item(i);
 			if(value.equals(elEntity.getTextContent()))
 					return true; //available
 		}
 		return false; //not available
 	}
 
 	/**
 	 *
 	 * @param source of one endpoint of an edge
 	 * @param target of the other endpoint of an edge
 	 * @return true if edge exit
 	 */
 	private boolean isEdgeAvailable(String source, String target) {
 		NodeList nodes = outputDoc.getElementsByTagName("edge");
 		for (int i=0; i<nodes.getLength(); i++) {
 			Element elEntity = (Element)nodes.item(i);
 			String theSource = elEntity.getAttribute("source");
 			String theTarget = elEntity.getAttribute("target");
 
 			if((theSource.equals("source") && theTarget.equals("target")) ||
 			   (theSource.equals("target") && theTarget.equals("source")))
 			   return true;
 		}
 		return false;
 	}
 }
