 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Module.QuitReasonHandler;
 import com.censoredsoftware.Demigods.Engine.Object.DPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public class PlayerListener implements Listener
 {
 	private static QuitReasonHandler quitReasonFilter = new QuitReasonHandler();
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		// Define Variables
 		Player player = event.getPlayer();
 		DPlayer wrapper = DPlayer.Util.getPlayer(player);
 		DPlayer.Character character = wrapper.getCurrent();
 
 		// Set their lastlogintime
 		Long now = System.currentTimeMillis();
 		wrapper.setLastLoginTime(now);
 
 		// Set Displayname
 		if(character != null && wrapper.canUseCurrent())
 		{
 			String name = character.getName();
 			ChatColor color = character.getDeity().getInfo().getColor();
 			player.setDisplayName(color + name + ChatColor.RESET);
 			player.setPlayerListName(color + name + ChatColor.RESET);
 			event.getPlayer().setMaxHealth(character.getMaxHealth());
 			event.getPlayer().setHealth(character.getHealth());
 		}
 
 		if(Demigods.config.getSettingBoolean("misc.welcome_message"))
 		{
 			player.sendMessage(ChatColor.GRAY + "This server is running Demigods version: " + ChatColor.YELLOW + Demigods.plugin.getDescription().getVersion());
 			player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/dg" + ChatColor.GRAY + " for more information.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerMove(PlayerMoveEvent event)
 	{
 		// No-PVP Zones
 		onPlayerLineJump(event.getPlayer(), event.getTo(), event.getFrom(), Demigods.config.getSettingInt("zones.pvp_area_delay_time"));
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		// Define variables
 		final Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 
 		if(DPlayer.Util.isPraying(player)) DPlayer.Util.togglePraying(player, false);
 
 		// No-PVP Zones
 		if(event.getCause() == TeleportCause.ENDER_PEARL || DataUtility.hasKeyTemp(player.getName(), "teleport_ability"))
 		{
 			onPlayerLineJump(player, to, from, Demigods.config.getSettingInt("zones.pvp_area_delay_time"));
 		}
 		else if(ZoneUtility.enterZoneNoPVP(to, from))
 		{
 			DPlayer.Util.getPlayer(player).setPvP(false);
 			player.sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 		}
 		else if(ZoneUtility.exitZoneNoPVP(to, from))
 		{
 			DPlayer.Util.getPlayer(player).setPvP(true);
 			player.sendMessage(ChatColor.GRAY + "You can now PVP!");
 			return;
 		}
 	}
 
 	public void onPlayerLineJump(final OfflinePlayer player, Location to, Location from, int delayTime)
 	{
 		DPlayer dPlayer = DPlayer.Util.getPlayer(player);
 
 		// NullPointer Check
 		if(!player.isOnline() || dPlayer.getPvP()) return;
 
 		// No Spawn Line-Jumping
 		if(ZoneUtility.enterZoneNoPVP(to, from) && delayTime > 0)
 		{
			DPlayer.Util.getPlayer(player).setPvP(true);
 			if(DataUtility.hasKeyTemp(player.getName(), "teleport_ability")) DataUtility.removeTemp(player.getName(), "teleport_ability");
 
 			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					if(!player.isOnline()) return;
 					DPlayer.Util.getPlayer(player).setPvP(false);
 					if(ZoneUtility.zoneNoPVP(player.getPlayer().getLocation())) player.getPlayer().sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 				}
 			}, (delayTime * 20));
 		}
 
 		// Let players know where they can PVP
 		if(!dPlayer.getPvP() && ZoneUtility.exitZoneNoPVP(to, from)) player.getPlayer().sendMessage(ChatColor.GRAY + "You can now PVP!");
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String name = event.getPlayer().getName();
 		String message = ChatColor.YELLOW + name + " has left the game.";
 		switch(quitReasonFilter.getLatestQuitReason())
 		{
 			case GENERIC_REASON:
 				message = ChatColor.YELLOW + name + " has either quit or crashed.";
 				break;
 			case SPAM:
 				message = ChatColor.YELLOW + name + " has disconnected due to spamming.";
 				break;
 			case END_OF_STREAM:
 				message = ChatColor.YELLOW + name + " has lost connection.";
 				break;
 			case OVERFLOW:
 				message = ChatColor.YELLOW + name + " has disconnected due to overload.";
 				break;
 			case QUITTING:
 				message = ChatColor.YELLOW + name + " has quit.";
 				break;
 			case TIMEOUT:
 				message = ChatColor.YELLOW + name + " has disconnected due to timeout.";
 				break;
 		}
 		event.setQuitMessage(message);
 		DPlayer.Character loggingOff = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 		if(loggingOff != null) loggingOff.setLocation(event.getPlayer().getLocation());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerRespawn(PlayerRespawnEvent event) // TODO Is this working?
 	{
 		DPlayer wrapper = DPlayer.Util.getPlayer(event.getPlayer());
 		if(wrapper.getCurrent() != null)
 		{
 			double maxhealth = wrapper.getCurrent().getMaxHealth();
 			event.getPlayer().setMaxHealth(maxhealth);
 			event.getPlayer().setHealth(maxhealth);
 		}
 	}
 }
