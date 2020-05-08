 package me.ayan4m1.plugins.zombo;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
 
 public class Zombo extends JavaPlugin implements Listener {
 	private final String  configFile   = "config.yml";
 	private final String  dataFile	   = "data.yml";
 	private final String  dropsFile	   = "drops.yml";
 	private final String  recipesFile  = "recipes.yml";
 	private final String  wavesFile    = "waves.yml";
 	private DataStore     dataStore    = new DataStore();
 	private Integer       wave         = 1;
 	private Integer		  maxWave	   = 1;
 	private Integer 	  roundXpBonus = 5000;
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 
 		loadData();
 	}
 
 	public void onDisable() {
 		try {
 			CustomClassLoaderConstructor dropLoader = new CustomClassLoaderConstructor(ZomboDropInfo.class.getClassLoader());
 			CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 			CustomClassLoaderConstructor recipeLoader = new CustomClassLoaderConstructor(ZomboCraftRecipe.class.getClassLoader());
 			CustomClassLoaderConstructor waveLoader = new CustomClassLoaderConstructor(ZomboMobInfo.class.getClassLoader());
 			TypeDescription playerTypeDesc = new TypeDescription(ZomboPlayerInfo.class);
 			playerTypeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 			dataLoader.addTypeDescription(playerTypeDesc);
 			TypeDescription recipeTypeDesc = new TypeDescription(ZomboCraftRecipe.class);
 			recipeTypeDesc.putListPropertyType("reagents", ItemStack.class);
 			recipeTypeDesc.putListPropertyType("enchants", String.class);
 			recipeLoader.addTypeDescription(recipeTypeDesc);
 
 			this.getLogger().info("Saving data to " + this.dataFile);
 			FileWriter writer = new FileWriter(new File(getDataFolder(), this.dataFile));
 
 			//Serialize the player list
 			writer.write(new Yaml(dataLoader).dump(dataStore.getPlayers()));
 			writer.close();
 
 			this.getLogger().info("Saving drops to " + this.dropsFile);
 			writer = new FileWriter(new File(getDataFolder(), this.dropsFile));
 
 			//Serialize the drop list
 			writer.write(new Yaml(dropLoader).dump(dataStore.getDrops()));
 			writer.close();
 
 			this.getLogger().info("Saving recipes to " + this.recipesFile);
 			writer = new FileWriter(new File(getDataFolder(), this.recipesFile));
 
 			//Serialize the recipe list
 			writer.write(new Yaml(recipeLoader).dump(dataStore.getCraftRecipes()));
 			writer.close();
 
 			this.getLogger().info("Saving wave spawns to " + this.wavesFile);
 			writer = new FileWriter(new File(getDataFolder(), this.wavesFile));
 
 			//Serialize the wave spawn list
 			writer.write(new Yaml(waveLoader).dump(dataStore.getWaveRecipes()));
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
 
 		getLogger().info(dataStore.getOnlinePlayers() + " online players");
 		if (dataStore.getOnlinePlayers() == 1) {
 			wave = 0;
 			advanceWave();
 		}
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
 			
 			//Clear any chest locks that exist for this player
 			Set<Location> locations = dataStore.getChestLocks().keySet();
 			for(Location chestLoc : locations) {
 				if (dataStore.getChestLock(chestLoc) == player.getName()) {
 					dataStore.removeChestLock(chestLoc);
 				}
 			}
 			
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
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		//Ensure the death occurred in the correct world
 		if (!event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 		
 		Player player = event.getEntity();
 
 		//Temporarily store inventory for the respawn event
 		dataStore.setTempInventoryForPlayer(player.getName(), player.getInventory().getContents());
 
 		//Prevent items from dropping
 		event.getDrops().clear();
 
 		//Inform players of the death
 		messagePlayers(player.getName() + " has died!");
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		//Ensure the respawn occurred in the correct world
 		if (!event.getPlayer().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		Player player = event.getPlayer();
 		ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 		if (playerInfo == null) {
 			getLogger().warning("Could not find player " + player.getName() + " when handling PlayerRespawnEvent!");
 			return;
 		}
 
 		player.sendMessage("The cost of death is " + Math.floor(playerInfo.getXp() / 2) + " XP.");
 		playerInfo.setXp((int)Math.floor(playerInfo.getXp() / 2));
 		dataStore.putPlayer(player.getName(), playerInfo);
 
 		ItemStack[] inventory = dataStore.getTempInventoryForPlayer(event.getPlayer().getName());
 		if (inventory != null) {
 			event.getPlayer().getInventory().setContents(inventory);
 		}
 	}
 	
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		//Ensure the entity is in the correct world
 		if (event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Suppress all mobs not created by us
 		if (!event.getSpawnReason().equals(SpawnReason.CUSTOM)) {
 			event.setCancelled(true);
 		}
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
 
 		Monster mob = (Monster)event.getEntity();
 
 		//Ensure the dead mob is in the data store
 		if (!dataStore.containsMob(mob.getEntityId())) {
 			return;
 		}
 
 		//Only do these steps when the mob was killed by a player
 		if (mob.getKiller() != null) {
 			//Fetch information from data store
 			Player player = mob.getKiller();
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			ZomboMobInfo entityInfo = dataStore.getMobById(mob.getEntityId());
 
 			//Update player information
 			playerInfo.addKill(entityInfo.getType());
 			playerInfo.addXp(entityInfo.getXp() * wave);
 			dataStore.putPlayer(player.getName(), playerInfo);
 
 			//Send message to the player
 			mob.getKiller().sendMessage("Killed a " + mob.getType().getName() + " [+" + entityInfo.getXp() + " XP]");
 
 			//Drop crafting items
 			ArrayList<ZomboDropInfo> mobDrops = dataStore.getDropsByType(mob.getType());
 			Random rand = new Random();
 			if (mobDrops != null && !mobDrops.isEmpty()) {
 				for (ZomboDropInfo dropInfo : mobDrops) {
 					if (dropInfo.canDrop(rand)) {
 						event.getDrops().add(new ItemStack(dropInfo.getType(), dropInfo.getAmount()));
 					}
 				}
 			}
 		}
 
 		//Disable vanilla drops
 		event.setDroppedExp(0);
 		event.getDrops().clear();
 
 		//Stop tracking mob
 		dataStore.removeMob(mob.getEntityId());
 
 		//If no mobs are left, advance to the next wave
 		if (dataStore.getMobs().isEmpty()) {
 			if (wave == maxWave) {
 				//Give XP bonus to online players
 				for (String playerName : dataStore.getPlayers().keySet()) {
 					Player msgPlayer = getServer().getPlayerExact(playerName);
 					ZomboPlayerInfo msgPlayerInfo = dataStore.getPlayerByName(playerName);
 
 					if (msgPlayer == null || msgPlayerInfo == null) {
 						continue;
 					}
 
 					msgPlayerInfo.addXp(roundXpBonus);
 					msgPlayer.sendMessage("Round complete bonus! [+" +  + roundXpBonus + " XP]");
 				}
 			}
 			if (this.isAutoAdvance()) {
 				advanceWave();
 			} else {
 				//Tell players to ready up
 				messagePlayers("Wave complete! /zready to continue.");
 			}
 		} else if (dataStore.getMobs().size() <= 3) {
 			//Tell players that the wave is almost over
 			messagePlayers(dataStore.getMobs().size() + " mobs remaining");
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		//Ensure player is on the correct world and that they are right clicking a chest
 		if (!event.getPlayer().getWorld().getName().equals(this.getWorldName())
 			|| event.getClickedBlock() == null
 			|| !event.getClickedBlock().getType().equals(Material.CHEST)) {
 			return;
 		}
 
 		Player player = event.getPlayer();
 
 		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 			Random rand = new Random();
 			Chest chest = (Chest)event.getClickedBlock().getState();
 			ZomboCraftRecipe recipe = dataStore.getCraftRecipeForInventory(chest.getInventory());
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			ItemStack craftItem = new ItemStack(recipe.getOutputType());
 
 			//Add enchantments with random level from 1-4
 			for (String enchantName : recipe.getEnchants()) {
				craftItem.addEnchantment(Enchantment.getByName(enchantName), (int)Math.floor(rand.nextFloat() * 3) + 1);
 			}
 
 			player.getInventory().addItem(craftItem);
 			playerInfo.setXp(playerInfo.getXp() - recipe.getXpCost());
 			dataStore.putPlayer(player.getName(), playerInfo);
 
 			player.sendMessage("You now have " + playerInfo.getXp() + " XP");
 		} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Location chestLocation = event.getClickedBlock().getLocation();
 	
 			//Notify the player of the existing lock
 			if (dataStore.containsChestLock(chestLocation)) {
 				player.sendMessage(dataStore.getChestLock(chestLocation) + " is using this chest currently!");
 				event.setCancelled(true);
 				return;
 			}
 	
 			//Lock this chest to the player
 			dataStore.setChestLock(chestLocation, player.getName());
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event) {
 		//Ensure player is on the right world and inventory being closed is that of a chest 
 		if (!event.getPlayer().getWorld().getName().equals(this.getWorldName())
 			|| !(event.getPlayer() instanceof Player)
 			|| !event.getInventory().getType().equals(InventoryType.CHEST)
 			|| !(event.getInventory().getHolder() instanceof Chest)) {
 			return;
 		}
 
 		Player player = (Player)event.getPlayer();
 		Inventory inventory = event.getInventory();
 
 		//Remove the lock this player has on the chest
 		Chest chest = (Chest)event.getInventory().getHolder();
 		dataStore.removeChestLock(chest.getLocation());
 
 		//Tell the user that there is no valid recipe and return their items
 		ZomboCraftRecipe craftRecipe = dataStore.getCraftRecipeForInventory(event.getInventory());
 		if (craftRecipe == null) {
 			boolean notified = false;
 			for(ItemStack item : inventory.getContents()) {
 				if (item != null && item.getAmount() > 0) {
 					if (!notified) {
 						player.sendMessage("Not a valid recipe! Returning your items...");
 						notified = true;
 					}
 					ItemStack newItem = item.clone();
 					inventory.remove(item);
 					player.getInventory().addItem(newItem);					
 				}
 			}
 			return;
 		}
 
 		//If there are items, look for a valid craft recipe
 		for (ItemStack item : inventory.getContents()) {
 			if (item != null && item.getAmount() > 0) {
 				player.sendMessage("Recipe: " + craftRecipe.getName());
 				player.sendMessage("Cost: " + craftRecipe.getXpCost());
 				player.sendMessage("Left click on the chest to craft.");
 				break;
 			}
 		}
 	}
 
 	private void messagePlayers(String message) {
 		for (Player msgPlayer : getServer().getWorld(this.getWorldName()).getPlayers()) {
 			msgPlayer.sendMessage(message);
 		}
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
 			player.sendMessage("XP: " + playerInfo.getXp());
 			player.sendMessage("Kills");
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
 		} else if (cmd.getName().equalsIgnoreCase("zreload")) {
 			if (loadData()) {
 				player.sendMessage("Reloaded data from disk!");
 			} else {
 				player.sendMessage("Error reloading data. Check server log for details.");
 			}
 		} else if (cmd.getName().equalsIgnoreCase("zwave")) {
 			player.sendMessage("Wave " + wave);
 		} else if (cmd.getName().equalsIgnoreCase("zready")) {
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			if (playerInfo == null) {
 				return false;
 			}
 
 			//Mark player as ready
 			playerInfo.setReady(true);
 			dataStore.putPlayer(player.getName(), playerInfo);
 
 			//Advance to the next wave if all players are ready
 			pollWave();
 		} else if (cmd.getName().equalsIgnoreCase("zresetwave")) {
 			wave = 0;
 			player.sendMessage("Wave has been reset");
 		} else if (cmd.getName().equalsIgnoreCase("zresetinv")) {
 			InventoryManager.starterKit(player);
 			player.sendMessage("Your inventory has been reset.");
 		}
 
 		return false;
 	}
 
 	/**
 	 * Advance the wave if all players are marked as ready
 	 * @return True if all players are ready, false otherwise
 	 */
 	private boolean pollWave() {
 		//Ensure all players are ready
 		for(ZomboPlayerInfo info : dataStore.getPlayers().values()) {
 			if (info.isOnline() && !info.isReady()) {
 				return false;
 			}
 		}
 
 		//Reset the ready state for all players
 		Set<String> playerNames = dataStore.getPlayers().keySet();
 		for(String playerName : playerNames) {
 			ZomboPlayerInfo info = dataStore.getPlayerByName(playerName);
 			info.setReady(false);
 			dataStore.putPlayer(playerName, info);
 		}
 
 		advanceWave();
 		return true;
 	}
 
 	private boolean loadData() {
 		try {
 			File configFile = new File(getDataFolder(), this.configFile);
 			File dataFile = new File(getDataFolder(), this.dataFile);
 			File dropsFile = new File(getDataFolder(), this.dropsFile);
 			File recipesFile = new File(getDataFolder(), this.recipesFile);
 			File wavesFile = new File(getDataFolder(), this.wavesFile);
 
 			this.getLogger().info("Loading config from " + this.configFile);
 			getConfig().load(configFile);
 
 			//This allows snakeyaml to deserialize correctly
 			CustomClassLoaderConstructor dropLoader = new CustomClassLoaderConstructor(ZomboDropInfo.class.getClassLoader());
 			CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 			CustomClassLoaderConstructor recipeLoader = new CustomClassLoaderConstructor(ZomboCraftRecipe.class.getClassLoader());
 			CustomClassLoaderConstructor waveLoader = new CustomClassLoaderConstructor(ZomboMobInfo.class.getClassLoader());
 			TypeDescription playerTypeDesc = new TypeDescription(ZomboPlayerInfo.class);
 			playerTypeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 			dataLoader.addTypeDescription(playerTypeDesc);
 			TypeDescription recipeTypeDesc = new TypeDescription(ZomboCraftRecipe.class);
 			recipeTypeDesc.putListPropertyType("reagents", ItemStack.class);
 			recipeTypeDesc.putListPropertyType("enchants", String.class);
 			recipeLoader.addTypeDescription(recipeTypeDesc);
 			
 			if (dropsFile.length() > 0) {
 				this.getLogger().info("Loading drops file from " + this.dropsFile);
 
 				HashMap<EntityType, ArrayList<ZomboDropInfo>> drops = (HashMap<EntityType, ArrayList<ZomboDropInfo>>)new Yaml(dropLoader).load(new FileReader(dropsFile));
 				dataStore.setDrops(drops);
 
 				this.getLogger().info("Loaded drops for " + drops.size() + " entity types");
 			}
 
 			if (dataFile.length() > 0) {
 				this.getLogger().info("Loading data file from " + this.dataFile);
 
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
 
 				this.getLogger().info("Loaded data for " + players.size()  + " players, " + onlineCount + " are online now");
 			}
 
 			if (recipesFile.length() > 0) {
 				this.getLogger().info("Loading craft recipes from " + this.recipesFile);
 
 				ArrayList<ZomboCraftRecipe> craftRecipes = (ArrayList<ZomboCraftRecipe>)new Yaml(recipeLoader).load(new FileReader(recipesFile));
 				dataStore.setCraftRecipes(craftRecipes);
 
 				this.getLogger().info("Loaded " + craftRecipes.size() + " recipes!");
 			}
 
 			if (wavesFile.length() > 0) {
 				this.getLogger().info("Loading wave spawns from " + this.wavesFile);
 
 				HashMap<Integer, ArrayList<ZomboMobInfo>> waveRecipes = (HashMap<Integer, ArrayList<ZomboMobInfo>>)new Yaml(waveLoader).load(new FileReader(wavesFile));
 				dataStore.setWaveRecipes(waveRecipes);
 
 				maxWave = waveRecipes.size();
 				this.getLogger().info("Loaded " + waveRecipes.size() +  " waves!");
 			}
 			
 			return true;
 		} catch (FileNotFoundException e) {
 			this.getLogger().warning("File was not found - " + e.getMessage());
 		} catch (IOException e) {
 			this.getLogger().warning("Error reading file - " + e.getMessage());
 		} catch (InvalidConfigurationException e) {
 			this.getLogger().warning(this.configFile + " is invalid - " + e.getMessage());
 		}
 
 		return false;
 	}
 
 	/**
 	 * Advance to the next wave, spawn appropriate mobs and notify users
 	 */
 	private void advanceWave() {
 		World world = getServer().getWorld(this.getWorldName());
 		Location loc = world.getSpawnLocation();
 
 		if (wave == maxWave) {
 			wave = 1;
 		} else {
 			wave++;
 		}
 
 		//Spawn mobs set in wave recipe
 		if (dataStore.getWaveByIndex(wave) == null) {
 			getLogger().warning("Could not find wave recipe for wave " + wave);
 			return;
 		}
 
 		for(ZomboMobInfo mobInfo : dataStore.getWaveByIndex(wave)) {
 			dataStore.spawnMob(getNearestFreeBlock(loc), mobInfo);
 		}
 
 		if (wave == maxWave) {
 			messagePlayers("Final Wave Spawned!");
 		} else {
 			messagePlayers("Wave " + wave + " Spawned!");
 		}
 	}
 
 	/**
 	 * Get the empty block nearest to the provided location 
 	 * @param loc A location to search nearby
 	 * @return A Location or null if no empty blocks are found in the search radius
 	 */
 	private Location getNearestFreeBlock(Location loc) {
 		if (loc.getBlock().isEmpty()) {
 			return loc;
 		}
 
 		//Check each block in our radius starting from the center
 		for(int x = 0; x <= (10); x++) {
 			for(int y = 0; y <= (10); y++) {
 				for(int z = 0; z <= (10); z++) {
 					Location testLoc = loc.add(
 							(x % 5) * ((x < 5) ? -1 : 0),
 							(y % 5) * ((y < 5) ? -1 : 0),
 							(z % 5) * ((z < 5) ? -1 : 0));
 					if (testLoc.getBlock().isEmpty()) {
 						return testLoc;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private String getWorldName() {
 		return getConfig().getString("world");
 	}
 
 	private boolean isAutoAdvance() {
 		return getConfig().getBoolean("autoadvance");
 	}
 }
 
