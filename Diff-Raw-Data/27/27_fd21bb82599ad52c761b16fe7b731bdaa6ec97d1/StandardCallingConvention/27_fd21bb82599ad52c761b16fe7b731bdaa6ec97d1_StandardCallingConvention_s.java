 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.Utils;
 import org.xins.common.collections.ProtectedPropertyReader;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 /**
  * Standard calling convention. The technical name for this calling convention
  * is <em>_xins-std</em>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 class StandardCallingConvention
 extends CallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The enconding for the data section.
     */
    static final String DATA_ENCODING = "UTF-8";
 
    /**
     * The response encoding format.
     */
    static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    static final String RESPONSE_CONTENT_TYPE = "text/xml; charset="
                                              + RESPONSE_ENCODING;
 
    /**
     * Secret key used when accessing <code>ProtectedPropertyReader</code>
     * objects.
     */
    private static final Object SECRET_KEY = new Object();
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>StandardCallingConvention</code> object.
     */
    StandardCallingConvention() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified request can be handled by this calling
     * convention.
     *
     * <p>This method will not throw any exception.
     *
     * @param httpRequest
     *    the HTTP request to investigate, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if this calling convention is <em>possibly</em>
     *    able to handle this request, or <code>false</code> if it
     *    <em>definitely</em> not able to handle this request.
     */
    protected boolean matches(HttpServletRequest httpRequest) {
 
       // If no _function parameter is specified, then there is no match
       return ! TextUtils.isEmpty(httpRequest.getParameter("_function"));
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
     *    the XINS request object, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considerd to be invalid.
     *
     * @throws FunctionNotSpecifiedException
     *    if the request does not indicate the name of the function to execute.
     */
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       // Parse the parameters in the HTTP request
       ProtectedPropertyReader params = gatherParams(httpRequest, SECRET_KEY);
 
       // Remove all invalid parameters
       cleanUpParameters(params, SECRET_KEY);
 
       // Determine function name
       String functionName = httpRequest.getParameter("_function");
       if (TextUtils.isEmpty(functionName)) {
          throw new FunctionNotSpecifiedException();
       }
 
       // Get data section
       String dataSectionValue = httpRequest.getParameter("_data");
       Element dataElement = null;
       if (dataSectionValue != null && dataSectionValue.length() > 0) {
          ElementParser parser = new ElementParser();
 
          // Parse the data section
          try {
             dataElement = parser.parse(new StringReader(dataSectionValue));
 
          // I/O error, should never happen on a StringReader
          } catch (IOException exception) {
             String thisClass  = StandardCallingConvention.class.getName();
             String thisMethod = "convertRequestImpl("
                               + HttpServletRequest.class.getName()
                               + ')';
             String subjectClass  = ElementParser.class.getName();
             String subjectMethod = "parse(java.io.Reader)";
             String detail        = null;
             throw Utils.logProgrammingError(thisClass,    thisMethod,
                                             subjectClass, subjectMethod,
                                             detail,       exception);
          // Parsing error
          } catch (ParseException exception) {
             String detail = "Cannot parse the data section.";
             throw new InvalidRequestException(detail, exception);
          }
       }
 
       // Construct and return the request object
       return new FunctionRequest(functionName, params, dataElement);
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
     * @param httpRequest
     *    the HTTP request, will not be <code>null</code>.
     *
     * @throws IOException
     *    if calling any of the methods in <code>httpResponse</code> causes an
     *    I/O error.
     */
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
      // Send the XML output to the stream and flush
      httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
      PrintWriter out = httpResponse.getWriter();
       httpResponse.setStatus(HttpServletResponse.SC_OK);
      CallResultOutputter.output(out, xinsResult, false);
      out.close();
    }
 }
