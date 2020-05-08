 package com.cianmcgovern.simpleprotect;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import java.io.FileReader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.bukkit.entity.Player;
 /**
  * SimpleProtect for Bukkit
  *
  * @author Philip Daian, Cian Mc Govern
  */
 public class SimpleProtect extends JavaPlugin {
     protected final SimpleProtectPlayerListener playerListener = new SimpleProtectPlayerListener(this);
     protected final SimpleProtectBlockListener blockListener = new SimpleProtectBlockListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     protected ArrayList<String> protectors;
     
     public void onEnable() {
         // TODO: Place any custom enable code here including the registration of any events
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
     	
     	String f = "plugins/SimpleProtect/protectors.txt";
     	File in = new File(f);
     	if(in.exists()!=true){
     		try {
     		System.out.println("SimpleProtect: No protectors.txt file found, creating blank default now!!");
     		in.createNewFile();
     		}
     		catch (IOException e){
     			System.out.println("SimpleProtect: Error creating protectors.txt file!!");
     			e.printStackTrace();
     		}
     	}
         
         try {
             protectors = readLines("plugins/SimpleProtect/protectors.txt");
         } catch (IOException ex) {
             Logger.getLogger(SimpleProtect.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         //SimpleProtectPlayerListener.load();
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
         pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.High, this);
         pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.High, this);
         pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High, this);
         pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.High, this);
         
         System.out.println("SimpleProtect Version 2.1 by cian1500ww is enabled!!");
     }
     public void onDisable() {
         // TODO: Place any custom disable code here
 
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         System.out.println("Disabling SimpleProtect!");
         SimpleProtectPlayerListener.save();
     }
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
     private ArrayList<String> readLines(String filename) throws IOException {
         FileReader fileReader = new FileReader(filename);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         ArrayList<String> lines = new ArrayList<String>();
         String line = null;
         while ((line = bufferedReader.readLine()) != null) {
             lines.add(line.toLowerCase());
         }
         bufferedReader.close();
         return lines;
     }
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
     	String[] trimmedArgs = args;
     	String commandName = command.getName().toLowerCase();
         Player player = (Player)sender;
     	
        if (protectors.contains(player.getName())) {
             
         	// Entering protect mode
         	if (commandName.equalsIgnoreCase("pm")) {
                 PlayerInfo pinfo = SimpleProtectPlayerListener.findProtecting(player.getName());
                 if (pinfo != null) {
                 	player.sendMessage("No longer in protect mode.");
                     SimpleProtectPlayerListener.pMode.remove(pinfo);
                 } else {
                     player.sendMessage("Now in protect mode.");
                     SimpleProtectPlayerListener.pMode.add(new PlayerInfo(player.getName()));
                 }
                 
                 return true;
             } 
         	
         	// Carrying out cinfo command
         	else if (commandName.equalsIgnoreCase("pinfo")) {
                 AimBlock aiming = new AimBlock(player);
                 Block block = aiming.getTargetBlock();
                 if (block == null) {
                     player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                 } else {
                     int[] coords = {block.getX(), block.getY() + 1, block.getZ()};
                     ArrayList<String> resp = SimpleProtectPlayerListener.getProtection(coords);
                     if (resp == null) {
                         player.sendMessage("Not a protected area.");
                     } else {
                         player.sendMessage("Protected to " + SimpleProtectPlayerListener.arrayToString(resp));
                     }
                 }
                 
                 return true;
             } 
         	
         	// Carrying out c1 and c2 command
         	else if (commandName.equalsIgnoreCase("c1") || commandName.equalsIgnoreCase("c2")) {
                 PlayerInfo pinfo = SimpleProtectPlayerListener.findProtecting(player.getName());
                 if (pinfo == null) {
                     player.sendMessage(ChatColor.RED + "Not in protect mode.");
                 } else {
                     AimBlock aiming = new AimBlock(player);
                     Block block = aiming.getTargetBlock();
                     if (block == null) {
                         player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                     } else {
                         int[] coords = {block.getX(), block.getY() + 1, block.getZ()};
                         player.sendMessage("Selecting block " + coords[0] + ", " + coords[1] + ", " + coords[2]);
                         if (commandName.equalsIgnoreCase("c1")) {
                             pinfo.setC1(coords);
                         } else {
                             pinfo.setC2(coords);
                         }
                     }
                 }
                 
                 return true;
             } 
         	
         	//Carrying out cstatus command
         	else if (commandName.equalsIgnoreCase("cstatus")) {
                 PlayerInfo pinfo = SimpleProtectPlayerListener.findProtecting(player.getName());
                 if (pinfo == null) {
                     player.sendMessage(ChatColor.RED + "Not in protect mode");
                 } else {
                     player.sendMessage(ChatColor.GREEN + "C1: " + SimpleProtectPlayerListener.arrayToString(pinfo.getC1()) + " C2: " + SimpleProtectPlayerListener.arrayToString(pinfo.getC2()));
                 }
                 
                 return true;
             } 
         	
         	// Carrying out cdel command
         	else if (commandName.equalsIgnoreCase("cdel")) {
                 AimBlock aiming = new AimBlock(player);
                 Block block = aiming.getTargetBlock();
                 if (block == null) {
                     player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                 } else {
                     int[] coords = {block.getX(), block.getY() + 1, block.getZ()};
                     for (int i = 0; i < SimpleProtectPlayerListener.areas.size(); i++) {
                         ArrayList<String> resp = SimpleProtectPlayerListener.getProtection(coords);
                         if (resp == null) {
                             player.sendMessage(ChatColor.RED + "No protected area here");
                         } else {
                             for (int z = 0; z < SimpleProtectPlayerListener.areas.size(); z++) {
                                 if (SimpleProtectPlayerListener.areas.get(z).contains(coords)) {
                                 	SimpleProtectPlayerListener.areas.remove(SimpleProtectPlayerListener.areas.get(z));
                                     player.sendMessage(ChatColor.RED + "Protected area removed");
                                 }
                             }
                         }
                     }
                 }
                 
                 return true;
             } 
         	
         	// Checking to see if player added arguments and carrying out cadd, callow or cdisallow for the args/player
         	else if (trimmedArgs.length > 0) {
                 if (commandName.equalsIgnoreCase("cadd")) {
                     PlayerInfo pinfo = SimpleProtectPlayerListener.findProtecting(player.getName());
                     if (pinfo == null) {
                         player.sendMessage("Error: You are not in protect mode");
                     } //else if (!Arrays.equals(pinfo.getC1(), SimpleProtectPlayerListener.ZERO) && !Arrays.equals(pinfo.getC2(), SimpleProtectPlayerListener.ZERO)) {
                     else if (!Arrays.equals(pinfo.getC1(), SimpleProtectPlayerListener.ZERO) && !Arrays.equals(pinfo.getC2(), SimpleProtectPlayerListener.ZERO)) {
                         player.sendMessage("Protection to " + trimmedArgs[0] + " successful.");
                         ProtectedArea t = new ProtectedArea(pinfo.getC1(), pinfo.getC2(), trimmedArgs[0]);
                         SimpleProtectPlayerListener.areas.add(t);
                         SimpleProtectPlayerListener.save();
                     } else {
                         player.sendMessage("You must select two coordinates to protect.");
                     }
                     
                     return true;
                 }
                 
                 else if (commandName.equalsIgnoreCase("callow")) {
                     AimBlock aiming = new AimBlock(player);
                     Block block = aiming.getTargetBlock();
                     if (block == null) {
                         player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                     } else {
                         int[] coords = {block.getX(), block.getY() + 1, block.getZ()};
                         for (int i = 0; i < SimpleProtectPlayerListener.areas.size(); i++) {
                             if (SimpleProtectPlayerListener.areas.get(i).contains(coords)) {
                                 player.sendMessage(ChatColor.RED + "Player added.");
                                 SimpleProtectPlayerListener.areas.get(i).addPlayer(trimmedArgs[0]);
                             }
                         }
                     }
                     SimpleProtectPlayerListener.save();
                     return true;
                 } 
                 
                 
                 else if (commandName.equalsIgnoreCase("cdisallow")) {
                     AimBlock aiming = new AimBlock(player);
                     Block block = aiming.getTargetBlock();
                     if (block == null) {
                         player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                     } else {
                         int[] coords = {block.getX(), block.getY() + 1, block.getZ()};
                         for (int i = 0; i < SimpleProtectPlayerListener.areas.size(); i++) {
                             if (SimpleProtectPlayerListener.areas.get(i).contains(coords)) {
                                 player.sendMessage(ChatColor.RED + "Player removed.");
                                 SimpleProtectPlayerListener.areas.get(i).removePlayer(trimmedArgs[0]);
                             }
                         }
                     }
                     SimpleProtectPlayerListener.save();
                     return true;
                 }
                 
                 else {
                     return false;
                 }
             
         	} 
         	
         	else {
                 return false;
             }
         
         }
     	
     	return false;
     }
    
 }
 
