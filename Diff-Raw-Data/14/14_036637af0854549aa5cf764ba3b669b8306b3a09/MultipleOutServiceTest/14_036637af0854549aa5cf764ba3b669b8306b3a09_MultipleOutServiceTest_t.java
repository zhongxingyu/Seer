 package org.codehaus.xfire.inout;
 
 import java.lang.reflect.Method;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.Holder;
 import org.codehaus.xfire.client.XFireProxyFactory;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.MessagePartContainer;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectInvoker;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.transport.http.SoapHttpTransport;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 21, 2004
  */
 public class MultipleOutServiceTest
         extends AbstractXFireAegisTest
 {
     private Service service;
 
     public void setUp()
             throws Exception
     {
         super.setUp();
         
         ObjectServiceFactory osf = new ObjectServiceFactory(getTransportManager()) {
 
             protected boolean isOutParam(Method method, int j)
             {
                 if (j >= 1) return true;
                 
                 return super.isOutParam(method, j);
             }
 
            protected boolean isInParam(Method method, int j)
            {
                if (j >= 1) return false;
                
                return super.isInParam(method, j);
            }
            
             protected boolean isHeader(Method method, int j)
             {
                 if (j == 2) return true;
                 
                 return super.isHeader(method, j);
             }
         };
         
         service = osf.create(MultipleOutService.class, null, "urn:MultipleOutService", null);
         service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, MultipleOutServiceImpl.class);
         
         getServiceRegistry().register(service);
     }
 
     public void testServiceConstruction()
             throws Exception
     {
         OperationInfo operation = service.getServiceInfo().getOperation("echo");
         
         assertEquals(1, operation.getInputMessage().size());
         assertEquals(2, operation.getOutputMessage().size());
         
         Binding binding = service.getBinding(SoapHttpTransport.SOAP11_HTTP_BINDING);
         MessagePartContainer headers = binding.getHeaders(operation.getOutputMessage());
         assertEquals(1, headers.size());
         
         Document response =
                 invokeService("MultipleOutService",
                               "/org/codehaus/xfire/inout/echo.xml");
 
         addNamespace("m", "urn:MultipleOutService");
         assertValid("/s:Envelope/s:Body/m:echoResponse/m:out[text()='Yo Yo']", response);
         assertValid("/s:Envelope/s:Body/m:echoResponse/m:in1[text()='hi']", response);
         assertValid("/s:Envelope/s:Header/m:in2[text()='header']", response);
     }
 
     public void testClient()
         throws Exception
     {
         XFireProxyFactory xpf = getXFireProxyFactory();
         MultipleOutService client = (MultipleOutService) xpf.create(service, "xfire.local://MultipleOutService");
         
         Holder out = new Holder();
         Holder headerOut = new Holder();
         String response = client.echo("Yo Yo", out, headerOut);
         
         assertEquals("Yo Yo", response);
         assertEquals("header", (String) headerOut.getValue());
         assertEquals("hi", (String) out.getValue());
     }
 
     public void testWSDL()
             throws Exception
     {
         Document doc = getWSDLDocument("MultipleOutService");
 
         String ns = "urn:MultipleOutService";
         
         addNamespace("wsdl", WSDLWriter.WSDL11_NS);
         addNamespace("wsdlsoap", WSDLWriter.WSDL11_SOAP_NS);
         addNamespace("xsd", SoapConstants.XSD);
 
         assertValid("//xsd:element[@name='echoResponse']/xsd:complexType/xsd:sequence" +
                     "/xsd:element[@name='out'][@type='xsd:string']",
                     doc);
         assertValid("//xsd:element[@name='echoResponse']/xsd:complexType/xsd:sequence" +
                     "/xsd:element[@name='in1'][@type='xsd:string']",
                     doc);
     }
 }
