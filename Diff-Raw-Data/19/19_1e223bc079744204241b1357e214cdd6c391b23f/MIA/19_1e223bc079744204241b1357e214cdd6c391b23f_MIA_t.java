 package com.bukkit.Top_Cat.MIA;
 
 import java.io.File;
 import java.sql.DriverManager;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.World.Environment;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * MIA for Bukkit
  *
  * @author Top_Cat
  */
 public class MIA extends JavaPlugin {
     public final MIAPlayerListener playerListener = new MIAPlayerListener(this);
     private final MIAEntityListener entityListener = new MIAEntityListener(this);
     public final MIABlockListener blockListener = new MIABlockListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     MIAFunctions mf = new MIAFunctions(this);
     Connection conn;
     
     public MIA(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
         
         try {
         	Class.forName("com.mysql.jdbc.Driver").newInstance();
         	// Step 2: Establish the connection to the database. 
            conn = DriverManager.getConnection(new sqllogin().url);
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
    
 
     public void onEnable() {
         // TODO: Place any custom enable code here including the registration of any events
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
         
         pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.REDSTONE_CHANGE , blockListener, Priority.Normal, this);
         
         pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_BLOCK, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGEDBY_PROJECTILE, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
         //pm.registerEvent(Event.Type.CREATURE_SPAWN, playerListener, Priority.Normal, this);
         
         if (getServer().getWorlds().size() == 1) {
         	getServer().createWorld("Nether", Environment.NETHER);
         }	
         
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         playerListener.loadallusers();
         for (Player i : getServer().getOnlinePlayers()) {
         	Date time = new Date();
     		playerListener.logintimes.put(i, time.getTime() / 1000);
         }
     }
     public void onDisable() {
         // TODO: Place any custom disable code here
 
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
     	for (Player i : getServer().getOnlinePlayers()) {
     		Date time = new Date();
     		mf.updatestats(i, 2, 4, (int) ((time.getTime() / 1000) - playerListener.logintimes.get(i)));        	
         }
     	
     	mf.updatestats();
         System.out.println("Goodbye world!");
     }
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
     /*@Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
     }*/
 }
 
