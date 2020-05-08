 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.collections.PropertyReader;
 
 /**
  * Result of a call to a XINS service.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.207
  */
 public final class XINSCallResult
 extends Object {
 
    //----------------------------------------------------------------------
    // Class fields
    //----------------------------------------------------------------------
 
    //----------------------------------------------------------------------
    // Class functions
    //----------------------------------------------------------------------
 
    //----------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Result</code> object.
     *
     * @param request
    *    the original {@link XINSCallRequest} that was used to perform the
     *    call, cannot be <code>null</code>.
     *
     * @param target
     *    the {@link TargetDescriptor} that was used to successfully get the
     *    result, cannot be <code>null</code>.
     *
     * @param duration
     *    the call duration, should be &gt;= 0.
     *
     * @param code
     *    the return code, if any, can be <code>null</code>.
     *
     * @param parameters
     *    output parameters returned by the function, or <code>null</code>.
     *
     * @param dataElement
     *    the data element returned by the function, or <code>null</code>; if
     *    specified then the name must be <code>"data"</code>, with no
     *    namespace.
     *
     * @throws IllegalArgumentException
     *    if <code>request    == null
     *          || target     == null
     *          || duration &lt; 0
     */
    XINSCallResult(XINSCallRequest      request,
                   TargetDescriptor target,
                   long             duration,
                   String           code,
                   PropertyReader   parameters,
                   DataElement      dataElement)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request, "target", target);
       if (duration < 0) {
          throw new IllegalArgumentException("duration (" + duration + ") < 0");
       }
 
       // Store all the information
       _request     = request;
       _target      = target;
       _duration    = duration;
       _code        = code;
       _parameters  = parameters;
       _dataElement = dataElement;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The original <code>XINSCallRequest</code>. Cannot be <code>null</code>.
     */
    private final XINSCallRequest _request;
 
    /**
     * The <code>TargetDescriptor</code> that was used to produce this
     * result. Cannot be <code>null</code>.
     */
    private final TargetDescriptor _target;
 
    /**
     * The call duration. Guaranteed to be &gt;= 0.
     */
    private final long _duration;
 
    /**
    /**
     * The error code. This field is <code>null</code> if the call was
     * successful and thus no error code was returned.
     */
    private final String _code;
 
    /**
     * The parameters and their values. This field is never <code>null</code>.
     */
    private final PropertyReader _parameters;
 
    /**
     * The data element. This field is <code>null</code> if there is no data
     * element.
     */
    private final DataElement _dataElement;
 
 
    //----------------------------------------------------------------------
    // Methods
    //----------------------------------------------------------------------
 
    /**
     * Returns the original <code>XINSCallRequest</code>.
     *
     * @return
     *    the {@link XINSCallRequest}, never <code>null</code>.
     */
    public XINSCallRequest getRequest() {
       return _request;
    }
 
    /**
     * Returns the <code>TargetDescriptor</code> that was used to generate
     * this result.
     *
     * @return
     *    the {@link TargetDescriptor}, cannot be <code>null</code>.
     */
    public TargetDescriptor getTarget() {
       return _target;
    }
 
    /**
     * Returns the call duration in milliseconds.
     *
     * @return
     *    the call duration in milliseconds, guaranteed to be &gt;= 0.
     */
    public long getDuration() {
       return _duration;
    }
 
    /**
     * Returns the error code.
     *
     * @return
     *    the error code or <code>null</code> if no code was returned.
     *
     * @deprecated
     *    Deprecated since XINS 0.182.
     *    Use {@link #getErrorCode()} instead.
     */
    public String getCode() {
       return _code;
    }
 
    /**
     * Returns the error code.
     *
     * @return
     *    the error code or <code>null</code> if no code was returned.
     */
    public String getErrorCode() {
       return _code;
    }
 
    /**
     * Returns the success indication.
     *
     * @return
     *    <code>true</code> if the result is successful, <code>false</code>
     *    otherwise.
     *
     * @deprecated
     *    Deprecated since XINS 0.182.
     *    Use {@link #getErrorCode()}<code> == null</code> instead.
     */
    public boolean isSuccess() {
       return getErrorCode() == null;
    }
 
    /**
     * Gets all parameters.
     *
     * @return
     *    a {@link PropertyReader} with all parameters, or <code>null</code>
     *    if there are none.
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
 
       // Short-circuit if there are no parameters at all
       if (_parameters == null) {
          return null;
       }
 
       // Otherwise return the parameter value
       return _parameters.get(name);
    }
 
    /**
     * Returns the optional extra data. The data is an XML {@link DataElement}, or
     * <code>null</code>.
     *
     * @return
     *    the extra data as an XML {@link DataElement}, can be <code>null</code>;
     */
    public DataElement getDataElement() {
 
       return _dataElement;
    }
 }
