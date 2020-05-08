 package com.md_5.district;
 
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class District extends JavaPlugin {
 
     public static final Logger logger = Bukkit.getServer().getLogger();
     private DistrictBlockListener blockListener;
     private DistrictPlayerListener playerListener;
 
     public void onEnable() {
         Config.load(this);
         Loader.load(this);
         blockListener = new DistrictBlockListener(this);
         playerListener = new DistrictPlayerListener(this);
         logger.info(String.format("District v%1$s by md_5 enabled", this.getDescription().getVersion()));
     }
 
     public void onDisable() {
         logger.info(String.format("District v%1$s by md_5 disabled", this.getDescription().getVersion()));
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player) {
             return onPlayerCommand((Player) sender, command, label, args);
         } else {
             return onConsoleCommand(sender, command, label, args);
         }
     }
 
     public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
         if (args.length == 0) {
             player.sendMessage(ChatColor.GOLD + "District by md_5, the following commands may be used at this time:");
             player.sendMessage(ChatColor.GOLD + "/district claim [size] [region]");
             player.sendMessage(ChatColor.GOLD + "/district show [region]");
             player.sendMessage(ChatColor.GOLD + "/district remove [region]");
            player.sendMessage(ChatColor.GOLD + "/district list");
             player.sendMessage(ChatColor.GOLD + "/district addmember [region] [player]");
             player.sendMessage(ChatColor.GOLD + "/district delmember [region] [player]");
            player.sendMessage(ChatColor.GOLD + "/district listmembers [region]");
             return true;
         }
         if (args[0].equalsIgnoreCase("claim")) {
             Commands.claim(player, args, this, 3);
             return true;
         }
         if (args[0].equalsIgnoreCase("show")) {
             Commands.show(player, args, 2);
             return true;
         }
         if (args[0].equalsIgnoreCase("remove")) {
             Commands.remove(player, args, this, 2);
             return true;
         }
         if (args[0].equalsIgnoreCase("addmember")) {
             Commands.addMember(player, args, this, 3);
             return true;
         }
         if (args[0].equalsIgnoreCase("delmember")) {
             Commands.delMember(player, args, this, 3);
             return true;
         }
         if (args[0].equalsIgnoreCase("list")) {
             Commands.list(player);
             return true;
         }
         if (args[0].equalsIgnoreCase("listmembers")) {
             Commands.listMembers(player, args, 2);
             return true;
         }
         player.sendMessage(ChatColor.RED + "District: That is not a valid command");
         return true;
     }
 
     public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
         sender.sendMessage(String.format("District v%1$s by md_5", this.getDescription().getVersion()));
         sender.sendMessage("District: No other console functionality is available at this time");
         return true;
     }
 }
