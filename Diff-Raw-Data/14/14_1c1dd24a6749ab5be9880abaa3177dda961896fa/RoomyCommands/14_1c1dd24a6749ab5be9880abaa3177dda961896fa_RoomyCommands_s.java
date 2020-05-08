 package net.cheesier.Roomy;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 public class RoomyCommands {
 	private static Logger log = Logger.getLogger("Minecraft");
 
 	// Say something like [Roomy] before we try talk to the commandline
 	private static String infoName = "[" + Roomy.getPlugin().getDescription().getName() + "]";
 
 
 	// Not used for now, waiting for the new system of permissions, if there is one...
 	public static boolean hasRights(Player player, String wrote, String command, String access) {
 		if (command.equalsIgnoreCase(wrote)) {
 			if (!Roomy.Permissions.has(player, access)) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return false;
 			}
 			else { // you have access
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * This might be consitered some ugly looking code, i know it it and will fix it when i use the new permission system.
 	 * 
 	 * Hopefully soon :)
 	 */
 
 	// setroom with 2 known vectors, that was defined by the RoomyTool
 	public static boolean setroomCmd(CommandSender sender, String[] args) {
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			if (!Roomy.Permissions.has(player, "Roomy.manage.set")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 			HashMap<Player, Vector> pre1 = Roomy.preSaved1;
 			HashMap<Player, Vector> pre2 = Roomy.preSaved2;
 
 			if (args.length == 1) {
 				if (pre1.containsKey(player) && pre1.containsKey(player)) {
 					//int id = Roomy.savedRooms.size();
 					Roomy.savedRooms.add(new SavedRoom(args[0], pre1.get(player), pre2.get(player)));
 					player.sendMessage("Saved room: " + args[0]);
 					return true;
 				}
 				else { // player do not have 2 positions of the room
 					player.sendMessage("Set 2 positions first, with the wand tool");
 					return true;
 				}
 			}
 			else { // wrong number of arguments
 				return false;
 			}
 		}
 		else {
 			System.out.println("This command cannot be used from a commandline.");
 			return true;
 		}
 	}
 
 	// To active the anounciation when a player enters a room
 	public static boolean trackCmd(CommandSender sender, String[] args) {		
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			if (!Roomy.Permissions.has(player, "Roomy.track")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 
 			Roomy.track = !Roomy.track; // change the setting
 			Roomy.getPlugin().getServer().broadcastMessage("Tracking " + (Roomy.track? "2enabled": "cdisabled"));
 			System.out.println("Tracking " + (Roomy.track?"enabled":"disabled"));
 
 			return true;
 		}
 		else { //Console or something like that
 			Roomy.track = !Roomy.track; // change the setting
 			Roomy.getPlugin().getServer().broadcastMessage("Tracking " + (Roomy.track?"2enabled":"cdisabled"));
 			System.out.println("Tracking " + (Roomy.track?"enabled":"disabled"));
 			return true;
 		}
 	}
 	
 
 	// The roomy general command, name should be changed
 	public static boolean roomyCmd(CommandSender sender, String[] args) {		
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			if (!Roomy.Permissions.has(player, "Roomy.tool")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 
 			if (!Roomy.roomySetting.containsKey(player)) { // add user
 				Roomy.roomySetting.put(player, true);
 				player.sendMessage("RoomTool 2enabledf, uses same item as WorldEdit(Wooden axe) id: 271");
 				return true;
 			}else{
 				boolean newVal = !Roomy.roomySetting.get(player);
 				Roomy.roomySetting.put(player, newVal);
 
 				player.sendMessage("RoomyTool " + (newVal?"2enabledf, item id 271 (Wooden axe)" : "RoomyTool cdisabled"));
 				return true;
 			}
 		}
 		else { // Console
 			System.out.println("This command cannot be used from a commandline.");
 			return true;
 		}
 		//return false; // did not find all the args or something
 	}
 
 	
 	// Save all the rooms
	public static boolean saveroomsCmd(CommandSender sender, String[] args) {		
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			if (!Roomy.Permissions.has(player, "Roomy.manage.save")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 
 			// Call the method that saves all the rooms
 			int savedNumRooms = RoomyFileManager.save();
 			player.sendMessage(ChatColor.GREEN + "Saved " + savedNumRooms + " rooms");
 			log.info(infoName + player.getName() + " saved " + savedNumRooms + " Rooms");
 			return true;
 		}
		else {
 			int savedNumRooms = RoomyFileManager.save();
 			log.info(infoName + " Saved " + savedNumRooms + " rooms");
 			return true;
 		}
 	}
 	
 	
 	// reload all the rooms into memory
 	public static boolean loadroomsCmd(CommandSender sender, String[] args) {		
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			
 			if (!Roomy.Permissions.has(player, "Roomy.manage.load")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 
 			// Call the method that loads all the rooms
 			int savedNumRooms = RoomyFileManager.save();
 			player.sendMessage(ChatColor.GREEN + "Loaded " + savedNumRooms + " rooms");
 			log.info(infoName + player.getName() + " reloaded the " + savedNumRooms + " existing rooms");
 			return true;
 
 		}
 		else {
 			int loadedNumRooms = RoomyFileManager.save();
 			log.info(infoName + " Loaded " + loadedNumRooms + " rooms");
 			return true;
 		}
 	}
 
 	
 	// remove a specified room
 	public static boolean removeroomCmd(CommandSender sender, String[] args) {
 		if (args.length != 1) {
 			return false;
 		}
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 
 			if (!Roomy.Permissions.has(player, "Roomy.manage.delete")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 
 			int deleted = RoomyFileManager.deleteRoom(args[0]);
 			if (deleted > 1) {
 				player.sendMessage("Deleted " + deleted + " parts of " + args[0]);
 				log.info(player.getName() + " Deleted " + deleted + " parts of " + args[0]);
 			}
 			else if (deleted == 1) {
 				player.sendMessage("Deleted room " + args[0]);
 				log.info(player.getName() + " Deleted " + args[0]);
 			}
 			else {
 				player.sendMessage("Room not found.");
 				log.info(player.getName() + " Tried to delete room " + args[0] + " but it was not found");
 			}
 		}
		else {
 			int deleted = RoomyFileManager.deleteRoom(args[0]);
 			if (deleted > 1) {
 				log.info("{Console} deleted " + deleted + " parts of " + args[0]);
 			}
 			else if (deleted == 1) {
 				log.info("{Console} Deleted room " + args[0]);
 			}
 			else {
 				log.info("Room not found.");
 			}
 		}
 		return true;
 	}
 
 	// tells you all the rooms available
 	public static boolean listroomsCmd(CommandSender sender, String[] args) {		
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 
 			if (!Roomy.Permissions.has(player, "Roomy.manage.load")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 			List<String> allRooms = RoomyLibrary.getAllRooms();
 			String strAllRooms = RoomyLibrary.stringlistToString(allRooms, true); // true for color, only players
 			player.sendMessage("Rooms:");
 			player.sendMessage(strAllRooms);
 			return true;
 			
 		}
		else {
 			List<String> allRooms = RoomyLibrary.getAllRooms();
 			String strAllRooms = RoomyLibrary.stringlistToString(allRooms, false); // true for color, only players
 			log.info("Rooms: " + strAllRooms);
 			return true;
 		}
 	}
 	
 	
 	
 	public static boolean insideCmd(CommandSender sender, String[] args) {
 		if ((sender instanceof Player)) {
 			Player player = (Player)sender;
 			if (!Roomy.Permissions.has(player, "Roomy.use.inside")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
 				return true;
 			}
 			
 			// not targeting yourself
 			if (args.length == 1) {
 				Player target = RoomyLibrary.getPlayerFromName(args[0]);
 				if (!(target instanceof Player)) { // see if target actually is a player
 					player.sendMessage(ChatColor.RED + "Player was not found");
 					return true;
 				}
 				String tell = RoomyLibrary.stringlistToString(RoomyLibrary.getRoomsIn(target), true);
 				player.sendMessage(target.getName() + " is in the rooms: " + tell);
 				return true;
 			}
 			// target yourself
 			else {
 				String tell = RoomyLibrary.stringlistToString(RoomyLibrary.getRoomsIn(player), true);
 				player.sendMessage("You are in the following rooms: " + tell);
 				return true;
 			}
 
 		}
		else {
 			if (args.length == 1) {
 				Player target = RoomyLibrary.getPlayerFromName(args[0]);
 				if (!(target instanceof Player)) { // see if target actually is a player
 					log.info("Player was not found");
 					return true;
 				}
 				String tell = RoomyLibrary.stringlistToString(RoomyLibrary.getRoomsIn(target), false);
 				log.info(target.getName() + " is in the rooms: " + tell);
 				return true;
 			}
 			else {
 				log.info("Need a playername for console usage");
 				return false;
 			}
 		}
 	}
 }
