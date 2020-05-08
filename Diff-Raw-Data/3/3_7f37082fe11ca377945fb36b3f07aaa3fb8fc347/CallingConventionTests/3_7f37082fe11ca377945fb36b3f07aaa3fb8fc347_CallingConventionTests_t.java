 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.File;
 import java.io.StringReader;
 
 import java.util.List;
 import java.util.Random;
 
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
 import org.xins.common.text.ParseException;
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
     * Tests with an unknown calling convention.
     */
    public void testInvalidCallingConvention() throws Throwable {
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/", 2000);
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
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/allinone/", 2000);
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-xml";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<request function=\"ResultCode\">" +
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
    public void testXSLTCallingConvention() throws Throwable {
       String html = getHTMLVersion(false);
       assertTrue("The returned data is not an HTML file.", html.startsWith("<html>"));
       assertTrue("Incorrect HTML data returned.", html.indexOf("XINS version") != -1);
 
       String html2 = getHTMLVersion(true);
       assertTrue("The returned data is not an HTML file.", html2.startsWith("<html>"));
       assertTrue("Incorrect HTML data returned.", html2.indexOf("API version") != -1);
    }
 
    private String getHTMLVersion(boolean useTemplateParam) throws Exception {
       TargetDescriptor descriptor = new TargetDescriptor("http://127.0.0.1:8080/", 2000);
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-soap";
       String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"urn:allinone\">" +
               "  <soap:Body>" +
               "    <ns0:ResultCodeRequest>" +
               "      <inputText>" + randomFive + "</inputText>" +
               "    </ns0:ResultCodeRequest>" +
               "  </soap:Body>" +
               "</soap:Envelope>";
       Element result = postXML(destination, data);
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-soap";
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-soap";
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-xmlrpc";
 
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-xmlrpc";
 
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
       String destination = "http://127.0.0.1:8080/allinone/?_convention=_xins-xmlrpc";
 
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
    /*public void testCustomCallingConvention() throws Exception {
       URL url = new URL("http://localhost:8080/?query=hello%20Custom");
       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
       connection.connect();
       assertEquals(200, connection.getResponseCode());
       URL url2 = new URL("http://localhost:8080/?query=hello%20Custom&_convention=xins-tests");
       HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
       connection2.connect();
       assertEquals(400, connection2.getResponseCode());
    }*/
 
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
       PostMethod post = new PostMethod(destination);
       post.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
       post.setRequestBody(data);
       HttpClient client = new HttpClient();
       client.setConnectionTimeout(5000);
       client.setTimeout(5000);
       try {
          int code = client.executeMethod(post);
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
 }
