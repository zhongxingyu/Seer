 package ca.q0r.msocial.events;
 
 import ca.q0r.msocial.MSocial;
 import ca.q0r.msocial.types.LocaleType;
 import com.miraclem4n.mchat.api.Parser;
 import com.miraclem4n.mchat.util.MessageUtil;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatListener implements Listener {
    private MSocial plugin;
 
     public ChatListener(MSocial instance) {
         plugin = instance;
     }
 
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         String pName = player.getName();
 
         String world = player.getWorld().getName();
         String msg = event.getMessage();
         String eventFormat = Parser.parseChatMessage(pName, world, msg);
 
         if (plugin.isMuted.get(pName) != null
                 && plugin.isMuted.get(pName)) {
             event.setCancelled(true);
 
             return;
         }
 
         if (plugin.isConv.get(pName) == null)
             plugin.isConv.put(pName, false);
 
         if (plugin.isConv.get(pName)) {
             Player recipient = plugin.getServer().getPlayer(plugin.chatPartner.get(pName));
             recipient.sendMessage(MessageUtil.addColour(LocaleType.MESSAGE_CONVERSATION_CONVERSATION.getVal() + eventFormat));
             player.sendMessage(MessageUtil.addColour(LocaleType.MESSAGE_CONVERSATION_CONVERSATION.getVal() + eventFormat));
             MessageUtil.log(MessageUtil.addColour(LocaleType.MESSAGE_CONVERSATION_CONVERSATION.getVal() + eventFormat));
             event.setCancelled(true);
         }
     }
 }
