 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flagsblock;
 
 import io.github.alshain01.flags.*;
 import io.github.alshain01.flags.System;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Flags Block - Module that adds block flags to the plug-in Flags.
  */
 public class FlagsBlock extends JavaPlugin {
 
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable() {
 		final PluginManager pm = Bukkit.getServer().getPluginManager();
 
 		if (!pm.isPluginEnabled("Flags")) {
 			getLogger().severe("Flags was not found. Shutting down.");
 			pm.disablePlugin(this);
 		}
 
 		// Connect to the data file and register the flags
 		Set<Flag> flags = Flags.getRegistrar().register(new ModuleYML(this, "flags.yml"), "Block");
         Map<String, Flag> flagMap = new HashMap<String, Flag>();
         for(Flag f : flags) {
             flagMap.put(f.getName(), f);
         }
 
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager().registerEvents(new BlockListener(flagMap), this);
 	}
 	
 	/*
 	 * The event handlers for the flags we created earlier
 	 */
 	private class BlockListener implements Listener {
         final System system = System.getActive();
         final Map<String, Flag> flags;
 
         private BlockListener(Map<String, Flag> flags) {
             this.flags = flags;
         }
 
 		/*
 		 * Snow and Ice melt event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockFade(BlockFadeEvent e) {
 			Flag flag;
 			switch(e.getBlock().getType()) {
 				case SNOW:
 					flag = flags.get("SnowMelt");
 					break;
 				case ICE:
 					flag = flags.get("IceMelt");
 					break;
 				default:
 					return;
 			}
 
 			if (flag != null) {
 				e.setCancelled(!system.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Snow and Ice form event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockForm(BlockFormEvent e) {
 			Flag flag;
			switch(e.getNewState().getType()) {
 			case SNOW:
 				flag = flags.get("Snow");
 				break;
 			case ICE:
 				flag = flags.get("Ice");
 				break;
 			default:
 				return;
 			}
 
 			if (flag != null) {
				e.setCancelled(!system.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Dragon Egg Teleport handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockFromTo(BlockFromToEvent e) {
 			final Flag flag = flags.get("DragonEggTp");
 			if (flag != null) {
 				e.setCancelled(!system.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Grass spread event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockSpread(BlockSpreadEvent e) {
 			Flag flag;
			switch(e.getNewState().getType()) {
 				case GRASS:
 					flag = flags.get("Grass");
 					break;
                 case FIRE:
                     flag = flags.get("Fire");
                     break;
                 case BROWN_MUSHROOM:
                 case RED_MUSHROOM:
                     flag = flags.get("Mushroom");
                     break;
 				default:
 					return;
 			}
 			
 			if (flag != null) {
				e.setCancelled(!system.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Leaf Decay handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onLeafDecay(LeavesDecayEvent e) {
 			final Flag flag = flags.get("LeafDecay");
 			if (flag != null) {
 				e.setCancelled(!system.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 	}
 }
