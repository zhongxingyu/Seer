 package org.swows.xmlinrdf;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.events.GraphUpdate;
 import org.swows.graph.events.Listener;
 import org.swows.runnable.RunnableContext;
 import org.swows.util.GraphUtils;
 import org.swows.vocabulary.xml;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Comment;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ProcessingInstruction;
 import org.w3c.dom.Text;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.Map1;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class DomDecoder implements Listener, RunnableContext, EventListener {
 	
 	private DocumentReceiver docReceiver;
 	private DOMImplementation domImplementation;
 	private DynamicGraph graph;
 //	private Set<DomEventListener> domEventListeners;
 	private Map<String, Set<DomEventListener>> domEventListeners;
 	
 	private Document document;
 	private RunnableContext updatesContext;
 	private Map<Node, Set<org.w3c.dom.Node>> graph2domNodeMapping = new HashMap<Node, Set<org.w3c.dom.Node>>();
 	private Map<org.w3c.dom.Node, Node> dom2graphNodeMapping = new HashMap<org.w3c.dom.Node, Node>();
 	
 	private Map<String, Set<Element>> eventType2elements = new HashMap<String, Set<Element>>();
 	private Map<Element, Set<String>> element2eventTypes = new HashMap<Element, Set<String>>();
 	
 	private Logger logger = Logger.getRootLogger();
 	
 	public void addDomEventListener(String eventType, DomEventListener l) {
 		synchronized(this) {
 			if (domEventListeners == null)
 				domEventListeners = new HashMap<String, Set<DomEventListener>>();
 		}
 		synchronized(domEventListeners) {
 			Set<DomEventListener> domEventListenersForType = domEventListeners.get(eventType);
 			if (domEventListenersForType == null) {
 				domEventListenersForType = new HashSet<DomEventListener>();
 				domEventListeners.put(eventType, domEventListenersForType);
 			}
 			domEventListenersForType.add(l);
 		}
 	}
 	
 	public void removeDomEventListener(String eventType, DomEventListener l) {
 		if (domEventListeners != null) {
 			synchronized(domEventListeners) {
 				domEventListeners.remove(l);
 			}
 		}
 	}
 
 	@Override
 	public synchronized void handleEvent(Event evt) {
 		logger.debug("In DOM decoder handling event " + evt + " of type " + evt.getType());
 		org.w3c.dom.Node eventTargetDomNode = (org.w3c.dom.Node) evt.getCurrentTarget();
 		Node eventTargetGraphNode = dom2graphNodeMapping.get(eventTargetDomNode);
 		if (domEventListeners != null) {
 			synchronized (domEventListeners) {
 				Set<DomEventListener> domEventListenersForType = domEventListeners.get(evt.getType());
 				for (DomEventListener l : domEventListenersForType) {
 					logger.debug("Sending to " + l + " the event " + evt);
 					l.handleEvent(evt, eventTargetGraphNode);
 				}
 			}
 		}
 	}
 
 	public static Document decode(DynamicGraph graph, Node docRootNode) {
 		try {
 			return decode(graph, docRootNode, DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0"));
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static Document decodeOne(DynamicGraph graph) {
 		return decodeAll(graph).next();
 	}
 
 	public static ExtendedIterator<Document> decodeAll(final DynamicGraph graph) {
 		return graph
 				.find(Node.ANY, RDF.type.asNode(), xml.Document.asNode())
 				.mapWith(new Map1<Triple, Document>() {
 					@Override
 					public Document map1(Triple triple) {
 						return decode(graph, triple.getSubject());
 					}
 				});
 	}
 
 	private static String qName(final Graph graph, final Node elementNode) {
 		return
 				GraphUtils.getSingleValueProperty(graph, elementNode, xml.nodeName.asNode())
 				.getLiteralLexicalForm();
 	}
 	
 	private static String namespace(final Graph graph, final Node elementNode) {
 		try {
 			return
 				graph.find(elementNode, xml.namespace.asNode(), Node.ANY)
 				.next().getObject().getURI();
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	private static String value(final Graph graph, final Node elementNode) {
 		try {
 			return
 				graph.find(elementNode, xml.nodeValue.asNode(), Node.ANY)
 				.next().getObject().getLiteralLexicalForm();
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	private Text decodeText(Graph graph, Node elementNode) {
 		return document.createTextNode(value(graph, elementNode));
 	}
 	
 	private Comment decodeComment(Graph graph, Node elementNode) {
 		return document.createComment(value(graph, elementNode));
 	}
 	
 	private ProcessingInstruction decodeProcessingInstruction(Graph graph, Node elementNode) {
 		return document.createProcessingInstruction(
 				qName(graph, elementNode),
 				value(graph, elementNode) );
 	}
 	
 	private Attr decodeAttr(Graph graph, Node elementNode) {
 		Attr attr =
 				document.createAttributeNS(
 						namespace(graph, elementNode),
 						qName(graph, elementNode) );
 		attr.setValue(value(graph, elementNode));
 		return attr;
 	}
 
 	private void decodeElementAttrsAndChildren(final Element element, final Graph graph, final Node elementNode) {
 		ExtendedIterator<Attr> attrs =
 				graph.find(elementNode, xml.hasAttribute.asNode(), Node.ANY)
 				.mapWith(new Map1<Triple, Attr>() {
 					@Override
 					public Attr map1(Triple triple) {
 						Attr newAttr = decodeAttr(graph, triple.getObject());
 						addNodeMapping(triple.getObject(), newAttr);
 						return newAttr;
 					}
 				});
 		while (attrs.hasNext())
 			element.setAttributeNodeNS(attrs.next());
 		Set<Node> orderedChildren = new HashSet<Node>();
 		{
 			Node child = GraphUtils.getSingleValueOptProperty(graph, elementNode, xml.firstChild.asNode());
 			while (child != null) {
 				orderedChildren.add(child);
 				org.w3c.dom.Node newChild = decodeNode(graph, child);
 				if (newChild != null) {
 					addNodeMapping(child, newChild);
 					element.appendChild(newChild);
 				}
 				child = GraphUtils.getSingleValueOptProperty(graph, child, xml.nextSibling.asNode());
 			}
 		}
 		ExtendedIterator<Node> children = GraphUtils.getPropertyValues(graph, elementNode, xml.hasChild.asNode());
 		while (children.hasNext()) {
 			Node child = children.next();
 			if (!orderedChildren.contains(child)) {
 				org.w3c.dom.Node domChild = decodeNode(graph, child);
 				if (domChild != null) {
 					addNodeMapping(child, domChild);
 					element.appendChild(domChild);
 				}
 			}
 		}
 //		System.out.println("Looking for eventListeners in element " + element + " (" + elementNode + ")");
 		Iterator<Node> eventTypeNodes = GraphUtils.getPropertyValues(graph, elementNode, xml.listenedEventType.asNode());
 		while (eventTypeNodes.hasNext()) {
 			Node eventTypeNode = eventTypeNodes.next();
 			if (eventTypeNode.isLiteral()) {
 				String eventType = eventTypeNode.getLiteralLexicalForm();
 //				System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 				((EventTarget) element).addEventListener(eventType, this, false);
 				
 				Set<Element> elemsForEventType = eventType2elements.get(eventType);
 				if (elemsForEventType == null) {
 					elemsForEventType = new HashSet<Element>();
 					eventType2elements.put(eventType, elemsForEventType);
 					((EventTarget) document).addEventListener(eventType, this, false);
 				}
 				elemsForEventType.add(element);
 				
 				Set<String> eventTypesForElement = element2eventTypes.get(element);
 				if (eventTypesForElement == null) {
 					eventTypesForElement = new HashSet<String>();
 					element2eventTypes.put(element, eventTypesForElement);
 				}
 				eventTypesForElement.add(eventType);
 				
 			}
 		}
 	}
 
 	private Element decodeElement(final Graph graph, final Node elementNode) {
 		Element element =
 				document.createElementNS(
 						namespace(graph, elementNode),
 						qName(graph, elementNode) );
 		addNodeMapping(elementNode, element);
 		decodeElementAttrsAndChildren(element, graph, elementNode);
 		return element;
 	}
 
 	private org.w3c.dom.Node decodeNode(Graph graph, Node elementNode) {
 		try {
 			Node nodeType = graph.find(elementNode, xml.nodeType.asNode(), Node.ANY).next().getObject();
 			if (nodeType.equals(xml.Attr.asNode()))
 				return decodeAttr(graph, elementNode);
 			if (nodeType.equals(xml.Comment.asNode()))
 				return decodeComment(graph, elementNode);
 			if (nodeType.equals(xml.Element.asNode()))
 				return decodeElement(graph, elementNode);
 			if (nodeType.equals(xml.ProcessingInstruction.asNode()))
 				return decodeProcessingInstruction(graph, elementNode);
 			if (nodeType.equals(xml.Text.asNode()))
 				return decodeText(graph, elementNode);
 			throw new RuntimeException("Type not recognised for node " + elementNode);
 		} catch(NoSuchElementException e) {
 			throw new RuntimeException("Type not found for node " + elementNode);
 		}
 	}
 
 	private void decodeDocument(Node docRootNode) {
 		Iterator<Node> possibleDocs =
 				GraphUtils.getPropertyValues(graph, docRootNode, xml.hasChild.asNode());
 		while (possibleDocs.hasNext()) {
 			try {
 				Node elementNode = possibleDocs.next();
 				document =
 					domImplementation.createDocument(
 					namespace(graph, elementNode),
 					qName(graph, elementNode),
 					null);
 				Element docElement = document.getDocumentElement();
 				addNodeMapping(docRootNode, document);
 				addNodeMapping(elementNode, docElement);
 				decodeElementAttrsAndChildren( docElement, graph, elementNode );
 			} catch(RuntimeException e) { }
 		}
 	}
 	
 	private void redecodeDocument(Node docRootNode) {
 		graph2domNodeMapping = new HashMap<Node, Set<org.w3c.dom.Node>>();
 		dom2graphNodeMapping = new HashMap<org.w3c.dom.Node, Node>();
 		decodeDocument(docRootNode);
 		if (docReceiver != null)
 			docReceiver.sendDocument(document);
 	}
 	
 
 	private void decodeWorker(DynamicGraph graph, Node docRootNode) {
 		decodeDocument(docRootNode);
 		graph.getEventManager2().register(this);
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl) {
 		return (new DomDecoder(graph, docRootNode, domImpl)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver) {
 		return (new DomDecoder(graph, docRootNode, domImpl, docReceiver)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl);
 		for (String eventType : domEventListeners.keySet())
 			for (DomEventListener listener : domEventListeners.get(eventType))
 				domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext);
 		for (String eventType : domEventListeners.keySet())
 			for (DomEventListener listener : domEventListeners.get(eventType))
 				domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, docReceiver);
 		for (String eventType : domEventListeners.keySet())
 			for (DomEventListener listener : domEventListeners.get(eventType))
 				domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver);
 		for (String eventType : domEventListeners.keySet())
 			for (DomEventListener listener : domEventListeners.get(eventType))
 				domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decodeOne(DynamicGraph graph, DOMImplementation domImpl) {
 		return decodeAll(graph, domImpl).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext) {
 		return decodeAll(graph, domImpl, updatesContext).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, docReceiver).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, docReceiver, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, domEventListeners).next();
 	}
 
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl) {
 		return decodeAll(graph, domImpl, (RunnableContext) null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, null, docReceiver);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext) {
 		return decodeAll(graph, domImpl, updatesContext, (DocumentReceiver) null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, (RunnableContext) null, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, null, docReceiver, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, null, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver,
 			final Map<String,Set<DomEventListener>> domEventListeners) {
 		return graph
 				.find(Node.ANY, RDF.type.asNode(), xml.Document.asNode())
 				.mapWith(new Map1<Triple, Document>() {
 					@Override
 					public Document map1(Triple triple) {
 						return decode(graph, triple.getSubject(), domImpl, updatesContext, docReceiver, domEventListeners);
 					}
 				});
 	}
 	
 	private void addNodeMapping(Node graphNode, org.w3c.dom.Node domNode) {
 //		System.out.println(this + ": adding mapping ( " + graphNode + " -> " + domNode + " )");
 		Set<org.w3c.dom.Node> domeNodeSet = graph2domNodeMapping.get(graphNode);
 		if (domeNodeSet == null) {
 			domeNodeSet = new HashSet<org.w3c.dom.Node>();
 			graph2domNodeMapping.put(graphNode, domeNodeSet);
 		}
 		domeNodeSet.add(domNode);
 		dom2graphNodeMapping.put(domNode, graphNode);
 	}
 	
 	private void removeNodeMapping(org.w3c.dom.Node domNode) {
 		Node graphNode = dom2graphNodeMapping.get(domNode);
 		if (graphNode != null) {
 			dom2graphNodeMapping.remove(domNode);
 			Set<org.w3c.dom.Node> domeNodeSet = graph2domNodeMapping.get(graphNode);
 			domeNodeSet.remove(domNode);
 			if (domeNodeSet.isEmpty())
 				graph2domNodeMapping.remove(graphNode);
 		}
 	}
 	
 	private void removeSubtreeMapping(org.w3c.dom.Node domNode) {
 //		System.out.println(this + ": removing subtree mapping of " + domNode );
 		removeNodeMapping(domNode);
 		NamedNodeMap attrMap = domNode.getAttributes();
 		if (attrMap != null) {
 			for (int i = 0; i < attrMap.getLength(); i++) {
 				removeSubtreeMapping(attrMap.item(i));
 			}
 		}
 		NodeList children = domNode.getChildNodes();
 		if (children != null) {
 			for (int i = 0; i < children.getLength(); i++) {
 				removeSubtreeMapping(children.item(i));
 			}
 		}
 		if (domNode instanceof Element) {
 			Element element = (Element) domNode;
 			Set<String> eventTypesForElement = element2eventTypes.get(element);
 			if (eventTypesForElement != null) {
 				for (String eventType : eventTypesForElement) {
 					Set<Element> elementsForEventType = eventType2elements.get(eventType);
 					elementsForEventType.remove(element);
 					if (elementsForEventType.isEmpty()) {
 						eventType2elements.remove(eventType);
 						((EventTarget) document).removeEventListener(eventType, DomDecoder.this, false);
 					}
 				}
 				element2eventTypes.remove(element);
 			}			
 		}
 	}
 	
 	private DomDecoder(Graph graph) {
 		//this.graph = graph;
 		this.updatesContext = this;
 	}
 
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl) {
 		this(graph, docRootNode, domImpl, (DocumentReceiver) null);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			DocumentReceiver docReceiver) {
 		this(graph, docRootNode, domImpl, null, docReceiver);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext) {
 		this(graph, docRootNode, domImpl, updatesContext, null);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			DocumentReceiver docReceiver) {
 		this.graph = graph;
 		this.domImplementation = domImpl;
 		this.updatesContext = ( updatesContext == null ? this : updatesContext );
 		this.docReceiver = docReceiver;
 		decodeWorker(graph, docRootNode);
 	}
 	
 	public void run(Runnable runnable) {
 		runnable.run();
 	}
 	
 	private Document getDocument() {
 		return document;
 	}
 
 	@Override
 	public synchronized void notifyUpdate(final Graph sourceGraph, final GraphUpdate update) {
 		
 		if (!update.getAddedGraph().isEmpty() || !update.getDeletedGraph().isEmpty()) {
 			updatesContext.run(
 					new Runnable() {
 						@Override
 						public void run() {
 							ExtendedIterator<Triple> addEventsIter =
 									update.getAddedGraph().find(Node.ANY, Node.ANY, Node.ANY);
 							
 							DomDecoder newDom = new DomDecoder(sourceGraph);
 							newDom.dom2graphNodeMapping = (Map<org.w3c.dom.Node, Node>) ((HashMap<org.w3c.dom.Node, Node>) dom2graphNodeMapping).clone();
 							newDom.graph2domNodeMapping = (Map<Node, Set<org.w3c.dom.Node>>) ((HashMap<Node, Set<org.w3c.dom.Node>>) graph2domNodeMapping).clone();
 							newDom.document = document;
 //							newDom.document = (Document) document.cloneNode(true);
 							
 							while (addEventsIter.hasNext()) {
 								final Triple newTriple = addEventsIter.next();
 //								org.w3c.dom.Node xmlSubj = nodeMapping.get(newTriple.getSubject());
 								//System.out.println("Checking add event " + newTriple);
 								Set<org.w3c.dom.Node> domSubjs = graph2domNodeMapping.get(newTriple.getSubject());
 //								if (domSubjs == null)
 //									System.out.println(this + ": managing add event " + newTriple + ", domSubjs is null");
 								if (domSubjs != null) {
 									//System.out.println("Managing add event " + newTriple + " for domSubjs " + domSubjs);
 									Set<org.w3c.dom.Node> domSubjsTemp = new HashSet<org.w3c.dom.Node>();
 									domSubjsTemp.addAll(domSubjs);
 									Iterator<org.w3c.dom.Node> domSubjIter = domSubjsTemp.iterator();
 									if ( 	( 	newTriple.getPredicate().equals(xml.nodeName.asNode())
 												|| newTriple.getPredicate().equals(xml.namespace.asNode()) )
 											&& !update.getDeletedGraph().contains(newTriple.getSubject(), xml.nodeType.asNode(), Node.ANY) ) {
 										//org.w3c.dom.Node parentNode = null;
 										Node nodeType =
 												sourceGraph
 												.find(newTriple.getSubject(), xml.nodeType.asNode(), Node.ANY)
 												.next().getObject();
 										if (nodeType.equals(xml.Attr.asNode())) {
 											while (domSubjIter.hasNext()) {
 												org.w3c.dom.Node domSubj = domSubjIter.next();
 												Attr oldAttr = (Attr) domSubj;
 												//Attr newAttr = decodeAttr(sourceGraph, newTriple.getSubject());
 												Attr newAttr = newDom.decodeAttr(sourceGraph, newTriple.getSubject());
 												Element ownerElement = oldAttr.getOwnerElement();
 												ownerElement.removeAttributeNode(oldAttr);
 												//removeSubtreeMapping(oldAttr);
 												newDom.removeSubtreeMapping(oldAttr);
 												ownerElement.setAttributeNodeNS(newAttr);
 												//addNodeMapping(newTriple.getSubject(), newAttr);
 												newDom.addNodeMapping(newTriple.getSubject(), newAttr);
 											}
 										} else if (!nodeType.equals(xml.Document.asNode())) {
 											while (domSubjIter.hasNext()) {
 												org.w3c.dom.Node domSubj = domSubjIter.next();
 												org.w3c.dom.Node parentNode = domSubj.getParentNode();
 												if (parentNode != null) {
 													org.w3c.dom.Node newNode = newDom.decodeNode(sourceGraph, newTriple.getSubject());
 													parentNode.replaceChild(newNode, domSubj);
 													newDom.removeSubtreeMapping(domSubj);
 													newDom.addNodeMapping(newTriple.getSubject(), newNode);
 												}
 											}
 										} else  {
 											newTriple.getSubject();
 											redecodeDocument(newTriple.getSubject());
 											return;
 										}
 									} else if ( newTriple.getPredicate().equals(xml.nodeValue.asNode()) ) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 //											System.out.println(this + ": for node " + domSubj + " setting new value: " + newTriple.getObject().getLiteralLexicalForm());
 											domSubj.setNodeValue(
 													newTriple.getObject().getLiteralLexicalForm());
 										}
 									} else if ( newTriple.getPredicate().equals(xml.hasAttribute.asNode()) ) {
 										while (domSubjIter.hasNext()) {
 											Element element = (Element) domSubjIter.next();
 											Attr newAttr = newDom.decodeAttr(sourceGraph, newTriple.getObject());
 											newDom.addNodeMapping(newTriple.getObject(), newAttr);
 											element.setAttributeNodeNS(newAttr);
 										}
 									} else if ( newTriple.getPredicate().equals(xml.hasChild.asNode()) ) {
 										Node nodeType =
 												sourceGraph
 												.find(newTriple.getSubject(), xml.nodeType.asNode(), Node.ANY)
 												.next().getObject();
 										//System.out.println("Managing add hasChild (" + newTriple + ") for domSubjs " + domSubjs + " and node type " + nodeType);
 										if (nodeType.equals(xml.Element.asNode())) {
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 												org.w3c.dom.Node newChild = newDom.decodeNode(sourceGraph, newTriple.getObject());
 												newDom.addNodeMapping(newTriple.getObject(), newChild);
 												element.appendChild(newChild);
 											}
 										} else if (nodeType.equals(xml.Document.asNode())) {
 											newTriple.getSubject();
 											redecodeDocument(newTriple.getSubject());
 											return;
 										}
 									} else if ( newTriple.getPredicate().equals(xml.listenedEventType.asNode()) ) {
 										Node eventTypeNode = newTriple.getObject();
 										if (eventTypeNode.isLiteral()) {
 											String eventType = eventTypeNode.getLiteralLexicalForm();
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 //												System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 												((EventTarget) element).addEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder.this, false);
 
 												Set<Element> elemsForEventType = eventType2elements.get(eventType);
 												if (elemsForEventType == null) {
 													elemsForEventType = new HashSet<Element>();
 													eventType2elements.put(eventType, elemsForEventType);
 													((EventTarget) document).addEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder.this, false);
 												}
 												elemsForEventType.add(element);
 
 												Set<String> eventTypesForElement = element2eventTypes.get(element);
 												if (eventTypesForElement == null) {
 													eventTypesForElement = new HashSet<String>();
 													element2eventTypes.put(element, eventTypesForElement);
 												}
 												eventTypesForElement.add(eventType);
 											}
 										}
 									}
 								}			
 							}
 
 							ExtendedIterator<Triple> deleteEventsIter =
 									update.getDeletedGraph().find(Node.ANY, Node.ANY, Node.ANY);
 							while (deleteEventsIter.hasNext()) {
 								Triple oldTriple = deleteEventsIter.next();
 								//org.w3c.dom.Node xmlSubj = nodeMapping.get(oldTriple.getSubject());
 								Set<org.w3c.dom.Node> domSubjs = graph2domNodeMapping.get(oldTriple.getSubject());
 								//System.out.println("Checking for " + oldTriple.getSubject() + " contained in " + sourceGraph);
 								if (domSubjs != null && sourceGraph.contains(oldTriple.getSubject(), RDF.type.asNode(), Node.ANY)) {
 									//System.out.println("Found " + oldTriple.getSubject() + " contained in " + sourceGraph);
 									//System.out.println("Managing " + oldTriple.getPredicate() + "/" + oldTriple.getObject());
 									Iterator<org.w3c.dom.Node> domSubjIter = domSubjs.iterator();
 									if ( 		(	oldTriple.getPredicate().equals(xml.nodeName.asNode())
 													&& !update.getAddedGraph().contains(oldTriple.getSubject(), xml.nodeName.asNode(), Node.ANY) )
 											||	(	oldTriple.getPredicate().equals(xml.nodeType.asNode())
 													&& !update.getAddedGraph().contains(oldTriple.getSubject(), xml.nodeType.asNode(), Node.ANY) ) ) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 											switch (domSubj.getNodeType()) {
 											case org.w3c.dom.Node.ATTRIBUTE_NODE:
 												Attr oldAttr = (Attr) domSubj;
 												Element ownerElement = oldAttr.getOwnerElement();
 												ownerElement.removeAttributeNode(oldAttr);
 												newDom.removeSubtreeMapping(oldAttr);
 												break;
 											case org.w3c.dom.Node.DOCUMENT_NODE: 
 												break;
 											default:
 												org.w3c.dom.Node parentNode = domSubj.getParentNode();
 												if (parentNode != null) {
 													parentNode.removeChild(domSubj);
 													newDom.removeSubtreeMapping(domSubj);
 												}
 											}
 												
 										}
 									} else if (
 											oldTriple.getPredicate().equals(xml.nodeValue.asNode())
 											&& !sourceGraph.contains(oldTriple.getSubject(), xml.nodeValue.asNode(), Node.ANY)) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 											domSubj.setNodeValue("");
 										}
 									} else if ( oldTriple.getPredicate().equals(xml.hasAttribute.asNode()) ) {
 										Set<org.w3c.dom.Node> domObjsOrig = graph2domNodeMapping.get(oldTriple.getObject());
 										if (domObjsOrig != null) {
 											Set<org.w3c.dom.Node> domObjs = new HashSet<org.w3c.dom.Node>();
 											domObjs.addAll(domObjsOrig);
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 												Iterator<org.w3c.dom.Node> domObjsIter = domObjs.iterator();
 												while (domObjsIter.hasNext()) {
 													try {
 														Attr oldAttr = (Attr) domObjsIter.next();
 														if ( oldAttr.getNamespaceURI() == null
 																? element.hasAttribute(oldAttr.getName())
 																: element.hasAttributeNS(oldAttr.getNamespaceURI(), oldAttr.getLocalName()))
 															element.removeAttributeNode(oldAttr);
 														newDom.removeSubtreeMapping(oldAttr);
 													} catch(DOMException e) {
 														if (!e.equals(DOMException.NOT_FOUND_ERR))
 															throw e;
 													}
 												}
 											}
 										}
 									} else if ( oldTriple.getPredicate().equals(xml.hasChild.asNode()) ) {
 										Set<org.w3c.dom.Node> domObjs = graph2domNodeMapping.get(oldTriple.getObject());
 										if (domObjs != null) {
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 												Iterator<org.w3c.dom.Node> domObjsIter = domObjs.iterator();
 												while (domObjsIter.hasNext()) {
 													try {
 														org.w3c.dom.Node domObj = domObjsIter.next();
 														try {
 															element.removeChild(domObj);
 														} catch(DOMException e) {
 															
 														}
 														newDom.removeSubtreeMapping(domObj);
 													} catch(DOMException e) {
 														if (!e.equals(DOMException.NOT_FOUND_ERR))
 															throw e;
 													}
 												}
 											}
 										}
 									} else if ( oldTriple.getPredicate().equals(xml.listenedEventType.asNode()) ) {
 										Node eventTypeNode = oldTriple.getObject();
 										if (eventTypeNode.isLiteral()) {
 											String eventType = eventTypeNode.getLiteralLexicalForm();
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 //												System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 												((EventTarget) element).removeEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder.this, false);
 												Set<Element> elemsForEventType = eventType2elements.get(eventType);
 												elemsForEventType.remove(element);
 												if (elemsForEventType.isEmpty()) {
 													eventType2elements.remove(eventType);
 													((EventTarget) document).removeEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder.this, false);
 												}
 
 												Set<String> eventTypesForElement = element2eventTypes.get(element);
 												eventTypesForElement.remove(eventType);
 												if (eventTypesForElement.isEmpty()) {
 													element2eventTypes.remove(element);
 												}
 											}
 										}
 									}
 								}
 //						        System.out.println("End of notifyEvents() in " + this);
 							}
 							
 							dom2graphNodeMapping = newDom.dom2graphNodeMapping;
 							graph2domNodeMapping = newDom.graph2domNodeMapping;
 //							document = newDom.document;
 						}
 					});
 		}
 
 	}
 
 }
