 package btwmod.protectedzones;
 
 import java.util.List;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.Packet3Chat;
 import net.minecraft.src.WrongUsageException;
 import btwmods.Util;
 import btwmods.commands.CommandBaseExtended;
 import btwmods.util.Area;
 import btwmods.util.Cube;
 
 public class CommandZone extends CommandBaseExtended {
 	
 	private final mod_ProtectedZones mod;
 	
 	public CommandZone(mod_ProtectedZones mod) {
 		this.mod = mod;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "zone";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (isStringMatch(args, 0, "create")) {
 			processCommandCreate(sender, args);
 		}
 		else if (isStringMatch(args, 0, "copy")) {
 			processCommandCopy(sender, args);
 		}
 		else if (isStringMatch(args, 0, "destroy")) {
 			processCommandDestroy(sender, args);
 		}
 		else if (isStringMatch(args, 0, "set")) {
 			processCommandSet(sender, args);
 		}
 		else if (isStringMatch(args, 0, "add")) {
 			processCommandAdd(sender, args);
 		}
 		else if (isStringMatch(args, 0, "remove")) {
 			processCommandRemove(sender, args);
 		}
 		else if (isStringMatch(args, 0, "info")) {
 			processCommandInfo(sender, args);
 		}
 		else if (isStringMatch(args, 0, "list")) {
 			processCommandList(sender, args);
 		}
 		else if (isStringMatch(args, 0, "grant")) {
 			processCommandGrant(sender, args);
 		}
 		else if (isStringMatch(args, 0, "revoke")) {
 			processCommandGrant(sender, args);
 		}
 		else if (isStringMatch(args, 0, "help") && args.length == 2) {
 			throw new WrongUsageException(getCommandUsage(args[1]), new Object[0]);
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 		}
 	}
 	
 	public void processCommandCreate(ICommandSender sender, String[] args) {
 		if (args.length == 3 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 
 			Zone zone = null;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if (mod.get(dimension, args[2]) != null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name already exists.");
 			}
 			else {
 				try {
 					zone = new Zone(args[2], dimension);
 				}
 				catch (IllegalArgumentException e) {
 					
 				}
 				
 				if (zone == null) {
 					sender.sendChatToPlayer(Util.COLOR_RED + "Invalid zone parameters.");
 				}
 				else if (mod.add(zone)) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Created new zone " + Util.COLOR_WHITE + args[2]);
 					mod.saveAreas();
 				}
 				else {
 					sender.sendChatToPlayer(Util.COLOR_RED + "Failed to add new zone.");
 				}
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("create"), new Object[0]);
 		}
 	}
 	
 	public void processCommandCopy(ICommandSender sender, String[] args) {
 		if (args.length == 4 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 
 			Zone zone = null;
 			Zone oldZone = null;
 			if (!Zone.isValidName(args[3])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name " + args[3] + " is invalid.");
 			}
 			else if (mod.get(dimension, args[3]) != null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with the name " + args[3] + " already exists.");
 			}
 			else if ((oldZone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with the name " + args[2] + " does not exist.");
 			}
 			else {
 				try {
 					zone = new Zone(oldZone, args[3]);
 				}
 				catch (IllegalArgumentException e) {
 					
 				}
 				
 				if (zone == null) {
 					sender.sendChatToPlayer(Util.COLOR_RED + "Invalid zone parameters.");
 				}
 				else if (mod.add(zone)) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Created zone " + Util.COLOR_WHITE + args[3] + Util.COLOR_YELLOW + " as copy of " + Util.COLOR_WHITE + oldZone.name);
 					mod.saveAreas();
 				}
 				else {
 					sender.sendChatToPlayer(Util.COLOR_RED + "Failed to add new zone.");
 				}
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("copy"), new Object[0]);
 		}
 	}
 	
 	public void processCommandDestroy(ICommandSender sender, String[] args) {
 		if (args.length == 3 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if (!mod.remove(dimension, args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			else {
 				sender.sendChatToPlayer(Util.COLOR_YELLOW + "Removed zone " + Util.COLOR_WHITE + args[2]);
 				mod.saveAreas();
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("destroy"), new Object[0]);
 		}
 	}
 	
 	public void processCommandSet(ICommandSender sender, String[] args) {
 		if (args.length == 5 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			Zone zone = null;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if ((zone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			else if (zone.permissions.set(args[3], args[4])) {
 				String newValue = zone.permissions.get(args[3]);
 				sender.sendChatToPlayer(Util.COLOR_YELLOW + "Setting '" + args[3] + "' set to " + (newValue == null ? "null" : newValue) + ".");
 				mod.saveAreas();
 			}
 			else {
 				sender.sendChatToPlayer(Util.COLOR_RED + "Invalid value for setting '" + args[3] + "'.");
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("set"), new Object[0]);
 		}
 	}
 	
 	public void processCommandAdd(ICommandSender sender, String[] args) {
 		if ((args.length == 7 || args.length == 9) && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			Zone zone = null;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if ((zone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			else if (args.length == 7) {
 				int x1 = parseInt(sender, args[3]);
 				int z1 = parseInt(sender, args[4]);
 				int x2 = parseInt(sender, args[5]);
 				int z2 = parseInt(sender, args[6]);
 				
 				Area area = zone.addArea(x1, z1, x2, z2);
 				
 				if (area != null) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Added area to " + zone.name + ": " + area.toString());
 					mod.saveAreas();
 				}
 				else {
 					sender.sendChatToPlayer(Util.COLOR_RED + "An area with the same dimensions already exists for the zone.");
 				}
 			}
 			else {
 				int x1 = parseInt(sender, args[3]);
 				int y1 = parseIntBounded(sender, args[4], 0, 256);
 				int z1 = parseInt(sender, args[5]);
 				int x2 = parseInt(sender, args[6]);
 				int y2 = parseIntBounded(sender, args[7], 0, 256);
 				int z2 = parseInt(sender, args[8]);
 				
 				Cube cube = zone.addCube(x1, y1, z1, x2, y2, z2);
 				
 				if (cube != null) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Added cube to zone " + zone.name + ": " + cube.toString());
 					mod.saveAreas();
 				}
 				else {
 					sender.sendChatToPlayer(Util.COLOR_RED + "A cube with the same dimensions already exists for the zone.");
 				}
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("add"), new Object[0]);
 		}
 	}
 	
 	public void processCommandRemove(ICommandSender sender, String[] args) {
 		if (args.length == 4 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			Zone zone = null;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if ((zone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			else {
 				int areaNum = parseIntWithMin(sender, args[3], 1);
 				Area removed = zone.removeArea(areaNum - 1);
 				if (removed != null) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Removed area #" + areaNum + " (" + removed.toString() + ") from zone " + zone.name + ".");
 					mod.saveAreas();
 				}
 				else {
 					sender.sendChatToPlayer(Util.COLOR_RED + "Zone " + zone.name + " does not have an area #" + areaNum + ".");
 				}
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("remove"), new Object[0]);
 		}
 	}
 	
 	public void processCommandInfo(ICommandSender sender, String[] args) {
 		if (args.length == 3 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			Zone zone;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if ((zone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			else {
 				sender.sendChatToPlayer(Util.COLOR_RED + "=== Zone " + zone.name + " in " + Util.getWorldNameFromDimension(zone.dimension) + " ===");
 				
 				for (int i = 1; i <= zone.areas.size(); i++) {
 					Area area = zone.areas.get(i - 1);
 					sender.sendChatToPlayer(Util.COLOR_AQUA + "<Area #" + i + ">" + Util.COLOR_WHITE + " " + area.toString());
 				}
 				
 				String settingsHeader = Util.COLOR_AQUA + "<Settings>" + Util.COLOR_WHITE + " ";
 				String playersHeader = Util.COLOR_AQUA + "<Whitelist>" + Util.COLOR_WHITE + " ";
 				
 				List<String> settingMessages = Util.combineIntoMaxLengthMessages(zone.permissions.asList(), Packet3Chat.maxChatLength, ", ", true);
 				if (settingMessages.size() == 1 && (settingsHeader.length() + settingMessages.get(0).length()) <= Packet3Chat.maxChatLength) {
 					sender.sendChatToPlayer(settingsHeader + settingMessages.get(0));
 				}
 				else {
 					sender.sendChatToPlayer(settingsHeader);
 					for (String message : settingMessages) {
 						sender.sendChatToPlayer(message);
 					}
 				}
 				
 				List<String> playerMessages = Util.combineIntoMaxLengthMessages(zone.whitelist.asList(), Packet3Chat.maxChatLength, ", ", true);
 				if (playerMessages.size() == 1 && (playersHeader.length() + playerMessages.get(0).length()) <= Packet3Chat.maxChatLength) {
 					sender.sendChatToPlayer(playersHeader + playerMessages.get(0));
 				}
				else if (playerMessages.size() >= 1) {
 					sender.sendChatToPlayer(playersHeader);
 					for (String message : playerMessages) {
 						sender.sendChatToPlayer(message);
 					}
 				}
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage("info"), new Object[0]);
 		}
 	}
 	
 	public void processCommandList(ICommandSender sender, String[] args) {
 		if (args.length > 3 || !isWorldName(args, 1))
 			throw new WrongUsageException(getCommandUsage("list"), new Object[0]);
 		
 		int dimension = Util.getWorldDimensionFromName(args[1]);
 		
 		List<String> zoneNames = mod.getZoneNames(dimension);
 		
 		if (zoneNames.size() == 0) {
 			sender.sendChatToPlayer(Util.COLOR_YELLOW + "There are no zones for " + Util.getWorldNameFromDimension(dimension) + ".");
 		}
 		else {
 			String headerShort = Util.COLOR_YELLOW + Util.getWorldNameFromDimension(dimension) + " Zones: " + Util.COLOR_WHITE;
 			String headerLong = Util.COLOR_YELLOW + Util.getWorldNameFromDimension(dimension) + " Zones (Page XX/YY): " + Util.COLOR_WHITE;
 			int page = args.length == 3 ? parseIntWithMin(sender, args[2], 1) : 1;
 			int maxListSize = Packet3Chat.maxChatLength - Math.max(headerShort.length(), headerLong.length());
 			
 			List<String> pages = Util.combineIntoMaxLengthMessages(zoneNames, maxListSize, ", ", false);
 			
 			page = Math.min(page, pages.size());
 			
 			sender.sendChatToPlayer((pages.size() == 1 ? headerShort : headerLong.replaceAll("XX/YY", page + "/" + pages.size()))
 					+ pages.get(Math.min(page, pages.size()) - 1));
 		}
 	}
 	
 	public void processCommandGrant(ICommandSender sender, String[] args) {
 		if (args.length == 4 && isWorldName(args, 1)) {
 			
 			int dimension = Util.getWorldDimensionFromName(args[1]);
 			
 			Zone zone;
 			if (!Zone.isValidName(args[2])) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "The zone name specified is invalid.");
 			}
 			else if ((zone = mod.get(dimension, args[2])) == null) {
 				sender.sendChatToPlayer(Util.COLOR_RED + "A zone with that name does not exist.");
 			}
 			
 			else if (args[0].equalsIgnoreCase("grant")) {
 				if (zone.whitelist.add(args[3])) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Whitelisted player " + args[3] + " for zone " + zone.name + ".");
 					mod.saveAreas();
 				}
 				else
 					sender.sendChatToPlayer(Util.COLOR_RED + "Player " + args[3] + " is already whitelisted for zone " + zone.name + ".");
 			}
 			
 			else if (args[0].equalsIgnoreCase("revoke")) {
 				if (zone.whitelist.remove(args[3])) {
 					sender.sendChatToPlayer(Util.COLOR_YELLOW + "Removed player " + args[3] + " from zone " + zone.name + "'s whitelist.");
 					mod.saveAreas();
 				}
 				else
 					sender.sendChatToPlayer(Util.COLOR_RED + "Player " + args[3] + " was not whitelisted for zone " + zone.name + ".");
 			}
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage(args[0]), new Object[0]);
 		}
 	}
 	
 	public String getCommandUsage(String subCommand) {
 		if (subCommand != null) {
 			if (subCommand.equalsIgnoreCase("create"))
 				return "/" + getCommandName() + " create <dimension> <zonename>";
 			
 			else if (subCommand.equalsIgnoreCase("copy"))
 				return "/" + getCommandName() + " copy <dimension> <zonename> <zonecopyname>";
 			
 			else if (subCommand.equalsIgnoreCase("destroy"))
 				return "/" + getCommandName() + " destroy <dimension> <zonename>";
 			
 			else if (subCommand.equalsIgnoreCase("set"))
 				return "/" + getCommandName() + " set <dimension> <zonename> <setting> <value>";
 			
 			else if (subCommand.equalsIgnoreCase("add"))
 				return "/" + getCommandName() + " add <dimension> <zonename> <x1> [<y1>] <z1> <x2> [<y2>] <z2>";
 			
 			else if (subCommand.equalsIgnoreCase("remove"))
 				return "/" + getCommandName() + " remove <dimension> <zonename> <areanum>";
 			
 			else if (subCommand.equalsIgnoreCase("info"))
 				return "/" + getCommandName() + " info <dimension> <zonename>";
 			
 			else if (subCommand.equalsIgnoreCase("list"))
 				return "/" + getCommandName() + " list <dimension> [<page>]";
 			
 			else if (subCommand.equalsIgnoreCase("grant"))
 				return "/" + getCommandName() + " grant <dimension> <zonename> <username>";
 			
 			else if (subCommand.equalsIgnoreCase("revoke"))
 				return "/" + getCommandName() + " revoke <dimension> <zonename> <username>";
 		}
 		
 		return "/" + getCommandName() + " [help] ( create | destroy | set | add | remove | info | list | grant | revoke ) ...";
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender) {
 		return getCommandUsage("");
 	}
 
 	@Override
 	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
 		if (args.length == 1)
 			return getListOfStringsMatchingLastWord(args, new String[] { "help", "create", "copy", "destroy", "set", "add", "remove", "info", "list", "grant", "revoke" });
 		
 		else if (args.length == 2 && isStringMatch(args, 0, "help")) {
 			return getListOfStringsMatchingLastWord(args, new String[] { "create", "copy", "destroy", "set", "add", "remove", "info", "list", "grant", "revoke" });
 		}
 		
 		// <dimension>
 		else if (args.length == 2 && isStringMatch(args, 0, new String[] { "create", "copy", "destroy", "set", "add", "remove", "info", "list", "grant", "revoke" })) {
 			return getListOfStringsMatchingLastWord(args, new String[] { "Overworld", "Nether", "TheEnd" });
 		}
 		
 		// <zonename>
 		else if (args.length == 3 && isWorldName(args, 1) && (isStringMatch(args, 0, new String[] { "copy", "destroy", "set", "add", "remove", "info", "grant", "revoke" }))) {
 			List names = mod.getZoneNames(Util.getWorldDimensionFromName(args[1]));
 			return getListOfStringsMatchingLastWord(args, (String[])names.toArray(new String[names.size()]));
 		}
 		
 		// <setting>
 		else if (args.length == 4 && isStringMatch(args, 0, "set")) {
 			return getListOfStringsMatchingLastWord(args, ZonePermissions.settings);
 		}
 		
 		// <value>
 		else if (args.length == 5 && isStringMatch(args, 0, "set")) {
 			return getListOfStringsMatchingLastWord(args, new String[] { "on", "whitelist", "off" });
 		}
 		
 		else if (args.length == 4 && isStringMatch(args, 0, "grant")) {
 			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
 		}
 		
 		else if (args.length == 4 && isStringMatch(args, 0, "revoke")) {
 			int dimension;
 			
 			try {
 				dimension = Util.getWorldDimensionFromName(args[1]);
 			}
 			catch (IllegalArgumentException e) {
 				return super.addTabCompletionOptions(sender, args);
 			}
 			
 			Zone zone = mod.get(dimension, args[2]);
 			if (zone == null)
 				return super.addTabCompletionOptions(sender, args);
 			
 			return getListOfStringsFromIterableMatchingLastWord(args, zone.whitelist.asList());
 		}
 		
 		return super.addTabCompletionOptions(sender, args);
 	}
 }
