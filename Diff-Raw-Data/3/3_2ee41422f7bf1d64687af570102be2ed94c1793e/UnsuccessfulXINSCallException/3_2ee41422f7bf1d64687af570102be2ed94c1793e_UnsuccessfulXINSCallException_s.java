 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.FastStringBuffer;
 
 /**
 * Exception that indicates that data was not received on a socket within a
 * designated time-out period.
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
     * Constructs a new <code>UnsuccessfulXINSCallException</code> based the
     * specified XINS call result object.
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
       if (result.getErrorCode() == null) {
          throw new IllegalArgumentException("result.getErrorCode() == null");
       }
 
       // Store reference to the call result object
       _result = result;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The call result. The value of this field cannot be <code>null</code>.
     */
    private final XINSCallResult _result;
 
 
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
