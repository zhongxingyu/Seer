 package com.rylinaux.who;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class WhoCommandExecutor implements CommandExecutor {
 
     private static final String NO_PERMS = Who.PREFIX + ChatColor.RED + "You don't have permission to do this.";
 
     private final Who plugin;
 
     public WhoCommandExecutor(Who plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
         if (args.length == 0) {
             if (sender.hasPermission("who.who")) {
                 sender.sendMessage(WhoUtils.who(sender));
             } else {
                 sender.sendMessage(NO_PERMS);
             }
             return true;
         }
 
         String object = args[0];
 
         if (WhoUtils.isPlayer(object)) {
             if (sender.hasPermission("who.player")) {
                 Player player = plugin.getServer().getPlayer(object);
                 if (player != null) {
                     WhoUtils.sendArray(sender, WhoUtils.playerInfo(player));
                 } else {
                     OfflinePlayer oplayer = plugin.getServer().getOfflinePlayer(object);
                     if (oplayer.hasPlayedBefore())
                         WhoUtils.sendArray(sender, WhoUtils.playerInfo(oplayer));
                 }
             } else {
                 sender.sendMessage(NO_PERMS);
             }
             return true;
         }
 
         if (WhoUtils.isWorld(object)) {
             World world = plugin.getServer().getWorld(object);
             if (sender.hasPermission("who.world"))
                 sender.sendMessage(WhoUtils.who(sender, world));
             else
                 sender.sendMessage(NO_PERMS);
             return true;
         }
 
        sender.sendMessage(Who.PREFIX + "Invalid Arguments.");
 
         return true;
     }
 
 }
