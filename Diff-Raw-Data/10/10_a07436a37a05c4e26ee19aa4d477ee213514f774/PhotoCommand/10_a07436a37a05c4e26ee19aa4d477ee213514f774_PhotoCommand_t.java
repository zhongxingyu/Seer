 package com.winthier.photos;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class PhotoCommand implements CommandExecutor {
         public final PhotosPlugin plugin;
 
         public PhotoCommand(PhotosPlugin plugin) {
                 this.plugin = plugin;
         }
 
         public void load(final Player player, final String url) {
                 ItemStack item = player.getItemInHand();
                 if (item == null || item.getType() != Material.MAP) {
                         player.sendMessage("" + ChatColor.RED + "You must hold a photo");
                         return;
                 }
                 short mapId = item.getDurability();
                 final Photo photo = plugin.getPhoto(mapId);
                 if (photo == null) {
                         player.sendMessage("" + ChatColor.RED  + "This map is not a photo. To get a photo, visit");
                         player.sendMessage("" + ChatColor.BLUE + "http://www.winthier.com/contributions");
                         return;
                 }
                 if (!plugin.photosConfig.hasPlayerMap(player, mapId)) {
                         player.sendMessage("" + ChatColor.RED  + "You don't own this map. To get a photo, visit");
                         player.sendMessage("" + ChatColor.BLUE + "http://www.winthier.com/contributions");
                         return;
                 }
                 new BukkitRunnable() {
                         public void run() {
                                 if (!photo.loadURL(url)) {
                                         sendAsyncMessage(player, "" + ChatColor.RED + "Can't load URL: " + url);
                                         return;
                                 }
                                sendAsyncMessage(player, "" + ChatColor.GREEN + "Image loaded: " + url);
                                 photo.save();
                         }
                 }.runTaskAsynchronously(plugin);
         }
 
         public void rename(Player player, String name) {
                 ItemStack item = player.getItemInHand();
                 if (item == null || item.getType() != Material.MAP) {
                         player.sendMessage("" + ChatColor.RED + "You must hold the photo you want to rename");
                         return;
                 }
                 short mapId = item.getDurability();
                 final Photo photo = plugin.getPhoto(mapId);
                 if (photo == null) {
                         player.sendMessage("" + ChatColor.RED  + "This map is not a photo. To get a photo, visit");
                         player.sendMessage("" + ChatColor.BLUE + "http://www.winthier.com/contributions");
                         return;
                 }
                 if (!plugin.photosConfig.hasPlayerMap(player, mapId)) {
                         player.sendMessage("" + ChatColor.RED  + "You don't own this map. To get a photo, visit");
                         player.sendMessage("" + ChatColor.BLUE + "http://www.winthier.com/contributions");
                         return;
                 }
                 photo.setName(name);
                 player.sendMessage("" + ChatColor.GREEN + "Name changed. Purchase a new copy by typing \"/photo menu\"");
         }
 
         public void sendAsyncMessage(final Player sender, final String message) {
                 new BukkitRunnable() {
                         public void run() {
                                 sender.sendMessage(message);
                         }
                 }.runTask(plugin);
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                 Player player = null;
                 if (sender instanceof Player) player = (Player)sender;
                 if (args.length == 2 && args[0].equals("load")) {
                         if (player == null) return false;
                         load(player, args[1]);
                         return true;
                 }
                 if (args.length >= 1 && args[0].equals("create")) {
                         if (player == null) return false;
                         String name = "Photo";
                         if (args.length > 1) {
                                 StringBuilder sb = new StringBuilder(args[1]);
                                 for (int i = 2; i < args.length; ++i) {
                                         sb.append(" ").append(args[i]);
                                 }
                                 if (sb.length() > 32) {
                                         player.sendMessage("" + ChatColor.RED + "Name is too long");
                                         return true;
                                 }
                                 name = sb.toString();
                         }
                         plugin.createPhoto(player, name);
                         return true;
                 }
                 if (args.length > 1 && args[0].equals("rename")) {
                         if (player == null) return false;
                         StringBuilder sb = new StringBuilder(args[1]);
                         for (int i = 2; i < args.length; ++i) {
                                 sb.append(" ").append(args[i]);
                         }
                         if (sb.length() > 32) {
                                 player.sendMessage("" + ChatColor.RED + "Name is too long");
                                 return true;
                         }
                         String name = sb.toString();
                         rename(player, name);
                         return true;
                 }
                 if (args.length == 1 && args[0].equals("menu")) {
                         if (player == null) return false;
                         plugin.photosMenu.openMenu(player);
                         return true;
                 }
                 sender.sendMessage("" + ChatColor.YELLOW + "Usage: /photo [args...]");
                 sender.sendMessage("" + ChatColor.GRAY   + "--- SYNOPSIS ---");
                 sender.sendMessage("" + ChatColor.YELLOW + "/photo menu");
                 sender.sendMessage("" + ChatColor.GRAY   + "View a menu with all your photos.");
                 sender.sendMessage("" + ChatColor.YELLOW + "/photo create [name]");
                 sender.sendMessage("" + ChatColor.GRAY   + "Create a photo with the give name.");
                 sender.sendMessage("" + ChatColor.YELLOW + "/photo rename <url>");
                 sender.sendMessage("" + ChatColor.GRAY   + "Change the name of a photo.");
                 sender.sendMessage("" + ChatColor.YELLOW + "/photo load <url>");
                 sender.sendMessage("" + ChatColor.GRAY   + "Load a photo from the internet onto the map in your hand.");
                 return true;
         }
 }
