 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests for calling conventions.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class CallingConventionTests extends TestCase {
 
    /**
     * The random number generator.
     */
    private final static Random RANDOM = new Random();
 
    /**
     * Constructs a new <code>CallingConventionTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public CallingConventionTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(CallingConventionTests.class);
    }
 
    /**
     * Tests with an unknown calling convention.
     */
    public void testInvalidCallingConvention() throws Throwable {
       TargetDescriptor descriptor = new TargetDescriptor(AllTests.url(), 2000);
       BasicPropertyReader params = new BasicPropertyReader();
      params.set("_function",  "ResultCode");
      params.set("inputText",  "blablabla");
       params.set("_convention", "_xins-bla");
       HTTPCallRequest request = new HTTPCallRequest(params);
       HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);
 
       HTTPCallResult result = caller.call(request);
       assertEquals(400, result.getStatusCode());
    }
 
    /**
     * Test the custom calling convention.
     */
    public void testCustomCallingConvention() throws Exception {
       URL url = new URL(AllTests.url() + "?query=hello%20Custom&_convention=xins-tests");
       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
       connection.connect();
       assertEquals(200, connection.getResponseCode());
       URL url2 = new URL(AllTests.url() + "?query=hello%20Custom&_convention=xins-tests");
       HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
       connection2.connect();
       try {
          assertEquals(400, connection2.getResponseCode());
       } catch (IOException ioe) {
          assertTrue(ioe.getMessage(), ioe.getMessage().indexOf(" 400") != -1);
       }
    }
 
    /**
     * Call the ResultCode function with the specified calling convention.
     * Parameters are pass in as URL parameters.
     *
     * @param convention
     *    the name of the calling convention parameter, or <code>null</code>
     *    if no calling convention parameter should be sent.
     *
     * @param inputText
     *    the value of the parameter to send as input.
     *
     * @return
     *    the data returned by the API.
     *
     * @throw Throwable
     *    if anything goes wrong.
     */
    static String callResultCode(String convention, String inputText) throws Throwable {
       TargetDescriptor descriptor = new TargetDescriptor(AllTests.url() + "allinone/", 2000);
       BasicPropertyReader params = new BasicPropertyReader();
      params.set("_function",  "ResultCode");
      params.set("useDefault",  "false");
       params.set("inputText",  inputText);
       if (convention != null) {
          params.set("_convention", convention);
       }
       HTTPCallRequest request = new HTTPCallRequest(params);
       HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);
 
       HTTPCallResult result = caller.call(request);
       return result.getString();
    }
 
    /**
     * Tests the HTTP OPTIONS method.
     */
    public void testOptionsMethod() throws Exception {
 
       String[] yes = new String[] { "GET", "HEAD", "POST", "OPTIONS" };
       String[] no  = new String[] { "CONNECT", "PUT", "DELETE" };
       doTestOptions("*",                        yes, no);
       doTestOptions("/?_convention=_xins-std",  yes, no);
 
       yes = new String[] { "POST", "OPTIONS" };
       no  = new String[] { "CONNECT", "PUT", "DELETE", "GET", "HEAD" };
       doTestOptions("/?_convention=_xins-soap", yes, no);
    }
 
    private void doTestOptions(String queryString, String[] yes, String[] no)
    throws Exception {
 
       String     host   = AllTests.host();
       int        port   = AllTests.port();
       String     method = "OPTIONS";
       Properties headers = new Properties();
       headers.put("Content-Length", "0");
 
       // Call the server
       HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
       // Expect 200 OK
       assertEquals("Expected 200 OK in response to HTTP OPTIONS request.", "200 OK", result.getStatus());
 
       // Expect an empty body
       String body = result.getBody();
       assertTrue("Expected no body in response to an HTTP OPTIONS request.", body == null || body.length() < 1);
 
       // Expect "Accept" field in the response (case-insensitive)
       List acceptHeaders = result.getHeaderValues("accept");
       assertTrue("Expected one \"Accept\" header in response to an HTTP OPTIONS request. Received " + acceptHeaders.size() + '.', acceptHeaders.size() == 1);
 
       // Make sure field is not empty
       String acceptHeader = (String) acceptHeaders.get(0);
       assertTrue("Expected \"Accept\" header in response to HTTP OPTIONS request to have a non-empty value.", acceptHeader.trim().length() > 0);
 
       // Split the list of acceptable HTTP methods
       List acceptValues = splitCommas(acceptHeader);
 
       // Make sure all expected HTTP methods are in the list
       for (int i = 0; i < yes.length; i++) {
          assertTrue("Expected \"Accept\" header in response to HTTP OPTIONS request to indicate the \"" + yes[i] + "\" method is supported. Instead the response is \"" + acceptHeader + "\".", acceptValues.contains(yes[i]));
       }
 
       // Make sure all forbidden HTTP methods are not in the list
       for (int i = 0; i < no.length; i++) {
          assertFalse("Expected \"Accept\" header in response to HTTP OPTIONS request to not indicate the \"" + no[i] + "\" method is supported. Instead the response is \"" + acceptHeader + "\".", acceptValues.contains(no[i]));
       }
    }
 
    private List splitCommas(String in) {
 
       // XXX: Before, this was the following code, but that is Java 1.4+ only:
       // return Arrays.asList(acceptHeader.split("[ ]*,[ ]*"));
 
       List list = new ArrayList();
       StringTokenizer st = new StringTokenizer(in, ",");
       while (st.hasMoreTokens()) {
          String item = (String) st.nextToken();
          list.add(item.trim());
       }
 
       return list;
    }
 
    /**
     * Posts the XML data the the given destination.
     *
     * @param destination
     *    the destination where the XML has to be posted.
     * @param data
     *    the XML to post.
     *
     * @return
     *    the returned XML already parsed.
     *
     * @throw Exception
     *    if anything goes wrong.
     */
    static Element postXML(String destination, String data) throws Exception {
       return postXML(destination, data, 200);
    }
 
    /**
     * Posts the XML data the the given destination.
     *
     * @param destination
     *    the destination where the XML has to be posted.
     *
     * @param data
     *    the XML to post.
     *
     * @param expectedStatus
     *    the HTTP status code that is expected.
     *
     * @return
     *    the returned XML already parsed.
     *
     * @throw Exception
     *    if anything goes wrong.
     */
    static Element postXML(String destination, String data, int expectedStatus)
    throws Exception {
 
       String content = postData(destination, data, "text/xml; charset=UTF-8", expectedStatus);
       ElementParser parser = new ElementParser();
       Element result = parser.parse(new StringReader(content));
       return result;
    }
 
    /**
     * Posts the data the the given destination.
     *
     * @param destination
     *    the destination where the data has to be posted.
     *
     * @param data
     *    the data to post.
     *
     * @param contentType
     *    the content type of the data to post.
     *
     * @param expectedStatus
     *    the HTTP status code that is expected.
     *
     * @return
     *    the data returned by the API.
     *
     * @throw Exception
     *    if anything goes wrong.
     */
    static String postData(String destination, String data, String contentType, int expectedStatus)
    throws Exception {
 
       PostMethod post = new PostMethod(destination);
       if (contentType != null) {
          post.setRequestHeader("Accept", contentType);
          post.setRequestHeader("Content-Type", contentType);
       }
       post.setRequestHeader("User-Agent", "PostMethod");
       post.setRequestBody(data);
       HttpClient client = new HttpClient();
       client.setConnectionTimeout(5000);
       client.setTimeout(5000);
       try {
          int code = client.executeMethod(post);
          assertEquals(expectedStatus, code);
          if (contentType != null) {
             String returnedContentType = post.getResponseHeader("Content-Type").getValue();
             assertEquals(contentType, returnedContentType);
          }
          String result = post.getResponseBodyAsString();
          return result;
       } finally {
 
          // Release current connection to the connection pool once you are done
          post.releaseConnection();
       }
    }
 
    /**
     * Tests that when different parameter values are passed to the
     * specified calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    static void doTestMultipleParamValues(String convention)
    throws Throwable {
 
       String destination = AllTests.url() + "allinone/";
 
       HttpClient client = new HttpClient();
       client.setConnectionTimeout(5000);
       client.setTimeout(5000);
 
       PostMethod post;
       String paramName, value1, value2, message;
       int actual, expected;
 
       // Different values for the same parameter
       post      = new PostMethod(destination);
       paramName = "_function";
       value1    = "_GetFunctionList";
       value2    = "_GetStatistics";
       post.addParameter("_convention", convention);
       post.addParameter(paramName, value1);
       post.addParameter(paramName, value2);
       try {
          actual = client.executeMethod(post);
       } finally {
          post.releaseConnection();
       }
       expected = 400;
       message = "Expected the \""
               + convention
               + "\" calling convention to return HTTP response code "
               + expected
               + " instead of "
               + actual
               + " when two different values are given for the \""
               + paramName
               + "\" parameter.";
       assertEquals(message, expected, actual);
 
       // Equal values for the same parameter
       post      = new PostMethod(destination);
       paramName = "_function";
       value1    = "_GetVersion";
       value2    = value1;
       post.addParameter("_convention", convention);
       post.addParameter(paramName, value1);
       post.addParameter(paramName, value2);
       try {
          actual = client.executeMethod(post);
       } finally {
          post.releaseConnection();
       }
       expected = 200;
       message = "Expected the \""
               + convention
               + "\" calling convention to return HTTP response code "
               + expected
               + " instead of "
               + actual
               + " when two equal values are given for the \""
               + paramName
               + "\" parameter.";
       assertEquals(message, expected, actual);
    }
 
    /**
     * Tests that unsupported HTTP methods return the appropriate HTTP error.
     */
    public void testUnsupportedHTTPMethods() throws Exception {
 
      String[] unsupported = new String[] { "GET",  "HEAD", };
 
       String     queryString = "/?_convention=_xins-xml";
       String     host        = AllTests.host();
       int        port        = AllTests.port();
       Properties headers     = new Properties();
       headers.put("Content-Length", "0");
 
       for (int i=0; i<unsupported.length; i++) {
          String method = unsupported[i];
 
          // Call the server
          HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
          // Expect "405 Method Not Allowed"
          assertEquals("Expected HTTP status code 405 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                       "405 Method Not Allowed", result.getStatus());
       }
    }
 
    /**
     * Tests that unknown HTTP methods return the appropriate HTTP error.
     */
    public void testUnknownHTTPMethods() throws Exception {
 
      String[] unknown = new String[] { "PUT", "DELETE", "POLL",  "JO-JO", "get", "post" };
 
       String     queryString = "/?_convention=_xins-xml";
       String     host        = AllTests.host();
       int        port        = AllTests.port();
       Properties headers     = new Properties();
       headers.put("Content-Length", "0");
 
       for (int i=0; i<unknown.length; i++) {
          String method = unknown[i];
 
          // Call the server
          HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
          // Expect "501 Not Implemented"
          assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                       "501 Not Implemented", result.getStatus());
       }
 
       // Same, but now without the content length header
       headers = new Properties();
       for (int i=0; i<unknown.length; i++) {
          String method = unknown[i];
 
          // Call the server
          HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
          // Expect "501 Not Implemented"
          assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                       "501 Not Implemented", result.getStatus());
       }
    }
 }
