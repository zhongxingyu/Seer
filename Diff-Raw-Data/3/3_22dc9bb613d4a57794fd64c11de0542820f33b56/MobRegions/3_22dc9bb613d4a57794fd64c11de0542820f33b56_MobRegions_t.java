 package com.ainast.morepowerfulmobsreloaded;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.egordm.simpleattributes.API.SimpleAttributesAPI;
 import me.egordm.simpleattributes.Attributes.AttributeType;
 
import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.PotionEffectType;
 import org.shininet.bukkit.playerheads.Tools;
 
 import com.herocraftonline.heroes.characters.Monster;
 
 public class MobRegions {
	
 
 	public static void region1(LivingEntity entity){
 		
 		if (entity.getType().equals(EntityType.ZOMBIE)){
 			Monster m = MPMTools.getHeroes().getCharacterManager().getMonster(entity);
 			m.setDamage(m.getDamage()*3);
 			
 			m.setExperience(1000);
 			
 			entity.setCustomName("Frank");
 			entity.setCustomNameVisible(true);
 			
 			
 			ItemStack rose = new ItemStack(Material.DIAMOND_SWORD);
             
 			ItemMeta itemMeta = rose.getItemMeta();
             itemMeta.setDisplayName("Flash, Aha!");
             
     		List<String> theList = new ArrayList<String>();
 			theList.add("Mana Regeneration:50");
 			theList.add("Health Regeneration:-50");
 			itemMeta.setLore(theList);
             
             rose.setItemMeta(itemMeta);
            
             entity.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(999999,0),true);
             
             rose = SimpleAttributesAPI.addItemAttribute(rose, "Flash, AHAAH" ,  AttributeType.GENERIC_ATTACK_DAMAGE, 100);
             rose = SimpleAttributesAPI.addItemAttribute(rose, "Flash, AHAAH", AttributeType.GENERIC_MOVEMENT_SPEED, .2);	
            
             entity.getEquipment().setItemInHand(rose);
             entity.getEquipment().setItemInHandDropChance(100); 
 
             ItemStack helmet = Tools.Skull("GoldenNitro");
             ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
             
             chest = SimpleAttributesAPI.addItemAttribute(chest, "Fucker", AttributeType.GENERIC_MAX_HEALTH, 100);
             
             entity.getEquipment().setChestplate(chest);
             entity.getEquipment().setChestplateDropChance(100);
             entity.getEquipment().setHelmet(helmet);
             entity.getEquipment().setHelmetDropChance(100);
 		}
 	}
 }
 
