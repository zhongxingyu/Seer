 package org.codehaus.xfire.aegis.inheritance.xfire704;
 
import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.inheritance.xfire704.response.TestBaseResponse;
 import org.codehaus.xfire.aegis.inheritance.xfire704.response.TestSubResponse;
 import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.wsdl.WSDLWriter;
 
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
        
        WSDLWriter wsdl = getWSDL(service.getSimpleName());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wsdl.write(out);
        out.close();
        
        String wstr = out.toString();
        int start = wstr.indexOf("xmlns:ns1");
        int count = 0;
        while (start != -1) {
            count++;
            start = wstr.indexOf("xmlns:ns1", start+1);
        }
        assertEquals(1, count);
     }
 }
