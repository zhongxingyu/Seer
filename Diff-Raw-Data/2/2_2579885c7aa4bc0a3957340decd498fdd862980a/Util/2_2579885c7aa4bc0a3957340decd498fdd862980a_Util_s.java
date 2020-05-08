 package com.wolvencraft.prison.mines.util;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
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
 		if(curMine == null) return parseChatColors(str);
 		
 		for(MineVariable var : MineVariable.values()) {
 			if(!str.contains(var.getName())) continue;
			str.replaceAll("<" + var.getName() + ">", var.parse(curMine));
 		}
 		
 		
 		if(str.startsWith("<M:") && str.endsWith(">")) str = parseVars(PrisonMine.getLanguage().SIGN_TITLE, curMine);
 		if(str.startsWith("<M>")) str = str.replaceAll("<M>", "");
 		
 		return parseChatColors(str);
 	}
 	
 	public static String parseChatColors(String str) {
 		if(str == null) return "";
 		
 		for(ChatColor color : ChatColor.values()) {
 			str = str.replaceAll("&" + color.getChar(), color + "");
 		}
 		
 		return str;
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
 	
 	/**
 	 * Returns the local copy of the list of players
 	 * @param world World to check
 	 * @return The list of players
 	 */
 	public static List<Player> getLocalPlayers(World world) {
 		List<Player> tempPlayers = new ArrayList<Player>();
 		for(Player p : world.getPlayers()) { tempPlayers.add(p); }
 		return tempPlayers;
 	}
 }
