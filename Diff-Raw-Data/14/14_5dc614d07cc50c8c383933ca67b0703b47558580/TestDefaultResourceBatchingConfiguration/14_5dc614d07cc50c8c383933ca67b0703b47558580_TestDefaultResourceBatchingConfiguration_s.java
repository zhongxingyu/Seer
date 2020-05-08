 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.util.PluginUtils;
 import junit.framework.TestCase;
 
 public class TestDefaultResourceBatchingConfiguration extends TestCase
 {
     private DefaultResourceBatchingConfiguration batchingConfiguration;
 
     @Override
     public void setUp() throws Exception
     {
         super.setUp();
 
         batchingConfiguration = new DefaultResourceBatchingConfiguration();
     }
 
     @Override
     public void tearDown() throws Exception
     {
         batchingConfiguration = null;
         super.tearDown();
     }
 
     public void testIsPluginWebResourceBatchingEnabled()
     {
         try
         {
             System.clearProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF);
             System.setProperty(PluginUtils.ATLASSIAN_DEV_MODE, "true");
            assertTrue(batchingConfiguration.isPluginWebResourceBatchingEnabled());
 
             System.setProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF, "true");
             System.clearProperty(PluginUtils.ATLASSIAN_DEV_MODE);
            assertTrue(batchingConfiguration.isPluginWebResourceBatchingEnabled());
 
             System.clearProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF);
             System.clearProperty(PluginUtils.ATLASSIAN_DEV_MODE);
            assertFalse(batchingConfiguration.isPluginWebResourceBatchingEnabled());
 
             System.clearProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF);
             System.setProperty(PluginUtils.ATLASSIAN_DEV_MODE, "false");
            assertFalse(batchingConfiguration.isPluginWebResourceBatchingEnabled());
 
             System.setProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF, "false");
             System.setProperty(PluginUtils.ATLASSIAN_DEV_MODE, "true");
            assertFalse(batchingConfiguration.isPluginWebResourceBatchingEnabled());
         }
         finally
         {
             System.clearProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF);
             System.clearProperty(PluginUtils.ATLASSIAN_DEV_MODE);
         }
     }
     
 }
