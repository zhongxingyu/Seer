 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit.util;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.lock.BukkitLock;
 import com.almuramc.bolt.lock.Lock;
 import com.almuramc.bolt.registry.CommonRegistry;
 import com.almuramc.bolt.storage.Storage;
 
 import net.milkbowl.vault.Vault;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 /**
  * The core utility class of Aqualock. Handles common functions between commands/GUI (to spare a LOT of duplicate code) such as lock, unlock, use, and change
  */
 public class LockUtil {
 	private static final CommonRegistry registry;
 	private static final Storage backend;
 
 	static {
 		registry = AqualockPlugin.getRegistry();
 		backend = AqualockPlugin.getBackend();
 	}
 
 	/**
 	 * @param playerName
 	 * @param coowners
 	 * @param passcode
 	 * @param location
 	 * @param data
 	 */
 	public static void lock(String playerName, List<String> coowners, String passcode, Location location, byte data) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		if (!PermissionUtil.canLock(player)) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "You do not have permission to lock!");
 			return;
 		}
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		BukkitLock lock = new BukkitLock(playerName, coowners, passcode, location, data);
 		//Make sure we aren't relocking blocks
 		if (registry.contains(lock)) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "This location has a lock. Did you mean to update instead?");
 			return;
 		}
 		//If the server has an economy system, use it
 		if (AqualockPlugin.getEconomies() != null) {
 			//Check if they need to be charged for this lock
 			if (EconomyUtil.shouldChargeForLock(player)) {
 				//No account? Let the player know some message and return
 				if (!EconomyUtil.hasAccount(player)) {
 					player.sendMessage(AqualockPlugin.getPrefix() + "Locks cost money and you do not have a wallet!");
 					return;
 				} else {
 					double cost = EconomyUtil.getCostForLock(player, location.getBlock().getType());
 					//Find out if they have the money.
 					if (EconomyUtil.hasEnough(player, cost)) {
 						if (cost < 0) {
							player.sendMessage(AqualockPlugin.getPrefix() + "You gained $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from locking");
 						} else if (cost > 0) {
							player.sendMessage(AqualockPlugin.getPrefix() + "You lost $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from locking");
 						}
 						EconomyUtil.apply(player, cost);
 						//Don't have enough? Tell them that and return
 					} else {
						player.sendMessage(AqualockPlugin.getPrefix() + "This lock costs $" + ChatColor.GREEN + cost + ChatColor.WHITE + " and you do not have enough!");
 						return;
 					}
 				}
 			}
 		}
 		//Is it a double door? Lets lock both doors
 		if (BlockUtil.isDoubleDoor(location.getBlock())) {
 			//Get the double door blocks
 			List<Block> doors = BlockUtil.getDoubleDoor(location.getBlock());
 			//Loop through the blocks (will be 2 iterations)
 			for (Block b : doors) {
 				//If the lock created in this method's location is not the location of this iteration then its the second door, lock it.
 				if (!b.getLocation().equals(location)) {
 					BukkitLock other = new BukkitLock(lock.getOwner(), lock.getCoOwners(), lock.getPasscode(), b.getLocation(), b.getData());
 					player.sendMessage(AqualockPlugin.getPrefix() + "Detected a door at " + b.getLocation().toString() + "so locking it as well");
 					registry.addLock(other);
 					backend.addLock(other);
 				}
 			}
 		}
 		//After all that is said and done, add the lock made to the registry and backend.
 		player.sendMessage(AqualockPlugin.getPrefix() + "You locked " + location.toString());
 		registry.addLock(lock);
 		backend.addLock(lock);
 	}
 
 	/**
 	 * @param playerName
 	 * @param passcode
 	 * @param location
 	 */
 	public static void unlock(String playerName, String passcode, Location location) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		if (!PermissionUtil.canUnlock(player)) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "You do not have permission to unlock!");
 			return;
 		}
 
 		Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 
 		if (lock == null) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "You attempted to unlock this block but Aqualock cannot find a lock here.");
 			return;
 		} else {
 			String owner = lock.getOwner();
 			//The owner of the lock at the location doesn't match the player's name, check co-owners next
 			if (!owner.equals(playerName)) {
 				//They aren't a co-owner either, so return and print a message
 				if (!lock.getCoOwners().contains(playerName)) {
 					player.sendMessage(AqualockPlugin.getPrefix() + "You are not an Owner or Co-Owner, your unlock has been denied.");
 					//TODO Hurt them?
 					return;
 				}
 			}
 			//At this point they are either an owner or co-owner but that is irrelevant.
 			//TODO Owner-level only privileges?
 			//Now lets handle Bukkit-like lock characteristics to determine if we can unlock
 			if (lock instanceof BukkitLock) {
 				//Is a Bukkit lock but the passcode passed in fails so fail to unlock (even if it was the owner...)
 				if (!((BukkitLock) lock).getPasscode().equals(passcode)) {
 					player.sendMessage(AqualockPlugin.getPrefix() + "Incorrect passcode for unlock, please try again.");
 					return;
 				}
 			}
 
 			//If the server has an economy system, use it
 			if (AqualockPlugin.getEconomies() != null) {
 				//Check if they need to be charged for this lock
 				if (EconomyUtil.shouldChargeForUnlock(player)) {
 					//No account? Let the player know some message and return
 					if (!EconomyUtil.hasAccount(player)) {
 						player.sendMessage(AqualockPlugin.getPrefix() + "Unlocks cost money and you do not have a wallet!");
 						return;
 					} else {
 						double cost = EconomyUtil.getCostForUnlock(player, location.getBlock().getType());
 						//Find out if they have the money.
 						if (EconomyUtil.hasEnough(player, cost)) {
 							if (cost < 0) {
								player.sendMessage(AqualockPlugin.getPrefix() + "You gained $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from unlocking.");
 							} else if (cost > 0) {
								player.sendMessage(AqualockPlugin.getPrefix() + "You lost $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from unlocking.");
 							}
 							EconomyUtil.apply(player, cost);
 							//Don't have enough? Tell them that and return
 						} else {
							player.sendMessage(AqualockPlugin.getPrefix() + "This unlock costs $" + ChatColor.GREEN + cost + ChatColor.WHITE + " and you do not have enough!");
 							return;
 						}
 					}
 				}
 			}
 		}
 		registry.removeLock(lock);
 		backend.removeLock(lock);
 	}
 
 	/**
 	 * @param playerName
 	 * @param coowners
 	 * @param passcode
 	 * @param location
 	 * @param data
 	 */
 	public static void update(String playerName, List<String> coowners, String passcode, Location location, byte data) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		if (!PermissionUtil.canUpdate(player)) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "You do not have permission to update!");
 			return;
 		}
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		//First check if the registry has a lock at this location, if not then lock.
 		if (!registry.contains(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "There is no lock at this location. Did you want to lock instead?");
 			return;
 		}
 		Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 		//The player updating the lock is neither an owner or a co-owner nor has the permission
 		if ((!lock.getOwner().equals(playerName) || !lock.getCoOwners().contains(playerName))) {
 			player.sendMessage(AqualockPlugin.getPrefix() + "Incorrect passcode for update, please try again.");
 			return;
 		}
 		if (lock instanceof BukkitLock) {
 			if (!((BukkitLock) lock).getPasscode().equals(passcode)) {
 				player.sendMessage(AqualockPlugin.getPrefix() + "Incorrect passcode for update, please try again.");
 				return;
 			}
 		}
 		//If the server has an economy system, use it
 		if (AqualockPlugin.getEconomies() != null) {
 			//Check if they need to be charged for this lock
 			if (EconomyUtil.shouldChargeForUpdate(player)) {
 				//No account? Let the player know some message and return
 				if (!EconomyUtil.hasAccount(player)) {
 					player.sendMessage(AqualockPlugin.getPrefix() + "Updates cost money and you do not have a wallet!");
 					return;
 				} else {
 					double cost = EconomyUtil.getCostForUpdate(player, location.getBlock().getType());
 					//Find out if they have the money.
 					if (EconomyUtil.hasEnough(player, cost)) {
 						if (cost < 0) {
							player.sendMessage(AqualockPlugin.getPrefix() + "You gained $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from updating.");
 						} else if (cost > 0) {
							player.sendMessage(AqualockPlugin.getPrefix() + "You lost $" + ChatColor.GREEN + cost + ChatColor.WHITE + " from updating.");
 						}
 						EconomyUtil.apply(player, cost);
 						//Don't have enough? Tell them that and return
 					} else {
						player.sendMessage(AqualockPlugin.getPrefix() + "This update costs $" + ChatColor.GREEN + cost + ChatColor.WHITE + " and you do not have enough!");
 						return;
 					}
 				}
 			}
 		}
 		lock.setOwner(playerName);
 		lock.setCoOwners(coowners);
 		if (lock instanceof BukkitLock) {
 			((BukkitLock) lock).setPasscode(passcode);
 			((BukkitLock) lock).setData(data);
 		}
 		//Update backend and registry
 		registry.addLock(lock);
 		backend.addLock(lock);
 	}
 
 	public static void use(String playerName, Location location) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		//First check if the registry has a lock at this location, if not then return
 		if (!registry.contains(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
 			return;
 		}
 		Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 		if ((!lock.getOwner().equals(playerName) || !lock.getCoOwners().contains(playerName)) && !PermissionUtil.canUse(player)) {
 			//TODO Tell them that they don't have privileges to use at this location and return
 			return;
 		}
 		//If the server has an economy system, use it
 		if (AqualockPlugin.getEconomies() != null) {
 			//Check if they need to be charged for this lock
 			if (EconomyUtil.shouldChargeForUse(player)) {
 				//No account? Let the player know some message and return
 				if (!EconomyUtil.hasAccount(player)) {
 					//TODO message
 					return;
 				} else {
 					double cost = EconomyUtil.getCostForUse(player, location.getBlock().getType());
 					//Find out if they have the money.
 					if (EconomyUtil.hasEnough(player, cost)) {
 						//TODO If the cost was zero then say something like "Using was free!"
 						if (cost == 0) {
 							//player.sendMessage
 							//TODO If cost was less than zero then say something like "Using gave you monies!"
 						} else if (cost < 0) {
 							//player.sendMessage
 							//TODO Tell the user how much they were charged
 						} else {
 							//player.sendMessage
 						}
 						EconomyUtil.apply(player, cost);
 						//Don't have enough? Tell them that and return
 					} else {
 						//TODO message
 						return;
 					}
 				}
 			}
 		}
 	}
 	////////////////////////////////////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////Overriden Methods///////////////////////////////////////////////
 	//////////////////////////////////////Don't touch these!//////////////////////////////////////////////
 	/////////////////////////////////////////////////////////////////////////////////////////////////////
 
 	public static void lock(String owner, List<String> coowners, String passcode, Location location) {
 		checkLocation(location);
 		lock(owner, coowners, passcode, location, location.getBlock().getData());
 	}
 
 	public static void lock(String owner, List<String> coowners, Location location) {
 		checkLocation(location);
 		lock(owner, coowners, "", location, location.getBlock().getData());
 	}
 
 	public static void lock(String owner, Location location) {
 		checkLocation(location);
 		lock(owner, null, "", location, location.getBlock().getData());
 	}
 
 	public static void unlock(String playerName, Location location) {
 		unlock(playerName, "", location);
 	}
 
 	public static void update(String playerName, List<String> coowners, String passcode, Location location) {
 		checkLocation(location);
 		update(playerName, coowners, passcode, location, location.getBlock().getData());
 	}
 
 	public static void update(String playerName, List<String> coowners, Location location) {
 		checkLocation(location);
 		update(playerName, coowners, "", location, location.getBlock().getData());
 	}
 
 	public static void update(String playerName, Location location) {
 		checkLocation(location);
 		update(playerName, null, location);
 	}
 
 	////////////////////////////////////////////////////////////////////////////////////////////////////
 	//////////////////////////////////////Private helpers//////////////////////////////////////////////
 	//////////////////////////////////////////////////////////////////////////////////////////////////
 
 	private static void checkLocation(Location location) {
 		if (location == null) {
 			throw new IllegalArgumentException("Location cannot be null!");
 		}
 	}
 
 	private static Player checkNameAndGetPlayer(String name) {
 		if (name == null || name.isEmpty()) {
 			throw new IllegalArgumentException("Name cannot be null or empty!");
 		}
 		Player player = Bukkit.getPlayerExact(name);
 		if (player == null) {
 			throw new IllegalArgumentException("No player found matching name: " + name + " found on this server!");
 		}
 		return player;
 	}
 }
