 package nl.crafters.delayedstop;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import javax.swing.Timer;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin; 
 import org.bukkit.util.config.Configuration;
 import com.nijiko.permissions.*;
 import com.nijikokun.bukkit.Permissions.*;
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 
 public class DelayedStop extends JavaPlugin{
 	int repeatingTask = 0;
 	public String CHATPREFIX = ChatColor.RED + "[NLC]" + ChatColor.AQUA;
 	Configuration config;
 	public  Calendar timeStop;
 	private final static char DEG = '\u00A7';
 	private static PermissionHandler Permissions=null;
 	private WorldsHolder wh = null;
 	private int pSystem = 0;
 	private boolean shuttingDown = false;
 	String reason = "";
 	boolean inPause = false; 
 	Timer timer;
 	
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		Tools.AddLog("Enabling Delayed Stop v" + pdfFile.getVersion());
 		getCommand("dstop").setExecutor(new DelayedStopCommand(this));
 		
 		CheckConfig();
 		loadConfig();
 		setupPermissions();
 		Tools.AddLog("version "  + pdfFile.getVersion() + " enabled");
 	}
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		Tools.AddLog("version "  + pdfFile.getVersion() + " disabled");
 		pdfFile = null;
 	}
 
 	ActionListener taction = new ActionListener ()
     {
       public void actionPerformed (ActionEvent event)
       {
     	  TimeTick();
       }
     };	
     
 	public String getTimeLeft() {
 		Long timeLeft;
 		try {
 			timeLeft = (timeStop.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 1000;
 		} catch (Exception e) {
			return "";
 		}
 		Long Minutes = (timeLeft / 60);
 		Long Seconds = (timeLeft - (Minutes*60));
 		String oneSecond = config.getString("broadcasttext.second-text");
 		String oneMinute = config.getString("broadcasttext.minute-text");
 		String moreSeconds = config.getString("broadcasttext.seconds-text",oneSecond);
 		String moreMinutes = config.getString("broadcasttext.minutes-text",oneMinute);
 		String andText  = config.getString("broadcasttext.and-text","and");
 		if (Minutes==0) {
 			if (Seconds>1) 
 				return Seconds +  " " + moreSeconds;
 			else
 				return Seconds +  " " + oneSecond;
 		}
 		else {
 			if (Seconds==0) {
 				if (Minutes>1)
 					return Minutes + " " + moreMinutes;
 				else
 					return Minutes + " " + oneMinute;
 			}
 			else {
 				if (Minutes>1 && Seconds > 1) {
 						return Minutes + " " + moreMinutes + " " + andText + " " + Seconds + "  " + moreSeconds;	
 				}
 				else if (Minutes==1 && Seconds>1) {
 					return Minutes + " " + oneMinute + " " + andText + " " + Seconds + "  " + moreSeconds;	
 				}
 				else { 
 					return Minutes + " " + oneMinute + " " + andText + " " + Seconds + "  " + oneSecond;	
 				}
 			}
 		}
 	}
 	private void TimeTick() {
 		if (inPause) {
 			timeStop.add(Calendar.SECOND,1);
 			return;
 		}
 		Long timeLeft = (timeStop.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 1000;
 		Long Minutes = (timeLeft / 60);
 		Long Seconds = (timeLeft - (Minutes*60));
 		int numsecs = 0;
 		String strNotifiers[] = config.getString("notification.notify-at","10m,5m,1m,30s,10s,5s").split(",");
 		for (String s: strNotifiers) {
 			if (Tools.isInteger(s)) { // Just seconds
 				numsecs = Integer.parseInt(s);
 			}
 			else if (s.contains("s")) { // Just seconds
 				numsecs = (Integer.parseInt(s.replaceAll("s","" )));
 			}
 			else if (s.contains("m")) { // just minutes
 				numsecs = (Integer.parseInt(s.replaceAll("m","" )) * 60);
 			}
 			if (timeLeft==numsecs) {
 				this.getServer().broadcastMessage(CHATPREFIX + " " + 
 						  getMessage("broadcasttext.time-left-message","") + reason);
 				Tools.AddLog(getMessage("broadcasttext.time-left-message","") + reason);
 			}
 		}
 		if (Minutes<=0 && Seconds<=0)  {
 			shutDown();
 		}
 		
 
 	}
 	private void shutDown() {
 		if (!shuttingDown) {
 			shuttingDown = true;
 			String msg = getMessage("broadcasttext.server-down-message","") + reason;
 
 			Tools.AddLog("Kicking " + this.getServer().getOnlinePlayers().length + " players");
 			for(Player p : this.getServer().getOnlinePlayers())
 			{
 	            try 
 	            {
 	            	p.kickPlayer(msg);
 	            }
 	            catch (Exception e) {}
 			}
             Tools.AddLog("Shutting down");
 			timer.stop();
 			repeatingTask=0;
 			((CraftServer) this.getServer()).getServer().a();
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
 		            Tools.AddLog("Default configuration file written: " + name);
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
 	private void loadConfig() {
     	
     	config = this.getConfiguration();
     	config.load();
 		CHATPREFIX = getColor("labelsandcolors.chat-prefix-color") + 
 		             getMessage("labelsandcolors.chat-prefix","") +
 		             getColor("labelsandcolors.chat-message-color");
 		//JailTime = config.getInt("settings.jail-time",-1);
     }	
 	String getMessage(String configMessage, String defaultMessage) {
 		config.load();
 		String t = "";
 		if (defaultMessage.equalsIgnoreCase(""))
 			t = config.getString(configMessage);
 		else
 			t = config.getString(configMessage,defaultMessage);
 			
 		if (t.indexOf("@")==-1) {
 			return t;
 		}
 		t = t.replaceAll("@time-left@", getColor("labelsandcolors.chat-highlighted-color") + getTimeLeft() + getColor("labelsandcolors.chat-message-color")  );
 		return t;
 	}
 	private String getColor(String configColor) 
 	{
 		String col =config.getString(configColor); 
 		String out = "";
 		if (col.length()==2) {
 			out = DEG + col.substring(1);
 			return out;
 		}
 		return col;
 	}	
 	private void setupPermissions() {
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
 			Tools.AddLog("GroupManager system found");
 		}
 		else  // Then search for permissions
 		{
 			p = this.getServer().getPluginManager().getPlugin("Permissions");
 			if(Permissions == null) {
 			    if(p!= null) {
 			    	Permissions = ((Permissions)p ).getHandler();
 			    	pSystem = 2;
 			    	Tools.AddLog("Permission system found");
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
