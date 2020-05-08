 package com.atlassian.plugin.parsers;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.impl.UnloadablePluginFactory;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
 import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
 import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptorFactory;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptorFactory;
 import org.dom4j.Element;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.SAXReader;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Iterator;
 import java.io.InputStream;
 
 /**
  * Provides access to the descriptor information retrieved from an XML InputStream.
  * <p/>
  * Uses the dom4j {@link SAXReader} to parse the XML stream into a document
  * when the parser is constructed.
  *
  * @see XmlDescriptorParserFactory
  */
 public class XmlDescriptorParser implements DescriptorParser
 {
     private static Log log = LogFactory.getLog(XmlDescriptorParser.class);
 
     boolean recogniseSystemPlugins = false;
     private Document document;
 
     /**
      * @throws PluginParseException if there is a problem reading the descriptor from the XML {@link InputStream}.
      */
     public XmlDescriptorParser(InputStream source) throws PluginParseException
     {
         if (source == null)
             throw new IllegalArgumentException("source cannot be null");
         document = createDocument(source);
     }
 
     protected Document createDocument(InputStream source) throws PluginParseException
     {
         SAXReader reader = new SAXReader();
         try
         {
             return reader.read(source);
         }
         catch (DocumentException e)
         {
             throw new PluginParseException("Cannot parse XML plugin descriptor", e);
         }
     }
 
     protected Document getDocument()
     {
         return document;
     }
 
     public Plugin configurePlugin(ModuleDescriptorFactory moduleDescriptorFactory, Plugin plugin) throws PluginParseException
     {
 
         Element pluginElement = getPluginElement();
         plugin.setName(pluginElement.attributeValue("name"));
         plugin.setKey(getKey());
 
         if (pluginElement.attributeValue("i18n-name-key") != null)
         {
             plugin.setI18nNameKey(pluginElement.attributeValue("i18n-name-key"));
         }
 
         if (plugin.getKey().indexOf(":") > 0)
             throw new PluginParseException("Plugin keys cannot contain ':'. Key is '" + plugin.getKey() + "'");
 
         if ("disabled".equalsIgnoreCase(pluginElement.attributeValue("state")))
             plugin.setEnabledByDefault(false);
 
         for (Iterator i = pluginElement.elementIterator(); i.hasNext();)
         {
             Element element = (Element) i.next();
 
             if ("plugin-info".equalsIgnoreCase(element.getName()))
             {
                 plugin.setPluginInformation(createPluginInformation(element));
             }
             else if (!"resource".equalsIgnoreCase(element.getName()))
             {
                 ModuleDescriptor moduleDescriptor = createModuleDescriptor(plugin, element, moduleDescriptorFactory);
 
                 // If we're not loading the module descriptor, null is returned, so we skip it
                 if (moduleDescriptor == null)
                     continue;
 
                 if (plugin.getModuleDescriptor(moduleDescriptor.getKey()) != null)
                     throw new PluginParseException("Found duplicate key '" + moduleDescriptor.getKey() + "' within plugin '" + plugin.getKey() + "'");
 
                 plugin.addModuleDescriptor(moduleDescriptor);
 
                 // If we have any unloadable modules, also create an unloadable plugin, which will make it clear that there was a problem
                 if (moduleDescriptor instanceof UnloadableModuleDescriptor)
                 {
                     log.error("There were errors loading the plugin '" + plugin.getName() + "'. The plugin has been disabled.");
                     return UnloadablePluginFactory.createUnloadablePlugin(plugin);
                 }
             }
         }
 
         plugin.setResources(Resources.fromXml(pluginElement));
 
         return plugin;
     }
 
     private Element getPluginElement()
     {
         return document.getRootElement();
     }
 
     protected ModuleDescriptor createModuleDescriptor(Plugin plugin, Element element, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         String name = element.getName();
 
         ModuleDescriptor moduleDescriptorDescriptor;
 
         // Try to retrieve the module descriptor
         try
         {
             moduleDescriptorDescriptor = moduleDescriptorFactory.getModuleDescriptor(name);
         }
         // When there's a problem loading a module, return an UnrecognisedModuleDescriptor with error
         catch (Throwable e)
         {
             UnrecognisedModuleDescriptor descriptor = UnrecognisedModuleDescriptorFactory.createUnrecognisedModuleDescriptor(plugin, element, e, moduleDescriptorFactory);
 
             log.error("There were problems loading the module '" + name + "' in plugin '" + plugin.getName() + "'. The module has been disabled.");
             log.error(descriptor.getErrorText(), e);
 
             return descriptor;
         }
 
         // When the module descriptor has been excluded, null is returned (PLUG-5)
         if (moduleDescriptorDescriptor == null)
         {
             log.info("The module '" + name + "' in plugin '" + plugin.getName() + "' is in the list of excluded module descriptors, so not enabling.");
             return null;
         }
 
         // Once we have the module descriptor, create it using the given information
         try
         {
             moduleDescriptorDescriptor.init(plugin, element);
         }
         // If it fails, return a dummy module that contains the error
         catch (Exception e)
         {
             UnloadableModuleDescriptor descriptor = UnloadableModuleDescriptorFactory.createUnloadableModuleDescriptor(plugin, element, e, moduleDescriptorFactory);
 
             log.error("There were problems loading the module '" + name + "'. The module and its plugin have been disabled.");
             log.error(descriptor.getErrorText(), e);
 
             return descriptor;
         }
 
         return moduleDescriptorDescriptor;
     }
 
 
     protected PluginInformation createPluginInformation(Element element)
     {
         PluginInformation pluginInfo = new PluginInformation();
 
         if (element.element("description") != null)
         {
             pluginInfo.setDescription(element.element("description").getTextTrim());
             if (element.element("description").attributeValue("key") != null)
             {
                 pluginInfo.setDescriptionKey(element.element("description").attributeValue("key"));
             }
         }
 
         if (element.element("version") != null)
             pluginInfo.setVersion(element.element("version").getTextTrim());
 
         if (element.element("vendor") != null)
         {
             final Element vendor = element.element("vendor");
             pluginInfo.setVendorName(vendor.attributeValue("name"));
             pluginInfo.setVendorUrl(vendor.attributeValue("url"));
         }
 
         // initialize any parameters on the plugin xml definition
         for (Iterator iterator = element.elements("param").iterator(); iterator.hasNext();)
         {
             Element param = (Element) iterator.next();
 
             // Retrieve the parameter info => name & text
             if (param.attribute("name") != null)
                 pluginInfo.addParameter(param.attribute("name").getData().toString(), param.getText());
         }
 
         if (element.element("application-version") != null)
         {
             pluginInfo.setMaxVersion(Float.parseFloat(element.element("application-version").attributeValue("max")));
             pluginInfo.setMinVersion(Float.parseFloat(element.element("application-version").attributeValue("min")));
         }
 
         if (element.element("java-version") != null)
         {
             pluginInfo.setMinJavaVersion(Float.valueOf(element.element("java-version").attributeValue("min")));
         }
 
         return pluginInfo;
     }
 
     public String getKey()
     {
         return getPluginElement().attributeValue("key");
     }
 
     public int getPluginsVersion()
     {
         String val = getPluginElement().attributeValue("pluginsVersion");
         if (val == null)
         {
             val = getPluginElement().attributeValue("plugins-version");
         }
         if (val != null) {
             return Integer.parseInt(val);
         } else {
             return 1;
         }
     }
 
     public PluginInformation getPluginInformation()
     {
         return createPluginInformation(getDocument().getRootElement().element("plugin-info"));
     }
 
 
     public boolean isSystemPlugin()
     {
         return "true".equalsIgnoreCase(getPluginElement().attributeValue("system"));
     }
 }
