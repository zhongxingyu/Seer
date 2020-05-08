 package be.thinkjavache.snitch;
 
 import ch9k.chat.Conversation;
 import ch9k.core.settings.Settings;
 import ch9k.plugins.AbstractPlugin;
 import ch9k.plugins.AbstractPluginInstance;
 import ch9k.plugins.Plugin;
 import javax.swing.JPanel;
 
 /**
  * The snitch plugin will tell on you!
  * @author Pieter De Baets
  */
 public class SnitchPlugin extends AbstractPlugin {
     @Override
     protected AbstractPluginInstance createPluginInstance(Plugin plugin,
             Conversation conversation, Settings settings) {
         return new Snitch(plugin, conversation, settings);
     }
 
     @Override
     protected JPanel createPreferencePane(Settings settings) {
         return null;
     }
 
     @Override
     protected Settings createDefaultSettings() {
         return new Settings();
     }
 }
