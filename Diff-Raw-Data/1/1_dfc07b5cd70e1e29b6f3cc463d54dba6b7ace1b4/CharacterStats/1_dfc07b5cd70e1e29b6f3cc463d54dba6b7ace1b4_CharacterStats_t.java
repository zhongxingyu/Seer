 package com.gildorymrp.gildorym.stats;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class CharacterStats {
 
 	private RaceStats Race;
 	private ClassStats Class;
 	private WeaponStats Weapon;
 	private ArmorStats Armor;
 	
 	double meleeAttack = 0.0;
 	double meleeDefence = 0.0;
 	double rangedAttack = 0.0;
 	double rangedDefence = 0.0;
 	double magicAttack = 0.0;
 	double magicDefence = 0.0;
 	double fortitude = 0.0;
 	double reflex = 0.0;
 	double will = 0.0;
 	ItemStack weapon;
 	
 	public double getMeleeAttack(Player player){
 		weapon = player.getInventory().getItemInHand();
 		
 		meleeAttack += Class.getMeleeAttack(player);
 		meleeAttack += Race.getMeleeAttack(player);
 	if (weapon != null)
 		meleeAttack += Weapon.getMeleeAttack(weapon);
 		
 		return meleeAttack;
 	}
 	
 	public double getMeleeDefence(Player player){
 		
 		meleeDefence += Class.getMeleeDefence(player);
 		meleeDefence += Race.getMeleeDefence(player);
 		meleeDefence += Armor.getArmorMelee(player);
 		return meleeDefence;
 	}
 	
 	public double getRangedAttack(Player player){
 		weapon = player.getInventory().getItemInHand();
 		
 		rangedAttack += Class.getRangedAttack(player);
 		rangedAttack += Race.getRangedDefence(player);
 		if (weapon != null)
 		rangedAttack += Weapon.getRangedAttack(weapon);
 				
 		return rangedAttack;
 	}
 	
 	public double getRangedDefence(Player player){
 		
 		rangedDefence += Class.getRangedDefence(player);
 		rangedDefence += Race.getRangedDefence(player);
 		rangedDefence += Armor.getArmorRanged(player);
 		
 		return rangedDefence;
 	}
 	
 	public double getMagicAttack(Player player){
 		weapon = player.getInventory().getItemInHand();
 		
 		magicAttack += Class.getMagicAttack(player);
 		magicAttack += Race.getMagicAttack(player);
 		if (weapon != null)
 		magicAttack += Weapon.getMagicAttack(weapon);
 		
 		return magicAttack;
 	}
 	
 	public double getMagicDefence(Player player){
 		
 		magicDefence += Class.getMagicDefence(player);
 		magicDefence += Race.getMagicDefence(player);
 		
 		return magicDefence;
 	}
 	
 	public double getFortitude(Player player){
 	
 		fortitude += Class.getFortitude(player);
 		fortitude += Race.getFortitude(player);
 		
 		return fortitude;
 	}
 	
 	public double getReflex(Player player){
 		
 		reflex += Class.getReflex(player);
 		reflex += Race.getReflex(player);
 		
 		return reflex;
 	}
 	
 	public double getWill(Player player){
 		
 		will += Class.getWill(player);
 		will += Race.getWill(player);
 		
 		return will;
 	}
 }
