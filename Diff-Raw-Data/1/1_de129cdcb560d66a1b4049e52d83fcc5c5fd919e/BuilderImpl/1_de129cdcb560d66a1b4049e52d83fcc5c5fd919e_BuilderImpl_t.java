 package org.xbrlapi.builder;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.utilities.XMLDOMBuilder;
 import org.xml.sax.Attributes;
 
 /**
  * Class defining the SAX event handlers that
  * enable a fragment to be built up during 
  * DTS discovery.
  * This should only be instantiated during
  * the creation of a fragment subclass.
  * The builder is responsible for construction of the XML
  * constituting the fragment itself and the XML that contains
  * the metadata about the fragment and its relationship to other 
  * fragments.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class BuilderImpl implements Builder {
 		
 	protected static Logger logger = Logger.getLogger(Loader.class);
 	
 	/**
 	 * The XML DOM used to build up fragments.
 	 */
 	private static Document dom = null;
 	
 	/**
 	 * The data root element.
 	 */
 	private Element data = null;
 	
 	/**
 	 * The metadata root element.
 	 */
 	private Element metadata = null;
 	
 	/**
 	 * The element to append new content during construction.
 	 */
 	private Element insertionPoint = null;
 	
 	/**
 	 * Flag to indicate that a fragment has yet to have any data inserted into it.
 	 */
 	private boolean isNewFragment = true;
 
 	/**
 	 * Create the builder making sure that the static DOM 
 	 * is instantiated and creating the metadata root element.
 	 * @param dom The DOM to use in the builder.
 	 * @throws XBRLException if the DOM is not the same as an existing
 	 * DOM being used by the builder.
 	 */
 	public BuilderImpl(Document dom) throws XBRLException {
 		if (BuilderImpl.dom != null) {
 			if (! BuilderImpl.dom.equals(dom))
 				throw new XBRLException("All builders used in a load must use the same DOM.");
 		}
 		BuilderImpl.dom = dom;
 		setupBuilder();
 	}
 	
 	/**
 	 * Set up the data and metadata elements.
 	 * The element that will contain the data itself is a child of the metadata element.
 	 * The metadata element becomes the root of the XML fragment that is stored in the data store.
 	 */
 	private void setupBuilder() {
 		metadata = dom.createElementNS(Constants.XBRLAPINamespace,Constants.XBRLAPIPrefix + ":" + Constants.FragmentRootElementName);
 		Element container = dom.createElementNS(Constants.XBRLAPINamespace,Constants.XBRLAPIPrefix + ":" + Constants.FragmentDataContainerElementName);
 		setInsertionPoint(container);
 		metadata.appendChild(container);
 
 		// Declare the xml namespace to keep Xindice happy.
         metadata.setAttribute("xmlns:" + Constants.XMLPrefix, Constants.XMLNamespace);
 		
 	}
 	
 	/**
 	 * Create the builder making sure that the static DOM 
 	 * is instantiated and creating the metadata root element.
 	 */
 	public BuilderImpl() {
 		if (dom == null) {
 			dom = XMLDOMBuilder.newDocument();
 		}		
 		setupBuilder();
 	}
 
 	/**
 	 * Restores the builder to its pre-use state.
 	 */
 	public void close() {
 		BuilderImpl.dom = null;
 	}
 	
 	/**
 	 * Get the XML DOM node.
 	 * @return the DOM node.
 	 */
 	private Document getDOM() {
 		return dom;
 	}
 
 	/**
 	 * Get the root element of the data structure.
 	 * @return the data XML structure.
 	 */
 	public Element getData() {
 		return data;
 	}
 	
 	/**
 	 * Get the metadata DOM document.
 	 * @return the metadata XML structure.
 	 */
 	public Element getMetadata() {
 		return metadata;
 	}
 
 	/**
 	 * @return true iff the builder has not yet added an element to the fragment.
 	 */
 	public boolean isNewFragment() {
 		return isNewFragment;
 	}
 
     /**
      * Get the insertion point for new data content.
      * @return the insertion point for new data nodes.
      */
     public Element getInsertionPoint() {
     	return insertionPoint;
     }
 
     /**
      * Set the insertion point for new fragment material
      * @param e The element that is the current insertion point for data.
      */
     private void setInsertionPoint(Element e) {
     	insertionPoint = e;
     }
     
     /**
      * Append a node to the data.
      * @param child The node to be appended.
      * @throws XBRLException if the first node to be appended
      * is not an Element node.
      */
     private void appendChild(Node child) throws XBRLException {
 		if (isNewFragment()) {
 			if (child.getNodeType() != Element.ELEMENT_NODE) throw new XBRLException("The first child to be inserted must be an element node");
 			getInsertionPoint().appendChild(child);
 			data = (Element) child;
 			isNewFragment = false;
 		} else {
 			getInsertionPoint().appendChild(child);
 		}
 		if (child.getNodeType() == Element.ELEMENT_NODE) setInsertionPoint((Element) child);
     }
     
 	/**
 	 * Append a text node.
 	 * @param text The node to be appended.
 	 * @throws XBRLException if the node cannot be appended.
 	 */
 	public void appendText(String text) throws XBRLException
 	{
 		appendChild(getDOM().createTextNode(text));
 	}
 	
 	/**
 	 * Append a processing instruction node
 	 * @param target The processing target application identifier.
 	 * @param data The data defining what is to be done.
 	 * @throws XBRLException if the node cannot be appended.
 	 */
 	public void appendProcessingInstruction(String target, String data) throws XBRLException
 	{
 		appendChild(getDOM().createProcessingInstruction(target,data));
 	}
 	
 	/**
 	 * Append a comment node.
 	 * @param text The data constituting the content of the comment.
 	 * @throws XBRLException if the node cannot be appended.
 	 */
 	public void appendComment(String text) throws XBRLException
 	{
 		appendChild(getDOM().createComment(text));
 	}
 
 	/**
 	 * Append an element node.
 	 * @param namespaceURI The namespace of the element found by the SAX parser.
 	 * @param lName The local name of the element found by the SAX parser.
 	 * @param qName The QName of the element found by the SAX parser.
 	 * @param attrs The set of attributes found by the SAX parser.
 	 * @throws XBRLException if the node cannot be appended.
 	 */
 	public void appendElement(
 			String namespaceURI, 
 			String lName, 
 			String qName, 
 			Attributes attrs) throws XBRLException	
 	{
 		Element newElement = createElement(namespaceURI, lName, qName, attrs);
 		if (newElement == null) {
 			throw new XBRLException("Could not create element: " + namespaceURI + " " + lName);
 		}
 		appendChild(newElement);
 	}
 
 	/**
 	 * Create an element node.
 	 * @param namespaceURI The namespace of the element found by the SAX parser.
 	 * @param lName The local name of the element found by the SAX parser.
 	 * @param qName The QName of the element found by the SAX parser.
 	 * @param attrs The set of attributes found by the SAX parser.
 	 */
 	private Element createElement(
 			String namespaceURI, 
 			String lName,
 			String qName, 
 			Attributes attrs)  {
 		
 		// Keep track of namespaces used by element or its attributes
 		HashMap<String,String> namespaces = new HashMap<String,String>();
 		namespaces.put(namespaceURI,"");
 
 		Element newElement = getDOM().createElementNS(namespaceURI, qName);
 				
 		// Handle elements created with a null attrs array (not created from SAX parsing input)
 		if (attrs == null)
 			return newElement;
 		
 		// Insert all attributes with namespaces
 		for (int i = 0; i < attrs.getLength(); i++) {
 			
 			if (attrs.getURI(i).equals(Constants.XMLNamespace)) {
 				newElement.setAttribute(attrs.getQName(i), attrs.getValue(i));
 				namespaces.put(Constants.XMLNamespace,"");
 			} else if (! attrs.getURI(i).equals("")) {
 				newElement.setAttributeNS(attrs.getURI(i), attrs.getQName(i),attrs.getValue(i));
 				namespaces.put(attrs.getURI(i),"");
 			}
 		}
 		
 		for (int i = 0; i < attrs.getLength(); i++) {
 			
 			if (! attrs.getURI(i).equals(""))
 				;
 			else if (namespaces.containsKey(attrs.getValue(i)))
 				;
 			else if (!(attrs.getQName(i).equals("xmlns") || attrs.getQName(i).startsWith("xmlns:")))
 				newElement.setAttribute(attrs.getQName(i),attrs.getValue(i));
 			else
 				newElement.setAttribute(attrs.getQName(i),attrs.getValue(i));
 		}
 
 		return newElement;
 	}		
 	
 	/**
 	 * Insert a new element without attributes.
 	 * @param namespaceURI The namespace of the element found by the SAX parser.
 	 * @param lName The local name of the element found by the SAX parser.
 	 * @param qName The QName of the element found by the SAX parser.
 	 * @throws XBRLException if the node cannot be appended.
 	 */
 	public void appendElement(
 			String namespaceURI, 
 			String lName, 
 			String qName) throws XBRLException	
 	{
 		appendElement(namespaceURI,lName,qName,null);
 	}
 
 	
 	
 	/**
 	 * Update the insertion point for new content when reaching 
 	 * the end of an element.
 	 * TODO try to make endElement this a private method.
 	 * @param namespaceURI The namespace URI of the element that is ending.
 	 * @param lName The local name of the element that is ending.
 	 * @param qName The QName of the element that is ending.
 	 * @throws XBRLException if the current insertion point is not an 
 	 * element node or if the new (parent) insertion point is not an element node.
 	 */
 	public void endElement(
 			String namespaceURI,
 			String lName,
 			String qName
 			) throws XBRLException {
 		
 		// Make sure that the insertion point is stepping up from an element node (to an element or document node)
 		if (getInsertionPoint().getNodeType() != Node.ELEMENT_NODE)
 			throw new XBRLException("The fragment insertion point is pointing to the wrong kind of node: " + getInsertionPoint().getNodeType() + ".");
 		
 		Node parentNode = getInsertionPoint().getParentNode();
 		if (parentNode != null) {
 			if (parentNode.getNodeType() != Element.ELEMENT_NODE) {
 				throw new XBRLException("The fragment builder insertion point is trying to move to a non-element node.");
 			}
 			setInsertionPoint((Element) parentNode);
 		}
 		
 	}
 	
 
 	/**
 	 * Append a notation declaration.
 	 */
 	public void appendNotationDecl(
 			String name, 
 			String publicId, 
 			String systemId
 			) throws XBRLException {
 	    StringBuffer b = new StringBuffer("<!NOTATION ");
 	    b.append(name);
 	    if (publicId != null)
 	      b.append(" PUBLIC \"").append(publicId).append('"');
 	    if (systemId != null)
 	      b.append(" SYSTEM \"").append(systemId).append('"');
 	    b.append('>');
 		// TODO How do I add a notation node to the DOM when fragment building
 	}
 	
 	/**
 	 * Append an unparsed entity declaration.
 	 */
 	public void appendUnparsedEntityDecl(
 			String name, 
 			String publicId,
 			String systemId, 
 			String notationName) 
 	throws XBRLException {
 	    StringBuffer b = new StringBuffer("<!ENTITY ");
 	    b.append(name);
 	    if (publicId != null)
 	      b.append(" PUBLIC \"").append(publicId).append('"');
 	    if (systemId != null)
 	      b.append(" SYSTEM \"").append(systemId).append('"');
 	    b.append(" NDATA \"").append(notationName).append('"');
 	    b.append('>');
 	    
 		// TODO How to add an unparsed Entity Declaration to a DOM.
 	    throw new XBRLException("Not yet implemented.");
 	}
 
 	/**
 	 * Append an element DTD declaration.
 	 */
 	public void appendElementDecl(
 			String name, 
 			String model
 			) throws XBRLException {
 		// TODO How to add an element DTD declaration to a DOM
 	    throw new XBRLException("Not yet implemented.");
 	}	
 	
 	/**
 	 * Append an internal entity DTD declaration.
 	 */
 	public void appendInternalEntityDecl(
 			String name, 
 			String value
 			) throws XBRLException {
 		// TODO How to add an internal entity declaration
 	    throw new XBRLException("Not yet implemented.");
 	}
 
 	/**
 	 * Append an external entity DTD declarations.
 	 */
 	public void appendExternalEntityDecl(
 			String name, 
 			String publicId, 
 			String systemId
 			) throws XBRLException {
 		// TODO Determine how to add an external entity declaration
 	    throw new XBRLException("Not yet implemented.");
 	}
 
 	/**
 	 * Append an attribute DTD declaration
 	 */
 	public void appendAttributeDecl(
 			String eName, 
 			String aName, 
 			String type,
 			String valueDefault, 
 			String value
 			) throws XBRLException {
 		// TODO How to add an attribute DTD declaration
 	    throw new XBRLException("Not yet implemented.");
 	}
 
     //===========================================================
     // Metadata construction methods
     //===========================================================	
 	
 	/**
 	 * Set a metadata attribute.
 	 * @param name The name of the attribute.
 	 * @param value The value of the attribute.
 	 **/
 	public void setMetaAttribute(String name, String value) {
 		getMetadata().setAttribute(name,value);		
 	}
 	
 	/**
 	 * Get a metadata attribute.
 	 * @param name The name of the attribute.
 	 * @return the string value of the metadata attribute or 
 	 * null if it is not specified.
 	 **/
 	public String getMetaAttribute(String name) {
 		String value = getMetadata().getAttribute(name); 
 		if (value == "") {
 			return null;
 		}
 		return value;
 	}
 
 	/**
 	 * Remove a metadata attribute.
 	 * @param name The name of the attribute.
 	 **/
 	public void removeMetaAttribute(String name) {
 		getMetadata().removeAttribute(name);		
 	}
 	
     /**
      * Appends a child element to the root metadata element.
      * @param eName Name of the element to be added (no namespaces are used).
      * @param attributes A hashmap from attribute name keys to attribute values.
      * @throws XBRLException if the metadata element cannot be appended.
      */
     public void appendMetadataElement(String eName, HashMap<String,String> attributes) throws XBRLException {
 
 		Element child = getDOM().createElementNS(Constants.XBRLAPINamespace,Constants.XBRLAPIPrefix + ":" + eName);
 		Iterator<String> attributeNames = attributes.keySet().iterator();
 		while (attributeNames.hasNext()) {
 			String aName = attributeNames.next();
 			String aValue = attributes.get(aName);
 			if (aName != null) {
 				if (aValue == null) throw new XBRLException("A metadata element is being added but attribute, " + aName + ", has a null value.");
 				child.setAttribute(aName,aValue); 
 			} else throw new XBRLException("A metadata element is being added with an attribute with a null name.");
 		}
 		getMetadata().appendChild(child);
  	
     }
     
     /**
      * Removes a child element from the metadata root element by 
      * specifying the name of the child and the value of the element's 
      * text content and/or the value of a named attribute.  All specified 
      * information must match for the deletion to succeed.
      * @param eName Name of the element to be added (no namespaces are used).
      * @param attributes A hashmap from attribute name keys to attribute values.
      * @throws XBRLException if the metadata element cannot be removed.
      */
     public void removeMetadataElement(String eName, HashMap<String,String> attributes) throws XBRLException {
 
 		NodeList children = getMetadata().getElementsByTagNameNS(Constants.XBRLAPINamespace,eName);
 		for (int i=0; i<children.getLength(); i++) {
 			boolean match = true;
 			Element child = (Element) children.item(i);
 			Iterator<String> attributeNames = attributes.keySet().iterator();
 			while (attributeNames.hasNext()) {
 				String aName = attributeNames.next();
 				String aValue = attributes.get(aName);
 				if (aName != null) {
 					if (aValue == null) throw new XBRLException("A metadata element is being checked but attribute, " + aName + ", has a null value.");
 					if (! child.getAttribute(aName).equals(aValue)) {
 						match = false;
 					}
 				} else throw new XBRLException("A metadata element is being checked against an attribute with a null name.");
 			}
 			
 			if (match) {
 				getMetadata().removeChild(child);
 				break;
 			}
 		}
     	
     }
 
 }
