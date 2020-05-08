 package net.betterverse.chatmanager.command;
 
 import net.betterverse.chatmanager.ChatManager;
 import net.betterverse.chatmanager.util.StringHelper;
 import net.betterverse.creditsshop.PlayerListener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PrefixExecutor implements CommandExecutor {
     private final ChatManager plugin;
 
     public PrefixExecutor(ChatManager plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (args.length == 0) {
                 plugin.setPrefix(player, "");
                 player.sendMessage(ChatColor.GREEN + "Prefix cleared.");
             } else if (args.length == 1) {
                if (StringHelper.isValidString(args[0], true, false) && args[0].length() <= 7) {
                     if (PlayerListener.deductCredits(player, plugin.config().getPrefixCost())) {
                         plugin.setPrefix(player, args[0]);
                         player.sendMessage(ChatColor.GREEN + "You set your prefix to " + ChatColor.YELLOW + args[0] + ChatColor.GREEN + ".");
                     }
                 } else {
                    player.sendMessage(ChatColor.RED + "Invalid prefix. Only a maximum of 7 letters are allowed.");
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "Invalid arguments. /prefix (prefix)");
             }
         } else {
             sender.sendMessage("Only in-game players can use '/" + cmdLabel + "'.");
         }
 
         return true;
     }
 }
