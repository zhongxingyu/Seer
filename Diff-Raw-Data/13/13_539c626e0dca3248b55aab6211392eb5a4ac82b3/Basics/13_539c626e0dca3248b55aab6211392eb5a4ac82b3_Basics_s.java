 package coolawesomeme.basics_plugin;
 
 import java.io.File;
 import java.io.FileWriter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import coolawesomeme.basics_plugin.commands.AFactorCommand;
 import coolawesomeme.basics_plugin.commands.BanHammerCommand;
 import coolawesomeme.basics_plugin.commands.BanRouletteCommand;
 import coolawesomeme.basics_plugin.commands.BanTheCoonCommand;
 import coolawesomeme.basics_plugin.commands.BrbCommand;
 import coolawesomeme.basics_plugin.commands.LockdownCommand;
 import coolawesomeme.basics_plugin.commands.ServerHelpCommand;
 import coolawesomeme.basics_plugin.commands.TPRCommand;
 import coolawesomeme.basics_plugin.commands.TPRouletteCommand;
 import coolawesomeme.basics_plugin.commands.TagCommand;
 
 public final class Basics extends JavaPlugin {
 
 	/** The unformatted string that server-owners is loaded into */
 	private static String ownersRaw;
 	
 	/** The current threat level */
 	private static ThreatLevel threatLevel = ThreatLevel.NULL;
 	
 	/** Array of server owner usernames */
 	public static String[] owners;
 	
 	/** The message that is displayed to players when they  */
 	public static String message;
 	
 	public static int versionMajor = 0;
 	public static int versionMinor = 6;
 	public static int versionRevision = 2;
 	public static String version = versionMajor + "." + versionMinor + "." + versionRevision;
 	public static boolean download;
 	public static boolean teleportRequests;
 	public static int tagMinutes;
 	public static boolean isBeta;
 	public static boolean disallowGamemode;
 	
 	@Override
 	/** Method that is executed when the plugin gets enabled */
     public void onEnable(){
 		PluginManager pm = Bukkit.getServer().getPluginManager();
         pm.registerEvents(new EventListener(), this);
 		this.saveDefaultConfig();
 		this.config();
 		this.recopyConfig();
 		this.configReadme();
 		this.createPlayerFolder();
 		this.commandHandlers();
 		AutoDownloader.checkForUpdate(this);
         getLogger().info("Plugin enabled!");
         //getLogger().info("Use /help basics for commands!");
     }
  
     @Override
     /** Method that is executed when the plugin gets disabled */
     public void onDisable() {
         getLogger().info("Plugin disabled!");
     }
 	
     /** Sets classes that handle commands */
     private void commandHandlers(){
     	getCommand("afactor").setExecutor(new AFactorCommand(this));
     	getCommand("banhammer").setExecutor(new BanHammerCommand(this));
     	getCommand("banroulette").setExecutor(new BanRouletteCommand(this));
     	getCommand("banthecoon").setExecutor(new BanTheCoonCommand(this));
     	getCommand("brb").setExecutor(new BrbCommand(this));
     	getCommand("serverhelp").setExecutor(new ServerHelpCommand(this));
     	getCommand("tproulette").setExecutor(new TPRouletteCommand(this));
     	getCommand("tpr").setExecutor(new TPRCommand(this));
     	getCommand("lockdown").setExecutor(new LockdownCommand(this));
     	getCommand("tag").setExecutor(new TagCommand(this));
     }
     
     /** Loads values from the config to memory */
 	private void config(){
     	ownersRaw = this.getConfig().getString("server-owners");
     	if(ownersRaw.length()!= 0){
     		if(ownersRaw.contains(" ")){
     			ownersRaw.replace(" ", "");
     		}if(ownersRaw.contains(",")){
     			owners = ownersRaw.split(",");
     		}else{
     			owners = new String[1];
     			owners[0] = ownersRaw + "";
     		}
     	}else{
     		owners[0] = "";
     	}
     	message = this.getConfig().getString("message");
     	download = this.getConfig().getBoolean("download-latest-version", true);
     	teleportRequests = this.getConfig().getBoolean("teleport-requests", true);
     	tagMinutes = this.getConfig().getInt("tag-playing-time-minutes", 30);
     	isBeta = this.getConfig().getBoolean("is-beta-version", false);
     	disallowGamemode = this.getConfig().getBoolean("disallow-gamemode-change", false);
     }
 	
 	/** Deletes the old config (with its values already loaded to memory), re-saves it and resets the previous values. This guarantees new config items are added */
 	private void recopyConfig(){
 		File config = new File(this.getDataFolder().getAbsolutePath() + "/config.yml");
 		config.delete();
 		this.saveDefaultConfig();
 		this.getConfig().set("server-owners", ownersRaw);
 		this.getConfig().set("message", message);
 		this.getConfig().set("download-latest-version", download);
 		this.getConfig().set("teleport-requests", teleportRequests);
 		this.getConfig().set("tag-playing-time-minutes", tagMinutes);
 		this.getConfig().set("is-beta-version", isBeta);
 		this.getConfig().set("disallow-gamemode-change", disallowGamemode);
 		this.saveConfig();
 	}
 	
 	//Creates (if not already created) the folder where player stats are stored
 	private void createPlayerFolder(){
 		File f = new File(this.getDataFolder().getAbsolutePath() + "/players");
 		f.mkdirs();
 		PlayerDataStorage.setDataFolder(this.getDataFolder().getAbsolutePath() + "/players");
 	}
  
 	private void configReadme(){
 		File f = new File(this.getDataFolder().getAbsolutePath() + "/Config-README.txt");
 		try{
 			String newLine = System.getProperty("line.separator");
 			f.createNewFile();
 			FileWriter lol = new FileWriter(f);
 			lol.write("Config File Readme" + newLine);
 			lol.write("---------------------------" + newLine);
			lol.write("is-beta-version: " + "This controls whether or not you get the latest beta (possibly quite buggy) version of Basics.");
			lol.write("server-owners: " + "This, along with the permission 'basics.brb', control whether or not a user can do /brb . (Note: This can be a list, seperated by commas)");
			lol.write("message: " + "A brief server description that is shown to new players who join while the brb command is active.");
			lol.write("download-latest-version: " + "This controls whether or not you will get the latest version in your /plugins/Basics/updates/ folder");
			lol.write("teleport-requests: " + "This controls whether requests will be sent to players who do /tpr . This is the equivelant of /tpr setRequest");
			lol.write("tag-playing-time-minutes: " + "This controls the amount of time that will pass before tag is over, and the winner(s) are declared.");
 			lol.write("disallow-gamemode-change: " + "Disallows OPs, other than Console, to changing gamemode.");
 			lol.write("");
 			lol.flush();
 			lol.close();
 		}catch(Exception e){
 			getLogger().severe("ConfigReadme Error: " + e.getMessage());
 		}
 	}
 	
 	/** Truth. 
 	 * @return true*/
 	public static boolean isCoolawesomemeAwesome(){
     	return true;
     }
 
 	/** The current threat level of the server
 	 * @return ThreatLevel*/
 	public static ThreatLevel getServerThreatLevel() {
 		return threatLevel;
 	}
 
 	/** Sets the server threat level */
 	public static void setServerThreatLevel(ThreatLevel threatLevel) {
 		Basics.threatLevel = threatLevel;
 	}
 }
