 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import java.io.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 
 /**
  * Handles the various multi-world operations.
  *
  * @author UndeadScythes
  */
 public class WorldCmd extends CommandHandler {
     @Override
     public final void playerExecute() {
         if(argsLength() == 1) {
             if(subCmdEquals("list")) {
                 player().sendNormal("Available worlds:");
                 String worldList = "";
                 for(World world : Bukkit.getWorlds()) {
                     worldList = worldList.concat(", " + world.getName());
                 }
                 player().sendText(worldList.substring(2));
             } else if(subCmdEquals("setspawn")) {
                 UDSPlugin.setWorldSpawn(player().getLocation());
                 player().sendNormal("Spawn location of world " + player().getWorld().getName() + " set.");
             } else if(subCmdEquals("info")) {
                 sendInfo(player().getWorld());
             } else {
                 subCmdHelp();
             }
         } else if(argsLength() == 2) {
             if(subCmdEquals("tp")) {
                 final World world = Bukkit.getWorld(arg(1));
                 if(world != null) {
                    player().teleport(UDSPlugin.getWorldSpawn(player().getWorld()));
                 } else {
                     player().sendError("That world does not exist.");
                 }
             } else if(subCmdEquals("create")) {
                 if(noBadLang(arg(1)) && noWorldExists(arg(1))) {
                     player().sendNormal("Generating spawn area...");
                     Bukkit.createWorld(new WorldCreator(arg(1)));
                     UDSPlugin.getData().newWorld(arg(1));
                     player().sendNormal("World created.");
                 }
             } else if(subCmdEquals("forget")) {
                 if(arg(1).equals(Config.MAIN_WORLD)) {
                     player().sendError("This world cannot be forgotten.");
                 }
                 final World world = getWorld(arg(1));
                 if(world != null) {
                     UDSPlugin.getData().getWorlds().remove(arg(1));
                     player().sendNormal("World forgotten.");
                 }
             } else if(subCmdEquals("delete")) {
                 if(arg(1).equals(Config.MAIN_WORLD)) {
                     player().sendError("This world cannot be deleted.");
                 }
                 final World world = getWorld(arg(1));
                 if(world != null) {
                     for(Player worldPlayer : world.getPlayers()) {
                         PlayerUtils.getOnlinePlayer(worldPlayer.getName()).sendNormal("That world is no longer safe.");
                         worldPlayer.teleport(UDSPlugin.getData().getSpawn());
                     }
                     UDSPlugin.getData().getWorlds().remove(arg(1));
                     final String worldName = world.getName();
                     Bukkit.unloadWorld(worldName, false);
                     final File file = new File(worldName);
                     if(deleteFile(file)) {
                         player().sendNormal("World deleted.");
                     } else {
                         player().sendError("World files could not be completely removed.");
                     }
                 }
             } else if(subCmdEquals("info")) {
                 final World world = getWorld(arg(1));
                 if(world != null) {
                     sendInfo(world);
                 }
             } else if(subCmdEquals("flag")) {
                 setFlag(player().getWorld(), arg(1));
             } else if(subCmdEquals("mode")) {
                 setMode(player().getWorld(), arg(1));
             } else {
                 subCmdHelp();
             }
         } else if(numArgsHelp(3)) {
             if(subCmdEquals("flag")) {
                 final World world = getWorld(arg(1));
                 if(world != null) {
                     setFlag(world, arg(2));
                 }
             } else if(subCmdEquals("mode")) {
                 final World world = getWorld(arg(1));
                 if(world != null) {
                     setMode(world, arg(2));
                 }
             } else {
                 subCmdHelp();
             }
         }
     }
 
     private void sendInfo(final World world) {
         player().sendNormal("World " + world.getName() + " info:");
         player().sendText("Game mode: " + UDSPlugin.getWorldMode(world).toString().toLowerCase());
         String flagString = "";
         for(WorldFlag test : WorldFlag.values()) {
             if(UDSPlugin.checkWorldFlag(world, test)) {
                 flagString = flagString.concat(test.toString() + ", ");
             }
         }
         for(RegionFlag test : RegionFlag.values()) {
             if(UDSPlugin.checkWorldFlag(world, test)) {
                 flagString = flagString.concat(test.toString() + ", ");
             }
         }
         if("".equals(flagString)) {
             player().sendText("No flags.");
         } else {
             player().sendText("Flags: " + flagString.substring(0, flagString.length() - 2));
         }
     }
 
     private void setFlag(final World world, final String flagName) {
         final Flag flag = getWorldFlag(flagName);
         if(flag != null) {
             player().sendNormal(world.getName() + " flag " + flag.toString() + " now set to " + UDSPlugin.toggleWorldFlag(world, flag) + ".");
         }
     }
 
     private void setMode(final World world, final String modeName) {
         final GameMode mode = getGameMode(modeName);
         if(mode != null) {
             UDSPlugin.changeWorldMode(world, mode);
             player().sendNormal(world.getName() + " game mode now set to " + mode.toString() + ".");
         }
     }
 
     private boolean deleteFile(final File file) {
         if(file.delete()) {
             return true;
         } else {
             if(file.isDirectory()) {
                 for(File subFile : file.listFiles()) {
                     subFile.delete();
                 }
             }
         }
         return file.delete();
     }
 
     private boolean noWorldExists(final String name) {
         if(Bukkit.getWorld(name) != null) {
             player().sendError("A world already exists with that name.");
             return false;
         } else {
             return true;
         }
     }
 
     private World getWorld(final String name) {
         final World world = Bukkit.getWorld(name);
         if(world == null) {
             player().sendError("That world does not exist.");
         }
         return world;
     }
 
     private GameMode getGameMode(final String name) {
         GameMode mode = null;
         for(GameMode test : GameMode.values()) {
             if(test.toString().equals(name.toUpperCase())) {
                 mode = test;
             }
             if(name.matches("[0-9]*") && test.getValue() == Integer.parseInt(name)) {
                 mode = test;
             }
         }
         if(mode == null) {
             player().sendError("That is not a valid game mode.");
         }
         return mode;
     }
 }
