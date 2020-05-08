package io.github.alshain01.FlagsVehicle;
 
 import io.github.alshain01.Flags.Flag;
 import io.github.alshain01.Flags.Flags;
 import io.github.alshain01.Flags.ModuleYML;
 import io.github.alshain01.Flags.area.Area;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Flags - Vehicle Module that adds vehicle flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsVehicle extends JavaPlugin {
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable() {
 		final PluginManager pm = Bukkit.getServer().getPluginManager();
 
 		if (!pm.isPluginEnabled("Flags")) {
 			getLogger().severe("Flags was not found. Shutting down.");
 			pm.disablePlugin(this);
 		}
 
 		// Connect to the data file and register the flags
 		Flags.getRegistrar().register(new ModuleYML(this, "flags.yml"), "Vehicle");
 
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager()
 				.registerEvents(new VehicleListener(), this);
 	}
 	
 	/*
 	 * The event handlers for the flags we created earlier
 	 */
 	private class VehicleListener implements Listener {
 
 		private boolean isDenied(Player player, Flag flag, Area area) {
 			if (player.hasPermission(flag.getBypassPermission())) {
 				return false;
 			}
 
 			if (area.getTrustList(flag).contains(player.getName().toLowerCase())) {
 				return false;
 			}
 
 			if (!area.getValue(flag, false)) {
 				player.sendMessage(area.getMessage(flag, player.getName()));
 				return true;
 			}
 			return false;
 		}
 
 		/*
 		 * Handler for Vehicle Creation
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerInteract(PlayerInteractEvent e) {
 			if(e.getItem() == null) {
 				return;
 			}
 			if (e.getItem().getType() == Material.BOAT) {
 				e.setCancelled(isDenied(e.getPlayer(), Flags.getRegistrar()
 						.getFlag("PlaceBoat"), Area.getAt(e.getClickedBlock().getLocation())));
 
 			} else if (e.getItem().getType() == Material.MINECART) {
 				e.setCancelled(isDenied(e.getPlayer(), Flags.getRegistrar()
 						.getFlag("PlaceMinecart"), Area.getAt(e.getClickedBlock().getLocation())));
 
 			}
 		}
 
 		/*
 		 * Handler for Vehicle Damage
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onVehicleDamage(VehicleDamageEvent e) {
 			if (e.getAttacker() instanceof Player) {
 				return;
 			}
 			
 			Location location = e.getVehicle().getLocation();
 			if (e.getVehicle() instanceof Boat) {
 				e.setCancelled(!Area.getAt(location).getValue(Flags.getRegistrar().getFlag("BoatDamage"), false));
 
 			} else if (e.getVehicle() instanceof Minecart) {
 				e.setCancelled(!Area.getAt(location).getValue(Flags.getRegistrar().getFlag("MinecartDamage"), false));
 
 			} else if (Flags.checkAPI("1.6.2")
 					&& e.getVehicle() instanceof Horse
 					&& ((Horse) e.getVehicle()).isTamed()) {
 				e.setCancelled(!Area.getAt(location).getValue(Flags.getRegistrar().getFlag("TamedHorseDamage"), false));
 
 			} else if (e.getVehicle() instanceof Pig
 					&& ((Pig) e.getVehicle()).hasSaddle()) {
 				e.setCancelled(!Area.getAt(location).getValue(Flags.getRegistrar().getFlag("SaddledPigDamage"), false));
 			}
 		}
 	}
 
 	/*
 	 * Handler for Saddling animals
 	 * 
 	 * @EventHandler(ignoreCancelled = true) private void
 	 * onPlayerInteractEntity(PlayerInteractEntityEvent e) {
 	 * if(e.getPlayer().getItemInHand().getType() != Material.SADDLE) {
 	 * return; }
 	 * 
 	 * if(e.getRightClicked() instanceof Pig) {
 	 * 
 	 * e.setCancelled(isDenied(e.getPlayer(),
 	 * Flags.getRegistrar().getFlag("SaddlePig"),
 	 * Director.getAreaAt(e.getRightClicked().getLocation())));
 	 * 
 	 * } else if (Flags.checkAPI("1.6.2") && e.getRightClicked() instanceof
 	 * org.bukkit.entity.Horse) {
 	 * 
 	 * e.setCancelled(isDenied(e.getPlayer(),
 	 * Flags.getRegistrar().getFlag("SaddleHorse"),
 	 * Director.getAreaAt(e.getRightClicked().getLocation())));
 	 * 
 	 * } }
 	 */
 }
