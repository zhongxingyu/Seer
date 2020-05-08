 package com.beecub.glizer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.beecub.command.bCommandRouter;
 import com.beecub.execute.Backup;
 import com.beecub.execute.Whitelist;
 import com.beecub.util.APIRequestThread;
 import com.beecub.util.FinishedQueueWorker;
 import com.beecub.util.Language;
 import com.beecub.util.TickCounter;
 import com.beecub.util.UpToDateTask;
 import com.beecub.util.bBackupManager;
 import com.beecub.util.bChat;
 import com.beecub.util.bConfigManager;
 import com.beecub.util.bConnector;
 import com.beecub.util.bTimer;
 import com.beecub.util.bWhitelist;
 
 import de.upsj.glizer.APIRequest.APIRequest;
 import de.upsj.glizer.APIRequest.BanQueue;
 
 
 @SuppressWarnings({ "unused", "static-access" })
 public class glizer extends JavaPlugin {
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static PluginDescriptionFile pdfFile;
 	public static boolean permissions = false;
 	public static String messagePluginName;
 	public static boolean onlinemode = false;
 	public static String serverip;
 	public static String serverport;
 	public static boolean offline = false;
 	public static boolean D;
 	public static boolean upToDate = true;
 	public static glizer plugin;
 	public static bTimer heartbeatThread;
 	public static Thread heartbeatCheckThread;
 	private static Thread heartbeatStartThread;
 	
 	private static Thread apiRequestThread;
 	public static Queue<APIRequest> queue = new ConcurrentLinkedQueue<APIRequest>();
 	public static Queue<APIRequest> finished = new ConcurrentLinkedQueue<APIRequest>();
 	public static BanQueue banqueue;
 
 	public void onEnable() {
 
 		pdfFile = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new glizerPlayerListener(this), this);
 
 		bConfigManager bConfigManager = new bConfigManager(this);
 		bBackupManager bBackupManager = new bBackupManager(this);
 		bWhitelist bWhitelist = new bWhitelist(this);
 		bChat bChat = new bChat(this.getServer());
 
 		plugin = this;
 
 		serverport = /*this.getServer().getIp() +*/ String.valueOf(this.getServer().getPort());
 
 		if(setupMessages()) {
 		}
 
 		Language.Load();
 		if(checkOnlineMode()) {
 		}
 
 		if(serverLogin()) {
 		}
 
 		if(heartbeat(this)) {
 		}
 		
 		final glizer plugin = this;
 		
 		if (banqueue == null)
 		{
 			banqueue = new BanQueue();
 		}
 		
 		apiRequestThread = new APIRequestThread();
 		
 		apiRequestThread.setPriority(Thread.MIN_PRIORITY);
 		
 		apiRequestThread.start();
 
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new FinishedQueueWorker(this), 1, 1);
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TickCounter(this), 200, 200);
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpToDateTask(), 2000, 2000);
 
 		Backup.getPlayers();
 		Whitelist.getPlayers();
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		bChat.log(pdfFile.getVersion() + " (Jackr)" + " is enabled!" );
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void onDisable() {
 
 		if (heartbeatStartThread != null && heartbeatStartThread.isAlive())
 			heartbeatStartThread.stop();
 		if (heartbeatCheckThread != null && heartbeatCheckThread.isAlive())
 			heartbeatCheckThread.stop();
 		if (heartbeatThread != null && heartbeatThread.isAlive())
 			heartbeatThread.stop();
 		heartbeatStartThread = null;
 		heartbeatThread = null;
 		heartbeatCheckThread = null;
 		
 		if (apiRequestThread != null && apiRequestThread.isAlive())
 			apiRequestThread.interrupt();
 		if(serverLogout()) {
 		}
 		bChat.log(messagePluginName + " Alpha " + pdfFile.getVersion() + " (Jackr)" + " disabled!");
 	}
 
 	// onCommand
 	@Override
 	public boolean onCommand(CommandSender sender, Command c, String commandLabel, String[] args) {	    
 		return bCommandRouter.handleCommands(sender, c, commandLabel, args);	    
 	}
 
 	private boolean setupMessages() {
 		messagePluginName = "[" + pdfFile.getName() + "]";        
 		return true;
 	}
 
 	private boolean serverLogin() {
 		Server server = this.getServer();
 		String serverversion = server.getVersion();
 		String pluginversion = pdfFile.getVersion().replaceAll("\\.", "");
 		String slots = Integer.toString(server.getMaxPlayers());
 		String servername = bConfigManager.servername;
 		String owner = bConfigManager.owner;      
 
 		HashMap<String, String> url_items = new HashMap<String, String>();
 		url_items.put("exec", "start");
 		url_items.put("account", "server");
 		url_items.put("ip", "1.1.1.1");
 		url_items.put("lastip", "1.1.1.1");
 		url_items.put("port", serverport);
 		url_items.put("name", servername);
 		url_items.put("version", pluginversion);
 		url_items.put("bukkit", serverversion);
 		url_items.put("slots", slots);
 		if(!checkWhiteList() || !bConfigManager.usewhitelist) url_items.put("whitelist", "0");
 		else url_items.put("whitelist", "1");
 		if(onlinemode == false) url_items.put("offlinemode", "0");
 		else url_items.put("offlinemode", "1");
 		url_items.put("banborder", bConfigManager.banborder);
 		url_items.put("owner", owner);        
 
 		String users = "";
 		if(bConfigManager.usewhitelist) {
 			for(String player : bWhitelist.whitelistPlayers) {
 				users += player + ",";
 			}
 			if(users.length() > 0) users = users.substring(0, users.length() - 1);
 		}
 		url_items.put("whitelistusers", users);
 
 
 
 		JSONObject result = bConnector.hdl_com(url_items);
 		String ok = null;
 		int version = 0;
 		int protocol = 1000;
 		try {
 			ok = result.getString("response");
 			version = result.getInt("version");
 			protocol = result.getInt("protocol");
 
 		} catch (Exception e) {
 			if(glizer.D) e.printStackTrace();
 			bChat.log("Cant establish a connection to glizer-server!"/* glizer is now in offline mode"*/, 2);
 			//offline = true;
 			return false;
 		} 
 		if(ok.equalsIgnoreCase("ok")) {
 			bChat.log("Connected to glizer-server");
 			offline = false;
 			if(version > Integer.valueOf(pluginversion)) {
 				bChat.log("A new version of glizer is available!");
 				upToDate = false;
 			}
 			if(protocol > 2) {
 				bChat.log("Update glizer immediately!", 2);
 			}
 			return true;
 		}
 		else {
 			bChat.log("Failure! Wrong server configuration data sent", 2);
 			//offline = true;
 			return false;
 		}
 	}
 
 	private boolean serverLogout() {
 		Server server = this.getServer();     
 
 		HashMap<String, String> url_items = new HashMap<String, String>();
 		url_items.put("exec", "shutdown");
 		url_items.put("account", "server");
 		url_items.put("ip", "1.1.1.1");
 
 		JSONObject result = bConnector.hdl_com(url_items);
 		return true;
 		/*String ok;
         try {
             ok = result.getString("response");
         } catch (JSONException e) {
             if(glizer.D) e.printStackTrace();
             bChat.log("&6 Cant establish a connection to glizer-server! glizer is now in offline mode.", 2);
             offline = true;
             return false;
         } 
         bChat.log(":::" + ok + ":::");
         if(ok.equalsIgnoreCase("ok")) {
             bChat.log("Connected to glizer-server.");
             return true;
         }
         else {
             bChat.log("Failure! Wrong server configuration data sent.", 2);
             offline = true;
             return false;
         }*/
 	}
 
 	private boolean checkOnlineMode() {
 		
 		if(bConfigManager.bungiecord == true)
 		{
			if(this.getServer().getIp().equals("127.0.0.1"))
 			{
 				onlinemode = true;
 				return true;
 			}
 		}
 		
 		if(this.getServer().getOnlineMode())
 		{
 			onlinemode = true;
 			return true;
 		}
 		
 		Properties prop = new Properties();
 		String f = "server.properties";
 		try{
 			FileInputStream in = new FileInputStream(new File(f));
 			prop.load(in);
 			String work = prop.getProperty("online-mode");
 			if(work.equals("true")) {
 				onlinemode = true;
 				return true;
 			} else {
 				bChat.log(messagePluginName + " Online-mode false! glizer disabled.", 2);
 				this.getServer().getPluginManager().disablePlugin(this);
 				return false;
 			}
 		} catch(IOException e) {
 			if(glizer.D) e.printStackTrace();
 			bChat.log(messagePluginName + " Online-mode false! glizer disabled.", 2);
 			this.getServer().getPluginManager().disablePlugin(this);
 			return false;
 		}
 	}
 
 	private boolean checkWhiteList() {
 		Properties prop = new Properties();
 		String f = "server.properties";
 		try{
 			FileInputStream in = new FileInputStream(new File(f));
 			prop.load(in);
 			String work = prop.getProperty("white-list");
 			if(work.equalsIgnoreCase("true")) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch(IOException e) {
 			if(glizer.D) e.printStackTrace();
 			return true;
 		} 
 	}
 
 	public static boolean heartbeat(glizer glizer) {
 		heartbeatThread = new bTimer (glizer);	
 		heartbeatCheckThread = new HeartbeatChecker(glizer, heartbeatThread);
 
 		heartbeatStartThread = new Thread(){
 			@Override
 			public void run() {
 				long time = 0;
 				while(time < 300000)
 				{
 					long start = System.nanoTime();
 					try 
 					{
 						sleep(1000);
 					} catch (InterruptedException e)
 					{
 						e.printStackTrace();
 					}
 					long end = System.nanoTime();
 					float x = (float)(end-start);
 					time += x/1000000.f;
 				}
 				heartbeatThread.start();
 				heartbeatCheckThread.start();
 			}};
 			heartbeatStartThread.start();
 
 	
 			return true;
 	}
 }
 
 
 @SuppressWarnings("deprecation")
 class HeartbeatChecker extends Thread {
 	glizer pGlizer;
 	bTimer pHeartbeatThread;
 	public HeartbeatChecker(glizer xpglizer, bTimer pHeartbeatThread)
 	{
 		pGlizer = xpglizer;
 		this.pHeartbeatThread = pHeartbeatThread;
 	}
 	public void run(){
 		while (true)
 		{    				
 			try 
 			{
 				sleep(60000);
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if (pHeartbeatThread != null && !pHeartbeatThread.isAlive())
 			{	
 				bChat.log("Heartbeat cancelled, attempting to restart it.");
 				pHeartbeatThread.stop();
 				pHeartbeatThread = null;
 				pHeartbeatThread = new bTimer (pGlizer);
 				pHeartbeatThread.start();
 			}
 		}
 	}
 };
