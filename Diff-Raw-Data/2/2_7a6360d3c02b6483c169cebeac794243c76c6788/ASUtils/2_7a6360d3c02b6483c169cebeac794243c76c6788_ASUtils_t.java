 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.Bed;
 import org.bukkit.material.Door;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.PermissionNodes.PermissionPackage;
 import com.turt2live.antishare.config.ASConfig;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.MobPattern.MobPatternType;
 
 /**
  * Utilities
  * 
  * @author turt2live
  */
 @SuppressWarnings ("deprecation")
 public class ASUtils {
 
 	public static enum EntityPattern {
 		SNOW_GOLEM, IRON_GOLEM, WITHER;
 	}
 
 	/**
 	 * Array of true block faces (none of the SOUTH_WEST-like ones)
 	 */
 	public static final List<BlockFace> TRUE_BLOCK_FACES = Collections.unmodifiableList(Arrays.asList(new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.UP}));
 	private static MobPattern SNOW_GOLEM_PATTERN;
 	private static MobPattern IRON_GOLEM_PATTERN;
 	private static MobPattern WITHER_PATTERN;
 	private static String NO_DROP_KEY = "antishare.no.drop.key";
 
 	/**
 	 * Determines if a player has the "no drop" metadata
 	 * 
 	 * @param player the player to check
 	 * @return true if the player has the no drop metadata
 	 */
 	public static boolean hasNoDrop(Player player) {
 		AntiShare plugin = AntiShare.p;
 		if (player.hasMetadata(NO_DROP_KEY)) {
 			List<MetadataValue> vals = player.getMetadata(NO_DROP_KEY);
 			for(MetadataValue val : vals) {
				if (System.currentTimeMillis() - val.asLong() < plugin.settings().onDeathTimerSeconds * 1000) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Removes the "no drop" metadata from the player
 	 * 
 	 * @param player the player to remove the metadata from
 	 */
 	public static void removeNoDrop(Player player) {
 		player.removeMetadata(NO_DROP_KEY, AntiShare.p);
 	}
 
 	/**
 	 * Adds the "no drop" metadata to the player
 	 * 
 	 * @param player the player to add it to
 	 */
 	public static void applyNoDrop(Player player) {
 		removeNoDrop(player);
 		player.setMetadata(NO_DROP_KEY, new FixedMetadataValue(AntiShare.p, System.currentTimeMillis()));
 	}
 
 	/**
 	 * Gets a boolean from a String
 	 * 
 	 * @param value the String
 	 * @return the boolean (or null if not found)
 	 */
 	public static Boolean getBoolean(String value) {
 		if (value == null || value.trim().length() == 0) {
 			return null;
 		}
 		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("active") || value.equalsIgnoreCase("1")) {
 			return true;
 		} else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("off") || value.equalsIgnoreCase("inactive") || value.equalsIgnoreCase("0")) {
 			return false;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a GameMode from a String
 	 * 
 	 * @param value the string
 	 * @return the GameMode (or null if not found)
 	 */
 	public static GameMode getGameMode(String value) {
 		if (value == null) {
 			return null;
 		}
 		if (value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("1")) {
 			return GameMode.CREATIVE;
 		} else if (value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("0")) {
 			return GameMode.SURVIVAL;
 		} else if (value.equalsIgnoreCase("adventure") || value.equalsIgnoreCase("a") || value.equalsIgnoreCase("2")) {
 			return GameMode.ADVENTURE;
 		}
 		return null;
 	}
 
 	/**
 	 * Converts a block to a string<br>
 	 * This returns the format 'id:data', data will be zero if no
 	 * data is found, or the data is actually zero. You can set 'zero'
 	 * in the parameters to false to just get the block ID. If 'zero' is
 	 * true and there is data, the correct format (id:data) will be returned.
 	 * 
 	 * @param block the block
 	 * @param zero true to add zero
 	 * @return the block as a string
 	 */
 	public static String blockToString(Block block, boolean zero) {
 		if (block == null) {
 			return null;
 		}
 		String typeId = "";
 		String data = "0";
 		typeId = Integer.toString(block.getTypeId());
 		if (block.getType().getMaxDurability() > 0) {
 			data = "0";
 		} else if (block.getData() > 0) {
 			data = Byte.toString(block.getData());
 		}
 		return typeId + (data.equals("0") && zero ? "" : ":" + data);
 	}
 
 	/**
 	 * Converts a material to a string<br>
 	 * This returns the format 'id:data', data will be zero if no
 	 * data is found, or the data is actually zero. You can set 'zero'
 	 * in the parameters to false to just get the material ID. If 'zero' is
 	 * true and there is data, the correct format (id:data) will be returned.<br>
 	 * <b>Worth Noting:</b> this (if zero is false) will return a :* id, such as
 	 * 1:* if you pass it Material.STONE.
 	 * 
 	 * @param material the material
 	 * @param zero true to add zero
 	 * @return the material as a string
 	 */
 	public static String materialToString(Material material, boolean zero) {
 		if (material == null) {
 			return null;
 		}
 		StringBuilder ret = new StringBuilder();
 		ret.append(material.getId());
 		if (!zero) {
 			ret.append(":");
 			ret.append("*");
 		}
 		return ret.toString();
 	}
 
 	/**
 	 * Converts words to ID. Eg: "light blue wool" -> "wool:3"
 	 * 
 	 * @param input the raw input
 	 * @return the wool ID (with data value) or null if not wool
 	 */
 	public static String getWool(String input) {
 		if (input == null || !input.toLowerCase().contains("wool")) {
 			return null;
 		}
 
 		String color = input.replace("wool", "").trim().toLowerCase();
 		if (color.length() == 0) {
 			color = "white";
 		}
 		color = color.replaceAll(" ", "_");
 		color = color.replace("orange", "1");
 		color = color.replace("white", "0");
 		color = color.replace("magenta", "2");
 		color = color.replace("light_blue", "3");
 		color = color.replace("yellow", "4");
 		color = color.replace("lime", "5");
 		color = color.replace("pink", "6");
 		color = color.replace("light_gray", "8");
 		color = color.replace("gray", "7");
 		color = color.replace("cyan", "9");
 		color = color.replace("purple", "10");
 		color = color.replace("blue", "11");
 		color = color.replace("brown", "12");
 		color = color.replace("green", "13");
 		color = color.replace("red", "14");
 		color = color.replace("black", "15");
 
 		return Material.WOOL.getId() + ":" + color;
 	}
 
 	/**
 	 * Gets a list of online players with a defined Game Mode
 	 * 
 	 * @param gamemode the Game Mode
 	 * @return the player names with that Game Mode (online only)
 	 */
 	public static List<String> findGameModePlayers(GameMode gamemode) {
 		List<String> affected = new ArrayList<String>();
 		for(Player player : Bukkit.getOnlinePlayers()) {
 			if (player.getGameMode() == gamemode) {
 				affected.add(player.getName());
 			}
 		}
 		return affected;
 	}
 
 	/**
 	 * Generates a comma-separated list from a List
 	 * 
 	 * @param list the list
 	 * @return the comma-separated String
 	 */
 	public static String commas(List<String> list) {
 		if (list == null) {
 			return "no one";
 		}
 		StringBuilder commas = new StringBuilder();
 		for(String s : list) {
 			commas.append(s).append(", ");
 		}
 		String finalComma = commas.toString().trim();
 		return finalComma.length() > 0 ? finalComma.substring(0, finalComma.length() - 1) : "no one";
 	}
 
 	/**
 	 * Gets the abbreviation for a game mode. Eg: CREATIVE = "GM = C" (if shortVersion=false) or CREATIVE = "C" (shortVersion=true)
 	 * 
 	 * @param gamemode the gamemode
 	 * @param shortVersion true to use the single letter, false otherwise
 	 * @return the short hand version, or null if invalid
 	 */
 	public static String gamemodeAbbreviation(GameMode gamemode, boolean shortVersion) {
 		if (gamemode == null) {
 			return null;
 		}
 		return (shortVersion ? "" : "GM = ") + gamemode.name().charAt(0);
 	}
 
 	/**
 	 * Creates a file safe name from a string
 	 * 
 	 * @param name the string
 	 * @return the file safe name
 	 */
 	public static String fileSafeName(String name) {
 		return name.replaceAll("[^0-9a-zA-Z]", "-");
 	}
 
 	/**
 	 * Wipes a folder
 	 * 
 	 * @param folder the folder to wipe
 	 * @param fileNames file names to wipe, can be null for "all"
 	 */
 	public static void wipeFolder(File folder, CopyOnWriteArrayList<String> fileNames) {
 		if (folder == null || !folder.exists()) {
 			return;
 		}
 		if (folder.listFiles() == null) {
 			return;
 		} else {
 			for(File file : folder.listFiles()) {
 				if (file.isDirectory()) {
 					wipeFolder(file, fileNames);
 				}
 				if (fileNames == null || fileNames.contains(file.getName())) {
 					file.delete();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gives a tool to a player
 	 * 
 	 * @param tool the tool
 	 * @param player the player
 	 */
 	public static void giveTool(Material tool, Player player) {
 		giveTool(tool, player, 1);
 	}
 
 	/**
 	 * Gives a tool to a player
 	 * 
 	 * @param tool the tool
 	 * @param player the player
 	 * @param slot the slot to place it in. <b>Starts at 1</b>
 	 */
 	public static void giveTool(Material tool, Player player, int slot) {
 		Inventory inv = player.getInventory();
 		if (inv.firstEmpty() >= 0) {
 			ItemStack original = inv.getItem(slot - 1);
 			if (original != null) {
 				original = original.clone();
 			}
 			ItemStack itemTool = new ItemStack(tool);
 			itemTool.setDurability(AntiShare.ANTISHARE_TOOL_DATA);
 			String title = null;
 			List<String> lore = new ArrayList<String>();
 			AntiShare p = AntiShare.p;
 			if (tool == AntiShare.ANTISHARE_TOOL) {
 				title = ChatColor.RESET + "" + ChatColor.AQUA + "AntiShare Tool";
 				lore.add(ChatColor.GREEN + p.getMessages().getMessage("tool-meta.generic-tool"));
 			} else if (tool == AntiShare.ANTISHARE_SET_TOOL) {
 				title = ChatColor.RESET + "" + ChatColor.AQUA + "AntiShare Set Tool";
 				lore.add(ChatColor.GREEN + p.getMessages().getMessage("tool-meta.set-tool-1"));
 				lore.add(ChatColor.RED + p.getMessages().getMessage("tool-meta.set-tool-2"));
 			} else if (tool == AntiShare.ANTISHARE_CUBOID_TOOL) {
 				title = ChatColor.RESET + "" + ChatColor.AQUA + "AntiShare Cuboid Tool";
 				lore.add(ChatColor.GREEN + p.getMessages().getMessage("tool-meta.cuboid-tool-1"));
 				lore.add(ChatColor.DARK_GREEN + p.getMessages().getMessage("tool-meta.cuboid-tool-2"));
 			}
 			lore.add(p.getMessages().getMessage("tool-meta.all"));
 			lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + p.getMessages().getMessage("tool-meta.created-for", player.getName()));
 			ItemMeta meta = itemTool.getItemMeta();
 			if (title != null) {
 				meta.setDisplayName(title);
 			}
 			if (lore.size() > 0) {
 				meta.setLore(lore);
 			}
 			itemTool.setItemMeta(meta);
 			inv.setItem(slot - 1, itemTool);
 			if (original != null) {
 				inv.addItem(original);
 			}
 			player.updateInventory();
 			player.getInventory().setHeldItemSlot(slot);
 		}
 	}
 
 	/**
 	 * Determines if the player has a tool already
 	 * 
 	 * @param material the material
 	 * @param player the player
 	 * @return true if found, false otherwise
 	 */
 	public static boolean hasTool(Material material, Player player) {
 		for(ItemStack item : player.getInventory().getContents()) {
 			if (item == null) {
 				continue;
 			}
 			if (item.getType() == material && item.getDurability() == AntiShare.ANTISHARE_TOOL_DATA) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Determines the second block to a source block (like a bed)
 	 * 
 	 * @param block the source
 	 * @return the second block, or null if not found
 	 */
 	public static Block multipleBlocks(Block block) {
 		if (block == null) {
 			return null;
 		}
 		switch (block.getType()) {
 		case WOODEN_DOOR:
 		case IRON_DOOR_BLOCK:
 			Door door = (Door) block.getState().getData();
 			if (door.isTopHalf()) {
 				return block.getRelative(BlockFace.DOWN);
 			} else {
 				return block.getRelative(BlockFace.UP);
 			}
 		case BED_BLOCK:
 			Bed bed = (Bed) block.getState().getData();
 			if (bed.isHeadOfBed()) {
 				return block.getRelative(bed.getFacing().getOppositeFace());
 			} else {
 				return block.getRelative(bed.getFacing());
 			}
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the mob pattern for the supplied entity, if found
 	 * 
 	 * @param pattern the pattern to look for
 	 * @return the pattern. This will be null if the pattern is not found or unsupported
 	 */
 	public static MobPattern getMobPattern(EntityPattern pattern) {
 		if (pattern == null) {
 			return null;
 		}
 		switch (pattern) {
 		case SNOW_GOLEM:
 			if (SNOW_GOLEM_PATTERN == null) {
 				SNOW_GOLEM_PATTERN = new MobPattern(MobPatternType.I_SHAPE, Material.SNOW_BLOCK, Material.PUMPKIN, Material.JACK_O_LANTERN);
 			}
 			return SNOW_GOLEM_PATTERN;
 		case IRON_GOLEM:
 			if (IRON_GOLEM_PATTERN == null) {
 				IRON_GOLEM_PATTERN = new MobPattern(MobPatternType.T_SHAPE, Material.IRON_BLOCK, Material.PUMPKIN, Material.JACK_O_LANTERN);
 			}
 			return IRON_GOLEM_PATTERN;
 		case WITHER:
 			if (WITHER_PATTERN == null) {
 				WITHER_PATTERN = new MobPattern(MobPatternType.T_SHAPE, Material.SOUL_SAND, Material.SKULL);
 			}
 			return WITHER_PATTERN;
 		}
 		return null;
 	}
 
 	/**
 	 * Used to determine if something is blocked. This method will not check regions.
 	 * 
 	 * @param player the player, this is the source location
 	 * @param target the target item stack
 	 * @param list the list to check
 	 * @param permissions the permissions to use
 	 * @param configuration the configuration to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, ItemStack target, ASMaterialList list, PermissionPackage permissions, ASConfig configuration) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, isPotion = false, isThrownPotion = false;
 		if (target.getType() == Material.POTION) {
 			isPotion = true;
 			if (target.getDurability() > 32000) {
 				isThrownPotion = true;
 			}
 		}
 		AntiShare p = AntiShare.p;
 		if (list.has(target)) {
 			illegal = true;
 		}
 		if (isThrownPotion && configuration.thrownPotions) {
 			illegal = true;
 		} else if (isPotion && configuration.potions) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, target.getType())) {
 			illegal = false;
 		}
 		return new ProtectionInformation(illegal, false, null, null);
 	}
 
 	/**
 	 * Used to determine if something is blocked. This method will not check regions.
 	 * 
 	 * @param player the player, this is the source location
 	 * @param target the target item stack
 	 * @param list the list to check
 	 * @param permissions the permissions to use
 	 * @param configuration the configuration to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, Block target, ASMaterialList list, PermissionPackage permissions, ASConfig configuration) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false;
 		AntiShare p = AntiShare.p;
 		if (list.has(target)) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, target.getType())) {
 			illegal = false;
 		}
 		return new ProtectionInformation(illegal, false, null, null);
 	}
 
 	/**
 	 * Used to determine if something is blocked
 	 * 
 	 * @param player the player, this is the source location
 	 * @param target the target block
 	 * @param list the list to check
 	 * @param permissions the permissions to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, Block target, ASMaterialList list, PermissionPackage permissions) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, region = false;
 		AntiShare p = AntiShare.p;
 		Region sourceRegion = p.getRegionManager().getRegion(player.getLocation());
 		Region targetRegion = p.getRegionManager().getRegion(target.getLocation());
 		if (list.has(target)) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, target.getType())) {
 			illegal = false;
 		}
 		if (target != null && permissions.region != null && !player.hasPermission(permissions.region)) {
 			if (sourceRegion != targetRegion) {
 				illegal = true;
 				region = true;
 			}
 		}
 		return new ProtectionInformation(illegal, region, sourceRegion, targetRegion);
 	}
 
 	/**
 	 * Used to determine if something is blocked
 	 * 
 	 * @param player the player, this is the source location
 	 * @param item the item in question
 	 * @param target the target block
 	 * @param list the list to check
 	 * @param permissions the permissions to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, ItemStack item, Location target, ASMaterialList list, PermissionPackage permissions) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, region = false;
 		AntiShare p = AntiShare.p;
 		Region sourceRegion = p.getRegionManager().getRegion(player.getLocation());
 		Region targetRegion = p.getRegionManager().getRegion(target);
 		if (list.has(item)) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, item.getType())) {
 			illegal = false;
 		}
 		if (target != null && permissions.region != null && !player.hasPermission(permissions.region)) {
 			if (sourceRegion != targetRegion) {
 				illegal = true;
 				region = true;
 			}
 		}
 		return new ProtectionInformation(illegal, region, sourceRegion, targetRegion);
 	}
 
 	/**
 	 * Used to determine if something is blocked
 	 * 
 	 * @param player the player, this is the source location
 	 * @param item the item in question
 	 * @param target the target block
 	 * @param list the list to check
 	 * @param permissions the permissions to use
 	 * @param config the configuration object to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, ItemStack item, Location target, ASMaterialList list, PermissionPackage permissions, ASConfig config) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, region = false, isPotion = false, isThrownPotion = false;
 		AntiShare p = AntiShare.p;
 		Region sourceRegion = p.getRegionManager().getRegion(player.getLocation());
 		Region targetRegion = p.getRegionManager().getRegion(target);
 		if (list.has(item)) {
 			illegal = true;
 		}
 		if (item.getType() == Material.POTION) {
 			isPotion = true;
 			if (item.getDurability() > 32000) {
 				isThrownPotion = true;
 			}
 		}
 		if (isThrownPotion && config.thrownPotions) {
 			illegal = true;
 		} else if (isPotion && config.potions) {
 			illegal = true;
 		}
 
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, item.getType())) {
 			illegal = false;
 		}
 		if (target != null && permissions.region != null && !player.hasPermission(permissions.region)) {
 			if (sourceRegion != targetRegion) {
 				illegal = true;
 				region = true;
 			}
 		}
 		return new ProtectionInformation(illegal, region, sourceRegion, targetRegion);
 	}
 
 	/**
 	 * Used to determine if something is blocked
 	 * 
 	 * @param player the player, this is the source location
 	 * @param target the target location
 	 * @param list the list to check
 	 * @param object the object to check
 	 * @param permissions the permissions to use
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, Location target, ASMaterialList list, Object object, PermissionPackage permissions) {
 		if (player == null || list == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, region = false;
 		AntiShare p = AntiShare.p;
 		Region sourceRegion = p.getRegionManager().getRegion(player.getLocation());
 		Region targetRegion = p.getRegionManager().getRegion(target);
 		if (object instanceof Material && list.has((Material) object)) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, object instanceof Material ? (Material) object : null)) {
 			illegal = false;
 		}
 		if (target != null && permissions.region != null && !player.hasPermission(permissions.region)) {
 			if (sourceRegion != targetRegion) {
 				illegal = true;
 				region = true;
 			}
 		}
 		return new ProtectionInformation(illegal, region, sourceRegion, targetRegion);
 	}
 
 	/**
 	 * Used to determine if something is blocked
 	 * 
 	 * @param player the player
 	 * @param target the target location
 	 * @param list the list to check
 	 * @param object the object to check for
 	 * @param permissions the permission package
 	 * @return protection information
 	 */
 	public static ProtectionInformation isBlocked(Player player, Location target, List<EntityType> list, EntityType object, PermissionPackage permissions) {
 		if (player == null || list == null || object == null || permissions == null) {
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		boolean illegal = false, region = false;
 		AntiShare p = AntiShare.p;
 		Region sourceRegion = p.getRegionManager().getRegion(player.getLocation());
 		Region targetRegion = p.getRegionManager().getRegion(target);
 		if (list.contains(object)) {
 			illegal = true;
 		}
 		if (!p.isBlocked(player, permissions.allow, permissions.deny, object.getName())) {
 			illegal = false;
 		}
 		if (target != null && permissions.region != null && !player.hasPermission(permissions.region)) {
 			if (sourceRegion != targetRegion) {
 				illegal = true;
 				region = true;
 			}
 		}
 		return new ProtectionInformation(illegal, region, sourceRegion, targetRegion);
 	}
 
 	/**
 	 * Copies a file
 	 * 
 	 * @param source source file
 	 * @param destination destination file
 	 * @throws IOException thrown if something goes wrong
 	 */
 	public static void copyFile(File source, File destination) throws IOException {
 		if (source == null || destination == null) {
 			throw new IllegalArgumentException("Null files are not allowed");
 		}
 		if (!source.exists()) {
 			throw new IllegalArgumentException("Source file not found");
 		}
 		if (!destination.exists()) {
 			destination.createNewFile();
 		}
 		InputStream input = new FileInputStream(source);
 		FileOutputStream out = new FileOutputStream(destination);
 		byte[] buf = new byte[1024];
 		int len;
 		while((len = input.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		out.close();
 		input.close();
 	}
 
 	/**
 	 * Gets the location of the inventory holder.
 	 * 
 	 * @param holder the inventory holder
 	 * @return the location, or null if not found
 	 */
 	public static Location getLocation(InventoryHolder holder) {
 		if (holder instanceof BlockState) {
 			BlockState state = (BlockState) holder;
 			return state.getLocation();
 		} else if (holder instanceof Entity) {
 			Location entity = ((Entity) holder).getLocation();
 			return entity;
 		}
 		return null;
 	}
 
 }
