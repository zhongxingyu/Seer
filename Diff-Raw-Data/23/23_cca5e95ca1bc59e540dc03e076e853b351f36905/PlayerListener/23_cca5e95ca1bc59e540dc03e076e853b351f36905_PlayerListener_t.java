 package com.incapable.peacekeeper;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 
 import com.incapable.peacekeeper.datahandling.Config;
 import com.incapable.peacekeeper.datahandling.PersistantStorage;
 
 
 public class PlayerListener implements Listener {
 	public Logger logger = Logger.getLogger("Minecraft");
 	private GenericTimer chatTimer;
 	private GenericTimer cmdTimer;
 	private PersistantStorage storage;
 	private Config config;
 	private BanHandler banHandler;
 
 	public PlayerListener(PersistantStorage storage, Config config, Main main) {
 		this.storage = storage;
 		this.config = config;
 		this.banHandler = new BanHandler(config, storage, main);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
 		Player player = storage.getPlayer(event.getName());
 		if (player.IsBanned() && config.enableBan)
 			event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED,
 					player.GetName() + config.bannedMessage);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = storage.getPlayer(event.getPlayer().getName());
 
 		if (messageAllowed(event, player)) {
 			player.SetLastMessage(event.getMessage());
 			player.SetCanTalk(false);
 			this.chatTimer = new GenericTimer(TIMER_MODE.CHAT);
 			this.chatTimer.run(config.messageCoolDownTime, player);
 		} else {
 			event.setCancelled(true);
 		}
 
 		player.AddMessageCount();
 	}
 
	private boolean messageAllowed(AsyncPlayerChatEvent event, Player player) {
 		if (!Bukkit.getPlayer(player.GetName()).hasPermission(
 				"peacekeeper.exempt")) {
 			if (player.CanTalk()) {
 
 				/* Message filtering */
 				if (FilterMessage(event, player))
 					return false;
 				if (CheckCaps(event, player))
 					return false;
 				if (CheckFlood(event, player))
 					return false;
 				if(CheckFloodPatern(event, player))
 					return false;
 				if(CheckRepeats(event, player))
 					return false;
 				
 			} else if (config.messageCoolDownEnabled) {
 				event.getPlayer().sendMessage(
 						ChatColor.RED + "Please wait "
 								+ config.messageCoolDownTime
 								+ " second(s) between each message!");
 
 				if (config.coolDownKickingEnabled) {
 					player.AddCoolDownWarning();
 
 					if (player.GetCooldownWarnings() >= config.coolDownWarningsToKick) {
 						banHandler.kick(player, "Obey the cooldown!");
 					}
 				}
 
 				return false;
 			}
 
 			return true;
 		}
 		return true;
 	}
 	
	private Boolean FilterMessage(AsyncPlayerChatEvent event, Player player) {
 		if (config.filterEnabled) {
 			for (String filteredWord : config.FilterList) {
 				if (event.getMessage().toLowerCase().contains(filteredWord)) {
 					if (config.giveWarningForBlock)
 						banHandler.warnPlayer(
 								player,
 								"You can't say such a thing!!!",
 								true);
 					else
 						banHandler.warnPlayer(
 								player,
 								"You can't say such a thing!!!",
 								false);
 					return true;
 				}
 
 			}
 
 		}
 		return false;
 	}
 
	private Boolean CheckCaps(AsyncPlayerChatEvent event, Player player) {
 		if ((config.enableCapsBlocking)
 				&& (event.getMessage().length() > config.startCounting)) {
 			float caps = 0;
 			for (int i = event.getMessage().length() - 1; i >= 0; i--) {
 				if (Character.isUpperCase(event.getMessage().charAt(i)))
 					caps++;
 			}
 			float capsPercent = (caps / (float)event.getMessage().length()) * 100.0F;
 			if (capsPercent >= config.disallowPercent) {
 				banHandler.warnPlayer(player, "DON'T use the capslock!!!", true);
 				return true;
 			}
 
 		}
 		return false;
 	}
 	
	private Boolean CheckFlood(AsyncPlayerChatEvent event, Player player) {
 		if (config.enableFloodBlocking) {
 			if (System.currentTimeMillis() - player.GetLastFlood() >= 600000)
 				player.ResetFloodWarnings();
 
 			char prevLetter = Character.UNASSIGNED;
 			int floodCounter = 0;
 
 			for (int i = 0; i < event.getMessage().length(); i++) {
 				if (prevLetter != Character.UNASSIGNED) {
 					if (prevLetter == event.getMessage().charAt(i)) {
 						floodCounter++;
 					} else
 						floodCounter = 0;
 				}
 
 				if (floodCounter >= 3) {
 					player.SetLastFlood();
 					player.AddFloodWarning();
 					if (player.GetFloodWarnings() < config.floodWarningsToKick) {
 						/*if (player.GetFloodWarnings() <= 2)
 							Bukkit.getServer().dispatchCommand(
 									Bukkit.getConsoleSender(),
 									"slap -v " + player.GetName());
 						else
 							Bukkit.getServer().dispatchCommand(
 									Bukkit.getConsoleSender(),
 									"punish " + player.GetName()); */
 
 						Bukkit.getPlayer(player.GetName()).sendMessage(
 								ChatColor.RED + "[PeaceKeeper]: "
 										+ ChatColor.BLUE
 										+ "FLood warning "
 										+ player.GetFloodWarnings()
 										+ "/"
 										+ config.floodWarningsToKick);
 						return true;
 					} else {
 						banHandler.floodKick(player);
 					}
 					return true;
 				} else
 					prevLetter = event.getMessage().charAt(i);
 			}
 		}
 		return false;
 	}
 	
	private boolean CheckFloodPatern(AsyncPlayerChatEvent event, Player player)
 	{
 		if (config.enableFloodPatternBlocking) {
 			if (System.currentTimeMillis() - player.GetLastFlood() >= 600000)
 				player.ResetFloodWarnings();
 			
 			String srchString = "";
 			
 			for(int i = 0; i < event.getMessage().length() - 4; i++)
 			{
 				while(srchString.length() < 4)
 				{
 					if(srchString.length() < 3)
 					{
 						srchString = event.getMessage().substring(i, i + 3);
 					}
 					else
 						srchString += event.getMessage().substring(i, i + 1);
 					
 					if(StringUtils.countMatches(event.getMessage(), srchString) > 3)
 					{
 						player.SetLastFlood();
 						player.AddFloodWarning();
 						if (player.GetFloodWarnings() < config.floodWarningsToKick) {
 							/*if (player.GetFloodWarnings() <= 2)
 								Bukkit.getServer().dispatchCommand(
 										Bukkit.getConsoleSender(),
 										"slap -v " + player.GetName());
 							else
 								Bukkit.getPlayer(player.GetName())
 										.setFireTicks(100);*/
 		
 							Bukkit.getPlayer(player.GetName()).sendMessage(
 									ChatColor.RED + "[PeaceKeeper]: "
 											+ ChatColor.BLUE
 											+ "FLood warning "
 											+ player.GetFloodWarnings()
 											+ "/"
 											+ config.floodWarningsToKick);
 							return true;
 						} else {
 							banHandler.floodKick(player);
 							return true;
 						}
 					}
 				}
 				
 				srchString = "";
 			}
 		}
 		return false;
 	}
 	
	private boolean CheckRepeats(AsyncPlayerChatEvent event, Player player){
 		if (event.getMessage().equals(player.GetLastMessage())) {
 			player.AddRepeats();
 			if (player.GetRepeats() > config.allowedRepeats) {
 				banHandler.warnPlayer(
 						player,
 						"You have repeated that message too many times!",
 						true);
 				return true;
 			}
 		} else {
 			player.ResetRepeats();
 		}
 		
 		return false;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player player = storage.getPlayer(event.getPlayer().getName());
 
 		if (!Bukkit.getPlayer(player.GetName()).hasPermission(
 				"peacekeeper.exempt")) {
 			if (config.commandCoolDownEnabled) {
 				if (player.CanUseCmd()) {
 					player.SetCanUseCmd(false);
 					this.cmdTimer = new GenericTimer(TIMER_MODE.COMMAND);
 
 					this.cmdTimer.run(config.commandCoolDownTime, player);
 				} else {
 					event.setCancelled(true);
 					event.getPlayer().sendMessage(
 							ChatColor.RED + "Please wait "
 									+ config.commandCoolDownTime
 									+ " second(s) between each command!");
 					if (config.coolDownKickingEnabled) {
 						player.AddCoolDownWarning();
 
 						if (player.GetCooldownWarnings() >= config.coolDownWarningsToKick)
 							banHandler.kick(player, "Obey the cooldown!");
 					}
 				}
 			}
 		}
 	}
 
 	private class GenericTimer {
 		private Timer timer;
 		private Player PlayerToSet;
 		private PlayerListener.TIMER_MODE mode;
 
 		public GenericTimer(PlayerListener.TIMER_MODE mode) {
 			this.timer = new Timer();
 			this.mode = mode;
 		}
 
 		public void run(int seconds, Player Player) {
 			if (this.mode == PlayerListener.TIMER_MODE.REP)
 				this.timer.scheduleAtFixedRate(new GenericTask(),
 						seconds * 1000, seconds * 1000);
 			else {
 				this.timer.schedule(new GenericTask(), seconds * 1000);
 			}
 			this.PlayerToSet = Player;
 		}
 
 		class GenericTask extends TimerTask {
 			GenericTask() {
 			}
 
 			public void run() {
 				if (PlayerListener.GenericTimer.this.mode == PlayerListener.TIMER_MODE.CHAT)
 					PlayerListener.GenericTimer.this.PlayerToSet
 							.SetCanTalk(true);
 				else if (PlayerListener.GenericTimer.this.mode == PlayerListener.TIMER_MODE.COMMAND)
 					PlayerListener.GenericTimer.this.PlayerToSet
 							.SetCanUseCmd(true);
 				else if (PlayerListener.GenericTimer.this.mode == PlayerListener.TIMER_MODE.REP)
 					PlayerListener.GenericTimer.this.PlayerToSet
 							.AddReputation();
 			}
 		}
 	}
 
 	private static enum TIMER_MODE {
 		CHAT, COMMAND, REP;
 	}
 }
