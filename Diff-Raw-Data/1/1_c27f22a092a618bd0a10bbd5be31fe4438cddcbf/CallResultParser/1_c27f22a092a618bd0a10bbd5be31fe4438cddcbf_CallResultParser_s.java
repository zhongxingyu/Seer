 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.StringReader;
 import java.util.ArrayList;
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
 
 /**
  * Call result parser. XML is parsed to produce a {@link CallResult} object.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class CallResultParser extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(CallResultParser.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallResultParser</code>.
     */
    public CallResultParser() {
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
     * Parses the given XML string to create a <code>CallResult</code> object.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if it is not a valid XINS
     *    API function call result.
     */
    public CallResult parse(String xml)
    throws IllegalArgumentException, ParseException {
       return parse(null, xml);
    }
 
    /**
     * Parses the given XML string to create a <code>CallResult</code> object,
     * optionally specifying a <code>FunctionCaller</code>.
     *
     * @param functionCaller
     *    the function caller to associate with the call result, or
     *    <code>null</code>.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if it is not a valid XINS
     *    API function call result.
     */
    public CallResult parse(FunctionCaller functionCaller, String xml)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       try {
          StringReader reader = new StringReader(xml);
          return parse(functionCaller, _xmlBuilder.build(reader));
       } catch (IOException ioException) {
          final String message = "Unable to parse XML returned by API.";
          LOG.error(message, ioException);
          // TODO: Include type of error in here somewhere
          throw new ParseException(message, ioException);
       } catch (JDOMException jdomException) {
          final String message = "Unable to parse XML returned by API.";
          LOG.error(message, jdomException);
          // TODO: Include type of error in here somewhere
          throw new ParseException(message, jdomException);
       }
    }
 
    /**
     * Parses the given XML document to create a <code>CallResult</code>
     * object.
     *
     * @param functionCaller
     *    the function caller to associate with the call result, or
     *    <code>null</code>.
     *
     * @param document
     *    the document to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>document == null || document.getRootElement() == null</code>
     *
     * @throws ParseException
     *    if the specified XML document is not a valid XINS API function call
     *    result.
     */
    private CallResult parse(FunctionCaller functionCaller, Document document)
    throws NullPointerException, ParseException {
 
       Element element = document.getRootElement();
 
       // Check that the root element is <result/>
       if ("result".equals(element.getName()) == false) {
          String message = "The returned XML is invalid. The type of the root element is \"" + element.getName() + "\" instead of \"result\".";
          LOG.error(message);
          throw new ParseException(message);
       }
 
       boolean success     = parseSuccessFlag(element);
       String code         = parseResultCode(element);
       Map parameters      = parseParameters(element);
       Element dataElement = element.getChild("data");
 
       return new CallResult(functionCaller, success, code, parameters, dataElement);
    }
 
    /**
     * Parses the value of the success attribute within the function result
     * element.
     *
     * @param element
     *    the <code>&lt;result/&gt;</code> element, not <code>null</code>.
     *
     * @return
     *    <code>true</code> if the call was successful, otherwise
     *    <code>false</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>.
     *
     * @throws ParseException
     *    if the <code>success</code> attribute could not be found in the
     *    element or if it had an invalid value (it must be either
     *    <code>"true"</code> or <code>"false"</code>).
     */
    private static boolean parseSuccessFlag(Element element)
    throws NullPointerException, ParseException {
 
       // Get the attribute value
       String value = element.getAttributeValue("success");
 
       // The attribute is mandatory
       if (value == null) {
          throw new ParseException("The returned XML is invalid. The attribute \"success\" has to be present in the \"result\" element.");
       }
 
       // Interpret the value
       if (value.equals("true")) {
          return true;
       } else if (value.equals("false")) {
          return false;
       } else {
          throw new ParseException("The returned XML is invalid. The \"success\" attribute in the \"result\" element can only have the value \"true\" or \"false\", the value \"" + value + "\" is invalid.");
       }
    }
 
    /**
     * Parses the result code in the specified result element.
     *
     * @param element
     *    the <code>&lt;result/&gt;</code> element, not <code>null</code>.
     *
     * @return
     *    the result code, or <code>null</code> if there is none.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>.
     */
    private static String parseResultCode(Element element)
    throws NullPointerException {
 
       String code = element.getAttributeValue("code");
       if (code == null || code.length() < 1) {
          return null;
       } else {
          return code;
       }
    }
 
    /**
     * Parses the parameters in the specified result element. The returned
     * {@link Map} will contain have the parameter names as keys
     * ({@link String} objects) and the parameter values as values
     * ({@link String} objects as well).
     *
     * @param element
     *    the <code>result</code> element to be parsed, not <code>null</code>.
     *
     * @return
     *    a non-empty {@link Map} containing the messages, or <code>null</code>
     *    if there are none.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>.
     *
     * @throws ParseException
     *    if the specified XML is not a valid part of a XINS API function call
     *    result.
     */
    private static Map parseParameters(Element element)
    throws NullPointerException, ParseException {
 
       final String elementName  = "param";
       final String keyAttribute = "name";
 
       // Get a list of all sub-elements
       List subElements = element.getChildren(elementName);
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
          String key   = subElement.getAttributeValue(keyAttribute);
          String value = subElement.getText();
 
          // If key or value is empty, then ignore the whole thing
          boolean noKey   = (key   == null || key.length()   < 1);
          boolean noValue = (value == null || value.length() < 1);
          if (noKey && noValue) {
             LOG.error("Found <" + elementName + "/> with an empty key and empty value.");
          } else if (noKey) {
             LOG.error("Found <" + elementName + "/> with an empty key.");
          } else if (noValue) {
             LOG.error("Found <" + elementName + "/> with " + keyAttribute + " \"" + key + "\" but an empty value.");
          } else {
 
             LOG.debug("Found <" + elementName + "/> with " + keyAttribute + " \"" + key + "\" and value \"" + value + "\".");
 
             // Lazily initialize the Map
             if (map == null) {
                map = new HashMap();
 
             // Only one value per key allowed
             } else if (map.get(key) != null) {
                throw new ParseException("The returned XML is invalid. Found <" + elementName + "/> with duplicate " + keyAttribute + " \"" + key + "\".");
             }
 
             // Store the mapping
             map.put(key, value);
          }
       }
 
       return map;
    }
 }
