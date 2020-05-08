 package com.beecub.glizer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.beecub.util.bBackupManager;
 import com.beecub.util.bChat;
 import com.beecub.util.bConfigManager;
 import com.beecub.util.bConnector;
 import com.beecub.util.bWhitelist;
 
 import de.upsj.glizer.APIRequest.LoginRequest;
 import de.upsj.glizer.APIRequest.LogoutRequest;
 
 public class glizerPlayerListener implements Listener {
 	Map<String, String> playerIPs = new HashMap<String, String>();
 	glizer glz;
 	public glizerPlayerListener(glizer glizer) 
 	{
 		this.glz = glizer;
 	}
 
 	@EventHandler
 	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
 	{
 		playerIPs.put(event.getName().toLowerCase(), bConnector.getIPAddress(event.getAddress()));
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		if (event.getResult() == Result.ALLOWED)
 		{
 			Player player = event.getPlayer();
 			
 			if (bConfigManager.usewhitelist) {
 				if (bWhitelist.checkWhiteList(player.getName()))
 				{
 					if (glizer.D)
 						bChat.log("Player " + player.getName() + " is whitelisted");
 				}
 				else
 				{
 					event.disallow(Result.KICK_WHITELIST, bConfigManager.whitelist_joinmessage);
 					if (glizer.D)
 						bChat.log("Player " + player.getName() + " isn't whitelisted");
 				}
 			}
 			
 			if (event.getResult() == Result.ALLOWED && bBackupManager.checkBanList(player.getName()))
 			{
 				event.disallow(Result.KICK_BANNED, bConfigManager.ban_joinmessage);
 				if (glizer.D)
 					bChat.log("Player " + player.getName() + " is banned from this server. Kick", 2);
 			}
 		}
 		
 		String ip = null;
 		ip = playerIPs.remove(event.getPlayer().getName().toLowerCase());
 		if(ip == null)
 			ip = bConnector.getIPAddress(event.getAddress());
 		
 		if(bConfigManager.bungiecord == true)
 		{
			if(!ip.equals("127.0.0.1"))
 			{
 				event.disallow(Result.KICK_BANNED, bConfigManager.ipcheck_joinmessage);
 				bChat.log(com.beecub.glizer.glizer.messagePluginName + " Bungee Cord error.", 2);
 				glz.getServer().getPluginManager().disablePlugin(glz);
 			}
 		}
 		glizer.queue.add(new LoginRequest(event.getPlayer(), ip));
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// TODO Language
 		bChat.sendMessageToPlayer(event.getPlayer(), "&6This server is running &2glizer - the Minecraft Globalizer&6");
 		bChat.sendMessageToPlayer(event.getPlayer(), "&6[GLIZER]: &eYour statistics are globally saved and public visible");
 		bChat.sendMessageToPlayer(event.getPlayer(), "&6[GLIZER]: &eFor more informations see www.glizer.de");
 
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		glizer.queue.add(new LogoutRequest(event.getPlayer()));
 	}
 }
