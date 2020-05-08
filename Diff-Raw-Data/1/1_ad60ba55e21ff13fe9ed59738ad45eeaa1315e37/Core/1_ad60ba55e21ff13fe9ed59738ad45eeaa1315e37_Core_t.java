 package com.d4l3k.Link;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 public class Core extends JavaPlugin implements Runnable{
 	static Logger log = Logger.getLogger("Minecraft");
 	public static Server server;
 	public static PermissionHandler permissionHandler;
 	
 	public static void debug(String msg)
 	{
 		log.info("[LINK] "+msg);
 	}
 	private void setupPermissions() {
 	    if (permissionHandler != null) {
 	        return;
 	    }
 	    
 	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 	    
 	    if (permissionsPlugin == null) {
 	        debug("Permission system not detected, defaulting to OP");
 	        return;
 	    }
 	    
 	    permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	    debug("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
 	}
 	public void onEnable(){
 		server = this.getServer();
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.REDSTONE_CHANGE, redstoneListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, breakListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, signListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, interactListener, Event.Priority.Normal, this);
 		setupPermissions();
 		Data.loadGates();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 1L, 10);
 		debug("by D4l3k! is enabled!");
 	}
 	public void onDisable(){
 		Data.saveGates();
 		debug("has been disabled. ;(");
 	}
 	private final WorldListener signListener = new WorldListener(this);
 	private final WorldListener breakListener = new WorldListener(this);
 	private final WorldListener redstoneListener = new WorldListener(this);
 	private final InteractListener interactListener = new InteractListener(this);
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(cmd.getName().equalsIgnoreCase("link"))
 		{
 			if(args.length>0)
 			{
 				if(args[0].equalsIgnoreCase("edit"))
 				{
 					if (!Core.permissionHandler.has((Player)sender, "link.edit")) {
 						sender.sendMessage("[LINK] "+ChatColor.RED+"Insufficient Permissions to enable Edit!");
 						return false;
 					}
 					Data.playerEditMode.put((Player)sender, true);
 					Data.playerEditStatus.put((Player)sender, 0);
 					sender.sendMessage("[LINK] "+ChatColor.GREEN+"Edit Enabled");
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 			if(Data.playerEditMode.containsKey((Player)sender))
 			{
 				if(Data.playerEditMode.get((Player)sender))
 				{
 					Data.playerEditMode.put((Player)sender, false);
 					sender.sendMessage("[LINK] "+ChatColor.RED+"Edit Disabled");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public void run() {
 		Data.runSelfTriggered();
 	}
 }
