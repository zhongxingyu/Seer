 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.xmlinrdf;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.swows.node.Skolemizer;
 import org.swows.vocabulary.DOC;
 import org.swows.vocabulary.XML;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.hp.hpl.jena.graph.BulkUpdateHandler;
 import com.hp.hpl.jena.graph.Capabilities;
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.GraphEventManager;
 import com.hp.hpl.jena.graph.GraphStatisticsHandler;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Reifier;
 import com.hp.hpl.jena.graph.TransactionHandler;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.graph.TripleMatch;
 import com.hp.hpl.jena.graph.impl.SimpleEventManager;
 import com.hp.hpl.jena.graph.impl.SimpleTransactionHandler;
 import com.hp.hpl.jena.graph.query.QueryHandler;
 import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.shared.AddDeniedException;
 import com.hp.hpl.jena.shared.DeleteDeniedException;
 import com.hp.hpl.jena.shared.PrefixMapping;
 import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
 import com.hp.hpl.jena.sparql.graph.GraphFactory;
 import com.hp.hpl.jena.sparql.graph.Reifier2;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.Filter;
 import com.hp.hpl.jena.util.iterator.Map1;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 import com.hp.hpl.jena.util.iterator.NullIterator;
 import com.hp.hpl.jena.util.iterator.SingletonIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DomEncoder.
  */
 public class DomEncoder2 {
 
 /*
 	private static interface NodeReference {
 		public abstract org.w3c.dom.Node getNode();
 	}
 */
 	
 
 	static class DocumentEncoder implements ContentHandler {
 		
 		private static final NullIterator<Triple> emptyTripleIterator = new NullIterator<Triple>();
 		private static final NullIterator<org.w3c.dom.Node> emptyNodeIterator = new NullIterator<org.w3c.dom.Node>();
 		private static Random rnd = new Random();
		private static String VOID_NAMESPACE = "http://www.swows.org/xml/no-namespace";
 		
 		private Graph outputGraph;
 		private Stack<Node> nodeStack = new Stack<Node>();
 		private Stack<Node> lastSiblingStack = new Stack<Node>();
 		private String docURI;
 		private Node docNode = null;
 		private Map<String,Map<String,Node>> typeMap = new HashMap<String,Map<String,Node>>();
 		private Map<String,Set<String>> defNsAttrs = new HashMap<String, Set<String>>();
 		private Set<String> defAttrs = new HashSet<String>();
 		private StringBuffer textBuffer = new StringBuffer();
 		
 		public DocumentEncoder(Graph outputGraph, String docURI) {
 			this.outputGraph = outputGraph;
 			this.docURI = docURI;
 		}
 
 		private Node getTypeNode(String namespace, String localName) {
 			Node typeNode = null;
 			Map<String,Node> nsMap = typeMap.get(namespace);
 			if ( nsMap == null ) {
 				nsMap = new HashMap<String,Node>();
 				typeMap.put(namespace, nsMap);
 			} else 
 				typeNode = nsMap.get(localName);
 			if (typeNode == null) {
 				typeNode = Node.createURI(namespace + "#" + localName);
 				nsMap.put(localName, typeNode);
 			}
 			return typeNode;
 		}
 		
 //		private boolean isAttrDefined(String namespace, String localName) {
 //			if (namespace == null || namespace.isEmpty())
 //				return 
 //		}
 
 		private Node defineAttr(String namespace, String localName) {
 			if (namespace == null || namespace.isEmpty()) {
				Node attrNode = getTypeNode(VOID_NAMESPACE, localName);
 				outputGraph.add(
 						new Triple(
 								attrNode,
 								RDF.type.asNode(),
 								XML.AttrType.asNode() ));
 				defAttrs.add(localName);
 				return attrNode;
 			} else {
 				Node attrNode = getTypeNode(namespace, localName);
 				Set<String> nsSet = defNsAttrs.get(namespace);
 				if (nsSet == null) {
 					nsSet = new HashSet<String>();
 					defNsAttrs.put(namespace, nsSet);
 				}
 				outputGraph.add(
 						new Triple(
 								attrNode,
 								RDF.type.asNode(),
 								XML.AttrType.asNode() ));
 				nsSet.add(localName);
 				return attrNode;
 			}
 		}
 		
 		private void textBufferFlush() {
 			String textBufferStr = textBuffer.toString().trim();
 			if (!textBufferStr.isEmpty()) {
 				Node newNode = Skolemizer.getInstance().getNode();
 				outputGraph.add(new Triple(newNode, RDF.type.asNode(), XML.Text.asNode()));
 				outputGraph.add(
 						new Triple(
 								newNode,
 								XML.text.asNode(),
 								Node.createLiteral(textBufferStr) ));
 				connectNode(newNode);
 				textBuffer = new StringBuffer();
 			}
 		}
 		
 		private void push(Node node) {
 			textBufferFlush();
 			nodeStack.push(node);
 			lastSiblingStack.push(null);
 		}
 		
 		private void pop() {
 			textBufferFlush();
 			Node node = nodeStack.pop();
 			Node lastSibling = lastSiblingStack.pop();
 			if (lastSibling != null)
 				outputGraph.add(new Triple(node, XML.lastChild.asNode(), lastSibling));
 		}
 
 		@Override
 		public void setDocumentLocator(Locator locator) {
 		}
 
 		@Override
 		public void startDocument() throws SAXException {
 			docNode = Node.createURI(docURI);
			outputGraph.add(new Triple(docNode, RDF.type.asNode(), XML.Document.asNode()));
 		}
 
 		@Override
 		public void startPrefixMapping(String prefix, String uri)
 				throws SAXException { }
 
 		@Override
 		public void endPrefixMapping(String prefix) throws SAXException { }
 
 		private void connectNode(Node newNode) {
 			outputGraph.add(new Triple(newNode, XML.ownerDocument.asNode(), docNode));
 			if (!nodeStack.isEmpty()) {
 				Node parentNode = nodeStack.peek();
 				outputGraph.add(new Triple(newNode, XML.parentNode.asNode(), parentNode));
 				outputGraph.add(new Triple(parentNode, XML.hasChild.asNode(), newNode));
 				Node lastSibling = lastSiblingStack.pop();
 				if (lastSibling == null) {
 					outputGraph.add(new Triple(parentNode, XML.firstChild.asNode(), newNode));
 				} else {
 					outputGraph.add(new Triple(lastSibling, XML.nextSibling.asNode(), newNode));
 					outputGraph.add(new Triple(newNode, XML.previousSibling.asNode(), lastSibling));
 				}
 				lastSiblingStack.push(newNode);
 			}
 		}
 		
 		@Override
 		public void startElement(
 				String uri, String localName,
 				String qName,
 				Attributes atts) throws SAXException {
 			
 			String idAttr = atts.getValue("id");
 			if (idAttr == null)
 				idAttr = atts.getValue("http://www.w3.org/XML/1998/namespace", "id");
 			if (idAttr == null)
 				idAttr = atts.getValue("http://www.w3.org/2000/svg", "id");
 			
 			Node newNode = (idAttr != null)
 								? Node.createURI(docURI + "#" + idAttr)
 				 				: Skolemizer.getInstance().getNode();
 								
 			outputGraph.add(new Triple(newNode, RDF.type.asNode(), XML.Element.asNode()));
 			outputGraph.add(new Triple(newNode, RDF.type.asNode(), getTypeNode(uri, localName)));
 			
 			connectNode(newNode);
 			
 			for (int i = 0; i < atts.getLength(); i++) {
 				outputGraph.add(
 						new Triple(
 								newNode,
 								defineAttr(atts.getURI(i), atts.getLocalName(i)),
 								Node.createLiteral( atts.getValue(i) ) ));
 			}
 								
 			push(newNode);
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			pop();
 		}
 
 		@Override
 		public void characters(char[] ch, int start, int length)
 				throws SAXException {
 			textBuffer.append(ch, start, length);
 		}
 
 		@Override
 		public void endDocument() throws SAXException {
 		}
 
 		@Override
 		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
 				throws SAXException { }
 
 		@Override
 		public void processingInstruction(String target, String data)
 				throws SAXException { }
 
 		@Override
 		public void skippedEntity(String name) throws SAXException { }
 
 	};
 
 	/**
 	 * Encode.
 	 *
 	 * @param document the document
 	 * @return the graph
 	 */
 	public static void encode(InputSource inputSAX, String rootUri, Graph graph) {
 //		return new DocumentEncoder(document, rootUri);
 		try {
 			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
 			xmlReader.setContentHandler( new DocumentEncoder(graph, rootUri) );
 			//xmlReader.setFeature("XML 2.0", true);
 			xmlReader.parse(inputSAX);
 		} catch(SAXException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Encode.
 	 *
 	 * @param document the document
 	 * @return the graph
 	 */
 	public static Graph encode(InputSource inputSAX, String rootUri) {
 		Graph outputGraph = GraphFactory.createGraphMem();
 		encode(inputSAX, rootUri, outputGraph);
 		return outputGraph;
 	}
 
 	public static void developInRDF(Graph graph) {
 		Model model = ModelFactory.createModelForGraph(graph);
 		Iterator<Resource> xmlResources =
 				model.listResourcesWithProperty(RDF.type, XML.Document)
 				.filterKeep( new Filter<Resource>() {
 					@Override
 					public boolean accept(Resource res) {
 						StmtIterator stmtIterator = res.listProperties();
 						boolean textProp = false;
 						while (stmtIterator.hasNext()) {
 							Property prop = stmtIterator.next().getPredicate();
 							if (prop.equals(XML.text)) {
 								// TODO: check for single literal string value of xml.text
 								textProp = true;
 							}
 							else if (!prop.equals(RDF.type))
 								return false;
 						}
 						return textProp;
 					}
 				});
 		//if (!xmlResources.hasNext())
 		//	System.out.println("Empty Iterator!");
 		
 		List<Resource> xmlResourcesList = new Vector<Resource>();
 		while (xmlResources.hasNext())
 			xmlResourcesList.add(xmlResources.next());
 		xmlResources = xmlResourcesList.iterator();
 		while (xmlResources.hasNext()) {
 			Resource xmlRes = xmlResources.next();
 			String xmlString = xmlRes.getRequiredProperty(XML.text).getString();
 			InputSource xmlInputSource = new InputSource(new StringReader(xmlString));
 			String rootUri = null;
 			Node rootNode = null;
 			while (rootUri == null || graph.contains(rootNode, Node.ANY, Node.ANY) || graph.contains(Node.ANY, Node.ANY, rootNode) ) {
 				rootUri = getRandomString();
 				rootNode = Node.createURI(rootUri);
 			}
 				//DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 				//docBuilderFactory.setCoalescing(true);
 				//docBuilderFactory.setFeature("XML 2.0", true);
 				//docBuilderFactory.setNamespaceAware(true);
 				//newDoc = docBuilderFactory.newDocumentBuilder().parse(xmlInputSource);
 				Graph newGraph = encode(xmlInputSource, rootUri);
 				graph.getBulkUpdateHandler().add(newGraph);
 				
 				Iterator<Triple> tripleIterator =
 						graph.find(rootNode, null, null).andThen(graph.find(null, null, rootNode));
 				List<Triple> tripleList = new Vector<Triple>();
 				while (tripleIterator.hasNext()) {
 					Triple triple = tripleIterator.next();
 					tripleList.add(triple);
 				}
 				tripleIterator = tripleList.iterator();
 				while (tripleIterator.hasNext()) {
 					Triple triple = tripleIterator.next();
 					graph.add(
 							new Triple(
 									triple.getSubject().equals(rootNode) ? xmlRes.asNode() : triple.getSubject(),
 									triple.getPredicate(),
 									triple.getObject().equals(rootNode) ? xmlRes.asNode() : triple.getObject()
 								));
 					graph.delete(triple);
 				}
 //SparqlJenaQuery query = new SparqlJenaQuery(queryString);
 				//query.addRootedGraph(graph, xmlRes.asNode());
 		}
 
 	}
 
 	//public static final String URI = "http://www.swows.org/datatypes/sparql/jena";
 	//public static final Resource namespace = ResourceFactory.createResource("http://www.swows.org/datatypes/sparql/jena");
 	public static final int RANDOM_HEX_SIZE = 8;
 	private static final Random random = new Random();
 	
 	private static char nibble2hexDigit(byte input) {
 		input |= 15;
 		switch(input) {
 		case 10:
 			return 'a';
 		case 11:
 			return 'b';
 		case 12:
 			return 'c';
 		case 13:
 			return 'd';
 		case 14:
 			return 'e';
 		case 15:
 			return 'f';
 		default:
 			return Byte.toString(input).charAt(0);
 		}
 	}
 	
 	private static String getRandomString() {
 		byte randomBytes[] = new byte[RANDOM_HEX_SIZE * 2];
 		random.nextBytes(randomBytes);
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < RANDOM_HEX_SIZE * 2; i++) {
 			sb.append( nibble2hexDigit( (byte) (randomBytes[i] >>> 8) ) );
 			sb.append( nibble2hexDigit( randomBytes[i] ) );
 		}
 		return sb.toString();
 	}
 
 }
