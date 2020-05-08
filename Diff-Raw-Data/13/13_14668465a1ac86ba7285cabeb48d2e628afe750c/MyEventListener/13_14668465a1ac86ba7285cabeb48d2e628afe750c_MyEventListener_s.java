 package me.rainoboy97.scrimmage;
 
 /*
  import java.util.List;
  import org.bukkit.entity.Entity;
  import org.bukkit.event.entity.ProjectileHitEvent;
  */
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.event.weather.ThunderChangeEvent;
 import org.bukkit.event.world.StructureGrowEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 @SuppressWarnings("deprecation")
 public class MyEventListener implements Listener {
 
 	private final Scrimmage plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 
 	public MyEventListener(Scrimmage plugin) {
 		this.plugin = plugin;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	@SuppressWarnings("unchecked")
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		event.setJoinMessage(null);
 		Player player = event.getPlayer();
 		String displayName = player.getDisplayName();
 		if (player.getGameMode().equals(GameMode.CREATIVE) || !(Scrimmage.gameActive)) {
 			player.setGameMode(GameMode.CREATIVE);
 			player.setAllowFlight(true);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 			player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 			player.teleport(Var.observerSpawn);
 			player.getInventory().clear();
 			player.getInventory().setHelmet(null);
 			player.getInventory().setChestplate(null);
 			player.getInventory().setLeggings(null);
 			player.getInventory().setBoots(null);
 			if (Scrimmage.specs.contains(player.getDisplayName())) {
 				Scrimmage.specs.remove(player.getDisplayName());
 			}
 			if (Scrimmage.team.contains(player.getDisplayName())) {
 				Scrimmage.team.remove(player.getDisplayName());
 			}
 			if (Scrimmage.enemyTeam.contains(player.getDisplayName())) {
 				Scrimmage.enemyTeam.remove(player.getDisplayName());
 			}
 			Scrimmage.specs.add(player.getDisplayName());
 			ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 			Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
 			Team spec = scoreboard.getTeam("spec");
 			spec.addPlayer((OfflinePlayer) player);
 		}
 		if (Scrimmage.team(player) == "") {
 			ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
 			Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
 			Team spec = scoreboard.getTeam("spec");
 			spec.addPlayer((OfflinePlayer) player);
 			Scrimmage.specs.add(player.getDisplayName());
 			if (Scrimmage.team.contains(player.getDisplayName())) {
 				Scrimmage.team.remove(player.getDisplayName());
 			}
 			if (Scrimmage.enemyTeam.contains(player.getDisplayName())) {
 				Scrimmage.enemyTeam.remove(player.getDisplayName());
 			}
 		}
 		if (player.isOp()) {
 		Bukkit.broadcastMessage(ChatColor.GREEN + "*" + ChatColor.AQUA + displayName + ChatColor.YELLOW + " joined the server");
 		}else{
 		Bukkit.broadcastMessage(ChatColor.AQUA + displayName + ChatColor.YELLOW + " joined the server");
 		}
 		player.sendMessage(ChatColor.GRAY + "Welcome!! This server is currently running Scrimmage with the map " + Var.mapName + ". (Made by " + Var.mapMakers + ") and config courtesy of " + Var.configMaker + ".");
 
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		event.setQuitMessage(null);
 		Player player = event.getPlayer();
 		String displayName = player.getDisplayName();
 		if (player.isOp()) {
 		Bukkit.broadcastMessage(ChatColor.GREEN + "*" + ChatColor.AQUA + displayName + ChatColor.YELLOW + " left the server");
 		}else{
 		Bukkit.broadcastMessage(ChatColor.AQUA + displayName + ChatColor.YELLOW + " left the server");
 		}
 
 		// if (!(player.getGameMode() == GameMode.CREATIVE && player.isOp())) {
 		// Bukkit.broadcastMessage(VariableHandler.enemyTeamTechnicalColor + ""
 		// + ChatColor.STRIKETHROUGH
 		// + displayName + " will be killed immediately.");
 		// player.setHealth(0D);
 		// }
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block block = event.getBlock();
 		int x = 0;
 		int y = 0;
 		int z = 0;
 		int count = 0;
 		for (String i : Var.teamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.teamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		if (block.getLocation().equals(teamWool1)) {
 			event.setCancelled(true);
 		}
 		if (block.getLocation().equals(teamWool2)) {
 			event.setCancelled(true);
 		}
 		if (block.getLocation().equals(enemyTeamWool1)) {
 			event.setCancelled(true);
 		}
 		if (block.getLocation().equals(enemyTeamWool2)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (event.getPlayer().getGameMode() == GameMode.CREATIVE && event.getBlock().getTypeId() == 111) {
 			event.setCancelled(true);
 			RemoveBlock removeBlock = new RemoveBlock(plugin, event.getBlock());
 			for (long i = 1; i <= 20; i++)
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, removeBlock, i);
 		}
 		boolean yellow = false;
 		boolean purple = false;
 		boolean orange = false;
 		boolean lime = false;
 		Block block = event.getBlock();
 		Block blockChecker;
 		int x = 0;
 		int y = 0;
 		int z = 0;
 		int count = 0;
 		for (String i : Var.teamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.teamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		blockChecker = teamWool1.getBlock();
 		if (blockChecker.getType() == Material.WOOL && blockChecker.getData() == Var.teamWool1TechnicalDye.getData()) {
 			orange = true;
 		}
 		blockChecker = teamWool2.getBlock();
 		if (blockChecker.getType() == Material.WOOL && blockChecker.getData() == Var.teamWool2TechnicalDye.getData()) {
 			lime = true;
 		}
 		blockChecker = enemyTeamWool1.getBlock();
 		if (blockChecker.getType() == Material.WOOL && blockChecker.getData() == Var.enemyTeamWool1TechnicalDye.getData()) {
 			yellow = true;
 		}
 		blockChecker = enemyTeamWool2.getBlock();
 		if (blockChecker.getType() == Material.WOOL && blockChecker.getData() == Var.enemyTeamWool2TechnicalDye.getData()) {
 			purple = true;
 		}
 		if (block.getType() == Material.WOOL && block.getData() == Var.teamWool1TechnicalDye.getData() && block.getLocation().equals(teamWool1)) {
 			Bukkit.broadcastMessage(ChatColor.AQUA + "Alert: The " + Var.teamWool1TechnicalColor + "" + ChatColor.BOLD + Var.teamWool1DisplayName + ChatColor.RESET + "" + ChatColor.AQUA + " wool has been placed for the " + Var.teamTechnicalColor + "" + ChatColor.BOLD + Var.teamDisplayName + ChatColor.RESET + ChatColor.AQUA + " team!");
 			if (orange && lime) {
 				Bukkit.broadcastMessage(Var.teamTechnicalColor + "" + ChatColor.BOLD + Var.teamDisplayName + ChatColor.RESET + ChatColor.AQUA + " has won the game!" + ChatColor.GREEN + " Good game!");
 				Scrimmage.gameActive = false;
 				for (Player i : Bukkit.getOnlinePlayers()) {
 					i.setGameMode(GameMode.CREATIVE);
 					i.setAllowFlight(true);
 					i.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 					i.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 					i.getInventory().clear();
 					i.getInventory().setHelmet(null);
 					i.getInventory().setChestplate(null);
 					i.getInventory().setLeggings(null);
 					i.getInventory().setBoots(null);
 					i.updateInventory();
 					Location loc = i.getLocation();
 					loc = Var.observerSpawn;
 					RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, i, loc);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 0L);
 				}
 			}
 		} else if (block.getLocation().equals(teamWool1)) {
 			event.setCancelled(true);
 		}
 		if (block.getType() == Material.WOOL && block.getData() == Var.teamWool2TechnicalDye.getData() && block.getLocation().equals(teamWool2)) {
 			Bukkit.broadcastMessage(ChatColor.AQUA + "Alert: The " + Var.teamWool2TechnicalColor + "" + ChatColor.BOLD + Var.teamWool2DisplayName + ChatColor.RESET + "" + ChatColor.AQUA + " wool has been placed for the " + Var.teamTechnicalColor + "" + ChatColor.BOLD + Var.teamDisplayName + ChatColor.RESET + ChatColor.AQUA + " team!");
 			if (orange && lime) {
 				Bukkit.broadcastMessage(Var.teamTechnicalColor + "" + ChatColor.BOLD + Var.teamDisplayName + ChatColor.RESET + ChatColor.AQUA + " has won the game!" + ChatColor.GREEN + " Good game!");
 				Scrimmage.gameActive = false;
 				for (Player i : Bukkit.getOnlinePlayers()) {
 					i.setGameMode(GameMode.CREATIVE);
 					i.setAllowFlight(true);
 					i.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 					i.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 					i.getInventory().clear();
 					i.getInventory().setHelmet(null);
 					i.getInventory().setChestplate(null);
 					i.getInventory().setLeggings(null);
 					i.getInventory().setBoots(null);
 					i.updateInventory();
 					Location loc = i.getLocation();
 					loc = Var.observerSpawn;
 					RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, i, loc);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 0L);
 				}
 			}
 		} else if (block.getLocation().equals(teamWool2)) {
 			event.setCancelled(true);
 		}
 		if (block.getType() == Material.WOOL && block.getData() == Var.enemyTeamWool1TechnicalDye.getData() && block.getLocation().equals(enemyTeamWool1)) {
 			Bukkit.broadcastMessage(ChatColor.AQUA + "Alert: The " + Var.enemyTeamWool1TechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamWool1DisplayName + ChatColor.RESET + "" + ChatColor.AQUA + " wool has been placed for the " + Var.enemyTeamTechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamDisplayName + ChatColor.RESET + ChatColor.AQUA + " team!");
 			if (yellow && purple) {
 				Bukkit.broadcastMessage(Var.enemyTeamTechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamDisplayName + ChatColor.RESET + ChatColor.AQUA + " has won the game!" + ChatColor.GREEN + " Good game!");
 				Scrimmage.gameActive = false;
 				for (Player i : Bukkit.getOnlinePlayers()) {
 					i.setGameMode(GameMode.CREATIVE);
 					i.setAllowFlight(true);
 					i.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 					i.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 					i.getInventory().clear();
 					i.getInventory().setHelmet(null);
 					i.getInventory().setChestplate(null);
 					i.getInventory().setLeggings(null);
 					i.getInventory().setBoots(null);
 					i.updateInventory();
 					Location loc = i.getLocation();
 					loc = Var.observerSpawn;
 					RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, i, loc);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 0L);
 				}
 
 			}
 		} else if (block.getLocation().equals(enemyTeamWool1)) {
 			event.setCancelled(true);
 		}
 		if (block.getType() == Material.WOOL && block.getData() == Var.enemyTeamWool2TechnicalDye.getData() && block.getLocation().equals(enemyTeamWool2)) {
 			Bukkit.broadcastMessage(ChatColor.AQUA + "Alert: The " + Var.enemyTeamWool2TechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamWool2DisplayName + ChatColor.RESET + "" + ChatColor.AQUA + " wool has been placed for the for the " + Var.enemyTeamTechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamDisplayName + ChatColor.RESET + ChatColor.AQUA + " team!");
 			if (yellow && purple) {
 				Bukkit.broadcastMessage(Var.enemyTeamTechnicalColor + "" + ChatColor.BOLD + Var.enemyTeamDisplayName + ChatColor.RESET + ChatColor.AQUA + " has won the game!" + ChatColor.GREEN + " Good game!");
 				Scrimmage.gameActive = false;
 				for (Player i : Bukkit.getOnlinePlayers()) {
 					i.setGameMode(GameMode.CREATIVE);
 					i.getAllowFlight();
 					i.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1));
 					i.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1));
 					i.getInventory().clear();
 					i.getInventory().setHelmet(null);
 					i.getInventory().setChestplate(null);
 					i.getInventory().setLeggings(null);
 					i.getInventory().setBoots(null);
 					i.updateInventory();
 					Location loc = i.getLocation();
 					loc = Var.observerSpawn;
 					RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, i, loc);
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 0L);
 				}
 
 			}
 		} else if (block.getLocation().equals(enemyTeamWool2)) {
 			event.setCancelled(true);
 		}
 
 	}
 
 	@EventHandler
 	public void onBlockBurn(BlockBurnEvent event) {
 		int x = 0;
 		int y = 0;
 		int z = 0;
 		int count = 0;
 		for (String i : Var.teamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.teamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location teamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool1Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		count = 0;
 		for (String i : Var.enemyTeamWool2Place) {
 			count++;
 			switch (count) {
 			case 1:
 				x = Integer.parseInt(i);
 			case 2:
 				y = Integer.parseInt(i);
 			case 3:
 				z = Integer.parseInt(i);
 			default:
 			}
 		}
 		Location enemyTeamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 		if (event.getBlock().getLocation().equals(teamWool1) || event.getBlock().getLocation().equals(teamWool2) || event.getBlock().getLocation().equals(enemyTeamWool1) || event.getBlock().getLocation().equals(enemyTeamWool2)) {
 
 		}
 	}
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		int count = 0;
 		String dir = "";
 		int loc = 0;
 		for (String i : Var.teamWoolRoom) {
 			count++;
 			if (count == 1) {
 				dir = i;
 			} else if (count == 2) {
 				loc = Integer.parseInt(i);
 			}
 		}
 		if (dir.contains("X")) {
 			if (dir.contains("+")) {
 				if (event.getPlayer().getLocation().getX() >= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setX(loc2.getX() - 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			} else if (dir.contains("-")) {
 				if (event.getPlayer().getLocation().getX() <= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setX(loc2.getX() + 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			}
 		} else if (dir.contains("Z")) {
 			if (dir.contains("+")) {
 				if (event.getPlayer().getLocation().getZ() >= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setZ(loc2.getZ() - 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			} else if (dir.contains("-")) {
 				if (event.getPlayer().getLocation().getZ() <= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setZ(loc2.getZ() + 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			}
 		}
 		count = 0;
 		dir = "";
 		loc = 0;
 		for (String i : Var.enemyTeamWoolRoom) {
 			count++;
 			if (count == 1) {
 				dir = i;
 			} else if (count == 2) {
 				loc = Integer.parseInt(i);
 			}
 		}
 		if (dir.contains("X")) {
 			if (dir.contains("+")) {
 				if (event.getPlayer().getLocation().getX() >= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setX(loc2.getX() - 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			} else if (dir.contains("-")) {
 				if (event.getPlayer().getLocation().getX() <= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setX(loc2.getX() + 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			}
 		} else if (dir.contains("Z")) {
 			if (dir.contains("+")) {
 				if (event.getPlayer().getLocation().getZ() >= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setZ(loc2.getZ() - 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			} else if (dir.contains("-")) {
 				if (event.getPlayer().getLocation().getZ() <= loc) {
 					if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 						event.setCancelled(true);
 						Location loc2 = event.getPlayer().getLocation();
 						loc2.setZ(loc2.getZ() + 0.2);
 						event.getPlayer().teleport(loc2);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		Location loc = player.getLocation();
 		if (player.getGameMode() == GameMode.CREATIVE) {
 			loc = Var.observerSpawn;
 		} else {
 			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 3));
 			for (Object e : Scrimmage.team) {
 				String i = (String) e;
 				if (player.getDisplayName().equals(i)) {
 					loc = Var.teamSpawn;
 				}
 			}
 			for (Object e : Scrimmage.enemyTeam) {
 				String i = (String) e;
 				if (player.getDisplayName().equals(i)) {
 					loc = Var.enemyTeamSpawn;
 				}
 			}
 		}
 		RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, player, loc);
 		for (int i = 1; i <= 20; i++)
 			Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 2L);
 		// player.teleport(loc);
 	}
 
 	@EventHandler
 	public void onInvClick(InventoryClickEvent event) {
 		HumanEntity player = event.getWhoClicked();
 		if (player.getGameMode() == GameMode.CREATIVE) {
 			event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		if (player.getGameMode() == GameMode.CREATIVE) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onItemPickup(PlayerPickupItemEvent event) {
 		Player player = event.getPlayer();
 		if (player.getGameMode() == GameMode.CREATIVE) {
 			event.setCancelled(true);
	}
 
 	@EventHandler
 	public void onWorldChange(PlayerChangedWorldEvent event) {
 		Player player = event.getPlayer();
 		if (player.getGameMode() == GameMode.CREATIVE && !(player.isOp())) {
 		} else {
 
 		}
 	}
 
 	@EventHandler
 	public void onStructureGrow(StructureGrowEvent event) {
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onWeatherChange(WeatherChangeEvent event) {
 		if (event.toWeatherState()) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onThunderChange(ThunderChangeEvent event) {
 		if (event.toThunderState()) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onLightningStrike(LightningStrikeEvent event) {
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Block block = event.getClickedBlock().getRelative(event.getBlockFace());
 			if (event.getMaterial() != Material.WOOL) {
 				int x = 0;
 				int y = 0;
 				int z = 0;
 				int count = 0;
 				for (String i : Var.teamWool1Place) {
 					count++;
 					switch (count) {
 					case 1:
 						x = Integer.parseInt(i);
 					case 2:
 						y = Integer.parseInt(i);
 					case 3:
 						z = Integer.parseInt(i);
 					default:
 					}
 				}
 				Location teamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 				count = 0;
 				for (String i : Var.teamWool2Place) {
 					count++;
 					switch (count) {
 					case 1:
 						x = Integer.parseInt(i);
 					case 2:
 						y = Integer.parseInt(i);
 					case 3:
 						z = Integer.parseInt(i);
 					default:
 					}
 				}
 				Location teamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 				count = 0;
 				for (String i : Var.enemyTeamWool1Place) {
 					count++;
 					switch (count) {
 					case 1:
 						x = Integer.parseInt(i);
 					case 2:
 						y = Integer.parseInt(i);
 					case 3:
 						z = Integer.parseInt(i);
 					default:
 					}
 				}
 				Location enemyTeamWool1 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 				count = 0;
 				for (String i : Var.enemyTeamWool2Place) {
 					count++;
 					switch (count) {
 					case 1:
 						x = Integer.parseInt(i);
 					case 2:
 						y = Integer.parseInt(i);
 					case 3:
 						z = Integer.parseInt(i);
 					default:
 					}
 				}
 				Location enemyTeamWool2 = new Location(Bukkit.getWorld(Var.mapName), x, y, z);
 				if (block.getLocation().equals(teamWool1)) {
 					event.setCancelled(true);
 				}
 				if (block.getLocation().equals(teamWool2)) {
 					event.setCancelled(true);
 				}
 				if (block.getLocation().equals(enemyTeamWool1)) {
 					event.setCancelled(true);
 				}
 				if (block.getLocation().equals(enemyTeamWool2)) {
 					event.setCancelled(true);
 				}
 			}
 			if (block.getLocation().getY() > Var.heightLimit) {
 				event.setCancelled(true);
 			}
 			Location loc = block.getLocation();
 			boolean isVoid = true;
 			for (int i = 0; i < 300; i++) {
 				loc.setY(i);
 				if (loc.getBlock().getType() != Material.AIR) {
 					isVoid = false;
 				}
 			}
 			if (isVoid) {
 				event.setCancelled(true);
 			}
 			int count1 = 0;
 			String dir = "";
 			int loc1 = 0;
 			for (String i : Var.teamWoolRoom) {
 				count1++;
 				if (count1 == 1) {
 					dir = i;
 				} else if (count1 == 2) {
 					loc1 = Integer.parseInt(i);
 				}
 			}
 			if (dir.contains("X")) {
 				if (dir.contains("+")) {
 					if (block.getLocation().getX() >= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				} else if (dir.contains("-")) {
 					if (block.getLocation().getX() <= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				}
 			} else if (dir.contains("Z")) {
 				if (dir.contains("+")) {
 					if (block.getLocation().getZ() >= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				} else if (dir.contains("-")) {
 					if (block.getLocation().getZ() <= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.teamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				}
 			}
 			count1 = 0;
 			dir = "";
 			loc1 = 0;
 			for (String i : Var.enemyTeamWoolRoom) {
 				count1++;
 				if (count1 == 1) {
 					dir = i;
 				} else if (count1 == 2) {
 					loc1 = Integer.parseInt(i);
 				}
 			}
 			if (dir.contains("X")) {
 				if (dir.contains("+")) {
 					if (block.getLocation().getX() >= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				} else if (dir.contains("-")) {
 					if (block.getLocation().getX() <= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				}
 			} else if (dir.contains("Z")) {
 				if (dir.contains("+")) {
 					if (block.getLocation().getZ() >= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				} else if (dir.contains("-")) {
 					if (block.getLocation().getZ() <= loc1) {
 						if (Scrimmage.team(event.getPlayer()).equals(Var.enemyTeamDisplayName)) {
 							event.setCancelled(true);
 						}
 					}
 				}
 			}
 		}
 		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
 			event.setCancelled(true);
 		}
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			if (event.getMaterial().getId() == 111) {
 				if (event.getPlayer().getGameMode() == GameMode.CREATIVE && event.getClickedBlock().getTypeId() == 111) {
 					event.setCancelled(true);
 					RemoveBlock removeBlock = new RemoveBlock(plugin, event.getClickedBlock());
 					for (long i = 1; i <= 20; i++)
 						Bukkit.getServer().getScheduler().runTaskLater(plugin, removeBlock, i);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
 		if (event.getDamager() instanceof Player) {
 			Player player = (Player) event.getDamager();
 			if (player.getGameMode() == GameMode.CREATIVE) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		event.setCancelled(true);
 		String message = "";
 		Player player = event.getPlayer();
 		String chat = "spec";
 		for (Object e : Scrimmage.team) {
 			String i = (String) e;
 			if (player.getDisplayName().equals(i)) {
 				chat = Var.teamDisplayName;
 			}
 		}
 		for (Object e : Scrimmage.enemyTeam) {
 			String i = (String) e;
 			if (player.getDisplayName().equals(i)) {
 				chat = Var.enemyTeamDisplayName;
 			}
 		}
 				if (chat == "spec") {
 					message = ChatColor.AQUA + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.teamDisplayName) {
 					message = Var.teamTechnicalColor + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.enemyTeamDisplayName) {
 					message = Var.enemyTeamTechnicalColor + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 			/*
 			 * if (player.getDisplayName().equals("Barnyard_Owl")) { if (chat ==
 			 * "spec") { message = VariableHandler.enemyTeamTechnicalColor +
 			 * "�?�" + ChatColor.AQUA + " [T] " + ChatColor.RESET +
 			 * player.getDisplayName() + ": " + event.getMessage(); } if (chat
 			 * == VariableHandler.teamDisplayName) { message =
 			 * VariableHandler.enemyTeamTechnicalColor + "�?�" +
 			 * VariableHandler.teamTechnicalColor + " [T] " + ChatColor.RESET +
 			 * player.getDisplayName() + ": " + event.getMessage(); } if (chat
 			 * == VariableHandler.enemyTeamDisplayName) { message =
 			 * VariableHandler.enemyTeamTechnicalColor + "�?�" +
 			 * VariableHandler.enemyTeamTechnicalColor + " [T] " +
 			 * ChatColor.RESET + player.getDisplayName() + ": " +
 			 * event.getMessage(); } } else
 			 */
 			if (player.isOp()) {
 				if (chat == "spec") {
 					message = ChatColor.AQUA + "[Team] " + ChatColor.GREEN + "*" + ChatColor.AQUA + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.teamDisplayName) {
 					message = Var.teamTechnicalColor + "[Team] " + ChatColor.GREEN + "*" + Var.teamTechnicalColor + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.enemyTeamDisplayName) {
 					message = Var.enemyTeamTechnicalColor + "[Team] " + ChatColor.GREEN + "*" + Var.enemyTeamTechnicalColor + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 			} else {
 				if (chat == "spec") {
 					message = ChatColor.AQUA + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.teamDisplayName) {
 					message = Var.teamTechnicalColor + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 				if (chat == Var.enemyTeamDisplayName) {
 					message = Var.enemyTeamTechnicalColor + "[Team] " + player.getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
 				}
 			}
 			if (chat == "spec") {
 				for (Player p : Bukkit.getOnlinePlayers()) {
 					boolean onTeam = false;
 					for (Object e : Scrimmage.team) {
 						String i = (String) e;
 						if (p.getDisplayName().equals(i)) {
 							onTeam = true;
 						}
 					}
 					for (Object e : Scrimmage.enemyTeam) {
 						String i = (String) e;
 						if (p.getDisplayName().equals(i)) {
 							onTeam = true;
 						}
 					}
 					if (!(onTeam)) {
 						p.sendMessage(message);
 					}
 				}
 			}
 			if (chat == Var.teamDisplayName) {
 				for (Player p : Bukkit.getOnlinePlayers()) {
 					for (Object e : Scrimmage.team) {
 						String i = (String) e;
 						if (p.getDisplayName().equals(i)) {
 							p.sendMessage(message);
 						}
 					}
 				}
 			}
 			if (chat == Var.enemyTeamDisplayName) {
 				for (Player p : Bukkit.getOnlinePlayers()) {
 					for (Object e : Scrimmage.enemyTeam) {
 						String i = (String) e;
 						if (p.getDisplayName().equals(i)) {
 							p.sendMessage(message);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// Credit to Chronicals for this nifty bit of code~
 	@SuppressWarnings("unused")
 	private static String credit = "Credit goes to Chronicals for the onServerListPing and onPlayerLogin methods";
 
 	public void onServerListPing(ServerListPingEvent paramServerListPingEvent) {
 		paramServerListPingEvent.setMaxPlayers(64);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerLogin(PlayerLoginEvent paramPlayerLoginEvent) {
 		if (Bukkit.getServer().getOnlinePlayers().length >= 64) {
 			paramPlayerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, "Server is full, you cannot log in!");
 		} else if (paramPlayerLoginEvent.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) {
 			paramPlayerLoginEvent.allow();
 		}
 	}
 
 	// End Chronicals code~
 	@EventHandler
 	public void onBlockFromTo(BlockFromToEvent event) {
 		Block block = event.getToBlock();
 		Location loc = block.getLocation();
 		boolean noBlock = true;
 		for (int i = 0; i <= 255; i++) {
 			loc.setY(i);
 			if (loc.getBlock().getType() != Material.AIR) {
 				noBlock = false;
 			}
 		}
 		if (noBlock) {
 			event.setCancelled(true);
 		}
 		if (event.getBlock().getLocation().getZ() <= -103 && event.getToBlock().getLocation().getZ() >= -104) {
 			event.setCancelled(true);
 		}
 		if (event.getBlock().getLocation().getZ() <= 118 && event.getToBlock().getLocation().getZ() >= 119) {
 			event.setCancelled(true);
 		}
 	}
 
 	/*
 	 * @EventHandler public void onProjectileHit(ProjectileHitEvent event) {
 	 * Entity entity = event.getEntity(); double radius = 10; double
 	 * radiusSquared = radius * radius;
 	 * 
 	 * List<Entity> entities = entity .getNearbyEntities(radius, radius,
 	 * radius); // All entities // withing a box for (Entity i : entities) {
 	 * 
 	 * if (i.getLocation().distanceSquared(entity.getLocation()) >
 	 * radiusSquared) continue; // All entities within a sphere
 	 * 
 	 * if (i instanceof Player) { if (((Player) i).getGameMode() ==
 	 * GameMode.CREATIVE) { Vector v = entity.getVelocity(); v = v.multiply(2D);
 	 * i.setVelocity(v); RespawnArrow respawnArrow = new RespawnArrow(plugin,
 	 * entity.getLocation(), entity.getVelocity());
 	 * Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnArrow, 1L);
 	 * } }
 	 * 
 	 * } }
 	 */
 	@EventHandler
 	public void onBlockPhysics(BlockPhysicsEvent event) {
 		// event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onArrowPickup(PlayerPickupItemEvent event) {
 		if (event.getItem() == new ItemStack(Material.ARROW)) {
 			event.setCancelled(true);
 		}
 	}
 }
