 package ch9k.plugins;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URL;
 import ch9k.chat.Conversation;
 
 /**
  * A singleton to manage plugins.
  * @author Jasper Van der Jeugt
  */
 public class PluginManager {
     /**
      * We keep, for every conversation, a list of activated plugins by name.
      */
     private Map<Conversation, Set<String>> enabledPlugins;
 
     /**
      * In addition, we keep for every conversation the list of actual plugin
      * instances.
      */
     private Map<Conversation, List<Plugin>> plugins;
 
     /**
      * A list of available plugins.
      */
     private List<String> availablePlugins;
 
     /**
      * A plugin installer.
      */
     private PluginInstaller installer;
 
     /**
      * Constructor.
      */
     public PluginManager() {
         availablePlugins = new ArrayList<String>();
         installer = new PluginInstaller(this);
     }
 
     /**
      * Get a list of available plugins.
      * @return A list of available plugins.
      */
     public List<String> getAvailablePlugins() {
         return availablePlugins;
     }
 
     /**
      * Add an available plugin to the list.
     * @param String Class name of the plugin to add.
      */
     public void addAvailablePlugin(String name) {
         availablePlugins.add(name);
     }
 
     /**
      * Enable a plugin for a given conversation.
      * @param conversation Conversation to enable the plugin for.
      * @param name Name of the plugin to load.
      */
     public void enablePlugin(Conversation conversation, String name) {
         /* Check that the plugin is not already enabled for the conversation. */
         Set<String> conversationPlugins = enabledPlugins.get(conversation);
         if(conversationPlugins != null && conversationPlugins.contains(name)) {
             return;
         }
 
         /* Find the class of the new plugin and initialize it. */
         Plugin plugin;
         try {
             Class pluginClass = installer.getPluginClass(name);
             plugin = (Plugin) pluginClass.newInstance();
         } catch (ClassNotFoundException exception) {
             // TODO: Show relevant warning.
             return;
         } catch (InstantiationException exception) {
             // TODO: Show relevant warning.
             return;
         } catch (IllegalAccessException exception) {
             // TODO: Show relevant warning.
             return;
         }
 
         /* Couple it with the conversation. */
         plugin.bind(conversation);
 
         /* Register plugin. */
         if(conversationPlugins == null) {
             conversationPlugins = new HashSet<String>();
             List<Plugin> tmp = new ArrayList<Plugin>();
             plugins.put(conversation, tmp);
         }
         conversationPlugins.add(name);
         plugins.get(conversation).add(plugin); 
     }
 
     /**
      * Disable a plugin for a given conversation.
      * @param conversation Conversation to disable the plugin for.
      * @param name Name of the plugin to disable.
      */
     public void disablePlugin(Conversation conversation, String name) {
         Set<String> names = enabledPlugins.get(conversation);
         List<Plugin> instances = plugins.get(conversation);
 
         if(names != null && names.contains(name)) {
             names.remove(name);
             for(Plugin plugin: instances) {
                 if(plugin.getClass().getName().equals(name)) {
                     plugin.unbind(conversation);
                     instances.remove(plugin);
                 }
             }
         }
     }
 
     /**
      * Access the plugin installer.
      * @return The plugin installer.
      */
     public PluginInstaller getPluginInstaller() {
         return installer;
     }
 }
