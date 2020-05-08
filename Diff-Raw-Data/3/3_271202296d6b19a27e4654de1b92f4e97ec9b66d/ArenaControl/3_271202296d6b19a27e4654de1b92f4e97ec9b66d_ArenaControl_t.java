 package net.SimplyCrafted.ArenaControl;
 
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Copyright © Brian Ronald
  * 02/09/13
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 public class ArenaControl extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         saveConfig();
         PlayerInteractEvent.getHandlerList().unregister((Listener) this);
     }
 
     private void assignTemplateToArena(String arena, String template, CommandSender sender) {
         int ArenaX1, ArenaY1, ArenaZ1;
         int ArenaX2, ArenaY2, ArenaZ2;
         int TemplateX1, TemplateY1, TemplateZ1;
         int OffsetX, OffsetY, OffsetZ;
         World TemplateWorld, ArenaWorld;
 
         // Sanity checking - make sure the arena exists by looking for its X1
         if (getConfig().getString("arenas." + arena + ".X1") == null) {
             sender.sendMessage("Arena " + arena + " is not defined");
             return;
         }
         // Sanity checking - make sure the template exists by looking for its X
         if (getConfig().getString("templates." + template + ".X") == null) {
             sender.sendMessage("Template " + template + " is not defined");
             return;
         }
 
         // Populate local variables
         ArenaX1 = getConfig().getInt("arenas." + arena + ".X1");
         ArenaY1 = getConfig().getInt("arenas." + arena + ".Y1");
         ArenaZ1 = getConfig().getInt("arenas." + arena + ".Z1");
         ArenaX2 = getConfig().getInt("arenas." + arena + ".X2");
         ArenaY2 = getConfig().getInt("arenas." + arena + ".Y2");
         ArenaZ2 = getConfig().getInt("arenas." + arena + ".Z2");
         TemplateX1 = getConfig().getInt("templates." + template + ".X");
         TemplateY1 = getConfig().getInt("templates." + template + ".Y");
         TemplateZ1 = getConfig().getInt("templates." + template + ".Z");
         // Set the opposite corner of the template using the dimensions of the arena
         OffsetX = TemplateX1 - ArenaX1;
         OffsetY = TemplateY1 - ArenaY1;
         OffsetZ = TemplateZ1 - ArenaZ1;
         ArenaWorld = getServer().getWorld(getConfig().getString("arenas." + arena + ".world"));
         TemplateWorld = getServer().getWorld(getConfig().getString("templates." + template + ".world"));
 
         // Copy the opaque blocks from the template to the arena, block by block.
         int BlockType;
         byte BlockData;
         for (int iZ=ArenaZ1; iZ <= ArenaZ2; iZ++) {
             for (int iY=ArenaY1; iY <= ArenaY2; iY++) {
                 for (int iX=ArenaX1; iX <= ArenaX2; iX++) {
                     if (!(TemplateWorld.getBlockAt(iX + OffsetX, iY + OffsetY, iZ + OffsetZ).getType().isTransparent())) {
                         BlockType = TemplateWorld.getBlockAt(iX + OffsetX, iY + OffsetY, iZ + OffsetZ).getTypeId();
                         BlockData = TemplateWorld.getBlockAt(iX + OffsetX, iY + OffsetY, iZ + OffsetZ).getData();
                         ArenaWorld.getBlockAt(iX,iY,iZ).setTypeIdAndData(BlockType,BlockData,false);
                     }
                 }
             }
         }
         // Re-copy the entire template to the arena, block by block.
         for (int iZ=ArenaZ1; iZ <= ArenaZ2; iZ++) {
             for (int iY=ArenaY1; iY <= ArenaY2; iY++) {
                 for (int iX=ArenaX1; iX <= ArenaX2; iX++) {
                     BlockType = TemplateWorld.getBlockAt(iX + OffsetX, iY + OffsetY, iZ + OffsetZ).getTypeId();
                     BlockData = TemplateWorld.getBlockAt(iX + OffsetX, iY + OffsetY, iZ + OffsetZ).getData();
                     ArenaWorld.getBlockAt(iX,iY,iZ).setTypeIdAndData(BlockType,BlockData,false);
                 }
             }
         }
         // Message to confirm that
         sender.sendMessage("Template " + template + " has been copied to arena " + arena);
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerInteractEvent (PlayerInteractEvent event) {
         // Check whether the block still exists
         if (event.getClickedBlock() == null) return;
         // Check the player did something to a sign
         if (!(event.getClickedBlock().getState() instanceof Sign)) return;
         // Check that that something was a right click
         if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
 
         Sign sign = (Sign) event.getClickedBlock().getState();
         // Check that this is *our* sign
         if (sign.getLine(0).equalsIgnoreCase("[ArenaControl]")) {
             // Check the player's permissions
             if (event.getPlayer().hasPermission("ArenaControl.apply")) {
                 // Assign the template on the third line to the
                 // arena on the second line
                 assignTemplateToArena(sign.getLine(1), sign.getLine(2), event.getPlayer());
             } else {
                 event.getPlayer().sendMessage("You don't have permission.");
                 return;
             }
            event.setCancelled(true);
         }
         // Successful, so eat the event.
     }
 
     // Define some strings. These are sub-commands.
     final String cmd_assign = "assign";      // Assign a template to an arena
     final String cmd_arena = "arena";        // Define, remove or list arenas
     final String cmd_template = "template";  // Define, remove or list templates
     final String cmd_list = "list";          // sub-sub-command
     final String cmd_define = "define";      // sub-sub-command
     final String cmd_remove = "remove";      // sub-sub-command
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("arenacontrol")) {
             if (args.length > 0) {
                 if (args[0].equalsIgnoreCase(cmd_assign)) {
 
                     /* * * ARENACONTROL ASSIGN * * */
 
                     // Permission check
                     if (sender instanceof Player && !(sender.hasPermission("ArenaControl.apply"))) {
                         sender.sendMessage("You do not have permission to run this command");
                         return true;
                     }
                     // Assign a template to an arena
                     if (args.length == 3) {
                         // Actually do something
                         assignTemplateToArena(args[1], args[2], sender);
                     } else {
                         sender.sendMessage("You must specify the arena, then the template");
                     }
                 } else if (args[0].equalsIgnoreCase(cmd_arena)) {
 
                     /* * * ARENACONTROL ARENA * * */
 
                     // Define, remove or list arena
                     if(args.length > 1) {
                         if (args[1].equalsIgnoreCase(cmd_list)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.apply"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Listing arenas
                             if (args.length > 2) {
                                 // A name was specified after the "list" command
                                 if (getConfig().getString("arenas." + args[2] + ".X1") == null) {
                                     // The name wasn't that of a defined arena
                                     sender.sendMessage("Arena " + args[2] + " not found");
                                     return true;
                                 } else {
                                     // The name was that of a defined arena, so show it to the sender
                                     sender.sendMessage("Arena " + args[2] +
                                             ": from (X=" + getConfig().getString("arenas." + args[2] + ".X1") +
                                             ",Y=" + getConfig().getString("arenas." + args[2] + ".Y1") +
                                             ",Z=" + getConfig().getString("arenas." + args[2] + ".Z1") +
                                             ") to (X=" + getConfig().getString("arenas." + args[2] + ".X2") +
                                             ",Y=" + getConfig().getString("arenas." + args[2] + ".Y2") +
                                             ",Z=" + getConfig().getString("arenas." + args[2] + ".Z2") + ") in world "
                                             + getConfig().getString("arenas." + args[2] + ".world"));
                                 }
                             } else {
                                 // List all defined arenas
                                 for (String arena : getConfig().getKeys(true)) {
                                     // Magic numbers:
                                     // 6 is the position of the '.' in "arenas."
                                     // 7 is the position of the next character.
                                     if (arena.startsWith("arenas.") && arena.lastIndexOf(".") == 6) {
                                         arena = arena.substring(7);
                                         sender.sendMessage("Arena: " + arena);
                                     }
                                 }
                             }
                         } else if (args[1].equalsIgnoreCase(cmd_define)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Define a new arena
                             if (args.length >= 9) {
                                 Integer corner1X, corner1Y, corner1Z,
                                         corner2X, corner2Y, corner2Z;
                                 try {
                                     // Convert all the numeric arguments
                                     corner1X = Integer.parseInt(args[3]);
                                     corner1Y = Integer.parseInt(args[4]);
                                     corner1Z = Integer.parseInt(args[5]);
                                     corner2X = Integer.parseInt(args[6]);
                                     corner2Y = Integer.parseInt(args[7]);
                                     corner2Z = Integer.parseInt(args[8]);
                                 } catch (NumberFormatException exception) {
                                     // One or more of the numeric arguments didn't parse
                                     sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
                                     sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                     sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2 [WorldID]");
                                     // No useful input, so drop out of the command handler now.
                                     return true;
                                 }
                                 // Now compare and sort the coordinates, so that we're using a consistent corner.
                                 // The lowest coordinate will be chosen as corner1, and the highest will be corner2.
                                 Integer temp;
                                 if (corner2X < corner1X) {temp = corner2X; corner2X = corner1X; corner1X = temp;}
                                 if (corner2Y < corner1Y) {temp = corner2Y; corner2Y = corner1Y; corner1Y = temp;}
                                 if (corner2Z < corner1Z) {temp = corner2Z; corner2Z = corner1Z; corner1Z = temp;}
                                 // Save this arena into the config
                                 getConfig().set("arenas." + args[2] + ".X1",corner1X);
                                 getConfig().set("arenas." + args[2] + ".Y1",corner1Y);
                                 getConfig().set("arenas." + args[2] + ".Z1",corner1Z);
                                 getConfig().set("arenas." + args[2] + ".X2",corner2X);
                                 getConfig().set("arenas." + args[2] + ".Y2",corner2Y);
                                 getConfig().set("arenas." + args[2] + ".Z2",corner2Z);
                                 if (args.length > 9) {
                                     getConfig().set("arenas." + args[2] + ".world",args[9]);
                                     sender.sendMessage("Arena \"" + args[2] + "\" defined from (X="+corner1X.toString()+",Y="+corner1Y.toString()+",Z="+corner1Z.toString()+
                                     ") to (X="+corner2X.toString()+",Y="+corner2Y.toString()+",Z="+corner2Z.toString()+")");
                                 } else if (sender instanceof Player) {
                                     getConfig().set("arenas." + args[2] + ".world",((Player) sender).getWorld().getName());
                                     sender.sendMessage("Arena \"" + args[2] + "\" defined from (X="+corner1X.toString()+",Y="+corner1Y.toString()+",Z="+corner1Z.toString()+
                                     ") to (X="+corner2X.toString()+",Y="+corner2Y.toString()+",Z="+corner2Z.toString()+")");
                                 } else {
                                     sender.sendMessage("You are not a player; you must specify a world ID.");
                                 }
                             } else {
                                 // Wrong number of arguments for definition of an arena
                                 sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
                                 sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                 sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2 [WorldID]");
                             }
                         } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Remove an arena
                             if (args.length > 2) {
                                 // A name was specified
                                 getConfig().set("arenas." + args[2],null);
                                 sender.sendMessage("Removed arena: " + args[2]);
                             } else {
                                 // No name was specified
                                 sender.sendMessage("Expecting the name of an arena to remove");
                                 sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_remove + " <name>");
                             }
                         } else {
                             // Something other than list, define or remove was provided as a command
                             sender.sendMessage("Not a valid sub-command");
                             sender.sendMessage(cmd_arena + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                         }
                     } else {
                         // Not enough arguments following "arena" command
                         sender.sendMessage("No sub-command provided");
                         sender.sendMessage(cmd_arena + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                     }
                 } else if (args[0].equalsIgnoreCase(cmd_template)) {
 
                     /* * * ARENACONTROL TEMPLATE * * */
 
                     // Define, remove or list template
                     if(args.length > 1) {
                         if (args[1].equalsIgnoreCase(cmd_list)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.apply"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Listing templates
                             if (args.length > 2) {
                                 // A name was specified after the "list" command
                                 if (getConfig().getString("templates." + args[2] + ".X") == null) {
                                     // The name wasn't that of a defined template
                                     sender.sendMessage("Template " + args[2] + " not found");
                                     return true;
                                 } else {
                                     // The name was that of a defined template, so show it to the sender
                                     sender.sendMessage("Template " + args[2] +
                                             ": from (" + getConfig().getString("templates." + args[2] + ".X") +
                                             "," + getConfig().getString("templates." + args[2] + ".Y") +
                                             "," + getConfig().getString("templates." + args[2] + ".Z") + ") in world " +getConfig().getString("templates." + args[2] + ".world"));
                                 }
                             } else {
                                 // List all defined templates
                                 for (String template : getConfig().getKeys(true)) {
                                     // Magic numbers:
                                     // 9 is the position of the '.' in "templates."
                                     // 10 is the position of the next character.
                                     if (template.startsWith("templates.") && template.lastIndexOf(".") == 9) {
                                         template = template.substring(10);
                                         sender.sendMessage("Template: " + template);
                                     }
                                 }
                             }
                         } else if (args[1].equalsIgnoreCase(cmd_define)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Define a new template
                             if (args.length >= 6) {
                                 Integer corner1X, corner1Y, corner1Z;
                                 try {
                                     // Convert all the numeric arguments
                                     corner1X = Integer.parseInt(args[3]);
                                     corner1Y = Integer.parseInt(args[4]);
                                     corner1Z = Integer.parseInt(args[5]);
                                 } catch (NumberFormatException exception) {
                                     // One or more of the numeric arguments didn't parse
                                     sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
                                     sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                     sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z [WorldID]");
                                     // No useful input, so drop out of the command handler now.
                                     return true;
                                 }
                                 // Save this template into the config
                                 getConfig().set("templates." + args[2] + ".X",corner1X);
                                 getConfig().set("templates." + args[2] + ".Y",corner1Y);
                                 getConfig().set("templates." + args[2] + ".Z",corner1Z);
                                 if (args.length > 6) {
                                     getConfig().set("templates." + args[2] + ".world",args[6]);
                                     sender.sendMessage("Template \"" + args[2] + "\" defined from (" + corner1X.toString() + "," + corner1Y.toString() + "," + corner1Z.toString() + ")");
                                 } else if (sender instanceof Player) {
                                     getConfig().set("templates." + args[2] + ".world",((Player) sender).getWorld().getName());
                                     sender.sendMessage("Template \"" + args[2] + "\" defined from (" + corner1X.toString() + "," + corner1Y.toString() + "," + corner1Z.toString() + ")");
                                 } else {
                                     sender.sendMessage("You are not a player; you must specify a world ID.");
                                 }
                             } else {
                                 // Wrong number of arguments for definition of a template
                                 sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
                                 sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                 sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z [WorldID]");
                             }
                         } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                             if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                 sender.sendMessage("You do not have permission to run this command");
                                 return true;
                             }
                             // Remove a template
                             if (args.length > 2) {
                                 // A name was specified
                                 getConfig().set("templates." + args[2],null);
                                 sender.sendMessage("Removed template: " + args[2]);
                             } else {
                                 // No name was specified
                                 sender.sendMessage("Expecting the name of a template to remove");
                                 sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_remove + " <name>");
                             }
                         } else {
                             // Something other than list, define or remove was provided as a command
                             sender.sendMessage("Not a valid sub-command");
                             sender.sendMessage(cmd_template + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                         }
                     } else {
                         // Not enough arguments following "template" command
                         sender.sendMessage("No sub-command provided");
                         sender.sendMessage(cmd_template + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                     }
                 }
                 return true;
             }
         }
         return false;
     }
 
 }
