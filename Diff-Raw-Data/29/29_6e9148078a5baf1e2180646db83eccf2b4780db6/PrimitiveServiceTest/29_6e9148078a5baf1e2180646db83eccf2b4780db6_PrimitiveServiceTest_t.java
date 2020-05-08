 package org.codehaus.xfire.message.wrapped;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 21, 2004
  */
 public class PrimitiveServiceTest
         extends AbstractXFireAegisTest
 {
     public void setUp()
             throws Exception
     {
         super.setUp();
         getServiceRegistry().register(getServiceFactory().create(AddNumbers.class));
     }
 
     public void testService()
             throws Exception
     {
         Document response =
                 invokeService("AddNumbers",
                               "/org/codehaus/xfire/message/wrapped/add.xml");
 
         addNamespace("a", "http://wrapped.message.xfire.codehaus.org");
         assertValid("//a:addResponse/a:out[text()='2']", response);
     }
 }
