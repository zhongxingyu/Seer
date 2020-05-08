 package littlegruz.autoruncommands.commands;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import littlegruz.autoruncommands.CommandMain;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class Commands implements CommandExecutor{
 private CommandMain plugin;
    
    public Commands(CommandMain instance){
       plugin = instance;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
       if(commandLabel.compareToIgnoreCase("addacommand") == 0){
          if(sender.hasPermission("autoruncommands.addcommand")){
             if(args.length > 1){
                String id;
                String command;
                if(plugin.getCommandMap().get(args[0] + "[op]") != null){
                   sender.sendMessage("An op command with that name already exists");
                   return true;
                }
                id = args[0];
                command = args[1];
                for(int i = 2; i < args.length; i++){
                   command += " " + args[i];
                }
                if(plugin.getCommandMap().put(id, command) != null)
                   sender.sendMessage("Overwrote old command");
                else
                   sender.sendMessage("Command added");
             }
             else
                sender.sendMessage("An identifier and command must be given");
          }
       }
       else if(commandLabel.compareToIgnoreCase("addopcommand") == 0){
          if(sender.hasPermission("autoruncommands.addopcommand")){
             if(args.length > 1){
                String id;
                String command;
                if(plugin.getCommandMap().get(args[0]) != null){
                   sender.sendMessage("A non-op command with that name already exists");
                   return true;
                }
                id = args[0] + "[op]";
                command = args[1];
                for(int i = 2; i < args.length; i++){
                   command += " " + args[i];
                }
                if(plugin.getCommandMap().put(id, command) != null)
                   sender.sendMessage("Overwrote old op command");
                else
                   sender.sendMessage("Op command added");
             }
             else
                sender.sendMessage("An identifier and command must be given");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removeacommand") == 0){
          if(sender.hasPermission("autoruncommands.removecommand")){
             if(args.length == 1){
                boolean remove = false;
                String rmCommand = args[0];
                
                //Check if the command to be removed exists or is op
                if(plugin.getCommandMap().remove(rmCommand) != null)
                   remove = true;
                else if(plugin.getCommandMap().remove(rmCommand + "[op]") != null){
                   rmCommand += "[op]";
                   remove = true;
                }
                   
                if(remove){
                   ArrayList<String> names = new ArrayList<String>();
                   // Remove the command where it is associated with players
                   Iterator<Map.Entry<String, String>> it1 = plugin.getPlayerClickMap().entrySet().iterator();
                   while(it1.hasNext()){
                      Entry<String, String> mp1 = it1.next();
                      if(mp1.getValue().compareTo(rmCommand) == 0)
                         names.add(mp1.getKey());
                   }
                   for(int i = 0; i < names.size(); i++)
                      plugin.getPlayerClickMap().remove(names.get(i));
                   names.clear();
 
                   // Remove the command where it is associated with blocks
                   ArrayList<Location> places = new ArrayList<Location>();
                   Iterator<Map.Entry<Location, String>> it2 = plugin.getBlockCommandMap().entrySet().iterator();
                   while(it2.hasNext()){
                      Entry<Location, String> mp2 = it2.next();
                      if(mp2.getValue().compareTo(rmCommand) == 0)
                         places.add(mp2.getKey());
                   }
                   for(int i = 0; i < places.size(); i++)
                      plugin.getBlockCommandMap().remove(places.get(i));
                   places.clear();
                   
                   // Remove the command where it is associated with the death of players
                   it1 = plugin.getPlayerDeathMap().entrySet().iterator();
                   while(it1.hasNext()){
                      Entry<String, String> mp1 = it1.next();
                      if(mp1.getValue().compareTo(rmCommand) == 0)
                         names.add(mp1.getKey());
                   }
                   for(int i = 0; i < names.size(); i++)
                      plugin.getPlayerDeathMap().remove(names.get(i));
                   names.clear();
 
                   // Remove the command from the repeating list
                   plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "removerepeatcommand " + rmCommand);
                   
                   // Remove the command from the respawn list
                  it1 = plugin.getPlayerRespawnMap().entrySet().iterator();
                  while(it1.hasNext()){
                     Entry<String, String> mp1 = it1.next();
                     if(mp1.getValue().compareTo(rmCommand) == 0)
                        names.add(mp1.getKey());
                  }
                  for(int i = 0; i < names.size(); i++)
                     plugin.getPlayerRespawnMap().remove(names.get(i));
                  names.clear();
                  names.trimToSize();
 
                   // Remove the command from the join list
                  if(plugin.getPlayerJoinCommand().compareTo(rmCommand) == 0){
                     plugin.setPlayerJoinCommand("chuckTesta");
                     sender.sendMessage("Join command removed");
                  }
 
                   // Remove the command from the start up list
                   plugin.setStartupCommands(plugin.getStartupCommands().replace(":" + rmCommand, ""));
                   
                   sender.sendMessage("Command removed");
                }
                else
                   sender.sendMessage("No command was found with that identifer");
             }
             else
                sender.sendMessage("No command identifier given");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("displaycommands") == 0){
          if(sender.hasPermission("autoruncommands.displaycommands")){
             sender.sendMessage("Identifier | Command");
             Iterator<Map.Entry<String, String>> it = plugin.getCommandMap().entrySet().iterator();
             while(it.hasNext()){
                Entry<String, String> mp = it.next();
                sender.sendMessage(mp.getKey() + " | /" + mp.getValue());
             }
          }
       }
       return true;
    }
 
 }
