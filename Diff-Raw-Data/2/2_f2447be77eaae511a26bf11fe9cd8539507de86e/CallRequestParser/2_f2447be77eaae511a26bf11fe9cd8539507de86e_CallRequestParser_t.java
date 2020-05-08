 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Collections;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.CollectionUtils;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Parser that takes XML to build a <code>CallRequest</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.46
  */
 public final class CallRequestParser extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(CallRequestParser.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallRequestParser</code>.
     */
    public CallRequestParser() {
       _xmlBuilder = new SAXBuilder();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Parser that takes an XML document and converts it to a JDOM Document.
     */
    private final SAXBuilder _xmlBuilder;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Parses the given XML string to create a <code>CallRequest</code>
     * object.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link CallRequest}, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if the structure of the
     *    XML is not valid for the definition of a {@link CallRequest}.
     */
    public CallRequest parse(String xml)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       StringReader reader = new StringReader(xml);
       return parse(new StringReader(xml));
    }
 
    /**
     * Parses the XML in the specified input stream to create a
     * <code>CallRequest</code> object.
     *
     * @param in
     *    the input stream to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link CallRequest}, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>in == null</code>
     *
     * @throws ParseException
     *    if there was an I/O error, if the data on the stream is not valid XML
     *    or if the structure of the XML is not valid for the definition of a
     *    {@link CallRequest}.
     */
    public CallRequest parse(Reader in)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("in", in);
 
       try {
          return parse(_xmlBuilder.build(in));
       } catch (Exception exception) {
          FastStringBuffer message = new FastStringBuffer(128);
          message.append("Unable to parse call request due to unexpected ");
          message.append(exception.getClass().getName());
          String exceptionMessage = exception.getMessage();
          if (exceptionMessage != null && exceptionMessage.length() > 0) {
             message.append(": ");
             message.append(exceptionMessage);
          } else {
             message.append('.');
          }
          String messageString = message.toString();
          LOG.error(messageString);
 
          // TODO: Include type of error in here somewhere
         throw new ParseException(messageString, exception);
       }
    }
 
    /**
     * Parses the given XML document to create a <code>CallRequest</code>
     * object.
     *
     * @param document
     *    the document to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link CallRequest}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>document == null || document.getRootElement() == null</code>
     *
     * @throws ParseException
     *    if the specified XML document is not valid as the definition of a
     *    {@link CallRequest}.
     */
    private CallRequest parse(Document document)
    throws NullPointerException, ParseException {
       return parse(document.getRootElement());
    }
 
    /**
     * Parses the given XML element to create a <code>CallRequest</code>
     * object.
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link CallRequest}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>
     *
     * @throws ParseException
     *    if the specified XML element is not valid as the definition of a
     *    {@link CallRequest}.
     */
    private CallRequest parse(Element element)
    throws NullPointerException, ParseException {
 
       // Check preconditions
       final String EXPECTED_ELEMENT_TYPE = "request";
       if (EXPECTED_ELEMENT_TYPE.equals(element.getName()) == false) {
          throw new ParseException("The specified XML element cannot be parsed to build a CallRequest object. The element type is \"" + element.getName() + "\" instead of \"" + EXPECTED_ELEMENT_TYPE + "\".");
       }
 
       // Parse the function name
       String functionName = element.getAttributeValue("function");
       if (functionName == null || functionName.length() == 0) {
          throw new ParseException("The specified XML element cannot be parsed to build a CallRequest object. The attribute \"function\" is not set.");
       }
 
       // Parse the session identifier, if any
       String sessionID = element.getAttributeValue("session");
 
       // Parse the list of input parameters
       Map parameters = parseParameters(element);
 
       return new CallRequest(sessionID, functionName, parameters);
    }
 
    /**
     * Parses the parameters in the specified element. The returned
     * {@link Map} will contain have the parameter names as keys
     * ({@link String} objects) and the parameter values as values
     * ({@link String} objects as well).
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a non-empty {@link Map} containing the parameters, or
     *    <code>null</code> if there are none.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>.
     *
     * @throws ParseException
     *    if the structure of the specified XML information subset is
     *    considered invalid.
     */
    private static Map parseParameters(Element element)
    throws NullPointerException, ParseException {
 
       final String ELEMENT_NAME  = "param";
       final String KEY_ATTRIBUTE = "name";
 
       // Get a list of all sub-elements
       List subElements = element.getChildren(ELEMENT_NAME);
       int count = (subElements == null)
                 ? 0
                 : subElements.size();
 
       // Loop through all sub-elements
       Map map = null;
       for (int i = 0; i < count; i++) {
 
          // Get the current subelement
          Element subElement = (Element) subElements.get(i);
 
          // Ignore empty elements in the list
          if (subElement == null) {
             continue;
          }
 
          // Get the key and the value
          String key   = subElement.getAttributeValue(KEY_ATTRIBUTE);
          String value = subElement.getText();
 
          // If key or value is empty, then ignore the whole thing
          boolean noKey   = (key   == null || key.length()   < 1);
          boolean noValue = (value == null || value.length() < 1);
          if (noKey && noValue) {
             LOG.error("Found <" + ELEMENT_NAME + "/> with an empty key and empty value.");
          } else if (noKey) {
             LOG.error("Found <" + ELEMENT_NAME + "/> with an empty key.");
          } else if (noValue) {
             LOG.error("Found <" + ELEMENT_NAME + "/> with " + KEY_ATTRIBUTE + " \"" + key + "\" but an empty value.");
          } else {
 
             LOG.debug("Found <" + ELEMENT_NAME + "/> with " + KEY_ATTRIBUTE + " \"" + key + "\" and value \"" + value + "\".");
 
             // Lazily initialize the Map
             if (map == null) {
                map = new HashMap();
 
             // Only one value per key allowed
             } else if (map.get(key) != null) {
                throw new ParseException("The returned XML is invalid. Found <" + ELEMENT_NAME + "/> with duplicate " + KEY_ATTRIBUTE + " \"" + key + "\".");
             }
 
             // Store the mapping
             map.put(key, value);
          }
       }
 
       return map;
    }
 }
