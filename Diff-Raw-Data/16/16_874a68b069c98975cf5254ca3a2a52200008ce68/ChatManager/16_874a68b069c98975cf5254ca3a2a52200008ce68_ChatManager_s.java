 package net.betterverse.chatmanager;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.betterverse.chatmanager.command.AliasExecutor;
 import net.betterverse.chatmanager.command.ChatExecutor;
 import net.betterverse.chatmanager.command.IgnoreExecutor;
 import net.betterverse.chatmanager.command.MeExecutor;
 import net.betterverse.chatmanager.command.ModeratorChatExecutor;
 import net.betterverse.chatmanager.command.MuteExecutor;
 import net.betterverse.chatmanager.command.PrefixExecutor;
 import net.betterverse.chatmanager.command.ReplyExecutor;
 import net.betterverse.chatmanager.command.WhisperExecutor;
 import net.betterverse.chatmanager.util.Configuration;
 import net.betterverse.chatmanager.util.PlayerData;
 import net.betterverse.chatmanager.util.StringHelper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatManager extends JavaPlugin implements Listener {
     private final List<ChatMessage> messages = new ArrayList<ChatMessage>();
     private final Set<String> tasks = new HashSet<String>();
     private Configuration config;
     private PlayerData data;
     private ChatExecutor chatCmd;
     private IgnoreExecutor ignoreCmd;
     private ModeratorChatExecutor modChatCmd;
     private MuteExecutor muteCmd;
     private ReplyExecutor replyCmd;
 
     @Override
     public void onDisable() {
         log(toString() + " disabled.");
     }
 
     @Override
     public void onEnable() {
         config = new Configuration(this);
         data = new PlayerData(this);
 
         // Register commands
         getCommand("alias").setExecutor(new AliasExecutor(this));
 
         chatCmd = new ChatExecutor(this);
         getCommand("chat").setExecutor(chatCmd);
 
         ignoreCmd = new IgnoreExecutor();
         getCommand("ignore").setExecutor(ignoreCmd);
         getCommand("unignore").setExecutor(ignoreCmd);
 
         modChatCmd = new ModeratorChatExecutor();
         getCommand("modchat").setExecutor(modChatCmd);
 
         replyCmd = new ReplyExecutor(this);
         getCommand("reply").setExecutor(replyCmd);
 
         getCommand("me").setExecutor(new MeExecutor(this));
 
         muteCmd = new MuteExecutor();
         getCommand("mute").setExecutor(muteCmd);
         getCommand("unmute").setExecutor(muteCmd);
 
         getCommand("prefix").setExecutor(new PrefixExecutor(this));
 
         getCommand("whisper").setExecutor(new WhisperExecutor(this));
 
         // Register events
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
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChatLowest(AsyncPlayerChatEvent event) {
         final Player player = event.getPlayer();
         // Check if the player is muted
         if (muteCmd.isPlayerMuted(player)) {
             player.sendMessage(ChatColor.RED + "You cannot speak. You have been muted for the reason: " + muteCmd.getMuteReason(player));
             event.setCancelled(true);
             return;
         }
 
         // Check for spam
         if (hasConsecutiveMessages(player)) {
             // Player has sent too many messages in a row, warn for spam
             player.sendMessage(config.getConsecutiveMessageWarning());
             event.setCancelled(true);
 
             // Start time-out if the player has not yet been warned
             if (!tasks.contains(player.getName())) {
                 tasks.add(player.getName());
                 getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
                     @Override
                     public void run() {
                         messages.clear();
 
                         player.sendMessage(config.getConsecutiveMessageTimeoutNotification());
                         tasks.remove(player.getName());
                     }
                 }, config.getConsecutiveMessageTimeout());
             }
             return;
         }
 
         // Check if player has sent too many messages within a certain period
         if (hasExceededChatLimit(player)) {
             player.sendMessage(config.getChatLimitWarning());
             return;
         }
 
         // Validated
 
         // Remember a message was sent.
         messages.add(new ChatMessage(player.getName(), System.currentTimeMillis()));
 
         // Message
 
         String message = event.getMessage();
         // Strip color codes from the message if the player does not have the proper permission
         if (!player.hasPermission("chatmanager.colored")) {
             message = StringHelper.stripColors(message);
         }
 
         // Do not allow invalid color codes
         if (!config.isValidString(message)) {
             message = config.stripInvalidColorCodes(message);
         }
 
         // Set the new message
         event.setMessage(message);
 
         // Format
 
         // Check if message format should be overwritten (Not the one set for the /me command)
         if (!config().isNonDefaultMessageFormat(event.getFormat())) {
             // Use normal message format
             event.setFormat(config().getChatMessageFormat());
         }
 
         // Replace all variables in message format
         String messageFormat = event.getFormat();
         messageFormat = config().parsedMessageFormat(messageFormat, event.getPlayer(), event.getMessage());
         messageFormat = StringHelper.parseColors(messageFormat);
         event.setFormat(messageFormat);
 
         // Recipients
 
         // Check if the player has silenced chat
         if (chatCmd.hasPlayerSilencedChat(player)) {
             event.getRecipients().remove(player);
         }
 
         // Remove recipients if they ignored the chatter
         for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext();) {
             Player recipient = it.next();
             if (ignoreCmd.isPlayerIgnoredByPlayer(recipient, player)) {
                 it.remove(); // Remove recipient from collection
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerChatLow(AsyncPlayerChatEvent event) {
         // Check if player is in mod chat
         if (modChatCmd.isInModChat(event.getPlayer())) {
             onModChat(event);
         }
     }
 
     public Configuration config() {
         return config;
     }
 
     public void doChatEvent(AsyncPlayerChatEvent event) {
         if (!event.isCancelled()) {
             String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
             getServer().getConsoleSender().sendMessage(s);
 
             for (Player recipient : event.getRecipients()) {
                 recipient.sendMessage(s);
             }
         }
     }
 
     public String getAlias(OfflinePlayer player) {
         return data.getName(player);
     }
 
     public String getPrefix(Player player) {
         return data.getPrefix(player);
     }
 
     public void log(Level level, String message) {
         getServer().getLogger().log(level, "[ChatManager] " + message);
     }
 
     public void log(String message) {
         log(Level.INFO, message);
     }
 
     public void setAlias(String player, String alias) {
         data.set(player + ".alias", alias);
     }
 
     public void setPrefix(Player player, String prefix) {
         data.set(player.getName() + ".prefix", prefix);
     }
 
     public void whisper(CommandSender sender, CommandSender receiver, String message) {
         config.sendWhisperMessages(sender, receiver, message);
         replyCmd.addReply(sender, receiver);
     }
 
     private boolean hasConsecutiveMessages(Player player) {
         String previous = "";
         int consecutive = 0;
         int max = config.getMaximumConsecutiveMessages();
         for (ChatMessage message : messages) {
             String name = message.getPlayer();
             if (!name.equals(player.getName())) {
                 consecutive = 0;
                 previous = name;
                 continue;
             }
 
             if (previous.isEmpty()) {
                 // First entry on the list
                 previous = name;
             }
 
             if (name.equals(previous)) {
                 consecutive++;
             } else {
                 previous = name;
                 consecutive = 0;
                 continue;
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
             }
 
             if (total == messageLimit) {
                 return true;
             }
         }
 
         return false;
     }
 
     private void onModChat(AsyncPlayerChatEvent event) {
         // Fitler recipients that don't have permission to read modchat messages.
         for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext();) {
             Player recipient = it.next();
             if (!recipient.hasPermission("chatmanager.moderate.modchat")) {
                 it.remove(); // Remove recipient from collection
             }
         }
 
         // Preprend the moderation tag onto the format
         String moderationTag = StringHelper.parseColors("&d[ModChat]&f ");
         event.setFormat(moderationTag + event.getFormat());
 
         // Cancel the event and send to recipients
         doChatEvent(event);
         event.setCancelled(true);
     }
 }
