 package com.atlassian.plugin.loaders;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.factories.PluginFactory;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.loaders.classloading.DeploymentUnit;
 import com.atlassian.plugin.loaders.classloading.Scanner;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.Validate;
 
 import java.io.File;
 import java.util.*;
 
 /**
  * A plugin loader to load plugins from a directory on disk.  A {@link Scanner} is used to locate plugin artifacts
  * and determine if they need to be redeployed or not.
  */
 public class DirectoryPluginLoader implements DynamicPluginLoader
 {
     private static Log log = LogFactory.getLog(DirectoryPluginLoader.class);
     private final Scanner scanner;
     /** Maps {@link DeploymentUnit}s to {@link Plugin}s. */
     private final Map plugins;
     private final List/*PluginFactory*/ pluginFactories;
 
     /**
      * Constructs a loader for a particular directory and the default set of deployers
      * @param path The directory containing the plugins
      * @param pluginEventManager The event manager, used for listening for shutdown events
      * @since 2.0.0
      */
     public DirectoryPluginLoader(File path, PluginEventManager pluginEventManager)
     {
         this(path, Collections.singletonList(new LegacyDynamicPluginFactory(PluginManager.PLUGIN_DESCRIPTOR_FILENAME)), pluginEventManager);
     }
 
     /**
      * Constructs a loader for a particular directory and set of deployers
      * @param path The directory containing the plugins
      * @param pluginFactories The deployers that will handle turning an artifact into a plugin
      * @param pluginEventManager The event manager, used for listening for shutdown events
      * @since 2.0.0
      */
     public DirectoryPluginLoader(File path, List pluginFactories,
                                  PluginEventManager pluginEventManager)
     {
         if (log.isDebugEnabled())
             log.debug("Creating plugin loader for url " + path);
 
         Validate.notNull(path, "The directory file must be specified");
         Validate.notNull(pluginFactories, "The list of plugin factories must be specified");
         Validate.notNull(pluginEventManager, "The event manager must be specified");
 
         scanner = new Scanner(path);
         plugins = new HashMap();
         this.pluginFactories = new ArrayList(pluginFactories);
         pluginEventManager.register(this);
     }
 
     public Collection loadAllPlugins(ModuleDescriptorFactory moduleDescriptorFactory)
     {
         scanner.scan();
 
         for (Iterator iterator = scanner.getDeploymentUnits().iterator(); iterator.hasNext();)
         {
             DeploymentUnit deploymentUnit = (DeploymentUnit) iterator.next();
             try
             {
                 Plugin plugin = deployPluginFromUnit(deploymentUnit, moduleDescriptorFactory);
                 plugins.put(deploymentUnit, plugin);
             }
             catch (PluginParseException e)
             {
                 // This catches errors so that the successfully loaded plugins can be returned.
                 // It might be nicer if this method returned an object containing both the succesfully loaded
                 // plugins and the unsuccessfully loaded plugins.
                 log.error("Error loading descriptor for : " + deploymentUnit, e);
             }
         }
 
         return plugins.values();
     }
 
 
     protected Plugin deployPluginFromUnit(DeploymentUnit deploymentUnit, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         Plugin plugin = null;
         for (Iterator i = pluginFactories.iterator(); i.hasNext(); )
         {
             PluginFactory factory = (PluginFactory) i.next();
             if (factory.canCreate(new JarPluginArtifact(deploymentUnit.getPath())) != null)
             {
                 plugin = factory.create(deploymentUnit, moduleDescriptorFactory);
                 if (plugin != null)
                     break;
             }
         }
         if (plugin == null)
             plugin = new UnloadablePlugin("No plugin deployers found for this plugin");
         return plugin;
     }
 
     public boolean supportsRemoval()
     {
         return true;
     }
 
     public boolean supportsAddition()
     {
         return true;
     }
 
     /**
      * @return all plugins, now loaded by the pluginLoader, which have been discovered and added since the
      * last time a check was performed.
      */
     public Collection addFoundPlugins(ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         // find missing plugins
         Collection updatedDeploymentUnits = scanner.scan();
 
         // create list while updating internal state
         List foundPlugins = new ArrayList();
         for (Iterator it = updatedDeploymentUnits.iterator(); it.hasNext();)
         {
             DeploymentUnit deploymentUnit = (DeploymentUnit) it.next();
             if (!plugins.containsKey(deploymentUnit))
             {
                 Plugin plugin = deployPluginFromUnit(deploymentUnit, moduleDescriptorFactory);
                 plugins.put(deploymentUnit, plugin);
                 foundPlugins.add(plugin);
             }
         }
 
         return foundPlugins;
     }
 
     /**
      * @param plugin - the plugin to remove
      * @throws PluginException representing the reason for failure.
      */
     public void removePlugin(Plugin plugin) throws PluginException
     {
         if (plugin.isEnabled())
             throw new PluginException("Cannot remove an enabled plugin");
 
         if (!plugin.isUninstallable())
         {
             throw new PluginException("Cannot remove an uninstallable plugin: [" + plugin.getName() + "]" );
         }
 
         DeploymentUnit deploymentUnit = findMatchingDeploymentUnit(plugin);
         File pluginOnDisk = deploymentUnit.getPath();
         plugin.close();
 
         try
         {
             boolean found = false;
            for (Iterator i = plugins.keySet().iterator(); i.hasNext();)
             {
                 DeploymentUnit unit = (DeploymentUnit) i.next();
                 if(unit.getPath().equals(deploymentUnit.getPath()) && !unit.equals(deploymentUnit))
                     found = true;
             }
 
             if (!found && !pluginOnDisk.delete())
                 throw new PluginException("Could not delete plugin [" + plugin.getName() + "].");
         }
         catch (SecurityException e)
         {
             throw new PluginException(e);
         }
 
         scanner.clear(pluginOnDisk);
         plugins.remove(deploymentUnit);
     }
 
     private DeploymentUnit findMatchingDeploymentUnit(Plugin plugin)
             throws PluginException
     {
         DeploymentUnit deploymentUnit = null;
         for (Iterator iterator = plugins.entrySet().iterator(); iterator.hasNext();)
         {
             Map.Entry entry = (Map.Entry) iterator.next();
             if (entry.getValue() == plugin)
                 deploymentUnit = (DeploymentUnit) entry.getKey();
         }
 
         if (deploymentUnit == null) //the pluginLoader has no memory of deploying this plugin
             throw new PluginException("This pluginLoader has no memory of deploying the plugin you are trying remove: [" + plugin.getName() + "]" );
         return deploymentUnit;
     }
 
     /**
      * Called during plugin framework shutdown
      * @param event The shutdown event
      */
     public void channel(PluginFrameworkShutdownEvent event)
     {
         scanner.clearAll();
         for (Iterator it = plugins.values().iterator(); it.hasNext();)
         {
             Plugin plugin  = (Plugin) it.next();
             plugin.close();
             it.remove();
         }
     }
 
     /**
      * @deprecated Since 2.0.0, shutdown will automatically occur when the plugin framework is shutdown
      */
     public void shutDown()
     {
         channel(null);
     }
 
     /**
      * Determines if the artifact can be loaded by any of its deployers
      *
      * @param pluginArtifact The artifact to test
      * @return True if this artifact can be loaded by this loader
      * @throws PluginParseException
      */
     public String canLoad(PluginArtifact pluginArtifact) throws PluginParseException
     {
         String pluginKey = null;
         for (Iterator i = pluginFactories.iterator(); i.hasNext(); )
         {
             PluginFactory factory = (PluginFactory) i.next();
             pluginKey = factory.canCreate(pluginArtifact);
             if (pluginKey != null)
                 break;
         }
         return pluginKey;
     }
 }
