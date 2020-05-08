 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Exception that indicates that a result code was returned by the API call.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.207
  */
 public final class UnsuccessfulXINSCallException
 extends XINSCallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnsuccessfulXINSCallException</code> based on a
     * <code>XINSCallResult</code> instance.
     *
     * @param result
     *    the call result, cannot be <code>null</code>; stores the original
     *    call request, the target descriptor and the call duration; must be
     *    unsuccessful.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.{@link XINSCallResult#getErrorCode() getErrorCode()} == null</code>.
     */
    UnsuccessfulXINSCallException(XINSCallResult result)
    throws IllegalArgumentException {
 
       super("Unsuccessful XINS call result", result, null, null);
 
       // Result object must be unsuccessful
       String errorCode = result.getErrorCode();
       if (errorCode == null) {
          throw new IllegalArgumentException("result.getErrorCode() == null");
       }
 
       // Store details
       _errorCode   = errorCode;
       _parameters  = result.getParameters();
       _dataElement = result.getDataElement();
    }
 
    /**
     * Constructs a new <code>Unsuccessful</code> based on a
     * <code>XINSCallResultData</code> instance.
     *
     * @param request
     *    the original request, cannot be <code>null</code>.
     *
     * @param target
     *    descriptor for the target that was attempted to be called, cannot be
     *    <code>null</code>.
     *
     * @param duration
     *    the call duration in milliseconds, must be &gt;= 0.
     *
     * @param resultData
     *    the result data, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request     == null
     *          || target      == null
     *          || duration  &lt; 0
     *          || resultData  == null
     *          || resultData.{@link XINSCallResult#getErrorCode() getErrorCode()} == null</code>.
     *
     * @since XINS 0.209
     */
    UnsuccessfulXINSCallException(XINSCallRequest    request,
                                  TargetDescriptor   target,
                                  long               duration,
                                 XINSCallResultData resultData) {
 
       super("Unsuccessful XINS call result", request, target, duration, null, null);
 
       // Check additional precondition
       MandatoryArgumentChecker.check("resultData", resultData);
 
       // TODO: Check all mandatory arguments at once
 
       // Result object must be unsuccessful
       String errorCode = resultData.getErrorCode();
       if (errorCode == null) {
          throw new IllegalArgumentException("resultData.getErrorCode() == null");
       }
 
       // Store details
       _errorCode   = errorCode;
       _parameters  = resultData.getParameters();
       _dataElement = resultData.getDataElement();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The error code. The value of this field cannot be <code>null</code>.
     */
    private final String _errorCode;
 
    /**
     * The parameters. The value of this field can be <code>null</code>.
     */
    private final PropertyReader _parameters;
 
    /**
     * Returns the optional extra data.
     */
    private final DataElement _dataElement;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the error code.
     *
     * @return
     *    the error code, never <code>null</code>.
     */
    public String getErrorCode() {
       return _errorCode;
    }
 
    /**
     * Gets all returned parameters.
     *
     * @return
     *    a {@link PropertyReader} containing all parameters, or
     *    <code>null</code> if there are none.
     */
    public PropertyReader getParameters() {
       return _parameters;
    }
 
    /**
     * Gets the value of the specified returned parameter.
     *
     * @param name
     *    the parameter name, not <code>null</code>.
     *
     * @return
     *    the value of the parameter, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
       return _parameters.get(name);
    }
 
    /**
     * Returns the optional extra data.
     *
     * @return
     *    the extra data as a {@link DataElement}, can be <code>null</code>;
     */
    public DataElement getDataElement() {
       return _dataElement;
    }
 }
