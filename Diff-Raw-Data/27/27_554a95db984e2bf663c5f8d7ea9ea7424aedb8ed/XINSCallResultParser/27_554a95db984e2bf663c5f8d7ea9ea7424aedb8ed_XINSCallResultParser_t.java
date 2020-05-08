 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import java.util.Stack;
 
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.ProtectedPropertyReader;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 
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
  * <p>Note: This parser is
  * <a href="http://www.w3.org/TR/REC-xml-names/">XML Namespaces</a>-aware.
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
     * The key for the <code>ProtectedPropertyReader</code> instances created
     * by this class.
     */
    private static final Object PROTECTION_KEY = new Object();
 
    /**
     * Error state for the SAX event handler.
     */
    private static final State ERROR = new State("ERROR");
 
    /**
     * Initial state for the SAX event handler, before the root element is
     * processed.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * State for the SAX event handler just within the root element
     * (<code>result</code>).
     */
    private static final State AT_ROOT_LEVEL = new State("AT_ROOT_LEVEL");
 
    /**
     * State for the SAX event handler at any depth within an ignorable
     * element.
     */
    private static final State IN_IGNORABLE_ELEMENT = new State("IN_IGNORABLE_ELEMENT");
 
    /**
     * State for the SAX event handler within the output parameter element
     * (<code>param</code>).
     */
    private static final State IN_PARAM_ELEMENT = new State("IN_PARAM_ELEMENT");
 
    /**
     * State for the SAX event handler in the data section (at any depth within
     * the <code>data</code> element).
     */
    private static final State IN_DATA_SECTION = new State("IN_DATA_SECTION");
 
    /**
     * State for the SAX event handler for the final state, when parsing is
     * finished.
     */
    private static final State FINISHED = new State("FINISHED");
 
    /**
     * The factory for SAX parsers. This field is never <code>null</code>, it
     * is initialized by a class initializer.
     */
    private static final SAXParserFactory SAX_PARSER_FACTORY;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class.
     */
    static {
       SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
       SAX_PARSER_FACTORY.setNamespaceAware(true);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSCallResultParser</code>.
     */
    public XINSCallResultParser() {
 
       // TRACE: Enter constructor
       org.xins.common.Log.log_1000(CLASSNAME, null);
 
       // empty
 
       // TRACE: Leave constructor
       org.xins.common.Log.log_1002(CLASSNAME, null);
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
 
       final String THIS_METHOD = "parse(byte[])";
 
       // TRACE: Enter method
       org.xins.common.Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       // Initialize our SAX event handler
       Handler handler = new Handler();
 
       ByteArrayInputStream stream = null;
       try {
 
          // Construct a SAX parser
          SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
 
          // Convert the byte array to an input stream
          stream = new ByteArrayInputStream(xml);
 
          // Let SAX parse the XML, using our handler
          saxParser.parse(stream, handler);
 
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
          throw new ParseException(buffer.toString(), exception, detail);
 
       // Always dispose the ByteArrayInputStream
       } finally {
          if (stream != null) {
             try {
                stream.close();
             } catch (IOException ioException) {
                final String SUBJECT_CLASS  = stream.getClass().getName();
                final String SUBJECT_METHOD = "close()";
                Utils.logProgrammingError(CLASSNAME,    THIS_METHOD,
                                         SUBJECT_CLASS, SUBJECT_METHOD,
                                         null,          ioException);
             }
          }
       }
 
       // TRACE: Leave method
       org.xins.common.Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
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
 
          // TRACE: Enter constructor
          org.xins.common.Log.log_1000(HANDLER_CLASSNAME, null);
 
          _state            = INITIAL;
          _level            = -1;
          _characters       = new FastStringBuffer(45);
          _dataElementStack = new Stack();
 
          // TRACE: Leave constructor
          org.xins.common.Log.log_1002(HANDLER_CLASSNAME, null);
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
       *
       * <p>The value will never return an empty string, so if the result is
       * not <code>null</code>, then it is safe to assume the length of the
       * string is at least 1 character.
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
        * The character content (CDATA or PCDATA) of the element currently
        * being parsed.
        */
       private final FastStringBuffer _characters;
 
       /**
        * The stack of child elements within the data section. The top element
        * is always <code>&lt;data/&gt;</code>.
        */
       private Stack _dataElementStack;
 
       /**
        * The level for the element pointer within the XML document. Initially
        * this field is <code>-1</code>, which indicates the current element
        * pointer is outside the document. The value <code>0</code> is for the
        * root element (<code>result</code>), etc.
        */
       private int _level;
 
 
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
        *    the local name (without prefix); cannot be <code>null</code>.
        *
        * @param qName
        *    the qualified name (with prefix), can be <code>null</code> since
        *    <code>namespaceURI</code> and <code>localName</code> are always
        *    used instead.
        *
        * @param atts
        *    the attributes attached to the element; if there are no
        *    attributes, it shall be an empty {@link Attributes} object; cannot
        *    be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>localName == null || atts == null</code>.
        *
        * @throws SAXException
        *    if the parsing failed.
        */
       public void startElement(String     namespaceURI,
                                String     localName,
                                String     qName,
                                Attributes atts)
       throws IllegalArgumentException, SAXException {
 
          final String THIS_METHOD = "startElement(java.lang.String,"
                                   + "java.lang.String,"
                                   + "java.lang.String,"
                                   + Attributes.class.getName()
                                   + ')';
 
          // Temporarily enter ERROR state, on success this state is left
          State currentState = _state;
          _state = ERROR;
 
          // Make sure namespaceURI is either null or non-empty
          namespaceURI = "".equals(namespaceURI) ? null : namespaceURI;
 
          // Cache quoted version of namespaceURI
          String quotedNamespaceURI = TextUtils.quote(namespaceURI);
 
          // TRACE: Enter method
          org.xins.common.Log.log_1003(HANDLER_CLASSNAME, THIS_METHOD,
                         "_state="       + currentState
                     + "; _level="       + _level
                     + "; namespaceURI=" + quotedNamespaceURI
                     + "; localName="    + TextUtils.quote(localName)
                     + "; qName="        + TextUtils.quote(qName));
 
          // Check preconditions
          MandatoryArgumentChecker.check("localName", localName, "atts", atts);
 
          // Increase the element depth level
          _level++;
 
          if (currentState == ERROR) {
             final String DETAIL = "_state=" + currentState + "; _level=" + _level;
             throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                             HANDLER_CLASSNAME, THIS_METHOD,
                                             DETAIL);
 
          } else if (currentState == INITIAL) {
 
             // Level and state must comply
             if (_level != 0) {
                final String DETAIL = "_state=" + currentState + "; _level=" + _level;
                throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                                HANDLER_CLASSNAME, THIS_METHOD,
                                                DETAIL);
             }
 
             // Root element must be 'result' without namespace
             if (! (namespaceURI == null && localName.equals("result"))) {
                Log.log_2200(namespaceURI, localName);
                final String DETAIL = "Root element is \""
                                    + localName
                                    + "\" with namespace "
                                    + quotedNamespaceURI
                                    + " instead of \"result\" with namespace (null).";
                throw new SAXException(DETAIL);
             }
 
             // Get the 'errorcode' and 'code attributes
             String code1 = atts.getValue("errorcode");
             String code2 = atts.getValue("code");
 
            // Convert an empty string to null
            if (code1 == null || code1.length() == 0) {
               code1 = null;
            }
            if (code2 == null || code2.length() == 0) {
               code2 = null;
            }

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
                // NOTE: No need to log here. This will be logged already (message 2205)
                throw new SAXException("Found conflicting duplicate value for error code, since errorcode=\"" + code1 + "\", while code=\"" + code2 + "\".");
             }
 
             // Change state
             _state = AT_ROOT_LEVEL;
 
          } else if (currentState == AT_ROOT_LEVEL) {
 
             // Output parameter
             if (namespaceURI == null && "param".equals(localName)) {
 
                // Store the name of the parameter. It may be null, but that will
                // be checked only after the element end tag is processed.
                _parameterName = atts.getValue("name");
 
                // TODO: Check parameter name here (null and pattern)
 
                // Reserve buffer for PCDATA
                _characters.clear();
 
                // Update the state
                _state = IN_PARAM_ELEMENT;
 
             // Start of data section
             } else if (namespaceURI == null && "data".equals(localName)) {
 
                // A data element stack should really be empty
                if (_dataElementStack.size() > 0) {
                   throw new SAXException("Found second data section.");
                }
 
                // Maintain a list of the elements, with data as the root
                _dataElementStack.push(new DataElement(null, "data"));
 
                // Update the state
                _state = IN_DATA_SECTION;
 
             // Ignore unrecognized element at root level
             } else {
                _state = IN_IGNORABLE_ELEMENT;
                Log.log_2206(namespaceURI, localName);
             }
 
          // Within output parameter element, no elements are allowed
          } else if (currentState == IN_PARAM_ELEMENT) {
             // NOTE: No need to log here. This will be logged already (message 2205)
             throw new SAXException("Found \"" + localName + "\" element with namespace " + quotedNamespaceURI + " within \"param\" element.");
 
          // Within the data section
          } else if (currentState == IN_DATA_SECTION) {
 
             // Construct a DataElement
             DataElement element = new DataElement(namespaceURI, localName);
 
             // Add all attributes
             for (int i = 0; i < atts.getLength(); i++) {
                String attrNamespaceURI = atts.getURI(i);
                String attrLocalName    = atts.getLocalName(i);
                String attrValue        = atts.getValue(i);
 
                element.setAttribute(attrNamespaceURI, attrLocalName, attrValue);
             }
 
             // Push the element on the stack
             _dataElementStack.push(element);
 
             // Reserve buffer for PCDATA
             _characters.clear();
 
             // Reset the state from ERROR back to IN_DATA_SECTION
             _state = IN_DATA_SECTION;
 
          // Deeper level within ignorable element
          } else if (currentState == IN_IGNORABLE_ELEMENT) {
             _state = IN_IGNORABLE_ELEMENT;
 
          // Unrecognized state
          } else {
             final String DETAIL = "_state=" + currentState + "; _level=" + _level;
             throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                             HANDLER_CLASSNAME, THIS_METHOD,
                                             DETAIL);
          }
 
          org.xins.common.Log.log_1005(HANDLER_CLASSNAME, THIS_METHOD,
                         "_state="       + _state
                     + "; _level="       + _level
                     + "; namespaceURI=" + TextUtils.quote(namespaceURI)
                     + "; localName="    + TextUtils.quote(localName)
                     + "; qName="        + TextUtils.quote(qName));
       }
 
       /**
        * Receive notification of the end of an element.
        *
        * @param namespaceURI
        *    the namespace URI, can be <code>null</code>.
        *
        * @param localName
        *    the local name (without prefix); cannot be <code>null</code>.
        *
        * @param qName
        *    the qualified name (with prefix), can be <code>null</code> since
        *    <code>namespaceURI</code> and <code>localName</code> are only
        *    used.
        *
        * @throws IllegalArgumentException
        *    if <code>localName == null</code>.
        *
        * @throws SAXException
        *    if the parsing failed.
        */
       public void endElement(String namespaceURI,
                              String localName,
                              String qName)
       throws IllegalArgumentException, SAXException {
 
          final String THIS_METHOD = "endElement(java.lang.String,"
                                   + "java.lang.String,"
                                   + "java.lang.String)";
 
          // Temporarily enter ERROR state, on success this state is left
          State currentState = _state;
          _state = ERROR;
 
          // Make sure namespaceURI is either null or non-empty
          namespaceURI = "".equals(namespaceURI) ? null : namespaceURI;
 
          // Cache quoted version of namespaceURI
          String quotedNamespaceURI = TextUtils.quote(namespaceURI);
 
          // TRACE: Enter method
          org.xins.common.Log.log_1003(HANDLER_CLASSNAME, THIS_METHOD,
                         "_state="       + currentState
                     + "; _level="       + _level
                     + "; namespaceURI=" + TextUtils.quote(namespaceURI)
                     + "; localName="    + TextUtils.quote(localName)
                     + "; qName="        + TextUtils.quote(qName));
 
          // Check preconditions
          MandatoryArgumentChecker.check("localName", localName);
 
          if (currentState == ERROR) {
             final String DETAIL = "_state=" + currentState + "; _level=" + _level;
             throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                             HANDLER_CLASSNAME, THIS_METHOD,
                                             DETAIL);
 
          // At root level
          } else if (currentState == AT_ROOT_LEVEL) {
 
             if (! (namespaceURI == null && "result".equals(localName))) {
                final String DETAIL = "Expected end of element of type \"result\" with namespace (null) instead of \""
                                    + localName
                                    + "\" with namespace "
                                    + quotedNamespaceURI
                                    + '.';
                throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                                HANDLER_CLASSNAME, THIS_METHOD,
                                                DETAIL);
             }
             _state = FINISHED;
 
          // Ignorable element
          } else if (currentState == IN_IGNORABLE_ELEMENT) {
             if (_level == 1) {
                _state = AT_ROOT_LEVEL;
             } else {
                _state = IN_IGNORABLE_ELEMENT;
             }
 
          // Within data section
          } else if (currentState == IN_DATA_SECTION) {
 
             // Get the DataElement for which we process the end tag
             DataElement child = (DataElement) _dataElementStack.pop();
 
             // If at the <data/> element level, then return to AT_ROOT_LEVEL
             if (_dataElementStack.size() == 0) {
                if (! (namespaceURI == null && "data".equals(localName))) {
                   final String DETAIL = "Expected end of element of type \"data\" with namespace (null) instead of \""
                                       + localName
                                       + "\" with namespace "
                                       + quotedNamespaceURI
                                       + '.';
                   throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                                   HANDLER_CLASSNAME, THIS_METHOD,
                                                   DETAIL);
                }
 
                // Push the root DataElement back
                _dataElementStack.push(child);
 
                // Reset the state
                _state = AT_ROOT_LEVEL;
 
             // Otherwise it's a custom element
             } else {
 
                // Set the PCDATA content on the element
                if (_characters != null && _characters.getLength() > 0) {
                   child.setText(_characters.toString());
                }
 
                // Add the child to the parent
                DataElement parent = (DataElement) _dataElementStack.peek();
                parent.addChild(child);
 
                // Reset the state back frmo ERROR to IN_DATA_SECTION
                _state = IN_DATA_SECTION;
             }
 
          // Output parameter
          } else if (currentState == IN_PARAM_ELEMENT) {
 
             if (! (namespaceURI == null && "param".equals(localName))) {
                final String DETAIL = "Expected end of element of type \"param\" with namespace (null) instead of \""
                                    + localName
                                    + "\" with namespace "
                                    + quotedNamespaceURI
                                    + '.';
                throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                                HANDLER_CLASSNAME, THIS_METHOD,
                                                DETAIL);
             }
 
             // Retrieve name and value for output parameter
             String name  = _parameterName;
             String value = _characters.toString();
 
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
                         final String DETAIL = "Found conflicting duplicate value for output parameter \""
                                             + name
                                             + "\". Initial value is \""
                                             + existingValue
                                             + "\". New value is \""
                                             + value +
                                             "\".";
                         throw new SAXException(DETAIL);
                      }
                   }
                }
 
                // Store the name-value combination for the output parameter
                _parameters.set(PROTECTION_KEY, name, value);
             }
 
             // Reset the state
             _parameterName = null;
             _state         = AT_ROOT_LEVEL;
             _characters.clear();
 
          // Unknown state
          } else {
             final String DETAIL = "Unrecognized state: "
                                 + currentState
                                 + ". Programming error suspected.";
             throw Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                             HANDLER_CLASSNAME, THIS_METHOD,
                                             DETAIL);
          }
 
          _level--;
          _characters.clear();
 
          // TRACE: Leave method
          org.xins.common.Log.log_1005(HANDLER_CLASSNAME, THIS_METHOD,
                         "_state="       + _state
                     + "; _level="       + _level
                     + "; namespaceURI=" + TextUtils.quote(namespaceURI)
                     + "; localName="    + TextUtils.quote(localName)
                     + "; qName="        + TextUtils.quote(qName));
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
        * @throws IndexOutOfBoundsException
        *    if characters outside the allowed range are specified.
        *
        * @throws SAXException
        *    if the parsing failed.
        */
       public void characters(char[] ch, int start, int length)
       throws IndexOutOfBoundsException, SAXException {
 
          final String THIS_METHOD = "characters(char[],int,int)";
 
          // Temporarily enter ERROR state, on success this state is left
          State currentState = _state;
          _state = ERROR;
 
          // TRACE: Enter method
          org.xins.common.Log.log_1003(HANDLER_CLASSNAME, THIS_METHOD, null);
 
          // Check state
          if (currentState != IN_PARAM_ELEMENT
           && currentState != IN_DATA_SECTION
           && currentState != IN_IGNORABLE_ELEMENT) {
             String text = new String(ch, start, length);
             if (text.trim().length() > 0) {
                // NOTE: This will be logged already (message 2205)
                throw new SAXException("Found character content \"" + text + "\" in state " + currentState + '.');
             }
          }
 
          if (_characters != null) {
             _characters.append(ch, start, length);
          }
 
          // Reset _state
          _state = currentState;
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
 
             // TODO: Should SUBJECT_METHOD not be something else?
             final String THIS_METHOD    = "assertFinished()";
             final String SUBJECT_METHOD = Utils.getCallingMethod();
             final String DETAIL = "State is "
                                 + _state
                                 + " instead of "
                                 + FINISHED
                                 + '.';
             Utils.logProgrammingError(HANDLER_CLASSNAME, THIS_METHOD,
                                       HANDLER_CLASSNAME, SUBJECT_METHOD,
                                       DETAIL);
             throw new IllegalStateException(DETAIL);
          }
       }
 
       /**
       * Returns the error code. If <code>null</code> is returned the call was
       * successful and thus no error code was returned. Otherwise the call
       * was unsuccessful.
       *
       * <p>This method will never return an empty string, so if the result is
       * not <code>null</code>, then it is safe to assume the length of the
       * string is at least 1 character.
        *
        * @return
       *    the returned error code, or <code>null</code> if the call was
       *    successful.
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
 
          if (_dataElementStack.isEmpty()) {
             return null;
          } else {
             return (DataElement) _dataElementStack.peek();
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
