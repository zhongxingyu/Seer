 package de.rene_zeidler.mojangstatus;
 
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
 import net.md_5.bungee.BungeeCord;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ServerPing;
 import net.md_5.bungee.api.event.ProxyPingEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.md_5.bungee.api.scheduler.ScheduledTask;
 import net.md_5.bungee.event.EventHandler;
 
 public class MojangStatus extends Plugin implements Listener {
 	/**
 	 * Current status of the service minecraft.net
 	 */
 	public static boolean minecraftNet = true;
 	/**
 	 * Current status of the service login.minecraft.net
 	 */
 	public static boolean loginMinecraft = true;
 	/**
 	 * Current status of the service session.minecraft.net
 	 */
 	public static boolean sessionMinecraft = true;
 	/**
 	 * Current status of the service account.mojang.com
 	 */
 	public static boolean accountMojang = true;
 	/**
 	 * Current status of the service auth.mojang.com
 	 */
 	public static boolean authMojang = true;
 	/**
 	 * Current status of the service skins.minecraft.net
 	 */
 	public static boolean skinsMinecraft = true;
 	/**
 	 * Current status of the service authserver.mojang.com
 	 */
 	public static boolean authserverMojang = true;
 	
 	//How long (in number of checks) the status of the server hasn't changed
	public int minecraftNetUnchangedTimer;
	public int loginMinecraftUnchangedTimer;
	public int sessionMinecraftUnchangedTimer;
	public int accountMojangUnchangedTimer;
	public int authMojangUnchangedTimer;
	public int skinsMinecraftUnchangedTimer;
	public int authserverMojangUnchangedTimer;
 	
 	/**
 	 * Static instance of the plugin itself
 	 * Can be accessed with getInstance() 
 	 */
 	private static MojangStatus instance = null;
 	private MainConfig config;
 	private ScheduledTask task;
 	
 	/**
 	 * Returns the instance of the plugin
 	 * @return instance of MojangStatus
 	 */
 	public static MojangStatus getInstance()
 	{
 		if(instance == null) instance = (MojangStatus) BungeeCord.getInstance().getPluginManager().getPlugin("MojangStatus");
 		return instance;
 	}
 	
 	/**
 	 * Returns the configuration object
 	 * @return instance of MainConfig
 	 */
 	public MainConfig getConfig()
 	{
 		return config;
 	}
 	
 	public void onEnable()
 	{
 		this.config = new MainConfig(this);
 		
		//set initial value of unchanged timers to config.broadcastUpWait to avoid a broadcast on startup
		minecraftNetUnchangedTimer = config.broadcastUpWait;
		loginMinecraftUnchangedTimer = config.broadcastUpWait;
		sessionMinecraftUnchangedTimer = config.broadcastUpWait;
		accountMojangUnchangedTimer = config.broadcastUpWait;
		authMojangUnchangedTimer = config.broadcastUpWait;
		skinsMinecraftUnchangedTimer = config.broadcastUpWait;
		authserverMojangUnchangedTimer = config.broadcastUpWait;
		
 		//register Listener/Commands
 		BungeeCord.getInstance().getPluginManager().registerListener(this, this);
 		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandMCStatus());
 		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandSetStatus());
 		
 		//schedule task
 		task = BungeeCord.getInstance().getScheduler().schedule(this, new StatusChecker(), 0, config.checkInterval, TimeUnit.SECONDS);
 		getLogger().log(Level.INFO, "Initialized Scheduler with an interval of " + config.checkInterval + " seconds");
 		
 		if(config.debug) getLogger().log(Level.INFO, "Debugging enabled");
 	}
 
 	
 	public void onDisable()
 	{
 		try {
 			this.config.save();
 		} catch (InvalidConfigurationException e) {
 			getLogger().log(Level.SEVERE, "Error while saving the config:");
 			e.printStackTrace();
 		}
 		
 		//cancel task
 		task.cancel();
 	}
 	
 	/**
 	 * Sets the status of the service minecraft.net
 	 * @param online true for online, false for offline
 	 */
 	public void setMinecraftNetStatus(boolean online)
 	{
 		if(minecraftNet != online) {
 			//status has changed
 			minecraftNetUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Minecraft.net just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			minecraftNetUnchangedTimer++;
 			if(!online && minecraftNetUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastMinecraftNetStillDown); //service still down and interval elapsed
 		}
 		
 		if(online && minecraftNetUnchangedTimer == config.broadcastUpWait) broadcast(config.broadcastMinecraftNetUp); //service went up and waiting time elapsed
 		else if(!online && minecraftNetUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastMinecraftNetDown); //service went down and waiting time elapsed
 		minecraftNet = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service login.minecraft.net
 	 * @param online true for online, false for offline
 	 */
 	public void setLoginMinecraftStatus(boolean online)
 	{
 		if(loginMinecraft != online) {
 			//status has changed
 			loginMinecraftUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Login servers (legacy) just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			loginMinecraftUnchangedTimer++;
 			if(!online && loginMinecraftUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastLoginMinecraftStillDown); //service still down and interval elapsed
 		}
 		
 		if(online && loginMinecraftUnchangedTimer == config.broadcastUpWait) broadcast(config.broadcastLoginMinecraftUp); //service went up and waiting time elapsed
 		else if(!online && loginMinecraftUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastLoginMinecraftDown); //service went down and waiting time elapsed
 		loginMinecraft = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service session.minecraft.net
 	 * @param online true for online, false for offline
 	 */
 	public void setSessionMinecraftStatus(boolean online)
 	{
 		if(sessionMinecraft != online) {
 			//status has changed
 			sessionMinecraftUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Session servers (legacy) just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			sessionMinecraftUnchangedTimer++;
 			if(!online && sessionMinecraftUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastSessionMinecraftStillDown); //service still down and interval elapsed
 		}
 		
 		if(online && sessionMinecraftUnchangedTimer == config.broadcastUpWait) {
 			broadcast(config.broadcastSessionMinecraftUp); //service went up and waiting time elapsed
 			if(!authserverMojang && config.broadcastLoginStillDownOnSessionUp) broadcast(config.broadcastAuthserverMojangStillDown); //special broadcast when login servers are still down
 		}
 		else if(!online && sessionMinecraftUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastSessionMinecraftDown); //service went down and waiting time elapsed
 		sessionMinecraft = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service account.mojang.com
 	 * @param online true for online, false for offline
 	 */
 	public void setAccountMojangStatus(boolean online)
 	{
 		if(accountMojang != online) {
 			//status has changed
 			accountMojangUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Mojang accounts website just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			accountMojangUnchangedTimer++;
 			if(!online && accountMojangUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastAccountMojangStillDown); //service still down and interval elapsed
 		}
 		
 		if(online && accountMojangUnchangedTimer == config.broadcastUpWait) broadcast(config.broadcastAccountMojangUp); //service went up and waiting time elapsed
 		else if(!online && accountMojangUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastAccountMojangDown); //service went down and waiting time elapsed
 		accountMojang = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service auth.mojang.com
 	 * @param online true for online, false for offline
 	 */
 	public void setAuthMojangStatus(boolean online)
 	{
 		if(authMojang != online) {
 			//status has changed
 			authMojangUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Mojang accounts login (legacy) just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			authMojangUnchangedTimer++;
 			if(!online && authMojangUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastAuthMojangStillDown);  //service still down and interval elapsed
 		}
 		
 		if(online && authMojangUnchangedTimer == config.broadcastUpWait) broadcast(config.broadcastAuthMojangUp); //service went up and waiting time elapsed
 		else if(!online && authMojangUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastAuthMojangDown); //service went down and waiting time elapsed
 		authMojang = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service skins.minecraft.net
 	 * @param online true for online, false for offline
 	 */
 	public void setSkinsMinecraftStatus(boolean online)
 	{
 		if(skinsMinecraft != online) {
 			//status has changed
 			skinsMinecraftUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Skin servers just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			skinsMinecraftUnchangedTimer++;
 			if(!online && skinsMinecraftUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastSkinsMinecraftStillDown);  //service still down and interval elapsed
 		}
 		
 		if(online && skinsMinecraftUnchangedTimer == config.broadcastUpWait) broadcast(config.broadcastSkinsMinecraftUp); //service went up and waiting time elapsed
 		else if(!online && skinsMinecraftUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastSkinsMinecraftDown); //service went down and waiting time elapsed
 		skinsMinecraft = online; //set attribute to new status
 	}
 	
 	/**
 	 * Sets the status of the service authserver.mojang.com
 	 * @param online true for online, false for offline
 	 */
 	public void setAuthServerMojangStatus(boolean online)
 	{
 		if(authserverMojang != online) {
 			//status has changed
 			authserverMojangUnchangedTimer = 0;
 			getLogger().log(Level.INFO, "Authentification service (Minecraft login) just went " + (online ? "online" : "offline"));
 		} else {
 			//status is unchanged
 			authserverMojangUnchangedTimer++;
 			if(!online && authserverMojangUnchangedTimer % config.remainsDownInterval == 0) broadcast(config.broadcastAuthserverMojangStillDown);  //service still down and interval elapsed
 		}
 		
 		if(online && authserverMojangUnchangedTimer == config.broadcastUpWait) {
 			broadcast(config.broadcastAuthserverMojangUp); //service went up and waiting time elapsed
 			if(!sessionMinecraft && config.broadcastSessionStillDownOnLoginUp) broadcast(config.broadcastSessionMinecraftStillDown); //special broadcast when session servers are still down
 		}
 		else if(!online && authserverMojangUnchangedTimer == config.broadcastDownWait) broadcast(config.broadcastAuthserverMojangDown); //service went down and waiting time elapsed
 		authserverMojang = online; //set attribute to new status
 	}
 	
 	@EventHandler
 	public void onPing(ProxyPingEvent ev)
 	{
 		//store original MOTD
 		String modt = ev.getResponse().getMotd();
 		
 		if(!sessionMinecraft && !authserverMojang) { //session + login offline
 			modt = parseModt(config.sessionsAndLoginDown, modt);
 		} else if(!sessionMinecraft) { //only session offline
 			modt = parseModt(config.sessionsDown, modt);
 		} else if(!authserverMojang) { //only login offline
 			modt = parseModt(config.loginDown, modt);
 		}
 		
 		if(!skinsMinecraft && !minecraftNet) { //skins and minecraft.net offline
 			modt = parseModt(config.skinsAndMinecraftNetDown, modt);
 		} else if(!skinsMinecraft) { //only skins offline
 			modt = parseModt(config.skinsDown, modt);
 		} else if(!minecraftNet) { //only minecraft.net offline
 			modt = parseModt(config.minecraftNetDown, modt);
 		}
 		
 		if(modt != ev.getResponse().getMotd()) { //MOTD was changed
 			//create new ServerPing with changed MOTD
 			ServerPing sp = new ServerPing(
 					ev.getResponse().getProtocolVersion(),
 					ev.getResponse().getGameVersion(),
 					modt,
 					ev.getResponse().getCurrentPlayers(),
 					ev.getResponse().getMaxPlayers());
 			ev.setResponse(sp); //replace ServerPing in the event
 		}
 	}
 	
 	/**
 	 * Parses an MOTD string
 	 * Replaces %motd% with the old MODT and translates color codes with &
 	 * @param s Original String
 	 * @param motd Old MOTD
 	 * @return Parsed string
 	 */
 	public String parseModt(String s, String motd)
 	{
 		return ChatColor.translateAlternateColorCodes('&', s.replace("%motd%", motd));
 	}
 	
 	/**
 	 * Broadcasts the message if it is not empty
 	 * The message gets split into multiple messages when there are line breaks with \n
 	 * @param s message
 	 */
 	public void broadcast(String s)
 	{
 		if(!s.isEmpty()) for(String m : ChatColor.translateAlternateColorCodes('&', s).split("\n")) BungeeCord.getInstance().broadcast(m);
 	}
 }
