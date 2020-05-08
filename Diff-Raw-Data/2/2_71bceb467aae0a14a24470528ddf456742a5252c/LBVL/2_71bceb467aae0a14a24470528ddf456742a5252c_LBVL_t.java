 package me.KillerSmurf.LavaBoat;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftBoat;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Player;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.event.vehicle.VehicleListener;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.util.Vector;
 
 public class LBVL extends VehicleListener {
 	LavaBoat lb;
 	
 	public LBVL(LavaBoat instance) {
 		lb = instance;
 	}
 	
 	public void onVehicleDamage(VehicleDamageEvent event){
 		if (!(event.getVehicle() instanceof CraftBoat)) return;
 		if (event.getAttacker() instanceof Player) return;
 		event.setCancelled(true);
 		event.getVehicle().setFireTicks(0);
 		if (lb.BOATISINVINCIBLE) return;
 		lb.BOATISINVINCIBLE = true;
 	}
 	
 	public void onVehicleEnter(VehicleEnterEvent event) {
 		System.out.print(event.getVehicle());
 		if (!(event.getVehicle() instanceof CraftBoat)) return;
 		if (!(event.getEntered() instanceof Player)) return;
 		if (lb.BOATISINVINCIBLE) {
 			lb.ISINBOAT = true;
 		}
 	}
 	
 	public void onVehicleExit(final VehicleExitEvent event) {
 		if (!(event.getVehicle() instanceof CraftBoat)) return;
 		if (!(event.getExited() instanceof Player)) return;
 		if (lb.BOATISINVINCIBLE) {
 			Runnable runnable = new Runnable() { 
 				public void run() {
 					lb.ISINBOAT = false;
 					event.getExited().setFireTicks(0);
 				}
 			};
 			lb.getServer().getScheduler().scheduleSyncDelayedTask(lb, runnable, 5*20);
 		}
 	}
 	public void onVehicleMove(VehicleMoveEvent e)
 	{
 		
 		if(e.getVehicle() instanceof Boat)
 		{
 			Vector vect=e.getVehicle().getVelocity();
 			int y=0;
 			Material mat=e.getVehicle().getLocation().getWorld().getBlockAt(e.getVehicle().getLocation()).getType();
 			if(mat==Material.LAVA||mat==Material.STATIONARY_LAVA)
 			{
 				y=1;
 			}
			//lb.getServer().broadcastMessage("boat is moving"); this was used for testing
 			e.getVehicle().setVelocity(new Vector(vect.getX(),y,vect.getZ()));
 		}
 	}
 }
