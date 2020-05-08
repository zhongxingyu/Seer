 package org.atlasapi.properties;
 
 import junit.framework.TestCase;
 
 import com.metabroadcast.common.properties.Configurer;
 
 public class PropertiesTest extends TestCase {
     public void testShouldTakeProdProperties() throws Exception {
         Configurer.load("prod");
        assertEquals("10.235.57.61", Configurer.get("mongo.host").get());
     }
 }
