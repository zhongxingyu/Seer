 package me.ayan4m1.plugins.zombo;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
 
 public class Zombo extends JavaPlugin implements Listener {
 	private final String	  configFile   = "config.yml";
 	private final String	  dataFile	   = "data.yml";
 	private DataStore         dataStore    = new DataStore();
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 
 		try {
 			File configFile = new File(getDataFolder(), this.configFile);
 			File dataFile = new File(getDataFolder(), this.dataFile);
 
 			this.getLogger().info("Loading config from " + this.configFile);
 			getConfig().load(configFile);
 
 			if (dataFile.length() > 0) {
 				this.getLogger().info("Loading data file from " + this.dataFile);
 
 				//This allows snakeyaml to deserialize the class correctly
 				CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 				TypeDescription typeDesc = new TypeDescription(ZomboPlayerInfo.class);
 				typeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 				dataLoader.addTypeDescription(typeDesc);
 
 				//Load saved player data from data file
 				HashMap<String, ZomboPlayerInfo> players = (HashMap<String, ZomboPlayerInfo>)new Yaml(dataLoader).load(new FileReader(dataFile));
 				dataStore.setPlayers(players);
 
 				//Set online status for currently connected players
 				Integer onlineCount = 0;
 				for (String playerName : dataStore.getPlayers().keySet()) {
 					Player player = getServer().getPlayer(playerName);
 					ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(playerName);
 
 					if (player == null || !player.getWorld().getName().equals(this.getWorldName())) {
 						playerInfo.setOnline(false);
 					} else {
 						playerInfo.setOnline(true);
 						onlineCount++;
 						getServer().broadcastMessage(player.getName() + " joined the fight!");
 					}
 
 					dataStore.putPlayer(playerName, playerInfo);
 				}
 				this.getLogger().info("Loaded data for " + dataStore.getPlayers().size()  + " players, " + onlineCount + " are online now");
 			}
 		} catch (FileNotFoundException e) {
 			this.getLogger().warning("File was not found");
 		} catch (IOException e) {
 			this.getLogger().warning("Error reading file - " + e.getMessage());
 		} catch (InvalidConfigurationException e) {
 			this.getLogger().warning(this.configFile + " is invalid - " + e.getMessage());
 		}
 	}
 
 	public void onDisable() {
 		try {
 			this.getLogger().info("Saving data to " + this.dataFile);
 			FileWriter writer = new FileWriter(new File(getDataFolder(), this.dataFile));
 
 			CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 			TypeDescription typeDesc = new TypeDescription(ZomboPlayerInfo.class);
 			typeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 			dataLoader.addTypeDescription(typeDesc);
 
 			//Serialize the player map
 			writer.write(new Yaml(dataLoader).dump(dataStore.getPlayers()));
 			writer.close();
 
 		} catch (IOException e) {
 			this.getLogger().warning("IO error - " + e.getMessage());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		if (!player.getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
		//Add player or mark existing player as online
 		ZomboPlayerInfo playerInfo;
 		if (!dataStore.containsPlayer(player.getName())) {
 			playerInfo = new ZomboPlayerInfo();
 			playerInfo.setOnline(true);
 			//First time player, give them a starter kit
 			InventoryManager.starterKit(player);
 		} else {
 			playerInfo = dataStore.getPlayerByName(player.getName());
 			playerInfo.setOnline(true);
 		}
 
 		//Update player info
 		dataStore.putPlayer(player.getName(), playerInfo);
 		getServer().broadcastMessage(player.getName() + " joined the fight!");
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		if (!player.getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Mark player as offline
 		if (dataStore.containsPlayer(player.getName())) {
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			playerInfo.setOnline(false);
 			dataStore.putPlayer(player.getName(), playerInfo);
 			getServer().broadcastMessage(player.getName() + " left the fight!");
 		}
 	}
 
 	@EventHandler
 	public Result onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
 		Player player = event.getPlayer();
 
		//Create new info object or fetch existing object
 		ZomboPlayerInfo playerInfo;
 		if (!dataStore.containsPlayer(player.getName())) {
 			playerInfo = new ZomboPlayerInfo();
 		} else {
 			playerInfo = dataStore.getPlayerByName(player.getName());
 		}
 
		//Update online status based on event state
 		if (player.getWorld().getName().equals(this.getWorldName())) {
 			playerInfo.setOnline(true);
 			getServer().broadcastMessage(player.getName() + " joined the fight!");
 		} else if (event.getFrom().getName().equals(this.getWorldName())) {
 			playerInfo.setOnline(false);
 			getServer().broadcastMessage(player.getName() + " left the fight!");
 		}
 
		//Update online status
 		dataStore.putPlayer(player.getName(), playerInfo);
 		return Result.ALLOW;
 	}
 
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		//Ensure the death occurred in the correct world
 		if (!event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Ensure the entity was a monster
 		if (!(event.getEntity() instanceof Monster)) {
 			return;
 		}
 
 		//Ensure the mob had a killer
 		Monster mob = (Monster)event.getEntity();
 		if (mob.getKiller() == null) {
 			return;
 		}
 
 		//Ensure the killer and entity are both in the data store
 		Player player = mob.getKiller();
 		if (!dataStore.containsPlayer(player.getName()) || !dataStore.containsMob(mob.getEntityId())) {
 			return;
 		}
 
 		//Fetch information from data store
 		ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 		ZomboMobInfo entityInfo = dataStore.getMobById(mob.getEntityId());
 
 		//Update player information
 		Integer oldLevel = playerInfo.getLevel();
 		playerInfo.addKill(entityInfo.getType());
 		playerInfo.addXp(entityInfo.getXp());
 		dataStore.putPlayer(player.getName(), playerInfo);
 
 		//Send message(s) to players
 		mob.getKiller().sendMessage("Killed a " + mob.getType().getName() + " [+" + entityInfo.getXp() + " XP]");
 		if (!oldLevel.equals(playerInfo.getLevel())) {
 			for (Player msgPlayer : getServer().getWorld(this.getWorldName()).getPlayers()) {
 				msgPlayer.sendMessage(mob.getKiller().getName() + " is now level " + playerInfo.getLevel());
 			}
 		}
 
 		//Disable vanilla drops
 		event.setDroppedExp(0);
 		event.getDrops().clear();
 
 		//Drop crafting items
 		ArrayList<ZomboDropInfo> mobDrops = dataStore.getDropsByType(mob.getType());
 		if (mobDrops != null && !mobDrops.isEmpty()) {
 			for (ZomboDropInfo dropInfo : mobDrops) {
 				if (dropInfo.canDrop()) {
 					event.getDrops().add(dropInfo);
 				}
 			}
 		}
 
 		//Stop tracking mob
 		dataStore.removeMob(mob.getEntityId());
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to use this command.");
 			return false;
 		}
 
 		Player player = (Player)sender;
 		if (!this.getWorldName().equals(player.getWorld().getName())) {
 			return false;
 		}
 
 		if (cmd.getName().equalsIgnoreCase("zinfo")) {
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			player.sendMessage("Level " + playerInfo.getLevel() + " (" + playerInfo.getXp() + " XP)");
 			player.sendMessage("Next level at " + (playerInfo.getLevel() * 5000) + " XP");
 			player.sendMessage("Kill Counts");
 			for(EntityType type : EntityType.values()) {
 				//Ensure that entity type descends from Monster
 				if (type.getEntityClass() == null || !Monster.class.isAssignableFrom(type.getEntityClass())) {
 					continue;
 				}
 
 				//Ensure that count is greater than zero
 				if (playerInfo.getKillsForType(type) == 0) {
 					continue;
 				}
 
 				player.sendMessage("  " + type.getName() + ": " + playerInfo.getKillsForType(type));
 			}
 		} else if (cmd.getName().equalsIgnoreCase("zwave")) {
 			player.sendMessage("Wave " + wave);
 		} else if (cmd.getName().equalsIgnoreCase("zresetinv")) {
 			InventoryManager.starterKit(player);
 			player.sendMessage("Your inventory has been reset.");
 		}
 
 		return false;
 	}
 
 	private String getWorldName() {
 		return getConfig().getString("world");
 	}
 }
 
