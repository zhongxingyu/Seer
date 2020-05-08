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
	     string.replace("&0", ChatColor.BLACK+"");
	     string.replace("&1", ChatColor.DARK_BLUE+"");
	     string.replace("&2", ChatColor.DARK_GREEN+"");
	     string.replace("&3", ChatColor.DARK_AQUA+"");
	     string.replace("&4", ChatColor.DARK_RED+"");
	     string.replace("&5", ChatColor.DARK_PURPLE+"");
	     string.replace("&6", ChatColor.GOLD+"");
	     string.replace("&7", ChatColor.GRAY+"");
	     string.replace("&8", ChatColor.DARK_GRAY+"");
	     string.replace("&9", ChatColor.BLUE+"");
	     string.replace("&a", ChatColor.GREEN+"");
	     string.replace("&b", ChatColor.AQUA+"");
	     string.replace("&c", ChatColor.RED+"");
	     string.replace("&d", ChatColor.LIGHT_PURPLE+"");
	     string.replace("&e", ChatColor.YELLOW+"");
	     string.replace("&f", ChatColor.WHITE+"");
 	    return string;
 	}
 	public String parseColor(String string) {
 		return Message.colorizeText(string);
 	}
 
 }
