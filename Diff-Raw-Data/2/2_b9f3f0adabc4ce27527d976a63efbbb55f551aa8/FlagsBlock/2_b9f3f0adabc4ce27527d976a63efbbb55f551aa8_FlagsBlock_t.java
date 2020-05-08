 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share  to copy, distribute and transmit the work
     to Remix  to adapt the work
 
  Under the following conditions:
     Attribution  You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial  You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver  Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain  Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights  In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice  For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
 */
 
 package alshain01.FlagsBlock;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import alshain01.Flags.Flag;
 import alshain01.Flags.Flags;
 import alshain01.Flags.ModuleYML;
 import alshain01.Flags.Registrar;
 import alshain01.Flags.Director;
 
 /**
  * Flags - Block
  * Module that adds block flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsBlock extends JavaPlugin {
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable(){
 		// Connect to the data file
 		ModuleYML dataFile = new ModuleYML(this, "flags.yml");
 		
 		
 		// Register with Flags
 		Registrar flags = Flags.instance.getRegistrar();
 		for(String f : dataFile.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
 			ConfigurationSection data = dataFile.getModuleData().getConfigurationSection("Flag." + f);
 			
 			// The description that appears when using help commands.
 			String desc = data.getString("Description");
 			
 			// Register it!
 			// Be sure to send a plug-in name or group description for the help command!
 			// It can be this.getName() or another string.
			flags.register(f, desc, true, "Block");
 		}
 		
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager().registerEvents(new BlockListener(), this);
 	}
 	
 	/*
 	 * The event handlers for the flags we created earlier
 	 */
 	public class BlockListener implements Listener{
 		/*
 		 * Snow and Ice form event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockForm(BlockFormEvent e) {
 			Flag flag = null;
 			if (e.getNewState().getType() == Material.SNOW) {
 				flag = Flags.instance.getRegistrar().getFlag("Snow");
 			} else if (e.getNewState().getType() == Material.ICE) {
 				flag = Flags.instance.getRegistrar().getFlag("Ice");
 			}
 			
 			if (flag != null) {
 				e.setCancelled(!Director.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 		
 		/*
 		 * Snow and Ice melt event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockFade(BlockFadeEvent e) {
 			Flag flag = null;
 			if (e.getBlock().getType() == Material.SNOW) {
 				flag = Flags.instance.getRegistrar().getFlag("SnowMelt");
 			} else if (e.getBlock().getType() == Material.ICE) {
 				flag = Flags.instance.getRegistrar().getFlag("IceMelt");
 			}
 			
 			if (flag != null) {
 				e.setCancelled(!Director.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 		
 		/*
 		 * Grass spread event handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockSpread(BlockSpreadEvent e) {
 			if (e.getNewState().getType() == Material.GRASS) {
 				Flag flag = Flags.instance.getRegistrar().getFlag("Grass");
 				
 				if (flag != null) {
 					e.setCancelled(!Director.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 				}
 			}
 		}
 		
 		/*
 		 * Dragon Egg Teleport handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onBlockFromTo(BlockFromToEvent e) {
 			if (e.getBlock().getType() == Material.DRAGON_EGG) {
 				Flag flag = Flags.instance.getRegistrar().getFlag("DragonEggTp");
 				
 				if (flag != null) {
 					e.setCancelled(!Director.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 				}
 			}
 		}
 		
 		/*
 		 * Leaf Decay handler
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onLeafDecay(LeavesDecayEvent e) {
 			Flag flag = Flags.instance.getRegistrar().getFlag("LeafDecay");
 			if (flag != null) {
 				e.setCancelled(!Director.getAreaAt(e.getBlock().getLocation()).getValue(flag, false));
 			}
 		}
 	}
 }
