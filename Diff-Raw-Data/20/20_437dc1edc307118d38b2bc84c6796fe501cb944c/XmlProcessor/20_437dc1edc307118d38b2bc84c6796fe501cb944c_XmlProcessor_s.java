 package de.uni_koblenz.ist.utilities.xml;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 public abstract class XmlProcessor {
 
 	/**
 	 * The STAX reader for parsing the XML file to convert.
 	 */
 	private XMLStreamReader parser;
 
 	/**
 	 * Contains XML element names.
 	 */
 	private Stack<String> elementNameStack;
 
 	/**
 	 * Collects character data per element.
 	 */
 	private Stack<StringBuilder> elementContentStack;
 
 	/**
 	 * Names of XML elements which are completely ignored (including children).
 	 */
 	private final Set<String> ignoredElements;
 
 	/**
 	 * Counter for ignored state. <br>
 	 * ignore > 0 ==> elements are ignored, <br>
 	 * ignore == 0 ==> elements are processed.
 	 */
 	private int ignoreCounter;
 
 	private String fileName;
 
 	public XmlProcessor() {
 		ignoredElements = new TreeSet<String>();
 	}
 
 	public void process(String fileName) throws FileNotFoundException,
 			XMLStreamException {
 		this.fileName = fileName;
 		InputStream in = new BufferedInputStream(new FileInputStream(fileName));
 		XMLInputFactory factory = XMLInputFactory.newInstance();
 		parser = factory.createXMLStreamReader(in);
 		for (int event = parser.getEventType(); event != XMLStreamConstants.END_DOCUMENT; event = parser
 				.next()) {
 			switch (event) {
 			case XMLStreamConstants.START_DOCUMENT:
 				startDocumentEvent();
 				break;
 			case XMLStreamConstants.START_ELEMENT:
 				startElementEvent();
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				endElementEvent();
 				break;
 			case XMLStreamConstants.CHARACTERS:
 				elementContentStack.peek().append(parser.getText());
 				break;
 			}
 		}
 		endDocumentEvent();
 	}
 
 	private void startDocumentEvent() throws XMLStreamException {
 		elementNameStack = new Stack<String>();
 		elementContentStack = new Stack<StringBuilder>();
 		ignoreCounter = 0;
 		startDocument();
 	}
 
 	private void endDocumentEvent() throws XMLStreamException {
 		if (!elementNameStack.isEmpty()) {
 			StringBuilder openElements = new StringBuilder(
 					"Missing end tags for");
 			String delim = ": ";
 			for (String s : elementNameStack) {
 				openElements.append(delim).append(s);
 				delim = ", ";
 			}
 			throw new XMLStreamException(openElements.toString());
 		}
 		assert ignoreCounter == 0;
 		endDocument();
 	}
 
 	private void startElementEvent() throws XMLStreamException {
 		QName qname = parser.getName();
 		String name;
 		if ((qname.getPrefix() == null) || qname.getPrefix().isEmpty()) {
 			name = qname.getLocalPart();
 		} else {
 			name = qname.getPrefix() + ":" + qname.getLocalPart();
 		}
 
 		elementNameStack.push(name);
 		elementContentStack.push(new StringBuilder());
 
 		if (ignoredElements.contains(name)) {
 			++ignoreCounter;
 		}
 		if (ignoreCounter == 0) {
 			startElement(name);
 		}
 	}
 
 	private void endElementEvent() throws XMLStreamException {
 		QName qname = parser.getName();
 		String name;
 		if ((qname.getPrefix() == null) || qname.getPrefix().isEmpty()) {
 			name = qname.getLocalPart();
 		} else {
 			name = qname.getPrefix() + ":" + qname.getLocalPart();
 		}
 
 		if (elementNameStack.isEmpty()) {
 			throw new XMLStreamException("Unexpected end element </" + name
 					+ "> in line " + parser.getLocation().getLineNumber());
 		}
 
 		String s = elementNameStack.peek();
 
 		if (!s.equals(name)) {
 			throw new XMLStreamException("Element <" + s
 					+ "> is terminated by </" + name + "> in line "
 					+ parser.getLocation().getLineNumber());
 		}
 
 		if (ignoreCounter == 0) {
 			endElement(name, elementContentStack.peek());
 		}
 		if (ignoredElements.contains(name)) {
 			assert ignoreCounter > 0;
 			--ignoreCounter;
 		}
 		elementNameStack.pop();
 		elementContentStack.pop();
 	}
 
 	protected abstract void startElement(String name) throws XMLStreamException;
 
 	protected abstract void endElement(String name, StringBuilder content)
 			throws XMLStreamException;
 
 	protected abstract void startDocument() throws XMLStreamException;
 
 	protected abstract void endDocument() throws XMLStreamException;
 
 	protected String getAttribute(String nsPrefix, String name)
 			throws XMLStreamException {
 		return parser.getAttributeValue(parser.getNamespaceURI(nsPrefix), name);
 	}
 
 	protected String getAttribute(String name) throws XMLStreamException {
		return parser.getAttributeValue(null, name);
 	}
 
 	protected void addIgnoredElements(String... names) {
 		for (String name : names) {
 			if (name != null) {
 				name = name.trim();
 				if (name.length() > 0) {
 					ignoredElements.add(name);
 				}
 			}
 		}
 	}
 
 	public int getNestingDepth() {
 		return elementNameStack.size();
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public Stack<String> getElementNameStack() {
 		return elementNameStack;
 	}
 
 	public XMLStreamReader getParser() {
 		return parser;
 	}
 }
