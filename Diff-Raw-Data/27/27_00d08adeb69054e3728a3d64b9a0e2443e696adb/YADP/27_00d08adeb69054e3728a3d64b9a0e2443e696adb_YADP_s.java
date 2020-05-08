 package com.github.peter200lx.yadp;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.CropState;
 import org.bukkit.DyeColor;
 import org.bukkit.GrassSpecies;
 import org.bukkit.Material;
 import org.bukkit.TreeSpecies;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class YADP extends JavaPlugin {
 	private Logger log = Logger.getLogger("Minecraft");
 	
 	public static boolean debug = false;
 
 	private static boolean permissions;
 
 	public static HashMap<String, Material> tools;
 
 	public static HashMap<Material, Material> dupeMap;
 
 	public static HashMap<Material, Integer> dataMap;
 
 	public static HashSet<Material> keepData;
 
 	@Override
 	public void onDisable() {
 
 	}
 
 	@Override
 	public void onEnable() {
 		if(loadConf()) {
 			// Register our events
 			getServer().getPluginManager().registerEvents(new ToolListener(), this);
 
 			//Print yadp loaded message
 			if(YADP.debug) {
 				PluginDescriptionFile pdfFile = this.getDescription();
 				log.info( "["+pdfFile.getName() + "] version " + pdfFile.getVersion() +
 						" is now loaded with debug enabled" );
 			}
 		} else {
 			log.warning( "[yadp] had an error loading config.yml and is now disabled");
 			this.setEnabled(false);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		//Safety check for determining console status
 		boolean console = true;
 		if (sender instanceof Player) {
 			console = false;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("yadp")&&(args.length == 1)){
 			if(args[0].contentEquals("reload")) {
 				if(YADP.hasPerm(sender,"yadp.reload")) {
 					this.reloadConfig();
 					if(loadConf())
 						sender.sendMessage("Configuration file config.yml has been reloaded");
 					else {
 						sender.sendMessage("[WARNING] Configuration file load error, "+
 						"check console logs");
 						YADP.tools = new HashMap<String,Material>();
 						sender.sendMessage("[WARNING] Tools have been disabled until "+
 								"a valid config file is loaded");
 					}
 				} else {
 					sender.sendMessage( "You don't have permission to reload the config file");
 				}
 				return true;
 			} else if (args[0].contentEquals("tools")) {
 				if (console) {
 					sender.sendMessage("This command can only be run by a player"); 
 				} else {
 					Boolean any = false;
 					if((YADP.hasPerm(sender,"yadp.tool.dupe"))&&
 							(YADP.tools.containsKey("dupe"))) {
 						any = true;
 						sender.sendMessage("Right-click with the "+YADP.tools.get("dupe")+
 								" to duplicate the item selected");
 					}
 					if((YADP.hasPerm(sender,"yadp.tool.scroll"))&&
 							(YADP.tools.containsKey("scroll"))) {
 						any = true;
 						sender.sendMessage("Click with the "+YADP.tools.get("scroll")+
 						" to change a block's data value");
 					}
 					if(any == false) {
 						sender.sendMessage("There are currently no tools " +
 								"that you can access");
 					}
 				}
 				return true;
 			}
 			//Add else "tool" with ability to enable/disable tool per person?
 		}
 		return false; 
 	}
 
 	public static Boolean hasPerm(CommandSender p,String what) {
 		if(YADP.permissions)
 			return p.hasPermission(what);
 		else
 			return true;
 	}
 
 	/* This function is currently not needed, as we can't set variables live
 	private void saveConf() {
 		this.getConfig().set("debug", YADP.debug);
 		this.getConfig().set("tools.bind", YADP.tools);
 		this.getConfig().set("tools.dupe.replace", YADP.dupeMap);
 		this.getConfig().set("tools.dupe.keepData", YADP.keepData.toArray());
 		this.getConfig().set("tools.scroll.dataMap", YADP.dataMap);
 		try {
 			this.getConfig().save("yadp_debug.yml");
 			log.info("[yadp][saveConf] yadp_debug.yml saved in craftbukkit folder");
 		} catch (IOException e) {
 			log.warning( "[yadp] Was unable to save yadp_debug.yml" );
 			e.printStackTrace();
 		}
 	}*/
 
 	private Boolean loadConf() {
 		// Load and/or initialize configuration file
 		if(!this.getConfig().isSet("tools")) {
 			this.saveDefaultConfig();
 			log.info( "[yadp][loadConf] config.yml copied from .jar (likely first run)" );
 		}
 
 		//Reload and hold config for this function
 		FileConfiguration conf = this.getConfig();
 
 		//Check and set the debug printout flag
 		Boolean old = YADP.debug;
 		YADP.debug = conf.getBoolean("debug", false);
 		if(YADP.debug) log.info( "[yadp][loadConf] Debugging is enabled");
 		if(old && (!YADP.debug))
 			log.info("[yadp][loadConf] Debugging has been disabled");
 
 		//Check and set the permissions flag
 		YADP.permissions = conf.getBoolean("permissions", true);
 		if(YADP.debug) log.info( "[yadp][loadConf] permmissions are "+YADP.permissions);
 
 		ConfigurationSection sect = conf.getConfigurationSection("tools.bind");
 
 		if(sect == null) {
 			log.warning("[yadp] tools.bind is returning null");
 			return false;
 		}
 
 		HashMap<String, Material> holdTool = new HashMap<String, Material>();
 		for(Entry<String, Object> entry :sect.getValues(false).entrySet()) {
 			if(entry.getValue() instanceof Number) {
 				int id = ((Number)entry.getValue()).intValue();
 				if(id > 0) {
 					Material type = Material.getMaterial(id);
 					if(type != null) {
 						if(!holdTool.containsValue(type)) {
 							holdTool.put(entry.getKey(),type);
 							if(YADP.debug) log.info( "[yadp][loadConf] tools: " +
 									entry.getKey() + " is now " + type);
 							continue;
 						} else {
 							log.warning("[yadp] tools.bind."+entry.getKey()+
 									": '"+entry.getValue() + "' has a duplicate " +
 									"id of another tool");
 							return false;
 						}
 					}
 				}
 			}
 			log.warning("[yadp] tools.bind."+entry.getKey()+
 					": '"+entry.getValue() + "' is not a Material type" );
 			//No return false; here so that an admin can disable tools
 			//    by setting them to zero and such
 		}
 		YADP.tools = holdTool;
 
 		sect = conf.getConfigurationSection("tools.dupe.replace");
 
 		if(sect == null) {
 			log.warning("[yadp] tools.dupe.replace is returning null");
 			return false;
 		}
 
 		HashMap<Material, Material> holdDupeMap = defDupeMap();
 		for(Entry<String, Object> entry :sect.getValues(false).entrySet()) {
 			try {
 				int key = Integer.parseInt(entry.getKey());
 				if(entry.getValue() instanceof Number) {
 					int val = ((Number)entry.getValue()).intValue();
 					if((key > 0)&&(val > 0)) {
 						Material keyType = Material.getMaterial(key);
 						Material valType = Material.getMaterial(val);
 						if((keyType != null)&&(valType != null)) {
 							holdDupeMap.put(keyType,valType);
 							if(YADP.debug) log.info( "[yadp][loadConf] added to dupeMap: " +
 									keyType + " to " + valType);
 							continue;
 						}
 					}
 				}
 				log.warning("[yadp] tools.dupe.replace: '"+entry.getKey()+
 						"': '"+entry.getValue() + "' is not a Material type" );
 				return false;
 			} catch(NumberFormatException e) {
 				log.warning("[yadp] tools.dupe.replace: '"+entry.getKey()+
 						"': is not an integer" );
 				return false;
 			}
 		}
 		YADP.dupeMap = holdDupeMap;
 
 		List<Integer> intL = conf.getIntegerList("tools.dupe.keepData");
 
 		if(intL == null) {
 			log.warning("[yadp] tools.dupe.keepData is returning null");
 			return false;
 		}
 
 		HashSet<Material> holdKeepData = new HashSet<Material>();
 		for(Integer entry : intL) {
 			if(entry > 0) {
 				Material type = Material.getMaterial(entry);
 				if(type != null) {
 					holdKeepData.add(type);
 					if(YADP.debug) log.info( "[yadp][loadConf] keepData: "+type);
 					continue;
 				}
 			}
 			log.warning("[yadp] tools.dupe.keepData: '" + entry +
 					"' is not a Material type" );
 			return false;
 		}
 		YADP.keepData = holdKeepData;
 
 		HashMap<Material, Integer> supported = defDataMap();
 		if(conf.getBoolean("tools.scroll.override",false)) {
 			HashMap<Material, Integer> holdDataMap = new HashMap<Material, Integer>();
 			intL = conf.getIntegerList("tools.scroll.allow");
 
 			if(intL == null) {
 				log.warning("[yadp] tools.scroll.allow is returning null");
 				return false;
 			}
 
 			for(Integer entry : intL) {
 				if(entry > 0) {
 					Material type = Material.getMaterial(entry);
 					if(type != null) {
 						if(supported.containsKey(type)) {
 							holdDataMap.put(type, supported.get(type));
 							if(YADP.debug) log.info( "[yadp][loadConf] scroll allow: "+type);
 						} else {
 							log.warning("[yadp] tools.scroll.allow: '" + entry +
 										"' is not supported for scrolling" );
 							return false;
 						}
 						continue;
 					}
 				}
 				log.warning("[yadp] tools.scroll.allow: '" + entry +
 						"' is not a Material type" );
 				return false;
 			}
 			YADP.dataMap = holdDataMap;
 		} else {
 			YADP.dataMap = supported;
 		}
 		return true;
 	}
 
 	private HashMap<Material, Material> defDupeMap() {
 		HashMap<Material, Material> dm = new HashMap<Material, Material>();
 		//TODO What about Material.GLOWING_REDSTONE_ORE ?
 		//TODO Investigate (Stationary)Water/Lava
 		//Material.STATIONARY_LAVA	Material.STATIONARY_WATER
 		//Material.LAVA				Material.WATER
 		dm.put(Material.BED_BLOCK, Material.BED);
 		dm.put(Material.PISTON_EXTENSION, Material.PISTON_BASE);
 		dm.put(Material.PISTON_MOVING_PIECE, Material.PISTON_BASE);
 		dm.put(Material.CROPS, Material.SEEDS);
 		//Material.DOUBLE_STEP This is fine for someone to have
 		//TODO Can anyone even click on Material.FIRE ?
 		//TODO Do we want to block Material.MOB_SPAWNER ?
 		dm.put(Material.REDSTONE_WIRE, Material.REDSTONE);
 		//TODO Do we want to block Material.SOIL ?
 		dm.put(Material.SIGN_POST, Material.SIGN);
 		dm.put(Material.WOODEN_DOOR, Material.WOOD_DOOR);
 		dm.put(Material.WALL_SIGN, Material.SIGN);
 		dm.put(Material.IRON_DOOR_BLOCK, Material.IRON_DOOR);
 		dm.put(Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON);
 		dm.put(Material.SUGAR_CANE_BLOCK, Material.SUGAR_CANE);
 		//TODO Do we want to block Material.PORTAL ?
 		dm.put(Material.CAKE_BLOCK, Material.CAKE);
 		dm.put(Material.DIODE_BLOCK_OFF, Material.DIODE);
 		dm.put(Material.DIODE_BLOCK_ON, Material.DIODE);
 		dm.put(Material.LOCKED_CHEST, Material.CHEST);
 		//TODO Do we want to block Material.NETHER_WARTS ?
 		dm.put(Material.BREWING_STAND,Material.BREWING_STAND_ITEM);
 		dm.put(Material.CAULDRON,Material.CAULDRON_ITEM);
 		//TODO Do we want to block Material.ENDER_PORTAL ?
 		//TODO Do we want to block Material.ENDER_PORTAL_FRAME ?
 		return dm;
 	}
 
 	private HashMap<Material, Integer> defDataMap() {
 		HashMap<Material, Integer> dm = new HashMap<Material, Integer>();
 		//If the integer is 0, that means that a simple numerical shift won't work
 		dm.put(Material.LOG, TreeSpecies.values().length);
 		dm.put(Material.LEAVES, 4);
 		dm.put(Material.JUKEBOX, 0);			//TODO More research into music
 		dm.put(Material.SAPLING, TreeSpecies.values().length);	//TODO More research into age
 		dm.put(Material.CACTUS, 16);
 		dm.put(Material.SUGAR_CANE_BLOCK, 16);
 		//TODO Add Water and Lava? Likely not, ... What if tool id is a bucket
 		//dm.put(Material.SOIL, 8);				//TODO More research into if should be modified
 		dm.put(Material.CROPS, CropState.values().length);
 		dm.put(Material.NETHER_WARTS, 4);
 		dm.put(Material.PUMPKIN_STEM, 8);
 		dm.put(Material.MELON_STEM, 8);
 		dm.put(Material.WOOL, DyeColor.values().length);
 		//TODO Add Dyes? Likely not
		dm.put(Material.TORCH, 6);
		dm.put(Material.REDSTONE_TORCH_OFF, 6);
		dm.put(Material.REDSTONE_TORCH_ON, 6);
 		dm.put(Material.RAILS, 10);
 		dm.put(Material.POWERED_RAIL, 0);		//TODO More research into powered state
 		dm.put(Material.DETECTOR_RAIL, 0);		//TODO More research into powered state
 		dm.put(Material.WOOD_STAIRS, 4);
 		dm.put(Material.COBBLESTONE_STAIRS, 4);
 		dm.put(Material.BRICK_STAIRS, 4);
 		dm.put(Material.SMOOTH_STAIRS, 4);
 		dm.put(Material.NETHER_BRICK_STAIRS, 4);
 		dm.put(Material.LEVER, 0);				//TODO More research into powered states
 		dm.put(Material.WOODEN_DOOR, 0);		//TODO More research into half-state
 		dm.put(Material.IRON_DOOR_BLOCK, 0);	//TODO More research into half-state
 		dm.put(Material.STONE_BUTTON, 0);		//TODO More research into pressed state
 		dm.put(Material.SIGN_POST, 16);
 		dm.put(Material.LADDER, 0);			//TODO More research into missing 0x0 & 0x1
 		dm.put(Material.WALL_SIGN, 0);			//TODO More research into missing 0x0 & 0x1
 		dm.put(Material.FURNACE, 0);			//TODO More research into missing 0x0 & 0x1
 		dm.put(Material.DISPENSER, 0);			//TODO More research into missing 0x0 & 0x1
 		dm.put(Material.CHEST, 0);				//TODO More research into missing 0x0 & 0x1
 		dm.put(Material.PUMPKIN, 4);
 		dm.put(Material.JACK_O_LANTERN, 4);
 		dm.put(Material.STONE_PLATE, 0);		//TODO More research into powered states
 		dm.put(Material.WOOD_PLATE, 0);		//TODO More research into powered states
 		//TODO Add Coal? Likely not
 		//TODO Tools & Armor? Likely not
 		dm.put(Material.STEP, 7);
 		dm.put(Material.DOUBLE_STEP, 7);
 		dm.put(Material.SNOW, 8);
 		dm.put(Material.CAKE_BLOCK, 6);
 		dm.put(Material.BED_BLOCK, 0);			//TODO More research into foot/head bit
 		dm.put(Material.DIODE_BLOCK_OFF, 0);	//TODO More research into delay/direction
 		dm.put(Material.DIODE_BLOCK_ON, 0);	//TODO More research into delay/direction
 		dm.put(Material.REDSTONE_WIRE, 0);		//TODO Likely not as it is power level
 		dm.put(Material.LONG_GRASS, GrassSpecies.values().length);
 		dm.put(Material.TRAP_DOOR, 0);			//TODO More research into open state
 		dm.put(Material.PISTON_BASE, 0);		//TODO More research into bit 0x8
 		dm.put(Material.PISTON_STICKY_BASE, 0);	//TODO same as above
 		dm.put(Material.PISTON_EXTENSION, 0);	//TODO Uhh, no (ignore)
 		dm.put(Material.SMOOTH_BRICK, 3);
 		dm.put(Material.HUGE_MUSHROOM_1, 11);
 		dm.put(Material.HUGE_MUSHROOM_2, 11);
 		dm.put(Material.VINE, 16);
 		dm.put(Material.FENCE_GATE, 0);		//TODO Look into keeping open state
 		//TODO Potions? Likely not
 		dm.put(Material.MONSTER_EGGS, 3);
 		dm.put(Material.BREWING_STAND, 0);		//TODO Anything that can be done here?
 		dm.put(Material.CAULDRON, 4);
 		dm.put(Material.ENDER_PORTAL_FRAME, 4);
 		dm.put(Material.EGG, 0);				//TODO More research into spawning
 		return dm;
 	}
 }
