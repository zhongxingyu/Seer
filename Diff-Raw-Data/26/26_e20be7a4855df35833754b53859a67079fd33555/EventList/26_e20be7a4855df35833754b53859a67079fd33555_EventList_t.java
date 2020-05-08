 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.storage;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 import com.turt2live.antishare.ASUtils;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.signs.Sign;
 
 /**
  * Event List
  * 
  * @author turt2live
  */
 public class EventList {
 
 	private boolean whitelist = false;
 	private boolean useString = false;
 	private boolean expBlocked = false;
 	private boolean canRepair = true;
 	private boolean potions = true;
 	private boolean thrownPotions = true;
 	private List<String> blocked = new ArrayList<String>();
 	private List<String> blocked_strings = new ArrayList<String>();
 	private List<Sign> blockedsigns = new ArrayList<Sign>();
 
 	/**
 	 * Creates a new Event List
 	 * 
 	 * @param file the configuration file name
 	 * @param node the configuration node
 	 * @param configurationValue the values
 	 */
 	public EventList(String file, String node, String... configurationValue){
 		// Setup
 		AntiShare plugin = AntiShare.getInstance();
 
 		// Sanity
 		if(configurationValue.length <= 0){
 			return;
 		}
 
 		// Loop
 		for(int index = 0; index < configurationValue.length; index++){
 			String blocked = configurationValue[index].trim();
 
 			// Sanity
 			if(blocked.length() <= 0){
 				continue;
 			}
 
 			// Check negation
 			boolean negate = false;
 			if(blocked.startsWith("-")){
 				negate = true;
 				blocked = blocked.replaceFirst("-", "");
 			}
 
 			// Check for "all"/"none"
 			if(blocked.equalsIgnoreCase("*") || blocked.equalsIgnoreCase("all")){
 				// Add materials
 				for(Material m : Material.values()){
 					if(!negate){
 						this.blocked.add(ASUtils.materialToString(m, false));
 					}else{
 						this.blocked.remove(ASUtils.materialToString(m, false));
 					}
 				}
 
 				// Add signs
 				for(Sign s : plugin.getSignManager().getAllSigns()){
 					if(!negate){
 						blockedsigns.add(s);
 					}else{
 						blockedsigns.remove(s);
 					}
 				}
 				continue;
 			}else if(blocked.equalsIgnoreCase("none")){
 				blockedsigns.clear();
 				this.blocked.clear();
 				continue; // For sanity sake
 			}
 
 			// Whitelist?
 			if(blocked.equalsIgnoreCase("whitelist") && index == 0){
 				whitelist = true;
 				if(negate){
 					whitelist = false;
 				}
 				continue;
 			}
 
 			// Sign?
 			if(blocked.toLowerCase().startsWith("sign:")){
 				String signname = blocked.split(":").length > 0 ? blocked.split(":")[1] : null;
 				if(signname == null){
 					plugin.getMessenger().log("Configuration Problem: '" + (negate ? "-" : "") + blocked + "' is not valid! (See '" + node + "' in your " + file + ")", Level.WARNING, LogType.INFO);
 					continue;
 				}
 				Sign sign = plugin.getSignManager().getSign(signname);
 				if(sign == null){
 					plugin.getMessenger().log("Configuration Problem: '" + (negate ? "-" : "") + blocked + "' is not valid! (See '" + node + "' in your " + file + ")", Level.WARNING, LogType.INFO);
 					continue;
 				}
 				if(!negate){
 					blockedsigns.add(sign);
 				}else{
 					blockedsigns.remove(sign);
 				}
 				continue;
 			}
 
 			// Check if experience is to be blocked
 			if(blocked.equalsIgnoreCase("exp") || blocked.equalsIgnoreCase("experience") || blocked.equalsIgnoreCase("xp")){
 				expBlocked = true;
 				if(negate){
 					expBlocked = false;
 				}
 				continue;
 			}
 
 			// Check if repair
 			if(blocked.equalsIgnoreCase("repair") || blocked.equalsIgnoreCase("repair items") || blocked.equalsIgnoreCase("repair tools")){
 				canRepair = false;
 				if(negate){
 					canRepair = true;
 				}
 				continue;
 			}
 
 			// Check if potion
 			if(blocked.equalsIgnoreCase("potion") || blocked.equalsIgnoreCase("potions")){
 				potions = false;
 				if(negate){
 					potions = true;
 				}
 				continue;
 			}
 
 			// Check if thrown potion
 			if(blocked.equalsIgnoreCase("thrown potion") || blocked.equalsIgnoreCase("thrown potions")){
 				thrownPotions = false;
 				if(negate){
 					thrownPotions = true;
 				}
 				continue;
 			}
 
			// Special case: Furnaces
			if(blocked.equalsIgnoreCase("furnace")
					|| blocked.replaceAll(" ", "").replaceAll("_", "").equalsIgnoreCase("litfurnace")
					|| blocked.equalsIgnoreCase("oven")
					|| blocked.equalsIgnoreCase(String.valueOf(Material.FURNACE.getId()))
					|| blocked.equalsIgnoreCase(String.valueOf(Material.BURNING_FURNACE.getId()))){
				if(!negate){
					this.blocked.add(ASUtils.materialToString(Material.FURNACE, false));
					this.blocked.add(ASUtils.materialToString(Material.BURNING_FURNACE, false));
				}else{
					this.blocked.remove(ASUtils.materialToString(Material.FURNACE, false));
					this.blocked.remove(ASUtils.materialToString(Material.BURNING_FURNACE, false));
				}
				continue;
			}

 			// Special case: Signs
 			if(blocked.equalsIgnoreCase("sign")
 					|| blocked.replaceAll(" ", "").replaceAll("_", "").equalsIgnoreCase("wallsign")
 					|| blocked.replaceAll(" ", "").replaceAll("_", "").equalsIgnoreCase("signpost")
 					|| blocked.equalsIgnoreCase(String.valueOf(Material.SIGN.getId()))
 					|| blocked.equalsIgnoreCase(String.valueOf(Material.WALL_SIGN.getId()))
 					|| blocked.equalsIgnoreCase(String.valueOf(Material.SIGN_POST.getId()))){
 				if(!negate){
 					this.blocked.add(ASUtils.materialToString(Material.SIGN, false));
 					this.blocked.add(ASUtils.materialToString(Material.SIGN_POST, false));
 					this.blocked.add(ASUtils.materialToString(Material.WALL_SIGN, false));
 				}else{
 					this.blocked.remove(ASUtils.materialToString(Material.SIGN, false));
 					this.blocked.remove(ASUtils.materialToString(Material.SIGN_POST, false));
 					this.blocked.remove(ASUtils.materialToString(Material.WALL_SIGN, false));
 				}
 				continue;
 			}
 
 			// Special case: Brewing Stand
 			if(blocked.replaceAll(" ", "").replaceAll("_", "").equalsIgnoreCase("brewingstand")
 					|| blocked.replaceAll(" ", "").replaceAll("_", "").equalsIgnoreCase("brewingstanditem")
 					|| blocked.equalsIgnoreCase(String.valueOf(Material.BREWING_STAND.getId()))
 					|| blocked.equalsIgnoreCase(String.valueOf(Material.BREWING_STAND_ITEM.getId()))){
 				if(!negate){
 					this.blocked.add(ASUtils.materialToString(Material.BREWING_STAND, false));
 					this.blocked.add(ASUtils.materialToString(Material.BREWING_STAND_ITEM, false));
 				}else{
 					this.blocked.remove(ASUtils.materialToString(Material.BREWING_STAND, false));
 					this.blocked.remove(ASUtils.materialToString(Material.BREWING_STAND_ITEM, false));
 				}
 				continue;
 			}
 
 			// Special case: Wool
 			if(ASUtils.getWool(blocked) != null){
 				if(!negate){
 					this.blocked.add(ASUtils.getWool(blocked));
 				}else{
 					this.blocked.remove(ASUtils.getWool(blocked));
 				}
 				continue;
 			}
 
 			// Try to add the item, warn otherwise
 			if(plugin.getItemMap().getSign(blocked) != null){
 				if(!negate){
 					this.blockedsigns.add(plugin.getItemMap().getSign(blocked));
 				}else{
 					this.blockedsigns.remove(plugin.getItemMap().getSign(blocked));
 				}
 				continue;
 			}
 			try{
 				if(plugin.getItemMap().getItem(blocked, false) == null){
 					throw new Exception("");
 				}
 				if(!negate){
 					this.blocked.add(plugin.getItemMap().getItem(blocked, false));
 				}else{
 					this.blocked.remove(plugin.getItemMap().getItem(blocked, false));
 				}
 			}catch(Exception e){
 				plugin.getMessenger().log("Configuration Problem: '" + (negate ? "-" : "") + blocked + "' is not valid! (See '" + node + "' in your " + file + ")", Level.WARNING, LogType.INFO);
 			}
 		}
 	}
 
 	/**
 	 * Creates a new Event List
 	 * 
 	 * @param stringsOnly true to set strings only
 	 * @param configurationValue the values
 	 */
 	public EventList(boolean stringsOnly, String... configurationValue){
 		this.useString = stringsOnly;
 		int index = 0;
 
 		// Sanity
 		if(configurationValue.length <= 0){
 			return;
 		}
 
 		// Loop through objects
 		for(String blocked : configurationValue){
 			blocked = blocked.trim();
 
 			// Sanity
 			if(blocked.length() <= 0){
 				continue;
 			}
 
 			// Check negation
 			boolean negate = false;
 			if(blocked.startsWith("-")){
 				negate = true;
 				blocked = blocked.replaceFirst("-", "");
 			}
 
 			// Check list for 'none'
 			if(blocked.equalsIgnoreCase("none")){
 				continue;
 			}
 
 			// Whitelist?
 			if(blocked.equalsIgnoreCase("whitelist") && index == 0){
 				whitelist = true;
 				if(negate){
 					whitelist = false;
 				}
 				index++;
 				continue;
 			}
 
 			// Add a '/' to all strings
 			if(!blocked.startsWith("/")){
 				blocked = blocked + "/";
 			}
 
 			// Add item
 			if(negate){
 				this.blocked_strings.remove(blocked.trim().toLowerCase());
 			}else{
 				this.blocked_strings.add(blocked.trim().toLowerCase());
 			}
 			index++;
 		}
 	}
 
 	/**
 	 * Checks if a block is blocked
 	 * 
 	 * @param block the block
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Block block){
 		if(block.getState() instanceof org.bukkit.block.Sign){
 			org.bukkit.block.Sign cbsign = (org.bukkit.block.Sign) block.getState();
 			for(int i = 0; i < blockedsigns.size(); i++){
 				Sign s = blockedsigns.get(i);
 				if(s.matches(cbsign)){
 					return true;
 				}
 			}
 		}
 		if(whitelist){
 			boolean contained = !blocked.contains(ASUtils.blockToString(block, false));
 			if(!contained){
 				contained = !blocked.contains(block.getTypeId() + ":*");
 			}
 			return contained;
 		}
 		boolean contained = blocked.contains(ASUtils.blockToString(block, false));
 		if(!contained){
 			contained = blocked.contains(block.getTypeId() + ":*");
 		}
 		return contained;
 	}
 
 	/**
 	 * Checks if an item is blocked
 	 * 
 	 * @param item the item
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Material item){
 		if(blocked.size() == 0){
 			return false;
 		}
 		if(whitelist){
 			boolean contained = !blocked.contains(ASUtils.materialToString(item, false));
 			if(!contained){
 				contained = !blocked.contains(item.getId() + ":*");
 			}
 			return contained;
 		}
 		boolean contained = blocked.contains(ASUtils.materialToString(item, false));
 		if(!contained){
 			contained = blocked.contains(item.getId() + ":*");
 		}
 		return contained;
 	}
 
 	/**
 	 * Checks if a string (usually command) is blocked
 	 * 
 	 * @param command the string
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(String command){
 		if(!useString){
 			return false;
 		}
 		if(blocked_strings.size() == 0){
 			return false;
 		}
 
 		// Prefix '/' if needed
 		if(!command.startsWith("/")){
 			command = "/" + command;
 		}
 
 		// Prepare
 		command = command.trim().toLowerCase();
 		boolean found = false;
 
 		// Loop through list and look for a match
 		for(String string : blocked_strings){
 			if(command.startsWith(string) || command.equalsIgnoreCase(string)){
 				found = true;
 				break;
 			}
 		}
 
 		// Return correct value
 		if(whitelist){
 			return found == false;
 		}
 		return found == true;
 	}
 
 	/**
 	 * Checks if experience if blocked
 	 * 
 	 * @return true if blocked
 	 */
 	public boolean isExperienceBlocked(){
 		return expBlocked;
 	}
 
 	/**
 	 * Checks if repairing of items is blocked
 	 * 
 	 * @return true if blocked
 	 */
 	public boolean isRepairBlocked(){
 		return !canRepair;
 	}
 
 	/**
 	 * Checks if potions (non-throwable) can be used
 	 * 
 	 * @return true if allowed
 	 */
 	public boolean isPotionAllowed(){
 		return potions;
 	}
 
 	/**
 	 * Checks if potions (throwable) can be used
 	 * 
 	 * @return true if allowed
 	 */
 	public boolean isThrownPotionAllowed(){
 		return thrownPotions;
 	}
 
 }
