 package us.fitzpatricksr.cownet.commands;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import us.fitzpatricksr.cownet.CowNetMod;
 import us.fitzpatricksr.cownet.commands.gatheredgame.GameStats;
 import us.fitzpatricksr.cownet.commands.gatheredgame.GameStatsMemory;
 import us.fitzpatricksr.cownet.commands.gatheredgame.GatheredGame;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 
 /**
  */
 public class TntWars extends GatheredGame implements org.bukkit.event.Listener {
 	private static final long GAME_FREQUENCY = 4;  // 5 times a second?
 	private static final String KILLS_KEY = "kills";
 	private static final String DEATHS_KEY = "deaths";
 	private static final String BOMBS_PLACED_KEY = "bombsPlaced";
 	private final Random rand = new Random();
 
 	@Setting
 	private int minPlayers = 2;
 	@Setting
 	private String loungeWarpName = "tntWarsLounge";
 	@Setting
 	private String spawnWarpName = "tntWarsSpawn";
 	@Setting
 	private int maxBlockPlacements = 1;
 	@Setting
 	private long explosionDelay = 5 * 1000; // 3 seconds
 	@Setting
 	private int explosionRadius = 6;
 	@Setting
 	private int spawnJiggle = 5;
 	@Setting
 	private int explosivePower = 0;
 	// manual setting
 	private Material explosiveBlockType = Material.TNT;
 
 	private HashMap<String, LinkedList<BombPlacement>> placements;
 	private GameStats tempStats;
 	private int gameTaskId;
 
 
 	// --------------------------------------------------------------
 	// ---- Settings management
 
 	@Override
 	// reload any settings not handled by @Setting
 	protected void reloadManualSettings() throws Exception {
		super.reloadManualSettings();
 		this.explosiveBlockType = Material.matchMaterial(getConfigValue("explosiveBlockType", explosiveBlockType.toString()));
 	}
 
 	// return any custom settings that are not handled by @Settings code
 	protected HashMap<String, String> getManualSettings() {
		HashMap<String, String> result = super.getManualSettings();
 		result.put("explosiveBlockType", explosiveBlockType.toString());
 		return result;
 	}
 
 	// update a setting that was not handled by @Setting and return true if it has been updated.
 	protected boolean updateManualSetting(String settingName, String settingValue) {
 		if (settingName.equalsIgnoreCase("explosiveBlockType")) {
 			explosiveBlockType = Material.valueOf(settingValue);
 			updateConfigValue("explosiveBlockType", settingValue);
			return true;
 		} else {
			return super.updateManualSetting(settingName, settingValue);
 		}
 	}
 
 	@Override
 	protected String getGameName() {
 		return "TntWars";
 	}
 
 	@Override
 	protected int getMinPlayers() {
 		return minPlayers;
 	}
 
 	@Override
 	protected String[] getHelpText(CommandSender player) {
 		return new String[] {
 				"usage: /" + getTrigger() + " join | info | quit | tp <player> | start",
 				"   join - join the games",
 				"   info - what's the state of the current game?",
 				"   quit - chicken out and just watch",
 				"   tp <player> - transport to a player, if you're a spectator",
 				"   start - just get things started already!",
 				"   scores - how players did last game.",
 				"   stats <player> - see someone's lifetime stats.",
 				"   leaders <kills | deaths | bombs | stealth | accuracy> - Global rank in a category."
 		};
 	}
 
 	// --------------------------------------------------------------
 	// ---- user commands
 
 	@CowCommand
 	private boolean doScores(CommandSender sender) {
 		if (tempStats != null) {
 			sender.sendMessage("Scores for most recent game: ");
 			for (String playerName : tempStats.getPlayerNames()) {
 				double k = tempStats.getStat(playerName, KILLS_KEY);
 				double d = tempStats.getStat(playerName, DEATHS_KEY);
 				double b = tempStats.getStat(playerName, BOMBS_PLACED_KEY);
 				double accuracy = (b != 0) ? k / b : 0;
 				double stealth = (d != 0) ? k / d : k;
 				sender.sendMessage("  " + StringUtils.fitToColumnSize(playerName, 10) + " accuracy = " + StringUtils.fitToColumnSize(Double.toString(accuracy), 5) + " stealth = " + StringUtils.fitToColumnSize(Double.toString(stealth), 5));
 			}
 		}
 		return true;
 	}
 
 	@CowCommand
 	private boolean doStats(Player player) {
 		return doStats(player, player.getName());
 	}
 
 	@CowCommand
 	private boolean doStats(CommandSender player, String playerName) {
 		double k = getStats().getStat(playerName, KILLS_KEY);
 		double d = getStats().getStat(playerName, DEATHS_KEY);
 		double b = getStats().getStat(playerName, BOMBS_PLACED_KEY);
 		double accuracy = (b != 0) ? k / b : 0;
 		double stealth = (d != 0) ? k / d : k;
 		player.sendMessage("Your stats: ");
 		player.sendMessage("  kills: " + k);
 		player.sendMessage("  deaths: " + d);
 		player.sendMessage("  bombs: " + b);
 		player.sendMessage("  accuracy = " + StringUtils.fitToColumnSize(Double.toString(accuracy * 100), 5) + "%");
 		player.sendMessage("  stealth = " + StringUtils.fitToColumnSize(Double.toString(stealth * 100), 5) + "%");
 		return true;
 	}
 
 	@CowCommand
 	private boolean doLeadersKills(CommandSender sender) {
 		sender.sendMessage("Top killers: ");
 		dumpLeaders(sender, getStats().getStatSummary(KILLS_KEY));
 		return true;
 	}
 
 	@CowCommand
 	private boolean doLeadersDeaths(CommandSender sender) {
 		sender.sendMessage("Most likely to die: ");
 		dumpLeaders(sender, getStats().getStatSummary(DEATHS_KEY));
 		return true;
 	}
 
 	@CowCommand
 	private boolean doLeadersBombs(CommandSender sender) {
 		sender.sendMessage("Top bombers: ");
 		dumpLeaders(sender, getStats().getStatSummary(BOMBS_PLACED_KEY));
 		return true;
 	}
 
 	@CowCommand
 	private boolean doLeadersAccuracy(CommandSender sender) {
 		sender.sendMessage("Most accurate: ");
 		HashMap<String, Double> accuracy = new HashMap<String, Double>();
 		for (String playerName : getStats().getPlayerNames()) {
 			double k = getStats().getStat(playerName, KILLS_KEY);
 			double b = getStats().getStat(playerName, BOMBS_PLACED_KEY);
 			double a = (b != 0) ? k / b : 0;
 			accuracy.put(playerName, a * 100);
 		}
 		dumpLeaders(sender, accuracy);
 		return true;
 	}
 
 	@CowCommand
 	private boolean doLeadersStealth(CommandSender sender) {
 		sender.sendMessage("Top stealthy: ");
 		HashMap<String, Double> stealth = new HashMap<String, Double>();
 		for (String playerName : getStats().getPlayerNames()) {
 			double k = getStats().getStat(playerName, KILLS_KEY);
 			double d = getStats().getStat(playerName, DEATHS_KEY);
 			double s = (d != 0) ? k / d : k;
 			stealth.put(playerName, s * 100);
 		}
 		dumpLeaders(sender, stealth);
 		return true;
 	}
 
 	private void dumpLeaders(CommandSender sender, Map<String, Double> map) {
 		TreeMap<Double, String> sortedMap = new TreeMap<Double, String>();
 		for (Map.Entry<String, Double> entry : map.entrySet()) {
 			sortedMap.put(entry.getValue(), entry.getKey());
 		}
 		for (Map.Entry entry : sortedMap.entrySet()) {
 			sender.sendMessage("  " + StringUtils.fitToColumnSize(entry.getValue().toString(), 15) + ": " + StringUtils.fitToColumnSize(entry.getKey().toString(), 5));
 		}
 	}
 
 	// --------------------------------------------------------------
 	// ---- game state transitions
 
 	@Override
 	protected void handleGathering() {
 		debugInfo("handleGathering");
 		broadcastToAllOnlinePlayers("A game is gathering.  To join use /" + getTrigger() + " join.");
 	}
 
 	@Override
 	protected void handleLounging() {
 		debugInfo("handleLounging");
 		broadcastToAllOnlinePlayers("All the players are ready.  The games are about to start.");
 		// teleport everyone to the lounge
 		Location loc = getWarpPoint(loungeWarpName);
 		if (loc != null) {
 			// hey jf - you need to jiggle this a bit or everyone will be on top of each other
 			Server server = getPlugin().getServer();
 			for (String playerName : getActivePlayers()) {
 				Player player = server.getPlayer(playerName);
 				player.teleport(loc);
 			}
 		}
 	}
 
 	@Override
 	protected void handleInProgress() {
 		debugInfo("handleInProgress");
 		broadcastToAllOnlinePlayers("Let the games begin!");
 		tempStats = new GameStatsMemory();
 
 		// teleport everyone to the spawn point to start the game
 		Location loc = getWarpPoint(spawnWarpName);
 		if (loc != null) {
 			Server server = getPlugin().getServer();
 			for (String playerName : getActivePlayers()) {
 				Player player = server.getPlayer(playerName);
 				player.teleport(loc);
 				giveTnt(playerName);
 			}
 		}
 		placements = new HashMap<String, LinkedList<BombPlacement>>();
 		Set<String> players = getActivePlayers();
 		for (String playerName : players) {
 			placements.put(playerName, new LinkedList<BombPlacement>());
 		}
 	}
 
 	@Override
 	protected void handleEnded() {
 		debugInfo("handleEnded");
 		stopBombWatcher();
 		placements = null;
 		for (String player : getActivePlayers()) {
 			removeTnt(player);
 		}
 		try {
 			getStats().saveConfig();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		broadcastToAllOnlinePlayers("The game has ended.");
 		for (String playerName : getActivePlayers()) {
 			Player player = getPlayer(playerName);
 			player.sendMessage("Scores this game:");
 			doScores(player);
 		}
 	}
 
 	@Override
 	protected void handleFailed() {
 		debugInfo("handleFailed");
 		stopBombWatcher();
 		placements = null;
 		for (String player : getActivePlayers()) {
 			removeTnt(player);
 		}
 		try {
 			getStats().saveConfig();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		broadcastToAllOnlinePlayers("The game has been canceled.");
 	}
 
 	@Override
 	protected boolean handlePlayerAdded(String playerName) {
 		// just add anyone who wants to be added
 		debugInfo("handlePlayerAdded");
 		broadcastToAllOnlinePlayers(playerName + " has joined the game.");
 		if (isGameLounging()) {
 			Location warp = getWarpPoint(loungeWarpName);
 			if (warp != null) {
 				getPlayer(playerName).teleport(warp);
 			}
 		} else if (isGameInProgress()) {
 			Player player = getPlayer(playerName);
 			Location warp = getWarpPoint(spawnWarpName);
 			if (warp != null) {
 				player.teleport(warp);
 			}
 			ItemStack itemInHand = new ItemStack(Material.TNT, 1);
 			player.setItemInHand(itemInHand);
 		}
 		return true;
 	}
 
 	@Override
 	protected void handlePlayerLeft(String playerName) {
 		// should remove their bombs
 		// remove that player's tnt
 		debugInfo("handlePlayerLeft");
 		if (placements != null) {
 			// placements is only non-null if the game has begun.
 			placements.remove(playerName);
 		}
 		broadcastToAllOnlinePlayers(playerName + " has left the game.");
 		if (!isGameGathering()) {
 			// we've already given them TNT, we need to remove at least 1 TNT from their inventory
 			removeTnt(playerName);
 		}
 	}
 
 	public boolean gameIsInProgress() {
 		return placements != null;
 	}
 
 	private Location getWarpPoint(String warpName) {
 		CowNetMod plugin = (CowNetMod) getPlugin();
 		CowWarp warpThingy = (CowWarp) plugin.getThingy("cowwarp");
 		Location loc = warpThingy.getWarpLocation(warpName);
 		if (spawnJiggle > 0) {
 			int dx = rand.nextInt(spawnJiggle * 2 + 1) - spawnJiggle - 1; // -5..5
 			int dz = rand.nextInt(spawnJiggle * 2 + 1) - spawnJiggle - 1; // -5..5
 			loc.add(dx, 0, dz);
 			loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
 			loc.add(0, 1, 0);
 		}
 		return loc;
 	}
 
 	private void giveTnt(String playerName) {
 		//give everyone TNT in hand
 		Player player = getPlayer(playerName);
 		ItemStack oldItemInHand = player.getItemInHand();
 		ItemStack itemInHand = new ItemStack(Material.TNT, 1);
 		player.setItemInHand(itemInHand);
 		player.getInventory().addItem(oldItemInHand);
 		player.updateInventory();
 	}
 
 	private void removeTnt(String playerName) {
 		Player player = getPlayer(playerName);
 		Inventory inventory = player.getInventory();
 		int slot = inventory.first(Material.TNT);
 		ItemStack stack = inventory.getItem(slot);
 		stack.setAmount(stack.getAmount() - 1);
 		inventory.setItem(slot, stack);
 		player.updateInventory();
 	}
 
 	private Player getPlayer(String playerName) {
 		Server server = getPlugin().getServer();
 		return server.getPlayer(playerName);
 	}
 
 	// --------------------------------------------------------------
 	// ---- explosive mgmt
 
 	/* place a bomb at players current location? */
 	private boolean placeBomb(Player player, Location loc) {
 		String playerName = player.getName();
 		if (playerIsAlive(playerName)) {
 			// check to see if they've already placed the maximum number of blocks.
 			LinkedList<BombPlacement> placementList = placements.get(playerName);
 			if (placementList.size() < maxBlockPlacements) {
 				// place a bomb
 				placementList.addLast(new BombPlacement(player, loc));
 				startBombWatcher();
 				accumulatStats(player.getName(), BOMBS_PLACED_KEY, 1);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private LinkedList<BombPlacement> getBombsToExplode() {
 		LinkedList<BombPlacement> result = new LinkedList<BombPlacement>();
 		for (String playerName : placements.keySet()) {
 			LinkedList<BombPlacement> bombPlacements = placements.get(playerName);
 			while ((bombPlacements.size() > 0) && bombPlacements.getFirst().shouldExplode()) {
 				result.add(bombPlacements.getFirst());
 				bombPlacements.removeFirst();
 			}
 		}
 		if (placements.size() <= 0) {
 			stopBombWatcher();
 		}
 		return result;
 	}
 
 	private class BombPlacement {
 		public Player placer;
 		public Location location;
 		public long blockPlacedTime;
 
 		public BombPlacement(Player placer, Location loc) {
 			this.placer = placer;
 			this.location = loc;
 			blockPlacedTime = System.currentTimeMillis();
 			location.getWorld().playEffect(location, Effect.ENDER_SIGNAL, 2);
 		}
 
 		public boolean shouldExplode() {
 			return System.currentTimeMillis() - blockPlacedTime > explosionDelay;
 		}
 
 		public void doExplosion() {
 			placer.sendMessage("Boom!");
 			location.getWorld().createExplosion(location, explosivePower);
 			Server server = getPlugin().getServer();
 			long radiusSquared = explosionRadius * explosionRadius;
 			for (String playerName : getActivePlayers()) {
 				Player player = server.getPlayer(playerName);
 				if (player != null) {
 					Location playerLocation = player.getLocation();
 					if (location.distanceSquared(playerLocation) < radiusSquared) {
 						//this player is in the blast zone.
 						//chalk up a kill and a death.
 						accumulatStats(playerName, DEATHS_KEY, 1);
 						if (!playerName.equals(placer.getName())) {
 							//you only get a kill if you don't kill yourself
 							accumulatStats(placer.getName(), KILLS_KEY, 1);
 						}
 						Location dest = getWarpPoint(spawnWarpName);
 						player.teleport(dest);
 						for (int i = 0; i < 10; i++) {
 							location.getWorld().playEffect(dest, Effect.SMOKE, rand.nextInt(9));
 						}
 						player.sendMessage("You were blown up by " + placer.getDisplayName());
 						placer.sendMessage("You blew up " + player.getDisplayName());
 					}
 				}
 			}
 		}
 	}
 
 	// --------------------------------------------------------------
 	// ---- Stats
 
 	private void accumulatStats(String playerName, String statName, int amount) {
 		getStats().accumulate(playerName, statName, amount);
 		tempStats.accumulate(playerName, statName, amount);
 	}
 
 	// --------------------------------------------------------------
 	// ---- Event handlers
 
 	@EventHandler(ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (!gameIsInProgress()) return;
 		debugInfo("BlockPlaceEvent");
 		// if it's an explosive block, update state
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		if (playerIsAlive(playerName)) {
 			// hey jf - do you want to look at the block in hand or block placed?
 			if (event.getBlock().getType().equals(explosiveBlockType)) {
 				// if they already have an unexploded block, just cancel event
 				if (placeBomb(player, event.getBlock().getLocation())) {
 					// remove the item in hand (ex. decrement count by one)
 					// ItemStack itemInHand = player.getItemInHand();
 					// itemInHand.setAmount(itemInHand.getAmount() - 1);
 				} else {
 					player.sendMessage("You already have the maximum number of explosive placed.");
 				}
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		if (!gameIsInProgress()) return;
 		// register a loss and teleport back to spawn point
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		if (playerIsAlive(playerName)) {
 			// Just teleport the person back to spawn here.
 			// losses and announcements are done when the player is killed.
 			Location loc = getWarpPoint(spawnWarpName);
 			if (loc != null) {
 				// hey jf - you need to jiggle this a bit or everyone will be on top of each other
 				// have the player respawn in the game spawn
 				event.setRespawnLocation(loc);
 			}
 		}
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		debugInfo("PlayerQuitEvent");
 		String playerName = event.getPlayer().getName();
 		// remove player from game.  this will cause handlePlayerLeft to be called.
 		removePlayerFromGame(playerName);
 	}
 
 	// --------------------------------------------------------------
 	// ---- Event handlers
 
 	private void startBombWatcher() {
 		if (gameTaskId == 0) {
 			debugInfo("startBombWatcher");
 			gameTaskId = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
 				public void run() {
 					bombWatcher();
 				}
 			}, GAME_FREQUENCY, GAME_FREQUENCY);
 		}
 	}
 
 	private void stopBombWatcher() {
 		if (gameTaskId != 0) {
 			debugInfo("stopBombWatcher");
 			getPlugin().getServer().getScheduler().cancelTask(gameTaskId);
 			gameTaskId = 0;
 		}
 	}
 
 	private void bombWatcher() {
 		//debugInfo("bombWatcher");
 		for (BombPlacement bomb : getBombsToExplode()) {
 			bomb.doExplosion();
 		}
 	}
 }
