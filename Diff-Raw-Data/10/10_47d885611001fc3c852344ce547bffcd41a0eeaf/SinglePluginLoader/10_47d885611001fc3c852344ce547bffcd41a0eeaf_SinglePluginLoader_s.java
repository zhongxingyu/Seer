 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginException;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class SinglePluginLoader extends AbstractXmlPluginLoader
 {
     List plugins;
     protected String resource;
     protected InputStream is;
 
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
        {
             plugins = new ArrayList();
             loadPlugins(moduleDescriptorFactory);
        }
 
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
 
     private void loadPlugins(ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         if (resource == null && is == null)
             throw new PluginParseException("No resource or inputstream specified to load plugins from.");
 
         try
         {
             Plugin plugin = configurePlugin(moduleDescriptorFactory, getDocument(), new StaticPlugin());
             plugins.add(plugin);
         }
         catch (DocumentException e)
         {
             throw new PluginParseException("Exception parsing plugin document", e);
         }
     }
 
     protected Document getDocument() throws DocumentException, PluginParseException
     {
         Document doc = null;
 
         if (resource != null)
             doc = getDocument(resource);
         else
             doc = getDocument(is);
 
         return doc;
     }
 
     public void setRecogniseSystemPlugins(boolean recogniseSystemPlugins)
     {
         this.recogniseSystemPlugins = recogniseSystemPlugins;
     }
 }
