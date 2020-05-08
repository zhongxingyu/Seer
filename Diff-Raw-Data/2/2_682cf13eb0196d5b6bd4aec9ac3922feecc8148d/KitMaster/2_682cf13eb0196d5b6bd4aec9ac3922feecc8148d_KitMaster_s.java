 package net.amoebaman.kitmaster;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginLogger;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 
 import net.amoebaman.kitmaster.enums.Attribute;
 import net.amoebaman.kitmaster.enums.ClearKitsContext;
 import net.amoebaman.kitmaster.handlers.*;
 import net.amoebaman.kitmaster.objects.Kit;
 import net.amoebaman.kitmaster.sql.SQLHandler;
 import net.amoebaman.kitmaster.sql.SQLQueries;
 import net.amoebaman.utils.GenUtil;
 import net.amoebaman.utils.plugin.MetricsLite;
 import net.amoebaman.utils.plugin.Updater;
 import net.amoebaman.utils.plugin.Updater.UpdateType;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 //TODO Javadoc for EVERYTHING
 
 public class KitMaster extends JavaPlugin implements Listener{
 	
 	private static int TASK_ID;
 	
 	private static SQLHandler SQL;
 	public static final String MAIN_DIR = "plugins/KitMaster";
 	public static final String KITS_DIR = MAIN_DIR + "/kits";
 	public static final String DATA_DIR = MAIN_DIR + "/data";
 	private static File CONFIG_FILE, KITS_FILE, CUSTOM_DATA_FILE,
 	    MESSAGES_FILE;
 	private static File SIGNS_FILE, TIMESTAMPS_FILE, HISTORY_FILE;
 	
 	private static boolean VAULT_ENABLED;
 	private static Permission PERMISSIONS;
 	private static Economy ECONOMY;
 	
 	private static boolean UPDATE_ENABLED;
 	private static Updater UPDATE;
 	
 	private static MetricsLite METRICS;
 	
 	public final static boolean DEBUG_PERMS = false;
 	public final static boolean DEBUG_KITS = false;
 	
 	@Override
 	public void onEnable(){
 		
 		new File(MAIN_DIR).mkdirs();
 		new File(KITS_DIR).mkdirs();
 		new File(DATA_DIR).mkdirs();
 		
 		CONFIG_FILE = GenUtil.getConfigFile(this, "config");
 		CUSTOM_DATA_FILE = GenUtil.getConfigFile(this, "custom-data");
 		KITS_FILE = GenUtil.getConfigFile(this, "kits");
 		MESSAGES_FILE = GenUtil.getConfigFile(this, "messages");
 		SIGNS_FILE = GenUtil.getConfigFile(this, "data/signs");
 		TIMESTAMPS_FILE = GenUtil.getConfigFile(this, "data/timestamps");
 		HISTORY_FILE = GenUtil.getConfigFile(this, "data/history");
 		
 		try{
 			reloadKits();
 			
 			if(getConfig().getBoolean("mysql.use-mysql"))
 				SQL = new SQLHandler(getConfig().getString("mysql.url", "localhost"), getConfig().getString("mysql.username", "root"), getConfig().getString("mysql.password", "raglfragl"));
 			
 			if(isSQLRunning())
 				logger().info("Will retreive data from MySQL server as needed");
 			else{
 				logger().info("Loading data from flat files...");
 				SignHandler.load(SIGNS_FILE);
 				logger().info("Loaded kit selection sign locations from " + SIGNS_FILE.getPath());
 				TimeStampHandler.load(TIMESTAMPS_FILE);
 				logger().info("Loaded player kit timestamps from " + TIMESTAMPS_FILE.getPath());
 				HistoryHandler.load(HISTORY_FILE);
 				logger().info("Loaded player kit history from " + HISTORY_FILE.getPath());
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		runUpdater();
 		startMetrics();
 		hookVault();
 		logger().info("Purged " + SignHandler.repairSigns() + " absent kit signs");
 		
 		KitMasterEventHandler.init(this);
 		KitMasterCommandHandler.init(this);
 		TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new InfiniteEffects(), 15, 15);
 	}
 	
 	@Override
 	public void onDisable(){
 		Bukkit.getScheduler().cancelTask(TASK_ID);
 		if(getConfig().getBoolean("clearKits.onDisable", true))
 			for(OfflinePlayer player : HistoryHandler.getPlayers())
 				if(player instanceof Player)
 					Actions.clearAll((Player) player, true, ClearKitsContext.PLUGIN_DISABLE);
 		if(!isSQLRunning()){
 			try{
 				SignHandler.save(SIGNS_FILE);
 				TimeStampHandler.save(TIMESTAMPS_FILE);
 				HistoryHandler.save(HISTORY_FILE);
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void reloadKits(){
 		
 		try{
 			plugin().getConfig().load(CONFIG_FILE);
 			plugin().getConfig().options().copyDefaults(true);
 			plugin().getConfig().save(CONFIG_FILE);
 			logger().info("Loaded configuration from " + CONFIG_FILE.getPath());
 		}
 		catch(Exception e){
 			logger().severe("Error while loading configuration from " + CONFIG_FILE.getPath());
 			e.printStackTrace();
 		}
 		
 		try{
 			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(CUSTOM_DATA_FILE);
 			
 			BookHandler.yaml = yaml.isConfigurationSection("books") ? yaml.getConfigurationSection("books") : yaml.createSection("books");
 			CustomItemHandler.yaml = yaml.isConfigurationSection("items") ? yaml.getConfigurationSection("items") : yaml.createSection("items");
 			CustomPotionHandler.yaml = yaml.isConfigurationSection("potions") ? yaml.getConfigurationSection("potions") : yaml.createSection("potions");
 			FireworkEffectHandler.yaml = yaml.isConfigurationSection("bursts") ? yaml.getConfigurationSection("bursts") : yaml.createSection("bursts");
 			FireworkHandler.yaml = yaml.isConfigurationSection("fireworks") ? yaml.getConfigurationSection("fireworks") : yaml.createSection("fireworks");
 			
 			logger().info("Loaded custom data from " + CUSTOM_DATA_FILE.getPath());
 		}
 		catch(Exception e){
 			logger().severe("Error while loading custom data from " + CUSTOM_DATA_FILE.getPath());
 			e.printStackTrace();
 		}
 		
 		try{
 			KitHandler.loadKits(KITS_FILE);
 			logger().info("Loaded all kit files from " + KITS_FILE.getPath());
 			
 			KitHandler.loadKits(new File(KITS_DIR));
 			logger().info("Loaded all kit files from " + KITS_DIR);
 			
 			if(isSQLRunning())
 				for(Kit kit : KitHandler.getKits())
 					getSQL().executeCommand(SQLQueries.ADD_KIT_TO_TIMESTAMP_TABLE.replace(SQLQueries.KIT_MACRO, kit.name));
 		}
 		catch(Exception e){
 			logger().severe("Error while loading kits");
 			e.printStackTrace();
 		}
 		
 		try{
 			MessageHandler.yaml = YamlConfiguration.loadConfiguration(MESSAGES_FILE);
 		}
 		catch(Exception e){
 			logger().severe("Error while loading messages");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void saveCustomData(){
 		YamlConfiguration yaml = new YamlConfiguration();
 		yaml.createSection("books", BookHandler.yaml.getValues(true));
 		yaml.createSection("items", CustomItemHandler.yaml.getValues(true));
 		yaml.createSection("potions", CustomPotionHandler.yaml.getValues(true));
 		yaml.createSection("bursts", FireworkEffectHandler.yaml.getValues(true));
 		yaml.createSection("fireworks", FireworkHandler.yaml.getValues(true));
 		try{
 			yaml.save(CUSTOM_DATA_FILE);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static Plugin plugin(){
 		return Bukkit.getPluginManager().getPlugin("KitMaster");
 	}
 	
 	public static FileConfiguration config(){
 		return plugin().getConfig();
 	}
 	
 	public static PluginLogger logger(){
 		return new PluginLogger(plugin());
 	}
 	
 	public static boolean isSQLRunning(){
 		return SQL != null && SQL.isConnected();
 	}
 	
 	public static SQLHandler getSQL(){
 		return SQL;
 	}
 	
 	private void runUpdater(){
 		UPDATE_ENABLED = getConfig().getBoolean("update.checkForUpdate");
 		if(UPDATE_ENABLED){
 			try{
 				getLogger().info("Checking for updates...");
 				UpdateType type = getConfig().getBoolean("update.autoInstallUpdate") ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD;
 				UPDATE = new Updater(this, 48658, this.getFile(), type, true);
 				switch(UPDATE.getResult()){
 					case FAIL_BADID:
 					case FAIL_NOVERSION:
 						getLogger().severe("Failed to check for updates due to bad code.  Contact the developer: " + UPDATE.getResult().name());
 						break;
 					case FAIL_DBO:
 						getLogger().severe("An error occurred while checking for updates.");
 						break;
 					case FAIL_DOWNLOAD:
 						getLogger().severe("An error occurred downloading the update.");
 						break;
 					case UPDATE_AVAILABLE:
 						getLogger().warning("An update is available for download on BukkitDev.");
 						break;
 					default:
 				}
 			}
 			catch(Exception e){
 				getLogger().severe("Error occurred while trying to update");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static boolean isUpdateEnabled(){
 		return UPDATE_ENABLED;
 	}
 	
 	public static Updater getUpdate(){
 		return UPDATE;
 	}
 	
 	private void startMetrics(){
 		try{
 			METRICS = new MetricsLite(this);
 			METRICS.start();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void hookVault(){
 		VAULT_ENABLED = Bukkit.getPluginManager().isPluginEnabled("Vault");
 		if(VAULT_ENABLED){
 			RegisteredServiceProvider<Permission> tempPerm = Bukkit.getServicesManager().getRegistration(Permission.class);
 			if(tempPerm != null){
 				PERMISSIONS = tempPerm.getProvider();
 				if(PERMISSIONS != null)
 					logger().info("Hooked into Permissions manager: " + PERMISSIONS.getName());
 			}
 			RegisteredServiceProvider<Economy> tempEcon = Bukkit.getServicesManager().getRegistration(Economy.class);
 			if(tempEcon != null){
 				ECONOMY = tempEcon.getProvider();
 				if(ECONOMY != null)
 					logger().info("Hooked into Economy manager: " + ECONOMY.getName());
 			}
 		}
 	}
 	
 	public static boolean isVaultEnabled(){
 		return VAULT_ENABLED;
 	}
 	
 	public static Permission getPerms(){
 		return PERMISSIONS;
 	}
 	
 	public static Economy getEcon(){
 		return ECONOMY;
 	}
 	
 	private static class InfiniteEffects implements Runnable{
 		
 		public void run(){
 			for(World world : Bukkit.getWorlds())
 				for(Player player : world.getPlayers())
 					for(Kit kit : HistoryHandler.getHistory(player))
 						if(kit != null && kit.booleanAttribute(Attribute.INFINITE_EFFECTS))
 							for(PotionEffect effect : kit.effects)
 								for(PotionEffect actual : player.getActivePotionEffects())
									if(actual.getAmplifier() < effect.getAmplifier())
 										player.addPotionEffect(effect, true);
 		}
 		
 	}
 	
 }
