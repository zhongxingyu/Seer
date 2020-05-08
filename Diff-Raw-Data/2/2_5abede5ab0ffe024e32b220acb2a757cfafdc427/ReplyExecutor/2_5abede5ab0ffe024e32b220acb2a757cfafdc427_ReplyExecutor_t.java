 package net.betterverse.chatmanager.command;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.betterverse.chatmanager.ChatManager;
 import net.betterverse.chatmanager.util.StringHelper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class ReplyExecutor implements CommandExecutor {
     private final ChatManager plugin;
     private final Map<String, String> cachedReplies = new HashMap<String, String>();
 
     public ReplyExecutor(ChatManager plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (sender.hasPermission("chatmanager.whisper")) {
             if (args.length >= 1) {
                 if (cachedReplies.containsKey(sender.getName())) {
                     // Player has been sent a private message from someone else and can reply
                     Player player = sender.getServer().getPlayer(cachedReplies.get(sender.getName()));
                     if (player != null) {
                         plugin.whisper(sender, player, StringHelper.concatenate(args, 0));
                     } else {
                         sender.sendMessage(ChatColor.RED + cachedReplies.get(sender.getName()) + " is not online.");
                     }
                 } else {
                     // No one has sent a private message to the player
                     sender.sendMessage(ChatColor.RED + "There is no one to reply to.");
                 }
             } else {
                 sender.sendMessage(ChatColor.RED + "Invalid arguments. /r <msg>");
             }
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission.");
         }
 
         return true;
     }
 
     public void addReply(CommandSender replier, CommandSender receiver) {
        cachedReplies.put(receiver.getName(), replier.getName());
     }
 }
