 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.circuit;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author andre
  */
 public class CommandList implements CommandExecutor {
     
     public Circuit plugin;
     
     public CommandList(Circuit aPlugin) {
         plugin = aPlugin;
     }
 
     @Override
     public boolean onCommand(CommandSender aCommandSender, Command aCommand, String aString, String[] aStrings) {
         String lName = "";
         String lWorldName = "";
         Player lPlayer = null;
         if (aStrings.length > 0) {
             lName = aStrings[0];
         }
         if (aStrings.length > 1) {
             lWorldName = aStrings[1];
         }
         if (aCommandSender instanceof Player) {
             lPlayer = (Player)aCommandSender;
             if (lName.isEmpty()) lName = lPlayer.getName();
             if (lWorldName.isEmpty()) lWorldName = lPlayer.getWorld().getName();
         } else {
             if (lName.isEmpty()) lName = "all";
             if (lWorldName.isEmpty()) lWorldName = plugin.getServer().getWorlds().get(0).getName();
         }
         if (lName.equals("types")) {
             for(CircuitHandler lHandler : plugin.circuitHandlers.values()) {
                 String lLine = lHandler.typeName + " " + lHandler.name + " " + lHandler.description;
                 if (lPlayer != null) {
                     lPlayer.sendMessage(lLine);
                 } else {
                     plugin.getLogger().info(lLine);
                 }
             }
         } else {
             for(CircuitBuilding lCircuit : plugin.DBs.getDB(lWorldName)) {
                if (lName.equals("all") || lCircuit.playerName.equals(lName)) {
                    String lLine = lCircuit.circuitType + " " + lCircuit.getName() + " " + lCircuit.playerName + " @ " + lCircuit.edge1;
                    if (lPlayer != null) {
                        lPlayer.sendMessage(lLine);
                    } else {
                        plugin.getLogger().info(lLine);
                    }
                 }
             }
         }
         return true;
     }
 }
