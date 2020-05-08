 package com.atlassian.plugin.impl;
 
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginInformation;
 import com.atlassian.plugin.Resourced;
 import com.atlassian.plugin.elements.ResourceDescriptor;
 import com.atlassian.plugin.elements.ResourceLocation;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.InputStream;
 import java.util.*;
 
 public class StaticPlugin implements Plugin, Comparable
 {
     private static final Log log = LogFactory.getLog(StaticPlugin.class);
     private String name;
     private String i18nNameKey;
     private String key;
    private Map modules = new LinkedHashMap();
     private boolean enabledByDefault = true;
     private PluginInformation pluginInformation;
     List resourceDescriptors;
     private boolean enabled;
     private boolean system;
     private Resourced resources;
     private Date dateLoaded = new Date();
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getI18nNameKey()
     {
         return i18nNameKey;
     }
 
     public void setI18nNameKey(String i18nNameKey)
     {
         this.i18nNameKey = i18nNameKey;
     }
 
     public String getKey()
     {
         return key;
     }
 
     public void setKey(String aPackage)
     {
         this.key = aPackage;
     }
 
     public void addModuleDescriptor(ModuleDescriptor moduleDescriptor)
     {
         modules.put(moduleDescriptor.getKey(), moduleDescriptor);
     }
 
     public Collection getModuleDescriptors()
     {
         return modules.values();
     }
 
     public ModuleDescriptor getModuleDescriptor(String key)
     {
         return (ModuleDescriptor) modules.get(key);
     }
 
     public List getModuleDescriptorsByModuleClass(Class aClass)
     {
         List result = new ArrayList();
 
         for (Iterator iterator = modules.values().iterator(); iterator.hasNext();)
         {
             ModuleDescriptor moduleDescriptor = (ModuleDescriptor) iterator.next();
 
             Class moduleClass = moduleDescriptor.getModuleClass();
             if (aClass.isAssignableFrom(moduleClass))
             {
                 result.add(moduleDescriptor);
             }
         }
 
         return result;
     }
 
     public boolean isEnabledByDefault()
     {
         return enabledByDefault && pluginInformation.satisfiesMinJavaVersion();
     }
 
     public void setEnabledByDefault(boolean enabledByDefault)
     {
         this.enabledByDefault = enabledByDefault;
     }
 
     public PluginInformation getPluginInformation()
     {
         return pluginInformation;
     }
 
     public void setPluginInformation(PluginInformation pluginInformation)
     {
         this.pluginInformation = pluginInformation;
     }
 
     public void setResources(Resourced resources)
     {
         this.resources = resources;
     }
 
     public List getResourceDescriptors()
     {
         return resources.getResourceDescriptors();
     }
 
     public List getResourceDescriptors(String type)
     {
         return resources.getResourceDescriptors(type);
     }
 
     public ResourceLocation getResourceLocation(String type, String name)
     {
         return resources.getResourceLocation(type, name);
     }
 
     /**
      * @deprecated
      */
     public ResourceDescriptor getResourceDescriptor(String type, String name)
     {
         return resources.getResourceDescriptor(type, name);  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * @return true if the plugin has been enabled
      */
     public boolean isEnabled()
     {
         return enabled;
     }
 
     /**
      * Setter for the enabled state of a plugin. If this is set to
      * false then the plugin will not execute.
      * @param enabled
      */
     public void setEnabled(boolean enabled)
     {
         this.enabled = enabled;
     }
 
     /**
      * Static plugins loaded from the classpath can't be uninstalled.
      */
     public boolean isUninstallable()
     {
         return false;
     }
 
     public Class loadClass(String clazz, Class callingClass) throws ClassNotFoundException
     {
         return ClassLoaderUtils.loadClass(clazz, callingClass);
     }
 
     public InputStream getResourceAsStream(String name)
     {
         return ClassLoaderUtils.getResourceAsStream(name, this.getClass());
     }
 
     public boolean isDynamicallyLoaded()
     {
         return false;
     }
 
     public boolean isSystemPlugin()
     {
         return system;
     }
 
     public boolean containsSystemModule()
     {
         for (Iterator iterator = modules.values().iterator(); iterator.hasNext();)
         {
             ModuleDescriptor moduleDescriptor = (ModuleDescriptor) iterator.next();
             if(moduleDescriptor.isSystemModule())
             {
                 return true;
             }
         }
         return false;
     }
 
     public void setSystemPlugin(boolean system)
     {
         this.system = system;
     }
 
     public Date getDateLoaded()
     {
         return dateLoaded;
     }
 
     /**
      * Plugins are comparable by version number. It should correctly calculate the higher, lower or equal versions
      * based on standard x.x.x-style version numbers. It ignores whitespaces. Any non-digit or period in a version
      * number is assumed to be non-parseable and returns as equal.
      * @param o
      * @return int -1 for lesserthen , 0 for equal, 1 for greater than.
      */
     public int compareTo(Object o)
     {
         // If the compared object isn't a plugin, then the current object is greater
         if (!(o instanceof Plugin)) return 1;
 
         // If the compared plugin doesn't have the same key, the current object is greater
         if (!((Plugin) o).getKey().equals(this.getKey())) return 1;
 
         // Get the version numbers, remove all whitespaces
         String thisVersion = "0";
         if(StringUtils.isNotEmpty(this.getPluginInformation().getVersion())){
             thisVersion = this.getPluginInformation().getVersion().replaceAll(" ", "");
         }
         String compareVersion = "0";
         if(StringUtils.isNotEmpty(((Plugin) o).getPluginInformation().getVersion())) {
             compareVersion = ((Plugin) o).getPluginInformation().getVersion().replaceAll(" ", "");
         }
 
         // If we can't read the version numbers, then take the new plugin is greater
         String validVersionPattern = "[\\d\\.]*";
         if( !thisVersion.matches(validVersionPattern) || !compareVersion.matches(validVersionPattern)){
           //log.error("Can't parse the plugin version");
           log.warn("Can't parse plugin version number. Taking the last loaded plugin ("  +
                   " v1 = " + thisVersion + ", v2 = " + compareVersion + ").");
           return -1;
         }
 
         // Split the version numbers
         String [] v1 = thisVersion.split("\\.");
         String [] v2 = compareVersion.split("\\.");
 
         // Compare each place, until we find a difference and then return. If empty, assume zero.
         for (int i = 0 ; i < (v1.length > v2.length ? v1.length : v2.length) ; i ++) {
             if(Integer.parseInt(i >= v1.length ? "0" : v1[i]) < Integer.parseInt(i >= v2.length ? "0" : v2[i])) {
                 return -1;
             } else if(Integer.parseInt(i >= v1.length ? "0" : v1[i]) > Integer.parseInt(i >= v2.length ? "0" : v2[i])) {
                 return 1;
             }
         }
 
         return 0;
 
     }
 
     public boolean isDeleteable()
     {
         return false;
     }
 
 }
 
