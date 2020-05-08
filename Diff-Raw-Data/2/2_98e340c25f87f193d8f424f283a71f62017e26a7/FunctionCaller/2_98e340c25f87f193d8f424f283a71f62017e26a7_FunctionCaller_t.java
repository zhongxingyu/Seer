 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * Interface for API function calling functionality.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public interface FunctionCaller {
 
    /**
     * Calls the specified API function.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters which are passed to that function, or
     *    <code>null</code>; keys must be {@link String Strings}, values can be
     *    of any class.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @throws IOException
     *    if the API could not be contacted due to an I/O error.
     *
    * @throws InvalidCallResultException
     *    if the calling of the function failed or if the result from the
     *    function was invalid.
     */
    CallResult call(String functionName,
                    Map    parameters)
    throws IllegalArgumentException, IOException, InvalidCallResultException;
 }
