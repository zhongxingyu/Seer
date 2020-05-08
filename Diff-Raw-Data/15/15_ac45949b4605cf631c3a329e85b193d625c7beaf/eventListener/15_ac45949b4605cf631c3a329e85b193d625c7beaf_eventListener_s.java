 package com.rushteamc.RTMCPlugin.sync;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.*;
 
 import com.rushteamc.RTMCPlugin.sync.message.*;
/*
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
*/
 public class eventListener implements Listener
 {
	//private PermissionManager permissions;
 	private FileConfiguration config;
 	syncMain syncmain;
 	
 	public eventListener(syncMain syncmain, FileConfiguration config)
 	{
 		this.syncmain = syncmain;
 		this.config = config;
		//permissions = PermissionsEx.getPermissionManager();
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onAsyncPlayerChatFirst(AsyncPlayerChatEvent event)
 	{
		/*
 		Player player = event.getPlayer();
 		String worldName = player.getWorld().getName();
 		PermissionUser user = permissions.getUser(player);
 		PermissionGroup[] userGroups = user.getGroups(worldName);
 		PermissionGroup userGroup = userGroups[0];
 		int maxRank = userGroups[0].getRank();
 		for(PermissionGroup group : userGroups )
 		{
 			if(group.getRank() > maxRank)
 				userGroup = group;
 		}
 		if( config.isString("chat.worlds." + worldName ) )
 			worldName = config.getString("chat.worlds." + worldName ).replace('&', ChatColor.COLOR_CHAR);
 		event.setFormat( config.getString("chat.format").replace('&', ChatColor.COLOR_CHAR).replace("{WORLD}", worldName + ChatColor.RESET ).replace("{RANK}", userGroup.getName() + ChatColor.RESET).replace("{PLAYERNAME}", "%s" + ChatColor.RESET).replace("{MESSAGE}", "%s" + ChatColor.RESET) );
		*/
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onAsyncPlayerChatSeccond(AsyncPlayerChatEvent event)
 	{
 		publicChat message = new publicChat(event.getFormat(),event.getPlayer().getDisplayName(),event.getMessage());
 		syncmain.sendMessage(message);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		playerJoin message = new playerJoin(event.getPlayer().getDisplayName(),event.getJoinMessage());
 		syncmain.sendMessage(message);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		playerLeave message = new playerLeave(event.getPlayer().getDisplayName(),event.getQuitMessage());
 		syncmain.sendMessage(message);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerKick(PlayerKickEvent event)
 	{
 		playerKick message = new playerKick(event.getPlayer().getDisplayName(),event.getLeaveMessage());
 		syncmain.sendMessage(message);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event)
 	{
 		playerDeath message = new playerDeath(event.getEntity().getDisplayName(),event.getDeathMessage());
 		syncmain.sendMessage(message);
 	}
 	
 }
