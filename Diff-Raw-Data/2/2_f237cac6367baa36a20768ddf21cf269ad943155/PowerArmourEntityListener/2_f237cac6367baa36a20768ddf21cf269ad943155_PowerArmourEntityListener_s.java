 package com.sargant.bukkit.powerarmour;
 
 import org.bukkit.Material;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.sargant.bukkit.powerarmour.PowerArmour;
 
 public class PowerArmourEntityListener extends EntityListener
 {
 	private PowerArmour parent;
 
 	public PowerArmourEntityListener(PowerArmour instance) {
 		parent = instance;
 	}
 
 	@Override
 	public void onEntityDamage(EntityDamageEvent event) {
 		
 		if(event.isCancelled()) {
 			return;
 		}
 
 		if(!(event.getEntity() instanceof HumanEntity)) {
 			return;
 		}
 
 		HumanEntity human = (HumanEntity) event.getEntity();
 
 		PowerArmourComponents loadout = new PowerArmourComponents();
 
 		loadout.head = human.getInventory().getHelmet().getType();
 		loadout.body = human.getInventory().getChestplate().getType();
 		loadout.legs = human.getInventory().getLeggings().getType();
 		loadout.feet = human.getInventory().getBoots().getType();
 		loadout.hand = human.getItemInHand().getType();
 		
 		parent.log.info(parent.armourList.keySet().toString());
 
 		for(PowerArmourAbilities a : parent.armourList.keySet()) {
 			
 			PowerArmourComponents c = parent.armourList.get(a);
 
 			if(c.head != Material.AIR && loadout.head != c.head) { continue; }
 			if(c.body != Material.AIR && loadout.body != c.body) { continue; }
 			if(c.legs != Material.AIR && loadout.legs != c.legs) { continue; }
 			if(c.feet != Material.AIR && loadout.feet != c.feet) { continue; }
			if(c.hand != Material.AIR && loadout.hand != c.hand) { parent.log.info("failed on hand check - wanted " + c.hand + ", got" + loadout.hand); continue; }
 
 			// The loadout is correct for the current power ability
 			// Does the current damage type match a corresponding proof-ness?
 			if(a.getAbilities().contains(event.getCause())) {
 				
 				event.setCancelled(true);
 				if(event.getCause().toString().toUpperCase().contains("FIRE"))
 				{
 					human.setFireTicks(0);
 				}
 				return;
 			}
 		}
 	}
 }
