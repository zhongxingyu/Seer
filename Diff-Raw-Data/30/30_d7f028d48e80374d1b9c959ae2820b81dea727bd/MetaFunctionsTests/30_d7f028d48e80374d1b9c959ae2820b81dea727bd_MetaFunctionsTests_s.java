 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.UnsuccessfulXINSCallException;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.http.HTTPCallException;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.TargetDescriptor;
 
 import org.xins.client.DataElement;
 import org.xins.client.XINSCallRequest;
 import org.xins.client.XINSCallResult;
 import org.xins.client.XINSServiceCaller;
 
import org.xins.tests.server.servlet.HTTPServletHandler;

 /**
  * Tests for XINS meta functions.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class MetaFunctionsTests extends TestCase {
 
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
       return new TestSuite(MetaFunctionsTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>MetaFunctionsTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public MetaFunctionsTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests the _GetVersion meta function.
     */
    public void testGetVersion() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_GetVersion", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned a data element.", result.getDataElement());
       PropertyReader parameters = result.getParameters();
       assertNotNull("The function did not returned any parameters.", parameters);
       assertNotNull("No java version specified.", parameters.get("java.version"));
       assertNotNull("No XINS version specified.", parameters.get("xins.version"));
       assertNotNull("No xmlenc version specified.", parameters.get("xmlenc.version"));
       assertNotNull("No API version specified.", parameters.get("api.version"));
       assertEquals("Wrong API version specified.", "1.6", parameters.get("api.version"));
       assertEquals("Incorrect number of parameters.", 4, parameters.size());
    }
 
    /**
     * Tests the _GetStatistics meta function.
     */
    public void testGetStatistics() throws Throwable {
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result;
 
       // Determine the remote Java version
       XINSCallRequest request = new XINSCallRequest("_GetVersion");
       result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       PropertyReader parameters = result.getParameters();
       assertNotNull("The function _GetVersion did not returned any parameters.", parameters);
       String javaVersion = parameters.get("java.version");
       assertNotNull("No Java version returned by _GetVersion.", javaVersion);
       assertTrue(javaVersion.length() > 0);
       boolean java14 = javaVersion.startsWith("1.4")
                     || javaVersion.startsWith("1.5")
                     || javaVersion.startsWith("1.6");
 
       // Get the statistics
       request = new XINSCallRequest("_GetStatistics", null);
       result = caller.call(request);
       assertNull("The function _GetStatistics returned a result code.", result.getErrorCode());
       assertNotNull("The function returned a data element.", result.getDataElement());
       parameters = result.getParameters();
       assertNotNull("The function _GetStatistics did not returned any parameters.", parameters);
       assertNotNull("No startup date specified.", parameters.get("startup"));
       try {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SSS");
          formatter.parse(parameters.get("startup"));
       } catch (ParseException parseException) {
          fail("Incorrect date format for startup time.");
       }
       assertNotNull("No (now) date specified.", parameters.get("now"));
       try {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SSS");
          Date now = formatter.parse(parameters.get("now"));
          assertTrue("Start-up time is after 'now' time.", now.after(formatter.parse(parameters.get("startup"))));
       } catch (ParseException parseException) {
          fail("Incorrect date format for 'now'.");
       }
       DataElement data = result.getDataElement();
       assertNull(data.getAttributes());
       List children = data.getChildElements();
 
       DataElement heap = (DataElement) children.get(0);
       assertNotNull("No total memory provided.", heap.get("total"));
       assertNotNull("No used memory provided.", heap.get("used"));
       assertNotNull("No free memory provided.", heap.get("free"));
       if (java14) {
          assertNotNull("No max memory provided.", heap.get("max"));
       }
 
       try {
          Long.parseLong(heap.get("total"));
          Long.parseLong(heap.get("used"));
          Long.parseLong(heap.get("free"));
          if (java14) {
             Long.parseLong(heap.get("max"));
          }
       } catch (Exception exception) {
          fail("Incorrect value while parsing a memory size.");
       }
 
       // browse all function
       int size = children.size();
       for (int i = 1; i < size; i++) {
          DataElement nextFunction = (DataElement) children.get(i);
          assertEquals("Object other than a function has been found.", "function", nextFunction.getName());
          assertNotNull("The function does not have a name", nextFunction.get("name"));
          // XXX: Also test the children.
          List subElements = nextFunction.getChildElements();
          assertTrue(subElements.size() >= 2);
          DataElement successful = (DataElement) subElements.get(0);
          checkFunctionStatistics(successful, true);
          DataElement unsuccessful = (DataElement) subElements.get(1);
          checkFunctionStatistics(unsuccessful, false);
       }
    }
 
    /**
     * Checks that the attributes of the successful or unsuccessful result are
     * returned correctly.
     *
     * @param functionElement
     *    the successful or unsuccessful element.
     * @param successful
     *    true if it's the successful element, false if it's the unsuccessful
     *    element.
     *
     * @throws Throwable
     *    if something fails.
     */
    private void checkFunctionStatistics(DataElement functionElement, boolean successful) throws Throwable {
       String success = successful ? "successful" : "unsuccessful";
 
       assertEquals("The function does not have any " + success + " sub-section.", success, functionElement.getName());
       assertNotNull("No average attribute defined", functionElement.get("average"));
       assertNotNull("No count attribute defined", functionElement.get("count"));
       List minMaxLast = functionElement.getChildElements();
       assertTrue(minMaxLast.size() >= 3);
       DataElement min = (DataElement) minMaxLast.get(0);
       assertEquals("The function does not have any successful sub-section.", "min", min.getName());
       assertNotNull("No average attribute defined", min.get("start"));
       assertNotNull("No count attribute defined", min.get("duration"));
       DataElement max = (DataElement) minMaxLast.get(1);
       assertEquals("The function does not have any successful sub-section.", "max", max.getName());
       assertNotNull("No average attribute defined", max.get("start"));
       assertNotNull("No count attribute defined", max.get("duration"));
       DataElement last = (DataElement) minMaxLast.get(2);
       assertEquals("The function does not have any successful sub-section.", "last", last.getName());
       assertNotNull("No average attribute defined", last.get("start"));
       assertNotNull("No count attribute defined", last.get("duration"));
    }
 
    /**
     * Tests the _NoOp meta function.
     */
    public void testNoOp() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_NoOp", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned a data element.", result.getDataElement());
       assertNull("The function returned some parameters.", result.getParameters());
    }
 
    /**
     * Tests the _GetFunctionList meta function.
     */
    public void testGetFunctionList() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_GetFunctionList", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned some parameters.", result.getParameters());
       assertNotNull("The function did not return a data element.", result.getDataElement());
       List functions = result.getDataElement().getChildElements();
       int size = functions.size();
       for (int i = 0; i < size; i++) {
          DataElement nextFunction = (DataElement) functions.get(i);
          assertEquals("Element other than a function found.", "function", nextFunction.getName());
          String version = nextFunction.get("version");
          String name = nextFunction.get("name");
          String enabled = nextFunction.get("enabled");
          assertNotNull(version);
          assertNotNull(name);
          assertNotNull(enabled);
          try {
             Double.parseDouble(version);
          } catch (NumberFormatException exception) {
             fail("Incorrect version number: " + exception.getMessage());
          }
          // By default all function are enabled
          assertEquals("The function is not enabled.", "true", enabled);
       }
    }
 
    /**
     * Tests the _GetSettings meta function.
     */
    public void testGetSettings() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_GetSettings", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned some parameters.", result.getParameters());
       assertNotNull("The function did not return a data element.", result.getDataElement());
       List functions = result.getDataElement().getChildElements();
 
       assertTrue("No build section defined.", functions.size() > 0);
       DataElement build = (DataElement) functions.get(0);
       assertNull(build.getAttributes());
       assertEquals("build", build.getName());
       List buildProps = build.getChildElements();
       assertNotNull(buildProps);
       int size = buildProps.size();
       for (int i = 0; i < size; i++) {
          DataElement nextProp = (DataElement) buildProps.get(i);
          assertEquals("Element other than a property found.", "property", nextProp.getName());
          assertNotNull("No name attribute for the property.", nextProp.get("name"));
          assertNotNull("No value for the \"" + nextProp.get("name") + "\" property", nextProp.getText());
       }
 
       assertTrue("No runtime section defined.", functions.size() > 1);
       DataElement runtime = (DataElement) functions.get(1);
       assertNull(runtime.getAttributes());
       assertEquals("runtime", runtime.getName());
       List runtimeProps = runtime.getChildElements();
       assertNotNull(runtimeProps);
       size = runtimeProps.size();
       for (int i = 0; i < size; i++) {
          DataElement nextProp = (DataElement) runtimeProps.get(i);
          assertEquals("Element other than a property found.", "property", nextProp.getName());
          assertNotNull("No name attribute for the property.", nextProp.get("name"));
          assertNotNull("No value for the \"" + nextProp.get("name") + "\" property", nextProp.getText());
       }
 
       assertTrue("No system section defined.", functions.size() > 2);
       DataElement system = (DataElement) functions.get(2);
       assertNull(system.getAttributes());
       assertEquals("system", system.getName());
       List systemProps = system.getChildElements();
       assertNotNull(systemProps);
       size = systemProps.size();
       for (int i = 0; i < size; i++) {
          DataElement nextProp = (DataElement) systemProps.get(i);
          assertEquals("Element other than a property found.", "property", nextProp.getName());
          assertNotNull("No name attribute for the property.", nextProp.get("name"));
          assertNotNull("No value for the \"" + nextProp.get("name") + "\" property", nextProp.getText());
       }
    }
 
    /**
     * Tests the _DisableFunction and _EnableFunction meta functions.
     */
    public void testDisableEnableFunction() throws Throwable {
       // Test that the function is working
       BasicPropertyReader parameters = new BasicPropertyReader();
       parameters.set("inputText", "12000");
       XINSCallRequest request = new XINSCallRequest("Logdoc", parameters);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned some parameters.", result.getParameters());
       assertNull("The function returned a data element.", result.getDataElement());
 
       // Disable the function
       BasicPropertyReader parameters2 = new BasicPropertyReader();
       parameters2.set("functionName", "Logdoc");
       XINSCallRequest request2 = new XINSCallRequest("_DisableFunction", parameters2);
       XINSCallResult result2 = caller.call(request2);
       assertNull("The function returned a result code.", result2.getErrorCode());
       assertNull("The function returned some parameters.", result2.getParameters());
       assertNull("The function returned a data element.", result2.getDataElement());
 
       // Test that the function is not working anymore
       try {
          XINSCallResult result3 = caller.call(request);
          fail("The call of a disabled function did not throw an exception.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("Incorrect error code.", "_DisabledFunction", exception.getErrorCode());
          assertNull("The function returned some parameters.", exception.getParameters());
          assertNull("The function returned a data element.", exception.getDataElement());
       }
 
       // Enable the function
       XINSCallRequest request3 = new XINSCallRequest("_EnableFunction", parameters2);
       XINSCallResult result3 = caller.call(request3);
       assertNull("The function returned a result code.", result3.getErrorCode());
       assertNull("The function returned some parameters.", result3.getParameters());
       assertNull("The function returned a data element.", result3.getDataElement());
 
       // Test that the function is working
       XINSCallResult result4 = caller.call(request);
       assertNull("The function returned a result code.", result4.getErrorCode());
       assertNull("The function returned some parameters.", result4.getParameters());
       assertNull("The function returned a data element.", result4.getDataElement());
    }
 
    /**
     * Tests the _ReloadProperties meta function.
     * This test just tests if the meta function return succeeded.
     */
    public void testReloadProperties() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_ReloadProperties", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/");
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       XINSCallResult result = caller.call(request);
       assertNull("The function returned a result code.", result.getErrorCode());
       assertNull("The function returned a data element.", result.getDataElement());
       assertNull("The function returned some parameters.", result.getParameters());
    }
 
    /**
     * Tests a meta function that does not exists.
     */
    public void testUnknownMetaFunction() throws Throwable {
       XINSCallRequest request = new XINSCallRequest("_Unknown", null);
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/", 2000);
       XINSServiceCaller caller = new XINSServiceCaller(descriptor);
       try {
          XINSCallResult result = caller.call(request);
       } catch (StatusCodeHTTPCallException exception) {
          assertEquals("Incorrect status code found.", 404, exception.getStatusCode());
       }
    }
 }
