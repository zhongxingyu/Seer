 package com.atlassian.plugin;
 
 import com.atlassian.plugin.loaders.PluginLoader;
 
 import java.util.*;
 
 public class DefaultPluginManager implements PluginManager
 {
     private final List pluginLoaders;
     private final PluginStateStore store;
     private Map moduleDescriptors;
     private PluginManagerState currentState;
     private HashMap plugins;
 
     public DefaultPluginManager(PluginStateStore store, List pluginLoaders, Map moduleDescriptors)
     {
         this.pluginLoaders = pluginLoaders;
         this.store = store;
         this.currentState = store.loadPluginState();
         this.moduleDescriptors = moduleDescriptors;
         this.plugins = new HashMap();
     }
 
     public void init() throws PluginParseException {
         // retrieve all the plugins
         for (Iterator iterator = pluginLoaders.iterator(); iterator.hasNext();)
         {
             PluginLoader loader = (PluginLoader) iterator.next();
 
             for (Iterator iterator1 = loader.getPlugins(moduleDescriptors).iterator(); iterator1.hasNext();)
             {
                 addPlugin((Plugin) iterator1.next());
             }
         }
     }
 
     private void addPlugin(Plugin plugin) throws PluginParseException
     {
         // testing to make sure plugin keys are unique
         if (plugins.containsKey(plugin.getKey()))
             throw new PluginParseException("Duplicate plugin key found: '" + plugin.getKey() + "'");
 
         plugins.put(plugin.getKey(), plugin);
     }
 
     private void saveState()
     {
         store.savePluginState(currentState);
     }
 
     public Collection getPlugins()
     {
         return plugins.values();
     }
 
     public Collection getEnabledPlugins()
     {
         List result = new ArrayList();
 
         for (Iterator iterator = plugins.keySet().iterator(); iterator.hasNext();)
         {
             String key = (String) iterator.next();
             Plugin p = getEnabledPlugin(key);
 
             if (p != null)
                 result.add(p);
         }
 
         return result;
     }
 
     public Plugin getPlugin(String key)
     {
         return (Plugin) plugins.get(key);
     }
 
     public Plugin getEnabledPlugin(String pluginKey)
     {
         if (isPluginEnabled(pluginKey))
             return getPlugin(pluginKey);
 
         return null;
     }
 
     public ModuleDescriptor getPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final Plugin plugin = getPlugin(key.getPluginKey());
 
         if (plugin != null)
             return plugin.getModule(key.getModuleKey());
 
         return null;
     }
 
     public ModuleDescriptor getEnabledPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         if (isPluginModuleEnabled(completeKey))
         {
             return getEnabledPlugin(key.getPluginKey()).getModule(key.getModuleKey());
         }
 
         return null;
     }
 
     public List getEnabledModulesByClass(Class moduleClass)
     {
         for (Iterator iterator = plugins.entrySet().iterator(); iterator.hasNext();)
         {
             Map.Entry entry = (Map.Entry) iterator.next();
             Plugin plugin = (Plugin) entry.getValue();
             List result = plugin.getModulesByClass(moduleClass);
 
             if (!result.isEmpty())
                 return result;
         }
 
         return Collections.EMPTY_LIST;
     }
 
 
     public void enablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (plugins.containsKey(key))
         {
             Plugin plugin = (Plugin) plugins.get(key);
 
             if (!plugin.isEnabledByDefault())
                 currentState.setState(key, Boolean.TRUE);
             else
                 currentState.removeState(key);
 
             saveState();
         }
     }
 
     public void disablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (plugins.containsKey(key))
         {
             Plugin plugin = (Plugin) plugins.get(key);
             if (plugin.isEnabledByDefault())
                 currentState.setState(key, Boolean.FALSE);
             else
                 currentState.removeState(key);
 
             saveState();
         }
     }
 
     public void disablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
         if (module != null)
         {
             if (module.isEnabledByDefault())
                 currentState.setState(completeKey, Boolean.FALSE);
             else
                 currentState.removeState(completeKey);
 
             saveState();
         }
     }
 
     public void enablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
         if (module != null)
         {
             if (!module.isEnabledByDefault())
                 currentState.setState(completeKey, Boolean.TRUE);
             else
                 currentState.removeState(completeKey);
 
             saveState();
         }
     }
 
     public boolean isPluginModuleEnabled(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
        return isPluginEnabled(key.getPluginKey()) && currentState.isEnabled(getPluginModule(completeKey));
     }
 
     public boolean isPluginEnabled(String key)
     {
         return plugins.containsKey(key) && currentState.isEnabled((Plugin) plugins.get(key));
     }
 
     public List getEnabledModuleDescriptorsByClass(Class descriptorClazz) {
         List result = new LinkedList();
 
         for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();) {
             Plugin plugin = (Plugin) iterator.next();
 
             if (isPluginEnabled(plugin.getKey()))
             {
                 for (Iterator iterator1 = plugin.getModules().iterator(); iterator1.hasNext();)
                 {
                     ModuleDescriptor module = (ModuleDescriptor) iterator1.next();
 
                     if (descriptorClazz.isInstance(module))
                     {
                         result.add(module);
                     }
                 }
             }
         }
 
         return result;
     }
 
     public List getEnabledModuleDescriptorsByType(String type)
     {
         final Class descriptorClazz = (Class) moduleDescriptors.get(type);
 
         if (descriptorClazz == null)
             throw new IllegalArgumentException("No module descriptor of type: " + type + " found.");
 
         return getEnabledModuleDescriptorsByClass(descriptorClazz);
     }
 }
