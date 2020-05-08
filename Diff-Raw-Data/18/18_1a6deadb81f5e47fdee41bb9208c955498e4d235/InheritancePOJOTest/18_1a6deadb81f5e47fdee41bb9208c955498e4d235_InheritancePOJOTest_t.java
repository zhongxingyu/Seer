 package org.codehaus.xfire.aegis.inheritance;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.test.AbstractXFireTest;
 import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class InheritancePOJOTest
     extends AbstractXFireTest
 {
     private Service endpoint;
 
     public void setUp()
         throws Exception
     {
         super.setUp();
         setServiceFactory(new ObjectServiceFactory(getTransportManager(),
                 new AegisBindingProvider()));
         ObjectServiceFactory osf = (ObjectServiceFactory) getServiceFactory();
 
         HashMap props = new HashMap();
        props.put(AegisBindingProvider.WRITE_XSI_TYPE_KEY, "true");
         ArrayList l = new ArrayList();
         l.add(Employee.class.getName());
 
         props.put(AegisBindingProvider.OVERRIDE_TYPES_KEY, l);
         endpoint = osf.create(InheritanceService.class,
                               "InheritanceService",
                               "urn:xfire:inheritance",
                               props);
 
         getServiceRegistry().register(endpoint);
     }
 
     public void testGenerateWsdl()
         throws Exception
     {
 
         Document d = getWSDLDocument("InheritanceService");
 
         String types = "//wsdl:types/xsd:schema/";
 
         // check for Employee as extension
         String employeeType = types + "xsd:complexType[@name='Employee']";
         assertValid(employeeType, d);
         String extension = "/xsd:complexContent/xsd:extension[@base='ns1:BaseUser']";
         assertValid(employeeType + extension, d);
         assertValid(employeeType + extension + "/xsd:sequence/xsd:element[@name='division']", d);
         assertValid("count(" + employeeType + extension + "/xsd:sequence/*)=1", d);
 
         // check for BaseUser as abstract
         String baseUserType = types + "xsd:complexType[(@name='BaseUser') and (@abstract='true')]";
         assertValid(baseUserType, d);
         assertValid(baseUserType + "/xsd:sequence/xsd:element[@name='name']", d);
         assertValid("count(" + baseUserType + "/xsd:sequence/*)=1", d);
     }
 
     public void testLocalReceiveEmployee()
         throws Exception
     {
         Document response = invokeService("InheritanceService", "ReceiveEmployee.xml");
         addNamespace("w", "urn:xfire:inheritance");
         assertValid("//s:Body/w:receiveUserResponse", response);
     }
 
     public void testLocalGetEmployee()
         throws Exception
     {
         Document response = invokeService("InheritanceService", "GetEmployee.xml");
         addNamespace("w", "urn:xfire:inheritance");
         addNamespace("p", "http://inheritance.aegis.xfire.codehaus.org");
         assertValid("//s:Body/w:getEmployeeResponse/w:out/p:division", response);
         assertValid("//s:Body/w:getEmployeeResponse/w:out[@xsi:type]", response);
     }
 }
