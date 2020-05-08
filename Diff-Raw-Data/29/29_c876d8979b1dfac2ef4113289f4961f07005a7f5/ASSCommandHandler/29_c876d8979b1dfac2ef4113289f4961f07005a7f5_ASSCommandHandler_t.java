 package com.github.CubieX.Assignment;
 
 import java.sql.ResultSet;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class ASSCommandHandler implements CommandExecutor
 {
     private final Assignment plugin;
     private final ASSConfigHandler cHandler;
     private final Logger log;
 
     // constructor
     public ASSCommandHandler(Assignment plugin, Logger log, ASSConfigHandler cHandler)
     {
         this.plugin = plugin;
         this.cHandler = cHandler;
         this.log = log;  
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
     {
         Player player = null;
         if (sender instanceof Player) 
         {
             player = (Player) sender;
         }
         //TODO: Liste anzeigen der eigenen Assignments (Item ID oder besser: ItemName + Koordinaten + Welt + Status)
         if(plugin.getConfig().getBoolean("debug")){log.info("onCommand");}
         if (cmd.getName().equalsIgnoreCase("ass"))
         { // If the player typed /ass then do the following... (can be run from console also)
             if (args.length == 0)
             { //no arguments, so help will be displayed
                 return false;
             }
             if (args.length==1)
             {
                 if (args[0].equalsIgnoreCase("version")) // argument 0 is given and correct
                 {            
                     sender.sendMessage(ChatColor.GREEN + "This server is running " + Assignment.logPrefix + "version " + plugin.getDescription().getVersion());
                     return true;
                 } //END version   
                 if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
                 {            
                     if(sender.hasPermission("assignment.*"))
                     {
                         cHandler.reloadConfig();
                         sender.sendMessage("[" + ChatColor.GREEN + "Info" + ChatColor.WHITE + "] " + ChatColor.YELLOW + Assignment.logPrefix + plugin.getDescription().getVersion() + " reloaded!");                        
                     }
                     else
                     {
                         sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload Assignment plugin!");
                     }
                     return true;
                 } //END reload
 
                 if (args[0].equalsIgnoreCase("cleanup")) // argument 0 is given and correct
                 {// Deletes all invalid assignments from DB (= Assignments where there is no sign for them any more)
 
                     plugin.cleanupAssignmentsInDB(sender);
                     return true;
                 }
 
                if ((args[0].equalsIgnoreCase("list")) ||
                        (args[0].equalsIgnoreCase("liste")))
                 { // lists all assignments of the player
                     ResultSet resSet = plugin.getAssignmentList(sender.getName());
 
                     if(null != resSet)
                     {
                         readAssignmentList(sender, resSet);                       
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.GREEN + "Es wurden keine Auftraege gefunden.");
                     }
                     return true;
                 }
             }
             if (args.length==2)
             {
                if ((args[0].equalsIgnoreCase("list")) ||
                        (args[0].equalsIgnoreCase("liste")))                        
                 {// lists assignments of given player
                     if(sender.hasPermission("assignment.*") || (sender.hasPermission("assignment.listother")))
                     {
                         ResultSet resSet = plugin.getAssignmentList(args[1].trim());
 
                         if(null != resSet)
                         {
                             readAssignmentList(sender, resSet);
 
                         }
                         else
                         {
                            sender.sendMessage(ChatColor.GREEN + "Es wurden keine Auftraege gefunden.");
                         }
                     }
                     else
                     {
                         sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to list other players assignments!");
                     }
                     return true;
                 }
             }
             else
             {
                 sender.sendMessage(ChatColor.YELLOW + "Falsche Anzahl an Parametern.");
             }
         }         
         return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
     }
 
     void readAssignmentList(CommandSender sender, ResultSet resSet)
     {
         String aState = "";
         boolean assignmentFound = false;
 
         try
         {            
             sender.sendMessage("Auftragsliste:");
             sender.sendMessage("-----------------------------------");
 
             while(resSet.next())
             {
                 assignmentFound = true;

                 if(resSet.getString("state").equalsIgnoreCase("active"))
                 {
                     aState = ChatColor.GREEN + "offen";
                 }
                 else
                 {
                     aState = ChatColor.YELLOW + "abholbar";
                 }
 
                sender.sendMessage("Nr " + ChatColor.GREEN + resSet.getRow() + ChatColor.WHITE + " am Standort: " + resSet.getString("x") + ", " + resSet.getString("y") + ", " + resSet.getString("z") + " in " + resSet.getString("world") + " ist " + aState);
             }
             if(!assignmentFound)
             {
                 sender.sendMessage(ChatColor.YELLOW + "Keine Auftraege gefunden.");
             }
             sender.sendMessage("-----------------------------------");
         }
         catch (Exception ex)
         {
             // nothing to do.
         }
     }
 }
