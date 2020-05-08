 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 
 /**
  * Call result parser. XML is parsed to produce a
  * {@link XINSServiceCaller.Result} object.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class ResultParser extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(ResultParser.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ResultParser</code>.
     */
    public ResultParser() {
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
     * Parses the given XML string to create a
     * <code>XINSServiceCaller.Result</code> object with the specified
     * <code>TargetDescriptor</code>.
     *
     * @param target
     *    the {@link TargetDescriptor} that was used to get the XML, cannot be
     *    <code>null</code>.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if it is not a valid XINS
     *    API function call result.
     */
    public XINSServiceCaller.Result parse(TargetDescriptor target,
                                          String           xml)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("target", target, "xml", xml);
 
       StringReader reader = new StringReader(xml);
       Document document;
       try {
          document = _xmlBuilder.build(reader);
       } catch (Throwable exception) {
          String detail = exception.getMessage();
          FastStringBuffer buffer = new FastStringBuffer(250);
          buffer.append("Unable to convert the specified character string to XML");
          if (detail != null && detail.length() > 0) {
             buffer.append(": ");
             buffer.append(detail);
          } else {
             buffer.append('.');
          }
          String message = buffer.toString();
          //LOG.error(message, exception);
          Log.log_2005(exception, detail);
          throw new ParseException(message);
       }
 
       return parse(target, document);
    }
 
    /**
     * Parses the given XML <code>Document</code> to create a
     * <code>XINSServiceCaller.Result</code> object with the specified
     * <code>TargetDescriptor</code>.
     *
     * @param target
     *    the {@link TargetDescriptor} that was used to get the XML, cannot be
     *    <code>null</code>.
     *
     * @param document
     *    the document to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>target == null
     *          || document == null
     *          || document.</code>{@link Document#getRootElement() getRootElement()}<code> == null</code>
     *
     * @throws ParseException
     *    if the specified XML document is not a valid XINS API function call
     *    result.
     */
    private XINSServiceCaller.Result parse(TargetDescriptor target,
                                           Document         document)
    throws NullPointerException, ParseException {
 
       Element element = document.getRootElement();
 
       // Check that the root element is <result/>
       if ("result".equals(element.getName()) == false) {
          String message = "The returned XML is invalid. The type of the root element is \"" + element.getName() + "\" instead of \"result\".";
          Log.log_2006(element.getName());
          throw new ParseException(message);
       }
 
       boolean success     = parseSuccessFlag(element);
       String code         = parseResultCode(element);
       Map parameters      = parseParameters(element);
       Element dataElement = element.getChild("data");
 
      return new XINSServiceCaller.Result(target, success, code, parameters, dataElement);
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
             Log.log_2001(ELEMENT_NAME);
          } else if (noKey) {
             Log.log_2002(ELEMENT_NAME);
          } else if (noValue) {
             Log.log_2003(ELEMENT_NAME, KEY_ATTRIBUTE, key);
          } else {
 
             Log.log_2004(ELEMENT_NAME, KEY_ATTRIBUTE, key, value);
 
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
