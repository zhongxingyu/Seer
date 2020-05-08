 package org.wickedsource.hooked.svn;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.wickedsource.hooked.plugins.api.Plugin;
 import org.wickedsource.hooked.plugins.api.collector.CollectorPlugin;
 import org.wickedsource.hooked.plugins.api.notifier.NotifierPlugin;
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.ServiceLoader;
 import java.util.Set;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public enum PluginRegistry {
 
     INSTANCE;
 
     private final Logger logger;
 
     private Set<CollectorPlugin> collectorPlugins;
 
     private Set<NotifierPlugin> notifierPlugins;
 
     private PluginPropertiesLoader pluginPropertiesLoader = new PluginPropertiesLoader();
 
     private PluginRegistry() {
         logger = LoggerFactory.getLogger(PluginRegistry.class);
         getCollectorPlugins();
         getNotifierPlugins();
     }
 
     public synchronized Set<CollectorPlugin> getCollectorPlugins() {
         if (this.collectorPlugins == null) {
             logger.debug("Loading collector plugins...");
             ServiceLoader<CollectorPlugin> loader = ServiceLoader.load(CollectorPlugin.class);
             Set<CollectorPlugin> plugins = new HashSet<>();
             for (CollectorPlugin plugin : loader) {
                 Properties properties = pluginPropertiesLoader.loadPluginProperties(plugin.getClass());
                 plugin.init(properties);
                 plugins.add(plugin);
             }
             this.collectorPlugins = plugins;
             logLoadedPlugins(this.collectorPlugins);
         }
         return this.collectorPlugins;
     }
 
     public synchronized Set<NotifierPlugin> getNotifierPlugins() {
         if (this.notifierPlugins == null) {
             logger.debug("Loading notifier plugins...");
             ServiceLoader<NotifierPlugin> loader = ServiceLoader.load(NotifierPlugin.class);
             Set<NotifierPlugin> plugins = new HashSet<>();
             for (NotifierPlugin plugin : loader) {
                 Properties properties = pluginPropertiesLoader.loadPluginProperties(plugin.getClass());
                 plugin.init(properties);
                 plugins.add(plugin);
             }
             this.notifierPlugins = plugins;
            logLoadedPlugins(this.collectorPlugins);
         }
         return this.notifierPlugins;
     }
 
     private void logLoadedPlugins(Set<? extends Plugin> plugins) {
         logger.debug(String.format("Loaded %d plugin(s):", plugins.size()));
         for (Plugin plugin : plugins) {
             logger.trace(plugin.getClass().getName());
         }
     }
 
 }
