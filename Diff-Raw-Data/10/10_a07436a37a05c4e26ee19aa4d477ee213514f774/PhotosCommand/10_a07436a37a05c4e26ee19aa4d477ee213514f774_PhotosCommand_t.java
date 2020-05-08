 package com.winthier.photos;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PhotosCommand implements CommandExecutor {
         public final PhotosPlugin plugin;
 
         public PhotosCommand(PhotosPlugin plugin) {
                 this.plugin = plugin;
         }
 
         public void grant(CommandSender sender, String player, int amount) {
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                 Player player = null;
                 if (sender instanceof Player) player = (Player)sender;
                 if (args.length == 1 && args[0].equals("reload")) {
                         plugin.load();
                         sender.sendMessage("Configuraton reloaded");
                         return true;
                 }
                 if (args.length == 1 && args[0].equals("save")) {
                         plugin.save();
                         sender.sendMessage("Configuraton saved");
                         return true;
                 }
                 if (args.length == 3 && args[0].equals("grant")) {
                         String name = args[1];
                         Integer amount = null;
                         try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException e) {}
                         if (amount == null || amount <= 0) {
                                 sender.sendMessage("" + ChatColor.RED + "[Photos] Positive number exptected: " + args[2]);
                                 return true;
                         }
                         plugin.photosConfig.addPlayerBlanks(name, amount);
                        plugin.photosConfig.saveConfig();
                        sender.sendMessage("" + ChatColor.GREEN + "[Photos] Granted " + name + " " + amount + " blank photos");
                         return true;
                 }
                 sender.sendMessage("" + ChatColor.YELLOW + "Usage: /photos [args...]");
                 sender.sendMessage("" + ChatColor.GRAY   + "--- SYNOPSIS ---");
                 sender.sendMessage("" + ChatColor.YELLOW + "/photos grant <player> <#photos>");
                 sender.sendMessage("" + ChatColor.GRAY   + "Give a player more blank photos");
                 return true;
         }
 }
