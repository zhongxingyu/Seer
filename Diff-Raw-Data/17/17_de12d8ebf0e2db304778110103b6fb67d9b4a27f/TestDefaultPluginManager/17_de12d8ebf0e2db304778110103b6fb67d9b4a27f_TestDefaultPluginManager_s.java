 package com.atlassian.plugin;
 
 import com.atlassian.plugin.loaders.SinglePluginLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import junit.framework.TestCase;
 
 import java.util.*;
 
 public class TestDefaultPluginManager extends TestCase
 {
     public void testRetrievePlugins() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         assertEquals(2, manager.getPlugins().size());
         assertEquals(1, manager.getEnabledPlugins().size());
         manager.enablePlugin("test.disabled.plugin");
         assertEquals(2, manager.getEnabledPlugins().size());
     }
 
     public void testEnabledDisabledRetrieval() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         // check non existant plugins don't show
         assertNull(manager.getPlugin("bull:shit"));
         assertNull(manager.getEnabledPlugin("bull:shit"));
         assertNull(manager.getPluginModule("bull:shit"));
         assertNull(manager.getEnabledPluginModule("bull:shit"));
 
         final String pluginKey = "test.atlassian.plugin";
         final String moduleKey = pluginKey + ":bear";
 
         // retrieve everything when enabled
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
 
         // now only retrieve via always retrieve methods
         manager.disablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
 
         // now enable again and check back to start
         manager.enablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
 
         // now let's disable the module, but not the plugin
         manager.disablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
 
         // now enable the module again
         manager.enablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
     }
 
     public void testDuplicatePluginKeysAreBad() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         try
         {
             manager.init();
             fail("Should have died with duplicate key exception.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Duplicate plugin key found: 'test.disabled.plugin'", e.getMessage());
         }
     }
 
     public void testGetPluginAndModules() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertNotNull(plugin);
         assertEquals("Test Plugin", plugin.getName());
 
         ModuleDescriptor bear = plugin.getModule("bear");
         assertEquals(bear, manager.getPluginModule("test.atlassian.plugin:bear"));
     }
 
     public void testGetModuleByModuleClassOneFound() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         Collection descriptors = manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class);
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         ModuleDescriptor moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         descriptors = manager.getEnabledModuleDescriptorsByClass(MockMineralModuleDescriptor.class);
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bar", moduleDescriptor.getName());
     }
 
     public void testGetModuleByModuleClassNoneFound() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         final Collection descriptors = manager.getEnabledModulesByClass(java.lang.String.class);
         assertNotNull(descriptors);
         assertTrue(descriptors.isEmpty());
     }
 
     public void testGetModuleDescriptorsByType() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         Map moduleDescriptors = new HashMap();
         moduleDescriptors.put("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptors.put("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptors);
         manager.init();
 
         Collection descriptors = manager.getEnabledModuleDescriptorsByType("animal");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         ModuleDescriptor moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         descriptors = manager.getEnabledModuleDescriptorsByType("mineral");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bar", moduleDescriptor.getName());
 
         try
         {
             manager.getEnabledModuleDescriptorsByType("foobar");
             fail("Should have thrown exception.");
         }
         catch (IllegalArgumentException e)
         {
         }
     }
 }
