 package nl.utwente.cs.pxml.transform;
 
 import static nl.utwente.cs.pxml.ProbabilityNodeType.EVENTS;
 import static nl.utwente.cs.pxml.ProbabilityNodeType.INDEPENDENT;
 import static nl.utwente.cs.pxml.ProbabilityNodeType.MUTEX;
 
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Namespace;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import nl.utwente.cs.pxml.ProbabilityNodeType;
 import nl.utwente.cs.pxml.util.CompoundIterator;
 
 public class DocumentTransformer {
 
 	public static final String NS_PREFIX = "p";
 	public static final String NS_URI = "http://www.utwente.nl/~keulen/pxml/";
 
 	// how often probability nodes will occur (0.0: never, 1.0: at every chance, taken from XMLToPXMLTransformer.java)
 	protected float pNodesOccurrence = 0.2f;
 	// array of probability node types (more occurrences will make the type more likely to be picked, taken from
 	// XMLToPXMLTransformer.java)
 	protected ProbabilityNodeType[] pNodeDistribution = {
 			MUTEX, MUTEX, MUTEX, MUTEX, INDEPENDENT, INDEPENDENT, INDEPENDENT, INDEPENDENT, EVENTS
 	};
 
 	// TODO: incorporate _ratioExpSubsets, _maxExpSubsetsPwr and _ratioCieRVars from XMLToPXMLTransformer.java
 
 	protected XMLEventFactory events;
 
 	public DocumentTransformer() {
 		this.events = XMLEventFactory.newInstance();
 	}
 
 	public DocumentTransformer(float pNodesOccurrence, ProbabilityNodeType[] pNodeDistribution) {
 		this();
 		this.pNodesOccurrence = pNodesOccurrence;
 		this.pNodeDistribution = pNodeDistribution;
 	}
 
 	public void transform(XMLEventReader in, XMLEventWriter out) throws DocumentTransformerException {
 		try {
 			// pass events up to the root element (the first StartElement)
 			XMLEvent current = in.nextEvent();
 			while (!current.isStartElement()) {
 				out.add(current);
 				current = in.nextEvent();
 			}
 
 			this.events.createNamespace(NS_PREFIX, NS_URI);
 
 			// current is now a StartElement representing the root element, add the namespace to it
 			StartElement root = this.addNamespaces(current.asStartElement(), events.createNamespace(NS_PREFIX, NS_URI));
 			out.add(root);
 
 			// children of root are next, proceed with probability elements insertion
 			this.insertElements(in, out, 1); // only root element was added, current depth is 1
 
 			// finish the document after element insertion
 			while (in.hasNext()) {
 				out.add(in.nextEvent());
 			}
 			out.close();
 		} catch (XMLStreamException e) {
 			throw new DocumentTransformerException("error parsing xml stream: " + e.getMessage(), e);
 		}
 	}
 
 	protected void insertElements(XMLEventReader in, XMLEventWriter out, int currentDepth) throws XMLStreamException {
 		int startingDepth = currentDepth;
 
 		// read the next event (always added)
 		XMLEvent current = in.nextEvent();
 		switch (current.getEventType()) {
 			case XMLStreamConstants.START_ELEMENT: {
 				out.add(current);
 				// element opening increases depth by 1
 				currentDepth++;
 				if (in.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
 					// encountered an element that has children (no immediate end element), chance to insert something
 					// TODO
 				}
 				break;
 			}
 			case XMLStreamConstants.END_ELEMENT: {
 				out.add(current);
 				// element closing decreases depth by 1
 				currentDepth--;
 				if (currentDepth == startingDepth && in.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
 					// starting depth reached and next event closes this level: this call is done
 					return;
 				}
 				break;
 			}
 			default: {
 				// default to 'copying' the input
 				out.add(current);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public StartElement addNamespaces(StartElement element, Namespace... namespaces) {
 		return this.events.createStartElement(element.getName(), element.getAttributes(), new CompoundIterator<Object>(
 				element.getNamespaces(), (Object[]) namespaces));
 	}
 
 }
