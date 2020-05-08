 package com.ainast.morepowerfulmobsreloaded;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.egordm.simpleattributes.API.SimpleAttributesAPI;
 import me.egordm.simpleattributes.Attributes.AttributeType;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 public class TieredItems {
	public ItemStack getTier1DentedPlateMail(){
 		/*
 		 * Includes:
 		 * 		Resistance to Physical: 3-7%
 		 * 		Max HP: 3-27
 		 * 		Knockback Resistance: 0.1-0.3%
 		 * 		Mana Regeneration: -3
 		 * 		Slow 1 Effect
 		 */	
 		ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
 		
 		int percent = 0;
 		
 		percent = MPMTools.generator.nextInt(3-1)+1;  //divide by 100 on next line to get percentage.
 		item = SimpleAttributesAPI.addItemAttribute(item, "Dented Plate Mail" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100);
 		
 		item.getItemMeta().setDisplayName("Dented Plate Mail");
 		List<String> lore = new ArrayList<String>();
 		
 		percent = MPMTools.generator.nextInt(7-5)+5;
 		lore.add("Resistance to Physical: " + percent);
 		
 		percent = MPMTools.generator.nextInt(27-3)+3;
 		lore.add("Maximum HEALTH: " + percent);
 		
 		lore.add("MANA Regeneration: -3");
 		
 		lore.add("SLOW I");
 		
 		item.getItemMeta().setLore(lore);
 		return item;
 	}
 }
