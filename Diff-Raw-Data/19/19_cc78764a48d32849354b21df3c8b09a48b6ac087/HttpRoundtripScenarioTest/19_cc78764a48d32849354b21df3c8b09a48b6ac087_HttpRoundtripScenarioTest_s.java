 package org.eclipse.swordfish.core.httproundtrip.test;
 
 import javax.xml.transform.Source;
 
 import org.apache.cxf.BusFactory;
 import org.apache.cxf.bus.CXFBusFactory;
 import org.apache.cxf.transport.ConduitInitiatorManager;
 import org.apache.cxf.transport.http.ClientOnlyHTTPTransportFactory;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.base.TargetPlatformOsgiTestCase;
 import org.eclipse.swordfish.core.test.util.domain.ClientUtil;
 import org.eclipse.swordfish.core.test.util.domain.HelloWorld;
 import org.eclipse.swordfish.core.util.SimpleClient;
 import org.junit.Test;
 
 public class HttpRoundtripScenarioTest extends TargetPlatformOsgiTestCase {
     @Test
     public void test1Provider() throws Exception {
         Thread.sleep(2000);
         ConduitInitiatorManager conduitInitiatorManager = BusFactory.getThreadDefaultBus().getExtension(ConduitInitiatorManager.class);//.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", new HttpBindingFactory());
         ClientOnlyHTTPTransportFactory clientOnlyHTTPTransportFactory = new ClientOnlyHTTPTransportFactory();
         clientOnlyHTTPTransportFactory.setBus(BusFactory.getThreadDefaultBus());
         conduitInitiatorManager.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", clientOnlyHTTPTransportFactory);
         System.out.println(conduitInitiatorManager);
         String wsdlUrl = HelloWorld.class.getClassLoader().getResource("HelloWorld.wsdl").toString();
         Thread.currentThread().setContextClassLoader(CXFBusFactory.class.getClassLoader());
         HelloWorld helloWorld = ClientUtil.createWebServiceStub(HelloWorld.class, wsdlUrl);
         assertEquals("Hello Swordfish", helloWorld.sayHi("Swordfish"));
     }
     @Test
     public void test2RoundTrip() throws Exception {
         SimpleClient simpleClient = new SimpleClient();
         simpleClient.setNmr(OsgiSupport.getReference(bundleContext, NMR.class));
         simpleClient.setUriToSend("http://localhost:8196/testsample/");
         simpleClient.setDelayBeforeSending(0);
         simpleClient.setDataToSend("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:sayHi xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\"><arg0>Swordfish</arg0></ns2:sayHi></soap:Body></soap:Envelope>");
         simpleClient.setTargetEndpointName("cxfEndpointHttpProvider");
         ExchangeImpl exchangeImpl = simpleClient.performSynchronousRequest();
         assertNotNull(exchangeImpl);
         String response = new SourceTransformer().toString(exchangeImpl.getOut().getBody(
                 Source.class));
         assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:sayHiResponse xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\"><return>Hello Swordfish</return></ns2:sayHiResponse>", response);
     }
     @Override
     protected String getManifestLocation() {
         return "classpath:org/eclipse/swordfish/core/httproundtrip/test/MANIFEST.MF";
     }
 }
