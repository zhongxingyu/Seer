 package com.censoredsoftware.demigods.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.item.Book;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 
 public class PlayerListener implements Listener
 {
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		if(Demigods.isDisabledWorld(event.getPlayer().getLocation())) return;
 
 		// Define variables
 		Player player = event.getPlayer();
 		DPlayer wrapper = DPlayer.Util.getPlayer(player);
 		DCharacter character = wrapper.getCurrent();
 
 		// Set their lastlogintime
 		Long now = System.currentTimeMillis();
 		wrapper.setLastLoginTime(now);
 
 		// Set display name
 		if(character != null && wrapper.canUseCurrent())
 		{
 			String name = character.getName();
 			ChatColor color = character.getDeity().getColor();
 			player.setDisplayName(color + name + ChatColor.RESET);
 			player.setPlayerListName(color + name + ChatColor.RESET);
 			event.getPlayer().setMaxHealth(character.getMaxHealth());
 			event.getPlayer().setHealth(character.getHealth());
 		}
 
 		// Demigods welcome message
 		if(Demigods.config.getSettingBoolean("misc.welcome_message"))
 		{
 			player.sendMessage(ChatColor.GRAY + "This server is running Demigods version: " + ChatColor.YELLOW + Demigods.plugin.getDescription().getVersion());
 			player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/dg" + ChatColor.GRAY + " for more information.");
 		}
 
 		// First join book
 		if(!player.hasPlayedBefore()) player.getInventory().addItem(Book.FIRST_JOIN.getBook());
 
 		// Notifications
 		if(character != null && character.getMeta().hasNotifications())
 		{
 			int size = character.getMeta().getNotifications().size();
 			player.sendMessage(size == 1 ? ChatColor.GREEN + "You have an unread notification!" : ChatColor.GREEN + "You have " + size + " unread notifications!");
 			player.sendMessage(ChatColor.GRAY + "Find an Altar to view your notifications.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		if(!Demigods.isDisabledWorld(event.getPlayer().getLocation()) && DPlayer.Util.isPraying(event.getPlayer())) DPlayer.Util.togglePraying(event.getPlayer(), false);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String name = event.getPlayer().getName();
 		String message = ChatColor.YELLOW + name + " has left the game.";
 		switch(Demigods.plugin.getLatestQuitReason())
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
 		DCharacter loggingOff = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 		if(loggingOff != null) loggingOff.setLocation(event.getPlayer().getLocation());
 	}
 
	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerRespawn(PlayerRespawnEvent event) // TODO Is this working?
 	{
 		if(Demigods.isDisabledWorld(event.getPlayer().getLocation())) return;
 		DPlayer wrapper = DPlayer.Util.getPlayer(event.getPlayer());
 		if(wrapper.getCurrent() != null)
 		{
 			double maxhealth = wrapper.getCurrent().getMaxHealth();
 			event.getPlayer().setMaxHealth(maxhealth);
 			event.getPlayer().setHealth(maxhealth);
 		}
 	}
 }
