 package com.vcazan.allaboard;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AllAboard extends JavaPlugin {
 	
     private final CartListener cartListener = new CartListener(this);
     private final CartLeave cartLeave = new CartLeave(this);
 
     Logger log = Logger.getLogger("Minecraft");
     Boolean enabled = true;
     
     public void onEnable(){
		log.info("AllAboard v0.03 has loaded.");
 
     	PluginManager pm = this.getServer().getPluginManager();
 
     		pm.registerEvent(Event.Type.VEHICLE_EXIT, cartLeave, Event.Priority.Normal,this);
     		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, cartListener, Event.Priority.Normal,this);
 
     }
     
     public void onDisable(){
 		log.info("AllAboard has unloaded.");
 
 	}
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
     	 
     	 if(cmd.getName().equalsIgnoreCase("autoboard")){ // If the player typed /basic then do the following...
     	   if (sender.isOp()){
     		   if (enabled){
     			   sender.sendMessage("AllAboard is now disabed");
     			   enabled = false;
     		   }else{
     			   sender.sendMessage("AllAboard is now enabled");
     			   enabled = true;
 
     		   }
     	   }
     	   return true;
     	 } //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
     	 return false; }
     
 }
