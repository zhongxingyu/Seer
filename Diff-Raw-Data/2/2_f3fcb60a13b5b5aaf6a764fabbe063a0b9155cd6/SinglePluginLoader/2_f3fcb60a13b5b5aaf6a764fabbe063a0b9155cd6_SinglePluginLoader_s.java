 package com.atlassian.plugin.loaders;
 
 import com.atlassian.core.util.ClassLoaderUtils;
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.PluginInformation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.*;
 
 public class SinglePluginLoader implements PluginLoader
 {
     private static Log log = LogFactory.getLog(SinglePluginLoader.class);
 
     List plugins;
     private String resource;
     private InputStream is;
 
     public SinglePluginLoader(String resource)
     {
         this.resource = resource;
     }
 
     public SinglePluginLoader(InputStream is)
     {
         this.is = is;
     }
 
     private void loadPlugins(Map moduleDescriptors) throws PluginParseException
     {
         if (resource == null && is == null)
             throw new PluginParseException("No resource or inputstream specified to load plugins from.");
 
         Plugin plugin = new Plugin();
 
         try
         {
             Document doc = getDocument();
             Element root = doc.getRootElement();
 
             plugin.setName(root.attributeValue("name"));
             plugin.setKey(root.attributeValue("key"));
 
             if (plugin.getKey().indexOf(":") > 0)
                 throw new PluginParseException("Plugin key's cannot contain ':'. Key is '" + plugin.getKey() + "'");
 
             if ("disabled".equalsIgnoreCase(root.attributeValue("state")))
                 plugin.setEnabledByDefault(false);
 
             for (Iterator i = root.elementIterator(); i.hasNext();)
             {
                 Element element = (Element) i.next();
 
                 if ("plugin-info".equalsIgnoreCase(element.getName()))
                 {
                     plugin.setPluginInformation(createPluginInformation(element));
                 }
                 else
                 {
                     ModuleDescriptor moduleDescriptor = createModuleDescriptor(plugin, element, moduleDescriptors);
 
                     if (plugin.getModule(moduleDescriptor.getKey()) != null)
                         throw new PluginParseException("Found duplicate key '" + moduleDescriptor.getKey() + "' within plugin '" + plugin.getKey() + "'");
 
                     if (moduleDescriptor != null)
                         plugin.addModule(moduleDescriptor);
                 }
             }
         }
         catch (DocumentException e)
         {
             throw new PluginParseException("Exception parsing plugin document", e);
         }
 
         plugins.add(plugin);
     }
 
     private Document getDocument() throws DocumentException, PluginParseException {
         SAXReader reader = new SAXReader();
 
         if (resource != null)
         {
             final InputStream is = ClassLoaderUtils.getResourceAsStream(resource, SinglePluginLoader.class);
 
             if (is == null)
                 throw new PluginParseException("Couldn't find resource: " + resource);
 
             return reader.read(is);
         }
         else if (is != null)
         {
             try
             {
                 return reader.read(is);
             }
             finally
             {
                 try
                 {
                     is.close();
                 }
                 catch (IOException e)
                 {
                     log.error("Bad inputstream close: " + e, e);
                 }
             }
         }
         else
             throw new PluginParseException("No resource or input stream specified.");
     }
 
     public Collection getPlugins(Map moduleDescriptors) throws PluginParseException
     {
         if (plugins == null)
         {
             plugins = new ArrayList();
             loadPlugins(moduleDescriptors);
         }
 
         return plugins;
     }
 
     private ModuleDescriptor createModuleDescriptor(Plugin plugin, Element element, Map moduleDescriptors) throws PluginParseException
     {
         String name = element.getName();
 
         Class descriptorClass = (Class) moduleDescriptors.get(name);
 
         if (descriptorClass == null)
         {
            throw new PluginParseException("Could not find descriptor for module: " + name);
         }
 
         ModuleDescriptor moduleDescriptorDescriptor = null;
 
         try
         {
             moduleDescriptorDescriptor = (ModuleDescriptor) ClassLoaderUtils.loadClass(descriptorClass.getName(), SinglePluginLoader.class).newInstance();
         }
         catch (InstantiationException e)
         {
             throw new PluginParseException("Could not instantiate module descriptor: " + descriptorClass.getName(), e);
         }
         catch (IllegalAccessException e)
         {
             throw new PluginParseException("Exception instantiating module descriptor: " + descriptorClass.getName(), e);
         }
         catch (ClassNotFoundException e)
         {
             throw new PluginParseException("Could not find module descriptor class: " + descriptorClass.getName(), e);
         }
 
         moduleDescriptorDescriptor.init(plugin, element);
 
         return moduleDescriptorDescriptor;
     }
 
     private PluginInformation createPluginInformation(Element element)
     {
         PluginInformation pluginInfo = new PluginInformation();
 
         if (element.element("description") != null)
             pluginInfo.setDescription(element.element("description").getTextTrim());
 
         if (element.element("version") != null)
             pluginInfo.setVersion(element.element("version").getTextTrim());
 
         if (element.element("vendor") != null)
         {
             final Element vendor = element.element("vendor");
             pluginInfo.setVendorName(vendor.attributeValue("name"));
             pluginInfo.setVendorUrl(vendor.attributeValue("url"));
         }
 
         if (element.element("application-version") != null)
         {
             pluginInfo.setMaxVersion(Float.parseFloat(element.element("application-version").attributeValue("max")));
             pluginInfo.setMinVersion(Float.parseFloat(element.element("application-version").attributeValue("min")));
         }
 
         return pluginInfo;
     }
 
 }
