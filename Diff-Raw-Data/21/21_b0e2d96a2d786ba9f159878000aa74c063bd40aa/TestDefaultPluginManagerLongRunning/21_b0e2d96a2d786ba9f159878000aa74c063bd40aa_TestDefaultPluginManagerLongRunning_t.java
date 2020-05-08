 package com.atlassian.plugin;
 
 import com.atlassian.plugin.descriptors.MockUnusedModuleDescriptor;
 import com.atlassian.plugin.descriptors.RequiresRestart;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.factories.XmlDynamicPluginFactory;
 import com.atlassian.plugin.impl.StaticPlugin;
 import com.atlassian.plugin.loaders.DirectoryPluginLoader;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.repositories.FilePluginInstaller;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import com.atlassian.plugin.test.PluginJarBuilder;
 import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
 
 import org.apache.commons.io.FileUtils;
 
 import com.mockobjects.dynamic.C;
 import com.mockobjects.dynamic.Mock;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Testing {@link DefaultPluginManager}
  */
 public class TestDefaultPluginManagerLongRunning extends AbstractTestClassLoader
 {
     /**
      * the object being tested
      */
     private DefaultPluginManager manager;
 
     private PluginStateStore pluginStateStore;
     private List<PluginLoader> pluginLoaders;
     private DefaultModuleDescriptorFactory moduleDescriptorFactory; // we should be able to use the interface here?
 
     private DirectoryPluginLoader directoryPluginLoader;
     private PluginEventManager pluginEventManager;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         pluginEventManager = new DefaultPluginEventManager();
 
         pluginStateStore = new MemoryPluginStateStore();
         pluginLoaders = new ArrayList<PluginLoader>();
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
 
         manager = new DefaultPluginManager(pluginStateStore, pluginLoaders, moduleDescriptorFactory, new DefaultPluginEventManager());
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
             directoryPluginLoader.shutDown();
             directoryPluginLoader = null;
         }
 
         super.tearDown();
     }
 
     public void testEnableFailed() throws PluginParseException
     {
         final Mock mockPluginLoader = new Mock(PluginLoader.class);
         final Plugin plugin = new StaticPlugin()
         {
             @Override
             public void setEnabled(final boolean enabled)
             {
             // do nothing
             }
         };
         plugin.setKey("foo");
         plugin.setEnabledByDefault(false);
         plugin.setPluginInformation(new PluginInformation());
 
         mockPluginLoader.expectAndReturn("loadAllPlugins", C.ANY_ARGS, Collections.singletonList(plugin));
 
         final PluginLoader proxy = (PluginLoader) mockPluginLoader.proxy();
         pluginLoaders.add(proxy);
         manager.init();
 
         assertEquals(1, manager.getPlugins().size());
         assertEquals(0, manager.getEnabledPlugins().size());
         assertFalse(plugin.getPluginState() == PluginState.ENABLED);
         manager.enablePlugin("foo");
         assertEquals(1, manager.getPlugins().size());
         assertEquals(0, manager.getEnabledPlugins().size());
         assertFalse(plugin.getPluginState() == PluginState.ENABLED);
     }
 
     private DefaultPluginManager makeClassLoadingPluginManager() throws PluginParseException
     {
         directoryPluginLoader = new DirectoryPluginLoader(pluginsTestDir, Arrays.asList(new LegacyDynamicPluginFactory(
             PluginAccessor.Descriptor.FILENAME), new XmlDynamicPluginFactory()), pluginEventManager);
         pluginLoaders.add(directoryPluginLoader);
 
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         manager.init();
         return manager;
     }
 
     public void testInstallPluginTwiceWithSameName() throws Exception
     {
         createFillAndCleanTempPluginDirectory();
 
         FileUtils.cleanDirectory(pluginsTestDir);
         final File plugin = File.createTempFile("plugin", ".jar");
        plugin.delete();
        File jar = new PluginJarBuilder("plugin")
                .addPluginInformation("some.key", "My name", "1.0", 1)
                .addResource("foo.txt", "foo")
                .addJava("my.MyClass",
                    "package my; public class MyClass {}")
                .build();
        FileUtils.moveFile(jar, plugin);
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         manager.setPluginInstaller(new FilePluginInstaller(pluginsTestDir));
 
         final String pluginKey = manager.installPlugin(new JarPluginArtifact(plugin));
 
         assertTrue(new File(pluginsTestDir, plugin.getName()).exists());
 
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
         catch (final ClassNotFoundException e)
         {
             // expected
         }
 
         // sleep to ensure the new plugin is picked up
         Thread.sleep(1000);
 
        File jartmp = new PluginJarBuilder("plugin")
                .addPluginInformation("some.key", "My name", "1.0", 1)
                .addResource("bar.txt", "bar")
                .addJava("my.MyNewClass",
                        "package my; public class MyNewClass {}")
                .build();
        plugin.delete();
        FileUtils.moveFile(jartmp, plugin);
 
         // reinstall the plugin
         final String pluginKey2 = manager.installPlugin(new JarPluginArtifact(plugin));
 
         assertTrue(new File(pluginsTestDir, plugin.getName()).exists());
 
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
         catch (final ClassNotFoundException e)
         {
             // expected
         }
     }
 
     public void testInstallPluginTwiceWithDifferentName() throws Exception
     {
         createFillAndCleanTempPluginDirectory();
 
         FileUtils.cleanDirectory(pluginsTestDir);
         final File plugin1 = new PluginJarBuilder("plugin").addPluginInformation("some.key", "My name", "1.0", 1).addResource("foo.txt", "foo").addJava(
             "my.MyClass", "package my; public class MyClass {}").build();
 
         final DefaultPluginManager manager = makeClassLoadingPluginManager();
         manager.setPluginInstaller(new FilePluginInstaller(pluginsTestDir));
 
         final String pluginKey = manager.installPlugin(new JarPluginArtifact(plugin1));
 
         assertTrue(new File(pluginsTestDir, plugin1.getName()).exists());
 
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
         catch (final ClassNotFoundException e)
         {
             // expected
         }
 
         // sleep to ensure the new plugin is picked up
         Thread.sleep(1000);
 
         final File plugin2 = new PluginJarBuilder("plugin").addPluginInformation("some.key", "My name", "1.0", 1).addResource("bar.txt", "bar").addJava(
             "my.MyNewClass", "package my; public class MyNewClass {}").build();
 
         // reinstall the plugin
         final String pluginKey2 = manager.installPlugin(new JarPluginArtifact(plugin2));
 
         assertFalse(new File(pluginsTestDir, plugin1.getName()).exists());
         assertTrue(new File(pluginsTestDir, plugin2.getName()).exists());
 
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
         catch (final ClassNotFoundException e)
         {
             // expected
         }
     }
 
     public void testAddPluginsWithDependencyIssuesNoResolution() throws Exception
     {
         final Plugin servicePlugin = new EnableInPassPlugin("service.plugin", 4);
         final Plugin clientPlugin = new EnableInPassPlugin("client.plugin", 1);
 
         manager.addPlugins(null, Arrays.asList(servicePlugin, clientPlugin));
 
         assertTrue(clientPlugin.getPluginState() == PluginState.ENABLED);
         assertFalse(servicePlugin.getPluginState() == PluginState.ENABLED);
     }
 
     public Plugin createPluginWithVersion(final String version)
     {
         final Plugin p = new StaticPlugin();
         p.setKey("test.default.plugin");
         final PluginInformation pInfo = p.getPluginInformation();
         pInfo.setVersion(version);
         return p;
     }
 
     private static class EnableInPassPlugin extends StaticPlugin
     {
         private int pass;
 
         public EnableInPassPlugin(final String key, final int pass)
         {
             this.pass = pass;
             setKey(key);
         }
 
         @Override
         public void setEnabled(final boolean val)
         {
             super.setEnabled((--pass) <= 0);
         }
     }
 
     class NothingModuleDescriptor extends MockUnusedModuleDescriptor
     {}
 
     @RequiresRestart
     public static class RequiresRestartModuleDescriptor extends MockUnusedModuleDescriptor
     {}
 }
