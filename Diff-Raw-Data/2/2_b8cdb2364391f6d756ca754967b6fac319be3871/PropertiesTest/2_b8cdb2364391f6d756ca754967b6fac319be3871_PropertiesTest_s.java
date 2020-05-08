 package org.atlasapi.properties;
 
 import junit.framework.TestCase;
 
 import com.metabroadcast.common.properties.Configurer;
 
 public class PropertiesTest extends TestCase {
     public void testShouldTakeProdProperties() throws Exception {
         Configurer.load("prod");
        assertEquals("10.228.167.143", Configurer.get("mongo.host").get());
     }
 }
