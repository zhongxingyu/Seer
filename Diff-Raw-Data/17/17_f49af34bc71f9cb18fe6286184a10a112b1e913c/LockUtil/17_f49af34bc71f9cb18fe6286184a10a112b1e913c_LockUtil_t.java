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
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuramc.aqualock.bukkit.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.configuration.AqualockConfiguration;
 import com.almuramc.aqualock.bukkit.lock.BukkitLock;
 import com.almuramc.bolt.lock.Lock;
 import com.almuramc.bolt.registry.CommonRegistry;
 import com.almuramc.bolt.storage.Storage;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 /**
  * The core utility class of Aqualock. Handles common functions between commands/GUI (to spare a LOT of duplicate code) such as lock, unlock, use, and change
  */
 public class LockUtil {
 	private static final CommonRegistry registry;
 	private static final Storage backend;
 	private static final AqualockConfiguration config;
 
 	static {
 		registry = AqualockPlugin.getRegistry();
 		backend = AqualockPlugin.getBackend();
 		config = AqualockPlugin.getConfiguration();
 	}
 
 	/**
 	 * @param playerName
 	 * @param coowners
 	 * @param passcode
 	 * @param location
 	 * @param data
 	 */
 	public static boolean lock(String playerName, List<String> coowners, List<String> users, String passcode, Location location, byte data) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		if (users == null) {
 			users = new ArrayList<>(1);
 			users.add("Everyone");
 		}
 		if (!performAction(player, passcode, location, "LOCK")) {
 			return false;
 		}
 		final BukkitLock lock = new BukkitLock(playerName, coowners, users, passcode, location, data);
 		//After all that is said and done, add the lock made to the registry and backend.
 		SpoutManager.getPlayer(player).sendNotification("Aqua", "Locked the block!", Material.CAKE);
 		registry.addLock(lock);
 		backend.addLock(lock);
 		if (BlockUtil.isDoubleDoor(location, location.getBlock().getFace(player.getLocation().getBlock()))) {
 			//Get the double door blocks
 			List<Location> doors = BlockUtil.getDoubleDoor(location, location.getBlock().getFace(player.getLocation().getBlock()));
 			//Loop through the blocks (will be 2 iterations)
 			for (Location loc : doors) {
 				//If the lock created in this method's location is not the location of this iteration then its the second door, lock it.
 				if (!loc.equals(location)) {
 					final BukkitLock other = new BukkitLock(lock.getOwner(), lock.getCoOwners(), lock.getUsers(), lock.getPasscode(), loc, loc.getBlock().getData());
 					player.sendMessage(AqualockPlugin.getPrefix() + "Detected a door at " + loc.toString() + "so locking it as well");
 					registry.addLock(other);
 					backend.addLock(other);
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param playerName
 	 * @param passcode
 	 * @param location
 	 */
 	public static void unlock(String playerName, String passcode, Location location) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		if (!performAction(player, passcode, location, "UNLOCK")) {
 			return;
 		}
 		final Lock lock = registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
 	public static void update(String playerName, List<String> coowners, List<String> users, String passcode, Location location, byte data) {
 		checkLocation(location);
 		Player player = checkNameAndGetPlayer(playerName);
 		if (coowners == null) {
 			coowners = Collections.emptyList();
 		}
 		if (users == null) {
 			users = new ArrayList<>(1);
 			users.add("Everyone");
 		}
 		if (!performAction(player, passcode, location, "UPDATE")) {
 			return;
 		}
 		final BukkitLock lock = (BukkitLock) registry.getLock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 		lock.setOwner(playerName);
 		lock.setCoOwners(coowners.toArray(new String[coowners.size()]));
 		lock.setUsers(users.toArray(new String[users.size()]));
 		lock.setPasscode(passcode);
 		lock.setData(data);
 		//Update backend and registry
 		registry.addLock(lock);
 		backend.addLock(lock);
 	}
 
 	public static void use(String playerName, String passcode, Location location) {
 		checkLocation(location);
 		final Player player = checkNameAndGetPlayer(playerName);
 		performAction(player, passcode, location, "USE");
 	}
 
 	public static boolean performAction(Player player, String passcode, Location location, String action) {
 		final World world = location.getWorld();
 		final UUID worldIdentifier = world.getUID();
 		final int x = location.getBlockX();
 		final int y = location.getBlockY();
 		final int z = location.getBlockZ();
 		final Lock lock = registry.getLock(worldIdentifier, x, y, z);
 		final String name = player.getName();
 		final SpoutPlayer splayer = SpoutManager.getPlayer(player);
 		switch (action) {
 			case "LOCK":
 				if (lock != null) {
 					splayer.sendNotification("Aqua", "This location has a lock!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForLock(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqua", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getLockCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqua", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqua", "Charged for lock: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqua", "Received for lock: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqua", "Lock was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 				return true;
 			case "UNLOCK":
 				if (lock == null) {
 					splayer.sendNotification("Aqua", "No lock at location!", Material.POTION);
 					return true;
 				}
 				boolean canUnlock = false;
				if (!name.equals(lock.getOwner()) && !canPerformAction(player, "UNLOCK")) {
 					for (String pname : lock.getCoOwners()) {
 						if (pname.equals(name)) {
							if (lock instanceof BukkitLock && (!((BukkitLock) lock).getPasscode().equals(passcode))) {
								splayer.sendNotification("Aqua", "Invalid password!", Material.LAVA_BUCKET);
								return false;
 							}
 							canUnlock = true;
 						}
 					}
 				} else {
 					canUnlock = true;
 				}
 				if (!canUnlock) {
 					splayer.sendNotification("Aqua", "Cannot unlock the lock!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUnlock(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqua", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getUnlockCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqua", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqua", "Charged for unlock: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqua", "Received for unlock: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqua", "Unlock was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 				return true;
 			case "USE":
 				if (lock == null) {
 					return true;
 				}
				if (!name.equals(lock.getOwner())&& !canPerformAction(player, "USE")) {
 					for (String pname : lock.getCoOwners()) {
 						if (pname.equals(name)) {
 							if (lock instanceof BukkitLock && (!((BukkitLock) lock).getPasscode().equals(passcode))) {
 								splayer.sendNotification("Aqua", "Invalid password!", Material.LAVA_BUCKET);
 								return false;
 							}
 						}
 					}
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUse(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqua", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getUseCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqua", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqua", "Charged for use: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqua", "Received for use: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqua", "Use was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 				}
 				return true;
 			case "UPDATE":
 				if (lock == null) {
 					return true;
 				}
 				boolean canUpdate = false;
				if (!name.equals(lock.getOwner())&& !canPerformAction(player, "UPDATE")) {
 					for (String pname : lock.getCoOwners()) {
 						if (name.equals(pname)) {
 							canUpdate = true;
 						}
 					}
 				}
 				if (!canUpdate) {
 					splayer.sendNotification("Aqua", "Not the Owner/CoOwner!", Material.LAVA_BUCKET);
 					return false;
 				}
 				if (AqualockPlugin.getEconomies() != null) {
 					if (EconomyUtil.shouldChargeForUpdate(player)) {
 						if (!EconomyUtil.hasAccount(player)) {
 							splayer.sendNotification("Aqua", "You have no account!", Material.LAVA_BUCKET);
 							return false;
 						}
 						final double value = config.getCosts().getUpdateCost(location.getBlock().getType());
 						if (!EconomyUtil.hasEnough(player, value)) {
 							splayer.sendNotification("Aqua", "Not enough money!", Material.LAVA_BUCKET);
 							return false;
 						}
 						if (value > 0) {
 							splayer.sendNotification("Aqua", "Charged for update: " + value, Material.POTION);
 						} else if (value < 0) {
 							splayer.sendNotification("Aqua", "Received for update: " + value, Material.CAKE);
 						} else {
 							splayer.sendNotification("Aqua", "Update was free!", Material.APPLE);
 						}
 						EconomyUtil.apply(player, value);
 					}
 					return true;
 				}
 		}
 		return false;
 	}
 
 	public static boolean canPerformAction(Player player, String action) {
 		//Determine if they have basic perms for the action defined
 		switch (action) {
 			case "LOCK":
 				if (!PermissionUtil.canLock(player)) {
 					return false;
 				}
 				break;
 			case "UNLOCK":
 				if (!PermissionUtil.canUnlock(player)) {
 					return false;
 				}
 				break;
 			case "USE":
 				if (!PermissionUtil.canUse(player)) {
 					return false;
 				}
 				break;
 			case "UPDATE":
 				if (!PermissionUtil.canUse(player)) {
 					return false;
 				}
 				break;
 			default:
 				return false;
 		}
 		return true;
 	}
 	////////////////////////////////////////////////////////////////////////////////////////////////////////
 	//////////////////////////////////////Overloaded Methods///////////////////////////////////////////////
 	//////////////////////////////////////Don't touch these!//////////////////////////////////////////////
 	/////////////////////////////////////////////////////////////////////////////////////////////////////
 
 	public static void lock(String owner, List<String> coowners, String passcode, Location location) {
 		checkLocation(location);
 		lock(owner, coowners, null, passcode, location, location.getBlock().getData());
 	}
 
 	public static void lock(String owner, List<String> coowners, Location location) {
 		checkLocation(location);
 		lock(owner, coowners, null, "", location, location.getBlock().getData());
 	}
 
 	public static void lock(String owner, Location location) {
 		checkLocation(location);
 		lock(owner, null, null, "", location, location.getBlock().getData());
 	}
 
 	public static void unlock(String playerName, Location location) {
 		unlock(playerName, "", location);
 	}
 
 	public static void update(String playerName, List<String> coowners, String passcode, Location location) {
 		checkLocation(location);
 		update(playerName, coowners, null, passcode, location, location.getBlock().getData());
 	}
 
 	public static void update(String playerName, List<String> coowners, Location location) {
 		checkLocation(location);
 		update(playerName, coowners, null, "", location, location.getBlock().getData());
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
