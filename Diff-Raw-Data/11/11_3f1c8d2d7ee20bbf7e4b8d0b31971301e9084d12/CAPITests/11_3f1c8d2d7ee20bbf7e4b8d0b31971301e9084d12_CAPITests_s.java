 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 import com.mycompany.allinone.capi.*;
 import com.mycompany.allinone.types.*;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.UnsuccessfulXINSCallException;
 import org.xins.client.XINSCallConfig;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 
 import org.xins.common.service.*;
 
 import org.xins.logdoc.ExceptionUtils;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests the generated <em>allinone</em> CAPI class, other than calling the
  * actual functions.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class CAPITests extends TestCase {
 
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
       return new TestSuite(CAPITests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CAPITests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public CAPITests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void testCAPIConstruction_XINS_1_0() throws Exception {
 
       TargetDescriptor td;
       CAPI capi;
 
       // Pass null to constructor (should fail)
       try {
          new CAPI((Descriptor) null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
 
       // Pass URL with unsupported protocol
       td = new TargetDescriptor("bla://www.xins.org/");
       try {
          new CAPI(td);
          fail("Expected UnsupportedProtocolException.");
       } catch (UnsupportedProtocolException exception) {
          assertEquals(td, exception.getTargetDescriptor());
       }
 
       // Pass URL with supported protocol
       td = new TargetDescriptor("http://www.xins.org/");
       capi = new CAPI(td);
       assertNotNull(capi.getXINSCallConfig());
       assertNotNull(capi.getXINSVersion());
    }
 
    public void testCAPIConstruction_XINS_1_1() throws Exception {
 
       TargetDescriptor td;
       CAPI capi;
       String url;
 
       BasicPropertyReader properties;
 
       // Pass nulls (should fail)
       properties = new BasicPropertyReader();
       try {
          CAPI.create((PropertyReader) null, (String) null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
       try {
          CAPI.create((PropertyReader) null, "SomeName");
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
       try {
          CAPI.create(properties, null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
 
       try {
          CAPI.create((PropertyReader) null, (String) null, (XINSCallConfig) null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
       try {
          CAPI.create(properties, (String) null, (XINSCallConfig) null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
       try {
          CAPI.create((PropertyReader) null, "SomeName", (XINSCallConfig) null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) { }
 
       // Pass empty property set (missing required property)
       try {
          CAPI.create(properties, "allinone");
          fail("Expected MissingRequiredPropertyException.");
       } catch (MissingRequiredPropertyException exception) {
          assertEquals("capis.allinone", exception.getPropertyName());
       }
       try {
          CAPI.create(properties, "allinone", (XINSCallConfig) null);
          fail("Expected MissingRequiredPropertyException.");
       } catch (MissingRequiredPropertyException exception) {
          assertEquals("capis.allinone", exception.getPropertyName());
       }
 
       // Pass property set with missing property
       properties = new BasicPropertyReader();
       properties.set("capis.allinone",     "group, ordered, one, two");
       properties.set("capis.allinone.one", "service, http://xins.org/, 5000");
       try {
          CAPI.create(properties, "allinone");
          fail("Expected MissingRequiredPropertyException.");
       } catch (MissingRequiredPropertyException exception) {
          assertEquals("capis.allinone.two", exception.getPropertyName());
       }
 
       // Pass invalid property value
       properties = new BasicPropertyReader();
       url = "bla://www.xins.org/";
       properties.set("capis.allinone", url);
       try {
          CAPI.create(properties, "allinone");
          fail("Expected InvalidPropertyValueException.");
       } catch (InvalidPropertyValueException exception) {
          assertEquals("capis.allinone", exception.getPropertyName());
          assertEquals(url, exception.getPropertyValue());
       }
 
       // Pass URL with unsupported protocol at root
       properties = new BasicPropertyReader();
       url = "bla://www.xins.org/";
       properties.set("capis.allinone", "service, " + url + ", 5000");
       try {
          CAPI.create(properties, "allinone");
       } catch (InvalidPropertyValueException exception) {
          assertEquals("capis.allinone", exception.getPropertyName());
          assertEquals(properties.get("capis.allinone"), exception.getPropertyValue());
          Throwable cause = ExceptionUtils.getCause(exception);
          assertNotNull("Expected InvalidPropertyValueException to have an UnsupportedProtocolException as the cause.", cause);
          assertTrue("Expected cause of InvalidPropertyValueException to be an UnsupportedProtocolException instead of an instance of class " + cause.getClass().getName(), cause instanceof UnsupportedProtocolException);
          UnsupportedProtocolException upe = (UnsupportedProtocolException) cause;
          assertEquals(url, upe.getTargetDescriptor().getURL());
       }
       try {
          CAPI.create(properties, "allinone", (XINSCallConfig) null);
       } catch (InvalidPropertyValueException exception) {
          Throwable cause = ExceptionUtils.getCause(exception);
          assertNotNull("Expected InvalidPropertyValueException to have an UnsupportedProtocolException as the cause.", cause);
          assertTrue("Expected cause of InvalidPropertyValueException to be an UnsupportedProtocolException instead of an instance of class " + cause.getClass().getName(), cause instanceof UnsupportedProtocolException);
          UnsupportedProtocolException upe = (UnsupportedProtocolException) cause;
          assertEquals(url, upe.getTargetDescriptor().getURL());
       }
 
       // Pass URL with unsupported protocol at leaf
       properties = new BasicPropertyReader();
       url = "bla://xins.org/";
       properties.set("capis.allinone",     "group, ordered, one, two");
       properties.set("capis.allinone.one", "service, http://xins.org/, 5000");
       properties.set("capis.allinone.two", "service, " + url + ", 5000");
       try {
          CAPI.create(properties, "allinone");
       } catch (InvalidPropertyValueException exception) {
          assertEquals("Expected invalid property value on property \"capis.allinone.two\" instead of on property \"" + exception.getPropertyName() + "\".", "capis.allinone.two", exception.getPropertyName());
          assertEquals(properties.get("capis.allinone.two"), exception.getPropertyValue());
          Throwable cause = ExceptionUtils.getCause(exception);
          assertNotNull("Expected InvalidPropertyValueException to have an UnsupportedProtocolException as the cause.", cause);
          assertTrue("Expected cause of InvalidPropertyValueException to be an UnsupportedProtocolException instead of an instance of class " + cause.getClass().getName(), cause instanceof UnsupportedProtocolException);
          UnsupportedProtocolException upe = (UnsupportedProtocolException) cause;
          assertEquals(url, upe.getTargetDescriptor().getURL());
       }
       try {
          CAPI.create(properties, "allinone", (XINSCallConfig) null);
       } catch (InvalidPropertyValueException exception) {
          assertEquals("Expected invalid property value on property \"capis.allinone.two\" instead of on property \"" + exception.getPropertyName() + "\".", "capis.allinone.two", exception.getPropertyName());
          assertEquals(properties.get("capis.allinone.two"), exception.getPropertyValue());
          Throwable cause = ExceptionUtils.getCause(exception);
          assertNotNull("Expected InvalidPropertyValueException to have an UnsupportedProtocolException as the cause.", cause);
          assertTrue("Expected cause of InvalidPropertyValueException to be an UnsupportedProtocolException instead of an instance of class " + cause.getClass().getName(), cause instanceof UnsupportedProtocolException);
          UnsupportedProtocolException upe = (UnsupportedProtocolException) cause;
          assertEquals(url, upe.getTargetDescriptor().getURL());
       }
    }
    
    public void testCompatibility() throws Exception {
       // Add the servlet
       AllTests.HTTP_SERVER.addServlet("org.xins.tests.client.MyProjectServlet", "/myproject");
       
       try {
          TargetDescriptor descriptor = new TargetDescriptor("http://localhost:8080/myproject");
          com.mycompany.myproject.capi.CAPI capi = new com.mycompany.myproject.capi.CAPI(descriptor);
          capi.callMyFunction(com.mycompany.myproject.types.Gender.MALE, "Bnd");
          fail("callMyFunction succeeded even with a invalid name");
       } catch (UnsuccessfulXINSCallException ex) {
          assertEquals("NoVowel", ex.getErrorCode());
       }
       
       // Remove the servlet
       AllTests.HTTP_SERVER.removeServlet("/myproject");
    }
 }
