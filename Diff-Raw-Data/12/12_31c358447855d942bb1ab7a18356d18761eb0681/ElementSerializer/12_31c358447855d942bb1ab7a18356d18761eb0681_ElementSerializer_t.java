 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.xml;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.ProgrammingError;
 
 import org.xins.common.io.FastStringWriter;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Serializer that takes an <code>Element</code> and converts it to an XML
  * string.
  *
  * <p>This class is not thread-safe. It should only be used on one thread at a
  * time.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public final class ElementSerializer extends Object {
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = ElementSerializer.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ElementSerializer</code>.
     */
    public ElementSerializer() {
      // TODO: Set _instanceNumber
       _lock       = new Object();
       _namespaces = new HashMap();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Lock object that is synchronized on when reading or writing
     * <code>_inUse</code>.
     */
    private final Object _lock;
 
    /**
     * Flag that indicates whether this serializer is currently in use. It may
     * only be used by one thread at a time.
     */
    private boolean _inUse;
 
    /**
     * Map that links namespaces to their prefixes. Each key is a namespace and
     * the corresponding value is the associated prefix.
     */
    private Map _namespaces;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Serializes the element to XML. This method is not reentrant. Hence, it
     * should only be used from a single thread.
     *
     * @param element
     *    the element to serialize, cannot be <code>null</code>.
     *
     * @return
     *    an XML document that is represents <code>element</code>, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>element == null</code>.
     */
    public String serialize(Element element)
    throws IllegalArgumentException {
 
       // TODO: TRACE logging
 
       synchronized (_lock) {
 
          // Make sure this serializer is not yet in use
          if (_inUse) {
            // TODO: Use _instanceNumber in message
            String message = "ElementSerializer instance already in use.";
             Log.log_1050(CLASSNAME, "serialize(Element)", message);
             throw new ProgrammingError(message);
          }
 
          // Lock this serializer
          _inUse = true;
       }
 
       // Check argument
       MandatoryArgumentChecker.check("element", element);
 
       // Create an XMLOutputter
       FastStringWriter fsw = new FastStringWriter();
       XMLOutputter out;
       final String ENCODING = "UTF-8";
       try {
          out = new XMLOutputter(fsw, ENCODING);
       } catch (UnsupportedEncodingException uee) {
          Log.log_1052(uee, XMLOutputter.class.getName(), "<init>(Writer,String)");
          throw new ProgrammingError("Expected XMLOutputter to support encoding \"" + ENCODING + "\".", uee);
       }
 
       // XXX: Allow output of declaration to be configured?
 
       // Output the XML that represents the Element
       try {
          output(out, element);
 
       // I/O errors should not happen on a FastStringWriter
       } catch (IOException exception) {
          Log.log_1052(exception, CLASSNAME, "output(XMLOutputter,Element)");
          throw new ProgrammingError(CLASSNAME + ".output(XMLOutputter,Element) threw unexpected " + exception.getClass().getName() + '.', exception);
 
       // Always close the FastStringWriter
       } finally {
          fsw.close();
          _inUse = false;
       }
 
       String xml = out.toString();
 
       // TODO: TRACE logging
 
       return xml;
    }
 
    /**
     * Generates XML for the specified <code>Element</code>.
     *
     * @param out
     *    the {@link XMLOutputter} to use, cannot be <code>null</code>.
     *
     * @param element
     *    the {@link Element} object to convert to XML, cannot be
     *    <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>out == null || element == null</code>.
     *
     * @throws IOException
     *    if there is an I/O error.
     */
    private void output(XMLOutputter out, Element element)
    throws NullPointerException, IOException {
 
       String namespaceURI = element.getNamespaceURI();
       String localName    = element.getLocalName();
 
       // Write an element with namespace
       if (namespaceURI != null) {
 
          // Determine if the namespace already has an assigned prefix
          String prefix = (String) _namespaces.get(namespaceURI);
          boolean newNamespace = (prefix == null);
          if (newNamespace) {
 
             // Generate a namespace prefix
             prefix = "ns" + _namespaces.size();
 
             // Remember the association of the namespace with the prefix
             _namespaces.put(namespaceURI, prefix);
          }
 
          // Write the start tag with prefix
          out.startTag(prefix + ':' + localName);
 
          // Associate the namespace with the prefix in the result XML
          if (newNamespace) {
             out.attribute("xmlns:" + prefix, namespaceURI);
          }
 
       // Write an element without namespace
       } else {
          out.startTag(localName);
       }
 
       // Loop through all attributes
       Map attributes = element.getAttributeMap();
       Iterator names = attributes.keySet().iterator();
       while (names.hasNext()) {
 
          // Get the namespace, local name and value
          Element.QualifiedName qn  = (Element.QualifiedName) names.next();
          String attrNamespaceURI  = qn.getNamespaceURI();
          String attrLocalName     = qn.getLocalName();
          String attrValue         = (String) attributes.get(qn);
 
          // Write an attribute with namespace
          if (attrNamespaceURI != null) {
 
             // Determine if the namespace already has an assigned prefix
             String prefix = (String) _namespaces.get(attrNamespaceURI);
             boolean newNamespace = (prefix == null);
             if (newNamespace) {
 
                // Generate a namespace prefix
                prefix = "ns" + _namespaces.size();
 
                // Remember the association of the namespace with the prefix
                _namespaces.put(namespaceURI, prefix);
             }
 
             // Write the attribute with prefix
             out.attribute(prefix + ':' + attrLocalName, attrValue);
 
             // Associate the namespace with the prefix in the result XML
             if (newNamespace) {
                out.attribute("xmlns:" + prefix, attrNamespaceURI);
             }
 
          // Write an attribute without namespace
          } else {
             out.attribute(attrLocalName, attrValue);
          }
       }
 
       // Process all contained elements
       List content = element.getChildElements();
       int count = content == null ? 0 : content.size();
       for (int i = 0; i < count; i++) {
          Object o = content.get(i);
          output(out, (Element) o);
       }
 
       // Output contained PCDATA
       if (element.getText() != null) {
          out.pcdata(element.getText());
       }
 
       // End the tag
       out.endTag();
    }
 }
