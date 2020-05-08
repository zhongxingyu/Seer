 package de.dustplanet.silkspawnersecoaddon;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
 import de.dustplanet.util.SilkUtil;
 
 public class SilkSpawnersEcoAddonListener implements Listener {
 	private SilkSpawnersEcoAddon plugin;
 	private SilkUtil su;
 
 	public SilkSpawnersEcoAddonListener(SilkSpawnersEcoAddon instance) {
 		plugin = instance;
 		su = SilkUtil.hookIntoSilkSpanwers();
 	}
 
 	@EventHandler
 	public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent event) {
 		// Get information
 		Player player = event.getPlayer();
 		short entityID = event.getEntityID();
 		String name = su.getCreatureName(entityID).toLowerCase().replaceAll(" ", "");
 		double price = plugin.defaultPrice;
 		// Is a specific price listed, yes get it!
 		if (plugin.config.contains(name)) price = plugin.getConfig().getDouble(name);
 		// If price is 0 or player has free perm, stop here!
 		if (price <= 0 || player.hasPermission("silkspawners.free")) return;
 		if (plugin.economy.has(player.getName(), price)) {
 			plugin.economy.withdrawPlayer(player.getName(), price);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("afforded")).replace("%money%", Double.toString(price)));
 		}
 		else {
 			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("cantAfford")));
 			event.setCancelled(true);
 		}
 	}
 }
