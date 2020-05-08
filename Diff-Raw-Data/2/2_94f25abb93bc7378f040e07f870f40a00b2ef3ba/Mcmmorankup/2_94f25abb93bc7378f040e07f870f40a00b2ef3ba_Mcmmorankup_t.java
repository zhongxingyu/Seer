 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.mcmmorankup;
 
 import com.avaje.ebeaninternal.server.lib.util.NotFoundException;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import me.stutiguias.apimcmmo.RankUp;
 import me.stutiguias.listeners.MRUCommandListener;
 import me.stutiguias.listeners.MRUPlayerListener;
 import me.stutiguias.mcmmorankup.task.UpdateTask;
 import me.stutiguias.metrics.Metrics;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Stutiguias
  */
 public class Mcmmorankup extends JavaPlugin {
 
     public String logPrefix = "[McMMoRankUp] ";
     String PluginDir = "plugins" + File.separator + "Mcmmorankup";
     public static final Logger log = Logger.getLogger("Minecraft");
     public Permission permission = null;
     public final MRUPlayerListener playerlistener = new MRUPlayerListener(this);
     public Economy economy = null;
     public RankUp RankUp = null;
     public HashMap<String,Boolean> isHabilityRankExist;
     public HashMap<String,HashMap<String,ArrayList<String>>> RankUpConfig;
     public HashMap<String,HashMap<String,String>> BroadCast;
     public String[] PlayerToIgnore;
     public String[] GroupToIgnore;
     public HashMap<String,Long> Playertime;
     public Integer total;
     
     // Messages
     public String ChooseHability;
     public String NotHaveProfile;
     public String MPromote;
     public String MSucess;
     public String MFail;
     public String NotFound;
     public String setGender;
     
     //ConfigAcess for hability
     public ConfigAccessor POWERLEVEL;
     public ConfigAccessor EXCAVATION;
     public ConfigAccessor FISHING;
     public ConfigAccessor HERBALISM;
     public ConfigAccessor MINING;
     public ConfigAccessor AXES;
     public ConfigAccessor ARCHERY;
     public ConfigAccessor SWORDS;
     public ConfigAccessor TAMING;
     public ConfigAccessor UNARMED;
     public ConfigAccessor ACROBATICS;
     public ConfigAccessor REPAIR;
     
     public boolean TagSystem;
     public String AutoUpdateTime;
     public String DefaultSkill;
     public boolean UseAlternativeBroadcast;
     public boolean PromoteOnJoin;
     public boolean AutoUpdate;
     
     @Override
     @SuppressWarnings("LoggerStringConcat")
     public void onEnable() {
 
             log.log(Level.INFO,logPrefix + "Mcmmorankup is initializing");
 
             onLoadConfig();
             getCommand("mru").setExecutor(new MRUCommandListener(this));
             setupEconomy();
             setupPermissions();
             
             PluginManager pm = getServer().getPluginManager();
             pm.registerEvents(playerlistener, this);
             
             if(AutoUpdate) {
                 Long uptime = new Long("0");
                 if(AutoUpdateTime.contains("h")) {
                   uptime = Long.parseLong(AutoUpdateTime.replace("h",""));
                   uptime = ( ( uptime * 60 ) * 60 ) * 20;
                 }
                 if(AutoUpdateTime.contains("m")) {
                   uptime = Long.parseLong(AutoUpdateTime.replace("m",""));
                   uptime =  ( uptime * 60 ) * 20;
                 }
                 getServer().getScheduler().scheduleAsyncRepeatingTask(this, new UpdateTask(this), uptime, uptime);
             }
             
             File f = new File("plugins"+ File.separator +"Mcmmorankup"+ File.separator +"userdata");
             if(!f.exists())  {
                 log.log(Level.INFO,logPrefix + " Diretory not exist creating new one");
                 f.mkdirs();
             }
             
             if(this.permission.isEnabled() == true)
             {
                 log.log(Level.INFO,logPrefix + "Vault perm enable.");    
             }else{
                 log.log(Level.INFO,logPrefix + "Vault NOT ENABLE.");    
             }
             
             //Metrics 
             try {
               log.info(logPrefix + "Sending Metrics for help the dev... http://metrics.griefcraft.com :-)");
               Metrics metrics = new Metrics(this);
               metrics.start();
             } catch (IOException e) {
               log.info(logPrefix + "Failed to submit the stats :-(");
             }
 
     }
 
     @Override
     public void onDisable() {
             getServer().getPluginManager().disablePlugin(this);
             log.log(Level.INFO, logPrefix + " Disabled. Bye :D");
     }
     
     public void onReload() {
         this.reloadConfig();
         saveConfig();
         getServer().getPluginManager().disablePlugin(this);
         getServer().getPluginManager().enablePlugin(this);
     }
 
     private void initConfig() {
                 
                 getConfig().addDefault("Message.NotHaveProfile", "Dot not find any profile of mcMMO of you");
                 getConfig().addDefault("Message.ChooseHability", "You choose to rank up base on %hability%");
                 getConfig().addDefault("Message.RankUp", "Player %player% promote to %group%");
                 getConfig().addDefault("Message.Sucess", "Promote Sucess");
                 getConfig().addDefault("Message.Fail", "Promote Fail");
                 getConfig().addDefault("Message.NotFound", "Hability For Rank not found or configured");
                 getConfig().addDefault("Message.setGender", "Your Gender is set to %gender%");
                 
                 getConfig().addDefault("Config.UseTagOnlySystem", false);
                 getConfig().addDefault("Config.PromoteOnJoin", true);
                 getConfig().addDefault("Config.AutoUpdate", true);
                 getConfig().addDefault("Config.AutoUpdateTime", "1h");
                 getConfig().addDefault("Config.UseAlternativeBroadCast", true);
                 getConfig().addDefault("Config.DefaultSkill", "POWERLEVEL");
                 getConfig().addDefault("PlayerToIgnore", "Stutiguias,Player2");
                 getConfig().addDefault("GroupToIgnore","Admin,Moderator");
 
                 getConfig().options().copyDefaults(true);
                 saveConfig();
     }
 
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         permission = rsp.getProvider();
         return permission != null;
     }
 
     private Boolean setupEconomy() {
             RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
             if (economyProvider != null) {
                     economy = economyProvider.getProvider();
             }
 
             return (economy != null);
     }
 
     public void onLoadConfig() {
             initConfig();
             UseAlternativeBroadcast = getConfig().getBoolean("Config.UseAlternativeBroadCast");
             PromoteOnJoin = getConfig().getBoolean("Config.PromoteOnJoin");
             AutoUpdate = getConfig().getBoolean("Config.AutoUpdate");
             AutoUpdateTime = getConfig().getString("Config.AutoUpdateTime");
             PlayerToIgnore = getConfig().getString("PlayerToIgnore").split((","));
             GroupToIgnore = getConfig().getString("GroupToIgnore").split((","));
             DefaultSkill = getConfig().getString("Config.DefaultSkill");
             TagSystem = getConfig().getBoolean("Config.UseTagOnlySystem");
             
             log.log(Level.INFO,logPrefix + " Alternative Broadcast is " + UseAlternativeBroadcast);
             log.log(Level.INFO,logPrefix + " Default skill is " + DefaultSkill);
             
             RankUp = new RankUp(this);
             RankUpConfig = new HashMap<String, HashMap<String,ArrayList<String>>>();
             BroadCast = new HashMap<String, HashMap<String, String>>();
             isHabilityRankExist = new HashMap<String, Boolean>();
             
             // InitAcessor
             POWERLEVEL = new ConfigAccessor(this,"powerlevel.yml");
             SetupAccessor("POWERLEVEL",POWERLEVEL);
             EXCAVATION = new ConfigAccessor(this,"excavation.yml");
             SetupAccessor("EXCAVATION",EXCAVATION);
             FISHING = new ConfigAccessor(this,"fishing.yml");
             SetupAccessor("FISHING",FISHING);
             HERBALISM = new ConfigAccessor(this,"herbalism.yml");
             SetupAccessor("HERBALISM",HERBALISM);
             MINING = new ConfigAccessor(this,"mining.yml");
             SetupAccessor("MINING",MINING);
             AXES = new ConfigAccessor(this,"axes.yml");
             SetupAccessor("AXES",AXES);
             ARCHERY = new ConfigAccessor(this,"archery.yml");
             SetupAccessor("ARCHERY",ARCHERY);
             SWORDS = new ConfigAccessor(this,"swords.yml");
             SetupAccessor("SWORDS",SWORDS);
             TAMING = new ConfigAccessor(this,"taming.yml");
             SetupAccessor("TAMING",TAMING);
             UNARMED = new ConfigAccessor(this,"unarmed.yml");
             SetupAccessor("UNARMED",UNARMED);
             ACROBATICS = new ConfigAccessor(this,"acrobatics.yml");
             SetupAccessor("ACROBATICS",ACROBATICS);
             REPAIR = new ConfigAccessor(this,"repair.yml");
             SetupAccessor("REPAIR",REPAIR);
     
             
             // Messages
             ChooseHability = getConfig().getString("Message.ChooseHability");
             NotHaveProfile = getConfig().getString("Message.NotHaveProfile");
             MPromote = getConfig().getString("Message.RankUp");
             MSucess = getConfig().getString("Message.Sucess");
             MFail = getConfig().getString("Message.Fail");
             NotFound = getConfig().getString("Message.NotFound");
             setGender = getConfig().getString("Message.setGender");
             
             Playertime = new HashMap<String, Long>();
     }
    
     public long getCurrentMilli() {
 		return System.currentTimeMillis();
     }
     
     public HashMap<String,ArrayList<String>> getRanks(ConfigAccessor ca){
         HashMap<String,ArrayList<String>> Ranks = new HashMap<String, ArrayList<String>>();
         ArrayList<String> Rank = new ArrayList<String>();
         for (String key : ca.getConfig().getConfigurationSection("RankUp.Male.").getKeys(false)){
           Rank.add(key + "," + ca.getConfig().getString("RankUp.Male." + key));
         }
         Ranks.put("Male", Rank);
         Rank = new ArrayList<String>();
         for (String key : ca.getConfig().getConfigurationSection("RankUp.Female.").getKeys(false)){
           Rank.add(key + "," + ca.getConfig().getString("RankUp.Female." + key));
         }
         Ranks.put("Female", Rank);
         return Ranks;
     }
     
     public String parseColor(String message) {
         try { 
             for (ChatColor color : ChatColor.values()) {
                 message = message.replaceAll(String.format("&%c", color.getChar()), color.toString());
             }
             return message;
         }catch(Exception ex) {
             return message;
         }
     }
     
     public HashMap<String,String> getAlternativeBroadcast(ConfigAccessor ca){
         HashMap<String,String> BroadCastCa = new HashMap<String, String>();
         for (String key : ca.getConfig().getConfigurationSection("Broadcast.").getKeys(false)){
           BroadCastCa.put(key, ca.getConfig().getString("Broadcast." + key));
          // log.log(Level.INFO, logPrefix + "Group " + key + " will broadcast " + ca.getConfig().getString("RankUpConfig." + key));
         }
         return BroadCastCa;
     }
     public void SetupAccessor(String name,ConfigAccessor ca) {
         try {
             RankUpConfig.put(name,getRanks(ca));
            if(UseAlternativeBroadcast) BroadCast.put(name,getAlternativeBroadcast(ca));
             log.info(logPrefix + name + " Rank Enable!");
             isHabilityRankExist.put(name,true);
         }catch(Exception ex) {
             log.info(logPrefix + name + " Rank file corrupt/not found. Disable!");
             isHabilityRankExist.put(name,false);
         }
     }
 }
