 package com.huskehhh.oresomenotes;
 
 import java.util.ListIterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class OresomeCommands implements CommandExecutor {
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("notes")) {
             if (sender.hasPermission("oresomenotes.staff")) {
                 if (args.length <= 16 && args.length >= 0) {
                     String query = args[0];
                    String perform = args[1];
 
                     if (query.equalsIgnoreCase("add")) {
                         String extra = Utility.argBuilding(args);
                         if (OresomeNotes.getInstance().getServer().getPlayer(perform) != null) {
                             Utility.addNote(OresomeNotes.getInstance().getServer().getPlayer(perform), Utility.prepareForConfig(extra));
                         } else {
                             sender.sendMessage(ChatColor.RED + "You must enter a valid player name.");
                         }
                         sender.sendMessage(ChatColor.GREEN + "Successfully added note; " + extra + " to " + ChatColor.RED + perform + ChatColor.GREEN + "'s notes!");
 
                     } else if (query.equalsIgnoreCase("remove")) {
                         if (Integer.valueOf((args[2])) != null) {
                             int entry = Integer.parseInt((args[2]));
                             if (OresomeNotes.getInstance().getServer().getPlayer(perform) != null) {
                                 Utility.removeNote(OresomeNotes.getInstance().getServer().getPlayer(perform), entry);
                             } else {
                                 sender.sendMessage(ChatColor.RED + "You must enter a valid player name.");
                             }
                         } else {
                             sender.sendMessage(ChatColor.RED + "Third paramater must be a number.");
                         }
                         return true;
 
                     } else if (query.equalsIgnoreCase("read")) {
                         if (OresomeNotes.getInstance().getServer().getPlayer(perform) != null) {
                             ListIterator<String> read = Utility.iterRead(OresomeNotes.getInstance().getServer().getPlayer(perform));
                             sender.sendMessage(ChatColor.GREEN + "=====Notes for " + ChatColor.RED + perform + ChatColor.GREEN + "======");
                             while (read.hasNext()) {
                                 String in = read.next();
                                 if (!in.equals("dataManage--noedit")) {
                                     sender.sendMessage(ChatColor.BLUE + Utility.readFromConfig(in));
                                 }
                             }
                         } else {
                             sender.sendMessage(ChatColor.RED + "You must enter a valid player name.");
                         }
                         return true;
                     }
 
                 } else {
                     sender.sendMessage(ChatColor.RED + "Amount of paramaters is wrong");
                 }
             } else {
                 sender.sendMessage(ChatColor.RED + "No permission for this.");
             }
             return true;
         }
         return false;
     }
 
 }
