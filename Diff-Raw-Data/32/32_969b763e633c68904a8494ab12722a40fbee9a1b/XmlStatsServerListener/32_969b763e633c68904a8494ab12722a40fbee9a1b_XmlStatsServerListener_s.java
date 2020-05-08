 /**
  * 
  */
 package de.sockenklaus.XmlStats;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 
 /**
  * @author socrates
  *
  */
 public class XmlStatsServerListener implements Listener {
 	private XmlStats plugin;
 	
 	public XmlStatsServerListener(XmlStats plugin){
 		this.plugin = plugin;
 	}
 	
 	public void onPluginDisable(PluginDisableEvent event){
 		/*
 		 * TODO
 		 * Not implemented yet!
 		 */
 		
 		/*Plugin iConomy = (Plugin)XmlStatsRegistry.get("iconomy");
 		Plugin Stats = (Plugin)XmlStatsRegistry.get("stats");
 		Plugin Achievements = (Plugin)XmlStatsRegistry.get("achievements");
 		
 		if (!XmlStats.checkAchievements()){
 			
 		}
 		if(!XmlStats.checkiConomy()){
 			
 		}
 		if(!XmlStats.checkStats()){
 			
 		}*/
 		
 	}
 	
 	@EventHandler
 	public void onPluginEnable(PluginEnableEvent event){
		if(this.identifyPlugin(event, "stats")){
 			this.plugin.hookStats();
 		}
 		if(this.identifyPlugin(event, "achievements")){
 			this.plugin.hookAchievements();
 		}
 		if(this.identifyPlugin(event, "register")){
 			this.plugin.hookRegister();
 		}
 	}
 	
 	private boolean identifyPlugin(PluginEnableEvent event, String name){
 		return event.getPlugin().getDescription().getName().equalsIgnoreCase(name);
 	}
 }
