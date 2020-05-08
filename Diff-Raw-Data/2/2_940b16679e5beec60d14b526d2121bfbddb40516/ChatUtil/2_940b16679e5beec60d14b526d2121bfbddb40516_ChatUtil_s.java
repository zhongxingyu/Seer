 package com.TeamNovus.Supernaturals.Util;
 
 import org.bukkit.ChatColor;
 
 public class ChatUtil {
 	
 	public static String fillBar(int bars, ChatColor fullColor, ChatColor emptyColor, int amount, int max) {
 		int fill = (int) (amount * 1.0 * bars/max);
 		String bar = new String();
 		
 		for (int i = 0; i < fill; i++) {
 			bar += fullColor + "|";
 		}
 		
		for (int i = 0; i < 50 - fill; i++) {
 			bar += emptyColor + "|";
 		}
 		
 		return bar;
 	}
 	
 }
