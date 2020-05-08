 package net.marcuswhybrow.minecraft.law;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.marcuswhybrow.minecraft.law.prison.Prison;
 import net.marcuswhybrow.minecraft.law.utilities.Config;
 import net.marcuswhybrow.minecraft.law.utilities.MessageDispatcher;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class LawWorld extends Entity {
 	private World bukkitWorld;
 	private HashMap<String, Prison> prisons;
 	private HashMap<String, Prison> selectedPrisons;
 	private int hashCode;
 	private HashMap<String, Location> latentTeleports;
 	private HashMap<String, InventoryManager.Action> latentInventoryChanges;
 	
 	public LawWorld(World bukkitWorld) {
 		this.bukkitWorld = bukkitWorld;
 		prisons = new HashMap<String, Prison>();
 		selectedPrisons = new HashMap<String , Prison>();
 		latentTeleports = new HashMap<String, Location>();
 		latentInventoryChanges = new HashMap<String, InventoryManager.Action>();
 		
 		setName(this.bukkitWorld.getName());
 		setConfigPrefix("worlds." + this.getName());
 	}
 	
 	public boolean hasPrison(String prisonName) {
 		return prisons.containsKey(prisonName);
 	}
 	
 	public Prison getPrison(String name) {
 		return prisons.get(name.toLowerCase());
 	}
 	
 	public void addPrison(Prison prison) {
 		prisons.put(prison.getName().toLowerCase(), prison);
 		setChanged("prisons");
 	}
 	
 	public void deletePrison(Prison prison) {
 		prisons.remove(prison.getName().toLowerCase());
 		prison.delete();
 		setChanged("prisons");
 		
 		// If there is only one prison remaining
 		if (prisons.size() == 1) {
 			// Get that prison
 			Prison lastPrison = prisons.values().iterator().next();
 			// And set it as everyones selected prison
 			MessageDispatcher.consoleInfo("selectedPrison: ");
 			for (String playerName : selectedPrisons.keySet()) {
 				MessageDispatcher.consoleInfo("  " + playerName);
 				selectedPrisons.put(playerName, lastPrison);
 			}
 			
 			setChanged("selectedPrisons");
 		}
 		
 		// Remove the the prison from any selectedPrison entries
 		Iterator<Prison> it = selectedPrisons.values().iterator();
 		while (it.hasNext()) {
 			if (it.next() == prison) {
 				it.remove();
 			}
 		}
 		setChanged("selectedPrisons");
 	}
 	
 	public Prison setSelectedPrison(String playerName, String prisonName) {
 		playerName = playerName.toLowerCase();
 		Prison prison = null;
 		
 		if (prisonName == null) {
 			selectedPrisons.remove(playerName.toLowerCase());
 			setChanged("selectedPrisons");
 		} else {
 			prison = prisons.get(prisonName.toLowerCase());
 			if (prison != null) {
 				selectedPrisons.put(playerName.toLowerCase(), prison);
 				setChanged("selectedPrisons");
 			}
 		}
 		
 		return prison;
 	}
 	
 	public Prison getSelectedPrison(String playerName) {
 		return selectedPrisons.get(playerName.toLowerCase());
 	}
 	
 	public Collection<Prison> getPrisons() {
 		return prisons.values();
 	}
 
 	@Override
 	public void onSave(boolean forceFullSave) {
 		if (forceFullSave) {
 			// For a full save first clean this node
 			configSet("", null);
 		}
 		
 		if (forceFullSave || isChanged("selectedPrisons")) {
 			// Because selected prisons is a HashMap its quicker to delete all lines
 			// in the configuration and write them in again
 			configSet("selected_prisons", null);
 			
 			// Write out all the selected prisons to the configuration
 			for (Entry<String, Prison> entry : selectedPrisons.entrySet()) {
 				String playerName = entry.getKey().toLowerCase();
 				String prisonName = entry.getValue().getName().toLowerCase();
 				configSet("selected_prisons." + playerName, prisonName);
 			}
 		}
 		
 		if (forceFullSave || isChanged("prisons")) {
 			// Tell all child prisons to save also
 			for (Prison prison : prisons.values()) {
 				prison.save(forceFullSave);
 			}
 		}
 		
 		if (forceFullSave || isChanged("latentInventoryChange")) {
 			configSet("latent_updates.inventory_change", null);
 			for (Entry<String, InventoryManager.Action> entry : latentInventoryChanges.entrySet()) {
 				configSet("latent_updates.inventory_change." + entry.getKey(), entry.getValue().toString());
 			}
 		}
 		
 		if (forceFullSave || isChanged("latentTeleports")) {
 			configSet("latent_updates.teleports", null);
 			for (Entry<String, Location> entry : latentTeleports.entrySet()) {
 				Config.setLocation(getConfigPrefix() + ".latent_updates.teleports." + entry.getKey(), entry.getValue());
 			}
 		}
 	}
 
 	@Override
 	public void onSetup() {
 		FileConfiguration config = Law.get().getPlugin().getConfig();
 		
 		// Get the prisons
 		ConfigurationSection prisons = config.getConfigurationSection(getConfigPrefix() + ".prisons");
 		if (prisons != null) {
 			for (String prisonName : prisons.getKeys(false)) {
 				prisonName = prisonName.toLowerCase();
 				if (hasPrison(prisonName)) {
 					MessageDispatcher.consoleWarning("Duplicate prison names defined for world \"" + getName() + "\" using first definition.");
 				} else if (Prison.validateName(prisonName) == false) {
 					MessageDispatcher.consoleWarning("Illegal prison name \"" + prisonName + "\" defined for world \"" + getName() + "\".");
 				} else {
 					Prison prison = new Prison(this, prisonName);
 					this.addPrison(prison);
 					prison.setup();
 				}
 			}
 		}
 		
 		// Get the selected prisons
 		ConfigurationSection activePrisons = config.getConfigurationSection(getConfigPrefix() + ".selected_prisons");
 		if (activePrisons != null) {
 			String prisonName;
 			for (String playerName : activePrisons.getKeys(false)) {
 				prisonName = config.getString(getConfigPrefix() + ".selected_prisons." + playerName);
 				this.setSelectedPrison(playerName, prisonName);
 			}
 		}
 		
 		// Get latent permission changes
 		ConfigurationSection latentInvnetoryChanges = config.getConfigurationSection(getConfigPrefix() + ".latent_updates.inventory_change");
 		if (latentInvnetoryChanges != null) {
 			String action;
 			for (String playerName : latentInvnetoryChanges.getKeys(false)) {
 				action = config.getString(getConfigPrefix() + "latent_updates.inventory_change." + playerName);
 				this.addLatentInventoryChange(playerName, InventoryManager.Action.valueOf(action));
 			}
 		}
 		
 		// Get latent teleports
 		ConfigurationSection latentTeleports = config.getConfigurationSection(getConfigPrefix() + ".latent_updates.teleports");
 		if (latentTeleports != null) {
 			Location location;
 			for (String playerName : latentTeleports.getKeys(false)) {
 				location = Config.getLocation(getConfigPrefix() + ".latent_updates.teleports." + playerName);
 				this.addLatentTeleport(playerName, location);
 			}
 		}
 	}
 	
 	public World getBukkitWorld() {
 		return this.bukkitWorld;
 	}
 
 	@Override
 	public boolean imprisonPlayer(String playerName) {
 		// A world cannot imprison a player automatically
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		if (hashCode == 0) {
 			hashCode = ("WORLD" + this.getName()).hashCode();
 		}
 		
 		return hashCode;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof LawWorld) {
 			LawWorld other = (LawWorld) obj;
 			return this.getName() == other.getName();
 		}
 		return false;
 	}
 	
 	public Map<String, Location> getLatentTeleports() {
 		return this.latentTeleports;
 	}
 	
 	public void addLatentTeleports(Map<String, Location> offlinePrisoners) {
 		for (Entry<String,Location> entry : offlinePrisoners.entrySet()) {
 			
 			this.addLatentTeleport(entry.getKey(), entry.getValue());
 		}
 	}
 	
 	public void addLatentTeleport(String playerName, Location location) {
 		this.latentTeleports.put(playerName.toLowerCase(), location);
 		setChanged("latentTeleports");
 	}
 	
 	public Location getLatentTeleport(String playerName) {
 		return this.latentTeleports.get(playerName);
 	}
 	
 	public Location removeLatentTeleport(String playerName) {
 		Location location = this.latentTeleports.remove(playerName.toLowerCase());
 		if (location != null) {
 			setChanged("latentTeleports");
 		}
 		return location;
 	}
 	
 	public boolean hasLatentTeleport(String playerName) {
 		return this.latentTeleports.containsKey(playerName.toLowerCase());
 	}
 	
 	
 	public void addLatentInventoryChange(String playerName, InventoryManager.Action action) {
 		this.latentInventoryChanges.put(playerName.toLowerCase(), action);
 		setChanged("latentInventoryChange");
 	}
 	
 	public InventoryManager.Action removeLatentInventoryChange(String playerName) {
		InventoryManager.Action removedInventoryChange = this.latentInventoryChanges.remove(playerName.toLowerCase());
 		if (removedInventoryChange != null) {
 			setChanged("latentInventoryChange");
 		}
 		return removedInventoryChange;
 	}
 	
 	public InventoryManager.Action getLatentInventoryChange(String playerName) {
		return this.latentInventoryChanges.get(playerName.toLowerCase());
 	}
 	
 	@Override
 	public boolean canDelete() {
 		// You can never delete a world
 		return false;
 	}
 }
