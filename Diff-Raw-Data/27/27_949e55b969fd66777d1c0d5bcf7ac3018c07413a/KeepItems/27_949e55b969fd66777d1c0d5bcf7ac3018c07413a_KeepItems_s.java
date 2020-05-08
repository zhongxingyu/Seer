 package net.robinjam.bukkit.keepitems;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * The main plugin class.
  * 
  * @author robinjam
  */
 public class KeepItems extends JavaPlugin implements Listener {
 	
 	private Random random = new Random();
 	
 	@Override
 	public void onEnable() {
 		// Load config.yml
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		// Register events and permissions
 		getServer().getPluginManager().registerEvents(this, this);
 		registerPermissions();
 	}
 	
 	/**
 	 * Registers the additional dynamic permissions required by this plugin, which cannot be included in the plugin.yml file.
 	 */
 	public void registerPermissions() {
 		Map<String, Boolean> children = new HashMap<String, Boolean>();
 		
 		// Register keep-items.cause.<type> for each damage cause
 		for (DamageCause cause : DamageCause.values()) {
			Permission p = new Permission("keep-items.cause." + cause.name().toLowerCase(), "Allows the user to keep their items and experience when they are killed by " + cause.name().toLowerCase(), PermissionDefault.FALSE);
 			getServer().getPluginManager().addPermission(p);
 			children.put(p.getName(), true);
 		}
 		
 		// Register keep-items.cause.*
 		getServer().getPluginManager().addPermission(new Permission("keep-items.cause.*", "Allows the player to keep their items and experience when they die for any reason", PermissionDefault.TRUE, children));
 		
 		children.clear();
 		
 		// Register keep-items.item.<id> for each item type
 		for (Material type : Material.values()) {
			Permission p = new Permission("keep-items.item." + type.getId(), "Allows the user to keep " + type.toString(), PermissionDefault.FALSE);
 			getServer().getPluginManager().addPermission(p);
 			children.put(p.getName(), true);
 		}
 		
 		// Register keep-items.item.*
 		getServer().getPluginManager().addPermission(new Permission("keep-items.item.*", "Allows the player to keep any type of item", PermissionDefault.TRUE, children));
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerDeath(final PlayerDeathEvent event) {
 		final Player player = event.getEntity();
 		
 		// Check if the player has permission for this death cause
 		EntityDamageEvent e = player.getLastDamageCause();
 		if (e == null) {
 			System.err.println("[KeepItems] Player " + player.getName() + " died due to an unknown cause. It is therefore impossible to determine whether or not they have permission to keep their items. Their items and experience will be dropped at their death location (" + formatLocation(player.getLocation()) + ").");
 			return;
 		}
 		if (!player.hasPermission("keep-items.cause." + e.getCause().name().toLowerCase()))
 			return;
 		
 		// Experience
 		if (player.hasPermission("keep-items.level")) {
 			if (player.hasPermission("keep-items.progress"))
 				event.setKeepLevel(true);
 			else
 				event.setNewLevel(player.getLevel());
 			
 			event.setDroppedExp(0);
 		}
 		
 		// Armour
 		if (player.hasPermission("keep-items.armor")) {
 			final ItemStack[] armor = player.getInventory().getArmorContents();
 			
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
 				@Override
 				public void run() {
 					player.getInventory().setArmorContents(armor);
 				}
 				
 			});
 			
 			for (ItemStack is : armor) {
 				event.getDrops().remove(is);
 			}
 		}
 		
 		// Items
 		final ItemStack[] inventory = player.getInventory().getContents();
 		for (int i = 0; i < inventory.length; i++) {
 			ItemStack is = inventory[i];
 			
 			if (is != null && player.hasPermission("keep-items.item." + is.getTypeId()) && random.nextDouble() > getConfig().getDouble("drop-chance"))
 				event.getDrops().remove(is);
 			else
 				inventory[i] = null;
 		}
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
 			@Override
 			public void run() {
 				player.getInventory().setContents(inventory);
 			}
 			
 		});
 	}
 	
 	/**
 	 * Creates a formatted string representing a Location.
 	 * 
 	 * @param location The location to format.
 	 * @return A string of the format world@x,y,z.
 	 */
 	private String formatLocation(Location location) {
 		return location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
 	}
 	
 }
