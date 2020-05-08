 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.OptionsMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.HexConverter;
 import org.xins.common.text.ParseException;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests for calling conventions.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
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
     * Tests that when different parameter values are passed to the
     * _xins-std calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    public void testStandardCallingConvention3() throws Throwable {
       doTestMultipleParamValues("_xins-std");
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
     * Tests that when different parameter values are passed to the
     * _xins-old calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    public void testOldCallingConvention2() throws Throwable {
       doTestMultipleParamValues("_xins-old");
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
       byte[] data = result.getData();
       ElementParser parser = new ElementParser();
       return parser.parse(new StringReader(new String(data)));
    }
 
    /**
     * Test the XML calling convention.
     */
    public void testXMLCallingConvention() throws Throwable {
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, RANDOM.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       // Successful call
       postXMLRequest(randomFive, true);
 
       // Unsuccessful call
       postXMLRequest(randomFive, false);
    }
 
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
       String destination = AllTests.url() + "allinone/?_convention=_xins-xml";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<request function=\"ResultCode\">" +
               "  <param name=\"useDefault\">false</param>" +
               "  <param name=\"inputText\">" + randomFive + "</param>" +
               "</request>";
       Element result = postXML(destination, data);
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
       doTestMultipleParamValues("_xins-xslt");
    }
 
    private String getHTMLVersion(boolean useTemplateParam) throws Exception {
       TargetDescriptor descriptor = new TargetDescriptor(AllTests.url(), 2000);
       BasicPropertyReader params = new BasicPropertyReader();
       params.set("_function",  "_GetVersion");
       params.set("_convention", "_xins-xslt");
       if (useTemplateParam) {
          String userDir = new File(System.getProperty("user.dir")).toURL().toString();
          params.set("_template", userDir + "src/tests/getVersion2.xslt");
       }
       HTTPCallRequest request = new HTTPCallRequest(params);
       HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);
 
       HTTPCallResult result = caller.call(request);
       return result.getString();
    }
 
    /**
     * Tests the SOAP calling convention.
     */
    public void testSOAPCallingConvention() throws Throwable {
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, RANDOM.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       // Successful call
       postSOAPRequest(randomFive, true);
 
       // Unsuccessful call
       postSOAPRequest(randomFive, false);
    }
 
    /**
     * Posts SOAP request.
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
    private void postSOAPRequest(String randomFive, boolean success) throws Exception {
       String destination = AllTests.url() + "allinone/?_convention=_xins-soap";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"urn:allinone\">" +
               "  <soap:Body>" +
               "    <ns0:ResultCodeRequest>" +
               "      <useDefault>false</useDefault>" +
               "      <inputText>" + randomFive + "</inputText>" +
               "    </ns0:ResultCodeRequest>" +
               "  </soap:Body>" +
               "</soap:Envelope>";
       int expectedStatus = success ? 200 : 500;
       Element result = postXML(destination, data, expectedStatus);
       assertEquals("Envelope", result.getLocalName());
       assertEquals("Incorrect number of \"Fault\" elements.", 0, result.getChildElements("Fault").size());
       assertEquals("Incorrect number of \"Body\" elements.", 1, result.getChildElements("Body").size());
       Element bodyElem = (Element) result.getChildElements("Body").get(0);
       if (success) {
          assertEquals("Incorrect number of response elements.", 1, bodyElem.getChildElements("ResultCodeResponse").size());
          Element responseElem = (Element) bodyElem.getChildElements("ResultCodeResponse").get(0);
          assertEquals("Incorrect number of \"outputText\" elements.", 1, responseElem.getChildElements("outputText").size());
          Element outputTextElem = (Element) responseElem.getChildElements("outputText").get(0);
          assertEquals("Incorrect returned text", randomFive + " added.", outputTextElem.getText());
       } else {
          assertEquals("Incorrect number of \"Fault\" elements.", 1, bodyElem.getChildElements("Fault").size());
          Element faultElem = (Element) bodyElem.getChildElements("Fault").get(0);
          assertEquals("Incorrect number of \"faultcode\" elements.", 1, faultElem.getChildElements("faultcode").size());
          Element faultCodeElem = (Element) faultElem.getChildElements("faultcode").get(0);
          assertEquals("Incorrect faultcode text", "soap:Server", faultCodeElem.getText());
          assertEquals("Incorrect number of \"faultstring\" elements.", 1, faultElem.getChildElements("faultstring").size());
          Element faultStringElem = (Element) faultElem.getChildElements("faultstring").get(0);
          assertEquals("Incorrect faultstring text", "AlreadySet", faultStringElem.getText());
       }
    }
 
    /**
     * Tests the SOAP calling convention for the type convertion.
     */
    public void testSOAPCallingConvention2() throws Throwable {
       String destination = AllTests.url() + "allinone/?_convention=_xins-soap";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"urn:allinone\">" +
               "  <soap:Body>" +
               "    <ns0:SimpleTypesRequest>" +
               "      <inputBoolean>0</inputBoolean>" +
               "      <inputByte>0</inputByte>" +
               "      <inputInt>0</inputInt>" +
               "      <inputLong>0</inputLong>" +
               "      <inputFloat>1.0</inputFloat>" +
               "      <inputText>0</inputText>" +
               "    </ns0:SimpleTypesRequest>" +
               "  </soap:Body>" +
               "</soap:Envelope>";
       Element result = postXML(destination, data);
       assertEquals("Envelope", result.getLocalName());
       assertEquals("Incorrect number of \"Fault\" elements.", 0, result.getChildElements("Fault").size());
       assertEquals("Incorrect number of \"Body\" elements.", 1, result.getChildElements("Body").size());
       Element bodyElem = (Element) result.getChildElements("Body").get(0);
       assertEquals("Incorrect number of response elements.", 1, bodyElem.getChildElements("SimpleTypesResponse").size());
    }
 
    /**
     * Tests the SOAP calling convention with a data section.
     */
    public void testSOAPCallingConvention3() throws Throwable {
       String destination = AllTests.url() + "allinone/?_convention=_xins-soap";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"urn:allinone\">" +
               "  <soap:Body>" +
               "    <ns0:DataSection3Request>" +
               "      <data>" +
               "        <address company=\"McDo\" postcode=\"1234\" />" +
               "        <address company=\"Drill\" postcode=\"4567\" />" +
               "      </data>" +
               "    </ns0:DataSection3Request>" +
               "  </soap:Body>" +
               "</soap:Envelope>";
       Element result = postXML(destination, data);
       assertEquals("Envelope", result.getLocalName());
       assertEquals("Incorrect number of \"Fault\" elements.", 0, result.getChildElements("Fault").size());
       assertEquals("Incorrect number of \"Body\" elements.", 1, result.getChildElements("Body").size());
       Element bodyElem = (Element) result.getChildElements("Body").get(0);
       assertEquals("Incorrect number of response elements.", 1, bodyElem.getChildElements("DataSection3Response").size());
    }
 
    /**
     * Tests the XML-RPC calling convention with an incomplete request.
     */
    public void testXMLRPCCallingConvention() throws Exception {
       String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";
 
       // Send an incorrect request
       String data = "<?xml version=\"1.0\"?>" +
               "<methodCall>" +
               "  <methodName>SimpleTypes</methodName>" +
               "  <params>" +
               "    <param><value><struct><member>" +
               "    <name>inputBoolean</name>" +
               "    <value><boolean>0</boolean></value>" +
               "    </member></struct></value></param>" +
               "  </params>" +
               "</methodCall>";
       Element result = postXML(destination, data);
       assertEquals("methodResponse", result.getLocalName());
       Element faultElem = getUniqueChild(result, "fault");
       Element valueElem = getUniqueChild(faultElem, "value");
       Element structElem = getUniqueChild(valueElem, "struct");
       Element member1 = (Element) structElem.getChildElements("member").get(0);
       Element member1Name = getUniqueChild(member1, "name");
       assertEquals("faultCode", member1Name.getText());
       Element member1Value = getUniqueChild(member1, "value");
       Element member1IntValue = getUniqueChild(member1Value, "int");
       assertEquals("3", member1IntValue.getText());
       Element member2 = (Element) structElem.getChildElements("member").get(1);
       Element member2Name = getUniqueChild(member2, "name");
       assertEquals("faultString", member2Name.getText());
       Element member2Value = getUniqueChild(member2, "value");
       Element member2StringValue = getUniqueChild(member2Value, "string");
       assertEquals("_InvalidRequest", member2StringValue.getText());
    }
 
    /**
     * Tests the XML-RPC calling convention for a successful result.
     */
    public void testXMLRPCCallingConvention2() throws Exception {
       String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";
 
       // Send a correct request
       String data = "<?xml version=\"1.0\"?>" +
               "<methodCall>" +
               "  <methodName>SimpleTypes</methodName>" +
               "  <params>" +
               "    <param><value><struct><member>" +
               "    <name>inputBoolean</name>" +
               "    <value><boolean>1</boolean></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputByte</name>" +
               "    <value><i4>0</i4></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputInt</name>" +
               "    <value><i4>50</i4></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputLong</name>" +
               "    <value><string>123456460</string></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputFloat</name>" +
               "    <value><double>3.14159265</double></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputText</name>" +
               "    <value><string>Hello World!</string></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputDate</name>" +
               "    <value><dateTime.iso8601>19980717T14:08:55</dateTime.iso8601></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>inputTimestamp</name>" +
               "    <value><dateTime.iso8601>19980817T15:08:55</dateTime.iso8601></value>" +
               "    </member></struct></value></param>" +
               "  </params>" +
               "</methodCall>";
       Element result = postXML(destination, data);
       assertEquals("methodResponse", result.getLocalName());
       Element paramsElem = getUniqueChild(result, "params");
       Element paramElem = getUniqueChild(paramsElem, "param");
       Element valueElem = getUniqueChild(paramElem, "value");
       Element structElem = getUniqueChild(valueElem, "struct");
    }
 
    /**
     * Tests the XML-RPC calling convention for a data section.
     */
    public void testXMLRPCCallingConvention3() throws Exception {
       String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";
 
       // Send a correct request
       String data = "<?xml version=\"1.0\"?>" +
               "<methodCall>" +
               "  <methodName>DataSection3</methodName>" +
               "  <params>" +
               "    <param><value><struct><member>" +
               "    <name>inputText</name>" +
               "    <value><string>hello</string></value>" +
               "    </member></struct></value></param>" +
               "    <param><value><struct><member>" +
               "    <name>data</name>" +
               "    <value><array><data>" +
               "    <value><struct><member>" +
               "    <name>address</name>" +
               "    <value><string></string></value>" +
               "    </member>" +
               "    <member>" +
               "    <name>company</name>" +
               "    <value><string>MyCompany</string></value>" +
               "    </member>" +
               "    <member>" +
               "    <name>postcode</name>" +
               "    <value><string>72650</string></value>" +
               "    </member></struct></value>" +
               "    </data></array></value>" +
               "    </member></struct></value></param>" +
               "  </params>" +
               "</methodCall>";
       Element result = postXML(destination, data);
       assertEquals("methodResponse", result.getLocalName());
       Element paramsElem = getUniqueChild(result, "params");
       Element paramElem = getUniqueChild(paramsElem, "param");
       Element valueElem = getUniqueChild(paramElem, "value");
       Element structElem = getUniqueChild(valueElem, "struct");
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
       HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
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
    private Element postXML(String destination, String data) throws Exception {
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
    private Element postXML(String destination, String data, int expectedStatus)
    throws Exception {
 
       PostMethod post = new PostMethod(destination);
       post.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
       post.setRequestBody(data);
       HttpClient client = new HttpClient();
       client.setConnectionTimeout(5000);
       client.setTimeout(5000);
       try {
          int code = client.executeMethod(post);
          assertEquals(expectedStatus, code);
          byte[] returnedData = post.getResponseBody();
          ElementParser parser = new ElementParser();
          String content = new String(returnedData);
          System.err.println("content: " + content);
          Element result = parser.parse(new StringReader(content));
          return result;
       } finally {
 
          // Release current connection to the connection pool once you are done
          post.releaseConnection();
       }
    }
 
    /**
     * Gets the unique child of the element.
     *
     * @param parentElement
     *    the parent element, cannot be <code>null</code>.
     *
     * @param elementName
     *    the name of the child element to get, or <code>null</code> if the
     *    parent have a unique child.
     *
     * @throws InvalidRequestException
     *    if no child was found or more than one child was found.
     */
    private Element getUniqueChild(Element parentElement, String elementName)
    throws ParseException {
       List childList = null;
       if (elementName == null) {
          childList = parentElement.getChildElements();
       } else {
          childList = parentElement.getChildElements(elementName);
       }
       if (childList.size() == 0) {
          throw new ParseException("No \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
                "\" element of the XML-RPC request.");
       } else if (childList.size() > 1) {
          throw new ParseException("More than one \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
                "\" element of the XML-RPC request.");
       }
       return (Element) childList.get(0);
    }
 
    /**
     * Tests that when different parameter values are passed to the
     * specified calling convention, it must return a 400 status code
     * (invalid HTTP request).
     */
    private void doTestMultipleParamValues(String convention)
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
          HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
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
          HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
          // Expect "501 Not Implemented"
          assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                       "501 Not Implemented", result.getStatus());
       }
 
       // Same, but now without the content length header
       headers = new Properties();
       for (int i=0; i<unknown.length; i++) {
          String method = unknown[i];
 
          // Call the server
          HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
          // Expect "501 Not Implemented"
          assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                       "501 Not Implemented", result.getStatus());
       }
    }
 
    /**
     * Tests that a HEAD request returns a correct Content-Length header, but 
     * no actual content.
     */
    public void testContentLengthFromHEAD() throws Exception {
 
       String     queryString = "/?_convention=_xins-std&_function=Echo";
       String     host        = AllTests.host();
       int        port        = AllTests.port();
       Properties headers     = null;
 
       // Perform a GET
       String method = "GET";
       HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
       // Content-length header should be set and correct
       List lengthHeaders = result.getHeaderValues("content-length");
       assertEquals("Expected GET request to return 1 \"Content-Length\" header. Instead it returned " + lengthHeaders.size(), 1, lengthHeaders.size());
       int bodyLength = result.getBody().length();
       int lengthHeader = Integer.parseInt((String) lengthHeaders.get(0));
       assertEquals("Expected \"Content-Length\" header from GET request (" + lengthHeader + ") to match actual body length (" + bodyLength + ").", bodyLength, lengthHeader);
 
       // Perform a HEAD
       method = "HEAD";
       result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
       // Content-length header should be set and correct
       lengthHeaders = result.getHeaderValues("content-length");
       assertEquals("Expected HEAD request to return 1 \"Content-Length\" header. Instead it returned " + lengthHeaders.size(), 1, lengthHeaders.size());
       assertEquals("Expected actual HEAD response length to be 0.", 0, result.getBody().length());
       lengthHeader = Integer.parseInt((String) lengthHeaders.get(0));
       assertEquals("Expected \"Content-Length\" header from HEAD request (" + lengthHeader + ") to match the one from GET (" + bodyLength + ").", bodyLength, lengthHeader);
    }
 
    public void testFileContentLength() throws Exception {
 
      String     fileName    = "src/tests/apis/allinone/spec/Age.typ";
       String     queryString = "/specs/Age.typ";
       String     host        = AllTests.host();
       int        port        = AllTests.port();
       Properties headers     = null;
 
       // Perform a GET
       String method = "GET";
       HTTPCaller.Result result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
 
       // Status should be 200 OK
       assertEquals("Expected 200 OK in response to HTTP/1.1 GET request.", "200 OK", result.getStatus());
 
       // Content-length header should be set and correct
       List lengthHeaders = result.getHeaderValues("content-length");
       assertEquals("Expected GET request to return 1 \"Content-Length\" header instead of " + lengthHeaders.size() + '.', 1, lengthHeaders.size());
      long lengthHeader = Long.parseLong((String) lengthHeaders.get(0));
      long expectedLength = determineFileSize(fileName);;
      assertEquals("Expected \"Content-Length\" header for \"" + queryString + "\" to return " + expectedLength + '.', expectedLength, lengthHeader);
      long bodyLength = result.getBody().length();
      assertEquals("Expected \"Content-Length\" header from GET request (" + lengthHeader + ") to match actual body length (" + bodyLength + ").", bodyLength, lengthHeader);
   }

   private long determineFileSize(String fileName) throws Exception {
      File file = new File(fileName);
      if (!file.exists()) {
         throw new Exception("File \"" + file.getName() + "\" does not exist. Absolute: \"" + file.getAbsolutePath() + "\". Canonical: \"" + file.getCanonicalPath() + "\".");
      }
      return file.length();
    }
 
    public void testHTTP_1_0() throws Exception {
 
       String     queryString = "/?_convention=_xins-std&_function=Echo";
       String     host        = AllTests.host();
       int        port        = AllTests.port();
       Properties headers     = null;
 
       // Perform a GET
       String method = "GET";
       HTTPCaller.Result result = HTTPCaller.call("1.0", host, port, method, queryString, headers);
 
       // Status should be 200 OK
       assertEquals("Expected 200 OK in response to HTTP/1.0 GET request.", "200 OK", result.getStatus());
    }
 }
