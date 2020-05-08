 package com.gmail.favorlock.bungeeannouncer;
 
 import java.util.Timer;
 import java.util.logging.Level;
 
 import com.gmail.favorlock.bungeeannouncer.cmd.AnnounceAdd;
 import com.gmail.favorlock.bungeeannouncer.cmd.AnnounceList;
 import com.gmail.favorlock.bungeeannouncer.cmd.AnnounceRemove;
 import com.gmail.favorlock.bungeeannouncer.task.AnnounceTask;
 
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.plugin.Plugin;
 
 public class BungeeAnnouncer extends Plugin {
 	
 	private MainConfig config;
 	private AnnounceTask task;
 	private Timer timer = new Timer();
    private static BungeeAnnouncer plugin;
 	
 	public void onEnable() {
        plugin = this;
 		
 		try {
 			 config = new MainConfig(this); // create config
 			 config.init(); // load config file if it exists, create it if it doesn't
 		} catch(Exception ex) {
 			 ProxyServer.getInstance().getLogger().log(Level.SEVERE, "FAILED TO LOAD CONFIG!!!", ex);
 			 return;
 		}
 		
 		// Register Plugin Commands
 		registerCommands();
 		
 		// Schedule Announcement Timer
 		sendAnnouncement();
 		
 	}
 	
 	public void onDisable() {
 		timer.cancel();
 		task.cancel();
 	}

    public static BungeeAnnouncer getPlugin() {
        return plugin;
    }
 	
 	public void sendAnnouncement() {
 		task = new AnnounceTask(this);
 		timer.schedule(task, 0, this.getConfigStorage().settings_interval * 1000);
 	}
 	
 	public MainConfig getConfigStorage() {
 		return config;
 	}
 	
 	public void registerCommands() {
 		ProxyServer.getInstance().getPluginManager().registerCommand(this, new AnnounceAdd(this));
 		ProxyServer.getInstance().getPluginManager().registerCommand(this, new AnnounceRemove(this));
 		ProxyServer.getInstance().getPluginManager().registerCommand(this, new AnnounceList(this));
 	}
 	
 	public AnnounceTask getAnnounceTask() {
 		return task;
 	}
 
 }
