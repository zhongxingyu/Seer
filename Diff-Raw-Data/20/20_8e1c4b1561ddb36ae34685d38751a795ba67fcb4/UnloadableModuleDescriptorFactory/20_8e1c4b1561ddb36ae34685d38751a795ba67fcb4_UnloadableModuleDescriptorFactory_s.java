 package com.atlassian.plugin.descriptors;
 
 import com.atlassian.plugin.ModuleDescriptorFactory;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.ModuleDescriptor;
 import org.dom4j.Element;
 
 /**
  * Utility class to create UnloadableModuleDescriptor instances when there are problems
  */
 public final class UnloadableModuleDescriptorFactory
 {
     /**
      * Creates a new UnloadableModuleDescriptor, for when a problem occurs during the construction
      * of the ModuleDescriptor itself.
      *
      * This instance has the same information as the original ModuleDescriptor, but also contains
      * an error message that reports the error.
      *
      * @param plugin the Plugin the ModuleDescriptor belongs to
      * @param element the XML Element used to construct the ModuleDescriptor
      * @param e the Throwable
      * @param moduleDescriptorFactory a ModuleDescriptorFactory used to retrieve ModuleDescriptor instances
      * @return a new UnloadableModuleDescriptor instance
      * @throws PluginParseException if there was a problem constructing the UnloadableModuleDescriptor
      */
     public static UnloadableModuleDescriptor createUnloadableModuleDescriptor(Plugin plugin, Element element, Throwable e, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         UnloadableModuleDescriptor descriptor = new UnloadableModuleDescriptor();
         descriptor.init(plugin, element);
 
         String name = element.getName();
         Class moduleClass = moduleDescriptorFactory.getModuleDescriptorClass(name);
         String moduleClassName;
 
         if (moduleClass == null)
             moduleClassName = descriptor.getKey();
         else
             moduleClassName = moduleClass.getName();
 
         String errorMsg = constructErrorMessage(plugin, name, moduleClassName, e);
 
         descriptor.setErrorText(errorMsg);
 
         return descriptor;
     }
 
     /**
      * Creates a new UnloadableModuleDescriptor based on an existing ModuleDescriptor, descriptor
      *
      * This method uses the information in an existing descriptor to construct a new UnloadableModuleDescriptor.
      *
      * @param plugin the Plugin the ModuleDescriptor belongs to
      * @param descriptor the ModuleDescriptor that reported an error
      * @param e the Throwable
      * @return a new UnloadableModuleDescriptor instance
      */
     public static UnloadableModuleDescriptor createUnloadableModuleDescriptor(Plugin plugin, ModuleDescriptor descriptor, Throwable e)
     {
         UnloadableModuleDescriptor unloadableDescriptor = new UnloadableModuleDescriptor();
         unloadableDescriptor.setName(descriptor.getName());
         unloadableDescriptor.setKey(descriptor.getKey());
         unloadableDescriptor.setPlugin(plugin);
 
         String errorMsg = constructErrorMessage(plugin, descriptor.getName(), (descriptor.getModuleClass() == null ? descriptor.getName() : descriptor.getModuleClass().getName()), e);
 
         unloadableDescriptor.setErrorText(errorMsg);
 
         return unloadableDescriptor;
     }
 
     /**
      * Constructs an error message from a module and exception
      *
      * @param plugin the Plugin the module belongs to
      * @param moduleName the name of the module
      * @param moduleClass the class of the module
      * @param e the Throwable
      * @return an appropriate String representing the error
      */
     private static String constructErrorMessage(Plugin plugin, String moduleName, String moduleClass, Throwable e)
     {
         String errorMsg;
 
         if (e instanceof PluginParseException)
            errorMsg = "There was a problem loading the descriptor for module '" + moduleName + "' in plugin '" + (plugin == null ? "null" : plugin.getName()) + "'";
         else if (e instanceof InstantiationException)
            errorMsg = "Could not instantiate module descriptor: " + moduleClass;
         else if (e instanceof IllegalAccessException)
            errorMsg = "Exception instantiating module descriptor: " + moduleClass;
         else if (e instanceof ClassNotFoundException)
            errorMsg = "Could not find module descriptor class: " + moduleClass;
         else if (e instanceof NoClassDefFoundError)
            errorMsg = "A required class was missing: " + moduleClass + ". Please check that you have all of the required dependencies.";
         else
            errorMsg = "There was a problem loading the module descriptor: " + moduleClass;
 
        return errorMsg;
     }
 }
