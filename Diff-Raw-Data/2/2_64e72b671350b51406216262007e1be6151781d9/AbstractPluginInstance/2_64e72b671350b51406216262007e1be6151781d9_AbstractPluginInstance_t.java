 package ch9k.plugins;
 
 import ch9k.chat.Conversation;
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.core.settings.Settings;
 import ch9k.core.settings.SettingsChangeEvent;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.NetworkEvent;
 import ch9k.eventpool.EventPool;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventFilter;
 import ch9k.plugins.event.RemotePluginSettingsChangeEvent;
 
 /**
  * Class that can serve as a base class for a simple plugin instance.
  * @author Jasper Van der Jeugt
  */
 public abstract class AbstractPluginInstance implements EventListener {
     /**
      * The plugin.
      */
     private Plugin plugin;
 
     /**
      * The conversation we are bound to.
      */
     private Conversation conversation;
 
     /**
      * The current settings.
      */
     private Settings settings;
 
     /**
      * Constructor.
      * @param plugin The corresponding plugin.
      * @param conversation Conversation to bind the plugin to.
      * @param settings Local plugin settings.
      */
     public AbstractPluginInstance(Plugin plugin,
             Conversation conversation, Settings settings) {
         this.plugin = plugin;
         this.conversation = conversation;
         this.settings = settings;
 
         /* We want to hear of remote changes, so we can adapt. */
         EventFilter filter = new ConversationEventFilter(
                 RemotePluginSettingsChangeEvent.class, getConversation());
         EventPool.getAppPool().addListener(this, filter);
     }
 
     /**
      * Get the conversation this plugin is coupled with.
      * @return The coupled conversation.
      */
     public Conversation getConversation() {
         return conversation;
     }
 
     /**
      * Get the current local settings.
      * @return The local settings for this instance.
      */
     public Settings getSettings() {
         return settings;
     }
 
     /**
      * Called when the plugin instance is started.
      */
     public abstract void enablePluginInstance();
 
     /**
      * Called when the plugin instance is disabled.
      */
     public abstract void disablePluginInstance();
 
     @Override
     public void handleEvent(Event e) {
         /* The remote settings changed. We need to listen to them if we did not
          * init the conversation. */
         if(e instanceof RemotePluginSettingsChangeEvent &&
                 !conversation.isInitiatedByMe()) {
             RemotePluginSettingsChangeEvent event =
                     (RemotePluginSettingsChangeEvent) e;
 
             /* Check that we're dealing with the correct plugin. */
            if(!event.getPlugin().equals(plugin.getClass().getName())) return;
 
             /* Propogate the changes. */
             SettingsChangeEvent changeEvent = event.getChangeEvent();
             settings.set(changeEvent.getKey(), changeEvent.getValue());
             System.out.println("Adapted local settings.");
         }
     }
 }
