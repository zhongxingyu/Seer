 package graindcafe.tribu;
 
 import graindcafe.tribu.executors.CmdDspawn;
 import graindcafe.tribu.executors.CmdIspawn;
 import graindcafe.tribu.executors.CmdTribu;
 import graindcafe.tribu.executors.CmdZspawn;
 import graindcafe.tribu.listeners.TribuBlockListener;
 import graindcafe.tribu.listeners.TribuEntityListener;
 import graindcafe.tribu.listeners.TribuPlayerListener;
 import graindcafe.tribu.listeners.TribuWorldListener;
 
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import me.graindcafe.gls.DefaultLanguage;
 import me.graindcafe.gls.Language;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Tribu extends JavaPlugin {
 	public static String getExceptionMessage(Exception e) {
 		String message = e.getLocalizedMessage() + "\n";
 		for (StackTraceElement st : e.getStackTrace())
 			message += "[" + st.getFileName() + ":" + st.getLineNumber() + "] " + st.getClassName() + "->" + st.getMethodName() + "\n";
 		return message;
 	}
 
 	private int aliveCount;
 	private TribuBlockListener blockListener;
 	private BlockTrace blockTrace;
 	private boolean dedicatedServer = false;
 	private TribuEntityListener entityListener;
 	private HashMap<Player, TribuInventory> inventories;
 	private boolean isRunning;
 	private Language language;
 
 	private TribuLevel level;
 	private LevelFileLoader levelLoader;
 	private LevelSelector levelSelector;
 
 	private Logger log;
 	private TribuPlayerListener playerListener;
 	private HashMap<Player, PlayerStats> players;
 	private Random rnd;
 
 	private LinkedList<PlayerStats> sortedStats;
 	private TribuSpawner spawner;
 	private SpawnTimer spawnTimer;
 	private HashMap<Player, TribuInventory> tempInventories;
 
 	private boolean waitingForPlayers = false;
 	private WaveStarter waveStarter;
 	private TribuWorldListener worldListener;
 
 	public void addPlayer(Player player) {
 		if (player != null && !players.containsKey(player)) {
 
 			if (getConfig().getBoolean("Players.StoreInventory", false)) {
 				inventories.put(player, new TribuInventory(player, true));
 				if (player.getInventory() != null)
 					player.getInventory().clear();
 			}
 			PlayerStats stats = new PlayerStats(player);
 			players.put(player, stats);
 			sortedStats.add(stats);
 			if (waitingForPlayers)
 				startRunning();
 			else if (getLevel() != null && isRunning) {
 				player.teleport(level.getDeathSpawn());
 				player.sendMessage(language.get("Message.GameInProgress"));
 			}
 		}
 	}
 
 	public void checkAliveCount() {
 		if (aliveCount == 0 && isRunning) {
 			stopRunning();
 			getServer().broadcastMessage(language.get("Message.ZombieHavePrevailed"));
 			getServer().broadcastMessage(String.format(language.get("Message.YouHaveReachedWave"), String.valueOf(getWaveStarter().getWaveNumber())));
 			if (getPlayersCount() != 0)
 				getLevelSelector().startVote(Constants.VoteDelay);
 		}
 	}
 
 	public int getAliveCount() {
 		return aliveCount;
 	}
 
 	public BlockTrace getBlockTrace() {
 		return blockTrace;
 	}
 
 	public TribuLevel getLevel() {
 		return level;
 	}
 
 	public LevelFileLoader getLevelLoader() {
 		return levelLoader;
 	}
 
 	public LevelSelector getLevelSelector() {
 		return levelSelector;
 	}
 
 	public String getLocale(String key) {
 		/*
 		 * String r = language.get(key); if (r == null) { LogWarning(key +
 		 * " not found"); r = ChatColor.RED +
 		 * "An error occured while getting this message"; } return r;
 		 */
 		return language.get(key);
 	}
 
 	public Set<Player> getPlayers() {
 		return this.players.keySet();
 	}
 
 	public int getPlayersCount() {
 		return this.players.size();
 	}
 
 	public Player getRandomPlayer() {
 		return sortedStats.get(rnd.nextInt(sortedStats.size())).getPlayer();
 	}
 
 	public LinkedList<PlayerStats> getSortedStats() {
 
 		Collections.sort(this.sortedStats);
 		/*
 		 * Iterator<PlayerStats> i=this.sortedStats.iterator(); while
 		 * (i.hasNext()) { PlayerStats ps = i.next();
 		 * LogInfo(ps.getPlayer().getDisplayName() +" "+ ps.getPoints()); }
 		 */
 		return this.sortedStats;
 	}
 
 	public TribuSpawner getSpawner() {
 		return spawner;
 	}
 
 	public SpawnTimer getSpawnTimer() {
 		return spawnTimer;
 	}
 
 	public PlayerStats getStats(Player player) {
 		return players.get(player);
 	}
 
 	public WaveStarter getWaveStarter() {
 		return waveStarter;
 	}
 
 	private void initConfig() {
 		getConfig().options().header("# Tribu Config File Version " + Constants.ConfigFileVersion + " \n");
 		HashMap<String, Object> DefaultConfiguration = new HashMap<String, Object>() {
 			private static final long serialVersionUID = 1L;
 
 			{
 				put("PluginMode.ServerExclusive", false);
 				put("PluginMode.Language", "english");
 				put("PluginMode.AutoStart", false);
 				put("PluginMode.DefaultLevel", "");
 				put("WaveStart.SetTime", true);
 				put("WaveStart.SetTimeTo", 37000);
 				put("WaveStart.Delay", 10);
 				put("WaveStart.TeleportPlayers", false);
 				put("WaveStart.HealPlayers", true);
 				put("Zombies.Quantity", Arrays.asList(0.5, 1.0, 1.0));
 				put("Zombies.Health", Arrays.asList(.5, 4.0));
 				put("Zombies.FireResistant", false);
 				put("Zombies.Focus", "None");
 				put("Stats.OnZombieKill.Money", 15);
 				put("Stats.OnZombieKill.Points", 10);
 				put("Stats.OnPlayerDeath.Money", 10000);
 				put("Stats.OnPlayerDeath.Points", 50);
 				put("Players.DontLooseItem", false);
 				put("Players.StoreInventory", false);
 				put("Players.RevertBlocksChanges", true);
 				put("Signs.ShopSign.DropItem", true);
 				
 			}
 		};
 		
 		for (String key : getConfig().getKeys(true)) {
 			DefaultConfiguration.remove(key);
 		}
 		// Add missings keys
 		for (Entry<String, Object> e : DefaultConfiguration.entrySet()) {
 			getConfig().set(e.getKey(), e.getValue());
 		}
 		// Create the file if it doesn't exist
 		try {
			getConfig().save("config.yml");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	private void initLanguage() {
 		DefaultLanguage.setAuthor("Graindcafe");
 		DefaultLanguage.setName("English");
 		DefaultLanguage.setVersion(Constants.LanguageFileVersion);
 		DefaultLanguage.setLanguagesFolder(getDataFolder().getPath() + "/languages/");
 		DefaultLanguage.setLocales(new HashMap<String, String>() {
 			private static final long serialVersionUID = 9166935722459443352L;
 			{
 				put("File.DefaultLanguageFile",
 						"# This is your default language file \n# You should not edit it !\n# Create another language file (custom.yml) \n# and put 'Default: english' if your default language is english\n");
 				put("File.LanguageFileComplete", "# Your language file is complete\n");
 				put("File.TranslationsToDo", "# Translations to do in this language file\n");
 				put("Sign.Buy", "Buy");
 				put("Sign.ToggleSpawner", "Spawn's switch");
 				put("Sign.Spawner", "Zombie Spawner");
 				put("Sign.HighscoreNames", "Top Names");
 				put("Sign.HighscorePoints", "Top Points");
 				put("Sign.TollSign", "Pay");
 				put("Message.Stats", ChatColor.GREEN + "Ranking of  best zombies killers : ");
 				put("Message.UnknownItem", ChatColor.YELLOW + "Sorry, unknown item");
 				put("Message.ZombieSpawnList", ChatColor.GREEN + "%s");
 				put("Message.ConfirmDeletion", ChatColor.YELLOW + "Please confirm the deletion of the %s level by redoing the command");
 				put("Message.ThisOperationIsNotCancellable", ChatColor.RED + "This operation is not cancellable!");
 				put("Message.LevelUnloaded", ChatColor.GREEN + "Level successfully unloaded");
 				put("Message.InvalidVote", ChatColor.RED + "Invalid vote");
 				put("Message.ThankyouForYourVote", ChatColor.GREEN + "Thank you for your vote");
 				put("Message.YouCannotVoteAtThisTime", ChatColor.RED + "You cannot vote at this time");
 				put("Message.LevelLoadedSuccessfully", ChatColor.GREEN + "Level loaded successfully");
 				put("Message.LevelIsAlreadyTheCurrentLevel", ChatColor.RED + "Level %s is already the current level");
 				put("Message.UnableToSaveLevel", ChatColor.RED + "Unable to save level, try again later");
 				put("Message.UnableToLoadLevel", ChatColor.RED + "Unable to load level");
 				put("Message.NoLevelLoaded", "No level loaded, type '/tribu load' to load one,");
 				put("Message.NoLevelLoaded2", "or '/tribu create' to create a new one,");
 				put("Message.TeleportedToDeathSpawn", ChatColor.GREEN + "Teleported to death spawn");
 				put("Message.DeathSpawnSet", ChatColor.GREEN + "Death spawn set.");
 				put("Message.TeleportedToInitialSpawn", ChatColor.GREEN + "Teleported to initial spawn");
 				put("Message.InitialSpawnSet", ChatColor.GREEN + "Initial spawn set.");
 				put("Message.UnableToSaveCurrentLevel", ChatColor.RED + "Unable to save current level.");
 				put("Message.LevelSaveSuccessful", ChatColor.GREEN + "Level save successful");
 				put("Message.LevelCreated", ChatColor.GREEN + "Level " + ChatColor.LIGHT_PURPLE + "%s" + ChatColor.GREEN + " created");
 				put("Message.UnableToDeleteLevel", ChatColor.RED + "Unable to delete current level.");
 				put("Message.LevelDeleted", ChatColor.GREEN + "Level deleted successfully.");
 				put("Message.Levels", ChatColor.GREEN + "Levels: %s");
 				put("Message.UnknownLevel", ChatColor.RED + "Unknown level: %s");
 				put("Message.MaybeNotSaved", ChatColor.YELLOW + "Maybe you have not saved this level or you have not set anything in.");
 				put("Message.ZombieModeEnabled", "Zombie Mode enabled!");
 				put("Message.ZombieModeDisabled", "Zombie Mode disabled!");
 				put("Message.SpawnpointAdded", ChatColor.GREEN + "Spawnpoint added");
 				put("Message.SpawnpointRemoved", ChatColor.GREEN + "Spawnpoint removed");
 				put("Message.InvalidSpawnName", ChatColor.RED + "Invalid spawn name");
 				put("Message.TeleportedToZombieSpawn", ChatColor.GREEN + "Teleported to zombie spawn " + ChatColor.LIGHT_PURPLE + "%s");
 				put("Message.UnableToGiveYouThatItem", "Unable to give you that item...");
 				put("Message.PurchaseSuccessfulMoney", ChatColor.GREEN + "Purchase successful."+ChatColor.DARK_GRAY+" Money: " + ChatColor.GRAY + "%s $");
 				put("Message.YouDontHaveEnoughMoney", ChatColor.DARK_RED + "You don't have enough money for that!");
 				put("Message.MoneyPoints", ChatColor.DARK_GRAY + "Money: " + ChatColor.GRAY + "%s $" + ChatColor.DARK_GRAY + " Points: "
 						+ ChatColor.GRAY + "%s");
 				put("Message.GameInProgress", ChatColor.YELLOW + "Game in progress, you will spawn next round");
 				put("Message.ZombieHavePrevailed", ChatColor.DARK_RED + "Zombies have prevailed!");
 				put("Message.YouHaveReachedWave", ChatColor.RED + "You have reached wave " + ChatColor.YELLOW + "%s");
 				put("Message.YouJoined", ChatColor.GOLD + "You joined the human strengths against zombies.");
 				put("Message.YouLeft", ChatColor.GOLD + "You left the fight against zombies.");
 				put("Message.TribuSignAdded", ChatColor.GREEN + "Tribu sign successfully added.");
 				put("Message.TribuSignRemoved", ChatColor.GREEN + "Tribu sign successfully removed.");
 				put("Message.ProtectedBlock", ChatColor.YELLOW + "Sorry, this sign is protected, please ask an operator to remove it.");
 				put("Message.CannotPlaceASpecialSign", ChatColor.YELLOW + "Sorry, you cannot place a special signs, please ask an operator to do it.");
 				put("Broadcast.MapChosen", ChatColor.DARK_BLUE + "Level " + ChatColor.LIGHT_PURPLE + "%s" + ChatColor.DARK_BLUE + " has been chosen");
 				put("Broadcast.MapVoteStarting", ChatColor.DARK_AQUA + "Level vote starting,");
 				put("Broadcast.Type", ChatColor.DARK_AQUA + "Type ");
 				put("Broadcast.SlashVoteForMap", ChatColor.GOLD + "'/tribu vote %s'" + ChatColor.DARK_AQUA + " for map " + ChatColor.BLUE + "%s");
 				put("Broadcast.VoteClosingInSeconds", ChatColor.DARK_AQUA + "Vote closing in %s seconds");
 				put("Broadcast.StartingWave", ChatColor.GRAY + "Starting wave " + ChatColor.DARK_RED + "%s" + ChatColor.GRAY + ", "
 						+ ChatColor.DARK_RED + "%s" + ChatColor.GRAY + " Zombies @ " + ChatColor.DARK_RED + "%s" + ChatColor.GRAY
 						+ " health");
 				put("Broadcast.Wave", ChatColor.DARK_GRAY + "Wave " + ChatColor.DARK_RED + "%s" + ChatColor.DARK_GRAY + " starting in "
 						+ ChatColor.DARK_RED + "%s" + ChatColor.DARK_GRAY + " seconds.");
 				put("Broadcast.WaveComplete", ChatColor.GOLD + "Wave Complete");
 				put("Info.LevelFound", "%s levels found");
 				put("Info.Enable", ChatColor.WHITE+"Starting "+ChatColor.DARK_RED+"Tribu"+ChatColor.WHITE+" by Graindcafe, original author : samp20");
 				put("Info.Disable", "Stopping Tribu");
 				put("Info.LevelSaved", "Level saved");
 				put("Info.ChosenLanguage", "Chosen language : %s (default). Provided by : %s.");
 				put("Info.LevelFolderDoesntExist", "Level folder doesn't exist");
 				put("Warning.AllSpawnsCurrentlyUnloaded", "All zombies spawns are currently unloaded.");
 				put("Warning.UnableToSaveLevel", "Unable to save level");
 				put("Warning.ThisCommandCannotBeUsedFromTheConsole", "This command cannot be used from the console");
 				put("Warning.IOErrorOnFileDelete", "IO error on file delete");
 				put("Warning.LanguageFileOutdated", "Your current language file is outdated");
 				put("Warning.LanguageFileMissing", "The chosen language file is missing");
 				put("Warning.UnableToAddSign", "Unable to add sign, maybe you've changed your locales, or signs' tags.");
 				put("Warning.UnknownFocus",
 						"The string given for the configuration Zombies.Focus is not recognized : %s . It could be 'None','Nearest','Random','DeathSpawn','InitialSpawn'.");
 				put("Severe.TribuCantMkdir","Tribu can't make dirs so it cannot create the level directory, you would not be able to save levels ! You can't use Tribu !");
 				put("Severe.WorldInvalidFileVersion", "World invalid file version");
 				put("Severe.WorldDoesntExist", "World doesn't exist");
 				put("Severe.ErrorDuringLevelLoading", "Error during level loading : %s");
 				put("Severe.ErrorDuringLevelSaving", "Error during level saving : %s");
 				put("Severe.PlayerHaveNotRetrivedHisItems", "The player %s have not retrieved his items, they will be deleted ! Items list : %s");
 				put("Severe.Exception", "Exception: %s");
 			}
 		});
 		language = Language.init(log, getConfig().getString("PluginMode.Language", "english"));
 		Constants.MessageMoneyPoints = language.get("Message.MoneyPoints");
 		Constants.MessageZombieSpawnList = language.get("Message.ZombieSpawnList");
 	}
 
 	public boolean isAlive(Player player) {
 		return players.get(player).isalive();
 	}
 
 	public boolean isDedicatedServer() {
 		return dedicatedServer;
 	}
 
 	public boolean isPlaying(Player p) {
 		return players.containsKey(p);
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public void keepTempInv(Player p, ItemStack[] items) {
 		// log.info("Keep " + items.length + " items for " +
 		// p.getDisplayName());
 		tempInventories.put(p, new TribuInventory(p, items));
 	}
 
 	public void LogInfo(String message) {
 		log.info(message);
 	}
 
 	public void LogSevere(String message) {
 		log.severe(message);
 	}
 
 	public void LogWarning(String message) {
 		log.warning(message);
 	}
 
 	public void Message(CommandSender sender, String message) {
 		if (sender == null)
 			log.info(ChatColor.stripColor(message));
 		else
 			sender.sendMessage(message);
 	}
 
 	@Override
 	public void onDisable() {
 		for (Entry<Player, TribuInventory> e : inventories.entrySet()) {
 			if (e.getKey().isOnline() && !e.getKey().isDead())
 				e.getValue().restore();
 			else if (level != null)
 				e.getValue().drop(level.getInitialSpawn());
 			else
 				// We have a BIG problem
 				log.severe(String.format(getLocale("Severe.PlayerHaveNotRetrivedHisItems"), e.getKey().getDisplayName(), e.getValue().toString()));
 		}
 		players.clear();
 		sortedStats.clear();
 		stopRunning();
 		LogInfo(language.get("Info.Disable"));
 	}
 
 	@Override
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		rnd = new Random();
 		initConfig();
 		initLanguage();
 		dedicatedServer = getConfig().getBoolean("PluginMode.ServerExclusive", false);
 		/*
 		 * if(dedicatedServer) LogInfo("dedicated"); else
 		 * LogInfo("not dedicated");
 		 * 
 		 * for(Entry<String, Object> cur :
 		 * getConfig().getAll().entrySet()) {
 		 * LogInfo(cur.getKey()+" = "+cur.getValue().toString()); }
 		 */
 		blockTrace = new BlockTrace(log);
 		isRunning = false;
 		aliveCount = 0;
 		level = null;
 		tempInventories = new HashMap<Player, TribuInventory>();
 		inventories = new HashMap<Player, TribuInventory>();
 		players = new HashMap<Player, PlayerStats>();
 		sortedStats = new LinkedList<PlayerStats>();
 		levelLoader = new LevelFileLoader(this);
 		levelSelector = new LevelSelector(this);
 		// Create listeners
 		playerListener = new TribuPlayerListener(this);
 		entityListener = new TribuEntityListener(this);
 		blockListener = new TribuBlockListener(this);
 		worldListener = new TribuWorldListener(this);
 		getServer().getPluginManager().registerEvents(playerListener, this);
 		getServer().getPluginManager().registerEvents(entityListener, this);
 		getServer().getPluginManager().registerEvents(blockListener, this);
 		getServer().getPluginManager().registerEvents(worldListener, this);
 		spawner = new TribuSpawner(this);
 		spawnTimer = new SpawnTimer(this);
 		waveStarter = new WaveStarter(this);
 
 		getCommand("dspawn").setExecutor(new CmdDspawn(this));
 		getCommand("zspawn").setExecutor(new CmdZspawn(this));
 		getCommand("ispawn").setExecutor(new CmdIspawn(this));
 		getCommand("tribu").setExecutor(new CmdTribu(this));
 		// getCommand("vote").setExecutor(new CmdVote(this));
 		// getCommand("level").setExecutor(new CmdLevel(this));
 		/*
 		 * List<String> zmAliases = new LinkedList<String>();
 		 * zmAliases.add("zm"); zmAliases.add("zombie");
 		 * getCommand("zombiemode").setAliases(zmAliases);
 		 * getCommand("zombiemode").setExecutor(new CmdZombieMode(this));
 		 */
 
 		int i = 0;
 		Player[] tmpPlayerList = this.getServer().getOnlinePlayers();
 		if (dedicatedServer) {
 			int c = tmpPlayerList.length;
 			while (i < c) {
 				this.addPlayer(tmpPlayerList[i]);
 				i++;
 			}
 		}
 		if (getConfig().getString("PluginMode.DefaultLevel", "") != "")
 			setLevel(levelLoader.loadLevel(getConfig().getString("PluginMode.DefaultLevel", "")));
 		if (getConfig().getBoolean("PluginMode.	", false))
 			startRunning();
 		LogInfo(language.get("Info.Enable"));
 	}
 
 	public void removePlayer(Player player) {
 		if (player != null && players.containsKey(player)) {
 			if (isAlive(player)) {
 				aliveCount--;
 			}
 			checkAliveCount();
 			sortedStats.remove(players.get(player));
 			players.remove(player);
 			if (!player.isDead())
 				if (inventories.containsKey(player))
 					inventories.remove(player).restore();
 		}
 	}
 
 	public void restoreInventory(Player p) {
 		// log.info("Restore items for " + p.getDisplayName());
 		if (inventories.containsKey(p))
 			inventories.remove(p).restore();
 	}
 
 	public void restoreTempInv(Player p) {
 		//log.info("Restore items for " + p.getDisplayName());
 		if (tempInventories.containsKey(p))
 			tempInventories.remove(p).restore();
 	}
 
 	public void revivePlayer(Player player) {
 
 		players.get(player).revive();
 		if (getConfig().getBoolean("WaveStart.HealPlayers", true))
 			player.setHealth(20);
 		restoreTempInv(player);
 		aliveCount++;
 
 	}
 
 	public void revivePlayers(boolean teleportAll) {
 
 		aliveCount = 0;
 		for (Player player : players.keySet()) {
 			revivePlayer(player);
 			if (isRunning && level != null && (teleportAll || !isAlive(player))) {
 				player.teleport(level.getInitialSpawn());
 			}
 
 		}
 	}
 
 	public void setDead(Player player) {
 		if (players.containsKey(player)) {
 			if (isAlive(player)) {
 				aliveCount--;
 				PlayerStats p = players.get(player);
 				p.resetMoney();
 				p.subtractmoney(getConfig().getInt("Stats.OnPlayerDeath.Money", 10000));
 				p.subtractPoints(getConfig().getInt("Stats.OnPlayerDeath.Points", 50));
 				p.msgStats();
 				/*
 				 * Set<Entry<Player, PlayerStats>> stats = players.entrySet();
 				 * for (Entry<Player, PlayerStats> stat : stats) {
 				 * stat.getValue().subtractPoints(50);
 				 * stat.getValue().resetMoney(); stat.getValue().msgStats(); }
 				 */
 			}
 			players.get(player).kill();
 			if (getLevel() != null && isRunning) {
 				checkAliveCount();
 			}
 		}
 	}
 
 	public void setLevel(TribuLevel level) {
 		this.level = level;
 	}
 
 	public void startRunning() {
 		if (!isRunning && getLevel() != null) {
 			if (players.isEmpty()) {
 				waitingForPlayers = true;
 			} else {
 				if (!getConfig().getBoolean("PluginMode.AutoStart", false))
 					waitingForPlayers = false;
 				isRunning = true;
 				if (dedicatedServer)
 					for (LivingEntity e : level.getInitialSpawn().getWorld().getLivingEntities()) {
 						if (!(e instanceof Player) && !(e instanceof Wolf))
 							e.damage(100000);
 
 					}
 
 				// Make sure no data is lost if server decides to die
 				// during a game and player forgot to /level save
 				if (!getLevelLoader().saveLevel(getLevel())) {
 					LogWarning(language.get("Warning.UnableToSaveLevel"));
 				} else {
 					LogInfo(language.get("Info.LevelSaved"));
 				}
 				getLevel().initSigns();
 				this.sortedStats.clear();
 				for (PlayerStats stat : players.values()) {
 					stat.resetPoints();
 					stat.resetMoney();
 					this.sortedStats.add(stat);
 				}
 
 				getWaveStarter().resetWave();
 				revivePlayers(true);
 				getWaveStarter().scheduleWave(Constants.TicksBySecond * getConfig().getInt("WaveStart.Delay", 10));
 			}
 		}
 	}
 
 	public void stopRunning() {
 		if (isRunning) {
 			isRunning = false;
 			getSpawnTimer().Stop();
 			getWaveStarter().cancelWave();
 			getSpawner().clearZombies();
 			getLevelSelector().cancelVote();
 			blockTrace.reverse();
 			for (TribuInventory ti : inventories.values())
 				ti.restore();
 			inventories.clear();
 			if (!dedicatedServer) {
 				players.clear();
 
 			}
 		}
 
 	}
 
 }
