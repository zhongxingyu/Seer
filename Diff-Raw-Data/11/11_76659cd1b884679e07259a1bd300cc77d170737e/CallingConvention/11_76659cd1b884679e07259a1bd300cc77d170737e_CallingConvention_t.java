 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.ProgrammingError;
 import org.xins.common.Utils;
 
 import org.xins.common.collections.CollectionUtils;
 import org.xins.common.collections.ProtectedPropertyReader;
 
 import org.xins.common.text.TextUtils;
 
 /**
  * Abstraction of a calling convention. A calling convention determines how an
  * HTTP request is converted to a XINS request and how a XINS response is
  * converted back to an HTTP response.
  *
  * <p>Calling convention implementations are thread-safe. Hence if a calling
  * convention does not have any configuration parameters per instance, then
  * the <em>Singleton</em> pattern can be applied.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 abstract class CallingConvention
 extends Object {
 
    //------------------------------------------------------------------------
    // Class fields
    //------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = CallingConvention.class.getName();
 
    /**
     * Perl 5 pattern compiler.
     */
    private static final Perl5Compiler PATTERN_COMPILER = new Perl5Compiler();
 
    /**
     * Pattern matcher.
     */
    private static final Perl5Matcher PATTERN_MATCHER = new Perl5Matcher();
 
    /**
     * The pattern which normal parameter names should match, as a character
    * string. Note that this will be applied in a <em>case-insensitive</em>
    * way.
     */
    private static final String PATTERN_STRING = "[a-z][a-z0-9_]*";
 
    /**
     * The compiled pattern which normal parameter names should match.
     */
    private static final Pattern PATTERN;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class. This function compiles {@link #PATTERN_STRING}
     * to a {@link Pattern} and then stores that in {@link #PATTERN}.
     */
    static {
       final String THIS_METHOD = "<clinit>()";
       try {
          PATTERN = PATTERN_COMPILER.compile(
             PATTERN_STRING, 
             Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK);
 
       } catch (MalformedPatternException exception) {
          final String SUBJECT_CLASS = PATTERN_COMPILER.getClass().getName();
          final String SUBJECT_METHOD = "compile(java.lang.String,int)";
          final String DETAIL = "The pattern \""
                              + PATTERN_STRING
                              + "\" is considered malformed.";
 
          throw Utils.logProgrammingError(CLASSNAME,
                                          THIS_METHOD,
                                          SUBJECT_CLASS,
                                          SUBJECT_METHOD,
                                          DETAIL,
                                          exception);
       }
    }
 
    /**
     * Removes all parameters that should not be passed to a function. If the
     * set of parameters passed is <code>null</code>, then nothing is done.
     *
     * @param parameters
     *    the {@link ProtectedPropertyReader} containing the set of parameters
     *    to investigate, or <code>null</code>.
     *
     * @param secretKey
     *    the secret key required to be able to modify the parameters, can be
     *    <code>null</code>.
     */
    void cleanUpParameters(ProtectedPropertyReader parameters,
                           Object                  secretKey) {
       
       // TODO: Should we not let the diagnostic context ID through?
 
       // If the set of parameters passed is null, then nothing is done.
       if (parameters == null) {
          return;
       }
 
       // Get an list of the parameter names
       ArrayList names = CollectionUtils.list(parameters.getNames());
 
       // Loop through all parameters
       for (int i = 0; i < names.size(); i++) {
 
          // Determine parameter name and value
          String name  = (String) names.get(i);
          String value = parameters.get(name);
 
          // If the name or value is empty, then remove the parameter
          if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
             parameters.set(secretKey, name, null);
 
          // XXX: If the parameter name is "function", then remove it
          } else if ("function".equals(name)) {
             parameters.set(secretKey, name, null);
 
         // TODO: Enable this for XINS 2.0.0:
/*
          // If the pattern is not matched, then log and remove it
          } else if (! PATTERN_MATCHER.matches(name, PATTERN)) {
            // TODO: Log this
             parameters.set(secretKey, name, null);
*/
          }
       }
    }
 
 
    //------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallingConvention</code>.
     */
    protected CallingConvention() {
       // empty
    }
 
 
    //------------------------------------------------------------------------
    // Fields
    //------------------------------------------------------------------------
 
    //------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------
 
    /**
     * Converts an HTTP request to a XINS request (wrapper method). This method
     * checks the arguments, then calls the implementation method and then
     * checks the return value from that method.
     *
     * @param httpRequest
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @return
     *    the XINS request object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considerd to be invalid.
     *
     * @throws FunctionNotSpecifiedException
     *    if the request does not indicate the name of the function to execute.
     */
    final FunctionRequest convertRequest(HttpServletRequest httpRequest)
    throws IllegalArgumentException,
           InvalidRequestException,
           FunctionNotSpecifiedException {
 
       final String THIS_METHOD = "convertRequest("
                                + HttpServletRequest.class.getName()
                                + ')';
 
       // Check preconditions
       MandatoryArgumentChecker.check("httpRequest", httpRequest);
 
       final String SUBJECT_CLASS  = getClass().getName(); // XXX: Cache?
       final String SUBJECT_METHOD = "convertRequestImpl("
                                   + HttpServletRequest.class.getName()
                                   + ')'; // XXX: Cache?
 
       // Delegate to the implementation method
       FunctionRequest xinsRequest;
       try {
          xinsRequest = convertRequestImpl(httpRequest);
 
       // Filter any thrown exceptions
       } catch (Throwable t) {
          if (t instanceof InvalidRequestException) {
             throw (InvalidRequestException) t;
          } else if (t instanceof FunctionNotSpecifiedException) {
             throw (FunctionNotSpecifiedException) t;
          } else {
             throw Utils.logProgrammingError(CLASSNAME,
                                             THIS_METHOD,
                                             SUBJECT_CLASS,
                                             SUBJECT_METHOD,
                                             null,
                                             t);
          }
       }
 
       // Make sure the returned value is not null
       if (xinsRequest == null) {
          final String DETAIL = "Method returned null.";
          throw Utils.logProgrammingError(CLASSNAME,
                                          THIS_METHOD,
                                          SUBJECT_CLASS,
                                          SUBJECT_METHOD,
                                          DETAIL);
       }
 
       return xinsRequest;
    }
 
    /**
     * Converts an HTTP request to a XINS request (implementation method). This
     * method should only be called from class {@link CallingConvention}. Only
     * then it is guaranteed that the <code>httpRequest</code> argument is not
     * <code>null</code>.
     *
     * @param httpRequest
     *    the HTTP request, will not be <code>null</code>.
     *
     * @return
     *    the XINS request object, should not be <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considerd to be invalid.
     *
     * @throws FunctionNotSpecifiedException
     *    if the request does not indicate the name of the function to execute.
     */
    protected abstract FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException;
    
    /**
     * Converts a XINS result to an HTTP response (wrapper method). This method
     * checks the arguments, then calls the implementation method and then
     * checks the return value from that method.
     *
     * @param xinsResult
     *    the XINS result object that should be converted to an HTTP response,
     *    cannot be <code>null</code>.
     *
     * @param httpResponse
     *    the HTTP response object to configure, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xinsResult == null || httpResponse == null</code>.
     *
     * @throws IOException
     *    if calling any of the methods in <code>httpResponse</code> causes an
     *    I/O error.
     */
    final void convertResult(FunctionResult      xinsResult,
                             HttpServletResponse httpResponse)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("xinsResult",   xinsResult,
                                      "httpResponse", httpResponse);
 
       // Delegate to the implementation method
       try {
          convertResultImpl(xinsResult, httpResponse);
 
       // Filter any thrown exceptions
       } catch (Throwable exception) {
          if (exception instanceof IOException) {
             throw (IOException) exception;
          } else {
             final String THIS_METHOD    = "convertResult("
                                         + FunctionResult.class.getName()
                                         + ','
                                         + HttpServletResponse.class.getName()
                                         + ')';
             final String SUBJECT_CLASS  = getClass().getName();
             final String SUBJECT_METHOD = "convertResultImpl("
                                         + HttpServletRequest.class.getName()
                                         + ')';
 
             throw Utils.logProgrammingError(CLASSNAME,
                                             THIS_METHOD,
                                             SUBJECT_CLASS,
                                             SUBJECT_METHOD,
                                             null,
                                             exception);
          }
       }
    }
    
    /**
     * Converts a XINS result to an HTTP response (implementation method).
     *
     * @param xinsResult
     *    the XINS result object that should be converted to an HTTP response,
     *    will not be <code>null</code>.
     *
     * @param httpResponse
     *    the HTTP response object to configure, will not be <code>null</code>.
     *
     * @throws IOException
     *    if calling any of the methods in <code>httpResponse</code> causes an
     *    I/O error.
     */
    protected abstract void convertResultImpl(FunctionResult      xinsResult,
                                              HttpServletResponse httpResponse)
    throws IOException;
    // XXX: Replace IOException with more appropriate exception?
 }
