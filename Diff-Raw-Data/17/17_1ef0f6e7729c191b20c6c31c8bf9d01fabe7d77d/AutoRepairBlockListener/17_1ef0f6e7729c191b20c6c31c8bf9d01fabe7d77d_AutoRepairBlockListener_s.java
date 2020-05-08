 package com.mrockey28.bukkit.ItemRepair;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * testing block listener
  * @author lostaris, mrockey28
  */
 public class AutoRepairBlockListener implements Listener {
 	private final AutoRepairPlugin plugin;
 	public AutoRepairSupport support;
 	public Repair repair;
 
 	public AutoRepairBlockListener(final AutoRepairPlugin plugin) {
 		this.plugin = plugin;
 		this.support = new AutoRepairSupport(plugin, null);
 		this.repair = new Repair(plugin);
 	}	
 
 /////
 //EVENT HANDLERS
 /////
 	
 	@EventHandler
 	public void onBlockDamage(BlockDamageEvent event) {		
 		eventAffectsItemInHand(event.getPlayer());
 	}
 
 	@EventHandler
 	//Need to check item in hand for damager when something or someone is damaged
 	//as the damager could be using a sword or other breakable item
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		
 		//Make sure damager is a player
 		if (event.getDamager().getType() != EntityType.PLAYER) {
 			return;
 		}
 		
 		//This is safe because we already ejected if EntityType is not player
 		Player player = (Player) (event.getDamager());
 		
 		eventAffectsItemInHand(player);
 	}
 	
 	@EventHandler
 	//This is only needed so bows autorepair
 	public void onEntityShootBow(EntityShootBowEvent event) {
 		
 		//If something else shoots a bow besides a player
 		if (event.getEntityType() != EntityType.PLAYER) {
 			return;
 		}
 		//We already know the entityType is a player, so this is a safe conversion
 		Player player = (Player) event.getEntity();
 		
 		eventAffectsItemInHand(player);
 		
 	}
 	
 	@EventHandler
 	//If something damages the player, we need to check his armor
 	//TODO: Need to replace this overall check with check for armor-damage-only events
 	//These are as follows (from minecraft wiki):
 	//Direct attacks from mobs
 	//Direct attacks from other players
 	//Getting hit with an arrow
 	//Getting hit with a fireball from a Ghast or Blaze
 	//Touching a block of fire or lava
 	//Touching a cactus
 	//Explosions
 	public void onEntityDamage(EntityDamageEvent event) {
 		
 		//Make sure this is a player
 		if (event.getEntity().getType() != EntityType.PLAYER) {
 			return;
 		}
 		//We already know the entityType is a player, so this is a safe conversion
 		Player player = (Player) event.getEntity();
 		
 		eventAffectsArmor(player);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	//If player changes held item, clear out the warning flags
 	public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
 		Player player = event.getPlayer();
 		this.support.setPlayer(player);
 		support.setWarning(false);
 		support.setLastWarning(false);
 	}
 	
 //////
 //NON-EVENT HANDLER FUNCTIONS
 /////
 	
 	//If we need to check the item in hand for breakage when
 	//certain events occur
 	private void eventAffectsItemInHand (Player player)
 	{
 		if (!AutoRepairPlugin.isAllowed(player, "access")) {
 			return;
 		}
 		this.support.setPlayer(player);
 
 		ItemStack toolHand = player.getItemInHand();
 		Short dmg = toolHand.getDurability();
 		if (dmg == 0){
 			return;
 		} else if (dmg ==1) {
 			support.setWarning(false);
 			support.setLastWarning(false);
 		}
 		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 		String itemName = Material.getMaterial(toolHand.getTypeId()).toString();
 		
 		int durability = 0;
 		if (durabilities.containsKey(itemName)) {
 			durability = durabilities.get(itemName);
 		}
 		else
 		{
 			return;
 		}
 		
		if (dmg > (durability -2) && AutoRepairPlugin.isAutoRepair()) {
 			repair.autoRepairTool(toolHand);
 		}
		//If the item is not enchanted, warn at 10% left
		else if (!support.isEnchanted(toolHand) && dmg > (durability - durability/10)) {
 			support.repairWarn(toolHand);
 		}
		//If the item IS enchanted, warn at 25% left
		else if (support.isEnchanted(toolHand) && (dmg > (durability - (durability/4)) || dmg > (durability - 10))){
 			support.repairWarn(toolHand);
 		}
 	}
 	
 	//If we need to check the item in hand for breakage when
 	//certain events occur
 	private void eventAffectsArmor (Player player)
 	{
 		if (!AutoRepairPlugin.isAllowed(player, "access")) {
 			return;
 		}
 		this.support.setPlayer(player);
 
 		ItemStack[] armorlist = player.getInventory().getArmorContents();
 		
 		for (ItemStack piece : armorlist) {
 			if (piece.getType() == Material.AIR) {
 				continue;
 			}
 			
 			Short dmg = piece.getDurability();
 			if (dmg == 0){
 				return;
 			} else if (dmg ==1) {
 				support.setWarning(false);
 				support.setLastWarning(false);
 			}
 			HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
 			String itemName = Material.getMaterial(piece.getTypeId()).toString();
 			
 			int durability = 0;
 			if (durabilities.containsKey(itemName)) {
 				durability = durabilities.get(itemName);
 			}
 			else
 			{
 				return;
 			}
 			
			if (dmg > (durability -2) && AutoRepairPlugin.isAutoRepair()) {
 				repair.autoRepairTool(piece);
 			} else if (durability <= 100 && dmg > (durability - 20)) {
 				support.repairWarn(piece);
 			} else if (durability > 100 && dmg > (durability - 100)) {
 				support.repairWarn(piece);
 			} 
 		}
 	}
 
 
 	public AutoRepairPlugin getPlugin() {
 		return plugin;
 	}
 }
 
