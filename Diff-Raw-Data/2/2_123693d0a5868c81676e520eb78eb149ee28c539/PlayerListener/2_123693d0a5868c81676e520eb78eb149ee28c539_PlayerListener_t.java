 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBattle;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public class PlayerListener implements Listener
 {
 	public static Boolean filterCheckGeneric = false;
 	public static Boolean filterCheckStream = false;
 	public static Boolean filterCheckOverflow = false;
 	public static Boolean filterCheckQuitting = false;
 	public static Boolean filterCheckTimeout = false;
 
 	public PlayerListener()
 	{
 		Demigods.message.getLog().setFilter(new DisconnectReason());
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		// Define Variables
 		Player player = event.getPlayer();
 		TrackedPlayer tracked = TrackedPlayer.getTracked(player);
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 		// Set their lastlogintime
 		Long now = System.currentTimeMillis();
 		tracked.setLastLoginTime(now);
 
 		// Set Displayname
 		if(character != null)
 		{
 			String name = character.getName();
 			ChatColor color = character.getDeity().getInfo().getColor();
 			player.setDisplayName(color + name + ChatColor.WHITE);
 			player.setPlayerListName(color + name + ChatColor.WHITE);
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
 		// Define variables
 		final Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 		int delayTime = Demigods.config.getSettingInt("pvp_area_delay_time");
 
 		// No-PVP Zones
 		onPlayerLineJump(player, to, from, delayTime);
 
 		// Player Hold
		if(DemigodsData.hasKeyTemp(player.getName(), "player_hold") && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()))
 		{
 			event.setCancelled(true);
 			player.teleport(from);
 			DemigodsData.saveTemp(player.getName(), "player_held", true);
 		}
 
 		// Handle prayer disable
 		if(TrackedPlayer.isPraying(player) && to.distance((Location) DemigodsData.getValueTemp(player.getName(), "praying_location")) >= Demigods.config.getSettingInt("zones.prayer_radius")) TrackedPlayer.togglePraying(player, false);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		// Define variables
 		final Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 		int delayTime = Demigods.config.getSettingInt("zones.pvp_area_delay_time");
 
 		if(TrackedPlayer.isPraying(player)) TrackedPlayer.togglePrayingSilent(player, false);
 
 		// No-PVP Zones
 		if(event.getCause() == TeleportCause.ENDER_PEARL || DemigodsData.hasKeyTemp(player.getName(), "teleport_ability"))
 		{
 			onPlayerLineJump(player, to, from, delayTime);
 		}
 		else if(ZoneUtility.enterZoneNoPVP(to, from))
 		{
 			DemigodsData.removeTemp(player.getName(), "was_PVP");
 			player.sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 		}
 		else if(ZoneUtility.exitZoneNoPVP(to, from))
 		{
 			player.sendMessage(ChatColor.GRAY + "You can now PVP!");
 			return;
 		}
 
 		// Player Hold
 		if(DemigodsData.hasKeyTemp(player.getName(), "player_held")) DemigodsData.removeTemp(player.getName(), "player_held");
 		else if(DemigodsData.hasKeyTemp(player.getName(), "player_hold")) event.setCancelled(true);
 	}
 
 	public void onPlayerLineJump(final Player player, Location to, Location from, int delayTime)
 	{
 		// NullPointer Check
 		if(to == null || from == null) return;
 
 		if(DemigodsData.hasKeyTemp(player.getName(), "was_PVP")) return;
 
 		// No Spawn Line-Jumping
 		if(ZoneUtility.enterZoneNoPVP(to, from) && delayTime > 0)
 		{
 			DemigodsData.saveTemp(player.getName(), "was_PVP", true);
 			if(DemigodsData.hasKeyTemp(player.getName(), "teleport_ability")) DemigodsData.removeTemp(player.getName(), "teleport_ability");
 
 			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					DemigodsData.removeTemp(player.getName(), "was_PVP");
 					if(ZoneUtility.zoneNoPVP(player.getLocation())) player.sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 				}
 			}, (delayTime * 20));
 		}
 
 		// Let players know where they can PVP
 		if(!DemigodsData.hasKeyTemp(player.getName(), "was_PVP"))
 		{
 			if(ZoneUtility.exitZoneNoPVP(to, from)) player.sendMessage(ChatColor.GRAY + "You can now PVP!");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String name = event.getPlayer().getName();
 		if(filterCheckGeneric)
 		{
 			String message = ChatColor.YELLOW + name + " has either lost connection or crashed.";
 			event.setQuitMessage(message);
 		}
 		else if(filterCheckStream)
 		{
 			String message = ChatColor.YELLOW + name + " has lost connection.";
 			event.setQuitMessage(message);
 		}
 		else if(filterCheckOverflow)
 		{
 			String message = ChatColor.YELLOW + name + " has disconnected due to overload.";
 			event.setQuitMessage(message);
 		}
 		else if(filterCheckQuitting)
 		{
 			if(ZoneUtility.canTarget(event.getPlayer()) && TrackedBattle.isInAnyActiveBattle(TrackedPlayer.getTracked(event.getPlayer()).getCurrent()))
 			{
 				String message = ChatColor.YELLOW + name + " has PvP Logged."; // TODO
 				event.setQuitMessage(message);
 				return;
 			}
 			String message = ChatColor.YELLOW + name + " has left the game.";
 			event.setQuitMessage(message);
 		}
 		else if(filterCheckTimeout)
 		{
 			String message = ChatColor.YELLOW + name + " has disconnected due to timeout.";
 			event.setQuitMessage(message);
 		}
 	}
 
 	public static class DisconnectReason implements Filter
 	{
 		@Override
 		public boolean isLoggable(LogRecord arg0)
 		{
 			if(arg0.getMessage().toLowerCase().contains("disconnect"))
 			{
 				filterCheckGeneric = false;
 				filterCheckStream = false;
 				filterCheckOverflow = false;
 				filterCheckTimeout = false;
 
 				if(arg0.getMessage().toLowerCase().contains("genericreason"))
 				{
 					filterCheckGeneric = true;
 					return true;
 				}
 				if(arg0.getMessage().toLowerCase().contains("endofstream"))
 				{
 					filterCheckStream = true;
 					return true;
 				}
 				if(arg0.getMessage().toLowerCase().contains("overflow"))
 				{
 					filterCheckOverflow = true;
 					return true;
 				}
 				if(arg0.getMessage().toLowerCase().contains("timeout"))
 				{
 					filterCheckTimeout = true;
 					return true;
 				}
 				if(arg0.getMessage().toLowerCase().contains("quitting"))
 				{
 					filterCheckQuitting = true;
 					return true;
 				}
 				return true;
 			}
 			return true;
 		}
 	}
 }
