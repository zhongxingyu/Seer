 /*
     MazeMania; a minecraft bukkit plugin for managing a maze as an arena.
     Copyright (C) 2012 Plugmania (Sorroko,korikisulda) and contributors.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.mazemania.commands;
 
 import info.plugmania.mazemania.MazeMania;
 import info.plugmania.mazemania.Util;
 import info.plugmania.mazemania.helpers.Trigger;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class MazeCommand implements CommandExecutor {
 
 	public SetCommand setCommand;
 	public ArenaCommand arenaCommand;
 
 	MazeMania plugin;
 
 	public MazeCommand(MazeMania instance) {
 		plugin = instance;
 
 		setCommand = new SetCommand(plugin);
 		arenaCommand = new ArenaCommand(plugin);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (command.getName().equalsIgnoreCase("maze")) {
 			Player player = null;
 			if (sender instanceof Player)
 				player = (Player) sender;
 
 			if (args.length == 0) {
 				sender.sendMessage(Util.formatMessage("---------------- MazeMania Help -----------------"));
 				sender.sendMessage(Util.formatMessage("Player Commands:"));
 				sender.sendMessage(Util.formatMessage("/maze join  - Join the MazeMania game"));
 				sender.sendMessage(Util.formatMessage("/maze leave - Leave the MazeMania game"));
 				sender.sendMessage(Util.formatMessage("/maze about - Show MazeMania credits and info"));
 			}
 
 			if (args.length > 0) {
 				if (args[0].equalsIgnoreCase("set")) {
 					if (!(sender instanceof Player)) {
 						Util.sendMessageNotPlayer(sender);
 						return true;
 					}
 					if (!plugin.util.hasPermMsg(player, "admin")) return true;
 					return setCommand.handle(sender, args);
 
 				} else if (args[0].equalsIgnoreCase("join")) {
 					if (!(sender instanceof Player)) {
 						Util.sendMessageNotPlayer(sender);
 						return true;
 					}
 					if (!plugin.util.hasPermMsg(player, "use")) return true;
 					return arenaCommand.joinHandle(player);
 
 				} else if (args[0].equalsIgnoreCase("leave")) {
 					if (!(sender instanceof Player)) {
 						Util.sendMessageNotPlayer(sender);
 						return true;
 					}
 					if (!plugin.util.hasPermMsg(player, "use")) return true;
 					return arenaCommand.leaveHandle(player);
 
 				} else if (args[0].equalsIgnoreCase("trigger")) {
 					if (!plugin.util.hasPermMsg(player, "admin")) return true;
 					return setCommand.triggerHandle(sender, args);
 					
 				} else if (args[0].equalsIgnoreCase("block")) {
 					if (!plugin.util.hasPermMsg(player, "admin")) return true;
 					if(args.length==1){
 						sender.sendMessage("========================");
 						sender.sendMessage("Block   Event   Args");
 						for (Trigger t:plugin.TriggerManager.getTriggers()){
 				
 							sender.sendMessage(Material.getMaterial(t.blockID).name() + " " + t.effect + " " + t.arguments);
 						}
 						sender.sendMessage("=========================");
 					}else if(args.length==2){
						plugin.TriggerManager.removeTrigger(Material.matchMaterial(args[1]));
 					}else if(args.length==3){
 						plugin.TriggerManager.addTrigger(new Trigger(Material.matchMaterial(args[1]).getId(), args[2], ""));
 					}else if(args.length>=4){
 						plugin.TriggerManager.addTrigger(new Trigger(Material.matchMaterial(args[1]).getId(), args[2], plugin.util.join(args, " ", 3)));
 					}else{
 						
 					}
 				} else if (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("info")) {
 					sender.sendMessage(Util.formatMessage("---------------------- " + Util.pdfFile.getName() + " ----------------------"));
 					sender.sendMessage(Util.formatMessage(plugin.getName() + " developed by " + Util.pdfFile.getAuthors().get(0)));
 					sender.sendMessage(Util.formatMessage("To view more information visit http://plugmania.github.com/ (<-- You can click it!)"));
 				}
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 }
