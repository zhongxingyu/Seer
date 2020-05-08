 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.collections.PropertyReader;
 
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.spec.ErrorCodeSpec;
 
 /**
  * Exception that indicates that a result code was returned by the API call.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public class UnsuccessfulXINSCallException
 extends XINSCallException
 implements XINSCallResultData {
 
    //-------------------------------------------------------------------------
    // NOTE: Since XINS 1.1.0, this class is no longer final. However, all
    //       methods still are.
 
    //-------------------------------------------------------------------------
    // NOTE: Since XINS 1.2.0, this class implements the XINSCallResultData
    //       interface.
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Delegate for the constructor that determines the detail message based on
     * a <code>XINSCallResultData</code> object and an optional detailed
     * description.
     *
     * @param result
     *    the {@link XINSCallResultData} instance, should not be
     *    <code>null</code>.
     *
     * @param detail
     *    detailed description to include, or <code>null</code> if unavailable.
     *
     * @return
     *    the detail message for the constructor to use, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.{@link XINSCallResultData#getErrorCode() getErrorCode()} == null</code>.
     */
    private static final String determineDetail(XINSCallResultData result,
                                                String             detail)
    throws IllegalArgumentException {
 
       // Argument cannot be null
       MandatoryArgumentChecker.check("result", result);
 
       // Result must be unsuccessful
       String errorCode = result.getErrorCode();
       if (errorCode == null) {
          throw new IllegalArgumentException("result.getErrorCode() == null");
       }
 
       if (detail == null || detail.length() < 1) {
          return "Error code \"" + errorCode + "\".";
       } else {
          return "Error code \"" + errorCode + "\": " + detail;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnsuccessfulXINSCallException</code> based on a
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
     * @param detail
     *    detail message, or <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request     == null
     *          || target      == null
     *          || duration  &lt; 0
     *          || resultData  == null
     *          || resultData.{@link XINSCallResult#getErrorCode() getErrorCode()} == null</code>.
     */
    UnsuccessfulXINSCallException(XINSCallRequest    request,
                                  TargetDescriptor   target,
                                  long               duration,
                                  XINSCallResultData resultData,
                                  String             detail)
    throws IllegalArgumentException {
 
       super("Unsuccessful XINS call result", request, target, duration,
             determineDetail(resultData, detail), null);
 
       // Check additional precondition
       MandatoryArgumentChecker.check("resultData", resultData);
 
       // TODO: Check all mandatory arguments at once
 
       // Result object must be unsuccessful
       String errorCode = resultData.getErrorCode();
       if (errorCode == null) {
          throw new IllegalArgumentException("resultData.getErrorCode() == null");
       }
 
       // Store details
       _result = resultData;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The result data. The value of this field cannot be <code>null</code>.
     */
    private final XINSCallResultData _result;
 
    /**
     * The type of the error.
     */
    private ErrorCodeSpec.Type _type;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the error code.
     *
     * @return
     *    the error code, never <code>null</code>.
     */
    public final String getErrorCode() {
       return _result.getErrorCode();
    }
 
    /**
     * Gets all returned parameters.
     *
     * @return
     *    a {@link PropertyReader} containing all parameters, or
     *    <code>null</code> if there are none.
     */
    public final PropertyReader getParameters() {
       return _result.getParameters();
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
    public final String getParameter(String name)
    throws IllegalArgumentException {
       PropertyReader p = getParameters();
       if (p == null) {
          return null;
       } else {
          return p.get(name);
       }
    }
 
    /**
     * Returns the optional extra data.
     *
     * @return
     *    the extra data as a {@link DataElement}, can be <code>null</code>;
     */
    public final DataElement getDataElement() {
       return _result.getDataElement();
    }
 
    /**
     * Sets the type of the error code.
     *
     * @param type
     *    the type of the error (functionnal or technical).
     */
    void setType(ErrorCodeSpec.Type type) {
       _type = type;
    }
    
    /**
     * Returns the type of the error code.
     *
     * @return
    *    the type as a {@link ErrorCodeSpec ErrorCodeSpec.Type}, can be <code>null</code> if it's unknown.
     */
    public final ErrorCodeSpec.Type getType() {
       return _type;
    }
 }
