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
	     string = string.replace("&0", ChatColor.BLACK+"");
	     string = string.replace("&1", ChatColor.DARK_BLUE+"");
	     string = string.replace("&2", ChatColor.DARK_GREEN+"");
	     string = string.replace("&3", ChatColor.DARK_AQUA+"");
	     string = string.replace("&4", ChatColor.DARK_RED+"");
	     string = string.replace("&5", ChatColor.DARK_PURPLE+"");
	     string = string.replace("&6", ChatColor.GOLD+"");
	     string = string.replace("&7", ChatColor.GRAY+"");
	     string = string.replace("&8", ChatColor.DARK_GRAY+"");
	     string = string.replace("&9", ChatColor.BLUE+"");
	     string = string.replace("&a", ChatColor.GREEN+"");
	     string = string.replace("&b", ChatColor.AQUA+"");
	     string = string.replace("&c", ChatColor.RED+"");
	     string = string.replace("&d", ChatColor.LIGHT_PURPLE+"");
	     string = string.replace("&e", ChatColor.YELLOW+"");
	     string = string.replace("&f", ChatColor.WHITE+"");
 	    return string;
 	}
 	public String parseColor(String string) {
 		return Message.colorizeText(string);
 	}
 
 }
