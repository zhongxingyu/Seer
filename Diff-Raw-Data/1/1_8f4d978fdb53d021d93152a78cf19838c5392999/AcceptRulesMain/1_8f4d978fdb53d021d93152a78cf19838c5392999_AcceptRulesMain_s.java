 package me.rigi.acceptrules;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AcceptRulesMain extends JavaPlugin {
 	Logger Log = Logger.getLogger("Minecraft");
 	public static AcceptRulesMain plugin;	
 	public static ArrayList<String> players = new ArrayList<String>();
 	public static ArrayList<String> rules = new ArrayList<String>();
 	public static ArrayList<Player> readed = new ArrayList<Player>();
 	public static String AcceptedMsg,AcceptedAllreadyMsg,InformMsg,MustReadRules,CantBuildMsg,TpWorld,SpawnWorld,RulesCmd;
 	public static boolean TpAfterAccept,AllowBuild,Notify,Inform,AllowMove,TpOnJoin, BlockCmds, RulesMngr;
 	public static Location TpPosition;
 	public static Location SpawnPosition;
 	public static FileConfiguration config;
 	//@Override
 	public void onDisable() {
 		Log.info("[AcceptRules] AcceptRules plugin succesfully disabled!");
 	}
 	//@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		AcceptRulesPreferences acceptrulespreferences = new AcceptRulesPreferences();
 		acceptrulespreferences.createDir();		
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		config = getConfig();
 		AcceptedMsg = config.getString("AcceptedMsg", "You have succesfully accepted the rules! Have fun!");
 		AcceptedAllreadyMsg = config.getString("AcceptedAllreadyMsg", "You have allready accepted the rules!");
 		CantBuildMsg = config.getString("CantBuildMsg", "You must accept rules to build!");
 		MustReadRules = config.getString("MustReadRules", "You must read the rules in order to accept them!");
 		InformMsg = config.getString("InformMsg","You have to accept the rules! Use /rules and then /acceptrules!");
 		RulesCmd = config.getString("RulesCmd","/rules");
 		TpAfterAccept = config.getBoolean("TpAfterAccept", false);
 		TpOnJoin = config.getBoolean("TpOnJoin", false);
 		AllowBuild = config.getBoolean("AllowBuildBeforeAccept", false);
 		AllowMove = config.getBoolean("AllowMoveBeforeAccept", true);
 		Notify = config.getBoolean("NotifyOPs", true);
 		Inform = config.getBoolean("InformUser", true);
 		BlockCmds = config.getBoolean("BlockCommandsBeforeAccept", true);
 		RulesMngr = config.getBoolean("BuiltInRulesManager", true);
 		TpWorld = config.getString("TpWorld", "world");		
 		SpawnWorld = config.getString("SpawnWorld", "world");
 		
 		Double PosX = config.getDouble("TpPositionX", 0);
 		Double PosY = config.getDouble("TpPositionY", 0);
 		Double PosZ = config.getDouble("TpPositionZ", 0);
 		float Pitch =(float) config.getDouble("TpPositionPitch", 0);	    
 		float Yaw = (float) config.getDouble("TpPositionYaw", 0);	 
 		World world =  Bukkit.getServer().getWorld(TpWorld);
 		Location location = new Location(world, PosX, PosY, PosZ, Yaw, Pitch);
 		TpPosition = location;	
 		
 		Double SPosX = config.getDouble("SpawnPositionX", 0);
 		Double SPosY = config.getDouble("SpawnPositionY", 0);
 		Double SPosZ = config.getDouble("SpawnPositionZ", 0);
 		float SPitch = (float) config.getDouble("SpawnPositionPitch", 0);	
 		float SYaw = (float) config.getDouble("SpawnPositionYaw", 0);
 		World Sworld =  Bukkit.getServer().getWorld(SpawnWorld);
 		Location Slocation = new Location(Sworld, SPosX, SPosY, SPosZ, SYaw, SPitch);
 		SpawnPosition = Slocation;	
 		
 		saveConfig();
 		AcceptRulesPreferences.UserReader();
 		AcceptRulesPreferences.RulesReader();
 
 		pm.registerEvents(new AcceptRulesListener(), this);	
 		getCommand("acceptrules").setExecutor(new acceptrulesCmdExecutor(this));
		getCommand("rules").setExecutor(new acceptrulesCmdExecutor(this));
 		Log.info("[AcceptRules] AcceptRules plugin succesfully enabled!");
 	}
 	
 	public void savePosToConfig(String type, String world, double x, double y, double z, double pitch, double yaw) {
 		if(type.equalsIgnoreCase("tp")){
 			config.set("TpWorld", world);
 		    config.set("TpPositionX", x);
 		    config.set("TpPositionY", y);
 		    config.set("TpPositionZ", z);	    
 		    config.set("TpPositionPitch", pitch);	    
 		    config.set("TpPositionYaw", yaw);	    
 		}
 		if(type.equalsIgnoreCase("spawn")){
 			config.set("SpawnWorld", world);
 			config.set("SpawnPositionX", x);
 			config.set("SpawnPositionY", y);
 			config.set("SpawnPositionZ", z);
 			config.set("SpawnPositionPitch", pitch);	
 			config.set("SpawnPositionYaw", yaw);	
 		}
 		saveConfig();
 		reloadConfig();
 		if(type.equalsIgnoreCase("tp")){
 			TpWorld = config.getString("TpWorld", "world");		
 			Double PosX = config.getDouble("TpPositionX", 0);
 			Double PosY = config.getDouble("TpPositionY", 0);
 			Double PosZ = config.getDouble("TpPositionZ", 0);
 			float Pitch =(float) config.getDouble("TpPositionPitch", 0);	    
 			float Yaw = (float) config.getDouble("TpPositionYaw", 0);	    
 			World worldd =  Bukkit.getServer().getWorld(TpWorld);
 			Location location = new Location(worldd, PosX, PosY, PosZ, Yaw, Pitch);
 			TpPosition = location;	
 		}
 		if(type.equalsIgnoreCase("spawn")){
 		    TpWorld = config.getString("SpawnWorld", "world");		
 			Double PosX = config.getDouble("SpawnPositionX", 0);
 			Double PosY = config.getDouble("SpawnPositionY", 0);
 			Double PosZ = config.getDouble("SpawnPositionZ", 0);
 			float Pitch = (float) config.getDouble("SpawnPositionPitch", 0);	
 			float Yaw = (float) config.getDouble("SpawnPositionYaw", 0);
 			config.set("SpawnPositionYaw", yaw);	
 			World worldd =  Bukkit.getServer().getWorld(TpWorld);
 			Location location = new Location(worldd, PosX, PosY, PosZ, Yaw, Pitch);
 			SpawnPosition = location;
 		}
 
 	}
 }
 
