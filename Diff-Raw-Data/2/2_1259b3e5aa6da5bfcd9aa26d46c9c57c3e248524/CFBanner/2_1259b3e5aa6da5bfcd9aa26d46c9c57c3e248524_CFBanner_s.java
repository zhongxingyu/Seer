 package me.cain.cfbanner;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class CFBanner extends JavaPlugin
 {
 	private final CFListener TheListener = new CFListener(this);
 	private final SpoutL SListener = new SpoutL(this);
 	Player player;
 	public static Configuration config;
 	Logger console = Logger.getLogger("Minecraft");
 	String pName = "CFBanner";
 	public static PermissionHandler permissionHandler;
 	Boolean spout = false;
 	String testmaven = "lolw";
 	
	// Maven support!
 
 
 	public void onDisable() 
 	{
 		console.info("[" + pName + "] " + pName + " has been disabled.");
 		console.info("[" + pName + "] " + pName + " was created by CainFoool");
 	}
 
 	public void onEnable() 
 	{
 		config = this.getConfiguration();
 		PluginManager pm = getServer().getPluginManager();
 		console.info("[" + pName + "] " + pName + " has been enabled.");
 		console.info("[" + pName + "] Created by CainFoool");
 		if(config.getBoolean("config.checkforupdates", true)) { VersionCheck(); }
 		setupPermissions();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, TheListener, Priority.Normal, this);
 		pm.registerEvent(Type.CUSTOM_EVENT, SListener, Priority.Low, this);
 		config.load();
 		ConfigurationCheck();
 	}
 	
 	  public static boolean PermissionCheck(String node, Player player) {
 		    if (permissionHandler != null) {
 		      return permissionHandler.has(player, node);
 		    }
 		    return player.hasPermission(node);
 		  }
 	
 	public void ConfigurationCheck() 
 	{
 		if(config.getProperty("config.showmessages") == null)
 		{
 		     config.setProperty("config.showmessages", "true");
 		     config.save();
 		}
 		if(config.getProperty("config.checkforupdates") == null)
 		{
 			config.setProperty("config.checkforupdates", "true");
 			config.save();
 		}
 		if(config.getProperty("config.cfdisabledmessage") == null)
 		{
 			config.setProperty("config.cfdisabledmessage", "Zombe is disabled for you.");
 			config.save();
 		}
 		
 		  String file = this.getDataFolder().toString()+"/config.yml";
 		  File yml = new File(file);
 		  if (!yml.exists()) {
 			  new File(this.getDataFolder().toString()).mkdir();
 			  try {
 				  yml.createNewFile();
 			  } catch (IOException e) {
 				  e.printStackTrace();
 			  	}
 		  	}
 		}
 	 
 	 private void setupPermissions() {
 		    if (permissionHandler != null) {
 		        return;
 		    }
 		    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		    if (permissionsPlugin == null) {
 		        console.info("[" + pName + "] Permissions not detected. Defaulting to OP.");
 		        return;
 		    }
 		    permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 		    console.info("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
 		}
 	 
 	 private void VersionCheck() {
 	        try {
 	            URL url = new URL("http://dl.dropbox.com/u/7186172/cfbanner_version.txt");
 	            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 	            String str;
 	            while ((str = in.readLine()) != null) {
 	                int version = Integer.parseInt(str);
 	                if (version > 170){
 	                    console.info("[CFBanner] A new update is available!");
 	                    console.info("[CFbanner] Download at: http://bit.ly/rj5iJl");
 	                    break;
 	                }
 	            }
 	            in.close();
 	        }
 	        catch (Exception e) { e.printStackTrace(); }
 	 }
 	 
 	 public boolean onCommand(CommandSender sender, Command cmd, String label, String [] args) {
 		 if(label.equals("cfbanner")) {
 			 if(!(sender instanceof Player)) {
 				 sender.sendMessage(ChatColor.DARK_RED + "You can only check settings in-game.");
 			 } else {
 		 if(!CFBanner.PermissionCheck("cf.command", (Player) sender)) {
 			 sender.sendMessage("You do not have permission to use this command!");
 		 } else {
 			 sender.sendMessage(ChatColor.GREEN + "=====Server CFBanner Settings=====");
 			 sender.sendMessage(ChatColor.GREEN + "| Show Messages: " + ChatColor.WHITE + CFBanner.config.getProperty("config.showmessages").toString());
 			 sender.sendMessage(ChatColor.GREEN + "| Version Checking: " + ChatColor.WHITE + CFBanner.config.getProperty("config.checkforupdates").toString());
 			 sender.sendMessage(ChatColor.GREEN + "| CFBanner Disabled Message: " + ChatColor.WHITE + CFBanner.config.getProperty("config.zombedisabledmessage").toString());
 			 sender.sendMessage(ChatColor.GREEN + "===============================");
 		 }
 	}
 		 }
 		return false;
 }
 	 
 }
