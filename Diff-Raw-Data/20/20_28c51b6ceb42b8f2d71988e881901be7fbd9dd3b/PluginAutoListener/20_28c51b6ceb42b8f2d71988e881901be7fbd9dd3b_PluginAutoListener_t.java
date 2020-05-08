 package bot.framework.components.groovy;
 
 import bot.framework.components.container.StartupListener;
 import bot.framework.components.skype.MessageReceivedListener;
 import com.skype.ChatMessage;
 import com.skype.ChatMessageListener;
 import com.skype.SkypeException;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jaroslaw Koscinski
  * Date: 02.10.2013
  * Time: 01:42
  */
 public class PluginAutoListener implements StartupListener, ChatMessageListener {
 
     Logger logger = Logger.getLogger(PluginAutoListener.class);
 
     List<PluginContainer> skypeListeners = new LinkedList<PluginContainer>();
    //List<String> chatMessageList = new LinkedList<String>();
 
     public void registerListener(PluginContainer plugin) {
 
         if(Arrays.asList(plugin.getPluginClass().getInterfaces()).contains(MessageReceivedListener.class)) {
             skypeListeners.add(plugin);
             logger.info("Registered skype listener for "+plugin.getPluginClass().getName());
         }
 
     }
 
     public void unregisterListener(PluginContainer plugin) {
         if(skypeListeners.contains(plugin)) {
             skypeListeners.remove(plugin);
             logger.info("Unregistered skype listener for "+plugin.getPluginClass().getName());
         }
     }
 
     @Override
     public void start() {
         try {
             com.skype.Skype.addChatMessageListener(this);
         } catch (SkypeException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void stop() {
         for (PluginContainer plugin : skypeListeners) {
             com.skype.Skype.removeChatMessageListener(this);
         }
     }
 
     @Override
     public void chatMessageReceived(ChatMessage chatMessage) throws SkypeException {
         logger.info("Message received with id:"+chatMessage.getId()+", in chat:"+chatMessage.getChat().getId()+", with status:"+chatMessage.getStatus().toString()+", with content:\""+chatMessage.getContent()+"\"");
        /*
         String uniqueId = chatMessage.getChat().getId()+chatMessage.getId();
 
         if(chatMessage.getStatus()==ChatMessage.Status.READ && chatMessageList.contains(uniqueId)) {
             chatMessageList.remove(uniqueId);
             logger.info("Listeners will not be notified, already were.");
             return;
         } else if(chatMessage.getStatus()==ChatMessage.Status.RECEIVED){
             chatMessageList.add(uniqueId);
        }    */
 
         logger.info("Listeners will be notified");
 
         for(PluginContainer plugin : skypeListeners) {
             try {
                 ((MessageReceivedListener)plugin.getInstance()).chatMessageReceived(chatMessage);
             } catch (Exception e) {
                logger.error("Error in listener: "+plugin.getPluginClass().getName(),e);
             }
         }
     }
 
     @Override
     public void chatMessageSent(ChatMessage chatMessage) throws SkypeException {
 
     }
 }
