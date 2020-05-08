 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.ServletException;
 
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
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.HexConverter;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 /**
  * Tests for XINS call convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class CallingConventionTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(CallingConventionTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The random number generator.
     */
    private final static Random RANDOM = new Random();
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
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
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests the standard calling convention which should be the default.
     */
    public void testStandardCallingConvention1() throws Throwable {
       callResultCodeStandard(null);
    }
 
    /**
     * Tests the standard calling convention.
     */
    public void testStandardCallingConvention2() throws Throwable {
       callResultCodeStandard("_xins-std");
    }
 
    /**
     * Tests the standard calling convention by passing an unknown calling convention.
     */
    public void testStandardCallingConvention3() throws Throwable {
       callResultCodeStandard("_xins-bla");
    }
 
    /**
     * Calls the ResultCode function and expect the standard calling convention back.
     *
     * @param convention
     *    the name of the calling convention parameter, or <code>null</code>
     *    if no calling convention parameter should be sent.
     *
     * @throw Throwable
     *    if anything goes wrong.
     */
    public void callResultCodeStandard(String convention) throws Throwable {
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, RANDOM.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       Element result1 = callResultCode(convention, randomFive);
       assertNull("The method returned an error code for the first call: " + result1.getAttribute("errorcode"), result1.getAttribute("errorcode"));
       assertNull("The method returned a code attribute for the first call: " + result1.getAttribute("code"), result1.getAttribute("code"));
       assertNull("The method returned a success attribute for the first call: " + result1.getAttribute("success"), result1.getAttribute("success"));
 
       Element result2 = callResultCode(convention, randomFive);
       assertNotNull("The method did not return an error code for the second call.", result2.getAttribute("errorcode"));
       assertNull("The method returned a code attribute for the second call: " + result2.getAttribute("code"), result2.getAttribute("code"));
       assertNull("The method returned a success attribute for the second call: " + result2.getAttribute("success"), result2.getAttribute("success"));
    }
 
    /**
     * Tests the old style calling convention.
     */
    public void testOldCallingConvention1() throws Throwable {
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, RANDOM.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       Element result1 = callResultCode("_xins-old", randomFive);
       assertNull("The method returned an error code for the first call: " + result1.getAttribute("errorcode"), result1.getAttribute("errorcode"));
       assertNull("The method returned a code attribute for the first call: " + result1.getAttribute("code"), result1.getAttribute("code"));
       assertNotNull("The method did not return a success attribute for the first call.", result1.getAttribute("success"));
 
       Element result2 = callResultCode("_xins-old", randomFive);
       assertNotNull("The method did not return an error code for the second call.", result2.getAttribute("errorcode"));
       assertNotNull("The method did not return a code attribute for the second call.", result2.getAttribute("code"));
       assertNotNull("The method did not return a success attribute for the second call.", result2.getAttribute("success"));
       assertEquals("The code and errorcode are different.", result2.getAttribute("code"), result2.getAttribute("errorcode"));
    }
 
    /**
     * Call the ResultCode function with the specified calling convention.
     *
     * @param convention
     *    the name of the calling convention parameter, or <code>null</code>
     *    if no calling convention parameter should be sent.
     *
     * @param inputText
     *    the value of the parameter to send as input.
     *
     * @return
     *    the parsed result as an Element.
     *
     * @throw Throwable
     *    if anything goes wrong.
     */
    private Element callResultCode(String convention, String inputText) throws Throwable {
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/", 2000);
       BasicPropertyReader params = new BasicPropertyReader();
       params.set("_function",  "ResultCode");
       params.set("inputText",  inputText);
       if (convention != null) {
          params.set("_convention", convention);
       }
       HTTPCallRequest request = new HTTPCallRequest(params);
       HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);
 
       HTTPCallResult result = caller.call(request);
       byte[] data = result.getData();
       ElementParser parser = new ElementParser();
       return parser.parse(new StringReader(new String(data)));
    }
 
    /**
     * Test the XML calling convention.
     */
    // To run this test add <calling-convention name="_xins-xml" /> to impl.xml
    // and remove a input.realLine() in the HTTPServletHandler.
    /*public void testXMLCallingConvention() throws Throwable {
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, RANDOM.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       // Successful call
       postXMLRequest(randomFive, true);
 
       // Unsuccessful call
       postXMLRequest(randomFive, false);
    }*/
 
    /**
     * Posts XML request.
     *
     * @param randomFive
     *    A randomly generated String.
     * @param success
     *    <code>true</code> if the expected result should be successfal,
     *    <code>false</code> otherwise.
     *
     * @throws Exception
     *    If anything goes wrong.
     */
    private void postXMLRequest(String randomFive, boolean success) throws Exception {
       PostMethod post = new PostMethod("http://127.0.0.1:8080/");
       post.setRequestHeader("Content-type", "text/xml; charset=UTF-8");
       post.setRequestBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<request>" +
               "  <param name=\"_function\">ResultCode</param>" +
               "  <param name=\"inputText\">" + randomFive + "</param>" +
               "</request>");
       HttpClient client = new HttpClient();
       client.setConnectionTimeout(5000);
       client.setTimeout(5000);
       try {
          int code = client.executeMethod(post);
          byte[] data = post.getResponseBody();
          ElementParser parser = new ElementParser();
          Element result = parser.parse(new StringReader(new String(data)));
          assertEquals("result", result.getLocalName());
          if (success) {
             assertNull("The method returned an error code: " + result.getAttribute("errorcode"), result.getAttribute("errorcode"));
          } else {
             assertNotNull("The method did not return an error code for the second call: " + result.getAttribute("errorcode"), result.getAttribute("errorcode"));
             assertEquals("AlreadySet", result.getAttribute("errorcode"));
          }
          assertNull("The method returned a code attribute: " + result.getAttribute("code"), result.getAttribute("code"));
          assertNull("The method returned a success attribute.", result.getAttribute("success"));
          List child = result.getChildElements();
          assertEquals(1, child.size());
          Element param = (Element) child.get(0);
          assertEquals("param", param.getLocalName());
          if (success) {
             assertEquals("outputText", param.getAttribute("name"));
             assertEquals(randomFive + " added.", param.getText());
          } else {
             assertEquals("count", param.getAttribute("name"));
             assertEquals("1", param.getText());
          }
       } finally {
 
          // Release current connection to the connection pool once you are done
          post.releaseConnection();
       }
    }
 }
