 package com.sleaker.combatevents;
 
 import java.util.List;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 

 @SuppressWarnings("serial")
public class EntityKilledByEntityEvent extends Event{
 	private LivingEntity attacker;
 	private List<ItemStack> drops;
 	private LivingEntity killed;
 
 	public EntityKilledByEntityEvent(LivingEntity attacker, LivingEntity killed, List<ItemStack> drops) {
 		super ("EntityKilledByEntityEvent");
 		this.attacker = attacker;
 		this.killed = killed;
 		this.drops = drops;	
 	}
 
 	public LivingEntity getAttacker() {
 		return attacker;
 	}
 
 	public LivingEntity getKilled() {
 		return killed;
 	}
 
 	public List<ItemStack> getDrops() {
 		return drops;
 	}
 
 	public void setDrops(List<ItemStack> drops) {
 		this.drops = drops;
 	}
 
 }
