 /*
  * Created on Oct 11, 2005
  */
 package org.codejive.common.xml;
 
 import java.util.ArrayList;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.stream.Location;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.ProcessingInstruction;
 
 public class DomReader implements XMLStreamReader {
 	private Document doc;
 	private Node start, current;
 	private boolean isStartElement;
 	private int currentEventType;
 	private ArrayList<Attr> attrList;
 	private ArrayList<Attr> nsList;
 	private char[] currentText;
 	private NamespaceContext currentContext;
 	private Location currentLocation;
 	
 	private class DummyLocation implements Location {
 
 		public int getCharacterOffset() {
 			return -1;
 		}
 
 		public int getColumnNumber() {
 			return -1;
 		}
 
 		public int getLineNumber() {
 			return -1;
 		}
 
 		public String getPublicId() {
 			return null;
 		}
 
 		public String getSystemId() {
 			return null;
 		}
 		
 	}
 	
 	protected DomReader(Document _doc) {
 		doc = _doc;
 		start = current = _doc;
 		attrList = new ArrayList<Attr>();
 		nsList = new ArrayList<Attr>();
 		currentText = null;
 		currentContext = null;
 		currentLocation = new DummyLocation();
 		isStartElement = true;
 		analyzeCurrent();
 	}
 
 	protected DomReader(Element _elem) {
 		doc = XmlHelper.ownerDocument(_elem);
 		start = current = _elem;
 		attrList = new ArrayList<Attr>();
 		nsList = new ArrayList<Attr>();
 		currentText = null;
 		currentContext = null;
 		currentLocation = new DummyLocation();
 		isStartElement = true;
 		analyzeCurrent();
 	}
 
 	// Check if the attribute represents a namespace definition
 	private boolean isNSAttr(Attr attr) {
 		String name = attr.getName();
 		return (name.matches("xmlns(:.*+)?"));
 	}
 
 	private QName getNodeName(Node _node) {
 		String ns = (_node.getNamespaceURI() != null) ? _node.getNamespaceURI() : XMLConstants.NULL_NS_URI;
 		String prefix = (_node.getPrefix() != null) ? _node.getPrefix() : XMLConstants.DEFAULT_NS_PREFIX;
 		return new QName(ns, _node.getLocalName(), prefix);
 	}
 	
 	private void analyzeCurrent() {
 		switch (current.getNodeType()) {
 			case Node.DOCUMENT_NODE:
 				currentEventType = (isStartElement) ? START_DOCUMENT : END_DOCUMENT;
 				break;
 			case Node.ELEMENT_NODE:
 				if (isStartElement) {
 					currentEventType = START_ELEMENT;
 					attrList.clear();
 					nsList.clear();
 					for (int i = 0; i < current.getAttributes().getLength(); i++) {
 						Attr attr = (Attr)current.getAttributes().item(i);
 						if (isNSAttr(attr)) {
 							nsList.add(attr);
 						} else {
 							attrList.add(attr);
 						}
 					}
 				} else {
 					currentEventType = END_ELEMENT;
 					attrList.clear();
 					nsList.clear();
 					for (int i = 0; i < current.getAttributes().getLength(); i++) {
 						Attr attr = (Attr)current.getAttributes().item(i);
 						if (isNSAttr(attr)) {
 							nsList.add(attr);
 						}
 					}
 				}
 				break;
 			case Node.ATTRIBUTE_NODE:
 				Attr attr = (Attr)current;
 				if (isNSAttr(attr)) {
 					currentEventType = NAMESPACE;
 				} else {
 					currentEventType = ATTRIBUTE;
 				}
 				break;
 			case Node.COMMENT_NODE:
 				currentEventType = COMMENT;
 				break;
 			case Node.CDATA_SECTION_NODE:
 				currentEventType = CDATA;
 				break;
 			case Node.ENTITY_NODE:
 				currentEventType = ENTITY_DECLARATION;
 				break;
 			case Node.ENTITY_REFERENCE_NODE:
 				currentEventType = ENTITY_REFERENCE;
 				break;
 			case Node.NOTATION_NODE:
 				currentEventType = NOTATION_DECLARATION;
 				break;
 			case Node.PROCESSING_INSTRUCTION_NODE:
 				currentEventType = PROCESSING_INSTRUCTION;
 				break;
 			case Node.TEXT_NODE:
 				currentEventType = CHARACTERS;
 				break;
 		}
 	}
 
 	public String getEncoding() {
 		return doc.getInputEncoding();
 	}
 
 	public boolean isStandalone() {
 		return doc.getXmlStandalone();
 	}
 
 	public boolean standaloneSet() {
 		return doc.getXmlStandalone();
 	}
 
 	public String getCharacterEncodingScheme() {
 		return doc.getXmlEncoding();
 	}
 
 	public String getVersion() {
 		return doc.getXmlVersion();
 	}
 
 	public int getEventType() {
 		return currentEventType;
 	}
 
 	public int next() throws XMLStreamException {
 		if ((currentEventType == START_DOCUMENT) || (currentEventType == START_ELEMENT)) {
 			if (current.getChildNodes().getLength() > 0) {
 				current = current.getFirstChild();
 			} else {
 				isStartElement = false;
 			}
 		} else {
 			if (current.getNextSibling() != null) {
 				current = current.getNextSibling();
 				isStartElement = true;
 			} else {
 				current = current.getParentNode();
 				isStartElement = false;
 			}
 		}
 		currentText = null;
 		currentContext = null;
 		analyzeCurrent();
 		return getEventType();
 	}
 
 	public int nextTag() throws XMLStreamException {
 		int eventType = next();
 		while((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip
 				// whitespace
 				|| (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) 
 				// skip whitespace
 				|| eventType == XMLStreamConstants.SPACE
 				|| eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
 				|| eventType == XMLStreamConstants.COMMENT
 		) {
 			eventType = next();
 		}
 		if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
 			throw new XMLStreamException("expected start or end tag", getLocation());
 		}
 		return eventType;
 	}
 
 	public boolean hasNext() throws XMLStreamException {
 		return ((current != start) || isStartElement);
 	}
 
 	public int getAttributeCount() {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		return attrList.size();
 	}
 
 	public QName getAttributeName(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return getNodeName(attr);
 	}
 
 	public String getAttributeLocalName(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return attr.getLocalName();
 	}
 
 	public String getAttributeNamespace(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return attr.getNamespaceURI();
 	}
 
 	public String getAttributePrefix(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return (attr.getPrefix() != null) ? attr.getPrefix() : XMLConstants.DEFAULT_NS_PREFIX;
 	}
 
 	public String getAttributeType(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return (attr.getSchemaTypeInfo().getTypeName() != null) ? attr.getSchemaTypeInfo().getTypeName() : "CDATA";
 	}
 
 	public boolean isAttributeSpecified(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		// TODO Figure out if this actually correct, the docs of Attr.getSpecified()
 		// and XMLStreamReader.isAttributeSpecified() refer to the same thing but seem
 		// to contradict eachother
 		return attr.getSpecified();
 	}
 
 	public String getAttributeValue(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr attr = attrList.get(_index);
 		return attr.getNodeValue();
 	}
 
 	public String getAttributeValue(String _namespaceURI, String _localName) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != ATTRIBUTE) {
 			throw new IllegalStateException("Only legal for element and attribute types");
 		}
 		Attr result = null;
 		for (Attr attr : attrList) {
 			if (((_namespaceURI == null) || _namespaceURI.equals(attr.getNamespaceURI()))
 					&& _localName.equals(attr.getLocalName())) {
 				result = attr;
 				break;
 			}
 		}
		return result.getValue();
 	}
 
 	public int getNamespaceCount() {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != NAMESPACE) {
 			throw new IllegalStateException("Only legal for element and namespace types");
 		}
 		return nsList.size();
 	}
 
 	public String getNamespacePrefix(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != NAMESPACE) {
 			throw new IllegalStateException("Only legal for element and namespace types");
 		}
 		String prefix;
 		Attr attr = nsList.get(_index);
 		if (("xmlns".equals(attr.getPrefix())) || ("xmlns".equals(attr.getLocalName()))) {
 			if ("xmlns".equals(attr.getLocalName())) {
 				prefix = null;
 			} else {
 				prefix = attr.getLocalName();
 			}
 		} else {
 			prefix = null;
 		}
 		return prefix;
 	}
 
 	public String getNamespaceURI(int _index) {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT && currentEventType != NAMESPACE) {
 			throw new IllegalStateException("Only legal for element and namespace types");
 		}
 		Attr attr = nsList.get(_index);
 		return attr.getNodeValue();
 	}
 
 	public String getNamespaceURI() {
 		String result;
 		if (currentEventType == START_ELEMENT || currentEventType == END_ELEMENT) {
 			result = current.getNamespaceURI();
 		} else {
 			result = null;
 		}
 		return result;
 	}
 
 	public String getNamespaceURI(String _prefix) {
 		NamespaceContext ctx = getNamespaceContext();
 		return ctx.getNamespaceURI(_prefix);
 	}
 
 	public boolean hasName() {
 		return ((currentEventType == START_ELEMENT)
 				|| (currentEventType == END_ELEMENT)
 				|| (currentEventType == ENTITY_REFERENCE)
 				|| (currentEventType == PROCESSING_INSTRUCTION));
 	}
 
 	public boolean hasText() {
 		return ((currentEventType == CHARACTERS)
 				|| (currentEventType == DTD)
 				|| (currentEventType == ENTITY_REFERENCE)
 				|| (currentEventType == CDATA)
 				|| (currentEventType == SPACE)
 				|| (currentEventType == COMMENT));
 	}
 
 	public boolean isCharacters() {
 		return (currentEventType == CHARACTERS) || (currentEventType == CDATA);
 	}
 
 	public boolean isStartElement() {
 		return (currentEventType == START_ELEMENT);
 	}
 
 	public boolean isEndElement() {
 		return (currentEventType == END_ELEMENT);
 	}
 
 	public boolean isWhiteSpace() {
 		boolean result;
 		if (isCharacters()) {
 			CharacterData cd = (CharacterData)current;
 			result = cd.getData().matches("\\s*");
 		} else {
 			result = false;
 		}
 		return result;
 	}
 
 	public QName getName() {
 		if (currentEventType != START_ELEMENT && currentEventType != END_ELEMENT) {
 			throw new IllegalStateException("Only legal for element types");
 		}
 		return getNodeName(current);
 	}
 
 	public Location getLocation() {
 		return currentLocation;
 	}
 
 	public String getLocalName() {
 		return current.getLocalName();
 	}
 
 	public String getPrefix() {
 		return current.getPrefix();
 	}
 
 	public String getElementText() throws XMLStreamException {
 		if (getEventType() != XMLStreamConstants.START_ELEMENT) {
 			throw new XMLStreamException("parser must be on START_ELEMENT to read next text", getLocation());
 		}
 		int eventType = next();
 		StringBuffer content = new StringBuffer();
 		while (eventType != XMLStreamConstants.END_ELEMENT) {
 			if (eventType == XMLStreamConstants.CHARACTERS
 					|| eventType == XMLStreamConstants.CDATA
 					|| eventType == XMLStreamConstants.SPACE
 					|| eventType == XMLStreamConstants.ENTITY_REFERENCE) {
 				content.append(getText());
 			} else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
 					|| eventType == XMLStreamConstants.COMMENT) {
 				// skipping
 			} else if (eventType == XMLStreamConstants.END_DOCUMENT) {
 				throw new XMLStreamException("unexpected end of document when reading element text content", getLocation());
 			} else if (eventType == XMLStreamConstants.START_ELEMENT) {
 				throw new XMLStreamException("element text content may not contain START_ELEMENT", getLocation());
 			} else {
 				throw new XMLStreamException("Unexpected event type " + eventType, getLocation());
 			}
 			eventType = next();
 		}
 		return content.toString();
 	}
 
 	public String getText() {
 		if (!hasText()) {
 			throw new IllegalStateException("Only valid for text elements");
 		}
 		return current.getTextContent();
 	}
 
 	public int getTextLength() {
 		if (!hasText()) {
 			throw new IllegalStateException("Only valid for text elements");
 		}
 		return current.getTextContent().length();
 	}
 
 	public int getTextStart() {
 		if (!hasText()) {
 			throw new IllegalStateException("Only valid for text elements");
 		}
 		return 0;
 	}
 
 	public char[] getTextCharacters() {
 		if (!hasText()) {
 			throw new IllegalStateException("Only valid for text elements");
 		}
 		if (currentText == null) {
 			currentText = current.getTextContent().toCharArray();
 		}
 		return currentText;
 	}
 
 	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
 		if (!hasText()) {
 			throw new IllegalStateException("Only valid for text elements");
 		}
 		current.getTextContent().getChars(sourceStart, sourceStart + length, target, targetStart);
 		int count = Math.min(current.getTextContent().length() - sourceStart, length);
 		return count;
 	}
 
 	public NamespaceContext getNamespaceContext() {
 		if (currentContext == null) {
 			currentContext = SimpleNamespaceContext.createContext(current);
 		}
 		return currentContext;
 	}
 
 	public Object getProperty(String _name) throws IllegalArgumentException {
 		if (_name == null) {
 			throw new IllegalArgumentException("Property name may not be null");
 		}
 		return null;
 	}
 
 	public void require(int _eventType, String _namespaceURI, String _localName) throws XMLStreamException {
 		if ((currentEventType != _eventType)
 				|| ((_namespaceURI != null) && (_namespaceURI.equals(current.getNamespaceURI())))
 				|| ((_localName != null) && (_localName.equals(current.getLocalName())))) {
 			throw new XMLStreamException("Required values do not match");
 		}
 	}
 
 	public String getPIData() {
 		ProcessingInstruction pi = (ProcessingInstruction) current;
 		return pi.getData();
 	}
 
 	public String getPITarget() {
 		ProcessingInstruction pi = (ProcessingInstruction) current;
 		return pi.getTarget();
 	}
 
 	public void close() throws XMLStreamException {
 		// Nothing to do here
 	}
 }
 
 
 /*
  * $Log:	$
  */
