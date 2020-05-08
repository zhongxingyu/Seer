 package com.imjake9.simplejail;
 
 import com.imjake9.simplejail.SimpleJail.JailMessage;
 import com.imjake9.simplejail.api.JailInfo;
 import com.imjake9.simplejail.api.SimpleJailCommandListener;
 import com.imjake9.simplejail.api.SimpleJailCommandListener.HandleStatus;
 import com.imjake9.simplejail.api.SimpleJailCommandListener.Priority;
 import com.imjake9.simplejail.utils.Messaging;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SimpleJailCommandHandler implements CommandExecutor {
     
     private final SimpleJail plugin;
     
     private Map<Priority, List<SimpleJailCommandListener>> commandListeners = new EnumMap<Priority, List<SimpleJailCommandListener>>(Priority.class);
     
     public SimpleJailCommandHandler(SimpleJail plugin) {
         this.plugin = plugin;
         
         this.registerCommands();
     }
     
     private void registerCommands() {
         plugin.getCommand("jail").setExecutor(this);
         plugin.getCommand("unjail").setExecutor(this);
         plugin.getCommand("setjail").setExecutor(this);
         plugin.getCommand("setunjail").setExecutor(this);
         plugin.getCommand("jailtime").setExecutor(this);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         
         if(commandLabel.equalsIgnoreCase("jail")) {
             
             if(!hasPermission(sender, "simplejail.jail")) {
                 Messaging.send(JailMessage.LACKS_PERMISSIONS, sender, "simplejail.jail");
                 return true;
             }
             
             HandleStatus status = this.dispatchCommandToListeners(sender, commandLabel, args);
             if (status == HandleStatus.SUCCESS)
                 return true;
             if (status == HandleStatus.FAILURE)
                 return false;
             
             if (args.length != 1 && args.length != 2) return false;
             
             // Catch JailExceptions
             JailInfo info;
             int time = 0;
             try {
                 
                 // Jail/tempjail player:
                 if (args.length == 1) 
                     info = plugin.jailPlayer(args[0], sender.getName());
                 else {
                     time = plugin.parseTimeString(args[1]);
                     info = plugin.jailPlayer(args[0], sender.getName(), time);
                 }
                 
             } catch (JailException ex) {
                 
                 // Send error message
                 sender.sendMessage(ex.getFormattedMessage());
                 return true;
                 
             }
             
             // Send success message:
             if (args.length == 1)
                 Messaging.send(JailMessage.JAIL, sender, info.getJailee());
             else
                 Messaging.send(JailMessage.TEMPJAIL, sender, info.getJailee(), plugin.prettifyMinutes(time));
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("unjail")) {
             
             if(!hasPermission(sender, "simplejail.unjail")) {
                 Messaging.send(JailMessage.LACKS_PERMISSIONS, sender, "simplejail.unjail");
                 return true;
             }
             
             HandleStatus status = this.dispatchCommandToListeners(sender, commandLabel, args);
             if (status == HandleStatus.SUCCESS)
                 return true;
             if (status == HandleStatus.FAILURE)
                 return false;
             
             if (args.length != 1) return false;
             
             // Catch JailExceptions
             JailInfo info;
             try {
                 
                 // Unjail player
                 info = plugin.unjailPlayer(args[0]);
                 
             } catch (JailException ex) {
                 
                 // Send error message
                 sender.sendMessage(ex.getFormattedMessage());
                 return true;
                 
             }
             
             // Send success message
             Messaging.send(JailMessage.UNJAIL, sender, info.getJailee());
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("setjail")) {
             
             if(!hasPermission(sender, "simplejail.setjail")) {
                 Messaging.send(JailMessage.LACKS_PERMISSIONS, sender, "simplejail.setjail");
                 return true;
             }
             
             if (args.length != 0 && args.length != 4) return false;
             
             // If not a player, only use explicit notation:
             if (!(sender instanceof Player) && args.length != 4) {
                 Messaging.send(JailMessage.ONLY_PLAYERS, sender);
                 return true;
             }
             
             Location loc;
             
             // Use explicit vs current location:
             if (args.length == 0) {
                 
                 Player player = (Player)sender;
                 loc = player.getLocation();
                 
             } else {
                 
                 // Check if passed coordinates are correct:
                 if (!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                     Messaging.send(JailMessage.INVALID_COORDINATE, sender);
                     return true;
                 }
                 
                 // Set up Location value
                 loc = new Location(
                         plugin.getServer().getWorld(args[3]),
                         Integer.parseInt(args[0]),
                         Integer.parseInt(args[1]),
                         Integer.parseInt(args[2]));
                 
             }
             
             // Set the jail point
             plugin.setJail(loc);
             
             // Send success message
             Messaging.send(JailMessage.JAIL_POINT_SET, sender);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("setunjail")) {
             
             if(!hasPermission(sender, "simplejail.setjail")) {
                 Messaging.send(JailMessage.LACKS_PERMISSIONS, sender, "simplejail.setjail");
                 return true;
             }
             
             if (args.length != 0 && args.length != 4) return false;
             
             // If not a player, only use explicit notation:
             if (!(sender instanceof Player) && args.length != 4) {
                 Messaging.send(JailMessage.ONLY_PLAYERS, sender);
                 return true;
             }
             
             Location loc;
             
             // Use explicit vs current location:
             if (args.length == 0) {
                 
                 Player player = (Player)sender;
                 loc = player.getLocation();
                 
             } else {
                 
                 // Check if passed coordinates are correct:
                 if (!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                     Messaging.send(JailMessage.INVALID_COORDINATE, sender);
                     return true;
                 }
                 
                 // Set up Location value
                 loc = new Location(
                         plugin.getServer().getWorld(args[3]),
                         Integer.parseInt(args[0]),
                         Integer.parseInt(args[1]),
                         Integer.parseInt(args[2]));
                 
             }
             
             // Set the jail point
             plugin.setUnjail(loc);
             
             // Send success message
             Messaging.send(JailMessage.UNJAIL_POINT_SET, sender);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("jailtime")) {
             
             if(!hasPermission(sender, "simplejail.jailtime")) {
                 Messaging.send(JailMessage.LACKS_PERMISSIONS, sender, "simplejail.jailtime");
                 return true;
             }
             
             if (args.length > 1) return false;
             
             // If not a player, a target must be explicit:
             if (!(sender instanceof Player) && args.length == 0) {
                 Messaging.send(JailMessage.MUST_SPECIFY_TARGET, sender);
                 return true;
             }
             
             // Get the target
             Player player = (args.length == 0) ? (Player) sender : plugin.getServer().getPlayer(args[0]);
            args[0] = player == null ? args[0].toLowerCase() : player.getName().toLowerCase();
             
             // Validate target:
            if (!plugin.playerIsJailed(args[0])) {
                Messaging.send(JailMessage.NOT_IN_JAIL, sender, args[0]);
                 return true;
             }
             
             if (!plugin.playerIsTempJailed(player)) {
                Messaging.send(JailMessage.NOT_TEMPJAILED, sender, (args.length == 0) ? sender.getName() : args[0]);
                 return true;
             }
             
             // Send success message:
             int minutes = (int) ((plugin.getTempJailTime(player) - System.currentTimeMillis()) / 60000);
             Messaging.send(JailMessage.JAIL_TIME, sender, plugin.prettifyMinutes(minutes));
  
             return true;
             
         } else {
             return false;
         }
         
     }
     
     /**
      * Checks any CommandSender for a permission node.
      * 
      * @param sender
      * @param permission
      * @return 
      */
     private boolean hasPermission(CommandSender sender, String permission) {
         if (sender instanceof Player)
             return sender.hasPermission(permission);
         else return true;
     }
     
     /**
      * Dispatches a particular command to all listeners, ordered by priority.
      * 
      * @param sender
      * @param command
      * @param args
      * @return 
      */
     private HandleStatus dispatchCommandToListeners(CommandSender sender, String command, String args[]) {
         HandleStatus handled = HandleStatus.UNHANDLED;
         execute:
         for (Priority priority : commandListeners.keySet()) {
             if (priority.equals(Priority.MONITOR)) break;
             for (SimpleJailCommandListener listener : commandListeners.get(priority)) {
                 HandleStatus status = listener.handleJailCommand(sender, command, args);
                 if (status != HandleStatus.UNHANDLED) {
                     handled = status;
                     break execute;
                 }
             }
         }
         if (commandListeners.containsKey(Priority.MONITOR))
             for (SimpleJailCommandListener listener : commandListeners.get(Priority.MONITOR)) {
                 listener.handleJailCommand(sender, command, args);
             }
         return handled;
     }
     
     /**
      * Registers a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void addListener(SimpleJailCommandListener listener, Priority priority) {
         if (!commandListeners.containsKey(priority))
             commandListeners.put(priority, new ArrayList<SimpleJailCommandListener>());
         commandListeners.get(priority).add(listener);
     }
     
     /**
      * Unregisters a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void removeListener(SimpleJailCommandListener listener, Priority priority) {
         if (!commandListeners.containsKey(priority)) return;
         if (!commandListeners.get(priority).contains(listener)) return;
         commandListeners.get(priority).remove(listener);
     }
     
 }
