 package com.mcbouncer.bungee.command;
 
 import com.mcbouncer.bungee.MCBouncer;
 import com.mcbouncer.util.MiscUtils;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 public class KickCommand extends Command {
 
     MCBouncer plugin;
     
     public KickCommand(MCBouncer plugin) {
         super("kick");
         this.plugin = plugin;
     }
     
     @Override
     public void execute(final CommandSender sender, final String[] args) {
         plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
 
             public void run() {
                 ProxiedPlayer player = null;
                 
                 if (!sender.hasPermission("mcbouncer.mod")) {
                     sender.sendMessage(ChatColor.RED + "You need permission to run that command.");
                     return;
                 }
                 
                 if (sender instanceof ProxiedPlayer) {
                     player = (ProxiedPlayer)sender;
                 }
                 if (args.length == 0) {
                     sender.sendMessage(ChatColor.RED + "Syntax:  /kick <username> [reason]");
                     return;
                 }
                 String toKick = args[0];
                 String reason = plugin.config.defaultKickMessage;
                 
                 if (args.length > 1) {
                     reason = MiscUtils.join(args, " ", 1, args.length);
                 }
 
                 ProxiedPlayer p = plugin.getProxy().getPlayer(toKick);
                 if (p != null) {
                    p.disconnect("Banned: " + reason);
                 }
                 
                 String message = ChatColor.GREEN + "User " + toKick + " has been kicked by " + sender.getName() + ". (" + reason + ")";
                 plugin.getLogger().info(ChatColor.stripColor(message));
                 if (plugin.config.showBanMessages) {
                     plugin.getProxy().broadcast(message);
                 }
                 else {
                     for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
                         if (pl.hasPermission("mcbouncer.mod")) {
                             pl.sendMessage(message);
                         }
                     }
                 }
             }
         });
     }
     
 }
