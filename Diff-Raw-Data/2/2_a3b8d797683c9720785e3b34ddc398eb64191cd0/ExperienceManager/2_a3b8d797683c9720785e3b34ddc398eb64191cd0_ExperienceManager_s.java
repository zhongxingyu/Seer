 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.rewards.xp;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * Code is based on net.ess3.craftbukkit.SetExpFix from Essentials.
  */
 public class ExperienceManager {
 
 	private final String playerName;
 
 	/**
 	 * Create a new ExperienceManager for the given player.
 	 * 
 	 * @param player The player for this ExperienceManager object
 	 */
 	public ExperienceManager(Player player) {
 		this.playerName = player.getName();
 		getPlayer(); // ensure it's a valid player name
 	}
 
 	/**
 	 * Get the Player associated with this ExperienceManager.
 	 * 
 	 * @return the Player object
 	 * @throws IllegalStateException if the player is no longer online
 	 */
 	public Player getPlayer() {
 		Player p = Bukkit.getPlayer(playerName);
 		if (p == null) {
 			throw new IllegalStateException("Player " + playerName
 					+ " is not online");
 		}
 		return p;
 	}
 
 	/**
 	 * Adjust the player's XP by the given amount.
 	 * @param amt Amount of XP, may be negative
 	 */
 	public void changeExp(int amt) {
 		
 		int xp = getCurrentExp() + amt;
 		
 		if (xp < 0)
 			xp = 0;
 
 		// Update experience
 		setTotalExperience(xp);
 	}
 
 	/**
 	 * Get the player's current XP total.
 	 * 
 	 * @return the player's total XP
 	 */
 	public int getCurrentExp() {
 		Player player = getPlayer();
 		int exp = (int) Math.round(getExpToLevel(player) * player.getExp());
 		int currentLevel = player.getLevel();
 
 		// Slow, but should work
 		while (currentLevel > 0) {
 			currentLevel--;
 			exp += getExpToLevel(currentLevel);
 		}
 		return exp;
 	}
 	
 	/**
 	 * This method is used to update both the recorded total experience and displayed total experience.
 	 * @param exp - new experience.
 	 */
 	public void setTotalExperience(final int exp) {
 		
 		Player player = getPlayer();
 		
 		if (exp < 0) {
 			throw new IllegalArgumentException("Experience is negative!");
 		}
 		
 		player.setExp(0);
 		player.setLevel(0);
 		player.setTotalExperience(0);
 
 		int amount = exp;
 		
 		// Give the correct amount of experience every level
 		while (amount > 0) {
 			final int expToLevel = getExpToLevel(player);
 			amount -= expToLevel;
 			if (amount >= 0) {
 				// give until next level
 				player.giveExp(expToLevel);
 			} else {
 				// give the rest
 				amount += expToLevel;
 				player.giveExp(amount);
 				amount = 0;
 			}
 		}
 	}
 
 	private static int getExpToLevel(final Player player) {
 		return getExpToLevel(player.getLevel());
 	}
 
 	private static int getExpToLevel(final int level) {
		return 17 + Math.min(3 * (level - 16), 0);
 	}
 
 	/**
 	 * Checks if the player has the given amount of XP.
 	 * 
 	 * @param amt The amount to check for.
 	 * @return true if the player has enough XP, false otherwise
 	 */
 	public boolean hasExp(int amt) {
 		return getCurrentExp() >= amt;
 	}
 }
