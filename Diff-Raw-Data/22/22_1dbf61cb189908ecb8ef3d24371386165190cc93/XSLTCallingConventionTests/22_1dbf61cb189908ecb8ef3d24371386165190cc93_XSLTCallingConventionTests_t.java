 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.util.*;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.HexConverter;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests for calling conventions.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class XSLTCallingConventionTests extends TestCase {
 
    /**
     * The random number generator.
     */
    private final static Random RANDOM = new Random();
 
    /**
     * Constructs a new <code>XSLTCallingConventionTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public XSLTCallingConventionTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(XSLTCallingConventionTests.class);
    }
 
    /**
     * Tests the XSLT calling convention.
     */
    public void testXSLTCallingConvention1() throws Throwable {
       String html = getHTMLVersion(false);
       assertTrue("The returned data is not an HTML file: " + html, html.startsWith("<html>"));
       assertTrue("Incorrect HTML data returned.", html.indexOf("XINS version") != -1);
 
       String html2 = getHTMLVersion(true);
       assertTrue("The returned data is not an HTML file: " + html2, html2.startsWith("<html>"));
       assertTrue("Incorrect HTML data returned.", html2.indexOf("API version") != -1);
    }
 
    /**
     * Tests that when different parameter values are passed to the
     * _xins-xslt calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    public void testXSLTCallingConvention2() throws Throwable {
       CallingConventionTests.doTestMultipleParamValues("_xins-xslt");
    }
 
    /**
     * Tests that when no XSLT is provided it fails
     * _xins-xslt calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    public void testXSLTCallingConvention3() throws Throwable {
       String randomLong = HexConverter.toHexString(new Random().nextLong());
       String randomFive = randomLong.substring(0, 5);
       try {
         CallingConventionTests.callResultCode("_xins-xslt", randomFive);
         fail("No XSLT Stylesheet should return and error.");
       } catch (StatusCodeHTTPCallException schcex) {
           int code = schcex.getStatusCode();
           assertEquals("Unexpected status code returned: " + code, 500, code);
       }
    }
 
    private String getHTMLVersion(boolean useTemplateParam) throws Exception {
       TargetDescriptor descriptor = new TargetDescriptor(AllTests.url(), 2000);
       BasicPropertyReader params = new BasicPropertyReader();
      params.set("_function", "_GetVersion");
       params.set("_convention", "_xins-xslt");
       if (useTemplateParam) {
          params.set("_template", "src/tests/getVersion2.xslt");
       }
       HTTPCallRequest request = new HTTPCallRequest(params);
       HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);
 
       HTTPCallResult result = caller.call(request);
       return result.getString();
    }
 }
