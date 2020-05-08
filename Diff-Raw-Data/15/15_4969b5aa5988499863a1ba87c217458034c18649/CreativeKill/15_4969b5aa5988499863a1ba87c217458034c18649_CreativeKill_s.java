 package com.LRFLEW.bukkit.ck;
 
import net.minecraft.server.DamageSource;

 import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftVehicle;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CreativeKill extends JavaPlugin implements Listener {
 	
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void entityDamageEntity(EntityDamageByEntityEvent e) {
 		if (!(e.getDamager() instanceof Player)) return;
 		Player player = (Player)e.getDamager();
 		if (!player.getGameMode().equals(GameMode.CREATIVE)) return;
 		if (!player.hasPermission("creativekill")) return;
		LivingEntity le = (LivingEntity)e.getEntity();
		if (le instanceof Player) return;
		le.setHealth(0);
		((CraftLivingEntity)le).getHandle().die(DamageSource.OUT_OF_WORLD);
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void vehicleDamage(VehicleDamageEvent e) {
 		if (!(e.getAttacker() instanceof Player)) return;
 		Player player = (Player)e.getAttacker();
 		if (!player.getGameMode().equals(GameMode.CREATIVE)) return;
 		if (!player.hasPermission("creativekill")) return;
		((CraftVehicle)e.getVehicle()).getHandle().die();
 	}
 }
