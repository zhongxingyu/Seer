 package com.rlminecraft.RLMDrink;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.rlminecraft.RLMDrink.Exceptions.BadConfigException;
 
 
 
 public class RLMDrink extends JavaPlugin implements Listener {
 	
 	Logger console;
 	boolean debugging = false;
 	int tps = 20;
 	public Map<String, Integer> drunkenness;
 	List<Drink> drinks;
 	List<State> states;
 	
 	public void onEnable() {
 		console = this.getLogger();
 		getServer().getPluginManager().registerEvents(this, this);
 		drinks = new LinkedList<Drink>();
 		states = new LinkedList<State>();
 		drunkenness = new HashMap<String, Integer>();
 		Debug("Player drunk map initialized");
 		Debug("Loading from config");
 		try {
 			if (!loadData("plugins/RLMDrink/config.yml")) {
 				RLMDrink.this.getServer().getPluginManager().disablePlugin(RLMDrink.this);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			RLMDrink.this.getServer().getPluginManager().disablePlugin(RLMDrink.this);
 		}
 		// Schedule sober timer
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				reduceDrunkenness();
 			}
 		}, (20*20), (30*20));
 		console.info("RLMDrinks has been enabled!");
 	}
 	
 	public void onDisable() {
 		console.info("RLMDrinks has been disabled!");
 	}
 	
 	
 	/* YAML Loader */
 	
 	private boolean loadData (String filename) throws FileNotFoundException {
 		// Load data from config.yml
 		File file = new File(filename);
 		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
 		Map<String, Object> config = yml.getValues(false);
 		
 		// Check that required sections are present
 		if (config.containsKey("drinks")) {
 			Debug("Key \"drinks\" found in config");
 			Map<String,Object> tempDrinks = ((MemorySection) config.get("drinks")).getValues(false);
 			for (int i = 0; i < tempDrinks.size(); i++) {
 				String tempName = tempDrinks.keySet().toArray()[i].toString();
 				MemorySection tempData = (MemorySection) tempDrinks.get(tempName);
 				try {
 					Drink drink = new Drink(tempName,tempData);
 					drinks.add(drink);
 				} catch (BadConfigException e) {
 					console.warning("Drink \"" + tempName + "\" could not be added properly! Reason: " + e.getCause());
 				} finally {
 					// do nothing
 				}
 			}
 		} else {
 			console.severe("The drinks section is missing from config.yml.");
 			return false;
 		}
 		if (config.containsKey("states")) {
 			Debug("Key \"states\" found in config");
 			Map<String,Object> tempStates = ((MemorySection) config.get("states")).getValues(false);
 			for (int i = 0; i < tempStates.size(); i++) {
 				String tempName = tempStates.keySet().toArray()[i].toString();
 				MemorySection tempData = (MemorySection) tempStates.get(tempName);
 				try {
 					Debug("Attempting to create new state...");
 					State state = new State(tempName,tempData);
 					Debug("Success! Adding state to global state list...");
 					states.add(state);
 					Debug ("success!");
 				} catch (BadConfigException e) {
 					// do nothing
 				}
 			}
 		} else {
 			console.severe("The states section is missing from config.yml.");
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 	/* LISTENERS */
 	
 	@EventHandler
 	public void onPlayerInteract (PlayerInteractEvent event) {
 		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
 			try {
 				MaterialData item = event.getItem().getData();
 				if (item.getItemType() == Material.POTION) {
 					Debug("Potion used! Entering potion command...");
 					PotionUse(player, item.getData());
 				} else if (item.getItemType() == Material.INK_SACK) {
 					//add weed effect
 				}
 			} catch (Exception e) {
 				console.warning("Exception caught!");
 			} finally {
 				// nothing
 			}
 		}
 		return;
 	}
 	
 	public void PotionUse (Player player, int type) {
 		if (player == null) {
 			console.warning("Null player event!");
 			return;
 		}
 		String drinkName = "";
 		String drinkUnit = "";
 		int currentDrunkLevel;
 		if (drunkenness.containsKey(player.getName())) {
 			currentDrunkLevel = drunkenness.get(player.getName());
 		} else {
 			currentDrunkLevel = 0;
 		}
 		int oldDrunkLevel = currentDrunkLevel;
 		
 		// Determine if drink consumed is alcoholic
 		ListIterator<Drink> drinkIterator = drinks.listIterator();
 		while (drinkIterator.hasNext()) {
 			Drink drink = drinkIterator.next();
 			if (drink.id == type) {
 				drinkName = drink.name;
 				drinkUnit = drink.unit;
 				currentDrunkLevel += drink.drunkenness;
 			}
 		}
 		if (drinkName == "") return;
 		
 		/*
 		// Decrease potion inventory by 1
 		PlayerInventory inv = player.getInventory();
 		int held = inv.getHeldItemSlot();
 		ItemStack item = inv.getItem(held);
 		int itemAmount = item.getAmount();
 		if (itemAmount > 1) {
 			Debug("More than one item! Removing one item from inventory slot.");
 			item.setAmount(item.getAmount() - 1);
 			inv.setItem(held, item);
 		} else {
 			Debug("Only one item! Clearing inventory slot.");
 			inv.clear(held);
 		}
 		player.setItemInHand(inv.getItem(held));
 		*/
 		
 		player.sendMessage("You drank a " + drinkUnit + " of " + drinkName + ".");
 		
 		// Determine change in state and/or effects to add
 		State oldState = new State();
 		State currentState = new State();
 		Debug("Current drunkenness: " + currentDrunkLevel);
 		ListIterator<State> stateIterator = states.listIterator();
 		while (stateIterator.hasNext()) {
 			State state = stateIterator.next();
 			if (state.start <= oldDrunkLevel
 					&& state.finish > oldDrunkLevel) {
 				oldState = state;
 			}
 			if (state.start <= currentDrunkLevel
 					&& state.finish > currentDrunkLevel) {
 				currentState = state;
 			}
 		}
 		if (currentState.name != oldState.name && !currentState.death) {
 			Debug(player.getName() + ": " + oldState.name + " --> " + currentState.name);
 			player.sendMessage("You are now " + currentState.name + "!");
 		} else if (currentState.name == "NULL" && oldState.name == "NULL") {
 			drunkenness.put(player.getName(), currentDrunkLevel);
 			return;
 		}
 		
 		// Get potion effects
 		if (currentState.death) {
 			Debug(player.getName() + ": " + oldState.name + " --> deceased");
 			player.sendMessage(ChatColor.RED + "You died of alcohol poisoning!");
 			currentDrunkLevel = 0;
 			player.setHealth(0);
 		} else {
 			Debug("Effects: " + currentState.effects.toString());
 			ListIterator<Effect> effectIterator = currentState.effects.listIterator();
 			while (effectIterator.hasNext()) {
 				player.addPotionEffect(effectIterator.next().toPotionEffect());
 			}
 		}
 		drunkenness.put(player.getName(), currentDrunkLevel);
 	}
 	
 	
 	private void reduceDrunkenness () {
 		Player[] players = this.getServer().getOnlinePlayers();
 		for (int i = 0; i < players.length; i++) {
 			String player = players[i].getName();
 			if (drunkenness.containsKey(player)) {
 				int playerDrunk = drunkenness.get(player);
 				playerDrunk -= 1;
 				if (playerDrunk > 0) {
 					drunkenness.put(player, playerDrunk);
 				} else {
 					drunkenness.remove(player);
 				}
 				applyStateEffects(players[i]);
 			}
 		}
 	}
 	
 	
 	private State getState (int drunkLevel) {
 		ListIterator<State> stateIterator = states.listIterator();
 		while (stateIterator.hasNext()) {
 			State state = stateIterator.next();
 			if (state.inRange(drunkLevel)) {
 				return state;
 			}
 		}
 		return new State();
 	}
 	
 	
 	private void applyStateEffects (Player player) {
 		if (player == null) return;
 		if (!drunkenness.containsKey(player.getName())) return;
 		State state = getState(drunkenness.get(player.getName()));
 		if (state.name == "NULL") return;
 		ListIterator<Effect> effectIterator = state.effects.listIterator();
 		while (effectIterator.hasNext()) {
 			Effect effect = effectIterator.next();
 			player.removePotionEffect(effect.name);
 			player.addPotionEffect(effect.toPotionEffect());
 		}
 	}
 	
 	
 	public void Debug(String message) {
 		if (debugging) console.info("[DEBUG] " + message);
 	}
 	
 }
