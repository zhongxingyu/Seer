 package com.gmail.scyntrus.dotaminecraft;
 
 import java.awt.Rectangle;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.gmail.scyntrus.dotaminecraft.metrics.MetricsLite;
 import com.onarandombox.MultiverseCore.MultiverseCore;
 
 public class DotaMinecraft extends JavaPlugin {
 	public World world;
 	public final Map<String, Boolean> playerhasjoined = new HashMap<String, Boolean>();
 	public final Map<String,Integer> playerlist = new HashMap<String,Integer>();
 	public final Map<String,Integer> playerkills = new HashMap<String,Integer>();
 	public final Map<String,Integer> playercs = new HashMap<String,Integer>();
 	public final Map<String,Integer> playerdeaths = new HashMap<String,Integer>();
 	public final Map<String,Integer> playerRecallID = new HashMap<String,Integer>();
 	public final Map<String,ItemStack[]> playerdeathitems = new HashMap<String,ItemStack[]>();
 	public final Map<String,ItemStack[]> playerdeatharmor = new HashMap<String,ItemStack[]>();
 	public final Map<Location, String> turretlocations = new HashMap<Location, String>();
 	public final Map<String, Boolean> turretstates = new HashMap<String, Boolean>();
 	public int RedCount = 0;
 	public int BlueCount = 0;
 	public final Rectangle WorldSpawn = new Rectangle(-1278,100,40,41);
 	public final Rectangle RedSpawn = new Rectangle(-1197,438,11,22);
 	public final Rectangle BlueSpawn = new Rectangle(-948,176,11,22);
 	public Location RedPoint = null;
 	public Location BluePoint = null;
 	public Location RedBed = null;
 	public Location BlueBed = null;
 	public boolean GameInProgress = true;
 	public Location FarAwayLocation = null;
 	public boolean PlayersKeepItems = true;
 	public boolean Enabled = false;
 	public boolean PlayersKeepLevel = true;
 	public boolean colorNameTag = true;
 	public String WorldName = "Minecraft_dota";
 	public MultiverseCore MVCorePlugin = null;
 	public PluginManager pm = null;
 	public boolean RecallEnabled = false;
 	public int RecallDelay = 6;
 	
 	public boolean removeMobArmor = false;
 	public boolean giveMobsHelmet = false;
 	
 	public boolean goodVersion = false;
 	
 	//Red is 1
 	//Blue is 2
 	
     public void onEnable() {
 		this.saveDefaultConfig();
     	FileConfiguration config = this.getConfig();
     	config.options().copyDefaults(true);
     	this.saveConfig();
     	this.Enabled = config.getBoolean("Enabled");
     	this.WorldName = config.getString("WorldName");
     	this.PlayersKeepItems = config.getBoolean("PlayersKeepItems");
     	this.PlayersKeepLevel = config.getBoolean("PlayersKeepLevel");
     	this.removeMobArmor = config.getBoolean("removeMobArmor");
     	this.giveMobsHelmet = config.getBoolean("giveMobsHelmet");
     	this.RecallEnabled = config.getBoolean("RecallEnabled");
     	this.RecallDelay = config.getInt("RecallDelay");
     	this.colorNameTag = config.getBoolean("colorNameTag");
     	
 		this.pm = this.getServer().getPluginManager();
     	
     	if (this.Enabled) {
     		if (this.pm.isPluginEnabled("Multiverse-Core")) {
     			PluginListener.LoadPlugin(this);
     		} else { 
         		this.pm.registerEvents(new PluginListener(this), this);
     		}
     	} else {
     		System.out.println("Dota Minecraft must have Enabled set to true in the config.yml file!");
     	}
 
 		try { // using mcstats.org metrics
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 		} catch (IOException e) {
 			System.out.println("[Metrics] " + e.getMessage());
 		}
     }
     
     public void onDisable() {
     	for (int a = -55; a <= -79; a++) {
         	for (int b = 7; b <= 31; b++) {
         		this.world.unloadChunk(a,b,false,false);
         	}
     	}
     	this.getServer().unloadWorld(this.world, false);
     }
     
     public void broadcastMessage(String message){
     	World world = this.getServer().getWorld(WorldName);
     	for (Player p : world.getPlayers()) {
     		p.sendMessage(message);
     	}
     }
 }
