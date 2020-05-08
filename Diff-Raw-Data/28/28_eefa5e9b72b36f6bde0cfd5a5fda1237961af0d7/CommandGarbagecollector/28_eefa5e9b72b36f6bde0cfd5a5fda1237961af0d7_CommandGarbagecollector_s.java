 package me.KeybordPiano459.kEssentials.commands;
 
 import java.util.logging.Level;
import java.util.logging.Logger;
 import me.KeybordPiano459.kEssentials.helpers.GarbageCollector;
 import me.KeybordPiano459.kEssentials.kEssentials;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandGarbagecollector extends kCommand implements CommandExecutor {
     public CommandGarbagecollector(kEssentials plugin) {
         super(plugin);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("garbagecollector")) {
             GarbageCollector gc = new GarbageCollector();
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 if (args.length == 0) {
                     if (player.hasPermission("kessentials.garbagecollector")) {
                         if (gc.getMaxMemory() < 0L) {
                             player.sendMessage(RED + "You may be using CACAO Java. Please switch to another JVM.");
                             player.sendMessage(RED + "Some of these values may be negative.");
                         }
                        player.sendMessage(GREEN + "Used Memory Before: " + gc.getUsedMemoryBefore() + " MB");
                        player.sendMessage(GREEN + "Current Memory: " + gc.getCurrentMemory() + " MB");
                        player.sendMessage(GREEN + "Memory Freed: " + gc.getMemoryFreed() + " MB");
                         player.sendMessage(GREEN + "Processors Available: " + gc.getProcessors());
                     } else {
                         noPermissionsMessage(player);
                     }
                 } else {
                     incorrectUsage(player, "/garbagecollector");
                 }
             } else {
                 if (args.length == 0) {
                     if (gc.getMaxMemory() < 0L) {
                         log(Level.WARNING, "You may be using CACAO Java. Please switch to another JVM.");
                         log(Level.WARNING, "Some of these values may be negative.");
                         log(Level.INFO, "");
                     }
                     log(Level.INFO, "Used Memory Before: " + gc.getUsedMemoryBefore() + "MB");
                     log(Level.INFO, "Current Memory: " + gc.getCurrentMemory() + "MB");
                     log(Level.INFO, "Memory Freed: " + gc.getMemoryFreed() + "MB");
                     log(Level.INFO, "Processors Available: " + gc.getProcessors());
                 } else {
                     incorrectUsageC("/garbagecollector");
                 }
             }
         }
         return false;
     }
 }
