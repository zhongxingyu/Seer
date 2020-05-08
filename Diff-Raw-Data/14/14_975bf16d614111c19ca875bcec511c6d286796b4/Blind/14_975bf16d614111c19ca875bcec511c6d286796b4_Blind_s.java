 package com.TeamNovus.Supernaturals.Classes.Rogue.Common.Abilities;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.TeamNovus.Supernaturals.Player.Class.Ability;
 
 public class Blind extends Ability {
 	private Integer duration;
 	private Integer chance;
 
 	public Blind(String name, String desc, Integer amplifier, Integer duration, Integer chance) {
 		super(name, desc, amplifier);
 		
 		this.duration = duration;
 		this.chance = chance;
 	}
 	
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
 		if(new Random().nextInt(101) <= chance) {			
 			if(event.getDamager() instanceof LivingEntity) {
 				((LivingEntity) event.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, getAmplifier()));
 
 				if(event.getEntity() instanceof Player) {
					((Player) event.getEntity()).sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + "Enemy Poisioned!");
 				}
 				
 				if(event.getDamager() instanceof Player) {
					((Player) event.getDamager()).sendMessage(ChatColor.ITALIC + "" + ChatColor.RED + "You have been poisioned!");
 				}
 			}
 		}
 	}
 }
