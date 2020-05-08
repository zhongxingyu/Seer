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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.apache.jena.riot.system.StreamRDF;
 import org.swows.node.Skolemizer;
 import org.swows.vocabulary.XML;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.NodeFactory;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * The Class DomEncoder.
  */
 public class DomEncoder {
 
 /*
 	private static interface NodeReference {
 		public abstract org.w3c.dom.Node getNode();
 	}
 */
 	
 
 	static class DocumentEncoder implements ContentHandler {
 		
 		private static String VOID_NAMESPACE = "http://www.swows.org/xml/no-namespace";
 		
 		private StreamRDF outputStream;
 		private Stack<Node> nodeStack = new Stack<Node>();
 //		private Stack<Node> lastSiblingStack = new Stack<Node>();
 		private Stack<Integer> childrenCountStack = new Stack<Integer>();
 		private String docURI;
 		private Node docNode = null;
 		
 		private Map<String,Map<String,Node>> typeMap = new HashMap<String,Map<String,Node>>();
 		private Map<String,Node> nsMap = new HashMap<String,Node>();
 		private Map<String,Node> lnMap = new HashMap<String,Node>();
 
 		private Map<String,Set<String>> defNsAttrs = new HashMap<String, Set<String>>();
 		private Set<String> defAttrs = new HashSet<String>();
 		private StringBuffer textBuffer = new StringBuffer();
 		
 		public DocumentEncoder(StreamRDF outputStream, String docURI) {
 			this.outputStream = outputStream;
 			this.docURI = docURI;
 		}
 
 		private Node getElementTypeNode(String namespace, String localName) {
 			Node typeNode = null;
 			Map<String,Node> nsMap = typeMap.get(namespace);
 			if ( nsMap == null ) {
 				nsMap = new HashMap<String,Node>();
 				typeMap.put(namespace, nsMap);
 			} else 
 				typeNode = nsMap.get(localName);
 			if (typeNode == null) {
 				typeNode = NodeFactory.createURI(nsAndLn( namespace, localName ));
 				outputStream.triple(new Triple(typeNode, RDF.type.asNode(), RDFS.Class.asNode()));
 				outputStream.triple(new Triple(typeNode, RDFS.subClassOf.asNode(), XML.Element.asNode()));
 				outputStream.triple(new Triple(typeNode, XML.namespace.asNode(), getNsNode(namespace)));
 				outputStream.triple(new Triple(typeNode, XML.nodeName.asNode(), getLnNode(localName)));
 				nsMap.put(localName, typeNode);
 			}
 			return typeNode;
 		}
 		
 		private Node getAttrTypeNode(String namespace, String localName) {
 			Node typeNode = null;
 			Map<String,Node> nsMap = typeMap.get(namespace);
 			if ( nsMap == null ) {
 				nsMap = new HashMap<String,Node>();
 				typeMap.put(namespace, nsMap);
 			} else 
 				typeNode = nsMap.get(localName);
 			if (typeNode == null) {
 				typeNode = NodeFactory.createURI(nsAndLn( namespace, localName ));
 //				outputStream.triple(new Triple(typeNode, RDF.type.asNode(), RDFS.Class.asNode()));
 				outputStream.triple(new Triple(typeNode, RDFS.subClassOf.asNode(), XML.Attr.asNode()));
 				outputStream.triple(new Triple(typeNode, XML.namespace.asNode(), getNsNode(namespace)));
 				outputStream.triple(new Triple(typeNode, XML.nodeName.asNode(), getLnNode(localName)));
 				nsMap.put(localName, typeNode);
 			}
 			return typeNode;
 		}
 
 		private Node getNsNode(String namespace) {
 			Node nsNode = nsMap.get(namespace);
 			if (nsNode == null) {
 				nsNode = NodeFactory.createURI(namespace);
 				nsMap.put(namespace, nsNode);
 			}
 			return nsNode;
 		}
 		
 		private Node getLnNode(String localName) {
 			Node lnNode = lnMap.get(localName);
 			if (lnNode == null) {
 				lnNode = NodeFactory.createLiteral(localName);
 				lnMap.put(localName, lnNode);
 			}
 			return lnNode;
 		}
 		
 //		private boolean isAttrDefined(String namespace, String localName) {
 //			if (namespace == null || namespace.isEmpty())
 //				return 
 //		}
 
 		private Node defineAttr(String namespace, String localName) {
 			if (namespace == null || namespace.isEmpty()) {
 				Node attrNode = getAttrTypeNode(VOID_NAMESPACE, localName);
 //				outputStream.triple(
 //						new Triple(
 //								attrNode,
 //								RDF.type.asNode(),
 //								XML.AttrType.asNode() ));
 				outputStream.triple(new Triple(attrNode, XML.nodeName.asNode(), getLnNode(localName)));
 				defAttrs.add(localName);
 				return attrNode;
 			} else {
 				Node attrNode = getAttrTypeNode(namespace, localName);
 				Set<String> nsSet = defNsAttrs.get(namespace);
 				if (nsSet == null) {
 					nsSet = new HashSet<String>();
 					defNsAttrs.put(namespace, nsSet);
 				}
 //				outputStream.triple(
 //						new Triple(
 //								attrNode,
 //								RDF.type.asNode(),
 //								XML.AttrType.asNode() ));
 				outputStream.triple(new Triple(attrNode, XML.namespace.asNode(), getNsNode(namespace)));
 				outputStream.triple(new Triple(attrNode, XML.nodeName.asNode(), getLnNode(localName)));
 				nsSet.add(localName);
 				return attrNode;
 			}
 		}
 		
 		private void textBufferFlush() {
 			String textBufferStr = textBuffer.toString().trim();
 			if (!textBufferStr.isEmpty()) {
 				Node newNode = Skolemizer.getInstance().getNode();
 				outputStream.triple(new Triple(newNode, RDF.type.asNode(), XML.Text.asNode()));
 				outputStream.triple(
 						new Triple(
 								newNode,
 //								XML.text.asNode(),
 								XML.nodeValue.asNode(),
 								NodeFactory.createLiteral(textBufferStr) ));
 				connectNode(newNode);
 				textBuffer = new StringBuffer();
 			}
 		}
 		
 		private void push(Node node) {
 			textBufferFlush();
 			nodeStack.push(node);
 //			lastSiblingStack.push(null);
 			childrenCountStack.push(0);
 		}
 		
 		private void pop() {
 			textBufferFlush();
 //			Node node = nodeStack.pop();
 //			Node lastSibling = lastSiblingStack.pop();
 //			if (lastSibling != null)
 //				outputStream.triple(new Triple(node, XML.lastChild.asNode(), lastSibling));
 			nodeStack.pop();
 			childrenCountStack.pop();
 		}
 
 		public void setDocumentLocator(Locator locator) {
 		}
 
 		private boolean nextIsRootElement = true;
 		
 		public void startDocument() throws SAXException {
 			docNode = NodeFactory.createURI(docURI);
 			outputStream.triple(new Triple(docNode, RDF.type.asNode(), XML.Document.asNode()));
 		}
 
 		public void startPrefixMapping(String prefix, String uri)
 				throws SAXException { }
 
 		public void endPrefixMapping(String prefix) throws SAXException { }
 
 		private void connectNode(Node newNode) {
 			outputStream.triple(new Triple(newNode, XML.ownerDocument.asNode(), docNode));
 			if (!nodeStack.isEmpty()) {
 				Node parentNode = nodeStack.peek();
 				outputStream.triple(new Triple(newNode, XML.parentNode.asNode(), parentNode));
 				outputStream.triple(new Triple(parentNode, XML.hasChild.asNode(), newNode));
 //				Node lastSibling = lastSiblingStack.pop();
 //				if (lastSibling == null) {
 //					outputStream.triple(new Triple(parentNode, XML.firstChild.asNode(), newNode));
 //				} else {
 //					outputStream.triple(new Triple(lastSibling, XML.nextSibling.asNode(), newNode));
 //					outputStream.triple(new Triple(newNode, XML.previousSibling.asNode(), lastSibling));
 //				}
 //				lastSiblingStack.push(newNode);
 				int childrenCount = childrenCountStack.pop();
 				outputStream.triple(new Triple(
 						newNode,
 						XML.orderKey.asNode(),
 						NodeFactory.createLiteral(Integer.toString(childrenCount), XSDDatatype.XSDinteger) ));
 				childrenCountStack.push(childrenCount + 1);
 			}
 		}
 		
 		private String nsAndLn(String ns, String ln) {
 			return
 					( (ns == null) || (ns.length() == 0) ?
 							VOID_NAMESPACE + "#" :
 							ns + (ns.charAt(ns.length() - 1) == '#' ? "" : "#") )
 					+ ln;
 		}
 		
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
 								? NodeFactory.createURI( nsAndLn(docURI, idAttr) )
 				 				: Skolemizer.getInstance().getNode();
 								
 //			outputStream.triple(new Triple(newNode, RDF.type.asNode(), XML.Element.asNode()));
 			
 			Node typeNode = getElementTypeNode(uri, localName);
 			outputStream.triple(new Triple(newNode, RDF.type.asNode(), typeNode));
 			
 			if (nextIsRootElement) {
 				outputStream.triple(new Triple(docNode, XML.hasChild.asNode(), newNode));
 				nextIsRootElement = false;
 			}
 			
 //			outputStream.triple(new Triple(typeNode, OWL.oneOf.asNode(), XML.Element.asNode()));
 			
 //			  <rdfs:subClassOf>
 //			    <owl:Restriction>
 //			      <owl:onProperty rdf:resource="#hasMaker" />
 //			      <owl:allValuesFrom rdf:resource="#Winery" />
 //			    </owl:Restriction>
 //			  </rdfs:subClassOf>
 			
 //			outputStream.triple(new Triple(newNode, XML.namespace.asNode(), getNsNode(uri)));
 //			outputStream.triple(new Triple(newNode, XML.nodeName.asNode(), getLnNode(localName)));
 
 			connectNode(newNode);
 			
 			for (int i = 0; i < atts.getLength(); i++) {
 				Node nodeAttr = defineAttr(atts.getURI(i), atts.getLocalName(i));
 				outputStream.triple(
 						new Triple(
 								newNode,
 								nodeAttr,
 								NodeFactory.createLiteral( atts.getValue(i) ) ));
 			}
 								
 			push(newNode);
 		}
 
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			pop();
 		}
 
 		public void characters(char[] ch, int start, int length)
 				throws SAXException {
 			textBuffer.append(ch, start, length);
 		}
 
 		public void endDocument() throws SAXException {
 		}
 
 		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
 				throws SAXException { }
 
 		public void processingInstruction(String target, String data)
 				throws SAXException { }
 
 		public void skippedEntity(String name) throws SAXException { }
 
 	};
 
 	/**
 	 * Encode.
 	 *
 	 * @param document the document
 	 * @return the graph
 	 */
 	public static ContentHandler encode(String rootUri, StreamRDF outputStream) {
 		return new DocumentEncoder(outputStream, rootUri);
 	}
 
 	/**
 	 * Encode.
 	 *
 	 * @param document the document
 	 * @return the graph
 	 */
 	public static void encode(InputSource inputSAX, String rootUri, StreamRDF outputStream) {
 //		return new DocumentEncoder(document, rootUri);
 		try {
 			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
 			xmlReader.setContentHandler( encode(rootUri, outputStream) );
 			//xmlReader.setFeature("XML 2.0", true);
 			xmlReader.parse(inputSAX);
 		} catch(SAXException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
