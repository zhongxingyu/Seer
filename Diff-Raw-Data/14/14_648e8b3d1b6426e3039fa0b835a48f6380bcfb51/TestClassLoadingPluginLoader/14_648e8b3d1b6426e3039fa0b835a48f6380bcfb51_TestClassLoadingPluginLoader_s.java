 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockBear;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import com.atlassian.plugin.util.FileUtils;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.net.URL;
 
 public class TestClassLoadingPluginLoader extends AbstractTestClassLoader
 {
     private static final Log log = LogFactory.getLog(TestClassLoadingPluginLoader.class);
     ClassLoadingPluginLoader loader;
     DefaultModuleDescriptorFactory moduleDescriptorFactory;
 
    public static final String BAD_PLUGIN_JAR = "plugins/crap-plugin.jar";
 
     public void setUp() throws Exception
     {
         super.setUp();
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
 
         createFillAndCleanTempPluginDirectory();
     }
 
     public void tearDown() throws Exception
     {
         if (loader != null)
         {
             loader.shutDown();
         }
 
         assertTrue(FileUtils.deleteDir(pluginsTestDir));
         super.tearDown();
     }
 
     public void testAtlassianPlugin() throws Exception
     {
         addTestModuleDecriptors();
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         Collection plugins = loader.loadAllPlugins(moduleDescriptorFactory);
 
         assertEquals(2, plugins.size());
 
         for (Iterator iterator = plugins.iterator(); iterator.hasNext();)
         {
             Plugin plugin = (Plugin) iterator.next();
 
             assertTrue(plugin.getName().equals("Test Class Loaded Plugin") || plugin.getName().equals("Test Class Loaded Plugin 2"));
 
             if (plugin.getName().equals("Test Class Loaded Plugin")) // asserts for first plugin
             {
                 assertEquals("Test Class Loaded Plugin", plugin.getName());
                 assertEquals("test.atlassian.plugin.classloaded", plugin.getKey());
                 assertEquals(1, plugin.getModuleDescriptors().size());
                 MockAnimalModuleDescriptor paddingtonDescriptor = (MockAnimalModuleDescriptor) plugin.getModuleDescriptor("paddington");
                 assertEquals("Paddington Bear", paddingtonDescriptor.getName());
                 MockBear paddington = (MockBear) paddingtonDescriptor.getModule();
                 assertEquals("com.atlassian.plugin.mock.MockPaddington", paddington.getClass().getName());
             }
             else if (plugin.getName().equals("Test Class Loaded Plugin 2")) // asserts for second plugin
             {
                 assertEquals("Test Class Loaded Plugin 2", plugin.getName());
                 assertEquals("test.atlassian.plugin.classloaded2", plugin.getKey());
                 assertEquals(1, plugin.getModuleDescriptors().size());
                 MockAnimalModuleDescriptor poohDescriptor = (MockAnimalModuleDescriptor) plugin.getModuleDescriptor("pooh");
                 assertEquals("Pooh Bear", poohDescriptor.getName());
                 MockBear pooh = (MockBear) poohDescriptor.getModule();
                 assertEquals("com.atlassian.plugin.mock.MockPooh", pooh.getClass().getName());
             }
             else
             {
                 fail("What plugin name?!");
             }
         }
     }
 
     // Tests that an UnloadablePlugin is returned when there's a missing class dependency
     public void testAtlassianPluginWithMissingClass() throws Exception
     {
         addTestModuleDecriptors();
 
         URL url = ClassLoaderUtils.getResource(BAD_PLUGIN_JAR, TestClassPathPluginLoader.class);
         String path = url.toExternalForm();
         path = path.replace('/', File.separatorChar);
         path = path.substring(5);
         File pluginFile = new File(path);
         loader = new ClassLoadingPluginLoader(pluginFile.getParentFile());
 
         Collection plugins = loader.loadAllPlugins(moduleDescriptorFactory);
 
         assertEquals(1, plugins.size());
 
         Object[] pluginsArray = plugins.toArray();
 
         Plugin plugin = (Plugin) pluginsArray[0];
 
         assertEquals(UnloadablePlugin.class, plugin.getClass());
 
         // grab module descriptors
         Collection moduleDescriptors = plugin.getModuleDescriptors();
         Object[] descriptorsArray = moduleDescriptors.toArray();
 
         assertEquals(1, moduleDescriptors.size());
 
         ModuleDescriptor descriptor = (ModuleDescriptor) descriptorsArray[0];
 
         assertEquals(UnloadableModuleDescriptor.class, descriptor.getClass());
     }
 
     private void addTestModuleDecriptors()
     {
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
     }
 
     public void testSupportsAdditionAndRemoval()
     {
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         assertTrue(loader.supportsAddition());
         assertTrue(loader.supportsRemoval());
     }
 
     public void testNoFoundPlugins() throws PluginParseException
     {
         addTestModuleDecriptors();
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         Collection col = loader.addFoundPlugins(moduleDescriptorFactory);
         assertFalse(col.isEmpty());
 
         col = loader.addFoundPlugins(moduleDescriptorFactory);
         assertTrue(col.isEmpty());
     }
 
     public void testFoundPlugin() throws PluginParseException, IOException
     {
         //delete paddington for the timebeing
         File paddington = new File(pluginsTestDir + File.separator + PADDINGTON_JAR);
         paddington.delete();
 
         addTestModuleDecriptors();
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         loader.loadAllPlugins(moduleDescriptorFactory);
 
         //restore paddington to test plugins dir
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
 
         Collection col = loader.addFoundPlugins(moduleDescriptorFactory);
         assertEquals(1, col.size());
         // next time we shouldn't find any new plugins
         col = loader.addFoundPlugins(moduleDescriptorFactory);
         assertEquals(0, col.size());
     }
 
     public void testRemovePlugin() throws PluginException, IOException
     {
         addTestModuleDecriptors();
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         Collection plugins = loader.loadAllPlugins(moduleDescriptorFactory);
 
         //duplicate the paddington plugin before removing the original
         //the duplicate will be used to restore the deleted original after the test
         File paddington = new File(pluginsTestDir + File.separator + PADDINGTON_JAR);
 
         Iterator iter = plugins.iterator();
 
         Plugin paddingtonPlugin = null;
 
         while (iter.hasNext())
         {
             Plugin plugin = (Plugin) iter.next();
 
             if (plugin.getName().equals("Test Class Loaded Plugin"))
             {
                 paddingtonPlugin = plugin;
                 break;
             }
         }
 
         if (paddingtonPlugin == null)
             fail("Can't find test plugin 1 (paddington)");
 
         loader.removePlugin(paddingtonPlugin);
     }
 
     public void testInvalidPluginHandled() throws IOException
     {
         String badJarTitle = "badplugin.jar";
         File atlassianPluginXML = new File(pluginsTestDir, "atlassian-plugin.xml");
 
         FileWriter writer = new FileWriter(atlassianPluginXML);
         writer.write("this is invalid XML - the classloadingpluginloader should throw PluginParseException");
         writer.close();
 
         //now jar up the evilplugin
 
         createJarFile("evilplugin.jar", atlassianPluginXML.getName(), pluginsTestDir.getAbsolutePath());
 
         loader = new ClassLoadingPluginLoader(pluginsTestDir);
         try
         {
             Collection plugins = loader.loadAllPlugins(moduleDescriptorFactory);
             fail("The ClassLoadingPluginLoader did not report an invalid atlassian-plugin.xml. This needs to happen as an invalid plugin can harm the state of the plugin manager.");
         }
         catch (PluginParseException e)
         {
         }
     }
 
     private void createJarFile(String jarname, String jarEntry, String saveDir)
             throws IOException
     {
         OutputStream os = new FileOutputStream(saveDir + File.separator + jarname);
         JarOutputStream plugin1 = new JarOutputStream(os);
         JarEntry jarEntry1 = new JarEntry(jarEntry);
 
         plugin1.putNextEntry(jarEntry1);
         plugin1.closeEntry();
         plugin1.flush();
         plugin1.close();
     }
 
 }
