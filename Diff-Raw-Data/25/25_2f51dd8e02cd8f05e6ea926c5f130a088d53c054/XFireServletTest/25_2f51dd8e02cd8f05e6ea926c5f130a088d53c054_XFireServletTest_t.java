 package org.codehaus.xfire.transport.http;
 
 import org.codehaus.xfire.handler.HandlerPipeline;
 import org.codehaus.xfire.service.AsyncService;
 import org.codehaus.xfire.service.BadEcho;
 import org.codehaus.xfire.service.Echo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.test.AbstractServletTest;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.wsdl.ResourceWSDL;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.codehaus.yom.Document;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 /**
  * XFireServletTest
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class XFireServletTest
         extends AbstractServletTest
 {
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         Service service = getServiceFactory().create(Echo.class,
                                                      Soap11.getInstance(),
                                                      SoapConstants.STYLE_MESSAGE,
                                                      SoapConstants.USE_LITERAL);
         WSDLWriter writer = new ResourceWSDL(getClass().getResource("/org/codehaus/xfire/echo11.wsdl"));
         service.setWSDLWriter(writer);
 
         service.setInPipeline(new HandlerPipeline());
         service.getInPipeline().addHandler(new MockSessionHandler());
         getServiceRegistry().register(service);
 
         Service faultService = getServiceFactory().create(BadEcho.class,
                                                           Soap11.getInstance(),
                                                           SoapConstants.STYLE_MESSAGE,
                                                           SoapConstants.USE_LITERAL);
 
         getServiceRegistry().register(faultService);
         
         // Asynchronous service
         Service asyncService = getServiceFactory().create(AsyncService.class,
                                                           Soap11.getInstance(),
                                                           SoapConstants.STYLE_MESSAGE,
                                                           SoapConstants.USE_LITERAL);
         OperationInfo op = asyncService.getServiceInfo().getOperation("echo");
         op.setMEP(SoapConstants.MEP_IN);
         getServiceRegistry().register(asyncService);
     }
 
     public void testServlet()
             throws Exception
     {
    	WebRequest getReq = new GetMethodWebRequest("http://localhost/services/Echo?wsdl")
        {

            /*
             * Work around bug 1212204 in httpUnit where as of 1.6 there was not
             * a way to support query strings with null values.
             * 
             * @see com.meterware.httpunit.HeaderOnlyWebRequest#getQueryString()
             */
            public String getQueryString()
            {
                return "WSDL";
            }
        };
        
     	WebResponse response = newClient().getResponse(getReq);
 
         WebRequest req = new PostMethodWebRequest("http://localhost/services/Echo",
                                                   getClass().getResourceAsStream("/org/codehaus/xfire/echo11.xml"),
                                                   "text/xml");
 
         response = newClient().getResponse(req);
 
         Document doc = readDocument(response.getText());
         addNamespace("m", "urn:Echo");
         assertValid("//m:echo", doc);
 
         assertTrue(MockSessionHandler.inSession);
     }
 
     public void testFaultCode()
             throws Exception
     {
         WebRequest req = new PostMethodWebRequest("http://localhost/services/BadEcho",
                                                   getClass().getResourceAsStream("/org/codehaus/xfire/echo11.xml"),
                                                   "text/xml");
 
         Transport transport = getXFire().getTransportManager().getTransport(SoapHttpTransport.NAME);
         assertNotNull(transport.getFaultPipeline());
 
         expectErrorCode(req, 500, "Response code 500 required for faults.");
     }
 
     public void testServiceWsdlNotFound()
             throws Exception
     {
         WebRequest req = new GetMethodWebRequest("http://localhost/services/NoSuchService?wsdl");
 
         expectErrorCode(req, 404, "Response code 404 required for invalid WSDL url.");
     }
 
     public void testAsync()
             throws Exception
     {
         WebRequest req = new PostMethodWebRequest("http://localhost/services/AsyncService",
                                                   getClass().getResourceAsStream("/org/codehaus/xfire/echo11.xml"),
                                                   "text/xml");
 
         WebResponse response = newClient().getResponse(req);
         assertTrue(response.getText().length() == 0);
     }
 }
