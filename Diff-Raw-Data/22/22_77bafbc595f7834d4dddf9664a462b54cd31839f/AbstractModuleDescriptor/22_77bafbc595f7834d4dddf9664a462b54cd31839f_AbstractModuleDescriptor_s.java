 package com.atlassian.plugin.descriptors;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.elements.ResourceDescriptor;
 import com.atlassian.plugin.loaders.LoaderUtils;
 import com.atlassian.plugin.util.JavaVersionUtils;
 import org.dom4j.Element;
 
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractModuleDescriptor implements ModuleDescriptor
 {
     protected Plugin plugin;
     String key;
     String name;
     Class moduleClass;
     String description;
     boolean enabledByDefault = true;
     boolean systemModule = false;
     protected boolean singleton = true;
     Map params;
     private Resources resources;
     private Float minJavaVersion;
 
     public void init(Plugin plugin, Element element) throws PluginParseException
     {
         this.plugin = plugin;
         this.key = element.attributeValue("key");
         this.name = element.attributeValue("name");
 
         String clazz = element.attributeValue("class");
         try
         {
             if (clazz != null)  //not all plugins have to have a class
             {
                 // First try and load the class, to make sure the class exists
                 moduleClass = plugin.loadClass(clazz, getClass());
 
                 // Then instantiate the class, so we can see if there are any dependencies that aren't satisfied
                moduleClass.newInstance();
             }
         }
         catch (ClassNotFoundException e)
         {
             throw new PluginParseException("Could not load class: " + clazz, e);
         }
         catch (IllegalAccessException e)
         {
             throw new PluginParseException(e);
         }
         catch (InstantiationException e)
         {
             throw new PluginParseException(e);
         }
         catch (NoClassDefFoundError e)
         {
             throw new PluginParseException("Error retrieving dependency of class: " + clazz + ". Missing class: " + e.getMessage());
         }
 
         this.description = element.elementTextTrim("description");
         params = LoaderUtils.getParams(element);
 
         if ("disabled".equalsIgnoreCase(element.attributeValue("state")))
         {
             enabledByDefault = false;
         }
 
         if ("true".equalsIgnoreCase(element.attributeValue("system")))
         {
             systemModule = true;
         }
 
         if (element.element("java-version") != null)
         {
             minJavaVersion = Float.valueOf(element.element("java-version").attributeValue("min"));
         }
 
         if ("false".equalsIgnoreCase(element.attributeValue("singleton")))
         {
             singleton = false;
         }
         else if ("true".equalsIgnoreCase(element.attributeValue("singleton")))
         {
             singleton = true;
         }
         else
         {
             singleton = isSingletonByDefault();
         }
 
         resources = Resources.fromXml(element);
     }
 
     /**
      * Override this if your plugin needs to clean up when it's been removed.
      * @param plugin
      */
     public void destroy(Plugin plugin)
     {}
 
     public boolean isEnabledByDefault()
     {
         return enabledByDefault && satisfiesMinJavaVersion();
     }
 
     public boolean isSystemModule()
     {
         return systemModule;
     }
 
     public boolean isSingleton()
     {
         return singleton;
     }
 
     /**
      * Override this method if you want your module descriptor to be or not be a singleton by default.
      * <p>
      * Default is "true" - ie all plugin modules are singletons by default.
      */
     protected boolean isSingletonByDefault()
     {
         return true;
     }
 
     /**
      * Check that the module class of this descriptor implements a given interface, or extends a given class.
      * @param requiredModuleClazz The class this module's class must implement or extend.
      * @throws PluginParseException If the module class does not implement or extend the given class.
      */
     final protected void assertModuleClassImplements(Class requiredModuleClazz) throws PluginParseException
     {
         if (!requiredModuleClazz.isAssignableFrom(getModuleClass()))
         {
             throw new PluginParseException("Given module class: " + getModuleClass().getName() + " does not implement " + requiredModuleClazz.getName());
         }
     }
 
     public String getCompleteKey() {
         return plugin.getKey() + ":" + getKey();
     }
 
     public String getPluginKey()
     {
         return plugin.getKey();
     }
 
     public String getKey()
     {
         return key;
     }
 
     public String getName()
     {
         return name;
     }
 
     public Class getModuleClass()
     {
         return moduleClass;
     }
 
     public abstract Object getModule();
 
     public String getDescription()
     {
         return description;
     }
 
     public Map getParams()
     {
         return params;
     }
 
     public List getResourceDescriptors()
     {
         return resources.getResourceDescriptors();
     }
 
     public List getResourceDescriptors(String type)
     {
         return resources.getResourceDescriptors(type);
     }
 
     public ResourceDescriptor getResourceDescriptor(String type, String name)
     {
         return resources.getResourceDescriptor(type, name);
     }
 
     public Float getMinJavaVersion()
     {
         return minJavaVersion;
     }
 
     public boolean satisfiesMinJavaVersion()
     {
         if(minJavaVersion != null)
         {
             return JavaVersionUtils.satisfiesMinVersion(minJavaVersion.floatValue());
         }
         return true;
     }
 
     /**
      * Sets the plugin for the ModuleDescriptor
      *
      * @param plugin
      */
     public void setPlugin(Plugin plugin)
     {
         this.plugin = plugin;
     }
 }
