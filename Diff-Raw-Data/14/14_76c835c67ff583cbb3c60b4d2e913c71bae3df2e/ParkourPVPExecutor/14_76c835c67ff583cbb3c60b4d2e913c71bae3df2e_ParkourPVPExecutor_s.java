 package com.rojel.parkourpvp.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.rojel.parkourpvp.ParkourPVP;
 import com.rojel.parkourpvp.data.Room;
 import com.rojel.parkourpvp.managers.RoomManager;
 import com.rojel.pluginsignapi.PluginSignAPI;
 
 public class ParkourPVPExecutor implements CommandExecutor {
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(args.length == 0) {
 			sender.sendMessage("3/ppvp room [room] create rCreates a new room with the name [room]");
 			sender.sendMessage("3/ppvp room [room] lobby rSets the lobby of [room] to your current position");
 			sender.sendMessage("3/ppvp room [room] spawn rSets the spawn of [room] to your current position");
 			sender.sendMessage("3/ppvp room [room] goal rSets the goal of [room] to your current position");
 			sender.sendMessage("3/ppvp room [room] delete rDeletes the room with the name [room]");
 			
 			sender.sendMessage("3/ppvp rooms rLists all rooms");
 			
 			sender.sendMessage("3/ppvp lobby rSets the room selection lobby to your current position");
 			return true;
 		} else if(args[0].equalsIgnoreCase("room") && args.length == 3) {
 			String name = args[1];
 			String task = args[2];
 			
 			if(task.equalsIgnoreCase("create")) {
 				RoomManager.createRoom(name);
 				sender.sendMessage("3Room r" + name + " 3created.");
 				
 				PluginSignAPI.updateSigns();
 				return true;
 			} else if(task.equalsIgnoreCase("lobby")) {
 				if(sender instanceof Player) {
 					Room room = RoomManager.getRoom(name);
 					
 					if(room != null) {
 						room.setLobby(((Player) sender).getLocation());
 						sender.sendMessage("3Lobby for r" + name + " 3set.");
 					} else {
 						sender.sendMessage("3Room r" + name + " 3does not exist.");
 					}
 				} else {
 					sender.sendMessage("You have to run this command in-game.");
 				}
 				
 				return true;
 			} else if(task.equalsIgnoreCase("spawn")) {
 				if(sender instanceof Player) {
 					Room room = RoomManager.getRoom(name);
 					
 					if(room != null) {
 						room.addSpawn(((Player) sender).getLocation());
 						sender.sendMessage("3Spawn for r" + name + " 3added.");
 					} else {
 						sender.sendMessage("3Room r" + name + " 3does not exist.");
 					}
 				} else {
 					sender.sendMessage("You have to run this command in-game.");
 				}
 				
 				return true;
 			} else if(task.equalsIgnoreCase("goal")) {
 				if(sender instanceof Player) {
 					Room room = RoomManager.getRoom(name);
 					
 					if(room != null) {
 						room.setGoal(((Player) sender).getLocation());
 						sender.sendMessage("3Goal for r" + name + " 3set.");
 					} else {
 						sender.sendMessage("3Room r" + name + " 3does not exist.");
 					}
 				} else {
 					sender.sendMessage("You have to run this command in-game.");
 				}
 				
 				return true;
 			} else if(task.equalsIgnoreCase("delete")) {
 				Room room = RoomManager.getRoom(name);
 				
 				if(room != null) {
 					RoomManager.removeRoom(room);
 					sender.sendMessage("3Room r" + name + " 3deleted.");
 					return true;
 				} else {
 					sender.sendMessage("3Room r" + name + " 3does not exist.");
 				}
 				
 				return true;
 			}
 		} else if(args[0].equalsIgnoreCase("rooms") && args.length == 1) {
 			if(RoomManager.getRooms().size() == 0)
 				sender.sendMessage("3No rooms saved.");
 			else
 				sender.sendMessage("============3ROOMSr============");
 			for(Room room : RoomManager.getRooms()) {
 				String line = room.getName() + "3: " + room.getState().name() + ", " + room.getPlayerCount() + "/" + Room.MAX_PLAYERS;
 				if(room.getLobby() == null)
 					line = line + ", cLOBBY MISSINGr";
				if(room.getSpawn() == null)
 					line = line + ", cSPAWN MISSINGr";
 				if(room.getGoal() == null)
 					line = line + ", cGOAL MISSINGr";
 				
 				sender.sendMessage(line);
 			}
 			
 			return true;
 		} else if(args[0].equalsIgnoreCase("lobby") && args.length == 1) {
 			if(sender instanceof Player) {
 				ParkourPVP.setLobby(((Player) sender).getLocation());
 				sender.sendMessage("3Lobby position set.");
 			} else {
 				sender.sendMessage("You have to run this command in-game.");
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 }
