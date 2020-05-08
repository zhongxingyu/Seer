 package org.codehaus.xfire.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.invoker.BeanInvoker;
 import org.codehaus.xfire.transport.local.LocalTransport;
import org.codehaus.xfire.util.LoggingHandler;
import org.codehaus.xfire.util.dom.DOMOutHandler;
 
 public class ConcatClientTest
     extends AbstractXFireAegisTest
 {
     public void testDynamicClient() throws Exception
     {
         Service s = getServiceFactory().create(ConcatService.class);
         s.setInvoker(new BeanInvoker(new ConcatService()
         {
             public String concat(String s1, String s2)
             {
                 return s1 + s2;
             }
         }));
 
         getServiceRegistry().register(s);
 
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         s.getWSDLWriter().write(bos);
         Client client = new Client(new ByteArrayInputStream(bos.toByteArray()), null);
         client.setXFire(getXFire());
         client.setUrl("xfire.local://ConcatService");
         client.setTransport(getTransportManager().getTransport(LocalTransport.BINDING_ID));
        
        client.addOutHandler(new LoggingHandler());
        client.addOutHandler(new DOMOutHandler());
        
         Object[] res = client.invoke("concat", new Object[]{"1", "2"});
         
         assertEquals("12", res[0]);
     }
 
     public static interface ConcatService
     {
         String concat(String s1, String s2);
     }
 }
