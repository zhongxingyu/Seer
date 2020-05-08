 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.XINSServiceCaller;
 
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.UnsupportedProtocolException;
 
import org.xins.tests.server.servlet.HTTPServletHandler;

 /**
  * Tests the <code>XINSServiceCaller</code>.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class XINSServiceCallerTests extends TestCase {
 
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
       return new TestSuite(XINSServiceCallerTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
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
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests the constructor arguments.
     */
    public void testXINSServiceCaller_constructor() throws Throwable {
       XINSServiceCaller caller = new XINSServiceCaller(null);
       assertEquals(null, caller.getDescriptor());
 
       // TODO: Move this test to a different function
       try {
          TargetDescriptor descriptor2 = new TargetDescriptor("blah://127.0.0.1:8080/");
          caller.setDescriptor(descriptor2);
          fail("The \"blah\" protocol should not be supported.");
       } catch (UnsupportedProtocolException upe) {
          // As expected.
       }
 
       TargetDescriptor descriptor = new TargetDescriptor("hTtP://127.0.0.1:8080/");
       caller = new XINSServiceCaller(descriptor);
 
       try {
          TargetDescriptor descriptor2 = new TargetDescriptor("blah://127.0.0.1:8080/");
          XINSServiceCaller caller2 = new XINSServiceCaller(descriptor2);
          fail("The \"blah\" protocol should not be supported.");
       } catch (UnsupportedProtocolException upe) {
          // As expected.
       }
    }
 }
