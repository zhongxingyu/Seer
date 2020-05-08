 package net.betterverse.chatmanager.command;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.betterverse.chatmanager.ChatManager;
 import net.betterverse.chatmanager.util.StringHelper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AliasExecutor implements CommandExecutor {
     private final ChatManager plugin;
     private final Map<String, Long> lastUse = new HashMap<String, Long>();
 
     public AliasExecutor(ChatManager plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (args.length == 1) {
                 if (player.hasPermission("chatmanager.alias")) {
                     if (!lastUse.containsKey(player.getName()) || lastUse.get(player.getName()) + plugin.config().getAliasCooldown() < System.currentTimeMillis()) {
                         String alias = args[0];
                         if (StringHelper.isValidString(alias, true, true, '_') && alias.length() <= 16) {
                             if (!plugin.getServer().getOfflinePlayer(alias).hasPlayedBefore() || alias.equals(player.getName())) {
                                 for (OfflinePlayer check : plugin.getServer().getOfflinePlayers()) {
                                     // Make sure no other player is using the alias
                                     if (plugin.getAlias(check).equals(alias) && !alias.equals(player.getName())) {
                                         player.sendMessage(ChatColor.RED + "Someone else is already using that alias!");
                                         return true;
                                     }
                                 }
 
                                 plugin.setAlias(player.getName(), alias);
                                 lastUse.put(player.getName(), System.currentTimeMillis());
                                 player.sendMessage(ChatColor.GREEN + "Your alias is now " + ChatColor.YELLOW + alias + ChatColor.GREEN + ".");
                             } else {
                                 player.sendMessage(ChatColor.RED + "You cannot set your alias to the name of an existing player.");
                             }
                         } else {
                            player.sendMessage(ChatColor.RED + "Invalid alias. Only 16 alphanumeric characters are allowed.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "This command is cooling down.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission.");
                 }
             } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
                 if (player.hasPermission("chatmanager.moderate.alias")) {
                     OfflinePlayer check = plugin.getServer().getOfflinePlayer(args[1]);
                     if (check != null && !plugin.getAlias(check).equals(check.getName())) {
                         plugin.setAlias(check.getName(), "");
                         lastUse.remove(check.getName());
                         player.sendMessage(ChatColor.GREEN + "Reset the alias of " + ChatColor.YELLOW + check.getName() + ChatColor.GREEN + ".");
                     } else {
                         player.sendMessage(ChatColor.RED + "That player has not yet set their alias.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission.");
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "Invalid arguments. /alias (alias)");
             }
         } else {
             sender.sendMessage("Only in-game players can use '/" + cmdLabel + "'.");
         }
 
         return true;
     }
 }
