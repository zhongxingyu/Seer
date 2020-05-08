 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.collections.ProtectedPropertyReader;
 
 import org.xins.common.text.TextUtils;
 
 import org.xins.common.xml.Element;
 
 /**
  * XML calling convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 final class XMLCallingConvention
 extends CallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The request encoding format.
     */
    static final String REQUEST_ENCODING = "UTF-8";
 
    /**
     * The response encoding format.
     */
    static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    static final String RESPONSE_CONTENT_TYPE = "text/xml;charset="
                                              + RESPONSE_ENCODING;
 
    /**
     * Secret key used when accessing <code>ProtectedPropertyReader</code>
     * objects.
     */
    private static final Object SECRET_KEY = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XMLCallingConvention</code> object.
     */
    XMLCallingConvention() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
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
     *
     * @throws Exception
     *    if analysis of the request causes an exception;
     *    <code>false</code> will be assumed.
     */
    protected boolean matches(HttpServletRequest httpRequest)
    throws Exception {
 
       // Parse the XML in the request (if any)
       Element element = parseXMLRequest(httpRequest);
 
      return element.getLocalName().equals("request") &&
            element.getAttribute("function") != null;
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
 
       Element requestElem = parseXMLRequest(httpRequest);
 
      String functionName = requestElem.getAttribute("function");
 
       // Determine function parameters
       ProtectedPropertyReader functionParams = new ProtectedPropertyReader(SECRET_KEY);
       Iterator parameters = requestElem.getChildElements("param").iterator();
       while (parameters.hasNext()) {
          Element nextParam = (Element) parameters.next();
          String name  = nextParam.getAttribute("name");
          String value = nextParam.getText();
          functionParams.set(SECRET_KEY, name, value);
       }
 
       // Check if function is specified
       if (TextUtils.isEmpty(functionName)) {
          throw new FunctionNotSpecifiedException();
       }
 
       // Remove all invalid parameters
       cleanUpParameters(functionParams, SECRET_KEY);
 
       // Get data section
       Element dataElement = null;
       List dataElementList = requestElem.getChildElements("data");
       if (dataElementList.size() == 1) {
          dataElement = (Element) dataElementList.get(0);
       } else if (dataElementList.size() > 1) {
          throw new InvalidRequestException("Found multiple data sections.");
       }
 
       return new FunctionRequest(functionName, functionParams, dataElement);
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
