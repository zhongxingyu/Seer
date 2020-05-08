 package com.github.imaginarydevelopment.bMcPlugin;
 
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class bMcPlugin extends JavaPlugin {
 	
 	Logger log;
 	bMcPluginCommandExecutor myExecutor;
 	
 	Logger get_Logger(){
 		return this.log;
 	}
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event){
 		Player p=event.getPlayer();
 		if(p==null || !(p instanceof Player)){
 			return;
 		}
 		String msg=event.getMessage();
 		String kickMsg=null;
 		if(msg.contains("!!!")){
 			int count=StringUtils.countMatches(msg, "!");
 			String padding=String.format("%"+count+"s","").replace(' ','!');
 			kickMsg="enjoy your kick"+padding;
 		}
 		if( msg.matches("[A-Z]{5,}")){
 			kickMsg="caps";
 		}
 		
 		if(kickMsg!=null){
 			p.kickPlayer(kickMsg);
 		}
 	}
 	public void onEnable(){
 		this.log=getLogger();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
 		log.info("bMcPlugin has been enabled.");
 		myExecutor=new bMcPluginCommandExecutor(this);
 		getCommand("bMcPlugin").setExecutor(myExecutor);
 	}
 	
 	public void onDisable(){
 		log.info("bMcPlugin has been disabled.");
 	}
 	
 }
