 package net.betterverse.chatmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.betterverse.chatmanager.util.Configuration;
import net.betterverse.chatmanager.util.StringHelper;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatManager extends JavaPlugin implements Listener {
     private final List<ChatMessage> messages = new ArrayList<ChatMessage>();
     private Configuration config;
 
     @Override
     public void onDisable() {
         log(toString() + " disabled.");
     }
 
     @Override
     public void onEnable() {
         config = new Configuration(this);
 
         getServer().getPluginManager().registerEvents(this, this);
 
         log(toString() + " enabled.");
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder(getDescription().getName() + " v" + getDescription().getVersion() + " [Written by: ");
         List<String> authors = getDescription().getAuthors();
         for (int i = 0; i < authors.size(); i++) {
             builder.append(authors.get(i) + (i + 1 != authors.size() ? ", " : ""));
         }
         builder.append("]");
 
         return builder.toString();
     }
 
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
         Player player = event.getPlayer();
         // Check for spam
         if (hasConsecutiveMessages(player)) {
             // Player has sent too many messages in a row, warn for spam
             player.sendMessage(config.getConsecutiveMessageWarning());
             event.setCancelled(true);
         } else {
             // Check if player has sent too many messages within a certain period
             if (hasExceededChatLimit(player)) {
                 player.sendMessage(config.getChatLimitWarning());
                 event.setCancelled(true);
             }
 
            String message = event.getMessage();

            // Strip color codes from the message if the player does not have the proper permission
            if (!player.hasPermission("chatmanager.colored")) {
                event.setMessage(StringHelper.stripColors(message));
            }

            // Cache message
            messages.add(new ChatMessage(player.getName(), message, System.currentTimeMillis()));
         }
     }
 
     public void log(Level level, String message) {
         getServer().getLogger().log(level, "[ChatManager] " + message);
     }
 
     public void log(String message) {
         log(Level.INFO, message);
     }
 
     private boolean hasConsecutiveMessages(Player player) {
         String previous = "";
         int consecutive = 0;
         int max = config.getMaximumConsecutiveMessages();
         for (ChatMessage message : messages) {
             String name = message.getPlayer();
             if (previous.isEmpty()) {
                 // First entry on the list
                 previous = name;
             }
 
             if (name.equals(previous)) {
                 consecutive++;
             } else {
                 previous = name;
                 consecutive = 0;
             }
 
             if (consecutive == max) {
                 return true;
             }
         }
 
         return false;
     }
 
     private boolean hasExceededChatLimit(Player player) {
         int total = 0;
         long earliestTime = System.currentTimeMillis() - config.getChatLimitMillis();
         int messageLimit = config.getChatLimit();
         for (ChatMessage message : messages) {
             if (!message.getPlayer().equals(player.getName())) {
                 continue;
             }
 
             // If message was sent within the limit, add to the total
             if (message.getTime() > earliestTime) {
                 total++;
                 System.out.println("total: " + total);
             }
 
             if (total == messageLimit) {
                 return true;
             }
         }
 
         return false;
     }
 }
