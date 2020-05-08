 package com.wolvencraft.prison.mines.util;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.hooks.MaterialHook;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
  
 public class Util {
 	
 	/**
 	 * Checks if the command sender has a permission node
 	 * @param node Node to check
 	 * @return true if has permission, false if does not
 	 */
 	public static boolean hasPermission(String node) {
 		CommandSender sender = CommandManager.getSender();
 		if(!(sender instanceof Player)) return true;
 		return (sender.isOp() || sender.hasPermission(node));
 	}
 	
 	/**
 	 * Checks if the player has a permission node
 	 * @param node Node to check
 	 * @return true if has permission, false if does not
 	 */
 	public static boolean hasPermission(Player player, String node) {
 		return (player.isOp() || player.hasPermission(node));
 	}
 	
 	/**
 	 * Parses a block specified for a material
 	 * @param blockName Name of a block
 	 * @return Block material if it exists, null if it does not.
 	 */
 	public static MaterialData getBlock(String blockName) {
 		Message.debug("Parsing block: " + blockName);
 		try
 		{
 			String[] parts = blockName.split(":");
 			if(parts.length > 2) return null;
 			
 			MaterialData block;
 			if(isNumeric(parts[0])) block = new MaterialData(Material.getMaterial(Integer.parseInt(parts[0])));
 			else {
 				MaterialHook ore = MaterialHook.match(parts[0]);
 				if(ore != null) parts[0] = ore.getMaterial();
 				
 				Message.debug(parts[0] + " => " + Material.getMaterial(parts[0].toUpperCase()).name());
 				block = new MaterialData(Material.getMaterial(parts[0].toUpperCase()));
 			}
 			
 			parts[0] = block.getItemTypeId() + "";
 			
 			if(parts.length == 2) {
 				if(!isNumeric(parts[1])) parts[1] = parseMetadata(parts, false);
 				block.setData(Byte.parseByte(parts[1]));
 			}
 			
 			return block;
 			
 		}
 		catch(NumberFormatException nfe) { return null; }
 		catch(NullPointerException npe) { return null; }
 		catch(Exception ex) { return null; }
 	}
 	
 	/**
 	 * Returns the data of the block specified
 	 * @param parts Block name
 	 * @return metadata of a block
 	 */
 	public static String parseMetadata(String[] parts, boolean recursive) {
 		if(recursive)
 		{
 			int data = Integer.parseInt(parts[1]);
 			switch(Integer.parseInt(parts[0]))
 			{
 				case 5:
 				case 6:
 				case 17:
 				case 18:
 				{
 					if(data == 1) parts[1] = "pine";
 					else if(data == 2) parts[1] = "birch";
 					else if(data == 3) parts[1] = "jungle";
 					else parts[1] = "oak";
 
 					break;
 				}
 				case 24:
 				{
 					if(data == 1) parts[1] = "chiseled";
 					else if(data == 2) parts[1] = "smooth";
 					else parts[1] = "";
 
 					break;
 				}
 				case 33:
 				case 34:
 				{
 					if(data == 1) parts[1] = "sandstone";
 					else if(data == 2) parts[1] = "wooden";
 					else if(data == 3) parts[1] = "cobblestone";
 					else if(data == 4) parts[1] = "brick";
 					else if(data == 5) parts[1] = "stone brick";
 					else if(data == 6) parts[1] = "smooth";
 					else parts[1] = "stone";
 
 					break;
 				}
 				case 35:
 				{
 					if(data == 1) parts[1] = "orange";
 					else if(data == 2) parts[1] = "magenta";
 					else if(data == 3) parts[1] = "lightblue";
 					else if(data == 4) parts[1] = "yellow";
 					else if(data == 5) parts[1] = "lime";
 					else if(data == 6) parts[1] = "pink";
 					else if(data == 7) parts[1] = "gray";
 					else if(data == 8) parts[1] = "lightgray";
 					else if(data == 9) parts[1] = "cyan";
 					else if(data == 10) parts[1] = "purple";
 					else if(data == 11) parts[1] = "blue";
 					else if(data == 12) parts[1] = "brown";
 					else if(data == 13) parts[1] = "green";
 					else if(data == 14) parts[1] = "red";
 					else if(data == 15) parts[1] = "black";
 					else parts[1] = "white";
 
 					break;
 				}
 				case 84:
 				{
 					if(data == 1) parts[1] = "gold disk";
 					else if(data == 2) parts[1] = "green disk";
 					else if(data == 3) parts[1] = "orange disk";
 					else if(data == 4) parts[1] = "red disk";
 					else if(data == 5) parts[1] = "lime disk";
 					else if(data == 6) parts[1] = "purple disk";
 					else if(data == 7) parts[1] = "violet disk";
 					else if(data == 8) parts[1] = "black disk";
 					else if(data == 9) parts[1] = "white disk";
 					else if(data == 10) parts[1] = "sea green disk";
 					else if(data == 11) parts[1] = "broken disk";
 					else parts[1] = "";
 
 					break;
 				}
 				case 98:
 				{
 					if(data == 1) parts[1] = "mossy";
 					else if(data == 2) parts[1] = "cracked";
 					else if(data == 3) parts[1] = "chiseled";
 					else parts[1] = "";
 				}
 				default:
 				{
 					if(data == 0) parts[1] = "";
 				}
 			}
 		}
 		else
 		{
 			if(parts[0].equalsIgnoreCase("5") || parts[0].equalsIgnoreCase("6") || parts[0].equalsIgnoreCase("17") || parts[0].equalsIgnoreCase("18"))
 			{
 				if(parts[1].equalsIgnoreCase("dark") || parts[1].equalsIgnoreCase("pine") || parts[1].equalsIgnoreCase("spruce")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("birch")) parts[1] = 2 + "";
 				else if(parts[1].equalsIgnoreCase("jungle")) parts[1] = 3 + "";
 				else parts[1] = 0 + "";
 			}
 			else if(parts[0].equalsIgnoreCase("24"))
 			{
 				if(parts[1].equalsIgnoreCase("chiseled") || parts[1].equalsIgnoreCase("creeper")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("smooth")) parts[1] = 2 + "";
 				else parts[1] = 0 + "";
 			}
 			else if(parts[0].equalsIgnoreCase("33") || parts[0].equalsIgnoreCase("34"))
 			{
 				if(parts[1].equalsIgnoreCase("sandstone")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("wooden") || parts[1].equalsIgnoreCase("wood") || parts[1].equalsIgnoreCase("plank")) parts[1] = 2+ "";
 				else if(parts[1].equalsIgnoreCase("cobblestone") || parts[1].equalsIgnoreCase("cobble")) parts[1] = 3 + "";
 				else if(parts[1].equalsIgnoreCase("brick")) parts[1] = 4 + "";
 				else if(parts[1].equalsIgnoreCase("stonebrick") || parts[1].equalsIgnoreCase("stone_brick")) parts[1] = 5 + "";
 				else if(parts[1].equalsIgnoreCase("smoothstone") || parts[1].equalsIgnoreCase("smooth")) parts[1] = 6 + "";
 			}
 			else if(parts[0].equalsIgnoreCase("35"))
 			{
 				if(parts[1].equalsIgnoreCase("orange")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("magenta")) parts[1] = 2 + "";
 				else if(parts[1].equalsIgnoreCase("lightblue")) parts[1] = 3 + "";
 				else if(parts[1].equalsIgnoreCase("yellow")) parts[1] = 4 + "";
 				else if(parts[1].equalsIgnoreCase("lime")) parts[1] = 5 + "";
 				else if(parts[1].equalsIgnoreCase("pink")) parts[1] = 6 + "";
 				else if(parts[1].equalsIgnoreCase("gray")) parts[1] = 7 + "";
 				else if(parts[1].equalsIgnoreCase("lightgray")) parts[1] = 8 + "";
 				else if(parts[1].equalsIgnoreCase("cyan")) parts[1] = 9 + "";
 				else if(parts[1].equalsIgnoreCase("purple")) parts[1] = 10 + "";
 				else if(parts[1].equalsIgnoreCase("blue")) parts[1] = 11 + "";
 				else if(parts[1].equalsIgnoreCase("brown")) parts[1] = 12 + "";
 				else if(parts[1].equalsIgnoreCase("green")) parts[1] = 13 + "";
 				else if(parts[1].equalsIgnoreCase("red")) parts[1] = 14 + "";
 				else if(parts[1].equalsIgnoreCase("black")) parts[1] = 15 + "";
 				else parts[1] = 0 + "";
 			}
 			else if(parts[0].equalsIgnoreCase("84"))
 			{
 				if(parts[1].equalsIgnoreCase("gold")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("green")) parts[1] = 2 + "";
 				else if(parts[1].equalsIgnoreCase("orange")) parts[1] = 3 + "";
 				else if(parts[1].equalsIgnoreCase("red")) parts[1] = 4 + "";
 				else if(parts[1].equalsIgnoreCase("lime")) parts[1] = 5 + "";
 				else if(parts[1].equalsIgnoreCase("purple")) parts[1] = 6 + "";
 				else if(parts[1].equalsIgnoreCase("violet")) parts[1] = 7 + "";
 				else if(parts[1].equalsIgnoreCase("black")) parts[1] = 8 + "";
 				else if(parts[1].equalsIgnoreCase("white")) parts[1] = 9 + "";
 				else if(parts[1].equalsIgnoreCase("seagreen")) parts[1] = 10 + "";
 				else if(parts[1].equalsIgnoreCase("broken")) parts[1] = 11 + "";
 				else parts[1] = 0 + "";
 			}
 			else if(parts[0].equalsIgnoreCase("98"))
 			{
 				if(parts[1].equalsIgnoreCase("mossy")) parts[1] = 1 + "";
 				else if(parts[1].equalsIgnoreCase("cracked")) parts[1] = 2 + "";
 				else if(parts[1].equalsIgnoreCase("chiseled")) parts[1] = 3 + "";
 				else parts[1] = 0 + "";
 			}
 		}
 		return parts[1];
 	}
 	
 	/**
 	 * Checks if a string is numeric
 	 * @param str String String to be checked
 	 * @return boolean True if a string is numeric
 	 */
 	public static boolean isNumeric(String str) {  
 	  try
 	  { Double.parseDouble(str); }
 	  catch(NumberFormatException nfe)  
 	  { return false; }  
 	  return true;  
 	}
 	
 	/**
 	 * Replaces the variables in the string with their values
 	 * @param str String to be parsed
 	 * @param mineName Name of the mine
 	 * @return Parsed string
 	 */
 	public static String parseVars(String str, Mine curMine) {
 		if(curMine == null) return parseColors(str);
 		
 		String displayName = curMine.getName();
 		if(displayName.equals("")) displayName =  curMine.getId();
 		str = str.replaceAll("<ID>", curMine.getId());
 		str = str.replaceAll("<NAME>", displayName);
 		
 		List<Mine> children = curMine.getChildren();
 		String mineIds = curMine.getId();
 		String mineNames = displayName;
 		if(!children.isEmpty()) {
 			for(Mine childMine : children) {
 				mineIds += ", " + childMine.getId();
 				mineNames += ", " + childMine.getName();
 			}
 		}
 		str = str.replaceAll("<NAMES>", mineNames);
 		str = str.replaceAll("<IDS>", mineIds);
 		
 		
 		if(curMine.getCooldown()) {
 			str = str.replaceAll("<COOLTIME>", parseSeconds(curMine.getCooldownPeriod()));
 			str = str.replaceAll("<COOLENDS>", parseSeconds(curMine.getCooldownEndsIn()));
 		}
 		
 		str = str.replaceAll("<TBLOCKS>", curMine.getTotalBlocks() + "");
 		str = str.replaceAll("<RBLOCKS>", curMine.getBlocksLeft() + "");
 		str = str.replaceAll("<PBLOCKS>", (curMine.getBlocksLeft() / curMine.getTotalBlocks()) * 100 + "");
 		
 		if(curMine.getAutomatic()) {
 			// Reset period variable calculations
 			int ptime = curMine.getResetPeriod();
 			
 			int phour = ptime / 3600;										// Unformatted variables.
 			int pmin = ptime / 60;											// Contain exact values for hour, minutes, seconds.
 			int psec = ptime;												// Used in further calculations.
 			
 			int phourFormatted = phour;										// Formatted variables.
 			int pminFormatted = pmin - phour * 60;							// Values of higher-level variables have been subtracted.
 			int psecFormatted = psec - pmin * 60;							// Do not have a 0 in front if the value is < 10.
 			
 			String phourClock = phourFormatted + "";						// Extra-formatted variables.
 			if(phourFormatted < 10) phourClock = "0" + phourClock;			// Have an added 0 in front
 			String pminClock = pminFormatted + "";							// if the value of the variable
 			if(pminFormatted < 10) pminClock = "0" + pminClock;				// is single-digit.
 			String psecClock = psecFormatted + "";							// Used in the super-formatted variable.
 			if(psecFormatted < 10) psecClock = "0" + psecClock;
 			
 			String ptimeClock = pminClock + ":" + psecClock;				// Super-formatted variable.
 			if(phour != 0) ptimeClock = phourFormatted + ":" + ptimeClock;	// Displays time in HOUR:MINUTE:SECOND format.
 
 			// Next reset variable calculations
 			int ntime = curMine.getResetsIn();
 			
 			int nhour = ntime / 3600;										// Unformatted variables.
 			int nmin = ntime / 60;											// Contain exact values for hour, minutes, seconds.
 			int nsec = ntime;												// Used in further calculations.
 			
 			int nhourFormatted = nhour;										// Formatted variables.
 			int nminFormatted = nmin - nhour * 60;							// Values of higher-level variables have been subtracted.
 			int nsecFormatted = nsec - nmin * 60;							// Do not have a 0 in front if the value is < 10.
 			
 			String nhourClock = nhourFormatted + "";						// Extra-formatted variables.
 			if(nhourFormatted < 10) nhourClock = "0" + nhourClock;			// Have an added 0 in front
 			String nminClock = nminFormatted + "";							// if the value of the variable
 			if(nminFormatted < 10) nminClock = "0" + nminClock;				// is single-digit.
 			String nsecClock = nsecFormatted + "";							// Used in the super-formatted variable.
 			if(nsecFormatted < 10) nsecClock = "0" + nsecClock;
 			
 			String ntimeClock = nminClock + ":" + nsecClock;				// Super-formatted variable.
 			if(nhour != 0) ntimeClock = nhourFormatted + ":" + ntimeClock;	// Displays time in HOUR:MINUTE:SECOND format.
 			
 			// Reset Period variables
 			str = str.replaceAll("<PHOUR>", phourFormatted + "");
 			str = str.replaceAll("<PMIN>", pminFormatted + "");
 			str = str.replaceAll("<PSEC>", psecFormatted + "");
 			str = str.replaceAll("<PTIME>", ptimeClock);
 			
 			// Next Reset variables
 			str = str.replaceAll("<NHOUR>", nhourFormatted + "");
 			str = str.replaceAll("<NMIN>", nminFormatted + "");
 			str = str.replaceAll("<NSEC>", nsecFormatted + "");
 			str = str.replaceAll("<NTIME>", ntimeClock);
 		}
 		
		if(str.startsWith("<M|") && str.endsWith(">")) str = parseVars(PrisonMine.getLanguage().SIGN_TITLE, curMine);
 		str = str.replaceAll("<M>", "");
 		
 		return parseColors(str);
 	}
 	
 	/**
 	 * Replaces the color codes with colors
 	 * @param msg String to be parsed
 	 * @return Parsed string
 	 */
 	public static String parseColors(String msg) {
 		if(msg == null) return "";
 		msg = msg.replaceAll("&0", ChatColor.BLACK.toString());
 		msg = msg.replaceAll("&1", ChatColor.DARK_BLUE.toString());
 		msg = msg.replaceAll("&2", ChatColor.DARK_GREEN.toString());
 		msg = msg.replaceAll("&3", ChatColor.DARK_AQUA.toString());
 		msg = msg.replaceAll("&4", ChatColor.DARK_RED.toString());
 		msg = msg.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
 		msg = msg.replaceAll("&6", ChatColor.GOLD.toString());
 		msg = msg.replaceAll("&7", ChatColor.GRAY.toString());
 		msg = msg.replaceAll("&8", ChatColor.DARK_GRAY.toString());
 		msg = msg.replaceAll("&9", ChatColor.BLUE.toString());
 
 		msg = msg.replaceAll("&a", ChatColor.GREEN.toString());
 		msg = msg.replaceAll("&b", ChatColor.AQUA.toString());
 		msg = msg.replaceAll("&c", ChatColor.RED.toString());
 		msg = msg.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
 		msg = msg.replaceAll("&e", ChatColor.YELLOW.toString());
 		msg = msg.replaceAll("&f", ChatColor.WHITE.toString());
 
 		msg = msg.replaceAll("&A", ChatColor.GREEN.toString());
 		msg = msg.replaceAll("&B", ChatColor.AQUA.toString());
 		msg = msg.replaceAll("&C", ChatColor.RED.toString());
 		msg = msg.replaceAll("&D", ChatColor.LIGHT_PURPLE.toString());
 		msg = msg.replaceAll("&E", ChatColor.YELLOW.toString());
 		msg = msg.replaceAll("&F", ChatColor.WHITE.toString());
 
 		msg = msg.replaceAll("&k", ChatColor.MAGIC.toString());
 		msg = msg.replaceAll("&l", ChatColor.BOLD.toString());
 		msg = msg.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
 		msg = msg.replaceAll("&n", ChatColor.UNDERLINE.toString());
 		msg = msg.replaceAll("&o", ChatColor.ITALIC.toString());
 		msg = msg.replaceAll("&r", ChatColor.RESET.toString());
 
 		msg = msg.replaceAll("&K", ChatColor.MAGIC.toString());
 		msg = msg.replaceAll("&L", ChatColor.BOLD.toString());
 		msg = msg.replaceAll("&M", ChatColor.STRIKETHROUGH.toString());
 		msg = msg.replaceAll("&N", ChatColor.UNDERLINE.toString());
 		msg = msg.replaceAll("&O", ChatColor.ITALIC.toString());
 		msg = msg.replaceAll("&R", ChatColor.RESET.toString());
 
 		return msg;
 	}
 	
 	/**
 	 * Parses the message for time and returns it in seconds
 	 * @param message Input in the HOUR:MIN:SEC format
 	 * @return Seconds
 	 */
 	public static int parseTime(String message) {
 		if(message.charAt(0) == ':') message = "0" + message;
 		if(message.charAt(message.length() - 1) == ':') message = message + "0";
 
 		String[] parts = message.split(":");
 		int time = 0;
 
 		try {
 			if(parts.length == 3) {
 				time += Integer.parseInt(parts[0]) * 3600;
 				time += Integer.parseInt(parts[1]) * 60;
 				time += Integer.parseInt(parts[2]);
 			}
 			else if(parts.length == 2) {
 				time += Integer.parseInt(parts[0]) * 60;
 				time += Integer.parseInt(parts[1]);
 			}
 			else if(parts.length == 1) {
 				time += Integer.parseInt(parts[0]);
 			}
 			else return -1;
 		}
 		catch(NumberFormatException nfe) { return -1; }
 
 		return time;
 	}
 	
 	/**
 	 * Parses the seconds and returns time in HOUR:MIN:SEC format
 	 * @param seconds Seconds to parse
 	 * @return Time in user-friendly format
 	 */
 	public static String parseSeconds(int seconds) {
 		int hour = (int) Math.floor(seconds / 3600);
 		int min = (int) Math.floor((seconds - (hour * 3600)) / 60);
 		int sec = seconds - (hour * 3600) - (min * 60);
 		String resetTime = min + ":";
 		if(min < 10)
 			resetTime = "0" + resetTime;
 		resetTime = hour + ":" + resetTime;
 		if(sec < 10)
 			resetTime = resetTime + "0";
 		resetTime = resetTime + sec;
 		return resetTime;
 	}
 	
 	/**
 	 * Parses the material and returns a user-friendly name
 	 * @param material Material to parse
 	 * @return Name of the material
 	 */
 	public static String parseMaterialData(MaterialData material) {
 		String str[] = {material.getItemTypeId() + "", material.getData() + ""};
 		String name = material.getItemType().toString().toLowerCase().replace("_", " ");
 		String meta = parseMetadata(str, true);
 		if(!meta.equalsIgnoreCase("0")) name = meta + " " + name;
 		return name;
 	}
 	
 	/**
 	 * Rounds the number to the 4 decimal points
 	 * @param number Number to round
 	 * @return The rounded number
 	 */
 	public static String round(double number) {
         NumberFormat formatter = new DecimalFormat("#0.0####%");
 		return formatter.format(number);
 	}
 }
