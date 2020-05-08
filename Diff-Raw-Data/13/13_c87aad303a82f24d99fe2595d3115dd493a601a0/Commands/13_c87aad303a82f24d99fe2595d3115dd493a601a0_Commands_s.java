 package com.md_5.district;
 
 import java.util.ArrayList;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class Commands {
 
     public static void claim(Player player, String[] args, final District district, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         int size;
         try {
             size = Integer.parseInt(args[1]);
         } catch (NumberFormatException ex) {
             player.sendMessage(ChatColor.RED + "District: " + args[1] + " is not a valid number");
             return;
         }
         if (size % 2 == 0) {
             player.sendMessage(ChatColor.RED + "District: Size must be odd");
             return;
         }
         size /= 2;
         size = (int) Math.floor(size);
         Location point1 = player.getLocation();
         Location point2 = player.getLocation();
         point1.add(size, size, size);
         point2.add(-size, -size, -size);
 
        if ((Util.getTotalSize(player.getName()) + Util.getSize(point1, point2)) > Util.getMaxSize(player)) {
             player.sendMessage(ChatColor.RED + "District: You cannot claim a region that big!");
             return;
         }
         if (Regions.getRegion(args[2]) != null) {
             player.sendMessage(ChatColor.RED + "District: Region " + args[2] + " is already claimed");
             return;
         }
         Region creation = new Region(point1.getWorld(), point1, point2, player.getName(), new ArrayList<String>(), args[2]);
         Regions.addRegion(creation);
         Loader.save(district, creation);
         player.sendMessage(ChatColor.GREEN + "District: A " + args[1] + "x" + args[1] + "x"
                 + args[1] + " region named " + args[2] + " has been claimed for you!");
         return;
     }
 
     public static void show(Player player, String[] args, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         Region r = Regions.getRegion(args[1]);
         if (r == null) {
             player.sendMessage(ChatColor.RED + "District: Error that region does not exist");
             return;
         }
         if (r.canUse(player)) {
             Util.outline(player, r);
             int size = r.getSize();
             player.sendMessage(ChatColor.GREEN + "District: Your " + size + "x" + size + "x" + size + " region has been outlined just for you");
         } else {
             r.sendDeny(player);
         }
     }
 
     public static void remove(Player player, String[] args, final District district, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         Region r = Regions.getRegion(args[1]);
         if (r == null) {
             player.sendMessage(ChatColor.RED + "District: Error that region does not exist");
             return;
         }
         if (r.isOwner(player)) {
             Regions.removeRegion(args[1]);
             Loader.remove(district, r);
             player.sendMessage(ChatColor.GREEN + "District: Region " + r.getName() + " removed");
         } else {
             r.sendDeny(player);
         }
     }
 
     public static void addMember(Player player, String[] args, final District district, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         Region r = Regions.getRegion(args[1]);
         if (r == null) {
             player.sendMessage(ChatColor.RED + "District: Error that region does not exist");
             return;
         }
         if (r.isOwner(player)) {
             if (!r.isMember(args[2])) {
                 r.addMember(args[2]);
                 Loader.save(district, r);
                 player.sendMessage(ChatColor.GREEN + "District: Player " + args[2] + " added to " + r.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "District: Player " + args[2] + " is already a member of " + r.getName());
             }
         } else {
             r.sendDeny(player);
         }
         return;
     }
 
     public static void delMember(Player player, String[] args, final District district, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         Region r = Regions.getRegion(args[1]);
         if (r == null) {
             player.sendMessage(ChatColor.RED + "District: Error that region does not exist");
             return;
         }
         if (r.isOwner(player)) {
             if (r.isMember(args[2])) {
                 r.removeMember(args[2]);
                 Loader.save(district, r);
                 player.sendMessage(ChatColor.GREEN + "District: Player " + args[2] + " removed from " + r.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "District: Player " + args[2] + " is not a member of " + r.getName());
             }
         } else {
             r.sendDeny(player);
         }
         return;
     }
 
     public static void list(Player player) {
         String regions = "";
         for (Region r : Regions.getRegions().values()) {
             if (r.isOwner(player)) {
                 regions += r.getName() + ", ";
             }
         }
         if (!regions.equals("")) {
             player.sendMessage(ChatColor.GREEN + "District: You own these regions: " + regions);
         } else {
             player.sendMessage(ChatColor.GREEN + "District: You own no regions");
         }
     }
 
     public static void listMembers(Player player, String[] args, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         Region r = Regions.getRegion(args[1]);
         if (r == null) {
             player.sendMessage(ChatColor.RED + "District: Error that region does not exist");
             return;
         }
         String peeps = "";
         if (r.isOwner(player)) {
             for (String member : r.getMembers()) {
                 peeps += member + ", ";
             }
             if (peeps != null) {
                 player.sendMessage(ChatColor.GREEN + "District: " + args[1] + " has these members: " + peeps);
             } else {
                 player.sendMessage(ChatColor.GREEN + "District: " + args[1] + " has no members");
             }
         } else {
             r.sendDeny(player);
         }
         return;
     }
 
     public static void invalidArgs(Player p) {
         p.sendMessage(ChatColor.RED + "District: Invalid number of arguments for that command");
     }
 }
