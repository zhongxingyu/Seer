 package org.codehaus.xfire.security.wssecurity;
 
 import java.io.InputStream;
 
 import org.codehaus.xfire.security.InSecurityResult;
 import org.codehaus.xfire.util.DOMUtils;
 import org.w3c.dom.Document;
 
 import junit.framework.TestCase;
 
 public class WSS4JProcessorsTest
     extends TestCase
 {
 
     /**
      * @param file
      * @return
      * @throws Exception
      */
     private Document readDocument(String file)
         throws Exception
     {
         InputStream inStream = getClass().getResourceAsStream(file);
 
         DOMUtils utils = new DOMUtils();
         Document doc = utils.readXml(inStream);
         inStream.close();
 
         return doc;
     }
 
     /**
      * @throws Exception
      */
     public void testSignatureFull()
         throws Exception
     {
         Document doc = readDocument("wsse-request-clean.xml");
 
         WSS4JOutSecurityProcessor outProcessor = new WSS4JOutSecurityProcessor();
         WSS4JOutProcessorBuilder outBuilder = (WSS4JOutProcessorBuilder) outProcessor.getBuilder();
         outBuilder.setConfigFile("META-INF/xfire/outsecurity_sign.properties");
         doc = outProcessor.process(doc);
         assertNotNull(doc);
         WSS4JInSecurityProcessor processor = new WSS4JInSecurityProcessor();
         WSS4JInProcessorBuilder inBuilder = (WSS4JInProcessorBuilder) processor.getBuilder();
         inBuilder.setConfigFile("META-INF/xfire/insecurity_sign.properties");
         InSecurityResult result = processor.process(doc);
 
     }
 
     /**
      * @throws Exception
      */
     public void testEncryptionFull()
         throws Exception
     {
         Document doc = readDocument("wsse-request-clean.xml");
 
         WSS4JOutSecurityProcessor outProcessor = new WSS4JOutSecurityProcessor();
         WSS4JOutProcessorBuilder outBuilder = (WSS4JOutProcessorBuilder) outProcessor.getBuilder();
         outBuilder.setConfigFile("META-INF/xfire/outsecurity_enc.properties");
         doc = outProcessor.process(doc);
         assertNotNull(doc);
         WSS4JInSecurityProcessor processor = new WSS4JInSecurityProcessor();
         WSS4JInProcessorBuilder inBuilder = (WSS4JInProcessorBuilder) processor.getBuilder();
         inBuilder.setConfigFile("META-INF/xfire/insecurity_enc.properties");
         InSecurityResult result = processor.process(doc);
 
     }
 
     /**
      * @throws Exception
      */
     public void testUserTokenFull()
         throws Exception
     {
         Document doc = readDocument("wsse-request-clean.xml");
 
         WSS4JOutSecurityProcessor outProcessor = new WSS4JOutSecurityProcessor();
         WSS4JOutProcessorBuilder outBuilder = (WSS4JOutProcessorBuilder) outProcessor.getBuilder();
         outBuilder.setConfigFile("META-INF/xfire/outsecurity_ut.properties");
         doc = outProcessor.process(doc);
         assertNotNull(doc);
         WSS4JInSecurityProcessor processor = new WSS4JInSecurityProcessor();
         WSS4JInProcessorBuilder inBuilder = (WSS4JInProcessorBuilder) processor.getBuilder();
         inBuilder.setConfigFile("META-INF/xfire/insecurity_ut.properties");
         InSecurityResult result = processor.process(doc);
        String usr = result.getUser();
        String pass = result.getPassword();

     }
 
 }
