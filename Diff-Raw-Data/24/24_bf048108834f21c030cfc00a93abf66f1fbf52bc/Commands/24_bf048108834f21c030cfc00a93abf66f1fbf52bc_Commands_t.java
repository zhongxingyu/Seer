 package com.me.tft_02.assassin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.me.tft_02.assassin.commands.ChatCommand;
 import com.me.tft_02.assassin.commands.DeactivateCommand;
 import com.me.tft_02.assassin.commands.LeaderboardCommand;
 import com.me.tft_02.assassin.commands.RefreshCommand;
 import com.me.tft_02.assassin.commands.ReloadCommand;
 import com.me.tft_02.assassin.commands.SpawnMaskCommand;
 import com.me.tft_02.assassin.commands.StatusCommand;
 
 public class Commands implements CommandExecutor {
     Assassin plugin;
 
     public Commands(Assassin instance) {
         plugin = instance;
     }
 
     private CommandExecutor statusCommand = new StatusCommand(plugin);
     private CommandExecutor chatCommand = new ChatCommand(plugin);
     private CommandExecutor deactivateCommand = new DeactivateCommand(plugin);
     private CommandExecutor refreshCommand = new RefreshCommand(plugin);
     private CommandExecutor spawnMaskCommand = new SpawnMaskCommand(plugin);
     private CommandExecutor reloadCommand = new ReloadCommand(plugin);
     private CommandExecutor leaderboardCommand = new LeaderboardCommand(plugin);
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Player player = null;
 
         if (sender instanceof Player) {
             player = (Player) sender;
         }
 
         if (command.getName().equalsIgnoreCase("assassin")) {
             if (player == null) {
                 sender.sendMessage("Assassin adds a new way of PVP.");
             }
             else {
                 if (player.hasPermission("assassin.assassin")) {
                     switch (args.length) {
                         case 0:
                             player.sendMessage(ChatColor.GOLD + "-----[ " + ChatColor.DARK_RED + "Assassin" + ChatColor.GOLD + " ]-----");
                             player.sendMessage(ChatColor.GOLD + "Become an " + ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.GOLD + " and kill other players anonymously:");
                             player.sendMessage(ChatColor.GREEN + "[1]" + ChatColor.GRAY + " Craft an Assassin Mask.");
                             player.sendMessage(ChatColor.GREEN + "[2]" + ChatColor.GRAY + " Right click while holding it, to put it on.");
                             player.sendMessage(ChatColor.GREEN + "[3]" + ChatColor.GRAY + " Your skin & name will be hidden.");
                             player.sendMessage(ChatColor.GREEN + "[4]" + ChatColor.GRAY + " Happy killing! :D");
                             player.sendMessage(ChatColor.GRAY + "Type /assassin [help] for more information.");
                             return true;
                         case 1:
                             if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("status")) {
                                 return statusCommand.onCommand(sender, command, label, args);
                             }
                             if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("top")) {
                                 return leaderboardCommand.onCommand(sender, command, label, args);
                             }
 
                             if (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c")) {
                                 return chatCommand.onCommand(sender, command, label, args);
                             }
 
                             if (args[0].equalsIgnoreCase("reload")) {
                                 return reloadCommand.onCommand(sender, command, label, args);
                             }
                         case 2:
                             if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                                 if (args.length == 2) {
                                     if (Integer.parseInt(args[1]) > 1) {
                                         getHelpPage(Integer.parseInt(args[1]), player);
                                         return true;
                                     }
                                     else {
                                         getHelpPage(1, player);
                                         return true;
                                     }
                                 }
                                 else {
                                     getHelpPage(1, player);
                                     return true;
                                 }
                             }
                             if (args[0].equalsIgnoreCase("mask")) {
                                 return spawnMaskCommand.onCommand(sender, command, label, args);
                             }
                             if (args[0].equalsIgnoreCase("refresh")) {
                                 return refreshCommand.onCommand(sender, command, label, args);
                             }
                             if (args[0].equalsIgnoreCase("deactivate")) {
                                 return deactivateCommand.onCommand(sender, command, label, args);
                             }
                     }
                 }
             }
         }
         return false;
     }
 
     private void getHelpPage(int page, Player player) {
         int maxPages = 2;
         int nextPage = page + 1;
         if (page > maxPages) {
             player.sendMessage(ChatColor.RED + "This page does not exist." + ChatColor.GOLD + " /help [0-" + maxPages + "]");
         }
         else {
             String dot = ChatColor.DARK_RED + "* ";
             player.sendMessage(ChatColor.GOLD + "-----[ " + ChatColor.DARK_RED + "Assassin Help" + ChatColor.GOLD + " ]----- Page " + page + "/" + maxPages);
             if (page == 1) {
                 player.sendMessage(ChatColor.GOLD + "How does it work?");
                 player.sendMessage(dot + ChatColor.GRAY + "When in Assassin mode, you can PVP.");
                 player.sendMessage(dot + ChatColor.GRAY + "Your skin and name will be hidden, even in chat.");
                 player.sendMessage(dot + ChatColor.GRAY + "You can chat with other Assassins in 'Assassin Chat'.");
                 player.sendMessage(dot + ChatColor.GRAY + "When the timer expires, you will be teleported back to where you put on your mask.");
                 player.sendMessage(dot + ChatColor.GRAY + "Nobody will ever know that you were an Assassin.");
             }
             if (page == 2) {
                 player.sendMessage(ChatColor.GOLD + "Commands:");
                 if (player.hasPermission("assassin.info")) {
                     player.sendMessage(dot + ChatColor.GREEN + "/assassin [info]/[status]" + ChatColor.GRAY + " Check your status");
                 }
                 if (player.hasPermission("assassin.chat")) {
                     player.sendMessage(dot + ChatColor.GREEN + "/assassin [chat]/[c]" + ChatColor.GRAY + " Toggle Assassin chat mode");
                 }
                 if (player.hasPermission("assassin.spawnmask")) {
                     player.sendMessage(dot + ChatColor.GREEN + "/assassin [mask] <amount>" + ChatColor.GRAY + " Spawn Assassin mask");
                 }
                 if (player.hasPermission("assassin.refresh")) {
                     player.sendMessage(dot + ChatColor.GREEN + "/assassin [refresh] <player>" + ChatColor.GRAY + " Reset cooldown time for <player>");
                 }
                 if (player.hasPermission("assassin.deactivate")) {
                     player.sendMessage(dot + ChatColor.GREEN + "/assassin [deactivate] <player>" + ChatColor.GRAY + " Deactivate Assassin mode for <player>");
                 }
             }
            if (nextPage <= maxPages) {
                 player.sendMessage(ChatColor.GOLD + "Type /assassin help " + nextPage + " for more");
            }
         }
     }
 }
