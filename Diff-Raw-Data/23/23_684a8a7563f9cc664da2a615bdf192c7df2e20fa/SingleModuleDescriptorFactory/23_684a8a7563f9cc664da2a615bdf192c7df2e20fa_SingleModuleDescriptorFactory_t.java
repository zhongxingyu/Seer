 package com.atlassian.plugin.osgi.external;
 
 import com.atlassian.plugin.*;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.beans.BeansException;
 
 /**
  * Single module descriptor factory for plugins to use when they want to expose just one plugin.  Has the added benefit
  * of using the available beanfactory to autowire new module descriptors
  * @since 2.1
  */
public class SingleModuleDescriptorFactory<T extends ModuleDescriptor> implements ModuleDescriptorFactory
 {
     private final String type;
     private final Class<T> moduleDescriptorClass;
 
     public SingleModuleDescriptorFactory(String type, Class<T> moduleDescriptorClass)
     {
         this.moduleDescriptorClass = moduleDescriptorClass;
         this.type = type;
     }
 
     public ModuleDescriptor getModuleDescriptor(String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException
     {
         T result = null;
         if (this.type.equals(type))
         {
            // We can't use an autowired bean factory to create the instance because it would be loaded by this class's
            // classloader, which will not have access to the spring instance in bundle space.
            result = moduleDescriptorClass.newInstance();
         }
         return result;
     }
 
     public boolean hasModuleDescriptor(String type)
     {
         return (this.type.equals(type));
     }
 
     public Class<? extends ModuleDescriptor> getModuleDescriptorClass(String type)
     {
         return (this.type.equals(type) ? moduleDescriptorClass : null);
     }
 }
