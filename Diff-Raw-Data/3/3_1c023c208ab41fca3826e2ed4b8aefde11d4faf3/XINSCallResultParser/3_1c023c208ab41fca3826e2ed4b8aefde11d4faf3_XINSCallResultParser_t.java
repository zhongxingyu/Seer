 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.ProtectedPropertyReader;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 
 /**
  * XINS call result parser. XML is parsed to produce a {@link XINSCallResult}
  * object.
  *
  * <p>The root element in the XML must be of type <code>result</code>. Inside
  * this element, <code>param</code> elements optionally define parameters and
  * an optional <code>data</code> element defines a data section.
  *
  * <p>If the result element contains an <code>errorcode</code> or a
  * <code>code</code> attribute, then the value of the attribute is interpreted
  * as the error code. If both these attributes are set and conflicting, then
  * this is considered a showstopper.
  *
  * <p>TODO: Describe rest of parse process.
  *
  * <p>Note: This parser does not support XML Namespaces.
  *
  * @version $Revision$ $Date$
  *
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public class XINSCallResultParser
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class. This field is not <code>null</code>.
     */
    private static final String CLASSNAME = XINSCallResultParser.class.getName();
 
    /**
     * Fully-qualified name of the inner class <code>Handler</code>. This field
     * is not <code>null</code>.
     */
    private static final String HANDLER_CLASSNAME = XINSCallResultParser.Handler.class.getName();
 
    /**
     * Constant for an <code>Integer</code> object representing the number
     * zero. This field is not <code>null</code>.
     */
    private static final Integer ZERO = new Integer(0);
 
    /**
     * The key for the <code>ProtectedPropertyReader</code> instances created
     * by this class.
     */
    private static final Object PROTECTION_KEY = new Object();
 
    /**
     * Initial state for the SAX event handler, before the root element is
     * processed.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * State for the SAX event handler for the level just within the root
     * element (<code>result</code>).
     */
    private static final State AT_ROOT_LEVEL = new State("AT_ROOT_LEVEL");
 
    /**
     * State for the SAX event handler for the level within the output
     * parameter element (<code>param</code>).
     */
    private static final State IN_PARAM_ELEMENT = new State("IN_PARAM_ELEMENT");
 
    /**
     * State for the SAX event handler for the level within the data section
     * (within the <code>data</code> element).
     */
    private static final State IN_DATA_SECTION = new State("IN_DATA_SECTION");
 
    /**
     * State for the SAX event handler for the final state, when parsing is
     * effectively done.
     */
    private static final State FINISHED = new State("FINISHED");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSCallResultParser</code>.
     */
    public XINSCallResultParser() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Parses the given XML string to create a <code>XINSCallResultData</code>
     * object.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    the parsed result of the call, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>.
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if it is not a valid XINS
     *    API function call result.
     */
    public XINSCallResultData parse(byte[] xml)
    throws IllegalArgumentException, ParseException {
 
       // TRACE: Enter method
       Log.log_2003(CLASSNAME, "parse(byte[])", null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       // Initialize a SAX event handler
       Handler handler = new Handler();
 
       try {
 
          // Construct a SAX parser
          SAXParserFactory factory   = SAXParserFactory.newInstance();
          SAXParser        saxParser = factory.newSAXParser();
 
          // TODO: Enable namespace processing and use localName instead of
          //       qName in the startElement and endElement methods
 
          // Convert the byte array to an input stream
          ByteArrayInputStream bais = new ByteArrayInputStream(xml);
 
          // Let SAX parse the XML, using our handler
          saxParser.parse(bais, handler);
 
          // Dispose the constructed input stream
          bais.close();
 
       } catch (Throwable exception) {
 
          // Log: Parsing failed
          String detail = exception.getMessage();
          Log.log_2205(exception, detail);
 
          // Construct a buffer for the error message
          FastStringBuffer buffer = new FastStringBuffer(142, "Unable to convert the specified character string to XML");
 
          // Include the exception message in our error message, if any
          if (detail != null && detail.length() > 0) {
             buffer.append(": ");
             buffer.append(detail);
          } else {
             buffer.append('.');
          }
 
          // Throw exception with message, and register cause exception
          throw new ParseException(buffer.toString(), exception);
       }
 
       // TRACE: Leave method
       Log.log_2005(CLASSNAME, "parse(byte[])", null);
 
       return handler;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * SAX event handler that will parse the result from a call to a XINS
     * service.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private class Handler
    extends DefaultHandler
    implements XINSCallResultData {
 
       //-------------------------------------------------------------------------
       // Constructors
       //-------------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Handler</code> instance.
        */
       private Handler() {
          _state = INITIAL;
       }
 
 
       //-------------------------------------------------------------------------
       // Fields
       //-------------------------------------------------------------------------
 
       /**
        * The current state. Never <code>null</code>.
        */
       private State _state;
 
       /**
        * The error code returned by the function or <code>null</code>, if no
        * error code is returned.
        */
       private String _errorCode;
 
       /**
        * The list of the parameters (name/value) returned by the function.
        * This field is lazily initialized.
        */
       private ProtectedPropertyReader _parameters;
 
       /**
        * The name of the output parameter that is currently being parsed.
        */
       private String _parameterName;
 
       /**
        * The PCDATA content of the element currently being parsed.
        */
       private FastStringBuffer _pcdata;
 
       /**
        * The child elements of the data element that is currently being
        * parsed.
        */
       private Map _elements;
 
       /**
        * The level of the element that is currently being parsed in the data
        * section. Initially this field is set to -1, which means that no
        * element is parsed; 0 means that the parser just read the &lt;data&gt;
        * tag; 1 means that the parser entered in an direct sub-element of the
        * &lt;data&gt; tag, etc.
        */
       private int _level = -1;
 
 
       //-------------------------------------------------------------------------
       // Methods
       //-------------------------------------------------------------------------
 
       /**
        * Receive notification of the beginning of an element.
        *
        * @param namespaceURI
        *    the namespace URI, can be <code>null</code>.
        *
        * @param localName
        *    the local name (without prefix); the empty string indicates that
        *    namespace processing is not being performed; can be
        *    <code>null</code>.
        *
        * @param qName
        *    the qualified name (with prefix); the empty string indicates that
        *    qualified names are not available; cannot be <code>null</code>.
        *
        * @param atts
        *    the attributes attached to the element; if there are no
        *    attributes, it shall be an empty {@link Attributes} object; cannot
        *    be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>qName == null || atts == null</code>.
        *
        * @throws SAXException
        *    if the element is unknown.
        */
       public void startElement(String     namespaceURI,
                                String     localName,
                                String     qName,
                                Attributes atts)
       throws IllegalArgumentException, SAXException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("qName", qName, "atts", atts);
 
          // TODO: Handle namespaces properly
 
          // Root element must be 'result'
          if (_state == INITIAL) {
             if (! qName.equals("result")) {
                Log.log_2200(qName);
                throw new SAXException("Root element is \"" + qName + "\" while only \"result\" is supported.");
             }
 
             // Get the 'errorcode' and 'code attributes
             String code1 = atts.getValue("errorcode");
             String code2 = atts.getValue("code");
 
             // Only one error code attribute set
             if (code1 != null && code2 == null) {
                _errorCode = code1;
             } else if (code1 == null && code2 != null) {
                _errorCode = code2;
 
             // Two error code attribute set
             } else if (code1 == null && code2 == null) {
                _errorCode = null;
             } else if (code1.equals(code2)) {
                _errorCode = code1;
 
             // Conflicting error codes
             } else {
                // NOTE: This will be logged already (message 2205)
                throw new SAXException("Found conflicting duplicate value for error code. First is \"" + code1 + "\". Second is \"" + code2 + "\".");
             }
 
             // Change state
             _state = AT_ROOT_LEVEL;
 
          // Start of data section
          } else if (_state == AT_ROOT_LEVEL) {
 
             // Output parameter
             if (qName.equals("param")) {
 
                // Store the name of the parameter. It may be null, but that will
                // be checked only after the element end tag is processed.
                _parameterName = atts.getValue("name");
 
                // Reserve a buffer for the PCDATA content already
                _pcdata = new FastStringBuffer(20);
 
                // Update the state
                _state = IN_PARAM_ELEMENT;
 
             // Start of data section
             } else if (qName.equals("data")) {
 
                // Maintain a list of the elements, with data as the root
                _elements = new HashMap();
                _elements.put(ZERO, new DataElement("data"));
                _level = 0;
 
                // Update the state
                _state = IN_DATA_SECTION;
 
             // No 'result' element allowed within 'result' element
             } else if (qName.equals("result")) {
                // NOTE: This will be logged already (message 2205)
                throw new SAXException("Found \"result\" element within \"result\" element.");
 
             // Unknown elements at the root level are okay
             } else {
                // Ignore unrecognized element
                Log.log_2206(qName);
             }
 
          // Within output parameter element
          } else if (_state == IN_PARAM_ELEMENT) {
             // NOTE: This will be logged already (message 2205)
             throw new SAXException("Found \"" + qName + "\" element within \"param\" element.");
 
          // Within the data section
          } else if (_state == IN_DATA_SECTION) {
 
             // Increase the depth level
             _level++;
 
             // Construct a DataElement
             DataElement element = new DataElement(qName);
 
             // Add all attributes
             for (int i = 0; i < atts.getLength(); i++) {
                String key = atts.getQName(i);
                String value = atts.getValue(i);
                element.addAttribute(key, value);
             }
             _elements.put(new Integer(_level), element);
 
             // Reserve buffer for PCDATA
             _pcdata = new FastStringBuffer(20);
 
          // Unrecognized state
          } else {
             String detail = "Unrecognized state: " + _state + '.';
             Log.log_2050(HANDLER_CLASSNAME, "startElement(String,String,String,Attributes)", detail);
             throw new Error(detail);
          }
       }
 
       /**
        * Receive notification of the end of an element.
        *
        * @param namespaceURI
        *    the namespace URI, can be <code>null</code>.
        *
        * @param localName
        *    the local name (without prefix); the empty string indicates that
        *    namespace processing is not being performed; can be
        *    <code>null</code>.
        *
        * @param qName
        *    the qualified name (with prefix); the empty string indicates that
        *    qualified names are not available; cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>qName == null</code>.
        *
        * @throws SAXException
        *    if the element is unknown.
        */
       public void endElement(String namespaceURI,
                              String localName,
                              String qName)
       throws IllegalArgumentException, SAXException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("qName", qName);
 
          // At root level
          if (_state == AT_ROOT_LEVEL) {
 
             if ("result".equals(qName)) {
                _state = FINISHED;
             } else {
                // ignorable element
             }
 
          // Within data section
          } else if (_state == IN_DATA_SECTION) {
 
             // Get the DataElement for which we process the end tag
             DataElement child = (DataElement) _elements.get(new Integer(_level));
 
             // If at the <data/> element level, then return to AT_ROOT_LEVEL
             if (_level == 0) {
                if (! qName.equals("data")) {
                   String detail = "Expected element name \"param\" instead of \"" + qName + "\".";
                   Log.log_2050(HANDLER_CLASSNAME, "endElement(String,String,String)", detail);
                   throw new Error(detail);
                }
 
                // Reset the state
                _level = -1;
                _state = AT_ROOT_LEVEL;
 
             // Otherwise it's a custom element
             } else {
 
                // Set the PCDATA content on the element
                if (_pcdata != null && _pcdata.getLength() > 0) {
                   child.setText(_pcdata.toString());
                }
 
                // Reset the PCDATA content and the level
                _pcdata = null;
                _level--;
 
                // Add the child to the parent
                DataElement parent = (DataElement) _elements.get(new Integer(_level));
                parent.addChild(child);
             }
 
          // Output parameter
          } else if (_state == IN_PARAM_ELEMENT) {
 
             if (! qName.equals("param")) {
                String detail = "Expected element name \"param\" instead of \"" + qName + "\".";
                Log.log_2050(HANDLER_CLASSNAME, "endElement(String,String,String)", detail);
                throw new Error(detail);
             }
 
             // Retrieve name and value for output parameter
             String name  = _parameterName;
             String value = _pcdata.toString();
 
             // Both name and value should be set
             boolean noName  = (name  == null || name.length()  < 1);
             boolean noValue = (value == null || value.length() < 1);
             if (noName && noValue) {
                Log.log_2201();
             } else if (noName) {
                Log.log_2202(value);
             } else if (noValue) {
                Log.log_2203(name);
 
             // Name and value are both set, correctly
             } else {
                Log.log_2204(name, value);
 
                // Previously no parameters, perform (lazy) initialization
                if (_parameters == null) {
                   _parameters = new ProtectedPropertyReader(PROTECTION_KEY);
 
                // Check if parameter is already set
                } else {
                   String existingValue = _parameters.get(name);
                   if (existingValue != null) {
                      if (!existingValue.equals(value)) {
                         // NOTE: This will be logged already (message 2205)
                         throw new SAXException("Found conflicting duplicate value for output parameter \"" + name + "\". Initial value is \"" + existingValue + "\". New value is \"" + value + "\".");
                      }
                   }
                }
 
                // Store the name-value combination for the output parameter
                _parameters.set(PROTECTION_KEY, name, value);
             }
 
             // Reset the state
             _parameterName = null;
             _pcdata       = null;
             _state        = AT_ROOT_LEVEL;
 
          // Unknown state
          } else {
             String detail = "Unrecognized state: " + _state + '.';
             Log.log_2050(HANDLER_CLASSNAME, "endElement(String,String,String)", detail);
             throw new Error(detail);
          }
       }
 
       /**
        * Receive notification of character data.
        *
        * @param ch
        *    the <code>char</code> array that contains the characters from the
        *    XML document, cannot be <code>null</code>.
        *
        * @param start
        *    the start index within <code>ch</code>.
        *
        * @param length
        *    the number of characters to take from <code>ch</code>.
        *
       * @throws IllegalStateException
       *    if the current state does not allow character data.
       *
        * @throws IndexOutOfBoundsException
        *    if characters outside the allowed range are specified.
        */
       public void characters(char[] ch, int start, int length)
       throws IllegalStateException, IndexOutOfBoundsException {
 
          // Check state
          if (_state != IN_PARAM_ELEMENT && _state != IN_DATA_SECTION) {
             // NOTE: This will be logged already (message 2205)
             throw new IllegalStateException("Found PCDATA content in state " + _state + '.');
          }
 
          if (_pcdata != null) {
             _pcdata.append(ch, start, length);
          }
       }
 
       /**
        * Checks if the state is <code>FINISHED</code> and if not throws an
        * <code>IllegalStateException</code>.
        *
        * @throws IllegalStateException
        *    if the current state is not {@link #FINISHED}.
        */
       private void assertFinished()
       throws IllegalStateException {
          if (_state != FINISHED) {
             String detail = "State is " + _state + " instead of " + FINISHED + '.';
             Log.log_2050(HANDLER_CLASSNAME, "getDataElement()", detail);
             throw new IllegalStateException(detail);
          }
       }
 
       /**
        * Gets the error code returned by the function if any.
        *
        * @return
        *    the error code returned by the function or <code>null<code>
        *    if no error code has been returned from the function.
        *
        * @throws IllegalStateException
        *    if the current state is invalid.
        */
       public String getErrorCode()
       throws IllegalStateException {
 
          // Check state
          assertFinished();
 
          return _errorCode;
       }
 
       /**
        * Get the parameters returned by the function.
        *
        * @return
        *    the parameters (name/value) or <code>null</code> if the function
        *    does not have any parameters.
        *
        * @throws IllegalStateException
        *    if the current state is invalid.
        */
       public PropertyReader getParameters()
       throws IllegalStateException {
 
          // Check state
          assertFinished();
 
          return _parameters;
       }
 
       /**
        * Get the data element returned by the function if any.
        *
        * @return
        *    the data element, or <code>null</code> if the function did not
        *    return any data element.
        *
        * @throws IllegalStateException
        *    if the current state is invalid.
        */
       public DataElement getDataElement()
       throws IllegalStateException {
 
          // Check state
          assertFinished();
 
          if (_elements == null) {
             return null;
          } else {
             return (DataElement) _elements.get(ZERO);
          }
       }
    }
 
    /**
     * State of the event handler.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private static final class State extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>State</code> object.
        *
        * @param name
        *    the name of this state, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       private State(String name) throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          _name = name;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The name of this state. Cannot be <code>null</code>.
        */
       private final String _name;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the name of this state.
        *
        * @return
        *    the name of this state, cannot be <code>null</code>.
        */
       public String getName() {
          return _name;
       }
 
       /**
        * Returns a textual representation of this object.
        *
        * @return
        *    the name of this state, never <code>null</code>.
        */
       public String toString() {
          return _name;
       }
    }
 }
