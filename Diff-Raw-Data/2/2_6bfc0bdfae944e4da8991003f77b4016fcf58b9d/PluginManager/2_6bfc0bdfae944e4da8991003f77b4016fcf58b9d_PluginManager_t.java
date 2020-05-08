 package ch9k.plugins;
 
 import ch9k.chat.Conversation;
 import ch9k.core.Model;
 import ch9k.core.settings.Settings;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.event.PluginChangeEvent;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 
 /**
  * A singleton to manage plugins.
  * @author Jasper Van der Jeugt
  */
 public class PluginManager extends Model implements EventListener {
     /**
      * Logger.
      */
     private static final Logger logger = Logger.getLogger(PluginManager.class);
 
     /**
      * We keep the plugins by name.
      */
     private Map<String, Plugin> plugins;
 
     /**
      * A plugin installer.
      */
     private PluginInstaller installer;
 
     /**
      * Constructor.
      */
     public PluginManager() {
         plugins = new HashMap<String, Plugin>();
         installer = new PluginInstaller(this);
         installer.loadInstalledPlugins();
 
         /* Some plugins are always available, because they ship with the
          * application. */
         addAvailablePlugin("ch9k.plugins.carousel.CarouselPlugin");
         addAvailablePlugin("ch9k.plugins.flickr.FlickrImageProviderPlugin");
         addAvailablePlugin("ch9k.plugins.liteanalyzer.LiteTextAnalyzerPlugin");
 
         /* Register as listener. We will listen to remote enable/disable plugin
          * events, so we can synchronize with the plugin manager on the other
          * side. */
         EventFilter filter = new EventFilter(PluginChangeEvent.class);
         EventPool.getAppPool().addListener(this, filter);
     }
 
     /**
      * Get a map of pretty names for the plugins. This map binds pretty names
      * to actual plugin names.
      * @return A map of pretty names for the plugins.
      */
     public Map<String, String> getPrettyNames() {
         Map<String, String> map = new HashMap<String, String>();
         for(String plugin: plugins.keySet()) {
             map.put(plugins.get(plugin).getPrettyName(), plugin);
         }
 
         return map;
     }
 
     /**
      * Add an available plugin to the list.
      * @param name Class name of the plugin to add.
      */
     public synchronized void addAvailablePlugin(String name) {
         /* Check that we don't have it already. */
         if(plugins.get(name) != null) return;
 
         /* Find the class of the new plugin. */
         Class<?> pluginClass = null;
         try {
             pluginClass = installer.getPluginClass(name);
         } catch (ClassNotFoundException exception) {
             /* Try all stuff in the classpath now. */
             try {
                 pluginClass = Class.forName(name);
             } catch (ClassNotFoundException e) {
                 /* Should not happen, because we registered it earlier. */
                 logger.warn("Class not found: " + name);
                 return;
             }
         }
 
         /* Initialize the plugin. */
         Plugin plugin = null;
         try {
             plugin = (Plugin) pluginClass.newInstance();
         } catch (InstantiationException exception) {
             logger.warn("Could not instantiate " + name + ": " + exception);
             return;
         } catch (IllegalAccessException exception) {
             logger.warn("Could not access " + name + ": " + exception);
             return;
         }
 
         plugins.put(name, plugin);
         fireStateChanged();
     }
 
     /**
      * Check if a given plugin is enabled for a given conversation.
      * @param name Name of the plugin to check.
      * @param conversation Conversation to check.
      * @return If the given plugin is enabled for the given conversation.
      */
     public boolean isEnabled(String name, Conversation conversation) {
         Plugin plugin = plugins.get(name);
         if(plugin == null) {
             return false;
         } else {
             return plugin.isEnabled(conversation);
         }
     }
 
     /**
      * Enable a plugin for a given conversation.
      * @param name Name of the plugin to load.
      * @param conversation Conversation to enable the plugin for.
      */
     public void enablePlugin(String name, Conversation conversation) {
         /* We are enabling this plugin, so we use our settings. */
         Plugin plugin = plugins.get(name);
         if(plugin == null) return;
         Settings settings = plugin.getSettings();
 
         if(enable(name, conversation, settings)) {
             /* Throw an event. */
             PluginChangeEvent event =
                     new PluginChangeEvent(conversation, name, true, settings);
             EventPool.getAppPool().raiseNetworkEvent(event);
         }
     }
 
     /**
      * Enable a plugin for a given conversation.
      * @param name Name of the plugin to load.
      * @param conversation Conversation to enable the plugin for.
      * @param settings Settings for the plugin.
      * @return If the operation was succesful.
      */
     private synchronized boolean enable(
             String name, Conversation conversation, Settings settings) {
         /* Retrieve the plugin. */
         Plugin plugin = plugins.get(name);
 
         /* Check that the plugin is not already enabled for the conversation. */
         if(plugin == null || plugin.isEnabled(conversation)) {
             return false;
         }
 
         /* Couple it with the conversation. */
         plugin.enablePlugin(conversation, settings);
 
         return true;
     }
 
     /**
      * Disable a plugin for a given conversation.
      * @param name Name of the plugin to disable.
      * @param conversation Conversation to disable the plugin for.
      */
     public synchronized void disablePlugin(
             String name, Conversation conversation) {
         if(disable(name, conversation)) {
             /* Throw an event. */
             PluginChangeEvent event =
                     new PluginChangeEvent(conversation, name, false, null);
             EventPool.getAppPool().raiseNetworkEvent(event);
         }
     }
 
     /**
      * Disable a plugin for a given conversation.
      * @param name Name of the plugin to disable.
      * @param conversation Conversation to disable the plugin for.
      * @return If the action was succesful.
      */
     private synchronized boolean disable(
             String name, Conversation conversation) {
        Plugin plugin = plugins.get(name);
         if(plugin == null || !plugin.isEnabled(conversation)) {
             return false;
         }
 
         plugin.disablePlugin(conversation);
 
         return true;
     }
 
     /**
      * Access the plugin installer.
      * @return The plugin installer.
      */
     public PluginInstaller getPluginInstaller() {
         return installer;
     }
 
     @Override
     public void handleEvent(Event e) {
         PluginChangeEvent event = (PluginChangeEvent) e;
         /* A plugin was enabled. */
         if(event.isPluginEnabled()) {
             /* If the event was external, enable the plugin here as well. */
             if(event.isExternal()) {
                 enable(event.getPlugin(), event.getConversation(),
                         event.getSettings());
             }
         /* A plugin was disabled. */
         } else {
             /* If the event was external, disable the plugin here as well. */
             if(event.isExternal()) {
                 disable(event.getPlugin(), event.getConversation());
             }
         }
     }
 }
