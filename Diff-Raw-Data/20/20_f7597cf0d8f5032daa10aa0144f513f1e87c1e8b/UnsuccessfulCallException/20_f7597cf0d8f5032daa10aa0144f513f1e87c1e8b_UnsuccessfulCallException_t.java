 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Exception that indicates that an API call result was unsuccessful.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.36
  */
 public final class UnsuccessfulCallException
 extends CallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a message for the constructor.
     *
     * @param result
     *    the call result that is unsuccessful, cannot be <code>null</code>,
     *    and <code>result.</code>{@link CallResult#isSuccess() isSuccess()}
     *    should be <code>false</code>.
     *
    * @return
    *    the constructed message for the construcotr to pass up to the
    *    superconstructor, never <code>null</code>.
    *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.</code>{@link CallResult#isSuccess() isSuccess()}.
     *
     * @since XINS 0.124
     */
    private static final String createMessage(CallResult result)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("result", result);
       if (result.isSuccess()) {
          throw new IllegalArgumentException("result.isSuccess() == true");
       }
 
       FastStringBuffer buffer = new FastStringBuffer(80);
       buffer.append("Call was unsuccessful");
       String code = result.getCode();
       if (code != null && code.length() > 0) {
          buffer.append(", result code was \"");
          buffer.append(code);
          buffer.append('"');
       }
       buffer.append('.');
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnsuccessfulCallException</code> with the
     * specified call result.
     *
     * @param result
     *    the call result that is unsuccessful, cannot be <code>null</code>,
     *    and <code>result.</code>{@link CallResult#isSuccess() isSuccess()}
     *    should be <code>false</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.</code>{@link CallResult#isSuccess() isSuccess()}.
     */
    public UnsuccessfulCallException(CallResult result)
    throws IllegalArgumentException {
 
       super(createMessage(result), null);
 
       _result = result;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The call result. The value of this field cannot be <code>null</code>.
     */
    private final CallResult _result;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the call result.
     *
     * @return
     *    the call result, cannot be <code>null</code>.
     */
    public CallResult getCallResult() {
       return _result;
    }
 }
