 package net.bluedash.resteasy.test.unit;
 
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.spi.ResteasyDeployment;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.jboss.resteasy.test.TJWSServletContainer;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class TestTJWSServletContainer {
 
     @BeforeClass
     public static void setUp() throws Exception {
 
         ResteasyDeployment deployment = TJWSServletContainer.start();
         deployment.getRegistry().addPerRequestResource(HelloWorldServiceImpl.class);
     }
 
     @AfterClass
     public static void stop() throws Exception {
         TJWSServletContainer.stop();
     }
 
 
     @Test
     public void testIt() throws Exception {
         RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
         HelloWorldService client = ProxyFactory.create(HelloWorldService.class, "http://127.0.0.1:8081");
         assertEquals("Hello, world!", client.printHelloWorld());
 
     }



 }
