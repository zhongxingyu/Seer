 /*
  * Copyright (C) [2011]  [Pascal Koenig]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, see <http://www.gnu.org/licenses/>. 
 */
 package de.sockenklaus.XmlStats;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nidefawl.Achievements.Achievements;
 import terranetworkorg.Stats.Stats;
 import com.nijikokun.register.Register;
 import com.nijikokun.register.payment.Methods;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class XmlStats.
  */
 public class XmlStats extends JavaPlugin {
 
 	private final static Logger log = Logger.getLogger("Minecraft");
 	private String version;
 	private final static String logprefix = "[XmlStats]";
 	private boolean enabled = false;
 	
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	@Override
 	public void onDisable() {
 		Webserver webserverTemp = (Webserver)XmlStatsRegistry.get("webserver");
 		
 		if(this.enabled && webserverTemp.isRunning()){
 			this.enabled = false;
 			
 			webserverTemp.stop();
 			LogDebug("Webserver stopped.");
 			
 			XmlStatsRegistry.flush();
 			getServer().getScheduler().cancelTasks(this);
 		}
 		LogInfo("XmlStats Disabled");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.Plugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {
 		this.version = getDescription().getVersion();
 		
 		getDataFolder().mkdirs();
 				
 		XmlStatsRegistry.put("settings", new Settings(this));
 		XmlStatsRegistry.put("xmlstats", this);
 		
 		Settings settingsTemp = (Settings)XmlStatsRegistry.get("settings");
 		
 		LogDebug("Settings read:");
 		LogDebug("options.webserver-enabled: "+settingsTemp.getBoolean("options.webserver-enabled"));
 		LogDebug("options.webserver-port: "+settingsTemp.getInt("options.webserver-port"));
 		LogDebug("options.verbose-enabled: "+settingsTemp.getBoolean("options.verbose-enabled"));
 			
 		if (settingsTemp.getBoolean("options.webserver-enabled")){
 			try {
 				XmlStatsRegistry.put("webserver", new Webserver());
 					
 				this.enabled = true;
 				LogInfo("XmStats "+this.version+" enabled");
 				this.hookPlugins();
 				this.registerEvents();
 			}
 			catch (Exception ex){
 				LogError("Fehler beim Erstellen des Webservers:");
 				LogError(ex.getMessage());
 				ex.printStackTrace();
 			}			
 		}
 		else {
 			LogWarn("Webserver ist derzeit in der "+settingsTemp.getSettingsFilename()+" deaktiviert.");
 		}
 		
 		
 	}
 	
 	/**
 	 * Log error.
 	 *
 	 * @param Message the message
 	 */
 	public static void LogError(String Message) {
 		log.log(Level.SEVERE, logprefix + " " + Message);
 	}
 
 	/**
 	 * Log info.
 	 *
 	 * @param Message the message
 	 */
 	public static void LogInfo(String Message) {
 		log.info(logprefix + " " + Message);
 	}
 	
 	/**
 	 * Log warn.
 	 *
 	 * @param Message the message
 	 */
 	public static void LogWarn(String Message){
 		log.log(Level.WARNING, logprefix + " "+ Message);
 	}
 	
 	/**
 	 * Log debug.
 	 *
 	 * @param Message the message
 	 */
 	public static void LogDebug(String Message){
 		Settings settingsTemp = (Settings)XmlStatsRegistry.get("settings");
 		if(settingsTemp.getBoolean("options.verbose-enabled")){
 			log.log(Level.INFO, logprefix+"[DEBUG] "+Message);
 		}
 	}
 	
 	/**
 	 * Hook plugins.
 	 */
 	protected void hookPlugins(){
 		this.hookAchievements();
 		this.hookRegister();
 		this.hookStats();
 	}
 	
 	protected void hookRegister(){
 		Plugin registerTemp = getServer().getPluginManager().getPlugin("Register");
 		Webserver webserver = (Webserver)XmlStatsRegistry.get("webserver");
 		
         if (this.checkRegister()) {
         	XmlStatsRegistry.put("register", (Register)registerTemp);
             LogInfo("Hooked into Register");
             webserver.startRegister();
         }
         else {
         	LogWarn("Register or no payment method found! Can't hook into it.");
         }
 	}
 	
 	protected void hookAchievements(){
 		Plugin AchievementsTemp = getServer().getPluginManager().getPlugin("Achievements");
 		Webserver webserver = (Webserver)XmlStatsRegistry.get("webserver");
 		
         if(this.checkAchievements()){
         	XmlStatsRegistry.put("achievements", (Achievements)AchievementsTemp);
         	LogInfo("Hooked into Achievements!");
         	webserver.startAchievements();
         }
         else {
         	LogWarn("Achievements not found! Can't hook into it.");
         }
 	}
 	
 	protected void hookStats(){
		Plugin StatsTemp = getServer().getPluginManager().getPlugin("Stats");
 		Webserver webserver = (Webserver)XmlStatsRegistry.get("webserver");
 		
 		if(this.checkStats()){
         	XmlStatsRegistry.put("stats", (Stats)StatsTemp);
         	LogInfo("Hooked into Stats!");
         	webserver.startStats();
         }
         else {
         	LogWarn("Stats not found! Can't hook into it.");
         }
 	}
 	
 	/**
 	 * Checks if is stats hooked.
 	 *
 	 * @return true, if is stats hooked
 	 */
 	public boolean checkStats(){
 		Plugin StatsTemp = getServer().getPluginManager().getPlugin("Stats 2.0");
 		
		if(StatsTemp != null && StatsTemp.getClass().getName().equals("terranetworkorg.Stats.Stats") && StatsTemp.isEnabled()) return true;
 		return false;
 	}
 	
 	/**
 	 * Checks if is i conomy hooked.
 	 *
 	 * @return true, if is i conomy hooked
 	 */
 	public boolean checkRegister(){
 		Plugin registerTemp = getServer().getPluginManager().getPlugin("Register");
 				
 		if (registerTemp != null && registerTemp.getClass().getName().equals("com.nijikokun.register.Register") && registerTemp.isEnabled() && Methods.hasMethod()) return true;
 		return false;
 	}
 	
 	/**
 	 * Checks if is Achievements hooked.
 	 *
 	 * @return true, if is Achievements hooked
 	 */
 	public boolean checkAchievements(){
 		Plugin AchievementsTemp = getServer().getPluginManager().getPlugin("Achievements");
 		
 		if(AchievementsTemp != null && AchievementsTemp.getClass().getName().equals("com.nidefawl.Achievements.Achievements") && AchievementsTemp.isEnabled()) return true;
 		return false;
 	}
 		
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(commandLabel.equalsIgnoreCase("xmlstats")){
 			if(sender.isOp()){
 				if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
 					sender.sendMessage("Reloading the XmlStats plugin...");
 					LogInfo("Reloading the XmlStats plugin...");
 					this.reload();
 					return true;
 				}
 			}
 			else {
 				sender.sendMessage("You don't have the permission to do this!");
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Reload.
 	 */
 	protected void reload() {
 		this.onDisable();
 		this.onEnable();
 	}
 	
 	private void registerEvents(){
 		XmlStatsServerListener listener = new XmlStatsServerListener(this);
 
 		getServer().getPluginManager().registerEvents(listener, this);
 	}
 }
