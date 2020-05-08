 package me.ase34.citylanterns.executor;
 
 import me.ase34.citylanterns.CityLanterns;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class SettingsComandExecutor implements CommandExecutor {
     
     private CityLanterns plugin;
     
     public SettingsComandExecutor(CityLanterns plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (args.length < 2) {
             return false;
         }
         
         String group = "main";
         if (args.length == 3) {
             group = args[0];
         }
         
         String variable = args[args.length - 2];
         String value = args[args.length - 1];
         
         try {
             if (variable.equalsIgnoreCase("day")) {
                 int time = Integer.parseInt(value);
                 plugin.getSettings().setDaytime(group, time);
                 
                 sender.sendMessage(ChatColor.GOLD + "Set daytime of group " + ChatColor.GRAY + "'" + group + "'" + ChatColor.GOLD + " to " + ChatColor.WHITE + time);
                 return true;
             } else if (variable.equalsIgnoreCase("night")) {
                 int time = Integer.parseInt(value);
                 plugin.getSettings().setNighttime(group, time);
                 
                 sender.sendMessage(ChatColor.GOLD + "Set nighttime of group " + ChatColor.GRAY + "'" + group + "'" + ChatColor.GOLD + " to " + ChatColor.WHITE + time);
                 return true;
             } else if (variable.equalsIgnoreCase("thunder")) {
                 boolean thunder = value.equalsIgnoreCase("true");
                 plugin.getSettings().setThunder(group, thunder);
                 
                sender.sendMessage(ChatColor.GOLD + "During thunder lanterns of group " + ChatColor.GRAY + "'" + group + "'" + ChatColor.GOLD + (thunder ? "will" : "won't") + " toggle");
                 return true;
             }
         } catch (NumberFormatException e) {
             sender.sendMessage(ChatColor.GRAY + value + ChatColor.RED + " is expected to be a number!");
             return true;
         }
         
         return false;       
     }
 }
