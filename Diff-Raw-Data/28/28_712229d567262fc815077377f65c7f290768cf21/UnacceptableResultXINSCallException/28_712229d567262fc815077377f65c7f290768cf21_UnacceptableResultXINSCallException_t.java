 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 
 /**
  * Exception that indicates that an API call returned a result that was
  * considered unacceptable by the application layer.
  *
  * <p>Note that this exception is <em>not</em> thrown if the result is
  * invalid according to the XINS rules for an result XML document. Only if the
  * result is just invalid in relation to the applicable API specification this
  * exception is thrown.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class UnacceptableResultXINSCallException
 extends XINSCallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
   /**
    * Checks the mandatory <code>result</code> argument for the constructor
    * that accepts an <code>AbstractCAPICallResult</code>.
    *
    * @param result
    *    the argument for the constructor, cannot be <code>null</code>.
    *
    * @return
    *    the argument <code>result</code>, guaranteed not <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>result == null</code>.
    */
   private static AbstractCAPICallResult checkArguments(AbstractCAPICallResult result)
   throws IllegalArgumentException {
      MandatoryArgumentChecker.check("result", result);
      return result;
   }


    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnacceptableCallResultException</code> using the
     * specified <code>XINSCallResult</code>.
     *
     * @param result
     *    the {@link XINSCallResult} that is considered unacceptable, never
     *    <code>null</code>.
     *
     * @param detail
     *    a detailed description of why the result is considered unacceptable,
     *    or <code>null</code> if such a description is not available.
     *
     * @param cause
     *    the optional cause exception, or <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null</code>.
     */
    public UnacceptableResultXINSCallException(XINSCallResult result,
                                               String         detail,
                                               Throwable      cause)
    throws IllegalArgumentException {
 
       super("Unacceptable XINS call result", result, detail, cause);
 
       // Store the result
       _result = result;
    }
 
    /**
     * Constructs a new <code>UnacceptableCallResultException</code> using the
     * specified <code>AbstractCAPICallResult</code>.
     *
     * @param result
     *    the {@link AbstractCAPICallResult} that is considered unacceptable,
     *    never <code>null</code>.
     *
     * @param detail
     *    a detailed description of why the result is considered unacceptable,
     *    or <code>null</code> if such a description is not available.
     *
     * @param cause
     *    the optional cause exception, or <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null</code>.
     */
    public UnacceptableResultXINSCallException(AbstractCAPICallResult result,
                                               String                 detail,
                                               Throwable              cause)
    throws IllegalArgumentException {
 
      this(checkArguments(result).getXINSCallResult(), detail, cause);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The result that is considered unacceptable. Never <code>null</code>.
     */
    private final XINSCallResult _result;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the error code.
     *
     * @return
     *    the error code or <code>null</code> if the call was successful and no
     *    error code was returned.
     */
    public String getErrorCode() {
       return _result.getErrorCode();
    }
 
    /**
     * Gets all returned parameters.
     *
     * @return
     *    a {@link PropertyReader} containing all parameters, or
     *    <code>null</code> if there are none.
     */
    public PropertyReader getParameters() {
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
    public String getParameter(String name)
    throws IllegalArgumentException {
       return _result.getParameter(name);
    }
 
    /**
     * Returns the optional extra data.
     *
     * @return
     *    the extra data as a {@link DataElement}, can be <code>null</code>;
     */
    public DataElement getDataElement() {
       return _result.getDataElement();
    }
 }
