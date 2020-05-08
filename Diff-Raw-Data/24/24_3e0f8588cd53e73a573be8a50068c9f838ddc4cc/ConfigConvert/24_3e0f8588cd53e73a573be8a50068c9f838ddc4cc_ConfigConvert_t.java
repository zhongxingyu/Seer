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
 package com.turt2live.antishare.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.SelfCompatibility;
 import com.turt2live.antishare.util.SelfCompatibility.FileType;
 import com.turt2live.antishare.util.WrappedEnhancedConfiguration;
 
 /**
  * Converts a 5.3.0 configuration to a 5.4.0 configuration
  * 
  * @author turt2live
  */
 public class ConfigConvert{
 
 	/**
 	 * Does the actual conversion.<br>
 	 * <i>This does attempt to look for 5.4.0-BETA keys that may exist</i>
 	 */
 	public static void doConvert(){
 		AntiShare p = AntiShare.p;
 
 		File config = new File(p.getDataFolder(), "config.yml");
 
 		if(!config.exists()){
 			return; // Fresh install
 		}
 
 		File original = new File(p.getDataFolder(), "config-backup-temp.yml");
 
 		try{
			ASUtils.copyFile(config, new File(p.getDataFolder(), "config-backup.yml"));
 			ASUtils.copyFile(config, original);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		WrappedEnhancedConfiguration c = new WrappedEnhancedConfiguration(config, p);
 		EnhancedConfiguration o = new EnhancedConfiguration(original, p);
 
 		c.load();
 		o.load();
 
 		if(o.getString("other.version_string") != null){
			original.delete();
 			return; // Already converted!
 		}
 
 		c.clearFile();
 		c.save();
 
 		// Load block lists
 		c.set("lists.break", convertList(o.getString("blocked-lists.block-break")));
 		c.set("lists.place", convertList(o.getString("blocked-lists.block-place")));
 		c.set("lists.death", convertList(o.getString("blocked-lists.dropped-items-on-death")));
 		c.set("lists.pickup", convertList(o.getString("blocked-lists.picked-up-items")));
 		c.set("lists.drop", convertList(o.getString("blocked-lists.dropped-items")));
 		c.set("lists.use", convertList(o.getString("blocked-lists.use-items")));
 		c.set("lists.interact", convertList(o.getString("blocked-lists.right-click")));
 		c.set("lists.commands", convertList(o.getString("blocked-lists.commands")));
 		c.set("lists.attack-mobs", convertList(o.getString("blocked-lists.mobs")));
 		c.set("lists.interact-mobs", convertList(o.getString("blocked-lists.right-click-mobs")));
 		c.set("lists.crafting", convertList(o.getString("blocked-lists.crafting-recipes")));
 
 		// Load tracked lists
 		c.set("tracking.creative", convertList(o.getString("block-tracking.tracked-creative-blocks")));
 		c.set("tracking.survival", convertList(o.getString("block-tracking.tracked-survival-blocks")));
 		c.set("tracking.adventure", convertList(o.getString("block-tracking.tracked-adventure-blocks")));
 
 		// Convert crafted mobs
 		List<String> mobs = new ArrayList<String>();
 		if(o.getBoolean("enabled-features.mob-creation.allow-snow-golems")){
 			mobs.add("snow golem");
 		}
 		if(o.getBoolean("enabled-features.mob-creation.allow-iron-golems")){
 			mobs.add("iron golem");
 		}
 		if(o.getBoolean("enabled-features.mob-creation.allow-wither")){
 			mobs.add("wither");
 		}
 		c.set("lists.craft-mob", mobs);
 
 		// Interaction settings
 		c.set("interaction.survival-breaking-creative.deny", o.get("settings.survival-breaking-creative-blocks.deny"));
 		c.set("interaction.creative-breaking-survival.deny", o.get("settings.creative-breaking-survival-blocks.deny"));
 		c.set("interaction.survival-breaking-adventure.deny", o.get("settings.survival-breaking-adventure-blocks.deny"));
 		c.set("interaction.creative-breaking-adventure.deny", o.get("settings.creative-breaking-adventure-blocks.deny"));
 		c.set("interaction.adventure-breaking-survival.deny", o.get("settings.adventure-breaking-survival-blocks.deny"));
 		c.set("interaction.adventure-breaking-creative.deny", o.get("settings.adventure-breaking-creative-blocks.deny"));
 		c.set("interaction.survival-breaking-creative.drop-items", o.get("settings.survival-breaking-creative-blocks.block-drops"));
 		c.set("interaction.creative-breaking-survival.drop-items", o.get("settings.creative-breaking-survival-blocks.block-drops"));
 		c.set("interaction.survival-breaking-adventure.drop-items", o.get("settings.survival-breaking-adventure-blocks.block-drops"));
 		c.set("interaction.creative-breaking-adventure.drop-items", o.get("settings.creative-breaking-adventure-blocks.block-drops"));
 		c.set("interaction.adventure-breaking-survival.drop-items", o.get("settings.adventure-breaking-survival-blocks.block-drops"));
 		c.set("interaction.adventure-breaking-creative.drop-items", o.get("settings.adventure-breaking-creative-blocks.block-drops"));
 
 		// Cleanup functions
 		c.set("settings.cleanup.inventories.enabled", o.get("settings.cleanup.use"));
 		c.set("settings.cleanup.inventories.method", o.get("settings.cleanup.method"));
 		c.set("settings.cleanup.inventories.after", o.get("settings.cleanup.after"));
 		c.set("settings.cleanup.inventories.remove-old-worlds", o.get("settings.remove-old-inventories"));
 
 		// Gamemode cooldown
 		c.set("settings.cooldown.enabled", o.get("gamemode-change-cooldown.use"));
 		c.set("settings.cooldown.wait-time-seconds", o.get("gamemode-change-cooldown.time-in-seconds"));
 
 		// Natural protection
 		c.set("settings.natural-protection.allow-mismatch-gamemode", o.get("settings.similar-gamemode-allow"));
 		c.set("settings.natural-protection.remove-attached-blocks", o.get("settings.enabled-features.no-drops-when-block-break.attached-blocks"));
 		c.set("settings.natural-protection.empty-inventories", o.get("settings.enabled-features.no-drops-when-block-break.inventories"));
 
 		// Gamemode change settings
 		c.set("settings.gamemode-change.change-level", o.get("enabled-features.change-level-on-gamemode-change"));
 		c.set("settings.gamemode-change.change-economy-balance", o.get("enabled-features.change-balance-on-gamemode-change"));
 		c.set("settings.gamemode-change.change-inventory", o.get("handled-actions.gamemode-inventories"));
 		c.set("settings.gamemode-change.change-ender-chest", o.get("handled-actions.gamemode-ender-chests"));
 
 		// Hook settings
 		c.set("hooks.magicspells.block-creative", o.get("magicspells.block-creative"));
 		c.set("hooks.logblock.stop-spam", o.get("other.stop-logblock-spam"));
 
 		// Other settings
 		c.set("other.ignore-updates", o.get("other.dont-look-for-updates"));
 		c.set("settings.adventure-is-creative", o.get("other.adventure-is-creative"));
 		c.set("settings.use-per-world-inventories", o.get("handled-actions.world-transfers"));
 
 		c.save();
 
 		SelfCompatibility.cleanFile(config, FileType.CONFIGURATION);
 
 		original.delete();
 
 		p.getLogger().warning("=========================");
 		p.getLogger().warning(p.getMessages().getMessage("configuration-update"));
 		p.getLogger().warning("=========================");
 	}
 
 	private static List<String> convertList(String raw){
 		List<String> l = new ArrayList<String>();
 		if(raw == null){
 			return null;
 		}
 		String[] parts = raw.split(",");
 		for(String s : parts){
 			s = s.trim();
 			l.add(s);
 		}
 		return l;
 	}
 
 }
