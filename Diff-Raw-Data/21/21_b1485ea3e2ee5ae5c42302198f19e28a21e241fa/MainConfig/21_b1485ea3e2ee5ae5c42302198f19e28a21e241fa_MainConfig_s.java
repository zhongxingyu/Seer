 package com.minecraftdimensions.bungeesuite.configs;
 
 import java.io.File;
 
 import com.minecraftdimensions.bungeesuite.configlibrary.Config;
 
 public class MainConfig {
 	public static String configpath = File.separator+"plugins"+File.separator+"BungeeSuite"+File.separator+"config.yml";
 	public static Config config = new Config(configpath);
 
 	/* MySQL Information */
 	public static String dbtype = config.getString("Database.Type", "MySQL");
	public static String irl = config.getString("Database.IRL", "localhost");
 	public static String database = config.getString("Database.Database", "minecraft");
 	public static String port = config.getString("Database.Port", "3306");
 	public static String username = config.getString("Database.Username", "username");
 	public static String password = config.getString("Database.Password", "password");
 	public static int threads = config.getInt("Database.Threads", 5);
 	public static boolean motd = config.getBoolean("MOTD.Enabled", true);
 	public static boolean newPlayerBroadcast = config.getBoolean("NewPlayerBroadcast", true);
 	public static int playerDisconnectDelay = config.getInt("PlayerDisconnectDelay", 10);
 	public static int BackupSocketPort = config.getInt("BackupSocketPort", 14455);
 }
