 package com.atlassian.plugin;
 
 import com.atlassian.plugin.descriptors.MockUnusedModuleDescriptor;
 import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
 import com.atlassian.plugin.impl.DynamicPlugin;
 import com.atlassian.plugin.impl.StaticPlugin;
 import com.atlassian.plugin.loaders.*;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.mock.*;
 import com.atlassian.plugin.parsers.DescriptorParser;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.predicate.PluginPredicate;
 import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import com.atlassian.plugin.test.PluginBuilder;
 import com.atlassian.plugin.repositories.FilePluginInstaller;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.factories.PluginFactory;
 import com.mockobjects.dynamic.C;
 import com.mockobjects.dynamic.Mock;
 import junit.framework.TestCase;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 /**
  * Testing {@link DefaultPluginManager}
  */
 public class TestDefaultPluginManager extends AbstractTestClassLoader
 {
     /**
      * the object being tested
      */
     private DefaultPluginManager manager;
 
     private PluginStateStore pluginStateStore;
     private List pluginLoaders;
     private DefaultModuleDescriptorFactory moduleDescriptorFactory; // we should be able to use the interface here?
 
     private DirectoryPluginLoader directoryPluginLoader;
     private PluginEventManager pluginEventManager;
 
 
     protected void setUp() throws Exception
     {
         super.setUp();
         pluginEventManager = new DefaultPluginEventManager();
 
         pluginStateStore = new MemoryPluginStateStore();
         pluginLoaders = new LinkedList();
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
 
         manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory, new DefaultPluginEventManager());
     }
 
     protected void tearDown() throws Exception
     {
         manager = null;
         moduleDescriptorFactory = null;
         pluginLoaders = null;
         pluginStateStore = null;
 
         if (directoryPluginLoader != null)
         {
             directoryPluginLoader.shutDown();
             directoryPluginLoader = null;
         }
 
         super.tearDown();
     }
 
     public void testRetrievePlugins() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         manager.init();
 
         assertEquals(2, manager.getPlugins().size());
         assertEquals(1, manager.getEnabledPlugins().size());
         manager.enablePlugin("test.disabled.plugin");
         assertEquals(2, manager.getEnabledPlugins().size());
     }
 
     public void testEnabledDisabledRetrieval() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("bullshit", MockUnusedModuleDescriptor.class);
         manager.init();
 
         // check non existant plugins don't show
         assertNull(manager.getPlugin("bull:shit"));
         assertNull(manager.getEnabledPlugin("bull:shit"));
         assertNull(manager.getPluginModule("bull:shit"));
         assertNull(manager.getEnabledPluginModule("bull:shit"));
         assertTrue(manager.getEnabledModuleDescriptorsByClass(NothingModuleDescriptor.class).isEmpty());
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
         assertEquals(new MockBear(), manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).get(0));
 
         // now only retrieve via always retrieve methods
         manager.disablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now enable again and check back to start
         manager.enablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now let's disable the module, but not the plugin
         manager.disablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now enable the module again
         manager.enablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
     }
 
     public void testDuplicatePluginKeysAreBad() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         try
         {
             manager.init();
             fail("Should have died with duplicate key exception.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Duplicate plugin found (installed version is the same or older) and could not be unloaded: 'test.disabled.plugin'", e.getMessage());
         }
     }
 
     public void testLoadOlderDuplicatePlugin() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin-newer.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         manager.init();
         assertEquals(1, manager.getEnabledPlugins().size());
     }
 
     public void testLoadNewerDuplicatePlugin() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin-newer.xml"));
         try
         {
             manager.init();
             fail("Should have died with duplicate key exception.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Duplicate plugin found (installed version is the same or older) and could not be unloaded: 'test.atlassian.plugin'", e.getMessage());
         }
     }
 
     public void testLoadNewerDuplicateDynamicPluginPreservesPluginState() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin-newer.xml"));
 
         PluginManagerState state = pluginStateStore.loadPluginState();
         state.setState("test.atlassian.plugin", Boolean.FALSE);
         pluginStateStore.savePluginState(state);
 
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertEquals("1.1", plugin.getPluginInformation().getVersion());
         assertFalse(manager.isPluginEnabled("test.atlassian.plugin"));
     }
 
     public void testLoadNewerDuplicateDynamicPluginPreservesModuleState() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin-newer.xml"));
 
         PluginManagerState state = pluginStateStore.loadPluginState();
         state.setState("test.atlassian.plugin:bear", Boolean.FALSE);
         pluginStateStore.savePluginState(state);
 
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertEquals("1.1", plugin.getPluginInformation().getVersion());
         assertFalse(manager.isPluginModuleEnabled("test.atlassian.plugin:bear"));
         assertTrue(manager.isPluginModuleEnabled("test.atlassian.plugin:gold"));
     }
 
     public void testLoadChangedDynamicPluginWithSameVersionNumberReplacesExisting() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin-changed-same-version.xml"));
 
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertEquals("Test Plugin (Changed)", plugin.getName());
     }
 
     public void testGetPluginsWithPluginMatchingPluginPredicate() throws Exception
     {
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.emptyList());
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockPluginPredicate = new Mock(PluginPredicate.class);
         mockPluginPredicate.expectAndReturn("matches", C.eq(plugin), true);
 
         manager.addPlugin(null, plugin);
         final Collection plugins = manager.getPlugins((PluginPredicate) mockPluginPredicate.proxy());
 
         assertEquals(1, plugins.size());
         assertTrue(plugins.contains(plugin));
         mockPluginPredicate.verify();
     }
 
     public void testGetPluginsWithPluginNotMatchingPluginPredicate() throws Exception
     {
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.emptyList());
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockPluginPredicate = new Mock(PluginPredicate.class);
         mockPluginPredicate.expectAndReturn("matches", C.eq(plugin), false);
 
         manager.addPlugin(null, plugin);
         final Collection plugins = manager.getPlugins((PluginPredicate) mockPluginPredicate.proxy());
 
         assertEquals(0, plugins.size());
         mockPluginPredicate.verify();
     }
 
     public void testGetPluginModulesWithModuleMatchingPredicate() throws Exception
     {
         final Object module = new Object();
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         final ModuleDescriptor moduleDescriptor = (ModuleDescriptor) mockModuleDescriptor.proxy();
         mockModuleDescriptor.expectAndReturn("getModule", module);
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), true);
 
         manager.addPlugin(null, plugin);
         final Collection modules = manager.getModules((ModuleDescriptorPredicate) mockModulePredicate.proxy());
 
         assertEquals(1, modules.size());
         assertTrue(modules.contains(module));
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModulesWithModuleNotMatchingPredicate() throws Exception
     {
         final Mock mockModule = new Mock(ModuleDescriptor.class);
         final ModuleDescriptor module = (ModuleDescriptor) mockModule.proxy();
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(module));
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(module), false);
 
         manager.addPlugin(null, plugin);
         final Collection modules = manager.getModules((ModuleDescriptorPredicate) mockModulePredicate.proxy());
 
         assertEquals(0, modules.size());
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModuleDescriptorWithModuleMatchingPredicate() throws Exception
     {
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         final ModuleDescriptor moduleDescriptor = (ModuleDescriptor) mockModuleDescriptor.proxy();
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), true);
 
         manager.addPlugin(null, plugin);
         final Collection modules = manager.getModuleDescriptors((ModuleDescriptorPredicate) mockModulePredicate.proxy());
 
         assertEquals(1, modules.size());
         assertTrue(modules.contains(moduleDescriptor));
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModuleDescriptorsWithModuleNotMatchingPredicate() throws Exception
     {
         final Mock mockModule = new Mock(ModuleDescriptor.class);
         final ModuleDescriptor module = (ModuleDescriptor) mockModule.proxy();
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(module));
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(module), false);
 
         manager.addPlugin(null, plugin);
         final Collection modules = manager.getModuleDescriptors((ModuleDescriptorPredicate) mockModulePredicate.proxy());
 
         assertEquals(0, modules.size());
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginAndModules() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertNotNull(plugin);
         assertEquals("Test Plugin", plugin.getName());
 
         ModuleDescriptor bear = plugin.getModuleDescriptor("bear");
         assertEquals(bear, manager.getPluginModule("test.atlassian.plugin:bear"));
     }
 
     public void testGetModuleByModuleClassOneFound() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
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
 
     public void testGetModuleByModuleClassAndDescriptor() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         Collection bearModules = manager.getEnabledModulesByClassAndDescriptor(new Class[]{MockAnimalModuleDescriptor.class, MockMineralModuleDescriptor.class}, MockBear.class);
         assertNotNull(bearModules);
         assertEquals(1, bearModules.size());
         assertTrue(bearModules.iterator().next() instanceof MockBear);
 
         Collection noModules = manager.getEnabledModulesByClassAndDescriptor(new Class[]{}, MockBear.class);
         assertNotNull(noModules);
         assertEquals(0, noModules.size());
 
         Collection mockThings = manager.getEnabledModulesByClassAndDescriptor(new Class[]{MockAnimalModuleDescriptor.class, MockMineralModuleDescriptor.class}, MockThing.class);
         assertNotNull(mockThings);
         assertEquals(2, mockThings.size());
         assertTrue(mockThings.iterator().next() instanceof MockThing);
         assertTrue(mockThings.iterator().next() instanceof MockThing);
 
         Collection mockThingsFromMineral = manager.getEnabledModulesByClassAndDescriptor(new Class[]{MockMineralModuleDescriptor.class}, MockThing.class);
         assertNotNull(mockThingsFromMineral);
         assertEquals(1, mockThingsFromMineral.size());
         final Object o = mockThingsFromMineral.iterator().next();
         assertTrue(o instanceof MockMineral);
     }
 
     public void testGetModuleByModuleClassNoneFound() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         final Collection descriptors = manager.getEnabledModulesByClass(java.lang.String.class);
         assertNotNull(descriptors);
         assertTrue(descriptors.isEmpty());
     }
 
     public void testGetModuleDescriptorsByType() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
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
         }
         catch (IllegalArgumentException e)
         {
             fail("Shouldn't have thrown exception.");
         }
     }
 
     public void testRetrievingDynamicResources() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         InputStream is = manager.getPluginResourceAsStream("test.atlassian.plugin.classloaded", "atlassian-plugin.xml");
         assertNotNull(is);
         IOUtils.closeQuietly(is);
     }
 
     public void testGetDynamicPluginClass() throws IOException, PluginParseException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
         try
         {
             manager.getDynamicPluginClass("com.atlassian.plugin.mock.MockPooh");
         }
         catch (ClassNotFoundException e)
         {
             fail(e.getMessage());
         }
     }
 
     public void testFindingNewPlugins() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         //delete paddington for the timebeing
         File paddington = new File(pluginsTestDir, PADDINGTON_JAR);
         paddington.delete();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         assertEquals(1, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
 
         //restore paddington to test plugins dir
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
 
         manager.scanForNewPlugins();
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
 
         manager.scanForNewPlugins();
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
     }
 
     private DefaultPluginManager makeClassLoadingPluginManager() throws PluginParseException
     {
         directoryPluginLoader = new DirectoryPluginLoader(pluginsTestDir,
                 Collections.<PluginFactory>singletonList(new LegacyDynamicPluginFactory(DefaultPluginManager.PLUGIN_DESCRIPTOR_FILENAME)),
                         pluginEventManager);
         pluginLoaders.add(directoryPluginLoader);
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         manager.init();
         return manager;
     }
 
     public void testRemovingPlugins() throws PluginException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
         MockAnimalModuleDescriptor moduleDescriptor = (MockAnimalModuleDescriptor) manager.getPluginModule("test.atlassian.plugin.classloaded:paddington");
         assertFalse(moduleDescriptor.disabled);
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertTrue("Module must have had disable() called before being removed", moduleDescriptor.disabled);
 
         // uninstalling a plugin should remove it's state completely from the state store - PLUG-13
         assertNull(pluginStateStore.loadPluginState().getState("test.atlassian.plugin.classloaded"));
 
         assertEquals(1, manager.getPlugins().size());
         assertNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertEquals(1, pluginsTestDir.listFiles().length);
     }
 
     public void testNonRemovablePlugins() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertFalse(plugin.isUninstallable());
         assertNotNull(plugin.getResourceAsStream("test-atlassian-plugin.xml"));
 
         try
         {
             manager.uninstall(plugin);
             fail("Where was the exception?");
         }
         catch (PluginException p)
         {
         }
     }
 
     public void testNonDeletablePlugins() throws PluginException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
 
         // Set plugin file can't be deleted.
         DynamicPlugin pluginToRemove = (DynamicPlugin) manager.getPlugin("test.atlassian.plugin.classloaded");
         pluginToRemove.setDeletable(false);
 
         // Disable plugin module before uninstall
         MockAnimalModuleDescriptor moduleDescriptor = (MockAnimalModuleDescriptor) manager.getPluginModule("test.atlassian.plugin.classloaded:paddington");
         assertFalse(moduleDescriptor.disabled);
 
         manager.uninstall(pluginToRemove);
 
         assertTrue("Module must have had disable() called before being removed", moduleDescriptor.disabled);
         assertEquals(1, manager.getPlugins().size());
         assertNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertEquals(2, pluginsTestDir.listFiles().length);
     }
 
     // These methods test the plugin compareTo() function, which compares plugins based on their version numbers.
     public void testComparePluginNewer()
     {
 
         Plugin p1 = createPluginWithVersion("1.1");
         Plugin p2 = createPluginWithVersion("1.0");
         assertTrue(p1.compareTo(p2) == 1);
 
         p1.getPluginInformation().setVersion("1.10");
         p2.getPluginInformation().setVersion("1.2");
         assertTrue(p1.compareTo(p2) == 1);
 
         p1.getPluginInformation().setVersion("1.2");
         p2.getPluginInformation().setVersion("1.01");
         assertTrue(p1.compareTo(p2) == 1);
 
         p1.getPluginInformation().setVersion("1.0.1");
         p2.getPluginInformation().setVersion("1.0");
         assertTrue(p1.compareTo(p2) == 1);
 
         p1.getPluginInformation().setVersion("1.2");
         p2.getPluginInformation().setVersion("1.1.1");
         assertTrue(p1.compareTo(p2) == 1);
     }
 
     public void testComparePluginOlder()
     {
 
         Plugin p1 = createPluginWithVersion("1.0");
         Plugin p2 = createPluginWithVersion("1.1");
         assertTrue(p1.compareTo(p2) == -1);
 
         p1.getPluginInformation().setVersion("1.2");
         p2.getPluginInformation().setVersion("1.10");
         assertTrue(p1.compareTo(p2) == -1);
 
         p1.getPluginInformation().setVersion("1.01");
         p2.getPluginInformation().setVersion("1.2");
         assertTrue(p1.compareTo(p2) == -1);
 
         p1.getPluginInformation().setVersion("1.0");
         p2.getPluginInformation().setVersion("1.0.1");
         assertTrue(p1.compareTo(p2) == -1);
 
         p1.getPluginInformation().setVersion("1.1.1");
         p2.getPluginInformation().setVersion("1.2");
         assertTrue(p1.compareTo(p2) == -1);
     }
 
     public void testComparePluginEqual()
     {
 
         Plugin p1 = createPluginWithVersion("1.0");
         Plugin p2 = createPluginWithVersion("1.0");
         assertTrue(p1.compareTo(p2) == 0);
 
         p1.getPluginInformation().setVersion("1.1.0.0");
         p2.getPluginInformation().setVersion("1.1");
         assertTrue(p1.compareTo(p2) == 0);
 
         p1.getPluginInformation().setVersion(" 1 . 1 ");
         p2.getPluginInformation().setVersion("1.1");
         assertTrue(p1.compareTo(p2) == 0);
     }
 
     // If we can't understand the version of a plugin, then take the new one.
     public void testComparePluginNoVersion()
     {
 
         Plugin p1 = createPluginWithVersion("1.0");
         Plugin p2 = createPluginWithVersion("#$%");
         assertEquals(-1, p1.compareTo(p2));
 
         p1.getPluginInformation().setVersion("#$%");
         p2.getPluginInformation().setVersion("1.0");
         assertEquals(-1, p1.compareTo(p2));
 
     }
 
     public void testComparePluginBadPlugin()
     {
 
         Plugin p1 = createPluginWithVersion("1.0");
         Plugin p2 = createPluginWithVersion("1.0");
 
         // Compare against something with a different key
         p2.setKey("bad.key");
        assertTrue(p1.compareTo(p2) != 0);
 
     }
 
     public void testInvalidationOfDynamicResourceCache() throws IOException, PluginException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         checkResources(manager, true, true);
         manager.disablePlugin("test.atlassian.plugin.classloaded");
         checkResources(manager, false, false);
         manager.enablePlugin("test.atlassian.plugin.classloaded");
         checkResources(manager, true, true);
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded"));
         checkResources(manager, false, false);
         //restore paddington to test plugins dir
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
         manager.scanForNewPlugins();
         checkResources(manager, true, true);
         // Resources from disabled modules are still available
         //manager.disablePluginModule("test.atlassian.plugin.classloaded:paddington");
         //checkResources(manager, true, false);
     }
 
     public void testValidatePlugin() throws PluginParseException
     {
         DefaultPluginManager manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory, new DefaultPluginEventManager());
         Mock mockLoader = new Mock(DynamicPluginLoader.class);
         pluginLoaders.add(mockLoader.proxy());
 
         Mock mockPluginJar = new Mock(PluginArtifact.class);
         PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
         mockLoader.expectAndReturn("canLoad", C.args(C.eq(pluginArtifact)), "foo");
 
         String key = manager.validatePlugin(pluginArtifact);
         assertEquals("foo", key);
         mockLoader.verify();
 
     }
 
     public void testValidatePluginWithNoDynamicLoaders() throws PluginParseException
     {
         DefaultPluginManager manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory, new DefaultPluginEventManager());
         Mock mockLoader = new Mock(PluginLoader.class);
         pluginLoaders.add(mockLoader.proxy());
 
         Mock mockPluginJar = new Mock(PluginArtifact.class);
         PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
         try
         {
             manager.validatePlugin(pluginArtifact);
             fail("Should have thrown exception");
         }
         catch (IllegalStateException ex)
         {
             // test passed
         }
     }
 
     public void testInvalidationOfDynamicClassCache() throws IOException, PluginException
     {
         createFillAndCleanTempPluginDirectory();
 
         DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         checkClasses(manager, true);
         manager.disablePlugin("test.atlassian.plugin.classloaded");
         checkClasses(manager, false);
         manager.enablePlugin("test.atlassian.plugin.classloaded");
         checkClasses(manager, true);
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded"));
         checkClasses(manager, false);
         //restore paddington to test plugins dir
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
         manager.scanForNewPlugins();
         checkClasses(manager, true);
     }
 
     public void testInstallPlugin() throws Exception
     {
         Mock mockPluginStateStore = new Mock(PluginStateStore.class);
         Mock mockModuleDescriptorFactory = new Mock(ModuleDescriptorFactory.class);
         Mock mockPluginLoader = new Mock(DynamicPluginLoader.class);
         Mock mockDescriptorParserFactory = new Mock(DescriptorParserFactory.class);
         Mock mockDescriptorParser = new Mock(DescriptorParser.class);
         Mock mockPluginJar = new Mock(PluginArtifact.class);
         Mock mockRepository = new Mock(PluginInstaller.class);
         Mock mockPlugin = new Mock(Plugin.class);
 
         ModuleDescriptorFactory moduleDescriptorFactory = (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy();
 
         DefaultPluginManager pluginManager = new DefaultPluginManager(
                 (PluginStateStore) mockPluginStateStore.proxy(),
                 Collections.<PluginLoader>singletonList((PluginLoader) mockPluginLoader.proxy()),
                 moduleDescriptorFactory, new DefaultPluginEventManager()
         );
 
         Plugin plugin = (Plugin) mockPlugin.proxy();
         PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
 
         mockPluginStateStore.expectAndReturn("loadPluginState", new PluginManagerState());
         mockPluginStateStore.expectAndReturn("loadPluginState", new PluginManagerState());
         mockDescriptorParser.matchAndReturn("getKey", "test");
         mockRepository.expect("installPlugin", C.args(C.eq("test"), C.eq(pluginArtifact)));
         mockPluginLoader.expectAndReturn("loadAllPlugins", C.eq(moduleDescriptorFactory), Collections.emptyList());
         mockPluginLoader.expectAndReturn("supportsAddition", true);
         mockPluginLoader.expectAndReturn("addFoundPlugins", moduleDescriptorFactory, Collections.singletonList(plugin));
         mockPluginLoader.expectAndReturn("canLoad", C.args(C.eq(pluginArtifact)), "test");
         mockPlugin.matchAndReturn("getKey", "test");
         mockPlugin.matchAndReturn("hashCode", mockPlugin.hashCode());
         mockPlugin.expectAndReturn("getModuleDescriptors", new ArrayList());
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
         mockPlugin.expect("setEnabled", C.args(C.IS_TRUE));
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
 
         pluginManager.setPluginInstaller((PluginInstaller) mockRepository.proxy());
         pluginManager.init();
         pluginManager.installPlugin(pluginArtifact);
 
         assertEquals(plugin, pluginManager.getPlugin("test"));
         assertTrue(pluginManager.isPluginEnabled("test"));
 
         mockPlugin.verify();
         mockRepository.verify();
         mockPluginJar.verify();
         mockDescriptorParser.verify();
         mockDescriptorParserFactory.verify();
         mockPluginLoader.verify();
         mockPluginStateStore.verify();
     }
 
     private void checkResources(PluginAccessor manager, boolean canGetGlobal, boolean canGetModule) throws IOException
     {
         InputStream is = manager.getDynamicResourceAsStream("icon.gif");
         assertEquals(canGetGlobal, is != null);
         IOUtils.closeQuietly(is);
         is = manager.getDynamicResourceAsStream("bear/paddington.vm");
         assertEquals(canGetModule, is != null);
         IOUtils.closeQuietly(is);
     }
 
     private void checkClasses(PluginAccessor manager, boolean canGet)
     {
         try
         {
             manager.getDynamicPluginClass("com.atlassian.plugin.mock.MockPaddington");
             if (!canGet)
             {
                 fail("Class in plugin was successfully loaded");
             }
         }
         catch (ClassNotFoundException e)
         {
             if (canGet)
             {
                 fail(e.getMessage());
             }
         }
     }
 
     public void testInstallPluginTwiceWithSameName() throws Exception
     {
         createFillAndCleanTempPluginDirectory();
 
         FileUtils.cleanDirectory(pluginsTestDir);
         File plugin = File.createTempFile("plugin", ".jar");
         new PluginBuilder("plugin")
                 .addPluginInformation("some.key", "My name", "1.0", 1)
                 .addResource("foo.txt", "foo")
                 .addJava("my.MyClass", "package my; public class MyClass {}")
                 .build()
                 .renameTo(plugin);
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         manager.setPluginInstaller(new FilePluginInstaller(pluginsTestDir));
 
         String pluginKey = manager.installPlugin(new JarPluginArtifact(plugin));
 
         assertTrue(new File(pluginsTestDir,plugin.getName()).exists());
 
         final Plugin installedPlugin = manager.getPlugin(pluginKey);
         assertNotNull(installedPlugin);
         assertNotNull(installedPlugin.getClassLoader().getResourceAsStream("foo.txt"));
         assertNull(installedPlugin.getClassLoader().getResourceAsStream("bar.txt"));
         assertNotNull(installedPlugin.getClassLoader().loadClass("my.MyClass"));
         try
         {
             installedPlugin.getClassLoader().loadClass("my.MyNewClass");
             fail("Expected ClassNotFoundException for unknown class");
         }
         catch (ClassNotFoundException e)
         {
             // expected
         }
 
         // sleep to ensure the new plugin is picked up
         Thread.currentThread().sleep(1000);
 
         new PluginBuilder("plugin")
                 .addPluginInformation("some.key", "My name", "1.0", 1)
                 .addResource("bar.txt", "bar")
                 .addJava("my.MyNewClass", "package my; public class MyNewClass {}")
                 .build()
                 .renameTo(plugin);
 
         // reinstall the plugin
         String pluginKey2 = manager.installPlugin(new JarPluginArtifact(plugin));
 
         assertTrue(new File(pluginsTestDir,plugin.getName()).exists());
 
         final Plugin installedPlugin2 = manager.getPlugin(pluginKey2);
         assertNotNull(installedPlugin2);
         assertEquals(1, manager.getEnabledPlugins().size());
         assertNull(installedPlugin2.getClassLoader().getResourceAsStream("foo.txt"));
         assertNotNull(installedPlugin2.getClassLoader().getResourceAsStream("bar.txt"));
         assertNotNull(installedPlugin2.getClassLoader().loadClass("my.MyNewClass"));
         try
         {
             installedPlugin2.getClassLoader().loadClass("my.MyClass");
             fail("Expected ClassNotFoundException for unknown class");
         }
         catch (ClassNotFoundException e)
         {
             // expected
         }
     }
 
     public void testInstallPluginTwiceWithDifferentName() throws Exception
     {
         createFillAndCleanTempPluginDirectory();
 
         FileUtils.cleanDirectory(pluginsTestDir);
         File plugin1 = new PluginBuilder("plugin")
                 .addPluginInformation("some.key", "My name", "1.0", 1)
                 .addResource("foo.txt", "foo")
                 .addJava("my.MyClass", "package my; public class MyClass {}")
                 .build();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         manager.setPluginInstaller(new FilePluginInstaller(pluginsTestDir));
 
         String pluginKey = manager.installPlugin(new JarPluginArtifact(plugin1));
 
         assertTrue(new File(pluginsTestDir,plugin1.getName()).exists());
 
         final Plugin installedPlugin = manager.getPlugin(pluginKey);
         assertNotNull(installedPlugin);
         assertNotNull(installedPlugin.getClassLoader().getResourceAsStream("foo.txt"));
         assertNull(installedPlugin.getClassLoader().getResourceAsStream("bar.txt"));
         assertNotNull(installedPlugin.getClassLoader().loadClass("my.MyClass"));
         try
         {
             installedPlugin.getClassLoader().loadClass("my.MyNewClass");
             fail("Expected ClassNotFoundException for unknown class");
         }
         catch (ClassNotFoundException e)
         {
             // expected
         }
 
         // sleep to ensure the new plugin is picked up
         Thread.currentThread().sleep(1000);
 
         File plugin2 = new PluginBuilder("plugin")
                 .addPluginInformation("some.key", "My name", "1.0", 1)
                 .addResource("bar.txt", "bar")
                 .addJava("my.MyNewClass", "package my; public class MyNewClass {}")
                 .build();
 
         // reinstall the plugin
         String pluginKey2 = manager.installPlugin(new JarPluginArtifact(plugin2));
 
         assertFalse(new File(pluginsTestDir,plugin1.getName()).exists());
         assertTrue(new File(pluginsTestDir,plugin2.getName()).exists());
 
         final Plugin installedPlugin2 = manager.getPlugin(pluginKey2);
         assertNotNull(installedPlugin2);
         assertEquals(1, manager.getEnabledPlugins().size());
         assertNull(installedPlugin2.getClassLoader().getResourceAsStream("foo.txt"));
         assertNotNull(installedPlugin2.getClassLoader().getResourceAsStream("bar.txt"));
         assertNotNull(installedPlugin2.getClassLoader().loadClass("my.MyNewClass"));
         try
         {
             installedPlugin2.getClassLoader().loadClass("my.MyClass");
             fail("Expected ClassNotFoundException for unknown class");
         }
         catch (ClassNotFoundException e)
         {
             // expected
         }
     }
 
     public Plugin createPluginWithVersion(String version)
     {
         Plugin p = new StaticPlugin();
         p.setKey("test.default.plugin");
         p.setPluginInformation(new PluginInformation());
         PluginInformation pInfo = p.getPluginInformation();
         pInfo.setVersion(version);
         return p;
     }
 
     /**
      * Dummy plugin loader that reports that removal is supported and returns plugins that report that they can
      * be uninstalled.
      */
     private static class SinglePluginLoaderWithRemoval extends SinglePluginLoader
     {
         public SinglePluginLoaderWithRemoval(String resource)
         {
             super(resource);
         }
 
         public SinglePluginLoaderWithRemoval(InputStream is)
         {
             super(is);
         }
 
         public boolean supportsRemoval()
         {
 
             return true;
         }
 
         public void removePlugin(Plugin plugin) throws PluginException
         {
             plugins = Collections.emptyList();
         }
 
         protected StaticPlugin getNewPlugin()
         {
             return new StaticPlugin()
             {
                 public boolean isUninstallable()
                 {
                     return true;
                 }
             };
         }
     }
 
     class NothingModuleDescriptor extends MockUnusedModuleDescriptor {}
 }
