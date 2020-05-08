 package com.selfequalsthis.grubsplugin;
 
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public abstract class AbstractGrubsModule implements CommandExecutor {
 	
 	protected final Logger log = Logger.getLogger("Minecraft");
 	
 	protected JavaPlugin pluginRef = null;
 	protected String logPrefix = "";
 	
 	public void enable() { }
 	public void disable() {	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		return false;
 	}
 	
 	protected void registerCommand(String cmdName) {
 		this.log.info(this.logPrefix + "Registering command '" + cmdName + "'");
		PluginCommand commandObj = this.pluginRef.getCommand(cmdName);
		
		if (commandObj == null) {
			log.info("Could not find command '" + cmdName + "' in plugin.yml");
			return;
		}
		
		commandObj.setExecutor(this);
 	}
 	
 	protected void registerEvent(Type type, Listener listener, Priority priority) {
 		this.log.info(this.logPrefix + "Listening to '" + type.toString() + "'");
 		this.pluginRef.getServer().getPluginManager().registerEvent(type, listener, priority, this.pluginRef);
 	}
 
 }
