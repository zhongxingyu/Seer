 package org.codehaus.xfire.xmlbeans;
 
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.wsdl.AbstractWSDL;
 import org.jdom.Document;
 
 /**
  * Tests that we can handle multiple schemas within the same namespace.
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class MultipleSchemaInNSTest
         extends AbstractXmlBeansTest
 {
     private Service endpoint;
     String ns = "urn:xfire:xmlbeans:nstest";
 
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         endpoint = getServiceFactory().create(MultipleSchemaService.class,
                                               null,
                                               ns,
                                               null);
                     
         getServiceRegistry().register(endpoint);
     }
 
     public void testWSDL() throws Exception
     {
         Document wsdl = getWSDLDocument("MultipleSchemaService");
 
         addNamespace("xsd", SoapConstants.XSD);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][1]", wsdl);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][1]/xsd:import[@namespace='" + ns + "']", wsdl);
         assertInvalid("//xsd:schema[@targetNamespace='" + ns + "']" +
                 "[1]/xsd:import[@namespace='" + ns + "'][@schemaLocation]", wsdl);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][2]", wsdl);
         assertInvalid("//xsd:schema[@targetNamespace='" + ns + "'][2]/xsd:import[@namespace='" + ns + "']", wsdl);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][3]", wsdl);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][3]/xsd:import[@namespace='" + ns + "']", wsdl);
         
         endpoint.setProperty(AbstractWSDL.REMOVE_ALL_IMPORTS, "True");
         
         wsdl = getWSDLDocument("MultipleSchemaService");
         
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][1]", wsdl);
         assertInvalid("//xsd:schema[@targetNamespace='" + ns + "'][1]" +
                 "/xsd:import[@namespace='" + ns + "']", wsdl);
         assertValid("//xsd:schema[@targetNamespace='" + ns + "'][3]", wsdl);
         assertInvalid("//xsd:schema[@targetNamespace='" + ns + "'][3]" +
                 "/xsd:import[@namespace='" + ns + "']", wsdl);
     }
 }
