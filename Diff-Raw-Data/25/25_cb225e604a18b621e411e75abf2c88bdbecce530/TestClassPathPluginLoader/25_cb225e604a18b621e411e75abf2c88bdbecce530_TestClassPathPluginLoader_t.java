 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.descriptors.ResourcedModuleDescriptor;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import junit.framework.TestCase;
 
 import java.util.Collection;
 
 public class TestClassPathPluginLoader extends TestCase
 {
     public void testAtlassianPlugin() throws Exception
     {
         ClassPathPluginLoader loader = new ClassPathPluginLoader("test-atlassian-plugin.xml");
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         Collection plugins = loader.getPlugins(moduleDescriptorFactory);
 
        assertEquals(1, plugins.size());
 
         Plugin plugin = (Plugin) plugins.iterator().next();
         assertEquals("Test Plugin", plugin.getName());
         assertEquals("test.atlassian.plugin", plugin.getKey());
         assertEquals("This plugin descriptor is just used for test purposes!", plugin.getPluginInformation().getDescription());
         assertEquals(2, plugin.getModuleDescriptors().size());
 
         assertEquals("Bear Animal", ((ResourcedModuleDescriptor) plugin.getModuleDescriptor("bear")).getName());
     }
 }
