 package org.switchyard.quickstarts.webservice.proxy.basic;
 
 import javax.xml.ws.Endpoint;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.switchyard.component.test.mixins.http.HTTPMixIn;
 import org.switchyard.test.SwitchYardRunner;
 import org.switchyard.test.SwitchYardTestCaseConfig;
 import org.switchyard.transform.config.model.TransformSwitchYardScanner;
 
 @RunWith(SwitchYardRunner.class)
 @SwitchYardTestCaseConfig(
         config = SwitchYardTestCaseConfig.SWITCHYARD_XML,
         mixins = { HTTPMixIn.class },
         scanners = { TransformSwitchYardScanner.class })
 public class WebserviceProxyBasicTest {
     
     private static final String WEB_SERVICE = "http://localhost:8080/Quickstart_webservice_proxy_basic/HelloWorldWS";
     private static final String PROXY_SERVICE = "http://localhost:8080/proxy/HelloWorldWSService";
     
     private Endpoint _endpoint;
     private HTTPMixIn _httpMixIn;
     
     @Before
     public void startWebService() throws Exception {
         _endpoint = Endpoint.publish(WEB_SERVICE, new HelloWorldWS());
     }
     
     @After
     public void stopWebService() throws Exception {
         _endpoint.stop();
     }
     
     @Test
     public void testWebService() throws Exception {
         _httpMixIn.postResourceAndTestXML(WEB_SERVICE, "/xml/soap-request.xml", "/xml/soap-response.xml");
     }
     
     @Test
     public void testProxyService() throws Exception {
        _httpMixIn.postResourceAndTestXML(PROXY_SERVICE, "/xml/soap-request.xml", "/xml/soap-response-proxy.xml");
     }
     
 }
