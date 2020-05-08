 package me.smickles.HelloMinecraft;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * HelloMinecraft plugin for Bukkit
  *
  * @author smickles
  */
 
 public class HelloMinecraft extends JavaPlugin {
 	
	private final HelloMinecraftPlayerListener playerListener = new HelloMinecraftPlayerListener(this);
	
 	Logger log = Logger.getLogger("Minecraft");
 
 	public void onEnable(){ 
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
 				
 		log.info("Your plugin has been enabled.");
 	}
 	
 	public void onDisable(){ 
 		log.info("Your plugin has been enabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(commandLabel.equalsIgnoreCase("hello")){ //If the player typed /hello then say hello
 			
 			log.info("hello");
 			return true;
 		}
 		return false;
 	}
 } 
