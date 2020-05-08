 package com.legit2.Demigods.Utilities;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 public class DAbilityUtil
 {
 	/*
 	 *  doAbilityPreProcess() : Returns the a boolean for success or failure.
 	 */
 	public static boolean doAbilityPreProcess(Player player, int cost)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
		if(DMiscUtil.canTarget(player))
 		{
 			player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 			return false;
 		}
 		else if(DCharUtil.getFavor(charID) < cost)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
 			return false;
 		}
 		else return true;
 	}
 	public static boolean doAbilityPreProcess(Player player, Entity target, int cost)
 	{
 		if(doAbilityPreProcess(player, cost))
 		{
 			if(!(target instanceof LivingEntity))
 			{
 				player.sendMessage(ChatColor.YELLOW + "No target found.");
 				return false;
 			}
 			else if(target instanceof Player)
 			{
 				if(DMiscUtil.areAllied(player, (Player) target)) return false;
 			}
 			if(!DMiscUtil.canTarget(target))
 			{
 				player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
 				return false;
 			}
 			else return true;
 		}
 		else return false;
 	}
 }
