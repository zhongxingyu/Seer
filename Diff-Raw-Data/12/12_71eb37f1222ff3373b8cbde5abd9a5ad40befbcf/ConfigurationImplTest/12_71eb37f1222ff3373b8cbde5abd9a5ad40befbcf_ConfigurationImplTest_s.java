 package org.sakaiproject.nakamura.lite;
 
 import java.io.IOException;
 import java.util.Map;
 
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.sakaiproject.nakamura.lite.content.InternalContentAccess;
 
 import com.google.common.collect.ImmutableMap;
 
 public class ConfigurationImplTest {
     @Test
     public void testProperties() throws IOException {
         InternalContentAccess.resetInternalContent();
         ConfigurationImpl configurationImpl = new ConfigurationImpl();
        Map<String,Object> properties = ImmutableMap.of();
         configurationImpl.activate(properties);
         Assert.assertEquals("n",configurationImpl.getKeySpace());
     }
     @Test
     public void testPropertiesOSGiOverride() throws IOException {
         InternalContentAccess.resetInternalContent();
         ConfigurationImpl configurationImpl = new ConfigurationImpl();
         Map<String,Object> properties = ImmutableMap.of(ConfigurationImpl.INDEX_COLUMN_NAMES,(Object)"somethingElse");
         configurationImpl.activate(properties);
         Assert.assertArrayEquals(new String[]{"somethingElse"}, configurationImpl.getIndexColumnNames());
     }
     @Test
     public void testPropertiesSharedOverride() throws IOException {
         InternalContentAccess.resetInternalContent();
         ConfigurationImpl configurationImpl = new ConfigurationImpl();
         System.setProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY, "src/test/resources/testsharedoverride.properties");
        Map<String,Object> properties = ImmutableMap.of();
         configurationImpl.activate(properties);
         System.clearProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY);
         Assert.assertArrayEquals(new String[]{"somethingElseFromProperties"}, configurationImpl.getIndexColumnNames());
     }
     @Test
     public void testPropertiesSharedOverrideOSGi() throws IOException {
         InternalContentAccess.resetInternalContent();
         ConfigurationImpl configurationImpl = new ConfigurationImpl();
         System.setProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY, "src/test/resources/testsharedoverride.properties");
         Map<String,Object> properties = ImmutableMap.of(ConfigurationImpl.INDEX_COLUMN_NAMES,(Object)"somethingElse");
         configurationImpl.activate(properties);
         System.clearProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY);
         Assert.assertArrayEquals(new String[]{"somethingElse"}, configurationImpl.getIndexColumnNames());
     }
 }
