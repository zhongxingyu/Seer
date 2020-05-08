 package no.runsafe.framework.server;
 
 import com.avaje.ebean.config.ServerConfig;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.help.HelpMap;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.map.MapView;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.plugin.messaging.Messenger;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import java.io.File;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class RunsafeServer {
 	public static RunsafeServer Instance = null;
 
 	public RunsafeServer(Server toWrap) {
 		this.server = toWrap;
 	}
 
 	//TODO: CREATE RUNSAFE RECIPE
 	public boolean addRecipe(Recipe recipe) {
 		return this.server.addRecipe(recipe);
 	}
 
 	public void banIP(String address) {
 		this.server.banIP(address);
 	}
 
 	public int broadcastMessage(String message, String permission) {
 		return this.server.broadcast(message, permission);
 	}
 
 	public int broadcastMessage(String message) {
 		return this.server.broadcastMessage(message);
 	}
 
 	public void clearRecipes() {
 		this.server.clearRecipes();
 	}
 
 	//TODO: Question this
 	public void configureDbConfig(ServerConfig config) {
 		this.server.configureDbConfig(config);
 	}
 
 	//TODO: Cast all inventory objects to Runsafe
 	public RunsafeInventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType) {
 		return new RunsafeInventory(this.server.createInventory(inventoryHolder, inventoryType));
 	}
 
 	public RunsafeInventory createInventory(InventoryHolder inventoryHolder, int size) {
 		return new RunsafeInventory(this.server.createInventory(inventoryHolder, size));
 	}
 
 	public RunsafeInventory createInventory(InventoryHolder inventoryHolder, int size, String title) {
 		return new RunsafeInventory(this.server.createInventory(inventoryHolder, size, title));
 	}
 
 	//TODO: Make RunsafeMapView
 	public MapView createMap(RunsafeWorld world) {
 		return this.server.createMap(world.getRaw());
 	}
 
 	//TODO: Cast commandsender to runsafe
 	public boolean dispatchCommand(CommandSender sender, String command) {
 		return this.server.dispatchCommand(sender, command);
 	}
 
 	public boolean getAllowEnd() {
 		return this.server.getAllowEnd();
 	}
 
 	public boolean getAllowFlight() {
 		return this.server.getAllowFlight();
 	}
 
 	public boolean getAllowNether() {
 		return this.server.getAllowNether();
 	}
 
 	public List<RunsafePlayer> getBannedPlayers() {
 		ArrayList<RunsafePlayer> result = new ArrayList<RunsafePlayer>();
 		for(OfflinePlayer banned : this.server.getBannedPlayers())
 			result.add(new RunsafePlayer(banned));
 		return result;
 	}
 
 	public String getBukkitVersion() {
 		return this.server.getBukkitVersion();
 	}
 
 	public Map<String, String[]> getCommandAliases() {
 		return this.server.getCommandAliases();
 	}
 
 	public long getConnectionThrottle() {
 		return this.server.getConnectionThrottle();
 	}
 
 	//TODO: Cast this to Runsafe
 	public ConsoleCommandSender getConsoleSender() {
 		return this.server.getConsoleSender();
 	}
 
 	//TODO: Cast GameMode to Runsafe
 	public GameMode getDefaultGameMode() {
 		return this.server.getDefaultGameMode();
 	}
 
 	public boolean getGenerateStructures() {
 		return this.server.getGenerateStructures();
 	}
 
 	//TODO: Cast HelpMap to runsafe
 	public HelpMap getHelpMap() {
 		return this.server.getHelpMap();
 	}
 
 	public String getIp() {
 		return this.server.getIp();
 	}
 
 	public Set<String> getIpBans() {
 		return this.server.getIPBans();
 	}
 
 	public Set<String> getListeningPluginChannels() {
 		return this.server.getListeningPluginChannels();
 	}
 
 	public Logger getLogger() {
 		return this.server.getLogger();
 	}
 
 	//TODO: Cast MapView to runsafe
 	public MapView getMap(short mapID) {
 		return this.server.getMap(mapID);
 	}
 
 	public int getMaxPlayers() {
 		return this.server.getMaxPlayers();
 	}
 
 	//TODO: Cast Messenger to Runsafe
 	public Messenger getMessenger() {
 		return this.server.getMessenger();
 	}
 
 	public String getName() {
 		return this.server.getName();
 	}
 
 	public RunsafePlayer getPlayer(String playerName) {
 		return new RunsafePlayer(this.server.getOfflinePlayer(playerName));
 	}
 
 	public List<RunsafePlayer> getOfflinePlayers() {
 		return RunsafePlayer.convert(server.getOfflinePlayers());
 	}
 
 	public boolean getOnlineMode() {
 		return this.server.getOnlineMode();
 	}
 
 	public List<RunsafePlayer> getOnlinePlayers() {
		return RunsafePlayer.convert(server.getOnlinePlayers());
 	}
 
 	public List<RunsafePlayer> getOperators() {
 		return RunsafePlayer.convert(server.getOperators());
 	}
 
 	public RunsafePlayer getPlayerExact(String playerName) {
 		return new RunsafePlayer(this.server.getPlayerExact(playerName));
 	}
 
 	//TODO: Cast PluginCommand to Runsafe
 	public PluginCommand getPluginCommand(String commandName) {
 		return this.server.getPluginCommand(commandName);
 	}
 
 	// TODO: Cast PluginManager to Runsafe
 	public PluginManager getPluginManager() {
 		return this.server.getPluginManager();
 	}
 
 	public int getPort() {
 		return this.server.getPort();
 	}
 
 	//TODO: Cast Recipe to Runsafe
 	public List<Recipe> getRecipesFor(RunsafeItemStack result) {
 		return this.server.getRecipesFor(result.getRaw());
 	}
 
 	//TODO: Cast BukkitScheduler to Runsafe?
 	public BukkitScheduler getScheduler() {
 		return this.server.getScheduler();
 	}
 
 	public String getServerId() {
 		return this.server.getServerId();
 	}
 
 	public String getServerName() {
 		return this.server.getServerName();
 	}
 
 	//TODO: Cast ServicesManager to Runsafe
 	public ServicesManager getServicesManager() {
 		return this.server.getServicesManager();
 	}
 
 	public int getSpawnRadius() {
 		return this.server.getSpawnRadius();
 	}
 
 	public int getTicksPerAnimalSpawns() {
 		return this.server.getTicksPerAnimalSpawns();
 	}
 
 	public int getTicksPerMonsterSpawns() {
 		return this.server.getTicksPerMonsterSpawns();
 	}
 
 	public String getUpdateFolder() {
 		return this.server.getUpdateFolder();
 	}
 
 	public int getViewDistance() {
 		return this.server.getViewDistance();
 	}
 
 	public List<RunsafePlayer> getWhitelistedPlayers() {
 		return RunsafePlayer.convert(this.server.getWhitelistedPlayers());
 	}
 
 	public RunsafeWorld getWorld(String worldName) {
 		return new RunsafeWorld(this.server.getWorld(worldName));
 	}
 
 	public RunsafeWorld getWorld(UUID uid) {
 		return new RunsafeWorld(this.server.getWorld(uid));
 	}
 
 	public File getWorldContainer() {
 		return this.server.getWorldContainer();
 	}
 
 	public List<RunsafeWorld> getWorlds() {
 		List<RunsafeWorld> returnList = new ArrayList<RunsafeWorld>();
 
 		for(World world : this.server.getWorlds()) {
 			returnList.add(new RunsafeWorld(world));
 		}
 
 		return returnList;
 	}
 
 	public String getWorldType() {
 		return this.server.getWorldType();
 	}
 
 	public boolean hasWhitelist() {
 		return this.server.hasWhitelist();
 	}
 
 	public List<RunsafePlayer> matchPlayer(String playerName) {
 		List<RunsafePlayer> returnList = new ArrayList<RunsafePlayer>();
 
 		for(Player player : this.server.matchPlayer(playerName)) {
 			returnList.add(new RunsafePlayer(player));
 		}
 
 		return returnList;
 	}
 
 	//TODO: Cast Recipe to Runsafe
 	public Iterator<Recipe> recipeIterator() {
 		return this.server.recipeIterator();
 	}
 
 	public void reload() {
 		this.server.reload();
 	}
 
 	public void reloadWhitelist() {
 		this.server.reloadWhitelist();
 	}
 
 	public void resetRecipes() {
 		this.server.resetRecipes();
 	}
 
 	public void savePlayers() {
 		this.server.savePlayers();
 	}
 
 	//TODO: Cast Plugin to runsafe.. or can it already be done?
 	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
 		this.server.sendPluginMessage(source, channel, message);
 	}
 
 	//TODO: Cast GameMode to Runsafe
 	public void setDefaultGameMode(GameMode gameMode) {
 		this.server.setDefaultGameMode(gameMode);
 	}
 
 	public void setSpawnRadius(int radius) {
 		this.server.setSpawnRadius(radius);
 	}
 
 	public void setWhitelist(boolean value) {
 		this.server.setWhitelist(value);
 	}
 
 	public void shutdown() {
 		this.server.shutdown();
 	}
 
 	public void unbanIp(String address) {
 		this.server.unbanIP(address);
 	}
 
 	public boolean unloadWorld(String worldName, boolean save) {
 		return this.server.unloadWorld(worldName, save);
 	}
 
 	public boolean unloadWorld(RunsafeWorld world, boolean save) {
 		return this.unloadWorld(world.getName(), save);
 	}
 
 	public boolean useExactLoginLocation() {
 		return this.server.useExactLoginLocation();
 	}
 
 	private final Server server;
 }
