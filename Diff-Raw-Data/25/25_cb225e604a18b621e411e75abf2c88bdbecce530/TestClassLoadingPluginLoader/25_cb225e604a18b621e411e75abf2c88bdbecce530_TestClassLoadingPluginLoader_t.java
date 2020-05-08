 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockBear;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class TestClassLoadingPluginLoader extends AbstractTestClassLoader
 {
     public void testAtlassianPlugin() throws Exception
     {
         // hacky way of getting to the directoryPluginLoaderFiles classloading
         File pluginsDirectory = getPluginsDirectory();
 
         ClassLoadingPluginLoader loader = new ClassLoadingPluginLoader(pluginsDirectory);
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         Collection plugins = loader.getPlugins(moduleDescriptorFactory);
 
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
                 MockBear paddington = (MockBear)paddingtonDescriptor.getModule();
                 assertEquals("com.atlassian.plugin.mock.MockPaddington", paddington.getClass().getName());
             }
             else if (plugin.getName().equals("Test Class Loaded Plugin 2")) // asserts for second plugin
             {
                 assertEquals("Test Class Loaded Plugin 2", plugin.getName());
                 assertEquals("test.atlassian.plugin.classloaded2", plugin.getKey());
                 assertEquals(1, plugin.getModuleDescriptors().size());
                 MockAnimalModuleDescriptor poohDescriptor = (MockAnimalModuleDescriptor) plugin.getModuleDescriptor("pooh");
                 assertEquals("Pooh Bear", poohDescriptor.getName());
                 MockBear pooh = (MockBear)poohDescriptor.getModule();
                 assertEquals("com.atlassian.plugin.mock.MockPooh", pooh.getClass().getName());
             }
             else
             {
                 fail("What plugin name?!");
             }
         }
     }
 
 }
