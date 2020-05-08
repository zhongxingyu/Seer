 package com.censoredsoftware.Demigods.Engine.Listener;
 
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
 import com.censoredsoftware.Demigods.Engine.Object.DemigodsPlayer;
 import com.censoredsoftware.Demigods.Engine.Object.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 import com.censoredsoftware.Modules.QuitReasonFilter;
 
 public class PlayerListener implements Listener
 {
 	private static QuitReasonFilter quitReasonFilter = new QuitReasonFilter();
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		// Define Variables
 		Player player = event.getPlayer();
 		DemigodsPlayer tracked = DemigodsPlayer.getPlayer(player);
 		PlayerCharacter character = DemigodsPlayer.getPlayer(player).getCurrent();
 
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
 		if(DataUtility.hasKeyTemp(player.getName(), "player_hold") && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()))
 		{
 			event.setCancelled(true);
 			player.teleport(from);
 			DataUtility.saveTemp(player.getName(), "player_held", true);
 		}
 
 		// Handle prayer disable
 		if(DemigodsPlayer.isPraying(player) && to.distance((Location) DataUtility.getValueTemp(player.getName(), "praying_location")) >= Demigods.config.getSettingInt("zones.prayer_radius")) DemigodsPlayer.togglePraying(player, false);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		// Define variables
 		final Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 		int delayTime = Demigods.config.getSettingInt("zones.pvp_area_delay_time");
 
 		if(DemigodsPlayer.isPraying(player)) DemigodsPlayer.togglePraying(player, false);
 
 		// No-PVP Zones
 		if(event.getCause() == TeleportCause.ENDER_PEARL || DataUtility.hasKeyTemp(player.getName(), "teleport_ability"))
 		{
 			onPlayerLineJump(player, to, from, delayTime);
 		}
 		else if(ZoneUtility.enterZoneNoPVP(to, from))
 		{
 			DataUtility.removeTemp(player.getName(), "was_PVP");
 			player.sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 		}
 		else if(ZoneUtility.exitZoneNoPVP(to, from))
 		{
 			player.sendMessage(ChatColor.GRAY + "You can now PVP!");
 			return;
 		}
 
 		// Player Hold
 		if(DataUtility.hasKeyTemp(player.getName(), "player_held")) DataUtility.removeTemp(player.getName(), "player_held");
 		else if(DataUtility.hasKeyTemp(player.getName(), "player_hold")) event.setCancelled(true);
 	}
 
 	public void onPlayerLineJump(final Player player, Location to, Location from, int delayTime)
 	{
 		// NullPointer Check
 		if(to == null || from == null) return;
 
 		if(DataUtility.hasKeyTemp(player.getName(), "was_PVP")) return;
 
 		// No Spawn Line-Jumping
 		if(ZoneUtility.enterZoneNoPVP(to, from) && delayTime > 0)
 		{
 			DataUtility.saveTemp(player.getName(), "was_PVP", true);
 			if(DataUtility.hasKeyTemp(player.getName(), "teleport_ability")) DataUtility.removeTemp(player.getName(), "teleport_ability");
 
 			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					DataUtility.removeTemp(player.getName(), "was_PVP");
 					if(ZoneUtility.zoneNoPVP(player.getLocation())) player.sendMessage(ChatColor.GRAY + "You are now safe from all PVP!");
 				}
 			}, (delayTime * 20));
 		}
 
 		// Let players know where they can PVP
 		if(!DataUtility.hasKeyTemp(player.getName(), "was_PVP"))
 		{
 			if(ZoneUtility.exitZoneNoPVP(to, from)) player.sendMessage(ChatColor.GRAY + "You can now PVP!");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String name = event.getPlayer().getName();
 		String message = ChatColor.YELLOW + name + " has left the game.";
 		switch(quitReasonFilter.getLatestQuitReason())
 		{
 			case GENERIC_REASON:
 				message = ChatColor.YELLOW + name + " has either lost connection or crashed.";
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
				message = ChatColor.YELLOW + name + " has left this plane of existence."; // TODO
 				break;
 			case TIMEOUT:
 				message = ChatColor.YELLOW + name + " has disconnected due to timeout.";
 				break;
 		}
 		event.setQuitMessage(message);
 	}
 }
