 package com.turt2live.antishare;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.conversations.Conversable;
 
 public class ASUtils {
 	public static String addColor(String message){
 		String colorSeperator = "&";
 		message = message.replaceAll(colorSeperator + "0", ChatColor.getByChar('0').toString());
 		message = message.replaceAll(colorSeperator + "1", ChatColor.getByChar('1').toString());
 		message = message.replaceAll(colorSeperator + "2", ChatColor.getByChar('2').toString());
 		message = message.replaceAll(colorSeperator + "3", ChatColor.getByChar('3').toString());
 		message = message.replaceAll(colorSeperator + "4", ChatColor.getByChar('4').toString());
 		message = message.replaceAll(colorSeperator + "5", ChatColor.getByChar('5').toString());
 		message = message.replaceAll(colorSeperator + "6", ChatColor.getByChar('6').toString());
 		message = message.replaceAll(colorSeperator + "7", ChatColor.getByChar('7').toString());
 		message = message.replaceAll(colorSeperator + "8", ChatColor.getByChar('8').toString());
 		message = message.replaceAll(colorSeperator + "9", ChatColor.getByChar('9').toString());
 		message = message.replaceAll(colorSeperator + "a", ChatColor.getByChar('a').toString());
 		message = message.replaceAll(colorSeperator + "b", ChatColor.getByChar('b').toString());
 		message = message.replaceAll(colorSeperator + "c", ChatColor.getByChar('c').toString());
 		message = message.replaceAll(colorSeperator + "d", ChatColor.getByChar('d').toString());
 		message = message.replaceAll(colorSeperator + "e", ChatColor.getByChar('e').toString());
 		message = message.replaceAll(colorSeperator + "f", ChatColor.getByChar('f').toString());
 		message = message.replaceAll(colorSeperator + "A", ChatColor.getByChar('a').toString());
 		message = message.replaceAll(colorSeperator + "B", ChatColor.getByChar('b').toString());
 		message = message.replaceAll(colorSeperator + "C", ChatColor.getByChar('c').toString());
 		message = message.replaceAll(colorSeperator + "D", ChatColor.getByChar('d').toString());
 		message = message.replaceAll(colorSeperator + "E", ChatColor.getByChar('e').toString());
 		message = message.replaceAll(colorSeperator + "F", ChatColor.getByChar('f').toString());
 		return message;
 	}
 
 	public static void sendToPlayer(CommandSender target, String message){
 		if(!message.equalsIgnoreCase("nomsg")
 				&& !message.equalsIgnoreCase("no message")
 				&& !message.equalsIgnoreCase("none")
 				&& !message.equalsIgnoreCase("noshow")
 				&& !message.equalsIgnoreCase("no show")){
 			message = addColor(message);
 			if(!ChatColor.stripColor(message).startsWith("[AntiShare]")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.WHITE + message;
 			}
 			target.sendMessage(message);
 		}
 	}
 
 	public static void sendToConversable(Conversable target, String message){
 		if(!message.equalsIgnoreCase("nomsg")
 				&& !message.equalsIgnoreCase("no message")
 				&& !message.equalsIgnoreCase("none")
 				&& !message.equalsIgnoreCase("noshow")
 				&& !message.equalsIgnoreCase("no show")){
 			message = addColor(message);
 			if(!ChatColor.stripColor(message).startsWith("[AntiShare]")){
 				message = ChatColor.GRAY + "[AntiShare] " + ChatColor.WHITE + message;
 			}
 			target.sendRawMessage(message);
 		}
 	}
 
 	public static void transfer(File original, File destination){
 		try{
 			if(!destination.exists()){
 				File d = new File(destination.getParent());
 				d.mkdirs();
 				destination.createNewFile();
 			}
 			InputStream in = new FileInputStream(original);
 			OutputStream out = new FileOutputStream(destination);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0){
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public static Boolean getValueOf(String value){
 		if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("on")
 				|| value.equalsIgnoreCase("active") || value.equalsIgnoreCase("1")){
 			return true;
 		}else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("off")
 				|| value.equalsIgnoreCase("inactive") || value.equalsIgnoreCase("0")){
 			return false;
 		}
 		return null;
 	}
 
 	public static GameMode getGameMode(String value){
 		if(value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c")){
 			return GameMode.CREATIVE;
 		}else if(value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s")){
 			return GameMode.SURVIVAL;
 		}
 		return null;
 	}
 
 	public static boolean isInteractable(Material material){
 		switch (material){
 		case DISPENSER:
 			return true;
 		case NOTE_BLOCK:
 			return true;
 		case BED_BLOCK:
 			return true;
		case TNT:
			return true;
 		case CHEST:
 			return true;
 		case WORKBENCH:
 			return true;
 		case FURNACE:
 			return true;
 		case BURNING_FURNACE:
 			return true;
 		case WOODEN_DOOR:
 			return true;
 		case LEVER:
 			return true;
 		case STONE_PLATE:
 			return true;
 		case IRON_DOOR_BLOCK:
 			return true;
 		case WOOD_PLATE:
 			return true;
 		case STONE_BUTTON:
 			return true;
 		case JUKEBOX:
 			return true;
 		case LOCKED_CHEST:
 			return true;
 		case TRAP_DOOR:
 			return true;
 		case MONSTER_EGGS:
 			return true;
 		case FENCE_GATE:
 			return true;
 		case ENCHANTMENT_TABLE:
 			return true;
 		case BREWING_STAND:
 			return true;
 		case CAULDRON:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
