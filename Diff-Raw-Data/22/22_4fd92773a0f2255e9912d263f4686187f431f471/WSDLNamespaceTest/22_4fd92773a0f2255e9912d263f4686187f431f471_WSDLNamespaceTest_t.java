 package org.codehaus.xfire.aegis.inheritance.xfire704;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.inheritance.xfire704.response.TestBaseResponse;
 import org.codehaus.xfire.aegis.inheritance.xfire704.response.TestSubResponse;
 import org.codehaus.xfire.service.Service;
import org.jdom.Document;
 
 public class WSDLNamespaceTest extends AbstractXFireAegisTest
 {
     public void testWsdl() throws Exception 
     {
         Map props = new HashMap();
         props.put("writeXsiType", Boolean.TRUE);
         
         List types = new ArrayList();
         types.add(TestValue.class.getName());
         types.add(TestBaseResponse.class.getName());
         types.add(TestSubResponse.class.getName());
 
         props.put("overrideTypesList", types);
         Service service = getServiceFactory().create(TestService.class, props);
         getServiceRegistry().register(service);

        Document wsdl = getWSDLDocument(service.getSimpleName());
        assertValid("//xsd:element[@name='testValue'][@type='tns:TestValue']", wsdl);
     }
 }
