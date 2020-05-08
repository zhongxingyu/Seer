 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.ModuleDescriptorFactory;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginException;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.parsers.DescriptorParser;
 import com.atlassian.plugin.parsers.XmlDescriptorParserFactory;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import com.atlassian.plugin.impl.StaticPlugin;
 
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Collections;
 
 /**
  * Loads a single plugin from the descriptor provided, which can either be an InputStream
  * or a resource on the classpath. The classes used by the plugin must already be available
  * on the classpath because this plugin loader does <b>not</b> load any classes.
  * <p/>
  * Because the code which is run by these plugins must already be in the classpath (and
  * is therefore more trusted than an uploaded plugin), if the plugin is marked as a system
  * plugin in the descriptor file, it will actually be marked as a system plugin at runtime.
  *
  * @see PluginLoader
  * @see ClassPathPluginLoader
  * @see DescriptorParser#isSystemPlugin()
  */
 public class SinglePluginLoader implements PluginLoader
 {
     Collection plugins;
     protected String resource;
     protected InputStream is;
     private DescriptorParserFactory descriptorParserFactory = new XmlDescriptorParserFactory();
 
     public SinglePluginLoader(String resource)
     {
         this.resource = resource;
     }
 
     public SinglePluginLoader(InputStream is)
     {
         this.is = is;
     }
 
     public Collection loadAllPlugins(ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         if (plugins == null)
             plugins = Collections.singleton(loadPlugin(moduleDescriptorFactory));
         return plugins;
     }
 
     public boolean supportsRemoval()
     {
         return false;
     }
 
     public boolean supportsAddition()
     {
         return false;
     }
 
     public Collection removeMissingPlugins()
     {
         throw new UnsupportedOperationException("This PluginLoader does not support removal.");
     }
 
     public Collection addFoundPlugins(ModuleDescriptorFactory moduleDescriptorFactory)
     {
         throw new UnsupportedOperationException("This PluginLoader does not support addition.");
     }
 
     public void removePlugin(Plugin plugin) throws PluginException
     {
         throw new PluginException("This PluginLoader does not support removal.");
     }
 
     private Plugin loadPlugin(ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         InputStream source = getSource();
         if (source == null)
             throw new PluginParseException("Invalid resource or inputstream specified to load plugins from.");
 
        Plugin plugin;
         try
         {
             DescriptorParser parser = descriptorParserFactory.getInstance(source);
             plugin = parser.configurePlugin(moduleDescriptorFactory, new StaticPlugin());
             if (parser.isSystemPlugin())
                 plugin.setSystemPlugin(true);
         }
         catch (PluginParseException e)
         {
            throw new PluginParseException("Unable to load plugin resource: " + resource + " - " + e.getMessage() ,e);
         }
 
         return plugin;
     }
 
     protected InputStream getSource()
     {
         if (resource == null)
             return is;
 
         return ClassLoaderUtils.getResourceAsStream(resource, this.getClass());
     }
 }
