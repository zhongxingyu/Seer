 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 import com.mycompany.allinone.capi.*;
 import com.mycompany.allinone.types.*;
 
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.DataElement;
 import org.xins.client.InternalErrorException;
 import org.xins.client.InvalidRequestException;
 import org.xins.client.UnacceptableRequestException;
 import org.xins.client.UnsuccessfulXINSCallException;
 import org.xins.client.XINSCallConfig;
 import org.xins.client.XINSCallRequest;
 import org.xins.client.XINSCallResult;
 import org.xins.client.XINSServiceCaller;
 import org.xins.common.ProgrammingException;
 import org.xins.common.Utils;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.types.standard.Date;
 import org.xins.common.types.standard.Timestamp;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 import org.xins.logdoc.ExceptionUtils;
 import org.xins.logdoc.LogCentral;
 
 import org.xins.tests.AllTests;
 
 /**
  * Tests the functions in the <em>allinone</em> API using the generated CAPI
  * classes.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 public class AllInOneAPITests extends TestCase {
 
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
       return new TestSuite(AllInOneAPITests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AllInOneAPITests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public AllInOneAPITests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The target descriptor to use in all tests. This field is initialized by
     * {@link #setUp()}.
     */
    private TargetDescriptor _target;
 
    /**
     * The <code>CAPI</code> object used to call the API. This field is
     * initialized by {@link #setUp()}.
     */
    private CAPI _capi;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void setUp() throws Exception {
       _target = new TargetDescriptor(AllTests.url(), 5000, 1000, 4000);
       //_target = new TargetDescriptor("file://./src/tests/build/webapps/allinone/allinone.war", 5000, 1000, 4000);
       _capi   = new CAPI(_target);
    }
 
    /**
     * Tests CAPI regarding pre-defined types.
     */
    public void testSimpleTypes() throws Exception {
       SimpleTypesResult result =
          _capi.callSimpleTypes(Boolean.FALSE,         // _boolean
                                (byte) 8,              // _int8
                                (Short) null,          // _int16
                                65,                    // _int32
                                88L,                   // _int64
                                -32.5f,                // _float32
                                new Double(37.2),      // _float64
                                "text",                // _text
                                null,
                                null,
                                Date.fromStringForRequired("20041213"),
                                Timestamp.fromStringForOptional("20041225153255"),
                                new byte[] {25,88,66}  // _base64
          );
       assertNull(result.getOutputByte());
       assertEquals((short) -1, result.getOutputShort());
       assertEquals(16,         result.getOutputInt());
       assertEquals(14L,        result.getOutputLong());
       assertEquals("Hello ~!@#$%^&*()_+<>?[]\\;',./ \u20AC\u0630&", result.getOutputText());
       assertNull(result.getOutputText2());
       assertNull(result.getOutputProperties());
       assertEquals(Date.fromStringForRequired("20040621"), result.getOutputDate());
       assertNull(result.getOutputTimestamp());
       assertEquals(3, result.getOutputBinary().length);
       assertEquals((byte) 25, result.getOutputBinary()[0]);
       assertEquals((byte) 88, result.getOutputBinary()[1]);
       assertEquals((byte) 66, result.getOutputBinary()[2]);
    }
 
    /**
     * Tests CAPI regarding pre-defined types, using the new (XINS 1.2) CAPI
     * call methods.
     */
    public void testSimpleTypes2() throws Exception {
 
       try {
          _capi.callSimpleTypes(null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       SimpleTypesRequest request = new SimpleTypesRequest();
       UnacceptableRequestException unacceptable = request.checkParameters();
       assertNotNull("Expected UnacceptableRequestException.", unacceptable);
 
       try {
          _capi.callSimpleTypes(request);
          fail("Expected UnacceptableRequestException.");
       } catch (InvalidRequestException exception) {
          fail("Expected the client to detect unacceptable request, instead of the server.");
       } catch (UnacceptableRequestException exception) {
          // as expected
       }
 
       request.setInputByte((byte) 8);
       // request.setInputShort(null);
       request.setInputInt(65);
       request.setInputLong(88L);
       request.setInputFloat(32.5F);
       request.setInputDouble(37.2);
       request.setInputText("text");
       request.setInputText2(null);
       // request.setInputProperties(null);
       request.setInputDate(Date.fromStringForRequired("20041213"));
       request.setInputTimestamp(Timestamp.fromStringForOptional("20041225153255"));
       request.setInputBinary(new byte[] {25,88,66});
 
       // Make the call
       SimpleTypesResult result = _capi.callSimpleTypes(request);
       assertNotNull("Result returned from CAPI.callSimpleTypes(SimpleTypesRequest) is null.", result);
 
       // Check the result
       assertNull(result.getOutputByte());
       assertEquals((short) -1, result.getOutputShort());
       assertEquals(16,         result.getOutputInt());
       assertEquals(14L,        result.getOutputLong());
       assertEquals("Hello ~!@#$%^&*()_+<>?[]\\;',./ \u20AC\u0630&", result.getOutputText());
       assertNull(result.getOutputText2());
       assertNull(result.getOutputProperties());
       assertEquals(Date.fromStringForRequired("20040621"), result.getOutputDate());
       assertNull(result.getOutputTimestamp());
       assertEquals(3, result.getOutputBinary().length);
       assertEquals((byte) 25, result.getOutputBinary()[0]);
       assertEquals((byte) 88, result.getOutputBinary()[1]);
       assertEquals((byte) 66, result.getOutputBinary()[2]);
    }
 
    /**
     * Tests a function called with some missing parameters.
     */
    public void testMissingParam() throws Exception {
       try {
          SimpleTypesResult result = _capi.callSimpleTypes(Boolean.TRUE, (byte)8, null, 65, 88L, 72.5f, new Double(37.2),
             null, null, null, Date.fromStringForRequired("20041213"), Timestamp.fromStringForOptional("20041225153222"), null);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          DataElement missingParam = (DataElement) dataSection.getChildElements().get(0);
          assertEquals("missing-param", missingParam.getName());
          assertEquals("inputText", missingParam.get("param"));
          assertEquals(0, missingParam.getChildElements().size());
          assertNull(missingParam.getText());
       }
    }
 
    /**
     * Tests CAPI and defined types.
     */
    public void testDefinedTypes() throws Exception {
       TextList.Value textList = new TextList.Value();
       textList.add("hello");
       textList.add("world");
       DefinedTypesResult result = _capi.callDefinedTypes("198.165.0.1", Salutation.LADY, (byte) 28, textList, "Username1");
       assertEquals("127.0.0.1", result.getOutputIP());
       assertEquals(Salutation.LADY, result.getOutputSalutation());
       assertEquals(Byte.decode("35"), result.getOutputAge());
       assertEquals(2, result.getOutputList().getSize());
       assertEquals(2, result.getOutputProperties().size());
    }
 
    /**
     * Tests CAPI and shared types.
     */
    public void testSharedTypes() throws Exception {
       try {
          DefinedTypesResult result = _capi.callDefinedTypes("198.165.0.1", Salutation.LADY, (byte) 28, null, "User==name1");
          fail("The call to DefinedTypes should have failed because of the User==name1 invalid parameter.");
       } catch (UnsuccessfulXINSCallException exception) {
 
          // TODO Move this code in a common private method
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          List invalidParams = dataSection.getChildElements();
          DataElement invalidParam1 = (DataElement) invalidParams.get(0);
          assertEquals("invalid-value-for-type", invalidParam1.getName());
          assertEquals("inputShared", invalidParam1.get("param"));
          assertEquals(0, invalidParam1.getChildElements().size());
          assertNull(invalidParam1.getText());
       }      
    }
 
    /**
     * Tests CAPI and shared types in data section.
     */
    public void testSharedTypes2() throws Exception {
       try {
          ElementBuilder builder1 = new ElementBuilder();
          builder1.startElement("person");
          builder1.setAttribute("gender", "Mister");
          builder1.setAttribute("name", "Doe++");
          builder1.setAttribute("age", "55");
          builder1.setAttribute("birthdate", "19551206");
          Element person1 = builder1.createElement();
          ElementBuilder dataBuilder = new ElementBuilder();
          dataBuilder.startElement("data");
          dataBuilder.addChild(person1);
          Element dataSection = dataBuilder.createElement();
 
          _capi.callDataSection4(dataSection);
          fail("The call to DataSection4 should have failed because of the User==name1 invalid parameter.");
       } catch (UnsuccessfulXINSCallException exception) {
 
          // TODO Move this code in a common private method
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          List invalidParams = dataSection.getChildElements();
          DataElement invalidParam1 = (DataElement) invalidParams.get(0);
          assertEquals("invalid-value-for-type", invalidParam1.getName());
          assertEquals("name", invalidParam1.get("param"));
          assertEquals("person", invalidParam1.get("element"));
          assertEquals(0, invalidParam1.getChildElements().size());
          assertNull(invalidParam1.getText());
       }      
    }
 
    /**
     * Tests a function called with some invalid parameters.
     */
    public void testInvalidParams() throws Exception {
       TextList.Value textList = new TextList.Value();
       textList.add("Hello");
       textList.add("Test");
       try {
          DefinedTypesResult result = _capi.callDefinedTypes("not an IP", Salutation.LADY, (byte) 8, textList, null);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          List invalidParams = dataSection.getChildElements();
          DataElement invalidParam1 = (DataElement) invalidParams.get(0);
          assertEquals("invalid-value-for-type", invalidParam1.getName());
          assertEquals("inputIP", invalidParam1.get("param"));
          assertEquals(0, invalidParam1.getChildElements().size());
          assertNull(invalidParam1.getText());
          DataElement invalidParam2 = (DataElement) invalidParams.get(1);
          assertEquals("invalid-value-for-type", invalidParam2.getName());
          assertEquals("inputAge", invalidParam2.get("param"));
          assertEquals(0, invalidParam2.getChildElements().size());
          assertNull(invalidParam2.getText());
       }
    }
 
    /**
     * Tests a function that should returned a defined result code.
     */
    public void testResultCode() throws Exception {
       String result1 = _capi.callResultCode(false, "hello").getOutputText();
       assertEquals("The first call to ResultCode returned an incorrect result", "hello added.", result1);
       try {
          _capi.callResultCode(false, "hello");
          fail("The second call with the same parameter should return an AlreadySet error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("AlreadySet", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNotNull(exception.getParameters());
          assertEquals("Incorrect value for the count parameter.", "1", exception.getParameter("count"));
          assertNull(exception.getDataElement());
       }
    }
 
    /**
     * Tests a function that should return a defined result code, using the new
     * XINS 1.2-based interface.
     */
    public void testResultCode2() throws Exception {
 
       // Call with null, should be checked
       ResultCodeRequest request = null;
       try {
          _capi.callResultCode(request);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       // Call once with key, should succeed
       request = new ResultCodeRequest();
       final String key = "johny";
       request.setUseDefault(false);
       request.setInputText(key);
       _capi.callResultCode(request);
 
       // Call again with same key, should fail
       request = new ResultCodeRequest(); // XXX: Can we re-use request object?
       request.setUseDefault(false);
       request.setInputText(key);
       try {
          _capi.callResultCode(request);
          fail("The second call with the same parameter should return an AlreadySet error code.");
       } catch (AlreadySetException exception) {
          assertEquals("AlreadySet", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNotNull(exception.getParameters());
          assertEquals("Incorrect value for the count parameter.", "1", exception.getParameter("count"));
          assertNull(exception.getDataElement());
       }
    }
 
    /**
     * Tests a function that should returned a defined result code with a data section.
     */
    public void testResultCode3() throws Exception {
       try {
          _capi.callResultCode(false, null);
          fail("The second call with the same parameter should return a MissingInput error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("MissingInput", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNotNull(exception.getDataElement());
          Element inputParam = exception.getDataElement().toXMLElement().getUniqueChildElement("inputParameter");
          assertNotNull(inputParam);
          assertEquals("Incorrect value for the name parameter.", "inputText", inputParam.getAttribute("name"));
       }
    }
 
    /**
     * Tests a function that writes messages to the Logdoc.
     */
    public void testLogdoc() throws Exception {
       // This method write some text using the logdoc
       // This test doesn't check that the data written in the logs are as expected
       try {
          _capi.callLogdoc("hello");
          fail("The logdoc call should return an InvalidNumber error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("InvalidNumber", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNull(exception.getDataElement());
       }
       _capi.callLogdoc("12000");
    }
 
    /**
     * Tests a function that get values from the runtime property file.
     */
    public void testRuntimeProps() throws Exception {
       RuntimePropsResult result = _capi.callRuntimeProps(100);
       assertEquals(20.6f, result.getTaxes(), 0.01f);
       assertEquals("Euros", result.getCurrency());
    }
 
    /**
     * Tests a function that returns a data section containing elements with
     * PCDATA.
     */
    public void testDataSection() throws Exception {
       dataSectionTests("doe");
    }
 
    // Next 8 tests check special characters as parameters.
    public void testSpecialCharacters() throws Exception {
       dataSectionTests("H$llo");
    }
 
    public void testSpecialCharacters1() throws Exception {
       dataSectionTests("H&llo");
    }
 
    public void testSpecialCharacters2() throws Exception {
       dataSectionTests("H'llo");
    }
 
    public void testSpecialCharacters3() throws Exception {
       dataSectionTests("H\"llo");
    }
 
    public void testSpecialCharacters4() throws Exception {
       dataSectionTests("Bon<our");
    }
 
    public void testSpecialCharacters5() throws Exception {
       dataSectionTests("H>llo");
    }
 
    public void testSpecialCharacters6() throws Exception {
       dataSectionTests("Euro sign: \u20AC");
    }
 
    public void testSpecialCharacters7() throws Exception {
       dataSectionTests("Arabic: \u0630");
    }
 
    public void testSpecialCharacters8() throws Exception {
       dataSectionTests("ends with &");
    }
 
    public void testSpecialCharacters9() throws Exception {
       dataSectionTests("ends with \u00e9");
    }
 
    public void testFrenchLogdoc() throws Exception {
       LogCentral.setLocale("fr_FR");
       LogCentral.setStackTraceAtMessageLevel(false);
       dataSectionTests("Bonjour");
       LogCentral.setLocale("en_US");
       LogCentral.setStackTraceAtMessageLevel(true);
    }
 
    /**
     * Tests a function that returns a data section with elements that contain
     * other elements.
     */
    public void testDataSection2() throws Exception {
       DataElement element = _capi.callDataSection2("hello").dataElement();
       List packets = element.getChildElements();
       assertTrue("No destination found.", packets.size() > 0);
       DataElement packet1 = (DataElement) packets.get(0);
       assertEquals("Incorrect elements.", "packet", packet1.getName());
       assertNotNull("No destination specified.", packet1.get("destination"));
       List products = packet1.getChildElements();
       assertTrue("No product specified.", products.size() > 0);
       DataElement product1 = (DataElement) products.get(0);
       assertEquals("Incorrect price for product1", "12", product1.get("price"));
       DataElement product12 = (DataElement) products.get(1);
       assertNull("Incorrect price for product1", product12.get("price"));
 
       DataElement packet2 = (DataElement) packets.get(1);
       assertEquals("Incorrect elements.", "packet", packet2.getName());
       assertNotNull("No destination specified.", packet2.get("destination"));
       List products2 = packet2.getChildElements();
       assertTrue("No product specified.", products2.size() > 0);
       DataElement product21 = (DataElement) products2.get(0);
       assertEquals("Incorrect price for product1", "12", product21.get("price"));
       assertTrue(product21.getChildElements().size() == 0);
    }
 
   /**
    * Tests a function that passes a data section as input and an output data
    * section with mutiple data element types.
    */
    public void testDataSection3() throws Exception {
       ElementBuilder builder1 = new ElementBuilder();
       builder1.startElement("address");
       builder1.setAttribute("company", "McDo");
       builder1.setAttribute("postcode", "1234");
       Element address1 = builder1.createElement();
       ElementBuilder builder2 = new ElementBuilder();
       builder2.startElement("address");
       builder2.setAttribute("company", "Hello");
       builder2.setAttribute("postcode", "5678");
       Element address2 = builder2.createElement();
       ElementBuilder builder3 = new ElementBuilder();
       builder3.startElement("data");
       builder3.addChild(address1);
       builder3.addChild(address2);
       Element dataSection = builder3.createElement();
 
       DataElement element = _capi.callDataSection3("hello", dataSection).dataElement();
       List packets = element.getChildElements();
       assertTrue("No packets or envelopes found.", packets.size() == 4);
 
       DataElement envelope1 = (DataElement) packets.get(0);
       assertEquals("Incorrect elements.", "envelope", envelope1.getLocalName());
       assertEquals("1234", envelope1.getAttribute("destination"));
 
       DataElement envelope2 = (DataElement) packets.get(1);
       assertEquals("Incorrect elements.", "envelope", envelope2.getLocalName());
       assertEquals("5678", envelope2.getAttribute("destination"));
 
       DataElement packet1 = (DataElement) packets.get(2);
       assertEquals("Incorrect elements.", "packet", packet1.getLocalName());
       assertNotNull("No destination specified.", packet1.getAttribute("destination"));
 
       DataElement envelope3 = (DataElement) packets.get(3);
       assertEquals("Incorrect elements.", "envelope", envelope3.getLocalName());
       assertNotNull("No destination specified.", envelope3.getAttribute("destination"));
 
       // Call with no data section
       _capi.callDataSection3("hello", null);
 
       // Call with an empty data section
       ElementBuilder builder4 = new ElementBuilder();
       builder4.startElement("data");
       Element emptyDataSection = builder4.createElement();
       _capi.callDataSection3("hello", emptyDataSection);
    }
 
   /**
    * Tests the generated add and list method for the input and output data section.
    */
    public void testGeneratedClassesDataSection3() throws Exception {
       DataSection3Request.Address address1 = new DataSection3Request.Address();
       address1.setPostcode("1001PZ");
       address1.setCompany("MyCompany");
       DataSection3Request request = new DataSection3Request();
       request.addAddress(address1);
 
       // Make the call
       DataSection3Result result = _capi.callDataSection3(request);
 
       List envelopes = result.listEnvelope();
       assertEquals("Incorrect number of envelopes returned.", 2, envelopes.size());
       boolean postCodeDone = false;
       boolean washintonDone = false;
       Iterator itEnvelopes = envelopes.iterator();
       while (itEnvelopes.hasNext()) {
          DataSection3Result.Envelope envelope = (DataSection3Result.Envelope) itEnvelopes.next();
          String destination = envelope.getDestination();
          postCodeDone = postCodeDone || destination.equals("1001PZ");
          washintonDone = washintonDone || destination.equals("55 Kennedy lane, Washinton DC");
       }
       assertTrue("No postcode returned.", postCodeDone);
       assertTrue("No address returned.", washintonDone);
    }
 
   /**
    * Tests a function that passes a data section with multiple elements as
    * input.
    */
    public void testDataSection4() throws Exception {
       ElementBuilder builder1 = new ElementBuilder();
       builder1.startElement("person");
       builder1.setAttribute("gender", "Mister");
       builder1.setAttribute("name", "Doe");
       builder1.setAttribute("age", "55");
       builder1.setAttribute("birthdate", "19551206");
       Element person1 = builder1.createElement();
       ElementBuilder builder2 = new ElementBuilder();
       builder2.startElement("person");
       builder2.setAttribute("gender", "Miss");
       builder2.setAttribute("name", "Doe");
       builder2.setAttribute("age", "54");
       builder2.setAttribute("size", "154");
       builder2.setAttribute("birthdate", "19561206");
       Element person2 = builder2.createElement();
       ElementBuilder builder3 = new ElementBuilder();
       builder3.startElement("address");
       builder3.setText("22 Washintong square, 22111 London, UK");
       Element address = builder3.createElement();
       ElementBuilder builder4 = new ElementBuilder();
       builder4.startElement("data");
       builder4.addChild(person1);
       builder4.addChild(person2);
       builder4.addChild(address);
       Element dataSection = builder4.createElement();
 
       _capi.callDataSection4(dataSection);
    }
 
   /**
    * Tests the param-combos using the old-style (XINS 1.0/1.1) call methods.
    */
    public void testParamCombo1() throws Exception {
 
       // Test 'inclusive-or' and 'exclusive-or'
       try {
          _capi.callParamCombo(null, null, null, null, null, null, null);
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("inclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo2.getLocalName());
             assertEquals("exclusive-or", paramCombo2.getAttribute("type"));
          } else {
             fail("Just one param combo element was found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo3 = (DataElement)itParamCombos.next();
             fail("Unexpected param combo element of type '" +
                  paramCombo3.getAttribute("type") + "' was found.");
          }
       }
 
       // Test 'all-or-none'
       try {
          _capi.callParamCombo(null, null, new Integer(5), null, "Paris", null, new Byte((byte)33));
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("all-or-none", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
   /**
    * Tests the param-combo constraints using the new-style (XINS 1.2) call
    * methods, which should throw an UnacceptableRequestException.
    */
    public void testParamCombo2() throws Exception {
 
       // Prepare an empty request
       ParamComboRequest request = new ParamComboRequest();
       request.setBirthYear(2006);
       // not setting birthMonth
       request.setBirthDay(20);
       request.setBirthCountry("France");
       request.setBirthCity("Paris");
 
       // Attempt the call
       try {
          _capi.callParamCombo(request);
          fail("Expected UnacceptableRequestException.");
 
       // Call failed as it should
       } catch (UnacceptableRequestException exception) {
          // as expected
       }
 
       // Prepare a request
       request = new ParamComboRequest();
       request.setBirthYear(2006);
       request.setBirthMonth(5);
       request.setBirthDay(20);
       request.setBirthCountry("France");
       request.setBirthCity("Paris");
 
       // Attempt the call
       try {
          _capi.callParamCombo(request);
          fail("Expected InternalErrorException.");
 
       // Call failed as it should
       } catch (InternalErrorException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
   /**
    * Tests the param-combo for the output section.
    */
    public void testParamCombo3() throws Exception {
 
       // Test 'all-or-none'
       try {
          _capi.callParamCombo(null,
                               new Integer(2006),
                               new Integer(5),
                               new Integer(20),
                               "France",
                               "Paris",
                               null);
          fail("The param-combo call should return an _InvalidResponse error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
    /**
     * Tests the param-combo for the output section.
     */
    public void testParamComboExclusiveOr() throws Exception {
 
       // Test 'all-or-none'
       try {
          _capi.callParamCombo(Date.fromStringForRequired("20060101"),
                               null,
                               null,
                               null,
                               "Texas",
                               "Paris",
                               Byte.valueOf("21"));
 
 
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             fail("Unexpected param combo element of type '" +
                  paramCombo2.getAttribute("type") + "' was found.");
          }
       }
    }
 
    public void testParamComboValueInclusiveOr() throws Exception {
 
       // Test 'inclusive-or'
       try {
          _capi.callParamComboValue(Salutation.MADAM, null, "Lee", "France", "French", null, null);
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("inclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             fail("Unexpected param combo element of type '" +
                 paramCombo2.getAttribute("type") + "' was found.");
          }
       }
    }
 
    public void testParamComboValueExclusiveOr() throws Exception {
 
       // Test 'exclusive-or'
       try {
          _capi.callParamComboValue(Salutation.MADAM, "Martin", "Lee", "Canada", "French", null, null);
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             fail("Unexpected param combo element of type '" +
                 paramCombo2.getAttribute("type") + "' was found.");
          }
       }
 
       // Test 'exclusive-or' which should work
       _capi.callParamComboValue(Salutation.MISTER, null, "Lee", "Canada", null, null, null);
    }
 
    public void testParamComboValueAllOrNone() throws Exception {
 
       // Test 'all-or-none'
       try {
          _capi.callParamComboValue(Salutation.MADAM, "Martin", "Lee", "Other", "French", null, new Integer(2007));
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("all-or-none", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             fail("Unexpected param combo element of type '" +
                 paramCombo2.getAttribute("type") + "' was found.");
          }
       }
 
       // Test 'all-or-none' which should work
       _capi.callParamComboValue(Salutation.MISTER, null, "Lee", "Other", "English", "123ID558", new Integer(2010));
    }
 
    /**
     * Tests the attribute-combo constraints, which should throw an UnacceptableRequestException.
     */
    public void testAttributeCombo1() throws Exception {
 
       // Prepare an empty request
       AttributeComboRequest request = new AttributeComboRequest();
       AttributeComboRequest.Person person = new AttributeComboRequest.Person();
       person.setBirthYear(2006);
       // not setting birthMonth
       person.setBirthDay(20);
       person.setBirthCountry("France");
       person.setBirthCity("Paris");
       request.addPerson(person);
 
       // Attempt the call
       try {
          _capi.callAttributeCombo(request);
          fail("Expected UnacceptableRequestException.");
 
       // Call failed as it should
       } catch (UnacceptableRequestException exception) {
          // as expected
          Utils.logIgnoredException("AllinOneAPITests", "testAttributeCombo1",
                "CAPI", "callAttributeCombo", exception);
       }
 
       // Prepare a request
       request = new AttributeComboRequest();
       AttributeComboRequest.Person person2 = new AttributeComboRequest.Person();
       person2.setBirthYear(2008);
       person2.setBirthMonth(5);
       person2.setBirthDay(20);
       person2.setBirthCountry("France");
       person2.setBirthCity("Paris");
       request.addPerson(person2);
 
       // Attempt the call
       try {
          _capi.callAttributeCombo(request);
          fail("Expected InternalErrorException.");
 
       // Call failed as it should
       } catch (InternalErrorException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("attribute-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No attribute combo element found.");
          }
       }
    }
 
    /**
     * Tests the getXINSVersion() CAPI method.
     */
    public void testCAPIVersion() throws Exception {
       assertNotNull("No XINS version specified.", _capi.getXINSVersion());
       assertTrue("The version does not starts with '1.'", _capi.getXINSVersion().startsWith("1."));
    }
 
    /**
     * Tests a function that does not exists
     */
    public void testUnknownFunction() throws Exception {
       XINSCallRequest request = new XINSCallRequest("Unknown");
       XINSServiceCaller caller = new XINSServiceCaller(_target);
       try {
          XINSCallResult result = caller.call(request);
       } catch (StatusCodeHTTPCallException exception) {
          assertEquals("Incorrect status code found.", 404, exception.getStatusCode());
       }
    }
 
    public void testEcho() throws Exception {
       EchoRequest request = new EchoRequest();
       EchoResult  result;
 
       // Do not set input text
       result = _capi.callEcho(request);
       assertNull(result.getOut());
 
       // Set input text explicitly to null
       request = new EchoRequest();
       request.setIn(null);
       result = _capi.callEcho(request);
       assertNull(result.getOut());
 
       // Set input text to an empty string, which is in the end equivalent to
       // null
       request = new EchoRequest();
       request.setIn("");
       result = _capi.callEcho(request);
       assertNull(result.getOut());
 
       // Non-empty input string
       String in = "Hello there!";
       request.setIn(in);
       result = _capi.callEcho(request);
       assertEquals(in, result.getOut());
 
       // First set to one value, then override that value
       in = "Hello there!";
       request.setIn("Old value!");
       request.setIn(in);
       result = _capi.callEcho(request);
       assertEquals(in, result.getOut());
    }
 
    /**
     * Tests the function 'DefinedTypes' that should return an _InvalidResponse error code.
     */
    public void testInvalidResponse3() throws Exception {
       TextList.Value textList = new TextList.Value();
       textList.add("Hello");
       textList.add("Test");
       try {
          DefinedTypesResult result = _capi.callDefinedTypes("127.0.0.1", Salutation.LADY, (byte) 60, textList, null);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          List invalidParams = dataSection.getChildElements();
          assertEquals(1, invalidParams.size());
          DataElement invalidParam1 = (DataElement) invalidParams.get(0);
          assertEquals("invalid-value-for-type", invalidParam1.getName());
          assertEquals("outputProperties", invalidParam1.get("param"));
          assertEquals(0, invalidParam1.getChildElements().size());
          assertNull(invalidParam1.getText());
       }
    }
 
   /**
    * Tests the XINS 1.3 'not-all' param-combos type.
    */
    public void testParamComboNotAll() throws Exception {
 
       // Test 'not-all'
       try {
          Integer i = new Integer(1);
          _capi.callParamComboNotAll(i, i, i, i);
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement) itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("not-all",     paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
    public void testResetInputParameter() throws Exception {
       EchoRequest request = new EchoRequest();
       EchoResult  result;
 
       // First set to one value, then override that value with an empty string
       request.setIn("Old value!");
       request.setIn("");
       result = _capi.callEcho(request);
       assertNull("Bug 1362875: Overriding Request input value with empty string ignored.", result.getOut());
    }
 
    public void testDefaultValues() throws Exception {
 
       // Test the default returned parameter
      DefaultValueResult result = _capi.callDefaultValue(null, null, null);
       assertEquals("Test of default", result.getOutputText());
    }
 
    /* Does not work as only one instance of an API is possible
     public void testCAPIWithFileProtocol() throws Exception {
       TargetDescriptor fileTarget = new TargetDescriptor("file://./src/tests/build/webapps/allinone/allinone.war", 5000, 1000, 4000);
       CAPI fileCapi = new CAPI(fileTarget);
       EchoRequest request = new EchoRequest();
       request.setIn("Hello file");
       EchoResult result = fileCapi.callEcho(request);
       assertEquals("Incorrect message received.", "Hello file", result.getOut());
    }*/
 
    private void dataSectionTests(String inputText)
    throws Exception {
       DataElement element = _capi.callDataSection(inputText).dataElement();
       List users = element.getChildElements();
       assertTrue("No users found.", users.size() > 0);
       DataElement su = (DataElement) users.get(0);
       assertEquals("Incorrect elements.", "user", su.getName());
       assertEquals("Incorrect name for su.", "superuser", su.get("name"));
       assertEquals("Incorrect address.", "12 Madison Avenue", su.get("address"));
       assertEquals("Incorrect PCDATA.", "This user has the root authorisation.", su.getText());
       assertEquals(0, su.getChildElements().size());
       DataElement doe = (DataElement) users.get(1);
       assertEquals("Incorrect elements.", "user", doe.getName());
       assertEquals("Incorrect name for " + inputText + ".", inputText, doe.get("name"));
       assertEquals("Incorrect address.", "Unknown", doe.get("address"));
       assertNull(doe.getText());
       assertEquals(0, doe.getChildElements().size());
    }
 }
