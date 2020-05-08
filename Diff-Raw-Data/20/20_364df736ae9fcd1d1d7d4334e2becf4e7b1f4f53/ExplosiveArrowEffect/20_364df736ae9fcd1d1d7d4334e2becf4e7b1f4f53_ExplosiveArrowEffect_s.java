 package com.ayan4m1.multiarrow.arrows;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 
 public class ExplosiveArrowEffect implements CustomArrowEffect {
 
 	@Override
 	public void hitEntity(Arrow arrow, Entity target) {
		arrow.getWorld().createExplosion(target.getLocation(), 2.0F);
 	}
 
 	@Override
 	public void hitGround(Arrow arrow) {
		arrow.getWorld().createExplosion(arrow.getLocation(), 2.5F);
 	}
 }
