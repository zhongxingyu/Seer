 package me.Guga.Guga_SERVER_MOD.Listeners;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Random;
 
 import me.Guga.Guga_SERVER_MOD.GameMaster;
 import me.Guga.Guga_SERVER_MOD.GugaBan;
 import me.Guga.Guga_SERVER_MOD.GugaFile;
 import me.Guga.Guga_SERVER_MOD.GugaHunter;
 import me.Guga.Guga_SERVER_MOD.GugaMute;
 import me.Guga.Guga_SERVER_MOD.GugaProfession;
 import me.Guga.Guga_SERVER_MOD.GugaSpectator;
 import me.Guga.Guga_SERVER_MOD.GugaVirtualCurrency;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.Homes;
 import me.Guga.Guga_SERVER_MOD.InventoryBackup;
 import me.Guga.Guga_SERVER_MOD.Handlers.GameMasterHandler;
 import me.Guga.Guga_SERVER_MOD.GameMaster.Rank;
 import me.Guga.Guga_SERVER_MOD.Handlers.ChatHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaAuctionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaBanHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaCommands;
 import me.Guga.Guga_SERVER_MOD.Handlers.HomesHandler;
 //import me.Guga.Guga_SERVER_MOD.Handlers.GugaFlyHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaMCClientHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaWorldSizeHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GugaPlayerListener implements Listener 
 {
 	public GugaPlayerListener(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerLoginEvent(PlayerLoginEvent e)
 	{
 		String pName = e.getPlayer().getName();
 		Player[] players = plugin.getServer().getOnlinePlayers();
 		int i = 0;
 		while(i<players.length)
 		{
 			if(players[i].getName().matches(pName))
 			{
 				if(plugin.acc.UserIsLogged(pName))
 				{
 					e.disallow(Result.KICK_OTHER, "Hrac s timto jmenem uz je online!");
 				}
 			}
 			i++;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent e)
 	{
 		final Player p = e.getPlayer();
 		e.setJoinMessage(ChatColor.YELLOW+p.getName()+ " se pripojil/a.");
 		if (!p.isOnline())
 			return;
 		if (!GugaMCClientHandler.HasClient(p))
 		{
 			p.kickPlayer("Stahnete si naseho klienta na www.mineandcraft.cz (navod na pripojeni)");
 			return;
 		}
 		if (GugaMCClientHandler.IsWhiteListed(p))
 			return;
 		if (GugaBanHandler.GetGugaBan(p.getName()) == null)
 			GugaBanHandler.AddBan(p.getName(), 0);
 		if (!GugaBanHandler.IsIpWhitelisted(p.getName()))
 		{
 			if (GugaBanHandler.IsBanned(p.getName()))
 			{
 				GugaBan ban = GugaBanHandler.GetGugaBan(p.getName());
 				long hours = (ban.GetExpiration() - System.currentTimeMillis()) / (60 * 60 * 1000);
 				p.kickPlayer("Na nasem serveru jste zabanovan! Ban vyprsi za " + Long.toString(hours) + " hodin(y)");
 				return;
 			   }
 		}
 		GugaVirtualCurrency curr = plugin.FindPlayerCurrency(p.getName());
 		if (curr == null)
 		{
 			curr = new GugaVirtualCurrency(plugin, p.getName(), 0, new Date(0));
 			plugin.playerCurrency.add(curr);
 		}
 		if (plugin.professions.get(p.getName()) == null)
 		{
 			plugin.professions.put(p.getName(), new GugaProfession(p.getName(), 0, plugin));
 		}
 		int maxP = plugin.getServer().getMaxPlayers();
 		if(plugin.getServer().getOnlinePlayers().length == maxP)
 		{
 			if(GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER) || curr.IsVip())
 			{
 				Player[]players = plugin.getServer().getOnlinePlayers();
 				int i = 0;
 				boolean isKicked = false;
 				Random r = new Random();
 				do{
 					int iToKick = r.nextInt(maxP - 1);
 					curr = plugin.FindPlayerCurrency(players[iToKick].getName());
 					if(curr.IsVip() || GameMasterHandler.IsAtleastRank(players[iToKick].getName(), Rank.BUILDER))
 					{
 						isKicked = false;
 					}
 					else
 					{
 						players[iToKick].kickPlayer("Bylo uvolneno misto pro VIP");
 						isKicked = true;
 					}
 					i++;
 				}while(!isKicked && i<maxP);
 				if(!isKicked)
 				{
 					p.kickPlayer("Neni koho vykopnout");
 				}
 			}
 			else
 			{
 				p.kickPlayer("Server je plny misto je rezervovano");
 			}
 		}
 		if (p.getName().contains(" "))
 		{
 			p.kickPlayer("Prosim zvolte si jmeno bez mezery!");
 			return;
 		}
 		if (!CanUseName(p.getName()))
 		{
 			p.kickPlayer("Prosim zvolte si jmeno slozene jen z povolenych znaku!   a-z A-Z 0-9 ' _ - .");
 			return;
 		}
 		if (p.getName().matches(""))
 		{
 			p.kickPlayer("Prosim zvolte si jmeno!");
 			return;
 		}
 
 		plugin.logger.LogPlayerJoins(p.getName() ,p.getAddress().toString());
 		GugaAuctionHandler.CheckPayments(p);
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_JOIN_EVENT: playerName=" + e.getPlayer().getName());
 		}
 		long timeStart = System.nanoTime();
 		p.sendMessage(ChatColor.RED + "Vitejte na serveru" + ChatColor.AQUA + " MineAndCraft!");
 		p.sendMessage("Pro zobrazeni prikazu napiste " + ChatColor.YELLOW +"/help.");
 		Player[]players = plugin.getServer().getOnlinePlayers();
 		String toSend = "";
 		int i=0;
 		while(i < players.length)
 		{
 			if(i==0)
 				toSend += players[i].getName();
 			else
 				toSend += ", " + players[i].getName();
 			i++;
 		}
 		ChatHandler.InitializeDisplayName(p);
 		p.sendMessage(ChatColor.YELLOW + "Online hraci: " + ChatColor.GRAY + toSend + ".");
 		if(!(GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER)))
 		{
 			if(GugaPlayerListener.IsCreativePlayer(p))
 			{
 				p.sendMessage("Jste uzivatel se zaregistorvanym creative modem!");
 			}
 			else
 			{
 				if (p.getGameMode().equals(GameMode.CREATIVE))
 					p.setGameMode(GameMode.SURVIVAL);
 			}
 		}
 		if(GugaCommands.fly.contains(p.getName().toLowerCase()))
 		{
 			p.setAllowFlight(true);
 			p.setFlying(true);
 		}
 		plugin.acc.playerStart.put(p.getName(), p.getLocation());
 		plugin.acc.tpTask.add(p.getName());
 		plugin.acc.StartTpTask();
 		if (plugin.debug)
 		{
 			plugin.log.info("DEBUG_TIME_PLAYERJOIN=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("COMMAND_PREPROCESS_EVENT: playerName=" + e.getPlayer().getName() + ",cmd=" + e.getMessage());
 		}
 		if(!plugin.acc.UserIsLogged(e.getPlayer()))
 		{
 			if(e.getMessage().contains("/login") || e.getMessage().contains("/help"))
 			{
 			}
 			else
 			{
 				e.getPlayer().sendMessage("Nejdrive se prihlaste!");
 				e.setCancelled(true);
 				return;
 			}
 		}
 		if(e.getMessage().equalsIgnoreCase("/plugins") || e.getMessage().equalsIgnoreCase("/pl"))
 		{
 			if(!GameMasterHandler.IsAtleastGM(e.getPlayer().getName()))
 			{
 				ChatHandler.FailMsg(e.getPlayer(), "K tomuto prikazu nemate pristup!");
 				e.setCancelled(true);
 				return;
 			}
 		}
 		if(e.getMessage().equalsIgnoreCase("/kill") && e.getPlayer().getWorld().getName().matches("arena"))
 		{
 			e.setCancelled(true);
 		}
 
 		String msg = "";
 		String[] splitted = e.getMessage().split(" ");
 		int i = 0;
 		if (e.getMessage().contains("/tell"))
 		{
 			if(GugaMute.getPlayerStatus(e.getPlayer().getName()))
 			{
 				e.getPlayer().sendMessage("Jste ztlumen. Nelze pouzit /tell");
 				e.setCancelled(true);
 				return;
 			}
 			String pName = splitted[1];
 			i = 2;
 			while (i < splitted.length)
 			{
 				msg += splitted[i];
 				msg += " ";
 				i++;
 			}
 			Player p = plugin.getServer().getPlayer(pName);
 			//plugin.socketServer.SendChatMsg(e.getPlayer().getName() + " -> " + p.getName() + ": " + msg);
 			e.getPlayer().sendMessage(ChatColor.GRAY + "To " + p.getName() + ": " + msg);
 			GugaCommands.reply.put(p, e.getPlayer());
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
 	{
 		Player p = e.getPlayer();
 		//plugin.socketServer.SendChatMsg(e.getPlayer().getName() + ": " + e.getMessage());
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_CHAT_EVENT: playerName=" + p.getName());
 		}
 		if (!plugin.acc.UserIsLogged(p))
 		{
 			e.setCancelled(true);
 			return;
 		}
 		ChatHandler.SendChatMessage(p, e.getMessage());
 		e.setCancelled(true);
 		return;
 		/*GameMaster gm;
 		if ( (gm = GameMasterHandler.GetGMByName(p.getName())) != null)
 		{
 			if (plugin.acc.UserIsLogged(p))
 			{
 				if(!GugaCommands.GMsOffState.contains(p))
 				{
 					if (gm.GetRank() == Rank.ADMIN)
 					{
 						e.setMessage(ChatColor.AQUA + e.getMessage());
 						return;
 					}
 					else if (gm.GetRank() == Rank.GAMEMASTER)
 					{
 						e.setMessage(ChatColor.GREEN + e.getMessage());
 						return;
 					}
 					else if(gm.GetRank()==Rank.BUILDER)
 					{
 						e.setMessage(ChatColor.GOLD + e.getMessage());
 						return;
 					}
 				}
 			}
 			else
 			{
 				e.setCancelled(true);
 				return;
 			}
 		}*/
 	}
 		/*else if(e.getMessage().contains(".Ownage"))
 		{
 			String msg = e.getMessage();
 			String playerName = msg.split(",")[1];
 			Location pLoc = e.getPlayer().getServer().getPlayer(playerName).getLocation();
 			Location eyeLoc = e.getPlayer().getTargetBlock(null, 100).getLocation();
 			Location finalLoc = pLoc;
 			double pX = pLoc.getX();
 			double pZ = pLoc.getZ();
 			double eX = eyeLoc.getX();
 			double eZ = eyeLoc.getZ();
 			if (pX-eX > 0)
 			{
 				finalLoc.setX(pX+1);
 				if (pZ-eZ > 0)
 				{
 					finalLoc.setZ(pZ+1);
 				}
 				else
 				{
 					finalLoc.setZ(pZ-1);
 				}
 			}
 			else
 			{
 				finalLoc.setX(pX-1);
 				if (pZ-eZ > 0)
 				{
 					finalLoc.setZ(pZ+1);
 				}
 				else
 				{
 					finalLoc.setZ(pZ-1);
 				}
 			}
 			e.getPlayer().getWorld().spawnCreature(finalLoc,CreatureType.CREEPER);
 			e.setCancelled(true);
 		}*/
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerPickupItem(PlayerPickupItemEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_PICKUP_EVENT: playerName=" + e.getPlayer() + ",itemID=" + e.getItem().getItemStack().getTypeId());
 		}
 		Player p = e.getPlayer();
 		if (!plugin.acc.UserIsLogged(p))
 		{
 			e.setCancelled(true);
 			return;
 		}
 		if (GugaSpectator.spectatorList.contains(p))
 		{
 			e.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerKick(PlayerKickEvent e)
 	{
 		e.setLeaveMessage(ChatColor.YELLOW+e.getPlayer().getName()+" se odpojil/a.");
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent e)
 	{
 		e.setQuitMessage(ChatColor.YELLOW+e.getPlayer().getName()+" se odpojil/a.");
 		long timeStart = System.nanoTime();
 		Player p = e.getPlayer();
 		GugaMCClientHandler.UnregisterUser(p);
 
 		if (plugin.config.accountsModule)
 		{
 			plugin.acc.loggedUsers.remove(p.getName());
 		}
 		plugin.SaveProfessions();
 		plugin.SaveCurrency();
 		GugaProfession prof;
 		if ((prof = plugin.professions.get(p.getName())) != null)
 		{
 			if (prof instanceof GugaHunter)
 			{
 				((GugaHunter)prof).StopRegenHp();
 			}
 		}
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_QUIT_EVENT: Time=" + ((System.nanoTime() - timeStart)/1000)+ ",playerName=" + e.getPlayer().getName());
 		}	
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRespawn(PlayerRespawnEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_RESPAWN_EVENT: playerName=" + e.getPlayer().getName());
 		}
 		Player p = e.getPlayer();
 		Homes home;
 		if(p.getBedSpawnLocation() != null)
 		{
 			e.setRespawnLocation(p.getBedSpawnLocation());
 		}
 		else if((home = HomesHandler.getHomeByPlayer(p.getName())) != null)
 		{
 			e.setRespawnLocation(HomesHandler.getLocation(home));
 		}
 		GugaSpectator spec;
 		if ((spec = GugaCommands.spectation.get(p.getName())) != null)
 		{
 			spec.Teleport();
 			spec.InvisTarget();
 		}
 		plugin.acc.SetStartLocation(p, e.getRespawnLocation());
 		Location respawnLoc;
 		if ((respawnLoc =plugin.arena.GetPlayerBaseLocation(p)) != null)
 		{
 			e.setRespawnLocation(respawnLoc);
 			plugin.arena.RemovePlayerBaseLocation(p);
 			InventoryBackup.InventoryReturnWrapped(p, true);
 		}
 		if(p.getName().matches("czrikub"))
 		{
 			InventoryBackup.InventoryReturnWrapped(p, true);
 		}
 		else if(p.getName().matches("Alma_Lodaka"))
 		{
 			InventoryBackup.InventoryReturnWrapped(p, true);
 		}
 		else if(p.getName().matches("Guga"))
 		{
 			InventoryBackup.InventoryReturnWrapped(p, true);
 		}
 		else if(p.getName().matches("Virus"))
 		{
 			InventoryBackup.InventoryReturnWrapped(p, true);
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerMove(PlayerMoveEvent e)
 	{
 		Player p = e.getPlayer();
 		if (!GugaWorldSizeHandler.CanMove(p.getLocation()))
 			GugaWorldSizeHandler.MoveBack(p);
 		else if (p.getLocation().getBlockY() < 0)
 			p.teleport(plugin.GetAvailablePortLocation(p.getLocation()));
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent e)
 	{
 		World world = e.getPlayer().getWorld();
 	    Chunk chunk = world.getChunkAt(e.getTo());
 	    int x = chunk.getX();
 	    int z = chunk.getZ();
 	    world.refreshChunk(x, z);
 		Player p = e.getPlayer();
 		GugaSpectator spec;
 		if ((spec = GugaCommands.spectation.get(p.getName())) != null)
 		{
 			spec.Teleport();
 			spec.InvisTarget();
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_INTERACT_EVENT: playerName=" + e.getPlayer().getName() + ",typeID=" + e.getClickedBlock().getTypeId());
 		}
 		long timeStart = System.nanoTime();
 		Player p = e.getPlayer();
 		Block b = e.getClickedBlock();
 		if (!plugin.acc.UserIsLogged(p) && plugin.config.accountsModule)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		if(e.getAction() == Action.LEFT_CLICK_BLOCK)
 		{
 			if(GameMasterHandler.IsAdmin(p.getName()))
 			{
 				GugaCommands.x1 = b.getX();
 				GugaCommands.z1 = b.getZ();
 				if(p.getItemInHand().getTypeId() == ID_SELECT_ITEM)
 				{
 					Player[]OnLinePlayers = plugin.getServer().getOnlinePlayers();
 					int i=0;
 					while(i < OnLinePlayers.length)
 					{
 						if(GameMasterHandler.IsAdmin(OnLinePlayers[i].getName()))
 						{
 							OnLinePlayers[i].sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " Sets first position to X:" + Integer.toString(GugaCommands.x1) + " Z: " + Integer.toString(GugaCommands.z1));
 						}
 						i++;
 					}
 				}
 			}
 		}
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
 		{
 			if(GameMasterHandler.IsAdmin(p.getName()))
 			{
 				if(p.getItemInHand().getTypeId() == ID_SELECT_ITEM)
 				{
 					GugaCommands.x2 = b.getX();
 					GugaCommands.z2 = b.getZ();
 					Player[]OnLinePlayers = plugin.getServer().getOnlinePlayers();
 					int i=0;
 					while(i < OnLinePlayers.length)
 					{
 						if(GameMasterHandler.IsAdmin(OnLinePlayers[i].getName()))
 						{
 							OnLinePlayers[i].sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " Sets second position to X:" + Integer.toString(GugaCommands.x2) + " Z: " + Integer.toString(GugaCommands.z2));
 						}
 						i++;
 					}
 				}
 			}
 			/*GugaSpectator spec;
 			if ((spec = GugaCommands.spectation.get(p.getName())) != null)
 			{
 				//spec.CloneInventory();
 			}*/
 			GugaProfession prof = plugin.professions.get(p.getName());
 			if (prof == null)
 			{
 				int itemID;
 				ItemStack item;
 				if ((item = e.getItem()) != null)
 				{
 					itemID = item.getTypeId();
 					if ( (itemID == 259) || (itemID == 327))
 					{
 						p.sendMessage("Musite byt alespon level 10, aby jste toto mohl pouzit!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 			}
 			else 
 			{
 				int level = prof.GetLevel();
 				if (level<10)
 				{
 					int itemID;
 					ItemStack item;
 					if ((item= e.getItem()) != null)
 					{
 						itemID = item.getTypeId();
 						if ( (itemID == 259) || (itemID == 327))
 						{
 							p.sendMessage("Musite byt alespon level 10, aby jste toto mohl pouzit!");
 							e.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 			Block targetBlock;
 			targetBlock = e.getClickedBlock();
 			if (plugin.config.chestsModule)
 			{
 				// *********************************CHEST OPENING*********************************
 
 				String blockOwner;
 				if (targetBlock.getTypeId() == ID_CHEST)
 				{
 					blockOwner = plugin.chests.GetBlockOwner(targetBlock);
 					if(blockOwner.matches(p.getName()) || blockOwner.matches("notFound") || GameMasterHandler.IsAtleastGM(p.getName()))
 					{
 						return;
 					}
 					else
 					{
 						e.setCancelled(true);
 						p.sendMessage(ChatColor.BLUE+"[LOCKER] "+ChatColor.WHITE+"Tato truhla je zamcena!");
 					}
 				}
 				else if(targetBlock.getTypeId() == ID_DISPENSER)
 				{
 					blockOwner = plugin.dispensers.GetBlockOwner(targetBlock);
 					if(blockOwner.matches(p.getName()) || blockOwner.matches("notFound") || GameMasterHandler.IsAtleastGM(p.getName()))
 					{
 						return;
 					}
 					else
 					{
 						e.setCancelled(true);
 						p.sendMessage(ChatColor.BLUE+"[LOCKER] "+ChatColor.WHITE+"Tento davkovac je zamcen!");
 					}
 				}
 				else if(targetBlock.getTypeId() == ID_FURNANCE || targetBlock.getTypeId() == ID_FURNANCE_BURNING)
 				{
 					blockOwner = plugin.furnances.GetBlockOwner(targetBlock);
 					if(blockOwner.matches(p.getName()) || blockOwner.matches("notFound") || GameMasterHandler.IsAtleastGM(p.getName()))
 					{
 						return;
 					}
 					else
 					{
 						e.setCancelled(true);
 						p.sendMessage(ChatColor.BLUE+"[LOCKER] "+ChatColor.WHITE+"Tato pec je zamcena!");
 					}
 				}
 			}
 		}
 		/*else if (e.getAction() == Action.LEFT_CLICK_BLOCK)
 		{
 			if (GugaCommands.speed.contains(p.getName().toLowerCase()))
 			{
 				Block targetBlock;
 				targetBlock = e.getClickedBlock();
 				targetBlock.setTypeId(0);
 			}
 		}*/
 		if (plugin.debug == true)
 		{
 			plugin.log.info("DEBUG_TIME_PLAYERINTERACT=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDropItem(PlayerDropItemEvent e)
 	{
 		if (!GugaPlayerListener.plugin.acc.UserIsLogged(e.getPlayer()))
 			e.setCancelled(true);
 
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e)
 	{
 		GameMaster gm = GameMasterHandler.GetGMByName(e.getPlayer().getName());
 		if(gm != null)
 		{
 			if(gm.GetRank() == Rank.EVENTER)
 			{
 				Player p = e.getPlayer();
 				if(e.getFrom().getName().matches("world_event"))
 				{
 					p.setGameMode(GameMode.SURVIVAL);
 					p.getInventory().clear();
 				}
 			}
 		}
 	}
 	private boolean CanUseName(String name)
 	{
 		char[] pName = name.toCharArray();
 		int i = 0;
 		while (i < pName.length)
 		{
 			boolean allowed = false;
 			if ( ((char)39) == pName[i])
 				allowed = true;
 			else if ( ((char)45) == pName[i])
 				allowed = true;
 			else if ( ((char)46) == pName[i])
 				allowed = true;
 			else if ( ((char)95) == pName[i])
 				allowed = true;
 
 			int i2 = 48;
 			while (i2 <= 57)
 			{
 				if ( ((char)i2) == pName[i] )
 					allowed = true;
 				if (allowed)
 					break;
 				i2++;
 			}
 			i2 = 65;
 			while (i2 <= 90)
 			{
 				if ( ((char)i2) == pName[i] )
 					allowed = true;
 				if (allowed)
 					break;
 				i2++;
 			}
 			i2 = 97;
 			while (i2 <= 122)
 			{
 				if ( ((char)i2) == pName[i] )
 					allowed = true;
 				if (allowed)
 					break;
 				i2++;
 			}
 			if (!allowed)
 				return false;
 			i++;
 		}
 		return true;
 	}
 	public static void LoadCreativePlayers()
 	{
 
 		GugaFile file = new GugaFile(creativePlayersPath, GugaFile.READ_MODE);
 		if (creativePlayers.size() > 0)
 			creativePlayers.clear();
 		file.Open();
 		String line = null;
 		while ((line = file.ReadLine()) != null)
 		{
 			creativePlayers.add(line);
 		}
 		file.Close();
 	}
 	public static boolean IsCreativePlayer(Player p)
 	{
 		String pName = p.getName();
 		Iterator<String> i = creativePlayers.iterator();
 
 		while (i.hasNext())
 		{
 			if (pName.equalsIgnoreCase(i.next()))				
 				return true;
 		}
 		return false;
 	}
 	private int ID_CHEST=54;
 	private int ID_DISPENSER=23;
 	private int ID_FURNANCE=61;
 	private int ID_FURNANCE_BURNING=62;
 	private int ID_SELECT_ITEM = 269;
 	private static ArrayList<String> creativePlayers = new ArrayList<String>();
 	public String[] vipCommands = { "/tp", "/time" };
 	public String[] allowedCommands = { "/login", "/register", "/help"};
 	public boolean canSpeedUp = true;
 	private static String creativePlayersPath = "plugins/creativePlayers.dat";
 	public static Guga_SERVER_MOD plugin;
 	}
