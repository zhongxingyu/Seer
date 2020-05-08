 package com.beecub.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import com.beecub.glizer.glizer;
 
 public class bConfigManager {
 	
 	protected static glizer plugin;
 	protected static YamlConfiguration conf;
     public static List<String> bannedPlayers = new LinkedList<String>();
     public static String key;
     public static String servername;
     public static String owner;
     public static String globalreputation;
     public static String banborder;
     public static String shared_banborder;
     
     // features
     public static boolean usewhitelist;
     public static boolean useglobalbans;
     public static boolean usebansystem;
     public static boolean useprofiles;
     public static boolean usecomments;
     public static boolean useratings;
     public static boolean useevents;
     public static boolean noip;
     public static boolean bungeeCord;
     
     // broadcasting
     public static boolean broadcastWarning;
     public static boolean broadcastBan;
 	public static boolean broadcastKick;
 	public static boolean messageReputation;
 	
 	public static String ban_kickmessage;
 	public static String tempban_kickmessage;
 	public static String ban_joinmessage;
 	public static String whitelist_joinmessage;
 	public static String ipcheck_joinmessage;
 	
 	// Language
 	public static String language;
 	private static File confFile;
     
 	
     public bConfigManager(glizer glizer) {
     	plugin = glizer;
     	
     	File theDir = new File(plugin.getDataFolder(),"");
 		if (!theDir.exists())
 		{
 			theDir.mkdir();
 		}
     	  
     	setupconf();
     	load();
     }
     
 	private static void load() {
     	try {
 			conf.load(confFile);
 		} catch (Exception e) {
 			e.printStackTrace();
     		bChat.log("Failed to save config.yml!", 3);			
 		}
     	
     	key = conf.getString("APIkey", null);
     	if(key == null) conf.set("APIkey", "");
     	
     	servername = conf.getString("servername", null);
     	if(servername == null) conf.set("servername", "please change");
     	
     	owner = conf.getString("owner", null);
     	if(owner == null) conf.set("owner", "please change");
     	
     	
     	// options
     	banborder = conf.getString("options.banborder", "1000");
     	if(banborder == "1000") conf.set("options.banborder", "-40");
     	
     	shared_banborder = conf.getString("options.shared.banborder", "-200");
     	conf.set("options.shared.banborder", shared_banborder);
     	
     	glizer.D = conf.getBoolean("options.debugmode", false);
     	
     	language = conf.getString("language", "en");
     	
     	// features
    	if(!conf.contains("features.bungiecord")) conf.set("features.bungiecord", false);
         if(!conf.contains("features.usewhitelist")) conf.set("features.usewhitelist", false);
         if(!conf.contains("features.useglobalbans")) conf.set("features.useglobalbans", true);
     	if(!conf.contains("features.usebansystem")) conf.set("features.usebansystem", true);
         if(!conf.contains("features.useprofiles")) conf.set("features.useprofiles", true);
         if(!conf.contains("features.usecomments")) conf.set("features.usecomments", true);
         if(!conf.contains("features.useratings")) conf.set("features.useratings", true);
         if(!conf.contains("features.useevents")) conf.set("features.useevents", false);
     	
         noip = conf.getBoolean("features.noip", false);  
         bungeeCord = conf.getBoolean("features.bungeeCord", false);  
     	usewhitelist = conf.getBoolean("features.usewhitelist", false);    	
     	useglobalbans = conf.getBoolean("features.useglobalbans", false);        
         usebansystem = conf.getBoolean("features.usebansystem", false);        
         useprofiles = conf.getBoolean("features.useprofiles", false);
         usecomments = conf.getBoolean("features.usecomments", false);        
         useratings = conf.getBoolean("features.useratings", false);
         useevents = conf.getBoolean("features.useevents", false);
         
     	if(!conf.contains("announcing.broadcastWarning")) conf.set("announcing.broadcastWarning", false);
     	if(!conf.contains("announcing.broadcastBan")) conf.set("announcing.broadcastBan", true);
     	if(!conf.contains("announcing.broadcastKick")) conf.set("announcing.broadcastBan", true);
     	if(!conf.contains("announcing.messageReputation")) conf.set("announcing.messageReputation", false);
     	
 
     	broadcastWarning = conf.getBoolean("announcing.broadcastWarning", false);
     	broadcastBan = conf.getBoolean("announcing.broadcastBan", false);
     	broadcastKick = conf.getBoolean("announcing.broadcastKick", false);
     	messageReputation = conf.getBoolean("announcing.messageReputation",false);
     	
     	ban_kickmessage = conf.getString("message.ban.kickmessage", "You are banned from this server. Reason: %1");
     	tempban_kickmessage = conf.getString("message.tempban.kickmessage", "You are temporarily banned from this server. Reason: %1");
     	ban_joinmessage = conf.getString("message.ban.joinmessage", "You are banned from this server. Check glizer.de");
     	whitelist_joinmessage = conf.getString("message.whitelist.joinmessage", "You aren't whitelisted on this server. Apply on glizer.de");
     	
     	if(!conf.contains("message.ipcheck.joinmessage")) conf.set("message.ipcheck.joinmessage", "You are connecting from a non allowed ip. Change your settings on glizer.de");
     	ipcheck_joinmessage = conf.getString("message.ipcheck.joinmessage", "You are connecting from a non allowed ip. Change your settings on glizer.de");
     	
         try {
         	if (!confFile.exists())
         		confFile.createNewFile();
 			conf.save(confFile);
 		} catch (IOException e) {
 			e.printStackTrace();
     		bChat.log("Failed to save config.yml!", 3);
 		}
     }
 	
 	public static void reload() {
 		load();
 	}
 	
     private static void setupconf() {
         confFile = new File(plugin.getDataFolder(), "config.yml");
         
         if (confFile.exists())
         {
             conf = new YamlConfiguration();
             try {
 				conf.load(confFile);
 			} catch (Exception e) {
 				e.printStackTrace();
         		bChat.log("Failed to load config.yml!", 3);
 			}
         }
         else {
             confFile = new File(plugin.getDataFolder(), "config.yml");
             
             conf = new YamlConfiguration();
             conf.set("APIkey", "Add your API-Key here");
             conf.set("servername", "Add your servername here");
             conf.set("owner", "Add your name here");
             
             conf.set("message.ban.kickmessage", "You are banned from this server. Reason: %1");
             conf.set("message.tempban.kickmessage", "You are temporarily banned from this server. Reason: %1");
             conf.set("message.ban.joinmessage", "You are banned from this server. Check glizer.de");
             conf.set("message.whitelist.joinmessage", "You aren't whitelisted on this server. Apply on glizer.de");
             conf.set("message.ipcheck.joinmessage", "You are connecting from a non allowed ip. Change your settings on glizer.de");
             
             conf.set("language", "en");
             
             // options
             conf.set("options.banborder", "-40");
             conf.set("options.shared.banborder", "-200");
             conf.set("options.debugmode", false);
             
             // features
             conf.set("features.usewhitelist", false);
             conf.set("features.useglobalbans", true);
             conf.set("features.usebansystem", true);
             conf.set("features.useprofiles", true);
             conf.set("features.usecomments", true);
             conf.set("features.useratings", true);
             conf.set("features.useevents", false);
             conf.set("features.noip",false);
             conf.set("features.bungeeCord",false);
             
             // announces
             conf.set("announcing.broadcastWarning", false);
         	conf.set("announcing.broadcastBan", true);
         	conf.set("announcing.broadcastKick", true);
         	conf.set("announcing.messageReputation", false);
             try {
                 confFile.createNewFile();
 				conf.save(confFile);
 			} catch (IOException e) {
 				e.printStackTrace();
         		bChat.log("Failed to save config.yml!", 3);
 			}
         }
 	}
 }
