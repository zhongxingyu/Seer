 package nl.crafters.delayedstop;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin; 
 import org.bukkit.util.config.Configuration;
 import com.nijiko.permissions.*;
 import com.nijikokun.bukkit.Permissions.*;
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 
 public class DelayedStop extends JavaPlugin{
 	int repeatingTask = 0;
 	public String CHATPREFIX = ChatColor.RED + "[NLC]" + ChatColor.AQUA;
 	static String maindirectory = "plugins/ChatCensor/";
 	static File configFile = new File(maindirectory + "Config.yml");
 	public Configuration config;
 	public Calendar timeStop;
 	public String lastMessage = ""; 
 	public final static char DEG = '\u00A7';
 	public static PermissionHandler Permissions=null;
 	public WorldsHolder wh = null;
 	public int pSystem = 0;
 	public boolean shuttingDown = false;
 	
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		AddLog("Enabling Delayed Stop v" + pdfFile.getVersion());
 		getCommand("dstop").setExecutor(new CommandExecutor() {
             public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
             	execCommand(sender, command, label, args);
         	    return true;
             }
         });
 		CheckConfig();
 		loadConfig();
 		setupPermissions();
 		AddLog("version "  + pdfFile.getVersion() + " enabled");
 	}
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		AddLog("version "  + pdfFile.getVersion() + " disabled");
 		pdfFile = null;
 	}
 	public void execCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		String cmd = command.getName();
 		String action = "";
 		Player player = null;
 		int delay = 0;
 		if (!cmd.equalsIgnoreCase("dstop"))
 			return;
 		if (sender instanceof Player){
 	     	player = (Player)sender;
 	    }
 	    else {
 	    	player = null;
 	    }
 		try {
 			action = args[0].toLowerCase();
 		} catch (Exception e) {
 			action = "";
 		}
 		if (isInteger(action)) {
 			delay = Integer.parseInt(action);
 			action = "start";
 		}
 		if ( action.equalsIgnoreCase("")&& (checkP(player,"delayedstop.cancel") || checkP(player,"delayedstop.start"))  ) {
 			Log(player,CHATPREFIX + " Usage: /dstop <minutes> or /dstop cancel");
 			return;
 			
 		}
 		else if ( action.equalsIgnoreCase("debug")&& checkP(player,"delayedstop.start")  ) {
 			try {
 				Long timeLeft = (timeStop.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 1000;
 				Log(player,"Time left :" + timeLeft);
 			} catch (Exception e) {}
 			Log(player,"processid:" + repeatingTask);
 		}
 		else if ( ( action.equalsIgnoreCase("cancel") || action.equalsIgnoreCase("off")) && checkP(player,"delayedstop.cancel")  ) {
 			if (repeatingTask==0) {
 				Log(player,CHATPREFIX + " No delayed stop in progress");
 				return;
 			}
 			AddLog("Cancelling task " + repeatingTask);
 			this.getServer().getScheduler().cancelTask(repeatingTask);
 			this.getServer().broadcastMessage(CHATPREFIX + " " + getMessage("broadcasttext.restart-cancelled-message"));
 			repeatingTask=0;
 			delay=0;
 		}
 		else if (action.equalsIgnoreCase("force") && checkP(player,"delayedstop.start")) {
 			AddLog("Saving player data");
 			this.getServer().savePlayers();
             for(org.bukkit.World w : this.getServer().getWorlds())
             {
             	AddLog("Saving world data" + w.getName());
                 w.save();
             }
             AddLog("Kicking " + this.getServer().getOnlinePlayers().length + " players");
             for(org.bukkit.entity.Player p : this.getServer().getOnlinePlayers())
             {
                 p.kickPlayer("Server is shutting down...");
             }
             
             AddLog("Disabling plugins");
             for (Plugin p : this.getServer().getPluginManager().getPlugins()) {
             	if (!p.equals(this)) {
             		p.onDisable();	
             	}
             }
             AddLog("Stopping server...");
             System.exit(0);				
 		}
 		else if (action.equalsIgnoreCase("save") && checkP(player,"delayedstop.start")) {
 			Log(player,"Saving player data");
 			this.getServer().savePlayers();
             for(org.bukkit.World w : this.getServer().getWorlds())
             {
             	Log(player,"Saving world data for world.." + w.getName());
                 w.save();
                 Log(player,"Done saving world  " + w.getName());
             }
             Log(player,"Done saving player data");
 		}	
 		else if (action.equalsIgnoreCase("start") && checkP(player,"delayedstop.start")) {
 			if (delay==0) {
 				return;
 			}
 			if (repeatingTask!=0) 
 			{
 				Log(player,CHATPREFIX + " Delayed stop already in progress!");
 				return;
 			}
 			timeStop = Calendar.getInstance();
 			timeStop.add(Calendar.SECOND, delay);
 			this.getServer().broadcastMessage(CHATPREFIX + " " + getMessage("broadcasttext.time-left-message"));
 			repeatingTask = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() 
 			{
 				@Override
 				public void run() {
 					TimeTick();
 				}
 			}, 0, 1*20);
 		}
 		
 	}
 	public String getTimeLeft() {
 		Long timeLeft = (timeStop.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 1000;
 		Long Minutes = (timeLeft / 60);
 		Long Seconds = (timeLeft - (Minutes*60));
 		if (Minutes==0) {
 			return Seconds +  " " + config.getString("broadcasttext.second-text");
 		}
 		else {
 			if (Seconds==0) {
 				return Minutes + " " + config.getString("broadcasttext.minute-text");
 			}
 			else {
 				return Minutes + " " + config.getString("broadcasttext.minute-text") + " and " + Seconds + "  " + config.getString("broadcasttext.second-text",null);
 			}
 		}
 	}
 	public void TimeTick() {
 		String newMessage = "";
 		Long timeLeft = (timeStop.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 1000;
 		Long Minutes = (timeLeft / 60);
 		Long Seconds = (timeLeft - (Minutes*60));
 		if (Minutes > 0) {
 			if (Seconds==0) {
 				newMessage = ChatColor.RED + " " + Minutes + " minutes remaining";
 			}
 		}
 		else if ( (Seconds <= 5) && Minutes==0)  {
 			newMessage = ChatColor.RED + " " + Seconds + " seconds remaining";
 		}
 		else if ((Seconds % 10)==0) {
 			newMessage = ChatColor.RED + " " + Seconds + " seconds remaining";
 		}
 		if (Minutes<=0 && Seconds<=0)  {
 			shutDown();
 		}
 		else {
 			if (newMessage.equalsIgnoreCase(""))
 				return;
 			if (!newMessage.equalsIgnoreCase(lastMessage)) {
 				this.getServer().broadcastMessage(CHATPREFIX + " " + getMessage("broadcasttext.time-left-message"));
 				AddLog(getMessage("broadcasttext.time-left-message"));
 			}
 			lastMessage=newMessage;
 		}
 	}
 	private void shutDown() {
 		if (!shuttingDown) {
 			shuttingDown = true;
			String msg = getMessage("broadcasttext.server-down-message");

			AddLog("Kicking " + this.getServer().getOnlinePlayers().length + " players");
			for(Player p : this.getServer().getOnlinePlayers())
			{
	            try 
	            {
	            	p.kickPlayer(msg);
	            }
	            catch (Exception e) {}
			}
             AddLog("Shutting down");
 			((CraftServer) this.getServer()).getServer().a();
 			this.getServer().getScheduler().cancelTask(repeatingTask);
 			repeatingTask=0;
 		}
 	}
 	public boolean isInteger( String input ) {  
 		try {  
 			Integer.parseInt( input );  
 			return true;  
 		}  
 		catch(Exception e) {  
 			return false;  
 		}  
 	}  	
     public void AddLog(String message) {
     	Logger.getLogger("Minecraft").info("[DelayedStop] " + message);
     }
 	public void Log(Player p, String message) {
 		if (p==null) {
 			AddLog(message);
 		}
 		else {
 			p.sendMessage(message);
 		}
 	}
 
 	private void CheckConfig() {
 		getDataFolder().mkdirs();
 		String name = "config.yml";
 		File actual = new File(getDataFolder(),name);
 		if (!actual.exists()) {
 		    InputStream input =this.getClass().getResourceAsStream("/config.yml");
 		    if (input != null) {
 		        FileOutputStream output = null;
 		        try {
 		            output = new FileOutputStream(actual);
 		            byte[] buf = new byte[8192];
 		            int length = 0;
 		            while ((length = input.read(buf)) > 0) {
 		                output.write(buf, 0, length);
 		            }
 		            AddLog("Default configuration file written: " + name);
 		        } catch (IOException e) {
 		            e.printStackTrace();
 		        } finally {
 		            try {
 		                if (input != null)
 		                    input.close();
 		            } catch (IOException e) {}
 	
 		            try {
 		                if (output != null)
 		                    output.close();
 		            } catch (IOException e) {}
 		        }
 		    }            
 		}
 	}
     public void loadConfig() {
     	
     	config = this.getConfiguration();
     	config.load();
 		CHATPREFIX = getColor("labelsandcolors.chat-prefix-color") + 
 		             getMessage("labelsandcolors.chat-prefix") +
 		             getColor("labelsandcolors.chat-message-color");
 		//JailTime = config.getInt("settings.jail-time",-1);
     }	
 	public String getMessage(String configMessage) {
 		config.load();
 		String t = config.getString(configMessage);
 		if (t.indexOf("@")==-1) {
 			return t;
 		}
 		t = t.replaceAll("@time-left@", getColor("labelsandcolors.chat-highlighted-color") + getTimeLeft() + getColor("labelsandcolors.chat-message-color")  );
 		return t;
 	}
 	public String getColor(String configColor) 
 	{
 		String col =config.getString(configColor); 
 		String out = "";
 		if (col.length()==2) {
 			out = DEG + col.substring(1);
 			return out;
 		}
 		return col;
 	}	
 	void setupPermissions() {
 		// first search for groupmanager
 		Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
 		pSystem =0;
 		if (p!=null) {
 			if (!this.getServer().getPluginManager().isPluginEnabled("GroupManager")) {
 				this.getServer().getPluginManager().enablePlugin(p);
 			}
 			GroupManager gm = (GroupManager) p;
 			wh = gm.getWorldsHolder();
 			pSystem = 1;
 			AddLog("GroupManager system found");
 		}
 		else  // Then search for permissions
 		{
 			p = this.getServer().getPluginManager().getPlugin("Permissions");
 			if(Permissions == null) {
 			    if(p!= null) {
 			    	Permissions = ((Permissions)p ).getHandler();
 			    	pSystem = 2;
 			    	AddLog("Permission system found");
 			    } 
 			}
 		}
 	}	
 	// Check permissions simplified
 	public boolean checkP(Player p, String perm) {
 		if (p==null) {
 			return true;
 		}
 		else {
 			if (pSystem==0) {
 				return (p.isOp());
 			}
 			else if (pSystem==1) {
 				 return wh.getWorldPermissions(p).has(p,perm);
 			}
 			else if (pSystem==2) {
 				if (Permissions.has(p,perm)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 }
