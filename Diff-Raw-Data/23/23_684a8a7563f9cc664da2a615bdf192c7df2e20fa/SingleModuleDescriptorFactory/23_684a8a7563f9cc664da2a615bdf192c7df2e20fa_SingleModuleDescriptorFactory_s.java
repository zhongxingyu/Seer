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
public class SingleModuleDescriptorFactory<T extends ModuleDescriptor> implements ModuleDescriptorFactory, BeanFactoryAware
 {
     private final String type;
     private final Class<T> moduleDescriptorClass;
    private AutowireCapableBeanFactory beanFactory;
 
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
            if (beanFactory != null)
            {
                result = (T) beanFactory.createBean(moduleDescriptorClass);
            }
            else
            {
                result = moduleDescriptorClass.newInstance();
            }
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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        if (beanFactory instanceof AutowireCapableBeanFactory)
            this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    }
 }
