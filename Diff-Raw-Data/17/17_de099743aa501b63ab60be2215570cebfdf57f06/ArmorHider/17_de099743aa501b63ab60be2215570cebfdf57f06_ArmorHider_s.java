 package com.github.snnappie.armorhider;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.snnappie.armorhider.commands.HideCommand;
 import com.github.snnappie.armorhider.listeners.DamageListener;
 import com.github.snnappie.armorhider.listeners.InventoryListener;
 import com.github.snnappie.armorhider.listeners.PlayerLeaveListener;
 
 /*
  * TODO clean up this class in it's entirety - it is very poorly structured
  */
 public class ArmorHider extends JavaPlugin {
 	
 	public static final String PERM_ALL = "armorhider.hideall";
 	public static final String PERM_HAT = "armorhider.hidehat";
 	public static final String PERM_CHEST = "armorhider.hidechest";
 	public static final String PERM_BOOTS = "armorhider.hideboots";
 	public static final String PERM_LEGS = "armorhider.hidelegs";
 	
 	public static final String PERM_OTHER = "armorhider.hideother";
 	public static final String PERM_ENCHANT = "armorhider.hideenchant";
 
 	private HashMap<Player, List<ItemStack>> hiddenArmor = new HashMap<Player, List<ItemStack>>();
 	private HashMap<Player, HashMap<ItemStack, Map<Enchantment, Integer>>> hiddenEnchantments = new HashMap<Player, HashMap<ItemStack, Map<Enchantment, Integer>>>();
 	
 	public boolean portalStickEnabled;
 	public void onEnable() {
 		portalStickEnabled = getServer().getPluginManager().getPlugin("PortalStick") != null;
 		getCommand("hidearmor").setExecutor(new HideCommand(this));
 		getCommand("showarmor").setExecutor(new HideCommand(this));
 		
 		getCommand("hideenchant").setExecutor(new HideCommand(this));
 		getCommand("showenchant").setExecutor(new HideCommand(this));
 		
 		getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
 		getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
 		getServer().getPluginManager().registerEvents(new DamageListener(this), this);
 	}
 	
 	public void onDisable() {
 		// when the server stops, force all players to show their armor
 		if (!hiddenArmor.isEmpty() || !hiddenEnchantments.isEmpty()) {
 			Player[] players = Bukkit.getOnlinePlayers();
 			for (Player player : players) {
 				if (isPlayerHidingArmor(player))
 					showArmor(player, ArmorPiece.ALL);
 				if (isPlayerHidingEnchantments(player))
 					showEnchantments(player, ArmorPiece.ALL);
 			}
 		}
 	}
 	
 	
 	/*
 	 * Hides the players armor
 	 */
 	public void hideArmor(Player player, ArmorPiece piece) {
 		
 		// reveal enchantments if player tries to hide armor
 		if (isPlayerHidingEnchantmentOnPiece(player, piece))
 			showEnchantments(player, piece);
 		else if (piece == ArmorPiece.ALL && isPlayerHidingEnchantments(player))
 			showEnchantments(player, ArmorPiece.ALL);
 		
 		
 		PlayerInventory inventory = player.getInventory();
 		ItemStack hat, chest, legs, boots;
 		ArrayList<ItemStack> armorSet = new ArrayList<ItemStack>();
 
 		hat = inventory.getHelmet();
 		chest = inventory.getChestplate();
 		legs = inventory.getLeggings();
 		boots = inventory.getBoots();
 
 		switch (piece) {
 		case ALL:
 			if (hat != null) {
 				armorSet.add(hat);
 				inventory.setHelmet(null);
 			}
 			if (chest != null) {
 				armorSet.add(chest);
 				inventory.setChestplate(null);
 			}
 			if (legs != null) {
 				armorSet.add(legs);
 				inventory.setLeggings(null);
 			}
 			if (boots != null) {
 				armorSet.add(boots);
 				inventory.setBoots(null);
 			}
 			break;
 		case BOOTS:
 			if (boots != null) {
 				armorSet.add(boots);
 				inventory.setBoots(null);
 			}
 			break;
 		case HAT:
 			if (hat != null) {
 				armorSet.add(hat);
 				inventory.setHelmet(null);
 			}
 			break;
 		case LEGS:
 			if (legs != null) {
 				armorSet.add(legs);
 				inventory.setLeggings(null);
 			}
 			break;
 		case CHEST:
 			if (chest != null) {
 				armorSet.add(chest);
 				inventory.setChestplate(null);
 			}
 			break;
 		}
 
 		addArmor(player, armorSet);
 	}
 
 	
 	
 	/*
 	 * reveals the players armor if it is hidden
 	 */
 	public void showArmor(Player player, ArmorPiece piece) {
 		if (!isPlayerHidingArmor(player))
 			return;
 		PlayerInventory inventory = player.getInventory();
 		List<ItemStack> armorSet = removeArmor(player, piece);
 		
 		for (ItemStack armorPiece : armorSet) {
 			switch (ArmorHider.getArmorType(armorPiece)) {
 			case BOOTS:
 				inventory.setBoots(armorPiece);
 				break;
 			case CHEST:
 				inventory.setChestplate(armorPiece);
 				break;
 			case HAT:
 				inventory.setHelmet(armorPiece);
 				break;
 			case LEGS:
 				inventory.setLeggings(armorPiece);
 				break;
 			case ALL:
 				break;
 			}
 		}
 	}
 	
 	
 	
 	// adds the armor for the given player to the hidden armor list
 	private void addArmor(Player player, List<ItemStack> armorSet) {
 		if (armorSet.isEmpty())
 			return;
 		if (hiddenArmor.containsKey(player)) {
 			// player is already hiding all of their armor
 			if (hiddenArmor.get(player).size() == 4)
 				return;
 			
 			// add the armor that isn't hidden
 			for (ItemStack piece : armorSet) {
 				if (!hiddenArmor.get(player).contains(piece))
 					hiddenArmor.get(player).add(piece);
 			}
 				
 		} else {
 			hiddenArmor.put(player, armorSet);
 		}
 	}
 	
 	
 	
 	// removes the armor from hidden armor for the given player
 	private List<ItemStack> removeArmor(Player player, ArmorPiece piece) {
 		List<ItemStack> ret = new ArrayList<ItemStack>();
 		if (hiddenArmor.get(player) == null)
 			return ret;
 		if (piece == ArmorPiece.ALL)
 			return hiddenArmor.remove(player);
 		
 		// check for the armor piece
 		for (int i = 0; i < hiddenArmor.get(player).size(); i++) {
 			if (getArmorType(hiddenArmor.get(player).get(i)) == piece) {
 				ret.add(hiddenArmor.get(player).remove(i));
 				break;
 			}
 		}
 		
 		if (hiddenArmor.get(player).isEmpty())
 			hiddenArmor.remove(player);
 		return ret;
 	}
 	
 	
 	
 	/*
 	 * Hides all the enchantments for the given piece of armor
 	 */
 	public void hideEnchantments(Player player, ArmorPiece piece) {
 		ItemStack hat, chest, legs, boots;
 		PlayerInventory inventory = player.getInventory();
 		
 		hat = inventory.getHelmet();
 		chest = inventory.getChestplate();
 		legs = inventory.getLeggings();
 		boots = inventory.getBoots();
 		
 		HashMap<ItemStack, Map<Enchantment, Integer>> enchantments = new HashMap<ItemStack, Map<Enchantment, Integer>>();
 		
 		switch (piece) {
 		case ALL:
 			if (hat != null && hat.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = hat.getEnchantments();
 				for (Enchantment e : hat.getEnchantments().keySet()) {
 					hat.removeEnchantment(e);
 				}
 				enchantments.put(hat, enchantment);
 			}
 
 			if (chest != null && chest.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = chest.getEnchantments();
 				
 				for (Enchantment e : chest.getEnchantments().keySet()) {
 					chest.removeEnchantment(e);
 				}
 				enchantments.put(chest, enchantment);
 			}
 
 			if (legs != null && legs.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = legs.getEnchantments();
 				for (Enchantment e : legs.getEnchantments().keySet()) {
 					legs.removeEnchantment(e);
 				}
 				enchantments.put(legs, enchantment);
 			}
 			if (boots != null && boots.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = boots.getEnchantments();
 				for (Enchantment e : boots.getEnchantments().keySet()) {
 					boots.removeEnchantment(e);
 				}
 				
 				enchantments.put(boots, enchantment);
 			}
 			break;
 		case BOOTS:
 			if (boots != null && boots.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = boots.getEnchantments();
 				for (Enchantment e : boots.getEnchantments().keySet()) {
 					boots.removeEnchantment(e);
 				}
 				enchantments.put(boots, enchantment);
 			}
 			break;
 		case HAT:
 			if (hat != null && hat.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = hat.getEnchantments();
 				for (Enchantment e : hat.getEnchantments().keySet()) {
 					hat.removeEnchantment(e);
 				}
 				enchantments.put(hat, enchantment);
 			}
 			break;
 		case LEGS:
 			if (legs != null && legs.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = legs.getEnchantments();
 				for (Enchantment e : legs.getEnchantments().keySet()) {
 					legs.removeEnchantment(e);
 				}
 				enchantments.put(legs, enchantment);
 			}
 			break;
 		case CHEST:
 			if (chest != null && chest.getEnchantments() != null) {
 				Map<Enchantment, Integer> enchantment = chest.getEnchantments();
 				for (Enchantment e : chest.getEnchantments().keySet()) {
 					chest.removeEnchantment(e);
 				}
 				enchantments.put(chest, enchantment);
 			}
 			break;
 		} // end switch
 		
 		addEnchantments(player, enchantments);
 	}
 	
 	// adds the enchantments to the hashmap
 	private void addEnchantments(Player player, HashMap<ItemStack, Map<Enchantment, Integer>> enchantments) {
 		
 		if (enchantments.isEmpty())
 			return;
 		
 		// player is hiding some enchantments already
 		if (hiddenEnchantments.containsKey(player)) {
 			if (hiddenEnchantments.get(player).size() == 4)
 				return;
 			
 			Iterator<Map.Entry<ItemStack, Map<Enchantment, Integer>>> iter = enchantments.entrySet().iterator();
 			while (iter.hasNext()) {
 				Map.Entry<ItemStack, Map<Enchantment, Integer>> entry = iter.next();
 				ItemStack armorPiece = entry.getKey();
 				Map<Enchantment, Integer> enchant = entry.getValue();
 				if (!hiddenEnchantments.get(player).containsKey(armorPiece)) {
 					hiddenEnchantments.get(player).put(armorPiece, enchant);
 				}
 			}
 			
 		} else {
 			hiddenEnchantments.put(player, enchantments);
 		}
 	}
 	
 	
 	/*
 	 * Show the players hidden enchantments
 	 */
 	public void showEnchantments(Player player, ArmorPiece piece) {
 		// player is not hiding enchantments
 		if (!isPlayerHidingEnchantments(player))
 			return;
 		
 		PlayerInventory inventory = player.getInventory();
 		Map<ItemStack, Map<Enchantment, Integer>> enchantments = removeEnchantments(player, piece);
 		for (Map.Entry<ItemStack, Map<Enchantment, Integer>> entry : enchantments.entrySet()) {
 			ItemStack armorPiece = entry.getKey();
 			Map<Enchantment, Integer> enchantment = entry.getValue();
 			switch (getArmorType(armorPiece)) {
 			case BOOTS:
 				ItemStack boots = inventory.getBoots();
 				boots.addEnchantments(enchantment);
 				inventory.setBoots(boots);
 				break;
 			case CHEST:
 				ItemStack chest = inventory.getChestplate();
 				chest.addEnchantments(enchantment);
 				inventory.setChestplate(chest);
 				break;
 			case HAT:
 				ItemStack hat = inventory.getHelmet();
 				hat.addEnchantments(enchantment);
 				inventory.setHelmet(hat);
 				break;
 			case LEGS:
 				ItemStack legs = inventory.getLeggings();
 				legs.addEnchantments(enchantment);
 				inventory.setLeggings(legs);
 				break;
 			case ALL:
 				break;
 			}
 		}
 	}
 	
 	
 	// removes the hidden enchantments from the hashmap
 	private Map<ItemStack, Map<Enchantment, Integer>> removeEnchantments(Player player, ArmorPiece piece) {
 		Map<ItemStack, Map<Enchantment, Integer>> ret = new HashMap<ItemStack, Map<Enchantment, Integer>>();
 		if (!isPlayerHidingEnchantments(player))
 			return ret;
 		if (piece == ArmorPiece.ALL)
 			return hiddenEnchantments.remove(player);
 		
 		// check if the player is hiding the enchantment they are trying to show
 		Iterator<Map.Entry<ItemStack, Map<Enchantment, Integer>>> iter = hiddenEnchantments.get(player).entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry<ItemStack, Map<Enchantment, Integer>> entry = iter.next();
 			ItemStack armorPiece = entry.getKey();
 			Map<Enchantment, Integer> enchantment = entry.getValue();
 			if (getArmorType(armorPiece) == piece) {
 				ret.put(armorPiece, enchantment);
 				iter.remove();
 				break; // no need to iterate over the rest after we found it
 			}
 		}
 		
 		if (hiddenEnchantments.get(player).isEmpty())
 			hiddenEnchantments.remove(player);
 		
 		return ret;
 	}
 	
 	public boolean isPlayerHidingArmor(Player player) {
 		return hiddenArmor.containsKey(player);
 	}
 	
 	public boolean isPlayerHidingEnchantments(Player player) {
 		return hiddenEnchantments.containsKey(player);
 	}
 	
 	public boolean isPlayerHidingArmorPiece(Player player, ArmorPiece piece) {
 		if (!isPlayerHidingArmor(player))
 			return false;
 		for (ItemStack armorPiece : hiddenArmor.get(player)) {
 			if (getArmorType(armorPiece) == piece)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isPlayerHidingEnchantmentOnPiece(Player player, ArmorPiece piece) {
 		
 		if (!isPlayerHidingEnchantments(player))
 			return false;
 		for (ItemStack armorPiece : hiddenEnchantments.get(player).keySet()) {
 			if (getArmorType(armorPiece) == piece)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	// returns null if none
 	public Map<Enchantment, Integer> getHiddenEnchantment(Player player, ItemStack piece) {
 		return hiddenEnchantments.get(player).get(piece);
 	}
 	
 	// returns the enumerated type for the given piece of armor
 	public static ArmorPiece getArmorType(ItemStack armor) {
 		switch (armor.getType()) {
 		case LEATHER_HELMET:
 		case GOLD_HELMET:
 		case CHAINMAIL_HELMET:
 		case IRON_HELMET:
 		case DIAMOND_HELMET:
 			return ArmorPiece.HAT;
 		case LEATHER_LEGGINGS:
 		case GOLD_LEGGINGS:
 		case CHAINMAIL_LEGGINGS:
 		case IRON_LEGGINGS:
 		case DIAMOND_LEGGINGS:
 			return ArmorPiece.LEGS;
 		case LEATHER_CHESTPLATE:
 		case GOLD_CHESTPLATE:
 		case CHAINMAIL_CHESTPLATE:
 		case IRON_CHESTPLATE:
 		case DIAMOND_CHESTPLATE:
 			return ArmorPiece.CHEST;
 		case LEATHER_BOOTS:
 		case GOLD_BOOTS:
 		case CHAINMAIL_BOOTS:
 		case IRON_BOOTS:
 		case DIAMOND_BOOTS:
 			return ArmorPiece.BOOTS;
 			default:
 				return null;
 		}
 	}
 	
 	
 	// enumerated type of each piece of armor, including all of them
 	public static enum ArmorPiece {
 		HAT, CHEST, LEGS, BOOTS, ALL;
 	}
 }
