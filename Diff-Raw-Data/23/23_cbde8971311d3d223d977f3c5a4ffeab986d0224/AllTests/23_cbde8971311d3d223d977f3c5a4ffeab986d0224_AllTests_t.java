 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests;
 
 import java.io.File;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.xins.common.servlet.container.HTTPServletHandler;
 
 /**
  * Combination of all XINS/Java tests.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class AllTests extends TestSuite {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    public static HTTPServletHandler HTTP_SERVER;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       TestSuite suite = new TestSuite();
 
       // Start the server
       suite.addTestSuite(StartServer.class);
 
       //
       // Add all tests
       //
 
       suite.addTestSuite(org.xins.tests.common.ExceptionUtilsTests.class);
       suite.addTestSuite(org.xins.tests.common.MandatoryArgumentCheckerTests.class);
       suite.addTestSuite(org.xins.tests.common.UtilsTests.class);
 
       suite.addTestSuite(org.xins.tests.common.collections.BasicPropertyReaderTests.class);
       suite.addTestSuite(org.xins.tests.common.collections.CollectionUtilsTests.class);
       suite.addTestSuite(org.xins.tests.common.collections.PropertyReaderUtilsTests.class);
       suite.addTestSuite(org.xins.tests.common.collections.ProtectedListTests.class);
       suite.addTestSuite(org.xins.tests.common.collections.ProtectedPropertyReaderTests.class);
 
       suite.addTestSuite(org.xins.tests.common.collections.expiry.ExpiryFolderTests.class);
       suite.addTestSuite(org.xins.tests.common.collections.expiry.ExpiryStrategyTests.class);
 
       suite.addTestSuite(org.xins.tests.common.net.IPAddressUtilsTests.class);
       suite.addTestSuite(org.xins.tests.common.net.URLEncodingTests.class);
 
       suite.addTestSuite(org.xins.tests.common.xml.ElementParserTests.class);
 
       suite.addTestSuite(org.xins.tests.common.spec.APITests.class);
       suite.addTestSuite(org.xins.tests.common.spec.DataSectionElementTests.class);
       suite.addTestSuite(org.xins.tests.common.spec.ErrorCodeTests.class);
       suite.addTestSuite(org.xins.tests.common.spec.FunctionTests.class);
       suite.addTestSuite(org.xins.tests.common.spec.ParamComboTests.class);
       suite.addTestSuite(org.xins.tests.common.spec.ParameterTests.class);
       
       suite.addTestSuite(org.xins.tests.common.service.DescriptorBuilderTests.class);
       suite.addTestSuite(org.xins.tests.common.service.TargetDescriptorTests.class);
       suite.addTestSuite(org.xins.tests.common.service.GroupDescriptorTests.class);
       suite.addTestSuite(org.xins.tests.common.service.UnsupportedProtocolExceptionTests.class);
 
       suite.addTestSuite(org.xins.tests.common.text.HexConverterTests.class);
       suite.addTestSuite(org.xins.tests.common.text.PatternParserTests.class);
       suite.addTestSuite(org.xins.tests.common.text.SimplePatternParserTests.class);
 
       suite.addTestSuite(org.xins.tests.common.text.FastStringBufferTest.class);
 
       suite.addTestSuite(org.xins.tests.common.types.standard.BooleanTests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.DateTests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.DescriptorTests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Int8Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Int16Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Int32Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Int64Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Float32Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Float64Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.Base64Tests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.PropertiesTests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.TimestampTests.class);
       suite.addTestSuite(org.xins.tests.common.types.standard.URLTests.class);
 
       suite.addTestSuite(org.xins.tests.common.http.HTTPServiceCallerTests.class);
 
       suite.addTestSuite(org.xins.tests.client.AllInOneAPITests.class);
       suite.addTestSuite(org.xins.tests.client.CAPITests.class);
       suite.addTestSuite(org.xins.tests.client.DataElementTests.class);
       suite.addTestSuite(org.xins.tests.client.InvalidRequestTests.class);
       suite.addTestSuite(org.xins.tests.client.UnacceptableResultXINSCallExceptionTests.class);
       suite.addTestSuite(org.xins.tests.client.XINSCallRequestTests.class);
       suite.addTestSuite(org.xins.tests.client.XINSCallResultParserTests.class);
       suite.addTestSuite(org.xins.tests.client.XINSServiceCallerTests.class);
       suite.addTestSuite(org.xins.tests.client.InvalidResponseTests.class);
 
       suite.addTestSuite(org.xins.tests.server.IPFilterTests.class);
       suite.addTestSuite(org.xins.tests.server.AccessRuleListTests.class);
       suite.addTestSuite(org.xins.tests.server.AccessRuleTests.class);
       suite.addTestSuite(org.xins.tests.server.CallingConventionTests.class);
       suite.addTestSuite(org.xins.tests.server.MetaFunctionsTests.class);
       
 
       // XXX: Perform just a single test
      //suite.addTest(new org.xins.tests.server.CallingConventionTests("testSOAPCallingConvention2"));
 
       // Stop the server
       suite.addTestSuite(StopServer.class);
 
       return suite;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AllTests</code> object with the specified name.
     * The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test case.
     */
    public AllTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
