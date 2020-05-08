 package net.daboross.bukkitdev.uberchat;
 
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.metadata.MetadataValue;
 
 /**
  *
  * @author Dabo Ross <Dabo.Ross at daboross.net>
  */
 public class UberChatListener implements Listener {
 
     private static final String capsMessage = ChatColor.RED + "I'm sorry, but your chat message contains to many uppercase letters.";
 
     public UberChatListener() {
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerChatEvent(AsyncPlayerChatEvent evt) {
         if (evt.isCancelled()) {
             return;
         }
         checkCaps(evt);
         checkColor(evt);
     }
 
     private void checkColor(AsyncPlayerChatEvent evt) {
         Player p = evt.getPlayer();
         if (p.hasMetadata("isMessageColorOn")) {
             List<MetadataValue> meta = p.getMetadata("isMessageColorOn");
             if (meta.size() >= 1) {
                 if (meta.get(0).asBoolean()) {
                     evt.setMessage(Colorizor.colorize(evt.getMessage()));
                 }
             }
         }
     }
 
     private void checkCaps(AsyncPlayerChatEvent evt) {
         Player p = evt.getPlayer();
         if (p.hasPermission("uberchat.ignorecaps")) {
             return;
         }
         String message = evt.getMessage();
         int totalChars = message.length();
         int capChars = 0;
         char[] charArray = message.toCharArray();
         for (char c : charArray) {
            if (Character.isUpperCase(c)) {
                 capChars += 1;
             }
         }
         if (totalChars / (double) capChars < 1.7 && totalChars > 5) {
             p.sendMessage(capsMessage);
             evt.setMessage(message.toLowerCase());
         }
     }
 }
