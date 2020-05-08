 package org.codehaus.xfire.fault;
 
 import org.codehaus.xfire.service.Echo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectInvoker;
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.test.AbstractXFireTest;
import org.codehaus.yom.Document;
 
 /**
  * XFireTest
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class CustomXFireFaultTest
         extends AbstractXFireTest
 {
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         Service service = getServiceFactory().create(Echo.class);
         service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, CustomFaultEcho.class);
 
         getServiceRegistry().register(service);
     }
 
     public void testInvoke()
             throws Exception
     {
         Document response = invokeService("Echo", "/org/codehaus/xfire/echo11.xml");
 
         printNode(response);
         addNamespace("s", Soap11.getInstance().getNamespace());
         assertValid("//s:Fault/faultcode[text()='MustUnderstand']", response);
         assertValid("//s:Fault/faultstring[text()='CustomFault']", response);
         assertValid("//s:Fault/detail/test", response);
     }
 }
