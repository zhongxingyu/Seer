 package jsr181.jaxb.globalweather;
 
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.annotations.AnnotationServiceFactory;
 import org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations;
 import org.codehaus.xfire.jaxb2.JaxbTypeRegistry;
 import org.codehaus.xfire.service.Service;
 
 public class WeatherTest   
     extends AbstractXFireAegisTest
 {
     private Service service;
     
     @Override
     protected void setUp()
         throws Exception
     {
         super.setUp();
         AnnotationServiceFactory asf = new AnnotationServiceFactory(new Jsr181WebAnnotations(),
                                                                     getXFire().getTransportManager(),
                                                                     new AegisBindingProvider(new JaxbTypeRegistry()));
         service = asf.create(GlobalWeatherCustomImpl.class);
 
         getServiceRegistry().register(service);
     }
 
     @Override
     protected void tearDown()
         throws Exception
     {
         getServiceRegistry().unregister(service);
         super.tearDown();
     }
     
     @Override
     protected XFire getXFire()
     {
         return XFireFactory.newInstance().getXFire();
     }
     
     public void testEcho() throws Exception
     {   
         GlobalWeatherClient service = new GlobalWeatherClient();
         
         GlobalWeatherSoap client = service.getGlobalWeatherSoapLocalEndpoint();
         assertNotNull(client);
         
        assertEquals("foo", client.getWeather("foo", "bar"));
     }
 }
