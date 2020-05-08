 package org.codehaus.xfire.message.wrapped;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.services.VoidService;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Dec 20, 2004
  */
 public class VoidTest
         extends AbstractXFireAegisTest
 {
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         Service endpoint = getServiceFactory().create(VoidService.class);
         getServiceRegistry().register(endpoint);
     }
 
     public void testVoidSync()
             throws Exception
     {
         Document response =
                 invokeService("VoidService",
                               "/org/codehaus/xfire/message/wrapped/voidRequest.xml");
 
         addNamespace("sb", "http://services.xfire.codehaus.org");
         assertValid("/s:Envelope/s:Body/sb:doNothingResponse", response);
     }
 
     public void testVoidAsync()
             throws Exception
     {
         Service endpoint = getServiceRegistry().getService("VoidService");
         OperationInfo op = endpoint.getServiceInfo().getOperation("doNothing");
         op.setAsync(true);
         op.setMEP(SoapConstants.MEP_IN);

         Document response =
                 invokeService("VoidService",
                               "/org/codehaus/xfire/message/wrapped/voidRequest.xml");
 
         assertNull(response);
     }
 
     public void testSyncWSDL()
             throws Exception
     {
         Document doc = getWSDLDocument("VoidService");
 
         addNamespace("wsdl", WSDLWriter.WSDL11_NS);
         addNamespace("wsdlsoap", WSDLWriter.WSDL11_SOAP_NS);
         addNamespace("xsd", SoapConstants.XSD);
 
         assertValid("//xsd:schema/xsd:element[@name='doNothing']", doc);
         assertValid("//xsd:schema/xsd:element[@name='doNothingResponse']", doc);
         assertValid("//wsdl:portType/wsdl:operation[@name='doNothing']/wsdl:input", doc);
         assertValid("//wsdl:portType/wsdl:operation[@name='doNothing']/wsdl:output", doc);
         assertValid("//wsdl:binding/wsdl:operation[@name='doNothing']/wsdl:input", doc);
         assertValid("//wsdl:binding/wsdl:operation[@name='doNothing']/wsdl:output", doc);
     }
 
     public void testAsyncWSDL()
             throws Exception
     {
         Service endpoint = getServiceRegistry().getService("VoidService");
         OperationInfo op = endpoint.getServiceInfo().getOperation("doNothing");
         op.setAsync(true);
         op.setMEP(SoapConstants.MEP_IN);
 
         Document doc = getWSDLDocument("VoidService");
 
         addNamespace("wsdl", WSDLWriter.WSDL11_NS);
         addNamespace("wsdlsoap", WSDLWriter.WSDL11_SOAP_NS);
         addNamespace("xsd", SoapConstants.XSD);
 
         assertValid("//xsd:schema/xsd:element[@name='doNothing']", doc);
         assertInvalid("//xsd:schema/xsd:element[@name='doNothingResponse']", doc);
         assertValid("//wsdl:portType/wsdl:operation[@name='doNothing']/wsdl:input", doc);
         assertInvalid("//wsdl:portType/wsdl:operation[@name='doNothing']/wsdl:output", doc);
         assertValid("//wsdl:binding/wsdl:operation[@name='doNothing']/wsdl:input", doc);
         assertInvalid("//wsdlbinding/wsdl:operation[@name='doNothing']/wsdl:output", doc);
     }
 }
