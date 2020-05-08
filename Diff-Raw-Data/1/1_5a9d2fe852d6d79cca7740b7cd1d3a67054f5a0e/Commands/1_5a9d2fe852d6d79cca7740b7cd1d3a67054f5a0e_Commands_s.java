 package com.md_5.district;
 
 import java.util.ArrayList;
 import org.bukkit.*;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands {
 
     public static void claim(final Player player, final String[] args, final int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         int size = 0, height;
         try {
             size = Integer.parseInt(args[1]);
         } catch (NumberFormatException ex) {
             player.sendMessage(args[1] + " is not a valid number");
             return;
         }
         if (size % 2 == 0) {
             player.sendMessage("Size must be odd");
             return;
         }
 
         World world = player.getWorld();
 
         // Limit height to world height
         height = Math.min(size, world.getMaxHeight());
 
         size /= 2;
         height /= 2;
 
         size = (int) Math.floor(size);
         height = (int) Math.floor(height);
 
         Location point1 = player.getLocation();
         Location point2 = player.getLocation();
         point1.add(size, height, size);
         point2.add(-size, -height, -size);
 
         if (((Util.getTotalSize(player.getName()) + size) > Util.getMaxSize(player)) && Util.getMaxSize(player) != -1) {
             player.sendMessage(ChatColor.RED + "District: You cannot claim a region that big!");
             player.sendMessage(ChatColor.RED + "District: Use /district quota to view your remaining quota");
             return;
         }
         if (Loader.load(args[2]) != null) {
             player.sendMessage(ChatColor.RED + "District: Region " + args[2] + " is already claimed");
             return;
         }
         if (Util.isOverlapping(point1, point2)) {
             player.sendMessage(ChatColor.RED + "District: Error! A region already exists here");
             return;
         }
 
         Region creation = new Region(point1.getWorld(), point1, point2, player.getName(), new ArrayList<String>(), args[2]);
         Loader.save(creation);
         player.sendMessage(ChatColor.GREEN + "District: A " + args[1] + "x" + args[1] + "x"
                 + args[1] + " region named " + creation.getName() + " has been claimed for you!");
     }
 
     public static void quota(final Player player, final String[] args) {
         int used = Util.getTotalSize(player);
         int total = Util.getMaxSize(player);
         String totalStr = total == -1 ? "infinite" : "" + total;
         player.sendMessage(ChatColor.GREEN + "District: You have claimed " + used
                 + " blocks of your " + totalStr + " block quota.");
         if (total != -1) {
             int remaining = total - used;
             player.sendMessage(ChatColor.GREEN + "District: You have " + remaining
                     + " blocks squared remaining");
         }
     }
 
     public static void show(final Player player, final String[] args, final Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         if (r.canUse(player)) {
             Util.outline(player, r);
             player.sendMessage(ChatColor.GREEN + "District: Your region has been outlined just for you");
         } else {
             Region.sendDeny(player);
         }
     }
 
     public static void hide(final Player player, final String[] args, final Region r) {
         if (args.length != 2) {
             invalidArgs(player);
         }
         if (r.canUse(player)) {
             Util.removeOutline(player, r);
             player.sendMessage(ChatColor.GREEN + "District: Your region has been hidden");
         } else {
             Region.sendDeny(player);
         }
     }
 
     public static void remove(final Player player, final String[] args, final Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
             Loader.remove(r.getName());
             player.sendMessage(ChatColor.GREEN + "District: Region " + r.getName() + " removed");
         } else {
             Region.sendDeny(player);
         }
     }
 
     public static void addMember(final Player player, final String[] args, final Region r) {
         if (args.length != 3) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
             if (!r.isMember(args[2])) {
                 r.addMember(args[2]);
                 Loader.save(r);
                 player.sendMessage(ChatColor.GREEN + "District: Player " + args[2] + " added to " + r.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "District: Player " + args[2] + " is already a member of " + r.getName());
             }
         } else {
             Region.sendDeny(player);
         }
     }
 
     public static void delMember(final Player player, String[] args, final Region r) {
         if (args.length != 3) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
             if (r.isMember(args[2])) {
                 r.removeMember(args[2]);
                 Loader.save(r);
                 player.sendMessage(ChatColor.GREEN + "District: Player " + args[2] + " removed from " + r.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "District: Player " + args[2] + " is not a member of " + r.getName());
             }
         } else {
             Region.sendDeny(player);
         }
     }
 
     public static void list(final String player, final CommandSender sender) {
         String owns = "";
         String isMemberOf = "";
         for (Region r : Loader.byOwner(player)) {
             owns += r.getName() + ", ";
         }
 
         Boolean isSender = player.equals(sender.getName());
         if (!isMemberOf.equals("")) {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender ? "You are" : (player + " is"))
                     + " a member of these regions: " + isMemberOf);
         } else {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender ? "You are" : (player + " is"))
                     + " not a member of any regions");
         }
 
         if (!owns.equals("")) {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender ? "You own" : (player + " owns"))
                     + " these regions: " + owns);
         } else {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender ? "You own" : (player + " owns"))
                     + " no regions");
         }
     }
 
     public static void listAll(final Player sender, final String[] args) {
         if (!sender.hasPermission("district.listall")) {
             sender.sendMessage("You don't have permission to access that command!");
         }
 
         if (args.length == 2) {
             String player = args[1];
 
             list(player, sender);
         } else if (args.length == 1) {
             String result = "";
             for (String r : Loader.listAll()) {
                 result += r + ", ";
             }
             sender.sendMessage(ChatColor.GREEN + "District: The following regions exist: " + result);
         } else {
             invalidArgs(sender);
         }
     }
 
     public static void listMembers(final Player player, final String[] args, final Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         String peeps = "";
         if (r.canAdmin(player)) {
             for (String member : r.getMembers()) {
                 peeps += member + ", ";
             }
             if (!peeps.isEmpty()) {
                 player.sendMessage(ChatColor.GREEN + "District: " + r.getName() + " has these members: " + peeps);
             } else {
                 player.sendMessage(ChatColor.GREEN + "District: " + r.getName() + " has no members");
             }
         } else {
             Region.sendDeny(player);
         }
     }
 
     private static void invalidArgs(final Player p) {
         p.sendMessage("Invalid number of arguments for that command");
     }
 
     public static void setOwner(final Player player, final String[] args, final Region region) {
         if (!player.hasPermission("district.setowner")) {
             player.sendMessage("You don't have permission to access that command!");
         }
 
         if (args.length != 3) {
             invalidArgs(player);
             return;
         }
 
         String newOwnerName = args[2];
         OfflinePlayer newOwner = Bukkit.getServer().getOfflinePlayer(newOwnerName);
 
         if (!newOwner.hasPlayedBefore()) {
             player.sendMessage(newOwnerName + " has never been on this server!");
         }
 
         region.setOwner(newOwnerName);
         player.sendMessage(ChatColor.GREEN + "District: Owner of region " + region.getName()
                 + " set to " + newOwnerName);
     }
 }
