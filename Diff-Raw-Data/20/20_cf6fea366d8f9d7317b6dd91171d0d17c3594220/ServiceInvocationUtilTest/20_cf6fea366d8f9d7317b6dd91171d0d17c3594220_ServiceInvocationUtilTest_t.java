 package org.cagrid.workflow.helper.test.unit;
 
 import java.util.Iterator;
 
 import javax.xml.namespace.QName;
 import javax.xml.rpc.NamespaceConstants;
 import javax.xml.soap.SOAPElement;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.cagrid.workflow.helper.util.ConversionUtil;
 import org.cagrid.workflow.helper.util.ServiceInvocationUtil;
 
 
 public class ServiceInvocationUtilTest extends TestCase {
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
 
     /*
      * The method tested is supposed to connect to a WebService. So, we can't
      * create a unit test for now. Maybe we'll be able to use mock objects to
      * overcome this. 
      */
     public void testGenerateRequest() {
         // fail("Not yet implemented");
         return;
     }
 
 
     public void testApplyXPathQuery() throws Exception {
 
         System.out.println("Begin testApplyXPathQuery");
 
         // XML for testing xpath on complex types
         String xml_response = "<GenerateDataResponse xmlns=\"http://service1.introduce.cagrid.org/Service1\">"
             + " <ns1:StringAndItsLenght xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\"> "
             + "<ns1:str>george teadoro gordao que falou</ns1:str>" + "<ns1:length>31</ns1:length>"
             + "</ns1:StringAndItsLenght>" + "</GenerateDataResponse>";
 
         // XML for testing xpath on simple types
         String xml_response2 = "<CheckStringAndItsLengthResponse xmlns=\"http://service5.introduce.cagrid.org/Service5\">"
             + "<response>true</response></CheckStringAndItsLengthResponse>";
 
         QName namespaces[] = new QName[]{new QName("http://www.w3.org/2001/XMLSchema", "xsd"),
                 new QName("http://service1.introduce.cagrid.org/Service1", "ns0"),
                 new QName("http://service1.workflow.cagrid.org/Service1", "ns1")};
         String xpath_query = "/ns0:GenerateDataResponse/ns1:StringAndItsLenght";
 
         String ret = ServiceInvocationUtil.applyXPathQuery(xml_response, xpath_query, namespaces, new QName("http://service1.workflow.cagrid.org/Service1", "StringAndItsLenght"));
        String expected_ret = "<nsns:StringAndItsLenght xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\" xmlns:nsns=\"http://service1.workflow.cagrid.org/Service1\"> <ns1:str>george teadoro gordao que falou</ns1:str><ns1:length>31</ns1:length></nsns:StringAndItsLenght>";
         System.out.println("Query 1: '" + xpath_query + "'");
         System.out.println("Returned 1: \n" + ret);
         if (!ret.equals(expected_ret)) {
             fail("First XPath query failed");
         }
 
         xpath_query = "/ns0:GenerateDataResponse/ns1:StringAndItsLenght/ns1:length";
         ret = ServiceInvocationUtil.applyXPathQuery(xml_response, xpath_query, namespaces, new QName(NamespaceConstants.NSPREFIX_SCHEMA_XSD, "string"));
         expected_ret = "<ns1:length xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\">31</ns1:length>";
         System.out.println("Query 2: '" + xpath_query + "'");
         System.out.println("Returned 2: \n" + ret);
         if (!ret.equals(expected_ret)) {
             fail("Second XPath query failed");
         }
 
         namespaces = new QName[]{new QName("http://service5.introduce.cagrid.org/Service5", "ns5")};
         xpath_query = "/ns5:CheckStringAndItsLengthResponse";
         ret = ServiceInvocationUtil.applyXPathQuery(xml_response2, xpath_query, namespaces, new QName(NamespaceConstants.NSPREFIX_SCHEMA_XSD, "boolean"));
         expected_ret = "true";
         System.out.println("Query 3: '" + xpath_query + "'");
         System.out.println("Returned 3: \n" + ret);
         if (!ret.equals(expected_ret)) {
             fail("Third XPath query failed");
         }
 
        
        // TODO Add XPath query on a complex type with attributes of type EndpointReference
        /*String xml_response3 = "";
        namespaces = new QName[]{new QName("http://inputdatastager.workflow.tma.org/InputDataStager", "svc0"), 
        			 			 new QName("http://inputdatastager.tma.workflow.org/InputDataStager", "types")};
        xpath_query = "/svc0:StageInputFilesResponse/types:TMAStagedInputsEPRs/types:TIF";
        ret = ServiceInvocationUtil.applyXPathQuery(xml_response3, xpath_query, namespaces, new QName(NamespaceConstants.NSPREFIX_SCHEMA_XSD, "boolean"));
        expected_ret = "true";
        System.out.println("Query 3: '" + xpath_query + "'");
        System.out.println("Returned 3: \n" + ret);
        if (!ret.equals(expected_ret)) {
            fail("Third XPath query failed");
        } // */
        
        
        
        
         System.out.println("End testApplyXPathQuery");
         return;
 
     }
 
 
     public void testNode2String() {
 
         System.out.println("Begin testNode2String");
 
         // Document to be converted
         String xml_str = "<Body>" + "<GenerateDataResponse xmlns=\"http://service1.introduce.cagrid.org/Service1\">"
             + "<ns1:StringAndItsLenght xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\">"
             + "<ns1:str>george teadoro gordao que falou</ns1:str>" + "<ns1:length>31</ns1:length>"
             + "</ns1:StringAndItsLenght>" + "</GenerateDataResponse>" + "</Body>";
 
         // Conversion
         this.testString2SOAPElement(); // Ensure the next method call is OK
         Iterator iter = ConversionUtil.String2SOAPElement(xml_str);
         if (iter.hasNext()) {
 
             Object next_obj = iter.next();
             SOAPElement body = (SOAPElement) next_obj;
 
             String node_str = null;
             try {
                 node_str = ConversionUtil.Node2String(body);
             } catch (Exception e) {
                 // TODOAuto-generated catch block
                 e.printStackTrace();
                 Assert.fail();
             }
             if (!node_str.equals(xml_str)) {
                 fail("Expected '" + xml_str + "', but we got '" + node_str + "'");
             }
         }
 
         System.out.println("End testNode2String");
 
         return;
     }
 
 
     public void testString2SOAPElement() {
 
         System.out.println("Begin testString2SOAPElement");
 
         // Document to be converted
         String xml_str = "<Body>" + "<GenerateDataResponse xmlns=\"http://service1.introduce.cagrid.org/Service1\">"
             + "<ns1:StringAndItsLenght xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\">"
             + "<ns1:str>george teadoro gordao que falou</ns1:str>" + "<ns1:length>31</ns1:length>"
             + "</ns1:StringAndItsLenght>" + "</GenerateDataResponse>" + "</Body>";
 
         // Conversion
         Iterator iter = ConversionUtil.String2SOAPElement(xml_str);
 
         // Traversal of the xml tree checking the correctness of the results
         if (iter.hasNext()) {
 
             Object next_obj = iter.next();
             SOAPElement body = (SOAPElement) next_obj;
             if (body.getNodeName().equals("Body")) {
 
                 Iterator body_children = body.getChildElements();
 
                 if (body_children.hasNext()) {
 
                     next_obj = body_children.next();
                     SOAPElement generate_data_response = (SOAPElement) next_obj;
                     if (generate_data_response.getNodeName().equals("GenerateDataResponse")
                         && generate_data_response.getNamespaceURI().equals(
                             "http://service1.introduce.cagrid.org/Service1")) {
 
                         Iterator response_children = generate_data_response.getChildElements();
                         if (response_children.hasNext()) {
 
                             next_obj = response_children.next();
                             SOAPElement string_n_its_length = (SOAPElement) next_obj;
 
                             if (string_n_its_length.getLocalName().equals("StringAndItsLenght")
                                 && string_n_its_length.getPrefix().equals("ns1")
                                 && string_n_its_length.getNamespaceURI().equals(
                                     "http://service1.workflow.cagrid.org/Service1")) {
 
                                 Iterator string_n_children = string_n_its_length.getChildElements();
 
                                 if (string_n_children.hasNext()) {
 
                                     next_obj = string_n_children.next();
                                     SOAPElement str = (SOAPElement) next_obj;
 
                                     // check 'str' content
                                     if (str.getLocalName().equals("str") && str.getPrefix().equals("ns1")) {
 
                                         if (!str.getFirstChild().getNodeValue().equals(
                                             "george teadoro gordao que falou")) {
                                             fail("Expected text value is 'george teadoro gordao que falou', but we got "
                                                 + str.getFirstChild().getNodeValue());
                                         }
 
                                     } else
                                         fail("<str> expected, but we got <" + str.getPrefix() + ':'
                                             + str.getLocalName() + ">");
 
                                     next_obj = string_n_children.next();
                                     SOAPElement length = (SOAPElement) next_obj;
 
                                     // check 'length' content
                                     if (length.getLocalName().equals("length") && length.getPrefix().equals("ns1")) {
 
                                         if (!length.getFirstChild().getNodeValue().equals("31")) {
                                             fail("Expected text value is '31', but we got "
                                                 + length.getFirstChild().getNodeValue());
                                         }
                                     } else
                                         fail("<length> expected, but we got <" + length.getPrefix() + ':'
                                             + length.getLocalName() + ">");
 
                                 }
 
                             } else
                                 fail("<ns1:StringAndItsLenght xmlns:ns1=\"http://service1.workflow.cagrid.org/Service1\"> expected, but we got "
                                     + "<"
                                     + string_n_its_length.getPrefix()
                                     + ':'
                                     + string_n_its_length.getLocalName()
                                     + " xmlns:"
                                     + string_n_its_length.getPrefix()
                                     + "="
                                     + "\""
                                     + string_n_its_length.getNamespaceURI() + "\">");
                         }
 
                     } else
                         fail("<GenerateDataResponse xmlns=\"http://service1.introduce.cagrid.org/Service1\"> expected, but we got <"
                             + generate_data_response.getNodeName()
                             + " xmlns=\"+generate_data_response.getNamespaceURI()+\">");
                 }
 
             } else
                 fail("<Body> expected, but we got <" + body.getNodeName() + ">");
 
         } else
             fail("Returned iterator is supposed to have at least one valid element");
 
         System.out.println("End testString2SOAPElement");
         return;
     }
 
 }
