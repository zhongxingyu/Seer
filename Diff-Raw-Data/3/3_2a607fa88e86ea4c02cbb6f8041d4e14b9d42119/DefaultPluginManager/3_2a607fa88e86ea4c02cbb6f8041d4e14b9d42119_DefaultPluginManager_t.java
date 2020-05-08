 package com.atlassian.plugin;
 
 import com.atlassian.plugin.classloader.PluginsClassLoader;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptorFactory;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.impl.UnloadablePluginFactory;
 import com.atlassian.plugin.loaders.DynamicPluginLoader;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.predicate.*;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginDisabledEvent;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartingEvent;
 import com.atlassian.plugin.util.WaitUntil;
 import com.atlassian.plugin.util.PluginUtils;
 import org.apache.commons.collections.Closure;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * This implementation delegates the initiation and classloading of plugins to a
  * list of {@link PluginLoader}s and records the state of plugins in a {@link PluginStateStore}.
  * <p/>
  * This class is responsible for enabling and disabling plugins and plugin modules and reflecting these
  * state changes in the PluginStateStore.
  * <p/>
  * An interesting quirk in the design is that {@link #installPlugin(PluginArtifact)} explicitly stores
  * the plugin via a {@link PluginInstaller}, whereas {@link #uninstall(Plugin)} relies on the
  * underlying {@link PluginLoader} to remove the plugin if necessary.
  */
 public class DefaultPluginManager implements PluginManager
 {
     private static final Log log = LogFactory.getLog(DefaultPluginManager.class);
     private final List<PluginLoader> pluginLoaders;
     private final PluginStateStore store;
     private final ModuleDescriptorFactory moduleDescriptorFactory;
     private final PluginsClassLoader classLoader;
     private final Map<String,Plugin> plugins = new HashMap<String,Plugin>();
     private final PluginEventManager pluginEventManager;
 
     /**
      * Installer used for storing plugins. Used by {@link #installPlugin(PluginArtifact)}.
      */
     private PluginInstaller pluginInstaller;
 
     /**
      * Stores {@link Plugin}s as a key and {@link PluginLoader} as a value.
      */
     private final Map<Plugin,PluginLoader> pluginToPluginLoader = new HashMap<Plugin,PluginLoader>();
 
     public DefaultPluginManager(PluginStateStore store, List<PluginLoader> pluginLoaders, ModuleDescriptorFactory moduleDescriptorFactory, PluginEventManager pluginEventManager)
     {
         if (store == null)
         {
             throw new IllegalArgumentException("PluginStateStore must not be null.");
         }
         if (pluginLoaders == null)
         {
             throw new IllegalArgumentException("Plugin Loaders list must not be null.");
         }
         if (moduleDescriptorFactory == null)
         {
             throw new IllegalArgumentException("ModuleDescriptorFactory must not be null.");
         }
         if (pluginEventManager == null)
         {
             throw new IllegalArgumentException("PluginEventManager must not be null.");
         }
         this.pluginLoaders = pluginLoaders;
         this.store = store;
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         this.pluginEventManager = pluginEventManager;
         classLoader = new PluginsClassLoader(this);
     }
 
     /**
      * Initialize all plugins in all loaders
      *
      * @throws PluginParseException
      */
     public void init() throws PluginParseException
     {
         long start = System.currentTimeMillis();
         log.info("Initialising the plugin system");
         pluginEventManager.broadcast(new PluginFrameworkStartingEvent(this, this));
         for (PluginLoader loader : pluginLoaders)
         {
             if (loader == null) continue;
 
             addPlugins(loader, loader.loadAllPlugins(moduleDescriptorFactory));
         }
         pluginEventManager.broadcast(new PluginFrameworkStartedEvent(this, this));
         long end = System.currentTimeMillis();
         log.info("Plugin system started in "+(end - start)+"ms");
     }
 
     /**
      * Fires the shutdown event
      * @since 2.0.0
      */
     public void shutdown()
     {
         log.info("Shutting down the plugin system");
         pluginEventManager.broadcast(new PluginFrameworkShutdownEvent(this, this));
     }
 
     /**
      * Set the plugin installation strategy for this manager
      *
      * @param pluginInstaller the plugin installation strategy to use
      * @see PluginInstaller
      */
     public void setPluginInstaller(PluginInstaller pluginInstaller)
     {
         this.pluginInstaller = pluginInstaller;
     }
 
     protected final PluginStateStore getStore()
     {
         return store;
     }
 
     public String installPlugin(PluginArtifact pluginArtifact) throws PluginParseException
     {
         String key = validatePlugin(pluginArtifact);
         pluginInstaller.installPlugin(key, pluginArtifact);
         scanForNewPlugins();
         return key;
     }
 
     /**
      * Validate a plugin jar.  Looks through all plugin loaders for ones that can load the plugin and
      * extract the plugin key as proof.
      *
      * @param pluginArtifact the jar file representing the plugin
      * @return The plugin key
      * @throws PluginParseException if the plugin cannot be parsed
      * @throws NullPointerException if <code>pluginJar</code> is null.
      */
     String validatePlugin(PluginArtifact pluginArtifact) throws PluginParseException
     {
         String key;
 
         boolean foundADynamicPluginLoader = false;
         for (PluginLoader loader : pluginLoaders)
         {
             if (loader instanceof DynamicPluginLoader)
             {
                 foundADynamicPluginLoader = true;
                 key = ((DynamicPluginLoader) loader).canLoad(pluginArtifact);
                 if (key != null)
                     return key;
             }
         }
 
         if (!foundADynamicPluginLoader)
         {
             throw new IllegalStateException("Should be at least one DynamicPluginLoader in the plugin loader list");
         }
         throw new PluginParseException("Jar " + pluginArtifact.getName() + " is not a valid plugin");
     }
 
     public int scanForNewPlugins() throws PluginParseException
     {
         int numberFound = 0;
 
         for (PluginLoader loader : pluginLoaders)
         {
             if (loader != null)
             {
                 if (loader.supportsAddition())
                 {
                     List<Plugin> pluginsToAdd = new ArrayList<Plugin>();
                     for (Plugin plugin : loader.addFoundPlugins(moduleDescriptorFactory))
                     {
                         // Only actually install the plugin if its module descriptors support it.  Otherwise, mark it as
                         // unloadable.
                         if (!(plugin instanceof UnloadablePlugin) && PluginUtils.doesPluginRequireRestart(plugin))
                         {
                             try
                             {
                                 plugin.close();
                             }
                             catch (RuntimeException ex)
                             {
                                 log.warn("Unable to uninstall the plugin after it was determined to require a restart", ex);
                             }
                             UnloadablePlugin unloadablePlugin = new UnloadablePlugin("Plugin requires a restart of the application");
                             unloadablePlugin.setKey(plugin.getKey());
                             plugin = unloadablePlugin;
                         }
                         pluginsToAdd.add(plugin);
                     }
                     addPlugins(loader, pluginsToAdd);
                     numberFound = pluginsToAdd.size();
                 }
             }
         }
 
         return numberFound;
     }
 
     public void uninstall(Plugin plugin) throws PluginException
     {
         unloadPlugin(plugin);
 
         // PLUG-13: Plugins should not save state across uninstalls.
         removeStateFromStore(getStore(), plugin);
     }
 
     protected void removeStateFromStore(PluginStateStore stateStore, Plugin plugin)
     {
         PluginManagerState currentState = stateStore.loadPluginState();
         currentState.removeState(plugin.getKey());
         for (ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
         {
             currentState.removeState(moduleDescriptor.getCompleteKey());
         }
         stateStore.savePluginState(currentState);
     }
 
     /**
      * Unload a plugin. Called when plugins are added locally,
      * or remotely in a clustered application.
      *
      * @param plugin the plugin to remove
      * @throws PluginException if th eplugin cannot be uninstalled
      */
     protected void unloadPlugin(Plugin plugin) throws PluginException
     {
         if (!plugin.isUninstallable())
             throw new PluginException("Plugin is not uninstallable: " + plugin.getKey());
 
         PluginLoader loader = pluginToPluginLoader.get(plugin);
 
         if (loader != null && !loader.supportsRemoval())
         {
             throw new PluginException("Not uninstalling plugin - loader doesn't allow removal. Plugin: " + plugin.getKey());
         }
 
         if (isPluginEnabled(plugin.getKey()))
             notifyPluginDisabled(plugin);
 
         notifyUninstallPlugin(plugin);
         if (loader != null)
         {
             removePluginFromLoader(plugin);
         }
 
         plugins.remove(plugin.getKey());
     }
 
     private void removePluginFromLoader(Plugin plugin) throws PluginException
     {
         if (plugin.isDeleteable())
         {
             PluginLoader pluginLoader = pluginToPluginLoader.get(plugin);
             pluginLoader.removePlugin(plugin);
         }
 
         pluginToPluginLoader.remove(plugin);
     }
 
     protected void notifyUninstallPlugin(Plugin plugin)
     {
         classLoader.notifyUninstallPlugin(plugin);
 
         for (ModuleDescriptor descriptor : plugin.getModuleDescriptors())
         {
             descriptor.destroy(plugin);
         }
     }
 
     protected PluginManagerState getState()
     {
         return getStore().loadPluginState();
     }
 
     /**
      * @deprecated Since 2.0.2, use {@link #addPlugins(PluginLoader,Collection<Plugin>...)} instead
      */
     protected void addPlugin(PluginLoader loader, Plugin plugin) throws PluginParseException
     {
         addPlugins(loader, Collections.singletonList(plugin));
     }
 
     /**
      * Update the local plugin state and enable state aware modules.
      * <p>
      * If there is an existing plugin with the same key, the version strings of the existing plugin and the plugin
      * provided to this method will be parsed and compared.  If the installed version is newer than the provided
      * version, it will not be changed.  If the specified plugin's version is the same or newer, the existing plugin
      * state will be saved and the plugin will be unloaded before the provided plugin is installed.  If the existing
      * plugin cannot be unloaded a {@link PluginException} will be thrown.
      *
      * @param loader the loader used to load this plugin
      * @param pluginsToAdd the plugins to add
      * @throws PluginParseException if the plugin cannot be parsed
      * @since 2.0.2
      */
     protected void addPlugins(PluginLoader loader, Collection<Plugin> pluginsToAdd) throws PluginParseException
     {
         final Set<Plugin> pluginsThatShouldBeEnabled = new HashSet<Plugin>();
         for (Plugin plugin : new TreeSet<Plugin>(pluginsToAdd))
         {
             // testing to make sure plugin keys are unique
             if (plugins.containsKey(plugin.getKey()))
             {
                 Plugin existingPlugin = plugins.get(plugin.getKey());
                 if (plugin.compareTo(existingPlugin) >= 0)
                 {
                     try
                     {
                         updatePlugin(existingPlugin, plugin);
                     }
                     catch (PluginException e)
                     {
                         throw new PluginParseException("Duplicate plugin found (installed version is the same or older) and could not be unloaded: '" + plugin.getKey() + "'", e);
                     }
                 }
                 else
                 {
                     // If we find an older plugin, don't error, just ignore it. PLUG-12.
                     if (log.isDebugEnabled())
                         log.debug("Duplicate plugin found (installed version is newer): '" + plugin.getKey() + "'");
                     // and don't install the older plugin
                     continue;
                 }
             }
 
             plugins.put(plugin.getKey(), plugin);
             if (getState().isEnabled(plugin))
             {
                 try
                 {
                     plugin.setEnabled(true);
                     pluginsThatShouldBeEnabled.add(plugin);
                 } catch (RuntimeException ex)
                 {
                     log.error("Unable to enable plugin "+plugin.getKey(), ex);
                 }
             }
 
             pluginToPluginLoader.put(plugin, loader);
         }
 
         if (!plugins.isEmpty())
         {
             // Now try to enable plugins that weren't enabled before, probably due to dependency ordering issues
             WaitUntil.invoke(new WaitUntil.WaitCondition()
             {
                 public boolean isFinished()
                 {
                     for (Iterator<Plugin> i = pluginsThatShouldBeEnabled.iterator(); i.hasNext(); )
                     {
                         Plugin plugin = i.next();
                         if (plugin.isEnabled())
                             i.remove();
                     }
                     return pluginsThatShouldBeEnabled.isEmpty();
                 }
 
                 public String getWaitMessage() {return "Plugins that have yet to start: "+pluginsThatShouldBeEnabled;}
             }, 60);
 
 
             // Disable any plugins that aren't enabled by now
             if (!pluginsThatShouldBeEnabled.isEmpty())
             {
                 StringBuilder sb = new StringBuilder();
                 for (Plugin plugin : pluginsThatShouldBeEnabled)
                 {
                     sb.append(plugin.getKey()).append(',');
                     disablePlugin(plugin.getKey());
                 }
                 sb.deleteCharAt(sb.length() - 1);
                 log.error("Unable to start the following plugins: " + sb.toString());
             }
         }
 
         for (Plugin plugin : pluginsToAdd)
             if (plugin.isEnabled())
                 enablePluginModules(plugin);
 
     }
 
     /**
      * Replace an already loaded plugin with another version. Relevant stored configuration for the plugin will be
      * preserved.
      *
      * @param oldPlugin Plugin to replace
      * @param newPlugin New plugin to install
      * @throws PluginException if the plugin cannot be updated
      */
     protected void updatePlugin(final Plugin oldPlugin, final Plugin newPlugin) throws PluginException
     {
         if (!oldPlugin.getKey().equals(newPlugin.getKey()))
             throw new IllegalArgumentException("New plugin must have the same key as the old plugin");
 
         if (log.isInfoEnabled())
             log.info("Updating plugin '" + oldPlugin + "' to '" + newPlugin + "'");
 
 
         // Preserve the old plugin configuration - uninstall changes it (as disable is called on all modules) and then
         // removes it
         Map<String,Boolean> oldPluginState = getState().getPluginStateMap(oldPlugin);
 
         if (log.isDebugEnabled()) log.debug("Uninstalling old plugin: " + oldPlugin);
         uninstall(oldPlugin);
         if (log.isDebugEnabled()) log.debug("Plugin uninstalled '" + oldPlugin +"', preserving old state");
 
         // Build a set of module keys from the new plugin version
         final Set<String> newModuleKeys = new HashSet<String>();
         newModuleKeys.add(newPlugin.getKey());
 
         for (Iterator moduleIter = newPlugin.getModuleDescriptors().iterator(); moduleIter.hasNext();)
         {
             ModuleDescriptor moduleDescriptor = (ModuleDescriptor) moduleIter.next();
             newModuleKeys.add(moduleDescriptor.getCompleteKey());
         }
 
         // Remove any keys from the old plugin state that do not exist in the new version
         CollectionUtils.filter(oldPluginState.keySet(), new Predicate()
         {
             public boolean evaluate(Object o)
             {
                 return newModuleKeys.contains(o);
             }
         });
 
         // Restore the configuration
         PluginManagerState currentState = getState();
         currentState.getMap().putAll(oldPluginState);
         getStore().savePluginState(currentState);
     }
 
     public Collection<Plugin> getPlugins()
     {
         return plugins.values();
     }
 
     /**
      * @see PluginAccessor#getPlugins(PluginPredicate)
      * @since 0.17
      */
     public Collection<Plugin> getPlugins(final PluginPredicate pluginPredicate)
     {
         return CollectionUtils.select(getPlugins(), new Predicate()
         {
             public boolean evaluate(Object o)
             {
                 return pluginPredicate.matches((Plugin) o);
             }
         });
     }
 
     /**
      * @see PluginAccessor#getEnabledPlugins()
      */
     public Collection<Plugin> getEnabledPlugins()
     {
         return getPlugins(new EnabledPluginPredicate(this));
     }
 
     /**
      * @see PluginAccessor#getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)
      * @since 0.17
      */
     public <T> Collection<T> getModules(final ModuleDescriptorPredicate<T> moduleDescriptorPredicate)
     {
         Collection<ModuleDescriptor<T>> moduleDescriptors = getModuleDescriptors(moduleDescriptorPredicate);
         return getModules(moduleDescriptors);
     }
 
     /**
      * @see PluginAccessor#getModuleDescriptors(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)
      * @since 0.17
      */
     public <T> Collection<ModuleDescriptor<T>> getModuleDescriptors(final ModuleDescriptorPredicate<T> moduleDescriptorPredicate)
     {
         final Collection<ModuleDescriptor<T>> moduleDescriptors = new ArrayList<ModuleDescriptor<T>>();
         for (ModuleDescriptor<?> desc : getModuleDescriptors(getPlugins()))
             moduleDescriptors.add((ModuleDescriptor<T>) desc);
 
         CollectionUtils.filter(moduleDescriptors, new Predicate()
         {
             public boolean evaluate(Object o)
             {
                 return moduleDescriptorPredicate.matches((ModuleDescriptor) o);
             }
         });
         return moduleDescriptors;
     }
 
     /**
      * Get the all the module descriptors from the given collection of plugins
      *
      * @param plugins a collection of {@link Plugin}s
      * @return a collection of {@link ModuleDescriptor}s
      */
     private Collection<ModuleDescriptor<?>> getModuleDescriptors(final Collection<Plugin> plugins)
     {
         final Collection<ModuleDescriptor<?>> moduleDescriptors = new LinkedList<ModuleDescriptor<?>>();
         for (Plugin plugin : plugins)
         {
             moduleDescriptors.addAll(plugin.getModuleDescriptors());
         }
         return moduleDescriptors;
     }
 
     /**
      * Get the modules of all the given descriptor.
      *
      * @param moduleDescriptors the collection of module descriptors to get the modules from.
      * @return a {@link Collection} modules that can be any type of object.
      *         This collection will not contain any null value.
      */
     private <T> List<T> getModules(final Collection<ModuleDescriptor<T>> moduleDescriptors)
     {
         final List<T> result = new ArrayList<T>();
         CollectionUtils.forAllDo(moduleDescriptors, new Closure()
         {
             public void execute(Object o)
             {
                 CollectionUtils.addIgnoreNull(result, ((ModuleDescriptor) o).getModule());
             }
         });
         return result;
     }
 
     public Plugin getPlugin(String key)
     {
         return plugins.get(key);
     }
 
     public Plugin getEnabledPlugin(String pluginKey)
     {
         if (!isPluginEnabled(pluginKey))
             return null;
 
         return getPlugin(pluginKey);
     }
 
     public ModuleDescriptor getPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final Plugin plugin = getPlugin(key.getPluginKey());
 
         if (plugin == null)
             return null;
 
         return plugin.getModuleDescriptor(key.getModuleKey());
     }
 
     public ModuleDescriptor getEnabledPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         // If it's disabled, return null
         if (!isPluginModuleEnabled(completeKey))
             return null;
 
         return getEnabledPlugin(key.getPluginKey()).getModuleDescriptor(key.getModuleKey());
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClass(Class)
      */
     public <T> List<T> getEnabledModulesByClass(final Class<T> moduleClass)
     {
         return getModules(getEnabledModuleDescriptorsByModuleClass(moduleClass));
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClassAndDescriptor(Class[], Class)
      * @deprecated since 0.17, use {@link #getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     public <T> List<T> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<T>>[] descriptorClasses, final Class<T> moduleClass)
     {
         final Collection moduleDescriptors = getEnabledModuleDescriptorsByModuleClass(moduleClass);
         filterModuleDescriptors(moduleDescriptors, new ModuleDescriptorOfClassPredicate(descriptorClasses));
 
         return (List<T>) getModules(moduleDescriptors);
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClassAndDescriptor(Class, Class)
      * @deprecated since 0.17, use {@link #getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     public <T> List<T> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<T>> descriptorClass, final Class<T> moduleClass)
     {
         final Collection moduleDescriptors = getEnabledModuleDescriptorsByModuleClass(moduleClass);
         filterModuleDescriptors(moduleDescriptors, new ModuleDescriptorOfClassPredicate(descriptorClass));
 
         return (List<T>) getModules(moduleDescriptors);
     }
 
     /**
      * Get all module descriptor that are enabled and for which the module is an instance of the given class.
      *
      * @param moduleClass the class of the module within the module descriptor.
      * @return a collection of {@link ModuleDescriptor}s
      */
     private <T> Collection<ModuleDescriptor<T>> getEnabledModuleDescriptorsByModuleClass(final Class<T> moduleClass)
     {
         final Collection<ModuleDescriptor<?>> moduleDescriptors = getModuleDescriptors(getEnabledPlugins());
         filterModuleDescriptors(moduleDescriptors, new ModuleOfClassPredicate(moduleClass));
         filterModuleDescriptors(moduleDescriptors, new EnabledModulePredicate(this));
 
         // silliness to get generics to compile properly
         List<ModuleDescriptor<T>> list = new ArrayList<ModuleDescriptor<T>>();
         for (Object o : moduleDescriptors)
             list.add((ModuleDescriptor<T>) o);
         return list;
     }
 
     public <T extends ModuleDescriptor> List<T> getEnabledModuleDescriptorsByClass(Class<T> moduleDescriptorClass)
     {
         return getEnabledModuleDescriptorsByClass(moduleDescriptorClass, false);
     }
 
     /**
      * This method has been reverted to pre PLUG-40 to fix performance issues that were encountered during
      * load testing. This should be reverted to the state it was in at 54639 when the fundamental issue leading
      * to this slowdown has been corrected (that is, slowness of PluginClassLoader).
      *
      * @see PluginAccessor#getEnabledModuleDescriptorsByClass(Class)
      */
     public <T extends ModuleDescriptor> List<T> getEnabledModuleDescriptorsByClass(Class<T> moduleDescriptorClass, boolean verbose)
     {
         final List<T> result = new LinkedList<T>();
         for (Plugin plugin : plugins.values())
         {
             // Skip disabled plugins
             if (!isPluginEnabled(plugin.getKey()))
             {
                 if (verbose && log.isInfoEnabled())
                 {
                     log.info("Plugin [" + plugin.getKey() + "] is disabled.");
                 }
                 continue;
             }
 
             for (ModuleDescriptor module : plugin.getModuleDescriptors())
             {
                 if (moduleDescriptorClass.isInstance(module) && isPluginModuleEnabled(module.getCompleteKey()))
                 {
                     result.add((T) module);
                 } else
                 {
                     if (verbose && log.isInfoEnabled())
                     {
                         log.info("Module [" + module.getCompleteKey() + "] is disabled.");
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * @see PluginAccessor#getEnabledModuleDescriptorsByType(String)
      * @deprecated since 0.17, use {@link #getModuleDescriptors(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     public List<ModuleDescriptor<?>> getEnabledModuleDescriptorsByType(String type) throws PluginParseException, IllegalArgumentException
     {
         final Collection<ModuleDescriptor<?>> moduleDescriptors = getModuleDescriptors(getEnabledPlugins());
         filterModuleDescriptors(moduleDescriptors, new ModuleDescriptorOfTypePredicate(moduleDescriptorFactory, type));
         filterModuleDescriptors(moduleDescriptors, new EnabledModulePredicate(this));
         return (List<ModuleDescriptor<?>>) moduleDescriptors;
     }
 
     /**
      * Filters out a collection of {@link ModuleDescriptor}s given a predicate.
      *
      * @param moduleDescriptors         the collection of {@link ModuleDescriptor}s to filter.
      * @param moduleDescriptorPredicate the predicate to use for filtering.
      */
     private static void filterModuleDescriptors(final Collection moduleDescriptors, final ModuleDescriptorPredicate moduleDescriptorPredicate)
     {
         CollectionUtils.filter(moduleDescriptors, new Predicate()
         {
             public boolean evaluate(Object o)
             {
                 return moduleDescriptorPredicate.matches((ModuleDescriptor) o);
             }
         });
     }
 
     public void enablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (!plugins.containsKey(key))
         {
             if (log.isInfoEnabled())
                 log.info("No plugin was found for key '" + key + "'. Not enabling.");
 
             return;
         }
 
         Plugin plugin = (Plugin) plugins.get(key);
 
         if (!plugin.getPluginInformation().satisfiesMinJavaVersion())
         {
             log.error("Minimum Java version of '" + plugin.getPluginInformation().getMinJavaVersion() + "' was not satisfied for module '" + key + "'. Not enabling.");
             return;
         }
 
         plugin.setEnabled(true);
 
         // Only change the state if the plugin was enabled successfully
         if (WaitUntil.invoke(new PluginEnabledCondition(plugin)))
         {
             enablePluginState(plugin, getStore());
             notifyPluginEnabled(plugin);
         }
     }
 
     protected void enablePluginState(Plugin plugin, PluginStateStore stateStore)
     {
         PluginManagerState currentState = stateStore.loadPluginState();
         String key = plugin.getKey();
         if (!plugin.isEnabledByDefault())
             currentState.setState(key, Boolean.TRUE);
         else
             currentState.removeState(key);
         stateStore.savePluginState(currentState);
     }
 
     /**
      * Called on all clustered application nodes, rather than {@link #enablePlugin(String)}
      * to just update the local state, state aware modules and loaders, but not affect the
      * global plugin state.
      *
      * @param plugin the plugin being enabled
      */
     protected void notifyPluginEnabled(Plugin plugin)
     {
        plugin.setEnabled(true);
         classLoader.notifyPluginOrModuleEnabled();
         enablePluginModules(plugin);
         pluginEventManager.broadcast(new PluginEnabledEvent(plugin));
     }
 
     /**
      * For each module in the plugin, call the module descriptor's enabled() method if the module is StateAware and enabled.
      *
      * @param plugin the plugin to enable
      */
     private void enablePluginModules(Plugin plugin)
     {
         for (Iterator it = plugin.getModuleDescriptors().iterator(); it.hasNext();)
         {
             ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
 
             if (!(descriptor instanceof StateAware))
             {
                 if (log.isDebugEnabled())
                     log.debug("ModuleDescriptor '" + descriptor.getName() + "' is not StateAware. No need to enable.");
                 continue;
             }
 
             if (!isPluginModuleEnabled(descriptor.getCompleteKey()))
             {
                 if (log.isDebugEnabled())
                     log.debug("Plugin module is disabled, so not enabling ModuleDescriptor '" + descriptor.getName() + "'.");
                 continue;
             }
 
             try
             {
                 if (log.isDebugEnabled())
                     log.debug("Enabling " + descriptor.getKey());
                 ((StateAware) descriptor).enabled();
             }
             catch (Throwable exception) // catch any errors and insert an UnloadablePlugin (PLUG-7)
             {
                 log.error("There was an error loading the descriptor '" + descriptor.getName() + "' of plugin '" + plugin.getKey() + "'. Disabling.", exception);
                 replacePluginWithUnloadablePlugin(plugin, descriptor, exception);
             }
         }
         classLoader.notifyPluginOrModuleEnabled();
     }
 
     public void disablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (!plugins.containsKey(key))
         {
             if (log.isInfoEnabled())
                 log.info("No plugin was found for key '" + key + "'. Not disabling.");
 
             return;
         }
 
         Plugin plugin = (Plugin) plugins.get(key);
 
         notifyPluginDisabled(plugin);
         disablePluginState(plugin, getStore());
     }
 
     protected void disablePluginState(Plugin plugin, PluginStateStore stateStore)
     {
         String key = plugin.getKey();
         PluginManagerState currentState = stateStore.loadPluginState();
         if (plugin.isEnabledByDefault())
             currentState.setState(key, Boolean.FALSE);
         else
             currentState.removeState(key);
         stateStore.savePluginState(currentState);
     }
 
     protected List<String> getEnabledStateAwareModuleKeys(Plugin plugin)
     {
         List<String> keys = new ArrayList<String>();
         List<ModuleDescriptor> moduleDescriptors = new ArrayList<ModuleDescriptor>(plugin.getModuleDescriptors());
         Collections.reverse(moduleDescriptors);
         for (ModuleDescriptor md : moduleDescriptors)
         {
             if (md instanceof StateAware)
             {
                 if (isPluginModuleEnabled(md.getCompleteKey()))
                 {
                     keys.add(md.getCompleteKey());
                 }
             }
         }
         return keys;
     }
 
     protected void notifyPluginDisabled(Plugin plugin)
     {
         List<String> keysToDisable = getEnabledStateAwareModuleKeys(plugin);
 
         for (String key : keysToDisable)
         {
             StateAware descriptor = (StateAware) getPluginModule(key);
             descriptor.disabled();
         }
 
         // This needs to happen after modules are disabled to prevent errors 
         plugin.setEnabled(false);
         pluginEventManager.broadcast(new PluginDisabledEvent(plugin));
     }
 
     public void disablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
 
         if (module == null)
         {
             if (log.isInfoEnabled())
                 log.info("Returned module for key '" + completeKey + "' was null. Not disabling.");
 
             return;
         }
         disablePluginModuleState(module, getStore());
         notifyModuleDisabled(module);
     }
 
     protected void disablePluginModuleState(ModuleDescriptor module, PluginStateStore stateStore)
     {
         String completeKey = module.getCompleteKey();
         PluginManagerState currentState = stateStore.loadPluginState();
         if (module.isEnabledByDefault())
             currentState.setState(completeKey, Boolean.FALSE);
         else
             currentState.removeState(completeKey);
         stateStore.savePluginState(currentState);
     }
 
     protected void notifyModuleDisabled(ModuleDescriptor module)
     {
         if (module instanceof StateAware)
             ((StateAware) module).disabled();
     }
 
     public void enablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
 
         if (module == null)
         {
             if (log.isInfoEnabled())
                 log.info("Returned module for key '" + completeKey + "' was null. Not enabling.");
 
             return;
         }
 
         if (!module.satisfiesMinJavaVersion())
         {
             log.error("Minimum Java version of '" + module.getMinJavaVersion() + "' was not satisfied for module '" + completeKey + "'. Not enabling.");
             return;
         }
         enablePluginModuleState(module, getStore());
         notifyModuleEnabled(module);
     }
 
     protected void enablePluginModuleState(ModuleDescriptor module, PluginStateStore stateStore)
     {
         String completeKey = module.getCompleteKey();
         PluginManagerState currentState = stateStore.loadPluginState();
         if (!module.isEnabledByDefault())
             currentState.setState(completeKey, Boolean.TRUE);
         else
             currentState.removeState(completeKey);
         stateStore.savePluginState(currentState);
     }
 
     protected void notifyModuleEnabled(ModuleDescriptor module)
     {
         classLoader.notifyPluginOrModuleEnabled();
         if (module instanceof StateAware)
             ((StateAware) module).enabled();
     }
 
     public boolean isPluginModuleEnabled(String completeKey)
     {
         // completeKey may be null 
         if (completeKey == null) {
             return false;
         }
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final ModuleDescriptor pluginModule = getPluginModule(completeKey);
         return isPluginEnabled(key.getPluginKey()) && pluginModule != null && getState().isEnabled(pluginModule);
     }
 
     /**
      * This method checks to see if the plugin should be enabled based on the state manager.  It also detects if the
      * state manager state doesn't match the actual plugin state, and if so, disables the plugin
      *
      * @param key The plugin key
      * @return True if the plugin is enabled
      */
     public boolean isPluginEnabled(String key)
     {
         Plugin plugin = plugins.get(key);
 
         boolean shouldBeEnabled = plugin != null && getState().isEnabled(plugin);
 
         // Detect if the plugin state is out of sync with the plugin state store
         if (shouldBeEnabled && !plugin.isEnabled())
         {
             log.warn("Plugin "+key+" is out of sync with the plugin system, disabling");
             // Just changing the state in the store to prevent a stack overflow as disabling modules requires the
             // plugin to be enabled, thus calling this method in a infinite loop.
             disablePluginState(plugin, getStore());
             shouldBeEnabled = false;
         }
         return shouldBeEnabled;
     }
 
     public InputStream getDynamicResourceAsStream(String name)
     {
         return getClassLoader().getResourceAsStream(name);
     }
 
     public Class getDynamicPluginClass(String className) throws ClassNotFoundException
     {
         return getClassLoader().loadClass(className);
     }
 
     public ClassLoader getClassLoader()
     {
         return classLoader;
     }
 
     public InputStream getPluginResourceAsStream(String pluginKey, String resourcePath)
     {
         Plugin plugin = getEnabledPlugin(pluginKey);
         if (plugin == null)
         {
             log.error("Attempted to retreive resource " + resourcePath + " for non-existent or inactive plugin " + pluginKey);
             return null;
         }
 
         return plugin.getResourceAsStream(resourcePath);
     }
 
     /**
      * Disables and replaces a plugin currently loaded with an UnloadablePlugin.
      *
      * @param plugin     the plugin to be replaced
      * @param descriptor the descriptor which caused the problem
      * @param throwable  the problem caught when enabling the descriptor
      * @return the UnloadablePlugin which replaced the broken plugin
      */
     private UnloadablePlugin replacePluginWithUnloadablePlugin(Plugin plugin, ModuleDescriptor descriptor, Throwable throwable)
     {
         UnloadableModuleDescriptor unloadableDescriptor =
                 UnloadableModuleDescriptorFactory.createUnloadableModuleDescriptor(plugin, descriptor, throwable);
         UnloadablePlugin unloadablePlugin = UnloadablePluginFactory.createUnloadablePlugin(plugin, unloadableDescriptor);
 
         unloadablePlugin.setUninstallable(plugin.isUninstallable());
         unloadablePlugin.setDeletable(plugin.isDeleteable());
         plugins.put(plugin.getKey(), unloadablePlugin);
 
         // Disable it
         disablePluginState(plugin, getStore());
         return unloadablePlugin;
     }
 
     public boolean isSystemPlugin(String key)
     {
         Plugin plugin = getPlugin(key);
         return plugin != null && plugin.isSystemPlugin();
     }
 
     /**
      * @deprecated Since 2.0.0.beta2
      */
     public void setDescriptorParserFactory(DescriptorParserFactory descriptorParserFactory)
     {
     }
 
     private static class PluginEnabledCondition implements WaitUntil.WaitCondition
     {
         private final Plugin plugin;
 
         public PluginEnabledCondition(Plugin plugin)
         {
             this.plugin = plugin;
         }
 
         public boolean isFinished()
         {
             return plugin.isEnabled();
         }
 
         public String getWaitMessage()
         {
             return "Waiting until plugin " + plugin + " is enabled";
         }
     }
 }
