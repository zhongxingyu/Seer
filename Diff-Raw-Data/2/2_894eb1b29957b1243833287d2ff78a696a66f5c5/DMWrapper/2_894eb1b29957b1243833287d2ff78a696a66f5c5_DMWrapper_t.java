 /**
  * 
  */
 package me.slaps.DMWrapper;
 
 import java.util.logging.Logger;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 /**
  * @author magik
  *
  */
 public class DMWrapper extends JavaPlugin {
 
 	public static String name; // = "DMWrapper";
 	public static String codename = "Botswana";
 	public static String version; // = "0.03";
 	
 	public static Logger logger = Logger.getLogger("Minecraft");
 	public static PluginDescriptionFile desc;
 	
 	public static Permissions perms;
 	public static DynamicMarket dm;
 	
 	public static DMWrapperBlockListener blockListener;
 	public static DMWrapperPlayerListener playerListener;
 	public static DMWrapperPluginListener pluginListener;
 	
 	protected LocationManager locMgr;
 	protected HashMap<String, String> cmdMap = new HashMap<String, String>();
 	protected HashMap<String, ShopLocation> tmpShop = new HashMap<String, ShopLocation>();
 
 	
 
     public static void info(String msg) {
     	logger.info("["+name+"] "+ msg);
     }
     public static void warning(String msg) {
     	logger.warning("["+name+"] "+ msg);
     }
 
 	@Override
 	public void onDisable() {
 		info("Version ["+version+"] ("+codename+") disabled");
 	}
 
 	@Override
     public void onEnable() {
 		
 		desc = getDescription();
 		name = desc.getName();
 		version = desc.getVersion();
 
 		getDataFolder().mkdir();
 		//directory = getDataFolder() + File.separator;
 
 		// setup listeners
 		blockListener =  new DMWrapperBlockListener(this);
 		playerListener = new DMWrapperPlayerListener(this);
 	  	pluginListener = new DMWrapperPluginListener(this);
 	  	
 	  	// try to check for if external plugins already enabled
 	  	pluginListener.tryEnablePlugins();
 
 	  	// setup location manager
 		locMgr = new LocationManager(this);
 
 		// clear the command histories
 		cmdMap.clear();
 		
 		info("Version ["+version+"] ("+codename+") enabled");
     }
     
 	
 	private boolean hasPermission(CommandSender sender, String permString)
 	{
 		if (sender instanceof Player)
 			return Permissions.Security.permission((Player)sender, name.toLowerCase()+"."+permString);
 		return true;
 	}	
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// location-based features only available to Players
 		if ( !(sender instanceof Player) ) return dm.wrapperCommand(sender, cmd.getName(), args);
 
 		// only intercept shop command
		if (cmd.getName().toLowerCase().equals("dshop")) {	
 			
 			// pass commands to DynamicMarket or intercept
 			
 			// just '/shop'
 			if ( (args.length == 0) || ( args[0].equalsIgnoreCase("help") ) ){
 				dm.wrapperCommand(sender, cmd.getName(), args);
 				sender.sendMessage("/shop location");
 				return true;
 				
 			// is location based shopping enabled?
 			} else if ( !locMgr.shopLocationsEnabled ) {
 				return dm.wrapperCommand(sender, cmd.getName(), args);
 				
 			// locations enabled, intercept commands
 			} else {
 				
 				// not a '/shop location' command
 				if ( !args[0].equalsIgnoreCase("location") ) {
 					
 					// in a shop location? or admin?
 					if ( hasPermission(sender, "admin") || locMgr.inShopLoc(((Player)sender).getLocation()) ) {
 						return dm.wrapperCommand(sender, cmd.getName(), args);
 					} else {
 						sender.sendMessage("Not in the shopping area!");
 						return true;
 					}
 					
 				// a '/shop location' command
 				} else {
 					if (!hasPermission(sender, "location"))	{
 						return dm.wrapperCommand(sender, cmd.getName(), args);
 						//return false;
 					}
 
 					String pname = ((Player)sender).getName();			
 					
 					if ( args.length == 2 && args[1].equalsIgnoreCase("set") ) {
 						sender.sendMessage("please right click the 1st corner");
 						cmdMap.put(pname, "location set");
 						return true;
 					} else if ( args.length == 2 && args[1].equalsIgnoreCase("cancel") ) {
 						if ( cmdMap.get(pname) == null ) {
 							sender.sendMessage("No operation to cancel");
 						} else {
 							sender.sendMessage("Operation canceled");
 							cmdMap.remove(pname);
 						}
 						return true;
 					} else if ( args.length == 2 && args[1].equalsIgnoreCase("check") ) {
 						Integer id = locMgr.getShopID(((Player)sender).getLocation());
 						if ( id > 0 ) {
 							sender.sendMessage("Shop ID: "+id);
 						} else {
 							sender.sendMessage("No shop location found here");
 						}
 						return true;
 					} else if ( args.length == 3 && args[1].equalsIgnoreCase("remove") ) {
 						if ( locMgr.removeShopByID(Integer.parseInt(args[2])) ) { 
 							sender.sendMessage("Shop Location removed");	
 						} else {
 							sender.sendMessage("Could not remove Shop ID: "+Integer.parseInt(args[2]));
 						}
 						return true;
 					} else if ( args.length == 2 && args[1].equalsIgnoreCase("enable") ) {
 						sender.sendMessage("Shop Locations enabled");
 						locMgr.enableShopLocations();
 						return true;
 					} else if ( args.length == 2 && args[1].equalsIgnoreCase("disable") ) {
 						sender.sendMessage("Shop Locations disabled");
 						locMgr.disableShopLocations();
 						return true;
 					} else if ( args.length == 2 && args[1].equalsIgnoreCase("list") ) {
 						sender.sendMessage("Shop IDs: "+locMgr.listShops());
 						return true;
 					} else if ( args.length == 3 && args[1].equalsIgnoreCase("tp") ) {
 						if ( sender instanceof Player ) {
 							Location dest = locMgr.getCenterOfShop(Integer.parseInt(args[2]));
 							if ( dest == null ) {
 								sender.sendMessage("Could not find shop.");
 							} else {
 								((Player)sender).teleportTo(dest);
 							}
 						}
 						return true;
 					} else {
 						// send player message on how to use /shop loc
 						sender.sendMessage("Usage: /shop location set - starts the shop setup process");
 						sender.sendMessage("         /shop location cancel - cancels setting a shop location");
 						sender.sendMessage("         /shop location check - checks ID of current location");
 						sender.sendMessage("         /shop location remove <ID> - removes the shop location");
 						sender.sendMessage("         /shop location enable - enables location based shops");
 						sender.sendMessage("         /shop location disable - disable location based shops");
 						sender.sendMessage("         /shop location list - lists shop IDs");
 						sender.sendMessage("         /shop location tp <ID> - teleports to shop");
 						return true; 
 					}					
 					
 				}
 			}
 			
 		} else {
 			return false;
 		}
 			
 			
 	}
 	
 	
 	
 }
