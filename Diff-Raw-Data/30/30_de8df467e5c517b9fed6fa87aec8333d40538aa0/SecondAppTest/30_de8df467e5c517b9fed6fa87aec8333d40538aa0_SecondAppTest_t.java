 package com.dynacrongroup;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.junit.Assert.assertTrue;
 
 public class SecondAppTest {
     Logger logger = LoggerFactory.getLogger(SecondAppTest.class);
 
     public SecondAppTest() {
        logger.info("SecondAppTest Constructor");
     }
 
     @Test
     public void testApp() {
         assertTrue(true);
        logger.info("SecondAppTest.testApp called");
     }
 
     @Test
     public void whichJdk() {
         assertTrue(true);
 
         logger.info("JDK being run is " + System.getProperty("java.version"));
     }
 
     @Test
     public void whichOs() {
         logger.info("OS being run is " + System.getProperty("os.version"));
     }
 
     @Test
     public void sampleProperty() {
         logger.info("sample.property is " + System.getProperty("sample.property"));
     }

    @Test
    public void sauceProperty() {
        logger.info("SELENIUM_DRIVER is " + System.getProperty("SELENIUM_DRIVER"));
    }

 }
