 package com.gmail.ebiggz.plugins.mythsentials;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;

 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Mythsentials extends JavaPlugin {
 	
 	FileConfiguration newConfig;
     Integer toolRepairPoint;
     Integer armorRepairPoint;
     public Boolean edIsAlive;
     public String edKiller;
     public HashMap<Integer, Boolean> tools;
     public HashMap<Integer, Boolean> armor; 
 	
 	private final BedrockBlocker bedrockbreaker = new BedrockBlocker(this);
 	private final InvincibleTools invincabletools = new InvincibleTools(this);
 	private final DragonListener dlistener = new DragonListener(this);
 	
 	private static final Logger log = Logger.getLogger("Minecraft");
     
     public void onDisable() {
     	log.info("[Mythsentials] Disabled!");
     }
 
     public void onEnable() {
     	loadConfig();
     	getServer().getPluginManager().registerEvents(new UnregNotifier(), this);
     	getServer().getPluginManager().registerEvents(new ColoredSignText(), this);
     	getServer().getPluginManager().registerEvents(new OnlineModNotifier(), this);
     	getServer().getPluginManager().registerEvents(new NoFallDamage(), this);
     	getServer().getPluginManager().registerEvents(bedrockbreaker, this);
     	getServer().getPluginManager().registerEvents(invincabletools, this);
     	getServer().getPluginManager().registerEvents(dlistener, this);
     	getCommand("helpme").setExecutor(new HelpMe(this));
     	getCommand("modhelp").setExecutor(new HelpMe(this));
     	getCommand("adminhelp").setExecutor(new HelpMe(this));
     	getCommand("mod").setExecutor(new HelpMe(this));
     	getCommand("admin").setExecutor(new HelpMe(this));
     	getCommand("mods").setExecutor(new HelpMe(this));
     	getCommand("admins").setExecutor(new HelpMe(this));
     	getCommand("restool").setExecutor(new ResTool());
     	getCommand("mythica").setExecutor(new HelpMenu());
     	getCommand("dragon").setExecutor(new EnderDragonChecker(this));
     	log.info("[Mythsentials] Enabled!");
     }
     public void loadConfig() {   	
         try {
         	reloadConfig();
             newConfig = this.getConfig();
             newConfig.options().copyDefaults(true);
             
             toolRepairPoint = newConfig.getInt("InvincibleStuff.toolRepairPoint", 99);
             armorRepairPoint = newConfig.getInt("InvincibleStuff.armorRepairPoint", 1);
             edIsAlive = newConfig.getBoolean("DragonData.dragonIsAlive");
             edKiller = newConfig.getString("DragonData.dragonLastKilledBy");
             
             // Load tools that are invincible. Convert to integers.
             tools = new HashMap<Integer, Boolean>();
             String[] tmp = newConfig.getString("InvincibleStuff.Tools", "276,277,278,279,293,261,359").split(",");
             for (String tool : tmp) {
                 if (tool.equals("")) continue;
                 tools.put(Integer.parseInt(tool), true);
             }
             // Load invincible armor
             armor = new HashMap<Integer, Boolean>();
             tmp = newConfig.getString("InvincibleStuff.Armor", "302,303,304,305").split(",");
             for (String arm : tmp) {
                 if (arm.equals("")) continue;
                 armor.put(Integer.parseInt(arm), true);
             }
             saveConfig();
         } catch (Exception e) {
             log.log(Level.SEVERE, "Exception while loading Mythsentials/config.yml", e);
         }
     }
 }
