 package com.webkonsept.bukkit.repairchest;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class RepairChest extends JavaPlugin {
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	private PermissionHandler Permissions;
 	private boolean usePermissions;
 	private RepairChestPlayerListener playerListener = new RepairChestPlayerListener(this);
 	private RepairChestBlockListener blockListener = new RepairChestBlockListener(this);
 	private HashMap<String,Boolean> fallbackPermissions = new HashMap<String,Boolean>();
 	private File configFile = new File("plugins/RepairChest/settings.yml");
 	public Configuration config = new Configuration(configFile);
  
 	public Integer currency = 266; // Gold Ingot
 	public String currencyName ="g";
 	public double baseCost = 0.01; // 100 damage = 1 this.currency
 	private boolean verbose = false;
 	public boolean partialRepair = false;
 	public boolean distributePartialRepair = true;
 	
 	@Override
 	public void onDisable() {
		if (! configFile.exists()){
			configFile.mkdir();
			config.save();
 		}
 		this.out("Disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		config.load();
 		verbose = config.getBoolean("verbose", false);
 		currency = config.getInt("currency",266);
 		baseCost = config.getDouble("baseCost",0.01);
 		currencyName = config.getString("currencyName","g");
 		partialRepair = config.getBoolean("partialRepair", false);
 		distributePartialRepair = config.getBoolean("distributePartialRepair", true);
 		String currencyString = Material.getMaterial(currency).toString();
 		this.out("Enabled!  currency: "+currencyString+"   baseCost: "+baseCost);
 		this.babble("VERBOSE MODE!  This will get spammy!");
 		if(!setupPermissions()){
 			fallbackPermissions.put("repairchest.create",false);
 			fallbackPermissions.put("repairchest.use",true);
 			fallbackPermissions.put("repairchest.destroy",false);
 			fallbackPermissions.put("repairchest.testing", false);
 		}
 		PluginManager pm =getServer().getPluginManager();
 		pm.registerEvent(Event.Type.SIGN_CHANGE,blockListener,Priority.Normal,this);
 		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal,this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 	}
 	public boolean permit(Player player,String permission){ 
 		
 		boolean allow = false; // Default to GTFO
 		if ( usePermissions ){
 			if (Permissions.has(player,permission)){
 				allow = true;
 			}
 		}
 		else if (player.isOp()){
 			allow = true;
 		}
 		else {
 			if (fallbackPermissions.get(permission) || false){
 				allow = true;
 			}
 		}
 		this.babble(player.getName()+" asked permission to "+permission+": "+allow);
 		return allow;
 	}
 	private boolean setupPermissions() {
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (this.Permissions == null){
 			if (test != null){
 				this.Permissions = ((Permissions)test).getHandler();
 				this.usePermissions = true;
 				return true;
 			}
 			else {
 				this.out("Permissions plugin not found, defaulting to OPS CHECK mode");
 				return false;
 			}
 		}
 		else {
 			this.out("Urr, this is odd...  Permissions are already set up!");
 			return true;
 		}
 	}
 	public void out(String message) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void crap(String message){
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " CRAP] " + message);
 	}
 	public void babble(String message){
 		if (!this.verbose){ return; }
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
 	}
 	public String plural(int number) {
 		if (number == 1){
 			return "";
 		}
 		else {
 			return "s";
 		}
 	}
 
 }
