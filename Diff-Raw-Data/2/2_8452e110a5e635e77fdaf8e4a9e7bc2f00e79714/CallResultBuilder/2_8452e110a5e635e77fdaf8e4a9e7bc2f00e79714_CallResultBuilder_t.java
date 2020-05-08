 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Collections;
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.CollectionUtils;
 
 /**
  * Builder for a call result. The result is built as the function call is
  * processed. The building must be done in a predefined order.
  *
  * <ol>
  *    <li>set the XSLT URL</li>
  *    <li>start the result</li>
  *    <li>set parameter values</li>
  *    <li>populate the data section</li>
  * </ol>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.119
  */
 final class CallResultBuilder extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private static final Logger LOG = Logger.getLogger(CallResultBuilder.class.getName());
 
    /**
     * Constant identifying the initial state.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * Constant identifying the state in which parameters values can be set.
     */
    private static final State DEFINE_PARAMETERS = new State("DEFINE_PARAMETERS");
 
    /**
     * Constant identifying the state in which nothing more can be added or
     * changed.
     */
    private static final State COMPLETED = new State("COMPLETED");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallResultBuilder</code> object.
     */
    public CallResultBuilder() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Current state.
     */
    private State _state;
 
    /**
     * The URL of the XSLT to link to.
     */
    private String _xslt;
 
    /**
     * Success indication.
     */
    private boolean _success;
 
    /**
     * The result code. This field is <code>null</code> if no code was
     * returned.
     */
    private String _code;
 
    /**
     * The parameters and their values. This field is never <code>null</code>.
     * If there are no parameters, then this field is <code>null</code>.
     */
    private Map _parameters;
 
    /**
     * The data element. This field is <code>null</code> if there is no data
     * element.
     */
    private Element _dataElement;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the current state.
     *
     * @return
     *    the current state, never <code>null</code>.
     */
    public State getState() {
       return _state;
    }
 
    /**
     * Sets the XSLT to link to. The state needs to be {@link #INITIAL}.
     * Calling this method will not change the state.
     *
     * @param url
     *    the URL of the XSLT to link to, or <code>null</code> if none.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #INITIAL}.
     */
    public void setXSLT(String url)
    throws IllegalStateException {
 
       // TODO: Only allow this method to be called once
 
       // Check preconditions
       if (_state != INITIAL) {
          throw new IllegalStateException("The state is " + _state + " instead of " + INITIAL + '.');
       }
 
       _xslt = url;
    }
 
    /**
     * Sets the success indication and the result code (if any).
     *
     * @param success
     *    success indication, <code>true</code> or <code>false</code>.
     *
     * @param code
     *    the result code, or <code>null</code>.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> != </code>{@link #INITIAL}.
     */
    public void start(boolean success, String code)
    throws IllegalStateException {
 
       // Check preconditions
       if (_state != INITIAL) {
          throw new IllegalStateException("The state is " + _state + " instead of " + INITIAL + '.');
       }
 
       // Set the fields
       _success = success;
       _code    = code;
    }
 
    /**
     * Returns the success indication.
     *
     * @return
     *    success indication, <code>true</code> or <code>false</code>.
     */
    public boolean isSuccess() {
       return _success;
    }
 
    /**
     * Returns the result code.
     *
     * @return
     *    the result code or <code>null</code> if no code was returned.
     */
    public String getCode() {
       return _code;
    }
 
    /**
     * Adds an output parameter to the result. The name and the value must
     * both be specified.
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
     *    if {@link #getState()}<code> != </code>{@link #DEFINE_PARAMETERS}.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || value == null || "".equals(name) || "".equals(value)</code>.
     *
     * @throws InvalidResponseException
     *    if the response is considered invalid.
     */
    public final void param(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state != DEFINE_PARAMETERS) {
          throw new IllegalStateException("The state is " + _state + " instead of " + DEFINE_PARAMETERS + '.');
       }
 
       // TODO: Check that parameter does not exist yet
       // TODO: Check that parameter is known
       // TODO: Check that parameter is valid for type
 
       _parameters.put(name, value);
    }
 
    /**
     * Gets all parameters.
     *
     * @return
     *    a <code>Map</code> containing all parameters, or <code>null</code> if
     *    no parameters are set; the keys will be the names of the parameters
     *    ({@link String} objects, cannot be <code>null</code>), the values will be the parameter values
     *    ({@link String} objects as well, cannot be <code>null</code>).
     */
    public Map getParameters() {
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
 
       MandatoryArgumentChecker.check("name", name);
 
       if (_parameters == null) {
          return null;
       }
 
       return (String) _parameters.get(name);
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
       if (_dataElement == null) {
          return null;
       } else {
          return (Element) _dataElement.clone();
       }
    }
 
    // TODO: _xmlOutputter.pi("xml-stylesheet", "type=\"text/xsl\" href=\"" + xslt + "\"");
 
    /**
     * Ends the result. This will change the state to {@link #COMPLETED}.
     *
     * @throws IllegalStateException
     *    if {@link #getState()}<code> == </code>{@link #COMPLETED}.
     */
    public void end()
    throws IllegalStateException {
 
       // Check state
       if (_state == COMPLETED) {
          throw new IllegalStateException("The state is " + _state + " instead of " + COMPLETED + '.');
       }
       _state = COMPLETED;
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
    public static final class State extends Object {
 
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
       public State(String name) throws IllegalArgumentException {
 
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
 
       public String toString() {
          return _name;
       }
    }
 }
