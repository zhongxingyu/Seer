 package org.switchyard.quickstarts.publish.as.webservice.inonly;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Map;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.switchyard.Exchange;
 import org.switchyard.component.bean.config.model.BeanSwitchYardScanner;
 import org.switchyard.component.test.mixins.cdi.CDIMixIn;
 import org.switchyard.component.test.mixins.http.HTTPMixIn;
 import org.switchyard.test.MockHandler;
 import org.switchyard.test.SwitchYardRunner;
 import org.switchyard.test.SwitchYardTestCaseConfig;
 import org.switchyard.test.SwitchYardTestKit;
 import org.switchyard.transform.config.model.TransformSwitchYardScanner;
 
 @RunWith(SwitchYardRunner.class)
 @SwitchYardTestCaseConfig(
         config = SwitchYardTestCaseConfig.SWITCHYARD_XML,
         mixins = { CDIMixIn.class, HTTPMixIn.class },
         scanners = { BeanSwitchYardScanner.class, TransformSwitchYardScanner.class })
 public class PublishAsWebserviceInonlyTest {
     
     private static final String WEB_SERVICE = "http://localhost:18001/Quickstart_publish_as_webservice_inonly/HelloWorldPubServiceInOnly";
     private static final String SERVICE = "HelloWorldPubServiceInOnly";
     private static final String SERVICE_FAULT = "FaultService";
     
     private SwitchYardTestKit _testKit;
     private HTTPMixIn _httpMixIn;
     
     @Test
     public void testWebService() throws Exception {
         _testKit.removeService(SERVICE);
         final MockHandler service = _testKit.registerInOnlyService(SERVICE);
         
         _httpMixIn.postResource(WEB_SERVICE, "/xml/soap-userpass-message.xml");
         
         service.waitForOKMessage();
         final LinkedBlockingQueue<Exchange> recievedMessages = service.getMessages();
         assertThat(recievedMessages, is(notNullValue()));
         final Exchange recievedExchange = recievedMessages.iterator().next();
         String content = recievedExchange.getMessage().getContent(String.class);
         SwitchYardTestKit.compareXMLToString(content,
                 "<say:sayHi xmlns:say='http://www.jboss.org/sayHi'><say:arg0>HelloWorld</say:arg0></say:sayHi>");
     }
     
     @Test
     public void testWebService_error() throws Exception {
         _testKit.removeService(SERVICE_FAULT);
         final MockHandler service = _testKit.registerInOnlyService(SERVICE_FAULT);
         
         _httpMixIn.postResource(WEB_SERVICE, "/xml/error-soap-message.xml");
         
         service.waitForOKMessage();
         final LinkedBlockingQueue<Exchange> recievedMessages = service.getMessages();
         assertThat(recievedMessages, is(notNullValue()));
         final Exchange recievedExchange = recievedMessages.iterator().next();
         @SuppressWarnings("unchecked")
         Map<String, String> content = (Map<String, String>) recievedExchange.getMessage().getContent(Map.class);
         assertThat(content.get(FaultService.DETAIL_CODE_CONTENT), is(equalTo("myErrorCode")));
         assertThat(content.get(FaultService.DETAIL_DESCRIPTION_CONTENT), is(equalTo("myDescription")));
         //@formatter:off
         final String expectedDetail =
                   "<say:sayFault xmlns:say='http://www.jboss.org/sayHi'>"
                 +   "<say:code>myErrorCode</say:code>"
                 +   "<say:faultString>myDescription</say:faultString>"
                 + "</say:sayFault>";
         //@formatter:on
         SwitchYardTestKit.compareXMLToString(content.get(FaultService.DETAIL_DETAIL_CONTENT), expectedDetail);
     }
     @Test
     public void runTest() throws Exception {
         _httpMixIn.postResource(WEB_SERVICE, "/xml/soap-userpass-message.xml");
     }
     
     @Test
     public void runTest_error() throws Exception {
         _httpMixIn.postResource(WEB_SERVICE, "/xml/error-soap-message.xml");
     }
     
 }
