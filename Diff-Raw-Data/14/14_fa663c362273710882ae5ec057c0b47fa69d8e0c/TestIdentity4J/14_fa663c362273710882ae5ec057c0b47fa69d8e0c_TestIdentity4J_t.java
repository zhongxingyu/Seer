 package com.ufp.identity4j.test;
 
 import java.io.File;
 
 import java.net.InetAddress;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import org.junit.Test;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 
 import com.ufp.identity4j.data.AuthenticationContext;
 import com.ufp.identity4j.data.AuthenticationPretext;
 import com.ufp.identity4j.data.DisplayItem;
 
 import com.ufp.identity4j.provider.IdentityServiceProvider;
 import com.ufp.identity4j.truststore.KeyManagerFactoryBuilder;
 import com.ufp.identity4j.truststore.TrustManagerFactoryBuilder;
 import com.ufp.identity4j.truststore.IdentityHostnameVerifier;
 import com.ufp.identity4j.resolver.StaticIdentityResolver;
 
 import org.apache.log4j.Logger;
 
 public class TestIdentity4J {
     private static IdentityServiceProvider identityServiceProvider;
     private static Logger logger = Logger.getLogger(TestIdentity4J.class);
     private static String clientIp;
 
     @BeforeClass
     public static void setupIdentity4JProvider() throws Exception {
         identityServiceProvider = new IdentityServiceProvider();
         clientIp = InetAddress.getLocalHost().getHostAddress();
         // setup the key manager factory
         KeyManagerFactoryBuilder keyManagerFactoryBuilder = new KeyManagerFactoryBuilder();
         keyManagerFactoryBuilder.setStore(new File("src/test/resources/example.com.p12"));
        keyManagerFactoryBuilder.setPassphrase("test");
 
         // setup the trust store
         TrustManagerFactoryBuilder trustManagerFactoryBuilder = new TrustManagerFactoryBuilder();
         trustManagerFactoryBuilder.setStore(new File("src/test/resources/truststore.jks"));
         trustManagerFactoryBuilder.setPassphrase("pSnHa(3QDixmi%\\");
 
         // set provider properties
         identityServiceProvider.setKeyManagerFactoryBuilder(keyManagerFactoryBuilder);
         identityServiceProvider.setTrustManagerFactoryBuilder(trustManagerFactoryBuilder);
 
         //identityServiceProvider.setHostnameVerifier(new IdentityHostnameVerifier("ufp.com"));
         //identityServiceProvider.setIdentityResolver(new StaticIdentityResolver("https://staging.ufp.com:8443/identity-services/services/"));
         identityServiceProvider.setHostnameVerifier(new IdentityHostnameVerifier("localhost"));
         identityServiceProvider.setIdentityResolver(new StaticIdentityResolver("https://localhost:8443/identity-services/services/"));
         identityServiceProvider.afterPropertiesSet();
     }
 
     @Test
     public void TestAuthenticate() throws Exception {
         AuthenticationPretext authenticationPretext = identityServiceProvider.preAuthenticate("guest", clientIp);
         assertNotNull(authenticationPretext);
        assertEquals("SUCCESS", authenticationPretext.getResult().getValue());
 
        assertEquals(1, authenticationPretext.getDisplayItem().size());
         DisplayItem displayItem = authenticationPretext.getDisplayItem().get(0);
         Map<String, String []> parameterMap = new HashMap<String, String []>();
 
         parameterMap.put(displayItem.getName(), new String [] {"guest"});
         AuthenticationContext authenticationContext = (AuthenticationContext)identityServiceProvider.authenticate(authenticationPretext.getName(), clientIp, parameterMap);
         assertNotNull(authenticationContext);
        assertEquals("SUCCESS", authenticationContext.getResult().getValue());
         logger.debug("found confidence of " + authenticationContext.getResult().getConfidence());
        assertEquals(0.33d, authenticationContext.getResult().getConfidence(), 0.10d);
     }
 
     @Test
     public void TestPreEnroll() {
         assertTrue(true);
     }
 
     @Test
     public void TestEnroll() {
         assertTrue(true);
     }
 }
