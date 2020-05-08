 package org.codehaus.xfire.annotations.jsr181;
 
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 import org.codehaus.xfire.annotations.AnnotationServiceFactory;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.test.AbstractXFireTest;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.codehaus.yom.Document;
 
 public class HeaderServiceTest
         extends AbstractXFireTest
 {
     private AnnotationServiceFactory osf;
     
     public void setUp()
             throws Exception
     {
         super.setUp();
         
         osf = new AnnotationServiceFactory(new Jsr181WebAnnotations(),
                                            getXFire().getTransportManager(),
                                            null);
 
         Service service = osf.create(HeaderService.class);
 
         getXFire().getServiceRegistry().register(service);
     }
 
     public void testHeaders()
             throws Exception
     {
         Document response = invokeService("HeaderService", "/org/codehaus/xfire/annotations/jsr181/headerMessage.xml");
         
         assertNotNull(HeaderService.a);
         assertEquals("one", HeaderService.a);
         assertNotNull(HeaderService.b);
         assertEquals("three", HeaderService.b);
         assertNotNull(HeaderService.header);
         assertEquals("two", HeaderService.header);
     }
     
     public void testWSDL() throws Exception
     {
         Document wsdl = getWSDLDocument("HeaderService");
         
         //printNode(wsdl);
 
         addNamespace("wsdl", WSDLWriter.WSDL11_NS);
         addNamespace("wsdlsoap", WSDLWriter.WSDL11_SOAP_NS);
         addNamespace("xsd", SoapConstants.XSD);
        
         assertValid("//wsdl:message[@name='doSomethingRequestHeaders']", wsdl);
        assertValid("//wsdl:message[@name='doSomethingRequestHeaders']/wsdl:part[@element='tns:header'][@name='header']", wsdl);
         assertValid("//wsdlsoap:header[@message='tns:doSomethingRequestHeaders'][@part='header'][@use='literal']", wsdl);
         assertValid("//xsd:element[@name='header']", wsdl);
         assertInvalid("//xsd:element[@name='header'][2]", wsdl);
     }
     
     @WebService(name="HeaderService", targetNamespace="urn:HeaderService")
     @SOAPBinding(parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
     public static class HeaderService
     {
         static String a;
         static String b;
         static String header;
         static UserToken authHeader;
         
         @WebMethod
         public void doSomething(@WebParam(name="a") String a,
                                 @WebParam(name="header", header=true) String header,
                                 @WebParam(name="b") String b) 
         {
             HeaderService.a = a;
             HeaderService.b = b;
             HeaderService.header = header;
         }
         
         @WebMethod
         public void doSomethingAuthenticated(@WebParam(name="header", header=true) UserToken authHeader) 
         {
             HeaderService.authHeader = authHeader;
         }
         
         @WebMethod
         public void doSomethingAuthenticated2(@WebParam(name="header", header=true) UserToken authHeader) 
         {
             HeaderService.authHeader = authHeader;
         }
     }
     
     public static class UserToken
     {
         private String username;
         private String password;
         
         public String getPassword()
         {
             return password;
         }
         public void setPassword(String password)
         {
             this.password = password;
         }
         public String getUsername()
         {
             return username;
         }
         public void setUsername(String username)
         {
             this.username = username;
         }
     }
 }
