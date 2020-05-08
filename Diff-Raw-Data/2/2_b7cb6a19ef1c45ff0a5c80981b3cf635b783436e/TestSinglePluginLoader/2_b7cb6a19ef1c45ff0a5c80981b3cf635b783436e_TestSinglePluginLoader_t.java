 package com.atlassian.plugin.loaders;
 
 import com.atlassian.core.util.ClassLoaderUtils;
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.descriptors.ResourcedModuleDescriptor;
 import com.atlassian.plugin.mock.*;
 import junit.framework.TestCase;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class TestSinglePluginLoader extends TestCase
 {
     public void testAtlassianPlugin() throws Exception
     {
         SinglePluginLoader loader = new SinglePluginLoader("test-atlassian-plugin.xml");
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
         Collection plugins = loader.getPlugins(moduleDescriptors);
 
         assertEquals(1, plugins.size());
 
         // test the plugin information
         Plugin plugin = (Plugin) plugins.iterator().next();
         assertEquals("Test Plugin", plugin.getName());
         assertEquals("test.atlassian.plugin", plugin.getKey());
         assertNotNull(plugin.getPluginInformation());
         assertEquals("1.0", plugin.getPluginInformation().getVersion());
         assertEquals("This plugin descriptor is just used for test purposes!", plugin.getPluginInformation().getDescription());
         assertEquals("Atlassian Software Systems Pty Ltd", plugin.getPluginInformation().getVendorName());
         assertEquals("http://www.atlassian.com", plugin.getPluginInformation().getVendorUrl());
         assertEquals(3f, plugin.getPluginInformation().getMinVersion(), 0);
         assertEquals(3.1f, plugin.getPluginInformation().getMaxVersion(), 0);
         assertEquals(2, plugin.getModules().size());
 
         ResourcedModuleDescriptor bearDescriptor = (ResourcedModuleDescriptor) plugin.getModule("bear");
         assertEquals("test.atlassian.plugin:bear", bearDescriptor.getCompleteKey());
         assertEquals("bear", bearDescriptor.getKey());
         assertEquals("Bear Animal", bearDescriptor.getName());
         assertEquals(MockBear.class, bearDescriptor.getModuleClass());
         assertEquals("A plugin module to describe a bear", bearDescriptor.getDescription());
 
         List resources = bearDescriptor.getResourceDescriptors();
         assertEquals(3, resources.size());
 
         assertEquals("20", bearDescriptor.getParams().get("height"));
         assertEquals("brown", bearDescriptor.getParams().get("colour"));
 
         List goldDescriptors = plugin.getModuleDescriptorsByClass(MockGold.class);
         assertEquals(1, goldDescriptors.size());
         ModuleDescriptor goldDescriptor = (ModuleDescriptor) goldDescriptors.get(0);
         assertEquals("test.atlassian.plugin:gold", goldDescriptor.getCompleteKey());
         assertEquals(new MockGold(20), goldDescriptor.getModule());
         assertEquals(goldDescriptors, plugin.getModuleDescriptorsByClass(MockMineral.class));
     }
 
     public void testDisabledPlugin() throws PluginParseException
     {
         SinglePluginLoader loader = new SinglePluginLoader("test-disabled-plugin.xml");
         Collection plugins = loader.getPlugins(new HashMap());
         assertEquals(1, plugins.size());
         assertFalse(((Plugin) plugins.iterator().next()).isEnabledByDefault());
     }
 
     public void testPluginByUrl() throws PluginParseException
     {
         SinglePluginLoader loader = new SinglePluginLoader(ClassLoaderUtils.getResourceAsStream("test-disabled-plugin.xml", SinglePluginLoader.class));        Collection plugins = loader.getPlugins(new HashMap());
         assertEquals(1, plugins.size());
         assertFalse(((Plugin) plugins.iterator().next()).isEnabledByDefault());
     }
 
     public void testUnfoundPlugin() throws PluginParseException
     {
         try
         {
             SinglePluginLoader loader = new SinglePluginLoader("bullshit.xml");
             loader.getPlugins(null);
             fail("Should have blown up.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Couldn't find resource: bullshit.xml", e.getMessage());
         }
     }
 
     public void testUnknownPluginModule() throws PluginParseException
     {
         try
         {
             SinglePluginLoader loader = new SinglePluginLoader("test-bad-plugin.xml");
             loader.getPlugins(new HashMap());
             fail("Should have blown up.");
         }
         catch (PluginParseException e)
         {
            assertEquals("Could not find descriptor for module 'unknown-plugin' in plugin 'Bad Plugin'", e.getMessage());
         }
     }
 
     public void testBadPluginKey() throws PluginParseException
     {
         try
         {
             SinglePluginLoader loader = new SinglePluginLoader("test-bad-plugin-key-plugin.xml");
             loader.getPlugins(null);
             fail("Should have blown up.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Plugin key's cannot contain ':'. Key is 'test:bad'", e.getMessage());
         }
     }
 
     public void testNonUniqueKeysWithinAPlugin() throws PluginParseException
     {
         SinglePluginLoader loader = new SinglePluginLoader("test-bad-non-unique-keys-plugin.xml");
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
         try
         {
             loader.getPlugins(moduleDescriptors);
             fail("Should have died with duplicate key exception.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Found duplicate key 'bear' within plugin 'test.bad.plugin'", e.getMessage());
         }
     }
 
     public void testBadResource()
     {
         try
         {
             new SinglePluginLoader("foo").getPlugins(null);
             fail("Should have thrown exception");
         }
         catch (PluginParseException e)
         {
             return;
         }
     }
 }
