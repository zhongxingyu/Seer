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
             if (args.length >= 1) {
                 if (args[0].equalsIgnoreCase("reset")) {
                     OfflinePlayer check;
                     if (args.length >= 2) {
                         if (!player.hasPermission("chatmanager.moderate.alias")) {
                             player.sendMessage("§cYou don't have permission to do that.");
                             return true;
                         }
                         check = plugin.getServer().getOfflinePlayer(args[1]);
                     } else {
                         check = plugin.getServer().getOfflinePlayer(player.getName());
                     }
                     if (check != null && !plugin.getAlias(check).equals(check.getName())) {
                         plugin.setAlias(check.getName(), "");
                         lastUse.remove(check.getName());
                         player.sendMessage(ChatColor.GREEN + "Reset the alias of " + ChatColor.YELLOW + check.getName() + ChatColor.GREEN + ".");
                     } else {
                         player.sendMessage(ChatColor.RED + "That player has not yet set their alias.");
                     }
                } else if (args[0].equalsIgnoreCase("check")) {
                    if (args.length == 2) {
                        String alias = args[1];
                        for (OfflinePlayer check : plugin.getServer().getOfflinePlayers()) {
                            if (plugin.getAlias(check).equalsIgnoreCase(alias)) {
                                player.sendMessage(ChatColor.GREEN + "The alias " + ChatColor.YELLOW + alias + ChatColor.GREEN + " belongs to " + ChatColor.YELLOW + check.getName() + ChatColor.GREEN
                                        + ".");
                                return true;
                            }
                        }

                        player.sendMessage(ChatColor.RED + "That alias does not belong to any player.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid arguments. /alias check (alias)");
                    }
                 } else {
                     if (player.hasPermission("chatmanager.alias")) {
                         if (!lastUse.containsKey(player.getName()) || lastUse.get(player.getName()) + plugin.config().getAliasCooldown() < System.currentTimeMillis()) {
                             String alias = args[0];
                             if (alias.length() >= 5) {
                                 if (StringHelper.isValidString(alias, true, true, '_') && StringHelper.stripColors(alias).length() <= 16) {
                                     if (plugin.config().isValidString(alias)) {
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
                                         player.sendMessage(ChatColor.RED + "Invalid alias. You used an invalid color code.");
                                     }
                                 } else {
                                     player.sendMessage(ChatColor.RED + "Invalid alias. Only a maximum of 16 alphanumeric characters are allowed.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "Invalid alias. It must be at least 5 characters long.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "This command is cooling down.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission.");
                     }
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
