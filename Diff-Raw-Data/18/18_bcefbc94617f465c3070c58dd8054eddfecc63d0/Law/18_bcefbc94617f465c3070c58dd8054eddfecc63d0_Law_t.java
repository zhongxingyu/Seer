 package net.marcuswhybrow.minecraft.law;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 public class Law {
 	public static final String PLUGIN_NAME = "Law";
 	public static final String ON_ENABLE_MESSAGE = PLUGIN_NAME + " enabled.";
 	public static final String ON_DISABLE_MESSAGE = PLUGIN_NAME + " disabled.";
 	public static final String MESSAGE_PREFIX = "[" + PLUGIN_NAME + "] ";
 	
 	private final Logger log = Logger.getLogger("Minecraft");
 	private Plugin plugin = null;
 	private static Law self = null;
 	
 	private HashMap<String, LawWorld> worlds;
 	
 	private Law() {
 		worlds = new HashMap<String, LawWorld>();
 	}
 	
 	public static Law get() {
 		if (self == null) {
 			self = new Law();
 		}
 		return self;
 	}
 	
 	public void logMessage(String message) {
 		log.info(PLUGIN_NAME + ": " + message);
 	}
 	
 	public void sendMessage(CommandSender sender, String message) {
 		sender.sendMessage(ChatColor.GRAY + MESSAGE_PREFIX + ChatColor.WHITE + message);
 	}
 	
 	public void setPlugin(Plugin plugin) {
 		this.plugin = plugin;
 	}
 	
 	public Plugin getPlugin() {
 		return this.plugin;
 	}
 	
 	public void readConfig() {
 		FileConfiguration config = this.plugin.getConfig();
 		
 		// Create LawWorld instances
 		ConfigurationSection worlds = config.getConfigurationSection("worlds");
 		
 		if (worlds != null) {
 			World world;
 			LawWorld lawWorld;
 			ConfigurationSection prisons;
 			ConfigurationSection activePrisons;
 			
 			for (String worldName : worlds.getKeys(false)) {
 				world = plugin.getServer().getWorld(worldName);
 				lawWorld = new LawWorld(world);
 				this.putLawWorld(lawWorld);
 				
 				// Create Prison instances
 				prisons = config.getConfigurationSection("worlds." + lawWorld.getName() + ".prisons");
 				
 				Prison prison;
 				ConfigurationSection cells;
 				double x, y, z;
 				Location cellLocation;
 				
 				Double defaultCellX, defaultCellY, defaultCellZ, defaultCellPitch, defaultCellYaw;
 				
 				if (prisons != null) {
 					for (String prisonName : prisons.getKeys(false)) {
 						prison = new Prison(lawWorld, prisonName, false);
 						lawWorld.addPrison(prison);
 						
 						// Retrieve the default cell for this prison
 						defaultCellX = config.getDouble("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".default_cell.location.x");
 						defaultCellY = config.getDouble("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".default_cell.location.y");
 						defaultCellZ = config.getDouble("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".default_cell.location.z");
 						defaultCellPitch = config.getDouble("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".default_cell.location.pitch");
 						defaultCellYaw = config.getDouble("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".default_cell.location.yaw");
 						
						if (defaultCellX != 0 || defaultCellY != 0 || defaultCellZ != 0) {
 							prison.setDefaultCell(new Location(world, defaultCellX, defaultCellY, defaultCellZ, new Float(defaultCellYaw), new Float(defaultCellPitch)), false);
 						}
 						
 						// Create the Prison cells
 						cells = config.getConfigurationSection("worlds." + lawWorld.getName() + ".prisons." + prison.getName() + ".cells");
 						if (cells != null) {
 							for (String cellName : cells.getKeys(false)) {
 								String prefix = "prisons." + prisonName + ".cells." + cellName + ".location.";
 								x = config.getDouble(prefix + "x");
 								y = config.getDouble(prefix + "x");
 								z = config.getDouble(prefix + "x");
 								cellLocation = new Location(world, x, y, z);
 								
 								prison.addCell(cellName, cellLocation, false);
 							}
 						}
 					}
 				}
 				
 				// Set active prisons
 				activePrisons = config.getConfigurationSection("worlds." + lawWorld.getName() + ".active_prisons");
 				
 				if (activePrisons != null) {
 					for (String playerDisplayName : activePrisons.getKeys(false)) {
 						String prisonName = config.getString("worlds." + lawWorld.getName() + ".active_prisons." + playerDisplayName);
 						Player player = plugin.getServer().getPlayerExact(playerDisplayName);
 						
 						logMessage(player.getDisplayName());
 						logMessage(prisonName);
 						
 						lawWorld.setSelectedPrison(player, prisonName, false);
 					}
 				}
 			}
 		}
 	}
 	
 	public LawWorld getLawWorld(String name) {
 		return worlds.get(name);
 	}
 	
 	public LawWorld getOrCreateLawWorld(World world) {
 		LawWorld lawWorld = worlds.get(world.getName());
 		if (lawWorld == null) {
 			lawWorld = new LawWorld(world);
 			this.putLawWorld(lawWorld);
 		}
 		return lawWorld;
 	}
 	
 	public void putLawWorld(LawWorld lawWorld) {
 		worlds.put(lawWorld.getName(), lawWorld);
 	}
 	
 	public void putWorld(World world) {
 		this.putLawWorld(new LawWorld(world));
 	}
 }
