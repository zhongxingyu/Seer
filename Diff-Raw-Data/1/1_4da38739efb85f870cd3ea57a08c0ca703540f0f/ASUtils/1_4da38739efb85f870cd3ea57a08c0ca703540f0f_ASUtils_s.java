 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.material.Attachable;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.tekkitcompat.EntityLayer;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.util.generic.ASEntity;
 
 /**
  * Utilities
  * 
  * @author turt2live
  */
 public class ASUtils {
 
 	public static final BlockFace[] realFaces = {BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.UP};
 
 	/**
 	 * Sends a message to a player.<br>
 	 * This will prefix "[AntiShare]" to the message and not send if the message is simply "no message".
 	 * 
 	 * @param target the player to send to
 	 * @param message the message to send
 	 * @param useSimpleNotice set to true if this method should use SimpleNotice if available
 	 */
 	public static void sendToPlayer(CommandSender target, String message, boolean useSimpleNotice){
 		if(!message.equalsIgnoreCase("nomsg")
 				&& !message.equalsIgnoreCase("no message")
 				&& !message.equalsIgnoreCase("none")
 				&& !message.equalsIgnoreCase("noshow")
 				&& !message.equalsIgnoreCase("no show")){
 			message = ChatColor.translateAlternateColorCodes('&', message);
 			String prefix = ChatColor.translateAlternateColorCodes('&', AntiShare.getInstance().getPrefix());
 			if(!ChatColor.stripColor(message).startsWith(ChatColor.stripColor(prefix))){
 				message = ChatColor.GRAY + prefix + " " + ChatColor.WHITE + message;
 			}
 			/* SimpleNotice support provided by feildmaster.
 			 * Support adapted by krinsdeath and further
 			 * modified by turt2live for AntiShare.
 			 */
 			if(target instanceof Player){
 				if(((Player) target).getListeningPluginChannels().contains("SimpleNotice")
 						&& useSimpleNotice
 						&& AntiShare.getInstance().isSimpleNoticeEnabled(target.getName())){
 					((Player) target).sendPluginMessage(AntiShare.getInstance(), "SimpleNotice", message.getBytes(java.nio.charset.Charset.forName("UTF-8")));
 				}else{
 					target.sendMessage(message);
 				}
 			}else{
 				target.sendMessage(message);
 			}
 		}
 	}
 
 	/**
 	 * Gets a boolean from a String
 	 * 
 	 * @param value the String
 	 * @return the boolean (or null if not found)
 	 */
 	public static Boolean getBoolean(String value){
 		if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("on")
 				|| value.equalsIgnoreCase("active") || value.equalsIgnoreCase("1")){
 			return true;
 		}else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("off")
 				|| value.equalsIgnoreCase("inactive") || value.equalsIgnoreCase("0")){
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
 	public static GameMode getGameMode(String value){
 		if(value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("1")){
 			return GameMode.CREATIVE;
 		}else if(value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("0")){
 			return GameMode.SURVIVAL;
 		}else if(ServerHas.adventureMode() && value.equalsIgnoreCase("adventure") || value.equalsIgnoreCase("a") || value.equalsIgnoreCase("2")){
 			return GameMode.ADVENTURE;
 		}
 		return null;
 	}
 
 	/**
 	 * Determines if a material can break a falling block (like sand)
 	 * 
 	 * @param material the material
 	 * @return true if it can break it
 	 */
 	public static boolean canBreakFallingBlock(Material material){
 		switch (material){
 		case RAILS:
 		case POWERED_RAIL:
 		case DETECTOR_RAIL:
 		case SAPLING:
 		case RED_ROSE:
 		case YELLOW_FLOWER:
 		case RED_MUSHROOM:
 		case BROWN_MUSHROOM:
 		case STONE_PLATE:
 		case WOOD_PLATE:
 		case LEVER:
 		case STONE_BUTTON:
 		case STRING:
 		case SIGN:
 		case WALL_SIGN:
 		case SIGN_POST:
 		case REDSTONE_WIRE:
 		case DIODE:
 		case TORCH:
 		case REDSTONE_TORCH_OFF:
 		case REDSTONE_TORCH_ON:
 		case CAKE_BLOCK:
 		case DIODE_BLOCK_OFF:
 		case DIODE_BLOCK_ON:
 		case STEP:
 		case WATER_LILY:
 			return true;
 		default:
 			if(ServerHas.mc14xItems()){
 				switch (material){
 				case ITEM_FRAME:
 				case FLOWER_POT:
 				case TRIPWIRE_HOOK:
 				case TRIPWIRE:
 					return true;
 				default:
 					return false;
 				}
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Determines if a block would be dropped if an attached block were to break.<br>
 	 * This also checks if the block is attached to a source.
 	 * 
 	 * @param block the block (attached to the breaking block)
 	 * @param source the block that the checked block may be attached to (null for no source)
 	 * @return true if the block would fall
 	 */
 	public static boolean isDroppedOnBreak(Block block, Block source){
 		boolean attached = false;
 		if(block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.STRING
 				|| block.getType() == Material.DIODE_BLOCK_OFF || block.getType() == Material.DIODE_BLOCK_ON
 				|| block.getType() == Material.DETECTOR_RAIL || block.getType() == Material.POWERED_RAIL
 				|| block.getType() == Material.RAILS || block.getType() == Material.RED_MUSHROOM
 				|| block.getType() == Material.RED_ROSE || block.getType() == Material.BROWN_MUSHROOM
 				|| block.getType() == Material.YELLOW_FLOWER || block.getType() == Material.STONE_PLATE
 				|| block.getType() == Material.WOOD_PLATE || block.getType() == Material.SEEDS
 				|| block.getType() == Material.WHEAT || block.getType() == Material.WOODEN_DOOR
 				|| block.getType() == Material.IRON_DOOR || block.getType() == Material.IRON_DOOR_BLOCK
 				|| block.getType() == Material.CROPS || block.getType() == Material.LONG_GRASS
 				|| block.getType() == Material.SAPLING || (ServerHas.mc14xItems() && (block.getType() == Material.ITEM_FRAME
 				|| block.getType() == Material.FLOWER_POT))){
 			// Check to ensure that the block is above the one we are breaking (so no nearby blocks are damaged)
 			Location l1 = source.getLocation();
 			Location l2 = block.getRelative(BlockFace.DOWN).getLocation();
 			attached = l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
 		}else if(block.getState().getData() instanceof Attachable && !block.getType().equals(Material.PISTON_EXTENSION)){
 			if(source != null){
 				Attachable att = (Attachable) block.getState().getData();
 				// We need to use location because Java is mean like that >.<
 				Location l1 = source.getLocation();
 				Location l2 = block.getRelative(att.getAttachedFace()).getLocation();
 				attached = l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
 			}else{
 				attached = true;
 			}
 		}
 		return attached;
 	}
 
 	/**
 	 * Capitalizes item names. Eg: EXP_BOTTLE -> Exp Bottle
 	 * 
 	 * @param string the string
 	 * @return the string, capitalized correctly
 	 */
 	public static String capitalize(String string){
 		String parts[] = string.toLowerCase().replaceAll(" ", "_").split("_");
 		StringBuilder returnString = new StringBuilder();
 		for(String part : parts){
 			// No need for part.substring(1).toLowerCase(), the split handles this
 			returnString.append(part.substring(0, 1).toUpperCase() + part.substring(1) + " ");
 		}
 		return returnString.toString().trim();
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
 	public static String blockToString(Block block, boolean zero){
 		if(block == null){
 			return null;
 		}
 		String typeId = "";
 		String data = "";
 		typeId = Integer.toString(block.getTypeId());
 		if(block.getType().getMaxDurability() > 0){
 			data = "0";
 		}else if(block.getData() > 0){
 			data = Byte.toString(block.getData());
 		}else{
 			data = "0";
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
 	public static String materialToString(Material material, boolean zero){
 		StringBuilder ret = new StringBuilder();
 		ret.append(material.getId());
 		if(!zero){
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
 	public static String getWool(String input){
 		if(!input.toLowerCase().contains("wool")){
 			return null;
 		}
 
 		String color = input.replace("wool", "").trim().toLowerCase();
 		color = color.replaceAll(" ", "_");
 		color = color.replace("orange", "1");
 		color = color.replace("white", "0");
 		color = color.replace("magenta", "2");
 		color = color.replace("light_blue", "3");
 		color = color.replace("yellow", "4");
 		color = color.replace("lime", "5");
 		color = color.replace("pink", "6");
 		color = color.replace("gray", "7");
 		color = color.replace("light_gray", "8");
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
 	 * Gets an entity name from an entity object
 	 * 
 	 * @param entity the entity
 	 * @return the name
 	 */
 	// TODO: Optimize
 	public static String getEntityName(Entity entity){
 		if(entity instanceof Blaze){
 			return "blaze";
 		}
 		if(entity instanceof CaveSpider){
 			return "cave spider";
 		}
 		if(entity instanceof Chicken){
 			return "chicken";
 		}
 		if(entity instanceof Cow){
 			return "cow";
 		}
 		if(entity instanceof Creeper){
 			return "creeper";
 		}
 		if(entity instanceof EnderDragon){
 			return "ender dragon";
 		}
 		if(entity instanceof Enderman){
 			return "enderman";
 		}
 		if(entity instanceof Ghast){
 			return "ghast";
 		}
 		if(entity instanceof Giant){
 			return "giant";
 		}
 		if(entity instanceof IronGolem){
 			return "iron golem";
 		}
 		if(entity instanceof MagmaCube){
 			return "magma cube";
 		}
 		if(entity instanceof MushroomCow){
 			return "mooshroom";
 		}
 		if(entity instanceof Ocelot){
 			return "ocelot";
 		}
 		if(entity instanceof Pig){
 			return "pig";
 		}
 		if(entity instanceof PigZombie){
 			return "pigman";
 		}
 		if(entity instanceof Sheep){
 			return "sheep";
 		}
 		if(entity instanceof Silverfish){
 			return "silverfish";
 		}
 		if(entity instanceof Skeleton){
 			return "skeleton";
 		}
 		if(entity instanceof Spider){
 			return "spider";
 		}
 		if(entity instanceof Squid){
 			return "squid";
 		}
 		if(entity instanceof Villager){
 			return "villager";
 		}
 		if(entity instanceof Wolf){
 			return "wolf";
 		}
 		if(entity instanceof Zombie){
 			return "zombie";
 		}
 		if(ServerHas.mc14xEntities()){
 			if(EntityLayer.isEntity(entity, "Witch")){
 				return "witch";
 			}
 			if(EntityLayer.isEntity(entity, "Wither")){
 				return "wither boss";
 			}
 			if(EntityLayer.isEntity(entity, "Bat")){
 				return "bat";
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Determines if the passed name is valid
 	 * 
 	 * @param entity the potential entity
 	 * @return the name, or null if invalid
 	 */
 	public static String getEntityName(String entity){
 		for(ASEntity asentity : allEntities()){
 			if(asentity.getProperName().equalsIgnoreCase(entity) || asentity.getGivenName().equalsIgnoreCase(entity)){
 				return asentity.getProperName();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a list of all entities AntiShare can handle
 	 * 
 	 * @return a list of entities
 	 */
 	public static List<ASEntity> allEntities(){
 		List<ASEntity> names = new ArrayList<ASEntity>();
 		names.add(new ASEntity("blaze", "blaze"));
 		names.add(new ASEntity("cavespider", "cave spider"));
 		names.add(new ASEntity("cave spider", "cave spider"));
 		names.add(new ASEntity("chicken", "chicken"));
 		names.add(new ASEntity("cow", "cow"));
 		names.add(new ASEntity("creeper", "creeper"));
 		names.add(new ASEntity("enderdragon", "ender dragon"));
 		names.add(new ASEntity("ender dragon", "ender dragon"));
 		names.add(new ASEntity("enderman", "enderman"));
 		names.add(new ASEntity("ghast", "ghast"));
 		names.add(new ASEntity("giant", "giant"));
 		names.add(new ASEntity("irongolem", "iron golem"));
 		names.add(new ASEntity("iron golem", "iron golem"));
 		names.add(new ASEntity("mushroomcow", "mooshroom"));
 		names.add(new ASEntity("mushroom cow", "mooshroom"));
 		names.add(new ASEntity("mooshroom", "mooshroom"));
 		names.add(new ASEntity("ocelot", "ocelot"));
 		names.add(new ASEntity("cat", "ocelot"));
 		names.add(new ASEntity("pig", "pig"));
 		names.add(new ASEntity("pigzombie", "pigman"));
 		names.add(new ASEntity("zombiepigman", "pigman"));
 		names.add(new ASEntity("pig zombie", "pigman"));
 		names.add(new ASEntity("zombie pigman", "pigman"));
 		names.add(new ASEntity("pigman", "pigman"));
 		names.add(new ASEntity("sheep", "sheep"));
 		names.add(new ASEntity("silverfish", "silverfish"));
 		names.add(new ASEntity("skeleton", "skeleton"));
 		names.add(new ASEntity("slime", "slime"));
 		names.add(new ASEntity("magmacube", "magma cube"));
 		names.add(new ASEntity("magma cube", "magma cube"));
 		names.add(new ASEntity("spider", "spider"));
 		names.add(new ASEntity("snowman", "snowman"));
 		names.add(new ASEntity("squid", "squid"));
 		names.add(new ASEntity("villager", "villager"));
 		names.add(new ASEntity("testificate", "villager"));
 		names.add(new ASEntity("wolf", "wolf"));
 		names.add(new ASEntity("zombie", "zombie"));
 		names.add(new ASEntity("witch", "witch"));
 		names.add(new ASEntity("wither", "wither boss"));
 		names.add(new ASEntity("witherboss", "wither boss"));
 		names.add(new ASEntity("wither boss", "wither boss"));
 		names.add(new ASEntity("bat", "bat"));
 		return names;
 	}
 
 	/**
 	 * Gets a list of online players with a defined Game Mode
 	 * 
 	 * @param gamemode the Game Mode
 	 * @return the player names with that Game Mode (online only)
 	 */
 	public static List<String> findGameModePlayers(GameMode gamemode){
 		List<String> affected = new ArrayList<String>();
 		for(Player player : Bukkit.getOnlinePlayers()){
 			if(player.getGameMode() == gamemode){
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
 	public static String commas(List<String> list){
 		StringBuilder commas = new StringBuilder();
 		for(String s : list){
 			commas.append(s).append(", ");
 		}
 		String finalComma = commas.toString().trim();
 		return finalComma.length() > 0 ? finalComma.substring(0, finalComma.length() - 1) : "no one";
 	}
 
 	/**
 	 * Determines if a material is affected by gravity (moves down when the block below it breaks)
 	 * 
 	 * @param material the material
 	 * @return true if gravity applies
 	 */
 	public static boolean isAffectedByGravity(Material material){
 		return material == Material.GRAVEL || material == Material.SAND || (ServerHas.mc14xItems() && material == Material.ANVIL);
 	}
 
 	/**
 	 * Returns true if the supplied material is breakable through water flow
 	 * 
 	 * @param type the material
 	 * @return true if it would be broken
 	 */
	// TODO: Test to make sure this list is complete
 	public static boolean canBeBrokenByWater(Material type){
 		switch (type){
 		case SAPLING:
 		case LONG_GRASS:
 		case DEAD_BUSH:
 		case YELLOW_FLOWER:
 		case RED_ROSE:
 		case BROWN_MUSHROOM:
 		case RED_MUSHROOM:
 		case TORCH:
 		case REDSTONE_WIRE:
 		case CROPS:
 		case STONE_PLATE:
 		case LEVER:
 		case WOOD_PLATE:
 		case STONE_BUTTON:
 		case WOOD_BUTTON:
 		case REDSTONE_TORCH_ON:
 		case REDSTONE_TORCH_OFF:
 		case SNOW:
 		case SUGAR_CANE_BLOCK:
 		case DIODE_BLOCK_ON:
 		case DIODE_BLOCK_OFF:
 		case PUMPKIN_STEM:
 		case MELON_STEM:
 		case VINE:
 		case COCOA:
 		case TRIPWIRE:
 		case TRIPWIRE_HOOK:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 }
