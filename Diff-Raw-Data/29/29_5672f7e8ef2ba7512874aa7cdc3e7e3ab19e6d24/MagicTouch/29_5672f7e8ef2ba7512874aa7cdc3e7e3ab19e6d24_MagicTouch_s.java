 package com.github.etsija;
 
 import java.util.logging.Logger;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import de.diddiz.LogBlock.Consumer;
 import de.diddiz.LogBlock.LogBlock;
 
 public class MagicTouch extends JavaPlugin {
 
 	private Logger _log = Logger.getLogger("Minecraft");
 	private Consumer lbConsumer = null;
 	
 	// Connect to WG plugin to respect build-restricted areas
 	private WorldGuardPlugin getWorldGuard() {
 		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 	 
 		// WorldGuard may not be loaded
 		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 			return null; // Maybe you want throw an exception instead
 		}
 	 
 		return (WorldGuardPlugin) plugin;
 	}
 	
 	public void onEnable(){
 		// Register the listener for the tool to use
 		getServer().getPluginManager().registerEvents(new pListener(), this);
 		final PluginManager pm = getServer().getPluginManager();
 		final Plugin plugin = pm.getPlugin("LogBlock");
 		if (plugin != null) {
 			lbConsumer = ((LogBlock)plugin).getConsumer();
 			_log.info("[MagicTouch] hooked into LogBlock");
 		}
 		
 		// Create a default config.yml if one does not exist
 		saveDefaultConfig();
 		
 		_log.info("[MagicTouch] has been enabled!");
 	}
 	 
 	public void onDisable(){ 
 		_log.info("[MagicTouch] has been disabled!");
 	}
 
 	// This is the listener which listens to right-click of the chosen tool
 	public class pListener implements Listener {
 
 		private boolean shiftClick = false;
 		
 		// Read configuration params from config.yml
 		private int magicTool = getConfig().getInt("general.magictool");
 		private Boolean useWG = getConfig().getBoolean("general.use_worldguard");
 		private Boolean useLB = getConfig().getBoolean("general.use_logblock");
 		private Boolean rotateLogs = getConfig().getBoolean("blocks.logs");
 		private Boolean rotateStairs = getConfig().getBoolean("blocks.stairs");
 		private Boolean rotatePistons = getConfig().getBoolean("blocks.pistons");
 		private Boolean rotateChests = getConfig().getBoolean("blocks.chests");
 		private Boolean rotateSlabs = getConfig().getBoolean("blocks.slabs");
 		
 		@EventHandler
 		public void onPlayerInteract(PlayerInteractEvent event) {
 			
 			if(event.isCancelled()) return;
 			
 			// Only left-click does something
 			final Action action = event.getAction();
 			if (action != Action.LEFT_CLICK_BLOCK) {
 				return;
 			}
 
 			// Main loop to handle left-clicking with the magic tool
 			Player player = event.getPlayer();
 			int itemInHand = player.getItemInHand().getTypeId();
 			
 			if (itemInHand == magicTool) {
 				
 				// Did the player shift-click?
 				shiftClick = player.isSneaking();
 				
 				Block block = event.getClickedBlock();
 				Material material = block.getType();
 				
 				// Can you actually build in this area? Let WG decide
 				if (useWG) {
 					if (!getWorldGuard().canBuild(player, block)) {
 						player.sendMessage(ChatColor.RED + "[MagicTouch] You cannot build in this area!");
 						return;
 					}
 				}
 				
				// Log your change with LogBlock
				if (useLB) {
					int typeAfter = 0;
					byte dataAfter = 0;
					lbConsumer.queueBlockReplace(player.getName(), block.getState(), typeAfter, dataAfter);
				}
				
 				//_log.info("Type = " + material);
 				
 				// ----------------------------------------------------------------------
 				// Main loop for different clickable materials
 				// ----------------------------------------------------------------------
 				
 				// LOGS:
 				// U/D -> E/W -> N/S -> U/D...
 				if (rotateLogs && 
 				   (material == Material.LOG)) {
 					byte data = block.getData();
 					if (data < 4) {
 						block.setData((byte)(data + 4));
 					} else if (data < 8) {
 	                    block.setData((byte)(data + 4));
 	                } else if (data < 12){
 	                    block.setData((byte)(data - 8));
 	                }
 					
 				// STAIRS: 
 				// N(3) -> E(0) -> S(2) -> W(1) -> N(3)...
 				// With shift-click, flip stairs upside down
 				} else if (rotateStairs && (
 						  (material == Material.WOOD_STAIRS) ||
 						  (material == Material.SPRUCE_WOOD_STAIRS) ||
 						  (material == Material.BIRCH_WOOD_STAIRS) ||
 						  (material == Material.JUNGLE_WOOD_STAIRS) ||
 						  (material == Material.COBBLESTONE_STAIRS) ||
 						  (material == Material.SMOOTH_STAIRS) ||
 						  (material == Material.SANDSTONE_STAIRS) ||
 						  (material == Material.BRICK_STAIRS) ||
 						  (material == Material.NETHER_BRICK_STAIRS))) {
 					byte data = block.getData();
                     int flipStatus = (data & 0x4) >> 2; // Save bit 3 (0x3)
 					int orientation = data & 0x3;
                     if (shiftClick) {
                         // Flip the block upside down
                         flipStatus = ~flipStatus;
                     } else {
                     	// Rotate the block
                         switch (orientation) {
                             case 3:
                                 orientation = 0;
                                 break;
                             case 0:
                                 orientation = 2;
                                 break;
                             case 2:
                                 orientation = 1;
                                 break;
                             case 1:
                                 orientation = 3;
                                 break;
                         }
                     }
                     // Combine flipStatus (bit 3) and orientation (bits 2-0)
                     data = (byte) ((flipStatus << 2) | orientation);
 					block.setData(data);
 
 				// PISTONS:
 				// N(2) -> E(5) -> S(3) -> W(4) -> U(1) -> D(0) -> N(2)...
 				} else if (rotatePistons && (
 						  (material == Material.PISTON_BASE) ||
 						  (material == Material.PISTON_STICKY_BASE))) {
 					byte data = block.getData();
 					int isExtended = (data & 0x8) >> 3; // Save bit 4 (0x8) = extension status
 					int orientation = data & 0x7;
 					switch (orientation) {
 						case 2:
 							orientation = 5;
 							break;
 						case 5:
 							orientation = 3;
 							break;
 						case 3:
 							orientation = 4;
 							break;
 						case 4:
 							orientation = 1;
 							break;
 						case 1:
 							orientation = 0;
 							break;
 						case 0:
 							orientation = 2;
 							break;
 					}
 					// Combine extension status (bit 4) and orientation (bits 3-0)
 					data = (byte) ((isExtended << 3) | orientation);
 					block.setData(data);
 		
 				// CHESTS, FURNACES, DISPENSERS:
 				// N(2) -> E(5) -> S(3) -> W(4) -> N(2)...
 				} else if (rotateChests && (
 						  (material == Material.CHEST) ||
 						  (material == Material.ENDER_CHEST) ||
 						  (material == Material.FURNACE) ||
 						  (material == Material.DISPENSER))) {
 					byte data = block.getData();
 					switch (data) {
 						case 2:
 							block.setData((byte) 5);
 							break;
 						case 5:
 							block.setData((byte) 3);
 							break;
 						case 3:
 							block.setData((byte) 4);
 							break;
 						case 4:
 							block.setData((byte) 2);
 							break;
 					}
 				
 				// SLABS:
 				// right-side up -> upside down -> ...
 				} else if (rotateSlabs && (material == Material.STEP)) {
 					byte data = block.getData();
 					if (data < 8) {
 						block.setData((byte)(data + 8));
 					} else {
 	                    block.setData((byte)(data - 8));
 	                }
 				}
 			}
 		}
 	}
 }
