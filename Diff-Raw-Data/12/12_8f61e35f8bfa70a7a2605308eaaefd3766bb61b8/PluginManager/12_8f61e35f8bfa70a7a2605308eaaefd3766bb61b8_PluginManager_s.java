 package ch9k.plugins;
 
 import java.io.File;
 import ch9k.chat.Conversation;
 import ch9k.core.ChatApplication;
import ch9k.chat.ConversationManager;
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import ch9k.core.Model;
 import ch9k.core.settings.Settings;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.core.settings.event.PreferencePaneEvent;
 import ch9k.plugins.event.PluginChangeEvent;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 import javax.swing.JPanel;
 import ch9k.core.I18n;
 
 /**
  * A singleton to manage plugins.
  * @author Jasper Van der Jeugt
  */
 public class PluginManager extends Model implements EventListener, Persistable {
     /**
      * Logger.
      */
     private static final Logger logger = Logger.getLogger(PluginManager.class);
 
     /**
      * We keep the plugins by name.
      */
     private Map<String, Plugin> plugins;
 
     /**
      * We keep the filename for every plugin as well.
      */
     private Map<String, String> fileNames;
 
     /**
      * A plugin installer.
      */
     private PluginInstaller installer;
 
     /**
      * Constructor.
      */
     public PluginManager() {
         plugins = new HashMap<String, Plugin>();
         fileNames = new HashMap<String, String>();
         installer = new PluginInstaller(this);
         installer.loadInstalledPlugins();
 
         /* Some plugins are always available, because they ship with the
          * application. */
         addAvailablePlugin("ch9k.plugins.carousel.CarouselPlugin", null);
         addAvailablePlugin(
                 "ch9k.plugins.flickr.FlickrImageProviderPlugin", null);
         addAvailablePlugin(
                 "ch9k.plugins.liteanalyzer.LiteTextAnalyzerPlugin", null);
 
         /* Register as listener. We will listen to remote enable/disable plugin
          * events, so we can synchronize with the plugin manager on the other
          * side. */
         EventFilter filter = new EventFilter(PluginChangeEvent.class);
         EventPool.getAppPool().addListener(this, filter);
 
         /* Throw our preference pane so the user can install more plugins */
         JPanel preferencePane = new PluginPreferencePane(this);
         Event event = new PreferencePaneEvent(
                 I18n.get("ch9k.plugins", "plugins") , preferencePane);
         EventPool.getAppPool().raiseEvent(event);
     }
 
     /**
      * Constructor.
      * @param pdo PersistentDataObject to load settings from.
      */
     public PluginManager(PersistentDataObject pdo) {
         this();
         load(pdo);
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
      * @param fileName Filename of the plugin.
      */
     public synchronized void addAvailablePlugin(String name, String fileName) {
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
         if(fileName != null) {
             fileNames.put(name, fileName);
         }
         fireStateChanged();
     }
 
     /**
      * Soft remove a plugin.
      * @param name Name of the plugin to soft-remove.
      */
     public void softRemovePlugin(String name) {
         /* Obtain the plugin. */
         Plugin plugin = plugins.get(name);
         if(plugin == null) {
             return;
         }
 
         /* Disable the plugin for every conversation. */
         for(Conversation conversation:
                 ChatApplication.getInstance().getConversationManager()) {
             disablePlugin(name, conversation);
         }
 
         /* Remove the plugin from the lists. */
         plugins.remove(name);
 
         /* Actually remove the file. */
         String fileName = fileNames.get(name);
         if(fileName != null) {
             File file = new File(fileName);
             file.delete();
         }
 
         plugin.softRemove();
 
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
      * Get the file name for a given plugin. This function will return null if
      * the plugin was not loaded from a file.
      * @param name Name of the plugin to find the file for.
      * @return The file name of the plugin file.
      */
     public String getPluginFileName(String name) {
         return fileNames.get(name);
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
 
     @Override
     public PersistentDataObject persist() {
         Element pluginManager = new Element("pluginManager");
         for(String name: plugins.keySet()) {
             Plugin plugin = plugins.get(name);
             /* Persist only the settings. */
             Settings settings = plugin.getSettings();
             if(settings != null) {
                 PersistentDataObject pdo = settings.persist();
                 /* Get the element. */
                 Element element = pdo.getElement();
                 /* Set the correct name. */
                 element.setAttribute("plugin", name);
                 /* Add it as a child. */
                 pluginManager.addContent(element);
             }
         }
 
         return new PersistentDataObject(pluginManager);
     }
 
     @Override
     public void load(PersistentDataObject pdo) {
         for(Object object : pdo.getElement().getChildren()) {
             Element child = (Element) object;
 
             /* Find the name of the plugin and get the corresponding plugin. */
             String name = child.getAttributeValue("plugin");
             Plugin plugin = plugins.get(name);
 
             if(plugin != null) {
                 /* load the plugin settings from the XML. */
                 Settings settings = plugin.getSettings();
                 settings.load(new PersistentDataObject(child));
             }
         }
     }
 }
