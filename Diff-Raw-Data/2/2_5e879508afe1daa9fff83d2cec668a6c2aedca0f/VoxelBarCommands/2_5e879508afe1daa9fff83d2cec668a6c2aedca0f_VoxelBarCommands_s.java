 package com.thevoxelbox.voxelbar;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class VoxelBarCommands implements CommandExecutor {
     private VoxelBar vb = new VoxelBar();
     private VoxelBarToggleManager tm = vb.tm;
     public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
         // Check if player is console.
         if (!(cs instanceof Player)) {
             cs.sendMessage("You cannot use VoxelBar from the console!");
             return true;
             
         }
         // Initialize variables
         Player player = (Player) cs;
         String command;
         // Check that the player has typed more than /vbar - If not send them a help message.
         if (args.length == 0) {
             player.sendMessage(helpMessage());
             return true;
         }
         // Set command to the first arg of the command
         command = args[0];
         if (command.equalsIgnoreCase("enable") || command.equalsIgnoreCase("on") || command.equalsIgnoreCase("e")) { // Check if they're running the /vbar enable command
                 if (tm.isEnabled(player.getName())) {
                     player.sendMessage(ChatColor.GREEN + "VoxelBar already enabled for you! Type " + ChatColor.DARK_AQUA + "/vbar disable" + ChatColor.GREEN + " to disable VoxelBar!");
                     return true;
                 }
                 tm.setStatus(player.getName(), true);
                 player.sendMessage(ChatColor.GREEN + "VoxelBar enabled for you - " + ChatColor.DARK_AQUA + "/vbar disable" + ChatColor.GREEN + " to turn it off again!");
                 return true;
                 
         } else if (command.equalsIgnoreCase("disable") || command.equalsIgnoreCase("off") || command.equalsIgnoreCase("d")) { // Check if they're running the /vbar disable command
                 if (tm.isEnabled(player.getName())) {
                     tm.setStatus(player.getName(), false);
                     player.sendMessage(ChatColor.GREEN + "VoxelBar disabled for you - " + ChatColor.DARK_AQUA + "/vbar enable" + ChatColor.GREEN + " to turn it on again!");   
                     return true;
                 }
                player.sendMessage(ChatColor.GREEN + "VoxelBar already enabled for you! Type " + ChatColor.DARK_AQUA + "/vbar disable" + ChatColor.GREEN + " to disable VoxelBar!");
                 return true;
                     
         
         }
         player.sendMessage(helpMessage());
         return true;   
     }
     
     public String helpMessage() {
         String help = null;
         help  = ChatColor.GREEN +     "==========[VoxelBar]==========\n";
         help += ChatColor.GREEN +     "/vbar                         \n";
         help += ChatColor.GREEN +     "/vbar enable                  \n";
         help += ChatColor.GREEN +     "/vbar disable                 \n";
         help += ChatColor.GREEN +     "==============================\n";
         return help;
         
     }
     
 }
