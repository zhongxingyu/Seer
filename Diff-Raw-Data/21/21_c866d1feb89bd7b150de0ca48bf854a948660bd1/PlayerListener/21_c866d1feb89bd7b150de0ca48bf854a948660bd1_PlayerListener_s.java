 package com.censoredsoftware.demigods.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.battle.Battle;
import com.censoredsoftware.demigods.data.DataManager;
import com.censoredsoftware.demigods.helper.QuitReasonHandler;
import com.censoredsoftware.demigods.item.DivineItem;
import com.censoredsoftware.demigods.player.DCharacter;
import com.censoredsoftware.demigods.player.DPlayer;
import com.censoredsoftware.demigods.util.Configs;

 public class PlayerListener implements Listener
 {
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 
 		// Define variables
 		Player player = event.getPlayer();
 		DPlayer wrapper = DPlayer.Util.getPlayer(player);
 		DCharacter character = wrapper.getCurrent();
 
 		// Set their last login-time
 		Long now = System.currentTimeMillis();
 		wrapper.setLastLoginTime(now);
 
 		// Set display name
 		if(character != null && wrapper.canUseCurrent())
 		{
 			String name = character.getName();
 			ChatColor color = character.getDeity().getColor();
 			player.setDisplayName(color + name + ChatColor.RESET);
			player.setPlayerListName(color + name + ChatColor.RESET);
 			if(character.isAlive())
 			{
 				event.getPlayer().setMaxHealth(character.getMaxHealth());
 				event.getPlayer().setHealth(character.getHealth() >= character.getMaxHealth() ? character.getMaxHealth() : character.getHealth());
 			}
 		}
 
 		// Demigods welcome message
 		if(Configs.getSettingBoolean("misc.welcome_message"))
 		{
 			player.sendMessage(ChatColor.GRAY + "This server is running Demigods version: " + ChatColor.YELLOW + Demigods.PLUGIN.getDescription().getVersion());
 			player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/dg" + ChatColor.GRAY + " for more information.");
 		}
 
 		// First join book
 		if(!player.hasPlayedBefore()) player.getInventory().addItem(DivineItem.WELCOME_BOOK.getSpecialItem().getItem());
 
 		// Notifications
 		if(character != null && character.getMeta().hasNotifications())
 		{
 			int size = character.getMeta().getNotifications().size();
 			player.sendMessage(size == 1 ? ChatColor.GREEN + "You have an unread notification!" : ChatColor.GREEN + "You have " + size + " unread notifications!");
 			player.sendMessage(ChatColor.GRAY + "Find an Altar to view your notifications.");
 		}
 
 		// Remove temp battle data
 		if(DataManager.hasKeyTemp(player.getName(), "quit_during_battle"))
 		{
 			DataManager.removeTemp(player.getName(), "quit_during_battle");
 			player.sendMessage(ChatColor.YELLOW + "WelcomeBook back! You are currently in a battle.");
 		}
 
 		// Alert of losing battle due to leaving
 		if(DataManager.hasKeyTemp(player.getName(), "quit_during_battle_final"))
 		{
 			DataManager.removeTemp(player.getName(), "quit_during_battle_final");
 			player.sendMessage(ChatColor.RED + "You left the game during a battle, and have lost.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		if(!Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation()) && DPlayer.Util.isPraying(event.getPlayer())) DPlayer.Util.togglePraying(event.getPlayer(), false);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		final String name = event.getPlayer().getName();
 		String message = ChatColor.YELLOW + name + " has quit.";
 		switch(QuitReasonHandler.latestQuit)
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
 		if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 		final DCharacter loggingOff = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 		if(loggingOff != null)
 		{
 			loggingOff.setLocation(event.getPlayer().getLocation());
 			if(Battle.Util.isInBattle(loggingOff))
 			{
 				Battle battle = Battle.Util.getBattle(loggingOff);
 				battle.addDeath(loggingOff);
 				DataManager.saveTemp(name, "quit_during_battle", true);
 				Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
 				{
 					@Override
 					public void run()
 					{
 						if(!Bukkit.getOfflinePlayer(name).isOnline() && DataManager.hasKeyTemp(name, "quit_during_battle"))
 						{
 							Battle battle = Battle.Util.getBattle(loggingOff);
 							battle.removeParticipant(loggingOff);
 							DataManager.removeTemp(name, "quit_during_battle");
 							battle.sendMessage(ChatColor.YELLOW + loggingOff.getName() + " has left the battle.");
 							DataManager.saveTemp(name, "quit_during_battle_final", true);
 						}
 					}
 				}, 200);
 			}
 		}
 
 		// Set their last logout-time
 		Long now = System.currentTimeMillis();
 		DPlayer.Util.getPlayer(event.getPlayer()).setLastLogoutTime(now);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 		DCharacter character = DPlayer.Util.getPlayer(event.getPlayer()).getCurrent();
 		if(character != null)
 		{
 			character.setAlive(true);
 			double maxhealth = character.getMaxHealth();
 			event.getPlayer().setMaxHealth(maxhealth);
 			event.getPlayer().setHealth(maxhealth);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerDeath(PlayerDeathEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getEntity().getLocation())) return;
 		DCharacter character = DPlayer.Util.getPlayer(event.getEntity()).getCurrent();
 		if(character != null) character.setAlive(false);
 	}
 }
