 package com.TeamNovus.Supernaturals.Classes.Brute;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 import com.TeamNovus.Supernaturals.Classes.Brute.Common.Abilities.Block;
import com.TeamNovus.Supernaturals.Classes.Brute.Common.Abilities.Spikes;
 import com.TeamNovus.Supernaturals.Classes.Brute.Common.Powers.Earthquake;
 import com.TeamNovus.Supernaturals.Classes.Brute.Common.Powers.Explode;
 import com.TeamNovus.Supernaturals.Classes.Brute.Common.Powers.Roar;
 import com.TeamNovus.Supernaturals.Models.ItemBag;
 import com.TeamNovus.Supernaturals.Models.Reagent;
 import com.TeamNovus.Supernaturals.Player.SNClass;
 
 public class Brute extends SNClass {
 
 	public Brute(SNClass parentClass) {
 		super("Brute", ChatColor.DARK_RED, 30, parentClass);
 		
 		addPower(1, new Roar("Roar", "Scare nearby mobs!", 60, 32, 20, 
 						new Reagent(10, new ItemBag(new ItemStack(Material.SULPHUR, 5))), 
 						new Reagent(10, new ItemBag(new ItemStack(Material.SULPHUR, 5)))));
 		addPower(10, new Explode("Explode", "Trigger explosions all around!", 60, 
 						new Reagent(25, new ItemBag(new ItemStack(Material.SULPHUR, 10))), 
 						new Reagent(25, new ItemBag(new ItemStack(Material.SULPHUR, 10)))));
 		addPower(30, new Earthquake("Earthquake", "Cause the ground to shake with fury!", 120, 
 						new Reagent(40, new ItemBag(new ItemStack(Material.TNT, 5), new ItemStack(Material.SULPHUR, 10))), 
 						new Reagent(40, new ItemBag(new ItemStack(Material.TNT, 5), new ItemStack(Material.SULPHUR, 10)))));
 		
 		addAbility(5, new Block("Block", "Small chance to block attacks!", 0, 2));
		addAbility(25, new Spikes("Spikes", "Small chance to inflict damage to attackers!", 0, 2, 3));
 		
 		setMaxMana(1, 20);
 		setMaxMana(5, 25);
 		setMaxMana(10, 30);
 		setMaxMana(25, 25);
 		setMaxMana(30, 40);
 		
 		setMaxHealth(1, 30);
 		setMaxHealth(5, 33);
 		setMaxHealth(10, 35);
 		setMaxHealth(25, 37);
 		setMaxHealth(30, 40);
 		
 		setMaxFoodLevel(1, 20);
 		setMaxFoodLevel(5, 19);
 		setMaxFoodLevel(10, 18);
 		setMaxFoodLevel(25, 17);
 		setMaxFoodLevel(30, 16);
 		
 		setSpeed(1, 0.17f);
 		setSpeed(5, 0.16f);
 		setSpeed(10, 0.15f);
 		setSpeed(25, 0.14f);
 		setSpeed(30, 0.13f);
 	}
 
 }
