 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.net.URL;
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
  * Parser that takes XML to build a <code>FunctionCaller</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.45
  */
 public class FunctionCallerParser
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(FunctionCallerParser.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>FunctionCallerParser</code>.
     */
    public FunctionCallerParser() {
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
     * Parses the given XML string to create a <code>FunctionCaller</code>
     * object.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if the structure of the
     *    XML is not valid for the definition of a {@link FunctionCaller}.
     */
    public FunctionCaller parse(String xml)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       return parse(new StringReader(xml));
    }
 
    /**
     * Parses the XML in the specified input stream to create a
     * <code>FunctionCaller</code> object.
     *
     * @param in
     *    the input stream to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws IllegalArgumentException
    *    if <code>in == null</code>
     *
     * @throws ParseException
     *    if there was an I/O error, if the data on the stream is not valid XML
     *    or if the structure of the XML is not valid for the definition of a
     *    {@link CallRequest}.
     */
    public FunctionCaller parse(Reader in)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("in", in);
 
       final String MESSAGE = "Unable to parse XML returned by API.";
 
       try {
          return parse(_xmlBuilder.build(in));
       } catch (IOException ioException) {
          LOG.error(MESSAGE, ioException);
          // TODO: Include type of error in here somewhere
          throw new ParseException(MESSAGE, ioException);
       } catch (JDOMException jdomException) {
          LOG.error(MESSAGE, jdomException);
          // TODO: Include type of error in here somewhere
          throw new ParseException(MESSAGE, jdomException);
       }
    }
 
    /**
     * Parses the given XML document to create a <code>FunctionCaller</code>
     * object.
     *
     * @param document
     *    the document to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>document == null || document.getRootElement() == null</code>
     *
     * @throws ParseException
     *    if the specified XML document is not valid as the definition of a
     *    {@link FunctionCaller}.
     */
    private FunctionCaller parse(Document document)
    throws NullPointerException, ParseException {
       return parse(document.getRootElement());
    }
 
 
    /**
     * Parses the given XML element to create a <code>FunctionCaller</code>
     * object. This method calls either {@link #parseCallTargetGroup(Element)}
     * or {@link #parseActualFunctionCaller(Element)}, depending on the name of
     * the element.
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>
     *
     * @throws ParseException
     *    if the specified XML element is not valid as the definition of a
     *    {@link FunctionCaller}.
     */
    private FunctionCaller parse(Element element)
    throws NullPointerException, ParseException {
 
       String elementName = element.getName();
 
       // Check that the root element is either <group/> or <api/>
       if ("group".equals(elementName)) {
          return parseCallTargetGroup(element);
       } else if ("api".equals(elementName)) {
          return parseActualFunctionCaller(element);
       }
 
       String message = "The returned XML is invalid. The type of the root element is \"" + elementName + "\" instead of \"group\" or \"api\".";
       LOG.error(message);
       throw new ParseException(message);
    }
 
    /**
     * Parses the given XML element to create a <code>CallTargetGroup</code>
     * object.
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link CallTargetGroup}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>
     *
     * @throws ParseException
     *    if the specified XML element is not valid as the definition of a
     *    {@link CallTargetGroup}.
     */
    private CallTargetGroup parseCallTargetGroup(Element element)
    throws NullPointerException, ParseException {
 
       // Determine the type of the group
       String typeName = element.getAttributeValue("type");
       if (typeName == null) {
          throw new ParseException("The definition for a CallTargetGroup needs to specify the type in the attribute \"type\".");
       }
       CallTargetGroup.Type type = CallTargetGroup.getTypeByName(typeName);
       if (type == null) {
          throw new ParseException("The definition for a CallTargetGroup specifies \"" + typeName + "\" as the type but that is not an existing type.");
       }
 
       // Add all the members
       List members       = new ArrayList();
       List childElements = element.getChildren();
       int  childCount    = childElements.size();
       for (int i = 0; i < childCount; i++) {
          Element child = (Element) childElements.get(i);
          members.add(parse(child));
       }
 
       return CallTargetGroup.create(type, members);
    }
 
    /**
     * Parses the given XML element to create a
     * <code>ActualFunctionCaller</code> object.
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link ActualFunctionCaller}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>
     *
     * @throws ParseException
     *    if the specified XML element is not valid as the definition of a
     *    {@link ActualFunctionCaller}.
     */
    private ActualFunctionCaller parseActualFunctionCaller(Element element)
    throws NullPointerException, ParseException {
 
       // Determine the URL of the API
       String urlString = element.getAttributeValue("url");
       if (urlString == null) {
          throw new ParseException("The definition for an ActualFunctionCaller needs to specify the URL in the attribute \"url\".");
       }
 
       // Create a URL object
       URL url;
       try {
          url = new URL(urlString);
       } catch (MalformedURLException mue) {
          throw new ParseException("The URL specified for ActualFunctionCaller is invalid: \"" + urlString + "\".");
       }
 
       try {
          return new ActualFunctionCaller(url);
       } catch (SecurityException exception) {
          throw new ParseException("DNS lookup disallowed of ActualFunctionCaller URL \"" + urlString + "\". Message: \"" + exception.getMessage() + '"');
       } catch (UnknownHostException exception) {
          throw new ParseException("Unknown host specified in ActualFunctionCaller URL \"" + urlString + "\".");
       } catch (MultipleIPAddressesException exception) {
          throw new ParseException("Multiple IP addresses found for host specified in ActualFunctionCaller URL \"" + urlString + "\".");
       }
    }
 }
