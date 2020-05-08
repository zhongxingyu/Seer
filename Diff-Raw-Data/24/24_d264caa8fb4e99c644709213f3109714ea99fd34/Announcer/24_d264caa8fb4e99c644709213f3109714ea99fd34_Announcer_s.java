 /*
  * AutoWalls by jkush321 is licensed under the
  * Creative Commons Attribution-NonCommercial 3.0 Unported License
  * 
  * You are fully allowed to modify the source code for your own network
  * of servers, but you may not distribute the modified code outside of
  * your servers.
  * 
  * AutoWalls was originally a personal project that was standalone for
  * my own private server, and it slowly accumulated into a giant plugin.
  * 
  * AutoWalls is for dedicated servers that are willing to run just Walls.
  * 
  * The license requires attribution and you have to give credit to jkush321
  * no matter how many changes were made to the code. In some clearly stated
  * way everyone who goes on the server must be able to easily see and be aware
  * of the fact that this code originated from jkush321 and was modified by
  * you or your team.
  * 
  * For more information visit http://bit.ly/AutoWalls
  * 
  */
 
 package com.jkush321.autowalls;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 public class Announcer implements Runnable {
 	
 	public static int message=0;
 	public static List<String> messages = new ArrayList<String>();
 	
 	public void run()
 	{
 		while (true)
 		{
 			try {
 				
				Bukkit.broadcastMessage(ChatColor.DARK_RED + "[" + AutoWalls.announcerName + "]" + ChatColor.DARK_AQUA + messages.get(message));
 				message++;
 				if (message==messages.size()) message=0;
 				Thread.sleep(60000);
 				
 			}catch (Exception e) { e.printStackTrace(); }
 		}
 	}
 }
