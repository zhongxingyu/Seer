 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.perftests;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.text.ParseException;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.types.standard.Date;
 import org.xins.common.types.standard.Timestamp;
 
 import org.xins.client.DataElement;
 import org.xins.client.XINSCallRequest;
 import org.xins.client.XINSCallResult;
 import org.xins.client.XINSServiceCaller;
 import org.xins.client.UnsuccessfulXINSCallException;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 
 import org.xins.common.servlet.container.HTTPServletHandler;
 
 import com.mycompany.allinone.capi.CAPI;
 import com.mycompany.allinone.capi.DefinedTypesResult;
 import com.mycompany.allinone.capi.SimpleTypesResult;
 import com.mycompany.allinone.types.Salutation;
 import com.mycompany.allinone.types.TextList;
 
 /**
  * Tests the allinone functions using the generated CAPI.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class AllInOneTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    private static final int ROUNDS = 40;
 
 
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
       return new TestSuite(AllInOneTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AllInOneTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public AllInOneTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The HTTP server used to handle the requests.
     */
    private HTTPServletHandler _httpServer;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Starts the HTTP server with the correct parameters.
     */
    protected void setUp() throws ServletException, IOException {
       File xinsProps = new File(System.getProperty("user.dir"), "src/tests/xins.properties");
       System.setProperty("org.xins.server.config", xinsProps.getAbsolutePath());
       String warLocation = "src/tests/build/webapps/allinone/allinone.war".replace('/', File.separatorChar);
       File warFile = new File(System.getProperty("user.dir"), warLocation);
 
       // Start the web server
       //System.out.println("Web server set up.");
       _httpServer = new HTTPServletHandler(warFile);
    }
 
    /**
     * Tests the performance of the <code>XMLEncoder</code> in the ASCII
     * encoding.
     *
     * @throws Exception
     *    in case of an error.
     */
    public void testPerformanceAllInOne()
    throws Exception {
       doTestAllInOne(true);
    }
    public void testOverheadAllInOne()
    throws Exception {
       doTestAllInOne(false);
    }
 
    /**
     * Tests the performance of calling all the function of the AllInOne API.
     *
     * @param real
     *    flag that indicates whether the test should really be run, if
     *    <code>false</code> then only the overhead is tested.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void doTestAllInOne(boolean real)
    throws Exception {
 
       for (int i = 0; i < ROUNDS; i++) {
 
          System.currentTimeMillis();
 
          if (! real) {
             continue;
          }
 
          callAllFunctionsAllInOne();
       }
    }
 
    /**
     * Call all the functions of the AllInOne API.
     */
    public void callAllFunctionsAllInOne() throws Exception {
 
       // Test the function the one after each other
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/allinone/");
       CAPI allInOne = new CAPI(descriptor);
       SimpleTypesResult result1 = allInOne.callSimpleTypes(null, (byte)8, null, 65, 88l, 32.5f, new Double(37.2),
         "text", null, null, Date.fromStringForRequired("20041213"), Timestamp.fromStringForOptional("20041225153255"), new byte[]{25,88,66});
       assertEquals("hello", result1.getOutputText());
 
       try {
          allInOne.callSimpleTypes(null, (byte)8, null, 65, 88l, 72.5f, new Double(37.2),
             null, null, null, Date.fromStringForRequired("20041213"), Timestamp.fromStringForOptional("20041225153222"), null);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
       }
 
       TextList.Value textList = new TextList.Value();
       textList.add("hello");
       textList.add("world");
       DefinedTypesResult result2 = allInOne.callDefinedTypes("198.165.0.1", Salutation.LADY, (byte)28, textList);
       assertEquals("127.0.0.1", result2.getOutputIP());
 
       try {
          allInOne.callDefinedTypes("not an IP", Salutation.LADY, (byte)8, textList);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
       }
 
       try {
          allInOne.callResultCode(false, "hello");
       } catch (UnsuccessfulXINSCallException exception) {
          // Expected after the first call
       }
 
       try {
          allInOne.callLogdoc("hello");
          fail("The logdoc call should return an InvalidNumber error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("InvalidNumber", exception.getErrorCode());
       }
       allInOne.callLogdoc("12000");
 
       DataElement element1 = allInOne.callDataSection("Doe").dataElement();
       List users = element1.getChildElements();
       assertTrue("No users found.", users.size() > 0);
 
       DataElement element2 = allInOne.callDataSection2("hello").dataElement();
       List packets = element2.getChildElements();
       assertTrue("No destination found.", packets.size() > 0);
 
       try {
          allInOne.callParamCombo(null, null, new Integer(5), null, "Paris", null, new Byte((byte)33));
          fail("The ParamCombo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
       }
    }
 
    /**
     * Stop the server.
     */
    protected void tearDown() {
       _httpServer.close();
    }
 }
