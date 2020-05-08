 package com.selfequalsthis.grubsplugin;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public abstract class AbstractGrubsModule implements CommandExecutor {
 	
 	protected final Logger log = Logger.getLogger("Minecraft");
 	
 	protected JavaPlugin pluginRef = null;
 	protected String logPrefix = "";
	protected ArrayList<String> commandNames = new ArrayList<String>();
 	
 	public void enable(JavaPlugin plugin) {
 		this.pluginRef = plugin;
 		this.setupCommandExecutors();
 	}
 	
 	public void disable() {	}
 	
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		return false;
 	}
 	
 	
 	protected void setupCommandExecutors() {
 		
 		if (this.commandNames.size() == 0) {
 			return;
 		}
 		
 		this.log.info(logPrefix + "Initializing command handlers.");
 		for (String cmdName : this.commandNames) {
 			this.pluginRef.getCommand(cmdName).setExecutor(this);
 		}
 	}
 
 }
