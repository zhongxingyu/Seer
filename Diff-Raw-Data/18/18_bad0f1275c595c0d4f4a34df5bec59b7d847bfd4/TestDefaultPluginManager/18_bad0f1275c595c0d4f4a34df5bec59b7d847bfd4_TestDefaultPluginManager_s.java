 package com.atlassian.plugin;
 
 import com.atlassian.plugin.loaders.SinglePluginLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import com.atlassian.plugin.descriptors.MockUnusedModuleDescriptor;
 import junit.framework.TestCase;
 
 import java.util.*;
 
 public class TestDefaultPluginManager extends TestCase
 {
     public void testRetrievePlugins() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
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
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("bullshit", MockUnusedModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         // check non existant plugins don't show
         assertNull(manager.getPlugin("bull:shit"));
         assertNull(manager.getEnabledPlugin("bull:shit"));
         assertNull(manager.getPluginModule("bull:shit"));
         assertNull(manager.getEnabledPluginModule("bull:shit"));
         assertTrue(manager.getEnabledModuleDescriptorsByClass(TestCase.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("bullshit").isEmpty());
         assertTrue(manager.getEnabledModulesByClass(TestCase.class).isEmpty());
 
         final String pluginKey = "test.atlassian.plugin";
         final String moduleKey = pluginKey + ":bear";
 
         // retrieve everything when enabled
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(pluginKey + ":shit"));
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
 
         // now only retrieve via always retrieve methods
         manager.disablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
 
         // now enable again and check back to start
         manager.enablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
 
         // now let's disable the module, but not the plugin
         manager.disablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
 
         // now enable the module again
         manager.enablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
 
     }
 
     public void testDuplicatePluginKeysAreBad() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
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
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertNotNull(plugin);
         assertEquals("Test Plugin", plugin.getName());
 
         ModuleDescriptor bear = plugin.getModuleDescriptor("bear");
         assertEquals(bear, manager.getPluginModule("test.atlassian.plugin:bear"));
     }
 
     public void testGetModuleByModuleClassOneFound() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
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
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         final Collection descriptors = manager.getEnabledModulesByClass(java.lang.String.class);
         assertNotNull(descriptors);
         assertTrue(descriptors.isEmpty());
     }
 
     public void testGetModuleDescriptorsByType() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
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
