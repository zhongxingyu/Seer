 package com.ne0nx3r0.quantum;
 
 import com.ne0nx3r0.quantum.circuits.CircuitManager;
 import com.ne0nx3r0.quantum.circuits.CircuitTypes;
 import com.ne0nx3r0.quantum.listeners.QuantumConnectorsBlockListener;
 import com.ne0nx3r0.quantum.listeners.QuantumConnectorsPlayerListener;
 import com.ne0nx3r0.quantum.listeners.QuantumConnectorsWorldListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class QuantumConnectors extends JavaPlugin{    
 
 //Register events
     private final QuantumConnectorsPlayerListener playerListener = new QuantumConnectorsPlayerListener(this);
     private final QuantumConnectorsBlockListener blockListener = new QuantumConnectorsBlockListener(this);
     private final QuantumConnectorsWorldListener worldListener = new QuantumConnectorsWorldListener(this);
 
 //Circuit Manager
     public static CircuitManager circuitManager;
     
 //Configurables
     public static int MAX_CHAIN_LINKS = 3;
    public static int MAX_DELAY_TIME = 20 * 20;//20s converted to ticks... I think?
 
     private static int AUTOSAVE_INTERVAL = 30;//specified here in minutes
     private static int AUTO_SAVE_ID = -1;
     
     @Override
     public void onDisable(){
         circuitManager.saveAllWorlds();
         
         log("Disabled");
     }
     
     @Override
     public void onEnable(){
     //This might be outdated...
         getDataFolder().mkdirs();
 
     //Create a circuit manager
         circuitManager = new CircuitManager(this);
         
     //Register qc command
         getCommand("qc").setExecutor(new QuantumConnectorsCommandExecutor(this));   
         
     //Register listeners
         PluginManager pm = getServer().getPluginManager();
         
         pm.registerEvents(playerListener, this);
         pm.registerEvents(blockListener, this);
         pm.registerEvents(worldListener, this);
         
     //Schedule saves
         AUTOSAVE_INTERVAL = AUTOSAVE_INTERVAL * 60 * 20;//convert to ~minutes
         
         AUTO_SAVE_ID = getServer().getScheduler().scheduleSyncRepeatingTask(
             this,
             autosaveCircuits,
             AUTOSAVE_INTERVAL,
             AUTOSAVE_INTERVAL);
         
     //All done!
         log("Enabled");
     }	
     
     public void msg(Player player, String sMessage) {
         player.sendMessage(ChatColor.LIGHT_PURPLE + "[QC] " + ChatColor.WHITE + sMessage);
     }
 
 //Generic wrappers for console messages
     public void log(Level level,String sMessage){
         if(!sMessage.equals(""))
             getLogger().log(level,sMessage);
     }
     public void log(String sMessage){
         log(Level.INFO,sMessage);
     }
     public void error(String sMessage){
         log(Level.WARNING,sMessage);
     }
     
     //Scheduled save mechanism
     private Runnable autosaveCircuits = new Runnable() {
         @Override
         public void run() {
             circuitManager.saveAllWorlds();
         }
     };
 }
