 package com.ensifera.animosity.craftirc;
 
 import java.util.logging.Logger;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
import to.joe.J2Plugin;
 
 public class CraftIRC  extends JavaPlugin {
 
	private J2Plugin j2;
 	private boolean nope=false;
 	
 	@Override
 	public void onDisable() {
 		
 	}
 
 	@Override
 	public void onEnable() {
		J2Plugin p = null;
 		Plugin test = this.getServer().getPluginManager().getPlugin("j2Plugin");
		if(test != null && test instanceof J2Plugin) {
			p = (J2Plugin)test;
 		}
 		if(p == null) {
 			Logger.getLogger("Minecraft").warning("Failed to find J2Plugin. Oh dear.");
 			nope=true;
 		}
 		
 		j2 = p;
 		Logger.getLogger("Minecraft").info("FakeCraftIRC is go. Found version "+j2.getDescription().getVersion());
 	}
 	
 	public void sendMessageToTag(String message, String tag) {
 		if(j2.debug){
 			j2.log.info("Fake: Got message, tag \""+tag+"\"");
 		}
 		if(!nope){
 			/*if(!message.startsWith("[WARNING]")){
 				return;
 			}*/
 			if(message.startsWith("[WARNING] NC: Moving summary")||message.startsWith("[SEVERE] NC: Moving summary")){
 				return;
 			}
 			j2.craftIRC_sendMessageToTag(message, tag);
 		}
 	}
 
 	
 }
