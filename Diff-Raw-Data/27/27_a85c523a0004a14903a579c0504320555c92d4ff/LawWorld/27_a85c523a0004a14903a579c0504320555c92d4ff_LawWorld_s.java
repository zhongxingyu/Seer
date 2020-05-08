 package net.marcuswhybrow.minecraft.law;
 
 import java.util.HashMap;
 
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 public class LawWorld {
 	private World bukkitWorld;
 	private HashMap<String, Prison> prisons;
 	private HashMap<Player, Prison> selectedPrisons;
 	
 	public LawWorld(World bukkitWorld) {
 		this.bukkitWorld = bukkitWorld;
 		prisons = new HashMap<String, Prison>();
 		selectedPrisons = new HashMap<Player , Prison>();
 	}
 	
 	public String getName() {
 		return this.bukkitWorld.getName();
 	}
 	
 	public Prison createPrison(String name) throws PrisonAlreadyExistsException, IllegalNameException {
 		if (prisons.containsKey(name)) {
 			throw new PrisonAlreadyExistsException();
 		} else {
 			if (name.matches("[a-zA-Z0-9_-]+")) {
 				Prison prison = new Prison(this, name);
				prisons.put(prison.getName(), prison);
 				return prison;
 			} else {
 				throw new IllegalNameException();
 			}
 		}
 	}
 	
 	public Prison getPrison(String name) {
 		return prisons.get(name);
 	}
 	
 	public Prison getOrCreatePrison(String name) {
 		Prison prison = prisons.get(name);
 		if (prison == null) {
 			prison = new Prison(this, name);
 			this.prisons.put(prison.getName(), prison);
 		}
 		return prison;
 	}
 	
 	public void addPrison(Prison prison) {
 		prisons.put(prison.getName(), prison);
 	}
 	
 	public Prison removePrison(String name) {
 		return prisons.remove(name);
 	}
 	
 	public Prison setSelectedPrison(Player player, String name) { 
 		return setSelectedPrison(player, name, true);
 	}
 	
 	public Prison setSelectedPrison(Player player, String name, Boolean save) {
 		Prison prison = prisons.get(name);
 		if (prison != null) {
 			selectedPrisons.put(player, prison);
 			
 			if (save) {
 				// Save the selected prison
 				Plugin plugin = Law.get().getPlugin();
 				FileConfiguration config = plugin.getConfig();
 				config.set("worlds." + this.getName() + ".active_prisons." + player.getDisplayName(), prison.getName());
 				plugin.saveConfig();
 			}
 		}
 		return prison;
 	}
 	
 	public Prison getSelectedPrison(Player player) {
 		return selectedPrisons.get(player);
 	}
 	
 	public Prison[] getPrisons() {
 		return prisons.values().toArray(new Prison[prisons.size()]);
 	}
 }
