 package com.md_5.district;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 public class Commands {
 
     public static void claim(Player player, String[] args, final District district, int count) {
         if (args.length != count) {
             invalidArgs(player);
             return;
         }
         int size, height;
         try {
             size = Integer.parseInt(args[1]);
         } catch (NumberFormatException ex) {
             throw new CommandException(args[1] + " is not a valid number");
         }
         if (size % 2 == 0) {
             throw new CommandException("Size must be odd");
         }
         
         World world = player.getWorld();
         
         // Limit height to world height
         height = Math.min(size, world.getMaxHeight());
         
         size /= 2;
         height /= 2;
         
         size = (int) Math.floor(size);
         height = (int) Math.floor(size);
         
         Location point1 = player.getLocation();
         Location point2 = player.getLocation();
         point1.add(size, height, size);
         point2.add(-size, -size, -size);
 
         if (((Util.getTotalVolume(player.getName()) + Util.getVolume(point1, point2)) > Util.getMaxVolume(player)) && Util.getMaxVolume(player) != -1) {
             player.sendMessage(ChatColor.RED + "District: You cannot claim a region that big!");
             return;
         }
         if (Regions.getRegion(args[2]) != null) {
             player.sendMessage(ChatColor.RED + "District: Region " + args[2] + " is already claimed");
             return;
         }
         if (Util.isOverlapping(point1, point2)) {
             player.sendMessage(ChatColor.RED + "District: Error! A region already exists here");
             return;
         }
 
         Region creation = new Region(point1.getWorld(), point1, point2, player.getName(), new ArrayList<String>(), args[2]);
         Regions.addRegion(creation);
         Loader.save(district, creation);
         player.sendMessage(ChatColor.GREEN + "District: A " + args[1] + "x" + args[1] + "x"
                 + args[1] + " region named " + creation.getName() + " has been claimed for you!");
         return;
     }
 
     public static void show(Player player, String[] args, Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         if (r.canUse(player)) {
             Util.outline(player, r);
             Vector size = r.getSize();
            player.sendMessage(ChatColor.GREEN + "District: Your " + (size.getBlockX() + 1) + 
                    "x" + (size.getBlockY() + 1) + "x" + (size.getBlockZ() + 1) + 
                     " region has been outlined just for you");
         } else {
             r.sendDeny(player);
         }
     }
 
     public static void remove(Player player, String[] args, final District district, Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
             Regions.removeRegion(args[1]);
             Loader.remove(district, r);
             player.sendMessage(ChatColor.GREEN + "District: Region " + r.getName() + " removed");
         } else {
             r.sendDeny(player);
         }
     }
 
     public static void addMember(Player player, String[] args, final District district, Region r) {
         if (args.length != 3) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
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
 
     public static void delMember(Player player, String[] args, final District district, Region r) {
         if (args.length != 3) {
             invalidArgs(player);
             return;
         }
         if (r.canAdmin(player)) {
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
 
     public static void list(String player, CommandSender sender) {
         String owns = "";
         String isMemberOf = "";
         for (Region r : Regions.getRegions()) {
             if (r.isOwner(player)) {
                 owns += r.getName() + ", ";
             }
             if(r.isMember(player)) {
                 isMemberOf += r.getName() + ", ";
             }
         }
         
         Boolean isSender = player == sender.getName();
         if(!isMemberOf.equals("")) {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender?"You are":(player + " is")) +
             		" a member of these regions: " + isMemberOf);
         }
         else {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender?"You are":(player + " is")) +
                     " not a member of any regions");
         }
         
         if (!owns.equals("")) {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender?"You own":(player + " owns")) + 
                     " these regions: " + owns);
         } else {
             sender.sendMessage(ChatColor.GREEN + "District: " + (isSender?"You own":(player + " owns")) +
                     " no regions");
         }
     }
 
     public static void listAll(Player sender, String[] args) {
         if(!sender.hasPermission("district.listall")) {
             throw new CommandException("You don't have permission to access that command!");
         }
         
         if(args.length == 2) {
             String player = args[1];
             
             list(player, sender);
         }
         else if(args.length == 1) {
             String result = "";
             for (Region r : Regions.getRegions()) {
                 result += r.getName() + ", ";
             }
             sender.sendMessage(ChatColor.GREEN + "District: The following regions exist: " + result);
         }
         else {
             invalidArgs(sender);
         }
     }
     
     public static void listMembers(Player player, String[] args, Region r) {
         if (args.length != 2) {
             invalidArgs(player);
             return;
         }
         String peeps = "";
         if (r.canAdmin(player)) {
             for (String member : r.getMembers()) {
                 peeps += member + ", ";
             }
             if (peeps != "") {
                 player.sendMessage(ChatColor.GREEN + "District: " + r.getName() + " has these members: " + peeps);
             } else {
                 player.sendMessage(ChatColor.GREEN + "District: " + r.getName() + " has no members");
             }
         } else {
             r.sendDeny(player);
         }
         return;
     }
 
     public static void invalidArgs(Player p) {
         throw new CommandException("Invalid number of arguments for that command");   
     }
 }
