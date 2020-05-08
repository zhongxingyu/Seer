 package de.blablubbabc.dreamworld;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.blablubbabc.dreamworld.listeners.PlayerListener;
 import de.blablubbabc.dreamworld.listeners.WorldListener;
 import de.blablubbabc.dreamworld.managers.CommandManager;
 import de.blablubbabc.dreamworld.managers.ConfigManager;
 import de.blablubbabc.dreamworld.managers.DreamDataStore;
 import de.blablubbabc.dreamworld.managers.DreamManager;
 import de.blablubbabc.dreamworld.messages.Messages;
 
 public class DreamworldPlugin extends JavaPlugin {
 	
 	private static DreamworldPlugin instance;
 	
 	public static DreamworldPlugin getInstance() {
 		return instance;
 	}
 	
 	public String DREAM_PERMISSION = "dreamworld.dream";
 	public String ADMIN_PERMISSION = "dreamworld.admin";
 	
 	private ConfigManager configManager;
 	private DreamManager dreamManager;
 	private DreamDataStore dreamDataStore;
 	
 	public ConfigManager getConfigManager() {
 		return configManager;
 	}
 	
 	public DreamManager getDreamManager() {
 		return dreamManager;
 	}
 	
 	public DreamDataStore getDreamDataStore() {
 		return dreamDataStore;
 	}
 	
 	public World getDreamWorld() {
 		String dreamWorldName = getConfigManager().dreamWorldName;
 		World dreamWorld = getServer().getWorld(dreamWorldName);
 		if (dreamWorld == null) {
 			getLogger().info("World '" + dreamWorldName + "' seems to not be loaded/created. Doing this now..");
 			dreamWorld = getServer().createWorld(new WorldCreator(dreamWorldName));
 		}
 		return dreamWorld;
 	}
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		
 		// load messages
 		Messages.loadMessages("plugins" + File.separator + "Dreamworld" + File.separator + "messages.yml");
 		
 		// init and load config:
 		configManager = new ConfigManager(this);
 		if (!configManager.wasConfigValid()) {
 			getLogger().severe("Something went wrong during the reading of the config file! Please verify that all your values are valid! Disabling now.");
 			Bukkit.getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		// dream data store:
 		dreamDataStore = new DreamDataStore(this);
 		
 		// dream manager:
 		dreamManager = new DreamManager(this);
 		
 		// after server start:
 		getServer().getScheduler().runTaskLater(this, new Runnable() {
 			
 			@Override
 			public void run() {
 				// load/create dreamworld:
 				World world = getDreamWorld();
 				// init world flags:
 				world.setSpawnFlags(!getConfigManager().noMonsterSpawning, !getConfigManager().noAnimalSpawning);
 				
 				// continue dreams:
 				for (Player player : getServer().getOnlinePlayers()) {
					getDreamManager().continueDreaming(player, false);
 				}
 				getDreamDataStore().saveCurrentDreamData();
 				
 			}
 		}, 1L);
 		
 		// world listener:
 		new WorldListener(this);
 		
 		// player listener:
 		new PlayerListener(this);
 		
 		// command manager:
 		new CommandManager(this);
 		
 		getLogger().info("Enabled");
 	}
 	
 	@Override
 	public void onDisable() {
 		if (dreamManager != null) dreamManager.onDisable();
 		instance = null;
 		getLogger().info("Disabled");
 	}
 	
 }
