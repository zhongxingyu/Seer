 /*
  * $Id$
  */
 package org.xins.server;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.ProtectedPropertyReader;
 
 /**
  * Builder for a call result. The result is built as the function call is
  * processed. The building must be done in a predefined order.
  *
  * <p />Initially the state is {@link #BEFORE_START}. The state is
  * changed by calls to the various modification methods.
  *
  * <p />The following table defines what the state transitions are when one of
  * the modification methods is called in a certain state. Horizontally are the
  * current states, vertically the output methods. The cells self contain the
  * new state.
  *
  * <p /><table class="states">
  *    <tr>
  *       <th></th>
  *       <th><acronym title="BEFORE_START">S0</acronym></th>
  *       <th><acronym title="WITHIN_PARAMS">S1</acronym></th>
  *       <th><acronym title="START_TAG_OPEN">S2</acronym></th>
  *       <th><acronym title="WITHIN_ELEMENT">S3</acronym></th>
  *       <th><acronym title="AFTER_END">S4</acronym></th>
  *    </tr>
  *    <tr>
 *       <th>{@link #startResponse(boolean,String)}</th>
  *       <td><acronym title="WITHIN_PARAMS">S1</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #param(String,String)}</th>
  *       <td><acronym title="WITHIN_PARAMS">S1</acronym></td>
  *       <td class="nochange"><acronym title="WITHIN_PARAMS">S1</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #startTag(String)}</th>
  *       <td><acronym title="START_TAG_OPEN">S2</acronym></td>
  *       <td><acronym title="START_TAG_OPEN">S2</acronym></td>
  *       <td class="nochange"><acronym title="START_TAG_OPEN">S2</acronym></td>
  *       <td><acronym title="START_TAG_OPEN">S2</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #attribute(String,String)}</th>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="nochange"><acronym title="START_TAG_OPEN">S2</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #pcdata(String)}</th>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td><acronym title="WITHIN_ELEMENT">S3</acronym></td>
  *       <td class="nochange"><acronym title="WITHIN_ELEMENT">S3</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #endTag()}</th>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *       <td><acronym title="WITHIN_ELEMENT">S3</acronym></acronym></td>
  *       <td class="nochange"><acronym title="WITHIN_ELEMENT">S3</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  *    <tr>
  *       <th>{@link #endResponse()}</th>
  *       <td><acronym title="AFTER_END">S4</acronym></td>
  *       <td><acronym title="AFTER_END">S4</acronym></td>
  *       <td><acronym title="AFTER_END">S4</acronym></td>
  *       <td><acronym title="AFTER_END">S4</acronym></td>
  *       <td class="err"><acronym title="IllegalStateException">ISE</acronym></td>
  *    </tr>
  * </table>
  *
  * <p />List of states as used in the table:
  *
  * <ul>
  *    <li>S0: BEFORE_START</li>
  *    <li>S1: WITHIN_PARAMS</li>
  *    <li>S2: START_TAG_OPEN</li>
  *    <li>S3: WITHIN_ELEMENT</li>
  *    <li>S4: AFTER_END</li>
  * </ul>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.119
  */
 final class CallResultBuilder extends Object implements CallResult {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Constant identifying the initial state.
     */
    private static final State BEFORE_START = new State("BEFORE_START");
 
    /**
     * Constant identifying the state in which parameters values can be set.
     */
    private static final State WITHIN_PARAMS = new State("WITHIN_PARAMS");
 
    /**
     * Constant identifying the state in which a start tag in the data section
     * is open.
     */
    private static final State START_TAG_OPEN = new State("START_TAG_OPEN");
 
    /**
     * Constant identifying the state in which a start tag is finished but the
     * end tag is not.
     */
    private static final State WITHIN_ELEMENT = new State("WITHIN_ELEMENT");
 
    /**
     * Constant identifying the state in which nothing more can be added or
     * changed.
     */
    private static final State AFTER_END = new State("AFTER_END");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallResultBuilder</code> object.
     */
    CallResultBuilder() {
       _state = BEFORE_START;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Current state.
     */
    private State _state;
 
    /**
     * The result code. This field is <code>null</code> if no code was
     * returned.
     */
    private String _code;
 
    /**
     * The parameters and their values. This field is never <code>null</code>.
     * If there are no parameters, then this field is <code>null</code>.
     */
    private ProtectedPropertyReader _parameters;
 
    /**
     * The data element. This field is <code>null</code> if there is no data
     * element.
     */
    private Element _dataElement;
 
    /**
     * The current element. This field is <code>null</code> if there is no
     * current element. It can <em>never</em> be <code>null</code> if
     * {@link #_state}<code> == </code>{@link #START_TAG_OPEN}.
     */
    private Element _currentElement;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the current state.
     *
     * @return
     *    the current state, never <code>null</code>.
     */
    State getState() {
       return _state;
    }
 
    /**
     * Sets the success indication and the result code (if any).
     *
     * @param code
     *    the result code, or <code>null</code>.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #BEFORE_START}.
     */
    void startResponse(String code)
    throws IllegalStateException {
 
       // TODO: Accept ResultCode object ?
       // TODO: If so, add to the state table
 
       // Check preconditions
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + " instead of " + BEFORE_START + '.');
       }
 
       // Store the information
       _code    = code;
 
       // Update the state
       _state = WITHIN_PARAMS;
    }
 
    /**
     * Returns the success indication.
     *
     * @return
     *    success indication, <code>true</code> or <code>false</code>.
     */
    public boolean isSuccess() {
       return _code == null;
    }
 
    /**
     * Returns the result code.
     *
     * @return
     *    the result code or <code>null</code> if no code was returned.
     */
    public String getErrorCode() {
       return _code;
    }
 
    /**
     * Adds an output parameter to the result. The name and the value must
     * both be specified. The state will be changed to {@link #WITHIN_PARAMS}.
     *
     * @param name
     *    the name of the output parameter, not <code>null</code> and not an
     *    empty string.
     *
     * @param value
     *    the value of the output parameter, not <code>null</code> and not an
     *    empty string.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #BEFORE_START} &amp;&amp; {@link #getState()}<code> != </code>{@link #WITHIN_PARAMS}.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || value == null || "".equals(name) || "".equals(value)</code>.
     *
     * @throws InvalidResponseException
     *    if the response is considered invalid.
     */
    void param(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state != BEFORE_START && _state != WITHIN_PARAMS) {
          throw new IllegalStateException("The state is " + _state + " instead of either " + BEFORE_START + " or " + WITHIN_PARAMS + '.');
       }
 
       // TODO: Check that parameter does not exist yet
       // TODO: Check that parameter is known
       // TODO: Check that parameter is valid for type
 
       // Initialize the _parameters field
       if (_parameters == null) {
          _parameters = new ProtectedPropertyReader(START_TAG_OPEN);
          // NOTE: The START_TAG_OPEN field is picked as the key to be used,
          //       since it's static, private, final and not null.
       }
 
       _parameters.set(START_TAG_OPEN, name, value);
 
       _state = WITHIN_PARAMS;
    }
 
    /**
     * Gets all parameters.
     *
     * @return
     *    a {@link PropertyReader} containing all parameters, or
     *    <code>null</code> if no parameters are set; the keys will be the
     *    names of the parameters ({@link String} objects, cannot be
     *    <code>null</code>), the values will be the parameter values
     *    ({@link String} objects as well, cannot be <code>null</code>).
     */
    public PropertyReader getParameters() {
       return _parameters;
    }
 
    /**
     * Gets the value of the specified parameter.
     *
     * @param name
     *    the parameter element name, not <code>null</code>.
     *
     * @return
     *    string containing the value of the parameter element,
     *    not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       // The set of parameters is lazily initialized, recognize this
       if (_parameters == null) {
          return null;
       }
 
       return _parameters.get(name);
    }
 
    /**
     * Writes a start tag within the data section.
     *
     * @param type
     *    the element type, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> == </code>{@link #AFTER_END}.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null</code>.
     */
    void startTag(String type)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       if (_state == AFTER_END) {
          throw new IllegalStateException("The state is " + AFTER_END + '.');
       }
 
       // Create the XML element
       Element e = new Element(type);
 
       // If there is no data section yet, create one
       if (_dataElement == null) {
          _dataElement = new Element("data");
          _dataElement.add(e);
       } else {
          _currentElement.add(e);
       }
 
       _currentElement = e;
 
       // Update the state
       _state = START_TAG_OPEN;
    }
 
    /**
     * Writes an attribute within the current element. The state needs to be
     * {@link #START_TAG_OPEN} and will not be changed.
     *
     * @param name
     *    the name of the attribute, not <code>null</code> and not an empty
     *    string.
     *
     * @param value
     *    the value for the attribute, not <code>null</code> and not an empty
     *    string.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #START_TAG_OPEN}.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null
     *          || value == null
     *          || "".{@link String#equals(Object) equals}(name)
     *          || "".{@link String#equals(Object) equals}(value)</code>.
     */
    void attribute(String name, String value)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       if (_state != START_TAG_OPEN) {
          throw new IllegalStateException("The state is " + _state + " instead of " + START_TAG_OPEN + '.');
       }
 
       // NOTE: If the state is START_TAG_OPEN then _currentElement is not null
 
       // Store the attribute
       _currentElement.addAttribute(name, value);
    }
 
    /**
     * Writes parsed character data.
     *
     * @param text
     *    the text to be written, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #START_TAG_OPEN}<code> &amp;&amp; </code>{@link #getState()}<code> != </code>{@link #WITHIN_ELEMENT}.
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     */
    void pcdata(String text)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + " instead of either " + START_TAG_OPEN + " or " + WITHIN_ELEMENT + '.');
       }
 
       // Check arguments
       MandatoryArgumentChecker.check("text", text);
 
       // Add the PCDATA as content to the current element
       _currentElement.add(text);
 
       // Update the state
       _state = WITHIN_ELEMENT;
    }
 
    /**
     * Ends the current element.
     *
     * <p>This changes the state to {@link #WITHIN_ELEMENT}.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #START_TAG_OPEN}<code> &amp;&amp; </code>{@link #getState()}<code> != </code>{@link #WITHIN_ELEMENT}.
     */
    void endTag()
    throws IllegalStateException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + " instead of either " + START_TAG_OPEN + " or " + WITHIN_ELEMENT + '.');
       }
 
       // TODO: Disallow closing of data section!
 
       _currentElement = _currentElement.getParent();
 
       // Update the state
       _state = WITHIN_ELEMENT;
    }
 
    /**
     * Returns the optional extra data. The data is an XML {@link Element}, or
     * <code>null</code>.
     *
     * @return
     *    the extra data as an XML {@link Element}, can be <code>null</code>;
     *    if it is not <code>null</code>, then
     *    <code><em>return</em>.{@link Element#getType() getType()}.equals("data")</code>.
     */
    public Element getDataElement() {
       return _dataElement;
    }
 
    /**
     * Ends the result. This will change the state to {@link #AFTER_END}.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> == </code>{@link #AFTER_END}.
     */
    void endResponse()
    throws IllegalStateException {
 
       // Check state
       if (_state == AFTER_END) {
          throw new IllegalStateException("The state is " + _state + " instead of " + AFTER_END + '.');
       }
       _state = AFTER_END;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * State of the call result builder.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     */
    static final class State extends Object {
 
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
       String getName() {
          return _name;
       }
 
       public String toString() {
          return _name;
       }
    }
 }
