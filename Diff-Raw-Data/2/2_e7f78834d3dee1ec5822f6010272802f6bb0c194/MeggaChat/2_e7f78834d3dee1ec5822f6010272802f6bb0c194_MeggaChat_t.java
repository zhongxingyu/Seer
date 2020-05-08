 package com.meggawatts.MeggaChat;
 
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class MeggaChat extends JavaPlugin implements Listener {
 
     HashMap adminschatting = new HashMap();
 
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         adminschatting.clear();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (player.hasPermission("meggachat.admin")) {
                 if (args[0].equalsIgnoreCase("on")) {
                     adminschatting.put(sender, true);
                     sender.sendMessage(ChatColor.DARK_GREEN + "Admin Chat enabled");
                 }
                 if (args[0].equalsIgnoreCase("off")) {
                     adminschatting.remove(sender);
                     sender.sendMessage(ChatColor.DARK_RED + "Admin Chat disabled");
                 }
                 if (args[0].equalsIgnoreCase("?")) {
                     sender.sendMessage(ChatColor.DARK_RED + "/a on will toggle AdminChat mode on.");
                     sender.sendMessage(ChatColor.DARK_RED + "/a off will toggle AdminChat mode off.");
                     sender.sendMessage(ChatColor.DARK_RED + "/a <message> will send the message to all who have access to adminchat.");
                 }
                 if (args.length > 1) {
                     sendToAdmins(args[0], player);
                 }
             }
 
         }
        else if (!(sender.hasPermission("meggachat.admin"))){
             sender.sendMessage(ChatColor.DARK_RED + "No Permissions!");
         }
         return true;
     }
 
     public void sendToAdmins(String Message, Player chatter) {
         Bukkit.broadcast(ChatColor.WHITE + "[" + ChatColor.DARK_RED + "AdminChat" + ChatColor.WHITE + "] " + chatter.getName() + ": " + ChatColor.BOLD + "" + ChatColor.GREEN + Message, "meggachat.admin");
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerChat(PlayerChatEvent event) {
         Player chatter = event.getPlayer();
 
         if (adminschatting.containsKey(chatter)) {
             sendToAdmins(event.getMessage(), event.getPlayer());
             event.setCancelled(true);
         }
     }
 }
