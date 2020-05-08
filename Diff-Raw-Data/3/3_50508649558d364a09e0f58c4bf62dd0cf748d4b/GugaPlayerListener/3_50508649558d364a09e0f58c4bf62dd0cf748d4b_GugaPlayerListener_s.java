 package me.Guga.Guga_SERVER_MOD.Listeners;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Random;
 
 import me.Guga.Guga_SERVER_MOD.BasicWorld;
 import me.Guga.Guga_SERVER_MOD.GameMaster;
 import me.Guga.Guga_SERVER_MOD.GameMaster.Rank;
 import me.Guga.Guga_SERVER_MOD.GugaEvent;
 import me.Guga.Guga_SERVER_MOD.GugaFile;
 import me.Guga.Guga_SERVER_MOD.GugaProfession2;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.Homes;
 import me.Guga.Guga_SERVER_MOD.InventoryBackup;
 import me.Guga.Guga_SERVER_MOD.Locker;
 import me.Guga.Guga_SERVER_MOD.MinecraftPlayer;
 import me.Guga.Guga_SERVER_MOD.Handlers.ChatHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GameMasterHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaCommands;
 import me.Guga.Guga_SERVER_MOD.Handlers.HomesHandler;
 
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
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GugaPlayerListener implements Listener 
 {
 	public GugaPlayerListener(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerLogin(PlayerLoginEvent e)
 	{
 		Player player = e.getPlayer();
 		//check if there is player with this name already connected
 		for(Player p : plugin.getServer().getOnlinePlayers())
 		{
 			if(p.getName().equalsIgnoreCase(player.getName()))
 			{
 				if(plugin.userManager.userIsLogged(p.getName()))
 				{
 					e.disallow(Result.KICK_OTHER, "Hrac s timto jmenem uz je online!");
 				}
 			}
 		}
 		
 		//check if player has allowed name
 		if (player.getName().equals(""))
 		{
 			e.disallow(Result.KICK_OTHER, "Prosim zvolte si jmeno!");
 			return;
 		}
 		if (!player.getName().matches("[a-zA-Z0-9_\\-\\.]{2,16}"))
 		{
 			e.disallow(Result.KICK_OTHER,"Prosim zvolte si jmeno slozene jen z povolenych znaku!   a-z A-Z 0-9 ' _ - .");
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent e)
 	{
 		final Player player = e.getPlayer();
 		e.setJoinMessage(ChatColor.YELLOW+player.getName()+ " se pripojil/a.");
 		if (!player.isOnline())
 		{
 			player.kickPlayer("You have timed out.");
 			return;
 		}
 		
 		// check for bans
 		long banExpiration = plugin.banHandler.userBanExpiration(player.getName());
 		if(banExpiration != 0)
 		{
 			if(banExpiration == -1)
 			{
 				player.kickPlayer("Na nasem serveru jste permanentne zabanovan!");
 			}
 			else if((banExpiration*1000)>System.currentTimeMillis())
 			{
 				player.kickPlayer("Na nasem serveru jste zabanovan! Ban vyprsi "+ new Date(banExpiration).toString());					}
 			return;
 		}
 		
 		String ipAddress = player.getAddress().getAddress().toString();
 		if(!plugin.banHandler.isIPWhitelisted(player.getName()))
 		{
 			long ipBanExpiration = plugin.banHandler.ipBanExpiration(ipAddress);
 			if(ipBanExpiration != 0)
 			{
 				if(ipBanExpiration == -1)
 				{
 					player.kickPlayer("Vase IP je na nasem serveru permanentne zabanovana!");
 					return;
 				}
 				else if(ipBanExpiration*1000 > System.currentTimeMillis())
 				{
 					player.kickPlayer("Vase IP je na nasem serveru zabanovana! Ban vyprsi "+ new Date(ipBanExpiration).toString());
 					return;
 				}
 			}
 		}
 
 		int maxP = plugin.getServer().getMaxPlayers();
 		if(plugin.getServer().getOnlinePlayers().length == maxP)
 		{
 			if(GameMasterHandler.IsAtleastRank(player.getName(), Rank.BUILDER) || plugin.vipManager.isVip(player.getName()) || (GugaEvent.GetPlayers().contains(player.getName())))
 			{
 				Player[]players = plugin.getServer().getOnlinePlayers();
 				int i = 0;
 				boolean isKicked = false;
 				Random r = new Random();
 				do{
 					int iToKick = r.nextInt(maxP - 1);
 					if((plugin.vipManager.isVip((players[iToKick].getName()))) || GameMasterHandler.IsAtleastRank(players[iToKick].getName(), Rank.BUILDER) || (GugaEvent.GetPlayers().contains(player.getName())))
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
 					player.kickPlayer("Neni koho vykopnout");
 				}
 			}
 			else
 			{
 				player.kickPlayer("Server je plny misto je rezervovano");
 			}
 		}
 		
 		plugin.logger.LogPlayerJoins(player.getName() ,player.getAddress().toString());
 		
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_JOIN_EVENT: playerName=" + e.getPlayer().getName());
 		}
 		
 		if(!plugin.userManager.userIsRegistered(player.getName()))
 		{
 			synchronized(player){
 				try{
 					player.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
 					player.getInventory().clear();
 				}catch(Exception x){
 					
 				}
 			}
 		}
 		
 		plugin.userManager.onPlayerJoin(player);
 		
 		long timeStart = System.nanoTime();
 		player.sendMessage(ChatColor.RED + "Vitejte na serveru" + ChatColor.AQUA + " MineAndCraft!");
 		player.sendMessage("Pro zobrazeni prikazu napiste " + ChatColor.YELLOW +"/help.");
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
 		ChatHandler.InitializeDisplayName(player);
 		player.sendMessage(ChatColor.YELLOW + "Online hraci: " + ChatColor.GRAY + toSend + ".");
 		if(!(GameMasterHandler.IsAtleastRank(player.getName(), Rank.BUILDER)))
 		{
 			if(GugaPlayerListener.IsCreativePlayer(player))
 			{
 				player.sendMessage("Jste uzivatel se zaregistorvanym creative modem!");
 			}
 			else
 			{
 				if (player.getGameMode().equals(GameMode.CREATIVE))
 					player.setGameMode(GameMode.SURVIVAL);
 			}
 		}
 		if(GugaCommands.fly.contains(player.getName().toLowerCase()))
 		{
 			player.setAllowFlight(true);
 			player.setFlying(true);
 		}
 		
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
 		if(!plugin.userManager.userIsLogged(e.getPlayer().getName()))
 		{
 			if(e.getMessage().contains("/login") || e.getMessage().contains("/help") || e.getMessage().contains("/register"))
 			{
 			}
 			else
 			{
 				ChatHandler.SuccessMsg(e.getPlayer(),"Nejdrive se prihlaste!");
 				e.setCancelled(true);
 				return;
 			}
 		}
 		if(e.getMessage().equalsIgnoreCase("/plugins") || e.getMessage().equalsIgnoreCase("/pl") 
 				|| e.getMessage().equalsIgnoreCase("/me") || e.getMessage().equalsIgnoreCase("/w"))
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
 		if(e.getMessage().toLowerCase().startsWith("/sg") || e.getMessage().toLowerCase().startsWith("/survivalgames"))
 		{
 			if(plugin.userManager.getUser(e.getPlayer().getName()).getProfession().GetLevel() < 10)
 			{
 				e.setCancelled(true);
 			}
 		}
 		
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
 	{
 		Player p = e.getPlayer();
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_CHAT_EVENT: playerName=" + p.getName());
 		}
 		e.setCancelled(true);
 		if (!plugin.userManager.userIsLogged(p.getName()))
 		{
 			return;
 		}
 		ChatHandler.Chat(p,e.getMessage());
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerPickupItem(PlayerPickupItemEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_PICKUP_EVENT: playerName=" + e.getPlayer() + ",itemID=" + e.getItem().getItemStack().getTypeId());
 		}
 
 		MinecraftPlayer pl = plugin.userManager.getUser(e.getPlayer().getName());
 		if(pl == null)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		if (!pl.isAuthenticated())
 		{
 			e.setCancelled(true);
 			return;
 		}		 
 		if(pl.getProfession() == null || (pl.getProfession().GetLevel() < 10 && !BasicWorld.IsBasicWorld(pl.getPlayerInstance().getLocation())))
 		{
 			e.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerKick(PlayerKickEvent e)
 	{
 		Player player = e.getPlayer();
 		e.setLeaveMessage(ChatColor.YELLOW+e.getPlayer().getName()+" se odpojil/a.");
 
 		plugin.userManager.logoutUser(player.getName());
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent e)
 	{
 		e.setQuitMessage(ChatColor.YELLOW+e.getPlayer().getName()+" se odpojil/a.");
 		long timeStart = System.nanoTime();
 		Player p = e.getPlayer();
 
 		plugin.userManager.logoutUser(p.getName());
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
 		p.teleport(e.getRespawnLocation());
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
 		else if(p.getName().matches("Stanley2"))
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
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerMove(PlayerMoveEvent e)
 	{
 		Player player = e.getPlayer();
 		MinecraftPlayer pl = plugin.userManager.getUser(player.getName());
 		if(pl == null)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (!pl.isAuthenticated())
 		{
 			Location from = e.getFrom();
 			Location to = e.getTo();
 			if(!(from.getX()==to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()))
 			{
 				if(!e.getPlayer().teleport(e.getFrom()))
 					e.setCancelled(true);
 				return;
 			}
 		}
 		
 		//if (!GugaWorldSizeHandler.CanMove(player.getLocation()))
 		//	GugaWorldSizeHandler.MoveBack(player);
 		//else if (player.getLocation().getBlockY() < 0)
 		//	player.teleport(plugin.GetAvailablePortLocation(player.getLocation()));
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent e)
 	{
 		World world = e.getPlayer().getWorld();
 	    Chunk chunk = world.getChunkAt(e.getTo());
 	    int x = chunk.getX();
 	    int z = chunk.getZ();
 	    world.refreshChunk(x, z);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("PLAYER_INTERACT_EVENT: playerName=" + e.getPlayer().getName() + ",typeID=" + e.getClickedBlock().getTypeId());
 		}
 		long timeStart = System.nanoTime();
 		Player player = e.getPlayer();
 		Block block = e.getClickedBlock();
 		MinecraftPlayer pl = plugin.userManager.getUser(e.getPlayer().getName());
 		if(pl == null)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		if (!pl.isAuthenticated())
 		{
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
 		{
 			
 			GugaProfession2 prof = pl.getProfession();
 			if (prof == null)
 			{
 				int itemID;
 				ItemStack item;
 				if ((item = e.getItem()) != null)
 				{
 					itemID = item.getTypeId();
 					if ( (itemID == 259) || (itemID == 327))
 					{
 						ChatHandler.FailMsg(player,"Musite byt alespon level 10, aby jste toto mohl pouzit!");
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
 							ChatHandler.FailMsg(player,"Musite byt alespon level 10, aby jste toto mohl pouzit!");
 							e.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
			if(e.getItem().getTypeId() == 407 && (plugin.userManager.getUser(player.getName()).getProfession() == null || plugin.userManager.getUser(player.getName()).getProfession().GetLevel() < 50))
 			{
 				ChatHandler.FailMsg(player, "Nemate lvl 50, nemuzete pouzit TNT.");
 				e.setCancelled(true);
 				return;
 			}
 			
 			if (Locker.LockableBlocks.isLockableBlock(block.getTypeId()))
 			{
 				// *********************************CHEST OPENING*********************************
 				if(!(plugin.blockLocker.hasBlockAccess(player,block)|| GameMasterHandler.IsAtleastGM(player.getName()) ))
 				{
 					e.setCancelled(true);
 					ChatHandler.FailMsg(player,"Tento blok je zamcen!");
 				}
 			}
 		}
 		if (plugin.debug == true)
 		{
 			plugin.log.info("DEBUG_TIME_PLAYERINTERACT=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDropItem(PlayerDropItemEvent e)
 	{
 		MinecraftPlayer pl = plugin.userManager.getUser(e.getPlayer().getName());
 		if(pl == null)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		if (!pl.isAuthenticated())
 		{
 			e.setCancelled(true);
 			return;
 		}		 
 		if(pl.getProfession() == null || (pl.getProfession().GetLevel() < 10 && !BasicWorld.IsBasicWorld(pl.getPlayerInstance().getLocation())))
 		{
 			e.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e)
 	{
 		Player p = e.getPlayer();
 		if(p.getAllowFlight())
 		{
 			if(p.getGameMode() == GameMode.SURVIVAL)
 			{
 				p.setFlying(false);
 				p.setAllowFlight(false);
 			}
 		}
 		GameMaster gm = GameMasterHandler.GetGMByName(p.getName());
 		if(gm != null)
 		{
 			if(gm.GetRank() == Rank.EVENTER)
 			{
 				if(e.getFrom().getName().matches("world_event"))
 				{
 					p.setGameMode(GameMode.SURVIVAL);
 					p.getInventory().clear();
 				}
 			}
 		}
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
 
 	private static ArrayList<String> creativePlayers = new ArrayList<String>();
 	public String[] vipCommands = { "/tp", "/time" };
 	public String[] allowedCommands = { "/login", "/register", "/help"};
 	private static String creativePlayersPath = "plugins/MineAndCraft_plugin/creativePlayers.dat";
 	public static Guga_SERVER_MOD plugin;
 }
