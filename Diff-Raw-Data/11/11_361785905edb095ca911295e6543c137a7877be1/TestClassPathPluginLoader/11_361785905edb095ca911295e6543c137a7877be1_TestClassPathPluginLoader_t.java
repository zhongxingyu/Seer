 package com.atlassian.plugin.loaders;
 
 import com.atlassian.core.util.ClassLoaderUtils;
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.descriptors.ResourcedModuleDescriptor;
 import com.atlassian.plugin.mock.*;
 import junit.framework.TestCase;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class TestClassPathPluginLoader extends TestCase
 {
     public void testAtlassianPlugin() throws Exception
     {
         ClassPathPluginLoader loader = new ClassPathPluginLoader("test-atlassian-plugin.xml");
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         Collection plugins = loader.getPlugins(moduleDescriptorFactory);
 
        assertEquals(2, plugins.size());
 
         Plugin plugin = (Plugin) plugins.iterator().next();
         assertEquals("Test Plugin", plugin.getName());
         assertEquals("test.atlassian.plugin", plugin.getKey());
         assertEquals("This plugin descriptor is just used for test purposes!", plugin.getPluginInformation().getDescription());
         assertEquals(2, plugin.getModuleDescriptors().size());
 
         assertEquals("Bear Animal", ((ResourcedModuleDescriptor) plugin.getModuleDescriptor("bear")).getName());
 
     }
 }
