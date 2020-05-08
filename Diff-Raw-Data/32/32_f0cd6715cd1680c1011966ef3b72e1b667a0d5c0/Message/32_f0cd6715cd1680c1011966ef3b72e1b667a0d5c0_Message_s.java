 package me.cmastudios.plugins.WarhubModChat.util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Message {
 	public void send (Player player, String message) {
 		if (player == null) {
 			System.out.println(message.replace("a", "").replace("b", "").replace("c", "").replace("d", "").replace("e", "").replace("f", "").replace("1", "").replace("2", "").replace("3", "").replace("4", "").replace("5", "").replace("6", "").replace("7", "").replace("8", "").replace("9", "").replace("0", ""));
 		} else {
 			player.sendMessage(colorizeText(message));
 		}
 	}
 	public void broadcast (String message) {
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 	        this.send(p, message);
 	      }
 		System.out.println(message.replace("a", "").replace("b", "").replace("c", "").replace("d", "").replace("e", "").replace("f", "").replace("1", "").replace("2", "").replace("3", "").replace("4", "").replace("5", "").replace("6", "").replace("7", "").replace("8", "").replace("9", "").replace("0", ""));
 	}
 	public static String colorizeText(String string) {
	    string = string.replaceAll("&0", ChatColor.BLACK+"");
	    string = string.replaceAll("&1", ChatColor.DARK_BLUE+"");
	    string = string.replaceAll("&2", ChatColor.DARK_GREEN+"");
	    string = string.replaceAll("&3", ChatColor.DARK_AQUA+"");
	    string = string.replaceAll("&4", ChatColor.DARK_RED+"");
	    string = string.replaceAll("&5", ChatColor.DARK_PURPLE+"");
	    string = string.replaceAll("&6", ChatColor.GOLD+"");
	    string = string.replaceAll("&7", ChatColor.GRAY+"");
	    string = string.replaceAll("&8", ChatColor.DARK_GRAY+"");
	    string = string.replaceAll("&9", ChatColor.BLUE+"");
	    string = string.replaceAll("&a", ChatColor.GREEN+"");
	    string = string.replaceAll("&b", ChatColor.AQUA+"");
	    string = string.replaceAll("&c", ChatColor.RED+"");
	    string = string.replaceAll("&d", ChatColor.LIGHT_PURPLE+"");
	    string = string.replaceAll("&e", ChatColor.YELLOW+"");
	    string = string.replaceAll("&f", ChatColor.WHITE+"");
 	    return string;
 	}
 	public String parseColor(String string) {
 		return Message.colorizeText(string);
 	}
 
 }
