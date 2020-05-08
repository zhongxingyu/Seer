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
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 public class JoinTimer implements Runnable {
 
 	public static int timeleft;
 	
 	public void run() {
 		while (timeleft > 0)
 		{
 			try
 			{
 				Thread.sleep(1000);
 				timeleft--;
 				
 				if (timeleft>=10 && timeleft % 10 == 0)
 				{
 					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " seconds!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
					Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "You can " + ChatColor.YELLOW + "/join " + ChatColor.DARK_GREEN + "at any time if you have " + AutoWalls.earlyJoinPriority + " or more priority!");
 				}
 				if (timeleft==5)
 				{
					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " seconds!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
 				}
 				if (timeleft==4)
 				{
					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " seconds!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
 				}
 				if (timeleft==3)
 				{
					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " seconds!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
 				}
 				if (timeleft==2)
 				{
					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " seconds!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
 				}
 				if (timeleft==1)
 				{
					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " in " + timeleft + " second!" + ChatColor.DARK_RED + " Make sure to use /kit !!!!");
 				}
 				if (timeleft==0)
 				{
 					Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "You can now " + ChatColor.YELLOW + "/join" + ChatColor.DARK_AQUA + " !");
 				}
 			}
 			catch (Exception e) { System.out.println("JoinTimer error."); e.printStackTrace(); }
 		}
 		AutoWalls.canJoin=true;
 	}
 	
 }
