 package alshain01.FlagsVehicle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
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
 
 import alshain01.Flags.Director;
 import alshain01.Flags.Flag;
 import alshain01.Flags.Flags;
 import alshain01.Flags.ModuleYML;
 import alshain01.Flags.Registrar;
 import alshain01.Flags.area.Area;
 
 /**
  * Flags - Vehicle Module that adds vehicle flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsVehicle extends JavaPlugin {
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
 			if (e.getItem().getType() == Material.BOAT) {
 				Flags.Debug("Boat Material In Hand.");
 				e.setCancelled(isDenied(e.getPlayer(), Flags.getRegistrar()
 						.getFlag("PlaceBoat"), Director.getAreaAt(e.getClickedBlock().getLocation())));
 
 			} else if (e.getItem().getType() == Material.MINECART) {
 				Flags.Debug("Minecart Material In Hand.");
 				e.setCancelled(isDenied(e.getPlayer(), Flags.getRegistrar()
 						.getFlag("PlaceMinecart"), Director.getAreaAt(e.getClickedBlock().getLocation())));
 
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
 				e.setCancelled(!Director
 						.getAreaAt(location).getValue(Flags.getRegistrar().getFlag("BoatDamage"), false));
 
 			} else if (e.getVehicle() instanceof Minecart) {
 				e.setCancelled(!Director.getAreaAt(location).getValue(Flags.getRegistrar().getFlag("MinecartDamage"), false));
 
 			} else if (Flags.checkAPI("1.6.2")
 					&& e.getVehicle() instanceof Horse
 					&& ((Horse) e.getVehicle()).isTamed()) {
 				e.setCancelled(!Director.getAreaAt(location).getValue(Flags.getRegistrar().getFlag("TamedHorseDamage"), false));
 
 			} else if (e.getVehicle() instanceof Pig
 					&& ((Pig) e.getVehicle()).hasSaddle()) {
 				e.setCancelled(!Director.getAreaAt(location).getValue(Flags.getRegistrar().getFlag("SaddledPigDamage"), false));
 			}
 		}
 	}
 
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
 
 		// Connect to the data file
 		final ModuleYML dataFile = new ModuleYML(this, "flags.yml");
 
 		// Register with Flags
 		final Registrar flags = Flags.getRegistrar();
 		for (final String f : dataFile.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
 			final ConfigurationSection data = dataFile.getModuleData().getConfigurationSection("Flag." + f);
 
 			// We don't want to register flags that aren't supported.
 			// It would just muck up the help menu.
 			// Null value is assumed to support all versions.
 			final String api = data.getString("MinimumAPI");
 			if (api != null && !Flags.checkAPI(api)) {
 				continue;
 			}
 
 			// The description that appears when using help commands.
 			final String desc = data.getString("Description");
 
 			final boolean def = data.getBoolean("Default");
 
 			final boolean isPlayer = data.getBoolean("Player");
 
 			// The default message players get while in the area.
 			final String area = data.getString("AreaMessage");
 
 			// The default message players get while in an world.
 			final String world = data.getString("WorldMessage");
 
 			// Register it!
 			// Be sure to send a plug-in name or group description for the help
 			// command!
 			// It can be this.getName() or another string.
 			if (isPlayer) {
 				flags.register(f, desc, def, "Vehicle", area, world);
 			} else {
 				flags.register(f, desc, def, "Vehicle");
 			}
 		}
 
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager()
 				.registerEvents(new VehicleListener(), this);
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
