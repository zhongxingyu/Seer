 package com.atlassian.plugin.manager;
 
 import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
 import com.atlassian.plugin.descriptors.MockUnusedModuleDescriptor;
 import com.atlassian.plugin.descriptors.RequiresRestart;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginDisabledEvent;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
 import com.atlassian.plugin.event.listeners.FailListener;
 import com.atlassian.plugin.event.listeners.PassListener;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.factories.XmlDynamicPluginFactory;
 import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
 import com.atlassian.plugin.impl.DynamicPlugin;
 import com.atlassian.plugin.impl.StaticPlugin;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.loaders.DirectoryPluginLoader;
 import com.atlassian.plugin.loaders.DynamicPluginLoader;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.loaders.SinglePluginLoader;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
 import com.atlassian.plugin.mock.*;
 import com.atlassian.plugin.parsers.DescriptorParser;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
 import com.atlassian.plugin.predicate.PluginPredicate;
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.test.PluginJarBuilder;
 import com.mockobjects.dynamic.C;
 import com.mockobjects.dynamic.Mock;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 public class TestDefaultPluginManager extends AbstractTestClassLoader
 {
     /**
      * the object being tested
      */
     private DefaultPluginManager manager;
 
     private PluginPersistentStateStore pluginStateStore;
     private List<PluginLoader> pluginLoaders;
     private DefaultModuleDescriptorFactory moduleDescriptorFactory; // we should be able to use the interface here?
 
     private DirectoryPluginLoader directoryPluginLoader;
     private PluginEventManager pluginEventManager;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         pluginEventManager = new DefaultPluginEventManager();
 
         pluginStateStore = new MemoryPluginPersistentStateStore();
         pluginLoaders = new ArrayList<PluginLoader>();
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
 
         manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory, pluginEventManager);
     }
 
     @Override
     protected void tearDown() throws Exception
     {
         manager = null;
         moduleDescriptorFactory = null;
         pluginLoaders = null;
         pluginStateStore = null;
 
         if (directoryPluginLoader != null)
         {
             directoryPluginLoader = null;
         }
         if (manager != null)
         {
             manager.shutdown();
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
 
     public void testEnableModuleFailed() throws PluginParseException
     {
         final Mock mockPluginLoader = new Mock(PluginLoader.class);
         final ModuleDescriptor<Object> moduleDescriptor = new AbstractModuleDescriptor<Object>()
         {
             @Override
             public String getKey()
             {
                 return "bar";
             }
 
             @Override
             public String getCompleteKey()
             {
                 return "foo:bar";
             }
 
             @Override
             public void enabled()
             {
                 throw new IllegalArgumentException("Cannot enable");
             }
 
             @Override
             public Object getModule()
             {
                 return null;
             }
         };
         Plugin plugin = new StaticPlugin()
         {
             @Override
             public Collection<ModuleDescriptor<?>> getModuleDescriptors()
             {
                 return Collections.<ModuleDescriptor<?>> singletonList(moduleDescriptor);
             }
 
             @Override
             public ModuleDescriptor<Object> getModuleDescriptor(final String key)
             {
                 return moduleDescriptor;
             }
         };
         plugin.setKey("foo");
         plugin.setEnabledByDefault(true);
         plugin.setPluginInformation(new PluginInformation());
 
         mockPluginLoader.expectAndReturn("loadAllPlugins", C.ANY_ARGS, Collections.singletonList(plugin));
 
         @SuppressWarnings("unchecked")
         final PluginLoader loader = (PluginLoader) mockPluginLoader.proxy();
         pluginLoaders.add(loader);
 
         pluginEventManager.register(new FailListener(PluginEnabledEvent.class));
 
         manager.init();
 
         assertEquals(1, manager.getPlugins().size());
         assertEquals(0, manager.getEnabledPlugins().size());
         plugin = manager.getPlugin("foo");
         assertFalse(plugin.getPluginState() == PluginState.ENABLED);
         assertTrue(plugin instanceof UnloadablePlugin);
     }
 
     public void testEnabledModuleOutOfSyncWithPlugin() throws PluginParseException
     {
         final Mock mockPluginLoader = new Mock(PluginLoader.class);
         Plugin plugin = new StaticPlugin();
         plugin.setKey("foo");
         plugin.setEnabledByDefault(true);
         plugin.setPluginInformation(new PluginInformation());
 
         mockPluginLoader.expectAndReturn("loadAllPlugins", C.ANY_ARGS, Collections.singletonList(plugin));
 
         @SuppressWarnings("unchecked")
         final PluginLoader loader = (PluginLoader) mockPluginLoader.proxy();
         pluginLoaders.add(loader);
         manager.init();
 
         assertEquals(1, manager.getPlugins().size());
         assertEquals(1, manager.getEnabledPlugins().size());
         plugin = manager.getPlugin("foo");
         assertTrue(plugin.getPluginState() == PluginState.ENABLED);
         assertTrue(manager.isPluginEnabled("foo"));
         plugin.disable();
         assertFalse(plugin.getPluginState() == PluginState.ENABLED);
         assertFalse(manager.isPluginEnabled("foo"));
     }
 
     public void testEnabledDisabledRetrieval() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("bullshit", MockUnusedModuleDescriptor.class);
 
         final PassListener enabledListener = new PassListener(PluginEnabledEvent.class);
         final PassListener disabledListener = new PassListener(PluginDisabledEvent.class);
         pluginEventManager.register(enabledListener);
         pluginEventManager.register(disabledListener);
 
         manager.init();
 
         // check non existant plugins don't show
         assertNull(manager.getPlugin("bull:shit"));
         assertNull(manager.getEnabledPlugin("bull:shit"));
         assertNull(manager.getPluginModule("bull:shit"));
         assertNull(manager.getEnabledPluginModule("bull:shit"));
         assertTrue(manager.getEnabledModuleDescriptorsByClass(NothingModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("bullshit").isEmpty());
 
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
         assertFalse(manager.getEnabledModulesByClass(MockBear.class).isEmpty());
         assertEquals(new MockBear(), manager.getEnabledModulesByClass(MockBear.class).get(0));
         enabledListener.assertCalled();
 
         // now only retrieve via always retrieve methods
         manager.disablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
         disabledListener.assertCalled();
 
         // now enable again and check back to start
         manager.enablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
         enabledListener.assertCalled();
 
         // now let's disable the module, but not the plugin
         pluginEventManager.register(new FailListener(PluginEnabledEvent.class));
         manager.disablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now enable the module again
         pluginEventManager.register(new FailListener(PluginDisabledEvent.class));
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
         catch (final PluginParseException e)
         {
             assertEquals("Duplicate plugin found (installed version is the same or older) and could not be unloaded: 'test.disabled.plugin'",
                 e.getMessage());
         }
     }
 
     public void testLoadOlderDuplicatePlugin() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         pluginLoaders.add(new MultiplePluginLoader("test-atlassian-plugin-newer.xml"));
         pluginLoaders.add(new MultiplePluginLoader("test-atlassian-plugin.xml", "test-another-plugin.xml"));
         manager.init();
         assertEquals(2, manager.getEnabledPlugins().size());
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
         catch (final PluginParseException e)
         {
             assertEquals("Duplicate plugin found (installed version is the same or older) and could not be unloaded: 'test.atlassian.plugin'",
                 e.getMessage());
         }
     }
 
     public void testLoadNewerDuplicateDynamicPluginPreservesPluginState() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin.xml"));
         manager.init();
 
         final DefaultPluginPersistentState state = new DefaultPluginPersistentState(pluginStateStore.load());
         state.setEnabled(manager.getPlugin("test.atlassian.plugin"), false);
         pluginStateStore.save(state);
 
         assertFalse(manager.isPluginEnabled("test.atlassian.plugin"));
         manager.shutdown();
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin-newer.xml"));
         manager.init();
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertEquals("1.1", plugin.getPluginInformation().getVersion());
         assertFalse(manager.isPluginEnabled("test.atlassian.plugin"));
     }
 
     public void testLoadNewerDuplicateDynamicPluginPreservesModuleState() throws PluginParseException
     {
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin.xml"));
         manager.init();
 
         final DefaultPluginPersistentState state = new DefaultPluginPersistentState(pluginStateStore.load());
         state.setEnabled(manager.getPluginModule("test.atlassian.plugin:bear"), false);
         pluginStateStore.save(state);
 
         manager.shutdown();
 
         pluginLoaders.add(new SinglePluginLoaderWithRemoval("test-atlassian-plugin-newer.xml"));
         manager.init();
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
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
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertEquals("Test Plugin (Changed)", plugin.getName());
     }
 
     public void testGetPluginsWithPluginMatchingPluginPredicate() throws Exception
     {
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.emptyList());
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockPluginPredicate = new Mock(PluginPredicate.class);
         mockPluginPredicate.expectAndReturn("matches", C.eq(plugin), true);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         final Collection<Plugin> plugins = manager.getPlugins((PluginPredicate) mockPluginPredicate.proxy());
 
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
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockPluginPredicate = new Mock(PluginPredicate.class);
         mockPluginPredicate.expectAndReturn("matches", C.eq(plugin), false);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         final Collection<Plugin> plugins = manager.getPlugins((PluginPredicate) mockPluginPredicate.proxy());
 
         assertEquals(0, plugins.size());
         mockPluginPredicate.verify();
     }
 
     public void testGetPluginModulesWithModuleMatchingPredicate() throws Exception
     {
         final MockThing module = new MockThing()
         {};
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         @SuppressWarnings("unchecked")
         final ModuleDescriptor<MockThing> moduleDescriptor = (ModuleDescriptor<MockThing>) mockModuleDescriptor.proxy();
         mockModuleDescriptor.expectAndReturn("getModule", module);
         mockModuleDescriptor.matchAndReturn("getCompleteKey", "some-plugin-key:module");
         mockModuleDescriptor.matchAndReturn("isEnabledByDefault", true);
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("getModuleDescriptor", "module", moduleDescriptor);
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.matchAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), true);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         @SuppressWarnings("unchecked")
         final ModuleDescriptorPredicate<MockThing> predicate = (ModuleDescriptorPredicate<MockThing>) mockModulePredicate.proxy();
         final Collection<MockThing> modules = manager.getModules(predicate);
 
         assertEquals(1, modules.size());
         assertTrue(modules.contains(module));
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModulesWithGetModuleThrowingException() throws Exception
     {
         Plugin badPlugin = new StaticPlugin();
         badPlugin.setKey("bad");
         MockModuleDescriptor<Object> badDescriptor = new MockModuleDescriptor<Object>(badPlugin, "bad", new Object())
         {
             @Override
             public Object getModule()
             {
                 throw new RuntimeException();
             }
         };
         badPlugin.addModuleDescriptor(badDescriptor);
 
         Plugin goodPlugin = new StaticPlugin();
         goodPlugin.setKey("good");
         MockModuleDescriptor<Object> goodDescriptor = new MockModuleDescriptor<Object>(goodPlugin, "good", new Object());
         goodPlugin.addModuleDescriptor(goodDescriptor);
 
 
         manager.addPlugins(null, Arrays.asList(goodPlugin, badPlugin));
         manager.enablePlugin("bad");
         manager.enablePlugin("good");
 
         assertTrue(manager.isPluginEnabled("bad"));
         assertTrue(manager.isPluginEnabled("good"));
         final Collection<Object> modules = manager.getEnabledModulesByClass(Object.class);
 
         assertEquals(1, modules.size());
         assertFalse(manager.isPluginEnabled("bad"));
         assertTrue(manager.isPluginEnabled("good"));
     }
 
     public void testGetPluginModulesWithModuleNotMatchingPredicate() throws Exception
     {
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         @SuppressWarnings("unchecked")
         final ModuleDescriptor<MockThing> moduleDescriptor = (ModuleDescriptor<MockThing>) mockModuleDescriptor.proxy();
         mockModuleDescriptor.matchAndReturn("getCompleteKey", "some-plugin-key:module");
         mockModuleDescriptor.matchAndReturn("isEnabledByDefault", true);
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("getModuleDescriptor", "module", moduleDescriptor);
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.matchAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), false);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         @SuppressWarnings("unchecked")
         final ModuleDescriptorPredicate<MockThing> predicate = (ModuleDescriptorPredicate<MockThing>) mockModulePredicate.proxy();
         final Collection<MockThing> modules = manager.getModules(predicate);
 
         assertEquals(0, modules.size());
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModuleDescriptorWithModuleMatchingPredicate() throws Exception
     {
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         @SuppressWarnings("unchecked")
         final ModuleDescriptor<MockThing> moduleDescriptor = (ModuleDescriptor) mockModuleDescriptor.proxy();
         mockModuleDescriptor.matchAndReturn("getCompleteKey", "some-plugin-key:module");
         mockModuleDescriptor.matchAndReturn("isEnabledByDefault", true);
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("getModuleDescriptor", "module", moduleDescriptor);
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.matchAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), true);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         @SuppressWarnings("unchecked")
         final ModuleDescriptorPredicate<MockThing> predicate = (ModuleDescriptorPredicate<MockThing>) mockModulePredicate.proxy();
         final Collection<ModuleDescriptor<MockThing>> modules = manager.getModuleDescriptors(predicate);
 
         assertEquals(1, modules.size());
         assertTrue(modules.contains(moduleDescriptor));
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginModuleDescriptorsWithModuleNotMatchingPredicate() throws Exception
     {
         final Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
         @SuppressWarnings("unchecked")
         final ModuleDescriptor<MockThing> moduleDescriptor = (ModuleDescriptor<MockThing>) mockModuleDescriptor.proxy();
         mockModuleDescriptor.matchAndReturn("getCompleteKey", "some-plugin-key:module");
         mockModuleDescriptor.matchAndReturn("isEnabledByDefault", true);
 
         final Mock mockPlugin = new Mock(Plugin.class);
         mockPlugin.matchAndReturn("getKey", "some-plugin-key");
         mockPlugin.matchAndReturn("getModuleDescriptors", Collections.singleton(moduleDescriptor));
         mockPlugin.matchAndReturn("getModuleDescriptor", "module", moduleDescriptor);
         mockPlugin.matchAndReturn("hashCode", 12);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.matchAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
 
         final Mock mockModulePredicate = new Mock(ModuleDescriptorPredicate.class);
         mockModulePredicate.expectAndReturn("matches", C.eq(moduleDescriptor), false);
 
         manager.addPlugins(null, Collections.singletonList(plugin));
         @SuppressWarnings("unchecked")
         final ModuleDescriptorPredicate<MockThing> predicate = (ModuleDescriptorPredicate<MockThing>) mockModulePredicate.proxy();
         final Collection<MockThing> modules = manager.getModules(predicate);
 
         assertEquals(0, modules.size());
 
         mockModulePredicate.verify();
     }
 
     public void testGetPluginAndModules() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertNotNull(plugin);
         assertEquals("Test Plugin", plugin.getName());
 
         final ModuleDescriptor<?> bear = plugin.getModuleDescriptor("bear");
         assertEquals(bear, manager.getPluginModule("test.atlassian.plugin:bear"));
     }
 
     public void testGetModuleByModuleClassOneFound() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         final List<MockAnimalModuleDescriptor> animalDescriptors = manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class);
         assertNotNull(animalDescriptors);
         assertEquals(1, animalDescriptors.size());
         final ModuleDescriptor<MockAnimal> moduleDescriptor = animalDescriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         final List<MockMineralModuleDescriptor> mineralDescriptors = manager.getEnabledModuleDescriptorsByClass(MockMineralModuleDescriptor.class);
         assertNotNull(mineralDescriptors);
         assertEquals(1, mineralDescriptors.size());
         final ModuleDescriptor<MockMineral> mineralDescriptor = mineralDescriptors.iterator().next();
         assertEquals("Bar", mineralDescriptor.getName());
     }
 
     public void testGetModuleByModuleClassAndDescriptor() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         final Collection<MockBear> bearModules = manager.getEnabledModulesByClassAndDescriptor(
             new Class[] { MockAnimalModuleDescriptor.class, MockMineralModuleDescriptor.class }, MockBear.class);
         assertNotNull(bearModules);
         assertEquals(1, bearModules.size());
         assertTrue(bearModules.iterator().next() instanceof MockBear);
 
         final Collection<MockBear> noModules = manager.getEnabledModulesByClassAndDescriptor(new Class[] {}, MockBear.class);
         assertNotNull(noModules);
         assertEquals(0, noModules.size());
 
         final Collection<MockThing> mockThings = manager.getEnabledModulesByClassAndDescriptor(
             new Class[] { MockAnimalModuleDescriptor.class, MockMineralModuleDescriptor.class }, MockThing.class);
         assertNotNull(mockThings);
         assertEquals(2, mockThings.size());
         assertTrue(mockThings.iterator().next() instanceof MockThing);
         assertTrue(mockThings.iterator().next() instanceof MockThing);
 
         final Collection<MockThing> mockThingsFromMineral = manager.getEnabledModulesByClassAndDescriptor(
             new Class[] { MockMineralModuleDescriptor.class }, MockThing.class);
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
 
         class MockSilver implements MockMineral
         {
             public int getWeight()
             {
                 return 3;
             }
         }
 
         final Collection<MockSilver> descriptors = manager.getEnabledModulesByClass(MockSilver.class);
         assertNotNull(descriptors);
         assertTrue(descriptors.isEmpty());
     }
 
     public void testGetModuleDescriptorsByType() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         Collection<ModuleDescriptor<MockThing>> descriptors = manager.getEnabledModuleDescriptorsByType("animal");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         ModuleDescriptor<MockThing> moduleDescriptor = descriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         descriptors = manager.getEnabledModuleDescriptorsByType("mineral");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         moduleDescriptor = descriptors.iterator().next();
         assertEquals("Bar", moduleDescriptor.getName());
 
         try
         {
             manager.getEnabledModuleDescriptorsByType("foobar");
         }
         catch (final IllegalArgumentException e)
         {
             fail("Shouldn't have thrown exception.");
         }
     }
 
     public void testRetrievingDynamicResources() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         final InputStream is = manager.getPluginResourceAsStream("test.atlassian.plugin.classloaded", "atlassian-plugin.xml");
         assertNotNull(is);
         IOUtils.closeQuietly(is);
     }
 
     public void testGetDynamicPluginClass() throws IOException, PluginParseException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         try
         {
             manager.getDynamicPluginClass("com.atlassian.plugin.mock.MockPooh");
         }
         catch (final ClassNotFoundException e)
         {
             fail(e.getMessage());
         }
     }
 
     public void testFindingNewPlugins() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         //delete paddington for the timebeing
         final File paddington = new File(pluginsTestDir, PADDINGTON_JAR);
         paddington.delete();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
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
 
     public void testFindingNewPluginsNotLoadingRestartRequiredDescriptors() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
 
         pluginLoaders.add(new DynamicSinglePluginLoader("test.atlassian.plugin", "test-requiresRestart-plugin.xml"));
 
         manager.scanForNewPlugins();
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertNotNull(manager.getPlugin("test.atlassian.plugin"));
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertTrue(plugin instanceof UnloadablePlugin);
 
         assertEquals(PluginRestartState.INSTALL, manager.getPluginRestartState("test.atlassian.plugin"));
     }
 
     /**
      * Tests upgrade of plugin where the old version didn't have any restart required module descriptors, but the new one does
      */
     public void testFindingUpgradePluginsNotLoadingRestartRequiredDescriptors() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertEquals(0, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
 
         pluginLoaders.add(new DynamicSinglePluginLoader("test.atlassian.plugin.classloaded2", "test-requiresRestartWithUpgrade-plugin.xml"));
 
         manager.scanForNewPlugins();
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertEquals(PluginRestartState.UPGRADE, manager.getPluginRestartState("test.atlassian.plugin.classloaded2"));
         assertEquals(0, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
     }
 
     public void testInstallPluginThatRequiresRestart() throws PluginParseException, IOException, InterruptedException
     {
         createFillAndCleanTempPluginDirectory();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
 
         new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>1.0</version>",
                     "    </plugin-info>",
                     "    <requiresRestart key='foo' />",
                     "</atlassian-plugin>")
                 .build(pluginsTestDir);
         manager.scanForNewPlugins();
 
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertFalse(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(0, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.INSTALL, manager.getPluginRestartState("test.restartrequired"));
 
         manager.shutdown();
         manager.init();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
     }
 
     public void testUpgradePluginThatRequiresRestart() throws PluginParseException, IOException, InterruptedException
     {
         createFillAndCleanTempPluginDirectory();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         File origFile = new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>1.0</version>",
                     "    </plugin-info>",
                     "    <requiresRestart key='foo' />",
                     "</atlassian-plugin>")
                 .build(pluginsTestDir);
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
 
         // Some filesystems only record last modified in seconds
         Thread.sleep(1000);
         File updateFile  = new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>2.0</version>",
                     "    </plugin-info>",
                     "    <requiresRestart key='foo' />",
                     "    <requiresRestart key='bar' />",
                     "</atlassian-plugin>")
                 .build();
 
         origFile.delete();
         FileUtils.moveFile(updateFile, origFile);
 
 
         manager.scanForNewPlugins();
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(PluginRestartState.UPGRADE, manager.getPluginRestartState("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
 
         manager.shutdown();
         manager.init();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(2, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
     }
 
     public void testUpgradePluginThatPreviouslyRequiredRestart() throws PluginParseException, IOException, InterruptedException
     {
         createFillAndCleanTempPluginDirectory();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         File origFile = new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>1.0</version>",
                     "    </plugin-info>",
                     "    <requiresRestart key='foo' />",
                     "</atlassian-plugin>")
                 .build(pluginsTestDir);
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
 
         // Some filesystems only record last modified in seconds
         Thread.sleep(1000);
         File updateFile = new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>2.0</version>",
                     "    </plugin-info>",
                     "</atlassian-plugin>")
                 .build(pluginsTestDir);
 
         origFile.delete();
         FileUtils.moveFile(updateFile, origFile);
 
         manager.scanForNewPlugins();
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(PluginRestartState.UPGRADE, manager.getPluginRestartState("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
 
         manager.shutdown();
         manager.init();
 
         assertEquals(0, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
     }
 
     public void testRemovePluginThatRequiresRestart() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         File pluginFile = new PluginJarBuilder()
                 .addFormattedResource("atlassian-plugin.xml",
                     "<atlassian-plugin name='Test 2' key='test.restartrequired' pluginsVersion='1'>",
                     "    <plugin-info>",
                     "        <version>1.0</version>",
                     "    </plugin-info>",
                     "    <requiresRestart key='foo' />",
                     "</atlassian-plugin>")
                 .build(pluginsTestDir);
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.restartrequired"));
 
         manager.uninstall(manager.getPlugin("test.restartrequired"));
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.restartrequired"));
         assertTrue(manager.isPluginEnabled("test.restartrequired"));
         assertEquals(PluginRestartState.REMOVE, manager.getPluginRestartState("test.restartrequired"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
 
         manager.shutdown();
         manager.init();
         
         assertFalse(pluginFile.exists());
         assertEquals(0, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(2, manager.getPlugins().size());
     }
 
     public void testCannotRemovePluginFromStaticLoader() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
         moduleDescriptorFactory.addModuleDescriptor("requiresRestart", RequiresRestartModuleDescriptor.class);
 
         directoryPluginLoader = new DirectoryPluginLoader(
                 pluginsTestDir,
                 Arrays.asList(
                         new LegacyDynamicPluginFactory(PluginAccessor.Descriptor.FILENAME),
                         new XmlDynamicPluginFactory("key")),
                 pluginEventManager);
         pluginLoaders.add(directoryPluginLoader);
         pluginLoaders.add(new SinglePluginLoader("test-requiresRestart-plugin.xml"));
 
         manager.init();
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin"));
         assertTrue(manager.isPluginEnabled("test.atlassian.plugin"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.atlassian.plugin"));
 
         try
         {
             manager.uninstall(manager.getPlugin("test.atlassian.plugin"));
             fail();
         }
         catch (PluginException ex)
         {
             // test passed
         }
 
         assertEquals(3, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin"));
         assertTrue(manager.isPluginEnabled("test.atlassian.plugin"));
         assertEquals(PluginRestartState.NONE, manager.getPluginRestartState("test.atlassian.plugin"));
         assertEquals(1, manager.getEnabledModuleDescriptorsByClass(RequiresRestartModuleDescriptor.class).size());
     }
 
 
     private DefaultPluginManager makeClassLoadingPluginManager() throws PluginParseException
     {
         directoryPluginLoader = new DirectoryPluginLoader(pluginsTestDir, Arrays.asList(new LegacyDynamicPluginFactory(
             PluginAccessor.Descriptor.FILENAME), new XmlDynamicPluginFactory("key")), pluginEventManager);
         pluginLoaders.add(directoryPluginLoader);
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         manager.init();
         return manager;
     }
 
     public void testRemovingPlugins() throws PluginException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
         final MockAnimalModuleDescriptor moduleDescriptor = (MockAnimalModuleDescriptor) manager.getPluginModule("test.atlassian.plugin.classloaded:paddington");
         assertFalse(moduleDescriptor.disabled);
         final PassListener disabledListener = new PassListener(PluginDisabledEvent.class);
         pluginEventManager.register(disabledListener);
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin.classloaded");
         manager.uninstall(plugin);
         assertTrue("Module must have had disable() called before being removed", moduleDescriptor.disabled);
 
         // uninstalling a plugin should remove it's state completely from the state store - PLUG-13
         assertTrue(pluginStateStore.load().getPluginStateMap(plugin).isEmpty());
 
         assertEquals(1, manager.getPlugins().size());
         // plugin is no longer available though the plugin manager
         assertNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertEquals(1, pluginsTestDir.listFiles().length);
         disabledListener.assertCalled();
     }
 
     public void testNonRemovablePlugins() throws PluginParseException
     {
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         manager.init();
 
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertFalse(plugin.isUninstallable());
         assertNotNull(plugin.getResourceAsStream("test-atlassian-plugin.xml"));
 
         try
         {
             manager.uninstall(plugin);
             fail("Where was the exception?");
         }
         catch (final PluginException p)
         {}
     }
 
     public void testNonDeletablePlugins() throws PluginException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
 
         // Set plugin file can't be deleted.
         final DynamicPlugin pluginToRemove = (DynamicPlugin) manager.getPlugin("test.atlassian.plugin.classloaded");
         pluginToRemove.setDeletable(false);
 
         // Disable plugin module before uninstall
         final MockAnimalModuleDescriptor moduleDescriptor = (MockAnimalModuleDescriptor) manager.getPluginModule("test.atlassian.plugin.classloaded:paddington");
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
 
         final Plugin p1 = createPluginWithVersion("1.1");
         final Plugin p2 = createPluginWithVersion("1.0");
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
 
         final Plugin p1 = createPluginWithVersion("1.0");
         final Plugin p2 = createPluginWithVersion("1.1");
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
 
         final Plugin p1 = createPluginWithVersion("1.0");
         final Plugin p2 = createPluginWithVersion("1.0");
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
 
         final Plugin p1 = createPluginWithVersion("1.0");
         final Plugin p2 = createPluginWithVersion("#$%");
        assertEquals(1, p1.compareTo(p2));
 
         p1.getPluginInformation().setVersion("#$%");
         p2.getPluginInformation().setVersion("1.0");
         assertEquals(-1, p1.compareTo(p2));
 
     }
 
     public void testComparePluginBadPlugin()
     {
 
         final Plugin p1 = createPluginWithVersion("1.0");
         final Plugin p2 = createPluginWithVersion("1.0");
 
         // Compare against something with a different key
         p2.setKey("bad.key");
         assertTrue(p1.compareTo(p2) != 0);
 
     }
 
     public void testInvalidationOfDynamicResourceCache() throws IOException, PluginException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
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
         final DefaultPluginManager manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory,
             new DefaultPluginEventManager());
         final Mock mockLoader = new Mock(DynamicPluginLoader.class);
         @SuppressWarnings("unchecked")
         final PluginLoader loader = (PluginLoader) mockLoader.proxy();
         pluginLoaders.add(loader);
 
         final Mock mockPluginJar = new Mock(PluginArtifact.class);
         final PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
         mockLoader.expectAndReturn("canLoad", C.args(C.eq(pluginArtifact)), "foo");
 
         final String key = manager.validatePlugin(pluginArtifact);
         assertEquals("foo", key);
         mockLoader.verify();
     }
 
     public void testValidatePluginWithNoDynamicLoaders() throws PluginParseException
     {
         final DefaultPluginManager manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory,
             new DefaultPluginEventManager());
         final Mock mockLoader = new Mock(PluginLoader.class);
         @SuppressWarnings("unchecked")
         final PluginLoader loader = (PluginLoader) mockLoader.proxy();
         pluginLoaders.add(loader);
 
         final Mock mockPluginJar = new Mock(PluginArtifact.class);
         final PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
         try
         {
             manager.validatePlugin(pluginArtifact);
             fail("Should have thrown exception");
         }
         catch (final IllegalStateException ex)
         {
             // test passed
         }
     }
 
     public void testInvalidationOfDynamicClassCache() throws IOException, PluginException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
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
         final Mock mockPluginStateStore = new Mock(PluginPersistentStateStore.class);
         final Mock mockModuleDescriptorFactory = new Mock(ModuleDescriptorFactory.class);
         final Mock mockPluginLoader = new Mock(DynamicPluginLoader.class);
         final Mock mockDescriptorParserFactory = new Mock(DescriptorParserFactory.class);
         final Mock mockDescriptorParser = new Mock(DescriptorParser.class);
         final Mock mockPluginJar = new Mock(PluginArtifact.class);
         final Mock mockRepository = new Mock(PluginInstaller.class);
         final Mock mockPlugin = new Mock(Plugin.class);
 
         final ModuleDescriptorFactory moduleDescriptorFactory = (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy();
 
         final DefaultPluginManager pluginManager = new DefaultPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
             Collections.<PluginLoader> singletonList((PluginLoader) mockPluginLoader.proxy()), moduleDescriptorFactory, pluginEventManager);
 
         final Plugin plugin = (Plugin) mockPlugin.proxy();
         final PluginArtifact pluginArtifact = (PluginArtifact) mockPluginJar.proxy();
 
         mockPluginStateStore.expectAndReturn("load", new DefaultPluginPersistentState());
         mockPluginStateStore.expectAndReturn("load", new DefaultPluginPersistentState());
         mockPluginStateStore.expectAndReturn("load", new DefaultPluginPersistentState());
         mockPluginStateStore.expect("save", C.ANY_ARGS);
         mockDescriptorParser.matchAndReturn("getKey", "test");
         mockRepository.expect("installPlugin", C.args(C.eq("test"), C.eq(pluginArtifact)));
         mockPluginLoader.expectAndReturn("loadAllPlugins", C.eq(moduleDescriptorFactory), Collections.emptyList());
         mockPluginLoader.expectAndReturn("supportsAddition", true);
         mockPluginLoader.expectAndReturn("addFoundPlugins", moduleDescriptorFactory, Collections.singletonList(plugin));
         mockPluginLoader.expectAndReturn("canLoad", C.args(C.eq(pluginArtifact)), "test");
         mockPlugin.matchAndReturn("getKey", "test");
         mockPlugin.matchAndReturn("hashCode", mockPlugin.hashCode());
         mockPlugin.expectAndReturn("getModuleDescriptors", new ArrayList<Object>());
         mockPlugin.expectAndReturn("getModuleDescriptors", new ArrayList<Object>());
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
         mockPlugin.expect("install");
         mockPlugin.expect("enable");
         mockPlugin.expectAndReturn("isEnabledByDefault", true);
         mockPlugin.matchAndReturn("isEnabled", true);
         mockPlugin.matchAndReturn("getPluginState", PluginState.ENABLED);
 
         pluginManager.setPluginInstaller((PluginInstaller) mockRepository.proxy());
         pluginManager.init();
         final PassListener enabledListener = new PassListener(PluginEnabledEvent.class);
         pluginEventManager.register(enabledListener);
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
         enabledListener.assertCalled();
     }
 
     private <T> void checkResources(final PluginAccessor manager, final boolean canGetGlobal, final boolean canGetModule) throws IOException
     {
         InputStream is = manager.getDynamicResourceAsStream("icon.gif");
         assertEquals(canGetGlobal, is != null);
         IOUtils.closeQuietly(is);
         is = manager.getDynamicResourceAsStream("bear/paddington.vm");
         assertEquals(canGetModule, is != null);
         IOUtils.closeQuietly(is);
     }
 
     private <T> void checkClasses(final PluginAccessor manager, final boolean canGet)
     {
         try
         {
             manager.getDynamicPluginClass("com.atlassian.plugin.mock.MockPaddington");
             if (!canGet)
             {
                 fail("Class in plugin was successfully loaded");
             }
         }
         catch (final ClassNotFoundException e)
         {
             if (canGet)
             {
                 fail(e.getMessage());
             }
         }
     }
 
     public void testAddPluginsThatThrowExceptionOnEnabled() throws Exception
     {
         final Plugin plugin = new CannotEnablePlugin();
 
         manager.addPlugins(null, Arrays.asList(plugin));
 
         assertFalse(plugin.getPluginState() == PluginState.ENABLED);
     }
 
     public void testUninstallPluginClearsState() throws IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
 
         checkClasses(manager, true);
         final Plugin plugin = manager.getPlugin("test.atlassian.plugin.classloaded");
 
         final ModuleDescriptor<?> module = plugin.getModuleDescriptor("paddington");
         assertTrue(manager.isPluginModuleEnabled(module.getCompleteKey()));
         manager.disablePluginModule(module.getCompleteKey());
         assertFalse(manager.isPluginModuleEnabled(module.getCompleteKey()));
         manager.uninstall(plugin);
         assertFalse(manager.isPluginModuleEnabled(module.getCompleteKey()));
         assertTrue(pluginStateStore.load().getPluginStateMap(plugin).isEmpty());
     }
 
     public void testCannotInitTwice() throws PluginParseException
     {
         manager.init();
         try
         {
             manager.init();
             fail("IllegalStateException expected");
         }
         catch (final IllegalStateException expected)
         {}
     }
 
     public void testCannotShutdownTwice() throws PluginParseException
     {
         manager.init();
         manager.shutdown();
         try
         {
             manager.shutdown();
             fail("IllegalStateException expected");
         }
         catch (final IllegalStateException expected)
         {}
     }
 
     public Plugin createPluginWithVersion(final String version)
     {
         final Plugin p = new StaticPlugin();
         p.setKey("test.default.plugin");
         final PluginInformation pInfo = p.getPluginInformation();
         pInfo.setVersion(version);
         return p;
     }
 
     /**
      * Dummy plugin loader that reports that removal is supported and returns plugins that report that they can
      * be uninstalled.
      */
     private static class SinglePluginLoaderWithRemoval extends SinglePluginLoader
     {
         public SinglePluginLoaderWithRemoval(final String resource)
         {
             super(resource);
         }
 
         public boolean supportsRemoval()
         {
 
             return true;
         }
 
         public void removePlugin(final Plugin plugin) throws PluginException
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
 
     class NothingModuleDescriptor extends MockUnusedModuleDescriptor
     {}
 
     @RequiresRestart
     public static class RequiresRestartModuleDescriptor extends MockUnusedModuleDescriptor
     {}
 
     private class MultiplePluginLoader implements PluginLoader
     {
         private final String[] descriptorPaths;
 
         public MultiplePluginLoader(final String... descriptorPaths)
         {
             this.descriptorPaths = descriptorPaths;
         }
 
         public Collection<Plugin> loadAllPlugins(final ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
         {
             final List<Plugin> result = new ArrayList<Plugin>(descriptorPaths.length);
             for (final String path : descriptorPaths)
             {
                 final SinglePluginLoader loader = new SinglePluginLoader(path);
                 result.addAll(loader.loadAllPlugins(moduleDescriptorFactory));
             }
             return result;
         }
 
         public boolean supportsAddition()
         {
             return false;
         }
 
         public boolean supportsRemoval()
         {
             return false;
         }
 
         public Collection<Plugin> addFoundPlugins(final ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
         {
             throw new UnsupportedOperationException("This PluginLoader does not support addition.");
         }
 
         public void removePlugin(final Plugin plugin) throws PluginException
         {
             throw new UnsupportedOperationException("This PluginLoader does not support addition.");
         }
     }
 
     private static class DynamicSinglePluginLoader extends SinglePluginLoader implements DynamicPluginLoader
     {
         private final String key;
 
         public DynamicSinglePluginLoader(final String key, final String resource)
         {
             super(resource);
             this.key = key;
         }
 
         public String canLoad(final PluginArtifact pluginArtifact) throws PluginParseException
         {
             return key;
         }
 
         public boolean supportsAddition()
         {
             return true;
         }
 
         @Override
         public Collection<Plugin> addFoundPlugins(final ModuleDescriptorFactory moduleDescriptorFactory)
         {
             return super.loadAllPlugins(moduleDescriptorFactory);
         }
     }
 
     private static class CannotEnablePlugin extends StaticPlugin
     {
         public CannotEnablePlugin()
         {
             setKey("foo");
         }
 
         @Override
         public void enable()
         {
             throw new RuntimeException("boo");
         }
 
         public void disabled()
         {}
     }
 }
