 package com.lenis0012.bukkit.pvp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.lenis0012.bukkit.pvp.commands.KdrCommand;
 import com.lenis0012.bukkit.pvp.commands.LevelCommand;
 import com.lenis0012.bukkit.pvp.conversion.UUIDConverter;
 import com.lenis0012.bukkit.pvp.conversion.UUIDFetcher;
 import com.lenis0012.bukkit.pvp.hooks.Hook;
 import com.lenis0012.bukkit.pvp.hooks.VaultHook;
 import com.lenis0012.bukkit.pvp.listeners.EntityListener;
 import com.lenis0012.bukkit.pvp.listeners.PlayerListener;
 import com.lenis0012.bukkit.pvp.listeners.ServerListener;
 import com.lenis0012.bukkit.pvp.utils.MathUtil;
 import com.lenis0012.database.Database;
 import com.lenis0012.database.DatabaseConfigBuilder;
 import com.lenis0012.database.DatabaseFactory;
 
 public class PvpLevels extends JavaPlugin {
 	public static final int REWARD_VERSION = 1;
	public static final int CONFIG_VERSION = 2;
 	public static boolean ENABLE_LEVEL_MESSAGES = false;
 	public static boolean ENABLE_KILLSTREAK_MESSAGES = false;
 	public static boolean UNSAFE_CONFIG = false;
 	public static PvpLevels instance;
 	private Database database;
 	private Map<String, Hook> hooks = new HashMap<String, Hook>();
 	private FileConfiguration rewards;
 	private Map<String, PvpPlayer> players = new HashMap<String, PvpPlayer>();
 	
 	public List<Integer> levelList = new ArrayList<Integer>();
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		File configFile = new File(this.getDataFolder(), "config.yml");
 		File rewardsFile = new File(this.getDataFolder(), "rewards.yml");
 		if(!configFile.exists()) {
 			this.getLogger().info("Creating default config.yml");
 			this.getDataFolder().mkdirs();
 			this.copy(this.getResource("config.yml"), configFile);
 			this.reloadConfig();
 		}  else {
 			FileConfiguration config = this.getConfig();
 			if(config.getInt("version") < CONFIG_VERSION) {
 				configFile.renameTo(new File(this.getDataFolder(), "config_backup.yml"));
 				this.copy(this.getResource("README.txt"), new File(this.getDataFolder(), "README.txt"));
 				this.copy(this.getResource("config.yml"), configFile);
 				this.reloadConfig();
 				this.getLogger().log(Level.WARNING, "config.yml has been updated! all options have been reset to fedault.");
 				UNSAFE_CONFIG = true;
 			}
 		}
 		
 		if(!rewardsFile.exists()) {
 			this.getLogger().info("Creating default rewards.yml");
 			this.getDataFolder().mkdirs();
 			this.copy(this.getResource("rewards.yml"), rewardsFile);
 			this.rewards = YamlConfiguration.loadConfiguration(rewardsFile);
 		} else {
 			this.rewards = YamlConfiguration.loadConfiguration(rewardsFile);
 			if(rewards.getInt("version") < REWARD_VERSION) {
 				rewardsFile.renameTo(new File(this.getDataFolder(), "rewards_backup.yml"));
 				this.copy(this.getResource("README.txt"), new File(this.getDataFolder(), "README.txt"));
 				this.copy(this.getResource("rewards.yml"), rewardsFile);
 				this.rewards = YamlConfiguration.loadConfiguration(rewardsFile);
 				this.getLogger().log(Level.WARNING, "rewards.yml has been updated! all options have been reset to fedault.");
 				UNSAFE_CONFIG = true;
 			}
 		}
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		FileConfiguration config = this.getConfig();
 		
 		//Database
 		DatabaseFactory databaseFactory = new DatabaseFactory(this);
 		databaseFactory.registerConverter(new UUIDConverter());
 		this.database = databaseFactory.getDatabase(new DatabaseConfigBuilder(config.getConfigurationSection("MySQL"), new File(getDataFolder(), "database.db")));
 		try { database.connect(); } catch(Exception e) { e.printStackTrace(); }
 		database.registerTable(Tables.ACCOUNTS);
 		
 		//Verify config data
 		ENABLE_LEVEL_MESSAGES = config.getBoolean("settings.messages.new-level", true);
 		ENABLE_KILLSTREAK_MESSAGES = config.getBoolean("settings.messages.killstreak", true);
 		
 		//Register commands & listeners
 		pm.registerEvents(new PlayerListener(this), this);
 		pm.registerEvents(new EntityListener(this), this);
 		pm.registerEvents(new ServerListener(this), this);
 		getCommand("kdr").setExecutor(new KdrCommand());
 		getCommand("level").setExecutor(new LevelCommand(this));
 		
 		//Create hooks
 		Hook vault = new VaultHook("Vault");
 		if(pm.isPluginEnabled("Vault")) vault.onEnable();
 		this.hooks.put("vault", vault);
 		
 		//Generate the level list
 		int a = this.getConfig().getInt("settings.default-kills");
 		double b = this.getConfig().getDouble("settingsincrement-value");
 		int c = 0;
 		int d = a;
 		for(int e = 0; e < this.getConfig().getInt("settings.max-level"); e++) {
 			this.levelList.add(c + d);
 			c += a;
 			d = MathUtil.floor(b * e);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 		for(Player player : Bukkit.getOnlinePlayers()) {
 			//Force unload all players
 			this.unloadPlayer(player);
 		}
 		
 		database.close();
 	}
 	
 	public PvpPlayer getPlayer(Player player) {
 		return players.get(player.getName());
 	}
 	
 	/**
 	 * Load unsafe pvp player.
 	 * This will NOT auto save.
 	 * 
 	 * @param name Player name
 	 * @return Unsafe pvp player
 	 */
 	@Deprecated
 	public PvpPlayer getPlayer(String name) {
 		try {
 			return new PvpPlayer(UUIDFetcher.getUUIDOf(name).toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new PvpPlayer(name);
 		}
 	}
 	
 	public void loadPlayer(Player player) {
 		PvpPlayer pp = new PvpPlayer(player.getUniqueId().toString());
 		players.put(player.getName(), pp);
 	}
 	
 	public void unloadPlayer(Player player) {
 		PvpPlayer pp = players.remove(player.getName());
 		if(pp != null) {
 			pp.save();
 		}
 	}
 	
 	public Database getSQLDatabase() {
 		return database;
 	}
 	
 	public Hook getHook(String name) {
 		name = name.toLowerCase();
 		if(this.hooks.containsKey(name))
 			return this.hooks.get(name);
 		else
 			return null;
 	}
 	
 	public Collection<Hook> getHooks() {
 		 return this.hooks.values();
 	}
 	
 	public Object getReward(String level, String value) {
 		if(level != null) {
 			if(this.rewards.contains("rewards."+level))
 				return this.rewards.get("rewards."+level+"."+value);
 			else
 				return this.rewards.get("rewards."+value);
 		} else {
 			if(this.rewards.contains("rewards."+value))
 				return this.rewards.get("rewards."+value);
 			else
 				return null;
 		}
 	}
 	
 	private void copy(InputStream from, File to) {
 		try {
 			OutputStream out = new FileOutputStream(to);
 			byte[] buffer = new byte[1024];
 			int size = 0;
 			
 			while((size = from.read(buffer)) != -1) {
 				out.write(buffer, 0, size);
 			}
 			
 			out.close();
 			from.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static double getKdr(PvpPlayer pp) {
 		int kills = pp.getKills();
 		int deaths = pp.getDeaths();
 		
 		if(kills == 0 && deaths == 0)
 			return 1;
 		else if(kills > 0 && deaths == 0) {
 			return kills;
 		} else if(deaths > 0 && kills == 0) {
 			return deaths;
 		} else {
 			return MathUtil.round(kills / (double) deaths, 2);
 		}
 	}
 	
 	public String fixColors(String message) {
 		return message.replaceAll("&", "\247");
 	}
 }
