 package com.olympuspvp.squads;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 
 
 public class PVPListener implements Listener{
 
 	olySquads squad;
 	
 	public PVPListener(olySquads s){
 		squad = s;
 		Bukkit.getPluginManager().registerEvents(this, s);
 	}
 	
 	@EventHandler(priority=EventPriority.HIGH)
 	public void onPlayerDamage(EntityDamageByEntityEvent e){
 		Entity entDamaged = e.getEntity();
 		Entity entDamager = e.getDamager();
 		if(entDamaged instanceof Player == false) return;
 		Player pDamaged = (Player) entDamaged;
 		Squad damagedSquad = squad.getPlayerSquad(pDamaged.getName());
 		Player pDamager = null;
 		if(entDamager instanceof Player){
 			pDamager = (Player) entDamager;
 		}else if(entDamager instanceof Projectile){
 			LivingEntity shooter = ((Projectile)entDamager).getShooter();
 			if(shooter instanceof Player) pDamager = (Player) shooter;
 		}if(pDamager == null) return;
		if(damagedSquad == null) return;
 		if(damagedSquad.contains(pDamager.getName())){
 			e.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onWolfTarget(EntityTargetEvent e){
 		Entity targeter = e.getEntity();
 		Entity target = e.getTarget();
 		if(targeter instanceof Wolf == false) return;
 		Wolf wTargeter = (Wolf) targeter;
 		if(!wTargeter.isTamed()) return;
 		if(target instanceof Player == false) return;
 		Player pTarget = (Player) target;
 		Squad targetSquad = squad.getPlayerSquad(pTarget.getName());
 		if(targetSquad == null) return;
 		Player p = (Player) wTargeter.getOwner();
 		if(p == null) return;
 		if(targetSquad.contains(p.getName())){
 			e.setCancelled(true);
 		}
 	}
 	
 }
