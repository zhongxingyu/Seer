
 package be.thinkjavache.snitch;
 
 import ch9k.chat.ChatMessage;
 import ch9k.chat.Conversation;
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.chat.event.NewChatMessageEvent;
 import ch9k.chat.event.NewConversationSubjectEvent;
 import ch9k.core.I18n;
 import ch9k.core.settings.Settings;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.AbstractPluginInstance;
 import ch9k.plugins.Plugin;
 import ch9k.plugins.event.PluginChangeEvent;
 
 /**
  * Will show all your little conversation secrets
  * @author Pieter De Baets
  */
 public class Snitch extends AbstractPluginInstance {
     public Snitch(Plugin plugin, Conversation conversation, Settings settings) {
         super(plugin, conversation, settings);
     }
 
     @Override
     public void enablePluginInstance() {
         EventPool.getAppPool().addListener(this,
                 new ConversationEventFilter(getConversation()));
 
         ChatMessage chatMessage = new ChatMessage("Snitch", I18n.get(
                 "be.thinkjavache.snitch", "enabled"), true);
         EventPool.getAppPool().raiseNetworkEvent(new NewChatMessageEvent(
                 getConversation(), chatMessage));
     }
 
     @Override
     public void disablePluginInstance() {
         EventPool.getAppPool().removeListener(this);
     }
 
     @Override
     public void handleEvent(Event e) {
         super.handleEvent(e);
 
         if(e instanceof NewConversationSubjectEvent) {
            informnConversationSubject((NewConversationSubjectEvent)e);
         }
         if(e instanceof PluginChangeEvent) {
             informPluginEvent((PluginChangeEvent)e);
         }
     }
 
    private void informnConversationSubject(NewConversationSubjectEvent event) {
         String[] subjects = event.getConversationSubject().getSubjects();
 
         StringBuilder message = new StringBuilder();
         for(int i = 0; i < subjects.length; i++) {
             if(i > 0) {
                 message.append(' ');
             }
             message.append(subjects[i]);
         }
 
         ChatMessage chatMessage = new ChatMessage("Snitch", I18n.get(
                 "be.thinkjavache.snitch", "new_subject", message), true);
         EventPool.getAppPool().raiseNetworkEvent(new NewChatMessageEvent(
                 getConversation(), chatMessage));
     }
     
     private void informPluginEvent(PluginChangeEvent event) {
         String message = "plugin_enabled";
         if(!event.isPluginEnabled()) {
             message = "plugin_disabled";
         }
 
         ChatMessage chatMessage = new ChatMessage("Snitch", I18n.get(
                 "be.thinkjavache.snitch", message, event.getPlugin()), true);
         EventPool.getAppPool().raiseNetworkEvent(new NewChatMessageEvent(
                 getConversation(), chatMessage));
     }
 }
