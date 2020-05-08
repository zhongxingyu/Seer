 package com.ayan4m1.multiarrow.arrows;
 
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ExplosionPrimeEvent;
 
 public class ExplosiveArrowEffect implements CustomArrowEffect {
 
 	@Override
 	public void hitEntity(Arrow arrow, Entity target) {
		this.triggerExplosion(arrow, 2.0F);
 	}
 
 	@Override
 	public void hitGround(Arrow arrow) {
		this.triggerExplosion(arrow, 2.5F);
	}

	private void triggerExplosion(Arrow arrow, Float radius) {
		ExplosionPrimeEvent event = new ExplosionPrimeEvent(arrow, radius, false);
		arrow.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			Location loc = arrow.getLocation();
			((CraftWorld)arrow.getWorld()).getHandle().createExplosion(((CraftEntity)arrow).getHandle(), loc.getX(), loc.getY(), loc.getZ(), radius, false);
		}
 	}
 }
