 /*
  * $Id$
  *
  * Copyright 2003-2008 Online Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.xins.client.InvalidResultXINSCallException;
 import org.xins.client.XINSCallConfig;
 import org.xins.client.XINSCallRequest;
 import org.xins.client.XINSCallResult;
 
 import org.xins.client.XINSServiceCaller;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.UnsupportedProtocolException;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests the <code>XINSServiceCaller</code>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  */
 public class XINSServiceCallerTests extends TestCase {
 
    /**
     * Constructs a new <code>XINSServiceCallerTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public XINSServiceCallerTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(XINSServiceCallerTests.class);
    }
 
    /**
     * Tests the constructor arguments.
     */
    public void testXINSServiceCallerConstructor() throws Throwable {
 
       TargetDescriptor  descriptor;
       XINSServiceCaller caller;
       String            url;
 
       // Construct a XINSServiceCaller with no descriptor
       descriptor = null;
       caller     = new XINSServiceCaller(null);
       assertEquals(descriptor, caller.getDescriptor());
 
       // Update descriptor in XINSServiceCaller
       url        = "http://1.2.3.4/";
       descriptor = new TargetDescriptor(url);
       caller.setDescriptor(descriptor);
       assertEquals(descriptor, caller.getDescriptor());
 
       // Test XINSServiceCaller with invalid protocol
       url        = "blah://1.2.3.4/";
       descriptor = new TargetDescriptor(url);
       try {
          caller = new XINSServiceCaller(descriptor);
          fail("Expected UnsupportedProtocolException.");
       } catch (UnsupportedProtocolException upe) {
          // as expected
       }
 
       // Construct XINSServiceCaller with valid descriptor
       url        = "hTtP://192.168.0.1:12345/";
       descriptor = new TargetDescriptor(url);
       caller     = new XINSServiceCaller(descriptor);
       assertEquals(descriptor, caller.getDescriptor());
 
       // Set the descriptor to null again
       descriptor = null;
       caller.setDescriptor(descriptor);
       assertEquals(descriptor, caller.getDescriptor());
    }
 
    /**
     * Test using the XINSServiceCaller with https
     */
    public void testXINSServiceCallerWithHTTPS() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_GetVersion", null);
      TargetDescriptor descriptor = new TargetDescriptor("https://appframework.dev.java.net/", 10000);
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       try {
          caller.call(request);
          fail("Received result where an exception was expected");
       } catch (InvalidResultXINSCallException exception) {
          // as expected
       }
    }
 
    /**
     * Test the XINSServiceCaller with HTTP GET
     */
    public void testXINSServiceCallerWithGET() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_GetVersion", null);
       XINSCallConfig config = new XINSCallConfig();
       config.setHTTPMethod(HTTPMethod.GET);
       request.setXINSCallConfig(config);
       TargetDescriptor descriptor = new TargetDescriptor(AllTests.url());
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       PropertyReader parameters = result.getParameters();
       assertNotNull("No java version specified.", parameters.get("java.version"));
 
       BasicPropertyReader parameters2 = new BasicPropertyReader();
       parameters2.set("useDefault", "false");
       parameters2.set("inputText", "bonjour");
       XINSCallRequest request2 = new XINSCallRequest("ResultCode", parameters2);
       request2.setXINSCallConfig(config);
       caller.call(request2);
    }
 }
